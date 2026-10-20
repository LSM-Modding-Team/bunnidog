#!/usr/bin/env python3
"""Trail Whistle, deliberate companion training and native feedback."""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write


def main():
    img=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(img)
    d.line([(4,10),(2,12),(3,14),(7,14),(8,12)],fill=(102,85,52),width=1)
    d.polygon([(4,7),(10,2),(13,3),(14,6),(8,12),(5,11)],fill=(86,61,45))
    d.polygon([(4,7),(10,2),(13,3),(12,6),(7,11),(5,10)],fill=(188,114,68))
    d.line([(5,7),(10,3),(12,3)],fill=(239,178,111),width=1)
    d.polygon([(9,5),(11,4),(12,5),(10,7)],fill=(49,53,44))
    d.line([(6,9),(7,10)],fill=(232,161,97),width=1)
    d.line([(3,13),(4,14),(6,14)],fill=(165,154,99),width=1)
    img.save(RES/'assets/bunnidogs/textures/item/trail_whistle.png')
    write('assets/bunnidogs/models/item/trail_whistle.json',{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/trail_whistle'}})
    write('assets/bunnidogs/items/trail_whistle.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/trail_whistle'}})
    write('data/bunnidogs/recipe/trail_whistle.json',{'type':'minecraft:crafting_shaped','pattern':[' C ',' SW',' S '],
        'key':{'C':'minecraft:copper_ingot','S':'#bunnidogs:whistle_cords','W':'#bunnidogs:whistle_tips'},'result':{'id':'bunnidogs:trail_whistle'}})
    write('data/bunnidogs/tags/item/whistle_cords.json',{'replace':False,'values':['minecraft:string','bunnidogs:sinew']})
    write('data/bunnidogs/tags/item/whistle_tips.json',{'replace':False,'values':['minecraft:feather','bunnidogs:whisker']})
    write('data/bunnidogs/advancement/recipes/tools/trail_whistle.json',{'parent':'minecraft:recipes/root','criteria':{
        'has_whisker':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:whisker'}]}},
        'has_feather':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:feather'}]}},
        'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:trail_whistle'}}},
        'requirements':[['has_whisker','has_feather','has_recipe']],'rewards':{'recipes':['bunnidogs:trail_whistle']}})
    write('data/bunnidogs/advancement/trail_partner.json',{'parent':'bunnidogs:journey/home','display':{
        'icon':{'id':'bunnidogs:trail_whistle'},'title':{'translate':'advancements.bunnidogs.trail_partner.title'},
        'description':{'translate':'advancements.bunnidogs.trail_partner.description'},'frame':'goal','show_toast':True,'announce_to_chat':False,'hidden':False},
        'criteria':{'trained':{'trigger':'minecraft:impossible'}},'requirements':[['trained']],'rewards':{'experience':25}})
    language={
        'item.bunnidogs.trail_whistle':'Trail Whistle',
        'tooltip.bunnidogs.trail.training':'Train a calm loyal/stoic adult; one plain guava in the other hand.',
        'tooltip.bunnidogs.trail.controls':'Animal: follow/wait; sneak: release. Air: call; sneak-air: return home.',
        'message.bunnidogs.trail.unavailable':'This animal cannot train here.',
        'message.bunnidogs.trail.adult':'Trail training needs an adult.',
        'message.bunnidogs.trail.health':'Training needs at least half health.',
        'message.bunnidogs.trail.temperament':'Breed expressed loyal or stoic temperament for trail training.',
        'message.bunnidogs.trail.danger':'Wait until the animal is calm and not courting.',
        'message.bunnidogs.trail.food':'Hold one plain guava in the other hand to train this partner.',
        'message.bunnidogs.trail.other_keeper':'Another keeper trained this partner.',
        'message.bunnidogs.trail.busy':'Training is already in progress.',
        'message.bunnidogs.trail.trained':'Trail partner trained. Following you; its home is preserved.',
        'message.bunnidogs.trail.train_first':'Train this partner with a Trail Whistle before enabling provisions.',
        'message.bunnidogs.trail.provisions_on':'Trail provisions enabled: suitable ordinary food from carried bundles can heal injuries.',
        'message.bunnidogs.trail.provisions_off':'Trail provisions disabled. Carried food stays in the bags.',
        'message.bunnidogs.trail.provisions_enabled':'enabled; own bundle food, normal meal recovery; upgraded guavas kept',
        'message.bunnidogs.trail.provisions_disabled':'disabled; use a bowl to opt in',
        'message.bunnidogs.care.provisions':'Trail provisions: %s',
        'message.bunnidogs.trail.follow':'Following you on safe ground.',
        'message.bunnidogs.trail.wait':'Waiting here. Danger can still interrupt.',
        'message.bunnidogs.trail.released':'Training released. Ordinary resident behavior restored.',
        'message.bunnidogs.trail.called':'Called %s nearby trail partners.',
        'message.bunnidogs.trail.sent_home':'Sent %s nearby partners back to resident/home routines.',
        'message.bunnidogs.trail.inspect':'%s; %s. Trail orders pause home chores.',
        'message.bunnidogs.trail.order.follow':'following',
        'message.bunnidogs.trail.order.wait':'waiting',
        'message.bunnidogs.trail.order.resident':'resident',
        'message.bunnidogs.trail.yours':'trained by you',
        'message.bunnidogs.trail.other':'trained by another keeper',
        'message.bunnidogs.care.trail':'Trail partner: %s',
        'message.bunnidogs.foraging.trail':'paused by a trail order; hand feeding still works',
        'message.bunnidogs.gardening.trail':'paused by a trail order; work preference preserved',
        'advancements.bunnidogs.trail_partner.title':'A Partner for the Path',
        'advancements.bunnidogs.trail_partner.description':'Train a loyal or stoic adult with a Trail Whistle'}
    path=RES/'assets/bunnidogs/lang/en_us.json'; value=json.loads(path.read_text()); value.update(language); path.write_text(json.dumps(value,indent=2)+'\n')
    preview=Image.new('RGB',(1060,490),(39,47,41)); draw=ImageDraw.Draw(preview)
    preview.paste(img.resize((288,288),Image.Resampling.NEAREST),(24,75),img.resize((288,288),Image.Resampling.NEAREST))
    font=ImageFont.truetype('DejaVuSans.ttf',18); title=ImageFont.truetype('DejaVuSans-Bold.ttf',22)
    draw.text((24,20),'Trail Whistle — a trained partner, with its own genes and hazards',font=title,fill=(229,216,179))
    lines=['Copper + 2 string/Sinew + feather/Whisker; reusable.',
        'Train a calm loyal/stoic adult; one plain guava in the other hand.',
        'Empty-hand care: train, order, or release the partnership.',
        'Air: call nearby partners. Sneak-air: resume resident routines.',
        'Bred ears and Senses affect hearing; no teleport or remote loading.',
        'Safe dry walking; panic and native transport retain priority.',
        'Care panel or bowl: opt in to ordinary carried-food meals.',
        'Cargo, care and training survive ordinary saves / baskets.']
    for i,line in enumerate(lines): draw.text((335,76+42*i),line,font=font,fill=(219,220,191))
    draw.text((24,446),'Procedural item/control preview; native rendering and ordinary journeys need gameplay review.',font=font,fill=(177,193,176))
    preview.save(ROOT/'art/trail_preview.png')
    print('Generated Trail Whistle, recipe, native training goal and companion feedback.')


if __name__=='__main__': main()
