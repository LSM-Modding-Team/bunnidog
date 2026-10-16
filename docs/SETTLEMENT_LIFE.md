# Settlement life L1–L4

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
