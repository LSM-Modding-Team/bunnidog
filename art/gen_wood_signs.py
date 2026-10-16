"""Generate W4a from checked-in Minecraft 26.2 templates and the guava palette.

Native block-model parents preserve all 16 rotations and hanging attachments.
The six reference PNGs preserve their UV layout, transparency and iron chains;
only wood pixels adopt the approved guava-plank palette. No game JAR is needed
to regenerate the shipped assets.
"""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
TEXTURES = RES / 'assets/bunnidogs/textures'


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')


def rewrite(value):
    text = json.dumps(value)
    for prefix in ['minecraft:block/oak_', 'minecraft:block/stripped_oak_', 'minecraft:item/oak_', 'minecraft:oak_', 'minecraft:stripped_oak_']:
        text = text.replace(prefix, prefix.replace('minecraft:', 'bunnidogs:').replace('oak_', 'guava_'))
    return json.loads(text)


def tag(domain, name, *ids):
    path = RES / f'data/minecraft/tags/{domain}/{name}.json'
    value = json.loads(path.read_text()) if path.exists() else {'replace': False, 'values': []}
    for ident in ids:
        if ident not in value['values']:
            value['values'].append(ident)
    write(path, value)


def wood(pixel):
    r, g, b, a = pixel
    return a > 0 and r > b * 1.2 and g > b * 1.05


def main():
    templates = json.loads((ROOT / 'art/wood_sign_templates.json').read_text())
    for path, value in templates['files'].items():
        path = path.replace('/minecraft/', '/bunnidogs/').replace('oak_', 'guava_')
        value = rewrite(value)
        if '/models/block/' in path:
            value['render_type'] = 'minecraft:cutout'
        write(RES / path, value)

    for part, family in [('sign', 'standing_signs'), ('wall_sign', 'wall_signs'),
                         ('hanging_sign', 'ceiling_hanging_signs'), ('wall_hanging_sign', 'wall_hanging_signs')]:
        ident = f'bunnidogs:guava_{part}'
        tag('block', family, ident)
        tag('block', 'mineable/axe', ident)
    tag('item', 'signs', 'bunnidogs:guava_sign')
    tag('item', 'hanging_signs', 'bunnidogs:guava_hanging_sign')

    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text())
        for part, label in [('sign', 'Guava Sign'), ('wall_sign', 'Guava Sign'),
                            ('hanging_sign', 'Guava Hanging Sign'), ('wall_hanging_sign', 'Guava Hanging Sign')]:
            value[f'block.bunnidogs.guava_{part}'] = label
        write(path, value)

    planks = Image.open(TEXTURES / 'block/guava_planks.png').convert('RGBA')
    colors = list(planks.getdata())
    target = tuple(sum(p[c] for p in colors) / len(colors) for c in range(3))
    # The UV reference and guava source keep their grain/shading; iron remains neutral.
    reference = Image.open(ROOT / 'art/reference/sign_templates/block_oak_sign.png').convert('RGBA')
    oak = [p for p in reference.getdata() if wood(p)]
    source = tuple(sum(p[c] for p in oak) / len(oak) for c in range(3))
    paths = ['block/oak_sign', 'block/oak_hanging_sign', 'gui/signs/oak',
             'gui/hanging_signs/oak', 'item/oak_sign', 'item/oak_hanging_sign']
    preview = Image.new('RGBA', (900, 430), (48, 45, 44, 255))
    draw = ImageDraw.Draw(preview)
    for i, path in enumerate(paths):
        img = Image.open(ROOT / 'art/reference/sign_templates' / (path.replace('/', '_') + '.png')).convert('RGBA')
        pixels = []
        for p in img.getdata():
            pixels.append(tuple(min(255, round(p[c] * target[c] / source[c])) for c in range(3)) + (p[3],) if wood(p) else p)
        img.putdata(pixels)
        destination = TEXTURES / (path.replace('oak', 'guava') + '.png')
        destination.parent.mkdir(parents=True, exist_ok=True)
        img.save(destination)
        x, y = (i % 3) * 300 + 16, (i // 3) * 210 + 12
        draw.text((x, y), path.replace('oak', 'guava'), fill='white')
        preview.alpha_composite(img.resize((img.width * 5, img.height * 5), Image.Resampling.NEAREST), (x, y + 24))
    preview.save(ROOT / 'art/guava_signs_preview.png')
    print('Generated guava signs: all rotations, attachments, models, icons, editors, recipes, unlocks, loot and tags.')


if __name__ == '__main__':
    main()
