# ROADMAP (current; replaces the old one in docs/history/ROADMAP_OLD.md)

Each item is one or a few small chunks. "Needs" = what must exist first. The user decides the order; unless they say otherwise, prefer items that fix reported problems and need no new Minecraft API.

## Done (do not redo)
Genome, breeding, wild lineages, bred stats and Bloom, seeds and crops, guava ladder (8 tiers), juice and infusion, warrens and villages (levels 1-5, houses, paths, landmarks, styles by biome, meeting tree), guava wood (log, leaves, planks, stripped forms, stairs, slab, fence), Calipers, Pedigree Book and sheet windows (S1-S8), Ear Tags, drops (D1-D8: pelts, flesh, materials, Mutagen, Marrow, Tissue Sample, Starlit Dust, legends, Trophy Pedestal, worn tag), Genetics Table T1–T4, bug batch F1-F3, Nest + claiming homes (H1-H4, H3b delivered).
U2/U3 received the user's "good, works well" gameplay feedback (individual
night/reload/chunk-order checks were not itemized).
U4 landmark rooms, U5 grand-tree hall/loop, U6b cave/Nether/End dens, T4 table
sample display, W3 guava mechanisms and V1c house details are implemented.
Gradle, model, resource and terrain/layout checks pass; the guava texture preview
was inspected. The user reports "everything compiled and worked this run" for
the six-stage `d7f39a9` release; individual edge cases were not itemized.
Delivered, not individually confirmed: **U6a** advancement, **U1** level-1 dens
and mouths, **H3b** house floor Nests. V1a, V1b, H3 and H4 WORK.

## Latest implemented work (gameplay pending)

C10 gives healthy expressed snow coats a cold-habitat brushing product at
Bloom +10, sharing native temperature rules and existing grooming recovery.
Hand/dispenser/Calipers agree; ordinary native shearing stays independent.
Java 25 build/all 50 cases pass; guide preview inspected. See `CARE_SPECS.md`.

O4/O5 adds native dispenser guava saplings, next-tick support recovery and
supported/bounded growth. Grass/podzol/mycelium work under 26.2 vegetation tags;
N8 restores the corresponding litter-crop fallback without changing farmland
planting. L23 allows safe regional farms in fresh five-tree cave/Nether feature
warrens. Java 25 build/all 49 native cases pass; final guide preview inspected.
See `ORCHARD_SPECS.md`, `NURSERY_SPECS.md` and `SETTLEMENT_LIFE.md`.

D6 adds injured residents' ripe-guava and diet-matching berry meals, preserving
plants and sharing ordinary healing/recovery. Grazing now follows complete safe
loaded routes and rechecks protection callbacks, preserving replacement
containers and legacy crop/hay behavior. Java 25 build/all 48 cases pass; guide
preview inspected. See `FORAGING_SPECS.md`.

L22 adds sustainable red/brown mushroom thinning for matching healthy residents,
leaving a living same-color companion inside loaded home bounds. Native spread,
safe routes, protection callbacks and normal trough/work controls apply. Java
25 build/all 47 cases pass; guide preview inspected. See `SETTLEMENT_LIFE.md`.

O1–O3 adds player-established guava fruit beneath any leaves, one-age native
bone meal growth by hand/dispenser and Silk Touch recovery of guava leaves.
Plain-guava/actual-hand costs, native placement protection and saved ages remain
safe. Java 25 build/all 46 cases pass; orchard/region/Habitat guide preview is
inspected. See `ORCHARD_SPECS.md`.

L21 gives new version-five farms regional crops and valid supporting blocks,
including water-free cactus/wart/mushroom/chorus beds and a glow-berry trellis.
The original footprint, fitted optional troughs and old saved plans remain.
Java 25 build/all 45 native cases pass; layout/guide previews inspected. See
`SETTLEMENT_LIFE.md` L21 and `WARREN_PLAN.md` 22.

L20 adds protected cactus-top pruning for desert browsers with enabled troughs,
preserving bases/flowers and rechecking callbacks before one real harvest.
P5 adds a fourth Habitat index and cactus chapter without renumbering earlier
pages. Java 25 build/all 44 cases pass; previews inspected. See
`SETTLEMENT_LIFE.md` L20 and `FIELD_GUIDE.md` P5.

T9/T10 add server-snapshotted guava/Mutagen/Fortune planning to the Genetics Table,
using real seed quality/drift/treatment and harvest bonus while preserving cues/samples.
Plain A|B payloads still parse; optional plan data uses matching network 7.
Saved items/animals/slots remain unchanged. Build/all 43 cases and focused
math/page checks pass; previews inspected. See `TABLE_SPECS.md` 9–10.

L19 adds bounded daytime rest near an actual Nest for adults below half
health, yielding to food/danger/player control and thunderstorm shelter.
No healing or persistent field; Calipers shares readiness. Java 25 build/all
43 native cases pass; preview inspected. See `SETTLEMENT_LIFE.md` L19.

T7/T8 connects native hoppers/comparators to the Genetics Table's saved A/B
samples and repairs actual-hand placement. Transfers preserve components and
roll back across both slots; manual take/forecasts remain. Java 25 build/all
42 cases pass; lab preview inspected. See `TABLE_SPECS.md` 8.

H8 marks recorded resident Nests with normal compass use, preserving loaded
and unloaded homes/tickets. L18 adds read-only sneak-Calipers bell roll calls
with injured-first tags/health/foods/homes and bounded loaded-only counts.
Build/all 41 native cases pass; guide preview inspected. See `HOME_SPECS.md`
and `SETTLEMENT_LIFE.md`.

E5 adds a deliberate tagged-resident finder through a renamed spare Ear Tag
and sneak-Calipers. Loaded 24-block searches use native outlines/cooldowns,
with exact item/animal preservation. Java 25 build/all 41 cases pass; finder/
index preview inspected. See `EXPLORATION_SPECS.md`.

S10 adds native local record filters to Pedigree/Registry contents: tags,
expressed/hidden copies, recorded age and bred-stat comparisons. Original
sheet numbers/parent links persist and arrows follow matches. Actual Java 25
build/focused query/layout checks pass; filtered/empty records and guide
previews inspected. Client input/font review remains. See `SHEET_SPECS.md`.

P4 expands the 50-page guide to three contents pages/twenty-four chapter links,
with direct access to newer systems. E4 adds native one-time working-line XP
milestones for a successful guard command/general or focused found-ore survey.
Build/all 40 native cases pass; the three-index preview is inspected. See
`FIELD_GUIDE.md` and `EXPLORATION_SPECS.md`.

L17 adds a native redstone pause for resident meals/crop/compost work from a
trough, retaining its stock/filter/automation/comparator and hoe setting.
Commit checks include changes during native protection callbacks. Build/all
40 native cases pass; the 49-page guide preview is inspected. See
`SETTLEMENT_LIFE.md`.

E3 adds Senses +20 mineral-family focus to compass surveys, using a reusable
mineral in the other hand and native ore-family tags. Recovery/range remain
shared and no-match searches retain the old marker. Coordinates now persist
in chat. Build/all 40 native cases pass; 48-page guide preview inspected. See
`EXPLORATION_SPECS.md`.

E2 gives expressed bat ears a bred exploration role: Senses +10 adults mark
nearby loaded ore on a compass, with a six-block sphere and saved thirty-second
recovery including empty surveys. Whole place-marker components persist after
mining; Calipers shares readiness. Build/all 40 native cases pass; the 47-page
guide preview is inspected. See `EXPLORATION_SPECS.md`.

E1 adds native journeyman-cartographer Warren Explorer Maps, preserving the
ordinary trade pool and omitting failed/blank results. The target tag includes
natural Overworld warrens, with native map saves/markers. The 46-page guide
explains the route. See `EXPLORATION_SPECS.md`.

L16 adds deliberate bone-command feral Nest guards, off in old saves. Defense
selects tagged hostiles along loaded safe routes within eight blocks of home,
with native bites and transport/health/gene interruptions. Calipers shares its
status; the 45-page guide has twenty chapter links. Build/all 39 native cases
pass; guide preview inspected. See `SETTLEMENT_LIFE.md`.

A5 gives expressed snow coats native powder-snow footing and escape jumping.
Hidden snow and woolly alone still sink; native descending/fall rules and safe
domestic routes remain. Gene edits apply immediately. Build/all 38 server
scenarios pass; physical-trait guide preview inspected. See `CARE_SPECS.md`.

N7 fits optional empty Cradles into new level-five grand-tree halls, retaining
their trough/bell, all floor Nests, routes, labels and old saved starts. The
station reuses a bedding footprint; its complete pad is reserved during fitting.
See `NURSERY_SPECS.md` and `WARREN_PLAN.md` 21.

N6 adds read-only sneak-Calipers incubation/crowd/pad checks and temporary
markers, sharing actual hatch preflight. Native Cradle structure rotation and
mirroring also follow its facing. See `NURSERY_SPECS.md`.

N5 lets hand/dispenser Mutagen treat a planted seed or Cradle in place,
preserving the whole saved item, age and remaining incubation. Repeats/refusals
retain the vial. The Field Guide has 44 pages. See `NURSERY_SPECS.md`.

C9 adds one renewable Moss Carpet from expressed moss adults at Bloom +10,
sharing hand/dispenser grooming products, tool wear and saved recovery. L15
adds native plain-guava/foliage compost values while excluding infused tiers,
connecting ordinary orchards to L14. See `CARE_SPECS.md`, `SETTLEMENT_LIFE.md`.

L14 connects opted-in resident work to vanilla composting: Bloom +10 adults
spend suitable surplus food above an eight-item reserve after crop tasks.
Native chances/maturation/hopper output, Diet/protection/safe paths and shared
work recovery apply. The guide has 43 pages. See `SETTLEMENT_LIFE.md`.

N2 preserves whole planted seeds, names and components through native saves,
early recovery and Mutagen treatment. N3 adds the Nursery Cradle: one saved
litter, paused incubation, growth tools, native hopper/comparator/dispenser
controls and checked 3×3 hatching with atomic spawn rollback. The guide now
has 42 pages/nineteen chapter links. See `NURSERY_SPECS.md` and `FIELD_GUIDE.md`.
N4 moves crop harvest after native break protection, recovers environmental
loss and rolls back refused crop/command litters with seed preservation.

N1 adds native dispenser litter-seed planting and Starlit Dust ripening, sharing
hand transactions, exact functional seed data and success-only costs. Refusals
retain items; Seed Crops now maintain dry farmland. See `NURSERY_SPECS.md`.

S9 logs carried native snapshots through deliberate Pedigree Book/Registry
sneak-use. Registry eligibility, duplicates/representatives and advancement
remain shared with live use; baskets retain all data. See `SHEET_SPECS.md`.

L13 allows short safe nighttime trough meals within four blocks of home,
sharing normal stock/healing/meal rest before night waiting resumes. Outdoor
foraging/work keep daylight rules. Calipers reflects the distinction and trough
paths now share loaded hazard checks. See `SETTLEMENT_LIFE.md`.

H7 adds ordinary-compass Nest marking and read-only ticket/resident inspection.
Bound places persist through Nest removal; names/components, stacked/off-hand
use, creative and inventory overflow follow native item behavior. See
`HOME_SPECS.md`.

L12 gives Bloom +10 adults one-stage cultivation between ripe harvests, sharing
the existing garden rest and respecting Diet, trough filters and protection.
Foragers also harvest ripe sweet berries without removing the bush. Native
garden paths now use bounded hazard checks. See `SETTLEMENT_LIFE.md`.

P3 expands the Field Guide to 40 pages with two linked contents pages and
eighteen chapter links. Newer chapters return to their own index; existing
chapter numbers stay stable. See `FIELD_GUIDE.md`.

H6 adds a craftable Travel Basket preserving one animal's UUID and native
biology/care state, freeing its old Nest and retaining payload on unsafe release.
Calipers reads the carried animal without unpacking. See `HOME_SPECS.md`.

C8 adds native dispenser Brush/Syringe care sharing hand-tool eligibility,
animal recovery and exact products/data, while preserving armadillo brushing.
One pulse treats one animal; output drops support hopper collection. See
`CARE_SPECS.md`.

L11 adds saved exact-component trough filters for manual/hopper/gardening input.
Virtual templates cost/refund nothing; normal withdrawals retain reservations
until deliberately cleared while empty. See `SETTLEMENT_LIFE.md`.

C7 makes woolly fleece follow expressed coat palettes, including spotted's
two-color pair. Total yield, shared recovery and native shears cost stay the
same; Calipers names the prospective products. See `CARE_SPECS.md`.

L10 lets actual bell rings call healthy adult residents by day outside noon.
Calls are bounded/transient, preserve visit rest and home tickets, and share
loaded safe paths with scheduled visits. See `SETTLEMENT_LIFE.md`.

L9 adds transactional trough hopper access and comparator stock output, using
the existing saved stack and its limits/food filters/components. No menu or
automatic restock is introduced. See `SETTLEMENT_LIFE.md`.

L8 adds low-priority thunderstorm shelter near home. Idle adults find real cover
or wait safely beneath it; feeding/work and other important routines preempt it.
Searches and complete native paths stay bounded. Ordinary rain keeps existing
idle behavior. See `SETTLEMENT_LIFE.md`.

L7 adds bounded non-combat refuge for non-feral residents around visible tagged
threats. It preserves the home, chooses loaded safer routes and pauses domestic
routines while yielding to transport. See `SETTLEMENT_LIFE.md`.

C6 adds read-only Calipers Nest/food/recovery status, sharing food eligibility
with the real resident goals and keeping inspection free of terrain searches.
Healthy animals need no healing; the Apex reports domestic care unavailable.
See `CARE_SPECS.md`.

Diet D5 lets injured resident Fishers hunt reachable tagged cod/salmon/tropical
fish near home, sharing bounded native hunting and saved meal recovery.
Land predators keep their separate prey list, and gene changes interrupt
incompatible targets. See `FIXES_PLAN.md` 6.5.

A4 adds native one-block stepping for ordinary hoofed adults and partial
soul-sand/honey slowing resistance from fleet legs and lean builds. Named
bonuses preserve external attributes and update across gene edits/reloads.
Gene hovers and the 29-page guide explain retained hazards. See `CARE_SPECS.md`.

L6 supplies newly placed underground and surface food-store barrels with modest
lineage-appropriate loot, preserving old tables and every existing container.
All eight tables guarantee a few guavas and retain the independent guide chance.
See `SETTLEMENT_LIFE.md`.

C5 adds renewable glowstone and Starlit Dust from carefully qualified living
lines using the existing Brush/recovery. Product eligibility is expressed-trait
based and combined products share one wear/timer. Calipers and the 28-page
guide explain the sources. See `CARE_SPECS.md`.

L5 adds Calipers gardening readiness/recovery and safely fitted empty enabled
troughs in new farm corners, retaining the old footprint and legacy starts.
AI and inspection share eligibility; unsupported walking space keeps the old
farm layout. See `SETTLEMENT_LIFE.md`.

P2 adds a read-only Calipers/sample Apex forecast and crucible offering readiness,
sharing combat rules and ring scans with real summoning. Native preview/cost
preservation and actual boss values pass; guide expanded to 27 pages. See
`APEX_SPECS.md`.

A3 adds slate natural armor, stocky/chonker knockback resistance and self-lit
expressed glow coats. Native modifiers preserve external attributes and cannot
stack across reloads; bosses inherit them too. Build/model/native cases pass;
software lighting preview inspected. See `CARE_SPECS.md` A3.

L3/L4 adds saved/chunk-owned den troughs and developed gathering bells, plus
hoe-controlled resident gardening. Mature matching food beside an enabled
trough becomes one stored item; crop/fruit regrowth, home/danger/griefing rules
and saved work recovery apply. Legacy starts/stock and crafted trough opt-in
are preserved. Build/layout/native cases pass; previews inspected. See
`SETTLEMENT_LIFE.md`. The Field Guide expands to 26 pages.

P1 adds a craftable native Field Guide: 27 translated pages, contents/return
links and a 35% independent pantry loot pool. It explains the whole progression
without new screens or payloads. Build/server loading pass; preview inspected and client
reading remains pending. See `FIELD_GUIDE.md`.

G4 completes the Apex: sample-defined boss, five legendary pedestal offerings,
crucible, bounded inherited combat attributes, warned attacks/enrage, native
boss bar and readable/displayable source-line crest. Ritual refusals/costs,
combat, saves and player-kill rewards pass the native encounter case; the build
passes. See `APEX_SPECS.md`.

Settlement L1/L2 adds a saved Feeding Trough and noon bell visits. Resident
feeding respects expressed diets, home bounds and shared forage recovery.
Gatherings use loaded native bell POIs without claiming villager tickets and
yield to danger, injury, transport and night. Native stock/save/path/interaction
checks and the build pass. See `SETTLEMENT_LIFE.md`.

G3 implements Show Ring judging: a marker stand, balanced-stat bronze/silver/
gold milestones for stable bred adults, non-destructive scorecards, saved animal
award limits, readable/displayable winning ribbons and Champion Line. Care C3
adds renewable loose whiskers from brushing healthy Senses +10 adults, at one
per ten minutes with saved recovery and Brush wear. Native server cases and
the build pass; client gameplay remains pending. See `SHOW_SPECS.md` and
`CARE_SPECS.md`.

A2 adds expressed moss Poison protection, sand cactus protection and void
Levitation protection. Native effect/damage cases establish hidden-allele limits,
retained vulnerabilities and changes when the expressed coat changes. Existing
models, stats, genomes and saves are unchanged; see `CARE_SPECS.md`.

T5/T6 adds both-direction Gene Splicer plans and inherited-copy forecasts to the
Genetics Table. It exposes recessive carrier odds, homozygous pairs, whole-litter
success and all-seven-fixed pup probability. G2 adds a reusable Purebred Registry
for distinct stable bred adult genomes, linked snapshot sheets and Breeds True.
Existing item/animal formats and protocol 6 remain unchanged. Creative animal
interaction now persists splicer selections and registry entries on the held
stack. Focused math/layout, actual build and server cases pass. See
`TABLE_SPECS.md` 7 and `REGISTRY_SPECS.md`.

G1 implements the Gene Splicer: one donor expressed allele enters a selected
gene at a sample/Mutagen/tool cost, preserving bred stats and requiring breeding
to fix a homozygote. Selection and recovery persist. Native server interaction
and focused genetics/material/save checks pass; client gameplay is pending.
See `SPLICER_SPECS.md`.

U8 adds saved, chunk-owned guava room/exit labels to newly planned warrens,
preserving old starts and player edits. Care C1/C2 adds a non-lethal Sampling
Syringe and renewable wool from healthy woolly adults. A1 gives expressed
ember/snow/woolly/hoofed traits native environmental benefits. Existing saves,
gene/allele order and approved body assets remain compatible. The combined
build/server checks pass. See `CARE_SPECS.md` and `WARREN_PLAN.md` 20.

W4b completes guava boats/chest boats: native vehicles and model layers, custom
hulls/icons, crafting/unlocks, proper item drops and a 27-slot chest. H5 makes
home/food navigation respect leads/rides and lets a player rehome the nearest
led adult by right-clicking a free reachable Nest. The old ticket is retained
until the destination is secured. See `HOME_SPECS.md` H5 and `FIXES_PLAN.md` 4.4.
Server GameTests cover vehicles, genetic inventory data, drops, sign persistence,
transport behavior and Nest-ticket safety; visual/client interaction review
remains separate. These tests now gate the GitHub artifact.

W4a: guava signs/hanging signs, four native placement forms, custom board/icon/
editor textures, crafting/unlocks, tags and shared wall loot. Compiler, resource
state/attachment checks and the model check pass; texture preview inspected.
See `FIXES_PLAN.md` 4.4. Server text/color/glow/wax saves and native tickers now
pass integration tests; client editing and natural chunk unload/reload remain pending.

Diet D1–D4: inherited seventh gene, compatible old data, dietary feeding and
temptation, home-bounded grazing/hunting and seven-row sheets/forecasts.
U7: a fitted lower commons under grand-tree halls. New pantry/market/commons
barrels now have modest food loot. See `FIXES_PLAN.md` 6.4 and `WARREN_PLAN.md` 19.
Compiler/headless checks are distinct from the previous release's gameplay report.

## Optional candidates

The user authorizes substantial coherent additions and mechanic changes beyond
the written plan without another confirmation. Choose useful work rather than
filling a quota, and honor any explicit pause in the current request.

| Item | What | Needs | Spec |
|---|---|---|---|
| Further living harvests | Other fitting renewable materials beyond fleece, whiskers and glow/starborn dust | C1–C5 | `CARE_SPECS.md`; define each source/yield before coding |
| Further lineage behavior | Other useful habitat routines beyond existing protection, defenses and terrain benefits | A1–A4 | Follow expressed genes; preserve native hazards and breeding tradeoffs |

## Ideas without a spec (design before implementing)
Further settlement life beyond troughs, bell visits and gardening; further renewable materials and lineage adaptations. Gene Splicer G1, its table preview T5, Purebred Registry G2 and Show Ring G3 and Apex G4 are implemented. Native leads and Nest reassignment are H5; live tissue samples, woolly fleece, brushed whiskers and inherited protections are C1–C3/A1–A2. More ideas: `docs/DESIGN.md`, section "Ideas".
