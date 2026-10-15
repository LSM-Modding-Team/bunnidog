# START HERE (for Claude, at the beginning of every conversation)

Read THIS FILE ONLY first. It tells you the status, what the user's words mean, the exact commands, and which other doc to open for which job.
Do not read `docs/DESIGN.md`, `docs/API_NOTES.md`, `docs/history/*` or any spec "to get oriented": open them only for the job named in the doc map below.

## 1. What this is (30 seconds)
**Bunnidogs**: a NeoForge mod for Minecraft 26.2 (Java 25, Gradle). A creature with a baby's face, bunny ears and a dog's body, plus a breeding/genetics game around it: 6 genes, bred stats, seeds planted like crops that hatch litters, a tier ladder of guavas, wild warrens/villages, drops, a Pedigree Book and Calipers windows, a Genetics Table, and (new) Nests that dogs claim as homes.
**Vocabulary (the word "warren" confused the user, so use it exactly like this):** a **warren** is where bunnidogs live: the dens under the guava trees, the **mouths** (mound entrances with a slope) on the surface and the tunnels between dens. A **settlement** (levels 1-5) is a warren plus what it has built on top (houses, paths, landmarks). Today (before U1) a warren is still only holes: see `docs/WARREN_PLAN.md`.
Pure game logic lives in `genetics/` and `layout/` (no Minecraft imports, self-checked here); everything else is thin Minecraft glue.

## 2. CURRENT STATUS (edit this block at the end of every session; it must always be true)
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
