# Settlement life L1/L2

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

## Evidence

The actual Gradle build and server suite pass. The focused native case covers
capacity, distinct components/food, visual fill, saved stock, diet filtering,
resident home/path lookup, exact meal/healing/cooldown, native withdrawal,
bell pathfinding, transport interruption, rest, injury and night refusal.
The first run caught an empty-hand fallback issue: rejected item interactions
must delegate to the native block method to reach inspect/withdraw. That is
fixed and the combined run passes. Generated textures/model-box preview were
inspected; client appearance and prolonged multiplayer routines need a playtest.
