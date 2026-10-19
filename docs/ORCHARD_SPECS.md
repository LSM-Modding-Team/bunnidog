# Guava orchards O1–O7

Implemented 2026-10-18 under standing design latitude. Natural trees already
provide repeatable hanging fruit, but a lost fruit could only be replaced by
growing another tree. Player propagation makes established orchards repairable
and allows intentional canopy layouts without new blocks, items or save fields.

## O1: establish hanging fruit

Use a **plain guava** on the underside of leaves with dry air below. One new
age-zero Guava Fruit is placed and survival consumes one guava from the actual
hand; creative keeps it. Enhanced tiers retain their breeding/food role and
never plant. Any `LeavesBlock` is valid, matching the existing fruit survival
rule. Other faces, solid supports, occupied cells, fluids, build-height/world
border limits and unloaded cells refuse. No replacement of existing fruit.

Player use follows the native ItemStack/GameMode placement path, including
Adventure placement predicates, use permission and NeoForge captured-block
protection/rollback. Cancellation restores the block and whole original stock,
including off-hand names/components. A new fruit is always green; item block
state components cannot inject ripeness. Native placement sounds, game events,
statistics and placement criteria apply. Existing food and breeding remain.

## O2: bone meal growth

Guava Fruit implements native `BonemealableBlock`. One successful application
advances exactly one age, so green → yellow → ripe takes two bone meal. Native
hand costs/creative handling, protection hooks, particles and dispenser pulses
apply. Fully ripe/unsupported fruit is not a growth target. Right-clicking a
ripe fruit retains its established picking behavior, including when carrying
bone meal: one plain guava drops and the same fruit resets to green.

Ordinary random growth is unchanged. Leaf removal still schedules the native
support check; ripe fruit drops one guava, unripe fruit none. Nothing forces
neighboring chunks to load, replenishes trees or changes saved ages.

## O3: recover canopy blocks

Guava leaves now recognize **Silk Touch** alongside shears. Either yields exactly
one leaf block and suppresses the sapling/stick pools. All other tool chances
remain the established 5% sapling and 20% stick. The predicate follows the
resolved Minecraft 26.2 native leaves table; no Fortune rebalance is included.

## O4: dispenser sapling stations

Dispensers plant one Guava Sapling into dry air directly in front, with valid
vegetation soil/farmland below. Native `BlockItem.place`/`DirectionalPlaceContext` handles
placement, sound and the single-item cost, as in native block-placement
dispensers. Occupied cells, fluids, invalid soil, missing terrain and world
limits retain the whole named/component-bearing stock without ejecting it.
Repeated pulses keep an already planted sapling intact. This applies only to
the sapling item; ordinary guava dispensing keeps its existing behavior.

## O5: supported, bounded sapling growth

Saplings now follow native `SUPPORTS_VEGETATION`, including grass, podzol and
mycelium, and honor NeoForge `canSustainPlant` overrides. Minecraft 26.2 narrowed
`DIRT` to dirt/coarse dirt/rooted dirt; the old broad-tag assumption made grass
placement refuse. Existing ordinary dirt/farmland support remains.

Losing soil support schedules a next-tick check and recovers exactly one
sapling instead of leaving it floating until a random tick then deleting it.
The random-tick fallback also recovers the item. Unsupported saplings refuse
native bone meal. Actual supported plants retain their usual 45% bone-meal
chance and slow random growth; a blocked trunk keeps the sapling intact.

Direct growth requires a current supported sapling. Before fitting/writing,
every planned tree cell must already be loaded and inside build height/world
border, so a stale request cannot grow a tree at an empty cell or request
unloaded terrain. Logs are preflighted before any changes; obstructing
containers remain. Natural settlement tree generation keeps its existing path.
No new tree shape, sapling state or save data is introduced.

## Evidence

Java 25 build/all 49 native cases pass. The native orchard case exercises real
player off-hand survival/creative use, Adventure refusal, canceled placement
rollback with named stock, occupied/water/non-leaf/wrong-face/enhanced-guava
refusals, oak-leaf support, native hand growth and ripe picking, actual Silk
Touch/shears loot, real dispenser growth/ripe refusal and scheduled support
loss with exact drops. The authored guide/preview adds page 59 and a third
Habitat chapter link. Native client controls/fonts and orchard appearance
remain gameplay review. Creature visuals and existing save formats are unchanged.

The native sapling-station case uses real pulses for invalid soil, occupied
containers, contained water, valid planting, repeat refusal and repaired-soil
retry, preserving exact named stock and spending one per success. Scheduled
support loss recovers exactly one sapling; unsupported bone meal retains its
stack. Stale/remote requests refuse without requesting a chunk. Actual grass,
podzol, mycelium, rooted dirt and farmland support pass; an obstructed trunk
preserves its chest/sapling, then clearing it grows the actual tree. Guide
page 62 and its Habitat link/preview are inspected. Natural growth/client
automation remain gameplay review.

## O6: dispenser guava picking

A dispenser with ordinary shears targets the block directly ahead. Supported
ripe Guava Fruit resets to age zero and pays one plain guava as the same native
drop used by hand picking; one successful pulse wears the tool once. An unripe,
unsupported, stale or unloaded fruit cannot pay or wear it. The shared server
picker resets the actual plant before paying, so a repeated stale request
cannot duplicate food. Ordinary hoppers can collect the drops. Native block-drop
rules remain shared with hand picking; no new inventory or saved state.

The original shears dispenser behavior remains the fallback away from Guava
Fruit, including animals, bunnidog fleece, beehives and native tool wear. The
wrapper is registered after native behaviors and delegates the original rather
than reconstructing it. This avoids relying on the 26.2 CommonHooks shear-block
helper, which queries the dispenser source state rather than this target.

## O7: dispenser canopy repair

A dispenser with plain guavas plants one fruit into a dry, loaded air gap
beneath valid leaves, using the same support/world-border/height checks as hand
propagation. A successful pulse spends exactly one item and keeps the rest of
its components. When planting is impossible, it delegates the previous ordinary
food-ejection behavior, preserving food stations and protecting containers.
Pulse once to fill a gap; continuous invalid pulses still dispense food.
Higher guava tiers retain their ordinary behavior and never plant fruit.
Together with native bone meal and O6 this completes plant/grow/pick orchard
stations without another block, item, slot, payload or persistent field.

O6/O7 evidence (2026-10-19): Java 25 build/all 56 native cases pass. The new
orchard-picking case uses actual redstone pulses and hopper collection, checks
exact one-fruit/one-wear output and shared stale/unripe/unsupported/unloaded
refusal, and exercises native beehive honeycomb output/wear through the fallback.
The extended case plants one named real guava, preserves its remainder, ejects
one ordinary food at a blocked chest without replacing it, and leaves upgraded
guavas as food rather than planting them. Existing hand propagation/picking,
care dispenser/shearing and bone-meal cases remain passing. New guide pages
and navigation are previewed; client automation appearance remains gameplay review.
