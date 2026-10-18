# Guava orchards O1–O3

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

## Evidence

Java 25 build/all 46 native cases pass. The native orchard case exercises real
player off-hand survival/creative use, Adventure refusal, canceled placement
rollback with named stock, occupied/water/non-leaf/wrong-face/enhanced-guava
refusals, oak-leaf support, native hand growth and ripe picking, actual Silk
Touch/shears loot, real dispenser growth/ripe refusal and scheduled support
loss with exact drops. The authored guide/preview adds page 59 and a third
Habitat chapter link. Native client controls/fonts and orchard appearance
remain gameplay review. Creature visuals and existing save formats are unchanged.
