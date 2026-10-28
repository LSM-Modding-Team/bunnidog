# Guava gathering and orchards

The 2026-10-28 early-game revision supersedes the old O1/O7 raw-fruit-under-any-
leaves shortcut. The goal is a productive, visible orchard progression supporting
breeding, food, treatment and later society, without a tutorial screen or another
quality system. Guava tiers and inherited seed quality retain their existing roles.

## The player loop

1. Find actual guava trees. Independent wild groves now generate in forests,
   flower/birch forests, savannas and jungles, rather than requiring a settlement.
   A placement averages one attempt per 16 eligible chunks and tries three nearby
   trees. These taller trees carry two-fruit bunches with mixed flowering, green
   and ripe stages, including a guaranteed ripe first bunch on each fitting tree.
2. Pick a low ripe bunch, or shake the standing trunk. The first normal punch
   also shakes it; empty-hand use works without cutting it down. Only ripe fruit
   on that trunk drops. Flowers and green fruit stay; the tree grows another crop.
3. Make a copper **Guava Picker**. Its fruit-only eight-block ray catches a whole
   bunch into the actual inventory; a full inventory drops the real remainder.
   Ordinary walls stop it. It does not increase combat, building or other reach.
4. Craft one ordinary guava into the existing Guava Sapling. Plant it in soil for
   a whole tree, or graft it below leaves connected to a living rooted guava trunk.
   Sapling-grown trees start flowering, so bone-mealing wood never creates an
   instantly ripe harvest. Raw guavas remain food; oak-leaf fruit wallpaper ends.
5. Improve the soil immediately under a trunk with **Orchard Mulch**. A connected
   tree normally grows two-fruit bunches; mulch grows three and doubles its random
   growth chance. White flowers, green fruit and yellow ripe bunches expose the
   stage and count. No fertility meter, watering chore or repeated fertilizer cost.
6. Teach and employ the existing bunnidog routines. Workers harvest a full actual
   bunch into their existing stores, respecting capacity, filters, protection,
   learned skills, genetic limits, home and work recovery. Low reachable orchard
   branches are useful for animal tending; hand tools still handle tall groves.

The recipe book unlocks saplings and pickers on collecting the first guava, then
mulch on collecting a sapling. Item purpose lines remain brief. The optional guide
has updated orchard pages; it is not required to find the first harvest. Ordinary
plain-guava tooltips no longer front-load seed-quality theory; advanced tooltips
and enhanced fruit preserve the existing measurements.

## Materials and timings

| Craft | Actual ingredients | Result |
| --- | --- | --- |
| Guava Sapling | 1 ordinary Guava | 1 sapling or graft |
| Guava Picker | 1 Copper Ingot, 2 Sticks, 2 Root Fibre | 1 picker |
| Orchard Mulch | 1 Dirt, 1 Bone Meal, 2 Wheat, 1 Root Fibre | 4 soil blocks |
| Root Fibre (existing) | 1 Guava Log | 4 fibre |

The picker has 128 durability, native Unbreaking/Mending, copper repair and a
six-tick shared item cooldown. One successful harvest costs one wear, irrespective
of bunch size. It cannot spend durability on unripe/protected/blocked/stale fruit.
Shaking rests twelve ticks per player and harvests at most 32 bunches from one
loaded nine-by-thirteen-by-nine canopy. Native per-target protection still applies.

The saved `age` remains 0–2: flowering, green, ripe. New `bunch` is 1–3, default 1
for old states. On the first growth stage, the current physical root/soil chooses
the new crop size. Removing soil later does not erase already-grown fruit.
Random growth odds per stage are 1/5 isolated, 1/4 rooted, 1/2 mulched. One native
hand/dispenser bone meal advances one stage; full ripeness refuses it. Random
timing is deliberately variable. There is no extra timer or world crop manager.

## Shared harvesting and compatibility

Hand, picker, trunk and shears dispenser harvesting reset the actual branch before
paying the exact ripe count. Native block drops are respected, including direct
picker collection. Breaking/support loss uses the same count in native loot;
flowers/green bunches drop nothing. Shears wear once per successful pulse and
retain ordinary animal/beehive behavior. Hoppers collect ordinary loose produce.

A dispenser holding an actual sapling can graft a valid canopy or plant valid
soil. Blocked targets retain named/component-bearing saplings. Dispensed raw fruit
uses ordinary food ejection. Native captured placement supports cancellation,
Adventure predicates, creative use, off-hand costs and exact stack rollback.

An injured animal eats **one** guava, preserving the other ripe fruit in a bunch.
Garden harvest and primitive wild harvesting pay the full count. Worker insertion
uses the actual inserted amount and drops only the remainder, preventing duplicate
multi-fruit payouts. Botanical gap restoration costs the existing real surplus
fruit and now requires a rooted guava canopy; learned/work/reserve gates remain.

Root lookup follows only loaded guava leaves for six steps and a vertical trunk
for at most 24 blocks. Shaking checks the same physical root for each bunch and
revalidates player, hand and target after callbacks. Picker rays check loaded
terrain, the first obstruction, target permission and unchanged tool/block after
native callbacks. Neither system loads a chunk or touches another tree's fruit.
New grove generation preflights its entire bounded shape before writing.

Old hanging fruit keeps its any-leaf survival rule and regenerates a single crop
when disconnected. No existing plot, terrain, stock, genome or guava tier is
rewritten. Independent groves need new terrain; existing standing guava trees can
be shaken and improved. Protocol **47** requires matching clients and server.

## Evidence

Native cases cover actual off-hand graft cancellation/rollback, Adventure and
creative behavior, rooted/oak/occupied/fluid refusals, bone meal, exact bunch loot,
shears and hopper collection, raw-food ejection, trunk protection/replacement,
root/mulch crop changes, picker reach/obstruction/cooldown/tool swap, block-drop
rules, worker stock conservation and one-fruit meals. The grove case uses an
actual detached WorldGenRegion and unfinished ProtoChunks, checking living leaf
support, tall trunks, mixed stages and a ready crop without loading live chunks.

Java 25 `build checkBunnidogModel runGameTestServer` passed, including all **267**
native scenarios and the maintained localization/model/asset gates.
`art/guava_tree_preview.png` shows the actual regenerated flowers/bunches, sapling,
mulch and picker textures. `art/orchard/overview.png` was inspected from the actual
seed-1832 ProtoChunk grove, with native cross planes and resource textures;
`native_grove_cells.json`/the NBT and provenance preserve its source. These are
headless checks. Native first-session feel, grove frequency in natural terrain
and long Survival orchard output remain human play review.
