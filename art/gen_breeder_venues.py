#!/usr/bin/env python3
"""Versioned regional kennels and public showground, native worldgen/loot/maps and cell previews.

Intact player-establishment sites: stations start empty, Nests unclaimed, no founders or prizes spawn.
The saved v1 native templates stay available if a later layout gets a v2 identifier.
"""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from nbt_template import save, string, integer


class Plan:
    def __init__(self):
        self.cells = {}

    def block(self, x, y, z, name, **props):
        self.cells[x, y, z] = (name if ':' in name else 'minecraft:'+name,
                              {k: str(v).lower() for k, v in props.items()}, None)

    def area(self, x1, x2, y1, y2, z1, z2, name, **props):
        for x in range(x1, x2+1):
            for y in range(y1, y2+1):
                for z in range(z1, z2+1):
                    self.block(x, y, z, name, **props)

    def data(self, x, y, z, value):
        name, props, _ = self.cells[x, y, z]
        self.cells[x, y, z] = name, props, value

    def chest(self, x, y, z, table):
        self.block(x, y, z, 'chest', facing='west', type='single', waterlogged=False)
        self.data(x, y, z, {'id': string('minecraft:chest'), 'LootTable': string('bunnidogs:chests/'+table)})

    def anchor(self, center):
        self.block(center, 1, center, 'jigsaw', orientation='down_south')
        self.data(center, 1, center, {'id': string('minecraft:jigsaw'), 'name': string('bunnidogs:venue_center'),
            'target': string('minecraft:empty'), 'pool': string('minecraft:empty'), 'final_state': string('minecraft:air'),
            'joint': string('rollable'), 'selection_priority': integer(0), 'placement_priority': integer(0)})

    def fence(self, x1, x2, z1, z2):
        for x in range(x1, x2+1):
            for z in (z1, z2): self.block(x, 1, z, 'bunnidogs:guava_fence')
        for z in range(z1+1, z2):
            for x in (x1, x2): self.block(x, 1, z, 'bunnidogs:guava_fence')


THEMES = {
    'meadow_kennel': dict(wall='white_terracotta', post='stripped_oak_log', roof='bunnidogs:guava_stairs',
                          base='cobblestone', floor='bunnidogs:guava_planks', yard='grass_block', accent='lime_wool'),
    'desert_kennel': dict(wall='cut_sandstone', post='stripped_acacia_log', roof=None,
                          base='smooth_sandstone', floor='cut_sandstone', yard='sand', accent='orange_wool'),
    'snowy_kennel': dict(wall='spruce_planks', post='stripped_spruce_log', roof='spruce_stairs',
                         base='stone_bricks', floor='bunnidogs:guava_planks', yard='coarse_dirt', accent='light_blue_wool'),
}


def kennel(kind):
    p = Plan(); t = THEMES[kind]
    p.area(1, 21, 0, 0, 2, 20, t['base'])
    p.area(1, 21, 1, 5, 2, 20, 'air')
    p.area(11, 21, 0, 0, 4, 20, t['yard'])
    p.area(2, 10, 0, 0, 5, 18, t['floor'])
    for x in (2, 10): p.area(x, x, 1, 4, 5, 18, t['wall'])
    for z in (5, 18): p.area(2, 10, 1, 4, z, z, t['wall'])
    for x in (2, 10):
        for z in (5, 11, 18): p.area(x, x, 1, 4, z, z, t['post'], axis='y')
    for z in (8, 15):
        p.area(2, 2, 2, 3, z, z+1, 'glass_pane', north=True, south=True, east=False, west=False, waterlogged=False)
    p.area(5, 6, 1, 3, 5, 5, 'air')
    p.area(10, 10, 1, 3, 13, 14, 'air')
    p.area(4, 7, 0, 0, 0, 4, 'dirt_path')
    # The arid house has a shaded flat roof; the snow/meadow roofs shed rain with a gable.
    if t['roof']:
        for x in range(1, 12):
            y = 5+min(x-1, 11-x)//2
            for z in range(4, 20):
                if x == 6: p.block(x, y, z, 'bunnidogs:guava_slab', type='bottom', waterlogged=False)
                else: p.block(x, y, z, t['roof'], facing='east' if x < 6 else 'west', half='bottom', shape='straight', waterlogged=False)
    else:
        p.area(1, 11, 5, 5, 4, 19, 'smooth_sandstone')
        for x in range(1, 12):
            for z in (4, 19): p.block(x, 6, z, 'sandstone_slab', type='bottom', waterlogged=False)
    p.area(2, 10, 5, 5, 11, 11, t['post'], axis='x')
    p.area(4, 7, 4, 4, 5, 5, t['accent'])
    p.block(6, 4, 11, 'lantern', hanging=True, waterlogged=False)
    p.block(3, 1, 8, 'bunnidogs:juicer', facing='south', working=False)
    p.block(8, 1, 8, 'bunnidogs:genetics_table')
    p.block(8, 2, 8, 'bunnidogs:specimen_jar', specimen='0')
    p.block(8, 1, 12, 'bunnidogs:nursery_cradle')
    p.chest(8, 1, 16, 'regional_kennel')
    p.block(3, 1, 16, 'blue_bed', facing='north', occupied=False, part='foot')
    p.block(3, 1, 15, 'blue_bed', facing='north', occupied=False, part='head')
    # A complete yard with one controller. The two floor Nests keep parents two blocks apart.
    p.fence(11, 21, 4, 20)
    p.area(11, 11, 1, 3, 13, 14, 'air')
    p.block(16, 1, 20, 'bunnidogs:guava_fence_gate', facing='south', open=False, powered=False, in_wall=False)
    p.area(15, 17, 0, 0, 21, 22, 'dirt_path')
    p.area(12, 18, 1, 3, 6, 6, t['wall'])
    for x in (12, 18): p.area(x, x, 1, 3, 7, 11, t['wall'])
    p.area(11, 19, 4, 4, 5, 12, 'bunnidogs:guava_slab', type='bottom', waterlogged=False)
    p.block(14, 0, 10, 'bunnidogs:nest'); p.block(16, 0, 10, 'bunnidogs:nest')
    p.block(15, 1, 12, 'bunnidogs:breeding_pen', facing='north', fill='0', powered=False)
    p.block(12, 1, 16, 'bunnidogs:feeding_trough', fill='0', tending=False)
    p.block(19, 1, 7, 'hay_block', axis='y'); p.block(19, 1, 18, 'composter', level='0')
    p.block(20, 1, 16, 'bunnidogs:guava_fence'); p.block(20, 2, 16, 'lantern', hanging=False, waterlogged=False)
    p.block(3, 0, 3, 'dirt'); p.block(3, 1, 3, 'bunnidogs:guava_sapling')
    p.anchor(11)
    return p.cells, (23, 8, 23)


def show_ring():
    p = Plan()
    p.area(1, 29, 0, 0, 1, 29, 'grass_block')
    p.area(1, 29, 1, 6, 1, 29, 'air')
    p.fence(1, 29, 1, 29)
    p.area(14, 16, 1, 2, 29, 29, 'air')
    p.area(14, 16, 0, 0, 22, 30, 'dirt_path')
    for x in (13, 17): p.area(x, x, 1, 4, 28, 28, 'bunnidogs:guava_log', axis='y')
    p.area(13, 17, 5, 5, 28, 28, 'bunnidogs:guava_log', axis='x')
    # An unobstructed eleven-block ring. The near gold mark is within six blocks of the stand.
    for x in range(9, 22):
        for z in range(9, 22):
            if x in (9, 21) or z in (9, 21): p.block(x, 0, z, 'dirt_path')
            elif x in (10, 20) or z in (10, 20): p.block(x, 1, z, 'white_carpet')
    p.block(15, 1, 11, 'yellow_carpet')
    p.area(10, 20, 0, 0, 3, 8, 'stone_bricks')
    for x in (10, 20):
        for z in (3, 8): p.area(x, x, 1, 4, z, z, 'bunnidogs:guava_log', axis='y')
    p.area(9, 21, 5, 5, 2, 8, 'bunnidogs:guava_slab', type='bottom', waterlogged=False)
    for x, color in ((12, 'orange'), (15, 'light_gray'), (18, 'yellow')):
        p.block(x, 4, 8, color+'_wool')
        p.block(x, 1, 5, 'bunnidogs:trophy_pedestal')
    p.block(15, 1, 7, 'bunnidogs:judging_stand')
    p.block(15, 4, 4, 'lantern', hanging=True, waterlogged=False)
    p.chest(20, 1, 5, 'show_ring')
    # Two shaded holding bays; no Pen here, so unrelated entrants cannot trigger a paired feed.
    for z in (10, 17):
        p.fence(2, 8, z, z+5)
        p.block(8, 1, z+2, 'bunnidogs:guava_fence_gate', facing='east', open=False, powered=False, in_wall=False)
        p.block(4, 0, z+2, 'bunnidogs:nest')
        p.block(3, 1, z+3, 'bunnidogs:feeding_trough', fill='0', tending=False)
        p.area(2, 8, 4, 4, z, z+5, 'bunnidogs:guava_slab', type='bottom', waterlogged=False)
        for x in (2, 8):
            for edge in (z, z+5): p.area(x, x, 2, 3, edge, edge, 'bunnidogs:guava_fence')
    # Native stair seating faces the ring; the high back is on the east side of each row.
    for x, y in ((24, 1), (26, 2), (28, 3)):
        p.area(x, x, 1, y-1, 10, 22, 'stone_bricks')
        p.area(x, x, y, y, 10, 22, 'bunnidogs:guava_stairs', facing='east', half='bottom', shape='straight', waterlogged=False)
    for z in (9, 23):
        for x in range(24, 29):
            y = 1+(x-24)//2
            p.area(x, x, 1, y-1, z, z, 'stone_bricks')
            p.block(x, y, z, 'bunnidogs:guava_stairs', facing='east', half='bottom', shape='straight', waterlogged=False)
    for x in (3, 27):
        for z in (4, 26):
            p.area(x, x, 1, 2, z, z, 'bunnidogs:guava_fence')
            p.block(x, 3, z, 'lantern', hanging=False, waterlogged=False)
    p.anchor(15)
    return p.cells, (31, 7, 31)


def loot(name, guaranteed, supplies):
    entries = []
    for item, weight, low, high in supplies:
        entry = {'type': 'minecraft:item', 'name': item, 'weight': weight}
        if high > 1:
            entry['functions'] = [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': low, 'max': high}}]
        entries.append(entry)
    pools = [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item}]} for item in guaranteed]
    pools.append({'rolls': {'type': 'minecraft:uniform', 'min': 3, 'max': 5}, 'entries': entries})
    write(f'data/bunnidogs/loot_table/chests/{name}.json', {'type': 'minecraft:chest',
        'random_sequence': 'bunnidogs:chests/'+name, 'pools': pools})


def resources(kind, radius, biomes):
    write(f'data/bunnidogs/worldgen/template_pool/{kind}/start.json', {'fallback': 'minecraft:empty', 'elements': [
        {'weight': 1, 'element': {'element_type': 'minecraft:single_pool_element', 'location': f'bunnidogs:{kind}/v1',
            'processors': 'bunnidogs:breeding_barn/preserve', 'projection': 'rigid'}}]})
    write(f'data/bunnidogs/worldgen/structure/{kind}.json', {'type': 'bunnidogs:breeder_venue',
        'start_pool': f'bunnidogs:{kind}/start', 'start_jigsaw_name': 'bunnidogs:venue_center', 'site_radius': radius,
        'biomes': '#bunnidogs:has_structure/'+kind, 'step': 'surface_structures', 'spawn_overrides': {}, 'terrain_adaptation': 'beard_thin'})
    write(f'data/bunnidogs/tags/worldgen/biome/has_structure/{kind}.json', {'replace': False, 'values': biomes})


def explorer_map(name, destination, level, price, xp):
    write(f'data/bunnidogs/villager_trade/cartographer/{name}_map.json', {
        'wants': {'id': 'minecraft:emerald', 'count': price}, 'additional_wants': {'id': 'minecraft:compass'},
        'gives': {'id': 'minecraft:map'}, 'max_uses': 6, 'xp': xp, 'reputation_discount': .2,
        'given_item_modifiers': [
            {'function': 'minecraft:exploration_map', 'destination': destination, 'decoration': 'minecraft:mansion',
             'search_radius': 50, 'skip_existing_chunks': True},
            {'function': 'minecraft:set_name', 'name': {'translate': f'filled_map.bunnidogs.{name}'}, 'target': 'item_name'},
            {'function': 'minecraft:filtered', 'item_filter': {'items': 'minecraft:filled_map', 'predicates': {'minecraft:map_id': {}}},
             'on_fail': {'function': 'minecraft:discard'}}]})
    path = f'data/minecraft/tags/villager_trade/cartographer/level_{level}.json'
    value = json.loads((RES/path).read_text()) if (RES/path).exists() else {'replace': False, 'values': []}
    offer = f'bunnidogs:cartographer/{name}_map'
    if offer not in value['values']: value['values'].append(offer)
    write(path, value)


COLORS = {'white_terracotta': (202, 171, 153), 'cut_sandstone': (209, 190, 134), 'smooth_sandstone': (217, 201, 154),
    'sandstone_slab': (208, 190, 139), 'sand': (220, 207, 152), 'spruce_planks': (113, 83, 48),
    'stripped_spruce_log': (126, 95, 57), 'stripped_oak_log': (170, 137, 83), 'stripped_acacia_log': (171, 96, 53),
    'cobblestone': (125, 127, 120), 'stone_bricks': (129, 132, 126), 'coarse_dirt': (127, 94, 64),
    'grass_block': (105, 142, 69), 'dirt_path': (158, 133, 77), 'dirt': (124, 90, 58),
    'guava_planks': (159, 112, 64), 'guava_log': (102, 75, 45), 'guava_stairs': (151, 102, 60), 'guava_slab': (168, 116, 66),
    'guava_fence': (119, 85, 48), 'guava_fence_gate': (137, 98, 54), 'spruce_stairs': (108, 77, 45),
    'nest': (200, 172, 96), 'chest': (173, 124, 57), 'breeding_pen': (193, 149, 78), 'feeding_trough': (151, 113, 73),
    'juicer': (160, 114, 69), 'genetics_table': (166, 159, 142), 'specimen_jar': (150, 197, 185),
    'nursery_cradle': (170, 131, 73), 'trophy_pedestal': (137, 134, 128), 'judging_stand': (191, 162, 88),
    'white_carpet': (230, 227, 208), 'yellow_carpet': (237, 194, 50), 'hay_block': (197, 169, 70),
    'composter': (117, 79, 47), 'lantern': (239, 191, 80), 'glass_pane': (136, 184, 181), 'blue_bed': (71, 101, 163),
    'guava_sapling': (88, 133, 66), 'lime_wool': (122, 166, 58), 'orange_wool': (209, 127, 53),
    'light_gray_wool': (180, 182, 169), 'yellow_wool': (224, 185, 57), 'light_blue_wool': (108, 168, 192)}


def preview(cells, size, title, filename):
    image = Image.new('RGB', (1280, 840), (42, 45, 39)); draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('DejaVuSans.ttf', 17); small = ImageFont.truetype('DejaVuSans.ttf', 14)
    draw.text((20, 14), title, font=font, fill=(239, 226, 193))
    for index, cutoff in enumerate((99, 3)):
        scale = 10 if size[0] == 31 else 13
        ox, oy = (320+640*index), 175
        def point(x, y, z): return ox+(x-z)*scale, oy+(x+z)*scale*.47-y*scale
        wall_materials = {'white_terracotta','cut_sandstone','spruce_planks','stripped_oak_log',
                          'stripped_acacia_log','stripped_spruce_log','glass_pane'}
        placed = {pos: name.split(':')[-1] for pos, (name, _, _) in cells.items()
                  if pos[1] <= cutoff and name not in ('minecraft:air', 'minecraft:jigsaw')
                  and not (index == 1 and size[0] == 23 and pos[1] > 1 and name.split(':')[-1] in wall_materials)}
        faces = []
        partial = {n for n in placed.values() if any(key in n for key in ('fence', 'stairs', 'slab', 'carpet', 'pane', 'bed'))}
        partial |= {'nest', 'lantern', 'guava_sapling', 'specimen_jar', 'breeding_pen', 'feeding_trough', 'judging_stand'}
        for (x, y, z), name in placed.items():
            props = cells[x, y, z][1]; boxes = [(0, 0, 0, 1, 1, 1)]
            if name.endswith('_stairs'):
                boxes = [(0, 0, 0, 1, .5, 1), (.5 if props['facing'] == 'east' else 0, .5, 0,
                         1 if props['facing'] == 'east' else .5, 1, 1)]
            elif name.endswith('_slab'): boxes = [(0, 0, 0, 1, .5, 1)]
            elif name.endswith('_carpet'): boxes = [(0, 0, 0, 1, .0625, 1)]
            elif name.endswith('_bed'): boxes = [(0, 0, 0, 1, .5625, 1)]
            elif name.endswith('_fence'):
                boxes = [(.375, 0, .375, .625, 1.5, .625)]
                for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if (x+dx, y, z+dz) in placed:
                        for low in (.375, .75): boxes.append((.5 if dx > 0 else 0 if dx < 0 else .4375, low,
                            .5 if dz > 0 else 0 if dz < 0 else .4375, 1 if dx > 0 else .5 if dx < 0 else .5625,
                            low+.1875, 1 if dz > 0 else .5 if dz < 0 else .5625))
            elif name.endswith('_fence_gate'): boxes = [(0, .375, .4, 1, 1.2, .6)]
            elif name == 'glass_pane': boxes = [(.4375, 0, 0, .5625, 1, 1)]
            elif name in ('breeding_pen', 'feeding_trough'): boxes = [(.0625, 0, .125, .9375, .55, .875)]
            elif name == 'specimen_jar': boxes = [(.2, 0, .2, .8, .875, .8)]
            elif name == 'judging_stand': boxes = [(.3, 0, .3, .7, .9, .7), (.05, .9, .05, .95, 1, .95)]
            elif name == 'lantern': boxes = [(.3, 0, .3, .7, .6, .7)]
            elif name == 'guava_sapling': boxes = [(.3, 0, .3, .7, .9, .7)]
            color = COLORS.get(name, (126, 130, 109))
            for a, b, c, d, e, f in boxes:
                x0, y0, z0, x1, y1, z1 = x+a, y+b, z+c, x+d, y+e, z+f
                for normal, vertices, shade in [((1, 0, 0), [(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)], .72),
                    ((0, 0, 1), [(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)], .86),
                    ((0, 1, 0), [(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)], 1)]:
                    neighbor = placed.get((x+normal[0],y+normal[1],z+normal[2]))
                    if name not in partial and neighbor is not None and neighbor not in partial: continue
                    faces.append((sum(sum(v) for v in vertices)/4, vertices, color, shade))
        for _, vertices, color, shade in sorted(faces, key=lambda face: face[0]):
            draw.polygon([point(*v) for v in vertices], fill=tuple(int(c*shade) for c in color), outline=(57,56,44))
        draw.text((160+640*index, 490), 'Exterior' if index == 0 else 'Cutaway: authored stations and routes', font=font, fill=(225,215,183))
    tile = 9; ox, oz = 35, 535
    for z in range(size[2]):
        for x in range(size[0]):
            value = cells.get((x,1,z))
            if not value or value[0] in ('minecraft:air','minecraft:jigsaw'): value = cells.get((x,0,z))
            if value:
                draw.rectangle((ox+x*tile,oz+z*tile,ox+(x+1)*tile-1,oz+(z+1)*tile-1), fill=COLORS.get(value[0].split(':')[-1],(126,130,109)))
    rows = ['Generated directly from the shipped native template cells.', 'Left: exterior. Right: roof/wall cutaway for station visibility.',
            'Empty stations, native supply loot, unclaimed Nests.', 'No spawned animals, villagers, rare genes or awarded ribbons.',
            'Native rotations and saved pool pieces; protected containers stay intact.']
    for i, text in enumerate(rows): draw.text((360, 560+i*37), text, font=font, fill=(223,218,188))
    draw.text((360, 770), 'Layout/material preview; native rendering and natural-world siting need gameplay review.', font=small, fill=(181,185,159))
    image.save(ROOT/'art'/filename)


def main():
    biomes = {'meadow_kennel': ['minecraft:plains', 'minecraft:sunflower_plains', 'minecraft:meadow', 'minecraft:cherry_grove'],
              'desert_kennel': ['minecraft:desert', 'minecraft:savanna'],
              'snowy_kennel': ['minecraft:snowy_plains', 'minecraft:snowy_taiga', 'minecraft:taiga']}
    for kind in THEMES:
        cells, size = kennel(kind); save(RES/f'data/bunnidogs/structure/{kind}/v1.nbt', cells, size)
        resources(kind, 12, biomes[kind]); preview(cells, size, kind.replace('_', ' ').title()+' — intact v1 regional breeding yard', kind+'_preview.png')
    cells, size = show_ring(); save(RES/'data/bunnidogs/structure/show_ring/v1.nbt', cells, size)
    resources('show_ring', 16, ['minecraft:plains','minecraft:sunflower_plains','minecraft:meadow','minecraft:savanna'])
    preview(cells, size, 'Show Ring arena — v1 public judging, trophy dais, holding bays and spectator stands', 'show_ring_arena_preview.png')
    # Sharing one placement grid keeps new venues apart and reserves space around old warren candidates.
    write('data/bunnidogs/worldgen/structure_set/breeder_venues.json', {'structures': [
        *[{'structure': 'bunnidogs:'+kind, 'weight': 3} for kind in THEMES],
        {'structure': 'bunnidogs:show_ring', 'weight': 1}],
        'placement': {'type': 'minecraft:random_spread', 'spacing': 32, 'separation': 12, 'salt': 631447208,
                      'exclusion_zone': {'other_set': 'bunnidogs:warren_village', 'chunk_count': 3}}})
    for tag, values in {'regional_kennels': list(THEMES), 'on_kennel_maps': list(THEMES), 'on_show_ring_maps': ['show_ring']}.items():
        write(f'data/bunnidogs/tags/worldgen/structure/{tag}.json', {'replace':False,'values':['bunnidogs:'+v for v in values]})
    supplies = [('bunnidogs:guava',8,6,16), ('minecraft:bone_meal',5,2,8), ('bunnidogs:ear_tag',4,1,4),
                ('minecraft:string',4,2,6), ('minecraft:leather',3,1,4), ('bunnidogs:golden_guava',1,1,1)]
    loot('regional_kennel', ['bunnidogs:field_guide'], supplies)
    loot('show_ring', ['bunnidogs:field_guide', 'minecraft:lead'], [('bunnidogs:guava',8,4,12),
        ('minecraft:string',5,2,6), ('minecraft:yellow_dye',2,1,3), ('minecraft:orange_dye',2,1,3),
        ('minecraft:light_gray_dye',2,1,3), ('minecraft:white_wool',3,1,4), ('bunnidogs:ear_tag',3,1,3)])
    explorer_map('kennel', 'bunnidogs:on_kennel_maps', 3, 12, 10)
    explorer_map('show_ring', 'bunnidogs:on_show_ring_maps', 4, 16, 15)
    for name, predicate, icon, parent in [('regional_kennel','#bunnidogs:regional_kennels','bunnidogs:nest','enter_warren'),
                                          ('show_ring','bunnidogs:show_ring','bunnidogs:judging_stand','regional_kennel')]:
        write(f'data/bunnidogs/advancement/{name}.json', {'parent':'bunnidogs:'+parent,'display':{
            'icon':{'id':icon},'title':{'translate':f'advancements.bunnidogs.{name}.title'},
            'description':{'translate':f'advancements.bunnidogs.{name}.description'},'frame':'task',
            'show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'found':{'trigger':'minecraft:location',
                'conditions':{'player':[{'condition':'minecraft:location_check','predicate':{'structures':predicate}}]}}},'requirements':[['found']]})
    path = RES/'assets/bunnidogs/lang/en_us.json'; language = json.loads(path.read_text())
    language.update({'filled_map.bunnidogs.kennel':'Kennel Explorer Map','filled_map.bunnidogs.show_ring':'Showground Explorer Map',
        'advancements.bunnidogs.regional_kennel.title':'Room for a Lineage',
        'advancements.bunnidogs.regional_kennel.description':'Find a regional kennel and its ready-to-stock breeding yard',
        'advancements.bunnidogs.show_ring.title':'The Showground',
        'advancements.bunnidogs.show_ring.description':'Find a public Show Ring arena'})
    path.write_text(json.dumps(language,indent=2)+'\n')
    print('Generated three intact regional kennels, Show Ring arena, native maps/loot/discoveries and four layout previews.')


if __name__ == '__main__': main()
