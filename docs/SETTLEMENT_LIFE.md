# Settlement life L1–L24

Implemented 2026-10-16 under standing continuation authorization. Careful,
home-bounded routines make resident animals useful and settlements feel occupied.
No hunger/starvation system, forced breeding or changes to existing containers.

## L23: developed underground farm plots

Fresh cave/Nether feature settlements can now fit their regional farm when
five guava trees actually survive fitting. Previously all underground
landmarks were skipped, making their L21 plot variants unreachable there.
Only the water-free regional `FARM` is eligible underground: cave glow-berry
trellises and Nether wart/soul-sand beds. Wells, stalls and other surface
landmarks retain their existing surface scope. Ground fitting uses cave mode
at the plot's own location, with the same all-cell preflight and optional
empty enabled trough. Unsafe/blocked plots are simply omitted.

Feature landmarks now use their actual kind's surviving-tree tier instead of
its position in a potentially shortened candidate list. A missing early
landmark cannot promote a later high-tier one after tree fitting loses trees.
The diagnostic wanted count reflects eligible plans. Existing generated
settlements are not retrofitted; saved structure piece versions/IDs remain.

L23 evidence: Java 25 build/all 49 native cases and existing focused warren
checks pass. The regional native case now uses cave-mode ground fitting for
cave/Nether plots and checks repeat container/stock preservation there as well
as surface roof probing. Surface variants, valid plant survival and legacy
saved versions remain covered. The new chapter/Habitat preview is inspected;
full natural discovery/chunk fitting remains gameplay review.

## L22: sustainable mushroom thinning

Healthy adults whose expressed Diet accepts mushrooms can thin small red or
brown beds beside an enabled trough. A candidate and at least one other living
mushroom of the **same color** must fit native survival. The companion is within
a three-block sphere, at most one block higher/lower, loaded and inside the
same home bounds. The job removes one mushroom and inserts exactly one matching
item, keeping seed stock for native spread. Huge mushrooms, Nether fungi and
the final specimen of either color are left alone. No Bloom threshold or
accelerated growth; native mycelium/light/space rules determine regrowth.

This shares harvesting's existing diet/filter/capacity, safe grounded routes,
griefing/protection and redstone controls. The companion is rechecked throughout
the task and after native protection callbacks, so disappearing seed stock
prevents a stale harvest without stock or work rest. Successful work shares
the saved one-minute recovery with every other garden/compost task. There is
no new animal state, item type or automatic planting.

L22 evidence (2026-10-18): Java 25 build/all 47 native cases pass. The new
claimed-home case checks separate red/brown seed stock, other Diet and exact
filter refusal, real safe work, a callback removing the companion, successful
one-item/no-loose-duplicate harvests of both colors, preserved final mushrooms,
shared recovery and powered/full/griefing refusals. The mushroom/orchard/Habitat
guide preview is inspected; natural spread and resident routines remain client
gameplay review.

## L21: regional farm beds

New version-five settlement farms use the same 3×3 footprint with suitable
regional food: a two-high central cactus on sand, wart on soul sand, two red
and two brown mushrooms on mycelium, two fruiting glow vines under a guava-plank
trellis, or a chorus flower on end stone. Meadow, tundra and mountain plots
retain their existing crops. These five variants introduce no water. The cave
trellis stays within the old three-block height reservation. Native plant
growth and survival remain authoritative; no boosted growth or restocking.

A safe corner can hold the same empty hoe-enabled trough on solid ground,
including in End plots. Its outside neighbors must already be dry, sturdy and
clear. Entire plans fit before any block changes, preserving containers and
obstructions. Regional farms refuse existing plank roofs as ground, preventing
a repeat cave placement from stacking trellises. New starts save version five;
older starts keep their stored version and previous crop plan. No retrofit.

L21 evidence (2026-10-18): Java 25 build/all 45 native scenarios pass. Focused
layout checks cover all eight lineages, four rotations and trough opt-in/out,
footprint/height/unique cells and unchanged ordinary geometry. The native case
places all five special plots, verifies every actual block's survival, no
fluids, empty enabled troughs, repeat stock preservation, preflight container
refusal and version-five/four saves. The reproducible FarmDump preview shows
actual layout cells with approximate colors; guide preview inspected. Natural
terrain fitting, cave lighting and client appearance still need gameplay review.

## L20: protected cactus pruning

Healthy desert-browser adults can maintain cactus beside a hoe-enabled trough.
One job trims one spare top from a two- or three-high column and stores one
cactus, preserving all lower segments and their ages. A single base, attached
flower or another segment above is never pruned. Native survival, loaded/home
bounds, safe grounded paths, exact stock/filter/capacity, redstone pause and
ordinary protection/mob-griefing rules apply. No Bloom threshold is needed;
there is no autonomous cactus planting or forced growth.

The existing bounded garden scan reaches two blocks above its trough; a top
can be pruned from clear ground with a three-block reach. Visibility targets
the real cactus collision face, avoiding the lower-segment obstruction to its
centre without ignoring intervening blocks. Other plants retain their shorter
reach. Candidate standing space includes ground two blocks below a tall crop;
all native route/hazard checks remain. The top/base/flower conditions recheck
through commitment, including after native protection callbacks, so new growth
cannot trigger a chain break. Food goes through existing stock insertion; there
is no extra loose harvest drop. The saved one-minute work recovery is shared
with ordinary harvesting, cultivation and composting. Homes/biology are untouched.

L20 evidence (2026-10-18): actual Java 25 build/all 44 native cases pass.
The new case uses a real home, two-/three-high columns, a complete safe route
and actual protection callback adding an upper segment. Stale work refuses
without stock/rest; retry trims the true top, preserves the exact older base,
stocks one cactus and creates no loose duplicate. Single bases, other diets,
wrong filters, flowering tops, full stock and disabled griefing refuse.
Cactus/main/habitat guide preview inspected; natural desert routines remain
client gameplay review.

## L19: serious-injury rest near home

Ordinary adults strictly below half maximum health replace daytime idle
strolling with quiet waiting within two blocks of their actual claimed Nest.
They must already be inside their normal home boundary. Find only loaded,
dry standing space and consider at most four complete native paths, wholly
inside the existing home bounds and avoiding hazards. Searches back off five
seconds, including failure; no safe place means no rest job. An animal already
safely near home can wait without a new path.

This is idle behavior: meals, hunting, grazing, work, breeding, temptation,
danger, leads/rides and ordinary home return retain priority. Thunderstorms
release the job for the existing shelter routine; night keeps native home
waiting/short meals. A flooded/blocked destination, missing/changed home,
combat, player control or health rising to half interrupts immediately. Failed
or exhausted paths stop and retry later. No teleport or chunk request occurs.

Rest supplies no healing, food, growth, stat bonus or reduced care recovery.
Stock suitable food in a reachable trough: food seeking can preempt rest,
including while the adult waits between shared meal recoveries. Its home and
POI ticket are untouched. No new persistent timer/field, pose, model or packet
is introduced. Sneak-Calipers gives cheap readiness/reasons without a search;
ready does not promise an available safe spot. Guide page 53 explains it.

L19 evidence (2026-10-18): actual Java 25 build/all 43 native cases pass. The
new case uses a real claimed home and complete safe path into the two-block
radius, then waits without healing or item/home/biology changes. Pup, half-
health, homeless, combat, night and Apex refusals pass. A real higher-priority
trough goal preempts idle rest before spending stock. Leads, native thunder
and a flooded resting place interrupt; healing restores normal idling. The
lab/recovery/index preview is inspected. Long natural routines/client movement
remain gameplay review.

## L18: read-only bell roll call

Sneak-use Calipers on a bell to list loaded living adult residents whose
recorded Nests are within twelve blocks of it, while the animals themselves
are within the native thirty-two-block call radius. No home/terrain/POI/path/
food search occurs. Unloaded, homeless, pup and Apex animals are excluded.
This is a local snapshot, not a full settlement census or a promise of visit
readiness. Adventure players may read it like ordinary Calipers measurements.

Persistent chat gives total loaded/injured counts and up to eight rows of
name, Ear Tag, current/max health, suitable foods and Nest coordinates. Injured
animals come first, then nearest Nests and UUID ties. Large groups get an
explicit remaining count. The code connects to E5 finding; the home connects
to H8 compass marking. Reading rings no bell, queues no visit and edits no
items, animal timers, health, home or ticket. No saved state/packet is added.
The tooltip and guide page 52 describe the gesture.

L18 evidence (2026-10-18): actual Java 25 build/all 41 native cases pass.
The existing bell-call case reads the actual off-hand Calipers entry beside a
real claimed resident, verifies no queued call or item/biology/home changes,
excludes a pup with a cached home and searches a remote origin without loading
terrain. Real native bell-ring/call/path/recovery cases remain passing.
The roll-call/home/index preview is inspected; client chat/layout and larger
natural-warren management remain gameplay review.

## L17: redstone trough pause

Power a Feeding Trough to pause resident meals, harvesting, cultivation and
surplus composting from that trough. Remove power to make it available again
at the resident's next bounded search. The hoe's gardening opt-in remains
independent; it is neither cleared nor replaced by the signal. Other unpowered
food/work sources remain available under their ordinary rules.

Stock, exact filter, comparator output and manual/hopper insertion/extraction
keep working while paused. The signal reserves food for automation or later
work rather than deleting it. Native hopper power rules still apply to the
hopper itself. Empty-hand stock inspection and insertion feedback mention
the redstone pause. No extra tick, block-state property, item, save field or
network change is required: the server reads the current native signal.

Starting/continuing meals and garden jobs share the pause, and actual food
reservations recheck it. A power interruption before commitment preserves
health, crops, stock and the existing meal/work recovery. Garden jobs also
recheck eligibility after external grief/destruction callbacks; a callback
that powers/removes the source or changes the crop cannot commit stale work.
If a reserved compost input is refused by a later state/power change, return
it through the existing exact-item refund path. Already committed work keeps
its ordinary result/recovery. There is no starvation system or new timer.

L17 evidence (2026-10-18): actual Java 25 build/all 40 native cases pass.
Existing meal/garden/compost cases exercise real redstone-block power, meal
interruption without health/stock/rest changes, actual reservation refusal,
transactional named-stock extraction/insertion and retained full comparator
signal while paused. Removing power resumes a real meal/job. Power inserted
by the native crop-protection callback prevents the final harvest and its
cost/recovery; powered compost selection/surplus reservation also refuse.
Existing hopper/filter/night-meal/cultivation cases remain passing. Updated
automation/pause guide preview is inspected; client circuits remain gameplay
review.

## L16: opt-in feral Nest guards

Sneak-use an ordinary bone on a living feral adult with a claimed Nest to
enable guarding; repeat to disable. The bone is a reusable command and is
never consumed. Adventure/spectator players cannot change this setting. Once
enabled it may be disabled even after losing the home or changing temperament.
Old animals default off. Optional `GuardNest` saves the choice; current targets
and short search/pursuit timers are transient. Baskets preserve the preference,
with work paused until a new Nest is claimed. Apex copies remain ineligible.

A free adult at least half healthy may intercept visible, non-allied tagged
hostile mobs within **eight blocks of both itself and its Nest**, by day or
night. The default `bunnidogs:guard_intruders` tag includes ordinary undead,
pillagers/vindicators and endermites, excluding creepers, major bosses and
neutral animals. Even data packs cannot make this routine select players,
bunnidogs, tameable animals or non-hostile mobs. Ordinary feral retaliation
still applies separately when attacked.

The defense uses the existing two-point native bite, at most once per second,
without free healing or stat bonuses. Search every two seconds, consider at
most four complete paths, and limit one pursuit to ten seconds. Every checked
path node must stay in the same eight-block home boundary and pass the shared
loaded, dry, hazard-avoiding route rules. Recheck routes/positions while working;
losing sight, safety, health, temperament, home or player control stops this job.
Leads/rides, native floating and danger take precedence. Guard targets pause
domestic jobs and the generic retaliation melee must not bypass the guard's
bounded path. Stopping clears only the target this job owns, preserving any
new external retaliation target. No teleport, terrain edits, extra home ticket,
inventory changes or chunk loading are introduced.

Sneak-Calipers shares the cheap enabled/ready/active/paused status without
searching for threats or paths. Ready means ready to search, not a promise of
an intruder. The guide explains the command and defensive limits. This gives
recessive feral breeding a deliberate resident role while leaving every
existing animal's behavior unchanged until the player opts in.
E4 later adds the native **Standing Watch** task/25 XP for the player's first
successful enable command; refused/disable commands grant nothing.

L16 evidence (2026-10-18): Java 25 build/all 39 native scenarios pass. The new
guard case uses a real claimed Nest, off-hand named bone, native Adventure
permissions, passive/creeper refusals, a complete route, lead interruption,
hidden-feral and half-health boundaries, external target preservation, a target
leaving home, actual water-trench refusal and a real native kill without meal
healing. Native saves retain the choice and old data defaults off; Calipers
is read-only. Updated guide/index preview inspected. Natural settlement combat
and balance remain client gameplay review.

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
The stock is player-provided; L9 later adds hopper access without a menu.

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

## L7: resident refuge

Non-feral adult residents with a claimed Nest may retreat from the nearest
visible mob in `bunnidogs:resident_threats`, within eight blocks. Default entries
are hostile mobs and the Apex; neutral Endermen, players, ordinary bunnidogs,
tame pets and allies are excluded. Data packs can edit the threat list. The
routine yields to native panic and pauses for leads/rides, external combat,
feral expression or a changed home. Pups retain parent-following behavior.

Search every two seconds plus a short per-animal stagger. Prefer dry, supported
air beside the current Nest, considering both floor/placed Nest heights; fall
back to a small ring around the animal, always inside the existing 16-block home
boundary. A destination must be at least four blocks from the detected threat
and farther away than the starting position. Try at most eight native paths.
Require complete, loaded, home-bounded routes; reject water, fire, lava, damaging
terrain and powder snow, and keep remaining route nodes at least two blocks from
the threat when the starting distance permits. No safe route means no retreat.

Follow the chosen route at 1.3 speed, then hold briefly. Stop after two seconds
without visible nearby danger, ten seconds total, an unsafe/blocked destination
or route, a lost threat/home or transport. Retreat makes no attacks, changes no
blocks and never moves/claims/releases a Nest. Meals, gardening and bell visits
pause while refuge is active; Calipers reflects the same transient danger state.
This is an attempt to get farther from one detected threat, not protection
against damage or a guarantee against every nearby mob.

The goal owns an unsaved threat reference; old animal and structure formats,
protocol 6 and the approved model remain unchanged. The Field Guide expands to
32 pages. The new native case covers passive/hostile selection, real home and
safe route, no attack target, paused work, lead/gene-change interruption, quiet
recovery, occupied-destination cancellation, retained health/home and Apex
exclusion. Actual build and all twenty-one native cases pass; guide preview
inspected. Native path regions use non-loading `getChunkNow`; visibility also
checks the normalized loaded chunk rectangle before sight tests. Natural travel
and multiplayer threat balance remain gameplay review.

## L8: idle storm shelter

Idle adult residents with a claimed Nest seek nearby cover during thunderstorms.
The goal runs below feeding, gardening, gatherings, home/parent following,
breeding, temptation, panic and refuge, but before random wandering. It only
owns movement, so looking around remains free. Meals and work can preempt it
even while the storm continues. Leads/rides, combat, active refuge, a changed
home or clearing thunder interrupt it; pups retain their existing routines.

An exposed animal must actually be in native rain, inside its 16-block home
boundary. Search at most once per five seconds plus a short per-animal stagger,
considering eighteen positions beside/above the current Nest. Try at most four
native paths to dry supported two-block air beneath cover. Reuse the loaded,
home-bounded hazard checks from refuge. No suitable route means ordinary idle
behavior. An animal already standing safely under cover can wait in place.
On arrival it stops moving until another routine takes over or thunder ends.
Ordinary rain alone does not start or retain shelter.

This small habitat routine adds no hunger, weather immunity, terrain changes,
home-ticket changes, saved fields or packets. It excludes the Apex. Existing
Nests and covered dens can be used without regenerating settlements. The Field
Guide now has 33 pages; the preview uses a substitute font, with native client
appearance and long-running natural weather behavior left for gameplay review.

Actual Java 25 build and all twenty-two native server cases pass. The new case
uses real exposed weather and a roof, checks a complete native route, lead
interruption, covered waiting and release when thunder ends, and exercises
native goal arbitration so a healing trough preempts shelter during the storm.
Nest/stock preservation and Apex exclusion pass; guide preview inspected.

## L9: trough automation and stock signals

Native hoppers can fill a trough from above or from the side. A hopper below
extracts stock; power that hopper to hold food for residents. Compatible modded
item-transfer devices can use the same one-slot capability from any face.
Automation keeps the existing 16-item capacity (or smaller item stack limit),
food/remainder filtering, exact data components and single-food rule. It never
changes Diet acceptance, starts breeding or enables gardening; a hoe still
controls work. Existing generated/player troughs gain access on loading.

Comparators read 0 when empty and 1–15 for stock, with 15 at effective capacity.
The formula is `1 + floor(14 * count / capacity)` for nonempty stock; eight
ordinary items yield 8 and sixteen yield 15. Every committed transfer, meal,
harvest or manual stock change notifies comparators, including changes within
one of the four visual fill levels. A smaller component-defined stack capacity
uses its actual limit for fullness.

The NeoForge 26.2 transactional item handler shares one journal on all faces.
Aborted transfers restore the exact stack without visible/redstone updates;
only root commits notify the level. It uses the existing `Food` save field and
preserves previously saved stock even if data-pack food tags change. Breaking
still drops the preserved stock once. No ticker, menu, new saved format, custom
packet, structure refill or recipe change is needed. The Field Guide grows to
34 pages, with an automation page explaining how to retain food.

Actual Java 25 build and all twenty-three native cases pass. The new case uses
real hopper transfer code and scheduled comparator ticks for capped insertion,
named-component extraction, same-fill-level redstone changes and empty output.
It also covers aborted transfers, invalid/mismatching/remainder foods, a smaller
component-defined capacity and exactly-once stock drops. Its drain hopper uses
real redstone power so native neighbour updates retain the lock. The guide
preview is inspected; modded-device compatibility and client contraptions remain
gameplay review.

## L10: native bell calls

Ring a native bell to request a daytime visit from healthy adult residents,
without waiting for noon. Hand use, projectiles, redstone and the bell's other
native ring sources work. The bell must be loaded, within 12 blocks of the
animal's current Nest and within the native 32-block hearing radius. Animals
still need to be within their existing home boundary, unled/unmounted, fully
healthy and free of combat, recent attacks and refuge. The Apex is excluded.
Night-home routines take precedence. Calls also work in dimensions without a
day clock; they do not invent a scheduled noon there.

The relay listens to an actual fresh bell ring's native game event instead of
polling bells or searching remote terrain. Each animal accepts at most one call
per five seconds, keeps it for ten seconds and consumes it on a search attempt.
A call can bypass failed-search backoff, but never the existing one-minute
post-visit rest. Repeated ringing cannot prolong the current twenty-second
visit or force a feeding/working animal to leave its higher-priority task.

Called and scheduled visits use loaded, dry supported standing spots and full
home-bounded hazard-checked native paths. One call tries four spots beside its
exact bell; scheduled visits still inspect at most two bells. Route replans at
most once per two seconds use the same safety rules. An occupied destination,
removed bell, changed home, injury, danger, transport or night interrupts the
visit. No attacks, block changes, Nest/villager-ticket changes or rewards occur.
The pending call and rate limit are transient and clear on native loading;
existing saves/protocol remain compatible. The Field Guide grows to 35 pages.

Actual Java 25 build and all twenty-four native cases pass. The new bell case
exercises the real native ring/event relay, a morning visit after a failed noon
lookup, safe native routing, injured/led refusals, rapid-ring limiting, changed
home interruption, post-visit rest, night refusal and Apex exclusion. Existing
scheduled-gathering cases still pass. The updated guide preview is inspected;
natural multiplayer bell use and client contraptions remain gameplay review.

## L11: saved trough filters

Sneak-use suitable food on a trough to reserve it for that exact item and its
data components. The virtual one-item template consumes nothing and adds no
stock. A different existing stack must be withdrawn before changing the filter.
Manual insertion, hopper transfers and gardening all respect the reservation,
including when the trough is empty. Named food is distinct from unnamed food.
Normal use still fills; a hoe still controls gardening.

Sneak-use with both hands empty withdraws real stock and retains the filter.
Repeat while already empty to clear the reservation. Inspection names the filter
alongside stock. Existing build permissions and explicit interaction denials
apply. A narrow right-click hook lets this crouched-food action reach the block
through the native server/client interaction flow, which normally skips blocks
when crouching with food. Other blocks and denied interactions are untouched.

Optional `FoodFilter` saves the full template components at count one; legacy
troughs remain unfiltered without restocking. Saved food is preserved even if
food tags or filter rules later change. Filtering never prevents extraction or
eating already stored compatible food. The virtual template never drops or
refunds on clearing/breaking; only actual stock does. Capacity, comparator stock
math, the existing update packet, block states and native transfer transactions
remain unchanged. The Field Guide grows to 37 pages.

Actual Java 25 build and all twenty-five native cases pass. The new case uses
the real crouched server interaction path, exact-component automation, conflict
refusal, native save/legacy loading, withdrawal/clearing and no virtual-template
refund/drop. It frees the hand after native withdrawal fills the selected slot.
The existing gardening case also leaves a wrong-filter crop intact. Guide
preview inspected; client input and modded-device use remain gameplay review.

## L12: bred cultivation and berry tending

Fully healthy adult residents with **Bloom +10** can cultivate compatible crops
beside a hoe-enabled trough. Ripe harvests take priority; otherwise three seconds
of work advances one crop by one stage, followed by the existing one-minute
`GardenWait`. Lower-Bloom adults still harvest normally. Both activities share
the same recovery, so stats changes, unloading or basket travel cannot bypass it.

Supported crops are wheat, carrots, potatoes, beetroot, Nether wart, low guava
fruit and sweet-berry bushes. Mature berries yield one sweet berry and return
to age one. Bloom cultivation can also replace berries on an empty existing
cave vine; it does not extend vines. Planted bunnidog seeds, stems, fungi,
chorus trees and arbitrary modded crops are excluded. Growth creates no stock
or loose item; a later harvest remains necessary.

The resulting food must suit the resident's current Diet and fit the enabled
trough, including exact filters and available capacity. Existing home/daytime,
danger, transport, health, griefing and block-protection rules apply. Losing
the required Bloom or changing the crop/stock/filter interrupts cultivation.
Native routes must be complete, loaded, home-bounded and free of the same
hazards rejected by refuge/shelter. No block, item, gene, packet or save format
is added. Calipers explains whether the adult has the Bloom cultivation bonus.

L12 evidence (2026-10-17): Java 25 `build runGameTestServer` passes all 28 native
scenarios. The cultivation case covers the exact Bloom threshold, interrupted
growth, empty wrong filters, Diet/griefing, one-stage/no-food growth, shared
work rest, ripe priority, ordinary sweet-berry harvest, embryo exclusion, guava
growth and existing-vine fruiting. The revised guide preview is inspected.
The dispenser fixture now confines randomized native drops to a catchment;
previously a product could leave its small query before the next pulse assertion.

## L13: short nighttime trough meals

An injured ordinary adult already within four blocks of its Nest may take a
short trough meal at night. The food, standing spot and entire loaded native
route must stay inside that four-block boundary. Meals still require matching
Diet/real stock, a safe dry supported destination, no combat/refuge/recent attack,
no lead/ride and readiness under the shared thirty-second `ForageWait`.
Unhealthy animals outside this boundary return home before looking for food.

The trough goal takes precedence over night waiting, but yields to panic,
refuge, breeding, temptation and parent following. After consuming one item
for the existing 2 HP, the animal resumes its normal two-block night tether.
Outdoor grazing, hunting, gardening, cultivation and gatherings remain daytime
activities. No hunger, free regeneration, night work, home changes, new saved
field or protocol is added. Calipers names the nighttime trough-only readiness.
Daytime trough paths also gain the shared complete/home-bounded hazard checks,
including bounded safe replans and blocked-destination interruption.

L13 evidence (2026-10-17): actual Java 25 build and all 30 native scenarios pass.
The night-care case exercises real goal arbitration from waiting to food and
back, exact stock/healing/cooldown, distant food and outside-home refusals,
unchanged daytime reach, outdoor/work exclusion, lead interruption, damaging
routes, blocked standing room and Apex exclusion. Existing daytime food,
shelter, travel and work cases also pass. Updated guide preview inspected;
natural overnight behavior remains gameplay review.

## L14: surplus composting

Bloom +10 adults may work a nearby vanilla composter from a hoe-enabled trough,
after ripe harvesting and cultivation candidates. The composter must be within
the existing three-block work area, at levels 0–6, with a safe loaded native
route and sight of both blocks. The source stack must suit the expressed Diet,
have a positive native composting value and contain more than eight items.
One completed three-second job consumes one exact real food item, retaining a
reserve of at least eight, and starts the shared one-minute `GardenWait`.

Use native compost chances, maturation, comparator signals and hopper output.
A failed chance still consumes that item as normal; the first layer follows
native guaranteed behavior. Residents leave ready compost for players/hoppers.
No food, compost or bone meal is created free of its input. Full troughs may
compost surplus; filters still reserve the exact food and are never consumed.
Non-compostable foods, fewer than nine stock, low Bloom, disabled troughs,
blocked routes, night, injury, danger, leads/rides, griefing/protection refusal
or changed inputs interrupt/refuse work. The source item is reserved before
native composter callbacks; an unused item returns to its original trough or
drops with its components if that trough is removed. Existing crop and meal
priorities, recovery saves, homes, allele IDs and protocol remain unchanged.

This connects existing resident food production to ordinary bone meal for
gardens and Nursery Cradles without another block, item or inventory screen.

L14 evidence (2026-10-17): Java 25 build/all 37 native scenarios pass. The
new native case claims a real Nest and path, exercises Bloom +9/+10 and the
eight-item reserve, committed transfer/lost-Bloom interruption, mob-griefing
and actual block-protection refusal, harvest/cultivation priority, named stock/
filter preservation, shared rest and non-compostable food refusal. Seeded native
randomness establishes both failed and successful composter chances with one
real input each; the actual scheduled tick matures the result and a real hopper
extracts exactly one bone meal. Priority fixtures use ordinary unfiltered
carrots, since the named reserve correctly refuses unnamed crop output.
The 43-page guide preview is inspected. Natural work cycles and multiplayer
farm balance remain gameplay review.

## L15: ordinary guava compost data

Native NeoForge compost data gives plain guavas a 65% chance and guava leaves/
saplings a 30% chance, matching ordinary fruit/foliage. Players and vanilla
hoppers can use them. Bloom +10 plain-Diet residents may consequently compost
real orchard surplus through L14's existing reserve, priority and rest rules.
All seven higher guava tiers remain non-compostable by default, preserving
their infused ingredients for breeding. The data map merges with native and
other mod entries rather than replacing them; it changes no item/save format.

Actual build/all 37 native scenarios pass. The composting case reads the real
loaded values, checks every higher-tier exclusion and runs one real plain
resident guava job with exactly one cost/eight-item reserve/shared rest. Native
chance/maturation/hopper coverage remains in the same case.

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

## L24: gather ordinary loose food

Healthy adults with bred Senses +10 can collect one compatible ordinary loose
food into a hoe-enabled trough within three blocks, by day and inside their
claimed home area. They need the same loaded dry standing space, safe complete
route and sight/reach as other work. Native pickup delay/owner reservations and
a ten-second item-age grace apply; upgraded guavas are always left intact.
The exact food components must match stock/filter, with capacity and redstone
pause rechecked. Animal Diet still decides which items it can handle.

Collection follows garden crop work, before bell visits, and shares the saved
one-minute GardenWait. No carried item, extra inventory, forced chunk load or
new persistent animal field. Before committing, native mob-griefing callbacks
run and the food, power, worker and route recheck. A native transaction journals
both the ground stack and trough: an abort/full destination restores the whole
source and stock. Committed stock notifications may remove the trough; normal
removal drops the paid item once, without restoring a second ground copy.

Java 25 build/all 52 native cases pass. The new collection case checks real
claimed-home paths, Senses +9/+10, exact named filters/components, native abort
and full-destination rollback, callback-added power, one-item transfer/signals,
shared garden recovery and actual stock-notification removal conserving food.
Calipers names the Senses gathering bonus. Guide/navigation preview inspected;
client worker behavior remains gameplay review.

## L25: renewable melon beds

Forager and omnivore Diet tags now include ordinary melon slices for healing,
pup growth, trough meals and loose-food handling; slices remain outside guava
breeding food. Healthy residents can harvest a vanilla melon near a hoe-enabled
trough only if a living attached melon stem points to that fruit. Both stem
and fruit must be loaded and inside the claimed home bounds. Decorative melons,
wrong-facing/detached stems and unsupported stems do not authorize a job.

One protected three-second job removes the fruit into exactly one slice of
stock, preserving the stem, soil and surrounding blocks. Native neighbor
updates turn the attached stem back into age-seven melon stem, so native growth
supplies the next fruit. This deliberately uses the existing modest one-item
resident work yield rather than vanilla player-break slice rolls. There is no
extra loose drop. Existing Diet, filter/components, capacity, redstone, daytime,
health, griefing, native safe paths, sight/reach and one-minute GardenWait apply.
Linkage rechecks after protection callbacks as well as during the visit.

## L26: cultivate young melon stems

Bloom +10 workers can advance an existing supported melon stem from ages 0–6
by exactly one age at a compatible enabled trough. The future slice must suit
the Diet, filter and available stock space. No item is produced, consumed or
carried; the job shares ordinary garden recovery. Ripe harvesting remains the
first work priority. Age-seven stems and attached stems cannot be harvested
as plants, wrapped to zero or forced to place fruit. Native light, soil, space
and random growth rules govern future fruit. Players plant seeds normally.

L25/L26 evidence (2026-10-19): Java 25 build/all 53 native cases pass. The new
native melon case claims a real Nest and complete safe path; it checks absent/
wrong-facing linked stems, Diet/filter refusals, protection-callback stem loss
without food/rest, exact one-slice harvest, real native age-seven stem reset,
no duplicated loose produce, Bloom +9/+10, one-stage/no-food cultivation,
shared rest and mature-stem refusal. Native hand feeding checks plain refusal,
forager adult healing and pup growth with exactly one real slice per success,
while slices remain outside breeding food. The 70-page guide preview is
inspected; natural melon cycles and client worker behavior need gameplay review.
No animal save, network, allele or settlement-plan format changes.

## L27: meadow melon supplies

Meadow pantry local-food rolls include modest ordinary melon slices alongside
existing crops/fruit. A separate independent 35% pool supplies one/two melon
seeds, letting explorers establish L25/L26 beds. Guavas, one-to-three local-food
rolls and the independent Field Guide chance remain separate. Seeds are supplies,
not resident food. Other lineage tables and the old generic pantry remain
unchanged; unopened meadow den/feed-store/market barrels use the updated native
table, while previously filled/emptied/player containers retain their stock.
No generation-plan or container save migration/refill is introduced.

Java 25 build/all 56 native cases pass. The existing native pantry scenario
validates modest seed bounds only for meadow, lineage Diet food compatibility,
native deferred saves, owner-column placement and repeated-generation stock
preservation. Sixteen deterministic native chest seeds cover both optional
supply outcomes while preserving the guaranteed guava and local-food pools.
Guide stem-growing page names meadow seed supplies. Natural exploration remains
client/gameplay review.

## L28–L30: selected work, restoration and nursery tending

`RESIDENT_WORK.md` defines five saved work modes (L28), real-food gap restoration
in Garden mode (L29), and paid one-stage Cradle care in Nursery mode (L30/N9).
Old/new default Mixed keeps existing jobs; more food-spending work requires an
explicit mode. Native controls/saves, resource conservation and protected work
pass all 59 native cases. The 77-page guide includes the new controls and jobs.

## L31: fresh meadow melon plots

New meadow farms mix two age-three melon stems with wheat/roots and one native
water source. Two dirt cells are explicitly reserved as air for future fruit;
the existing optional empty hoe-enabled corner trough replaces one crop when
its outside walking space fits. Layouts rotate within the old 3×3 footprint
and height reservation. Native growth supplies fruit; no mature melon or free
new stock is placed. The ordinary native growth rules may use other valid
neighboring soil as well, so players should keep room around the stems.

New pieces save WarrenVersion six. Version-five/older pieces keep the former
crop geometry; other regional farm types and every existing saved tunnel,
fixture, container, house and tree plan remain unchanged. Direct fresh feature
placement uses the new layout. Preflight includes fruit-gap air cells, so a
protected container there refuses the whole plot before soil/crops are edited.
Repeated placement preserves grown fruit and trough stock.

Java 25 build/all 59 native cases pass. The existing regional-farm scenario
now exercises all four meadow rotations, valid young stems/real dirt gaps,
actual native age-seven fruit growth, untouched repeat stock and protected-gap
refusal, plus six/five/four generation-version saves and old meadow crop
selection. Focused layout checks cover footprint/unique supports/water, both
corner variants, stem-to-gap adjacency and unchanged other regions. Actual
cell/farm and guide previews are inspected. Natural terrain and long growth
cycles remain gameplay review.

## L32: trough work roll call

Sneak-Calipers on a trough explains selected-mode worker eligibility without
performing searches or changing residents/food. `RESIDENT_WORK.md` L32 gives
its bounded loaded-only scope, reasons and native evidence. All 61 native
cases pass, including actual hand interaction and whole-state preservation.

## L33–L34: End care and supply stations

Bloom +20 chorus-compatible residents revive spent flowers for one fruit,
preserving living branch ages/support. See `FORAGING_SPECS.md` D10 and
`RESIDENT_WORK.md` L33. Supply mode lets Senses +20 adults stock nearby meal
troughs from ordinary surplus with an eight-food source reserve and destination
ceiling; native paired transactions preserve exact food through rollback and
container removal. See `RESIDENT_WORK.md` L34.

L14 correction: after reserving compost input, recheck the entire job, including
mode, Diet/Bloom/health/home, safe route and actual blocks. An interrupted job
refunds its exact food and starts no work rest; a real native compost attempt
still spends one food/rests even when its ordinary random chance fails. The
existing compost scenario now changes mode and Bloom from actual source-fill
notifications, retaining eleven named food/filter and zero compost/rest. Java
25 build/all 66 native cases pass, including all earlier work and food cases.
