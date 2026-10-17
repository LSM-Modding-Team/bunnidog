"""Travel Basket: a woven guava-wood carrier with a padded lid; occupied state uses native foil/name."""
import json
from PIL import Image, ImageDraw
from gen_syringe import ROOT, RES, write


def main():
    icon = Image.new('RGBA',(16,16))
    d = ImageDraw.Draw(icon)
    dark, wood, light = (78,50,31), (161,106,58), (216,165,100)
    d.arc((3,0,12,10),180,360,fill=dark,width=3)
    d.arc((4,1,11,9),180,360,fill=light,width=1)
    d.polygon([(1,5),(14,5),(13,14),(3,15)],fill=dark)
    d.polygon([(2,6),(13,6),(12,13),(4,14)],fill=wood)
    for y in (7,10,13): d.line((3,y,12,y-1),fill=light,width=1)
    for x in (4,7,10): d.line((x,6,x+1,13),fill=dark,width=1)
    d.polygon([(2,4),(12,4),(14,6),(3,7),(1,6)],fill=(190,159,114))
    d.line((3,5,12,5),fill=(235,214,168),width=1)
    d.rectangle((7,6,9,9),fill=dark)
    d.rectangle((8,7,8,8),fill=(215,201,155))
    icon.save(RES/'assets/bunnidogs/textures/item/travel_basket.png')
    write('assets/bunnidogs/models/item/travel_basket.json',{'parent':'minecraft:item/generated',
          'textures':{'layer0':'bunnidogs:item/travel_basket'}})
    write('assets/bunnidogs/items/travel_basket.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/travel_basket'}})
    write('data/bunnidogs/recipe/travel_basket.json',{'type':'minecraft:crafting_shaped','pattern':['P P','PLP','PSP'],
          'key':{'P':'#minecraft:planks','L':'minecraft:leather','S':'minecraft:lead'},'result':{'id':'bunnidogs:travel_basket'}})
    write('data/bunnidogs/advancement/recipes/tools/travel_basket.json',{'parent':'minecraft:recipes/root',
          'criteria':{'has_lead':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:lead'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:travel_basket'}}},
          'requirements':[['has_lead','has_recipe']],'rewards':{'recipes':['bunnidogs:travel_basket']}})
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text())
        value.update({'item.bunnidogs.travel_basket':'Travel Basket',
          'item.bunnidogs.travel_basket.filled':'Occupied Travel Basket',
          'tooltip.bunnidogs.basket.occupant':'Carries: %s',
          'tooltip.bunnidogs.basket.capture':'Use on a calm, unled bunnidog to carry it.',
          'tooltip.bunnidogs.basket.release':'Use dry solid ground with room for the animal to release it.',
          'tooltip.bunnidogs.basket.calipers':'Hold Calipers in the other hand to read its line.',
          'message.bunnidogs.basket.permission':'Use a single Travel Basket where you may build.',
          'message.bunnidogs.basket.full':'This basket already carries an animal.',
          'message.bunnidogs.basket.ordinary':'Only living ordinary bunnidogs fit; the Apex cannot be carried.',
          'message.bunnidogs.basket.transported':'Unleash it and remove all rides or riders first.',
          'message.bunnidogs.basket.danger':'Let it calm down before packing it.',
          'message.bunnidogs.basket.captured':'Packed safely. Its old Nest is free; care recovery pauses while carried.',
          'message.bunnidogs.basket.blocked':'Cannot release here. Leave dry solid ground and room for the animal.',
          'message.bunnidogs.basket.invalid':'Cannot read this basket\'s contents. Kept unchanged.',
          'message.bunnidogs.basket.duplicate':'That animal is already in the world. Contents kept.',
          'message.bunnidogs.basket.released':'Released safely; adults can claim a new Nest.'})
        path.write_text(json.dumps(value,indent=2)+'\n')
    preview = Image.new('RGBA',(300,300),(43,46,39,255))
    preview.alpha_composite(icon.resize((256,256),Image.Resampling.NEAREST),(22,22))
    preview.save(ROOT/'art/basket_preview.png')
    print('Generated Travel Basket icon, model, recipe, unlock and translated interaction text.')


if __name__ == '__main__': main()
