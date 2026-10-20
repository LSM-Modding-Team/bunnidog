#!/usr/bin/env python3
"""Native commission station and one-time rewards for every wild lineage plus a combined stable line."""
import json
import math
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write
from gen_workshop import cube, tag, drop, unlock, block_item, draw_model

REQUESTS=[
    ('night_guide','Night Guide','Fixed pale coat and bat ears; Senses +20.',50,[('minecraft:emerald',4),('minecraft:glow_berries',8)]),
    ('coastal_scout','Coastal Scout','Fixed seal coat, paddle legs and shore-forager Diet; Senses +10 and Swiftness +10.',50,
        [('minecraft:emerald',6),('minecraft:lead',1),('minecraft:dried_kelp',8)]),
    ('mountain_courier','Mountain Courier','Fixed hoofed legs and Build with capacity 3 or 4; Leap +20, Swiftness +10; equip a bundle.',50,
        [('minecraft:emerald',6),('minecraft:bundle',1)]),
    ('ember_gardener','Ember Gardener','Fixed ember coat and nether-forager Diet; Bloom +10.',50,
        [('minecraft:emerald',4),('minecraft:nether_wart',8),('minecraft:bone_meal',4)]),
    ('frost_steward','Frost Steward','Fixed snow coat, tiny ears and fisher Diet; Bloom +10 and Vitality +20.',50,
        [('minecraft:emerald',4),('minecraft:snowball',4),('minecraft:cod',8)]),
    ('desert_messenger','Desert Messenger','Fixed fennec ears, fleet legs and lean Build; Swiftness +20 and Senses +10.',50,
        [('minecraft:emerald',6),('minecraft:string',4),('minecraft:lead',1)]),
    ('swamp_herbalist','Swamp Herbalist','Fixed moss coat, webbed legs and mushroom-eater Diet; Bloom +10 and Senses +10.',50,
        [('minecraft:emerald',4),('minecraft:bone_meal',4),('minecraft:moss_block',4)]),
    ('end_nursery','End Nursery','Fixed void coat, phase legs, starborn fertility and chorus-eater Diet; Bloom +20.',50,
        [('minecraft:emerald',6),('minecraft:chorus_fruit',4),('bunnidogs:starlit_dust',2)]),
    ('crossing_branches','Crossing Branches','All seven pairs fixed, with at least four distinct wild origins; Senses +20.',100,
        [('minecraft:emerald',12),('bunnidogs:golden_guava',1)])]


def main():
    for ready in (False,True):
        image=Image.new('RGBA',(16,16),(221,206,161)); d=ImageDraw.Draw(image)
        d.rectangle((0,0,15,15),outline=(64,122,71) if ready else (119,88,52),width=2)
        d.line((3,3,12,3),fill=(116,105,69),width=1)
        for y in range(5,13):
            x=5+round(2*math.sin(y*.8)); other=10-round(2*math.sin(y*.8))
            if y%2==0:d.line((x,y,other,y),fill=(158,143,96))
            d.point((x,y),fill=(53,112,103)); d.point((other,y),fill=(90,127,58))
        for x,y in [(2,2),(13,2),(2,13),(13,13)]:d.point((x,y),fill=(233,174,76))
        image.save(RES/f'assets/bunnidogs/textures/block/commission_sheet_{int(ready)}.png')
        body=cube([1,6,5],[15,16,11],'wood')
        sheet=cube([2,7,4.9],[14,15,5.05],'sheet',[0,0,16,16])
        model={'parent':'minecraft:block/block','textures':{'particle':'bunnidogs:block/guava_planks','wood':'bunnidogs:block/guava_planks',
            'sheet':f'bunnidogs:block/commission_sheet_{int(ready)}'},'elements':[
                cube([2,0,6],[4,8,10],'wood'),cube([12,0,6],[14,8,10],'wood'),body,sheet]}
        write(f'assets/bunnidogs/models/block/commission_board_{int(ready)}.json',model)
    write('assets/bunnidogs/blockstates/commission_board.json',{'variants':{
        f'facing={facing},ready={str(ready).lower()}':{'model':f'bunnidogs:block/commission_board_{int(ready)}','y':rotation}
        for facing,rotation in [('north',0),('east',90),('south',180),('west',270)] for ready in (False,True)}})
    block_item('commission_board','commission_board_0'); drop('commission_board'); unlock('commission_board','bunnidogs:whisker')
    tag('data/minecraft/tags/block/mineable/axe.json',['bunnidogs:commission_board'])
    write('data/bunnidogs/recipe/commission_board.json',{'type':'minecraft:crafting_shaped','pattern':['PPP',' W ','CCC'],
        'key':{'P':'#minecraft:planks','W':'bunnidogs:whisker','C':'minecraft:copper_ingot'},'result':{'id':'bunnidogs:commission_board'}})
    language={'block.bunnidogs.commission_board':'Breeder Commission Board',
        'tooltip.bunnidogs.commission.controls':'Empty hands: choose a request, see progress and submit. Paper also submits by hand.',
        'message.bunnidogs.commission.status':'%s: %s',
        'message.bunnidogs.commission.completed':'Commission completed: %s. Animal and cargo stay with you.',
        'message.bunnidogs.commission.ready':'ready; submit one paper',
        'message.bunnidogs.commission.unavailable':'station or animal unavailable',
        'message.bunnidogs.commission.blocked':'keep the marked front block clear above solid ground',
        'message.bunnidogs.commission.absent':'bring one animal onto the front block',
        'message.bunnidogs.commission.crowded':'the front block needs exactly one animal',
        'message.bunnidogs.commission.breeder':'needs a living adult Breeder villager within eight blocks',
        'message.bunnidogs.commission.adult':'needs an adult',
        'message.bunnidogs.commission.parents':'needs an animal with both recorded birth parents',
        'message.bunnidogs.commission.health':'needs at least half health',
        'message.bunnidogs.commission.transported':'dismount the animal and remove passengers; leads are allowed',
        'message.bunnidogs.commission.danger':'needs a calm animal without danger or courtship',
        'message.bunnidogs.commission.genes':'gene pairs do not yet meet the request',
        'message.bunnidogs.commission.stats':'breed the requested stat values',
        'message.bunnidogs.commission.cargo':'equip at least one native bundle',
        'message.bunnidogs.commission.busy':'submission is already in progress',
        'message.bunnidogs.commission.already':'you already completed this request; paper kept'}
    for key,title,requirements,xp,rewards in REQUESTS:
        pools=[{'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':count}]}]}
            for item,count in rewards]
        write(f'data/bunnidogs/loot_table/commissions/{key}.json',{'type':'minecraft:advancement_reward','pools':pools})
        write(f'data/bunnidogs/advancement/commissions/{key}.json',{'parent':'bunnidogs:journey/litter','display':{
            'icon':{'id':'bunnidogs:commission_board'},'title':{'translate':f'commission.bunnidogs.{key}.title'},
            'description':{'translate':f'commission.bunnidogs.{key}.requirements'},'frame':'challenge' if key=='crossing_branches' else 'goal',
            'show_toast':True,'announce_to_chat':False,'hidden':False},
            'criteria':{'submitted':{'trigger':'minecraft:impossible'}},'requirements':[['submitted']],
            'rewards':{'experience':xp,'loot':[f'bunnidogs:commissions/{key}']}})
        language[f'commission.bunnidogs.{key}.title']=title
        language[f'commission.bunnidogs.{key}.requirements']=requirements
        reward=', '.join(f'{count} '+item.split(':')[1].replace('_',' ') for item,count in rewards)
        language[f'commission.bunnidogs.{key}.reward']=f'Once per keeper: {reward}; {xp} XP.'
    path=RES/'assets/bunnidogs/lang/en_us.json'; value=json.loads(path.read_text()); value.update(language); path.write_text(json.dumps(value,indent=2)+'\n')
    preview=Image.new('RGB',(1390,1370),(43,46,39)); d=ImageDraw.Draw(preview)
    titlefont=ImageFont.truetype('DejaVuSans-Bold.ttf',22); font=ImageFont.truetype('DejaVuSans.ttf',17)
    d.text((20,17),'Breeder Commission Board — practical targets for a living bred line',font=titlefont,fill=(231,222,192))
    draw_model(preview,'commission_board_0',(225,240),front=True); draw_model(preview,'commission_board_1',(630,240),front=True)
    d.text((115,347),'Waiting / not qualified',font=font,fill=(219,218,188));d.text((526,347),'Ready; comparator 15',font=font,fill=(191,218,177))
    notes=['One calm parented adult on the clear front block.', 'Keep an actual Breeder villager within eight blocks.',
        'Paper submits; animal, genes and cargo stay.', 'Empty hands open targets, live progress and named controls.',
        'Each request pays native material / XP rewards once per player.']
    for i,line in enumerate(notes): d.text((790,120+40*i),line,font=font,fill=(213,219,191))
    for i,(key,title,requirements,xp,rewards) in enumerate(REQUESTS):
        y=420+i*95
        d.text((24,y),title,font=titlefont,fill=(226,216,179))
        d.text((24,y+33),requirements,font=font,fill=(207,214,185))
        d.text((24,y+57),language[f'commission.bunnidogs.{key}.reward'],font=font,fill=(178,204,168))
    d.text((24,1335),'Preview from shipped native models; the selected request and personal completion are read at the station.',font=font,fill=(178,191,175))
    preview.save(ROOT/'art/commission_preview.png')
    print('Generated nine Breeder commissions, native one-time rewards, station art, recipe and controls.')


if __name__=='__main__':main()
