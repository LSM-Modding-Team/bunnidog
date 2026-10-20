#!/usr/bin/env python3
"""Pedigree Comparator block, live reference/output overlays, crafting and interaction text."""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from gen_workshop import cube, block_item, drop, unlock, tag, draw_model


def main():
    for name,base in [('detector_copper',(156,106,65)),('detector_ivory',(218,215,192)),
                      ('detector_reference',(81,175,181)),('detector_signal',(233,101,62))]:
        image=Image.new('RGBA',(16,16),base+(255,)); d=ImageDraw.Draw(image)
        d.rectangle((0,0,15,15),outline=tuple(max(0,c-45) for c in base))
        d.line((1,1,14,1),fill=tuple(min(255,c+30) for c in base))
        if name == 'detector_copper':
            for x in (2,13):
                for y in (2,13): d.rectangle((x,y,x+1,y+1),fill=(218,172,95))
        if name == 'detector_ivory':
            for y in range(3,14,3):
                d.line((5,y,10,y),fill=(163,135,99))
                d.point((5 if y%2 else 6,y),fill=(170,77,79)); d.point((10 if y%2 else 9,y),fill=(71,122,158))
        if name == 'detector_reference':
            d.line((2,3,7,3),fill=(201,239,225)); d.line((2,4,2,10),fill=(157,219,208))
            d.line((5,5,10,11),fill=(44,111,127),width=2); d.line((10,5,5,11),fill=(234,224,155),width=2)
        image.save(RES/f'assets/bunnidogs/textures/block/{name}.png')
    textures={'particle':'bunnidogs:block/detector_copper','copper':'bunnidogs:block/detector_copper',
              'ivory':'bunnidogs:block/detector_ivory','reference':'bunnidogs:block/detector_reference','signal':'bunnidogs:block/detector_signal'}
    base=[cube([2,0,2],[14,3,14],'copper'),cube([3,3,5],[13,6,13],'ivory'),
          cube([3,3,3],[5,12,6],'copper'),cube([11,3,3],[13,12,6],'copper'),
          cube([4,10,3],[12,13,6],'copper'),cube([5,5,2.5],[11,10,4],'ivory'),
          cube([7,3,2.1],[9,3.5,3.1],'copper')]
    reference=[cube([6,6,2.35],[10,9,2.5],'reference',[0,0,16,16])]
    signal=[cube([7,3.5,2.1],[9,3.7,3.1],'signal',[0,0,16,16])]
    for name,elements in [('pedigree_comparator',base),('pedigree_comparator_reference',reference),('pedigree_comparator_signal',signal)]:
        write(f'assets/bunnidogs/models/block/{name}.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements})
    parts=[]
    for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:
        parts.append({'when':{'facing':facing},'apply':{'model':'bunnidogs:block/pedigree_comparator','y':rotation}})
        parts.append({'when':{'facing':facing,'reference':'true'},'apply':{'model':'bunnidogs:block/pedigree_comparator_reference','y':rotation}})
        parts.append({'when':{'facing':facing,'power':'|'.join(str(i) for i in range(1,16))},
                      'apply':{'model':'bunnidogs:block/pedigree_comparator_signal','y':rotation}})
    write('assets/bunnidogs/blockstates/pedigree_comparator.json',{'multipart':parts})
    block_item('pedigree_comparator'); drop('pedigree_comparator')
    write('data/bunnidogs/recipe/pedigree_comparator.json',{'type':'minecraft:crafting_shaped','pattern':['QGQ','CWC',' R '],
        'key':{'Q':'minecraft:quartz','G':'minecraft:glass','C':'minecraft:copper_ingot','W':'bunnidogs:whisker','R':'minecraft:comparator'},
        'result':{'id':'bunnidogs:pedigree_comparator'}})
    unlock('pedigree_comparator','bunnidogs:whisker'); tag('data/minecraft/tags/block/mineable/pickaxe.json',['bunnidogs:pedigree_comparator'])
    text={'block.bunnidogs.pedigree_comparator':'Pedigree Comparator',
        'tooltip.bunnidogs.detector.controls':'Empty hands: reference, signal controls and Show front pad. Held samples copy for free; sneak-empty clears.',
        'message.bunnidogs.detector.status':'%s: %s. Output %s.',
        'message.bunnidogs.detector.exact':'Exact mode', 'message.bunnidogs.detector.analog':'Analog mode',
        'message.bunnidogs.detector.empty':'copy a Tissue Sample first',
        'message.bunnidogs.detector.unloaded':'front pad is unloaded or outside the world border',
        'message.bunnidogs.detector.blocked':'keep the front block and its headroom empty',
        'message.bunnidogs.detector.absent':'no animal centered on the front pad',
        'message.bunnidogs.detector.crowded':'use exactly one animal on the front pad',
        'message.bunnidogs.detector.unavailable':'unavailable; the Apex cannot be sorted',
        'message.bunnidogs.detector.reading':'%s of 7 gene pairs match',
        'message.bunnidogs.detector.reference':'Copied genome: %s'}
    path=RES/'assets/bunnidogs/lang/en_us.json'; language=json.loads(path.read_text()); language.update(text)
    path.write_text(json.dumps(language,indent=2)+'\n')
    preview=Image.new('RGB',(920,330),(42,43,39)); d=ImageDraw.Draw(preview); font=ImageFont.truetype('DejaVuSans.ttf',17)
    d.text((18,14),'Pedigree Comparator — sample reference, live front-pad gene pairs',font=font,fill=(235,222,189))
    for i,(layers,label) in enumerate([((),'No reference'),(('pedigree_comparator_reference',),'Reference copied'),
            (('pedigree_comparator_reference','pedigree_comparator_signal'),'Live output')]):
        draw_model(preview,'pedigree_comparator',(155+305*i,180),front=True,layers=layers)
        d.text((65+305*i,270),label,font=font,fill=(220,215,185))
    preview.save(ROOT/'art/pedigree_comparator_preview.png')
    print('Generated Pedigree Comparator models/overlays, recipe, text and preview.')


if __name__ == '__main__':
    main()
