"""Breeder workshop assets: tier carriers, screw press, coat rugs and sample jars.

Run from the repository root. Native JSON models and recipe/data files are the
source of the preview; no changes to the approved animal mesh are required.
"""
import json
import math
import re
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from gen_juice import draw as bottle, PLAIN
from gen_bunnidog import COATS

TIERS = [('guava',(184,214,92)),('golden_guava',(236,190,67)),
         ('diamond_guava',(85,209,214)),('netherite_guava',(105,93,106)),
         ('nether_star_guava',(197,235,226)),('beacon_guava',(99,235,201)),
         ('dragon_egg_guava',(151,90,205)),('command_block_guava',(226,143,82))]
TEXT = {}
COAT_ORDER = re.findall(r'(?:spec|rare|wild)\("([^"]+)"',
    (ROOT/'src/main/java/com/bunnidogs/mod/genetics/Gene.java').read_text().split('COAT("coat"',1)[1].split('LEGS("legs"',1)[0])


def unlock(name, ingredient, category='tools'):
    write(f'data/bunnidogs/advancement/recipes/{category}/{name}.json',{
        'parent':'minecraft:recipes/root','criteria':{
            'has_input':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':ingredient}]}},
            'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':f'bunnidogs:{name}'}}},
        'requirements':[['has_input','has_recipe']],'rewards':{'recipes':[f'bunnidogs:{name}']}})


def drop(name):
    write(f'data/bunnidogs/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,
        'entries':[{'type':'minecraft:item','name':f'bunnidogs:{name}'}],
        'conditions':[{'condition':'minecraft:survives_explosion'}]}]})


def tag(path, values):
    path = RES/path
    existing = json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
    existing['values'] = list(dict.fromkeys(existing['values']+values))
    write(str(path.relative_to(RES)),existing)


def cube(a,b,texture,uv=None):
    face = {'texture':'#'+texture}
    if uv: face['uv'] = uv
    return {'from':a,'to':b,'faces':{side:dict(face) for side in ('up','down','north','south','east','west')}}


def block_item(name, model=None):
    write(f'assets/bunnidogs/items/{name}.json',{'model':{'type':'minecraft:model','model':'bunnidogs:block/'+(model or name)}})


def carriers():
    icons = []
    for i,(guava,color) in enumerate(TIERS):
        name = guava+'_juice'
        if i == 0:
            icons.append(Image.open(RES/'assets/bunnidogs/textures/item/guava_juice.png').convert('RGBA')); continue
        shade = lambda factor: tuple(min(255,int(c*factor)) for c in color)
        image = bottle({'liquid':[shade(1.18),color,shade(.7)],'creature':False})
        draw = ImageDraw.Draw(image)
        draw.line((6,4,9,4),fill=color,width=1)
        # Marks on the label make neighboring dark / pale tiers distinguishable too.
        draw.rectangle((5,10,10,12),fill=(239,226,190))
        for mark in range(i): image.putpixel((6+mark%4,10+mark//4),color+(255,))
        image.save(RES/f'assets/bunnidogs/textures/item/{name}.png'); icons.append(image)
        write(f'assets/bunnidogs/items/{name}.json',{'model':{'type':'minecraft:model','model':f'bunnidogs:item/{name}'}})
        write(f'assets/bunnidogs/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'bunnidogs:item/{name}'}})
        write(f'data/bunnidogs/recipe/{name}.json',{'type':'minecraft:crafting_shapeless',
            'ingredients':['bunnidogs:'+guava,'minecraft:glass_bottle'],'result':{'id':'bunnidogs:'+name}})
        unlock(name,'bunnidogs:'+guava,'food')
        TEXT['item.bunnidogs.'+name] = guava.replace('_',' ').title()+' Juice'
    return icons


def press():
    wood = Image.open(RES/'assets/bunnidogs/textures/block/guava_planks.png').convert('RGBA')
    for active in (False,True):
        texture = Image.new('RGBA',(16,16),(133,75,44)); d = ImageDraw.Draw(texture)
        for y in range(16):
            for x in range(16):
                texture.putpixel((x,y),(160+(x*3+y*7)%23,94+(x+y)%14,55,255))
        d.line((0,0,15,0),fill=(217,154,91)); d.line((0,15,15,15),fill=(91,55,39))
        d.rectangle((5,5,10,11),fill=(56,66,61)); d.rectangle((6,6,9,10),fill=(178,210,86) if active else (82,94,71))
        d.point((6,6),fill=(237,243,186)); texture.save(RES/f'assets/bunnidogs/textures/block/juicer_{int(active)}.png')
        elements = [cube([1,0,1],[15,2,15],'wood'),cube([2,2,2],[14,4,14],'press'),
            cube([2,4,3],[4,14,13],'wood'),cube([12,4,3],[14,14,13],'wood'),
            cube([2,14,3],[14,16,13],'press'),cube([7,7,7],[9,14,9],'iron'),
            cube([4,6,4],[12,8,12],'press'),cube([4,4,4],[12,5,12],'wood'),
            cube([5,2,1],[11,6,3],'press'),cube([6,11,6],[10,12,10],'iron')]
        write(f'assets/bunnidogs/models/block/juicer_{int(active)}.json',{'parent':'minecraft:block/block',
            'textures':{'particle':'bunnidogs:block/guava_planks','wood':'bunnidogs:block/guava_planks',
                'press':f'bunnidogs:block/juicer_{int(active)}','iron':'minecraft:block/iron_block'},'elements':elements})
    write('assets/bunnidogs/blockstates/juicer.json',{'variants':{
        f'facing={facing},working={str(active).lower()}':{'model':f'bunnidogs:block/juicer_{int(active)}','y':rotation}
        for facing,rotation in [('north',0),('east',90),('south',180),('west',270)] for active in (False,True)}})
    block_item('juicer','juicer_0'); drop('juicer')
    write('data/bunnidogs/recipe/juicer.json',{'type':'minecraft:crafting_shaped','pattern':['PIP','PBP','CCC'],
        'key':{'P':'#minecraft:planks','I':'minecraft:iron_ingot','B':'minecraft:glass_bottle','C':'minecraft:copper_ingot'},
        'result':{'id':'bunnidogs:juicer'}})
    unlock('juicer','bunnidogs:guava')
    tag('data/minecraft/tags/block/mineable/axe.json',['bunnidogs:juicer'])
    TEXT.update({'block.bunnidogs.juicer':'Juicer',
        'tooltip.bunnidogs.juice.carrier':'Infusion quality floor: %s. Seed genes and drift stay the same.',
        'tooltip.bunnidogs.juice.infuse':'Use a seed with this bottle off-hand, or load a Juicer.',
        'message.bunnidogs.juicer.inserted':'Loaded %s. Top: fruit/seed; side: bottle/juice; bottom: result.',
        'message.bunnidogs.juicer.inputs':'Load guava + bottle, or a valid seed + uninfused juice.',
        'message.bunnidogs.juicer.status':'Inputs %s / %s; output %s; %s%% (%s). Sneak-empty recovers inputs.'})
    return wood


def rugs():
    images = []
    for coat,look in COATS.items():
        name = coat+'_pelt_rug'; image = Image.new('RGBA',(32,32)); shape = set()
        for y in range(32):
            for x in range(32):
                body = ((x-15.5)/9.8)**2+((y-14.5)/12.7)**2 <= 1
                paw = any(((x-cx)/4.5)**2+((y-cy)/4.0)**2 <= 1 for cx,cy in [(5,7),(26,7),(5,23),(26,23)])
                tail = 14 <= x <= 17 and 25 <= y <= 30
                if body or paw or tail: shape.add((x,y))
        for x,y in shape:
            edge = any((x+dx,y+dy) not in shape for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)])
            shade = 'dark' if edge else 'light' if (x*7+y*13)%23 < 4 else 'base'
            color = look[shade]
            if 'patches' in look and not edge and any((x-cx)**2+(y-cy)**2 < radius**2 for cx,cy,radius in [(12,9,3),(21,15,4),(12,22,3)]): color = look['patches']
            image.putpixel((x,y),tuple(color[:3])+(255,))
        image.save(RES/f'assets/bunnidogs/textures/block/{name}.png'); images.append((coat,image))
        write(f'assets/bunnidogs/models/block/{name}.json',{'parent':'minecraft:block/carpet','render_type':'minecraft:cutout',
            'textures':{'wool':f'bunnidogs:block/{name}'}})
        write(f'assets/bunnidogs/blockstates/{name}.json',{'variants':{
            'facing='+facing:{'model':'bunnidogs:block/'+name,'y':rotation}
            for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]}})
        block_item(name); drop(name)
        write(f'data/bunnidogs/recipe/{name}.json',{'type':'minecraft:crafting_shapeless',
            'ingredients':['bunnidogs:'+coat+'_pelt','bunnidogs:'+coat+'_pelt','minecraft:string'],
            'result':{'id':'bunnidogs:'+name}})
        unlock(name,'bunnidogs:'+coat+'_pelt','decorations')
        TEXT['block.bunnidogs.'+name] = coat.title()+' Pelt Rug'
    names = ['bunnidogs:'+coat+'_pelt_rug' for coat in COATS]
    for kind in ('item','block'):
        tag(f'data/bunnidogs/tags/{kind}/pelt_rugs.json',names)
        tag(f'data/minecraft/tags/{kind}/carpets.json',names)
    return images


def jars():
    glass = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(glass)
    d.rectangle((0,0,15,15),outline=(123,164,168),width=1)
    d.line((1,1,5,1),fill=(226,242,229)); d.line((1,2,1,7),fill=(201,229,219))
    d.line((13,9,13,13),fill=(147,190,186)); glass.save(RES/'assets/bunnidogs/textures/block/specimen_glass.png')
    cork = Image.new('RGBA',(16,16));
    for y in range(16):
        for x in range(16): cork.putpixel((x,y),(148+(x*11+y*7)%29,103+(x+y)%14,61,255))
    cork.save(RES/'assets/bunnidogs/textures/block/specimen_cork.png')
    label = Image.new('RGBA',(16,16),(224,213,175)); d = ImageDraw.Draw(label)
    d.line((2,5,13,5),fill=(102,91,67)); d.line((2,10,9,10),fill=(102,91,67))
    label.save(RES/'assets/bunnidogs/textures/block/specimen_label.png')
    variants = {}
    for index,coat in enumerate([None]+COAT_ORDER):
        elements = [cube([3,0,3],[13,1,13],'wood'),cube([3,1,3],[13,12,13],'glass',[0,0,16,16]),
                    cube([3,12,3],[13,14,13],'cork'),cube([2.5,11.5,2.5],[13.5,12.5,13.5],'cork'),
                    cube([5,1,2.95],[11,3,3.05],'label',[0,0,16,16])]
        textures = {'particle':'bunnidogs:block/specimen_glass','glass':'bunnidogs:block/specimen_glass',
            'cork':'bunnidogs:block/specimen_cork','wood':'bunnidogs:block/guava_planks','label':'bunnidogs:block/specimen_label'}
        if coat:
            look = COATS[coat]; vial = Image.new('RGBA',(16,16),tuple(look['base'][:3])+(255,)); d = ImageDraw.Draw(vial)
            for y in range(16):
                x = 7+round(3*math.sin(y*.7)); d.rectangle((x,y,x+1,y),fill=tuple(look['light'][:3])+(255,))
                if y%3 == 0: d.line((5,y,10,y),fill=tuple(look['dark'][:3])+(255,))
            vial.save(RES/f'assets/bunnidogs/textures/block/specimen_{coat}.png')
            textures['sample'] = 'bunnidogs:block/specimen_'+coat
            elements += [cube([6,3,6],[10,9,10],'sample',[0,0,16,16]),cube([6,9,6],[10,10,10],'cork')]
        name = 'specimen_jar_'+str(index)
        write(f'assets/bunnidogs/models/block/{name}.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout',
            'textures':textures,'elements':elements})
        variants['specimen='+str(index)] = {'model':'bunnidogs:block/'+name}
    write('assets/bunnidogs/blockstates/specimen_jar.json',{'variants':variants}); block_item('specimen_jar','specimen_jar_0'); drop('specimen_jar')
    write('data/bunnidogs/recipe/specimen_jar.json',{'type':'minecraft:crafting_shaped','pattern':[' P ','G G',' G '],
        'key':{'P':'#minecraft:wooden_slabs','G':'minecraft:glass'},'result':{'id':'bunnidogs:specimen_jar','count':2}})
    unlock('specimen_jar','bunnidogs:tissue_sample','decorations')
    TEXT.update({'block.bunnidogs.specimen_jar':'Specimen Jar','message.bunnidogs.jar.empty':'Insert a Tissue Sample. Use to read; sneak with both hands empty to recover it.',
        'screen.bunnidogs.lineage.name':'Name lineage','screen.bunnidogs.lineage.hint':'Up to 40 characters; empty clears the name.',
        'screen.bunnidogs.lineage.local':'Names belong to this book’s exact genotype.',
        'screen.bunnidogs.lineage.stale':'Book changed. Reopen it before editing.',
        'screen.bunnidogs.lineage.saving':'Saving…','screen.bunnidogs.lineage.invalid':'Use 40 characters or fewer, without formatting.',
        'screen.bunnidogs.records.filter_help':'Filter: trait/tag words or line:name; shown:bat, carried:glow, adult, pup; senses>=20, bloom<10 or speed=5. Space-separated terms all match. Clear to restore original record numbers and parent links.'})


def model_faces(name):
    """Project the real native model's exposed faces for a small headless art preview."""
    data = json.loads((RES/f'assets/bunnidogs/models/block/{name}.json').read_text())
    for element in data['elements']:
        x0,y0,z0 = element['from']; x1,y1,z1 = element['to']
        for side,points,uv in [('south',[(x0,y1,z1),(x1,y1,z1),(x1,y0,z1),(x0,y0,z1)],[x0,16-y1,x1,16-y0]),
                            ('east',[(x1,y1,z1),(x1,y1,z0),(x1,y0,z0),(x1,y0,z1)],[16-z1,16-y1,16-z0,16-y0]),
                            ('up',[(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],[x0,z0,x1,z1])]:
            face = element['faces'][side]; texture = data['textures'][face['texture'][1:]]
            path = RES/('assets/'+texture.replace(':','/textures/')+'.png')
            img = Image.open(path).convert('RGBA') if path.exists() else Image.new('RGBA',(16,16),(188,191,190,255))
            yield sum(x+y*.8+z for x,y,z in points)/4,points,img,face.get('uv',uv),side


def draw_model(preview,name,origin):
    d = ImageDraw.Draw(preview,'RGBA'); ox,oy = origin
    for _,points,img,uv,side in sorted(model_faces(name),key=lambda face:face[0]):
        p0,p1,_,p3 = points
        project = lambda u,v: (ox+(p0[0]+u*(p1[0]-p0[0])+v*(p3[0]-p0[0])-p0[2]-u*(p1[2]-p0[2])-v*(p3[2]-p0[2]))*8,
            oy+((p0[0]+u*(p1[0]-p0[0])+v*(p3[0]-p0[0])+p0[2]+u*(p1[2]-p0[2])+v*(p3[2]-p0[2]))*.42-p0[1]-u*(p1[1]-p0[1])-v*(p3[1]-p0[1]))*8)
        steps_u = max(1,math.ceil(abs(uv[2]-uv[0])*img.width/16)); steps_v = max(1,math.ceil(abs(uv[3]-uv[1])*img.height/16))
        for row in range(steps_v):
            for col in range(steps_u):
                u,v = (col+.5)/steps_u,(row+.5)/steps_v
                color = img.getpixel((min(img.width-1,max(0,int((uv[0]+u*(uv[2]-uv[0]))*img.width/16))),
                                     min(img.height-1,max(0,int((uv[1]+v*(uv[3]-uv[1]))*img.height/16)))))
                if color[3] < 128: continue
                factor = 1 if side == 'up' else .86 if side == 'south' else .7
                color = tuple(int(c*factor) for c in color[:3])+(255,)
                d.polygon([project(col/steps_u,row/steps_v),project((col+1)/steps_u,row/steps_v),
                    project((col+1)/steps_u,(row+1)/steps_v),project(col/steps_u,(row+1)/steps_v)],fill=color)


def main():
    icons = carriers(); press(); hides = rugs(); jars()
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        values = json.loads(path.read_text()); values.update(TEXT); path.write_text(json.dumps(values,indent=2)+'\n')
    preview = Image.new('RGB',(1120,1020),(43,42,40)); d = ImageDraw.Draw(preview)
    font = ImageFont.truetype('DejaVuSans.ttf',16)
    d.text((20,15),'Tiered guava juice — richer carriers preserve the seed’s inherited traits',font=font,fill=(238,226,198))
    for i,icon in enumerate(icons):
        preview.paste(icon.resize((112,112),Image.Resampling.NEAREST),(14+i*138,62),icon.resize((112,112),Image.Resampling.NEAREST))
        d.text((18+i*138,188),TIERS[i][0].replace('_guava','').replace('_',' '),font=font,fill=(230,219,198))
    d.text((20,244),'Juicer: guava + bottle → matching juice; seed + juice → one frozen infusion.',font=font,fill=(223,218,197))
    d.text((20,276),'100 loaded ticks; powered pauses; top / side inputs, bottom output.',font=font,fill=(190,213,189))
    draw_model(preview,'juicer_1',(130,440)); draw_model(preview,'specimen_jar_0',(410,440)); draw_model(preview,'specimen_jar_7',(690,440)); draw_model(preview,'specimen_jar_12',(970,440))
    d.text((32,518),'Screw press',font=font,fill=(232,222,194)); d.text((322,518),'Empty jar',font=font,fill=(232,222,194))
    d.text((598,518),'Moss sample',font=font,fill=(232,222,194)); d.text((882,518),'Glow sample',font=font,fill=(232,222,194))
    d.text((20,575),'Twelve coat rugs — two matching pelts and string',font=font,fill=(238,226,198))
    for i,(coat,image) in enumerate(hides):
        x = 28+i%6*182; y = 620+i//6*186; large = image.resize((144,144),Image.Resampling.NEAREST)
        preview.paste(large,(x,y),large); d.text((x+4,y+147),coat,font=font,fill=(230,219,198))
    preview.save(ROOT/'art/workshop_preview.png')
    print('Generated breeder workshop assets and tier recipes.')


if __name__ == '__main__': main()
