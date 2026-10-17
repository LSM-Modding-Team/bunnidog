"""Nursery Cradle: woven wool bedding, guava wood, a glass window and five growth displays."""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write


def texture():
    side = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(side)
    d.rectangle((0,0,15,15),fill=(151,95,52))
    for y in range(16):
        for x in range(16):
            color = (181,118,67) if (x*7+y*11)%9 < 3 else (161,101,55)
            side.putpixel((x,y),color+(255,))
    for y in (0,4,12,15): d.line((0,y,15,y),fill=(96,61,39))
    for x in (0,15): d.line((x,0,x,15),fill=(95,60,38))
    d.rectangle((2,2,13,4),fill=(225,219,193)); d.line((3,3,12,3),fill=(248,242,214))
    for x in (1,14):
        for y in (1,14): d.point((x,y),fill=(207,166,78))
    side.save(RES/'assets/bunnidogs/textures/block/nursery_cradle_side.png')
    top = side.copy(); t = ImageDraw.Draw(top)
    t.rectangle((2,2,13,13),fill=(230,223,195)); t.rectangle((4,4,11,11),fill=(182,170,131))
    for y in range(3,13,2): t.line((3,y,12,y),fill=(247,239,207))
    t.polygon([(7,0),(9,2),(8,2),(8,4),(6,4),(6,2),(5,2)],fill=(77,114,78))
    top.save(RES/'assets/bunnidogs/textures/block/nursery_cradle_top.png')
    fronts = []
    for fill in range(5):
        front = side.copy(); f = ImageDraw.Draw(front)
        f.rectangle((2,5,13,12),fill=(57,81,82)); f.rectangle((3,6,12,11),fill=(96,133,132))
        f.line((3,6,6,6),fill=(185,214,194)); f.line((3,7,3,9),fill=(160,193,182))
        f.rectangle((4,10,11,11),fill=(216,206,163))
        if fill:
            size = fill+1
            f.ellipse((8-size//2,10-size,8+(size+1)//2,10),fill=(141,169,75) if fill < 4 else (194,223,112))
            f.line((8,9,8,7),fill=(76,105,47))
            if fill >= 3: f.point((9,7),fill=(235,233,177))
        f.rectangle((6,13,9,14),fill=(89,109,67) if fill == 4 else (97,70,44))
        front.save(RES/f'assets/bunnidogs/textures/block/nursery_cradle_front_{fill}.png'); fronts.append(front)
    return side,top,fronts


def main():
    side,top,fronts = texture()
    variants = {}
    for fill in range(5):
        write(f'assets/bunnidogs/models/block/nursery_cradle_{fill}.json',{'parent':'minecraft:block/cube',
              'textures':{'particle':'bunnidogs:block/nursery_cradle_side','north':f'bunnidogs:block/nursery_cradle_front_{fill}',
              'south':'bunnidogs:block/nursery_cradle_side','east':'bunnidogs:block/nursery_cradle_side',
              'west':'bunnidogs:block/nursery_cradle_side','up':'bunnidogs:block/nursery_cradle_top','down':'bunnidogs:block/guava_planks'}})
        for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:
            variants[f'facing={facing},fill={fill}'] = {'model':f'bunnidogs:block/nursery_cradle_{fill}','y':rotation}
    write('assets/bunnidogs/blockstates/nursery_cradle.json',{'variants':variants})
    write('assets/bunnidogs/items/nursery_cradle.json',{'model':{'type':'minecraft:model','model':'bunnidogs:block/nursery_cradle_0'}})
    write('data/bunnidogs/recipe/nursery_cradle.json',{'type':'minecraft:crafting_shaped','pattern':['WWW','GNG','PPP'],
          'key':{'W':'#minecraft:wool','G':'minecraft:glass','N':'bunnidogs:nest','P':'#minecraft:planks'},'result':{'id':'bunnidogs:nursery_cradle'}})
    write('data/bunnidogs/advancement/recipes/tools/nursery_cradle.json',{'parent':'minecraft:recipes/root',
          'criteria':{'has_nest':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:nest'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:nursery_cradle'}}},
          'requirements':[['has_nest','has_recipe']],'rewards':{'recipes':['bunnidogs:nursery_cradle']}})
    write('data/bunnidogs/loot_table/blocks/nursery_cradle.json',{'type':'minecraft:block','pools':[{'rolls':1,
          'entries':[{'type':'minecraft:item','name':'bunnidogs:nursery_cradle'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    path = RES/'data/minecraft/tags/block/mineable/axe.json'; tag = json.loads(path.read_text())
    if 'bunnidogs:nursery_cradle' not in tag['values']: tag['values'].append('bunnidogs:nursery_cradle')
    write(str(path.relative_to(RES)),tag)
    text = {'block.bunnidogs.nursery_cradle':'Nursery Cradle',
        'message.bunnidogs.cradle.empty':'Empty Cradle. Insert a litter seed; the front marks its hatch pad.',
        'message.bunnidogs.cradle.invalid':'Stored item has no valid litter. Sneak-empty to recover it.',
        'message.bunnidogs.cradle.growing':'Stage %s/7; next in %ss. Sneak-empty withdraws and resets growth.',
        'message.bunnidogs.cradle.ready':'Ready. Clear the pad in front, then use empty-handed or pulse redstone.',
        'message.bunnidogs.cradle.withdrawn':'Recovered the stored seed. Incubation restarts when reinserted.',
        'message.bunnidogs.cradle.not_ripe':'The litter is still incubating.',
        'message.bunnidogs.cradle.blocked':'Step aside and clear a dry 3x3 pad in front, with two blocks of air.',
        'message.bunnidogs.cradle.crowded':'Too many bunnidogs nearby. The ripe litter is retained.',
        'message.bunnidogs.cradle.refused':'Hatching was refused. The litter is retained.',
        'message.bunnidogs.cradle.hatched':'Hatched %s pups. The Cradle is empty.'}
    path = RES/'assets/bunnidogs/lang/en_us.json'; values = json.loads(path.read_text()); values.update(text); path.write_text(json.dumps(values,indent=2)+'\n')
    preview = Image.new('RGB',(1200,430),(40,45,43)); d = ImageDraw.Draw(preview)
    try: font = ImageFont.truetype('DejaVuSans.ttf',17)
    except OSError: font = ImageFont.load_default()
    d.text((20,15),'Nursery Cradle — opaque window, wool bedding, guava frame',font=font,fill=(236,231,205))
    for index,fill in enumerate((0,1,3,4)):
        x = 24+index*230
        preview.paste(fronts[fill].resize((192,192),Image.Resampling.NEAREST),(x,70))
        d.text((x,280),['Empty','Incubating','Developed','Ready'][index],font=font,fill=(230,225,199))
    preview.paste(top.resize((160,160),Image.Resampling.NEAREST),(1000,70))
    d.text((1000,245),'Top / front arrow',font=font,fill=(230,225,199))
    d.text((24,330),'Recipe: WWW / GNG / PPP   (wool / glass + Nest / planks)',font=font,fill=(221,220,193))
    d.text((24,365),'Hatch pad: 3 x 3, one to three blocks in front; clear dry floor and two-block air.',font=font,fill=(190,209,190))
    preview.save(ROOT/'art/cradle_preview.png')
    print('Generated Nursery Cradle textures, block/item models, recipe, drops, unlock, text and preview.')


if __name__ == '__main__': main()
