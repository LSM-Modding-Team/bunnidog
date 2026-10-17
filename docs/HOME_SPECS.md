# HOME_SPECS — bunnidogs find their own home (session 41, second turn)

**CURRENT (2026-10-17): H1-H4 have prior gameplay reports. U1–U7 put Nests in fitted dens/halls; H3b keeps a Nest in each built house. H5 adds transport-aware behavior and player-directed Nest reassignment. H6 adds a Travel Basket preserving an animal's identity and native care state. Current server evidence and remaining gameplay review are in START_HERE.md.**

**Status (session 44): H1-H4 WORK (user). H3b (nests in houses) is plan only.** This REPLACES the hardcoded home of V2a (`restrictTo(treeFloor, 16)` at spawn),
which the user rejected: "it should work like it does with villagers, some conditions that make it determine its home instead of a
hardcoded location; introduce modded mechanics if more natural". Everything about dogs staying near home in `docs/FIXES_PLAN.md` 5.3
is superseded by this file; the rest of that plan (wood, villages, diets) is unchanged.

## 1. The idea in one paragraph

A villager has no coordinates written into it. It has a *need* (a bed), the world offers *points of interest* (beds), and the villager
claims a free one by a few conditions, remembers it, and gives it up when it is gone. A bunnidog does the same with a new block, the
**Nest** (a woven straw nest on a guava-plank base). Nests come from three places: the player (craft and place them, so a tamed line
can be penned), worldgen (burrow entrances and the houses of a warren village get nests, so wild dogs find homes by themselves), and
nothing else. A dog with a claimed nest strolls within 16 blocks of it and walks back when something took it further; a dog without
one looks for a free nest.

## 2. The rules (the "conditions", all decided here, changeable by the user)

| # | Rule | Villager equivalent |
|---|---|---|
| R1 | Only an ADULT looks for a home. Pups follow a parent (the existing goal). | children do get beds in vanilla, but our pups already have a job (follow) |
| R2 | A homeless dog looks about every 100 ticks (+ random 0-40, so a herd does not all look on the same tick), server side only. | sensor interval |
| R3 | It looks for a FREE nest within 24 blocks (`SEARCH_RADIUS`) of itself. "Free" = the nest has an unclaimed ticket (`maxTickets` = 1: one dog per nest). | `PoiManager.take` for beds |
| R4 | The nest must be REACHABLE by the dog's path finder (not behind a wall or across a lake); if not, the ticket is given back at once and the dog tries again at the next look. | villager `canReach` check |
| R5 | A claim remembers the nest position (our own saved `NestX/Y/Z`). A dog more than 16 blocks (`HOME_RADIUS`) from it walks back until it is within 8 (`RETURN_RADIUS`) (`GoHomeGoal`). *(Session 42: the first plan used vanilla's `restrictTo`, which does not exist in this version.)* | the HOME memory |
| R6 | A dog with a home checks every ~200 ticks that the nest block is still there. If not, the home is cleared (the point of interest vanished with the block, its ticket with it) and R2 starts again. | memory expiry |
| R7 | When a dog DIES, it gives its ticket back, so the nest is free for the next dog. Unloading a chunk does NOT give it back (the dog still owns it). | `releasePoi` on death |
| R8 | (H4) At dusk an adult with a home walks to it and stays within 2 blocks until dawn; nothing else changes. Rain does not count. | the bedtime schedule |
| R9 | (H5) Leads, pending saved leads and vehicle rides suspend home/food goals and Nest scans/checks, retaining the home. Normal behavior resumes after release. Unloaded home chunks are skipped during block validation. | player-directed travel |
| R10 | (H5) A player right-clicking a Nest with an empty hand or a lead can rehome the nearest adult directly leashed to them, within 8 blocks of the Nest. The destination must be reachable and free. Secure its POI ticket before releasing the previous one; failure preserves the old home. | explicit reassignment |

Known limit, same as vanilla beds: a dog removed by a command that is not a kill (`/data`, `discard`) leaves a stale ticket, and its
nest stays "taken". Breaking and re-placing the nest fixes it. Not worth code now.

## 3. Where nests come from (worldgen, H3)

* **Burrow entrances:** each burrow hole of a warren gets a Nest on the ground next to it (a new pure `WarrenShape.nests()`, tested:
  never on the trunk column, a burrow, a dog spot or a low fruit). A warren has 3-4 burrows and 2-4 dogs, so every dog finds one.
* **Houses (villages):** each burrow house gets one Nest inside (H3b, after V1 changes the houses; until then only the entrances).
* **The player:** crafts it: 3 wheat over 3 guava planks -> 1 Nest. Any dog within 24 blocks without a home claims it.
* No dog is given a home at spawn any more.

## 4. Chunks (one per turn; the user compiles in between)

| Chunk | What | New Minecraft API |
|---|---|---|
| **H1** (coded) | The Nest block (plain block, texture, model, recipe, loot, tags, lang, creative tab), the POI type `bunnidogs:nest` (`maxTickets` 1, `validRange` 1), registered in the mod entry; the V2a spawn-time home is REMOVED (the go-home goal stays, idle for now). Pure constants in `genetics/Home`. | `PoiType` (constructor) + `DeferredRegister` on `Registries.POINT_OF_INTEREST_TYPE` |
| **H2** (coded, session 42; a timer-driven `NestHome` instead of a goal: finding a nest is an instant action, not a behaviour) | `FindNestGoal` and the rules R1-R7 in `Bunnidog` (pure decisions in `genetics/Home`, tested): claim with `PoiManager.take`, `restrictTo`, validate, release on death. After H2 a player-placed nest already works end to end. | `ServerLevel#getPoiManager`, `PoiManager#take` / `release`, `PathNavigation#createPath` + `Path#canReach` |
| **H3** (coded, session 43) | Worldgen nests (`WarrenShape.nests()`, `WarrenFeature.buildCluster` places them). Entrances only; the nests inside burrow houses are H3b. | none |
| **H4** (coded, session 44) | Night: R8 (`NightHomeGoal` sends an adult home at dusk and keeps it there until dawn). | the world clock (`dimensionType().defaultClock()`, `clockManager().getTotalTicks`), SOURCE |

## 5. Left out on purpose

Dogs "sleeping" (no pose in the model), claiming by smell of other dogs, nests with an inventory, a leash/owner link, and sharing a
nest between a mated pair (`maxTickets` 2 is the one-number change if the user wants it).

## 6. API facts (see also API_NOTES, QUICK INDEX)

* NeoForge 26.2.x supports registering a modded `PoiType`: its registry add-callback (`NeoForgeRegistryCallbacks.PoiTypeCallbacks`) fills the
  block state -> type map for every state in `matchingStates`, and throws if one state is in two types (SOURCE).
* The registration order is vanilla's (`GameData.getRegistrationOrder`), where BLOCK comes before POINT_OF_INTEREST_TYPE, so the POI
  type's supplier may read `ModBlocks.NEST.get()` (SOURCE for the order mechanism).
* `PoiType` is a record whose compact constructor wraps `matchingStates` (SOURCE: NeoForge patch); the other two components,
  `maxTickets` and `validRange`, and their order, are from memory of 1.21.x (ASSUMED).

## 7. What H2 did differently from the plan (session 42)

* **No goal class:** `entity/NestHome` is ticked from `Bunnidog.tick()`; the walk home stays the vanilla `MoveTowardsRestrictionGoal` of V2a.
* **Own save field:** the claimed nest is saved as `NestX/Y/Z` by us (the ticket owner is never in doubt); vanilla's `home_pos` is set at the same time for the stroll limit but never read back.
* **Reject list (new, small):** a nest found unreachable (R4) is skipped on the next looks (up to 8 remembered, then cleared), because `take` would otherwise hand out the same unreachable nest every time.
* **Reach test** targets the block above the nest (`createPath(nest.above(), 1)`), since the nest block is solid.
* **Not done:** release on despawn or `/kill`-less removal (the known limit above), and a re-placed nest inside the 200-tick check window.
* **No vanilla home (session 42, after the first compile):** `Mob#restrictTo` / `clearRestriction` were not found. The dog's home is only our `NestX/Y/Z`; `GoHomeGoal` calls it back beyond 16 blocks (to within 8). The stroll itself is no longer limited by vanilla, so a dog can wander about one stroll past the radius before turning back.

## 8. What H3 did (session 43)

* **Which column:** for each burrow, in order, the free orthogonal neighbour nearest the trunk (ties by the shuffled order). Free = not the trunk or any column where the tree comes within 2 of the ground (the dog stands on the nest, so it needs two free blocks above it), not a burrow, not a dog's spot, not another nest, at most 3 columns (larger of dx, dz) from the trunk.
* **Why 3:** a village's landmarks keep their centre 5 columns from every trunk (footprint 4+) and houses 6, so a nest can never be overwritten or block them. Paths only replace grass or dirt, so they leave a nest alone.
* **Order in `buildCluster`:** patch, tree, burrows dug, NESTS, dogs, fallback dogs (which skip nest columns), so no dog is ever put where a nest goes.
* **Count:** at most one per burrow (3-4); a warren has 2-4 dogs. Over 600 samples every warren had at least 2 nests. On steep ground a column can have no ground and gets none.
* **Nothing new at spawn:** the dogs get no home when they are made; the H2 timer finds the nest within about 5-7 seconds of their first ticks (24 blocks is far more than a warren).
* **ASSUMED:** a block set during world generation registers its point of interest, as village beds do. Test: on a naturally generated warren, `/data get entity @e[type=bunnidogs:bunnidog,limit=1,sort=nearest]` shows `NestX/Y/Z`. If only `/bunnidogs warren` ones do, the generation path is the cause.

## 9. What H4 did (session 44)

* **Time source:** `isDarkOutside` / `getDayTime` were never seen in 26.x (the clock system replaced day time). The code reads the dimension's default clock: `level.clockManager().getTotalTicks(level.dimensionType().defaultClock().get())`, then `Home.isNight(ticks)` (pure: tick 12500 to 22999 of a 24000 day). Rain and thunder do not matter (R8). The Nether and the End have no default clock, so their dogs never have a night.
* **The goal:** `entity/NightHomeGoal`, MOVE flag, priority 6, registered before `GoHomeGoal`. It starts when the dog is an adult, has a nest and it is night; it keeps running all night (so the stroll at 7 cannot move the dog), walks to the nest top, then just stands within 2 blocks. Panic, avoid, melee, breeding and tempting (priorities 1-5) can still interrupt it; afterwards it starts again. It ends at dawn.
* **A dog already walking home** (GoHomeGoal, same priority) finishes that walk first; the night goal takes over once it stops.
* **Path failures:** a dog that cannot find a way home retries every 20 ticks, not every tick.
* **Not done:** a sleeping pose (no pose in the model), pups (they follow a parent), dogs without a nest (they keep strolling at night), light level (a dog in a dark cave by day is not sent home).
* **ASSUMED (runtime):** one clock day is 24000 ticks and total tick 0 is sunrise. If dogs go home at the wrong time of day, change `DUSK` / `DAWN` in `Home` (one place).
* **Test:** `/time set 13000` (night): an adult with a nest walks to it and waits; `/time set 1000`: it strolls again. If the dog does not react, check `/time query daytime` against the numbers above.

## 10. What H3b did (session 44, fourth turn)

* **Where:** `WarrenFeature.buildHouse` sets `ModBlocks.NEST` on `base.below()`, the floor block under the hollow centre, after all cells are placed. The nest REPLACES the floor (the way H3 puts a nest on the ground), so the dog stands in the hollow, not under the roof: the hollow is 2 blocks high in a mound house (roof at dy 2) and 3 in a tower or a loft house; the doorway is 2 high in every form and tier.
* **Which houses:** every house that `buildHouse` builds, so burrow houses in `/bunnidogs warren`, natural warrens and structure pieces (`WarrenPiece` calls the same method). No new pure logic, no save-format change; houses already saved are untouched (only NEW villages change).
* **Count:** one per house (`maxTickets` 1), so a village of level 5 offers its entrance nests plus the houses' nests; a free dog within 24 blocks claims the nearest FREE nest the path finder can reach (R3, R4).
* **ASSUMED (runtime):** a dog fits through the doorway and stands on the nest block; the reach test targets the block above the nest (`nest.above()`), which is the hollow's lowest air block. If dogs never claim house nests, look at R4 first (the doorway may be too narrow for the path finder), then at the POI registration during worldgen (H3's assumption).
* **Left out on purpose:** a bed-like furnishing, a second nest per house, a nest per tower floor.

## 11. Nests in the dens (U1 and U2; current build passes, gameplay pending)

* The 3-4 nests of a warren stop standing beside surface holes (H3's `WarrenShape.nests()`) and stand on the floor of the **den** under the tree (a new pure `DenShape.nests()`). A house keeps its own nest in the hollow (H3b). Player-placed nests are unchanged.
* **Rules R1-R8 do not change.** R3/R4 matter more: a free dog on the surface must find a nest within 24 blocks and the path finder must reach it down the mouth's slope (1 step per block, 3 high). If no dog ever gets `NestX/Y/Z` after U1, look at R4 first.
* H4 (night) then shows the warren: at dusk the surface empties as the dogs walk down their mouth; at dawn they come out. No new behaviour is needed.
* If a den does not fit the ground, its warren keeps the surface nests of H3 (rare fallback, WARREN_PLAN section 7).


U2 extends den Nests to newly planned level 2–5 surface settlements. A retained
den has a terrain-tested route through the tunnel network to at least one
fitted entrance; otherwise its cluster retains surface Nests. Three or four
Nests replace the den floor's corner blocks. House Nests and H2/H4 rules are
unchanged. Pure traversal checks establish player-sized geometric access,
not Minecraft navigation or successful POI claims.

U3 adds covered cellar ramps beside fitted house doorsteps, joining the retained
underground network. It preserves each house's centre-floor Nest and front-door
access; house claiming and night goals use their existing rules. Surface paths
skip saved ramp columns. Check both house Nests and den Nests in a newly
generated village; pure traversal checks are not confirmation of navigation.

## H6: Travel Basket

Craft six any planks, one leather and one lead with `P P / PLP / PSP` (P plank,
L leather, S lead). The reusable basket stacks to one and is in Tools & Utilities.
Getting a lead unlocks the recipe. Use an empty basket on a calm ordinary adult
or pup to pack it; filled baskets have an occupied name, foil and occupant
tooltip. The Apex, dead animals, leads/connections, riders/vehicles, active combat,
recent attacks and refuge are refused. Ordinary build permissions apply.

Packing removes that existing animal without death drops and stores its full
native data in the vanilla `BUCKET_ENTITY_DATA` component with a version marker.
UUID, genome, bred stats, health, age/breeding rest, parents, Ear Tag, name,
awards, attributes and saved care/food/work recovery travel together. Native
remaining timers and age pause while carried, as during unloading. The basket's
own name/components are retained. Creative also changes the real held basket;
it neither leaves a second animal nor produces a spare filled carrier.

The old Nest ticket is released immediately and omitted from the carried data.
Another resident can claim it; a released adult looks for a new home normally.
Release uses the POI storage rather than requesting the old home's terrain
chunk, also improving remote ticket release on death. Leads/rides still use
H5's existing keep-home behavior; basket travel deliberately relinquishes it.

Use the filled basket on dry solid ground with clear space for the animal's
actual dimensions. Check loaded bounds, support, fluids, block/entity/world-border
collision and native spawn acceptance before emptying the basket. Blocked,
unloaded, invalid or refused placement retains the entire payload. An already
present UUID in any loaded dimension is refused. Release preserves health and
remaining recovery without growth, healing, cloning or home-ticket theft;
old movement/fall distance is cleared for safe placement. No biome reroll occurs.

Native item save/load carries all components. Calipers opens the normal animal
sheet from the other hand, including genome, stats, UUID, parents, Ear Tag and
age class, without unpacking or changing the payload.
Malformed occupied data is retained with feedback. Dispensers do not pack/release
animals. No custom data component, payload or protocol change is needed. The
39-page Field Guide and `art/gen_basket.py` own the controls/icon/recipe text.

H6 evidence (2026-10-17): Java 25 `build runGameTestServer` passes all 27 required
native scenarios. `BasketGameTest` uses actual creative/survival interactions,
native item codec round-trips, blocked/duplicate/invalid release, a pup, exact
identity/line/parents/tag/name/health/age and all six recovery timers. It checks
the exact free POI ticket immediately, then another grounded resident claims
it before release; a fresh spawn cannot ground-pathfind before physics ticks.
The item and guide previews are inspected. Client use/tooltip rendering and
long-distance travel remain gameplay review.

## H7: Nest wayfinding and inspection

Use an ordinary compass on a Nest to mark its position in that dimension.
The native bound-compass needle points to it there and spins in other dimensions.
This is a persistent place marker: removing the Nest or changing its resident
does not erase the destination. Rebinding a compass replaces the previous
target. Names and other components survive; stacked compasses bind one item,
with inventory overflow dropping normally. A single creative compass is changed
in place. Binding consumes no Nest or other resource and respects build/use
permissions. Native item components carry the marker through saves; no new item,
recipe, block entity or payload is needed.

Use a Nest with both hands empty and no animal on your lead to inspect its
ticket. A free Nest says so; a claimed Nest names its loaded nearby resident,
or explains that the resident is away. Inspection never takes/releases a ticket,
requests terrain, changes a home or redirects an animal. Existing nearest-led
adult rehoming and its pup/occupied/unreachable feedback keep priority.

H7 evidence (2026-10-17): actual Java 25 build and all 29 native scenarios pass.
The marker case uses server game-mode interactions, free/claimed ticket checks,
stack conservation, named off-hand rebinding, native item save/tick after Nest
removal, single creative binding, denied permission and full-inventory overflow.
The guide preview is inspected. Native compass rendering and long-distance
multidimension travel remain client gameplay review.

L13 adds one bounded exception to H4's night waiting: an injured adult already
within four blocks may eat from a safe nearby trough using real stock/shared
meal rest. Its whole route stays within four blocks; afterward the ordinary
two-block night tether resumes. See `SETTLEMENT_LIFE.md` L13.

S9 lets Pedigree Books and Purebred Registries record a filled basket's occupant
through deliberate sneak-use with the basket in the other hand. This reuses
the existing identity snapshot without unpacking or changing saved animal
state. Missing UUIDs are refused by sheet/record reading as well as release.
See `SHEET_SPECS.md` S9.
