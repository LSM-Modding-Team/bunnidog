#!/usr/bin/env python3
"""Apex focus sprites/recipes; phenotype mesh remains the approved creature. No region-specific translations."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
ROLES={'apex':('Brute','Bruto',(185,135,83),'minecraft:iron_ingot'),
       'prismmaw':('Prismmaw','Fauces prismáticas',(169,104,233),'minecraft:amethyst_shard'),
       'broodcaller':('Broodcaller','Invocador de crías',(110,177,78),'minecraft:spider_eye'),
       'blastmaw':('Blastmaw','Fauces explosivas',(236,132,43),'minecraft:gunpowder'),
       'lifebinder':('Lifebinder','Vinculador vital',(222,92,145),'minecraft:ghast_tear')}
def write(path,value):
    p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n')
def main():
    labels={lang:{} for lang in ['en_us','es_pe']};icons=[]
    for role,(en,es,color,ingredient) in ROLES.items():
        for flying in [False,True]:
            entity=('flying_' if flying else '')+role
            for lang,name in [('en_us',en),('es_pe',es)]:
                labels[lang]['entity.bunnidogs.'+entity]=(('Flying ' if lang=='en_us' else 'Ápice volador: ') if flying else '')+name if role!='apex' or flying else ('Apex of the Line' if lang=='en_us' else 'Ápice del linaje')
            if role=='apex' and not flying:continue
            item=entity+'_sigil';im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
            d.rectangle((3,1,12,14),fill=(65,48,41));d.rectangle((4,2,11,13),fill=(224,207,167));d.rectangle((5,3,10,12),fill=(194,167,121))
            d.ellipse((4,4,11,11),fill=color,outline=(49,36,53));d.rectangle((7,5,8,10),fill=(255,241,196))
            if role=='prismmaw':d.line((3,8,12,8),fill=(243,218,255));d.point((8,7),fill='white')
            elif role=='broodcaller':
                for x,y in [(5,6),(9,6),(7,10)]:d.rectangle((x,y,x+1,y+1),fill=(35,64,26))
            elif role=='blastmaw':d.line((5,10,9,5,9,9,6,12),fill=(255,238,133),width=1)
            elif role=='lifebinder':d.line((5,6,7,5,10,6,7,10,5,6),fill=(255,208,222),width=1)
            if flying:d.line((1,4,1,7,4,9),fill=(221,238,255),width=2);d.line((14,4,14,7,11,9),fill=(221,238,255),width=2)
            path=RES/f'assets/bunnidogs/textures/item/{item}.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path);icons.append((item,im))
            write(Path(f'assets/bunnidogs/models/item/{item}.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+item}})
            write(Path(f'assets/bunnidogs/items/{item}.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+item}})
            ingredients=['minecraft:paper','minecraft:copper_ingot',ingredient]
            if flying:ingredients.append('minecraft:feather')
            write(Path(f'data/bunnidogs/recipe/{item}.json'),{'type':'minecraft:crafting_shapeless','ingredients':ingredients,'result':{'id':'bunnidogs:'+item}})
            write(Path(f'data/bunnidogs/advancement/recipes/combat/{item}.json'),{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':ingredient}]}},'recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:'+item}}},'requirements':[['ingredient','recipe']],'rewards':{'recipes':['bunnidogs:'+item]}})
            labels['en_us']['item.bunnidogs.'+item]=('Flying ' if flying else '')+en+' Sigil'
            labels['es_pe']['item.bunnidogs.'+item]='Sigilo de '+es.lower()+(' volador' if flying else '')
    for lang in labels:
        labels[lang].update({'effect.bunnidogs.apex_stagger':'Hooked' if lang=='en_us' else 'Enganchado',
            'entity.bunnidogs.apex_minion':'Enraged Brood' if lang=='en_us' else 'Cría enfurecida',
            'entity.bunnidogs.apex_hook':'Apex Hook' if lang=='en_us' else 'Gancho del Ápice',
            'entity.bunnidogs.apex.charging_kind':'%s — gathering force' if lang=='en_us' else '%s — acumulando fuerza',
            'tooltip.bunnidogs.apex_sigil':'Use on an Apex Crucible with a Tissue Sample in your other hand.' if lang=='en_us' else 'Úsalo en un Crisol del Ápice con una muestra de tejido en la otra mano.',
            'care.bunnidogs.apex.focus_cost':'Costs one sigil, one sample and five different legends.' if lang=='en_us' else 'Consume un sigilo, una muestra y cinco piezas legendarias distintas.',
            'death.attack.bunnidogs.apex_backlash.item':'%1$s exhausted its volatile heart fighting %2$s with %3$s' if lang=='en_us' else '%1$s agotó su corazón volátil al combatir con %2$s y %3$s',
            'care.bunnidogs.apex.kind':'Encounter: %s' if lang=='en_us' else 'Encuentro: %s',
            'item.bunnidogs.apex_crest.variant':'%s Crest' if lang=='en_us' else 'Emblema de %s',
            'death.attack.bunnidogs.apex_drain':'%1$s was drained by %2$s' if lang=='en_us' else '%2$s drenó la vida de %1$s',
            'death.attack.bunnidogs.apex_drain.item':'%1$s was drained by %2$s using %3$s' if lang=='en_us' else '%2$s drenó la vida de %1$s con %3$s',
            'death.attack.bunnidogs.apex_backlash':'%1$s exhausted its volatile heart' if lang=='en_us' else '%1$s agotó su corazón volátil',
            'death.attack.bunnidogs.apex_backlash.player':'%1$s exhausted its volatile heart fighting %2$s' if lang=='en_us' else '%1$s agotó su corazón volátil al combatir con %2$s'})
        p=RES/f'assets/bunnidogs/lang/{lang}.json';data=json.loads(p.read_text());data.update(labels[lang]);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
    im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im);d.line((2,8,15,8,10,3,8,15,5,5,15,12),fill=(236,147,199),width=2)
    p=RES/'assets/bunnidogs/textures/mob_effect/apex_stagger.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
    preview=Image.new('RGB',(576,200),(35,31,37));d=ImageDraw.Draw(preview);d.text((10,8),'Apex ritual focuses | ground / flying | native item sprites',fill='white')
    for i,(name,im) in enumerate(icons):
        x=(i%5)*114;y=(i//5)*84+36;preview.paste(im.resize((48,48),Image.Resampling.NEAREST),(x+30,y),im.resize((48,48),Image.Resampling.NEAREST));d.text((x+2,y+51),name.replace('_sigil',''),fill=(222,211,218))
    preview.save(ROOT/'art/apex_types_preview.png')
if __name__=='__main__':main()
