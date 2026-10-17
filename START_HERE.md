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

## Latest gameplay work — 2026-10-17

- **N1, nursery dispensers:** plant a saved litter into empty space above
  farmland or ripen one valid planted crop with Starlit Dust. Hand/machine
  actions share success-only costs and preserve the full functional seed.
  Refusals retain items. Seed Crops now maintain dry farmland. See
  `docs/NURSERY_SPECS.md`.

- **S9, carried records:** sneak-use a Pedigree Book or Purebred Registry with
  a filled basket in the other hand to log its native identity snapshot without
  unpacking. Registry eligibility, first representative and advancement stay
  shared with live registration. Air/block/off-hand/creative use and malformed
  identity refusal are covered. See `docs/SHEET_SPECS.md` S9.

- **L13, nighttime home care:** injured adults may eat real trough stock along
  safe routes wholly within four blocks of their Nest at night, then resume
  normal night waiting. Outdoor foraging/work remain daytime; the existing
  meal recovery is shared. Calipers reports trough-only readiness. Daytime
  troughs also use safe paths/replans. See `docs/SETTLEMENT_LIFE.md` L13.

- **H7, Nest wayfinding:** use a compass to mark a Nest in its dimension.
  Markers persist if the Nest is removed, preserve components and handle stacks,
  creative/off-hand use and inventory overflow. Empty-hand inspection reports
  ticket/resident without changing homes. See `docs/HOME_SPECS.md` H7.

- **L12, bred cultivation:** Bloom +10 adults advance matching unripe crops by
  one stage between ripe harvests. Growth and harvest share existing work rest,
  trough capacity/filters and protected safe routes. Ripe sweet berries now
  yield one food while keeping the bush. Calipers names the Bloom bonus. See
  `docs/SETTLEMENT_LIFE.md` L12.

The previous release already includes two-page guide navigation (P3), full-state
Travel Baskets (H6), dispenser care with native armadillo fallback (C8), saved
trough filters (L11), coat-colored fleece (C7), actual bell calls (L10) and
transactional hopper/comparator troughs (L9). Earlier home/food Calipers, refuge,
storm shelter, fishing, terrain traits, local supplies, living dust and fitted
farms are also complete. Their rules/evidence remain in the specifications
listed below; do not redo them.

The native Field Guide has 40 translated pages and eighteen chapter links across
two linked contents pages.
`art/gen_guide.py` owns text/icon/preview; `art/gen_pantries.py` owns eight
lineage food tables and shares the guide pool. See `docs/FIELD_GUIDE.md`.

## Implemented systems — do not redo

| Area | Current behavior and specification |
|---|---|
| Genetics and breeding | Seven genes, inherited pairs, bred stats/Bloom, eight guava tiers, litter seeds/crops, infused juice. `docs/FEATURES.md`, `docs/FIXES_PLAN.md` 6 |
| Sheets and records | Calipers, Pedigree Book, Ear Tags, seed/sample sheets, forecasts and carried-animal records. `docs/SHEET_SPECS.md` S1–S9 |
| Nursery | Native seed planting/ripening, dispenser seed/dust stations, exact saved litters and dry-farmland upkeep. `docs/NURSERY_SPECS.md` N1 |
| Laboratory | Two sample slots, vial display, pup/copy odds and both-direction splice plans. `docs/TABLE_SPECS.md` T1–T6 |
| Gene Splicer | One donor expressed copy, chosen gene, sample/Mutagen/tool cost, recipient recovery; breeding fixes pairs. `docs/SPLICER_SPECS.md` |
| Registry and shows | Distinct stable bred lines, linked snapshots, balanced-stat bronze/silver/gold judging, per-animal awards, ribbons and Champion Line. `docs/REGISTRY_SPECS.md`, `docs/SHOW_SPECS.md` |
| Care | Live samples, fleece palettes, whiskers/dust, dispenser care, saved recoveries and sneak-Calipers home/food/care/work status. `docs/CARE_SPECS.md` C1–C8 |
| Adaptations | Expressed fire/cold/fall/Poison/cactus/Levitation benefits, slate armor, heavy-build knockback resistance, self-lit glow and terrain movement. `docs/CARE_SPECS.md` A1–A4 |
| Homes/travel | Claimable Nests, day tether/night return, floor Nests, native leads/rides, led-adult reassignment, Travel Baskets and persistent compass place markers. `docs/HOME_SPECS.md` H1–H7 |
| Settlement life | Saved trough stock/automation/filters, diet healing/short night meals, bell visits, fitted furniture, harvest/cultivation, local stores, refuge and storm shelter. `docs/SETTLEMENT_LIFE.md` L1–L13 |
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

The Java 25 build and all **thirty-one** native scenarios pass (thirty mod
cases plus native sanity). Latest coverage:

- **N1:** real redstone pulses, malformed/blocked/wrong-soil/occupied planting,
  exact parental genomes/stats/quality/Mutagen and native crop saves, dust cost,
  ripe/empty/other-plant refusals, no ejection, real hand survival/creative use
  and native dry-farmland upkeep beneath a Seed Crop.
- **S9/H6:** exact carried records through air/block/off-hand/creative use,
  duplicate protection, registry award, invalid UUID/pup refusal and unchanged
  payload. Basket release still preserves all six paused timers, identity,
  ancestry/tag/name/health/age and home-ticket transfer.
- **L13:** native night waiting/meal arbitration, exact food/healing/shared
  rest, short night bounds, ordinary daytime reach, outdoor/work exclusions,
  lead interruption, damaging/blocked routes and Apex exclusion.
- **H7:** server free/claimed inspection, stack conservation, named off-hand
  rebinding, native marker save/tick after Nest removal, creative, permission
  and full-inventory cases.
- **L12:** Bloom threshold, one-stage/no-food growth, shared rest, ripe priority,
  Diet/filter/griefing refusals, interrupted growth, berries, guavas/vines and
  embryo exclusion. Care-dispenser drops now stay in a physical test catchment.

The 40-page guide previews are inspected with a substitute font; the approved
creature model is unchanged. GitHub additionally checks that model before the
exact-commit artifact upload. All earlier care/lab/show/registry, diet, home,
transport, refuge/weather, fixture/garden, pantry, automation/filter/bell,
sign/boat and Apex cases remain in the suite; detailed evidence lives in their
specifications. Fresh mob pathfinding needs a native physics tick on the floor.
Native path regions use non-loading getChunkNow and sight checks loaded bounds.

Native cases establish server behavior. Client interaction/book fonts/lighting,
natural chunk order/travel/unload, long multiplayer routines, modded transfer
devices and progression/decorative breeding balance remain gameplay review.

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
