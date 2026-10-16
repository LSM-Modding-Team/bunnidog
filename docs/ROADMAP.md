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
| Settlement wayfinding | Use guava signs to label new halls, pantries and entrances; preserve old saved starts | W4a | Design terrain/chunk placement and saved text before coding |

## Ideas without a spec (design before implementing)
Gene Splicer / Purebred registry / Show Ring / Apex boss (the old "Phase 6 artificial lineage tools"); settlement life (the old 6d); living harvest instead of death-only drops; environment effects of lineages (cold resistance, heat tolerance, night vision, ...). Native leads, transport-aware behavior and player-directed Nest reassignment are now implemented as H5. More ideas: `docs/DESIGN.md`, section "Ideas".
