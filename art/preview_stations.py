#!/usr/bin/env python3
"""Review compact station layout from native snapshots; substitute font, not client screenshots."""
import argparse
import json
import re
import zipfile
from io import BytesIO
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


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('log', type=Path)
    args = parser.parse_args()
    views = {}
    for line in args.log.read_text().splitlines():
        if 'CARE_PREVIEW ' in line:
            view = json.loads(line.split('CARE_PREVIEW ', 1)[1])
            views[view['name']] = view
    names = ['Food and garden work', 'Resident work choices', 'Juicer inputs', 'Genetics Table', 'Blocked nursery', 'Commission progress']
    canvas = Image.new('RGB', (1200, 1430), (25, 29, 25))
    draw = ImageDraw.Draw(canvas)
    draw.text((12, 8), 'Station layout | native stock/actions | substitute font and background; not client screenshots', font=BOLD, fill='white')
    for row, locale in enumerate(['en_us', 'es_pe']):
        text, messages = translator(locale)
        for column, name in enumerate(names):
            frame = Image.new('RGB', (400, 350), (48, 58, 43))
            d = ImageDraw.Draw(frame)
            d.text((8, 4), locale+' / '+name, font=FONT, fill=(190, 199, 182))
            view = views[name]
            x, y, width, height = 50, 25, 300, 310
            title = wrap(text(view['title']), width-24)
            at = y+10
            for part in title:
                d.text((x+12, at), part, font=BOLD, fill=(244, 231, 201))
                at += 9
            at += 8
            # Draw into a clipped viewport, matching the screen's scrolling body.
            body = Image.new('RGB', (288, 1200), (48, 58, 43))
            painter, by = ImageDraw.Draw(body), 0
            stocks = view.get('stock', [])
            if stocks:
                sw = (264-8*(len(stocks)-1))//len(stocks)
                for i, stock in enumerate(stocks):
                    sx = i*(sw+8)
                    for line_no, part in enumerate(wrap(text(stock['label']), sw)[:2]):
                        painter.text((sx+(sw-FONT.getlength(part))/2, 2+line_no*9), part, font=FONT, fill=(224, 214, 193))
                    if stock['count']:
                        image = icon(stock['item'])
                        if image:
                            body.paste(image.resize((16, 16), Image.Resampling.NEAREST), (sx+(sw-18)//2+1, 31), image.resize((16, 16), Image.Resampling.NEAREST))
                        if stock['count']>1:
                            painter.text((sx+(sw-18)//2+12, 41), str(stock['count']), font=FONT, fill='white', stroke_width=1, stroke_fill='black')
                    else:
                        painter.text((sx+(sw-18)//2+4, 34), '—', font=FONT, fill=(176, 171, 161))
                by += 61
            for value in [view['next']]+view['rows']:
                parts = wrap(text(value), 264)
                for part in parts:
                    painter.text((0, by), part, font=FONT, fill=(230, 226, 217))
                    by += 9
                if parts:
                    by += 7
            back = next((b for b in view['controls'] if b['action'] in [18, 151, 182, 142]), None)
            for button in sorted(view['controls'], key=lambda b: b['section']):
                if button is back:
                    continue
                painter.rectangle((0, by, 263, by+19), fill=(79, 76, 70) if button['enabled'] else (48, 46, 42), outline=(135, 130, 116))
                label = text(button['label'])
                painter.text(((264-FONT.getlength(label))/2, by+4), label, font=FONT, fill='white' if button['enabled'] else (136, 132, 123))
                by += 27
            visible = height-len(title)*9-54
            frame.paste(body.crop((0, 0, 288, visible)), (x+12, at))
            if by>visible:
                d.rectangle((x+width-8, at, x+width-5, at+visible-1), fill=(100, 100, 92))
                d.rectangle((x+width-8, at, x+width-5, at+max(12, visible*visible//by)), fill=(191, 190, 177))
            footer_width = (width-32)//2
            for offset, label in [(12, '← '+messages['interaction.bunnidogs.back'] if back else None), (20+footer_width, messages['interaction.bunnidogs.close'])]:
                if label:
                    d.rectangle((x+offset, y+height-28, x+offset+footer_width-1, y+height-9), fill=(79, 76, 70), outline=(135, 130, 116))
                    d.text((x+offset+(footer_width-FONT.getlength(label))/2, y+height-24), label, font=FONT, fill='white')
            canvas.paste(frame, ((column%3)*400, 30+(row*2+column//3)*350))
    output = ROOT/'art/station_interactions_preview.png'
    canvas.save(output)
    print(output)


if __name__ == '__main__':
    main()
