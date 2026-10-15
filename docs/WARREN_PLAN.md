# WARREN_PLAN: readable warrens — U1, U6a, U2 and U3 implemented

**Historical plan acceptance (session 45, third turn): ACCEPTED. The user said "good, integrate it into the documentation"; no pick of section 9 was changed, so every bold pick stands (they can still change any of them). U1/U6a are recorded below; U2/U3 are implemented in sections 14–15.** Chunks U1-U7 (section 8). Test command stays `/bunnidogs warren <level> surface`.
Supersedes the burrow-hole part of Phase 4g/6b (`WarrenShape.burrows`, `WarrenFeature.dig`); everything else (levels = trees, houses, paths, landmarks, styles, nests, night) stays.

## 1. The problem (user, session 45)

> "i didn't even know what 'warren' meant, and i thought it was bugged and generated with random erosion-like 2-block-deep holes."

At plan acceptance, the existing implementation generated: 55% of all settlements are level 1 = ONE guava tree, a disc of coarse dirt, **3-4 holes that are one block wide and two deep**
(`WarrenFeature.dig` removes the ground block and the one under it), a Nest beside each hole and 2-4 dogs standing around. A hole of that size is a dead end with nothing in it.
It looks like a worldgen bug because nothing says "somebody lives down there", and the word "warren" appears nowhere in the game itself (only in `/bunnidogs warren`, `/locate`).
Higher levels have houses and landmarks, but the holes are still just holes, and the houses are closed one-block rooms that lead nowhere.

**Root cause: the idea lives in the docs and the code, not in the world.** The fix is not a tooltip; it is making the thing itself readable, and then adding one or two cheap in-game words for it.

## 2. The idea in one paragraph

A **warren** is where bunnidogs live: the **dens** under the guava trees, the **mouths** on the surface that lead down into them, and the **tunnels** between dens. A **settlement** (levels 1-5) is a warren plus whatever it has built on top (houses, paths, landmarks).
Three rules make it readable on sight:
1. **Door, slope, room.** Every entrance is a visible doorway into a mound, with a slope going down into the dark. You can walk in. Nothing is a bare hole.
2. **What is above has a counterpart below.** Each tree has a den under it; each footpath has a tunnel under it; each house has a cellar; the well is also a cistern. Dig anywhere and you find the other half.
3. **The dogs use it.** Their Nests are in the dens, so at night (H4) the surface empties and at dawn they come out of the mounds. That is the strongest "warren" signal there is, and it needs no new behaviour: only the nests move.

The warren is **both underground and on the surface** (the user's words): the surface is the village you see, the underground is the home you find.

## 3. The ways up and down (surface <-> underground)

| Connection | What it is | From level |
|---|---|---|
| **Mouth** | a 3x3 earth mound with a 2-high doorway; inside, a slope of steps goes down toward the den | 1 |
| **Cellar ramp** | a house's own slope down, starting at its doorstep (so the house is the surface home, the cellar the den) | 2 (houses) |
| **Cistern shaft** | the well also drops to a water-filled room below (water only; a dry side ledge lets you reach it) | 3 |
| **Back door** | a mouth at the edge of the farm plot, so the harvest goes straight to the pantry | 5 |
| **Skylight** | a 1-block opening in a den ceiling covered by a glass pane / leaves with a lantern under it, seen from above as a glow at dusk | 3+ |

Dogs and players use only mouths, ramps and (for the cistern) nothing: dogs never climb ladders, so **no ladders anywhere**; every way down is a slope of steps.

## 4. The parts (sizes in blocks; dy 0 = first air block above the ground, the surface block is dy -1)

* **Den** (under every tree, from level 1): a room 5x5 wide and 3 high, standing level dy -5 (interior dy -5 to -3, so two solid blocks of ceiling under the grass). The trunk's own column is not dug at dy -1 (the tree stands on it).
  Contents: the dogs' **Nests** (3-4, on the floor, one per burrow as today), a hay bale or two, moss carpet, one light source (see D3), walls and ceiling lined with the lineage's wall block (coarse dirt in the meadow; sandstone, packed mud, stone, nether brick, end stone... = the existing `WarrenPalette` roles) so a loose block or a fluid never spills in.
* **Slope** (mouth to den, den to den): 1 block wide, **2 high**, one step down per block of length (full blocks, no stairs needed; vanilla animals climb a 1-block step by jumping, ASSUMED for our dog), so a mouth's slope is ~5 long. Sized for a 0.6-wide dog and a 0.6x1.8 player.
* **Tunnel** (den to den, level 2+): the same 1x2 section, level, at the den floor height, following the same line as the footpath above (PathShape's spokes) so what is above matches what is below. One light source about every 8 blocks.
* **Hall** (level 5, under the grand tree): 7x7, 4 high, hanging roots as the ceiling look, several lanterns, the village's nursery (a ring of Nests). Joined to all four dens.
* **Pantry** (levels 3+): a 3x3 side room off a tunnel with barrels and hay (decoration; guava storage is for D1-D4 diets to use later).
* **Mouth** (surface part): reuses the idea of `HouseShape` (3x3 mound, 2-high doorway) but as a new `Kind` set `MOUTH`, because a house is closed and a mouth opens into a slope. At level 1 it is plain earth; the style (tower of sandstone in the desert, etc.) follows `HouseShape.Form` and the palette like houses do.

## 5. By level (proposal; the user changes any row)

| Level | Trees | Underground | Surface connections |
|---|---|---|---|
| 1 "den" (55%) | 1 | one den under the tree, 3-4 nests | 2 mouths (instead of 3-4 holes), a worn trail from each to the tree |
| 2 | 2 | 2 dens + a tunnel joining them | the 2 houses get cellar ramps into the tunnel; mouths |
| 3 | 3 | + a pantry; tunnels mirror the footpaths | + well/cistern shaft, skylights |
| 4 | 4 | + the market's cellar (barrels, a lantern) | + stall cellar hatch |
| 5 "village" (2%) | 5 | + the hall under the grand tree, all dens joined (a ring) | + back door at the farm; everything above joined |

"Absurd escalation" (the user's taste) lives mainly here: the hall and, if the user wants, a second floor below the hall (U7, optional).

## 6. Making it readable (cheap, in order of value)

1. **The mouth itself** (door + slope + a lit dark inside; section 4). Light inside means you can see the slope from outside at dusk.
2. **Trails**: a worn path (coarse dirt / path blocks, the existing patch/path code) from each mouth to the tree.
3. **Dogs go in and out** (nests in the dens; H4 night rule unchanged).
4. **One advancement, data only** (no code): "Warren: Bunnidogs live in dens under their guava trees. Walk into a mound and follow the slope." Triggered by entering the structure (`minecraft:location` with the structure predicate). Works for the surface structure only (cave/Nether warrens are Features, no structure id; D4). The wording is the only place the word "warren" is explained in-game, plus the lang name of the structure for `/locate`.
5. **Docs/Pedigree Book**: no change now (the book already says where a dog was born); a "Field Notes" page is an idea, not planned.

## 7. How it fits the code (constraints already known)

* Pure layout in `layout/` (new `DenShape`, `TunnelShape`, `HouseShape.Kind.MOUTH`), self-checked; the world code stays thin. **Self-check targets:** every Nest reachable from every mouth along cells with 2-high clearance and at most 1 step between neighbours; no den/tunnel cell inside a trunk column or a house footprint; everything within `Settlement.MAX_EXTENT` (12) horizontally and a fixed depth; level N has exactly the rooms in section 5.
* **Structure pieces** (`WarrenPiece`) build everything in ONE call at their anchor chunk and may reach about 6 blocks (the path piece 12). A den (reach 2 + slope 6 = 8) is one `CLUSTER` piece job; the tunnels are a new piece kind laid **column by column in the chunk that owns the column**, exactly like `PATH`. The piece bounding boxes go `BELOW = 10` blocks down: enough for dy -8; a deeper hall needs `BELOW` raised (saved structures keep the old box, so only new ones change).
* **Nothing saved changes** for the layout (it is rebuilt from the seed); worlds generated before keep their old holes (only new chunks change), same policy as V1.
* **Fit test before building**, like `buildHouse`: every cell must be replaceable ground, no fluid and no block entity in the shell; if a room does not fit, that room (and the slopes that lead only to it) is left out; a level-1 warren whose den does not fit keeps the tree, the patch and the dogs, and gets its nests on the surface beside the mouths, as today (rare). Caves and aquifers: air is allowed (it just joins); water or lava anywhere in or next to the shell cancels that room.
* **Light** keeps hostile mobs out of the dens (spawning needs no block light; ASSUMED for this version, but one light per room and per ~8 tunnel blocks fixes it either way). The light source follows the style (D3).
* **Nests**: `WarrenShape.nests()` today puts a nest beside each hole; new `DenShape.nests()` puts them on the den floor. The H2 claim, the reach test (R4) and H4 night rule are unchanged. A dog must be able to path from the surface into the den; if R4 rejects every nest the first test shows it at once (`/data get` has no `NestX`).
* **Cave, Nether and End warrens** are worldgen Features (they search for a floor underground), not structures. Their floor is already below ground, so a mouth makes no sense there; see U6.
* New Minecraft names expected: none beyond blocks already used (`Blocks.HANGING_ROOTS`, `MOSS_CARPET` and `HAY_BLOCK`: block constants, ASSUMED to exist under those names; the advancement JSON syntax is the other risk). Only data errors, no API.

## 8. Chunks (one per turn; the user compiles in between)

| Chunk | What | Size / risk |
|---|---|---|
| **U1** | Level 1 only, surface kind: replace the 3-4 holes with 2 mouths + one den; nests move to the den; the old `dig` is removed; patch/trails. Pure `DenShape` + tests. Test: `/bunnidogs warren 1 surface`, walk in, see nests, wait for dogs to claim them | medium; the biggest value (55% of all warrens) |
| **U2** | Joined dens and tunnels for levels 2-5 (new `TUNNEL` piece kind, column by column), lights | medium |
| **U3** | Cellar ramps for houses (the doorstep slope, joined to a tunnel) | small-medium; must not fight the path code at the doorstep |
| **U4** | Under the landmarks: pantry, cistern shaft under the well, stall cellar, back door at the farm, skylights | medium |
| **U5** | Hall under the grand tree (level 5) | small |
| **U6** | Advancement + lang names (data only) and the cave / Nether / End variants (den cut into the cave floor, no mouth) | small |
| **U7 (optional)** | A second floor below the hall (the absurd end of the ladder) | the user decides |

Order if the user says nothing: U1, U6 (the advancement part), U2, U3, U4, U5, U6 (variants). U1 alone already removes the "erosion holes".

## 9. Decisions (ACCEPTED as proposed, session 45 third turn; my pick in bold; the user may still change any)

1. **D1 Mouths:** replace the holes with mound mouths + a slope (**yes**) / keep holes but give them a doorway frame.
2. **D2 Where the nests go:** in the den (**yes**, dogs sleep underground) / stay on the surface beside the mouths.
3. **D3 Light in the dens:** one lantern per room in every style (**yes**, the block the level-5 plaza already uses) / glow lichen in the meadow and lanterns only in villages / none (hostile mobs may spawn).
4. **D4 Advancement** for the surface warren (**yes**) / no.
5. **D5 Cave, Nether, End warrens:** later, as in U6 (**yes**) / do them in U1.
6. **D6 Depth and size:** den 5x5x3 at dy -5, slope 1x2, hall 7x7x4 (**as written**) / bigger.
7. **D7 U7** (a second floor under the hall): **later, only if the user asks**.

## 10. Left out on purpose

Ladders and vertical shafts for dogs; loot chests in the dens (a village-style loot idea is open in DESIGN); hostile mobs or traps in the tunnels; the dogs digging new tunnels (settlement life, the old 6d); changing the level odds or the number of dogs; redoing worlds already generated.

## 11. What this plan changes in the other docs (done in session 45, third turn)

| Doc | What it says now |
|---|---|
| `START_HERE.md` | status block: plan accepted, next chunk U1; vocabulary line in section 1 (what a warren is) |
| `docs/ROADMAP.md` | U1-U7 are the first planned items |
| `docs/CHUNKS.md`, `docs/history/CHUNKS_FULL.md` | UW0 (this plan) ACCEPTED; U1-U7 PLANNED |
| `docs/HOME_SPECS.md` | section 11: from U1 the worldgen nests are in the dens (R1-R8 unchanged); H3b's house nest stays |
| `docs/FIXES_PLAN.md` | pointer: houses/villages continue in WARREN_PLAN (cellars U3, hall U5); V1c (doors) is unchanged |
| `docs/DESIGN.md` | "Warren redesign decisions" at the end; the old "not done: a hill, tunnels" note points here |
| `docs/FEATURES.md` | the player guide now says what a warren IS, and that the hole-style warrens are being replaced |
| `docs/REFERENCE.md` | user preference: a feature must be readable in the world, not only in the docs |
| `docs/API_NOTES.md` | ASSUMED rows for the new block names and the advancement predicate |
| `README.md` | docs list |

When U1 is built: update FEATURES (the mouths and the den), HOME_SPECS (nests), this file (what U1 did) and flip UW0/U1 in CHUNKS.

## 12. What U1 did (session 45, fourth turn; NOT COMPILED)

* **Files:** `layout/DenShape.java` (pure: den, mound, slope, furnishing), `worldgen/WarrenDen.java` (reads the ground, tests, then writes), `buildCluster` in `WarrenFeature` (den first; the old holes and surface nests only if no den was built), `WarrenPiece` (den at level 1, `REACH` 9), self-check group `den()`.
* **Where it applies:** the surface kind, rolled level 1 (the Feature and the structure piece alike). Level 2+ clusters keep the old holes until U2; cave, Nether and End kinds until U6b.
* **Changes from the plan, and why:**
  * **The slope is 3 high, not 2.** A one-block step needs the room above the lower column to jump (the self-check walks the slope with that rule); the hollow in the mound is 4 high at the back.
  * **The door faces AWAY from the tree and sits 7 blocks out** (8 or 9 on higher ground, `DenShape.canReachDen`): the slope has to drop 5 blocks at one step per block, and that run only fits if it starts that far out and ends under the den's edge. The mound is 4 long x 3 wide, so it never touches the crown (radius 2).
  * **No trails yet.** A path from the doorstep to the patch would have to go round the mound; left for later (WARREN_PLAN 6.2).
  * **The den's ceiling is lined** with the style's fill block (no sand or gravel can drop in); the slope's ceiling is lined only below the surface block.
  * **Nests replace the floor block** (like H3b), in the four corners; one lantern on a hay block, one more hay block for bedding.
  * **No new Minecraft names:** the hay, hanging roots and moss carpet ideas of section 7 were not used (`HANGING_ROOTS`, `MOSS_CARPET` are not needed).
* **Fit rules** (all tested before the first block is set): the den needs a solid floor and ceiling layer under its 25 columns, nothing fluid in or beside it, nothing harder than stone to dig; a mouth needs 12 grounded columns (at most 3 blocks apart, the low ones filled), a free mound and doorstep, and a covered slope with a solid floor and ceiling and no fluid beside it. Up to 2 sides are tried in a random order. If no side fits, or the den does not, nothing is changed and the old holes are built.
* **To check in game:** START_HERE "To verify with U1".


## 13. What U6a did (session 46; NOT COMPILED / NOT RUN)

* **Files (data only):** `data/bunnidogs/advancement/enter_warren.json`, three keys in `assets/bunnidogs/lang/en_us.json`. No Java.
* **What it does:** a task advancement "Welcome to the Warren" in the Adventure tab, granted when the player is inside a piece of a `bunnidogs:warren_village` structure; its description is the only in-game place that explains the word (dens under guava trees, walk into a mound, follow the slope).
* **Not covered:** cave, Nether and End warrens (Features, no structure id); the `/bunnidogs warren` command (a Feature too). Natural worlds or `/locate structure bunnidogs:warren_village` only.
* **Likeliest problem:** a field name in the JSON. The log names the file at pack load; deleting the file is safe.


## 14. U2: higher-level dens and tunnels (2026-10-15)

Implemented for newly planned level 2–5 surface-kind settlements, both natural
structures and `/bunnidogs warren <level> surface`. U1's level-1 implementation
and cave-kind features retain their behavior. U3 is implemented in section 15; U4–U6b remain planned. The grand
tree currently has an ordinary den, not U5's hall.

- `layout/TunnelShape.java` plans a lined 5x5x3 den five blocks below each fitted
  tree, joins the central room to satellites along the same long-axis-first
  spokes as `PathShape`, and fits up to two mouths per room. Entrances may shift
  one or two blocks sideways so they can fit between existing buildings without
  changing the surface layout. All writes and doorsteps stay within MAX_EXTENT.
- Room elevations follow the terrain at their trees. Connecting corridors have
  three-block clearance, use full-block steps with at most one block of rise,
  and are omitted if the elevation difference cannot fit in their available run.
  The short gaps (at most five columns) receive light from the adjoining den
  lanterns. Lanterns/bedding sit near the centre to keep jittered tunnel entries
  clear; Nests remain in the floor corners.
- Every room, corridor and mouth is tested against read-only terrain before
  writing. Fluids in or adjacent to the shell, protected blocks, unsupported
  floors/ceilings or blocked trunks reject that part. Only components connected
  to a fitted mouth survive. Rejected rooms keep the old holes and surface Nests;
  a fully rejected plan writes nothing. Rare dense surface layouts may leave no
  room for an entrance even on flat ground. Spare house candidates in the feature
  generator are skipped if they would close a fitted mouth or its doorstep.
- `worldgen/WarrenTunnels.java` adapts live terrain for the debug feature and
  cached base-noise columns for structure planning. `WarrenStructure` creates
  one TUNNEL piece before the surface pieces. `WarrenPiece` saves the exact
  `(dx,dy,dz,kind)` cell tuples in `TunnelCells` and each cluster's `NetworkDen`
  flag, so terrain changes and generation order cannot move the plan. Cell
  kinds use the existing DenShape.Kind order; preserve that order in future
  versions. Each chunk writes only its own columns, with a box encompassing all
  planned heights. A later structure/feature conflict can still make the writer
  skip a column containing fluid, a block entity or a hard protected block.
- Previously saved starts have no new fields and retain their old behavior.
  This change does not rebuild existing villages or alter genome/entity saves.

**Validation:** real `./gradlew build --no-daemon` passes. The new
`checkWarrenNetwork` task is part of Gradle `check`/`build`; it exercises connected
flat layouts at all four levels, deterministic plans, building/doorstep clearance,
slopes in four directions, cliffs, flooded/protected rooms, blocked trunks and
chunk partitions at positive/negative borders. All 94 nonempty plans out of the
96 flat samples connect every nest to every entrance; two dense layouts safely
fall back. The existing genetics/layout suite also passes. These checks use
synthetic terrain and do not establish in-game pathfinding, lighting, appearance,
POI claims, natural generation or save/reload behavior.

**Play test:** on open flat grass run `/bunnidogs warren 2 surface` through
`5 surface`, several times at different spots. Walk into a mound and between
dens; look for 3–4 floor Nests and a lantern in each fitted room. Check dogs'
`NestX/Y/Z` and `/time set 13000`. Then locate a newly generated natural village,
cross its chunk borders from different directions, leave/reload, and check the
same routes. The advancement still requires a natural structure, not the command.


## 15. U3: house cellar ramps (2026-10-15)

Implemented in `layout/CellarShape.java`, attached to U2's fitted network by
`TunnelShape`. A fitted house can have a covered ramp branching sideways beside
its doorstep and descending into a den or connecting corridor. This side branch
keeps the front door and normal yard approach usable within the existing 3x3
house footprint; the house's centre-floor Nest stays intact. There is no
separate new cellar room in U3.

The bounded deterministic search uses at most ten horizontal steps, descending
at most one block at a time, with three-high clearance. It avoids planned houses,
doorsteps, landmarks and shallow tree roots; only complete routes reaching an
existing underground walking column are retained. Floors, roofs and side walls
are lined, with a recessed lantern where a new side wall permits it. Unfitted
houses retain their existing behavior. The planner tests both house styles and
all affected terrain before adding any ramp cells.

Natural structures save ramp cells in the same TUNNEL piece as U2. PATH pieces
also save `PathExclusions` as `(dx,dz)` pairs so paving cannot overwrite a ramp
opening or mound, regardless of chunk order or a saved-piece reload. The debug
feature uses the same exclusions. Old saved starts without this field retain
their old path behavior; existing villages are not rebuilt.

Validation: `./gradlew build checkBunnidogModel --no-daemon` passes. The focused
network check fits 289 cellar ramps across the existing flat-layout fixtures and
checks their complete step routes after both house styles and surface paving.
It also exercises slopes, fluid/protected terrain, unchanged front-door access,
building clearance and chunk ownership. The existing genetics/layout suite
passes. Minecraft generation, navigation, lighting and reload behavior remain
unconfirmed by gameplay.

Play test: generate `/bunnidogs warren 3 surface` or `5 surface` on flat open
ground. Walk in/out of houses normally, take a covered ramp beside a doorstep,
and follow it into a den. Paths should stop short of ramp openings, not replace
the steps. Then repeat in a new natural village and reload across chunk borders.
Use the usual `NestX/Y/Z` and night checks to confirm that dogs can use both
house and den Nests. U4's landmark rooms are the next planned feature.
