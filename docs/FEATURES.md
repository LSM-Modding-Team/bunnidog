# Bunnidogs: how to use each feature in game

**Field Guide (P1):** combine a vanilla book with any guava. Use the book to
open 111 pages from first warren to Apex. Nine linked contents pages offer 82
chapter links, including breeder workshop and fieldcraft. Habitat and Lineage traits
collect regional gardens and inherited abilities.
It can also appear in unopened warren pantry barrels (35% chance). Native book
pages, rather than a separate custom screen, explain the progression in-game.
Craft a fresh guide for the appended chapters in an existing world.

**Bundle carriers (H11):** use a bundle on a calm adult to equip it. Slim/lean
carry one, average two, stocky/woolly three, chonker four. Both hands empty:
normal use inspects; sneak retrieves the last. Whole contents, names and colors
save and travel in baskets. Smaller builds retain excess but slow 25% per extra
bundle, up to 75%; retrieval restores speed. Cargo drops as equipment on death
and exposes camouflage. Calipers reports count/capacity/overload. Visible leather
panniers are separate from the approved animal sculpt. See `CARRYING_SPECS.md`.

**Coat camouflage (A10):** full-health calm animals blend on matching support:
sand for tan/cream/sand, exposed earth for brown/spotted, green cover for moss,
snow for snow, pale/dark stone for pale/slate, netherrack for ember and obsidian/
purpur for void. Glow cannot blend. Native detection range halves and walking
vibrations stop; injury, danger, love, Glowing, water, jumping and transport break
cover. Eating/attacks/landings still reach sculk. Sneak-Calipers explains status.

**Breeder villagers (V1):** place a Breeding Pen for an unemployed adult villager.
Native trade progression supplies tags, guides, syringes, Pens, Juicers and early
quality guavas. Apprentice pelt contracts follow the villager's region. Master
diamond fruit and Splicers still require real diamonds/Marrow beside emeralds.
The Pen keeps its own paid animal stock. See `BREEDER_SPECS.md` for base prices.

**Abandoned breeding barns (V2):** explore new dry clearings for weathered guava
barns with empty workshop tools, two Nests and one native supply chest. Restore
the broken paddock rail before bringing animals. Existing containers, fluid and
ore are protected; opened loot is never restocked. Cartographers may sell Barn
Explorer Maps. Use `/locate structure bunnidogs:breeding_barn` to find candidates.

**Pedigree Comparator (R1):** craft quartz/glass, copper/Whisker and a comparator.
Use a Tissue Sample to copy its reference without spending it. Exactly one
bunnidog centered on the clear front block gives 15 when all seven pairs match.
Hidden copies count; pair order, stats and age do not. Empty-hand switches analog
mode: 1 + 2 per matched pair. Sneak with both hands empty clears; Calipers reads.
Blocked/absent/crowded pads output 0. Checks run every half second, powering normal
redstone and comparators for player-built gates and sorting. The information saves
on the block and clears when broken; no physical sample inventory is held.

**Breeding Pen (G6):** craft `FHF / PWP / CCC` with wooden fences, hay, planks,
a Whisker and copper. Place inside a dry pen with two healthy, ready adult
residents and their Nests. Keep other adults twelve blocks away; bring the pair
within four blocks of the manger and less than three apart. Front-left A and
front-right B hold separate guavas. Empty-hand use or one redstone pulse spends
one each; ordinary courtship produces the seed with their averaged tiers.
Calipers inspects, sneak-empty recovers stock. Hoppers above feed A; horizontal
hoppers feed B. Put collection hoppers on the surrounding floor. Held power
does not feed repeatedly; hazards, leads, rests and automatic protection refuse.

**Seed quality colors (S11):** eight looks follow stored quality at 0, 1, 2, 4,
7, 12, 20 and 33, using the lower band for fractions. Hover for exact quality.
Old seeds gain the look directly from their existing data. Fortune is added
at harvest; colors change no genes, stats, Mutagen or hatching behavior.

**Juicer (J1):** craft `PIP / PBP / CCC` with planks, iron, a bottle and copper.
Load guava + bottle to press matching juice, or a valid litter seed + uninfused
juice to make one frozen infusion. Each pair takes five loaded seconds; power
pauses it. Hoppers feed fruit/seeds above, bottles/juice horizontally and extract
results below. Empty-hand use collects results; sneak with both hands empty
recovers inputs. Blocked output waits without consuming the pair. Comparators
report output stock; saved stations retain exact items and progress.

**Richer juice (J2):** every guava tier crafts/presses its own juice. Hand or
Juicer infusion uses the higher seed/carrier quality for effects. The seed still
determines rolled genes, mutation and inherited stat drift. Ordinary drinking
gives the same hunger restoration at every tier; keep rich carriers for infusion.
The original plain recipe and hand use work.

**Name a lineage (G5):** open a Pedigree Book or Registry, select a record and
choose **Name lineage**. Use up to 40 characters; empty clears. Labels belong to
that exact genome in this book, appear in the index/sheets and match `line:name`
searches. Re-logging preserves names. Acknowledged saves prevent a changed held
book from receiving an old edit. Animals, Ear Tags, parents and stats stay intact.

**Pelt rugs (D12):** two matching pelts and a string craft one coat rug. All
twelve coats have their own colors/patterns. Place on a supported floor, choosing
its orientation; removing support returns the rug. Mixed pelts still make leather
without string. Find rugs under Functional Blocks in creative.

**Specimen Jars (T5):** craft one wooden slab and three glass in ` P / G G /  G `
for two jars. Insert a Tissue Sample with either hand. The vial shows its coat;
use opens its complete genetic sheet. Sneak with both hands empty to recover
the sample; breaking drops jar and exact sample. Comparators read zero empty,
otherwise one plus twice the number of fixed gene pairs, up to fifteen. This
reads genes, without claiming the animal qualifies for the Registry.

**End Phase legs (A7):** find fresh End founders and breed or splice their Phase
legs into a line. Grounded adults blink 3–6 blocks to safe dry ground after an
actual nonlethal attack, farther from danger. Damage still lands; recovery is
20 seconds and persists while saved/packed. Leads, rides and passengers prevent
blinking. A resident stays inside its Nest boundary, and native teleport
protection can refuse it. Phase animals can cross ordinary fences/walls.

**Swamp webbed paws (A8):** fresh swamp lines carry webbed legs. Expressed
animals gain 50% stronger ordinary native water propulsion and visible toe
membranes/paddling, with slightly slower land speed/lower jumps. They still
need air; home jobs keep dry routes. Breeding/splicing transfers the trait
across coats. Existing animals retain their genes. Phase/webbed juice give Slow
Falling/Dolphin's Grace respectively; gene hovers show exact numbers.

**Visible fertility (A9):** read the tail tip: average is unmarked; prolific has
one rose rosette; shy a closed green bud; swarm three rosettes; starborn a violet
star. Only the expressed allele shows, including pups. This identifies fertility
rather than current breeding readiness or bred Bloom. The fixed baby face stays.

**Completed small-warren trails (U1):** fresh version-seven level-one surface
warrens fit worn paths from each mouth around its mound to the tree patch. A
blocked side uses the opposite route or leaves no paving; old generated/saved
starts keep their prior layouts. Higher-level streets retain their plans.

Update both client and server for this batch (network protocol 10). World/item/
animal formats stay compatible; the Pen and seed model need updated client content.

**Apex preparation (P2):** empty-hand crucible use lists missing legendary
offerings. Hold Calipers in the main hand and a Tissue Sample in the other, then
use the crucible to forecast the source line's boss: health, normal/enraged
attacks, armor, knockback resistance, awareness and motion. Reading preserves
all items and offerings. The forecast is available before completing the ring.

**Apex (G4):** craft an Apex Crucible (`OMO/ODO/OOO`: obsidian, Mutant
Marrow, diamond). Place five Trophy Pedestals at its height, using any five of
the eight ring slots: three blocks N/E/S/W or two blocks diagonally. Display a
Titan Haunch, Gilded Foot, Moon Sinew, Listening Shell and Everbloom. Offer a
Tissue Sample to summon a hostile boss with that source line's appearance and
adaptations. The donor stays intact; five legends and one sample are spent on
success (creative keeps them). Clear dry room above it; Peaceful and a nearby
living Apex prevent the ritual. Empty-hand use explains the setup.

Watch its charge, dodge pounces and leave the stomp's four-block reach. At half
health it becomes faster and stronger. A player victory gives an Apex Crest
that opens the original line's sheet or stands on a Trophy Pedestal; the first
victory earns Apex of the Line and 200 XP. Bosses reject food, care, leads and
rides. See `docs/APEX_SPECS.md` for exact rules.

**Inherited defenses and glow (A3):** slate coats add four Armor points against
ordinary attacks. Stocky/chonker builds reduce knockback by 15%/30%. A visibly
expressed glow coat stays bright in darkness; a hidden copy does not. It lights
the animal rather than nearby blocks. Crossbreeding can combine coat/build/leg
benefits. Gene hovers explain them, including on samples and forecasts.

**Terrain traits (A4):** hoofed adults walk up one-block ledges and keep their
half fall damage. Fleet legs reduce soul-sand/honey slowing by 25%, lean builds
by 50%; the two combine to 75%. Cobwebs, Slowness and other hazards remain.

**Shore fishing (Diet D5):** injured adult Fishers can hunt reachable cod,
salmon and tropical fish near their Nest by day. A selected kill heals 4 HP and
starts the shared 30-second food rest. They leave land prey/pufferfish alone;
full health, night, leads/rides and combat pause hunting.

**Local supplies (L6):** search new feed stores, markets and underground
pantries for guavas and local foods, such as tundra fish, cave glow berries,
swamp mushrooms, Nether wart/fungi and End chorus fruit. The independent 35%
Field Guide chance remains. Existing or emptied barrels never refill.

**Refuge (L7):** non-feral adults with a Nest retreat from nearby visible
hostiles when a safer dry route fits near home. Food, work and bell visits pause;
leads/rides take over. The routine preserves the Nest and makes no attacks.
Feral lines retain their retaliation. No safe route means no retreat, and
neither distance nor shelter makes an animal immune to harm.

**Storm shelter (L8):** idle adults wait under dry reachable cover near their
Nest during thunderstorms. Keep a safe floor and walking space near home.
Meals, work, home/social routines, breeding, danger and player control take
priority. Ordinary rain leaves their existing idle routines in place.

**Travel Basket (H6):** craft six planks, leather and a lead. Pack one calm,
unled ordinary adult or pup, then release on dry solid ground with room for its
actual size. Identity, health, age, pedigree and paused care recovery persist.
Packing frees its old Nest; blocked/invalid/duplicate placement keeps the animal
stored. Calipers reads the carried animal's sheet. The Apex cannot be carried.
Dispensers pack one eligible animal ahead into an empty basket, or release a
filled one onto dry supported ground. Failed pulses keep it; use separate
stations or sort baskets with hoppers to avoid alternating capture/release.
For records (S9), hold the filled basket in the other hand and sneak-use a
Pedigree Book or Purebred Registry. Normal use opens the book; registry adults,
recorded parents and matching copies in all seven genes are still required.

**Bloom cultivation (L12):** Bloom +10 adults advance matching unripe crops by
one stage when no ripe harvest is available. Growth shares the one-minute
garden rest and respects Diet, trough filters/capacity and block protection.
It creates no food until a later harvest. Ordinary foragers can gather one
sweet berry from a ripe bush, leaving it to regrow. Calipers names the bonus.

**Night meals (L13):** injured adults near their Nest can eat matching trough
stock at night. Keep food, safe standing room and the whole route within four
blocks of home. One item heals the normal 2 HP and starts the shared thirty-second
meal rest, then night waiting resumes. Calipers explains trough-only readiness;
outdoor foraging and work still wait for daylight. Leads and danger interrupt.

**Surplus composting (L14):** place a vanilla composter beside a hoe-enabled
trough. Healthy Bloom +10 residents compost one suitable real food after crop
work, leaving at least eight items for meals. Native chances apply and each job
shares the one-minute garden rest. Leave safe walking space and use a hopper
below the composter for ordinary bone meal. Diet, protection and daytime rules
apply; residents leave mature compost for you or the hopper.
Plain guavas compost at 65% and guava leaves/saplings at 30% (L15), including
ordinary manual/hopper use. Higher guava tiers remain non-compostable by default.

**Living moss (C9):** brush a healthy adult expressing a moss coat at Bloom +10
for one Moss Carpet. Qualified whiskers and Starlit Dust are collected together
for one Brush wear and the same ten-minute animal recovery. Hidden moss copies
yield none; changing genes or tools cannot bypass rest. Dispensers share the
same products and rules. Calipers names the prospective yield.

**Redstone nursery (N1):** face a dispenser at empty space above farmland and
supply litter seeds. One successful pulse plants one exact saved litter. A
second dispenser facing the crop can use one Starlit Dust to ripen it. Refused
planting/ripening retains the item. Break ripe crops to hatch as usual; seeds,
parents/stats, quality and Mutagen are not rerolled by planting or growth.
Planted Seed Crops now keep dry farmland. A Dropper delivers loose seed/dust
items. Hand planting/ripening keeps its ordinary controls and creative costs.
Named seeds retain all item components in the ground and on recovery (N2).
Cancelled protected breaks create no items or pups (N4). Environmental removal
returns the seed; a crowded ripe crop stays planted. If native spawning refuses
a harvested litter, all new siblings roll back and the original seed returns.

**Nursery Cradle (N3):** craft `WWW/GNG/PPP` with wool, glass, a Nest and planks.
Insert one litter seed; each loaded minute adds one of seven stages. Bone meal
adds 2–5 stages and Starlit Dust ripens it. Hoppers move the whole named seed;
comparators read 0 empty to 15 ripe. Dispensers supply seeds, meal or dust.
Use Calipers to forecast the litter. Sneak-use with both hands empty to withdraw
and reset incubation. Breaking returns the block and seed once.

Use a ripe Cradle empty-handed or give it a fresh redstone pulse to hatch.
Clear a dry, supported 3×3 pad one to three blocks in front with two blocks of
air, and stand aside. Obstruction, occupants, crowding or refused spawns retain
the litter. Holding power does not repeat or hatch a seed that ripens later.

**In-place Mutagen (N5):** use a vial directly on a planted seed or occupied
Cradle, or dispense it at that block. One application treats the existing
litter without uprooting it or resetting growth. Names, parents, quality and
incubation stay intact. Ripe litters may be treated; already-treated or invalid
targets keep the vial. Existing held-seed/off-hand treatment still works.

**Cradle planning (N6):** sneak-use Calipers for incubation, comparator output,
crowding and a blocked location. Temporary green markers show a clear pad;
angry particles show trouble and smoke means crowding. This works while empty
or growing and changes nothing. Normal Calipers still shows litter genetics.

**Grand-tree nurseries (N7):** newly generated level-five halls may include an
empty Cradle with a fitted pad, alongside their regular trough/bell and eight
floor Nests. Existing saved warrens retain their old furniture. Actual animals
and later block changes can occupy a pad; sneak-Calipers checks current conditions.

**Nest wayfinding (H7):** use a compass on a Nest to mark that place. Its needle
points there in that dimension and spins elsewhere; the marker remains if the
Nest is removed. Names/components survive and stacked compasses bind one item.
Empty-hand use with no led animal inspects the free/claimed Nest and any nearby
resident. Led-adult reassignment keeps priority. Inspection changes no tickets.

**Bell calls (L10):** ring a vanilla bell to call healthy adult residents by day,
including outside noon. Hand use, redstone and projectiles work. Keep it within
12 blocks of their Nest and leave safe walking space. Calls use the same short
visits and minute of rest; injury, danger, leads/rides and night take priority.

**Trough filters (L11):** sneak-use food to reserve its exact type/components
without spending it. Hoppers and gardeners obey the choice even while empty.
Sneak-empty withdraws stock; repeat while empty to clear the filter. Saved
reservations never count as real food or drop on breaking.

**Trough automation (L9):** use hoppers above/beside a trough to supply food.
A hopper below drains it; power that hopper to retain meals for residents.
The existing 16-item single stack and food/component rules apply. Comparators
read 0 when empty, up to 15 when full. Gardening still needs a hoe opt-in.

**Resident gardens (L3/L4):** newly fitted dens have a trough with two starter
guavas, and levels 3–5 also fit a central gathering bell. Use a hoe on any trough
to toggle gardening (green markers = on). Fully healthy adult residents with a Nest
can gather matching ripe food within three blocks, one item per minute of work
recovery. Keep walking space beside the trough and mob griefing enabled. Crops
reset rather than breaking; food enters the trough, with no seed/bonus drops.
Old/crafted troughs start off. New generated ones start on. Empty a guava-stocked
trough before asking it to store another food. Existing stock never refills by
generation. Foods still follow the gardener's expressed Diet.
New farms may fit an empty enabled trough in one corner, retaining seven crops
inside the same footprint. Blocked walking space keeps the old eight-crop farm.
Sneak-use Calipers for gardening blockers and recovery; ready means ready to
search for an enabled trough, compatible ripe food and a safe route (L5).

**Settlement life (L1/L2):** craft a Feeding Trough with seven planks and one
iron ingot (`P P/PIP/PPP`). Add food in the main hand, inspect empty-handed,
sneak with both hands empty to withdraw. It holds 16 of one food type; injured
adult residents eat matching food near their Nest for 2 HP per meal, with the
usual 30-second pause. Guavas heal without starting love. Healthy adults can
gather around nearby vanilla bells at noon; visits stop for injury, danger,
leads/rides and night. Place a bell within 12 blocks of home with walking space.

**Care status (C4):** sneak-use Calipers on a bunnidog to see sampling, fleece,
grooming, splicing and gardening readiness in chat, including recovery times and eligibility
requirements. Normal use opens its genetics sheet as before. The readout is a
snapshot and costs nothing. C6 also shows the claimed Nest coordinates/distance,
accepted foods and shared meal recovery, including while healthy. Resident meals
heal injury; there is no hunger meter. Ready means ready to search for reachable
matching food. The Apex instead explains that domestic care is unavailable.

**Show Ring (G3):** craft a Judging Stand with ` B /PGP/PWP`: book, any planks,
gold ingot and any wool. Lead a bred adult within six blocks and use the stand
with an empty main hand or a lead. It needs all seven gene pairs fixed, at least
half health and **every** bred stat +5 (bronze), +20 (silver) or +50 (gold).
Sneak-use with both hands empty to preview the stat tier and a limiting stat.
Awards keep the animal alive and led. Each animal gets its highest tier once;
reloads and other stands cannot repeat it. Use a ribbon in air to read the
winner, or display it on a Trophy Pedestal. Gold earns Champion Line once.

**Grooming (Care C3/C5):** use a vanilla Brush on an adult at least half healthy.
Senses +10 gives one loose whisker, expressed glow gives one glowstone dust,
and expressed starborn with Bloom +20 gives one Starlit Dust. All qualifying
products arrive together for one durability and ten minutes of shared recovery.
Pups refuse. Health, genes and points stay intact; full inventories drop the
products normally. Hidden copies do not yield dust.

**More inherited protection (A2):** moss coats resist Poison, sand coats resist
cactus damage, and void coats resist Levitation. Each requires the expressed
coat, not a hidden copy. They retain other vulnerabilities (including wither,
ordinary damage and shulker impacts). Changing to an immune coat clears its
already-active blocked effect. Crossbreed with woolly/hoofed genes to combine
independent protections; sheets explain the effects.

**Breeding laboratory (T5/T6):** put two Tissue Samples on a Genetics Table and
read its five spreads: A, B, pup forecast, inherited alleles and splice plans.
The allele page includes hidden copies and two-copy odds; hover for whole-litter
success. The Two Copies heading forecasts an all-seven-fixed pup. Splice pages
show exact pairs in both directions without spending anything. Use the table
with a Gene Splicer to jump to that plan. Samples are snapshots of their donors.

**Purebred Registry (G2):** shapeless vanilla book + gold ingot + whisker.
Use on a bred adult with recorded parents and identical copies in all seven
genes, including Diet. It records up to 24 distinct stable genomes, retaining
the first representative's adult stats, ear tag and parents. Air opens its index;
click a line for the full sheet. Registration costs nothing and awards Breeds
True once (25 XP). Hover GENOME on Calipers/sample sheets to find mixed pairs.
Matching registered genomes breed true before mutation; stats still drift.

Player-side guide, one paragraph per feature, in the order they were built. Status words like "works" mean the author compiled and played that chunk; the exact status of every chunk is in `docs/CHUNKS.md`.

Breed them: `/give @s bunnidogs:guava 64`, feed a guava to two adults. They drop ONE seed holding both parents' genes (Phase 4c). Plant the seed by right-clicking the top of farmland (Phase 4d): it is an embryo on an umbilical cord that grows through 8 stages (bone meal speeds it up) and, when ripe (ears up, eye open), breaking it births the litter (size depends on the Fertility gene); a tool with Fortune raises the quality. `/bunnidogs hatch` (holding a seed) skips the waiting and `/bunnidogs seed` reads a seed. Its tooltip shows quality, litter and stat drift (F3+H adds the parents).
Wild lineages (tundra, desert, swamp, cave, mountain, nether, end) spawn in their biomes; `/bunnidogs lineage` shows the one under your feet.
Bred stats and Bloom (bloodlines gain or lose Vitality/Swiftness/Leap/Senses/Bloom each generation, steered by their genes) show in `/bunnidogs genome`.
Bunnidogs eat guavas (`/give @s bunnidogs:guava 64` for now), not carrots. Guavas come in eight tiers (Phase 4e, plus the joke tier of session 7), worth seed quality 0, 1, 2, 4, 7, 12, 20 and 33: craft Golden (8 gold ingots around a guava), Diamond and Netherite the same way, then a Nether Star Guava (4 nether stars in a plus around the netherite guava), a Beacon Guava (8 beacons around the nether star guava) and a Dragon Egg Guava (previous tier + a dragon egg, shapeless). Feed each parent its own guava: the seed's quality is the average of the two tiers' qualities (`/bunnidogs seed` shows it; `/bunnidogs genome` shows the tier a bunnidog in love remembers).
Debug in game: `/bunnidogs genome`; spawn a chosen genome with `/summon bunnidogs:bunnidog ~ ~ ~ {Genome:"33-33-33-33-33-33"}`.


**Guava juice (Phase 4f):** craft a Guava Juice from a guava and a glass bottle (it restores a little hunger). Hold a seed in your main hand and the juice in your off hand and right-click: the seed is dropped into the bottle as a "liquid pup" (one pup rolled from the seed's genes and quality), giving an Infused Guava Juice. Its tooltip lists the effects it will give, which come from the pup's genes (ears, coat, legs, build, temperament, fertility); better seeds make stronger, longer juice.

**What a warren is:** where wild bunnidogs live: guava trees above, Nests in dens below, and mound entrances you can walk into. Level 1 uses U1's den and entrance slopes. Newly planned level 2–5 surface settlements now have a den under each fitted tree, with short lit tunnels connecting them below the tree-to-tree paths (U2). Walk into an entrance mound and follow its steps down; the lanterns and hay sit away from the tunnel openings. Try `/bunnidogs warren 2 surface` through `5 surface` on open, flat ground. Unsuitable ground or a layout with no safe entrance keeps the old holes and surface Nests. Existing generated villages and saved structure starts are unchanged. Fitted houses also have covered cellar ramps beside their doorsteps (U3), leading into the underground network while keeping their front doors and house Nests usable. Surface paths leave these ramps clear. U4 adds fitted pantries under haystacks, glass-lined well cisterns, market cellars/open hatches, farm ramps and small glazed skylights. The level-5 grand tree has a 7x7x4 nursery hall with eight Nests, roots, lanterns and a loop joining satellite dens. Cave, Nether and End floors now use open stair cuts instead of mounds (U6b); omit `surface` for cave/Nether floors. Find natural End villages with `/locate structure bunnidogs:warren_end_village`. Terrain can omit unsafe additions. U2/U3 received broad positive gameplay feedback; the user confirms the new U4/U5/U6b release compiled and worked in game, without itemizing edge cases.

**Warrens and guava trees (Phase 4g):** wild bunnidogs no longer spawn randomly. They live in rare warrens: a small guava tree, a few burrow holes and 2-4 wild bunnidogs of the local lineage (surface biomes, lush and dripstone caves, the Nether and the outer End islands). Guavas hang from the tree; right-click a ripe one to pick it (it grows back). A Guava Sapling (one guava) grows more trees. `/bunnidogs warren` builds a warren at your feet for testing.

**Settlements (Phase 6a):** a warren is now a settlement whose development level is its number of guava trees, 1 to 5 (most are 1; each tree has its own burrows and 2-4 bunnidogs). `/bunnidogs warren [1-5]` builds one at your feet at that level (rolled if you give none) and says how many trees were really built. From two trees on a settlement also has burrow houses (Phase 6b): 2 per tree beyond the first. Paths and the village level come in later chunks (see docs/DESIGN.md, "Phase 6 decisions").

**Dev client options:** `dev/options.txt` is copied to `run/options.txt` the first time the client is run from a fresh checkout (only if that file is missing), so your own settings and key bindings are there from the first launch. To change them: edit `dev/options.txt` and delete `run/options.txt`.

**Calipers (Phase 5a):** craft them from iron ingots (corners) and a copper ingot (middle), or take them from the Tools tab. Right-click a bunnidog and its sheet opens in a window (Phase 8 S6: the same two-page sheet as the Pedigree Book, for that one animal: portrait, genes in words with hover boxes, stats in real units, where its line will drift, parents); hold a seed in the other hand and right-click the air to read the seed (quality, litter, both parents); right-click a planted seed crop to read the seed inside it. These last readouts come into the chat (windows for them are chunk S8). Texture: `python3 art/gen_calipers.py`.

**Recorded parents (Phase 5b):** a bunnidog hatched from a seed remembers the genomes of its mother and father; the Calipers show them (wild and summoned ones have none).

**Pedigree Book (Phase 5c):** craft a vanilla book + a guava. Right-click a bunnidog to log it, right-click the air to OPEN THE BOOK (Phase 8 S3): a contents page, then one two-page sheet per logged animal (its look, ear tag, genome code with a word for every gene, stats in hearts, blocks/s and so on, where its line drifts, its parents); Each sheet shows the animal itself in its portrait box (Phase 8 S4: it sways slowly; a logged pup is drawn at pup size). Hover a gene row to see what the gene does (the matching pair of the code lights up), the code for how to read it, a stat for the arithmetic behind it, the animal for what it drops (Phase 8 S5). Previous / Next (or the Left / Right arrow keys) turn the pages; click an index line, or a parent's line on a sheet, to jump to that animal (Phase 8 S7); Escape closes. Holds 24 animals; animals with identical genes and parents count as one. Texture of the item: `python3 art/gen_pedigree_book.py`; a preview of the pages: see `tools/SheetDump.java` (`art/gen_book_preview.py`).

**Command Block Guava (session 7):** the top of the ladder, quality 33. Craft it shapeless from a Dragon Egg Guava and a command block (so in practice with `/give` or in creative: `/give @s bunnidogs:command_block_guava`). Orange guava with a green glow.

**Ear Tag (Phase 5d):** craft an iron nugget + paper (2 tags), or take one from the Tools tab. Right-click a bunnidog to clip it on: the animal gets a permanent registration code like `BD-3F7A2C`, shown floating over it (unless you already named it) and by the Calipers. One tag per animal; pups are born untagged. Texture: `python3 art/gen_ear_tag.py`.

**Resident finder (E5):** rename a spare Ear Tag at an anvil to a code such as `BD-3F7A2C`. Hold it opposite Calipers and sneak-use the air. Matching loaded animals within 24 blocks receive a ten-second native outline; chat reports count/nearest coordinates. The tag is reusable, searches rest five seconds, and baskets are excluded. Ordinary clipping still gives the animal its own generated code.

**Pedigree Book by UUID (Phase 5e):** the book now recognises each animal exactly (by its UUID): two bunnidogs with identical genes are two entries, and logging the same animal twice changes nothing. If it wears an Ear Tag the tag is shown on its line (`#2  BD-3F7A2C  generation 1  ...`); tagging an animal that is already logged and logging it again just updates its entry. Books from before this update still work.

**Warrens on slopes (session 9):** natural warrens now follow hills and uneven ground (bunnidogs, outer trees and houses used to be dropped on slopes; a house on a slope gets a levelled pad). `/bunnidogs warren [level] surface` builds the natural (surface) kind at your feet to try this on a hill or in a forest; `/bunnidogs warren [level]` is the cave kind as before. Every settlement writes one line to `run/logs/latest.log` (`Warren built at ...` or `Warren gave up at ...` with the reason).

**Footpaths (Phase 6c, session 10):** a settlement with three or more guava trees now has dirt paths: one from the middle tree to each of the others, and a side path from the door of every burrow house to the nearest path. Try `/bunnidogs warren 3 surface` to `5 surface` on open grassy ground (the message counts the path blocks); one or two trees have no paths. The village level (well, market, ...) is the next step.

## Village landmarks (Phase 6e)
Settlements of 2+ guava trees get landmarks: a haystack (2 trees), a well (3), a market stall (4), and at 5 trees (the village) a farm plot and a lamp plaza. Test with `/bunnidogs warren 5 surface` on a big flat area, or find one with `/locate structure bunnidogs:warren_village`.

## Settlement styles (Phase 6f)
A settlement is built of what fits its biome: earth mounds in the meadow, sandstone in the desert, spruce and snow in the tundra, mud bricks in the swamp, stone on mountains and in caves, nether bricks in the Nether, end stone bricks and purpur on the End islands. Walk to the biome and run `/bunnidogs warren 5 surface` to compare.

In the desert, on mountains, in the Nether and on the End islands the houses are flat-roofed towers with windows; elsewhere they are earth mounds (Phase 6g).

## Meeting tree (Phase 6h)
The middle guava tree of a level-5 village is a grand tree: taller, with a wide rounded crown and several fruits you can pick. Try `/bunnidogs warren 5 surface` on a big flat area and compare it with the four ordinary trees around it.

## Guava wood (Phase 7a)
Guava trees are now made of Guava Logs and Guava Leaves (Natural Blocks tab). The leaves decay like vanilla ones: cut the trunk and they (and the guavas hanging from them) disappear after a while; leaves drop sticks and sometimes a sapling, shears keep the block. Regenerate the textures with `python3 art/gen_guava_wood.py`.

**Warren labels (U8):** fresh fitted warrens now place guava signs for dens,
nursery, pantry, cistern, market, farm, lower commons and exits where a safe wall
fits. Green glowing text helps in tunnels; signs remain editable. Old saved
starts are unchanged. Generate `/bunnidogs warren 5 surface` or find a fresh
natural village to explore them. Saved plans, chunk ownership and edit
preservation pass focused checks; natural chunk order/reload remain manual.

**Living tissue samples (Care C1):** craft a Sampling Syringe from iron, glass
bottle and stick diagonally, or `/give @s bunnidogs:syringe`. Use it on a dog at
half health or better to take its exact genes/stats without hurting it. Pups
can be sampled too. It has 32 uses; a dog needs five minutes between samples.
The ordinary sample works with Calipers and the Genetics Table. Recovery is
saved, and refusal costs no durability.

**Gene Splicer (G1):** craft `DMD / CIC /  I ` using diamonds, Mutant Marrow,
copper and iron, or `/give @s bunnidogs:gene_splicer`. Sneak-use to select a
gene. Hold a donor Tissue Sample in the other hand, carry a Mutagen, then use
the tool on a healthy adult. It copies the donor's expressed allele into the
recipient's less-dominant copy, preserving its bred stats and relative health.
Already-carried alleles are refused: breed carriers to make a trait homozygous.
One successful splice costs a sample, Mutagen and one of 16 tool uses; the animal
needs one minute before another splice. Tool selection and recovery are saved.
Server checks pass; client interaction is pending. See `docs/SPLICER_SPECS.md`.

**Care dispensers (C8):** point a dispenser at an eligible bunnidog and pulse
redstone. A Brush collects its qualified products together; a Sampling Syringe
produces its exact tissue sample, including from pups. Hand/machine recovery is
shared; success costs one tool use and products drop for hoppers. Failed care
retains the tool. Native armadillo brushing still works at its original cost.

**Woolly fleece (Care C2/C7):** healthy adults expressing woolly build yield two
wool in their expressed coat's palette with vanilla shears, then regrow it in
ten minutes. Spotted gives one yellow and one brown; glow cyan, moss green,
ember red and void purple. Calipers names the ready colors/counts. Changing
genes cannot bypass recovery. Pups and ordinary
builds cannot be sheared. Health, genes and the approved model stay intact.
Recovery persists across saves; both sides need the current version 6 protocol.

**Inherited adaptations (A1):** ember coats resist fire and lava; snow coats
and woolly builds cannot freeze in powder snow; hoofed legs halve fall damage.
These follow expressed genes, so benefits can be crossbred and hidden alleles
do not grant them. Hover the gene to read its effect. Server behavior checks
pass; client gameplay review remains pending. See `docs/CARE_SPECS.md`.

## Drops (D1-D8 done)
Bunnidogs will drop items by genome and stats: a fast line drops rabbit's feet (several if very fast), a high-Vitality line drops bunnidog flesh, and so on, with new modded items (flesh, sinew, whiskers, coat pelts, mutant marrow, tissue samples, ...). The why is in `docs/DROPS_PLAN.md`, and the exact formulas, tables and every decision are in `docs/DROPS_SPECS.md` (all questions answered).

**Chunk D1 (works):** a dying bunnidog drops Raw Bunnidog Flesh (more with Vitality and a chonky build; cooked if it dies burning), Rabbit's Foot (more with Swiftness and long legs) and Bone Meal (more with Bloom). Cook the flesh in a furnace, smoker or campfire. The Calipers show the odds in a new `Drops` line, `/bunnidogs drops` prints them for the nearest bunnidogs, and `/bunnidogs drops simulate 500` rolls that many kills for each and prints the average of every item (try it in creative). Looting helps, pups give a quarter, the `mobDrops` game rule is honoured. The art is regenerated with `python3 art/gen_flesh.py`.

**Chunk D2 (works):** an adult bunnidog also always drops one **pelt** of the coat it shows (12 coats, 12 pelts; a Looting level adds a 15% chance of a second; pups drop none). Any two pelts craft into one leather. The Calipers and `/bunnidogs drops` show `pelt: <coat>` first. The pelt art and tag are regenerated with `python3 art/gen_pelts.py` (it reads the coat table of `art/gen_bunnidog.py`).

**Chunk D3 (works):** an adult bunnidog killed by a player can also drop a vanilla item that its coat, ears, legs, build or temperament stands for (a snow coat: snowballs and packed ice; sand: gold nuggets; slate: flint and raw iron; fleet legs: sugar; woolly: wool; ...). The chance grows with how heavily the animal is bred and with Looting, and the Calipers show it as a percentage (`gold nugget 13%`). Pups, mob kills and merely carried alleles give nothing. Nothing new to craft or place: these are all vanilla items. Sinew, whiskers and fangs, the haunch, marrow, tissue samples and legends follow in D4-D8.

**Chunk D4 (works):** three new materials. **Bunnidog Sinew** drops from animals bred for Leap, **Bunnidog Whisker** from animals bred for Senses (an unbred animal gives none, and the Calipers/`/bunnidogs drops` list them only when the animal can drop them), and a **Bunnidog Fang** can drop (20% for a plain one, more with Vitality) from a feral adult killed by a player. They are in the creative Ingredients tab and each has a use: 1 sinew crafts 2 string; whisker, copper ingot and stick in a column craft a Brush; fang, stick and feather in a column craft 4 arrows. The art is regenerated with `python3 art/gen_materials.py`. The haunch follows in D5.

**Chunk D5 (works):** an animal bred for Vitality past +20 can now drop a **Prime Bunnidog Haunch** (about 1 at Vitality +60, up to 4; a quarter from a pup, more with Looting), and one that dies burning drops a **Roast Bunnidog Haunch** instead. Cook a prime haunch in a furnace, smoker or campfire to get the roast, the best food of the mod (10 hunger). Both are in the creative Food & Drinks tab. The Calipers and `/bunnidogs drops` list the haunch after the flesh when the animal can drop it. To try it, give an animal Vitality with `/summon bunnidogs:bunnidog ~ ~ ~ {Stats:"60,0,0,0,0"}` (Vitality, Swiftness, Leap, Senses, Bloom). The art is regenerated with `python3 art/gen_haunch.py`.

**Chunk D6a (works): the Mutagen.** A green-violet vial in the creative Ingredients tab (`/give @s bunnidogs:mutagen`; the recipe with Mutant Marrow is in D6b below). Hold a seed in your main hand and the Mutagen in your off hand and right-click the air: the seed is now **treated** (its tooltip says `Treated with Mutagen`), and every pup it hatches, or any liquid pup you infuse from it, mutates with a 50% chance instead of the usual few percent. One Mutagen per seed; a treated seed plants and hatches like any other, and old seeds are unaffected. The art is regenerated with `python3 art/gen_mutagen.py`.

**Chunk D6b (works): Mutant Marrow.** An adult bunnidog killed by a player rolls once for every rare (mutation) allele it shows (huge ears, spotted coat, springy legs, chonker build, stoic temperament, swarm fertility), 25% each for one **Mutant Marrow** (Looting raises the chance); a full six-gene mutant gives about 1.5 marrow per kill. The Calipers and `/bunnidogs drops` list it last (`mutant marrow 0.25 (25% for one)`); pups, mob kills and ordinary or wild-lineage animals give none. Craft **1 Mutant Marrow + 1 Fermented Spider Eye + 1 Glass Bottle** (shapeless) into a Mutagen. The marrow is also in the creative Ingredients tab. The art is regenerated with `python3 art/gen_marrow.py`.

**Chunk D7a (works): the Tissue Sample.** Every kill by a player rolls once for a **Tissue Sample**, a vial holding the genes and bred stats of the animal that died: **certain if you hold the Calipers in your OFF hand when you kill**, about 1 in 25 otherwise (pups count too). Hold the sample in one hand and the Calipers in the other and right-click the air: the Calipers print its genome gene by gene, its stats and its drift. The Calipers tooltip explains the off-hand rule, and their `Drops` line now ends with `tissue sample 4%`. There is one plain sample in the creative Ingredients tab, and `/give @s bunnidogs:tissue_sample[bunnidogs:tissue_data="33-33-33-33-33-33/0,0,0,0,0"]` writes one by hand. The art is regenerated with `python3 art/gen_tissue.py`. 

**Chunk D7b (compiles): Starlit Dust.** An adult bunnidog of the **starborn** line (End islands) killed by a player can now drop 1-2 **Starlit Dust** (25% for an unbred one, more with Bloom; the Calipers show it as `starlit dust 25%`). Right-click a **planted seed crop that is not ripe yet** with the dust: it ripens at once (ears up, eye open) and one dust is used up (not in creative); a ripe crop or any other block ignores it. It is in the creative Ingredients tab, and the art is regenerated with `python3 art/gen_dust.py`. This was the last "item that waits" of the drop tables: every row is live now. Next in the drops plan: D8 (Trophy Pedestal and legends).

**Pedigree Book and readouts (Phase 8 S2).** The Calipers, the Pedigree Book list and the Tissue Sample now describe an animal in plain words (for example "spotted, short ears, loyal", and its stats as hearts, blocks per second and so on); the raw genome code is always the last line. The Pedigree Book also remembers each animal's stats and whether it was a pup when you logged it: right-click the animal with the book again to bring its entry up to date.

### Seed and Tissue Sample windows (Phase 8 S8)
Hold the Calipers in one hand and a seed (or a Tissue Sample) in the other and right-click the air, or right-click a planted seed crop with the Calipers: a window opens instead of chat text. The seed window shows the seed (quality, litter, mutation chance, where the pups' stats will drift; hover each for the numbers) and, on Next or a click on Mother / Father, each parent's full sheet. The sample window is one sheet of the animal the sample was taken from.

**Page-turn sound (Phase 8 S7b, session 29):** turning a page of the Pedigree Book or the seed window (Previous / Next, Left / Right arrows, a click on an index line or a parent's line) plays the vanilla book sound; it is silent when no page really turns.

**Trophy Pedestal (drops D8a, works):** craft a gold ingot (top middle) over a stone brick column over a row of stone bricks, or `/give @s bunnidogs:trophy_pedestal`. It is a plain stone-and-gold display stand for now; in the next chunks it will hold one item (a pelt, a Tissue Sample, later the legends) and show it on top.

**Pedestal holds an item (drops D8b, works):** right-click a Trophy Pedestal with an item and one of it stands on the pedestal; right-click again to take it back; mining the pedestal drops it.

**Pedestal shows its item (drops D8c, works):** the item on a Trophy Pedestal floats above its plate and turns slowly (everyone nearby sees it; it is still there after a reload). The chat lines of the previous chunk are gone. Next: the legends (D8d).

**The five legends (drops D8d, works):** an adult bunnidog killed by a player can now drop a **legend** for every stat bred to 100 points or more: a **Titan Haunch** (Vitality), **Gilded Foot** (Swiftness), **Moon Sinew** (Leap), **Listening Shell** (Senses) or **Everbloom** (Bloom). The chance is 2% at 100 points, 4% at 200, 8% at 400 and 15% from 750; Looting does not help, pups and mob kills give nothing, and each stat rolls on its own. The Calipers line and `/bunnidogs drops` list them (`titan haunch 8%`) once a stat reaches 100. They are plain epic-rarity items (also in the creative Ingredients tab, `/give @s bunnidogs:everbloom`) and can stand on a Trophy Pedestal or fund the Apex ritual described above. To try it without breeding for generations: `/summon bunnidogs:bunnidog ~ ~ ~ {Stats:"400,0,0,0,0"}`, then `/bunnidogs drops simulate 1000`. The art is regenerated with `python3 art/gen_legends.py`. Next: the worn ear tag (D8e).

**Worn Ear Tag (drops D8e, works):** clip an Ear Tag on a bunnidog and, when it dies, it leaves a **Worn Ear Tag** behind: a weathered tag whose tooltip shows the `BD-XXXXXX` code the animal wore. It always drops (pup or adult, any cause, no luck involved); an untagged animal leaves nothing. It cannot be clipped onto another animal; stand it on a Trophy Pedestal as a keepsake. One sample is in the creative Tools tab, and `/give @s bunnidogs:worn_ear_tag[bunnidogs:worn_tag_code="BD-3F7A2C"]` writes one by hand. The art is regenerated with `python3 art/gen_worn_ear_tag.py`.

**Genetics Table (T1-T3 work):** craft it from 2 whiskers, a copper ingot and 5 planks (`WCW / PPP / P P`), or `/give @s bunnidogs:genetics_table`. It holds two Tissue Samples, slot A and slot B, like a small stack: right-click with a sample to put it on (A first, then B; with both full a new sample swaps with B); right-click with an empty hand (or anything else) to open the table window (sample A's sheet, then sample B's with Next / the arrow keys); sneak with both hands empty to take the last one back (B, then A). **With both samples on, a third spread is the FORECAST of their litter** (T3): how many pups to expect, the mutation chance, the seed quality on plain guavas, the points each stat will have, and the odds of each look per gene (hover a row for the working). T4 now displays occupied A/B sample vials at the front of the bench and plays a quiet click for successful interactions; the user confirms the release compiled and worked in game (individual appearance/sound checks were not itemized). See `docs/TABLE_SPECS.md`. Textures: `python3 art/gen_table.py`.

**Nest (H1, H2 and H3 work; H4, session 44, not compiled yet):** craft it from 3 wheat over 3 guava planks, or `/give @s bunnidogs:nest`. Place it near a bunnidog: within about 7 seconds the nearest adult without a home claims it (one dog per nest, and only if it can walk there), then walks back to it whenever it ends up more than 16 blocks away. Break the nest and the dog forgets it and looks for another within about 10 seconds; a dead dog frees its nest. Pups look for one once they are adults. Check with `/data get entity @e[type=bunnidogs:bunnidog,limit=1,sort=nearest]` and look for `NestX`, `NestY`, `NestZ`. **Warrens now come with nests (H3):** each burrow hole has a Nest beside it, so wild dogs claim one by themselves a few seconds after the warren appears (`/bunnidogs warren` to test, then the `/data get` above). **At night (H4):** from dusk to dawn an adult with a nest walks home and waits within 2 blocks of it; at dawn it strolls again. Rain does not matter, and the Nether and End have no night. Try `/time set 13000`, then `/time set 1000`. **Houses have nests (H3b, session 44, not compiled yet):** every burrow house of a NEW village has a Nest in the floor of its hollow (look through the door), so a dog near the village can claim a house as its home and, at night, wait in it. Try `/bunnidogs warren 4`, then watch the dogs or use the `/data get` above. Older villages are unchanged. The plan is `docs/HOME_SPECS.md`.

**Villages of guava wood (V1a, works):** in NEW villages, the houses now show the village's level. Level 2: the mound house gets a guava-log frame round its doorway. Level 3 and up: a guava cottage (earth or stone below, guava planks above, log corners, a guava slab roof); in desert, mountain, Nether and End villages the tower gets log corner posts. Test with `/bunnidogs warren 2`, `/bunnidogs warren 3` and so on (the level is the number of trees). Old villages and level 1 are unchanged. New houses now get initially open guava doors (V1c), preserving access to their Nests.

**Villages of guava wood, part 2 (V1b, works):** in NEW villages: level 4 houses stand on a course of stone (cobblestone in the meadow, stone bricks in the tundra, sandstone in the desert, mud bricks in the swamp, nether bricks in the Nether...), level 5 mound houses get a loft with three windows under a taller slab roof, and the well, the stall and the lamp posts are guava wood in every biome (guava fence posts, guava slab roofs). Test with `/bunnidogs warren 4` and `/bunnidogs warren 5` (the level is the number of trees).

## Lightweight model

All bunnidogs use the same lightweight model (the previous Low preset). There
is no graphics menu or distance switching. Coat, ear, leg and build genes still
change their appearance. See `docs/GRAPHICS.md` for rendering details.


**Guava mechanisms (W3):** guava planks now craft doors, trapdoors, buttons,
pressure plates and fence gates using the usual wooden recipes. Find them in
the Building Blocks creative tab. They use oak's normal hand, redstone,
projectile and entity activation rules with custom guava textures. Doors drop
one item when broken. Build/resource checks pass; the user confirms the release worked in game.
Individual interaction and appearance checks were not itemized. Art: `python3 art/gen_wood_interactives.py`.

**Guava signs (W4a):** craft three signs from six guava planks above a stick, or
six hanging signs from six stripped guava logs beneath two iron chains. Find
both in Functional Blocks, or use `/give @s bunnidogs:guava_sign` and
`/give @s bunnidogs:guava_hanging_sign`. They have custom guava wood, native
standing/wall and hanging attachments, waterlogging and two-sided text editing.
Vanilla handles dyes, glow ink, ink sacs and honeycomb waxing. Breaking a wall
form returns its corresponding sign item. Recipes unlock from the matching
wood; stacks hold 16. Build/resource checks pass and the texture preview was
inspected; in-game interactions and text persistence need gameplay review.
Regenerate assets with `python3 art/gen_wood_signs.py`. Server GameTests now
confirm native sign validity/tickers and both text faces, colors, glow and wax
through save/load; actual client editing and placement still need review.

**Guava boats (W4b):** craft a two-seat boat with five guava planks (`P P / PPP`),
then combine it with a chest for a one-seat boat with 27 inventory slots. Both
are in Tools & Utilities; commands are `/give @s bunnidogs:guava_boat` and
`/give @s bunnidogs:guava_chest_boat`. Native vehicle rules handle paddles,
passengers, chest access and drops. The hull/paddles use guava wood; the chest
retains its native appearance. Server tests cover seats, names, inventory and
genetic item data across save/load, and exact destruction drops. Texture preview
inspected; client rowing/menu use remains pending. Regenerate with
`python3 art/gen_wood_boats.py`.

**Moving a bunnidog (H5):** native leads and boat rides suspend home/food
navigation and Nest searches while preserving its home. Let it off the lead or
boat and normal behavior resumes. To move its home, lead it to a free Nest and
right-click the Nest with an empty hand or a lead. This moves the nearest led
adult within eight blocks, provided it can reach the Nest. One click moves one
resident; an occupied/unreachable Nest leaves the old home intact. The lead
stays attached, and a quiet click/action-bar message confirms success. Pups
cannot claim a Nest. The old Nest becomes available only after a successful
move. Server interaction and ticket tests pass; manual multiplayer use remains
pending.

**House details (V1c):** new level 2–5 houses have guava doors, initially open.
Level 4 cottages gain glass windows; levels 4–5 have shutters, and level 5 has a
hanging lantern above the doorstep. A player can close a door; dogs have no new
door-opening ability. Terrain can omit blocked shutters/lamps. Existing villages
and old saved house starts retain their previous appearance. Build/layout
checks pass; the user confirms the release worked in game. Both styles remain
useful regression checks, without separate reported results.


**Inherited diets (Diet D1–D4):** the Calipers and Genetics Table now show a
seventh gene, Diet. Hover it for accepted foods. Guavas still breed every dog;
other foods that fit its expressed diet heal 2 HP or grow an unlocked pup,
without starting love. A healthy adult refuses them. Plain dogs stay guava-only;
grazers accept hay/wheat/grass, foragers accept produce/berries, hunters accept
raw meat, and the rare omnivore combines those three. Wild lineages also have
fish, desert plants, mushrooms, glow berries, mountain greenery, Nether fungi
or chorus diets. Old animals/items gain plain Diet and retain their old genes.
Both server and client need the current matching update (network version 6).

**Food near home:** injured adults with a claimed Nest can seek meals by day,
within 16 blocks of the Nest. Grazers nibble hay, grass or ripe wheat; foragers
nibble ripe carrots, potatoes and beetroot. Hay remains, crops restart at age 0,
and no harvest items drop. Crop/grass changes obey mob griefing and block
protection. Hunters/omnivores take adult chickens/rabbits by default, gaining
4 HP for their own prey kill. Babies, players, bunnidogs, tame pets and allies
are protected. A meal gives a 30-second pause; night and higher-priority needs
interrupt foraging. There is no starvation meter. Try a grazer with
`/summon bunnidogs:bunnidog ~ ~ ~ {Genome:"00-00-00-00-00-00-11"}` or a hunter
with final pair `33`, place a nearby Nest, let it claim it, then test after
injuring the adult. Food and prey tags can be extended by data packs.

**Deeper villages (U7):** fresh level-5 villages can have another room below the
grand-tree hall. Follow its turning stairs to four more Nests, a lantern and a
supply barrel. New pantry/market/lower-room barrels have small stores of food
and plain guavas. Existing containers are not refilled; terrain can omit the
lower floor. Test with `/bunnidogs warren 5 surface` on deep flat ground.
These additions have build/headless checks and still need gameplay review.


**Resident home markers (H8):** normal compass use on a resident marks its
recorded Nest, including an unloaded remembered home without loading terrain.
Sneaking retains mineral scouting. Missing loaded homes refuse; whole compass
components and all home tickets remain intact.

**Bell roll call (L18):** sneak-use Calipers on a bell for loaded adults whose
Nests lie within twelve blocks and whose bodies lie within thirty-two. Chat
lists up to eight, injured first, with tags, health, foods and home coordinates,
plus total/remaining counts. Reading never rings or redirects residents.


**Lab automation (T7/T8):** hoppers fill a Genetics Table's single-item A/B
slots in order and extract A first. A full table refuses swaps; power the
hopper to keep samples for reading. Comparators read 0/8/15 occupied slots.
Whole components/native saves and aborted transfers preserve samples. Manual
sample use now works from the actual hand, including the off hand; ordinary
B-first manual take and forecasts stay the same.


**Recovery rest (L19):** adults below half health rest at a safe loaded spot
within two blocks of their claimed Nest during daytime idle periods. Meals,
danger, breeding, player control and storm shelter take priority. Rest heals
nothing; keep real suitable food nearby. Unsafe paths/places refuse, and
healing restores normal idling. Sneak-Calipers reports readiness without
searching for a path.


**Litter planning (T9/T10):** read a Genetics Table holding one guava to preview
that tier for both parents, or two to average their qualities. Hold Mutagen
to preview treatment; one guava opposite the vial combines them. The real
seed rules include starborn and expected stat drift. A held Fortune tool
adds the real future crop-harvest bonus (highest held level); Cradles get none.
No cue or sample is spent. Clear hands for plain, untreated forecasts. Choices travel
with the server snapshot; updated clients and server use network 7.


**Cactus gardens (L20):** healthy desert browsers trim one spare top from a
two-/three-high cactus into a hoe-enabled trough. They keep lower segments
and flowers, use safe loaded routes and share the existing one-minute work
rest. Diet, exact filters, power, capacity, griefing and protection apply.
One base never gets harvested and growth is not accelerated.

**Regional farms (L21):** new settlements can fit desert cactus on sand,
Nether wart on soul sand, swamp mushrooms on mycelium, cave glow berries under
a guava-plank trellis and End chorus on end stone. Other regions retain the
ordinary plot. A safely fitted corner may hold an empty enabled trough. Keep
food stocked for meals; plants grow natively. Existing settlements keep their
saved layout, and generation preserves containers and obstructions.

**Guava orchards (O1–O3):** use a plain guava on the underside of any leaves
with air below to establish a green hanging fruit. Survival spends one from the
actual hand; creative keeps it. Enhanced guavas never plant. Bone meal advances
one age, by hand or dispenser; two applications ripen a new fruit. Picking keeps
the plant to regrow. Shears and Silk Touch each recover one guava leaf block
without sapling/stick extras. Native protection/Adventure restrictions apply.

**Mushroom beds (L22):** healthy mushroom eaters gather a spare red or brown
mushroom beside an enabled trough only while another living mushroom of that
same color remains within three blocks in their loaded home area. One item
enters stock with the shared one-minute work rest. Native spread replenishes
the bed; the final seed of each color stays. Filters, power, safe routes,
capacity and native griefing/protection rules apply.

**Fruit meals (Diet D6):** injured adult residents eat ripe hanging guavas near
their Nest by day; matching Diets also gather sweet/glow berries, including
native vine heads/bodies. One meal heals 2 HP and shares the normal thirty-second
food rest. The fruit/bush/vine stays to regrow, with no extra loose drop or love
mode. Outdoor meals use complete safe loaded paths and native protection;
stale or callback-replaced plants refuse without overwriting blocks or healing.
Healthy adults leave crops for gardening. Night meals still require troughs.

**Sapling stations (O4/O5):** dispensers plant a Guava Sapling into dry air over
valid vegetation soil/farmland, spending one only on success. Occupied/invalid
targets retain stock without ejection. Grass, podzol and mycelium work under
26.2's native soil rules. Lost support recovers one sapling on its next tick;
unsupported/stale/unloaded growth refuses. Give the whole tree loaded space.

**Underground farms (L23):** new cave/Nether feature warrens with five fitted
trees may place their reserved glow-berry trellis or soul-sand wart bed. Native
cave terrain fitting and optional empty enabled troughs apply; unsafe plots
are omitted. Existing settlements retain their generated layout.

**Safe seedbeds (N8):** planting still requires farmland. If soil later becomes
dirt, grass, podzol or mycelium, the same litter crop keeps its whole named seed
and continues growth. Support loss recovers the original seed once; soil
changes do not hatch pups, reroll parents or reset crop progress.

**Cold-climate care (C10):** brush a healthy adult snow coat at Bloom +10 in
a climate cold enough for native snow to gather one Snowball. Hidden snow and
warm climates give none. Qualified whiskers/Starlit Dust combine under one
tool wear and ten-minute grooming rest. Native dispenser brushes work too;
ordinary woolly shearing uses its separate fleece recovery.

**Loose food (D7/L24):** injured adult residents eat ordinary loose matching
food after ten seconds, healing two HP with the normal thirty-second meal rest.
Healthy Senses +10 adults collect it beside hoe-enabled troughs instead,
sharing one-minute garden rest. Diet, pickup delays, owner reservations, exact
filters, power and safe loaded paths apply. Upgraded guavas stay. Transfers
preserve whole components and conserve food through native transaction aborts
and trough-removal notifications. Calipers explains the gathering bonus.

**Melon beds (L25/L26):** foragers and omnivores can eat melon slices for healing
or pup growth; slices never trigger breeding. Healthy residents harvest one
slice into a nearby hoe-enabled trough only if a living attached melon stem
points to the fruit. Decorative melon blocks stay. The stem resets naturally
and grows its next fruit normally. Bloom +10 advances young melon stems by
one age, creating no food. Mature stems await native growth. Exact filters,
power, safe routes, protection and the shared one-minute gardening rest apply.

**Orchard stations (O6/O7):** dispenser shears pick a ripe hanging guava into
one ordinary drop, resetting fruit to green for one tool use. Unripe fruit
keeps the shears; ordinary animals and beehives keep their previous shearing.
A dispenser with plain guavas can repair dry empty canopy gaps beneath leaves
for one item. Other targets keep ordinary food ejection, so use a single pulse
for repair; upgraded guavas never plant fruit. Native bone meal grows it and
hoppers collect ripe harvests.

**Habitat meals (D8):** injured adults near home can eat their Diet's spare
cactus top, extra same-color mushroom, ripe Nether wart or stem-linked melon.
A last cactus base/mushroom, attached flower, unripe wart and decorative melon
stay intact. No trough is needed. These meals heal two HP and share thirty-second
food rest, safe routes and native protection. Growing stock is conserved under
the same rules as healthy trough work.

**Nest signals (H9):** a comparator reads 15 for a claimed Nest and zero for a
free Nest. It follows the saved home claim rather than where the resident is
standing. Rehoming switches both signals; Travel Basket packing and death free
the old one. An unloaded resident retains its claim.

**Meadow supplies (L27):** unopened meadow den/market food stores can provide
melon slices in their ordinary local-food rolls and have an independent 35%
chance of one or two melon seeds. Other lineages keep their foods; emptied or
player-filled containers are never refilled.

**Select resident jobs (L28–L30/N9):** sneak-use a hoe on a trough to cycle and
enable Mixed, Garden, Gather, Compost, Nursery or Supply work. Normal hoe use toggles
work while retaining its mode. Old troughs keep Mixed. Food/filter/automation
still work normally. Garden mode lets Bloom +10 adults spend one self-planting
food to restore a valid gap; Nursery lets Bloom +20 adults spend one ordinary
guava for exactly one occupied Cradle stage. Both leave eight food in reserve,
share one-minute work rest and refund interrupted jobs. Nurses preserve the
whole litter and never hatch it. See `RESIDENT_WORK.md` for supported plants.

**Meadow melon farms (L31):** fresh five-tree meadow settlements now mix young
melon stems with wheat/roots, native water and empty dirt fruit spaces. A
clear corner can fit an empty enabled trough. Plants grow and fruit normally;
old saved settlements retain their existing crop layouts. Garden-mode workers
can later restore valid gaps from surplus food.

**Safe food hunts (D9):** wounded hunters and fishers follow complete dry home
routes, including safe shore positions. New terrain hazards and changed homes
interrupt pursuit. Named, leashed, riding, tamed, baby and allied animals are
protected. One eligible chosen prey kill heals four HP with normal meal rest.
Other resident routes also notice new native hazards under cached path nodes.

**Work roll call (L32):** sneak-use Calipers on a trough for its mode, stock,
filter and up to eight nearby resident rows. It explains health/home/recovery,
traits, power, reserve and capacity limits. Counts include loaded residents
within eight blocks, with eligible workers first. Ready means eligible to
search; provide the correct target and a clear route. Reading changes nothing.

**End gardens (D10/L33):** Bloom +20 adults with a chorus-fruit Diet revive spent
chorus flowers for one fruit, leaving branches and living flowers intact.
Healthy adults bank it at enabled Mixed/Garden troughs; injured adults eat it
directly for 2 HP. Existing power/protection, safe home routes and work/food
recovery apply. Calipers names this bonus for compatible lines.

**Supply workers (L34):** select Supply on a trough with more than eight ordinary
food. Healthy Senses +20 adults move one food to a compatible non-Supply dish
within four blocks, stopping at eight there. The destination can keep gardening
off. Both dishes need no power, dry home routes and exact filter compatibility.
Each transfer shares one-minute garden rest; upgraded guavas stay for breeding.

**Keeper controls (L35/N10):** sneak-use a hoe on a resident to pause/resume its
chores, including gathering and supply. Food, breeding and home routines
continue; the saved preference travels in baskets. Sneak-hoe a Cradle to make
hatching manual only. Its incubation, nursery tending and stock automation
continue; ripe empty-hand hatching stays available. Re-enable redstone and send
a fresh pulse to restore automatic hatching. Tools spend no durability.

**Guava hand care (D11):** sneak-feed a plain guava to heal 2 HP and help pups
grow without breeding. Healthy adults and upgraded guavas keep food. Existing
love quality/time and care/work/food rests are preserved. Normal feeding still
starts breeding. The plain guava tooltip explains the gesture.

**Snow routes (A6):** expressed snow coats can use supported powder-snow
surfaces for home routines, meals and work. Woolly or hidden snow alone gives
no footing. Powder interiors, missing floors and neighboring hazards stay
unsafe; losing the trait interrupts existing snow routes. Native equipment
and data-pack footing also apply.
