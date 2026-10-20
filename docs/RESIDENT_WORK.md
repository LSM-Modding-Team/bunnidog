# Resident work L28–L35 / N9

These jobs extend the existing opted-in, healthy adult routine. Work needs a
claimed Nest, daytime, safe loaded routes, sight/reach, no danger/transport,
native mob griefing and a hoe-enabled, unpowered trough. Diet and exact food
components still apply. All jobs share the saved one-minute GardenWait; no
carried inventory or forced terrain loading is introduced. L35 adds one optional
keeper preference; job inputs and recovery retain their established saves.

## L28: choose a trough's work

Sneak-use any hoe to cycle a mode and enable work. Normal hoe use toggles work
without changing that mode. Either hand works, costs no durability and respects
Adventure/interaction protection. Redstone pauses resident access as before.

| Mode | Resident jobs |
|---|---|
| Mixed (default) | Existing harvest, cultivation, surplus compost and loose-food gathering |
| Garden | Harvest, cultivation, surplus replanting and loose-food gathering |
| Gather | Loose-food gathering only |
| Compost | Surplus composting only |
| Nursery | One-stage Cradle tending only |
| Supply | One surplus food to a nearby non-Supply feeding dish below eight |

Modes affect work, while food/healing, filters, stock capacity, native hoppers
and comparator behavior keep their existing rules. Empty-hand inspection names
the current mode and enable state. A native optional `WorkMode` string saves it
alongside existing Food/FoodFilter. Missing/unknown values use Mixed; old block
states need no migration and existing enabled troughs retain their old routine.

## L29: restore garden gaps

Bloom +10 adults in Garden mode can spend one real food while leaving at least
eight in the trough. They plant into loaded, dry, empty home terrain within the
existing work area, on valid native support. Foods that plant themselves are
carrots, potatoes, sweet/glow berries, mushrooms, Nether fungi, wart and cactus.
One plain guava establishes hanging fruit beneath leaves. One melon slice pays
for its ordinary one-seed recipe and a young melon stem. Wheat/beetroot need
their separate seeds and are not silently converted; upgraded guavas and embryo
seeds never qualify. Cactus work plants a base rather than extending columns,
and refuses a cell intersecting a living animal.

Harvest/cultivation takes priority. Planting pays no extra produce and preserves
the remaining source components/filter; native growth handles the new plant.
Explicit Garden mode keeps this food-spending action out of legacy Mixed work.

## L30 / N9: nursery tending

Bloom +20 adults in Nursery mode spend one ordinary guava above the same
eight-food reserve to advance a nearby occupied Nursery Cradle by exactly one
stage. Upgraded guavas are left for breeding. No quality, parents, stats, Mutagen,
name or other seed component changes. Growth wait resets as for normal stage
advancement. Ripe/empty Cradles refuse; workers never hatch the litter. Existing
player/redstone hatching, pad safety and incubation remain shared.

The worker captures the actual Cradle, exact whole seed and current age. A
replacement seed, changed age, missing block, lost Bloom, mode/power change or
unsafe route cancels the stale job. The native one-stage helper rechecks the
same seed/age and attachment atomically before changing incubation.

## Conservation and protection

Before spending, native grief/destroy hooks run and the job rechecks. Planting
also invokes the native mob placement gate at the target before writing, with
the same pre-placement snapshot convention as native mob placement. Recheck
again after that gate and after reserving stock, including callbacks caused
by stock/comparator notifications. A failed job returns the exact reserved
item to its original attached trough or drops it once if the source is gone
or cannot accept it. Success consumes it only after placing/growing. Eight
unspent food remain; no free plant, overwritten container or lost litter.

## Evidence

Java 25 build/all 59 native cases pass. Three new scenarios check real crouched
off-hand controls/cycling, normal pause, saved/legacy/unknown policies and
Adventure refusal; real claimed-home paths and Bloom/reserve/mode thresholds;
native placement refusal/container replacement/source-removal conservation;
one-item named carrot/guava planting and shared rest; cactus occupant and embryo/
upgraded-food exclusions; nursery Bloom +19/+20, reserve/tier refusal, a seed
swapped during real stock notifications, exact one-stage cost with unchanged
whole named treated litter, and explicit ripe hatching. Existing work, care,
automation and nursery scenarios remain passing. The guide preview uses a
substitute font; natural worker cycles and client controls need gameplay review.

## L32: read-only trough roll call

Sneak-use Calipers on a trough, in either hand, to show its selected mode,
enable/pause state, exact stock/filter and nearby residents. Count loaded live
ordinary bunnidogs in an eight-block sphere, with up to eight rows and eligible
workers first. Pups, unhealthy/homeless/resting/outside-home animals and trait/
Diet failures get their ordinary reason. Gather checks Senses/capacity/ordinary
food; Compost checks Bloom/surplus/value; Nursery checks Bloom/plain guavas;
Garden/Mixed account for available stock space or supported spending jobs.
Ready means eligible to search, not a guarantee of a target or route.

The report creates no paths, POI claims, tags or persistent state, and never
requests missing terrain. Power reads guard the native two-block conductor
area; unknown boundary power is conservatively shown as paused. Native chat
components use the current language; no screen or payload is added.

Java 25 build/all 61 native cases pass. A new native case uses real off-hand
Calipers, checks selected nursery/gather trait and pup eligibility, power and
work pause, and compares whole stock/filter/mode saves, animal NBT, tag,
navigation and tool before/after inspection. Remote requests leave chunks
unloaded. Guide preview inspected; client chat display remains gameplay review.

## L33: End-garden specialists

Bloom +20 workers with a chorus-fruit Diet revive only supported spent chorus
flowers and bank one fruit beside enabled Mixed/Garden troughs. Native living
branch ages stay unchanged; each job shares normal garden rest. Injured matching
adults use the same stock-preserving rule for a direct meal. See
`FORAGING_SPECS.md` D10 for mechanics and evidence.

## L34: supply nearby feeding troughs

Supply is an explicit sixth work mode, appended after Nursery. Healthy adult
residents at Senses +20 move one ordinary Diet-matching food from its source
above an eight-item reserve into a nearby non-Supply trough below eight food.
The destination need not enable gardening: it can be a dedicated meal dish.
Both troughs must be unpowered, exact-component compatible, inside the same
claimed home area and within four blocks of each other. Upgraded guavas stay
for breeding. No inventories other than the two actual troughs are touched.

A complete dry loaded native route must reach a clear standing place within
three blocks of both dishes, with sight to both. The existing one-minute garden
rest and full-health/daytime/home/Diet/griefing/danger/transport rules apply.
Capture both block-entity identities, the home and one exact food template;
recheck mode, stock, filters, power, animal and path before commitment and after
the native mob-griefing callback. A native transaction extracts/inserts exactly
one item together; abort restores both. Notifications follow committed stock,
so a callback removing a trough drops only its actual post-transfer contents.
There is no in-flight item, new animal save state or new network payload.

The work roll call explains the Senses threshold, ordinary food and surplus
requirements. Like its other modes, Ready means eligible to search, not a
promise that a destination or route exists. Source Supply troughs are not
destinations, preventing two supply stations from bouncing their reserves.

Search reads at most twelve nearby loaded troughs, tries at most two eligible
sources and eight native paths. Its bounded approach prevents busy settlements
from making a route search per dish pair. Source/destination positions and
current identities remain checked throughout the job.

Java 25 build/all 66 native cases pass. Two new native scenarios verify Senses
+19/+20 and census readiness including a full source, exact filters, both power
pauses, legacy Mixed and Supply-destination exclusions, source reserve, upgraded
guavas, native mode/stock saves, paired rollback and destination-refused rollback,
real complete routes, exact named transfers/shared rest and the eight-food
ceiling. Real protection callbacks change mode/filter before transfer; real
post-commit stock notifications remove both dishes, returning exactly eleven
named food with no ghost stock. The guide preview is inspected; natural supply
loops and client controls remain gameplay review.

## L35: pause one resident's work

Sneak-use a hoe on an ordinary living bunnidog to pause/resume its chores. Either
hand works with build permission and no tool wear or item cost. This keeper
preference applies to harvesting, cultivation, composting, gathering, planting,
nursery and supply work through their shared gardening status. Existing jobs
recheck it, including after protection and stock callbacks, before spending.
Meals, breeding, home tickets, native care/rest, shelter and guard preference
continue under their existing rules. This is a work preference, not a stay or
follow command. Pups can be paused before adulthood.

The optional animal `WorkPaused` boolean saves only when true; missing means
normal work. Whole-animal Travel Baskets retain it. It is not inherited in
seeds or included in genetic sample/record snapshots. Sneak-Calipers and trough
work roll calls show the paused status without resuming the animal or spending
its care/work timers. No client entity-data slot or network payload is added.

Java 25 build/all 68 native scenarios pass. The new case uses actual crouched
creative off-hand hoe control and Adventure refusal, interrupts a ready real
harvest without stock/rest/home changes, checks paused census and supply,
performs a real two-HP trough meal, saves/restores the preference and legacy
default on reused entities, travels through a real basket with paused food rest,
resumes by hoe and sets a pup's future preference without growth. The guide
preview is inspected; natural work resumption and client controls need playtesting.

## L36: shore gardens

Healthy housed shore-forager residents trim spare upper kelp into enabled
nearby troughs, retaining the last growing root and restoring native water.
Garden-mode Bloom +10 workers can also spend one actual kelp to plant a bare
valid bed beneath two blocks of still water, keeping eight in reserve. Mixed
permits harvesting; paid planting remains an explicit Garden-mode choice.

The shared search extends two blocks below a trough to reach shallow beds.
Workers approach from loaded dry banks, preserving ordinary home/hazard paths,
filters, pauses, mode, real protection callbacks, exact cancellation refunds
and garden recovery. No aquatic domestic route is added. Native harvested
roots, real paid planting, reserve/refusal/rollback and wounded direct meals
pass in the combined 95-case checkpoint. See `ISLAND_SPECS.md` for the complete
D13/L36 contract and generated tide-garden source.
