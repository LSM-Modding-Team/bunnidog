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

**Settlement L1/L2:** Feeding Troughs hold up to 16 matching food items with
saved components, four visible fill levels, main-hand insertion, empty-hand
inspection and sneak-empty withdrawal. Injured adult residents find loaded,
reachable matching food within eight blocks and the existing home boundary.
One meal heals 2 HP and shares the saved 30-second forage cooldown, without love
or world/crop edits. Craft `P P/PIP/PPP` (any planks, iron). Healthy adult
residents also visit nearby vanilla bells from clock ticks 6,000–9,000 for up to
20 seconds, resting a minute afterward. Gatherings keep homes/villager tickets
intact and yield to danger, food, transport and night. Player-placed/native bells
work with existing settlements; generation layouts are unchanged.
Build and the native stock/food/home/gathering case pass. There are fourteen
mod server scenarios plus native sanity. The trough preview was inspected;
client visuals and multiplayer routines remain gameplay review. See
`docs/SETTLEMENT_LIFE.md`. Existing genome/save formats and protocol 6 persist.

**Care C4 — quick care readout:** sneak-use Calipers on a bunnidog for a server
chat snapshot of sampling, fleece, grooming and splicing readiness. It explains
age/health/build/Senses requirements and shows recovery as minutes:seconds when
eligible. Splicing readiness still requires donor materials. Normal use keeps
opening the genetics sheet. No animal, item, timer, save or protocol change.
Actual build checks pass; manual client interaction remains pending.

**G3 — Show Ring:** a craftable Judging Stand awards bronze/silver/gold ribbons
for balanced stable bred adults. Require recorded birth parents, all seven
gene pairs fixed, half health, and every bred stat at least +5/+20/+50. Lead an
adult within six blocks and use the stand empty-handed or with a lead. Sneak-use
with both hands empty for a read-only scorecard. Saved `ShowAward` prevents the
same/lower tier being claimed again across stands/reloads; future positive IDs
are preserved. Judging keeps the animal and its leash intact. Each ribbon holds
the winning adult's complete pedigree snapshot, opens its sheet in air, and can
stand on the existing Trophy Pedestal. Gold grants Champion Line once per player
(100 XP). Recipe: ` B /PGP/PWP` (book, any planks, gold ingot, any wool). The
registry unlocks the recipe. See `docs/SHOW_SPECS.md`.

**Care C3 — loose whiskers:** use a vanilla Brush on a healthy adult bred to
Senses +10 for one whisker. It costs one Brush durability and starts ten minutes
of saved animal recovery (`GroomWait`), preserving genes/stats/health/age. Pups,
injured animals, insufficient points and recovery refuse without wear. Creative
keeps the tool but retains recovery. Old animals load ready. Hover Senses on a
sheet for the rule. Existing death drops are unchanged; see `docs/CARE_SPECS.md`.

Combined Gradle build/server checks pass, including native judging and grooming,
award/refusal boundaries, non-destructive preview, creative/tool break behavior,
advancement progress, saved awards/recovery and ribbon/pedestal snapshots.
There are thirteen mod scenarios plus the native sanity test. Generated stand/
ribbon textures and the geometry preview were inspected; client gameplay remains
pending. No new item payload, sync field, genome format or protocol change.

**A2 — swamp/desert/End protection:** expressed moss coat now prevents Poison,
sand prevents native cactus damage, and void prevents Levitation. Hidden copies
grant nothing, and other damage/effects retain their role. Setting an immune
genome clears its already-active blocked effect; changing away restores normal
susceptibility. Existing native protection/effect hooks still apply. Readouts
explain the benefits; allele order, stats, models, saves and protocol 6 stay the
same. The extended native adaptation case covers actual effect/damage behavior,
hidden alleles and changing genes; combined build/server evidence passes.
See `docs/CARE_SPECS.md`. Manual gameplay remains pending.

**T5/T6 + G2 — breeding laboratory and stable lines:** two Genetics Table samples
now give five spreads: A, B, existing look/stat forecast, inherited-copy odds,
and both-direction splice plans. Carry/fixed odds include hidden recessives;
hovers show the chance of at least one success in an uncrowded litter. The
Two Copies heading also forecasts a pup with all seven pairs fixed. Splice plans
use the actual tool rule and show exact pairs, expression and refusal without
spending samples. Reading with a Gene Splicer in the main hand opens that page.
The existing `A|B` payload and saved slots are unchanged.

Craft a **Purebred Registry** from a vanilla book, gold ingot and whisker.
Use it on a bred adult with recorded parents and matching copies in all seven
genes, including Diet. It keeps the first representative's snapshot for up to
24 distinct stable genomes; air opens a linked index and the normal animal
sheets. Registration is reusable and non-destructive, awarding **Breeds True**
and 25 XP once per player. Hover GENOME on any animal/sample sheet for fixed-pair
progress and the remaining mixed genes. It uses the existing pedigree component;
the ordinary Pedigree Book and network protocol 6 remain unchanged.

Native creative item interaction supplies a temporary copy. Registry writes and
Gene Splicer selection on animals now target the actual held item. Focused
server cases establish creative registration, qualification/refusals, snapshots,
native advancement progress, item/table saves and read preservation. The combined
build/server run passes all eleven mod scenarios plus native sanity; the focused
breeding-lab check covers probability math, registry capacity and page clearance.
The legacy genetics run found one obsolete H1 assertion expecting the pre-H5
plain Nest block; that assertion was corrected and its H1 section rechecked.
Registry icon/layout previews are separate from client gameplay, which remains
pending. See `docs/REGISTRY_SPECS.md`, `docs/TABLE_SPECS.md` 7 and
`art/lab_preview.png` (substitute font/layout only).

**G1 — Gene Splicer:** a rare 16-use tool copies a donor's expressed allele
into one selected gene of a healthy adult. Sneak-use in air or on a bunnidog
cycles the seven genes; selection is shown in the tooltip/action bar and saved
on the tool. Put a donor Tissue Sample in the other hand and carry Mutagen;
normal use on the recipient consumes one of each and one tool use. The less
dominant recipient copy is replaced (first copy on a tie). An already-carried
donor allele is refused, so breeding is still needed to fix a homozygous pair.
Bred stats are not copied; relative health and birth ancestry are preserved.
The animal needs one minute between splices, saved as remaining `SpliceWait`.
Recipe: `DMD / CIC /  I ` (diamond, Mutant Marrow, copper, iron). Native tool
interaction, seven-gene isolation, costs/refusals, stats/health, selection and
animal saves pass the focused server case. The new icon preview was inspected.
See `docs/SPLICER_SPECS.md`. Current network protocol is **6**, requiring
matching updated clients/server for the new selection component.

**U8 — warren wayfinding:** newly planned underground networks have guava wall
signs for dens, the nursery, pantries, cisterns, markets, farms, the lower commons
and fitted exits where a safe wall fits. Green glowing text is readable in the
tunnels. The pure planner saves relative positions, facing and label IDs in a
separate `Wayfinding` array on the TUNNEL piece; old starts load with no labels.
Each sign and its support share a chunk. Placement follows that chunk's tunnel
columns, skips missing supports/occupied blocks and preserves existing or edited
signs on repeat calls. Signs remain editable. Normal command/feature generation
uses the same planner. Existing cell IDs and bounding boxes are unchanged.

**Care C1/C2:** the new 32-use Sampling Syringe takes the existing Tissue Sample
from a living adult or pup at half health or better. It preserves health, age,
genes, stats and breeding state; the animal needs five minutes of recovery.
Craft with iron ingot / glass bottle / stick diagonally, or use
`/give @s bunnidogs:syringe`. Healthy adults expressing woolly build now yield
two white wool with vanilla shears every ten minutes. Pelts and existing death
drops retain their role. Sampling/fleece recovery timers are saved separately
as remaining ticks; missing old tags mean ready. Fleece readiness is synced to
clients; current matching clients/server use network version **6**. The animal's
approved model stays intact. `art/gen_syringe.py` generates the new tool assets;
its nearest-filtered icon preview was inspected.

**Adaptations A1:** expressed ember coat grants fire/lava immunity; expressed
snow coat or woolly build prevents powder-snow freezing; expressed hoofed legs
halve native fall damage. Hidden alleles confer no benefit. These traits can be
crossbred, following ordinary dominance; they are not tied to spawn biome.
Gene hover/readout text explains the effects. Existing allele IDs, genome
formats, bred-stat curves and approved creature geometry are unchanged.

`./gradlew build runGameTestServer --no-daemon` passes. New focused checks cover
saved/chunk-owned labels and player edits, native syringe use/cost/recovery,
live pup genetics, creative use, real fire/fall damage and native fleece drops,
refusals, save/load and regrowth. The existing server, resource, diet and layout
checks pass with the additions. Client visuals, natural structure chunk order
and full world unload/reload remain gameplay review. See `docs/CARE_SPECS.md`
and `docs/WARREN_PLAN.md` 20 for the new rules.

**W4b — guava boats and chest boats** completes the guava wood set. Five guava
planks in the native boat recipe craft a two-seat boat; combining it with a
chest crafts a one-seat boat with 27 inventory slots. Both stack to one and
appear in Tools & Utilities. Native 26.2 vehicle classes preserve paddling,
passenger rules, inventories, names and their own item drops. Native model
layers use guava hull/paddle atlases; chest panels remain the native chest.
Regenerate with `python3 art/gen_wood_boats.py`; the texture preview was inspected.

**H5 — transport and rehoming:** leads and vehicle rides suspend home/food
navigation and Nest searches/checks, retaining the saved home. Pending saved
leashes also suspend those goals while vanilla restores the holder. After
release/disembarking, normal behavior resumes. Nest checks skip unloaded chunks.
While leading dogs, right-click a Nest with an empty hand or a lead to rehome
the nearest led adult within eight blocks. A route and a free destination
ticket are required; only then is the previous ticket released. Occupied,
unreachable and invalid destinations preserve the old claim. The lead stays
attached; a quiet click and action-bar message confirm the move. Pups cannot
claim homes. The existing `bunnidogs:nest` ID, saved coordinates, POI and assets
are preserved.

Development-only server GameTests cover boat seating, full chest inventory and
genetic item components through save/load, exact vehicle/content drops, native
sign validity/tickers and two-sided text/color/glow/wax persistence, lead/ride
goal suspension and restoration, and Nest reassignment/ticket protection.
Tests and their empty structure fixture are excluded from the shipped JAR.
The server suite now contains fourteen mod scenarios (plus the native runner's
always-pass sanity test); current local evidence is recorded in the batch above.
GitHub Actions now runs `build checkBunnidogModel runGameTestServer` before
uploading the artifact. `checkGuavaBoats` is part of `build` and checks native
layers/paddles/chest geometry, renderer texture paths, crafting/unlocks and tags.
Client rowing appearance, sign editors, multiplayer input and natural chunk
unload/reload remain manual gameplay checks.

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
recipes/unlocks, tags and drops. Server tests now establish native block-entity
validity, editing-lock tickers and two-sided text/color/glow/wax save/load.
Actual editor use, dye/glow/wax interactions, hanging chains and support removal need gameplay
review. Try `/give @s bunnidogs:guava_sign` and
`/give @s bunnidogs:guava_hanging_sign`. W4b boats/chest boats are implemented above.

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
  allele IDs stay put. Network version 6 requires updated matching clients.
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
- U1–U7, Diet D1–D4, W1–W4 and H5 transport/rehoming are implemented;
  coherent new work is authorized under the user's broader design
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
| Homes / Nests / H-chunks | `docs/HOME_SPECS.md` (H1–H5 and H3b implemented) |
| Living samples, woolly fleece and inherited environmental protection | `docs/CARE_SPECS.md` |
| Donor-allele introgression, Gene Splicer controls/costs | `docs/SPLICER_SPECS.md` |
| Stable bred-line registration and qualification | `docs/REGISTRY_SPECS.md` |
| Judging Stand, show tiers and winning ribbon snapshots | `docs/SHOW_SPECS.md` |
| Feeding Trough and daytime bell gatherings | `docs/SETTLEMENT_LIFE.md` |
| Genetics Table | `docs/TABLE_SPECS.md` (T1–T4 implemented; current evidence at top) |
| Drops / loot | `docs/DROPS_PLAN.md` (why) + `docs/DROPS_SPECS.md` (numbers, wins). All of D1-D8 done |
| Pedigree Book, Calipers window, "the sheet" | `docs/SHEET_SPECS.md` (S1-S8 done; wins over older Phase 8 notes) |
| Wood (W1–W4 done), villages (V1 done), diets (D1–D4 implemented) | `docs/FIXES_PLAN.md` sections 4, 5, 6 (V2 superseded by HOME_SPECS) |
| Old phase status, old session notes, old roadmap, old API research | `docs/history/` (never needed to continue) |
