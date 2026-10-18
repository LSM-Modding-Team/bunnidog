"""A small native-book reference from first warren to Apex. Page text is translatable."""
import json
import textwrap
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write

PAGES = {
    2: ('Find a warren', 'Look for guava trees and entrance mounds. Follow lit tunnels to Nests. Settlements have one to five trees; cave, Nether and End lines have their own habitats. Maps: page 46. Scouts: page 47.'),
    3: ('Genes and stats', 'Each of seven genes has two copies. Dominance picks the visible trait; hidden copies still pass to pups. Bred stat points are separate. Use Calipers to read both.'),
    4: ('Breed a seed', 'Feed a guava to each of two ready adults. They drop one seed holding both parents. Its tooltip shows the planned litter, quality and stat drift. The parents then rest.'),
    5: ('Hatch the litter', 'Plant a seed on farmland; dispensers plant into air above it. Bone meal speeds growth; hand or dispenser Starlit Dust ripens it. Break the ripe crop to hatch pups. Fortune improves quality. Leave room.'),
    6: ('Guava quality', 'Better guavas improve inherited stat gains. Feed both parents: their qualities are averaged. Golden: 1, Diamond: 2, Netherite: 4, Star: 7, Beacon: 12, Dragon Egg: 20.'),
    7: ('Keep a pedigree', 'Log animals with a Pedigree Book; Ear Tags label them. Sneak-use with a filled basket in the other hand to log its animal. Normal use opens records. Filter contents by traits/tags or senses>=20; hover the field for syntax.'),
    8: ('Nests and travel', 'Adults claim reachable Nests and return at night. Lead one to a free Nest and use it to rehome. Empty-hand Nest use inspects it. Use a compass on a Nest or resident to mark home; the place stays marked if removed.'),
    9: ('Food and troughs', 'Guavas suit every line; other foods follow Diet. Fill a trough for healing. Injured adults can eat near their Nest at night; keep food and a safe route within four blocks of home. Sneak-empty withdraws stock.'),
    10: ('Life at home', 'Healthy adults visit nearby bells at noon. Ring one to call a daytime visit. Keep it within 12 blocks of home, with walking space. Injury, threats, leads, rides and night interrupt.'),
    11: ('Gentle sampling', 'Use a Sampling Syringe on an animal at half health or better. It yields one Tissue Sample without harm; recovery takes five minutes. Sneak-use Calipers for care readiness and timers.'),
    12: ('Wool and whiskers', 'Shear a healthy woolly adult for two wool in coat colors. Brush adults at Senses +10 for one whisker. Each needs ten minutes of recovery. Tools wear once; pups yield neither.'),
    13: ('Useful coats', 'Expressed ember resists fire and lava; moss prevents Poison; sand prevents cactus damage; void prevents Levitation. Hidden copies give no protection. Other dangers still matter.'),
    14: ('Physical traits', 'Hoofed legs halve fall damage. Slate adds 4 armor. Stocky/chonker resist knockback by 15%/30%. Snow walks on powder snow; snow/woolly resist freezing. Glow is self-lit. Hidden traits give no benefit.'),
    15: ('Laboratory', 'Put two Tissue Samples on a Genetics Table: sheets, pup forecasts, copy odds and splice plans. Reading spends nothing. Hoppers fill A then B and drain A first; power them to hold samples. Comparators read 0 / 8 / 15.'),
    16: ('Gene Splicer', 'Sneak-use to select a gene. Hold a donor sample in the other hand and carry Mutagen. Use on a healthy adult: one donor copy enters that gene. Costs one sample, Mutagen and tool use.'),
    17: ('Stable lines', 'Register stable bred adults with matching copies in all seven genes and recorded parents. Use the Registry on an animal, or sneak-use with its filled basket in the other hand. Keeps 24 distinct lines.'),
    18: ('Show Ring', 'Lead a stable bred adult to a Judging Stand. Use empty-handed or with a lead. Every stat needs +5 for bronze, +20 for silver, +50 for gold. Sneak-empty use previews; each tier pays only once.'),
    19: ('Material drops', 'Death drops follow genes and bred points. Player kills unlock signatures and legends; pups give less. Pelts craft leather, sinew makes string, whiskers make Brushes and marrow makes Mutagen.'),
    20: ('The five legends', 'At stat +100, adult player kills can drop a legend. Vitality: Titan Haunch; speed: Gilded Foot; Leap: Moon Sinew; Senses: Listening Shell; Bloom: Everbloom. More points improve the chance.'),
    21: ('Apex ritual', 'Craft an Apex Crucible from seven obsidian, a Mutant Marrow and a diamond. Put all five distinct legends on Trophy Pedestals in its ring. Offer a Tissue Sample to choose the boss line.'),
    22: ('The ritual ring', 'Use any five slots: three blocks N/E/S/W or two blocks diagonally, at crucible height. Leave dry space above it. Success spends the sample and five legends. The living donor stays safe.'),
    23: ('Apex of the Line', 'Watch for its charge: dodge pounces and leave the four-block stomp. Half health enrages it. A player victory drops a crest that reads the original line or stands on a pedestal. Care tools refuse.'),
    24: ('Guava juice', 'Craft a guava with a glass bottle. Hold a seed in the main hand and juice in the other, then use the seed to infuse it. The rolled pup genes determine the drink effects. Better seeds improve them.'),
    25: ('Trough gardening', 'Hoe-enable a trough. Healthy residents harvest matching ripe food, then rest a minute. Bloom +10 also advances unripe crops one stage. Diet, filters and mob griefing apply. Sweet berries regrow on the bush.'),
    26: ('Furnished warrens', 'New dens have a trough with two guavas; levels 3-5 may fit a central bell. Grand-tree nurseries may fit an empty Cradle. New farm corners have empty enabled troughs. Use a hoe to enable work on crafted or old troughs.'),
    27: ('Before the Apex', 'Hold Calipers in the main hand and a Tissue Sample in the other. Use an Apex Crucible for health, attacks and defenses. Empty-hand use lists missing offerings. Reading spends nothing.'),
    28: ('Living harvests', 'Brush healthy adults: glow gives glowstone; starborn at Bloom +20 gives Starlit Dust; moss at Bloom +10 gives moss carpet. Senses +10 adds a whisker. Gather one of each match for one tool use and ten-minute recovery.'),
    29: ('Terrain traits', 'Hoofed adults step up one-block ledges and take half fall damage. Fleet legs reduce soul-sand and honey slowing by 25%; lean builds by 50%. Together: 75%. Cobwebs, Slowness and other hazards remain.'),
    30: ('Shore fishing', 'Hurt fisher adults hunt nearby cod, salmon or tropical fish. They need a Nest, daylight and a reachable fish. A kill heals 4 HP, then rests 30 seconds. Pufferfish and land prey are excluded.'),
    31: ('Resident needs', 'Sneak-use Calipers for a claimed Nest, distance, foods and meal recovery. Healthy adults need no healing. Ready means ready to search, not a promise of nearby food. Reading changes nothing.'),
    32: ('Find refuge', 'Non-feral residents retreat from visible threats to a safer dry spot near home. Meals and work pause. Leads and rides take over. Feral lines keep their retaliation. No safe route means no retreat.'),
    33: ('Storm shelter', 'Idle adults wait under cover near their Nest during thunderstorms. Leave a dry floor, room to stand and a safe route. Meals, work, danger and player control take priority. Normal rain keeps idle routines.'),
    34: ('Trough automation', 'Hoppers fill and drain troughs. Power the hopper to hold food. Stock: one matching stack, up to 16; comparators read 0 empty to 15 full. A hoe toggles gardening. Redstone pauses residents: page 49.'),
    35: ('Bell calls', 'Ring a bell within 12 blocks of a Nest to call healthy adults by day. Redstone and projectiles work too. Safe paths, leads and care take priority. Visits: 20 seconds, one-minute rest. Sneak-Calipers: roll call (page 52).'),
    36: ('Fleece colors', 'Woolly adults yield two wool in their coat colors. Spotted gives one yellow and one brown. Glow gives cyan; moss green; ember red; void purple. Hidden coats add nothing. Calipers names the ready yield.'),
    37: ('Trough filters', 'Sneak-use food to reserve a trough for that exact food and its components, without spending it. Hoppers and gardeners obey the filter. Sneak-empty withdraws stock; repeat while empty to clear the filter.'),
    38: ('Care dispensers', 'Dispensers use Brushes or Sampling Syringes on one eligible bunnidog ahead per pulse. One tool use; hand and machine share recovery. Collect drops with hoppers. Armadillo brushing still works.'),
    39: ('Travel Basket', 'Craft six planks, leather and a lead into a basket. Pack one calm, unled animal; release on clear dry ground. Identity and care rest persist; its old Nest becomes free. Calipers reads it; sneak-use books to log it.'),
    41: ('Nursery Cradle', 'Craft wool, glass, a Nest and planks into a Nursery Cradle. Insert one seed; a loaded minute adds one stage. Bone meal helps; Starlit Dust ripens it. Sneak-empty withdraws and resets growth. Calipers reads the litter.'),
    42: ('Hatch safely', 'Use a ripe Cradle empty-handed or pulse redstone. Clear a dry 3x3 pad in front with two blocks of air. Sneak-Calipers checks the pad and marks trouble. Hoppers move seeds; comparators read 15 when ripe. Steady power does not repeat.'),
    43: ('Surplus compost', 'Bloom +10 adults compost food above an eight-item reserve at hoe-enabled troughs. Place a composter nearby with a safe route. Crop work takes priority. Each job spends one food and rests a minute. Hoppers collect bone meal.'),
    44: ('Treat a litter', 'Use or dispense one Mutagen at a planted seed or occupied Cradle. Growth, incubation, parents, quality and item details stay intact. Treated or invalid litters keep the vial. You can treat a ripe litter before hatching.'),
    45: ('Nest guards', 'Sneak-use a bone on a feral adult with a Nest to toggle guarding. No bone is spent. Free, half-healthy guards bite selected hostiles within eight blocks of home. Leads pause them. Calipers reports readiness. They can still be hurt.'),
    46: ('Warren maps', 'Journeyman cartographers may sell a Warren Explorer Map for 14 emeralds and a compass. Follow its village marker to a natural Overworld warren. The trade is not guaranteed. Failed searches omit it. Cave dens and End warrens are excluded.'),
    47: ('Mineral scouts', 'Sneak-use a compass on a half-healthy bat-eared adult with Senses +10. Marks ore through rock within six blocks. A survey rests 30 seconds, even when empty. Places stay after mining. Focused surveys: page 48.'),
    48: ('Choose a mineral', 'Senses +20 scouts can focus: hold a mineral in the other hand. Raw metals/ingots select their ore; coal, redstone, lapis, gems, quartz or netherite scrap select theirs. No mineral is spent. Every focus shares the same survey rest.'),
    49: ('Pause trough work', 'Power a Feeding Trough to pause resident meals, crops and composting. Stock, filters, hoppers and comparator output still work. Remove power to resume at the next search. The hoe keeps its setting. Empty-hand inspection names the pause.'),
    51: ('Find a tagged animal', 'Rename a spare Ear Tag to a code like BD-3F7A2C. Hold it opposite Calipers; sneak-use the air. Loaded matches within 24 blocks glow ten seconds. Search rest: five seconds. Reusable tag; packed animals are excluded.'),
    52: ('Bell roll call', 'Sneak-use Calipers on a bell for loaded adults with nearby Nests. Lists tags, health, foods and homes; injured first. Shows up to eight animals within 32 blocks, with Nests within 12. Reading rings no bell and changes nothing.'),
    53: ('Recovery rest', 'Adults below half health rest near their Nest instead of strolling by day. Food, danger, storms, breeding and leads take priority. Keep a dry safe route. Rest heals nothing; stock suitable food. Sneak-Calipers reports readiness.'),
}
PAGE_COUNT = 53
MAIN_INDEX = {2:'Warrens & scouts',4:'Breeding & seeds',8:'Homes & food',11:'Care & traits',15:'Lab & splicing',
              17:'Registry & shows',19:'Material drops',20:'Apex of the Line'}
MORE_INDEX = {24:'Guava juice',25:'Gardens & stores',27:'Boss forecast',28:'Living harvests',29:'Terrain & fishing',
              31:'Resident needs',32:'Refuge & storms',34:'Troughs & bell calls',36:'Fleece & dispensers',39:'Travel Basket'}
NEW_INDEX = {41:'Nursery & compost',44:'Treat a litter',45:'Nest guards',46:'Warren maps',47:'Mineral scouts',49:'Pause trough work',51:'Find a tagged animal',52:'Bell roll call',53:'Recovery rest'}


def pantry_guide_pool():
    return {'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.35}],
            'entries':[{'type':'minecraft:item','name':'bunnidogs:field_guide'}]}


def main():
    img = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(img)
    d.polygon([(2,2),(12,1),(14,3),(14,13),(4,15),(2,13)], fill=(45,53,36))
    d.polygon([(4,3),(13,2),(13,12),(4,14)], fill=(178,155,106))
    d.polygon([(4,2),(12,1),(12,11),(4,13)], fill=(79,123,70))
    d.line((2,3,2,12,4,13,4,3), fill=(58,78,43), width=1)
    d.line((5,3,11,2), fill=(167,185,105))
    d.ellipse((6,6,10,10), fill=(223,204,143))
    d.line((7,6,6,4), fill=(223,204,143)); d.line((9,6,10,4), fill=(223,204,143))
    img.save(RES/'assets/bunnidogs/textures/item/field_guide.png')
    write('assets/bunnidogs/models/item/field_guide.json', {'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/field_guide'}})
    write('assets/bunnidogs/items/field_guide.json', {'model':{'type':'minecraft:model','model':'bunnidogs:item/field_guide'}})
    write('data/bunnidogs/recipe/field_guide.json', {'type':'minecraft:crafting_shapeless',
          'ingredients':['minecraft:book','#bunnidogs:bunnidog_food'],'result':{'id':'bunnidogs:field_guide'}})
    write('data/bunnidogs/advancement/recipes/tools/field_guide.json', {'parent':'minecraft:recipes/root',
          'criteria':{'has_guava':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#bunnidogs:bunnidog_food'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:field_guide'}}},
          'requirements':[['has_guava','has_recipe']],'rewards':{'recipes':['bunnidogs:field_guide']}})
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({'item.bunnidogs.field_guide':'Bunnidogs Field Guide',
          'guide.bunnidogs.title':'Bunnidogs Guide','guide.bunnidogs.contents':'Contents',
          'guide.bunnidogs.more_contents':'More chapters','guide.bunnidogs.main_contents':'Main contents',
          'guide.bunnidogs.new_contents':'New chapters'})
        for page,label in (MAIN_INDEX | MORE_INDEX | NEW_INDEX).items(): value[f'guide.bunnidogs.index.{page}'] = label
        for page,(title,body) in PAGES.items():
            value[f'guide.bunnidogs.page.{page}.title'] = title
            value[f'guide.bunnidogs.page.{page}.body'] = body
        path.write_text(json.dumps(value,indent=2)+'\n')
    for path in sorted((RES/'data/bunnidogs/loot_table/chests').glob('warren_pantry*.json')):
        loot = json.loads(path.read_text())
        if not any(entry.get('name') == 'bunnidogs:field_guide' for pool in loot['pools'] for entry in pool['entries']):
            loot['pools'].append(pantry_guide_pool())
        write(str(path.relative_to(RES)),loot)
    preview = Image.new('RGBA',(980,420),(43,46,39,255)); d = ImageDraw.Draw(preview)
    preview.alpha_composite(img.resize((224,224),Image.Resampling.NEAREST),(14,85))
    try:
        font = ImageFont.truetype('DejaVuSans.ttf',13)
        bold = ImageFont.truetype('DejaVuSans-Bold.ttf',13)
    except OSError:
        font = bold = ImageFont.load_default()
    for x,page in [(255,15),(495,53),(735,50)]:
        d.rectangle((x,20,x+224,397),fill=(230,216,174),outline=(117,90,48),width=2)
        title = 'Bunnidogs Guide' if page == 1 else 'More chapters' if page == 40 else 'New chapters' if page == 50 else PAGES[page][0]
        d.text((x+14,35),title,font=bold,fill=(40,39,30))
        if page == 1: body = '\n'.join(MAIN_INDEX.values()) + '\n\nMore chapters\nNew chapters'
        elif page == 40: body = '\n'.join(MORE_INDEX.values()) + '\n\nMain contents'
        elif page == 50: body = '\n'.join(NEW_INDEX.values()) + '\n\nMain contents'
        else: body = '\n'.join(textwrap.wrap(PAGES[page][1],25)) + '\n\nContents'
        d.multiline_text((x+14,75),body,font=font,spacing=6,fill=(40,59,36))
        d.text((x+14,365),f'Page {page} / {PAGE_COUNT}',font=font,fill=(85,77,56))
    d.text((15,350),'Book icon + text preview',font=font,fill=(224,217,185))
    d.text((15,375),'Substitute font; native UI pending',font=ImageFont.load_default(),fill=(224,217,185))
    preview.save(ROOT/'art/guide_preview.png')
    print('Generated native Field Guide icon, pages, recipe/unlock and 35% pantry pool.')


if __name__ == '__main__': main()
