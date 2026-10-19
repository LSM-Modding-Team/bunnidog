# Bunnidog drops: the specifics (session 13, second and third turn)

Current endgame addition: G4 now consumes one of each legendary drop from a
Trophy Pedestal ring to summon a source-line Apex. A credited player victory
drops its lineage-bearing Apex Crest instead of ordinary bunnidog materials;
`mobDrops` applies. See `APEX_SPECS.md`. Historical future references below
record the original drops design.

**CURRENT (session 43): ALL DONE: chunks D1-D8e are coded and work (D7b compiles; its behaviour was not itemized). The status lines below are from the planning sessions. Chunk status: docs/CHUNKS.md.**

**Care update (2026-10-16):** C1 adds live Tissue Samples using a Sampling
Syringe; C2 gathers renewable woolly fleece with native shears; C3 uses a Brush
to collect one loose whisker from healthy Senses +10 adults every ten minutes. These are
separate care interactions with saved recovery, health/age gates and modest
yields. C5 also gathers one glowstone dust from expressed glow adults and one
Starlit Dust from expressed starborn adults with Bloom +20; qualifying Brush
products share one wear and the same grooming recovery. The existing death-drop
engine/tables are unchanged. See
`CARE_SPECS.md` for exact rules and server evidence. This supersedes the older
"death only for now"/future-syringe wording below.

**Status: SPECIFICATION ONLY, ALL DECISIONS MADE. Nothing here is coded.** It refines `docs/DROPS_PLAN.md` (read that first for the why).
Where the two differ, THIS file wins (section 12 lists the differences). All numbers are starting values, kept in one
place so they are cheap to change. **Third turn:** the three open questions were settled (rows 13-15 below), a contradiction about
Mutant Marrow and pups was fixed (row 16), and a Calipers tooltip was added to D7 (row 17). Nothing is open any more (section 13);
the next step was chunk D1, which **was written in the fourth turn (NOT COMPILED)**; see `docs/CHUNKS.md` (D1) and `docs/history/CHUNKS_FULL.md` and DESIGN "Drops D1 decisions" for what it does and the small choices it made (layer 0 moved to D2 with the pelts; sinew/whisker/haunch defined but inactive; the yield function is `Drops.yieldOf`).

## 0. Decisions so far

| # | Question (DROPS_PLAN section 9) | Decision | Who |
|---|---|---|---|
| 1 | Death only, or living harvest too? | **Death only FOR NOW.** The engine is built so a living harvest can be added later without redoing anything (section 11). | user (emphasis on "for now") |
| 2 | Pelts | **One modded pelt per coat** (12 items), built so "interesting stuff" can hang off them later (section 3). | user |
| 3 | Legends | Kept, **gated behind a Trophy Pedestal**, and they are the offering for the future Apex boss (section 7). | Claude (user: "you go over this") |
| 4 | Pups | Stat lines x0.25; no pelt, no signatures, **no marrow**, no legends. Only the Tissue Sample besides the stat lines. | Claude, default kept (marrow added in the third turn, row 16) |
| 5 | Tissue Sample | Guaranteed when the killer holds the Calipers in the OFF hand, else 4%. A non-lethal Syringe is the later "living harvest" version. | Claude |
| 6 | Rare layers need a player kill | Yes (`isRecentlyHit()`). | Claude, default kept |
| 7 | Hidden (carried) recessives | Do not count. Parked. | Claude, default kept |
| 8 | Where the tables live | In Java (`Drops.java`), like `Juice.DEFS`. | Claude, default kept |
| 9 | Experience scaling | No. | Claude, default kept |
| 10 | Raw flesh effects | None (harmless, like vanilla rabbit). | Claude, default kept |
| 11 | Flesh flavour from genes | Parked (needs a data component on the food). | Claude |
| 12 | Which items | Keep all, but **an item ships together with its use** (section 8). | Claude |
| 13 | Legends and the Trophy Pedestal (old open question 1) | **Keep them in the plan, decide at D8.** Nothing in D1-D7 depends on them, they are one deletable table (2.6), and the Apex stays undesigned. | Claude (user: "go with your picks") |
| 14 | Bone meal from a starborn line (old open question 2) | **Keep x0.7** (`innate` = the Fertility value, no special case). Costs about 1.4 bone meal at Bloom +120 (3.29 instead of 4.7); Starlit Dust makes up for it. | Claude (user: "go with your picks") |
| 15 | Tissue Sample without Calipers (old open question 3) | **Keep 4%.** With the Calipers in the off hand it is guaranteed anyway, so the 4% is only a surprise for players who never read about it. | Claude (user: "go with your picks") |
| 16 | Mutant Marrow and pups (contradiction found in the third turn: 1.3 and row 4 said a pup keeps only layer 1 and the sample, 2.4 said "adult or pup") | **Adults only.** A seed gives 1-6 pups; pup marrow at 25% per rare allele would make a mutant litter a marrow farm, against the plan's rule that culling a litter at birth must not be the best way to farm. | Claude (user: "go with your picks") |
| 17 | Telling the player about the off-hand rule | **One more tooltip line on the Calipers** (in D7, with the Tissue Sample): "Off hand when you kill: a Tissue Sample". `CalipersItem.appendHoverText` already exists (COMPILED), so no new Minecraft name. | Claude (user: "go with your picks") |

## 1. The engine, exactly

One pure-Java class, `genetics/Drops.java` (no Minecraft imports; items are string ids like `minecraft:rabbit_foot`, as `Juice` names effects).

### 1.1 Inputs
* `Genome` and `Stats` of the dying bunnidog (EXPRESSED alleles only, like Juice/Trends).
* `Context`: `source` (always `DEATH` for now, see 11), `baby`, `playerKill` (= `LivingDropsEvent#isRecentlyHit()`), `burning`, `looting` (0..3+), `calipersOffHand`.
* A `RandomGenerator` (the glue passes `new Random(entity.getRandom().nextLong())`, the project's pattern).

### 1.2 Output
`List<Drop(String itemId, int count)>` for the real kill, and `List<Expectation(itemId, mean, chanceAtLeastOne)>` for the tools (Calipers line, `/bunnidogs drops`).
The two share the same rule tables, so what the tool shows is what the kill rolls.

### 1.3 Order of evaluation (a drop never depends on another drop)
0. **Hide** (pelt), 1. **Stat lines**, 2. **Signatures** (one rule = one roll), 3. **Marrow**, 4. **Tissue Sample**, 5. **Legends**.
Context filters: a pup keeps only layer 1 (at x0.25) and the Tissue Sample (no hide, no signatures, no marrow, no legends); "needs player kill" rows are skipped when `playerKill` is false.

### 1.4 The stat-line formula (layer 1)
For a line with `onset, base, per, tail, floor, cap, innate`:

    q       = max(0, p - onset)                 p = bred points of the line's stat (negative allowed)
    yield   = base + per*q                      if q <= 20
            = base + per*20 + tail*sqrt(q - 20) otherwise
    yield   = max(floor, yield)                 (when onset = 0 a negative p just continues the linear part down to the floor)
    E       = innate x yield x (1 + 0.15 x looting) x (baby ? 0.25 : 1)
    count   = min(cap, floor(E) + (rng < frac(E) ? 1 : 0))        unbiased stochastic rounding, like Drift

`innate` is what the genes already do: `Traits.healthMultiplier()` (flesh, haunch), `speedMultiplier()` (foot), `jumpMultiplier()` (sinew),
`hearingMultiplier()` (whisker), the Fertility value (bone meal). No new alleles data is needed.
Looting multiplies E, so a line with E = 0 stays 0: nobody farms sinew from an unbred animal with Looting III.

### 1.5 The chance formula (layers 2, 3 and the rest)
    statFactor(p)  = clamp(1 + p/50, 0.5, 3)          a bred line is up to 3x likelier, a line bred AGAINST the stat is halved
    worthFactor(w) = min(3, 1 + w/100)                w = sum of the animal's POSITIVE points in all five stats ("how heavily bred")
    chance         = min(0.95, base x factor x (1 + 0.10 x looting))
    count          = uniform integer in [min, max]    (only when the roll succeeds)

## 2. What a bunnidog drops, in numbers

### 2.1 Layer 1: the six stat lines
| Line | Stat | Item | onset | base | per | tail | floor | cap | innate |
|---|---|---|---|---|---|---|---|---|---|
| flesh | Vitality | `bunnidogs:raw_bunnidog_flesh` (cooked if burning) | 0 | 1.0 | 0.10 | 0.5 | 0.5 | 16 | health |
| haunch | Vitality | `bunnidogs:prime_haunch` (roast if burning) | 20 | 0 | 0.02 | 0.15 | 0 | 4 | health |
| foot | Swiftness | `minecraft:rabbit_foot` | 0 | 0.10 | 0.05 | 0.3 | 0 | 8 | speed |
| sinew | Leap | `bunnidogs:sinew` | 0 | 0 | 0.08 | 0.3 | 0 | 8 | jump |
| whisker | Senses | `bunnidogs:whisker` | 0 | 0 | 0.08 | 0.3 | 0 | 8 | hearing |
| bone meal | Bloom | `minecraft:bone_meal` | 0 | 0.5 | 0.06 | 0.3 | 0 | 12 | fertility |

Yield by bred points, before `innate` (the table the code must reproduce; cap applied):

| points | -10 | 0 | 10 | 20 | 40 | 60 | 100 | 300 | 1000 |
|---|---|---|---|---|---|---|---|---|---|
| flesh | 0.50 | 1.00 | 2.00 | 3.00 | 5.24 | 6.16 | 7.47 | 11.37 | 16 (cap) |
| haunch | 0 | 0 | 0 | 0 | 0.40 | 1.07 | 1.56 | 2.82 | 4 (cap) |
| foot | 0 | 0.10 | 0.60 | 1.10 | 2.44 | 3.00 | 3.78 | 6.12 | 8 (cap) |
| sinew / whisker | 0 | 0 | 0.80 | 1.60 | 2.94 | 3.50 | 4.28 | 6.62 | 8 (cap) |
| bone meal | 0 | 0.50 | 1.10 | 1.70 | 3.04 | 3.60 | 4.38 | 6.72 | 11.09 |

Worked examples (E = expected count; a count is floor(E) plus one more with probability frac(E)):
* **Plain meadow animal** (all zero): flesh 1.0, foot 0.10 (a 10% chance of one), bone meal 0.5, plus its pelt.
* **Desert racer** (fleet legs x1.21, lean build x0.55, Swiftness +30, Vitality -5): foot 2.48 (2 feet 52%, 3 feet 48%), flesh 0.275 (27.5% for one), no haunch.
* **Chonker bred for Vitality +40** (build x1.9): flesh 9.95 (9 or 10), haunch 0.76 (76% for one), plus a Mutant Marrow chance (chonker is a rare allele).
* **Hoofed mountain goat-dog, Leap +20** (jump x1.36): sinew 2.18.  **Bat-eared cave animal, Senses +30** (x1.45): whisker 3.70.
* **Starborn End line, Bloom +120** (fertility x0.7): bone meal 3.29, and starborn's own Starlit Dust (2.3).
* Looting III multiplies each E by 1.45 (a racer's 2.48 feet become 3.6).

### 2.2 Layer 0: the hide (pelt)
An ADULT always drops one pelt of its EXPRESSED coat, on any kind of death (like vanilla leather); Looting adds a 15% chance per level of one extra.
So a `glow` pelt only comes from a bunnidog that shows glow (a homozygous swamp one), a nice reward for breeding the recessive.

### 2.3 Layer 2: gene signatures (player kill, adult only)
Rows are `allele key -> item, count, base chance, factor`. Factor `S:<stat>` = `statFactor` of that stat, `W` = `worthFactor`.
Common alleles have no row (their effect is already in `innate`), **with one exception found in the sixth turn: `temperament:skittish` and `temperament:feral` are common alleles but have rows, because temperament has no numeric innate effect** (DESIGN "Drops D3 decisions"). The fang and Starlit Dust rows are defined but inactive until D4 and D7.

| Allele | Item | Count | Base | Factor |
|---|---|---|---|---|
| coat:spotted (rare) | `minecraft:cookie` | 1-3 | 12% | W |
| coat:snow | `minecraft:snowball` | 4-8 | 30% | W |
| coat:snow | `minecraft:packed_ice` | 1-2 | 8% | W |
| coat:sand | `minecraft:gold_nugget` | 1-3 | 10% | W |
| coat:moss | `minecraft:slime_ball` | 1-2 | 10% | W |
| coat:pale | `minecraft:amethyst_shard` | 1-2 | 10% | W |
| coat:slate | `minecraft:flint` | 1-3 | 15% | W |
| coat:slate | `minecraft:raw_iron` | 1 | 6% | W |
| coat:ember | `minecraft:blaze_powder` | 1-2 | 10% | W |
| coat:void | `minecraft:chorus_fruit` | 1 | 12% | W |
| coat:void | `minecraft:ender_pearl` | 1 | 4% | W |
| coat:glow | `minecraft:glowstone_dust` | 1-3 | 12% | W |
| ears:bat | `minecraft:phantom_membrane` | 1 | 10% | S:Senses |
| ears:huge (rare) | `minecraft:echo_shard` | 1 | 4% | S:Senses |
| legs:fleet | `minecraft:sugar` | 1-3 | 20% | S:Swiftness |
| legs:springy (rare) | `minecraft:slime_ball` | 1-2 | 15% | S:Leap |
| legs:hoofed | `minecraft:leather` | 1 | 20% | S:Leap |
| build:woolly | `minecraft:white_wool` | 1-3 | 30% | S:Vitality |
| build:lean | `minecraft:bone` | 1-2 | 25% | S:Vitality |
| temperament:feral | `bunnidogs:fang` | 1 | 20% | S:Vitality |
| temperament:feral | `minecraft:bone` | 1-2 | 20% | S:Vitality |
| temperament:skittish | `minecraft:feather` | 1-2 | 25% | S:Swiftness |
| temperament:stoic (rare) | `minecraft:flint` | 1-2 | 20% | S:Vitality |
| fertility:starborn | `bunnidogs:starlit_dust` | 1-2 | 25% | S:Bloom |

Example: a sand-coated racer (worth 30): gold nugget chance 10% x 1.3 = 13% (x1.1 with Looting I).
A fleet-legged line at Swiftness +100 has sugar at 20% x 3 = 60%.

### 2.4 Layer 3: Mutant Marrow
For EVERY rare (mutation) allele the animal expresses (`Allele.isRare()`: huge ears, spotted, springy, chonker, stoic, swarm): one roll at a flat **25%**, count 1.
An animal can carry up to six, so a thoroughly mutant one drops up to six marrow. Player kill only, **adult only** (a pup gets none, decision 16).

### 2.5 Layer 4: Tissue Sample
One roll per kill, count 1: **100% if the killer holds the Calipers in the off hand**, otherwise 4%. Player kill only; pups count (their genes are real).

### 2.6 Layer 5: legends (after the Trophy Pedestal exists, section 7)
Per stat, player kill, adult only, count 1, NOT affected by Looting:

    chance(p) = 0                                  for p < 100
              = min(0.15, 0.02 + 0.0002 x (p-100)) for p >= 100

2% at 100 points, 4% at 200, 8% at 400, 15% from 750. A line bred for several stats rolls each one separately.

## 3. Pelts: one per coat, built to be used later
**D12 update:** all twelve pelts now craft matching coat rugs (two matching pelts
plus string). Native carpet support/drops retain the coat; mixed pelts still
craft leather. This develops the optional rug hook below under the user's
standing design latitude. See `WORKSHOP_SPECS.md` D12.

* **Items:** `bunnidogs:<coat>_pelt` for each allele of `Gene.COAT` (tan, cream, brown, spotted, snow, sand, moss, pale, slate, ember, void, glow). Generated from ONE loop over the coat table, exactly like the textures
  of the entity: a new coat in `Gene.COAT` means a new pelt, and the self-check fails until its texture, model, lang line and tag entry exist.
* **Tag:** `bunnidogs:pelts` contains all of them, so any later recipe can say `#bunnidogs:pelts` for "any pelt" or name one coat for "this exact pelt".
* **Look:** a folded hide in the coat's own palette (the same colours as the entity skin), made by `art/gen_pelts.py` (one script, 12 PNGs, one preview).
* **Rarity (name colour):** common coats COMMON, wild lineage coats UNCOMMON, rare mutation coat (spotted) RARE.
* **Use today:** two pelts of any coat craft one leather (shapeless, `#bunnidogs:pelts` twice), so every pelt is useful at once. Stack size 64.
* **Hooks for the "interesting stuff" (NOT planned, just made possible):** matching-coat rugs/carpets, armour lining, banners, trophies on the Pedestal, a Show Ring judged on pelt rarity,
  a trade with a future Breeder villager, dyeing. Because each coat has its own item and a tag, none of that needs a change in the drop code.

## 4. The new items (everything modded)

| Item id | Kind | Properties | Comes from | Use (shipped with it) |
|---|---|---|---|---|
| `raw_bunnidog_flesh` | food | nutrition 3, saturation 0.3 | flesh line | smelt / smoke / campfire -> cooked |
| `cooked_bunnidog_flesh` | food | nutrition 6, saturation 0.6 | burning death, cooking | food |
| `prime_haunch` | food | nutrition 6, saturation 0.4 | haunch line | cook -> roast |
| `roast_haunch` | food | nutrition 10, saturation 1.0 (the best food of the mod) | burning death, cooking | food |
| `sinew` | material | stack 64 | sinew line | 1 sinew -> 2 string now; the Leash later |
| `whisker` | material | stack 64 | whisker line | whisker + copper ingot + stick -> vanilla Brush now; the Genetics Table later |
| `fang` | material | stack 64 | feral temperament | fang + stick + feather -> 4 arrows now; the clasp of the Leash later |
| `starlit_dust` | material | UNCOMMON | starborn (End) | right-click a planted seed crop: it ripens at once (one dust, one crop) |
| `mutant_marrow` | material | RARE | rare alleles, adults only | ships WITH the Mutagen (D6) |
| `tissue_sample` | data item | component `bunnidogs:tissue_data` = `genome/stats` text (same pattern as the seed and infusion) | layer 4 | read by the Calipers; the Gene Splicer later |
| `<coat>_pelt` x12 | material | see section 3 | hide | leather now |
| *later:* `titan_haunch`, `gilded_foot`, `moon_sinew`, `listening_shell`, `everbloom` | legends | EPIC | layer 5 | Pedestal + Apex (section 7) |
| *later:* `trophy_pedestal` | block | block entity showing one item | craft | display |

That is **22 modded items now** (10 materials/foods/data + 12 pelts), 5 legends and a block later.
Vanilla items used as drops need no art: rabbit's foot, bone meal, cookie, snowball, packed ice, gold nugget, slime ball, amethyst shard, flint, raw iron, blaze powder, chorus fruit,
ender pearl, glowstone dust, phantom membrane, echo shard, sugar, leather, white wool, bone, feather.

### 4.1 Recipes to write (formats)
* Crafting recipes: the verified format in API_NOTES (`crafting_shaped` / `crafting_shapeless`, plain id strings, `#ns:tag` for tags, `result: {id}`; `count` is read from the one `result` object).
* Cooking (`minecraft:smelting`, `minecraft:smoking`, `minecraft:campfire_cooking`): the one 26.x example in NeoForge's tests is
  `{"type":"minecraft:blasting","ingredient":{"item":"..."},"result":{"id":"...","count":9},"cookingtime":100}`. Whether `ingredient` may also be a plain id string (as in crafting) and whether
  `experience` is required is NOT confirmed: D1 verifies it first and the user's game load settles it.
  Times: smelting 200, smoking 100, campfire 600 ticks; experience 0.35.

## 5. The tools show the numbers
* **Calipers:** one new line group in `Readout.animal` (pure Java, tested): `  Drops (adult kill, no Looting): flesh 1.0, rabbit's foot 0.10 (10% for one), bone meal 0.5, pelt: tan, ...`.
  Layer 2 and 3 rows print as `chance%` (e.g. `gold nugget 13%`). A pup shows its own x0.25.
* **`/bunnidogs drops [simulate <n>]`** (D1): same target selection as `/bunnidogs genome` (the bunnidogs within RADIUS, nearest first). Without an argument it prints each animal's expectations; with `simulate`
  it rolls n kills (n <= 1000) and prints the average count of each item, so the user can see the maths work in game, in a creative world.

## 6. The glue (the one handler), step by step
A class `drop/BunnidogDrops` with `@EventBusSubscriber(modid = MODID)` and one `@SubscribeEvent` on `LivingDropsEvent` (the pattern of `SeedCropHarvest`):
1. Return unless `event.getEntity() instanceof Bunnidog` and the level is a `ServerLevel`.
2. Return if the game rule `GameRules.MOB_DROPS` is off (SOURCE: the name appears in NeoForge's own `LivingEntity` patch); this matters because our drops are added by the event, not by vanilla's loot step.
3. Looting: `source.getEntity() instanceof LivingEntity killer` -> `killer.getMainHandItem().getEnchantmentLevel(<LOOTING holder>)`, read exactly like Fortune in `SeedCropHarvest` (COMPILED).
   An arrow's killer is its shooter (`getEntity()` is the cause, `getDirectEntity()` the arrow), so bows work.
4. `calipersOffHand`: `source.getEntity() instanceof Player p && p.getOffhandItem().is(ModItems.CALIPERS.get())` (`getOffhandItem()` SOURCE).
5. `burning = entity.isOnFire()` (SOURCE: used in NeoForge's tests; whether it is still true at the moment of death is ASSUMED, the cooked-flesh rule is the first thing to try in game).
6. `Drops.roll(...)`, then for each result `new ItemEntity(level, x, y, z, stack)` added to `event.getDrops()` (NeoForge spawns the collection after the event unless it is cancelled).
Names to confirm at the user's first compile: the `LivingDropsEvent` import path, `Enchantments.LOOTING` through the registry lookup, `GameRules.MOB_DROPS` `get(...)`, `isOnFire()`, `getOffhandItem()`.
Nothing is cancelled and no vanilla loot table is involved (a bunnidog has none).

## 7. Legends and the Trophy Pedestal (the answer to "what are they for")
The honest problem: five absurd items need a purpose that is not a global system or a charm (the user dislikes gimmick and global mechanics). The answer:
1. **Today (cheap, in the mod's spirit): prestige.** A **Trophy Pedestal** block (the bloat list already has it) shows one item on top; the item may be a legend, a pelt, a Tissue Sample or a
   **Worn Ear Tag** (an Ear Tag that keeps its `BD-XXXXXX` code and drops from a tagged animal, parked here). It is a block entity like `SeedCropBlockEntity`, no new API except the item display.
2. **Later (the user decides): the Apex.** The design has an endgame boss "a version of the player's own line, stats maxed". The five legends, one per stat, are its offering: you have to
   have bred a 100+ line in every stat (one all-rounder or five specialists). The Apex is NOT planned in detail; this is only the reason the legends exist.
3. If the Apex is never built they stay a trophy collection, which costs little. If the user dislikes legends they are one table (section 2.6) to delete.
Because of this the legends and the Pedestal are the LAST chunk (D8): no legend drops before there is somewhere to put it.

## 8. Rule: an item ships with its use
| Item | Ships in | With |
|---|---|---|
| flesh, cooked, foot, bone meal | D1 | cooking recipes; the vanilla uses of foot and bone meal |
| pelts | D2 | the leather recipe |
| signatures (vanilla items only) | D3 | nothing needed, they are vanilla items |
| sinew, whisker, fang | D4 | string, Brush, arrows (interim); Leash and Genetics Table keep them useful later |
| prime/roast haunch | D5 | the cooking recipes |
| marrow | D6 | the Mutagen (an item to feed a parent so its next seed mutates; it needs ONE extra field in the seed text, old seeds must still parse: its own design turn) |
| tissue sample, starlit dust | D7 | Calipers reading it (and the tooltip line that explains the off-hand rule); the crop ripening |
| legends, worn tag | D8 | the Trophy Pedestal |

## 9. Delivery plan, replacing DROPS_PLAN section 8 (small chunks, the user compiles each)
| Chunk | What | New Minecraft names |
|---|---|---|
| D1 (WORKS, user: "it worked") | `genetics/Drops.java` engine (layer 1 for flesh, foot, bone meal; the hide (layer 0) comes with the pelts in D2; context; Looting), the handler, raw/cooked flesh + 3 cooking recipes, `/bunnidogs drops [simulate n]`, the Calipers line | `LivingDropsEvent`, `Enchantments.LOOTING` lookup, `GameRules.MOB_DROPS`, `isOnFire`, cooking recipe JSON |
| D2 (WORKS, user: "it worked") | 12 pelts (art script `art/gen_pelts.py`, tag, leather recipe) + the hide layer | none |
| D3 (WORKS, user: "everything compiled and worked") | layer 2 signatures (vanilla drops): 22 of the 24 rows active, the fang and Starlit Dust rows wait for D4 and D7 | none |
| D4 (WORKS, user: "everything compiled and worked") | sinew, whisker, fang + their recipes (string, Brush, arrows); the sinew and whisker lines and the fang row go live | none |
| D5 (WORKS, user: "it worked") | haunch line + prime/roast haunch (nutrition 6 / 10) + 3 cooking recipes; every stat line is now active | none |
| D6a (WRITTEN, not compiled; session 15) | the Mutagen: item, the seed's 4th text part, the 50% mutation floor, the apply gesture (section 14, picks accepted by the user) | none |
| D6b | Mutant Marrow: layer 3 drop, the item, the Mutagen recipe, Calipers listing | none expected |
| D7a (WORKS) + D7b (written session 18) | Tissue Sample (component) + Calipers reading + the Calipers tooltip line for the off-hand rule + Starlit Dust on the crop | `useOn` on a crop (the Calipers' `useOn` shape COMPILED); the tooltip adds a line to the existing `CalipersItem.appendHoverText` (COMPILED), no new name D7 was split: **D7a** = Tissue Sample + Calipers reading + tooltip line (written); **D7b** = Starlit Dust on the crop (written session 18). |
| D8 | Trophy Pedestal + legends (+ worn ear tags) | block entity item display |
| D8a (WORKS, user: "it worked", start of session 31) | the Pedestal as a plain block: block, item, model (base, column, gold-trimmed plate), 2 textures, recipe, loot, pickaxe tag. No block entity | none (`SoundType.STONE` is the only new vanilla constant) |
| D8b (WORKS, user: "worked", start of session 32) | block entity holding ONE item: right-click with an item puts it on, an empty hand takes it back, breaking the pedestal drops it; saved in NBT | `BlockEntity` save/load of an `ItemStack` (copy `SeedCropBlockEntity` for the pattern; the stack codec is new), `useItemOn` / `useWithoutItem` |
| D8c (WORKS, user: "stuff worked", start of session 33) | draw that item above the pedestal (a `BlockEntityRenderer` + the block entity's client sync; both copied from NeoForge's `CustomItemDisplayContextTest`) | `BlockEntityRenderer` / `BlockEntityRenderState` / `ItemModelResolver#updateForTopItem` / `ItemStackRenderState#submit` / `registerBlockEntityRenderer`, `getUpdateTag` + `getUpdatePacket` + `onDataPacket`, `sendBlockUpdated` (all SOURCE-verified) |
| D8d (WORKS, user: "things worked", start of session 34) | the five legends (items + layer 5 of `genetics/Drops.java`, section 2.6): `titan_haunch`, `gilded_foot`, `moon_sinew`, `listening_shell`, `everbloom`, plain EPIC items, art `art/gen_legends.py` | none (plain items, pure engine) |
| D8e (WORKS, user: "worked", start of session 35) | the Worn Ear Tag (an Ear Tag that keeps its code, dropped by a tagged animal): item `worn_ear_tag` + string component `worn_tag_code`, one statement in `BunnidogDrops`, art `art/gen_worn_ear_tag.py`; certain, not a roll, so `genetics/Drops` is unchanged | none (plain item, a component like `tissue_data`) |
The user may reorder; D1 is what they asked for first (flesh and rabbit's feet).

## 10. Tests when it is coded (small, per the user's verification preference)
A handful of exact assertions in `GenomeCheck.java`, no big sampling: the yield table of 2.1 (every cell), caps, `E` for the five worked examples, stochastic rounding is unbiased over a few hundred
rolls, a pup gets x0.25 and no pelt/signature/marrow/legend (but still a Tissue Sample roll), `playerKill = false` skips layers 2-5, a negative stat hits the floor, every item id in the tables has its model/texture/lang entry (resources by file reading).
Nothing is run now: this turn changed docs only.

## 11. Why "death only for now" does not paint us into a corner (the living harvest, later)
The engine already separates WHAT the animal can give from HOW it is taken:
* `Context.source` is an enum (`DEATH` now). Every table row has the set of sources it applies to, `{DEATH}` for all rows today. A living harvest adds `SHEAR`, `BRUSH`, `SYRINGE` and marks a few rows with them.
* The yield functions do not change: a harvest just applies a fraction (idea: one third of the death yield) of the same `E`, so a line's output is the SAME readout of the animal.
* The ONLY new stored data a harvest needs is one saved number on the animal (`LastHarvest`, a game time), the project's `putLong`/`getLongOr` pattern. Nothing in the current plan touches the save format, so adding it later cannot break a world.
* Natural renewables (not planned, just fitting): shear a woolly one for wool, a Syringe for a Tissue Sample (the non-lethal version of 2.5), brushing for pelt clippings.
  A neat rule when the time comes: the cooldown shrinks with Bloom (`24000 / Stats.growthMultiplier()`), so the growth stat also makes a line regrow faster.
* Because of this, the handler must not assume death anywhere except in the glue (`isRecentlyHit`, `burning` and the killer's tools are inputs of `Context`, not facts of `Drops`).

## 12. Changes to DROPS_PLAN.md
* Looting on layer 1 is a multiplier on E (`x(1 + 0.15 L)`), not "+0.25 on E" (the old rule gave free drops to a line with E = 0).
* Haunch became an ordinary line (onset 20 on Vitality, cap 4) instead of a separate chance rule.
* Pelts drop on ANY adult death (like vanilla leather), not only on player kills.
* Tissue Sample: Calipers in the OFF hand, 4% otherwise (the old "killer holds the Calipers" was ambiguous).
* The Worn Ear Tag moved to the Pedestal chunk; legends moved behind the Pedestal.
* The chunk list (D1-D8) replaces D1-D7; every item now ships with its use (section 8).
* The five worked examples and the yield table were recomputed for this file; use these.
* (Third turn) Mutant Marrow is adults only; DROPS_PLAN 5.5 said "any bunnidog" without saying pups, and this file's own 2.4 first said "adult or pup".
* (Third turn) The yield table, the five worked examples and the legend curve were re-derived outside this file (a small script, 45 cells + 9 examples + 4 legend points) and match.

## 13. Open questions: none
The three questions that were open at the end of the second turn are settled (decisions 13-15), the marrow contradiction is fixed (16) and the tooltip is planned (17).
What remains are only the two things that cannot be settled by reading:
1. **In-game confirmations at the first compile of D1** (listed in section 6 and in `docs/API_NOTES.md`): the `LivingDropsEvent` import path, the Looting lookup, `GameRules.MOB_DROPS`, whether `isOnFire()` is still true at the moment of death, and the cooking-recipe JSON format.
2. **The Legends decision at D8** (decision 13): keep them as a Pedestal trophy collection and the future Apex offering, or delete table 2.6. Nothing before D8 is affected by the answer.

**Next step: chunk D1 (section 9), when the user says to start coding.**

## 14. D6 design: the Mutagen and Mutant Marrow (session 15, second turn: picks ACCEPTED by the user in the third turn ("good choices"); D6a coded, D6b next)
The spec asked for "its own design turn" because the seed text needs one extra field. What the code looks like today (read this turn): a seed is `mother/stats|father/stats|quality` (2 or 3 `|` parts), a planted crop stores that same text, the litter is rolled by `Seed.hatch` and the infusion's phantom pup by `Infusion.roll`, both through `Genome.breed(mother, father, rng)` (natural chance = 0.04 x fertility squared, capped at `MAX_MUTATION_CHANCE` 0.5), and `Genome.breed(..., chance)` already takes a chance (its own comment says "Pass 1.0 to force a mutation (Mutagen)"). The seed's tooltip, `/bunnidogs seed` and the Calipers read `Seed.mutationChance()`.

| # | Question | Proposed pick | Why |
|---|---|---|---|
| 18 | Who receives the Mutagen: a parent ("feed a parent", spec 8) or the seed? | **The seed.** Seed in the main hand, Mutagen in the OFF hand, right-click: the seed gets the flag and the Mutagen is used up. This is the infusion gesture (`SeedItem.use`, COMPILED, WORKS) with the Mutagen where the juice is | The flag has to live in the seed text anyway. Feeding a parent would make the animal remember it (new NBT, like `GuavaMemory`) and the mutagen would hit whichever seed that mating drops; treating the seed lets you pick the seed you care about, and needs no entity change |
| 19 | What does it do? | **Raises the mutation chance of every pup of that seed to at least 50%** (`max(natural, MAX_MUTATION_CHANCE)`; 50% is the existing cap, so no new number). Not "every pup mutates" | A litter is 1-6 pups: forcing all of them would turn ONE marrow into up to six rare-allele pups. A 50% floor still gives about 1.75 mutants in an average litter, is visible in the tooltip, and keeps the dice |
| 20 | Seed text | **A 4th `|` part, the word `mutagen`:** `mother/stats|father/stats|quality|mutagen`. A seed WITHOUT the part prints exactly as today (byte for byte, so old seeds, stacks and crops are unchanged); `a|b||mutagen` is valid (blank quality = 0); any other 4th part makes the seed malformed. `Seed` gets a 6th record field `mutagen` (boolean), the 5-argument constructor stays for old callers | Old seeds must still parse (spec 8). A word, not a number, because there is only one level; a future flag would be another word |
| 21 | Stacking | **One Mutagen per seed.** A treated seed refuses a second one (nothing is used up) | Keeps the 50% a clean rule |
| 22 | Where it takes effect | `Seed.mutationChance()` returns `max(natural, 0.5)` when treated; `Seed.hatch` and `Infusion.roll` call `Genome.breed(..., seed.mutationChance())`. The tooltip, command and Calipers follow by themselves, plus one line `Treated with Mutagen` | One source of truth, the readouts cannot disagree with the dice |
| 23 | The item | `bunnidogs:mutagen`, "Mutagen", UNCOMMON, stack 16, creative **Ingredients** tab (and `/give`) from D6a; a small green-violet vial (art `art/gen_mutagen.py`). No glint (it would add a Minecraft name) | Plain item like the D4 materials |
| 24 | Order | **D6a = the Mutagen first** (item, seed field, gesture, hatch/infusion, tests; obtainable by creative/`/give` until D6b), **D6b = Mutant Marrow** (layer 3 of `Drops`, the item, the Mutagen recipe, Calipers listing) | Every item then ships with its use (spec 8): the Mutagen has its gesture at once, the marrow arrives together with its recipe. The spec's "marrow ships WITH the Mutagen" is kept in spirit (both inside D6) |
| 25 | Mutagen recipe (D6b) | **Shapeless: 1 Mutant Marrow + 1 Fermented Spider Eye + 1 Glass Bottle -> 1 Mutagen** | The vanilla corrupting ingredient; one marrow per Mutagen. Marrow is 25% per expressed rare allele on a kill (spec 2.4), so about 4 such rolls per Mutagen, and each kill removes that mutant from breeding: the choice the plan wants |
| 26 | Marrow display (D6b) | `mutant marrow` listed by the tools as an average count (0.25 per expressed rare allele, up to seven rolls (including rare Diet)), adults + player kill only | Same display as the stat lines; the signature style (a chance) fits one roll only |

**Compile risk:** D6a should add NO Minecraft name (item registration and `use` copy the juice/seed code that compiled; the extra field is pure Java in `Seed`). **Left out:** a Mutagen for a parent, a stronger level, a glint, a way to choose the mutated gene (a Gene Splicer idea, D7+).


Diet compatibility (2026-10-16): the seventh gene can express rare omnivore,
which participates in the existing Mutant Marrow count. The per-gene chance,
Looting rule and adult/player-kill gates are unchanged; seven rare genes can
now show at once. Legacy six-pair data loads with plain Diet, so its drops stay
the same. New canonical writes append the Diet pair; old seed/sample texts
remain readable even when their normalized text grows by that pair.
