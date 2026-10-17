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

- **P3, guide navigation:** two linked contents pages offer eighteen chapter
  links. Newer chapters return to the second index; existing chapter numbers
  stay stable. Native book/page-change behavior is retained. See
  `docs/FIELD_GUIDE.md` P3.

- **H6, Travel Basket:** carries one existing calm, unled ordinary animal using
  its full native data. UUID, line, name/parents/tag and paused care state survive
  native item saves. Packing frees the old Nest; blocked/duplicate/invalid
  release keeps the payload. Calipers opens its normal animal sheet. See
  `docs/HOME_SPECS.md` H6; art source `art/gen_basket.py`.

- **C8, dispenser care:** one redstone pulse uses a Brush or Sampling Syringe
  on one eligible animal ahead. Shared hand/machine transactions preserve
  all qualifications, products and recovery; failed care retains the tool.
  Exact sample data ejects normally. Native armadillo brushing remains.
  See `docs/CARE_SPECS.md` C8.

- **L11, trough filters:** sneak-food reserves exact item/components without
  spending food. Hoppers/gardeners respect empty reservations. Withdraw stock
  with sneak-empty, then repeat while empty to clear. The virtual template is
  saved separately and never refunded/dropped. See `SETTLEMENT_LIFE.md` L11.

- **C7, fleece palettes:** woolly adults yield two wool in their expressed coat's
  palette. Spotted gives one yellow and one brown; other coats use one color.
  Ready Calipers names the same products as native shearing. Shared regrowth
  and tool cost persist across gene changes. See `docs/CARE_SPECS.md` C7.
- **L10, bell calls:** actual native rings request nearby healthy residents by
  day, outside noon. Pending calls/rate limits are transient and preserve visit
  rest, home/hearing bounds and safe paths. Injury, danger, transport, night
  and home changes interrupt. See `docs/SETTLEMENT_LIFE.md` L10.
- **L9, trough automation:** native hoppers supply/extract the existing stack;
  comparators read 0 empty through 15 full. The shared transactional handler
  preserves the effective 16-item limit, filters and exact components. Power
  a hopper below to retain food. No new saves/menu/refill. See L9 in that spec.

Recent completed work also includes read-only home/food Calipers (C6), bounded
non-feral threat refuge (L7), low-priority thunderstorm shelter (L8), resident
shore fishing (Diet D5), native terrain traits (A4), local supplies (L6), living
dust (C5) and gardening/fitted farm corners (L5). Their full rules and evidence
are in the care/settlement specs and `docs/FIXES_PLAN.md` 6.5. Do not redo them.

The native Field Guide has 40 translated pages and eighteen chapter links across
two linked contents pages.
`art/gen_guide.py` owns text/icon/preview; `art/gen_pantries.py` owns eight
lineage food tables and shares the guide pool. See `docs/FIELD_GUIDE.md`.

## Implemented systems — do not redo

| Area | Current behavior and specification |
|---|---|
| Genetics and breeding | Seven genes, inherited pairs, bred stats/Bloom, eight guava tiers, litter seeds/crops, infused juice. `docs/FEATURES.md`, `docs/FIXES_PLAN.md` 6 |
| Sheets and records | Calipers, Pedigree Book, Ear Tags, seed/sample sheets and forecasts. `docs/SHEET_SPECS.md` S1–S8 |
| Laboratory | Two sample slots, vial display, pup/copy odds and both-direction splice plans. `docs/TABLE_SPECS.md` T1–T6 |
| Gene Splicer | One donor expressed copy, chosen gene, sample/Mutagen/tool cost, recipient recovery; breeding fixes pairs. `docs/SPLICER_SPECS.md` |
| Registry and shows | Distinct stable bred lines, linked snapshots, balanced-stat bronze/silver/gold judging, per-animal awards, ribbons and Champion Line. `docs/REGISTRY_SPECS.md`, `docs/SHOW_SPECS.md` |
| Care | Live samples, fleece palettes, whiskers/dust, dispenser care, saved recoveries and sneak-Calipers home/food/care/work status. `docs/CARE_SPECS.md` C1–C8 |
| Adaptations | Expressed fire/cold/fall/Poison/cactus/Levitation benefits, slate armor, heavy-build knockback resistance, self-lit glow and terrain movement. `docs/CARE_SPECS.md` A1–A4 |
| Homes/travel | Claimable Nests, day tether/night return, floor Nests, native leads/rides, safe led-adult reassignment and identity-preserving Travel Baskets. `docs/HOME_SPECS.md` H1–H6 |
| Settlement life | Saved trough stock/automation/filters, diet healing, scheduled/called bell visits, fitted furniture, gardening, local stores, refuge and storm shelter. `docs/SETTLEMENT_LIFE.md` L1–L11 |
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

The current Java 25 build and all **twenty-seven** native scenarios pass
(twenty-six mod cases plus native sanity). Latest coverage includes:

- **H6:** actual creative/survival capture, native item save/load, exact UUID,
  ancestry/tag/name/health/age and six paused recovery timers, immediate Nest
  ticket release/reclaim, blocked release, occupied/led/Apex refusals, duplicate
  identity, malformed data and pup release. Fresh mobs need a physics tick
  before native ground navigation can claim the freed Nest.

- **C8:** actual redstone/scheduled dispenser pulses, all combined products,
  shared refusal, exact pup sample/last charge and native armadillo fallback.
  Manual care cases remain covered. The shelter fixture waits for native roof
  lighting before assertions rather than assuming two ticks are sufficient.

- **L11:** actual server crouch-food input, exact filter components, conflicts,
  native saves/legacy stock, empty reservation transfers, withdrawal/clear and
  no virtual-template refund/drop. Gardeners leave wrong-filter crops intact.

- **L9:** real hopper transfers, effective capacity/components, transaction abort,
  scheduled comparator changes inside one visual fill level, empty stock and
  exactly-once break drops. Its drain lock uses actual redstone power.
- **L10:** real bell/event relay, morning calls after failed-search backoff,
  safe routes, injured/led refusals, rapid rings, changed home, rest and night.
  Existing scheduled gatherings remain covered.
- **C7:** native shears/drop colors, hidden glow, coat changes during recovery,
  cyan regrowth, spotted's combined yield/one wear, read-only Calipers and all
  palette names/total yields. Save/readiness cases remain covered.

The 40-page guide and basket previews are inspected. Each published GitHub build also checks
the approved model before uploading its exact-commit artifact. Earlier
fixture/gardening, diet/fishing, terrain, care/lab/show/registry, transport/Nest,
refuge/weather, sign/boat and Apex cases remain part of this suite; detailed
evidence stays in their specifications. Native path regions use non-loading
getChunkNow and sight guards use normalized loaded bounds.

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
