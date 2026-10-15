# Archived Claude workflow — superseded 2026-10-15

Historical context only. Current instructions are in `AGENTS.md`; current status is in `START_HERE.md`. Constraints below describe the previous environment and are not current instructions.

## Archived README.md

# Bunnidogs (NeoForge, Minecraft 26.2)

A small creature with a baby's face, a bunny's ears and a dog's body, and a full breeding/genetics mod around it: seeds and guavas, warrens and villages, drops, a Pedigree Book, a Genetics Table, homes.

**Run:** `./gradlew runClient`, then in a creative world `/summon bunnidogs:bunnidog`. **Build:** `./gradlew build` (JDK 25; first run downloads a lot).
**Play guide:** `docs/FEATURES.md` (how to use every feature; start with "Breed them").
**Dev client options:** `dev/options.txt` is copied to `run/options.txt` the first time the client runs from a fresh checkout (only if that file is missing).

Checks that need no Minecraft: `java tools/ParseAll.java src/main/java` (syntax of every source) and the genetics/layout self-check (commands in `docs/REFERENCE.md`, "Genetics self-check").
Regenerate art: `pip install pillow numpy`, then `python3 art/gen_bunnidog.py` (other textures: `art/gen_*.py`).

**Bunnidog model:** authored from the marked head contour, with sculpted facial asymmetry, folded human/rabbit ears, short fur and outward paws. See `art/bunnidog_front.png` or `art/bunnidog_model_preview.png`, open `art/bunnidog.bbmodel` in Blockbench (Free Model), or read `docs/BUNNIDOG_MODEL.md` for sources and regeneration details.

## Continuing with Claude
Upload this whole zip in a new conversation and say what happened with the last build, in your own words: **"it worked, continue"**, **"it compiled"**, or paste the compile errors. Claude reads `START_HERE.md` (current status, what to do, where everything is) and does the next small chunk.
Claude cannot compile: you build and run, Claude writes code and does syntax/logic checks only.

## Docs at a glance
`START_HERE.md` (read first) / `docs/CHUNKS.md` (status of every chunk) / `docs/ROADMAP.md` (what is next) / `docs/REFERENCE.md` (layout, conventions, process) / `docs/DESIGN.md` (game design) / `docs/API_NOTES.md` (verified Minecraft API facts) / `docs/*_SPECS.md` and `docs/WARREN_PLAN.md` (feature plans) / `docs/history/` (everything old).


## Archived START_HERE.md

# START HERE (for Claude, at the beginning of every conversation)

Read THIS FILE ONLY first. It tells you the status, what the user's words mean, the exact commands, and which other doc to open for which job.
Do not read `docs/DESIGN.md`, `docs/API_NOTES.md`, `docs/history/*` or any spec "to get oriented": open them only for the job named in the doc map below.

## 1. What this is (30 seconds)
**Bunnidogs**: a NeoForge mod for Minecraft 26.2 (Java 25, Gradle). A creature with a baby's face, bunny ears and a dog's body, plus a breeding/genetics game around it: 6 genes, bred stats, seeds planted like crops that hatch litters, a tier ladder of guavas, wild warrens/villages, drops, a Pedigree Book and Calipers windows, a Genetics Table, and (new) Nests that dogs claim as homes.
**Vocabulary (the word "warren" confused the user, so use it exactly like this):** a **warren** is where bunnidogs live: the dens under the guava trees, the **mouths** (mound entrances with a slope) on the surface and the tunnels between dens. A **settlement** (levels 1-5) is a warren plus what it has built on top (houses, paths, landmarks). Today (before U1) a warren is still only holes: see `docs/WARREN_PLAN.md`.
Pure game logic lives in `genetics/` and `layout/` (no Minecraft imports, self-checked here); everything else is thin Minecraft glue.

## 2. CURRENT STATUS (edit this block at the end of every session; it must always be true)
* **Latest visual update (2026-10-15): contour-led Bunnidog sculpt.** The head follows manually sampled points on the user's blue outline, with separate cheek/jaw contours and integrated facial anatomy. Human ears have folded helices/conchae; rabbit ears differ in length and angle; legs and outward paws are asymmetric. Photographic skin and short-fur source textures are generated from the reference and projected onto authored surfaces. Twelve 2048px coats, smooth per-vertex normals, sparse fur strands. Sources: `art/sculpt_bunnidog.py`, `art/gen_bunnidog.py`, `art/reference/`; loader: `BunnidogMesh.java`; skeleton: generated `BunnidogModelLayer.java`. Preview: `art/bunnidog_front.png` / `art/bunnidog_model_preview.png`; Blockbench Free Model: `art/bunnidog.bbmodel`. **Gradle build and a headless Minecraft load/render check passed; in-game appearance/motion/performance remain unreviewed.** Details: `docs/BUNNIDOG_MODEL.md`. Body proportions now follow the photo more closely: torso wider than the head, forward/full chest, thick forearms and broader paws, with a 0.5px lower stance and 5.1px hip height; previews `art/bunnidog_robusto_frente.png` / `art/bunnidog_robusto.png`. Rabbit ears refined against the photo: broader/shorter outlines, less outward splay, deeper cups, rolled rims and closed rounded tips; non-ear geometry and fur verified unchanged. Preview `art/bunnidog_orejas_frente.png`. Prior gameplay status below remains as recorded.
* **Newest delivered chunk: U6a, the advancement that explains "warren"** (session 46; data only, NOT COMPILED/NOT RUN). NEW `data/bunnidogs/advancement/enter_warren.json` (parent `minecraft:adventure/root`, trigger `minecraft:location` with `entity_properties` > `location.structures` = `bunnidogs:warren_village`, icon the Nest), lang keys `advancement.bunnidogs.enter_warren.title/description` and `structure.bunnidogs.warren_village` in `en_us.json`. No Java changed. ASSUMED: the field names follow the vanilla 1.21 format (copied from the pattern of vanilla `nether/find_fortress`; the NeoForge test data confirms `criteria`/`display`/`requirements`/`parent`/icon `{"id"}` only); the toast fires when the player stands in any piece of a surface warren structure (the piece box reaches 9 blocks round the tree and 10 down, so the den and the mouths count). Test: `/bunnidogs warren 1 surface` does NOT trigger it (it is a Feature, not the structure); use a naturally generated warren or `/locate structure bunnidogs:warren_village` and teleport. If the pack fails to load, the log names the file: delete it, nothing else depends on it.
* **Before U6a, last code chunk: U1, level 1 warrens with mouths and a den** (session 45, fourth turn; plan `docs/WARREN_PLAN.md`). Status: **NOT COMPILED** (no report yet), together with H3b (unconfirmed too). Files: NEW `layout/DenShape.java` (pure), NEW `worldgen/WarrenDen.java` (world glue), `worldgen/WarrenFeature.java` (`buildCluster` takes `Lineage style, boolean den`; the old holes + surface nests only run when no den was built), `worldgen/WarrenPiece.java` (asks for a den at level 1; `REACH` 6 to 9), `tools/GenomeCheck.java` (group `den()`, 2839558 checks, 0 failures). No new Minecraft name (only `Blocks.LANTERN`, `WarrenPalette` roles, `ModBlocks.NEST`, `getDestroySpeed`, `hasBlockEntity`, all already compiled elsewhere).
* **U1 in one paragraph:** a level 1 SURFACE warren (rolled level 1: the Feature kind, `/bunnidogs warren 1 surface`, and the structure piece with level 1) gets a 5x5x3 den 5 blocks under the tree (floor dy -5, two solid layers of ceiling, the ceiling lined with the style's fill block), 3-4 Nests in its corners (they REPLACE the floor block), a hay block with a LANTERN on it and one more hay block, and up to 2 MOUTHS: a 4-long 3-wide mound (style wall/roof, guava-log door posts and lintel) with a doorway facing AWAY from the tree 7 blocks from the trunk (8 or 9 on higher ground), a hollow inside, and a covered slope of 1-block steps (3 high) running back and down into the middle of one side of the den. Everything is tested BEFORE the first block is set (`WarrenDen.build` answers false and changes nothing otherwise): then the OLD holes and surface nests are built (rare fallback on steep or watery ground, and always for level 2+ and for the cave/Nether/End kinds, which U2/U6b will change).
* **PLAN ACCEPTED (session 45): `docs/WARREN_PLAN.md`**: warrens read as bugged 2-block holes; the plan makes them readable in chunks U1-U7; the user said "good" and changed no pick. **U1 is delivered (above); the next chunk is U6a (the advancement that explains the word) or U2 (dens and tunnels for levels 2-5), the user picks; "ur pick" = U2.**
* **Everything before H3b WORKS** (V1b is now WORKS: the user compiled and played it), except three chunks that COMPILE but whose in-game behaviour was never itemized: D7b (Starlit Dust), 7a (guava log/leaves), 6h (meeting tree).
* **To verify with U1 (do this first, it also tells whether H3b works):** `/bunnidogs warren 1 surface` on flat grass, several times. Expected: a tree, 3-4 dogs, and two mounds about 7 blocks from the trunk whose doorway faces away from the tree; walk in, down the slope (you can jump up the steps), into a lit room under the tree with 3-4 Nests. A few seconds after the dogs appear, one near a nest claims it (`/data get entity @e[type=bunnidogs:bunnidog,limit=1,sort=nearest]` shows `NestX/Y/Z`), walks down the slope and sleeps there at night (`/time set 13000`). ASSUMED (runtime): dogs path down a 1:1 slope of full blocks and fit the 1-wide tunnel; if no dog ever claims a den nest, look at R4 (reach) first, then at the slope. If the ground was too steep or wet, the old holes appear (the log says nothing about it; try another spot). **To verify with H3b:** `/bunnidogs warren 3` (or 4, 5): every house has a Nest in the floor of its hollow; a dog near the village claims one.
* **Next candidates (the user picks; the warren redesign U1 comes first, see above; "continue with ur pick" = you choose and say why in one line):** W3 (door, trapdoor, button, pressure plate, fence gate: new WoodType/BlockSetType API, the riskiest; V1c needs it) / D1-D4 (diets) / T4 (show the sample on the table). Full list: `docs/ROADMAP.md`.
* Open decisions waiting for the user: none (the warren plan's picks are accepted, but any can still be changed).

## 3. How to read the user's messages
The user replies loosely and says what happened to the LAST delivered chunk. They write English or Spanish: answer in their language (docs and code stay English).
| They say | It means / you do |
|---|---|
| "worked", "stuff worked", "things worked", "good", "it all worked" | The last NOT COMPILED chunk is now **WORKS**. Update its status (section 6), then do the next chunk. |
| "it compiled" | Last chunk is **COMPILES** (names/types fine, behaviour untested). Do the next chunk. |
| pasted compile errors / a log | Fix them FIRST, in the files the errors name, as a tiny chunk of its own. Then deliver the zip. No new feature. |
| "continue" with nothing else | Treat the last chunk as unconfirmed; say so in one line; keep the next chunk small. |
| "continue with X" / names a feature | Do X. If X is big or has no spec, write the plan in `docs/` first and do not code it until they say so ("plan first, then specifics, then code"). |
| "continue with ur pick" / "go with your picks" | You choose from the next candidates (or accept your own recommendations) and say why in one line. |
| "update the docs only" / "clean up" | Change ONLY docs and tooling. No new phase. Say what changed. |

## 4. Who does what
* **The USER compiles and runs** (`./gradlew build` / `runClient` in IntelliJ). Your sandbox has no Minecraft, no Gradle, no mod jars. So you can only (a) parse-check syntax and (b) self-check the pure Java. **Never claim a build passed or a feature works**; "NOT COMPILED" means the user has not compiled it.
* **Small chunks, one per turn**: one feature the user can compile and try in a couple of minutes (about 2-6 files). Say what you deliberately left out. If in doubt, cut the chunk, never the docs or the zip.
* The user dislikes: many checks ("stop doing so many checks"), gimmick/global-system mechanics, a surreal tone. They like: breeding/management/long-term-care skeletons and absurd escalation (silly tiers, silly items). Loose feedback is enough: never ask for a template.

## 5. Commands (copy-paste)
```bash
# 0. get the project (extract it: working on it is the whole point, the file-reading skill's "list, don't extract" does not apply)
cd /home/claude && unzip -q -o /mnt/user-data/uploads/bunnidogs-mod.zip && cd bunnidogs-mod
# 1. syntax of ALL sources (Minecraft glue included). Always run after code changes.
java tools/ParseAll.java src/main/java            # prints "parsed N files, 0 errors"
# 2. self-check of genetics/ + layout/ (+ greps of the glue). Run ONLY if genetics/ or layout/ logic changed; ONCE per turn; never for glue/resource/doc-only changes.
rm -rf /tmp/gcheck && java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d /tmp/gcheck src/main/java/com/bunnidogs/mod/genetics/*.java src/main/java/com/bunnidogs/mod/layout/*.java tools/GenomeCheck.java && java -cp /tmp/gcheck GenomeCheck     # last line: "N checks, 0 failures"
# 3. art (Pillow is installed): python3 art/gen_<thing>.py, then LOOK at art/<thing>_preview.png once.
# 4. deliver: always the same name, without run/ and build/
cd /home/claude && rm -f /mnt/user-data/outputs/bunnidogs-mod.zip && zip -qr /mnt/user-data/outputs/bunnidogs-mod.zip bunnidogs-mod -x 'bunnidogs-mod/run/*' 'bunnidogs-mod/build/*'
# then present_files on that zip. Reply SHORT: what it does / what is NOT compiled / what to try / what is next.
```
Sandbox facts: JRE 21 with the compiler module but no `javac` binary (hence the `-m jdk.compiler` form); Python + Pillow work; `github.com` and `raw.githubusercontent.com` were reachable in most sessions (not Maven/Gradle). To check a NeoForge API, sparse-clone it (steps in `docs/REFERENCE.md`, "Dev environment notes"); the clone has patches and tests but NO vanilla source bodies, so a vanilla-only name stays ASSUMED until the user compiles. `tools/GenomeCheck.java` is ~470 KB: `grep -n` it, never read it whole.

## 6. The loop for one chunk
1. Read this file; read the doc for the area you touch (section 7). For any Minecraft/NeoForge name: the QUICK INDEX at the top of `docs/API_NOTES.md` first; do not re-verify what it lists. Copy patterns that already COMPILED.
2. Pick ONE chunk. Pure logic into `genetics/` or `layout/`; Minecraft glue thin. At most ONE genuinely new Minecraft name, flagged as the likeliest compile problem.
3. Build in this order (so running out of room costs polish, never a broken zip): code and resources, then parse check, then (only if pure logic changed) a SMALL group in `tools/GenomeCheck.java` and one run, then docs, then the zip.
4. **Docs to touch, the whole list** (small edits):
   * this file, section 2 (status block);
   * `docs/CHUNKS.md`: one line for the chunk; flip the previous chunk to WORKS/COMPILES if the user said so;
   * the feature's spec file (or `docs/history/CHUNKS_FULL.md` if it has none): what the chunk did, files, what was tested, likeliest compile problems;
   * `docs/API_NOTES.md` QUICK INDEX row for anything newly looked up (SOURCE or ASSUMED);
   * `docs/DESIGN.md` "decisions made without asking" (short) and `docs/FEATURES.md` (one paragraph: how to use it in game);
   * `docs/history/SESSION_LOG.md`: 2-4 lines.
5. Deliver (section 5). Never mark anything COMPILES/WORKS on your own.

## 7. Doc map: which file for which job
| Job | Open |
|---|---|
| Status of any chunk, one line each | `docs/CHUNKS.md` (detail: grep the id in `docs/history/CHUNKS_FULL.md`) |
| What to build next, prerequisites | `docs/ROADMAP.md` |
| A Minecraft/NeoForge API name | `docs/API_NOTES.md` (QUICK INDEX, top; grep the lookup table) |
| Project layout, conventions, self-check details, art pipeline, dev environment, user preferences in full | `docs/REFERENCE.md` |
| How a feature is used in game (player view) | `docs/FEATURES.md` |
| Why the game is designed this way, decisions per chunk | `docs/DESIGN.md` (Contents at the top; `grep -n '^## ' docs/DESIGN.md`) |
| Warren redesign: mouths, dens, tunnels, cellars (U1-U7) | `docs/WARREN_PLAN.md` (PLAN, not coded) |
| Homes / Nests / H-chunks | `docs/HOME_SPECS.md` (ACTIVE: H1-H4 and H3b done) |
| Genetics Table | `docs/TABLE_SPECS.md` (T1-T3 done, T4 left; it wins over any other statement) |
| Drops / loot | `docs/DROPS_PLAN.md` (why) + `docs/DROPS_SPECS.md` (numbers, wins). All of D1-D8 done |
| Pedigree Book, Calipers window, "the sheet" | `docs/SHEET_SPECS.md` (S1-S8 done; wins over older Phase 8 notes) |
| Wood (W3-W4), villages in wood (V1), diets (D1-D4 of that plan) | `docs/FIXES_PLAN.md` sections 4, 5, 6 (F1-F3, W1, W1b, W2 done; V2 superseded by HOME_SPECS) |
| Old phase status, old session notes, old roadmap, old API research | `docs/history/` (never needed to continue) |


## Archived docs/REFERENCE.md

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
`paint_texture(coat)` plus the sculpt in `art/sculpt_bunnidog.py`: one texture PNG per coat (`bunnidog_<coat>.png`), `art/bunnidog.bbmodel`, the preview sheet and
`BunnidogModelLayer.java` and `models/entity/bunnidog.mesh`. Coat names in `COATS` MUST match the Coat alleles in `genetics/Gene.java` (the self-check
enforces it). A new coat = one palette entry + one allele; the face never changes. It converts Blockbench space (y-up) to Java
model space (y-down, ground at y=24; X/Z rotation signs flip). To change the shape or the painting, edit the
script and re-run — do not edit the generated Java or the PNG directly. Animation is separate (BunnidogModel).
The contour-led model uses authored anatomical surfaces from `art/sculpt_bunnidog.py`, photographic source textures from `art/reference/`, and twelve 2048x2048 atlases. `BunnidogMesh.java` renders per-vertex normals for smooth lighting. The Blockbench export is a Free Model mesh. `python3 art/preview_bunnidog.py` (Pillow + NumPy) renders the actual geometry and textures. Gene proportions (ears, legs, build) use per-part scale in `BunnidogModel.applyProportions`. See `docs/BUNNIDOG_MODEL.md` for the mesh format and validation.

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
