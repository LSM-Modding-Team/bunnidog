"""A small native-book reference from first warren to Apex. Page text is translatable."""
import json
import textwrap
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write

PAGES = {
    2: ('Start playing', 'Empty hands open animal and station controls. Feed two adults guavas. Plant their seed on farmland; break the ripe crop to hatch pups. This book is optional reference. Warrens: 46.'),
    3: ('Genes and stats', 'Each of seven genes has two copies. Dominance picks the visible trait; hidden copies still pass to pups. Bred stat points are separate. Use Calipers to read both.'),
    4: ('Breed a seed', 'Feed each of two ready adults a guava, or choose Courtship in their panels. They drop one litter seed and then rest. Its tooltip shows litter size and quality; Calipers forecasts inheritance.'),
    5: ('Hatch the litter', 'Plant a seed on farmland; dispensers plant into air above it. Bone meal speeds growth; hand or dispenser Starlit Dust ripens it. Break the ripe crop to hatch pups. Fortune improves quality. Leave room.'),
    6: ('Guava quality', 'Better guavas improve inherited stat gains. Feed both parents: their qualities are averaged. Golden: 1, Diamond: 2, Netherite: 4, Star: 7, Beacon: 12, Dragon Egg: 20.'),
    7: ('Keep a pedigree', 'Log animals with a Pedigree Book; Ear Tags label them. Sneak-use with a filled basket to log its animal. Normal use opens records. Filter by traits, tags or senses>=20. Name recorded lines: page 95.'),
    8: ('Nests and travel', 'Adults claim reachable Nests and return at night. Lead one to a free Nest, open it and choose Assign led adult. Each adult needs one Nest. A compass on a Nest or resident marks the way home.'),
    9: ('Food and troughs', 'Guavas suit every line; other foods follow Diet. Troughs heal injured residents, including safe night meals within four blocks of home. Sneak-empty withdraws stock. Direct guava care: 87.'),
    10: ('Life at home', 'Healthy adults visit nearby bells at noon. Ring one to call a daytime visit. Keep it within 12 blocks of home, with walking space. Injury, threats, leads, rides and night interrupt.'),
    11: ('Gentle sampling', 'Use a Syringe on an animal at half health or better; pups work too. Samples are reusable and recovery takes five minutes. In Useful roles & work, Calipers compare a living pup with a commission before raising it.'),
    12: ('Wool and whiskers', 'Shear a healthy woolly adult for two wool in coat colors. Brush adults at Senses +10 for one whisker. Each needs ten minutes of recovery. Tools wear once; pups yield neither.'),
    13: ('Useful coats', 'Expressed ember resists fire and lava; moss prevents Poison; sand prevents cactus damage; void prevents Levitation. Hidden copies give no protection. Other dangers still matter.'),
    14: ('Physical traits', 'Hoofed legs halve fall damage. Slate adds 4 armor. Stocky/chonker resist knockback by 15%/30%. Snow walks on powder snow; snow/woolly resist freezing. Glow is self-lit. Hidden traits give no benefit.'),
    15: ('Laboratory', 'Tables accept feathers or Whiskers in their copper-and-planks recipe. Choose a goal; two reusable samples forecast it. Compare next real seed checks saved quality. Calipers reveal matching nearby offspring and their actual targets.'),
    16: ('Gene Splicer', 'Sneak-use to select a gene. Hold a donor sample in the other hand and carry Mutagen. Use on a healthy adult: one donor copy enters that gene. Costs one sample, Mutagen and tool use.'),
    17: ('Stable lines', 'Register stable bred adults with matching copies in all seven genes and recorded parents. Use the Registry on an animal, or sneak-use with its filled basket in the other hand. Keeps 24 distinct lines.'),
    18: ('Show Ring', 'Lead an adult to a Judging Stand and open it empty-handed for a scorecard. Fix mixed gene pairs and breed toward all five stat targets. Claim eligible ribbon awards once per tier. A held lead remains a judging shortcut.'),
    19: ('Material drops', 'Death drops follow genes and bred points. Player kills unlock signatures and legends; pups give less. Pelts craft leather, sinew makes string, whiskers make Brushes and marrow makes Mutagen.'),
    20: ('The five legends', 'At stat +100, adult player kills can drop a legend. Vitality: Titan Haunch; speed: Gilded Foot; Leap: Moon Sinew; Senses: Listening Shell; Bloom: Everbloom. More points improve the chance.'),
    21: ('Apex preparation', 'Open a Crucible empty-handed or with a chosen Tissue Sample. Its panel shows the source, combat forecast and five distinct offerings. Review summon, then Summon Apex, starts the hostile encounter. Inspection spends nothing.'),
    22: ('The ritual ring', 'Show ring positions marks eight possible Pedestal spots. Use any five at Crucible height: three blocks N/E/S/W or two diagonally. Clear dry space above. Success spends one sample and five legends; the living donor stays safe.'),
    23: ('Apex of the Line', 'Watch for its charge: dodge pounces and leave the four-block stomp. Half health enrages it. A player victory drops a crest that reads the original line or stands on a pedestal. Care tools refuse.'),
    24: ('Guava juice', 'Craft any guava with glass, or use a Juicer (93). Use a main-hand seed with juice off-hand to infuse. One seed makes one frozen bottle. Genes choose effects; seed quality and richer juice (94) improve them.'),
    25: ('Trough gardening', 'Ordinary healthy adults can harvest suitable ripe crops without bred points. Open Useful roles & work in care for readiness. Enable a home Trough. Bloom +10 unlocks growth, planting and compost; Nursery needs +20.'),
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
    39: ('Travel Basket', 'Craft six planks, leather and a lead into a basket. Pack one calm unled animal; release on clear dry ground. Identity and care rest persist; its old Nest becomes free. Calipers reads it. Stations: 82.'),
    41: ('Nursery Cradle', 'Craft wool, glass, a Nest and planks. Insert one seed; a loaded minute grows one stage. Bone meal helps; Starlit Dust ripens it. Read litter forecast shows its saved parents, quality and pup odds. Cradles add no Fortune bonus.'),
    42: ('Hatch safely', 'Open a Cradle and choose Show hatch area to mark its actual 3x3 pad. Clear dry footing and two air blocks; angry particles mark the first obstruction. When ripe, Hatch litter or pulse redstone. Manual-only controls stay in the panel.'),
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
    54: ('Plan a breeding goal', 'At a Table, choose a commission and forecast two samples. Compare the real seed next: quality is already saved. With Calipers, check offspring and raise a promising pup, then show the adult at the same request’s Board.'),
    55: ('Plan the harvest', 'Read the table with a Fortune tool in either hand. Uses the highest held level, adding 0.5 quality per level at crop harvest. An opposite guava or Mutagen also applies. Cues stay intact. Cradles get no Fortune bonus.'),
    57: ('Cactus gardens', 'Hoe-enable a trough for desert-browser adults. They trim one spare cactus top, leaving lower segments. Keep clear ground and a safe route. Flowers stay intact. Diet, filters, power, protection and shared work rest apply.'),
    58: ('Regional farms', 'New farms grow desert cactus, Nether wart, swamp mushrooms, cave glow berries or End chorus. Some fit an empty enabled trough. Stock it for meals. Old settlements keep their plans. End care: 81.'),
    59: ('Guava orchards', 'Use one plain guava on the underside of leaves to plant green fruit in air. Any leaf type works. Bone meal adds one age; two ripen it. Pick ripe fruit to keep it growing. Shears or Silk Touch recover guava leaves for new orchards.'),
    60: ('Mushroom beds', 'Healthy mushroom eaters thin red or brown beds beside an enabled trough. Another living mushroom of the same color must remain within three blocks. Keep mycelium and clear routes. Native spread regrows the patch; work rests a minute.'),
    61: ('Fruit meals', 'Injured adult residents eat ripe hanging guavas near home by day. Matching Diets also pick sweet or glow berries. Plants stay to regrow. One meal heals 2 HP and shares the thirty-second food rest. Safe routes and protection apply.'),
    62: ('Sapling stations', 'Dispensers plant guava saplings into air over valid soil. Blocked targets keep stock. Grass, podzol and mycelium work. Bone meal tries growth. Lost soil drops one sapling. Give the whole tree loaded space.'),
    63: ('Underground farms', 'New five-tree cave and Nether warrens may fit a regional farm: glow berries under a trellis, or wart on soul sand. Plots need no water. A clear corner may fit an empty enabled trough. Existing settlements keep their plans.'),
    64: ('Safe seedbeds', 'Plant litter seeds on farmland. If soil changes to dirt, grass, podzol or mycelium, the crop keeps its whole litter and continues growing. Lost support returns the saved seed. Changing soil never hatches it or resets its parents.'),
    65: ('Snow-coat care', 'Brush healthy adult snow coats at Bloom +10 in a snowy climate for one snowball. Whiskers and Starlit Dust can combine under shared ten-minute rest. Shears gather wool separately. Home routes: page 88.'),
    67: ('Loose food meals', 'Injured adults eat ordinary loose food near home by day after ten seconds. Diet, pickup delays and owner reservations apply. Upgraded guavas stay. One item heals 2 HP; food rest is thirty seconds. Keep a dry clear route.'),
    68: ('Trough gathering', 'Healthy adults at Senses +10 move one old loose matching food into a nearby hoe-enabled trough by day. Diet, filters, power and safe routes apply. Collection shares the one-minute garden rest. Loose meals: page 67.'),
    69: ('Melon beds', 'Healthy foragers and omnivores harvest a slice beside a living attached melon stem, near an enabled trough. The stem regrows fruit; decorative melons stay. Slices heal or grow matching lines. Stems: 70. Farms: 78.'),
    70: ('Grow melon stems', 'Bloom +10 grows a young melon stem by one age at an enabled trough. No food is made. Mature stems await native fruit growth; leave nearby soil and air. Meadow stores may hold seeds. All jobs share one-minute garden rest.'),
    71: ('Orchard pickers', 'A dispenser with shears picks one ripe hanging guava and resets it to green for one tool use. Unripe fruit keeps the tool. Hoppers collect drops; shears still work on animals and beehives. Repair gaps: page 74.'),
    72: ('Habitat meals', 'Injured matching lines eat spare cactus tops, extra same-color mushrooms, ripe wart or stem-linked melons near home by day. Growing stock stays. No trough is needed. A meal heals 2 HP and shares thirty-second food rest. Predators: 80.'),
    73: ('Nest signals', 'Read a Nest with a comparator: 15 means claimed, 0 means free. The resident can be away or unloaded. Rehoming updates both Nests. Packing into a Travel Basket or death frees the old one.'),
    74: ('Repair a canopy', 'A dispenser plants one plain guava into an empty dry gap beneath leaves. Other targets eject the guava as food, so pulse once to repair a canopy. Upgraded guavas never plant fruit. Bone meal grows it; shears pick it.'),
    75: ('Work modes', 'Sneak-hoe a trough to cycle Mixed, Garden, Gather, Compost, Nursery and Supply. Normal hoe toggles work; power pauses it. Garden replants. Pause one resident: 85. Specialist jobs: 84.'),
    76: ('Restore a garden', 'Garden mode: Bloom +10 residents spend one suitable food above an eight-item reserve to replant a valid gap. Use the plant\'s normal soil or support. Carrots, guavas and berries are examples. Jobs share one-minute work rest.'),
    77: ('Nursery workers', 'Nursery mode: Bloom +20 adults spend one plain guava above an eight-food reserve for one Cradle stage. The whole litter stays. Jobs share garden rest. Players or redstone hatch ripe litters.'),
    78: ('Meadow melon farms', 'Fresh five-tree meadow farms mix young melon stems with ordinary crops. Empty dirt spaces allow fruit; a clear corner may fit an enabled trough. Plants grow naturally. Old settlements keep their plans. Garden repair: page 76.'),
    79: ('Work roll call', 'Sneak-use Calipers on a trough for its mode, food, filter and nearby residents. The report explains age, health, home, traits, recovery and stock limits. Ready means eligible to search. Leave a clear route and the right work nearby.'),
    80: ('Safe hunting', 'Wounded hunters and fishers follow dry home routes to wild prey. They stop for unsafe terrain, changed homes or protection. Named, leashed and transported animals stay. One kill heals 4 HP and shares thirty-second food rest.'),
    81: ('Chorus gardens', 'Bloom +20 chorus eaters revive spent flowers for one fruit. Mixed/Garden troughs store it; injured adults eat it for 2 HP. Living flowers keep their branch age. Leave supported branches and dry paths.'),
    82: ('Basket stations', 'A dispenser packs one calm unled bunnidog ahead. Filled baskets release onto clear dry ground. Failed pulses keep the basket. Identity and care rest persist. Each pulse acts once; sort with hoppers.'),
    83: ('Supply the troughs', 'Supply mode: Senses +20 adults move one ordinary surplus food to a nearby non-Supply trough below eight food, keeping eight at source. Keep both unpowered with dry paths. One job shares the one-minute work rest.'),
    85: ('Pause a resident', 'Open Useful roles & work in the care panel to pause/resume home chores and see readiness. A crouched hoe remains a shortcut without wear. Meals, breeding and home routines continue. Pups and Travel Baskets keep the preference.'),
    86: ('Cradle controls', 'Open a Cradle for Hatch litter and manual-only controls. Sneak-hoe also toggles redstone hatching. Growth and nursery work continue. Re-enable redstone before sending a fresh hatch pulse.'),
    87: ('Guava hand care', 'Sneak-feed a plain guava for healing or pup growth. Care shows loaded growth time. A golden dandelion pauses or resumes it; Bloom respects the pause. Food cannot add bred points. Normal guava use on ready adults starts courtship.'),
    88: ('Snow surface routes', 'Snow-footed residents cross powder-snow surfaces on home routes. Woolly or hidden snow gives no footing. Deep powder and other hazards stay blocked. Lost footing or floors interrupt jobs. Keep dry room above.'),
    90: ('Phase legs', 'End founders may express Phase legs. After a nonlethal attack, grounded adults blink 3-6 blocks to safe dry ground, farther from danger. Recovery: 20 seconds. Leads, rides and passengers prevent it. Damage still lands.'),
    91: ('Webbed paws', 'Swamp webbed legs add 50% water propulsion but still need air. Land speed and jumps are slightly lower. Toe membranes show only when expressed. Breed or splice them into other coats. Home work keeps dry routes.'),
    92: ('Fertility marks', 'Tail tips show fertility: average bare; prolific one rosette; shy a green bud; swarm three rosettes; starborn a violet star. Pups show them too. Marks show expressed genes, not readiness.'),
    93: ('The Juicer', 'Load guava + bottle, or seed + juice. A pair takes 5 seconds; power pauses. Hoppers feed fruit/seed above and bottles/juice beside, then drain below. Empty-hand takes output; sneak-empty recovers inputs.'),
    94: ('Richer juice', 'All eight guava tiers make juice. Infusion uses the higher seed or juice quality. The seed still decides genes and inherited stat drift. Uninfused drinks only restore hunger; save rich juice for infusion.'),
    95: ('Name a lineage', 'Open a Pedigree Book or Registry, choose a record, then Name lineage. Limit: 40 characters; empty clears. Names label that exact genome in this book. Search line:name. Re-logging keeps names; animals stay unchanged.'),
    96: ('Pelt rugs', 'Two matching pelts and string craft one coat rug. All 13 coats keep their colors and patterns. Place on a supported floor; removing support drops the rug. Mixed pelts still craft leather without string.'),
    97: ('Specimen Jars', 'Open empty-handed for Preserve next sample, Read sample and Retrieve sample. Direct held samples still fill an empty Jar. Complete saved samples stay reusable. Comparator: zero empty; 1 plus 2 per fixed gene pair, up to 15.'),
    99: ('Breeding Pen', 'Craft fences, hay, planks and copper with a feather or Whisker. Open for stock and Feed both parents. Parents need full health, separate Nests and safe nearby space. Redstone also feeds. An adult villager can claim the Pen as a Breeder job site.'),
    100: ('Seed quality colors', 'Seed bands: plain 0, gold 1, diamond 2, netherite 4, star 7, beacon 12, egg 20, command 33. Fractions use the lower band. Tooltips give exact quality. Fortune adds quality at harvest. Stored genes and Mutagen stay intact.'),
    102: ('Coat camouflage', 'Healthy calm grounded coats blend: half visual range, no walking vibrations. Injury, danger, love, Glowing, water, cargo and transport break cover. Other vibrations still reach sculk.'),
    103: ('Matching ground', 'Tan/cream/sand: sand. Brown/spotted: earth. Moss: green cover. Snow: snow. Pale/slate: light/dark stone. Ember: netherrack. Void: obsidian or purpur. Glow cannot blend. Calipers shows cover.'),
    104: ('The Breeder', 'An unemployed adult villager claims a Breeding Pen job site. Animal Nests stay separate. Trading locks the career; native restocks and discounts apply. Your Pen still pays its own guavas.'),
    105: ('Breeder supplies', 'Novice: 20 guavas for 1 emerald; 1 emerald for 4 tags, 2 for a guide. Apprentice: 4 local pelts for 1 emerald; syringe costs 4. Journeyman: 8 sinew for 1 emerald; Pen costs 10.'),
    106: ('Expert trades', 'Expert: Juicer 12 emeralds; golden guava 8. Master: diamond guava 32 emeralds + 4 diamonds; Splicer 24 emeralds + 1 Mutant Marrow. Prices vary. Stock renews at work.'),
    107: ('Abandoned barns', 'New dry clearings may hold rare barns: empty tools, two Nests and one supply chest. Repair the broken rail. Cartographers may sell Barn Explorer Maps for 10 emeralds + a compass.'),
    108: ('Gene comparator', 'Open empty-handed to copy a shown sample and choose Exact or Matched-pair signal. Show front pad marks the one-animal space. Copies are free. Hidden genes count; parental copy order, age and bred points do not.'),
    109: ('Genetic circuits', 'Empty-hand switches exact/analog; sneak with both hands empty clears. Analog: 1 + 2 per matched pair. Missing, blocked or crowded pads give 0. Checks each half second. Calipers inspects.'),
    110: ('Bundle carriers', 'Adults carry bundles: slim/lean 1, average 2, stocky/woolly 3, chonker 4. Use a bundle to equip it. Sneak with both hands empty to retrieve the last. Empty-hand inspects. Cargo safety: 111.'),
    111: ('Cargo safety', 'Saves and baskets retain whole bundles. Smaller builds keep excess: each extra slows movement 25%, up to 75%. Retrieve to recover speed. Cargo breaks camouflage and drops as equipment on death.'),
    113: ('Regional kennels', 'Meadow, desert and snowy kennels have an intact yard, two floor Nests, a bed, care tools and supplies. Bring your own animals. The empty Pen can become a villager job site.'),
    114: ('The pairing yard', 'Lead two adults home to the sheltered Nests. Stock both Pen mangers with guavas. Open the Pen and choose Feed both parents, or pulse it. Other adults within 12 blocks stop pairing. Courtship makes the seed.'),
    115: ('Show Ring arenas', 'Rare clearings hold a public stand, two holding bays and three empty trophy pedestals. Lead your entrant to the gold floor mark near the stand. Normal judging rules apply. Ribbons stay earned.'),
    116: ('Venue maps', 'Cartographers may sell Kennel Explorer Maps at journeyman for 12 emeralds + compass, Showground Maps at expert for 16 + compass. Look in new dry clearings. Native prices vary; repair around obstacles.'),
    118: ('Island refuge', 'Mild-ocean refuges hold three coastal founders, sheltered Nests, guavas and supplies. Expert cartographers may sell maps for 20 emeralds + compass. Explore new chunks; bring a basket or lead.'),
    119: ('Coastal genes', 'Seal fur normally doubles air on average. Paddle feet normally double water propulsion, with shorter legs on land. Both breed into other lines. Still needs air; hidden copies give no benefit.'),
    120: ('Shore meals', 'Shore foragers eat kelp, dried kelp and seagrass. Injured adults trim upper kelp from dry home banks by day. Roots stay and water returns. Healing shares normal meal rest; mob-griefing applies.'),
    121: ('Kelp workers', 'Healthy housed shore residents trim upper kelp into a nearby enabled trough. Opt in with a hoe. Keep a two-block stalk and reachable dry bank. Roots stay; natural growth supplies the next top.'),
    122: ('Restore a tide bed', 'Choose Garden mode with a sneaking hoe. Bloom +10 workers spend one kelp on a bare bed under two blocks of still water, keeping eight in stock. They stay dry and leave living stems to regrow.'),
    123: ('Pale cave guide', 'In dim light, sneak-feed a calm pale adult with Senses +20 one glow berry for 60 seconds of Night Vision. Needs half health; rests 30 seconds. Existing vision keeps the berry. Saves keep recovery.'),
    124: ('Genetic discoveries', 'Log a bred animal carrying a mutation-only allele: Nature Finds a Way. Keep all eight wild origins in one book: Wild Gene Library. Hidden copies count; unlogged parents do not. Rewards pay XP once.'),
    126: ('Train a trail partner', 'Craft a Whistle with copper, two string or Sinew, and a feather or Whisker. Open a calm loyal/stoic adult and choose Train with the tool and one plain guava in your inventory. Direct whistle use still works.'),
    127: ('Trail orders', 'Open your partner empty-handed for Follow, Wait and Return home. A whistle still switches Follow/Wait; air calls Follow, sneak-air returns home. Sneak-whistle on the animal releases training.'),
    128: ('Safe trail journeys', 'Trail orders pause home chores. Following walks dry paths; danger and transport take priority. Phase still escapes real attacks. Training and cargo travel in baskets; baskets clear the old home as usual.'),
    129: ('Whistle calls', 'Calls reach at most 16 partners. Hearing follows ears and Senses, 8-32 blocks; calls rest two seconds. A follower waits ten seconds for a lost keeper, then resumes residence. Wait stays until changed.'),
    131: ('Breeder commissions', 'Boards use planks, copper and a feather/Whisker. In animal care, Calipers check a goal before you raise a pup. Lead a fitting parented adult onto its clear front pad near a Breeder. Choose the same request; submit one paper.'),
    132: ('Night Guide', 'Fix pale coat and bat ears; breed Senses +20. Bring the parented adult to a staffed Board with one paper. Night Guide pays 4 emeralds, 8 glow berries and 50 XP once per keeper. Your animal stays.'),
    133: ('Coastal Scout', 'Fix seal coat, paddle legs and shore-forager Diet; breed Senses +10 and Swiftness +10. Coastal Scout pays 6 emeralds, a lead, 8 dried kelp and 50 XP once. Board rules apply; keep the animal.'),
    134: ('Mountain Courier', 'Fix hoofed legs and a Build that carries 3 or 4 bundles. Breed Leap +20 and Swiftness +10; equip one bundle. Mountain Courier pays 6 emeralds, a bundle and 50 XP once. Keep all cargo.'),
    135: ('Ember Gardener', 'Fix ember coat and nether-forager Diet; breed Bloom +10. Ember Gardener pays 4 emeralds, 8 nether wart, 4 bone meal and 50 XP once at a staffed Board. Bring a calm bred adult; keep the animal.'),
    136: ('Frost Steward', 'Fix snow coat, tiny ears and fisher Diet; breed Bloom +10 and Vitality +20. Frost Steward pays 4 emeralds, 4 snowballs, 8 cod and 50 XP once. The snow coat also supports cold-climate care.'),
    137: ('Desert Messenger', 'Fix fennec ears, fleet legs and lean Build; breed Swiftness +20 and Senses +10. Desert Messenger pays 6 emeralds, 4 string, a lead and 50 XP once. Lean runners still trade health for speed.'),
    138: ('Swamp Herbalist', 'Fix moss coat, webbed legs and mushroom-eater Diet; breed Bloom +10 and Senses +10. Swamp Herbalist pays 4 emeralds, 4 bone meal, 4 moss blocks and 50 XP once. Keep your living harvest line.'),
    139: ('End Nursery', 'Fix void coat, phase legs, starborn fertility and chorus-eater Diet; breed Bloom +20. End Nursery pays 6 emeralds, 4 chorus fruit, 2 Starlit Dust and 50 XP once. Native seed/work rules remain.'),
    140: ('Crossing Branches', 'Fix all seven gene pairs and combine at least four wild origins; breed Senses +20. Crossing Branches pays 12 emeralds, a golden guava and 100 XP once. Common alleles add no wild origin. Animal stays.'),
    141: ('Trail provisions', 'Allow bundle meals in your partner\'s care panel, or use a bowl. Injured calm adults eat one suitable ordinary food for 2 HP, then rest 30 seconds. Upgraded guavas stay. Follow/Wait only; hand care still works.'),
}
PAGE_COUNT = 141
MAIN_INDEX = {2:'Start playing',4:'Breeding & seeds',8:'Homes & food',11:'Care & traits',15:'Lab & splicing',
              17:'Registry & shows',19:'Material drops',20:'Apex of the Line'}
MORE_INDEX = {112:'Workshops & sites',25:'Gardens & stores',27:'Boss forecast',28:'Living harvests',101:'Coats & fieldcraft',
              31:'Resident needs',32:'Refuge & storms',34:'Troughs & bell calls',36:'Fleece & dispensers',39:'Travel Basket'}
NEW_INDEX = {41:'Nursery & compost',44:'Treat a litter',45:'Nest guards',46:'Warren maps',47:'Mineral scouts',49:'Pause trough work',51:'Find a tagged animal',52:'Bell roll call',53:'Recovery rest',54:'Plan a litter'}

HABITAT_INDEX = {57:'Cactus gardens',58:'Regional farms',59:'Guava orchards',60:'Mushroom beds',61:'Fruit meals',62:'Sapling stations',63:'Underground farms',64:'Safe seedbeds',89:'Lineage traits'}
WORK_INDEX = {67:'Loose food meals',68:'Trough gathering',69:'Melon beds',71:'Orchard pickers',72:'Habitat meals',73:'Nest signals',75:'Work modes',76:'Restore a garden',84:'Specialist work'}
SPECIALIST_INDEX = {77:'Nursery workers',79:'Work roll call',80:'Safe hunting',81:'Chorus gardens',82:'Basket stations',83:'Supply the troughs',85:'Pause a resident',86:'Cradle controls',87:'Guava hand care'}
LINEAGE_INDEX = {13:'Useful coats',14:'Physical traits',29:'Terrain traits',65:'Snow-coat care',88:'Snow surface routes',90:'Phase legs',91:'Webbed paws',92:'Fertility marks',101:'Coats & fieldcraft'}
WORKSHOP_INDEX = {15:'Genetics Table',17:'Stable line registry',24:'Juice & infusion',93:'The Juicer',94:'Richer juice',95:'Name a lineage',96:'Pelt rugs',97:'Specimen Jars',99:'Pens & seed colors'}
FIELD_INDEX = {102:'Coat camouflage',103:'Matching ground',104:'The Breeder',105:'Breeder supplies',106:'Expert trades',107:'Abandoned barns',108:'Pedigree Comparator',109:'Genetic circuits',110:'Bundle carriers'}
VENUE_INDEX = {98:'Breeder workshop',107:'Abandoned barns',113:'Regional kennels',114:'The pairing yard',115:'Show Ring arenas',116:'Venue maps',117:'Wild & companions',130:'Breeder requests'}
EXPLORER_INDEX = {118:'Island refuge',119:'Coastal genes',120:'Shore meals',121:'Kelp workers',122:'Restore a tide bed',123:'Pale cave guide',124:'Genetic discoveries',125:'Trail partners'}
TRAIL_INDEX = {126:'Train a partner',127:'Trail orders',128:'Safe trail journeys',129:'Whistle calls',141:'Trail provisions',131:'Commission Board',130:'Breeder requests'}
COMMISSION_INDEX = {132:'Night Guide',133:'Coastal Scout',134:'Mountain Courier',135:'Ember Gardener',136:'Frost Steward',137:'Desert Messenger',138:'Swamp Herbalist',139:'End Nursery',140:'Crossing Branches'}

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
          'ingredients':['minecraft:book','minecraft:paper','#bunnidogs:bunnidog_food'],'result':{'id':'bunnidogs:field_guide'}})
    write('data/bunnidogs/advancement/recipes/tools/field_guide.json', {'parent':'minecraft:recipes/root',
          'criteria':{'has_guava':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#bunnidogs:bunnidog_food'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:field_guide'}}},
          'requirements':[['has_guava','has_recipe']],'rewards':{'recipes':['bunnidogs:field_guide']}})
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({'item.bunnidogs.field_guide':'Bunnidogs Field Guide',
          'guide.bunnidogs.title':'Bunnidogs Guide','guide.bunnidogs.contents':'Contents',
          'guide.bunnidogs.more_contents':'More chapters','guide.bunnidogs.main_contents':'Main contents',
          'guide.bunnidogs.new_contents':'New chapters','guide.bunnidogs.habitat_contents':'Habitat chapters','guide.bunnidogs.work_contents':'Working residents','guide.bunnidogs.specialist_contents':'Specialist work','guide.bunnidogs.lineage_contents':'Lineage traits','guide.bunnidogs.workshop_contents':'Breeder workshop','guide.bunnidogs.field_contents':'Coats & fieldcraft','guide.bunnidogs.venue_contents':'Workshops & sites','guide.bunnidogs.explorer_contents':'Wild & companions','guide.bunnidogs.trail_contents':'Trail partners','guide.bunnidogs.commission_contents':'Breeder requests'})
        for page,label in (MAIN_INDEX | MORE_INDEX | NEW_INDEX | HABITAT_INDEX | WORK_INDEX | SPECIALIST_INDEX | LINEAGE_INDEX | WORKSHOP_INDEX | FIELD_INDEX | VENUE_INDEX | EXPLORER_INDEX | TRAIL_INDEX | COMMISSION_INDEX).items(): value[f'guide.bunnidogs.index.{page}'] = label
        for page,(title,body) in PAGES.items():
            value[f'guide.bunnidogs.page.{page}.title'] = title
            value[f'guide.bunnidogs.page.{page}.body'] = body
        path.write_text(json.dumps(value,indent=2)+'\n')
    for path in sorted((RES/'data/bunnidogs/loot_table/chests').glob('warren_pantry*.json')):
        loot = json.loads(path.read_text())
        if not any(entry.get('name') == 'bunnidogs:field_guide' for pool in loot['pools'] for entry in pool['entries']):
            loot['pools'].append(pantry_guide_pool())
        write(str(path.relative_to(RES)),loot)
    preview = Image.new('RGBA',(1220,2050),(43,46,39,255)); d = ImageDraw.Draw(preview)
    preview.alpha_composite(img.resize((224,224),Image.Resampling.NEAREST),(14,85))
    try:
        font = ImageFont.truetype('DejaVuSans.ttf',13)
        bold = ImageFont.truetype('DejaVuSans-Bold.ttf',13)
    except OSError:
        font = bold = ImageFont.load_default()
    for i,page in enumerate([1,2,4,5,8,11,15,41,42,54,86,87,99,110,114,125,126,127,131,132]):
        x=255+i%4*240; oy=i//4*410
        d.rectangle((x,oy+20,x+224,oy+397),fill=(230,216,174),outline=(117,90,48),width=2)
        title = 'Bunnidogs Guide' if page == 1 else 'More chapters' if page == 40 else 'New chapters' if page == 50 else 'Habitat chapters' if page == 56 else 'Working residents' if page == 66 else 'Specialist work' if page == 84 else 'Lineage traits' if page == 89 else 'Breeder workshop' if page == 98 else 'Coats & fieldcraft' if page == 101 else 'Workshops & sites' if page == 112 else 'Wild & companions' if page == 117 else 'Trail partners' if page == 125 else 'Breeder requests' if page == 130 else PAGES[page][0]
        d.text((x+14,oy+35),title,font=bold,fill=(40,39,30))
        if page == 1: body = '\n'.join(MAIN_INDEX.values()) + '\n\nMore chapters\nNew chapters\nHabitat chapters'
        elif page == 40: body = '\n'.join(MORE_INDEX.values()) + '\n\nMain contents'
        elif page == 50: body = '\n'.join(NEW_INDEX.values()) + '\n\nMain contents'
        elif page == 56: body = '\n'.join(HABITAT_INDEX.values()) + '\n\nMain contents\nWorking residents'
        elif page == 66: body = '\n'.join(WORK_INDEX.values()) + '\n\nMain contents\nHabitat chapters'
        elif page == 84: body = '\n'.join(SPECIALIST_INDEX.values()) + '\n\nMain contents\nWorking residents'
        elif page == 89: body = '\n'.join(LINEAGE_INDEX.values()) + '\n\nMain contents\nHabitat chapters'
        elif page == 98: body = '\n'.join(WORKSHOP_INDEX.values()) + '\n\nMain contents\nMore chapters'
        elif page == 101: body = '\n'.join(FIELD_INDEX.values()) + '\n\nMain contents\nMore chapters'
        elif page == 112: body = '\n'.join(VENUE_INDEX.values()) + '\n\nMain contents\nMore chapters'
        elif page == 117: body = '\n'.join(EXPLORER_INDEX.values()) + '\n\nMain contents\nWorkshops & sites'
        elif page == 125: body = '\n'.join(TRAIL_INDEX.values()) + '\n\nMain contents\nWild & companions'
        elif page == 130: body = '\n'.join(COMMISSION_INDEX.values()) + '\n\nMain contents\nTrail partners'
        else: body = '\n'.join(textwrap.wrap(PAGES[page][1],25)) + '\n\nContents'
        d.multiline_text((x+14,oy+75),body,font=font,spacing=6,fill=(40,59,36))
        d.text((x+14,oy+365),f'Page {page} / {PAGE_COUNT}',font=font,fill=(85,77,56))
    d.text((15,350),'Book icon + text preview',font=font,fill=(224,217,185))
    d.text((15,375),'Substitute font; native UI pending',font=ImageFont.load_default(),fill=(224,217,185))
    preview.save(ROOT/'art/guide_preview.png')
    print('Generated native Field Guide icon, pages, recipe/unlock and 35% pantry pool.')


if __name__ == '__main__': main()
