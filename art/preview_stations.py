#!/usr/bin/env python3
"""Review compact station layout from native snapshots; substitute font, not client screenshots."""
import argparse
import json
import re
import zipfile
from io import BytesIO
from functools import lru_cache
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
FONT = ImageFont.truetype('DejaVuSans.ttf', 9)
BOLD = ImageFont.truetype('DejaVuSans-Bold.ttf', 10)


def translator(locale):
    messages = json.loads((ROOT/f'src/main/resources/assets/bunnidogs/lang/{locale}.json').read_text())

    def text(value):
        if isinstance(value, str):
            return value
        if isinstance(value, (float, int)):
            return str(value)
        if isinstance(value, list):
            return ''.join(map(text, value))
        if 'text' in value:
            result = value['text']
        else:
            result = messages.get(value.get('translate', ''), value.get('fallback', value.get('translate', '')))
            arguments, index = value.get('with', []), [0]

            def sub(match):
                at = int(match[1])-1 if match[1] else index[0]
                if not match[1]:
                    index[0] += 1
                return text(arguments[at]) if at < len(arguments) else match[0]

            result = re.sub(r'%(?:(\d+)\$)?s', sub, result).replace('%%', '%')
        return result+''.join(text(part) for part in value.get('extra', []))

    return text, messages


def wrap(text, width):
    lines = []
    for paragraph in text.splitlines():
        line = ''
        for word in paragraph.split():
            if line and FONT.getlength(line+' '+word) > width:
                lines.append(line)
                line = ''
            line = (line+' '+word).strip()
        if line:
            lines.append(line)
    return lines


def icon(item):
    namespace, name = item.split(':')
    if namespace == 'bunnidogs':
        path = ROOT/f'src/main/resources/assets/bunnidogs/textures/item/{name}.png'
        if path.exists():
            return Image.open(path).convert('RGBA')
    if namespace == 'minecraft':
        with zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar') as archive:
            path = f'assets/minecraft/textures/item/{name}.png'
            if path in archive.namelist():
                return Image.open(BytesIO(archive.read(path))).convert('RGBA')
    return None


PANEL_INK = (64, 64, 64)


@lru_cache(maxsize=1)
def container_texture():
    with zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar') as archive:
        return Image.open(BytesIO(archive.read('assets/minecraft/textures/gui/container/crafting_table.png'))).convert('RGBA')


def panel(target, x, y, width, height):
    source = container_texture()
    for dx, dy, w, h, u, v, sw, sh in [
        (4, 4, width-8, height-8, 4, 4, 1, 1),
        (0, 0, 4, 4, 0, 0, 4, 4), (4, 0, width-8, 4, 4, 0, 168, 4),
        (width-4, 0, 4, 4, 172, 0, 4, 4), (0, 4, 4, height-8, 0, 4, 4, 158),
        (width-4, 4, 4, height-8, 172, 4, 4, 158),
        (0, height-4, 4, 4, 0, 162, 4, 4), (4, height-4, width-8, 4, 4, 162, 168, 4),
        (width-4, height-4, 4, 4, 172, 162, 4, 4),
    ]:
        patch = source.crop((u, v, u+sw, v+sh)).resize((w, h), Image.Resampling.NEAREST)
        target.paste(patch, (x+dx, y+dy), patch)


def container_slot(target, x, y):
    source = container_texture().crop((7, 83, 25, 101))
    target.paste(source, (x-1, y-1), source)


def panel_color(value):
    named = {'green': (85, 255, 85), 'red': (255, 85, 85), 'gold': (255, 170, 0)}
    rgb = named.get(value.get('color')) if isinstance(value, dict) else None
    return tuple(int(c*96/max(rgb)) for c in rgb) if rgb else PANEL_INK


BACK = {18, 151, 182, 142}
SLOT_X = {'TROUGH': [80], 'PEN': [53, 107], 'CRADLE': [80], 'JUICER': [18, 76, 134], 'TABLE': [53, 107], 'JAR': [80], 'PEDESTAL': [80]}


def frame(view, name, locale, size):
    text, messages = translator(locale)
    screen_width, screen_height = size
    result = Image.new('RGB', (screen_width, screen_height+30), (28, 32, 27))
    painter = ImageDraw.Draw(result)
    painter.text((8, 6), locale+' / '+name, font=FONT, fill=(220, 224, 207))
    scene = Image.new('RGB', size, (48, 58, 43))
    draw = ImageDraw.Draw(scene)
    kind = view.get('station', {}).get('kind', 'NONE')
    family = kind != 'NONE'
    storage = family and view.get('page', 0)&255 == 0
    width = min(300, screen_width-(16 if family else 24))
    height = min(224 if family else 310, screen_height-(16 if family else 24))
    x, y = (screen_width-width)//2, (screen_height-height)//2
    panel(scene, x, y, width, height)
    title = wrap(text(view['title']), width-24)
    for i, part in enumerate(title):
        draw.text((x+12, y+10+i*9), part, font=BOLD, fill=PANEL_INK)

    def item(target, painter, stock, sx, sy):
        sprite = icon(stock['item']) if stock['count'] else None
        if sprite:
            sprite = sprite.resize((16, 16), Image.Resampling.NEAREST)
            target.paste(sprite, (sx, sy), sprite)
        if stock['count'] > 1:
            count = str(stock['count'])
            painter.text((sx+17-FONT.getlength(count), sy+8), count, font=FONT, fill='white', stroke_width=1, stroke_fill='black')

    stocks = view.get('stock', [])
    if storage:
        for slot in view['slots']:
            sx, sy = x+slot['x'], y+slot['y']
            container_slot(scene, sx, sy)
            item(scene, draw, slot, sx, sy)
        draw.text((x+8, y+93), 'Inventario' if locale == 'es_pe' else 'Inventory', font=FONT, fill=PANEL_INK)
        for i, sx in enumerate(SLOT_X[kind]):
            for line_no, part in enumerate(wrap(text(stocks[i]['label']), 54)[:2]):
                draw.text((x+sx+8-FONT.getlength(part)/2, y+20+line_no*9), part, font=FONT, fill=PANEL_INK)
        if kind == 'JUICER':
            draw.text((x+52, y+48), '+', font=FONT, fill=PANEL_INK)
            draw.text((x+110, y+48), '→', font=FONT, fill=PANEL_INK)
        progress = view['station']['progress']
        if progress >= 0:
            draw.rectangle((x+18, y+73, x+149, y+77), fill=(55, 55, 55))
            if progress:
                draw.rectangle((x+19, y+74, x+18+130*progress//100, y+76), fill=(140, 182, 106))
        if kind == 'TROUGH' and len(stocks) > 1:
            filter_stock = stocks[1]
            for i, part in enumerate(wrap(text(filter_stock['label']), 46)[:2]):
                draw.text((x+126+(46-FONT.getlength(part))/2, y+20+i*9), part, font=FONT, fill=PANEL_INK)
            if filter_stock['count']:
                item(scene, draw, filter_stock, x+141, y+49)
            else:
                draw.text((x+143, y+51), '—', font=FONT, fill=(96, 96, 96))

    text_width = 100 if storage else width-36
    content = Image.new('RGBA', (text_width+12, 1600))
    ink, at = ImageDraw.Draw(content), 0
    if stocks and not storage:
        columns = min(3, len(stocks))
        sw = (text_width-8*(columns-1))//columns
        for i, stock in enumerate(stocks):
            sx, sy = (i%columns)*(sw+8), (i//columns)*61
            for line_no, part in enumerate(wrap(text(stock['label']), sw)[:2]):
                ink.text((sx+(sw-FONT.getlength(part))/2, sy+2+line_no*9), part, font=FONT, fill=PANEL_INK)
            item(content, ink, stock, sx+(sw-16)//2, sy+31)
            if not stock['count']:
                ink.text((sx+(sw-18)//2+4, sy+34), '—', font=FONT, fill=(96, 96, 96))
        at += ((len(stocks)+columns-1)//columns)*61
    for value in [view['next']]+view['rows']:
        parts = wrap(text(value), text_width)
        for part in parts:
            ink.text((0, at), part, font=FONT, fill=panel_color(value))
            at += 9
        if parts:
            at += 7
    for button in sorted(view['controls'], key=lambda b: b['section']):
        if button['action'] in BACK:
            continue
        ink.rectangle((0, at, text_width-1, at+19), fill=(79, 76, 70) if button['enabled'] else (48, 46, 42), outline=(135, 130, 116))
        label = text(button['label'])
        ink.text(((text_width-FONT.getlength(label))/2, at+4), label, font=FONT, fill='white' if button['enabled'] else (136, 132, 123))
        at += 27
    at = max(0, at-7)  # LinearLayout has no spacing after its final child.
    visible = height-len(title)*9-54
    content_x, content_y = x+(188 if storage else 12), y+len(title)*9+18
    clipped = content.crop((0, 0, text_width+12, visible))
    scene.paste(clipped, (content_x, content_y), clipped)
    if at > visible:
        draw.rectangle((x+width-8, content_y, x+width-5, content_y+visible-1), fill=(100, 100, 92))
        draw.rectangle((x+width-8, content_y, x+width-5, content_y+max(12, visible*visible//at)), fill=(191, 190, 177))
    footer_width = 100 if family else (width-32)//2
    has_back = any(b['action'] in BACK for b in view['controls'])
    for offset, label in [(12, '← '+messages['interaction.bunnidogs.back'] if has_back else None), (188 if family else 20+footer_width, messages['interaction.bunnidogs.close'])]:
        if label:
            draw.rectangle((x+offset, y+height-28, x+offset+footer_width-1, y+height-9), fill=(79, 76, 70), outline=(135, 130, 116))
            draw.text((x+offset+(footer_width-FONT.getlength(label))/2, y+height-24), label, font=FONT, fill='white')
    result.paste(scene, (0, 30))
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('log', type=Path)
    parser.add_argument('--views', nargs='+', default=['Food and garden work', 'Resident work choices', 'Juicer inputs', 'Genetics Table', 'Blocked nursery', 'Commission progress'])
    parser.add_argument('--minimum', action='store_true', help='Use the 320×240 minimum Minecraft GUI viewport')
    parser.add_argument('--output', type=Path, default=ROOT/'art/station_interactions_preview.png')
    args = parser.parse_args()
    views = {}
    for line in args.log.read_text().splitlines():
        if 'CARE_PREVIEW ' in line:
            view = json.loads(line.split('CARE_PREVIEW ', 1)[1])
            views[view['name']] = view
    size = (320, 240) if args.minimum else (400, 340)
    cell_width, cell_height = size[0], size[1]+30
    rows = (len(args.views)+2)//3
    canvas = Image.new('RGB', (3*cell_width, 35+2*rows*cell_height), (25, 29, 25))
    ImageDraw.Draw(canvas).text((12, 8), 'Station inventory | native slots/actions | substitute font/background; not client screenshots', font=BOLD, fill='white')
    for row, locale in enumerate(['en_us', 'es_pe']):
        for column, name in enumerate(args.views):
            canvas.paste(frame(views[name], name, locale, size), ((column%3)*cell_width, 35+(row*rows+column//3)*cell_height))
    canvas.save(args.output)
    print(args.output)


if __name__ == '__main__':
    main()
