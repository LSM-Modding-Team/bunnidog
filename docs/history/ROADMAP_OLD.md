# Old roadmap (history)

The roadmap as PROJECT_CONTEXT had it until session 43. Superseded by `docs/ROADMAP.md`; kept for the per-phase notes on what was deliberately left out.

* **Phase 1 – Genome core:** DONE and VERIFIED IN-GAME (session 3).
  `Genome` record (6 genes x 2 alleles) in entity data/NBT; Mendelian inheritance + mutation roll; attributes derived
  from genes; per-gene look (coat texture variants, ear/leg/build proportions, temperament tail/ears).
  Deliberately NOT done yet: ears' heat tolerance, coat's cold resistance/camouflage (need biome/temperature hooks,
  Phase 3), build's carrying capacity (needs a carrying feature), fertility's visible cue and litter size (Phase 2;
  fertility currently only scales mutation chance), loyal temperament has no behaviour beyond looks (taming, Phase 2+).
* **Phase 2 – Breeding:** DONE and VERIFIED IN-GAME (user's report, session 4). Carrots, love
  mode, litters sized by Fertility, pups, per-pup Mendel + mutation. Deliberately NOT done: a Pedigree (who is whose parent is not
  recorded yet, only slot 0 = mother's allele / slot 1 = father's), visible fertility cue, gene-dependent growth time, a visible
  "too crowded" cue, breeding items other than the vanilla carrot.
* **Phase 3 – Wild lineages:** COMPILED OK by the user (session 4), lineage/founder logic tested here; in-game feedback (biomes, spawns) not reported yet. Eight lineages, biome
  tags, natural spawns, first-tick founder roll, 8 new coats. Deliberately NOT done: any mechanical effect tied to the environment (tundra cold
  resistance, desert heat tolerance, cave night vision + ore sense, swamp swim speed / webbed paws visual, nether fireproof, end "Phase" blink,
  coat camouflage), the biome/lineage line in a future Calipers tool, a bonus isolated-island lineage, structures. Cave/nether/end spawning
  is untested and the most likely thing to need tuning.
* **Phase 4 – Seed & Guava (4a-4f compile and work, 4g is written (not compiled yet); the user's own ideas, design in DESIGN.md "Seed & Guava loop"):**
  breeding drops ONE seed holding both parent genomes + quality; planted like wheat, the litter is born at harvest (Fortune helps); food becomes
  tiered guavas (found at wild "warrens", craftable up to absurd tiers) and guava tier = seed quality; guava juice infused with a seed gives
  effects from that seed's genes. Stats now GROW (user, session 4): genes set the direction a line drifts in per generation (a sturdy line gains Vitality, loses Swiftness),
  seed quality = guava tier (+ Fortune, only at harvest) raises the gains and shrinks the losses; see DESIGN "4. Bred stats".
  Steps (4a, 4b, 4c, 4d and 4e all compile, see their sections; the in-game results of Phases 3 and 4a-4e are still to be reported): 4a bred stats incl. Bloom = growth speed + the End allele `starborn` (pure Java, testable here; works with today's carrots at quality 0), 4b guava + food swap, 4c seed item + drop,
  4d seed crop + harvest (Fortune -> quality), 4e guava tiers -> quality, 4f juice + infusion (works),
  4g warrens (written in session 6, not compiled yet) + guava trees (replaces the Phase 3 random biome spawns). 4c has REPLACED the tested Phase 2 birth-of-pups code (the litter maths now lives in `Seed.hatch`, which the crop of 4d calls through `Bunnidog.hatch`).
* **ON STAND-BY (session 39): everything below (T3 forecast test report, T4, Leash, Phase 6 lineage tools, ...) waits for the batch in `docs/FIXES_PLAN.md`.**
* **Phase 5 – Tools:** 5a Calipers, 5b recorded parents and 5c Pedigree Book WORK (user, session 8). 5d Ear Tag and 5e (the Pedigree Book identifies animals by UUID and shows tags) WORK (user, session 8). The Genetics Table is being built in chunks T1-T4 (docs/TABLE_SPECS.md; T1 WORKS, T2 (two slots + the table window) WORKS, T3 (the litter forecast) written in session 38, not compiled; T4 the looks is next). Still to do: Leash.
* **TO DO (user, session 8; do NOT start until the user says so): a proper GUI for the Pedigree Book and the Calipers** instead of text printed in chat (a screen with the animal's genes, stats and family; needs menu/screen APIs not used anywhere yet: verify them first, see DESIGN "Ideas").
* **Settlements (Phase 6a-6d, the user's request of session 8; DESIGN "Phase 6 decisions"):** 6a trees = development level WORKS, 6b burrow houses WORKS, fix9 warrens on uneven ground WORKS, 6c footpaths (written, session 10; the village level pieces are still to do), then 6d life in the settlement. NOTE: the name "Phase 6" below is the older plan (artificial lineage tools); in the code and DESIGN "Phase 6a-6d" means settlements.
* **Phase 6 – Artificial lineage tools:** Gene Splicer, Purebred registry, Show Ring, Apex boss (including the Mutagen: quality no longer touches mutation chance).
* **Drops (session 13-16; D1 to D6b WORK (D6b = the Mutant Marrow + the Mutagen recipe), D7a (Tissue Sample, Calipers off-hand tooltip + reading) WORKS, D7b (Starlit Dust) COMPILES (in every zip since session 18), D8a-D8e (the Trophy Pedestal: block, held item, drawn item; the five legends; the worn ear tag) all WORK; `docs/DROPS_PLAN.md` = why, `docs/DROPS_SPECS.md` = numbers and decisions):** bunnidogs drop items by genome and stats (user's examples: fast -> rabbit's foot, several if very fast; more Vitality -> more bunnidog flesh; "be creative" with modded items). Death only for now (living harvest left possible, specs section 11). D1 (engine + flesh + rabbit's foot + bone meal + `/bunnidogs drops` + Calipers line) WORKS; D2 WORKS; D3 (gene signatures: vanilla items, adult player kills; engine only) WORKS; D4 (sinew, whisker, fang + string/Brush/arrow recipes) WORKS; D5 (the haunch: prime/roast haunch + cooking recipes) WORKS; D6a (the Mutagen, applied to a seed) WORKS; D6b (layer 3, the Mutant Marrow, and the Mutagen recipe) WORKS; D7a (layer 4, the Tissue Sample, the Calipers reading it and the off-hand tooltip) WORKS; D7b (Starlit Dust: the starborn row went live, and the dust ripens a planted crop) was written in session 18 (not compiled). Chunks D1-D8 are in the specs, each item ships with its use. Its drops feed the roadmap items that lacked a material source (Leash, Genetics Table, Mutagen, Gene Splicer).
* Bloat/theming layer (blocks, food, structures, villager profession, achievements) can be added at any point.
