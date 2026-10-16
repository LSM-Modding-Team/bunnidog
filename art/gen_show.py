"""Deterministic Show Ring assets. Preview contains native-size tiles and model-box silhouette."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
from gen_syringe import write

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def box(a, b, texture, top=None):
    return {'from': a, 'to': b, 'faces': {face: {'texture': '#' + (top if face == 'up' and top else texture), 'uv': [0, 0, 16, 16]}
            for face in ('up', 'down', 'north', 'south', 'east', 'west')}}


def main():
    tiles = {}
    for tier, metal, cloth in [('bronze', (193, 123, 70), (83, 143, 86)),
                               ('silver', (199, 218, 229), (71, 119, 177)),
                               ('gold', (245, 194, 61), (178, 58, 64))]:
        img = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(img)
        d.polygon([(4, 7), (8, 8), (7, 15), (5, 13), (2, 14)], fill=(37, 37, 43))
        d.polygon([(8, 8), (12, 7), (14, 14), (11, 13), (9, 15)], fill=(37, 37, 43))
        d.polygon([(5, 8), (7, 9), (6, 13), (4, 13)], fill=cloth)
        d.polygon([(9, 9), (11, 8), (12, 13), (10, 13)], fill=cloth)
        d.ellipse((2, 0, 13, 11), fill=(47, 43, 41)); d.ellipse((3, 1, 12, 10), fill=metal)
        d.ellipse((5, 3, 10, 8), fill=tuple(int(c * .67) for c in metal))
        for x in (6, 8): d.line((x, 4, x, 7), fill=(255, 239, 185))
        tiles[tier] = img
        img.save(RES / f'assets/bunnidogs/textures/item/{tier}_show_ribbon.png')
        write(f'assets/bunnidogs/items/{tier}_show_ribbon.json', {'model': {'type': 'minecraft:model', 'model': f'bunnidogs:item/{tier}_show_ribbon'}})
        write(f'assets/bunnidogs/models/item/{tier}_show_ribbon.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'bunnidogs:item/{tier}_show_ribbon'}})

    top = Image.new('RGBA', (16, 16), (201, 154, 65)); d = ImageDraw.Draw(top)
    d.rectangle((1, 1, 14, 14), fill=(42, 98, 82)); d.rectangle((2, 2, 13, 13), outline=(75, 131, 98))
    card = Image.new('RGBA', (16, 16), (121, 69, 41)); d = ImageDraw.Draw(card)
    d.rectangle((1, 1, 14, 14), fill=(238, 220, 174)); d.line((7, 2, 7, 13), fill=(183, 154, 111))
    for y in (4, 7, 10):
        d.line((2, y, 5, y), fill=(109, 107, 84)); d.line((9, y, 13, y), fill=(109, 107, 84))
    for name, img in [('judging_top', top), ('judging_card', card)]:
        img.save(RES / f'assets/bunnidogs/textures/block/{name}.png'); tiles[name] = img
    textures = {'particle': 'bunnidogs:block/guava_planks', 'wood': 'bunnidogs:block/guava_planks',
                'top': 'bunnidogs:block/judging_top', 'card': 'bunnidogs:block/judging_card'}
    elements = [box([2, 0, 2], [14, 2, 14], 'wood'), box([6, 2, 6], [10, 12, 10], 'wood'),
                box([1, 12, 1], [15, 15, 15], 'wood', 'top'), box([4, 15, 5], [12, 16, 11], 'wood', 'card')]
    write('assets/bunnidogs/models/block/judging_stand.json', {'parent': 'minecraft:block/block', 'textures': textures, 'elements': elements})
    write('assets/bunnidogs/blockstates/judging_stand.json', {'variants': {'': {'model': 'bunnidogs:block/judging_stand'}}})
    write('assets/bunnidogs/models/item/judging_stand.json', {'parent': 'bunnidogs:block/judging_stand'})
    write('assets/bunnidogs/items/judging_stand.json', {'model': {'type': 'minecraft:model', 'model': 'bunnidogs:item/judging_stand'}})
    write('data/bunnidogs/loot_table/blocks/judging_stand.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1,
          'entries': [{'type': 'minecraft:item', 'name': 'bunnidogs:judging_stand'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    write('data/bunnidogs/recipe/judging_stand.json', {'type': 'minecraft:crafting_shaped', 'pattern': [' B ', 'PGP', 'PWP'],
          'key': {'B': 'minecraft:book', 'P': '#minecraft:planks', 'G': 'minecraft:gold_ingot', 'W': '#minecraft:wool'}, 'result': {'id': 'bunnidogs:judging_stand'}})
    write('data/bunnidogs/advancement/recipes/tools/judging_stand.json', {'parent': 'minecraft:recipes/root',
          'criteria': {'has_registry': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'bunnidogs:purebred_registry'}]}},
                       'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': 'bunnidogs:judging_stand'}}},
          'requirements': [['has_registry', 'has_recipe']], 'rewards': {'recipes': ['bunnidogs:judging_stand']}})
    write('data/bunnidogs/advancement/champion_line.json', {'parent': 'bunnidogs:breeds_true',
          'criteria': {'judged': {'trigger': 'minecraft:impossible'}}, 'requirements': [['judged']], 'rewards': {'experience': 100},
          'display': {'icon': {'id': 'bunnidogs:gold_show_ribbon'}, 'title': {'translate': 'advancement.bunnidogs.champion_line.title'},
                      'description': {'translate': 'advancement.bunnidogs.champion_line.description'}, 'frame': 'challenge',
                      'show_toast': True, 'announce_to_chat': True, 'hidden': False}})
    tag = RES / 'data/minecraft/tags/block/mineable/axe.json'
    value = json.loads(tag.read_text())
    if 'bunnidogs:judging_stand' not in value['values']: value['values'].append('bunnidogs:judging_stand')
    tag.write_text(json.dumps(value, indent=2) + '\n')
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({
          'block.bunnidogs.judging_stand': 'Judging Stand',
          'tooltip.bunnidogs.ribbon.requirement': 'All five bred stats +%s or higher; seven fixed pairs.',
          'tooltip.bunnidogs.ribbon.use': 'Use air to read. Display on a Trophy Pedestal.',
          'tooltip.bunnidogs.ribbon.blank': 'No winning record on this ribbon.',
          'message.bunnidogs.show.lead': 'Lead an adult bunnidog within six blocks of the stand.',
          'message.bunnidogs.show.parents': 'Show entries need recorded birth parents. Breed a representative.',
          'message.bunnidogs.show.mixed': 'Fix all seven gene pairs before entering this animal.',
          'message.bunnidogs.show.health': 'Let this animal recover to at least half health before showing.',
          'message.bunnidogs.show.stats': 'Bronze needs at least +5 in every bred stat. Silver +20; Gold +50.',
          'message.bunnidogs.show.already': 'This animal already earned this tier or higher. No repeat award.',
          'message.bunnidogs.show.awarded': 'Awarded %s! The winner stays on its lead.',
          'message.bunnidogs.show.preview': 'Stat tier: %s. Lowest: %s %s. Fixed pairs: %s/7. Preview only.',
          'show.bunnidogs.tier.none': 'Below Bronze',
          'show.bunnidogs.tier.bronze': 'Bronze',
          'show.bunnidogs.tier.silver': 'Silver',
          'show.bunnidogs.tier.gold': 'Gold',
          'advancement.bunnidogs.champion_line.title': 'Champion Line',
          'advancement.bunnidogs.champion_line.description': 'Win a Gold Show Ribbon with a stable bred adult at +50 in every stat.'})
        for tier in ('bronze', 'silver', 'gold'): value[f'item.bunnidogs.{tier}_show_ribbon'] = f'{tier.title()} Show Ribbon'
        path.write_text(json.dumps(value, indent=2) + '\n')

    # Nearest-filtered source tiles, plus geometry drawn from the same model-box list.
    preview = Image.new('RGBA', (1000, 400), (46, 43, 42, 255)); draw = ImageDraw.Draw(preview)
    for i, img in enumerate(tiles.values()): preview.alpha_composite(img.resize((160, 160), Image.Resampling.NEAREST), (20 + i * 195, 15))
    def project(x, y, z): return (500 + (x-z)*7, 290 + (x+z)*3-y*9)
    for element in elements:
        x, y, z = element['from']; X, Y, Z = element['to']
        faces = [([(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)], (133,82,49)),
                 ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)], (160,101,60)),
                 ([(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)], (65,120,90) if element['faces']['up']['texture']=='#top'
                  else (232,213,172) if element['faces']['up']['texture']=='#card' else (190,131,80))]
        for points, color in faces: draw.polygon([project(*p) for p in points], fill=color, outline=(42,35,28))
    preview.save(ROOT / 'art/show_preview.png')
    print('Generated Judging Stand, three ribbon items, recipes, advancement and source preview.')


if __name__ == '__main__': main()
