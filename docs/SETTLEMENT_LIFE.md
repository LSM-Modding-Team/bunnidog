# Settlement life L1–L6

Implemented 2026-10-16 under standing continuation authorization. Careful,
home-bounded routines make resident animals useful and settlements feel occupied.
No hunger/starvation system, forced breeding or changes to existing containers.

## L1: Feeding Trough

Craft `P P /PIP/PPP`: seven any planks and one iron ingot. The shallow trough
holds one food type, up to 16 items (or its smaller native stack limit). Normal
use with food in the main hand adds as much as fits. Empty-hand use inspects;
sneak-use with both hands empty withdraws the stock. Breaking returns the stock
and the block. Four visual fill levels represent its contents. Whole item
components persist; changed data-pack food rules do not delete existing stock.

Accept guavas or any inherited diet's tagged food, excluding food with a use
remainder (bottles/bowls). Injured adult residents seek a reachable loaded trough
within eight blocks and their existing 16-block home boundary, by day. They eat
only food matching their own expressed diet, or guavas; each meal consumes one
item, heals 2 HP and shares the saved 30-second foraging recovery. No love mode,
pup growth, inventory raiding, crop edits or mob-griefing requirement. Higher
priority danger, breeding, temptation, home, leads and rides take precedence.
Searches use a shared POI index, run at bounded intervals and never load terrain.
The stock is player-provided; no hopper/menu system is added in this stage.

## L2: Bell gatherings

Healthy adult residents may visit a loaded vanilla bell near home from clock
ticks 6,000–9,000, spending at most 20 seconds there before at least one minute
of rest. They choose separate nearby standing spots within the home boundary;
villager meeting tickets are neither acquired nor released. Danger, injury,
breeding, temptation, transport and night-home behavior interrupt the routine.
Dimensions without a default day clock have no scheduled gathering. Player
placed bells and native village bells work in existing settlements. This stage
keeps warren generation layouts unchanged. No new animal save fields are required.

## L3: fitted settlement furniture

New fitted dens get one Feeding Trough, with two starter guavas. Levels 3–5
also get a floor-mounted native bell in the central den/hall where room fits.
Furniture occupies spare side cells, with two walking neighbours; it leaves
floor Nests, door middles, room centres, existing bedding and signs clear.
The level-one standalone den receives its own trough after successful fitting.
Unsafe/unfitted rooms receive no furniture. The same planner serves natural
surface/End structures and cave/Nether/command settlements.

Network furniture uses separate saved `LifeFixtures` tuples, never new/reordered
tunnel-cell IDs. Each fixture/support column belongs to one chunk and placement
follows that chunk's tunnel/sign writes. Existing blocks, containers and trough
contents are preserved. New standalone starts use WarrenVersion 3; older starts
without fixture tuples or the new version retain their prior generation.
Starter food is set only on new troughs and never replenished by repeat placement.

## L4: resident gardening

Use any hoe on a trough to toggle gardening, without tool wear. Two green leaf
markers show enabled mode. Crafted/old troughs default off; new generated ones
start enabled. Normal feeding, inspection and withdrawal stay available.

A fully healthy adult with a Nest can tend ripe food beside an enabled trough,
within its normal home boundary, while free of danger/leads/rides and night.
It gathers only food accepted by its expressed Diet and the trough's current
type/components. Search at most two troughs within eight blocks, crops within
three horizontal blocks and one vertical block, with at most eight native path
attempts per ten-second search. Work needs a dry supported adjacent standing
spot and sight/reach of both crop and trough (two/three blocks respectively).

After three seconds of work, reset one mature wheat/root crop or Nether Wart
to age zero, or pick a reachable ripe guava/glow berry and reset its fruit.
Exactly one produce goes into the trough; there are no seed/bonus drops.
Unsupported crops, full/mismatching stock and unripe plants are refused.
Native mobGriefing and NeoForge grief/destruction protection hooks apply.
A successful job starts a minute of saved `GardenWait`; old animals load rested.
Protection refusals also back off for a minute before trying again.
Interruptions preserve the crop and home. Higher-priority feeding, danger,
breeding, temptation and home routines interrupt; gardening precedes idle visits.
No existing barrel/chest is inspected or restocked.

## L5: gardening clarity and working farms

Sneak-use Calipers for a fifth, gardening row in the care snapshot. It uses
the AI's own eligibility result: adult/full health/Nest, home boundary, danger,
night, leads/rides, recovery and mobGriefing. Rest uses rounded-up minutes:seconds.
"Ready to search" still needs an enabled trough, room for matching ripe food
and a safe path; inspection does no crop/POI/path search and changes no state.

New WarrenVersion 4 farms can replace one corner wheat crop and its farmland
with an empty enabled trough on solid palette support. The old 3×3 footprint,
central water and seven other crops remain. Its two outside walking neighbours
must already be dry, supported and clear at the farm's fitted height; otherwise
the farm keeps all eight crops and omits only the trough. Corner rotation follows
the existing prop rotation. No paths, entrances or extra terrain are replaced.
Empty stock allows the first matching harvest to choose its food type. End
mushroom plots retain the old layout because mushrooms are not supported work.
The existing anchor chunk owns the entire prop, including the corner trough;
saved versions 1–3 retain their old farm layout. Existing den furniture and its
two starter guavas are unchanged. Repeat placement preserves existing stock.

The extended native fixtures/gardening cases cover old/new farms, blocked
walking space, support, empty enabled stock, repeat preservation and agreement
between work blockers/recovery and the read-only Calipers snapshot. Focused
layout checks cover all corner rotations, retained crops and footprint.
Actual build checks and all eighteen native scenarios pass. The first farm
fixture scanned its native barrier roof as terrain; the fixture now clears
the scan window. Farm and guide previews inspected. Natural terrain/chunk order
and client Calipers formatting remain gameplay review.

## L6: local food stores

Fresh underground pantry/market/commons barrels now use one of eight native
lineage loot tables. Fresh surface haystack feed stores and market stalls also
receive this deferred loot. The whole settlement uses its centre's lineage,
matching its existing palette and founder population. Supply is deliberately
modest: one roll of 1–2 guavas, then 1–3 local food rolls of 1–3 items each,
plus an independent 35% Field Guide chance. There are no materials, legends or
equipment rolls; food can be moved into nearby troughs by the player.

| Lineage | Local food choices |
|---|---|
| Meadow | Wheat, carrots, potatoes, beetroot, apples, sweet berries |
| Tundra | Raw cod and salmon |
| Desert | Cactus and dried kelp |
| Swamp | Red and brown mushrooms |
| Cave | Glow berries |
| Mountain | Wheat, moss blocks, azalea leaves and occasional hay |
| Nether | Nether wart, crimson and warped fungi |
| End | Chorus fruit |

Only newly placed barrels receive loot metadata. The legacy `warren_pantry`
table remains for existing unopened barrels; existing loot keys, seeds, items
and their components are preserved. Emptied barrels are never restocked, and
worldgen does not unpack existing loot. Underground columns retain their owner
chunk and container preflight. Surface prop fitting also refuses existing block
entities under the fitted pad, preventing repeat stores above older containers.
No layout, genome, network or animal save format changes are required.

`art/gen_pantries.py` owns the eight tables; guide chance comes from the shared
`art/gen_guide.py` pool. The old mixed table stays available. The new native
pantry case opens real barrels for every lineage, checking actual parsed loot,
diet suitability/counts, unread key/seed saves, owner chunks, old pending loot,
named player stock, empty-repeat preservation and both surface store kinds.
Actual build and all nineteen native scenarios pass; updated guide preview
inspected. The native case opens deferred loot before simulating empty stock,
and repeat surface fitting is refused above existing containers. Natural
multichunk generation and multiplayer exploration remain gameplay review.

## Evidence

L3/L4 actual build and the focused fixture/gardening server cases pass (seventeen
mod scenarios plus native sanity). Cases cover owner-chunk writes, two starter
guavas/visible mode, repeat tunnel/furniture preservation, containers/supports,
native optional structure metadata/legacy versions, hoe use, diet/health/griefing/
stock/night refusals, exact carrot and guava harvest, home/lead interruption and
saved recovery. Focused layout checks retain access to all Nests and rooms after
conservatively replacing fixtures with solid obstacles. Trough/guide previews
were inspected. Natural multi-chunk generation, client input/appearance and
prolonged multiplayer gardening still need gameplay review.

Existing L1/L2 evidence follows.

The actual Gradle build and server suite pass. The focused native case covers
capacity, distinct components/food, visual fill, saved stock, diet filtering,
resident home/path lookup, exact meal/healing/cooldown, native withdrawal,
bell pathfinding, transport interruption, rest, injury and night refusal.
The first run caught an empty-hand fallback issue: rejected item interactions
must delegate to the native block method to reach inspect/withdraw. That is
fixed and the combined run passes. Generated textures/model-box preview were
inspected; client appearance and prolonged multiplayer routines need a playtest.
