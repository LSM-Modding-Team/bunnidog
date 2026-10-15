#!/usr/bin/env python3
"""W3: deterministic guava mechanisms using checked-in Minecraft 26.2 oak templates and guava plank art."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
PARTS = ['door', 'trapdoor', 'button', 'pressure_plate', 'fence_gate']


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')


def rewrite(value):
    text = json.dumps(value)
    for prefix in ['minecraft:block/oak_', 'minecraft:item/oak_', 'minecraft:oak_', 'minecraft:blocks/oak_']:
        text = text.replace(prefix, prefix.replace('minecraft:', 'bunnidogs:').replace('oak_', 'guava_'))
    return json.loads(text)


def main():
    template = json.loads((ROOT / 'art/wood_interactives_templates.json').read_text())
    for path, value in template['files'].items():
        path = path.replace('assets/minecraft/', 'assets/bunnidogs/').replace('data/minecraft/', 'data/bunnidogs/').replace('oak_', 'guava_')
        value = rewrite(value)
        if '/models/block/guava_door' in path or '/models/block/guava_trapdoor' in path:
            value['render_type'] = 'minecraft:cutout'
        write(RES / path, value)
    patterns = {
        'door': (['PP','PP','PP'], 3), 'trapdoor': (['PPP','PPP'],2),
        'button': (['P'],1), 'pressure_plate': (['PP'],1), 'fence_gate': (['SPS','SPS'],1)
    }
    for part, (pattern, count) in patterns.items():
        key = {'P': 'bunnidogs:guava_planks'}
        if part == 'fence_gate': key['S'] = 'minecraft:stick'
        write(RES / f'data/bunnidogs/recipe/guava_{part}.json', {'type':'minecraft:crafting_shaped', 'pattern':pattern,
              'key':key,'result':{'id':f'bunnidogs:guava_{part}','count':count}})
    tags = {'door':'wooden_doors','trapdoor':'wooden_trapdoors','button':'wooden_buttons',
            'pressure_plate':'wooden_pressure_plates','fence_gate':'fence_gates'}
    for domain in ['block','item']:
        for part, tag in tags.items():
            path = RES / f'data/minecraft/tags/{domain}/{tag}.json'
            value = json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
            item = f'bunnidogs:guava_{part}'
            if item not in value['values']: value['values'].append(item)
            write(path,value)
    path = RES / 'data/minecraft/tags/block/mineable/axe.json'
    value = json.loads(path.read_text())
    for part in PARTS:
        item = f'bunnidogs:guava_{part}'
        if item not in value['values']: value['values'].append(item)
    write(path,value)
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text())
        for part in PARTS: value[f'block.bunnidogs.guava_{part}'] = 'Guava ' + part.replace('_',' ').title()
        write(path,value)

    textures = RES / 'assets/bunnidogs/textures/block'
    planks = Image.open(textures / 'guava_planks.png').convert('RGBA')
    bottom = planks.copy()
    d = ImageDraw.Draw(bottom)
    d.rectangle((2,2,13,13),outline=(111,59,42,255))
    d.rectangle((12,2,13,3),fill=(218,169,70,255))
    top = planks.copy()
    d = ImageDraw.Draw(top)
    d.rectangle((3,3,12,13),fill=(0,0,0,0),outline=(105,58,39,255))
    d.line((7,3,7,13),fill=(145,83,56,255),width=2)
    d.line((3,8,12,8),fill=(145,83,56,255),width=2)
    trapdoor = planks.copy()
    d = ImageDraw.Draw(trapdoor)
    for x in [3,9]:
        for y in [3,9]: d.rectangle((x,y,x+3,y+3),fill=(0,0,0,0))
    for y in [2,12]: d.rectangle((0,y,2,y+1),fill=(181,132,55,255))
    for name, img in [('guava_door_bottom',bottom),('guava_door_top',top),('guava_trapdoor',trapdoor)]: img.save(textures / f'{name}.png')
    door = Image.new('RGBA',(16,32))
    door.alpha_composite(top,(0,0)); door.alpha_composite(bottom,(0,16))
    icon = Image.new('RGBA',(16,16))
    icon.alpha_composite(door.resize((8,16),Image.Resampling.NEAREST),(4,0))
    icon.save(RES / 'assets/bunnidogs/textures/item/guava_door.png')
    preview = Image.new('RGBA',(520,440),(48,45,44,255))
    preview.alpha_composite(door.resize((160,320),Image.Resampling.NEAREST),(24,52))
    preview.alpha_composite(trapdoor.resize((192,192),Image.Resampling.NEAREST),(252,52))
    draw = ImageDraw.Draw(preview)
    draw.text((24,24),'Guava door',fill='white'); draw.text((252,24),'Guava trapdoor',fill='white')
    draw.text((24,404),'Button, pressure plate and gate use the existing guava planks.',fill='white')
    preview.save(ROOT / 'art/guava_interactives_preview.png')
    print('Generated W3 blockstates, models, items, loot, recipes, tags and guava door/trapdoor art.')


if __name__ == '__main__': main()
