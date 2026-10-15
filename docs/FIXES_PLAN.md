# FIXES_PLAN — bug and polish batch (session 39)

**Warrens (mouths, dens, tunnels, cellars, hall) are covered in `docs/WARREN_PLAN.md`. House materials and doors are covered here.**

**CURRENT (2026-10-15): F1–F3, W1/W1b/W2 and V1a/V1b work. W3 and V1c are implemented, with Gradle/resource/layout checks passing; gameplay is pending. V2 was superseded by HOME_SPECS.md. D1–D4 diets remain planned; W4 remains optional. See `docs/ROADMAP.md`.**

**Status (session 41): F1, F2/F3a, F3b, W1, W1b and W2 WORK (user). V2 is SUPERSEDED by `docs/HOME_SPECS.md` (a Nest block dogs claim like villagers claim beds; the user rejected the hardcoded home of V2a). Section 5.3 below is kept as history only. Everything else here is unchanged.**
**(Older line, session 40, fourth turn: F1, F2/F3a, F3b, W1 and W1b WORK; W2 (stairs, slab, fence; `art/gen_wood_parts.py`) was coded, NOT COMPILED. The FENCE GATE moved from W2 to W3 (it needs a `WoodType`). Next: W3, or V2 first if the user wants the dogs fixed sooner.)**
**Status (session 39, second turn): the user accepted every pick of section 7 ("go with your picks"). CODED, NOT COMPILED: F1 and F2/F3a. Everything else here is still plan only.** The user listed bugs and design problems after playing; the roadmap (T3 forecast spread
still uncompiled, T4, Leash, ...) is **ON STAND-BY** until this batch is done or the user says otherwise.
The old one-chunk-per-turn process is obsolete; follow `AGENTS.md`.
This file is the WHY and the ORDER; numbers marked *(pick)* are my starting values for the user to change.

## 0. What was found, in one table

| # | User's report | What the code does (read in session 39) | Chunk |
|---|---|---|---|
| 1 | Guavas don't break when the leaves above them break | `GuavaFruitBlock` only checks `canSurvive` inside `randomTick`, so a fruit under a broken leaf hangs in the air until a random tick hits it (minutes). Nothing reacts to the neighbour changing | F1 |
| 2 | Pedigree Book text is hard to read | (a) page text is drawn with `graphics.text(font, s, x, y, color)`, which draws the **drop shadow**; on a light parchment the shadow doubles every glyph. (b) `MUTED` ink `0xFF8A7A5C` on parchment `0xFFE8D8AE` has a contrast of **2.96:1** (readable text needs about 4.5); `GOOD` is 3.63. Those are the faint "base 4.0 hearts" lines in the screenshot | F2 |
| 3 | Bunnidogs aren't rendered in the book | The box shows the flat coat swatch, which is the **fallback** of `PedigreeScreen`: `animal == null` or `portraitFailed`. Both `buildPortrait` and `drawAnimal` swallow their `RuntimeException` **without logging**, so we cannot tell why. (S4 was marked WORKS from the user's "worked", which may have been the swatch showing; the S4 note says the swatch should never show) | F3 |
| 4 | Guava wood has no planks or plank crafts | Only `guava_log`, `guava_leaves`, `guava_sapling` exist. No planks, so `#minecraft:planks` has no guava member and nothing that takes "any planks" (crafting table, chest, sticks, our Genetics Table, boats...) accepts it | W1-W4 |
| 5 | Higher-level huts stay dirt while wells etc. get better | `WarrenPalette` picks WALL/ROOF/FILL **by lineage only**. The development level only decides how many houses and which landmarks exist (`PropShape.kindsFor`). So a level-3 meadow warren has a cobblestone well next to coarse-dirt mounds | V1 |
| 6 | Bunnidogs wander far from home | `WarrenFeature.spawnDog` only calls `setPersistenceRequired()`; the goals are `WaterAvoidingRandomStrollGoal` (unbounded). No home is stored anywhere | V2 |
| 7 | Hay blocks exist but bunnidogs don't eat wheat | Food is the item tag `bunnidogs:bunnidog_food` = the 8 guava tiers, used by love, tempting and pup feeding alike. Hay (level 2 haystack, level 5 plaza) and the level-5 farm are scenery (PropShape even says the carrots "are for show") | D1-D4 |

## 1. Order of work and why

Independent quick fixes first (they need no design), then the pieces that depend on each other.

| Order | Chunk | One line | Needs | Size |
|---|---|---|---|---|
| 1 | **F1** | fruit falls when its leaf goes | - | 1 file |
| 2 | **F2** | book text readable (no shadow, darker ink) **+ F3a logging** of why the portrait fails | - | 1 file |
| 3 | **F3b** | portrait fixed according to the log the user pastes | F3a | 1 file (maybe 2) |
| 4 | **W1** | guava planks, stripped log, wood, tags, log -> planks | - | table-driven files |
| 5 | **W2** | stairs, slab, fence, fence gate | W1 | table-driven |
| 6 | **W3** | door, trapdoor, button, pressure plate | W1 | new API (BlockSetType/WoodType) |
| 7 | **V2** | home anchoring (wild dogs stay near their settlement) | - | pure Java + entity glue |
| 8 | **V1a/b/c** | villages built of guava wood, material tier by level | W1, W2 (W3 for doors) | layout + palette |
| 9 | **D1-D4** | diets: gene, feeding, grazing, hunting | decision Q1 | the biggest item |
| last | **W4** | signs, hanging signs, boats (optional) | W3 | heavy, see 4.4 |

V2 can move before the wood if the user wants dogs fixed sooner: it does not touch wood.

---

## 2. F1 — the fruit falls with its leaves

**Cause:** see table. `canSurvive` is right but is only consulted by `randomTick`.
**Fix:** make the block react when the block above changes, using the shape-update hook of the block class, and keep the `randomTick` check as a safety net.
When the block above is no longer leaves: a RIPE fruit goes through `level.destroyBlock(pos, true)` so the existing loot table gives its guava; an UNRIPE one just vanishes (its loot table gives nothing anyway). That covers a player breaking the leaf, leaf decay, pistons and explosions.
**Verify before coding:** the 26.2 signature of the shape-update method (it changed in 1.21.2 to take a `ScheduledTickAccess` and a `RandomSource`; ASSUMED) in the NeoForge sources, and what worldgen's `setBlock` flags do to it (the tree places leaves before fruit, so it should be harmless).
**Files:** `block/GuavaFruitBlock.java` only. **Test:** break the leaf over a ripe fruit (a guava pops), over a green one (it vanishes); let leaves decay by removing the log; a freshly generated warren keeps its fruit.

## 3. F2 and F3 — the book

### F2 — legibility
1. Page text (`PedigreeScreen` the loop over `page.texts()`) is drawn **without** the drop shadow. The `graphics.text` overload with a trailing `boolean dropShadow` is the expected way (ASSUMED name/shape, verify in NeoForge's `GuiGraphicsExtractor` users). The hover box (light text on a dark box) can keep the shadow. Glyph widths do not change, so `Metrics` and every layout stay valid.
2. Darker muted and good inks, same hues, contrast against the parchment: `MUTED` `0xFF8A7A5C` -> `0xFF65563A` *(pick)*: 2.96 -> **5.05:1**; `GOOD` `0xFF2E7D32` -> `0xFF246B2E`: 3.63 -> **4.62:1**; `BAD` (4.63) and `HEAD` (7.19) stay. The hierarchy INK > HEAD > MUTED stays visible.
**Files:** `client/screen/PedigreeScreen.java`. No layout change.

### F3 — the portrait
The honest first step is **to find out why**, not to guess:
- **F3a (ships with F2):** log once, with the stack trace, at `BunnidogsMod.LOGGER.warn`, in both `catch` blocks of `PedigreeScreen`, and log when `Minecraft.getInstance().level` is null. The user opens a book, then pastes the `run/logs/latest.log` line.
- **F3b:** fix per that message. Suspects, in order: an exception while vanilla extracts the render state of an entity that was never added to a level (the renderer itself only reads `traits()`); something in `setBaby` / `setGenome` on a client entity; the entity-in-GUI call needing a different overload. The S4 note already warns that a failure *after* the call (vanilla draws entities later in the frame) can't be caught by our `try`.
- **Plan B if the entity route is not fixable cheaply:** draw a flat head from the coat texture (`textures/entity/bunnidog_<coat>.png`) with `graphics.blit(RenderPipelines.GUI_TEXTURED, ...)` (already in the API index, never used by us). Less nice than the turning 3D animal, but it can't fail the same way. Only if F3b stalls.
The same screen serves the Calipers, seed, sample and table windows, so one fix covers all of them.

---

## 4. Guava wood (W1-W4)

Goal: guava wood behaves like a vanilla wood set, **and** the villages (V1) use it. Table-driven like the rest of the mod: one list of "wood parts", every part gets its block, item, model, blockstate, texture, lang, loot and recipe from it, and the self-check loops over the list (resources exist, recipes are well-formed).
Most of the "usual crafts" come for free by joining the tags: `minecraft:planks` (crafting table, chest, barrel, sticks, ladder, bookshelf, boats, our Genetics Table, ...), `minecraft:logs_that_burn` (fuel), `wooden_stairs/slabs/fences/doors/trapdoors/buttons/pressure_plates`, `fence_gates`, `wooden_fences`. Data-only, no code.

- **4.1 W1 — the base:** `guava_planks`; recipe 1 log -> 4 planks (plus a tag `bunnidogs:guava_logs` for the recipe); `stripped_guava_log`; `guava_wood` and `stripped_guava_wood` (bark on all sides) with their 3-to-3 recipe; the tags above; axe-stripping log -> stripped log and wood -> stripped wood (NeoForge hook for tool-modified state; ASSUMED name, verify); `mineable/axe`; loot tables (drop themselves); art through `art/gen_guava_wood.py` (planks and stripped textures; look at the preview once).
- **4.2 W2 — shapes (works):** stairs, slab and fence. Recipes are the vanilla shapes with guava planks. Fence gate moved to W3.
- **4.3 W3 — mechanisms (implemented):** door, trapdoor, wooden button, pressure plate and fence gate. Thin subclasses expose the protected 26.2 constructors; `BlockSetType.OAK` and `WoodType.OAK` provide native wooden behavior/sounds without registering a duplicate wood type. The door uses `DoubleHighBlockItem` and lower-half-only loot. Five vanilla-shaped recipes, block/item wood tags and axe tags accompany the blocks. `art/gen_wood_interactives.py` uses checked-in 26.2 vanilla JSON templates and the custom guava plank texture; it generates the cutout door/trapdoor textures, models and preview. `checkWoodAssets` verifies all state combinations, model/texture links, recipes, tags and door loot. Preview inspected; actual placement, breaking, redstone, projectile activation, connectivity and sound need gameplay review.
- **4.4 W4 — optional, heavy:** signs, hanging signs, boat, chest boat. Signs need a block entity type that accepts our blocks, a sign screen/renderer hook and models; boats need an entity type, a renderer and a model layer. This is more work than W1-W3 together. **Recommendation: build W1-W3, then ask whether W4 is wanted.**
- Settlement use: V1 needs only W1 (+ W2 for slabs/stairs/fences, W3 for doors).

---

## 5. Villages

### 5.1 What is wrong, precisely
Level changes the COUNT of houses (2 per tree beyond the first) and the landmarks (haystack L2, well L3, stall L4, farm + plaza L5). It never changes the house MATERIAL, which is fixed per lineage (meadow: coarse dirt walls + grass roof). Props use the lineage's `RING_A`/`RING_B` (cobblestone) and `FENCE` (oak). So the village looks like it was built by two different civilisations.

### 5.2 V1 — one material ladder for houses AND props
Principle: **a landmark never uses a better material than the houses of the same level**, and guava wood is the common thread of every lineage (the per-biome stone/sand/mud/nether-brick styles the user liked in 6f stay as the *other* half of the walls).
Palette gets new roles: `BEAM` (guava log), `PLANK` (guava planks), `PLANK_SLAB`, `PLANK_STAIRS`, `FENCE` (guava fence), `DOOR` (guava door, W3). Starting proposal *(pick; the user changes it)*:

| Level | Houses | Landmarks |
|---|---|---|
| 1 | no houses (tree and burrows only) | none |
| 2 | earth mound as today, **plus** a guava-log door frame (log posts + lintel) | haystack (stays) |
| 3 | **guava cottage**: lineage wall material on the lower part, guava planks above, log corner posts, plank-slab roof, a door | well: the cobble ring stays, but the roof posts and frame become guava fence/planks |
| 4 | cottage with a stone-brick (lineage style) foundation course and shutters (trapdoors, W3) | stall built of guava planks/slabs |
| 5 | the village: cottages grow a second layer (loft) with windows and a lantern at the door | plaza and farm as today; the lamp posts become guava fence |

- A new `HouseShape.Form` (COTTAGE) keeps the **3x3 footprint** (so `Settlement` spacing is untouched); desert/mountain/Nether/End keep their TOWER form but gain guava beams/roof slabs/door.
- The tier is a function of saved `level`. V1c adds a backwards-compatible `WoodDetails` flag; old starts default to false and retain their previous house appearance. Existing generated villages stay unchanged.
- **V1b WORKS:** levels 4 (stone foundation) and 5 (loft), landmarks in guava fence and slab; see `docs/history/CHUNKS_FULL.md` (V1b).
- **V1a WORKS; coded in session 44:** see `docs/history/CHUNKS_FULL.md` (V1a). Decisions: the tiers are a parameter of `HouseShape.cells`, not a new `Form.COTTAGE`; levels 4 and 5 use the cottage until V1b; the tower keeps its roof and only gains log posts.
- Chunks: **V1a** palette roles + house tiers 2-3 (needs W1/W2), **V1b** tiers 4-5 and the prop materials, **V1c** doors/shutters once W3 exists. `/bunnidogs warren <level>` is the test command, as always.

**V1c implemented (2026-10-15):** new houses at levels 2–5 get guava doors,
initially open so dogs can still reach their floor Nests. No door-opening AI was
added; a player may deliberately close a door. Level 4 cottages gain three glass
windows, and levels 4–5 have guava trapdoor shutters on their three non-door
sides (at loft height for a level 5 cottage). Level 5 houses have a guava-log
bracket and hanging lantern above the doorstep, preserving two-block clearance.
Optional dressing is omitted where terrain blocks it. The pure `HouseDetails`
layout is separate from legacy `HouseShape`; `WarrenPiece` saves `WoodDetails`
and defaults it to false for old starts. Existing villages are not rebuilt.
Terrain checks cover cellar routes with both house styles and the new dressing;
in-game appearance and dog navigation still need review.

### 5.3 V2 — bunnidogs are anchored to their home
Today nothing remembers where a warren dog was born. Plan:
- **Home = a position + a radius.** Level 1: the trunk of the tree. Level 2+: the door of one house, dogs shared out round-robin over the settlement's houses (new pure `Settlement.homeFor(dogIndex)`, testable in the self-check). Radius *(pick)* 16 blocks from the home (a settlement reaches about 12).
- **Stored on the entity** as its own NBT (like `Mother`/`Father`), because it is unverified whether vanilla saves a mob's restriction (verify `Mob#restrictTo` / `isWithinRestriction` and the saved `home_pos` in the sources; if vanilla saves it, use it instead).
- **Effect:** the stroll goal only picks spots inside the radius (vanilla's random-position helpers honour the restriction; ASSUMED), plus a `ReturnHomeGoal` (below panic and tempt, above stroll) that walks a dog home if it ended up more than radius + 4 away. Panic, fleeing from the player and following a guava still work and may exceed the radius for a while; the dog comes back afterwards.
- **Only structure-spawned dogs have a home.** Bunnidogs hatched from seeds, tamed ones and `/summon`ed ones stay free, so farms and the breeding loop don't change.
- *(Optional, later: V2b, at dusk they go to their door and stand inside. Not in the first chunk.)*
- Files: new `layout` helper + test, `Bunnidog` (field, NBT, goal), `WarrenFeature.spawnDog` (pass the home). Dogs spawned by an already-generated world keep wandering (no home); a new world fixes them.
- **What V2a did instead (session 41):** no own NBT (vanilla saves `home_pos` / `home_radius` on every mob since 25w18a, and limits pathfinding to it) and no `Settlement.homeFor` (the home is the dog's own tree: `buildCluster` makes one tree's dogs per call, and a test shows every house is within 16 blocks of some tree). Files: `layout/WarrenShape` (`HOME_RADIUS = 16`), `worldgen/WarrenFeature.spawnDog` (`restrictTo(floor, ...)`), `entity/Bunnidog` (vanilla `MoveTowardsRestrictionGoal` at priority 6). V2b (dusk indoors) is still open and optional.

---

## 6. Diets (D1-D4)

### 6.1 The idea, restated
Genes decide what ELSE a bunnidog can eat. The **guava stays the favourite and the ONLY food that makes them breed**, so the whole seed/quality loop is untouched. Some bunnidogs hunt on their own. That finally gives the hay blocks and the level-5 farm a purpose.

### 6.2 Decision Q1 (the one that matters) — where does diet live?
| Option | What it means | Cost |
|---|---|---|
| **A (recommended): a new 7th gene, `Diet`** | The mod is about breeding for traits, so diet should be bred. Alleles by lineage identity (below). Old genomes read as the plain allele | Genome grows from 48 to 56 bits: `Gene.COUNT` 6 -> 7, the synced `DATA_GENES_HI` int must hold 32 bits (read it with `& 0xFFFFFFFFL`), text form 12 -> 14 hex digits (legacy 12-digit text is padded with `00`), one more row in the sheet, the forecast, `Plain`, `Readout`, founder recipes. Real work, all in places the self-check covers |
| B: derived from existing genes | feral temperament = hunter, wild lineage alleles = their foods, nothing new in the save format | Cheap, but diet can't be bred on purpose and the same gene means two things |

Starting alleles for A *(pick)*: **plain** (guava only, today's behaviour, the default), **grazer** (hay, wheat, grass), **forager** (carrots, potatoes, beetroot, apples, berries), **hunter** (raw meats; hunts). Lineage wild alleles: tundra = fish, cave = glow berries, desert = cactus/dried kelp, swamp = mushrooms, mountain = grazer+, nether = nether wart/fungi, end = chorus fruit. Foods are **item tags** (`bunnidogs:diet/grazer`, ...), so any modded food joins with a JSON line.

### 6.3 Chunks
- **D1 — genetics only (no gameplay):** the gene and its alleles, a pure `genetics/Diet` (expressed allele -> tag set), legacy-parse, the extra row on the sheet. Self-check group.
- **D2 — feeding:** split "breeding food" (the guava tag, as today) from "edible food" (guava tags + the diet tags of the *expressed* allele). A non-guava edible item fed by hand heals the bunnidog and feeds a pup (grows a little), never starts love. Tempt goal follows what that dog can eat.
- **D3 — grazing:** a `GrazeGoal`: a grazer/forager walks to a hay block or a ripe crop *inside its home radius* (V2) and nibbles (heals; a crop resets to age 0, hay is not consumed). Makes the haystack and farm meaningful; update `PropShape`'s comment about carrots.
- **D4 — hunting:** hunter dogs target small prey from an entity tag (`bunnidogs:prey`, e.g. rabbits and chickens; never players, pups or other bunnidogs), inside the home radius; a kill heals them. Gated in `canUse()` like the other goals (goals register before the genome exists).

---

## 7. Decisions (ALL ACCEPTED by the user: "go with your picks", session 39)
1. **Q1 Diet:** new 7th gene (A, recommended) or derived from existing genes (B)?
2. **Fruit:** ripe drops its guava when the leaf goes, unripe vanishes (my pick) — OK?
3. **Wood:** build W1-W3 first and decide on signs/boats (W4) afterwards (my pick) — OK?
4. **Village tiers:** is the table in 5.2 a good starting point (it will be walked through before coding, like the drops specs)?
5. **Home:** radius 16, no sleeping indoors in the first chunk (my pick) — OK?
6. **Order:** F1 -> F2/F3a -> F3b -> W1 -> W2 -> W3 -> V2 -> V1 -> D1-D4 as in section 1, or do the villages before the wood?

## 8. Left out on purpose
Leash, the Gene Splicer and other unspecified ideas remain outside this plan. T4 is now implemented in `TABLE_SPECS.md`. Hay is not made edible for *every* bunnidog on purpose: `plain` stays the default so existing lines behave as they do now.

## 9. API facts to verify BEFORE each chunk (not verified in session 39: the sandbox had no network)
`GuavaFruitBlock` shape-update signature (F1) · `GuiGraphicsExtractor#text` with `dropShadow` (F2) · why the portrait fails (F3a log) · tool-modified-state hook for stripping (W1) · `BlockSetType` / `WoodType` registration (W3) · `Mob#restrictTo`, `isWithinRestriction` and whether the restriction is saved (V2) · `Gene.COUNT`-dependent places (D1: grep `Gene.COUNT`).
