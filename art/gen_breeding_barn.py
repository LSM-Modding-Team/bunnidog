#!/usr/bin/env python3
"""Authored abandoned breeding barn, native template/pool, supply loot and layout preview.

v1 is an immutable saved-start resource: future layout revisions get a new template id.
No entities, litters, locked progression materials or recurring stock are generated.
"""
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from nbt_template import save, string, integer

SIZE = (19, 11, 19)


def layout():
    cells = {}

    def block(x, y, z, name, **props):
        ident = name if ':' in name else 'minecraft:'+name
        cells[x, y, z] = (ident, {k: str(v).lower() for k, v in props.items()}, None)

    def area(x1, x2, y1, y2, z1, z2, name, **props):
        for x in range(x1, x2+1):
            for y in range(y1, y2+1):
                for z in range(z1, z2+1):
                    block(x, y, z, name, **props)

    # A fitted stone sill, wooden floor and open-front guava barn.
    for x in range(1,12):
        for z in range(2,16):
            block(x,0,z,'mossy_cobblestone' if (x*7+z*11)%9 < 2 else 'cobblestone')
    area(2,10,0,0,3,14,'bunnidogs:guava_planks')
    area(2,10,1,8,3,14,'air')
    for x in (1,11): area(x,x,1,4,2,15,'bunnidogs:guava_planks')
    for z in (2,15): area(1,11,1,4,z,z,'bunnidogs:guava_planks')
    for x in (1,11):
        for z in (2,8,15): area(x,x,1,5,z,z,'bunnidogs:guava_log',axis='y')
    for z in (2,8,15): area(1,11,5,5,z,z,'bunnidogs:guava_log',axis='x')
    for x in (4,8): area(x,x,1,3,2,2,'bunnidogs:stripped_guava_log',axis='y')
    area(4,8,4,4,2,2,'bunnidogs:stripped_guava_log',axis='x')
    area(5,7,1,3,2,2,'air')
    area(11,11,1,3,9,10,'air')
    for z in (5,12):
        area(1,1,2,3,z,z+1,'glass_pane',north='true',south='true',east='false',west='false',waterlogged='false')
    area(5,7,2,3,15,15,'glass_pane',north='false',south='false',east='true',west='true',waterlogged='false')
    # Missing planks, cobwebs and a broken roof make this a find to restore.
    block(1,1,13,'air'); block(11,4,5,'air'); block(2,4,14,'cobweb')
    for x in range(13):
        y = 6+min(x,12-x)//2
        for z in range(1,17):
            if 2 <= x <= 4 and 9 <= z <= 11 or 8 <= x <= 10 and 5 <= z <= 7:
                continue
            if x == 6:
                block(x,y,z,'bunnidogs:guava_slab',type='bottom',waterlogged='false')
            else:
                block(x,y,z,'bunnidogs:guava_stairs',facing='east' if x < 6 else 'west',half='bottom',shape='straight',waterlogged='false')
    area(2,4,5,5,11,14,'bunnidogs:guava_planks')
    for pos in ((2,6,13),(3,6,13),(2,6,14),(3,6,14)):
        block(*pos,'hay_block',axis='y')
    block(6,4,8,'lantern',hanging='true',waterlogged='false')
    block(3,1,5,'bunnidogs:juicer',facing='south',working='false')
    block(8,1,5,'bunnidogs:genetics_table')
    block(8,2,5,'bunnidogs:specimen_jar',specimen='0')
    block(3,1,12,'bunnidogs:trophy_pedestal')
    block(8,1,12,'chest',facing='west',type='single',waterlogged='false')
    name, props, _ = cells[8,1,12]
    cells[8,1,12] = name, props, {'id': string('minecraft:chest'), 'LootTable': string('bunnidogs:chests/breeding_barn')}

    # Open, two-Nest side paddock. Restoring the missing rail is deliberate player work.
    area(12,17,0,0,5,15,'dirt')
    area(12,17,1,4,5,15,'air')
    for x in range(12,18):
        for z in (5,15): block(x,1,z,'bunnidogs:guava_fence')
    for z in range(6,15): block(17,1,z,'bunnidogs:guava_fence')
    block(14,1,5,'bunnidogs:guava_fence_gate',facing='north',open='false',powered='false',in_wall='false')
    block(17,1,13,'air')
    block(15,0,8,'bunnidogs:nest'); block(15,0,12,'bunnidogs:nest')
    block(13,1,10,'bunnidogs:breeding_pen',facing='east',fill='0',powered='false')
    block(16,1,14,'composter',level='0')
    block(16,1,6,'hay_block',axis='y')
    area(4,8,0,0,0,1,'dirt_path')
    block(17,0,2,'dirt'); block(17,1,2,'bunnidogs:guava_sapling')

    block(9,1,9,'jigsaw',orientation='down_south')
    name, props, _ = cells[9,1,9]
    cells[9,1,9] = name, props, {'id': string('minecraft:jigsaw'), 'name': string('bunnidogs:barn_center'),
        'target': string('minecraft:empty'), 'pool': string('minecraft:empty'), 'final_state': string('minecraft:air'),
        'joint': string('rollable'), 'selection_priority': integer(0), 'placement_priority': integer(0)}
    return cells


COLORS = {'guava_planks':(159,112,64),'guava_log':(102,75,45),'stripped_guava_log':(186,145,88),
    'guava_stairs':(151,102,60),'guava_slab':(168,116,66),'guava_fence':(119,85,48),'guava_fence_gate':(137,98,54),
    'cobblestone':(120,122,117),'mossy_cobblestone':(106,125,82),'dirt':(123,92,61),'dirt_path':(157,131,77),
    'glass_pane':(136,184,181),'nest':(200,172,96),'hay_block':(197,169,70),'juicer':(160,114,69),
    'genetics_table':(166,159,142),'specimen_jar':(150,197,185),'trophy_pedestal':(137,134,128),
    'chest':(173,124,57),'breeding_pen':(193,149,78),'composter':(117,79,47),'lantern':(239,191,80),
    'cobweb':(201,205,194),'guava_sapling':(88,133,66)}


def preview(cells):
    image = Image.new('RGB',(1160,700),(42,45,39)); draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('DejaVuSans.ttf',17); small = ImageFont.truetype('DejaVuSans.ttf',14)
    draw.text((20,15),'Abandoned breeding barn — native v1 template, workshop and two-Nest paddock',font=font,fill=(239,226,193))
    solid = {pos:name.split(':')[-1] for pos,(name,_,_) in cells.items() if name not in ('minecraft:air','minecraft:jigsaw')}

    def iso(origin, cutaway=False):
        scale=13
        def point(x,y,z): return origin[0]+(x-z)*scale,origin[1]+(x+z)*scale*.47-y*scale
        placed = {p:n for p,n in solid.items() if not cutaway or p[1] < 4}
        faces=[]
        partial={'guava_fence','guava_fence_gate','guava_stairs','guava_slab','glass_pane','specimen_jar',
                 'lantern','cobweb','guava_sapling','breeding_pen'}
        for (x,y,z),name in placed.items():
            props=cells[x,y,z][1]
            boxes=[(0,0,0,1,1,1)]
            if name == 'guava_fence':
                boxes=[(.375,0,.375,.625,1.5,.625)]
                for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
                    if (x+dx,y,z+dz) not in placed: continue
                    for low in (.375,.75):
                        boxes.append((.5 if dx > 0 else 0 if dx < 0 else .4375,low,
                            .5 if dz > 0 else 0 if dz < 0 else .4375,
                            1 if dx > 0 else .5 if dx < 0 else .5625,low+.1875,
                            1 if dz > 0 else .5 if dz < 0 else .5625))
            elif name == 'guava_fence_gate': boxes=[(0,.375,.4,1,1.2,.6)]
            elif name == 'guava_slab': boxes=[(0,0,0,1,.5,1)]
            elif name == 'guava_stairs':
                boxes=[(0,0,0,1,.5,1),(.5 if props['facing']=='east' else 0,.5,0,1 if props['facing']=='east' else .5,1,1)]
            elif name == 'glass_pane':
                boxes=[(.4375,0,0,.5625,1,1)] if props.get('north')=='true' else [(0,0,.4375,1,1,.5625)]
            elif name == 'specimen_jar': boxes=[(.2,0,.2,.8,.875,.8)]
            elif name == 'lantern': boxes=[(.3,0,.3,.7,.6,.7)]
            elif name in ('cobweb','guava_sapling'): boxes=[(.4,0,.4,.6,.8,.6)]
            elif name == 'breeding_pen':
                boxes=[(.0625,0,.125,.9375,.125,.875),(.0625,.125,.125,.1875,.75,.25),
                       (.8125,.125,.125,.9375,.75,.25),(.0625,.125,.75,.1875,.75,.875),(.8125,.125,.75,.9375,.75,.875)]
            color = COLORS.get(name,(126,130,109))
            for x0,y0,z0,x1,y1,z1 in boxes:
                x0+=x; x1+=x; y0+=y; y1+=y; z0+=z; z1+=z
                for normal,vertices,shade in [((1,0,0),[(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)],.72),
                    ((0,0,1),[(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],.86),
                    ((0,1,0),[(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],1.0)]:
                    neighbor=placed.get((x+normal[0],y+normal[1],z+normal[2]))
                    if name not in partial and neighbor is not None and neighbor not in partial: continue
                    faces.append((sum(sum(v) for v in vertices)/4,vertices,color,shade))
        for _,vertices,color,shade in sorted(faces,key=lambda f:f[0]):
            draw.polygon([point(*v) for v in vertices],fill=tuple(int(c*shade) for c in color),outline=(57,56,44))
    iso((290,195)); iso((870,195),True)
    draw.text((160,405),'Roof and weathered exterior',font=font,fill=(225,215,183))
    draw.text((740,405),'Cutaway: empty workshop and paddock',font=font,fill=(225,215,183))
    # Top-down floor/equipment key, taken directly from the same cell map.
    ox,oz=60,455; tile=11
    for z in range(SIZE[2]):
        for x in range(SIZE[0]):
            name=solid.get((x,1,z),solid.get((x,0,z)))
            if name: draw.rectangle((ox+x*tile,oz+z*tile,ox+(x+1)*tile-1,oz+(z+1)*tile-1),fill=COLORS.get(name,(126,130,109)))
    rows=['Juicer and Genetics Table: left workshop', 'Specimen Jar sits on the Table; Pedestal is empty',
          'One native supply chest; no recurring stock', 'Pen, two floor Nests, hay and composter: right paddock',
          'Missing fence rail and roof patches invite restoration', 'Dry, almost flat clearings; no spawned animals']
    for i,text in enumerate(rows): draw.text((315,465+i*31),text,font=font,fill=(223,218,188))
    draw.text((65,674),'Generated cell layout',font=small,fill=(181,185,159))
    image.save(ROOT/'art/breeding_barn_preview.png')


def main():
    cells=layout(); save(RES/'data/bunnidogs/structure/breeding_barn/v1.nbt',cells,SIZE)
    write('data/bunnidogs/worldgen/template_pool/breeding_barn/start.json',{'fallback':'minecraft:empty','elements':[
        {'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'bunnidogs:breeding_barn/v1',
            'processors':'bunnidogs:breeding_barn/preserve','projection':'rigid'}}]})
    write('data/bunnidogs/worldgen/processor_list/breeding_barn/preserve.json',{'processors':[{'processor_type':'bunnidogs:barn_preservation'}]})
    write('data/bunnidogs/tags/block/barn_preserve.json',{'replace':False,'values':[
        '#minecraft:features_cannot_replace',{'id':'#c:ores','required':False},'minecraft:ancient_debris']})
    write('data/bunnidogs/worldgen/structure/breeding_barn.json',{'type':'bunnidogs:breeding_barn',
        'biomes':'#bunnidogs:has_structure/breeding_barn','step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin'})
    write('data/bunnidogs/worldgen/structure_set/breeding_barn.json',{'structures':[{'structure':'bunnidogs:breeding_barn','weight':1}],
        'placement':{'type':'minecraft:random_spread','spacing':36,'separation':14,'salt':784901663,
            'exclusion_zone':{'other_set':'bunnidogs:warren_village','chunk_count':3}}})
    write('data/bunnidogs/tags/worldgen/structure/on_barn_maps.json',{'replace':False,'values':['bunnidogs:breeding_barn']})
    write('data/bunnidogs/villager_trade/cartographer/barn_map.json',{
        'wants':{'id':'minecraft:emerald','count':10},'additional_wants':{'id':'minecraft:compass'},'gives':{'id':'minecraft:map'},
        'max_uses':6,'xp':10,'reputation_discount':.2,'given_item_modifiers':[
            {'function':'minecraft:exploration_map','destination':'bunnidogs:on_barn_maps','decoration':'minecraft:mansion',
             'search_radius':50,'skip_existing_chunks':True},
            {'function':'minecraft:set_name','name':{'translate':'filled_map.bunnidogs.barn'},'target':'item_name'},
            {'function':'minecraft:filtered','item_filter':{'items':'minecraft:filled_map','predicates':{'minecraft:map_id':{}}},
             'on_fail':{'function':'minecraft:discard'}}]})
    # Append to the native pool, preserving the existing Warren Explorer Map and other additions.
    import json
    path=RES/'data/minecraft/tags/villager_trade/cartographer/level_3.json'; cartographers=json.loads(path.read_text())
    if 'bunnidogs:cartographer/barn_map' not in cartographers['values']:
        cartographers['values'].append('bunnidogs:cartographer/barn_map')
    write(str(path.relative_to(RES)),cartographers)
    write('data/bunnidogs/tags/worldgen/biome/has_structure/breeding_barn.json',{'replace':False,'values':[
        'minecraft:plains','minecraft:sunflower_plains','minecraft:meadow','minecraft:savanna',
        'minecraft:taiga','minecraft:snowy_plains','minecraft:cherry_grove','#minecraft:is_forest']})
    entries=[]
    for item,weight,low,high in [('bunnidogs:guava',8,4,12),('minecraft:bone_meal',5,2,6),('bunnidogs:ear_tag',3,1,3),
        ('minecraft:leather',3,1,4),('minecraft:string',4,2,6),('bunnidogs:sinew',2,1,3),('bunnidogs:golden_guava',1,1,1),
        ('bunnidogs:syringe',1,1,1)]:
        entry={'type':'minecraft:item','name':item,'weight':weight}
        if high > 1: entry['functions']=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':low,'max':high}}]
        entries.append(entry)
    write('data/bunnidogs/loot_table/chests/breeding_barn.json',{'type':'minecraft:chest','random_sequence':'bunnidogs:chests/breeding_barn',
        'pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:field_guide'}]},
            {'rolls':{'type':'minecraft:uniform','min':3,'max':5},'entries':entries}]})
    write('data/bunnidogs/advancement/old_breeding_grounds.json',{'parent':'bunnidogs:enter_warren','display':{
        'icon':{'id':'bunnidogs:breeding_pen'},'title':{'translate':'advancements.bunnidogs.barn.title'},
        'description':{'translate':'advancements.bunnidogs.barn.description'},'frame':'task','show_toast':True,'announce_to_chat':False,'hidden':False},
        'criteria':{'found':{'trigger':'minecraft:location','conditions':{'player':[{'condition':'minecraft:location_check',
            'predicate':{'structures':'bunnidogs:breeding_barn'}}]}}},'requirements':[['found']]})
    path=RES/'assets/bunnidogs/lang/en_us.json'; language=json.loads(path.read_text())
    language.update({'advancements.bunnidogs.barn.title':'Old Breeding Grounds',
                     'advancements.bunnidogs.barn.description':'Find an abandoned breeding barn','filled_map.bunnidogs.barn':'Barn Explorer Map'})
    path.write_text(json.dumps(language,indent=2)+'\n')
    preview(cells)
    print(f'Generated native breeding-barn v1 template ({len(cells)} cells), worldgen, supply loot and preview.')


if __name__ == '__main__':
    main()
