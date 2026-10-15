# REFERENCE: how the project works (stable facts)

Everything here changes rarely. For the current status read `START_HERE.md`; for what a chunk did, `docs/CHUNKS.md`.
Contents: What this is / Build and run / Development process (full) / Project layout / Self-check / Art pipeline / Conventions / Dev environment notes / User preferences.

## What this is
A NeoForge mod, **Bunnidogs**, for **Minecraft 26.2** (NeoForge `26.2.0.88`, Java 25, Gradle 9.2.1, ModDevGradle 2.0.147).
The bunnidog is a small creature with a baby's face, a bunny's ears and a dog's body (reference: a photo of a
pale, bald, wide-eyed creature with tall ears on a tan dog body, held in a hand).

## How the user builds/runs it
1. Install JDK 25 (Gradle can auto-download it via the foojay plugin in `settings.gradle`).
2. In the project root: `./gradlew runClient` (Windows: `gradlew.bat runClient`). First run downloads a lot.
3. In a creative world: `/summon bunnidogs:bunnidog`. `./gradlew build` makes the jar in `build/libs/`.
The user opens the folder in IntelliJ IDEA (recommended) as a Gradle project.

## Development process (small chunks), full text
The loop that worked in session 7 (Calipers -> recorded parents -> Pedigree Book): **one small chunk per turn, delivered as a zip, confirmed by the user's compile, then the next one.**

**1. Start of a session**
* Read `START_HERE.md` (status, doc map). Read other docs only for the area you are about to touch. Before any API lookup, the QUICK INDEX of `docs/API_NOTES.md` (do not re-verify what the index lists).
* Do not ask the user for a template or a status form: loose words are enough ("it compiled", "works", "continue with X"). If they name the next thing, do that; if they just say "continue", take the next item of the roadmap.

**2. Pick ONE chunk**
A chunk is one feature the user can compile and try in a couple of minutes. Rules of thumb (all met by the session 7 chunks):
* One item/block/command or one small behaviour change; about 2-6 new files, nothing sprawling. If an idea needs more, split it and do the first half.
* No new Minecraft API if it can be avoided: copy a pattern that already COMPILED (see the QUICK INDEX). If a new name is unavoidable, verify it in the NeoForge sources first (see the research playbook), keep it to ONE, and flag it as the likeliest compile problem.
* Logic that can be pure Java goes into `genetics/` (testable here); the Minecraft glue stays thin.
* Pick a size Claude is SURE to finish AND package (the user's words, session 7: "a length you are SURE you can manage to finish and present the updated .zip before running out of tokens"). When in doubt, cut the chunk, never the docs or the zip.
* Say what was deliberately left out (naming a lineage, a GUI, ...) so it is not mistaken for forgotten.

**3. Build it, in this order** (so that running out of room can only ever cost polish, never a broken zip)
1. Code and resources (item/class, registration, JSON, lang, recipe; art through its `art/gen_*.py` script, look at the preview image once).
2. `java tools/ParseAll.java src/main/java` (syntax of every file).
3. Add a SMALL group to `tools/GenomeCheck.java` (exact assertions on real numbers, resources by file reading, glue by grepping the key lines and their order), then run the self-check ONCE. If an old count-style check breaks because of a new file (e.g. the recipe-file count), update it. If a new test fails, first decide whether the TEST or the code is wrong (session 7: a test used invalid genomes). No sabotage rounds, no re-runs for show (see "User preferences").
4. Docs (below), then the zip, then `present_files`. The reply is short: what it does, what is NOT compiled, what to try, what is next.

**4. Docs to touch in every chunk (the whole list; keep each edit small)**
* `START_HERE.md`: the CURRENT STATUS block (last delivered chunk, what is unconfirmed, next candidates). This is the one file that must always be true.
* `docs/CHUNKS.md`: one line for the chunk (id, what, status). Flip the previous chunk's status when the user reports.
* The spec file of the feature (`docs/HOME_SPECS.md`, `TABLE_SPECS.md`, ...): a short "what chunk X did / differently from the plan" section. For a feature without a spec file: `docs/history/CHUNKS_FULL.md`, new row at the top (files, what was tested here, likeliest compile problems).
* `docs/API_NOTES.md`: a QUICK INDEX row for anything newly looked up (status SOURCE or ASSUMED until the user compiles).
* `docs/DESIGN.md`: a short "decisions made without asking" section (and a line in its Contents if it opens a new area); `docs/FEATURES.md`: one paragraph on how to use the feature in game.
* `docs/history/SESSION_LOG.md`: 2-4 lines for the session. `docs/ROADMAP.md`: only if the plan changed.
* Never mark anything COMPILES / WORKS on your own: only the user's report does that (see "Who does what" in START_HERE).

**5. Deliver**
* Zip the project WITHOUT `run/` and `build/` (`zip -qr ... -x 'bunnidogs-mod/run/*' 'bunnidogs-mod/build/*'`) to the outputs folder, always the same name `bunnidogs-mod.zip`, then present it.

**6. After the user reports**
* "Compiled well" -> flip the chunk to COMPILES in `docs/CHUNKS.md` and in the START_HERE status block; "works" -> WORKS. Compile errors -> fix them first, in the files they name, before any new chunk; then deliver the fixed zip (a fix is its own tiny chunk).
* If the user says "continue" while earlier chunks are still unconfirmed: say so in one line, keep the new chunk small, and mention which compile errors belong to which chunk (errors are told apart by file).

## Project layout

### Package map (generated from the tree in session 43; the older annotated list is below it)
```
genetics/  (28) PURE JAVA, no Minecraft imports: genome, alleles, breeding, stats, drops engine, plain-language text, home rules. Everything testable lives here.
    Allele, Breeding, Cross, Drift, Drops, EarTag, Founders, Gene, Genome, Growth, GuavaMemory, GuavaTier, Home
    Infusion, Juice, Lineage, Pedigree, Plain, Readout, Seed, Stat, Stats, TableUse, Temperament, Tissue, Traits
    Trends, Units
layout/  (13) PURE JAVA: settlement shapes (tree, warren, houses, paths, props) and the book/sheet/table page layouts.
    BookContents, ForecastSheet, HouseShape, Metrics, Page, PathShape, PropShape, SeedSheet, Settlement, SheetLayout
    TableSheet, TreeShape, WarrenShape
entity/  (3) the Bunnidog mob and its goals/home behaviour (thin glue over genetics/).
    Bunnidog, GoHomeGoal, NestHome
block/  (10) blocks and block entities: seed crop, guava fruit/sapling, Genetics Table, Trophy Pedestal, wood stripping.
    GeneticsTableBlock, GeneticsTableBlockEntity, GuavaFruitBlock, GuavaSaplingBlock, GuavaStripping, SeedCropBlock
    SeedCropBlockEntity, SeedCropHarvest, TrophyPedestalBlock, TrophyPedestalBlockEntity
item/  (10) items: guavas, seed, juice, Calipers, Pedigree Book, Ear Tags, Tissue Sample, Mutagen, Starlit Dust.
    CalipersItem, EarTagItem, GuavaItem, JuiceItem, MutagenItem, PedigreeBookItem, SeedItem, StarlitDustItem
    TissueSampleItem, WornEarTagItem
registry/  (7) DeferredRegisters: items, blocks, entities, features, structures, POI type, biome tags.
    ModBiomeTags, ModBlocks, ModEntities, ModFeatures, ModItems, ModPoi, ModStructures
worldgen/  (5) warrens/villages: WarrenFeature (the builder), WarrenStructure/WarrenPiece (surface villages), GuavaTree, WarrenPalette (styles).
    GuavaTree, WarrenFeature, WarrenPalette, WarrenPiece, WarrenStructure
command/  (1) the /bunnidogs debug commands.
    BunnidogsCommands
drop/  (1) the LivingDrops handler that turns genetics/Drops into item stacks.
    BunnidogDrops
network/  (5) the mod channel: payloads that open the sheet/seed/sample/table windows.
    ModNetwork, SamplePayload, SeedPayload, SheetPayload, TablePayload
client/  (2) client only: renderer, model, the Pedigree/Calipers/table window (screen/PedigreeScreen).
    BunnidogsClient, ClientScreens
```
Resources: `src/main/resources/assets/bunnidogs/{blockstates,items,models,textures,lang}`, `src/main/resources/data/bunnidogs/{recipe,loot_table,tags,worldgen,neoforge}`, `data/minecraft/tags` (wood/log/plank tags). Art is generated: `art/gen_*.py` (+ `*_preview.png`).
Tools: `tools/ParseAll.java` (syntax check of all sources), `tools/GenomeCheck.java` (self-check, ~470 KB: grep it, never read it whole), `tools/SheetDump.java`.

### Annotated file list (older, by phase; still accurate where it names a file)
```
art/gen_bunnidog.py            SINGLE SOURCE OF TRUTH for the model + textures (see "Art pipeline")
art/bunnidog.bbmodel           generated; open in Blockbench (Bedrock format) to preview/tweak
art/bunnidog_texture_preview.png  all coats side by side, 4x
genetics/Cross.java + layout/ForecastSheet.java (T3: the litter forecast and its two pages) + layout/TableSheet.java + network/TablePayload.java (T2: the table window's spreads and its payload) + block/GeneticsTableBlock.java + block/GeneticsTableBlockEntity.java (two slots since T2) + genetics/TableUse.java + art/gen_table.py (the 3 table textures + `table_preview.png`) + docs/TABLE_SPECS.md (the Genetics Table plan, T1), item/WornEarTagItem.java + art/gen_worn_ear_tag.py (the worn tag texture + client item + model + `worn_ear_tag_preview.png`, D8e), block/TrophyPedestalBlock.java + block/TrophyPedestalBlockEntity.java (D8a, D8b), client/renderer/TrophyPedestalRenderer.java + TrophyPedestalRenderState.java (D8c), art/gen_legends.py (the 5 legend textures + client items + models + `legends_preview.png`, D8d), art/gen_pedestal.py (Trophy Pedestal textures + `pedestal_preview.png`, D8a), art/gen_dust.py (starlit dust texture + client item + model + `dust_preview.png`, D7b), art/gen_tissue.py (tissue sample texture + client item + model + `tissue_preview.png`, D7a), art/gen_marrow.py (mutant marrow texture + client item + model + `marrow_preview.png`, D6b), art/gen_mutagen.py (mutagen texture + client item + model, D6a), art/gen_haunch.py (prime + roast haunch textures + client items + models + `haunch_preview.png`, D5), art/gen_pelts.py (the 12 pelts + tag, from the entity's COATS, D2), art/gen_materials.py (sinew, whisker, fang textures + client items + models, D4), art/gen_flesh.py (raw + cooked bunnidog flesh, D1), art/gen_guava.py, art/gen_seed.py, art/gen_seed_crop.py, art/gen_juice.py  the 16x16 textures (the 7 guava tiers: one palette each in TIERS, same keys as GuavaTier ids; seed; the 8 embryo crop pictures; the 2 juice bottles) + art/*_preview.png
tools/GenomeCheck.java         plain-Java self-check of the genetics package (see "Genetics self-check")
tools/ParseAll.java            syntax-only check of ALL sources incl. the Minecraft glue: java tools/ParseAll.java src/main/java
src/main/java/com/bunnidogs/mod/
  BunnidogsMod.java            @Mod entry, registers ModEntities on the mod bus
  registry/ModEntities.java    DeferredRegister for entity types + attribute registration + spawn placement (Phase 3)
  registry/ModItems.java       DeferredRegister.Items + DataComponents: the eight guavas from the tier table (4b, 4e: registerGuava, GUAVAS list, rarity), seed + component bunnidogs:seed_data (4c); tag bunnidogs:bunnidog_food; creative tab entries
  registry/ModBiomeTags.java   biome tag keys bunnidogs:lineage_<key>; lineageAt(level, pos); natural-spawn rule (Phase 3)
  item/PedigreeBookItem.java   the Pedigree Book (Phase 5c): log a bunnidog; right-click the air opens the window (Phase 8 S3, on the client); logic in genetics/Pedigree
  item/EarTagItem.java         the Ear Tag (Phase 5d): clip a registration code on a bunnidog; code in genetics/EarTag
  item/CalipersItem.java       the Calipers (Phase 5a): read a bunnidog / seed / planted crop into the chat; text from genetics/Readout (S6: a bunnidog opens its sheet window instead)
  drop/BunnidogDrops.java      LivingDropsEvent handler: a dying bunnidog gets the stacks rolled by genetics/Drops added to its drops (D1; honours the mobDrops game rule; Looting from the killer's main hand)
  layout/                      PURE JAVA (settlement shapes): TreeShape, WarrenShape (4g; H3: `nests`), Settlement (6a, 6b: trees, house spots), HouseShape (6b), PathShape (6c: footpaths); add layout/*.java to the self-check compile command
  worldgen/                    GuavaTree, WarrenFeature (settlement builder: trees, houses, paths, bunnidogs; the ground lookup `groundAt`, `layPath`)
  genetics/                    PURE JAVA, NO Minecraft imports (keeps it testable outside the game)
    Readout.java               the Calipers' text (Phase 5a; 5d adds the ear tag line)
    Drops.java                 what a bunnidog drops: the hide (layer 0, D2: `peltId`, `hideCount`, `hideMean`, `givesHide`) the six stat-line definitions and the 24 signature rows (`ALL_SIGNATURES`; `SIGNATURES` = the 23 whose item exists (D4: the fang row went live); `LINES` = all six lines (D5 activated the haunch); D6b: layer 3, `rareCount`, `givesMarrow`, `marrowChance`, `MARROW_*`; `canDrop` hides E = 0 lines from the tools; D3: `chance`, `statFactor`, `worthFactor`) (flesh, haunch, foot, sinew, whisker, bone meal; all active since D5), `yieldOf`, `expectedCount`, stochastic `count`, `roll`, `expected`, `simulate`, Calipers text (D1)
    EarTag.java                the Ear Tag's registration code `BD-XXXXXX` from a UUID (Phase 5d)
    Pedigree.java              the Pedigree Book's log, generations and text form (Phase 5c; 5e: entries carry the animal's UUID and ear tag)
    Gene.java                  the 6 genes + their allele TABLES (table position = saved id: append-only!)
    Allele.java                record(id, name, dominance, value, weight, lineage): common (weight>0), rare (weight 0, mutation-only) or wild (weight 0 + lineage)
    Lineage.java               MEADOW TUNDRA DESERT SWAMP CAVE MOUNTAIN NETHER END (key = tag name)
    Founders.java              founder recipes per lineage (signature alleles + chances), Founders.roll(lineage, rng)
    Stat.java, Stats.java      the five bred stats (Vitality Swiftness Leap Senses Bloom) as whole points; curves; text form "3,-1,0,2,12"
    Trends.java, Drift.java    which way each allele pushes the stats per generation; quality shaping, pair drift, inheritance, starborn bonus
    Growth.java                Bloom growth-speed tick maths (age ticks added to a pup)
    GuavaTier.java             the guava ladder as an enum (level 0-6 = ladder place, quality 0/1/2/4/7/12/20 = what the tier is worth, item id, display name, previous/next, average of two tiers' qualities): the TABLE the items, art, recipes and checks follow (4e)
    GuavaMemory.java           what one bunnidog remembers of the guava that put it in love (tier + 600-tick timer, cleared when the age leaves 0; save/load); mirrors vanilla's love mode without isInLove() (4e)
    Juice.java                 what an infused juice does: allele -> vanilla effect ids, amplifier/duration rules, caps, hearing radius, tooltip text (4f)
    Infusion.java              the liquid pup in a bottle: genome + stats + quality, text form, Infusion.roll(seed) (4f)
    Tissue.java                the payload of a Tissue Sample: genome + stats, text `genome/stats` (D7a)
    Plain.java                 plain language for the Animal Sheet: the genome in words, the look phrase, what each allele does (S1)
    Units.java                 real units for the sheet: hearts, blocks/s, jump height, hearing blocks, minutes (S1; speed constant ASSUMED)
    Seed.java                  seed payload record (both parents' genomes + stats, quality, mutagen flag): text form (optional 4th part `mutagen`, D6a), Seed.of, Seed.hatch (the litter), mutationChance (50% floor when treated), tooltip summary (Phase 4c)
    Genome.java                48-bit genome record, parse/toString, expressed/hidden, randomFounder, breed, mutation
    Traits.java                phenotype after dominance + stat multipliers
    Breeding.java              litter size / cooldown / crowding guard from Fertility (Phase 2, tested)
    Temperament.java           LOYAL / SKITTISH / FERAL / STOIC
  registry/ModBlocks.java      DeferredRegister.Blocks + BlockEntityTypes: the seed crop block and its block entity (Phase 4d)
  item/JuiceItem.java          guava juice (plain and infused, one class): drink animation, effects by registry id, tooltip; item/SeedItem.java: planting (useOn) + infusing (use) (4f)
  block/SeedCropBlock.java     the planted seed: AGE 0-7, random-tick growth, farmland/dirt soil, EntityBlock + BonemealableBlock (session 6), no loot table (4d)
  block/SeedCropBlockEntity.java  holds the planted Seed as text (tag "Seed")
  block/SeedCropHarvest.java   BreakBlockEvent handler: unripe -> the seed comes back; ripe -> Bunnidog.hatch with the Fortune bonus (4d)
  item/GuavaItem.java          one rung of the guava ladder: holds its GuavaTier, tierOf(stack), the "Seed quality N" tooltip (4e)
  item/SeedItem.java           the seed item: payload = Seed text in the data component bunnidogs:seed_data; tooltip; create(seed) / read(stack); useOn plants it on farmland (4c, 4d); use: infusion (4f) and applying a Mutagen (D6a)
  item/MutagenItem.java        the Mutagen: a plain item with a tooltip; it is applied through SeedItem.use (D6a)
  item/StarlitDustItem.java    Starlit Dust: right-click an unripe seed crop and it ripens at once (D7b)
  item/TissueSampleItem.java   the Tissue Sample: a vial with the genes of a dead bunnidog (component `tissue_data`); read by the Calipers (D7a)
  entity/Bunnidog.java         the mob (vanilla Animal since Phase 2): synced genome (2 INTs), NBT "Genome", attributes from genes,
                               gated goals, guava breeding (mobInteract remembers the fed guava's tier in a GuavaMemory, 4e), a mating drops a Seed whose guava quality is the average of both tiers (spawnChildFromBreeding), static hatch(seed) births its litter (4c),
                               first-tick founder roll by biome (tick(), Phase 3)
  entity/NestHome.java         the nest a dog has claimed (H2): look / reach-check / claim / check / release, timer-driven; pure rules in genetics/Home
  command/BunnidogsCommands.java   /bunnidogs genome (debug read-out of nearest bunnidogs), /bunnidogs lineage (lineage of the biome you stand in),
                               /bunnidogs seed (read the seed in your hand), /bunnidogs hatch [bonus] (debug shortcut: hatch it at your feet without waiting; the crop of 4d hatches through the same `Bunnidog.hatch`)
  client/BunnidogsClient.java  client-only: registers model layer + renderer (@EventBusSubscriber, Dist.CLIENT)
  client/ClientScreens.java    the one door from items to client windows (Phase 8 S3): openBook(Pedigree); only called inside level.isClientSide() (S6 adds openSheet(Entry), the Calipers window)
  client/screen/PedigreeScreen.java  the book window (S3): draws what layout/BookContents lists with fill + text; Previous / Next buttons; S4: draws a throwaway client-side Bunnidog in the portrait box (InventoryScreen.renderEntityInInventoryFollowsAngle), flat swatch as fallback
  network/SeedPayload.java, network/SamplePayload.java  (S8) the same payload shape for a seed and a Tissue Sample; layout/SeedSheet.java  (S8) the seed window's three spreads
  network/SheetPayload.java, network/ModNetwork.java  the mod's one network channel (Phase 8 S6): server -> client "open the sheet of this animal" (payload = the text of one Pedigree.Entry); ModNetwork.register is on the mod bus, ModNetwork.sendSheet sends it
  layout/Metrics.java, Page.java, SheetLayout.java, BookContents.java  pure layout of the book (S3; S5 adds the hover regions to Page/SheetLayout): text widths, the draw list of a page, one animal = one spread, contents + paging
  client/renderer/BunnidogRenderer.java     MobRenderer; texture per coat; fills the render state from Traits
  client/renderer/BunnidogRenderState.java  snapshot the model reads (coat, ear/leg/build scale, temperament)
  client/model/BunnidogModel.java           HAND-WRITTEN animation + gene proportions + pup size (setupAnim)
  client/model/BunnidogModelLayer.java      GENERATED geometry — never edit by hand
src/main/resources/assets/bunnidogs/    items/{<7 guavas>,seed}.json (client items), models/item/{<7 guavas>,seed}.json, textures/item/{<7 guavas>,seed}.png (guava ids = the ids in GuavaTier), blockstates/seed_crop.json (8 ages -> 8 models, session 6), models/block/seed_crop_stage0-7.json (parent block/cross), textures/block/seed_crop_stage0-7.png, lang/en_us.json, textures/entity/bunnidog_<coat>.png (generated, 12 coats)
src/main/resources/data/bunnidogs/      tags/item/bunnidog_food.json (what bunnidogs eat: the 7 guavas), recipe/<6 guava tiers>.json (crafting, 4e), tags/worldgen/biome/lineage_<key>.json + warren_surface.json (which biomes belong to which lineage / get surface warrens),
                               neoforge/biome_modifier/add_warren_<surface|underground|nether|end>.json, worldgen/configured_feature + placed_feature (warrens, 4g; the old natural spawns are gone)
src/main/templates/META-INF/neoforge.mods.toml   templated from gradle.properties
docs/                          see START_HERE.md (the doc map)
```

## Genetics self-check (run after ANY change in genetics/ or the coat list)
(Do NOT run it by default: only when pure logic changed, at most once per turn, see "User preferences". No sabotage rounds unless the user asks.)
From the project root (needs only a JDK/JRE with the compiler module; no Gradle, no Minecraft):
```
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d /tmp/gcheck src/main/java/com/bunnidogs/mod/genetics/*.java src/main/java/com/bunnidogs/mod/layout/*.java tools/GenomeCheck.java
java -cp /tmp/gcheck GenomeCheck          # prints "N checks, 0 failures"; exit code 1 on failure
```
(With a normal JDK use `javac` instead of the first command.) It also verifies that every Coat allele has a texture file, that every wild allele is handed out by exactly its lineage's recipe,
that mutation never creates wild alleles, and the `/summon` cheat sheet (end of this file) (1,249,333 checks at the end of session 4, 1,249,970 at the end of session 5, 1,250,139 after the first half of session 6, 1,261,162 at the end of session 6).
It is deliberately NOT under `src/test`: Gradle 9 fails a build whose test task finds no tests.

### How the self-check is organized (extend it the same way; this is the pattern every phase since 4b used)
* **Pure logic** goes into `genetics/` and gets exhaustive checks (e.g. every pair of guava tiers, not a few samples). If a piece of entity behaviour has any logic in it, move that logic into `genetics/` so it can be tested (`GuavaMemory` came out of `Bunnidog` for this reason).
* **Resources** are checked by reading the files: one loop over the table (`GuavaTier.values()`, the coat list, ...) asserts each entry has its client item, model, 16x16 PNG, lang key, tag entry, recipe and art palette. Adding a table entry without its files fails the run.
* **Minecraft glue** (cannot be compiled here) is checked by grepping the source for the exact lines that matter, including their ORDER (e.g. the held tier is read before `super.mobInteract`), and for things that must be gone (e.g. the old hard-coded `0.0D` quality).
  Such checks are brittle by design: when you rename something on purpose, update the grep. Helpers in `GenomeCheck`: `compact(json)` (strips all whitespace: never use it on text with spaces), `pngSize(bytes)`, `near(..)`, `check(..)`.
* **Test the checks once per new group** (a check that never failed proves nothing): copy `src tools art` to `/tmp/mut`, break one thing with `sed` (wrong recipe ingredient, `average` as a sum, delete a guard, swap two lines), recompile and run, expect "N failures", restore from the real tree, repeat.
  A sabotage that is NOT caught is either a real hole or an equivalent mutant (same observable behaviour, e.g. a redundant condition): decide which before moving on. Session 5 ran 10 of these for 4e.
* Checking the glue for syntax: `java tools/ParseAll.java src/main/java` ("parsed N files, 0 errors"). It does not resolve names; the user's compile does.

## Art pipeline (important — keep using it)
`python3 art/gen_bunnidog.py` (needs `pip install pillow`) regenerates, from the `PARTS` list, the `COATS` palettes and
`paint_texture(coat)`: one texture PNG per coat (`bunnidog_<coat>.png`), `art/bunnidog.bbmodel`, the preview sheet and
`BunnidogModelLayer.java`. Coat names in `COATS` MUST match the Coat alleles in `genetics/Gene.java` (the self-check
enforces it). A new coat = one palette entry + one allele; the face never changes. It converts Blockbench space (y-up) to Java
model space (y-down, ground at y=24; X/Z rotation signs flip). To change the shape or the painting, edit the
script and re-run — do not edit the generated Java or the PNG directly. Animation is separate (BunnidogModel).
Model budget: 10 cubes, 64x64 textures, all four legs share one UV island, ears/head/body have separate islands so
coat/face overlays can be layered later. Gene proportions (ears, legs, build) are done with per-part scale in `BunnidogModel.applyProportions`, not extra
models.

## Conventions
* Mod id `bunnidogs`, package `com.bunnidogs.mod`. Register everything through `DeferredRegister`s in `registry/`.
* Client-only code stays under `client/` and is registered with `@EventBusSubscriber(value = Dist.CLIENT)`.
* Prefer data-driven and texture-layer solutions over new classes ("cheap to program, looks like a lot of content").
* When unsure of a Minecraft API, FIRST read the QUICK INDEX in API_NOTES.md (most names are already settled there, with the exact file to open), THEN look it up in NeoForge's repo tests instead of guessing,
  and write the result back into the index. Prefer designs that need only names that are already COMPILED; if a name is unverifiable (`isInLove()`), build the state yourself in pure Java.
  The sandbox could reach `github.com` and `raw.githubusercontent.com` in sessions 1-3 (not Maven, not Gradle servers); in session 4 bash had
  NO network at all, and `web_search` + `web_fetch` on docs.neoforged.net worked instead. Check which one you have before planning research.
* Say clearly when something is unverified/uncompiled. Do not claim it works until the user has run it. A compile confirms names and types only; data files (recipes, models, tags, blockstates) are open until the game has loaded them.
* Tables drive content: one enum/table per ladder (coats, lineages, guava tiers) -> items, textures, lang, recipes and self-check loops all follow it. Adding a rung = one table entry + its files; the self-check fails until they all exist.
* `genetics/` stays free of Minecraft imports (use `java.util.random.RandomGenerator`; the entity adapts with
  `new Random(entity.getRandom().nextLong())`). Game logic that can live there, should, so it stays testable.
* Allele tables in `Gene.java` are the SAVE FORMAT: never reorder/delete; append (max 16 per gene). Dominance ranks are
  unique per gene. Entities save the genome as a readable string (`Genome:"00-01-20-00-03-11"`).
* Which biome belongs to which lineage is DATA (biome tags), never hard-coded. Vanilla biome ids in those tags are `"required": false`, so a
  biome that does not exist in some Minecraft version is skipped instead of crashing the world load.
* `registerGoals()` runs inside the super constructor (before the genome exists), so goals register for every
  temperament and gate themselves in `canUse()`.

## Dev environment notes
* NeoForge 26.x RENAMED events and its javadoc still mentions the old names (`BlockEvent.BreakEvent` is now `event.level.block.BreakBlockEvent`; the javadoc of `BlockSnapshot` still says BreakEvent). When verifying an event, grep for its CLASS DECLARATION (`class .*Break.*Event`), never for a name found in a comment.
* Session 4 part 7: bash reached github.com again. A sparse clone of NeoForge 26.2.x (tests, patches, src; 33 MB, under a minute) is the best way to verify APIs:
  `git init; git remote add origin https://github.com/neoforged/NeoForge; git config core.sparseCheckout true; printf 'tests\npatches\nsrc\n' > .git/info/sparse-checkout;
  git fetch --depth 1 origin 26.2.x; git checkout FETCH_HEAD`, then grep `tests/` for real usage. Vanilla sources are still absent (only patches), so purely vanilla names stay unverifiable.
* Checking the Minecraft glue without Minecraft: `java tools/ParseAll.java src/main/java` (syntax only). Compiling the glue with javac and no Minecraft jars gives hundreds of
  missing-symbol errors; grep their `symbol:` lines for names of OUR classes to catch cross-class mistakes (weak: javac skips checks on expressions of unresolved types).
* Authoring sandbox (Claude): JRE 21 WITHOUT `javac` but WITH the compiler module -> use
  `java -m jdk.compiler/com.sun.tools.javac.Main`. `apt` cannot install a JDK there. Python + Pillow are available.
  A parse-only syntax checker for all sources is a ~20-line JavacTask#parse program (see session 2 approach).
* The dev client runs from `run/` inside the project (separate from the user's normal .minecraft). Settings and test
  worlds persist there. Exclude `run/` and `build/` from zips.
* First `runClient` takes ~10+ min (it recompiles Minecraft); later launches are fast. Gradle shows "executing"
  until the game window is closed — that is normal, not a hang.

## User preferences (learned)
* The user writes in Spanish ("Continuar", "continúa") or English (session 5, part 2) depending on the day: answer in the language of their message; these docs and the code stay in English.
* When told "update the information only / leave the next phase for later" (session 4 part 9, session 5 part 2): change ONLY docs, start no new phase, and say what was changed.
* Noticed in session 5: re-verifying the same APIs every session was wasting effort; the user asked for an index of findings (API_NOTES Quick Index). Keep it current.
* Tastes: likes management/long-term-care/breeding skeletons; dislikes gimmick or global-system mechanics.
* Gives loose, unstructured feedback ("A sounds fun, F sounds tedious") — that is enough, don't ask for a template.
* Said they misunderstood the word "surreal" and asked to drop it: **do not push a surreal tone**. The remaining
  wish is a mod that has a lot of content across all feature types, built cheaply, unified by a theme.
  Session 4: "some (ideas) are really absurd, that's the soul of the mod" -> absurd escalation (nether star guavas, ...) is WELCOME; it is about
  silly game objects, not a surreal writing tone.
* Wants everything set up so it can be resumed from this zip without redoing work.
* **A feature must be readable in the world, not only in the docs (session 45):** the user took the warrens (a tree and a few 2-deep holes) for a worldgen bug and did not know the word. Before building anything the player is meant to understand, ask "would someone who has never read the docs know what this is and that it is on purpose?" Prefer a visible structure that explains itself (a doorway, a lit slope, dogs that use it) over a hidden mechanic; explain the one new word in the game (advancement, lang) and in FEATURES. They also like "underground and surface" settlements with creative variety, built level by level.
* **The user is the one who compiles** (session 7: "I'm the one who compiles, it seems that was never stated in the docs"); see "Who does what" at the top. And: **small chunks**, one per turn ("Development process").
* **Plan first, then specifics, then code (session 13, drops):** for a big feature the user asked for a cohesive plan in `docs/` and the zip, then a walk through the specifics over the next messages, "still not coding anything". Do not start the code of a plan until the user says so; keep the plan's numbers as discussable starting values. (Third turn: the user answered "go with your picks" to Claude's recommendations on the last open questions, so on a small leftover decision Claude may propose a pick and the user can accept it in one line.)
* **Verification effort (session 6, said twice: "Stop doing so many checks, you're wasting time and resources" and then "once per change is still too much, your checks mean millions of checks"):**
  The default is NOT to run the self-check. Run it only when pure-logic code changed in a way a bug could hide in (`genetics/`, `layout/`), never after glue, resource, art or doc-only changes, and never more than once in a turn.
  No sabotage/mutation rounds, no "prove the check can fail" experiments, no repeated re-runs. New tests stay small: hundreds of samples, not tens of thousands (the layout tests were cut from 20,000 samples to 300 each); prefer a handful of exact assertions on real numbers.
  Do not report check counts as a selling point. The user compiles and plays the glue themselves, so effort goes into reading the real API sources before writing glue code, and into short, clear docs. If something is doubtful, say so in one line instead of running experiments.

## Debug cheat sheet (copied from the old Phase 3/4 notes; `tools/GenomeCheck.java` verifies the /summon genomes)
Genome text = ears coat legs build temperament fertility, two hex digits each (`/summon bunnidogs:bunnidog ~ ~ ~ {Genome:".."}`). Other debug commands: `/bunnidogs genome | lineage | seed | hatch | warren [1-5] [surface] | drops [simulate n]`.
* Wild lineages: tundra `44-44-00-44-00-00`, desert `55-55-44-55-00-00`,
  swamp `00-66-00-00-00-11`, swamp moss/glow carrier `00-6b-00-00-00-00` (two of them give glow pups 1 in 4), glow `00-bb-00-00-00-00`,
  cave `66-77-00-00-11-00`, mountain `00-88-55-11-00-00`, nether `00-99-00-00-22-00`, end `00-aa-00-00-11-00`. (The self-check verifies that
  every one of these expresses what this table says.)
* Cheat sheet: stocky pair (sturdy line) `00-00-00-11-00-00`; "Bloom build" (starborn + stocky + long ears) `11-00-00-11-00-44`; an End-like
  founder `00-aa-00-00-00-44` (void coat, starborn).
* Seed: stocky pair, quality 1.5: `/give @s bunnidogs:seed[bunnidogs:seed_data="00-00-00-11-00-00|00-00-00-11-00-00|1.5"]`;
* `/bunnidogs hatch` still exists as a no-waiting shortcut. Test cheat sheet: `/gamerule randomTickSpeed 100` makes crops grow in a minute or two; `/setblock ~ ~ ~ bunnidogs:seed_crop[age=7]`
