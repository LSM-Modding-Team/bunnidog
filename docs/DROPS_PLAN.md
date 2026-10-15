# Bunnidog drops: overall plan (session 13)

**CURRENT (session 43): ALL DONE (D1-D8 work). Kept as the "why" of the drops; the numbers are in DROPS_SPECS.md.**

**NOTE (session 13, third turn): the specifics are in `docs/DROPS_SPECS.md`, all decisions are made there, and where the two files differ THE SPECS WIN** (its section 12 lists the differences; chunks are D1-D8 there, not D1-D7 here). This file stays as the "why".

**Status: PLAN ONLY. Nothing in this file is coded.** The user's order (session 13): "make a cohesive overall plan to go in
docs ... then in the next messages you'll go over the specifics of that plan, still not coding anything." So the next
turns walk through the sections below one at a time and settle the open questions (section 9); code starts only when the
user says so, in the small chunks of section 8. Every number here is a STARTING value, chosen to be discussed.

The user's words: "add drops to the bunnidogs. their probabilities should depend on their genome and their stats. For
example, a fast one has more probability to drop rabbit's foot, even multiple if very fast. More vitality would be more
bunnidog flesh, etc. Don't be afraid to add multiple modded items. Be creative."

## 1. The idea in one paragraph

**A bunnidog's loot is a readout of the animal.** The genes decide WHAT it can drop; the bred stats decide HOW MUCH and HOW
OFTEN. That is the same split the mod already uses everywhere: "genes decide the direction a line drifts in, quality decides
how much it gains" (DESIGN "4. Bred stats"), and "final attribute = base x innate allele multiplier x curve(points)". Drops
follow the same recipe:

    count of a drop  =  yield(bred points of the matching stat)  x  innate multiplier of the matching genes

A plain unbred bunnidog is worth about what a vanilla rabbit is (about 1 piece of meat, a 10% rabbit's foot). A line bred
for Swiftness drops feet in handfuls; a line bred for Vitality drops a pile of flesh; a rare mutation or a wild allele adds
loot nobody else has; and the five stats each have an absurd "legendary" drop for the ones bred past the guava ladder.

## 2. Principles (why it looks like this)

1. **Genes = what, stats = how much.** Stats are unbounded, so drops are unbounded too, but through the same curve shape as
   the attributes (linear, then a square-root tail), so a huge stat never means an infinite stack.
2. **Nothing new is stored on the animal.** Everything is derived from the Genome and the Stats it already has, plus the
   circumstances of its death. No save-format change, no new NBT, old worlds just work.
3. **Pure Java first.** The whole roll is `genetics/Drops.java` (no Minecraft imports, like `Juice`, `Trends`, `Drift`), so it is
   testable here. The Minecraft side is one event handler and some plain items.
4. **Tables drive content** (project convention): one table entry = one drop = item + texture + lang + model, like `GuavaTier`.
   The self-check fails until every entry has its files. **Every drop must have a use** (section 6); no dead-end items.
5. **Loot closes the loop with breeding.** The new materials are the ingredients of things already on the roadmap (Leash,
   Genetics Table, Mutagen, Gene Splicer), so culling feeds breeding instead of competing with it.
6. **Optimistic, like the rest of the mod.** Nobody is forced to kill anything, a pacifist loses nothing, and surplus
   pups from a big litter finally have a purpose. No penalties.
7. **Balanced by the economy that already exists** (section 7): rare loot needs stat points, stat points need guava tiers.

## 3. How it works (technical overview)

### 3.1 The hook (SOURCE-verified in session 13, NeoForge 26.2.x clone)
* A bunnidog has NO loot table today (no `data/bunnidogs/loot_table/entities/`), so it drops nothing; our handler is the only source.
* NeoForge patches `LivingEntity#dropAllDeathLoot` to capture the drops and then fire **`LivingDropsEvent`**
  (`net.neoforged.neoforge.event.entity.living`, on the game bus). It carries `getSource()`, `getDrops()` (a mutable
  `Collection<ItemEntity>`), `isRecentlyHit()`, and is cancellable. Our handler: `@EventBusSubscriber(modid = MODID)` class
  (same pattern as `SeedCropHarvest`, which COMPILED), server side only, `event.getEntity() instanceof Bunnidog`, build a
  `Context`, call `Drops.roll`, create `new ItemEntity(level, x, y, z, stack)` per result and add it to `event.getDrops()`.
* **Looting:** in 26.2 the event has NO looting level (it was dropped from the event; verified in the class). We read it
  ourselves the way Fortune is read in `SeedCropHarvest` (COMPILED): `stack.getEnchantmentLevel(registry.getOrThrow(Enchantments.LOOTING))`,
  with the killer's main-hand stack. `Enchantments.LOOTING` exists (used by NeoForge's own tag provider); getting the
  killer's stack from `DamageSource#getEntity()` needs one vanilla name to confirm at compile time.
* **Rejected:** a vanilla loot-table JSON (no loot function can say "count = f(stat points)"); a NeoForge global loot modifier
  (needs a codec registration and cannot see our Stats); overriding a vanilla death method (its body and signature are not
  in the sources, so it could not be verified).

### 3.2 The pure-Java side (`genetics/Drops.java`, new)
* `Context(baby, killedByPlayer, burning, lootingLevel, ...)`, `Drop(itemId, count)`.
* `List<Drop> roll(Genome, Stats, Context, RandomGenerator)` for the real kill; `List<Expectation> expected(Genome, Stats, Context)` for
  tools (the average count of each drop, and the chance of at least one).
* Items are named by string id (`minecraft:rabbit_foot`, `bunnidogs:bunnidog_flesh`), exactly like `Juice` names effects, so
  the package stays free of Minecraft. The glue turns an id into an `Item` and skips (with a log line) one that does not exist.

### 3.3 The tools show the odds (the mod's rule: "genes are visible, tools give exact measurements")
* The Calipers' `Readout` gets a "Drops:" line (expected count of each drop for this animal).
* `/bunnidogs drops` for testing: the same numbers for the bunnidog you look at, and optionally a quick simulation of N kills.

## 4. The roll model

Four layers, rolled in this order. A "rule" is one row of a table (like `Juice.DEFS` and `Trends`).

### Layer 1: stat lines (commodities; the user's two examples live here)
One line per bred stat. Expected count `E`, then `count = floor(E) + (1 with probability frac(E))`, then a hard cap.
So "more probability" and "multiple if very fast" are ONE formula:

    E = innate x ( base + perPoint x p )                         for p up to the knee (20 points)
    E = innate x ( base + perPoint x 20 + tail x sqrt(p - 20) )  beyond it      (never below the line's floor)

`p` = the bred points of the stat (negative points lower it; the floor stops it at a minimum). `innate` = the multiplier the
genes already give (`Traits.healthMultiplier()`, `speedMultiplier()`, `jumpMultiplier()`, `hearingMultiplier()`), so a chonker
(1.9x health) is meatier and a fleet-legged animal (1.21x speed) loses fewer feet to bad luck, with no extra storage.

| Stat | Drop | base | per point | tail | floor | cap | innate x | Use |
|---|---|---|---|---|---|---|---|---|
| Vitality | **Raw Bunnidog Flesh** (new) | 1.0 | 0.10 | 0.5 | 0.5 | 16 | health | food; smelt/smoke/campfire to Cooked (section 6) |
| Swiftness | **Rabbit's Foot** (vanilla) | 0.10 | 0.05 | 0.3 | 0 | 8 | speed | vanilla Leaping potions already use it |
| Leap | **Bunnidog Sinew** (new) | 0 | 0.08 | 0.3 | 0 | 8 | jump | the Leash (roadmap Phase 5) |
| Senses | **Bunnidog Whisker** (new) | 0 | 0.08 | 0.3 | 0 | 8 | hearing | the Genetics Table (roadmap Phase 5) |
| Bloom | **Bone Meal** (vanilla) | 0.5 | 0.06 | 0.3 | 0 | 12 | fertility | the seed crop already takes bone meal: a Bloom line is a fertiliser farm |

(0.10 / 0.5 on flesh and foot mean "a plain bunnidog is a vanilla rabbit"; Sinew and Whisker are 0 at 0 points, so an unbred
animal has none: you have to breed for them. Bloom's `innate` is the Fertility multiplier; `starborn` is handled in layer 2.)

Expected count by bred points (before the innate multiplier; flesh, foot, sinew):

| points | -10 | 0 | 5 | 10 | 20 | 40 | 100 | 300 | 1000 |
|---|---|---|---|---|---|---|---|---|---|
| Flesh | 0.5 | 1.0 | 1.5 | 2.0 | 3.0 | 5.2 | 7.5 | 11.4 | 18.7 (cap 16) |
| Rabbit's Foot | 0 | 0.10 | 0.35 | 0.6 | 1.1 | 2.4 | 3.8 | 6.1 | 10.5 (cap 8) |
| Sinew | 0 | 0 | 0.4 | 0.8 | 1.6 | 2.9 | 4.3 | 6.6 | 11 (cap 8) |

Three bunnidogs, to check the feel:
* **Plain meadow animal** (all zeros): 1 flesh, a 10% rabbit's foot, a tan pelt. Nothing else.
* **Desert racer** (fleet legs, lean build, Swiftness +30, Vitality -5): about 2.5 feet (2, or 3 half the time), a 27% chance of one
  flesh (the lean build x0.55 and a negative Vitality), a sand pelt, a gold nugget chance. A real speed line pays in feet.
* **Chonker bred for Vitality +40**: about 10 flesh (5.2 x 1.9), a Prime Haunch chance, and a Mutant Marrow chance (chonker is a rare allele).

### Layer 2: gene signatures (chance-based, one roll per rule, need a player kill)
A rule is `(allele, item, base chance, linked stat or "worth", max count)`, the same shape as `Juice.Def` (allele, effect, base, cap, stat link):
`chance = base x (1 + points / 50)` of the linked stat (cap x3), plus Looting. A rule without a linked stat uses **worth** = the sum
of the animal's positive stat points ("how heavily bred it is"), so a heavily bred animal is worth more everywhere.
Only the EXPRESSED allele of a gene counts (like Juice, Trends, the look); carried recessives are parked (section 9).
Ordinary allele kinds get ordinary loot, in ladder order: **common** alleles have almost none (their trait is already in the
innate multiplier), **wild** (lineage) alleles have signature items, **rare** (mutation) alleles have the dramatic ones.

### Layer 3: legends (the over-bred)
Each stat has one absurd drop that only appears at 100+ points in that stat (the guava ladder is what gets you there: in the
simulation of DESIGN "5. Bloom" a line fed tier-4 guavas reaches about Vitality +106 by generation 30). Chance 0 below 100, 2% at 100,
growing slowly, capped at 15%. Five items, section 5.7. Nobody reaches them by accident.

### Layer 4: context
| Circumstance | Effect |
|---|---|
| Pup (baby) | stat lines x0.25, no signatures, no legends. A litter harvested at birth must not be the best way to farm. |
| Not killed by a player | stat lines drop normally (it is still meat); layer 2 and 3 rolls are skipped, like vanilla rare drops (`isRecentlyHit()`). Keeps traps, cacti and lava from farming rarities. |
| Burning when it dies | flesh drops COOKED (vanilla's rule for cows and chickens). |
| Looting | +10% of the base chance per level on layer 2; +0.25 on `E` per level on layer 1; layer 3 untouched. |
| Creative-mode kill | rolled like any other (so tests in creative show real numbers). |

## 5. The catalog

Marked **(new)** = a modded item; the rest are vanilla items (no art needed). Mappings are proposals for the next messages.

### 5.1 Food chain (Vitality, Build)
* **Raw Bunnidog Flesh** and **Cooked Bunnidog Flesh** (steak-like, a little under: nutrition 6). Raw has no bad effect (like vanilla rabbit).
* **Prime Haunch** (raw) and **Roast Haunch** (cooked), a luxury cut: appears with high Vitality (from about 30) and from a chonker or woolly build;
  the best food in the mod and it shows (nutrition 10, long saturation, short Absorption). "Do not eat your investment" still applies:
  a line bred to Vitality +40 costs real guavas.
* Optional extra: **Glazed Flesh** (cooked flesh + a guava; better the higher the guava tier).

### 5.2 Hide: coat pelts (every adult, one per kill)
* **One Pelt per coat**, 12 items from ONE loop over the coat table (Tan Pelt, Cream, Brown, Spotted, Snow, Sand, Moss, Pale, Slate, Ember, Void,
  Glow), coloured like the coat. A new coat in `Gene.COAT` later means a new pelt automatically (the self-check fails until the art exists).
  This is the "a lot of content, built cheaply" part. Uses: leather by crafting, and a collection for trophies / pelt rugs (parked).

### 5.3 Lineage signatures: the coat is the lineage's flag, so the coat picks a signature drop
(the same idea as the juice, where each coat has its own effect). Linked stat: worth.

| Coat (lineage) | Signature drop | Base chance |
|---|---|---|
| tan, cream, brown (meadow) | none (the pelt is enough) | - |
| spotted (rare mutation) | Cookie, 1-3 | 12% |
| snow (tundra) | Snowballs 4-8, Packed Ice | 30% / 8% |
| sand (desert) | Gold Nugget 1-3 | 10% |
| moss (swamp) | Slime Ball | 10% |
| pale (cave) | Amethyst Shard | 10% |
| slate (mountain) | Flint, Raw Iron | 15% / 6% |
| ember (nether) | Blaze Powder | 10% |
| void (end) | Chorus Fruit, Ender Pearl | 12% / 4% |
| glow (swamp, recessive) | Glowstone Dust | 12% |

### 5.4 Body-part signatures (alleles of Ears, Legs, Build, Temperament, Fertility)
Mostly the innate multiplier already shows these; only flavourful ones get their own row.

| Allele | Drop | Linked stat |
|---|---|---|
| ears: bat (cave) | Phantom Membrane | Senses |
| ears: huge (rare) | Echo Shard | Senses |
| legs: fleet (desert) | Sugar (the Speed-potion ingredient) | Swiftness |
| legs: springy (rare) | Slime Ball | Leap |
| legs: hoofed (mountain) | Leather | Leap |
| build: woolly (tundra) | White Wool, 1-3 | Vitality |
| build: lean (desert) | Bone | Vitality |
| temperament: feral | Bunnidog Fang (new), also a Bone | Vitality |
| temperament: skittish | Feather | Swiftness |
| temperament: stoic (rare) | Flint | Vitality |
| fertility: starborn (End) | **Starlit Dust** (new), scales with Bloom | Bloom |

### 5.5 Mutant Marrow (new): the Mutagen's ingredient
Any bunnidog that EXPRESSES a rare (mutation) allele (huge ears, springy legs, chonker, stoic, swarm, spotted coat) can drop **Mutant Marrow**, 25% per
expressed rare allele. The roadmap's Mutagen (an item that forces a mutation in the next litter) gets its ingredient here, and it creates a real
choice: a mutant is both the best breeding stock (it spreads the rare allele) and the only source of Mutagen.

### 5.6 Tissue Sample (new) and keepsakes
* **Tissue Sample:** holds the animal's genome and stats as text (a data component, the same pattern as the seed's `bunnidogs:seed_data`); the Calipers
  read it like they read a seed. It is the future Gene Splicer's raw material. How you get it is open (section 9): the proposal is "100% when the
  killer holds the Calipers, else 3%".
* **Worn Ear Tag:** an animal wearing an Ear Tag leaves it behind (a keepsake with its `BD-XXXXXX` code), cheap and in the spirit of the Pedigree Book.

### 5.7 Legends (layer 3; five absurd items, one per stat)
Working names, all plain ingredients/trophies (no new global systems): **Titan Haunch** (Vitality), **Gilded Foot** (Swiftness), **Moon Sinew** (Leap),
**Listening Shell** (Senses), **Everbloom** (Bloom). What they are FOR is the big open question (section 9); the likeliest answer is "the top rung of the
Leash / Genetics Table / bone-meal-like recipes", not charms.

## 6. What the drops are for (every item has a use before it is added)

| Item | Use |
|---|---|
| Flesh / Haunch | smelting, smoking and campfire recipes to the cooked versions; Glazed Flesh (optional) |
| Rabbit's Foot | vanilla Leaping potions (works with no work from us) |
| Sinew | Leash (roadmap Phase 5) |
| Whisker | Genetics Table (roadmap Phase 5) |
| Bone Meal | the seed crop and every vanilla crop |
| Pelts | leather by crafting; trophies/rugs later |
| Fang, Starlit Dust | open: Starlit Dust is the candidate for an End-flavoured recipe; Fang for a small tool/weapon recipe |
| Mutant Marrow | Mutagen (roadmap Phase 6) |
| Tissue Sample | read by the Calipers; Gene Splicer (roadmap Phase 6) |
| Legends | open, section 9 |

The consequence: three roadmap items that had no source for their materials (Leash, Genetics Table, Mutagen) now have one, and the order of work can follow.

## 7. Balance: why this is not an exploit

* **The anchor:** a plain animal is a vanilla rabbit. The loot curve starts where vanilla's does.
* **Rare loot is paid for in guavas.** Stat points only grow with seed quality above 0 (quality 0 is zero-sum, DESIGN "4. Bred stats"), and quality comes from
  the guava tiers. So 40 points of Vitality, and certainly a legend at 100, cost the golden-to-netherite-and-up ladder, exactly like the other benefits of a bred line.
* **Cheap loot is cheap on purpose.** Plain flesh is renewable with plain guavas (a seed = two guavas = 1 to 6 pups). That is cattle-farm economics and it is fine; meat is not scarce.
  The existing crowding guard (40 bunnidogs within 16 blocks) and the breeding cooldowns cap throughput; pups give only a quarter.
* **Concave tails.** Stats are unbounded but the drop count is not: at 1000 points flesh is about 19 (cap 16). Same trick as the Bloom to quality conversion.
* **No farming of rarities:** layers 2 and 3 need a player kill; Looting is a modest bonus on chance and does not touch the legends.
* **A bad line pays badly.** Negative points cut a line's output to its floor, so "an animal bred against Swiftness has no feet" and the drops carry a signal about the line.
* **Culling has a cost.** Each kill is a lost animal, and the high-value animals are the ones you want for breeding. The Mutagen choice (breed the mutant or process it) is the clearest case.

## 8. Delivery plan (small chunks, per `START_HERE.md` section 6)
D0 is this plan. Every chunk below is one turn, not compiled until the user says so, and ends with the usual docs and a zip.

| Chunk | What | New Minecraft names (to verify first) |
|---|---|---|
| D1 | The engine: `genetics/Drops.java` (curves, Context, Layer 1) + tests; the event handler; Raw/Cooked Bunnidog Flesh and Rabbit's Foot only; Calipers line + `/bunnidogs drops` | `LivingDropsEvent` (SOURCE), `Enchantments.LOOTING`, `new ItemEntity(...)` (SOURCE), the smelting/smoking/campfire recipe JSON formats, one name to read the killer's weapon |
| D2 | The rest of layer 1: Sinew, Whisker, Bone Meal; baby/burning/looting context; Prime/Roast Haunch | none expected |
| D3 | Coat pelts (12) + lineage signatures (layer 2, coat table) | none (items from a table; art by `art/gen_pelts.py`) |
| D4 | Body-part signatures + Mutant Marrow + Fang + Starlit Dust | none |
| D5 | Tissue Sample (component) + Calipers reads it + Worn Ear Tag | the data component pattern is COMPILED (seed) |
| D6 | Legends (layer 3) | none |
| D7+ | The sinks, each its own chunk and decision: Leash (Sinew), Genetics Table (Whisker), Mutagen (Marrow), Gene Splicer (Sample) | each its own |

Each chunk keeps the project's habits: pure Java in `genetics/` with an exact-number self-check (a handful of assertions, not thousands of samples), the glue thin,
art through a script, `docs/` updated, never marked COMPILED until the user says so.

## 9. Open questions (the agenda for the next messages; defaults in brackets)

1. **Death only, or also living harvest?** The mod's taste is management and long-term care, and a non-lethal channel fits it: shear a woolly one for wool, brush
   fluff off any of them, with a cooldown (this needs one new saved field, the last-harvest time). [death only for now; living harvest as a later wave]
2. **Pups:** a quarter of the stat lines and nothing else [yes], or nothing at all?
3. **Pelts:** modded per coat, 12 items [yes] or a single vanilla rabbit hide?
4. **Legends:** keep, drop, or merge into the top tier of recipes? And what are they for?
5. **Tissue Sample:** "Calipers in hand = guaranteed" [yes], a lower flat chance, or a separate syringe item (non-lethal, from the original bloat list)?
6. **Layers 2 and 3 only on player kills** [yes].
7. **Carried (hidden) recessive alleles:** count at half chance, so culling reveals what an animal carries (swamp `glow`)? [parked]
8. **Where does the table live:** in Java like `Juice.DEFS` [yes; testable, one place] or in data JSON so modpacks can edit it?
9. **Experience:** scale the XP orbs with worth? [no]
10. **Raw flesh:** harmless like vanilla rabbit [yes] or a small Hunger chance?
11. **Flesh flavour (parked idea):** flesh from a fleet-legged animal gives a few seconds of Speed when eaten (reuse `Juice.DEFS`; needs a data component on the stack).
12. **Which items to cut or add** from the catalog above: the user said not to be afraid of many items, so the default is to keep all of them.

## 10. What does NOT change
No new genes, alleles or stats; no change to seeds, guavas, juice, breeding or the save format; the innate multipliers stay as they are (drops only read them).
