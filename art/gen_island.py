#!/usr/bin/env python3
"""Ocean-refuge resources and material preview from the actual exported Java IslandShape.

First ./gradlew exportIslandLayout, then python art/gen_island.py.
The checked-in JSON is a preview export, never a second worldgen layout or shipped game resource.
"""
import json
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from gen_breeder_venues import explorer_map, loot

COLORS = {'STONE':(119,126,123),'DIRT':(128,94,65),'COARSE_DIRT':(129,107,69),'GRASS':(114,151,70),
    'SAND':(221,206,151),'SANDSTONE':(202,188,133),'SLAB':(210,195,148),'STAIRS':(202,188,133),'LOG':(115,82,46),
    'LEAVES':(82,130,57),'FRUIT':(173,191,73),'NEST':(201,173,96),'TROUGH':(161,118,71),
    'BELL':(213,171,64),'LANTERN':(250,195,83),'CHEST':(174,124,58),'WATER':(65,133,161),
    'KELP':(79,141,60),'KELP_PLANT':(63,118,52),'SEAGRASS':(73,156,87),'PATH':(164,135,81)}


def preview():
    data = json.loads((ROOT/'art/island_layout.json').read_text())
    cells = {}
    for x, z, bottom, top, name in data['columns']:
        for y in range(bottom, top+1):
            cells[x,y,z] = name if y == top else 'STONE' if y < top-3 else 'SAND' if top <= 1 else 'DIRT'
    for c in data['decorations']:
        key = c['dx'], c['dy'], c['dz']
        if c['material'] == 'AIR': cells.pop(key,None)
        else: cells[key] = c['material']
    image = Image.new('RGB',(1440,1000),(35,47,48)); draw=ImageDraw.Draw(image)
    font=ImageFont.truetype('DejaVuSans.ttf',18); small=ImageFont.truetype('DejaVuSans.ttf',14)
    draw.text((22,17),'Island refuge — actual version-one Java layout: rooted shoal, sheltered founders and a shore garden',font=font,fill=(231,224,197))
    for index in range(2):
        ox=370+700*index; oy=225; scale=8
        def point(x,y,z): return ox+(x-z)*scale,oy+(x+z)*scale*.47-y*scale
        draw.polygon([point(-29,0,-29),point(29,0,-29),point(29,0,29),point(-29,0,29)],fill=(55,113,137))
        placed={pos:name for pos,name in cells.items() if not(index==0 and pos[1]<0)
                and not(index==1 and (name=='SLAB' or name in ('SANDSTONE','LOG') and pos[1]>4 and pos[2]<2))}
        partial={'SLAB','STAIRS','LANTERN','FRUIT','TROUGH','BELL','KELP','KELP_PLANT','SEAGRASS','PATH'}; faces=[]
        for (x,y,z),name in placed.items():
            boxes=[(0,0,0,1,1,1)]
            if name=='SLAB': boxes=[(0,0,0,1,.5,1)]
            elif name=='STAIRS': boxes=[(0,0,0,1,.5,1),(0,.5,0,1,1,.5)]
            elif name=='PATH': boxes=[(0,0,0,1,.9375,1)]
            elif name in ('FRUIT','LANTERN'): boxes=[(.3,.1,.3,.7,.7,.7)]
            elif name in ('KELP','KELP_PLANT','SEAGRASS'): boxes=[(.35,0,.35,.65,1,.65)]
            elif name=='TROUGH': boxes=[(.0625,0,.25,.9375,.4375,.75)]
            elif name=='BELL': boxes=[(.25,.25,.3,.75,.9,.7)]
            color=COLORS[name]
            for a,b,c,d,e,f in boxes:
                x0,y0,z0,x1,y1,z1=x+a,y+b,z+c,x+d,y+e,z+f
                for normal,vertices,shade in [((1,0,0),[(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)],.70),
                    ((0,0,1),[(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],.84),
                    ((0,1,0),[(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],1)]:
                    neighbor=placed.get((x+normal[0],y+normal[1],z+normal[2]))
                    if name not in partial and neighbor and neighbor not in partial: continue
                    faces.append((sum(sum(v) for v in vertices)/4,vertices,color,shade))
        for _,vertices,color,shade in sorted(faces,key=lambda p:p[0]):
            draw.polygon([point(*v) for v in vertices],fill=tuple(int(v*shade) for v in color),outline=(46,57,52))
        draw.text((155+700*index,510),'Ocean silhouette; underwater rock hidden' if index==0 else 'Cutaway: rooted shoal and sheltered floor Nests',font=font,fill=(226,218,190))
    draw.rectangle((0,0,1440,50),fill=(35,47,48))
    draw.text((22,17),'Island refuge — actual version-one Java layout: rooted shoal, sheltered founders and a shore garden',font=font,fill=(231,224,197))
    # Plan view is also drawn from the exact native cells, with an explicit plant/fixture key.
    tile=7; ox,oz=28,585
    for x in range(-24,25):
        for z in range(-24,25):
            column=[(y,name) for (cx,y,cz),name in cells.items() if cx==x and cz==z and y<=4]
            if column:
                _,name=max(column); px,pz=ox+(x+24)*tile,oz+(z+24)*tile
                draw.rectangle((px,pz,px+tile-1,pz+tile-1),fill=COLORS[name])
    notes=['A real ocean island with sampled open water at 64 / 96 blocks.',
        'Three wild founders; all new alleles must be collected and bred.',
        'Seal coat: Oxygen Bonus. Paddle legs: stronger swimming / shorter land stride.',
        'Shore foragers eat kelp, dried kelp and seagrass.',
        'Guava tree, contained tide pool, three Nests, two troughs, bell and supplies.',
        'Saved chunk progress prevents terrain rebuilding, restocking and founder replay.']
    for i,note in enumerate(notes): draw.text((415,615+i*46),note,font=font,fill=(219,219,191))
    draw.text((415,932),'Material/layout preview from Java export; native rendering and world frequency need gameplay review.',font=small,fill=(177,192,177))
    image.save(ROOT/'art/island_refuge_preview.png')


def traits_preview():
    from preview_bunnidog import world_faces, render
    texture=np.asarray(Image.open(RES/'assets/bunnidogs/textures/entity/bunnidog_seal.png').convert('RGB'),dtype=float)
    canvas=Image.new('RGB',(1440,690),(239,235,226)); draw=ImageDraw.Draw(canvas)
    font=ImageFont.truetype('DejaVuSans.ttf',18)
    for i,(baby,yaw,label) in enumerate([(False,35,'Seal coat, ordinary body, short paddle legs'),
            (False,120,'Same approved sculpt; expressed toe webbing'),(True,-35,'Inherited pup proportions and coastal coat')]):
        faces=world_faces(leg_scale=.85,baby=baby,webbed=True)
        canvas.paste(render(faces,texture,yaw=yaw,elevation=10,width=480,height=580,zoom=27),(480*i,50))
        draw.text((480*i+12,645),label,font=font,fill=(63,56,43))
    draw.text((18,15),'Island lineage — actual generated mesh and atlas; no base anatomy or face changes',font=font,fill=(63,56,43))
    canvas.save(ROOT/'art/island_traits_preview.png')


def main():
    write('data/bunnidogs/worldgen/structure/island_refuge.json',{'type':'bunnidogs:island_refuge',
        'biomes':'#bunnidogs:has_structure/island_refuge','step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
    write('data/bunnidogs/worldgen/structure_set/island_refuge.json',{'structures':[{'structure':'bunnidogs:island_refuge','weight':1}],
        'placement':{'type':'minecraft:random_spread','spacing':32,'separation':12,'salt':2057301947,
            'exclusion_zone':{'other_set':'minecraft:ocean_monuments','chunk_count':5}}})
    write('data/bunnidogs/tags/worldgen/biome/has_structure/island_refuge.json',{'replace':False,'values':[
        'minecraft:ocean','minecraft:lukewarm_ocean','minecraft:deep_lukewarm_ocean','minecraft:warm_ocean']})
    write('data/bunnidogs/tags/worldgen/structure/on_island_maps.json',{'replace':False,'values':['bunnidogs:island_refuge']})
    explorer_map('island','bunnidogs:on_island_maps',4,20,15)
    loot('island_refuge',['bunnidogs:field_guide','minecraft:lead'],[
        ('bunnidogs:guava',8,4,10),('minecraft:kelp',8,4,12),('minecraft:dried_kelp',6,3,8),
        ('minecraft:seagrass',3,2,5),('bunnidogs:ear_tag',3,1,3),('minecraft:bone_meal',4,2,6),('minecraft:sea_pickle',1,1,3)])
    write('data/bunnidogs/advancement/island_refuge.json',{'parent':'bunnidogs:enter_warren','display':{
        'icon':{'id':'bunnidogs:seal_pelt'},'title':{'translate':'advancements.bunnidogs.island.title'},
        'description':{'translate':'advancements.bunnidogs.island.description'},'frame':'goal','show_toast':True,'announce_to_chat':False,'hidden':False},
        'criteria':{'found':{'trigger':'minecraft:location','conditions':{'player':[{'condition':'minecraft:location_check',
            'predicate':{'structures':'bunnidogs:island_refuge'}}]}}},'requirements':[['found']]})
    path=RES/'assets/bunnidogs/lang/en_us.json'; language=json.loads(path.read_text())
    language.update({'filled_map.bunnidogs.island':'Island Refuge Explorer Map',
        'advancements.bunnidogs.island.title':'A World Apart',
        'advancements.bunnidogs.island.description':'Discover the isolated ocean-refuge lineage'})
    path.write_text(json.dumps(language,indent=2)+'\n')
    preview()
    traits_preview()
    print('Generated isolated ocean refuge data, maps, supply loot, discovery and actual-layout preview.')


if __name__ == '__main__': main()
