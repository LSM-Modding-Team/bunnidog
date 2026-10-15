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

## Remaining accepted work

| Item | What | Needs | Size / risk | Spec |
|---|---|---|---|---|
| **D1–D4 (diets)** | Seventh gene, then feeding, grazing and hunting; audit every `Gene.COUNT` consumer and old six-gene saves/items first | Accepted picks in `FIXES_PLAN.md` section 7 | Largest remaining item; implement in coherent stages with compatibility checks | `FIXES_PLAN.md` section 6 |

## Optional candidates

The user now authorizes substantial coherent additions and mechanic changes
beyond the written plan, without another confirmation. These candidates are
optional design choices, not approval gates. Honor the current development
pause and choose useful work rather than filling a quota.

| Item | What | Needs | Spec |
|---|---|---|---|
| U7 | A second floor under the hall | U5 | `WARREN_PLAN.md` 5, decision D7 |
| W4 | Signs, hanging signs, boats | W3 | `FIXES_PLAN.md` 4.4 |

## Ideas without a spec (design before implementing)
Leash; Gene Splicer / Purebred registry / Show Ring / Apex boss (the old "Phase 6 artificial lineage tools"); settlement life (the old 6d); living harvest instead of death-only drops; environment effects of lineages (cold resistance, heat tolerance, night vision, ...). More ideas: `docs/DESIGN.md`, section "Ideas".
