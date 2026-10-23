#!/usr/bin/env python3
"""Review native interaction/readout snapshots; substitute font and sculpt, not client screenshots."""
import argparse
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from preview_bunnidog import ROOT, render, world_faces
from preview_stations import icon, translator, wrap

FONT = ImageFont.truetype('DejaVuSans.ttf', 9)
BOLD = ImageFont.truetype('DejaVuSans-Bold.ttf', 10)
BACK = {18, 151, 182, 142}


def frame(view, name, locale, scene):
    text, messages = translator(locale)
    result = Image.new('RGB', (600, 355), (75, 91, 68))
    result.paste(scene, (6, 20))
    animal = view.get('subjectId', -1) >= 0
    if not animal:
        result = Image.blend(result, Image.new('RGB', result.size, 'black'), 96/255)
    painter = ImageDraw.Draw(result)
    painter.text((12, 4), locale+' / '+name, font=BOLD, fill=(245, 238, 211))
    width, height = (214 if animal else 300), 310
    x, y = (600-width-12, 12) if animal else ((600-width)//2, (355-height)//2)
    title = wrap(text(view['title']), width-24)
    for i, part in enumerate(title):
        painter.text((x+12, y+10+i*9), part, font=BOLD, fill=(244, 231, 201), stroke_width=1, stroke_fill=(30, 30, 25))
    viewport_y, visible = y+len(title)*9+18, height-len(title)*9-54
    content = Image.new('RGBA', (width-24, 1600))
    draw, at, text_width = ImageDraw.Draw(content), 0, width-36

    def lines(value):
        nonlocal at
        parts = wrap(text(value), text_width)
        for part in parts:
            draw.text((0, at), part, font=FONT, fill=(230, 226, 217), stroke_width=1, stroke_fill=(30, 30, 25))
            at += 9
        if parts:
            at += 7

    def buttons(section):
        nonlocal at
        for button in view['controls']:
            if button['action'] in BACK or button['section'] != section:
                continue
            draw.rectangle((0, at, text_width-1, at+19), fill=(79, 76, 70) if button['enabled'] else (48, 46, 42), outline=(135, 130, 116))
            label = text(button['label'])
            draw.text(((text_width-FONT.getlength(label))/2, at+4), label, font=FONT, fill='white' if button['enabled'] else (136, 132, 123))
            at += 27

    stocks = view.get('stock', [])
    if stocks:
        columns = min(3, len(stocks))
        sw = (text_width-8*(columns-1))//columns
        for i, stock in enumerate(stocks):
            sx, sy = (i%columns)*(sw+8), (i//columns)*61
            for line_no, part in enumerate(wrap(text(stock['label']), sw)[:2]):
                draw.text((sx+(sw-FONT.getlength(part))/2, sy+2+line_no*9), part, font=FONT, fill=(224, 214, 193))
            sprite = icon(stock['item']) if stock['count'] else None
            if sprite:
                sprite = sprite.resize((16, 16), Image.Resampling.NEAREST)
                content.paste(sprite, (sx+(sw-18)//2+1, sy+31), sprite)
                if stock['count'] > 1:
                    draw.text((sx+(sw-18)//2+10, sy+40), str(stock['count']), font=FONT, fill='white', stroke_width=1, stroke_fill='black')
            else:
                draw.text((sx+(sw-18)//2+4, sy+34), '—', font=FONT, fill=(176, 171, 161))
        at += ((len(stocks)+columns-1)//columns)*61
    lines(view['next'])
    if animal:
        buttons(0)
    for row in view['rows']:
        lines(row)
    if not animal:
        buttons(0)
    buttons(1)
    buttons(2)
    clipped = content.crop((0, 0, width-24, visible))
    result.paste(clipped, (x+12, viewport_y), clipped)
    if at > visible:
        painter.rectangle((x+width-8, viewport_y, x+width-5, viewport_y+visible-1), fill=(100, 100, 92))
        painter.rectangle((x+width-8, viewport_y, x+width-5, viewport_y+max(12, visible*visible//at)), fill=(191, 190, 177))
    footer_width = (width-32)//2
    has_back = any(button['action'] in BACK for button in view['controls'])
    for offset, label in [(12, '← '+messages['interaction.bunnidogs.back'] if has_back else None), (20+footer_width, messages['interaction.bunnidogs.close'])]:
        if label:
            painter.rectangle((x+offset, y+height-28, x+offset+footer_width-1, y+height-9), fill=(79, 76, 70), outline=(135, 130, 116))
            painter.text((x+offset+(footer_width-FONT.getlength(label))/2, y+height-24), label, font=FONT, fill='white')
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('log', type=Path)
    parser.add_argument('--views', nargs='+', default=['First contact', 'Care without tools', 'Guava choices', 'Packed cargo'])
    parser.add_argument('--output', type=Path, default=Path(ROOT)/'art/interaction_preview.png')
    args = parser.parse_args()
    views = {}
    for line in args.log.read_text().splitlines():
        if 'CARE_PREVIEW ' in line:
            view = json.loads(line.split('CARE_PREVIEW ', 1)[1])
            views[view['name']] = view
    texture = np.asarray(Image.open(Path(ROOT)/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'), dtype=float)
    scene = render(world_faces(), texture, -35, 8, 330, 330, 17, background_rgb=(75, 91, 68))
    canvas = Image.new('RGB', (600*len(args.views), 760), (28, 32, 27))
    ImageDraw.Draw(canvas).text((12, 8), 'Interaction layout | native snapshots | substitute font/sculpt/background; not client screenshots', font=BOLD, fill='white')
    for row, locale in enumerate(['en_us', 'es_pe']):
        for column, name in enumerate(args.views):
            canvas.paste(frame(views[name], name, locale, scene), (column*600, 35+row*360))
    canvas.save(args.output)
    print(args.output)


if __name__ == '__main__':
    main()
