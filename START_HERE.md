# Current project status

Read `AGENTS.md` for the continuation workflow, `README.md` for build/run commands,
and only the specifications/source relevant to the next batch. Historical sandbox,
upload and approval routines in `docs/history/` do not apply. User requests and
standing design latitude determine scope; a feature or push is a checkpoint.

## Project and delivery

Bunnidogs targets Minecraft 26.2 / NeoForge 26.2.0.88, Java 25 and Gradle 9.2.1.
Pure rules live in `genetics/` and `layout/`; native glue and client code remain
separate. A warren contains dens, mouths and tunnels; a settlement includes its
surface trees, houses, paths and landmarks (levels 1–5).

Work directly on `main`, preserving player saves and unrelated changes. Push
coherent validated batches under the standing continuation authorization.
The user downloads JARs from GitHub Actions artifacts; never attach/link a local
JAR in chat. Discover `get_usage_limits` at substantial starts/checkpoints. It
is not exposed in this session: use available work budget without inventing quota.

## Latest gameplay work — 2026-10-16

- **L9, trough automation:** native hoppers supply/extract the existing stack;
  comparators read 0 empty through 15 full. The shared transactional handler
  preserves the 16-item/effective limit, food filters and exact components.
  Power a hopper below to retain food. No new saves, menu or structure refill.
  See `docs/SETTLEMENT_LIFE.md` L9.

- **L8, storm shelter:** idle adults seek dry reachable cover near their Nest
  during native thunderstorms, then wait there. The bounded low-priority goal
  yields to care, work, social/home routines, breeding, danger and player control.
  Ordinary rain keeps existing idle behavior; no new saves, tickets or packets.
  See `docs/SETTLEMENT_LIFE.md` L8.

- **L7, refuge:** non-feral adult residents retreat from nearby visible tagged
  threats to a farther dry spot near home or a bounded local fallback. Native
  paths must stay loaded, home-bounded and clear of hazards/close threat nodes.
  Work/food/bell visits pause; leads, rides and gene/home changes interrupt.
  Health and Nest claims are preserved, with no combat or new save fields.
  See `docs/SETTLEMENT_LIFE.md` L7.

- **C6, resident needs:** sneak-Calipers shows the Nest coordinates/distance,
  accepted foods, shared meal recovery and forage blockers. Food AI and readout
  use the same eligibility snapshot; inspection does not search/load terrain
  or change tickets/timers/stock. Healthy adults need no healing, and the Apex
  gets a clear domestic-care refusal. See `docs/CARE_SPECS.md` C6.

- **Diet D5, shore fishing:** injured adult Fishers hunt reachable tagged cod,
  salmon and tropical fish near their Nest. Hunter/omnivore land prey stays
  separate; pufferfish are excluded by default. A selected prey kill heals 4 HP
  and shares saved 30-second food recovery. Existing bounds, night, transport,
  danger and protected-creature rules apply. Gene changes interrupt incompatible
  prey. See `docs/FIXES_PLAN.md` 6.5.
- **A4, terrain:** hoofed legs add 0.4 native step height (ordinary adult: one
  full block). Fleet legs/lean builds reduce soul-sand/honey lost speed by
  25%/50%, combining to 75%. Cobwebs, Slowness and damage remain. Named transient
  bonuses preserve external bases, update on gene edits and restore once after
  loading; Apex copies inherit them. See `docs/CARE_SPECS.md` A4.
- **L6, local supplies:** fresh underground and surface feed-store/market barrels
  use eight lineage food tables. Each gives 1–2 guavas, 1–3 small local food rolls
  and an independent 35% Field Guide chance. Native deferred loot, existing keys/
  seeds/stock and emptied barrels are preserved. Surface fitting refuses block
  entities under its pad. See `docs/SETTLEMENT_LIFE.md` L6.
- **C5, living dust:** one Brush use gathers one whisker at Senses +10, one
  glowstone dust from expressed glow, and one Starlit Dust from expressed
  starborn with Bloom +20, for every applicable product. Adults must be at least
  half healthy. All products share one wear and saved ten-minute grooming rest;
  overflow drops normally. Calipers names products. See `docs/CARE_SPECS.md` C5.
- **L5, work clarity:** sneak-Calipers shares the AI's gardening eligibility and
  recovery snapshot, without crop/path searches. New WarrenVersion 4 farms can
  fit an empty enabled corner trough inside the old 3×3 footprint, retaining
  seven crops. Two outside walking neighbours must already be safe; otherwise
  all eight crops remain. End mushroom plots and saved versions 1–3 keep their
  previous layout. See `docs/SETTLEMENT_LIFE.md` L5.

The native Field Guide now has 34 translated pages and eleven contents links.
`art/gen_guide.py` owns its text/icon/preview, including gardening, forecasts,
renewable care, terrain traits and fishing. `art/gen_pantries.py` owns the eight
new loot tables and shares the guide pool. See `docs/FIELD_GUIDE.md`.

## Implemented systems — do not redo

| Area | Current behavior and specification |
|---|---|
| Genetics and breeding | Seven genes, inherited pairs, bred stats/Bloom, eight guava tiers, litter seeds/crops, infused juice. `docs/FEATURES.md`, `docs/FIXES_PLAN.md` 6 |
| Sheets and records | Calipers, Pedigree Book, Ear Tags, seed/sample sheets and forecasts. `docs/SHEET_SPECS.md` S1–S8 |
| Laboratory | Two sample slots, vial display, pup/copy odds and both-direction splice plans. `docs/TABLE_SPECS.md` T1–T6 |
| Gene Splicer | One donor expressed copy, chosen gene, sample/Mutagen/tool cost, recipient recovery; breeding fixes pairs. `docs/SPLICER_SPECS.md` |
| Registry and shows | Distinct stable bred lines, linked snapshots, balanced-stat bronze/silver/gold judging, per-animal awards, ribbons and Champion Line. `docs/REGISTRY_SPECS.md`, `docs/SHOW_SPECS.md` |
| Care | Live Tissue Samples, woolly fleece, whiskers/dust, saved recoveries and sneak-Calipers home/food/care/work status. `docs/CARE_SPECS.md` C1–C6 |
| Adaptations | Expressed fire/cold/fall/Poison/cactus/Levitation benefits, slate armor, heavy-build knockback resistance, self-lit glow and terrain movement. `docs/CARE_SPECS.md` A1–A4 |
| Homes | Claimable Nests, day tether/night return, floor Nests, native leads/rides, safe led-adult reassignment. `docs/HOME_SPECS.md` H1–H5 |
| Settlement life | Saved trough stock/automation, diet healing, noon bell visits, fitted den furniture, gardening, local stores, refuge and storm shelter. `docs/SETTLEMENT_LIFE.md` L1–L9 |
| Warrens | Fitted dens/mouths/tunnels/cellars, landmark rooms, grand-tree hall/loop/lower commons, cave/Nether/End entrances and saved labels. `docs/WARREN_PLAN.md` U1–U8 |
| Wood and travel | Guava building/redstone set, doors/shutters/lamps, four sign forms, native boats/chest boats with custom assets and drops. `docs/FIXES_PLAN.md` 4–5 |
| Drops and endgame | Pelts/materials, Mutagen/Marrow/Tissue/Dust, five stat legends and pedestals. Sample-defined Apex ritual, inherited combat, warned attacks/enrage, boss bar and source-line crest. `docs/DROPS_SPECS.md`, `docs/APEX_SPECS.md` |
| Apex preparation | Crucible missing-offering status; main-hand Calipers/off-hand sample forecasts real boss values without spending or spawning. `docs/APEX_SPECS.md` P2 |

## Save and implementation constraints

- Keep gene/allele IDs append-only. Old six-pair data loads with plain/plain
  Diet; current genomes use seven pairs / 56 bits and mask the signed high sync
  word. Current network protocol is **6**; matching client/server versions apply.
- Preserve whole item components in samples, books, table slots, vehicles,
  troughs and trophies. Existing inventories and containers are never restocked.
- Structures save fitted tunnel cells, path exclusions, labels and optional
  `LifeFixtures`; column-owned underground writes preserve later containers,
  fluids and hard blocks. Surface props remain anchor-owned. `WoodDetails` and
  `WarrenVersion` preserve old starts; do not regenerate existing villages.
- Native terrain fitting can omit unsafe rooms, ramps, furniture or networks.
  Floor Nests, exits, water containment and existing player edits must stay safe.
- Named transient genetic modifiers preserve external attributes and reapply
  after native load. Care/food/work rest saves remaining ticks and pauses while
  unloaded. Apex is excluded from domestic tools, breeding, Nests and transport.

## Approved visuals

The sole model is approved **Low**, 2,172 quads / 8,688 vertices with twelve
512px coat atlases. Preserve the photographic baby face, human/rabbit ears,
compact robust body, outward paws, short fur and source imperfections. Do not
restore tiers, distance switching or graphics configuration. Sources:
`art/sculpt_bunnidog.py`, `art/gen_bunnidog.py`, `art/reference/`.

The latest head-lighting fix uses authored surface-derivative normals and one
continuous front UV chart; no geometry was added. Closely spaced front-ring
normal change fell from 0.478 to 0.038. Software preview inspected. The user's
prior Low/Ultra screenshots establish earlier rendering; final client lighting
and performance of the fix remain gameplay review. See `docs/BUNNIDOG_MODEL.md`
and `docs/GRAPHICS.md`. This continuation does not alter creature geometry,
atlases, renderer or animation.

## Validation and evidence

Use actual Java 25 compilation. In the current workspace:

```sh
source /workspace/.bunnidog-setup/activate.sh
./gradlew build runGameTestServer --no-daemon
```

`build` includes focused diet, breeding-lab, wood/boat asset and warren checks.
Add `checkBunnidogModel` for mesh/renderer/animation changes. GitHub Actions runs
`build checkBunnidogModel runGameTestServer` before uploading its commit artifact.
Avoid the large GenomeCheck suite unless substantial pure genetics/layout work
requires it; do not repeat passing checks without a relevant change or concern.

L5, C5, L6 and A4 pass actual builds and native server cases. L5 covers fitted/
fallback/legacy farms and shared work status; C5 exact combined products and
inventory overflow; L6 actual loot for all eight lineages and container saves/
preservation; A4 real collision stepping and native slow-block math. The final
Diet D5 run passes all **twenty** native scenarios (nineteen mod + native sanity),
including a real shore-fish kill, exact healing and saved meal recovery. Its
fixture allows native melee's twenty-tick startup window and keeps the bank dry.
Farm and affected guide previews inspected. Each published checkpoint's GitHub
build also checks the approved model before making an artifact.

C6 also passes the actual build and all twenty native cases. Its extended
settlement case checks read-only needs, real meal recovery, healthy animals and
the home boundary. The updated guide preview is inspected.

L7 passes the actual build and all twenty-one native cases, including safer
routes, routine pauses, lead/feral changes, quiet recovery and occupied refuge
cancellation. Native path regions are non-loading; sight checks use a loaded
rectangle. The updated guide preview is inspected. Natural threat balance and
multiplayer travel remain gameplay review.

L8 passes the actual build and all twenty-two native cases. Its new case covers
real thunder/sky exposure, a complete path beneath a roof, covered waiting,
lead/clear-weather interruption and native healing-trough preemption. Shared
navigation also retains the L7 refuge cases. The 33-page guide preview is
inspected; natural weather balance and client book fonts remain gameplay review.

L9 passes the actual build and all twenty-three native cases, including
real hopper transfers, comparator scheduling, capacity/components, transaction
abort, empty outputs and stock drops. Its 34-page guide preview is inspected.
Modded item devices and client contraptions remain gameplay review.

Native tests establish server behavior, not complete client or natural-world
play. Remaining review includes client interaction/lighting/book fonts, natural
chunk order/travel/unload, long multiplayer routines and progression balance.
Native sign validity/tickers/text/color/glow/wax, boat inventory/drop/save,
transport/home tickets, care/tools, laboratory/show/registry and Apex encounter
checks already pass. Keep their evidence in the relevant specs.

Earlier user feedback: U2/U3 received “good, works well”; the six-stage `d7f39a9`
release (U4/U5/U6b/T4/W3/V1c) received “everything compiled and worked this run”.
Individual edge cases were not itemized. These reports do not confirm newer
features. U6a's entry advancement needs a natural structure rather than a
command feature; U1/H3b natural home/den review remains useful.

## Continuing

`docs/ROADMAP.md` lists optional next work; coherent additions beyond it are
already authorized. Prefer useful habitat/resident behavior or clarity, define
bounded rules first, and complete meaningful batches through build and delivery.
For terrain review use `/bunnidogs warren 1 surface`, `3 surface`, `5 surface`,
cave/Nether `1`, End `5 surface`, and natural structure lookup:
`/locate structure bunnidogs:warren_village` or `bunnidogs:warren_end_village`.

API lookups: `docs/API_NOTES.md`. Player reference: `docs/FEATURES.md`.
Conventions/art/environment: `docs/REFERENCE.md`. Original decisions and old
chunk evidence: `docs/DESIGN.md`, `docs/CHUNKS.md`, `docs/history/`.
