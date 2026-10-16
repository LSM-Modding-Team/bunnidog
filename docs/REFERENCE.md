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

## Development workflow

`AGENTS.md` contains the current Codex instructions; `README.md` has portable
build, preview, model-check and source-package commands. Work continues in the
checkout, using Git diffs and real Gradle builds. A ZIP is only a transfer format.
Update relevant docs and distinguish compile/headless/preview/gameplay evidence.
Old chunk sizes and user-only compilation rules are archived in
`docs/history/CLAUDE_WORKFLOW.md` and no longer apply.

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
art/bunnidog.bbmodel           generated; open in Blockbench (Free Model mesh) to preview/tweak
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
    Genome.java                56-bit seven-gene genome record; reads legacy six pairs as plain Diet, signed high-word sync; parse/toString, expressed/hidden, randomFounder, breed, mutation
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

## Focused verification

`./gradlew build` compiles the actual mod. `./gradlew checkBunnidogModel`
loads its mesh with the Minecraft runtime, exercises the smooth renderer and
checks normals, UVs, winding, foot contact across leg genes and baby reset.
The latter's source is in `tools/model/`, separate from the shipped mod.
Neither command launches the game or proves resource reload/gameplay behavior.

The legacy `tools/GenomeCheck.java` harness covers pure rules plus resource and
source-text assertions. Run it only for relevant genetics/layout changes, not
art, documentation or routine glue changes. On a Unix shell with JDK 25:

```sh
mkdir -p build/genetics-check
javac -d build/genetics-check src/main/java/com/bunnidogs/mod/genetics/*.java src/main/java/com/bunnidogs/mod/layout/*.java tools/GenomeCheck.java
java -cp build/genetics-check GenomeCheck
```

Keep new assertions focused on meaningful behavior and a few exact cases.
Existing source-string checks are legacy checks, not a pattern to expand.
Do not perform mutation/sabotage rounds or rerun large checks for show.

## Art pipeline (important — keep using it)
`python3 art/gen_bunnidog.py` (install `art/requirements.txt`) regenerates, from the `PARTS` list, the `COATS` palettes and
`paint_texture(coat)` plus the sculpt in `art/sculpt_bunnidog.py`: one texture PNG per coat (`bunnidog_<coat>.png`), `art/bunnidog.bbmodel`, the preview sheet and
`BunnidogModelLayer.java` and `models/entity/bunnidog.mesh`. Coat names in `COATS` MUST match the Coat alleles in `genetics/Gene.java` (the self-check
enforces it). A new coat = one palette entry + one allele; the face never changes. It converts Blockbench space (y-up) to Java
model space (y-down, ground at y=24; X/Z rotation signs flip). To change the shape or the painting, edit the
script and re-run — do not edit the generated Java or the PNG directly. Animation is separate (BunnidogModel).
The contour-led model uses authored anatomical surfaces from `art/sculpt_bunnidog.py`, photographic source textures from `art/reference/`, and twelve 512x512 atlases. `BunnidogMesh.java` renders per-vertex normals for smooth lighting. The Blockbench export is a Free Model mesh. `python3 art/preview_bunnidog.py` (Pillow + NumPy) renders the actual geometry and textures. Gene proportions (ears, legs, build) use per-part scale in `BunnidogModel.applyProportions`. See `docs/BUNNIDOG_MODEL.md` for the mesh format and validation.

## Conventions
* Mod id `bunnidogs`, package `com.bunnidogs.mod`. Register everything through `DeferredRegister`s in `registry/`.
* Client-only code stays under `client/` and is registered with `@EventBusSubscriber(value = Dist.CLIENT)`.
* Prefer data-driven and texture-layer solutions over new classes ("cheap to program, looks like a lot of content").
* For Minecraft APIs, consult `docs/API_NOTES.md`, then resolved Minecraft/NeoForge source and real compilation. Record useful findings with version and evidence; do not treat old sandbox limitations as present constraints.
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

- JDK 25 and the checked-in Gradle wrapper are the supported build environment.
  Dependency downloads need network access; configure the environment's proxy
  and trust store when necessary, without committing credentials or local paths.
- Resolved Minecraft sources are available through the Gradle/IDE development
  setup; prefer them and compiler errors over assumptions from older versions.
  `docs/API_NOTES.md` records useful API findings and their original evidence.
- NeoForge event javadocs can mention renamed classes: verify the declaration
  and real usage, not just a name in a comment.
- `java tools/ParseAll.java src/main/java` checks syntax only, as an optional
  fallback when dependency access prevents compilation.
- `run/` contains development settings and saves. Gradle's `runClient` remains
  running while the game window is open. Never delete worlds to fix a build.
- `packageProject` excludes build output, caches, local IDE state and runtime
  directories. Inspect the archive before sharing; never add secrets to source.

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
* Codex now builds locally when the environment supports it. The old user-only compilation and fixed chunk-size constraints are retired.
* **Plan first, then specifics, then code (session 13, drops):** for a big feature the user asked for a cohesive plan in `docs/` and the zip, then a walk through the specifics over the next messages, "still not coding anything". When the current request is explicitly planning-only, do not start implementation until requested; keep the plan's numbers as discussable starting values. (Third turn: the user answered "go with your picks" to Claude's recommendations on the last open questions, so on a small leftover decision Claude may propose a pick and the user can accept it in one line.)
* **Verification effort (session 6, said twice: "Stop doing so many checks, you're wasting time and resources" and then "once per change is still too much, your checks mean millions of checks"):**
  The default is NOT to run the self-check. Run it only when pure-logic code changed in a way a bug could hide in (`genetics/`, `layout/`), never after glue, resource, art or doc-only changes, and never more than once in a turn.
  No sabotage/mutation rounds, no "prove the check can fail" experiments, no repeated re-runs. New tests stay small: hundreds of samples, not tens of thousands (the layout tests were cut from 20,000 samples to 300 each); prefer a handful of exact assertions on real numbers.
  Do not report check counts as a selling point. Use Gradle to compile the Minecraft glue and report gameplay validation separately. If something is doubtful, say so in one line instead of running experiments.

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
