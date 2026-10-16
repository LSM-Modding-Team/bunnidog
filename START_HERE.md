# Current project status

Read `AGENTS.md` for the Codex workflow and `README.md` for build/run commands.
This file records current evidence and pending work; historical process rules
in `docs/history/` do not apply. Follow the current request, not an automatic
next-chunk loop. Code and documentation use English; replies follow the user.

## Project

Bunnidogs is a NeoForge 26.2.0.88 mod for Minecraft 26.2, Java 25 and Gradle
9.2.1. Pure breeding and layout rules live in `genetics/` and `layout/`.
A **warren** includes dens, entrance mounds (mouths) and tunnels; a **settlement**
is the warren plus its surface structures (levels 1–5).

## Current visual work — 2026-10-15

The approved sculpt has a photographic baby face, separate human and rabbit
ears, compact robust body, outward paws and short fur. Keep those proportions
and imperfections; recognizability takes priority over a Minecraft-like style.
Sources and regeneration: `docs/BUNNIDOG_MODEL.md`.

The user compared Low and Ultra in Minecraft, prefers Low (including its nose),
and requested removing the other tiers and the graphics configuration. The
sole model is now the approved Low geometry: 2,172 quads / 8,688 vertices, twelve
512px coat atlases. Presets, distance switching and unused runtime assets are
removed. Face/ear/body vertex positions match the previous Low model exactly.

Horizontal bands had two causes: area-weighted head normals biased by unequal
ring spacing, and independently sampled photo patches. Head normals now follow
derivatives of the authored surface; neighboring head polygons share one
continuous UV chart. Maximum normal change across closely spaced front rings
fell from 0.478 to 0.038. No geometry was added. See `docs/GRAPHICS.md` and
`art/bunnidog_smooth_head.png` (nearest-filtered software preview).

Validation: real Gradle build and headless checks passed for the lightweight
mesh. The check covers winding, coat atlases, rendering, normals, gene/baby foot
contact and regression checks for head UV/lighting continuity. The user's
screenshots establish prior in-game Low/Ultra rendering, not confirmation of
this fix. Final in-game lighting and performance still need gameplay review.

**Branch preference:** work directly on `main`; do not create additional branches.

**Delivery preference:** do not attach or link JARs in chat. The user downloads
builds from GitHub. Actions now uploads the validated JAR as a run artifact.

## Current gameplay work — 2026-10-16

**W4a — guava signs and hanging signs** is implemented. Two craftable items
cover standing/wall signs and ceiling/wall hanging signs, with all native
rotations, attachments and waterlogging. Six guava planks plus a stick craft
three signs; six stripped guava logs plus two iron chains craft six hanging
signs. Both stack to 16 and appear in Functional Blocks. Recipes unlock when
the matching wood is obtained. Wall variants share their item's name and loot.

The four blocks use native sign classes and block entities, extended through
NeoForge's `BlockEntityTypeAddBlocksEvent`. A namespaced guava WoodType selects
the custom editor backgrounds; vanilla handles front/back text, dye, glowing
ink, wax, editing locks, packets and saves. In 26.2 the boards are regular block
models, so no custom renderer or model layer is needed. Generated cutout models,
UV textures, GUI backgrounds and item icons come from `art/gen_wood_signs.py`;
the nearest-filtered texture preview was inspected. Existing villages are not
dressed with generated signs in this batch.

`./gradlew build checkBunnidogModel --no-daemon` passes. `checkWoodAssets` now
covers all four sign states/attachments, model and texture links, editor sizes,
recipes/unlocks, tags and drops. Actual editor use, dye/glow/wax interactions,
hanging chains, support removal and text across save/reload need gameplay
review. Try `/give @s bunnidogs:guava_sign` and
`/give @s bunnidogs:guava_hanging_sign`. **W4b boats/chest boats remain pending.**
An isolated `runServer --initSettings` probe reached vanilla settings creation;
NeoForge then exited because no server thread was started. This is not a
successful dedicated-server or gameplay test; no player world was opened.

The preceding batch implemented **Diet D1–D4**, **U7** and **warren pantry loot**.
These are new changes with compiler/headless evidence; gameplay is pending.

- Diet is the seventh inherited gene: plain, grazer, forager, hunter, rare
  omnivore and seven lineage diets. All guavas still breed. Additional accepted
  foods heal 2 HP or grow an unlocked pup; healthy adults refuse them. Temptation
  follows the expressed diet. Food tags are extendable data.
- Injured adults with a Nest graze or hunt by day, within 16 blocks of home.
  Grazers use hay/grass/ripe wheat; foragers use ripe root crops. Crops reset
  without drops, hay stays, changed blocks respect mob griefing and protection
  hooks. Hunters take tagged adult prey (chickens/rabbits by default), heal 4 HP
  for their own selected kill, and exclude babies, players, bunnidogs, tame pets
  and allies. Searches are bounded; meals share a saved 30-second cooldown.
- Old six-pair animals, seeds, samples, books, table contents and infused juice
  load with plain/plain Diet. New writes use seven pairs / 56 bits. Client sync
  preserves a signed 32-bit high word; the original first six genes and all
  allele IDs stay put. Network version 4 requires updated matching clients.
  Sheet and forecast layouts fit all seven rows; approved creature geometry is
  unchanged. Diet adds no body/stat drift or juice effect.
- A fitted level-5 hall can have a 5x5x3 lower commons six blocks deeper, reached
  by full-block stairs, with four more Nests, a lamp and supply barrel. Unsafe
  terrain omits this floor; the upper nursery stays intact. New pantry/market/
  commons barrels contain modest food/guava loot; old containers are not refilled.

`checkDiets` is now part of Gradle `build` and covers old/new payloads, sync,
inheritance, food/home/prey policies, tags and seven-row page clearance. The
existing genetics/layout suite passes. `checkWarrenNetwork` covers upper/lower
Nests, stair access, slopes, deep fluids/protected blocks and chunk ownership.
`./gradlew build checkBunnidogModel --no-daemon` passes, including diet,
network, wood and model checks. A temporary layout-only preview from actual Page
data was inspected (test font and portrait placeholder, not a game screenshot).
GitHub delivery is verified for the pushed commit.
Use `docs/FIXES_PLAN.md` 6.4 for exact diets/rules and `docs/WARREN_PLAN.md` 19
for the lower commons. Gameplay checks should cover feeding versus love, a pup,
ripe crops with mob griefing on/off, a hunter's kill, night interruption and
save/reload; generate fresh level-5 villages to inspect U7 and barrel contents.

The user reports **"everything compiled and worked this run"** for the
`d7f39a9` release, confirming U4/U5/U6b/T4/W3/V1c as a working gameplay batch.
Individual dimension, chunk-order, reload, fluid/light and navigation cases
were not itemized; do not invent results for those separate cases.

The user has resumed Bunnidogs development; diets and their food goals are
implemented above, preserving old six-gene data. The user grants standing latitude for substantial coherent additions beyond the roadmap;
usage-aware continuation and delivery instructions are in `AGENTS.md`.

The U2/U3 batch received the user's "good, works well" gameplay feedback.
That confirms the batch broadly; natural chunk order, night home claims and
reload behavior were not individually reported.

The previous working release implemented these six stages:

- **U4:** fitted pantry/barrels under haystacks, well cisterns with glass-lined
  water shafts and dry ledges, market cellars with open hatches, farm entrances
  and small glazed skylights. Decorations do not automate storage or feeding.
- **U5:** a 7x7x4 hall under the level-5 grand tree, eight floor Nests, hanging
  roots and four hay/lantern positions. Spokes connect its satellite dens; a lit
  loop around the hall also links satellites at compatible elevations.
- **U6b:** cave, Nether and End floors use open stair cuts into dens instead of
  above-ground mounds, including level 1. End natural villages use the existing
  separate `bunnidogs:warren_end_village` structure; its biome tag stays separate
  from the Overworld structure.
- **T4:** stationary A/B Tissue Sample vials on the Genetics Table, plus a quiet
  click for successful interactions. Existing sample saves/sync are reused.
- **W3:** craftable guava door, trapdoor, wooden button, pressure plate and fence
  gate, using vanilla oak interaction/redstone rules and custom guava assets.
- **V1c:** new houses get initially open guava doors; levels 4–5 get shutters,
  level 4 cottages get glass windows, and level 5 has a hanging doorstep lamp.
  Open doors preserve dog access; no door-opening AI was added.

Terrain fitting remains conservative: fluids/protected blocks, blocked trunks,
unsupported shells or crowded entrances can omit rooms, ramps or a whole
network. U4 rooms require a flat landmark pad. Unfitted tree clusters retain old
holes and surface Nests. Natural structures save exact underground cells and
path exclusions, then write only chunk-owned columns. New cell kinds are
append-only. `WoodDetails` and `WarrenVersion` preserve old saved starts' house
geometry and End fallback. Existing villages and genome/entity saves are not
rebuilt or changed. Later conflicts can still cause a protected column to be
skipped.

Validation: `./gradlew build checkBunnidogModel --no-daemon` and the existing
pure genetics/layout suite pass. `checkWarrenNetwork` covers connectivity,
water containment, hall Nests/loop, cave entrances, slopes, cliffs, protected
terrain, cellar clearance with the new house details, and chunk partitions.
`checkWoodAssets` checks complete native state coverage, model/texture links,
recipes, wood tags and door loot. The generated guava texture preview was
inspected. The user subsequently confirmed compilation and gameplay for this
release. The individual cases above remain useful regression checks, rather
than separately reported test results.

Try `/bunnidogs warren 3 surface` and `5 surface` on open flat ground; inspect
rooms beneath landmarks, follow the hall loop, and use house/market/farm ramps.
In a cave or the Nether try `/bunnidogs warren 1` (no `surface`); in the End try
`/bunnidogs warren 5 surface`. Find fresh natural villages with
`/locate structure bunnidogs:warren_village` or
`/locate structure bunnidogs:warren_end_village`, cross chunk borders and reload.
Use `/time set 13000` and inspect dogs' `NestX/Y/Z` for home behavior.
See `docs/WARREN_PLAN.md` sections 16–18 for the new generation details.

## Earlier gameplay status and pending work

The current art work does not establish that every older gameplay feature works.
The historical ledger is `docs/CHUNKS.md`; its old NOT COMPILED labels describe
evidence at delivery, not the current ability to build the project.

- U6a adds the warren-entry advancement. Test in a naturally generated structure
  (use `/locate structure bunnidogs:warren_village`); the feature generated with
  `/bunnidogs warren 1 surface` does not trigger a structure advancement.
- U1 adds a level-1 surface den, 3–4 Nests, lantern and entrance mounds around
  the tree, falling back to old holes on unsuitable ground. H3b adds house Nests.
  Runtime pathfinding and home claims remain unconfirmed. Test
  `/bunnidogs warren 1 surface` on flat grass: enter the mounds, descend to the
  lit den, inspect nearby dogs' `NestX/Y/Z`, and set time to 13000 for sleeping.
  Levels 3–5 should also have claimable house Nests.
- Earlier user reports are preserved in the ledger. D7b, 7a and 6h had compile
  confirmation without itemized gameplay reports. Do not infer new evidence.
- U1–U7, Diet D1–D4 and W4a signs/hanging signs are implemented. W4b boats/chest
  boats remain candidates; coherent new work is authorized under the user's broader design
  permission. See `docs/ROADMAP.md`.

## Document map
| Job | Open |
|---|---|
| Status of any chunk, one line each | `docs/CHUNKS.md` (detail: grep the id in `docs/history/CHUNKS_FULL.md`) |
| What to build next, prerequisites | `docs/ROADMAP.md` |
| A Minecraft/NeoForge API name | `docs/API_NOTES.md` (QUICK INDEX, top; grep the lookup table) |
| Project layout, conventions, self-check details, art pipeline, dev environment, user preferences in full | `docs/REFERENCE.md` |
| How a feature is used in game (player view) | `docs/FEATURES.md` |
| Why the game is designed this way, decisions per chunk | `docs/DESIGN.md` (Contents at the top; `grep -n '^## ' docs/DESIGN.md`) |
| Warren redesign: mouths, dens, tunnels, cellars (U1-U7) | `docs/WARREN_PLAN.md` (U1–U7 implemented) |
| Homes / Nests / H-chunks | `docs/HOME_SPECS.md` (ACTIVE: H1-H4 and H3b done) |
| Genetics Table | `docs/TABLE_SPECS.md` (T1–T4 implemented; current evidence at top) |
| Drops / loot | `docs/DROPS_PLAN.md` (why) + `docs/DROPS_SPECS.md` (numbers, wins). All of D1-D8 done |
| Pedigree Book, Calipers window, "the sheet" | `docs/SHEET_SPECS.md` (S1-S8 done; wins over older Phase 8 notes) |
| Wood (W1–W4a done, W4b boats pending), villages (V1 done), diets (D1–D4 implemented) | `docs/FIXES_PLAN.md` sections 4, 5, 6 (V2 superseded by HOME_SPECS) |
| Old phase status, old session notes, old roadmap, old API research | `docs/history/` (never needed to continue) |
