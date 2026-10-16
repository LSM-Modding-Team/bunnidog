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
