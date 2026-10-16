"""Generate guava boats from the native 26.2 UV templates and guava wood palette."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
from gen_wood_signs import rewrite, tag, write, wood

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
TEXTURES = RES / 'assets/bunnidogs/textures'
REFERENCE = ROOT / 'art/reference/boat_templates'


def main():
    for path, value in json.loads((ROOT / 'art/wood_boat_templates.json').read_text())['files'].items():
        write(RES / path.replace('/minecraft/', '/bunnidogs/').replace('oak_', 'guava_'), rewrite(value))
    tag('item', 'boats', 'bunnidogs:guava_boat')
    tag('item', 'chest_boats', 'bunnidogs:guava_chest_boat')
    tag('entity_type', 'boat', 'bunnidogs:guava_boat', 'bunnidogs:guava_chest_boat')
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text())
        for kind, label in [('boat', 'Guava Boat'), ('chest_boat', 'Guava Boat with Chest')]:
            value[f'item.bunnidogs.guava_{kind}'] = label
            value[f'entity.bunnidogs.guava_{kind}'] = label
        write(path, value)

    planks = Image.open(TEXTURES / 'block/guava_planks.png').convert('RGBA')
    pixels = list(planks.getdata())
    target = tuple(sum(p[c] for p in pixels) / len(pixels) for c in range(3))
    base = Image.open(REFERENCE / 'entity_boat_oak.png').convert('RGBA')
    pixels = [p for p in base.getdata() if wood(p)]
    source = tuple(sum(p[c] for p in pixels) / len(pixels) for c in range(3))
    icon = Image.open(REFERENCE / 'item_oak_boat.png').convert('RGBA')
    paths = ['entity/boat/oak', 'entity/chest_boat/oak', 'item/oak_boat', 'item/oak_chest_boat']
    preview = Image.new('RGBA', (800, 560), (48, 45, 44, 255))
    draw = ImageDraw.Draw(preview)
    for i, path in enumerate(paths):
        img = Image.open(REFERENCE / (path.replace('/', '_') + '.png')).convert('RGBA')
        for y in range(img.height):
            for x in range(img.width):
                p = img.getpixel((x, y))
                # Keep native chest panels/lock; only hull and paddles adopt guava wood.
                chest = (i == 1 and y >= 55) or (i == 3 and p != icon.getpixel((x, y)))
                if wood(p) and not chest:
                    img.putpixel((x, y), tuple(min(255, round(p[c] * target[c] / source[c])) for c in range(3)) + (p[3],))
        destination = TEXTURES / (path.replace('oak', 'guava') + '.png')
        destination.parent.mkdir(parents=True, exist_ok=True)
        img.save(destination)
        x, y, scale = (i % 2) * 400 + 12, (i // 2) * 280 + 12, 2 if i < 2 else 8
        draw.text((x, y), path.replace('oak', 'guava'), fill='white')
        preview.alpha_composite(img.resize((img.width * scale, img.height * scale), Image.Resampling.NEAREST), (x, y + 20))
    preview.save(ROOT / 'art/guava_boats_preview.png')
    print('Generated guava boat/chest boat atlases, icons, items, recipes, unlocks, tags and names.')


if __name__ == '__main__':
    main()
