#!/usr/bin/env python3
"""Generates the guava tree textures (Phase 4g): src/main/resources/assets/bunnidogs/textures/block/guava_fruit_stage0..2.png and guava_sapling.png (16x16).

Run from the project root:  python3 art/gen_guava_tree.py     (needs Pillow)      Also writes art/guava_tree_preview.png (12x).

The fruit hangs from the TOP of its block (a short stem, then the guava), in three ages that match the item's colours (art/gen_guava.py):
0 small and dark green, 1 bigger and lighter, 2 ripe: yellow with a pink blush and speckles (the picture that tells you it can be picked).
The sapling is a thin brown stem with three pairs of leaves. Both are drawn for minecraft:block/cross (two crossing planes).
"""
import os

from PIL import Image,ImageDraw
import json,random
from pathlib import Path

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/guava_tree_preview.png"

STEM = [(94, 120, 52), (62, 88, 34)]
LEAF = [(120, 176, 66), (84, 138, 48), (52, 96, 36)]
BARK = [(142, 104, 64), (98, 68, 40)]

# per age: radius, skin (highlight, light, mid, dark, outline), speckle colour or None, blush or False
AGES = [
    dict(r=3, skin=[(176, 206, 104), (132, 176, 74), (104, 148, 58), (74, 112, 44), (44, 70, 30)], speckle=None, blush=False),
    dict(r=4, skin=[(226, 238, 150), (184, 214, 92), (152, 192, 72), (112, 152, 56), (62, 88, 34)], speckle=(120, 160, 60), blush=False),
    dict(r=5, skin=[(255, 246, 170), (246, 226, 100), (226, 198, 70), (182, 150, 48), (110, 82, 30)], speckle=(196, 164, 56), blush=True),
]


def fruit(age,bunch=1):
    if age==0:
        im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line((7,0,7,5),fill='#657f36',width=2)
        for x,y in ([(8,8)] if bunch==1 else [(4,7),(11,9)] if bunch==2 else [(4,6),(11,7),(8,12)]):
            d.line((7,4,x,y),fill='#678840');d.rectangle((x-2,y-1,x+2,y+1),fill='#efddcf');d.rectangle((x-1,y-2,x+1,y+2),fill='#ffe9da');d.point((x,y),fill='#d2a752');d.point((x-2,y-1),fill='#d7a6b7')
        return im
    if bunch>1:
        im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line((7,0,7,4),fill='#657f36',width=2)
        tiny=fruit(age,1).resize((9,10),Image.Resampling.NEAREST)
        for x,y in ([(0,2),(7,4)] if bunch==2 else [(0,1),(7,1),(3,6)]):
            d.line((7,3,x+4,y+2),fill='#678840');im.alpha_composite(tiny,(x,y))
        return im
    p = AGES[age]
    r = p["r"]
    hi, light, mid, dark, outline = p["skin"]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 7.5, 4.5 + r
    # stem and a leaf at the top (the fruit hangs from the leaves above)
    for y in range(0, 5):
        px[7, y] = STEM[0] + (255,)
        px[8, y] = STEM[1] + (255,)
    px[9, 1] = LEAF[1] + (255,)
    px[10, 1] = LEAF[2] + (255,)
    px[6, 2] = LEAF[0] + (255,)
    for y in range(16):
        for x in range(16):
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d <= r + 0.2:
                if d > r - 0.9:
                    colour = outline
                elif (x + 0.5 - cx) + (y + 0.5 - cy) < -r * 0.55:
                    colour = hi
                elif (x + 0.5 - cx) + (y + 0.5 - cy) > r * 0.75:
                    colour = dark
                elif (x + 0.5 - cx) + (y + 0.5 - cy) > r * 0.25:
                    colour = mid
                else:
                    colour = light
                px[x, y] = colour + (255,)
    if p["speckle"]:
        for (dx, dy) in [(-1, 0), (1, 1), (0, -2), (2, -1), (-2, 2)][: r - 1]:
            x, y = int(cx + dx), int(cy + dy)
            if px[x, y][3] == 255 and px[x, y][:3] not in (outline, hi):
                px[x, y] = p["speckle"] + (255,)
    if p["blush"]:
        for (dx, dy) in [(1, 2), (2, 1), (2, 2), (1, 3)]:
            x, y = int(cx + dx), int(cy + dy)
            if px[x, y][3] == 255 and px[x, y][:3] != outline:
                px[x, y] = (244, 150, 140, 255)
    # a dark calyx dot at the bottom of the fruit
    px[int(cx), int(cy + r - 1)] = outline + (255,)
    return img


def sapling():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(7, 16):
        px[7, y] = BARK[0] + (255,)
        px[8, y] = BARK[1] + (255,)
    leaves = [(5, 12, 6, 12), (4, 11, 6, 11), (9, 10, 11, 10), (9, 9, 12, 9), (6, 8, 7, 8), (8, 8, 9, 8), (6, 7, 9, 7), (7, 6, 8, 6)]
    for (x0, y0, x1, y1) in leaves:
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                px[x, y] = LEAF[(x + y) % 3] + (255,)
    for (x, y) in [(3, 11), (12, 9), (7, 5), (8, 5)]:
        px[x, y] = LEAF[2] + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    scale = 12
    imgs = [("guava_fruit_stage%d%s" % (a,'' if b==1 else '_bunch'+str(b)), fruit(a,b)) for b in range(1,4) for a in range(3)] + [("guava_sapling", sapling())]
    sheet = Image.new("RGBA", (3*(16*scale+8)+8,4*(16*scale+8)+8), (52, 52, 60, 255))
    for i, (name, img) in enumerate(imgs):
        img.save(os.path.join(OUT, name + ".png"))
        sheet.alpha_composite(img.resize((16 * scale, 16 * scale), Image.NEAREST), (8+(i%3)*(16*scale+8),8+(i//3)*(16*scale+8)))
    root=Path(__file__).resolve().parents[1];assets=root/'src/main/resources/assets/bunnidogs';data=root/'src/main/resources/data/bunnidogs'
    def write(base,path,value):
        p=base/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2,ensure_ascii=False)+'\n')
    variants={}
    for b in range(1,4):
        for age in range(3):
            name=f'guava_fruit_stage{age}'+('' if b==1 else f'_bunch{b}')
            variants[f'age={age},bunch={b}']={'model':'bunnidogs:block/'+name}
            write(assets,'models/block/'+name+'.json',{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'bunnidogs:block/'+name}})
    write(assets,'blockstates/guava_fruit.json',{'variants':variants})
    write(data,'loot_table/blocks/guava_fruit.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:guava','functions':[{'function':'minecraft:set_count','count':b}],'conditions':[{'condition':'minecraft:block_state_property','block':'bunnidogs:guava_fruit','properties':{'age':'2','bunch':str(b)}}]}]} for b in range(1,4)]})
    # Root soil is a visible, ordinary material investment, not a hidden fertilizer timer.
    im=Image.new('RGBA',(16,16));rng=random.Random(90231)
    for x in range(16):
        for y in range(16):
            n=rng.randrange(-12,13);im.putpixel((x,y),(79+n,56+n,34+n,255))
    d=ImageDraw.Draw(im)
    for x,y in [(2,3),(8,2),(12,6),(5,10),(11,13),(2,14)]:d.line((x,y,x+2,y+1),fill='#bb995c');d.point((x,y+1),fill='#72954a')
    im.save(assets/'textures/block/orchard_mulch.png')
    sheet.alpha_composite(im.resize((16*scale,16*scale),Image.Resampling.NEAREST),(208,608))
    write(assets,'models/block/orchard_mulch.json',{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/orchard_mulch'}})
    write(assets,'models/item/orchard_mulch.json',{'parent':'bunnidogs:block/orchard_mulch'})
    write(assets,'blockstates/orchard_mulch.json',{'variants':{'':{'model':'bunnidogs:block/orchard_mulch'}}})
    write(assets,'items/orchard_mulch.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/orchard_mulch'}})
    write(data,'loot_table/blocks/orchard_mulch.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:orchard_mulch'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    d.line((2,15,11,5),fill='#443b2c',width=3);d.line((2,14,10,5),fill='#c3a475');d.line((10,6,15,3,14,0),fill='#65978e',width=2);d.line((8,5,9,1,11,0),fill='#ad6c43',width=2);d.line((11,6,12,1),fill='#d5a367');d.line((9,7,14,5),fill='#e4c086')
    im.save(assets/'textures/item/fruit_picker.png')
    sheet.alpha_composite(im.resize((16*scale,16*scale),Image.Resampling.NEAREST),(408,608));sheet.save(PREVIEW)
    write(assets,'models/item/fruit_picker.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'bunnidogs:item/fruit_picker'}})
    write(assets,'items/fruit_picker.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/fruit_picker'}})
    write(data,'recipe/fruit_picker.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':[' CF',' SF','S  '],'key':{'C':'minecraft:copper_ingot','F':'bunnidogs:root_fibre','S':'minecraft:stick'},'result':{'id':'bunnidogs:fruit_picker'}})
    write(data,'recipe/orchard_mulch.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:dirt','minecraft:bone_meal','minecraft:wheat','minecraft:wheat','bunnidogs:root_fibre'],'result':{'id':'bunnidogs:orchard_mulch','count':4}})
    for name,material in [('guava_sapling','bunnidogs:guava'),('fruit_picker','bunnidogs:guava'),('orchard_mulch','bunnidogs:guava_sapling')]:
        write(data,f'advancement/recipes/orchard/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':material}]}}},'requirements':[['material']],'rewards':{'recipes':['bunnidogs:'+name]}})
    for name in ['supports_vegetation','dirt','mineable/shovel']:
        p=root/f'src/main/resources/data/minecraft/tags/block/{name}.json';obj=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+['bunnidogs:orchard_mulch']));write(root/'src/main/resources/data/minecraft/tags/block',name+'.json',obj)
    p=root/'src/main/resources/data/minecraft/tags/item/enchantable/durability.json';obj=json.loads(p.read_text());obj['values']=list(dict.fromkeys(obj['values']+['bunnidogs:fruit_picker']));write(p.parent,p.name,obj)
    write(data,'worldgen/configured_feature/guava_grove.json',{'type':'bunnidogs:guava_grove','config':{}})
    write(data,'worldgen/placed_feature/guava_grove.json',{'feature':'bunnidogs:guava_grove','placement':[{'type':'minecraft:rarity_filter','chance':16},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
    write(data,'neoforge/biome_modifier/add_guava_groves.json',{'type':'neoforge:add_features','biomes':['minecraft:forest','minecraft:flower_forest','minecraft:birch_forest','minecraft:savanna','minecraft:jungle','minecraft:sparse_jungle'],'features':'bunnidogs:guava_grove','step':'vegetal_decoration'})
    messages={
      'block.bunnidogs.orchard_mulch':('Orchard Mulch','Mantillo de huerto'),
      'item.bunnidogs.fruit_picker':('Guava Picker','Recolector de guayabas'),
      'block.bunnidogs.guava_fruit':('Guava Bunch','Racimo de guayabas'),
      'tooltip.bunnidogs.orchard.graft':('Grafts onto living guava branches.','Se injerta en ramas vivas de guayabo.'),
      'tooltip.bunnidogs.orchard.picker':('Reaches high branches and catches picked fruit.','Alcanza ramas altas y recoge sus frutos.'),
      'tooltip.bunnidogs.orchard.mulch':('Guava trees grow larger bunches in this soil.','Los guayabos dan racimos más grandes en este suelo.'),
      'guide.bunnidogs.page.59.body':('Wild groves already carry ripe bunches. Shake a standing trunk to gather them; a copper Guava Picker catches high fruit. Craft a sapling from a guava, plant a tree and enrich its root soil with Orchard Mulch.','Los huertos silvestres ya tienen racimos maduros. Sacude un tronco para recogerlos; el recolector de cobre alcanza los frutos altos. Fabrica un plantón con una guayaba, planta un árbol y mejora su suelo con mantillo de huerto.'),
      'guide.bunnidogs.page.71.body':('The Guava Picker catches a whole ripe bunch within eight blocks. Solid blocks stop it. Dispensers with shears drop the same bunch for hoppers to collect. Both wear once per harvest; flowers and green fruit stay on the tree.','El recolector alcanza un racimo maduro a ocho bloques y lo guarda entero. Los bloques sólidos lo detienen. Los dispensadores con tijeras sueltan el mismo racimo para las tolvas. Cada cosecha gasta un uso; las flores y los frutos verdes quedan en el árbol.'),
      'guide.bunnidogs.page.74.body':('A Guava Sapling also grafts a flowering bunch below guava leaves connected to a living rooted trunk. Dispensers can graft it too. Raw guavas remain food. Bone meal advances a stage; the tree keeps producing after harvest.','Un plantón de guayabo también permite injertar un racimo en flor bajo hojas conectadas a un tronco vivo con raíces. Los dispensadores también pueden injertarlo. Las guayabas siguen siendo alimento. El polvo de hueso avanza una etapa; el árbol vuelve a dar frutos tras la cosecha.'),
      'tooltip.bunnidogs.guava.plant':('Craft a sapling to start a guava orchard.','Fabrica un plantón para empezar un huerto de guayabos.')}
    for locale,index in [('en_us',0),('es_pe',1)]:
        p=assets/f'lang/{locale}.json';lang=json.loads(p.read_text());lang.update({k:v[index] for k,v in messages.items()});p.write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
    print('Generated nine flowering/fruit bunches, picker, mulch, native recipes and grove placement.')


if __name__ == "__main__":
    main()
