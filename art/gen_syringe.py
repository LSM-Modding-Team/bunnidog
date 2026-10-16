"""Sampling syringe: 16px iron needle, glass barrel and copper plunger, matching the genetics tools."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def write(path, value):
    path = RES / path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')


def main():
    img = Image.new('RGBA',(16,16))
    draw = ImageDraw.Draw(img)
    dark, iron, copper = (55,66,76), (211,220,225), (209,128,81)
    draw.line((9,6,14,1),fill=dark,width=3)
    draw.line((10,5,14,1),fill=iron,width=1)
    draw.polygon([(3,8),(8,3),(12,7),(7,12)],fill=dark)
    draw.polygon([(4,8),(8,4),(11,7),(7,11)],fill=(161,216,225))
    draw.line((5,8,8,5),fill=(230,248,245),width=1)
    draw.line((7,9,9,7),fill=(195,102,138),width=2)
    draw.line((2,13,5,10),fill=dark,width=3)
    draw.line((2,13,5,10),fill=copper,width=1)
    draw.line((0,12,3,15),fill=dark,width=3)
    draw.line((0,12,3,15),fill=copper,width=1)
    draw.line((2,7,8,13),fill=dark,width=2)
    draw.line((2,7,8,13),fill=iron,width=1)
    img.save(RES / 'assets/bunnidogs/textures/item/syringe.png')
    preview = Image.new('RGBA',(256,256),(45,43,42,255))
    preview.alpha_composite(img.resize((256,256),Image.Resampling.NEAREST))
    preview.save(ROOT / 'art/syringe_preview.png')
    write('assets/bunnidogs/items/syringe.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/syringe'}})
    write('assets/bunnidogs/models/item/syringe.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'bunnidogs:item/syringe'}})
    write('data/bunnidogs/recipe/syringe.json',{'type':'minecraft:crafting_shaped','pattern':['  I',' G ','S  '],
          'key':{'I':'minecraft:iron_ingot','G':'minecraft:glass_bottle','S':'minecraft:stick'},'result':{'id':'bunnidogs:syringe'}})
    write('data/bunnidogs/advancement/recipes/tools/syringe.json',{'parent':'minecraft:recipes/root',
          'criteria':{'has_glass':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:glass_bottle'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:syringe'}}},
          'requirements':[['has_glass','has_recipe']],'rewards':{'recipes':['bunnidogs:syringe']}})
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text())
        value.update({'item.bunnidogs.syringe':'Sampling Syringe',
          'tooltip.bunnidogs.syringe.use':'Use on a bunnidog to collect a Tissue Sample.',
          'tooltip.bunnidogs.syringe.health':'Requires at least half health. Works on pups too.',
          'tooltip.bunnidogs.syringe.recovery':'Five minutes of recovery between samples.',
          'message.bunnidogs.sampling.hurt':'Help this bunnidog recover to at least half health first.',
          'message.bunnidogs.sampling.wait':'This bunnidog needs %s more seconds before sampling.',
          'message.bunnidogs.sampling.taken':'Tissue Sample collected.'})
        path.write_text(json.dumps(value,indent=2)+'\n')
    print('Generated sampling syringe icon, preview, crafting, recipe unlock and text.')


if __name__ == '__main__':
    main()
