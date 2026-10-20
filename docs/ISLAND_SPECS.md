# I1/I2 — isolated ocean lineage and refuge

Implemented 2026-10-20 from the original disconnected-island proposal. The
refuge makes exploration supply new breeding material rather than giving every
ocean animal coastal genes. Pure genetics and terrain/layout rules remain
separate from native world generation. Protocol **14** requires updated clients
and servers; existing genome, seed, sample and pedigree formats remain valid.

## I1: inherited coastal traits

`ISLAND` is appended after the existing lineages. Three alleles are appended to
their saved tables; no prior IDs move and the seven-pair, four-bit format stays.

| Gene | Saved ID / dominance | Expressed effect |
|---|---|---|
| Coat `seal` | 12 / 17 | Gray-blue patched fur; +1 native Oxygen Bonus |
| Legs `paddle` | 8 / 14 | Shorter legs and visible toe membranes; +1 native Swim Speed |
| Diet `shore_forager` | 12 / 17 | Kelp, dried kelp and seagrass, plus ordinary guavas |

Seal coats normally double air duration on average through Minecraft's native
air-consumption probability. Maximum air stays 300. The trait neither refills
air on expression nor grants water breathing: prolonged submersion still hurts.
Water Breathing and external oxygen modifiers retain their normal behavior.
The named transient genetic attribute is replaced on gene changes/load without
overwriting other modifiers or a changed base attribute.

Paddle legs use scale 0.85: before bred stats, land speed is 0.91× and jump
power 0.82× ordinary. Their +1 Swim Speed normally doubles water propulsion.
Swamp `webbed` remains its own allele with its existing +0.5 bonus and smaller
land tradeoff. Paddles reuse the approved expressed webbing/paddling variant;
they add no base anatomy or render pass. Adults and pups keep the established
body/foot placement. Hidden copies grant no oxygen, propulsion or visible look.
Coat, legs and diet inherit independently, including into other lines.

Island founders start with ordinary common alleles, then have independent
per-copy chances of seal 90%, paddle 85%, shore-forager 85% and loyal 60%.
They receive no rare mutation alleles, bred-stat boost or fabricated parents.
The new wild alleles have zero common weight and are never mutation targets.
The reserved `lineage_island` biome tag is empty by default; datapacks may opt
custom biomes in. Ordinary ocean spawning does not become an Island source.

Existing material systems recognize the new coat: Seal Pelt, gray wool on
woolly builds, a matching rug and Specimen Jar, and stone-ground camouflage.
Juice reads seal as Water Breathing and paddle as Dolphin's Grace, both with
120-second base/600-second cap, using the existing quality/stat infusion rules.
Calipers, gene sheets, samples and inheritance tools read the appended alleles.

## I2: a fitted island in real ocean terrain

`bunnidogs:island_refuge` constructs a small rooted island in mild oceans. Its
centre tag includes ocean, lukewarm ocean, deep lukewarm ocean and warm ocean.
At sea level 63, 29 footprint samples within radius 24 require open ocean water
and depths 4–32. Two additional rings at 64 and 96 blocks each sample sixteen
ocean/open-water columns, rejecting sampled mainland or neighbouring land.
These are sampled fitting constraints, not a claim that every intervening cell
or a continuous 96-block ring was inspected.

The independent native random-spread set has spacing 32, separation 12 and salt
2057301947. Its native exclusion zone rejects ocean-monument candidates within
five chunks. Fitting reads native biome/noise columns without loading distant
chunks. Terrain adaptation is `none`; the authored shoal supplies its foundation.
Deep/cold/frozen ocean centres are excluded, and unsuitable candidates stay empty.

The version-one piece spans at most 49×49 horizontally, from its sampled bed
to sea+16. A stone/soil core reaches the sampled seabed beneath its centre,
with a sloping sand shore. The surface includes:

- A sandstone/log shelter with three real floor Nests, an empty trough, bell,
  hanging lantern and deferred supply chest.
- Three persistent adult Island founders, one at each Nest. They can claim
  the homes through ordinary housing behavior; there is no forced ownership.
- A normal decaying guava tree with native leaf distances and young fruit.
- A contained two-deep tide garden with kelp, seagrass, a dry rim and a second
  empty trough. The shore trail includes sandstone stairs for shorter legs.

Construction intentionally displaces natural ocean water. Before writing a
column, it checks the planned vertical envelope for block entities or existing
non-natural construction. Those columns stay untouched, including waterlogged
construction and lava. Tagged ores/ancient debris are recognized as natural but
their actual cells are retained. All placement stays in the supplied chunk box;
no domestic job or founder placement requests distant chunks.

Each piece saves a completed-chunk bit mask and attempted-founder mask. Atomic
in-progress reservations keep concurrent neighbour generation from repeating
work; only finished chunks are recorded complete. Replaying a completed chunk
does not restore terrain, refill opened supplies or replace removed founders.
Founder attempts are reserved before native spawn callbacks and use stable
per-piece UUIDs. A blocked/refused attempt is not a renewable spawn source.
The saved layout version is 1; future layout revisions must preserve old starts.

The chest guarantees a guide and lead, plus modest regional food, tags, bone
meal or sea-pickle supplies. It gives no samples, legendary ingredients or
earned trophies. Expert cartographers may offer a native Refuge Explorer Map
for 20 emeralds + compass, six uses and 15 villager XP before native discounts.
The failed-map filter and existing map offers remain. Discovering a generated
refuge awards **A World Apart**. Use `/locate structure bunnidogs:island_refuge`
or explore new chunks; old generated terrain is not rewritten.

## Shore care: D13 / L36

Shore foragers use ordinary meals/troughs and sustainable shallow kelp care.
An injured, calm housed adult can eat a spare upper kelp block by day for the
normal two HP and shared 600-tick meal recovery. A valid top must have a living
kelp-plant stem immediately below. Harvesting returns a water source and native
updates promote the remaining stem to a growing head; the final root stays.
There is no extra dropped kelp from a direct meal.

Healthy opted-in workers harvest the same tops into a nearby enabled trough.
Mixed or Garden mode permits harvesting; Garden additionally permits paid
restoration at Bloom +10. Workers spend one actual surplus kelp on a bare valid
bed beneath two blocks of still water, keeping eight food in stock. Native
survival rules reject invalid floors, shallow/flowing water and existing stems.
This does not turn stocked kelp into free growth on a living stalk.

Both routines approach from loaded dry banks within reach. They reuse safe
home routes, bounded searches, current terrain/home/health/diet checks,
mob-griefing and native destruction/placement protection. Work retains filters,
power, selected mode, per-resident pause, 60 work ticks and shared 1,200-tick
garden recovery. Cancelled paid placement returns the exact stock without
starting work recovery. Paddle feet never authorize an underwater domestic route.

## Evidence

Java 25 build, the model/assets gate and all **95** native cases pass at the
combined implementation checkpoint. Focused pure rules check saved IDs,
isolation/inheritance, trait tradeoffs, ocean fitting and the authored layout.
Native cases cover clipped chunk order, piece saves/replay, actual founder
housing, supplies, protected columns, real shore-to-shelter paddle navigation,
native air/drowning/propulsion, and paid/protected kelp meals and work. A fixed
default-Overworld seed-zero candidate at chunk [-88, -16] also fits real native
biomes/noise and placement exclusion without generating those remote chunks.

`exportIslandLayout` writes preview data directly from `IslandShape`; it is not
a second game layout. Actual-layout and approved-sculpt/atlas previews are
inspected separately from native rendering. Natural-world frequency, complete
cartographer journeys, GPU appearance and ordinary long-running resident life
remain gameplay review.
