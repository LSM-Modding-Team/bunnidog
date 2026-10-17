"""Manual Feeding Trough: native wood cuboids and four visible stock levels."""
import json
import random
from pathlib import Path
from PIL import Image, ImageDraw
from gen_show import box
from gen_syringe import write

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def main():
    rng = random.Random(41)
    feed = Image.new('RGBA',(16,16)); pixels = feed.load()
    colors = [(121,83,45),(164,117,62),(195,146,78),(89,111,55),(154,79,65)]
    for y in range(16):
        for x in range(16): pixels[x,y] = (*rng.choice(colors),255)
    feed.save(RES/'assets/bunnidogs/textures/block/trough_feed.png')
    base = [box([1,0,4],[15,2,12],'wood'),box([1,2,4],[15,7,6],'wood'),box([1,2,10],[15,7,12],'wood'),
            box([1,2,6],[3,7,10],'wood'),box([13,2,6],[15,7,10],'wood')]
    variants = {}
    for tending in (False,True):
        for fill in range(4):
            name = 'feeding_trough' + (f'_{fill}' if fill else '') + ('_tending' if tending else '')
            elements = base + ([box([3,2,6],[13,2+fill,10],'feed')] if fill else [])
            if tending: elements += [box([.75,3,7],[1.1,6,9],'leaf'),box([14.9,3,7],[15.25,6,9],'leaf')]
            write(f'assets/bunnidogs/models/block/{name}.json',{'parent':'minecraft:block/block',
                  'textures':{'particle':'bunnidogs:block/guava_planks','wood':'bunnidogs:block/guava_planks',
                              'feed':'bunnidogs:block/trough_feed','leaf':'minecraft:block/moss_block'},'elements':elements})
            variants[f'fill={fill},tending={str(tending).lower()}'] = {'model':f'bunnidogs:block/{name}'}
    write('assets/bunnidogs/blockstates/feeding_trough.json',{'variants':variants})
    write('assets/bunnidogs/models/item/feeding_trough.json',{'parent':'bunnidogs:block/feeding_trough'})
    write('assets/bunnidogs/items/feeding_trough.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/feeding_trough'}})
    write('data/bunnidogs/recipe/feeding_trough.json',{'type':'minecraft:crafting_shaped','pattern':['P P','PIP','PPP'],
          'key':{'P':'#minecraft:planks','I':'minecraft:iron_ingot'},'result':{'id':'bunnidogs:feeding_trough'}})
    write('data/bunnidogs/loot_table/blocks/feeding_trough.json',{'type':'minecraft:block','pools':[{'rolls':1,
          'entries':[{'type':'minecraft:item','name':'bunnidogs:feeding_trough'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    write('data/bunnidogs/advancement/recipes/tools/feeding_trough.json',{'parent':'minecraft:recipes/root',
          'criteria':{'has_nest':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:nest'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:feeding_trough'}}},
          'requirements':[['has_nest','has_recipe']],'rewards':{'recipes':['bunnidogs:feeding_trough']}})
    path = RES/'data/minecraft/tags/block/mineable/axe.json'
    value = json.loads(path.read_text())
    if 'bunnidogs:feeding_trough' not in value['values']: value['values'].append('bunnidogs:feeding_trough')
    path.write_text(json.dumps(value,indent=2)+'\n')
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({
          'block.bunnidogs.feeding_trough':'Feeding Trough',
          'message.bunnidogs.trough.added':'Added %s food; %s stored. One food type, up to 16.',
          'message.bunnidogs.trough.empty':'Empty trough. Add food; sneak-food sets a filter. Hoe: toggle gardening.',
          'message.bunnidogs.trough.stock':'Stored: %s x %s. Sneak-empty: withdraw. Sneak-food: filter. Hoe: gardening.',
          'message.bunnidogs.trough.filter_set':'Reserved for %s with matching components. Food was not consumed.',
          'message.bunnidogs.trough.filter_conflict':'Withdraw the different stored food before changing the filter.',
          'message.bunnidogs.trough.filtered':'Reserved for %s with matching components. Sneak-empty while empty clears the filter.',
          'message.bunnidogs.trough.filter_cleared':'Food filter cleared. This trough accepts any suitable food again.',
          'message.bunnidogs.trough.stock_filter':'%s Reserved for %s (matching components).',
          'message.bunnidogs.trough.tending.on':'Gardening enabled. Healthy residents harvest; Bloom +10 also cultivates.',
          'message.bunnidogs.trough.tending.off':'Gardening disabled. Residents still eat stored food.',
          'message.bunnidogs.care.gardening':'Gardening: %s',
          'message.bunnidogs.care.cultivation':'Cultivation: %s',
          'message.bunnidogs.care.cultivation_bonus':'Bloom +%s bonus; can advance matching unripe crops between harvests',
          'message.bunnidogs.care.cultivation_requirement':'needs Bloom +%s; ripe harvesting remains available',
          'message.bunnidogs.gardening.ready':'ready to search; needs an enabled trough, room for matching food and a safe path',
          'message.bunnidogs.gardening.unavailable':'not a resident worker',
          'message.bunnidogs.gardening.adult':'needs an adult',
          'message.bunnidogs.gardening.full_health':'needs full health',
          'message.bunnidogs.gardening.home':'needs a claimed Nest',
          'message.bunnidogs.gardening.daytime':'rests at night',
          'message.bunnidogs.gardening.transported':'paused while led or riding',
          'message.bunnidogs.gardening.danger':'paused by danger, seeking refuge or a recent attack',
          'message.bunnidogs.gardening.outside_home':'needs to be within 16 blocks of its Nest',
          'message.bunnidogs.gardening.griefing_off':'needs mob griefing enabled'})
        path.write_text(json.dumps(value,indent=2)+'\n')
    preview = Image.new('RGBA',(1100,300),(46,43,42,255)); d = ImageDraw.Draw(preview)
    preview.alpha_composite(feed.resize((160,160),Image.Resampling.NEAREST),(620,20))
    for fill in range(4):
        ox = 85+fill*145
        def p(x,y,z): return (ox+(x-z)*4,190+(x+z)*2-y*5)
        elements = base + ([box([3,2,6],[13,2+fill,10],'feed')] if fill else [])
        if fill >= 2: elements += [box([.75,3,7],[1.1,6,9],'leaf'),box([14.9,3,7],[15.25,6,9],'leaf')]
        for element in elements:
            x,y,z=element['from']; X,Y,Z=element['to']
            is_feed = element['faces']['up']['texture']=='#feed'
            is_leaf = element['faces']['up']['texture']=='#leaf'
            for vertices,color in [([(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)],(119,75,44)),
                    ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],(159,102,60)),
                    ([(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],(167,127,66) if is_feed else (192,132,81))]:
                d.polygon([p(*v) for v in vertices],fill=(100,144,56) if is_leaf else color,outline=(46,35,25))
        d.text((ox-20,245),'Gardening on' if fill >= 2 else 'Manual stock',fill=(232,218,188))
    d.text((825,25),'New farm: same 3 x 3 footprint',fill=(232,218,188))
    for x in range(-2,2):
        for z in range(-2,2):
            color = (74,99,51)
            label = ''
            if x >= -1 and z >= -1:
                color,label = ((139,93,52),'T') if (x,z) == (-1,-1) else ((55,110,161),'~') if (x,z) == (0,0) else ((161,152,65),'C')
            elif (x,z) in ((-2,-1),(-1,-2)):
                color,label = (104,134,72),'Walk'
            px,pz = 825+(x+2)*53,70+(z+2)*44
            d.rectangle((px,pz,px+49,pz+40),fill=color)
            d.text((px+7,pz+12),label,fill=(240,232,211))
    d.text((825,258),'T: empty enabled trough; C: crops',fill=(232,218,188))
    preview.save(ROOT/'art/settlement_preview.png')
    print('Generated trough models, food texture, crafting, unlock, drops and source preview.')


if __name__ == '__main__': main()
