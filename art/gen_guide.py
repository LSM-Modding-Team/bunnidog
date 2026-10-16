"""A small native-book reference from first warren to Apex. Page text is translatable."""
import json
import textwrap
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write

PAGES = {
    2: ('Find a warren', 'Look for guava trees and entrance mounds. Follow lit tunnels to the Nests. Settlements have one to five trees; cave, Nether and End lines live in their own habitats.'),
    3: ('Genes and stats', 'Each of seven genes has two copies. Dominance picks the visible trait; hidden copies still pass to pups. Bred stat points are separate. Use Calipers to read both.'),
    4: ('Breed a seed', 'Feed a guava to each of two ready adults. They drop one seed holding both parents. Its tooltip shows the planned litter, quality and stat drift. The parents then rest.'),
    5: ('Hatch the litter', 'Plant the seed on farmland. Let it ripen, then break the ripe crop to hatch pups. Bone meal speeds growth; Starlit Dust ripens it. Fortune improves quality. Leave room for the litter.'),
    6: ('Guava quality', 'Better guavas improve inherited stat gains. Feed both parents: their qualities are averaged. Golden: 1, Diamond: 2, Netherite: 4, Star: 7, Beacon: 12, Dragon Egg: 20.'),
    7: ('Keep a pedigree', 'Use a Pedigree Book on animals to record them. Ear Tags give permanent codes. Calipers also read a seed or sample held in the other hand. Compare parents before choosing a mate.'),
    8: ('Nests and travel', 'An adult claims one free reachable Nest. At night it returns home. Leads and rides pause home routines. While leading it, use a free Nest with an empty hand or lead to move its home.'),
    9: ('Food and troughs', 'Guavas suit every line; other foods follow Diet. New feed stores, markets and pantries hold local food. Fill a trough near home for healing. Sneak-use with both hands empty to withdraw.'),
    10: ('Life at home', 'Healthy adults visit nearby bells at noon. Put a bell within 12 blocks of home and leave walking space. Injury, threats, leads, rides and night interrupt the visit.'),
    11: ('Gentle sampling', 'Use a Sampling Syringe on an animal at half health or better. It yields one Tissue Sample without harm; recovery takes five minutes. Sneak-use Calipers for care readiness and timers.'),
    12: ('Wool and whiskers', 'Shear a healthy woolly adult for two wool. Brush a healthy adult with Senses +10 for one whisker. Each needs ten minutes of recovery; brushing wears the tool. Pups cannot yield either.'),
    13: ('Useful coats', 'Expressed ember resists fire and lava; moss prevents Poison; sand prevents cactus damage; void prevents Levitation. Hidden copies give no protection. Other dangers still matter.'),
    14: ('Physical traits', 'Hoofed legs halve fall damage. Slate coats add 4 armor. Stocky/chonker builds resist knockback by 15%/30%. Snow/woolly resist freezing. A glow coat is self-lit; hidden copies give no benefit.'),
    15: ('Laboratory', 'Put two Tissue Samples on a Genetics Table. Read the source sheets, pup forecast, copy odds and splice plans. Hidden copies count. Reading never spends samples.'),
    16: ('Gene Splicer', 'Sneak-use to select a gene. Hold a donor sample in the other hand and carry Mutagen. Use on a healthy adult: one donor copy enters that gene. Costs one sample, Mutagen and tool use.'),
    17: ('Stable lines', 'Breed adults with matching copies in all seven genes, including Diet. A Purebred Registry logs up to 24 distinct stable bred lines with recorded parents. Stats still drift each generation.'),
    18: ('Show Ring', 'Lead a stable bred adult to a Judging Stand. Use empty-handed or with a lead. Every stat needs +5 for bronze, +20 for silver, +50 for gold. Sneak-empty use previews; each tier pays only once.'),
    19: ('Material drops', 'Death drops follow genes and bred points. Player kills unlock signatures and legends; pups give less. Pelts craft leather, sinew makes string, whiskers make Brushes and marrow makes Mutagen.'),
    20: ('The five legends', 'At stat +100, adult player kills can drop a legend. Vitality: Titan Haunch; speed: Gilded Foot; Leap: Moon Sinew; Senses: Listening Shell; Bloom: Everbloom. More points improve the chance.'),
    21: ('Apex ritual', 'Craft an Apex Crucible from seven obsidian, a Mutant Marrow and a diamond. Put all five distinct legends on Trophy Pedestals in its ring. Offer a Tissue Sample to choose the boss line.'),
    22: ('The ritual ring', 'Use any five slots: three blocks N/E/S/W or two blocks diagonally, at crucible height. Leave dry space above it. Success spends the sample and five legends. The living donor stays safe.'),
    23: ('Apex of the Line', 'Watch for its charge: dodge pounces and leave the four-block stomp. Half health enrages it. A player victory drops a crest that reads the original line or stands on a pedestal. Care tools refuse.'),
    24: ('Guava juice', 'Craft a guava with a glass bottle. Hold a seed in the main hand and juice in the other, then use the seed to infuse it. The rolled pup genes determine the drink effects. Better seeds improve them.'),
    25: ('Trough gardening', 'Use a hoe to enable gardening. Fully healthy adults with a Nest gather matching ripe food, then rest a minute. Sneak-use Calipers for work status. Leave walking space; keep mob griefing on.'),
    26: ('Furnished warrens', 'New dens have a trough with two guavas. Levels 3-5 have a central bell. New farms fit an empty enabled corner trough for matching crops. Crafted and old troughs need a hoe to enable work.'),
    27: ('Before the Apex', 'Hold Calipers in the main hand and a Tissue Sample in the other. Use an Apex Crucible for health, attacks and defenses. Empty-hand use lists missing offerings. Reading spends nothing.'),
    28: ('Living dust', 'Brush a healthy glow adult for one glowstone dust. A starborn adult with Bloom +20 yields one Starlit Dust. Senses +10 still adds a whisker. One Brush use gathers all matches; recovery is ten minutes.'),
    29: ('Terrain traits', 'Hoofed adults step up one-block ledges and take half fall damage. Fleet legs reduce soul-sand and honey slowing by 25%; lean builds by 50%. Together: 75%. Cobwebs, Slowness and other hazards remain.'),
    30: ('Shore fishing', 'Hurt fisher adults hunt nearby cod, salmon or tropical fish. They need a Nest, daylight and a reachable fish. A kill heals 4 HP, then rests 30 seconds. Pufferfish and land prey are excluded.'),
    31: ('Resident needs', 'Sneak-use Calipers for a claimed Nest, distance, foods and meal recovery. Healthy adults need no healing. Ready means ready to search, not a promise of nearby food. Reading changes nothing.'),
    32: ('Find refuge', 'Non-feral residents retreat from visible threats to a safer dry spot near home. Meals and work pause. Leads and rides take over. Feral lines keep their retaliation. No safe route means no retreat.'),
    33: ('Storm shelter', 'Idle adults wait under cover near their Nest during thunderstorms. Leave a dry floor, room to stand and a safe route. Meals, work, danger and player control take priority. Normal rain keeps idle routines.'),
    34: ('Trough automation', 'Hoppers fill troughs from above or the side; one below drains stock. Keep it powered to hold food. One food stack fits up to 16. Comparators read stock: 0 empty, 15 full. A hoe still controls gardening.'),
}
INDEX = {2:'Warrens & genes',4:'Breeding & seeds',8:'Homes & food',11:'Care & traits',15:'Lab & splicing',
         17:'Registry & shows',19:'Material drops',20:'Apex of the Line',24:'Guava juice',25:'Trough gardening',27:'Boss forecast'}


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
          'guide.bunnidogs.title':'Bunnidogs Guide','guide.bunnidogs.contents':'Contents'})
        for page,label in INDEX.items(): value[f'guide.bunnidogs.index.{page}'] = label
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
    for x,page in [(255,1),(495,33),(735,34)]:
        d.rectangle((x,20,x+224,397),fill=(230,216,174),outline=(117,90,48),width=2)
        d.text((x+14,35), 'Bunnidogs Guide' if page == 1 else PAGES[page][0], font=bold,fill=(40,39,30))
        if page == 1: body = '\n'.join(INDEX.values())
        else: body = '\n'.join(textwrap.wrap(PAGES[page][1],25)) + '\n\nContents'
        d.multiline_text((x+14,75),body,font=font,spacing=6,fill=(40,59,36))
        d.text((x+14,365),f'Page {page} / {max(PAGES)}',font=font,fill=(85,77,56))
    d.text((15,350),'Book icon + text preview',font=font,fill=(224,217,185))
    d.text((15,375),'Substitute font; native UI pending',font=ImageFont.load_default(),fill=(224,217,185))
    preview.save(ROOT/'art/guide_preview.png')
    print('Generated native Field Guide icon, pages, recipe/unlock and 35% pantry pool.')


if __name__ == '__main__': main()
