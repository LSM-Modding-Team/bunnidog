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
| Living harvest extensions | Additional renewable materials beyond fleece and brushed whiskers, with conservative yields/recovery | C1–C3 | `CARE_SPECS.md`; define each source/yield before coding |
| Additional lineage behavior | Useful cave/terrain adaptations beyond current fire/cold/fall/poison/cactus/levitation rules | A1/A2 | Follow expressed genes; preserve native hazards and breeding tradeoffs |

## Ideas without a spec (design before implementing)
Apex boss (the remaining old "Phase 6 artificial lineage tool"); further settlement life beyond troughs/bell visits; further renewable materials and lineage adaptations. Gene Splicer G1, its table preview T5, Purebred Registry G2 and Show Ring G3 are implemented. Native leads and Nest reassignment are H5; live tissue samples, woolly fleece, brushed whiskers and inherited protections are C1–C3/A1–A2. More ideas: `docs/DESIGN.md`, section "Ideas".
