# ROADMAP (current; replaces the old one in docs/history/ROADMAP_OLD.md)

Each item is one or a few small chunks. "Needs" = what must exist first. The user decides the order; unless they say otherwise, prefer items that fix reported problems and need no new Minecraft API.

## Done (do not redo)
Genome, breeding, wild lineages, bred stats and Bloom, seeds and crops, guava ladder (8 tiers), juice and infusion, warrens and villages (levels 1-5, houses, paths, landmarks, styles by biome, meeting tree), guava wood (log, leaves, planks, stripped forms, stairs, slab, fence), Calipers, Pedigree Book and sheet windows (S1-S8), Ear Tags, drops (D1-D8: pelts, flesh, materials, Mutagen, Marrow, Tissue Sample, Starlit Dust, legends, Trophy Pedestal, worn tag), Genetics Table T1-T3, bug batch F1-F3, Nest + claiming homes (H1-H4, H3b delivered).
Delivered, not yet confirmed: **U6a** (advancement), **U1** (level 1 dens and mouths), **H3b** (a Nest in the floor of every burrow house). V1a, V1b, H3 and H4 WORK.

## Planned, in the order I would take them
| Item | What | Needs | Size / risk | Spec |
|---|---|---|---|---|
| **U2** | Joined dens and tunnels for levels 2-5 (a `TUNNEL` piece, lights) | U1 | medium | `WARREN_PLAN.md` 8 |
| **U3** | Cellar ramps from the houses into the tunnels | U2 | small-medium | `WARREN_PLAN.md` 8 |
| **U4** | Pantry, cistern under the well, stall cellar, back door at the farm, skylights | U2 | medium | `WARREN_PLAN.md` 8 |
| **U5** | Hall under the grand tree (level 5) | U2 | small | `WARREN_PLAN.md` 8 |
| **U6b** | Cave, Nether and End variants (den cut into the floor, no mouth) | U1 | small-medium | `WARREN_PLAN.md` 8 |
| U7 (optional) | A second floor under the hall (only if the user asks) | U5 | the user decides | `WARREN_PLAN.md` 5 |
| V1c (V1a works, V1b delivered; session 44) | Villages built of guava wood: doors, shutters and a lantern at the door (needs W3). Original text: material tier by development level (fixes "huts stay dirt while wells improve") | W1, W2 (W3 for doors) | layout + palette, medium | `FIXES_PLAN.md` section 5 |
| W3 | Guava door, trapdoor, button, pressure plate, fence gate | W1 | new API (`WoodType`, `BlockSetType`): the riskiest | `FIXES_PLAN.md` section 4 |
| D1-D4 (diets) | A 7th gene "diet"; hay/wheat/farm matter, some dogs hunt | accepted picks in `FIXES_PLAN.md` section 7 | the biggest item; split into chunks first | `FIXES_PLAN.md` section 6 |
| T4 | Show the loaded sample on the Genetics Table (+ a click sound) | T1-T3 | small; maybe a block-state property | `TABLE_SPECS.md` |
| W4 | Signs, hanging signs, boats | W3 | heavy, optional | `FIXES_PLAN.md` section 4.4 |

## Ideas without a spec (the user must ask; write a plan first)
Leash; Gene Splicer / Purebred registry / Show Ring / Apex boss (the old "Phase 6 artificial lineage tools"); settlement life (the old 6d); living harvest instead of death-only drops; environment effects of lineages (cold resistance, heat tolerance, night vision, ...). More ideas: `docs/DESIGN.md`, section "Ideas".
