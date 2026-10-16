# Chunks: one line each (newest first)

Historical statuses below record what the user reported at the time: WORKS = compiled and played, COMPILES = compiled, behaviour not itemized, NOT COMPILED = delivered, no report yet.
Current build/headless/gameplay evidence lives in `START_HERE.md`. Old NOT COMPILED entries are historical, not claims that the current checkout fails to build.
Full text of every chunk (files, decisions, compile risks): `docs/history/CHUNKS_FULL.md` (grep the id). Newer chunks: the spec file of the feature.

| Chunk | What | Status |
|---|---|---|
| U7 | Terrain-tested lower commons, stairs, four more Nests and a supply barrel beneath the grand-tree hall | BUILD + LAYOUT/MODEL CHECKS PASS; gameplay pending |
| Pantry supplies | Lazy modest food/guava loot in newly placed pantry/market/commons barrels; protect existing containers | BUILD PASS + RESOURCE REVIEW; gameplay pending |
| Diet D1–D4 | Seventh inherited gene, legacy data/signed sync, diet feeding and temptation, home-bounded grazing/hunting, seven-row sheets | BUILD + DIET/GENETICS/MODEL CHECKS PASS; gameplay pending |
| V1c | Initially open guava house doors, shutters, level-4 cottage windows and level-5 doorstep lamps; old-start dressing flag | WORKS: user confirms d7f39a9 batch; build/layout/model checks pass |
| W3 | Guava door/trapdoor/button/pressure plate/fence gate, crafting, tags, loot and generated assets | WORKS: user confirms d7f39a9 batch; build/resource checks pass; preview inspected |
| T4 | Synced A/B vial display on Genetics Table and successful-interaction click | WORKS: user confirms d7f39a9 batch; build/model checks pass |
| U6b | Cave/Nether/End dens with open floor stair cuts; separate natural End structure retained | WORKS: user confirms d7f39a9 batch; build/layout checks pass |
| U5 | Grand-tree 7x7x4 nursery hall, eight Nests, roots, lights and satellite loop | WORKS: user confirms d7f39a9 batch; build/layout checks pass |
| U4 | Pantry, well cistern, market cellar/hatch, farm entrance and skylights | WORKS: user confirms d7f39a9 batch; build/layout checks pass |
| U3 | Covered cellar ramps beside house doorsteps; join the underground network, preserve house Nests/doors and save path exclusions | BUILD + LAYOUT/MODEL CHECKS PASS; user: "good, works well" for U2/U3 batch |
| U2 | Level 2–5 surface dens, lit connecting tunnels, terrain-tested entrances and saved chunk-owned underground cells | BUILD + LAYOUT CHECKS PASS; user: "good, works well" for U2/U3 batch |
| Low-only + smooth head | Preserve approved Low positions; surface-derived head normals and continuous UV chart; remove presets/config; GitHub build artifacts | BUILD + HEADLESS PASS; nearest-filtered preview inspected; in-game fix pending |
| Graphics tiers + facial depth | Four client detail levels, Medium default, optional distance reduction, deeper anatomical face in every tier | BUILD + ALL-TIER HEADLESS PASS; previews inspected; in-game/FPS pending |
| Codex workflow + temple fix | Smooth photo-to-scalp transition, preserve mesh; workspace instructions, portable headless model check/CI and optional source package | BUILD + HEADLESS PASS; preview inspected; fixed texture in-game pending |
| U6a | Advancement "Welcome to the Warren" (enter the surface warren structure), data only + lang | NOT COMPILED |
| U1 | Level 1 surface warrens: a den under the tree with the nests, a lantern and hay, and up to 2 mouths (mound + slope); the old holes only as a fallback | NOT COMPILED |
| UW0 | Plan: readable underground warrens (mouths, dens, tunnels, cellars; docs/WARREN_PLAN.md) | ACCEPTED |
| H3b | A Nest in the floor of every burrow house (the hollow centre), so each house is a home for one dog | NOT COMPILED |
| V1b | Villages of guava wood, second step: level 4 cottages get a stone foundation course, level 5 cottages a loft; well, stall and lamp posts use guava fence and slab in every lineage | WORKS |
| V1a | Villages of guava wood, first step: level 2 houses get a guava-log door frame, level 3+ become guava cottages (HouseShape tiers, palette roles BEAM/PLANK/PLANK_SLAB) | WORKS |
| H4 | Night: an adult with a nest walks home at dusk and stays within 2 blocks until dawn (NightHomeGoal, Home.isNight) | WORKS |
| H3 | Worldgen nests: one Nest beside each burrow hole | WORKS |
| H2 | Dogs find, claim, check and release a Nest (NestHome, GoHomeGoal) | WORKS |
| H1 | The Nest block and its point-of-interest type | WORKS |
| V2a | Hardcoded dog home at spawn (replaced by H1/H2) | SUPERSEDED |
| W2 | Guava stairs, slab, fence | WORKS |
| W1b | An axe strips guava logs | WORKS |
| W1 | Guava planks, stripped log, wood, tags, recipes | WORKS |
| F3b | Pedigree Book portrait fixed (entity id) | WORKS |
| F2+F3a | Readable book text; portrait failure logged | WORKS |
| F1 | Guava fruit falls when its leaf goes | WORKS |
| FIX-PLAN | Plan for the bug/polish batch (docs/FIXES_PLAN.md) | PLANNED |
| T3 | Genetics Table: litter forecast spread | WORKS |
| T2 | Genetics Table: two slots + table window | WORKS |
| T1 | Genetics Table block holds and reads one Tissue Sample | WORKS |
| D8e | Worn Ear Tag drop | WORKS |
| D8d | The five legend drops | WORKS |
| D8c | Trophy Pedestal draws its item | WORKS |
| D8b | Trophy Pedestal holds one item | WORKS |
| D8a | Trophy Pedestal block | WORKS |
| S7b | Book page-turn sound | WORKS |
| S8 | Seed and Tissue Sample windows | WORKS |
| S6 | Calipers window (first networking) | WORKS |
| S7 | Click to jump, arrow keys | WORKS |
| S5b | Portrait hover lists drops | WORKS |
| S5 | Hover boxes + pair highlight | WORKS |
| S4 | Portrait of the animal in the sheet | WORKS |
| S3 | Pedigree Book becomes a window | WORKS |
| S2 | Entry v2 + plain-word chat readouts | WORKS |
| S1 | Plain words and real units (Plain, Units) | WORKS |
| D7b | Starlit Dust | COMPILES |
| D7a | Tissue Sample | WORKS |
| D6b | Mutant Marrow | WORKS |
| D6a | Mutagen | WORKS |
| D5 | Prime and Roast Haunch | WORKS |
| D4 | Sinew, whisker, fang | WORKS |
| D3 | Gene-signature drops | WORKS |
| D2 | Pelts + leather | WORKS |
| D1 | Drops engine, flesh, rabbit's foot, bone meal | WORKS |
| D0 | Drops plan and specs (docs only) | SPECIFIED |
| 7a | Guava log + decaying leaves | COMPILES |
| fix12 | Fix: infusing with a bred seed lost the bottle | WORKS |
| 6h | Meeting tree (level-5 grand tree) | COMPILES |
| 6g | House forms by biome | WORKS |
| 6f | Settlement styles by biome | WORKS |
| 6e | Village landmarks by level | WORKS |
| 6d | Settlements as a real Structure | WORKS |
| 6c | Footpaths | WORKS |
| fix9 | Fix: warrens on uneven ground | WORKS |
| 6b | Burrow houses | WORKS |
| dev | dev/options.txt copied to run/ | WORKS |
| 6a | Settlements: level = number of trees | WORKS |
| fix | Fix: surface warrens not in caves | WORKS |
| 5e | Pedigree Book by UUID + ear tags | WORKS |
| 5d | Ear Tag | WORKS |
| 4h | Command Block Guava (8th tier) | WORKS |
| 5c | Pedigree Book | WORKS |
| 5b | Recorded parents | WORKS |
| 5a | Calipers | WORKS |
| 4g | Warrens + guava trees | WORKS |
| 4f | Guava juice + infusion | WORKS |
| 4a-4e, session 6 changes | Bred stats, guava, seed, crop, tiers, bone meal | COMPILE and WORK |
| 1, 2 | Genome core, breeding | VERIFIED IN-GAME |
| 3 | Wild lineages | COMPILES |
