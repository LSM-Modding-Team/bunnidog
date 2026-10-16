"""Obsidian/gold Apex Crucible, lineage crest and the endgame's native data assets."""
import json
import random
from PIL import Image, ImageDraw
from gen_syringe import ROOT, RES, write


def main():
    rng = random.Random(73)
    side = Image.new('RGBA', (16,16)); d = ImageDraw.Draw(side)
    for y in range(16):
        for x in range(16):
            v = rng.randrange(18,36)
            d.point((x,y), fill=(v+5,v,v+15))
    d.line((0,2,15,2), fill=(167,117,42)); d.line((0,13,15,13), fill=(100,68,35))
    for x in (3,11):
        d.rectangle((x,5,x+1,10), fill=(110,69,141)); d.point((x,5), fill=(215,151,224))
    top = side.copy(); d = ImageDraw.Draw(top)
    d.rectangle((1,1,14,14), outline=(198,146,65), width=1)
    d.ellipse((4,4,11,11), fill=(59,31,81), outline=(185,102,209))
    d.line((6,7,7,5,8,7,9,5,10,8,8,10,6,8), fill=(237,195,244), width=1)
    for name,img in [('apex_crucible_side',side),('apex_crucible_top',top)]:
        img.save(RES/f'assets/bunnidogs/textures/block/{name}.png')
    write('assets/bunnidogs/models/block/apex_crucible.json', {'parent':'minecraft:block/cube_bottom_top',
          'textures':{'side':'bunnidogs:block/apex_crucible_side','top':'bunnidogs:block/apex_crucible_top','bottom':'minecraft:block/obsidian'}})
    write('assets/bunnidogs/blockstates/apex_crucible.json', {'variants':{'':{'model':'bunnidogs:block/apex_crucible'}}})
    write('assets/bunnidogs/models/item/apex_crucible.json', {'parent':'bunnidogs:block/apex_crucible'})
    crest = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(crest)
    d.polygon([(2,3),(5,5),(5,1),(8,4),(11,1),(11,5),(14,3),(13,10),(8,15),(3,10)], fill=(84,48,43))
    d.polygon([(3,5),(6,7),(6,3),(8,6),(10,3),(10,7),(13,5),(12,10),(8,13),(4,10)], fill=(221,167,67))
    d.polygon([(8,6),(11,9),(8,12),(5,9)], fill=(112,53,140))
    d.line((8,7,6,9,8,11), fill=(236,173,243))
    d.point((9,9), fill=(158,90,187)); d.line((4,6,4,9), fill=(255,220,124))
    crest.save(RES/'assets/bunnidogs/textures/item/apex_crest.png')
    write('assets/bunnidogs/models/item/apex_crest.json', {'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/apex_crest'}})
    for name in ('apex_crucible','apex_crest'):
        write(f'assets/bunnidogs/items/{name}.json', {'model':{'type':'minecraft:model','model':f'bunnidogs:item/{name}'}})
    write('data/bunnidogs/recipe/apex_crucible.json', {'type':'minecraft:crafting_shaped','pattern':['OMO','ODO','OOO'],
          'key':{'O':'minecraft:obsidian','M':'bunnidogs:mutant_marrow','D':'minecraft:diamond'},'result':{'id':'bunnidogs:apex_crucible'}})
    write('data/bunnidogs/loot_table/blocks/apex_crucible.json', {'type':'minecraft:block','pools':[{'rolls':1,
          'entries':[{'type':'minecraft:item','name':'bunnidogs:apex_crucible'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    write('data/bunnidogs/advancement/recipes/tools/apex_crucible.json', {'parent':'minecraft:recipes/root',
          'criteria':{'has_marrow':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:mutant_marrow'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:apex_crucible'}}},
          'requirements':[['has_marrow','has_recipe']],'rewards':{'recipes':['bunnidogs:apex_crucible']}})
    write('data/bunnidogs/advancement/apex_of_the_line.json', {'parent':'bunnidogs:champion_line',
          'criteria':{'defeated':{'trigger':'minecraft:impossible'}},'requirements':[['defeated']], 'rewards':{'experience':200},
          'display':{'icon':{'id':'bunnidogs:apex_crest'},'title':{'translate':'advancement.bunnidogs.apex.title'},
                     'description':{'translate':'advancement.bunnidogs.apex.description'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False}})
    for tag in ('mineable/pickaxe','needs_diamond_tool'):
        path = RES/f'data/minecraft/tags/block/{tag}.json'
        value = json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
        if 'bunnidogs:apex_crucible' not in value['values']: value['values'].append('bunnidogs:apex_crucible')
        write(f'data/minecraft/tags/block/{tag}.json',value)
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({
          'block.bunnidogs.apex_crucible':'Apex Crucible', 'item.bunnidogs.apex_crest':'Apex Crest',
          'entity.bunnidogs.apex':'Apex of the Line', 'entity.bunnidogs.apex.charging':'Apex of the Line — gathering force',
          'message.bunnidogs.apex.ritual':'Offer a Tissue Sample to summon its line as an Apex. The donor stays safe; the sample and five legends are spent.',
          'message.bunnidogs.apex.ring':'Use five Trophy Pedestals at the crucible height: 3 blocks N/E/S/W or 2 blocks diagonally. Any five of the eight slots work.',
          'message.bunnidogs.apex.legends':'Offer one each: Titan Haunch, Gilded Foot, Moon Sinew, Listening Shell and Everbloom.',
          'message.bunnidogs.apex.permission':'You need building permission to start this ritual.',
          'message.bunnidogs.apex.sample':'Use a Tissue Sample containing valid genes.',
          'message.bunnidogs.apex.peaceful':'An Apex cannot be summoned on Peaceful difficulty.',
          'message.bunnidogs.apex.loaded':'Load the ritual ring and space above it first.',
          'message.bunnidogs.apex.active':'A living Apex is already within 48 blocks.',
          'message.bunnidogs.apex.missing':'The ring needs all five distinct legendary drops. Empty-hand use explains the ritual.',
          'message.bunnidogs.apex.blocked':'Clear dry space above the crucible for the Apex. Nothing was spent.',
          'message.bunnidogs.apex.summoned':'Your line has an Apex. Defeat it to claim its crest.',
          'message.bunnidogs.apex.no_care':'An Apex cannot be sampled, spliced or cared for.',
          'message.bunnidogs.apex.measure':'Apex health: %s / %s. Its crest preserves the original source line.',
          'tooltip.bunnidogs.apex_crest.source':'Source line of a defeated Apex',
          'tooltip.bunnidogs.apex_crest.use':'Use to read its source sheet, or display on a Trophy Pedestal.',
          'tooltip.bunnidogs.apex_offering.use':'One of five distinct offerings for an Apex Crucible.',
          'tooltip.bunnidogs.apex_offering.pedestal':'Display in its Trophy Pedestal ring; a Tissue Sample chooses the line.',
          'advancement.bunnidogs.apex.title':'Apex of the Line',
          'advancement.bunnidogs.apex.description':'Summon a line’s Apex and defeat it.'})
        path.write_text(json.dumps(value,indent=2)+'\n')
    preview = Image.new('RGBA',(640,270),(42,38,43,255)); d = ImageDraw.Draw(preview)
    for x,img,label in [(20,side,'Crucible side'),(230,top,'Crucible top'),(440,crest,'Lineage crest')]:
        preview.alpha_composite(img.resize((180,180),Image.Resampling.NEAREST),(x,20))
        d.text((x,220),label,fill=(237,218,178))
    preview.save(ROOT/'art/apex_preview.png')
    print('Generated Apex crucible, crest, recipes, drops, advancement and preview.')


if __name__ == '__main__': main()
