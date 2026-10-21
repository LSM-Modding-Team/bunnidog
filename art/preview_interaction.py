#!/usr/bin/env python3
"""Preview native interaction snapshots over the approved sculpt; substitute font, not a client capture."""
import argparse
import json
import re
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from preview_bunnidog import ROOT, render, world_faces


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('log', type=Path)
    args = parser.parse_args()
    views = {}
    for line in args.log.read_text().splitlines():
        if 'CARE_PREVIEW ' in line:
            value = json.loads(line.split('CARE_PREVIEW ', 1)[1])
            views[value['name']] = value
    root = Path(ROOT)
    font = ImageFont.truetype('DejaVuSans.ttf', 9)
    bold = ImageFont.truetype('DejaVuSans-Bold.ttf', 10)
    texture = np.asarray(Image.open(root/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'), dtype=float)
    scene = render(world_faces(), texture, -35, 8, 330, 330, 17, background_rgb=(75, 91, 68))
    canvas = Image.new('RGB', (1200, 760), (28, 32, 27))
    draw = ImageDraw.Draw(canvas)
    draw.text((12, 8), 'Interaction layout | native server snapshots | substitute font; not a client capture', font=bold, fill='white')
    for row, locale in enumerate(('en_us', 'es_pe')):
        messages = json.loads((root/f'src/main/resources/assets/bunnidogs/lang/{locale}.json').read_text())

        def text(value):
            if isinstance(value, str):
                return value
            if isinstance(value, list):
                return ''.join(map(text, value))
            if 'text' in value:
                result = value['text']
            else:
                result = messages.get(value.get('translate', ''), value.get('fallback', value.get('translate', '')))
                arguments = value.get('with', [])
                index = [0]

                def sub(match):
                    at = int(match[1])-1 if match[1] else index[0]
                    if not match[1]:
                        index[0] += 1
                    return text(arguments[at]) if at < len(arguments) else match[0]

                result = re.sub(r'%(?:(\d+)\$)?s', sub, result).replace('%%', '%')
            return result+''.join(text(part) for part in value.get('extra', []))

        for column, name in enumerate(('First contact', 'Care without tools')):
            view = views[name]
            frame = Image.new('RGB', (600, 355), (75, 91, 68))
            frame.paste(scene, (6, 20))
            painter = ImageDraw.Draw(frame)
            painter.text((12, 12), locale+' / '+name, font=bold, fill=(245, 238, 211))
            x, y, width = 374, 50, 214
            height = 70+27*len(view['controls'])+(16 if text(view['next']) else 0)
            painter.rectangle((x-1, y-1, x+width, y+height), fill=(34, 28, 22), outline=(225, 199, 144))
            painter.text((x+12, y+10), text(view['title']), font=bold, fill=(244, 231, 201))
            at = y+30
            if text(view['next']):
                painter.text((x+12, at), text(view['next']), font=font, fill=(230, 225, 214))
                at += 16
            for button in view['controls']:
                painter.rectangle((x+12, at, x+189, at+19), fill=(79, 76, 70) if button['enabled'] else (48, 46, 42), outline=(135, 130, 116))
                label = text(button['label'])
                painter.text((x+101-font.getlength(label)/2, at+4), label, font=font,
                             fill='white' if button['enabled'] else (136, 132, 123))
                at += 27
            painter.rectangle((x+12, y+height-28, x+width-12, y+height-9), fill=(79, 76, 70), outline=(135, 130, 116))
            done = 'Done' if locale == 'en_us' else 'Listo'
            painter.text((x+width/2-font.getlength(done)/2, y+height-24), done, font=font, fill='white')
            canvas.paste(frame, (column*600, 35+row*360))
    output = root/'art/interaction_preview.png'
    canvas.save(output)
    print(output)


if __name__ == '__main__':
    main()
