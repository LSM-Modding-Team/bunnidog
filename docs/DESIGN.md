# Bunnidogs — game design (current, agreed with the user)

**Contents (the sections are in the order they were written; find one with `grep -n '^## ' docs/DESIGN.md`):**
* *Core design*: Skeleton "Generations"; Phase 1, 2, 3 decisions (genome, breeding, lineages); "Seed & Guava loop" (the long design of seeds, crops, guava tiers, stat drift); Phase 4a-4e and 4g decisions; Session 6 tier qualities; Rejected / parked skeletons.
* *Tools and the book*: Phase 5a, 5c, 5d, 5e; Command Block Guava; Phase 8 S1-S7b (the sheet, portrait, hover, Calipers window); Genetics Table decisions.
* *World*: Phase 6 decisions (settlements: levels, houses, paths, props, styles); Session 9 (uneven ground); Phase 6h (meeting tree); Phase 7a (guava wood); V1a/V1b (wood villages); **Warren redesign decisions (session 45, at the end; the plan is `docs/WARREN_PLAN.md`)**.
* *Drops*: Drops plan; Drops D1-D8e decisions.
* *Homes*: Home H2 and H3 decisions (the system itself is in HOME_SPECS.md).
* *Ideas*: "Ideas (not started; the user decides when)". The current to-do order is `docs/ROADMAP.md`.


## Skeleton: "Generations" (breeding & genetics)
Everything hangs off breeding lineages. You'd spend ~10 minutes at a time: checking the pedigree, pairing two
bunnidogs, waiting for the litter, inspecting the babies. Systems are **optimistic** (no inbreeding penalties/defects).

### Genome: 6 gene slots, two alleles each (one per parent), dominant/recessive
| Gene | Visible effect | Mechanical effect |
|---|---|---|
| Ears | length, shape, droop | hearing range, heat tolerance |
| Coat | color, fluff, patches | cold resistance, camouflage |
| Legs | length, paws vs hooves | speed, jump height |
| Build | chonk level | health, carrying capacity |
| Temperament | posture, tail | skittish / loyal / feral |
| **Fertility** (replaces the old "Face" gene) | (visible cue TBD) | **how often they breed and litter size** |

Fertility trade-off: high fertility = bigger swarm capacity but LESS CONTROLLED evolution (more random offspring).
The face itself stays a fixed baby-like face (the uncanny look is the base model, not a gene).

### Rules
* Dominant/recessive: two normal parents can carry a hidden recessive.
* Mutations: small random chance per litter of a brand-new allele (mostly neutral, some strong).
* NO inbreeding/defect mechanic (user chose optimistic).
* Genes are **visible** (guessable by looking); tools give **exact measurements**.

### Wild lineages = disconnected branches of evolution
Each biome has an isolated wild population with founder alleles that exist nowhere else, so exploration provides
raw material and breeding shapes it. Tundra (tiny ears, thick coat), Desert (huge ears, fast, fragile), Swamp
(webbed paws, glow recessive), Cave (pale, night vision, detects ores), Mountain (hooves, goat jumps), Nether
(fireproof, aggressive), End ("Phase" allele: short blinks). Each has a distinct look and origin readable at a glance.
Bonus location: an isolated island where a lineage evolved with no contact (unique alleles).

### Artificial lineages (player-made)
Pedigree Book (auto-logs family trees, lets you name a lineage), Mutagen (forces a mutation next litter),
Gene Splicer (late game: move a fully bred-true allele between animals, costs donor health), Purebred status
(stable 5 generations -> bonus + registry title), Show Ring (contests judged on gene rarity/symmetry).
Endgame: **The Apex**, a boss version of the player's own line. G4 implements
its source-sample ritual, amplified bounded combat stats, inherited look and
adaptations, five legendary offerings and readable lineage crest. The original
bred points remain intact in the source/crest. See `APEX_SPECS.md`.

### Bloat layer (hangs off the breeding loop; add any time)
Blocks: Breeding Pen, Nesting Box, Genetics Table, Specimen Jar, Trophy Pedestal. Tools: Leash, Ear Tag, Calipers,
Carrot Syringe. Food: Growth Carrot, Gene Stew, Old Bones. Structures: abandoned breeding barns, regional
kennels, Show Ring arena. Villager: Breeder. Redstone: Pedigree Comparator. Achievements: "Nature Finds a Way",
"First Purebred", ... Everything should be a data/texture variation, cheap to program.

## Phase 1 decisions (implemented in code; made by Claude without asking, change freely)
Allele tables live in `genetics/Gene.java`. Each gene has 3 common alleles (a dominant "standard" and two recessives)
and 1 rare allele that only appears through mutation. Founders (wild-caught) never carry rare alleles.

| Gene | common (dominant first) | rare (mutation only) | value meaning |
|---|---|---|---|
| Ears | standard, long, short | huge | ear length x1.0 / 1.35 / 0.65 / 1.75; also scales awareness (follow) range |
| Coat | tan, cream, brown | spotted | texture variant only (cold resistance / camouflage come with biomes, Phase 3) |
| Legs | standard, long, short | springy | leg length x1.0 / 1.2 / 0.8 / 1.5: speed (+60% of the deviation), jump (+120%), visible length |
| Build | average, stocky, slim | chonker | body width x1.0 / 1.25 / 0.85 / 1.6; health x(1 + 1.5 x deviation) so chonker = 1.9x |
| Temperament | loyal, skittish, feral | stoic | loyal: big wag. skittish: avoids players, tail tucked. feral: bites back instead of fleeing. stoic: barely moves |
| Fertility | average, prolific, shy | swarm | x1.0 / 1.5 / 0.6 / 2.0; currently only scales mutation chance (litter size in Phase 2) |

* Dominance: the allele with the higher rank shows; the other one is "carried". Ranks are unique per gene.
* **Rare alleles are dominant** (they show at once, so a mutation is noticeable and fun). Recessive rares are still
  possible later (wild lineages will bring their own alleles); this was a judgement call, ask the user if they would
  rather have hidden mutations that need two carriers.
* Inheritance: per gene, one random allele from each parent, genes independent (no linkage). Slot 0 = mother's allele,
  slot 1 = father's (so a Pedigree can show who gave what).
* Mutation: chance per offspring = 4% x (average parent fertility)^2, capped at 50%. Examples: shy x shy 1.4%,
  average 4%, swarm x swarm 16%. This is the "high fertility = less controlled evolution" trade-off from above.
  A mutation replaces ONE allele slot of ONE random gene with a rare allele. A Mutagen will just pass chance 1.0.
* Visible cues already in: coat (texture), ears/legs/build (proportions), temperament (tail + ears). Fertility has no
  visible cue yet (open question, see the Genome table above).

## Phase 2 decisions (implemented in code; made by Claude without asking, change freely)
Rules live in `genetics/Breeding.java` (pure Java, tested); the entity glue is in `entity/Bunnidog.java`.

* **How you breed:** feed a **carrot** to two adults (vanilla love mode: hearts, they walk to each other). A carrot fed to a
  pup speeds its growth (vanilla behaviour). Holding a carrot makes bunnidogs follow you (vanilla Tempt goal). Pups follow an
  adult. No new item was needed, so nothing to register.
* **Litter size = 2 x the parents' average fertility multiplier**, rounded randomly (2.6 = 2 or 3 pups, 3 with 60%), 1..6:
  shy x shy 1.2, average 2, prolific x prolific 3, swarm x swarm 4. Every pup rolls its OWN Mendelian genome and its own mutation
  chance, so siblings differ.
* **Cooldown = 6000 ticks / the parent's OWN fertility** (never under 1200): shy 10000 (8.3 min), average 6000 (5 min),
  prolific 4000, swarm 3000 (2.5 min). Each parent rests by its own fertility.
* **The trade-off is now real:** mutations per LITTER = litter size x per-pup chance (4% x fertility^2): shy 0.017, average 0.08,
  swarm x swarm 0.64. A swarm line mutates roughly every second litter but it is nearly impossible to keep a line "true".
* **"Mother" convention:** the parent whose breed goal reaches the mating first counts as the mother (its alleles fill slot 0,
  the partner's slot 1). Bunnidogs have no sex.
* **Crowding guard (safety net, not a mechanic):** if 40 bunnidogs (pups included) already live within 16 blocks, no litter is born
  and the pair just rests. Silent for now; a visible cue (smoke particles / message) is an open question.
* **Pup look:** same model at 0.65 scale with a 1.25x head; genes still show (a chonker pup is a wide small pup).
* Temperament has no breeding effect. Side effect worth knowing: skittish ones keep 8 blocks away from players, so feeding them
  needs a pen or a corner (loyal ones are the easy ones). Left as is: it makes temperament matter to a breeder.
* Known limitation: another mod cancelling NeoForge's `BabyEntitySpawnEvent` would stop the first pup but not the rest of the litter.
* Not done yet (needs later phases): pup growth time is vanilla's 20 minutes and not gene-dependent; fertility still has no visible
  cue; no way to see genes except `/bunnidogs genome`.

## Phase 3 decisions (implemented in code; made by Claude without asking, change freely)
Code: `genetics/Lineage.java`, `genetics/Founders.java`, wild alleles in `genetics/Gene.java`; biome data in
`src/main/resources/data/bunnidogs/`; game glue in `registry/ModBiomeTags.java`, `entity/Bunnidog.java` (`tick()`), `registry/ModEntities.java`.

* **Three kinds of allele now:** common (in every ordinary founder), rare (mutation-only, unchanged) and **wild** (owned by one lineage: only its
  founders carry it, mutation never produces it, so it exists nowhere else unless a breeder moves it). Ids are still append-only.
* **Eight lineages, one per place** (recipes and numbers are in `Founders.java`, the debug genomes per lineage are in `docs/REFERENCE.md`, "Debug cheat sheet"): meadow (the baseline,
  plains/forest/savanna/taiga), tundra, desert, swamp, cave, mountain, nether, end. Every non-meadow lineage has its own coat so it is readable at
  a glance; the four other genes carry 1-3 wild or favoured alleles each. A recipe line means "each of the two allele slots of this gene becomes
  this allele with this chance, otherwise the ordinary pool" (so wild founders are usually but not always homozygous).
* **Wild alleles are dominant** (ranks 10+, below mutation alleles at 50, above common ones 1-3). Same reasoning as the rare alleles in Phase 1:
  the find shows at once. Side effect: crossing a wild bunnidog into a domestic line spreads its traits fast, and when two lineages are crossed the
  higher rank wins (bat > fennec > tiny ears; ember > slate > ... coats, see the ranks in `Gene.java`). Ask the user if they would rather have
  blending or recessive wild alleles.
* **One deliberate recessive: swamp `glow`** (rank 0, below every other coat). Most swamp bunnidogs only CARRY it (44% carry one copy, 6% show it),
  and two carriers give glow pups 1 in 4. It is the "hidden treasure" of the swamp and the first taste of breeding for a recessive.
* **Effects come through the existing channels only** (ears -> hearing range, legs -> speed and jump, build -> health, temperament, fertility):
  tiny ears 0.5x hearing, fennec 1.55x, bat 1.45x; fleet legs 1.21x speed / 1.42x jump; hoofed legs 1.36x jump; woolly 1.53x health; lean 0.55x
  health. Nothing depends on the biome or temperature yet (needs hooks, see "Not done").
* **Which lineage a bunnidog belongs to is decided by where it is first ticked**, not where it was created: a bunnidog with no genome of its own
  (natural spawn, plain `/summon`) rolls `Founders.roll(lineageAt(its biome))` on its first server tick. A genome from NBT (`Genome:"..."`),
  breeding or a command is never overwritten. The lineage itself is not stored: it can be read off the wild alleles (`/bunnidogs genome` prints
  `[tundra]` next to them).
* **Biomes -> lineage is data:** biome tags `bunnidogs:lineage_<key>` (vanilla biome ids, all `required: false`); a biome in no tag counts as meadow;
  if a biome is in two tags, code priority nether > end > cave > swamp > desert > tundra > mountain. The union tag `bunnidogs:wild_spawns` is what the
  biome modifier spawns into. Modpacks can extend the tags.
* **Natural spawning:** biome modifier `add_spawns`, weight 6 (vanilla animals are 8-12), packs of 2-3, mob category CREATURE (shares the creature
  mob cap with vanilla animals). Placement: on solid ground at the top of the world; on the surface only where the sky is open; cave and nether
  lineages also spawn underground/without sky. Deep dark is deliberately not in any tag (warden), and the main End island is not either.
* **Not done (needs later phases or was left out):** environment effects (tundra cold resistance, desert heat tolerance, cave night vision and ore
  sense, swamp swim speed and a webbed-paw look, nether fireproof, end "Phase" blink, coat camouflage) - these need biome/temperature hooks and
  are the natural Phase 3b; an isolated bonus island; structures; a visible fertility cue. The End lineage has a coat only (the "Phase" allele is
  the point of it and comes with a blink mechanic). No tundra/desert/... spawn tuning per lineage: one weight for all.

## Seed & Guava loop (user's ideas, session 4; steps 4a-4e are built, see the Phase 4a-4e decisions at the end; defaults below are Claude's, change freely)
The user's three ideas, made into ONE loop. "Some are really absurd, that's the soul of the mod": escalating absurdity is welcome.

    guava (rare, found at wild settlements) --feed to two adults--> love mode --> 1 SEED drops (holds both parents' genomes + quality)
    seed --plant like wheat--> ripe crop --harvest--> the litter is born HERE (genes rolled now; Fortune helps)
    seed + guava juice --infuse--> a "liquid pup": drinking it gives effects from that (rolled) genome. Seed is used up.

The one new object is the **Seed**: an item carrying `mother genome + stats, father genome + stats, quality`. Everything else hangs off it.

### 1. Seed (replaces the Phase 2 birth)
* Breeding no longer spawns pups. When a pair mates, exactly ONE seed drops (parents still rest by their own fertility). This deliberately replaces
  the tested Phase 2 litter code: `spawnChildFromBreeding` / `getBreedOffspring` stop spawning pups; `Breeding.rollLitter` is reused at harvest.
* The dice are NOT rolled when the seed drops. The seed stores the two parent genomes; `Genome.breed` runs per pup at harvest. Consequences:
  a seed can be inspected (Calipers later: "these parents, these odds"), planted anywhere (seeds are portable genes, e.g. a nether seed on your farm),
  and stacking is impossible unless the payload is identical (different data components = separate stacks; fine).
* Fertility keeps its meaning: it sets the litter size at harvest (still 2 x average fertility, 1..6) and the parents' cooldown. The crowding guard
  moves to harvest time (if 40 bunnidogs are within 16 blocks, the ripe crop yields nothing / stays ripe until there is room).
* Crop: like wheat (farmland, 8 growth stages, bone meal works). Needs a BlockEntity to remember its seed payload (the most expensive new piece).
  Harvested unripe: you get the seed back, payload intact. Harvested ripe: pups appear, the seed is gone (one seed = one litter).
* **Fortune** (the tool that breaks the ripe crop) does exactly ONE thing (user): it raises the quality used for this harvest, by 0.5 per level
  (Fortune III = +1.5, about a golden-to-diamond guava; one number to tune). Litter size and mutation chance are untouched: fertility still governs
  both, exactly as in Phase 2.

### 2. Guava (replaces the carrot)
* Food for breeding and for growing pups (vanilla `isFood`): plain guava = quality 0. Carrots stop working (user: "instead of carrots").
* **Where:** small, very rare guava trees. Every natural settlement of wild bunnidogs ("warren") has one. This REPLACES the Phase 3 random
  biome-modifier spawns: wild bunnidogs live in warrens (a small structure/feature per lineage biome: a few bunnidogs held by structure spawn
  data + one guava tree). The lineage still comes from the biome (already built), so no genetics change. Exploration = find a warren.
* **Renewable, but slow (Claude's default):** guava fruit can be planted (guava sapling/pit), so the rarity is in FINDING the first tree, not in a
  hard cap. Growth is slow and yields are low, so it stays precious. Without this, breeding would be finite.
* **Tiers** (golden-apple style: previous-tier guava in the middle, 8 of the material around it; last ones use a single rare item):

  | Tier | Guava | Made from (session 6) | Seed quality |
  |---|---|---|---|
  | 0 | Guava | grown / found | 0 |
  | 1 | Golden Guava | guava in the middle, 8 gold ingots around | 1 |
  | 2 | Diamond Guava | golden guava + 8 diamonds around | 2 |
  | 3 | Netherite Guava | diamond guava + 8 netherite ingots around | 4 |
  | 4 | Nether Star Guava | netherite guava + **4 nether stars in a plus** | 7 |
  | 5 | Beacon Guava | nether star guava + **8 beacons around** | 12 |
  | 6 | Dragon Egg Guava | beacon guava + a dragon egg (shapeless) | 20 (absurd cap) |

  The quality column is NOT the tier number any more (session 6, see "Session 6 decisions" at the end: why 0, 1, 2, 4, 7, 12, 20).
  (Built in Phase 4e, see "Phase 4e decisions". Names and count are examples; the ladder is a table, adding a tier = one enum constant + one recipe + one texture palette + one lang line + one tag entry, and the self-check fails until all exist.) Each tier costs the whole ladder
  below it, so the top is absurdly expensive on purpose. A joke creative-only "Command Block Guava" is free bloat.
* **Quality of a seed = average of the two parents' guava QUALITIES** (each parent is fed its own guava; fractions allowed; the quality of each tier is the table above). Harvest quality = seed
  quality + the Fortune bonus. **Quality has ONE job: it scales the stat drift** of the pups (see "4. Bred stats"): it raises the positives
  and lowers the negatives. It no longer touches litter size or mutation chance (an earlier draft had it do both; dropped so the dial is clear).
  Parked: "high tiers may mutate in WILD alleles from any lineage", "top tier = guaranteed mutation". The Mutagen item returns to the late phase.

### 3. Guava juice and infusion
* Guava juice: a drink item (crafted from guavas; modest food by itself, "an okay food source").
* **Infuse** = juice + one seed (shapeless recipe with a custom result, or a later Juicer block). The seed is used up. At infusion the game rolls ONE
  phantom pup from the seed (`Genome.breed` with the seed's quality) and freezes that genome in the bottle, i.e. the bottle is a "liquid pup".
  Its tooltip shows the effects, so nothing is a surprise at drinking time. Potency also scales with the phantom's bred stat points, so a heavily bred seed makes stronger juice.
* Effects come from the phantom's EXPRESSED genes (strength = the allele's value, duration/amplifier scale with seed quality, hard cap on both):

  | Gene | Effect idea |
  |---|---|
  | Ears | hearing = short Glowing on nearby mobs (the mobs you "hear"); `bat` also Night Vision; `tiny` a cozy little Resistance |
  | Coat | one effect per coat: snow = freeze immunity, sand = Fire Resistance, moss = Dolphin's Grace / Water Breathing, glow = Night Vision + you glow, pale = Night Vision, slate = Slow Falling, ember = Fire Resistance + Strength, void = a short Levitation blink; plain coats = nothing |
  | Legs | Speed, Jump Boost (`fleet` more speed, `hoofed` more jump) |
  | Build | Resistance / Absorption (`woolly`, `chonker`), `lean` = Speed instead |
  | Temperament | feral = Strength, loyal = Regeneration, skittish = a short Invisibility, stoic = Resistance + Slowness |
  | Fertility | luck-ish: `swarm` = Luck, `shy` = nothing |

  Absurd extras are welcome (e.g. a `spotted`/`chonker` juice that makes you briefly wide). Limits: potency capped, and the price is a seed
  (a whole litter) plus guava tiers, so it is a real trade-off: pups or a drink.

**BUILT in Phase 4f (session 6); the list above was the idea, this is what the code does** (`genetics/Juice.java`, `genetics/Infusion.java`, `item/JuiceItem.java`, `SeedItem#use`):
* **Two items, one class.** `guava_juice` (plain, stacks to 16) = shapeless recipe: one PLAIN guava + a glass bottle (a golden or dragon egg guava is never wasted on juice). Drinking it restores a little hunger (Saturation II). `infused_guava_juice`
  (stacks to 1, uncommon) holds the liquid pup in the string component `bunnidogs:infusion_data` (`genome/stats|quality`, e.g. `00-00-00-11-00-00/3,-1,0,2,12|4.5`; hand-writable with `/give`). It has no recipe and no creative-tab entry.
* **Infusing is done with the SEED, not a recipe:** hold a seed in the main hand and a plain juice in the off hand and right-click (not on farmland: planting wins there). `Infusion.roll(seed)` is one pup of `Seed.hatch` (a Mendelian genome with its own mutation
  roll, stats = the parents' average + the pair's drift at the seed's quality), without the litter size and crowding, and without Fortune. The seed and the juice are used up (not in creative), the bottle goes to the inventory or the floor.
  Why not a shapeless recipe, as the idea said: the result carries data, which needs a custom recipe class (several vanilla recipe types whose 26.2 signatures are not in the sources I can read). It stays possible later (a Juicer block is the other parked idea).
* **What a juice does** is fixed at infusion and shown on the tooltip (F3+H adds the raw liquid pup). Each EXPRESSED allele maps to effects in `Juice.DEFS` (28 alleles; every wild and rare allele has one, plain ones none, `spotted` is the deliberate blank). Differences from the idea table:
  snow = Absorption (vanilla has no freeze effect), every temperament has an effect (so no juice is ever empty; the common `loyal` gives a little Regeneration), swarm and starborn both give Luck (starborn also makes you glow), `chonker` gives Absorption and a short Slowness
  (the "briefly wide" joke needs the scale attribute, parked), `prolific` and `shy` give nothing, `tiny` ears and `stocky`/`woolly` build give Resistance, hearing ears (`long`, `huge`, `fennec`, `bat`) put Glowing on EVERY OTHER living thing within 8 blocks x the ear scale.
* **The numbers (all constants in `Juice`, all tested):** amplifier of a scaling effect = floor(quality / 4), at most that effect's own cap (Resistance stops at III so nobody is invulnerable; Speed, Jump, Strength, Regeneration at IV; Absorption and Luck at V); effects that are a drawback or a trick (Slowness, Levitation,
  Night Vision, Fire Resistance, Glowing, ...) never scale. Duration = base seconds x (1 + 0.1 x quality) x (1 + 0.02 per bred point of the stat that belongs to the gene, at most doubled), never above the effect's own maximum (Levitation 6 s, Slowness 60-90 s, Glowing and Regeneration 90-120 s, the rest 5-30 minutes).
  Two genes with the same effect make ONE effect, the longer duration wins. Stat links: ears Senses, legs Swiftness (speed) or Leap (jump), build and the fighting temperaments Vitality, loyal Senses, luck Bloom, coats none.
* **With the quality table of session 6:** plain, golden and diamond seeds (q 0-2) make level I juices; two netherite guavas (4) level II; a nether star pair (7) level II with 1.7x the duration; a beacon pair (12) level IV where the cap allows, 2.2x; a dragon egg + beacon (16) and above hit every cap.
  The strongest juice therefore needs the strongest seed, which is the point: a seed is a whole litter.
* **Not done:** a Juicer block, a custom infusion recipe, the scale-based "wide" joke, tier-dependent juice (a golden-guava juice), juice from several seeds, cooldowns. The two non-glued parts `JuiceItem` and `SeedItem#use` are not compiled yet.

### 4. Bred stats (user, session 4: "genes as fixed multipliers do not allow indefinite growth; change that")
The problem: in Phase 1 every stat is fixed by the expressed alleles (a chonker is 1.9x health, forever), so a breeder reaches the best combination
and is done. New rule: **genes decide the DIRECTION a line drifts in, quality decides how much it gains.** Stats can grow without limit.

* **Five bred stats**, stored per bunnidog as signed whole *points* (start 0, unbounded upward, NBT key `Stats` as text like "3,-1,0,2,12", an old save without it reads as
  zeros so nothing breaks): **Vitality** (max health), **Swiftness** (speed), **Leap** (jump strength), **Senses** (follow/awareness range) and
  **Bloom** (how fast pups grow up, and the seed-quality gene; see "5. Bloom").
* **Final attribute = base x innate allele multiplier (Phase 1, unchanged) x curve(points).** The innate multiplier stays as the flavour you get
  from a find; the points are what breeding adds on top. Starting curve (all tunable, one pure-Java class): about +4% per point; for Swiftness and
  Leap a square-root tail past ~25 points (still unbounded, but a huge speed or jump breaks vanilla movement); a floor of 0.3x on the negative
  side so nothing reaches zero.
* **Inheritance, per pup, at harvest:** child points = average of the two parents' points + drift, then stochastic rounding to whole points
  (drift 0.5 = +1 half the time). So siblings from one seed differ, and picking the best pup to breed on is real breeder skill.
* **Drift is set by the PARENTS' genes** (their expressed alleles, so it can be read off the animals or the seed before anything is planted; the
  seed stores the parents' genomes and points, so a Calipers tool can show the exact expected result). Each parent adds up a **trend** from its
  five stat-relevant genes (Ears, Legs, Build, Temperament, Fertility); the pups get the average of the two parents' trends. An allele's trend is "+a to one stat, -b to another".
* **Quality reshapes every trend:** each plus part is multiplied by (1 + 0.5 q), each minus part by max(0, 1 - 0.25 q), q = seed quality +
  Fortune bonus. At q = 0 a trend is zero-sum: a line can be re-specialised but not grown, so plain guava only steers. Growth starts at q > 0,
  the negatives are gone at q = 4, and the absurd guavas are absurd. Example, a sturdy line (+1 Vitality, -1 Swiftness per generation):
  q 0 gives +1/-1, q 2 gives +2/-0.5, q 4 gives +3/0, q 6 gives +4/0.
* **Trend sizes:** the default alleles (standard, average, loyal) have no trend, so an unbred line stays put. Ordinary alleles are +1/-1
  (zero-sum), wild-lineage alleles +2/-1, rare mutation alleles +3/-1 (finding new alleles is what makes growth worth chasing). Coat has no trend (look only).

  | Gene | Allele: (+stat / -stat) |
  |---|---|
  | Build | stocky +Vit/-Swift; slim +Swift/-Vit; chonker (rare) +Vit/-Swift; woolly (tundra) +Vit/-Leap; lean (desert) +Swift/-Vit |
  | Legs | long +Swift/-Senses; short +Senses/-Swift; springy (rare) +Leap/-Senses; fleet (desert) +Swift/-Vit; hoofed (mountain) +Leap/-Swift |
  | Ears | long +Senses/-Vit; short +Vit/-Senses; huge (rare) +Senses/-Vit; tiny (tundra) +Vit/-Senses; fennec (desert) +Senses/-Vit; bat (cave) +Senses/-Leap |
  | Temperament | skittish +Swift/-Vit; feral +Vit/-Senses; stoic (rare) +Vit/-Swift; loyal none |
  | Fertility | prolific +Bloom/-Senses; shy +Senses/-Bloom; swarm (rare) +Bloom/-Vit; starborn (End) see "5. Bloom"; average none |

  Several genes add up: stocky build + long ears, for example, nets Vitality 0, Swiftness -1, Senses +1 (the Vitality plus and minus cancel).
* **Not done:** wild founders start at 0 points (option: pre-drifted founders, as if they had drifted for a few generations); no visible cue for the
  points (they show in `/bunnidogs genome`, later in Calipers); no cap and no decay (deliberate).
* **Independent of seeds and guavas:** it works with today's carrot breeding at quality 0 (a sturdy pair's pups drift), which makes it the natural
  FIRST step of Phase 4 and lets the stat maths be tested to the same standard as the genetics (pure Java: zero-sum at q 0 for every ordinary
  allele, negatives vanish at q 4, hundreds of generations grow without bound and never reach the floor, unbiased rounding, every allele of the five genes has a trend entry, old saves read as zeros).

### 5. Bloom (user, session 4): faster growing up, and the gene that feeds seed quality
The ask: some Fertility genes should also affect how quickly a pup becomes an adult and give a seed-quality boost, as a numerical stat of its own.
The gene that touches quality itself is very powerful, so it is very rare (the End) and balanced, but still exploitable.

* **Bloom** is the fifth bred stat, with the same rules as the other four (points, trends, quality-scaled drift, unbounded).
* **Growth speed (every animal):** more Bloom = pups become adults sooner, less = later. Multiplier 1 + 0.05 per point up to 20 points (x2), then a
  square-root tail (x4.2 at 100, x9.8 at 1000); negative Bloom slows down to 0.4x at worst (a shy line: up to 50 minutes). Vanilla is 20 minutes.
  Hard floor: never an adult in under 60 s (the same one-minute floor as the breeding cooldown), so generation time stays bounded. Implemented by
  giving baby bunnidogs extra (or fewer) age ticks; guava still speeds a pup up on top, as in vanilla.
  | Bloom | 0 | 10 | 20 | 50 | 100 | 300 | 1000 |
  |---|---|---|---|---|---|---|---|
  | Time to adult | 20 min | 13 min | 10 min | 5.9 min | 4.7 min | 3.2 min | 2.0 min |
* **The common half:** the ordinary Fertility alleles carry Bloom trends (prolific +Bloom/-Senses, shy +Senses/-Bloom, rare swarm +Bloom/-Vit), so
  fertile lines mature fast and shy lines slowly. Safe and useful; no quality effect.
* **The powerful gene, `starborn`:** a new WILD Fertility allele owned by the END lineage (append-only id 4 in the Fertility table, added in step 4a
  together with Bloom; about 40% per slot in the End founder recipe). Dominant like every wild allele. It does three things:
  1. Trend: +2 Bloom per generation.
  2. **Quality conversion:** a parent that EXPRESSES starborn adds 0.3 x sqrt(its Bloom points) to the seed's quality; the seed gets the AVERAGE of
     the two parents' bonuses (a pure pair gets all of it, one starborn parent half). Nobody else turns Bloom into quality.
  3. **Its price:** fertility multiplier 0.7 (smaller litters, longer cooldowns, fewer mutations: "fewer, better"), and a toll of -1 Vitality and
     -1 Senses per generation that quality does NOT shrink (the single exception to "quality lowers the negatives").
* **Why it is balanced, and still exploitable:**
  * Rare: only the outer End islands (an End trip); a wild allele, so mutation never makes it.
  * Concave conversion: the sqrt is essential. Simulated at guava tier 2, a linear +0.1-per-point bonus runs away (Bloom 1770, quality 162 after 40
    generations); with sqrt it is Bloom 283, quality 7. Still unbounded, just not explosive.
  * Needs other stats: the toll bleeds Vitality and Senses every generation, so the line must carry counter-alleles (stocky build for Vitality, long
    ears for Senses). That pins Build, Ears and Fertility to a recognisable "Bloom build", and stocky's Swiftness cost stays until quality reaches 4.
  * Throughput: 0.7 fertility means no swarm of pups; it is not a mass-production engine.
  * The exploit path: End trip, starborn founders, cross into stocky/long-eared lines, feed guavas to bootstrap, and the loop feeds itself. Simulation of
    a pure line with starborn + stocky + long ears (both parents identical, drift per generation, Fortune 0):

    | Guava tier | gen 10: q / Bloom / Vit / Senses / Swift | gen 30: q / Bloom / Vit / Senses / Swift |
    |---|---|---|
    | 0 | 1.5 / 29 / -3 / +5 / -8 | 3.2 / 118 / +14 / +29 / -16 |
    | 2 | 4.1 / 53 / +15 / +16 / -2 | 6.1 / 197 / +67 / +68 / -2 |
    | 4 | 6.5 / 75 / +28 / +28 / 0 | 8.8 / 271 / +106 / +106 / 0 |

    Even plain guava bootstraps it, slowly (Vitality bleeds for about 15 generations until quality catches up); tier 2 and up shortcut that, which is
    what the absurd guavas are for. A Bloom of 100 also cuts a generation to under 5 minutes.
* **Seed quality, complete:** average guava tier of the two parents + their average starborn bonus (both fixed when the seed drops) + the Fortune bonus
  (added at harvest).
* Tests to add with step 4a (pure Java): the growth curve is monotone with its floor and the 60 s cap; the starborn bonus is 0 without the allele
  and is the average of the two parents; the toll is not reduced by quality; every Fertility allele has a trend; the loop simulation above stays
  polynomial (no runaway) and never reaches the negative floor with counter-alleles.

### How this changes the roadmap (the current roadmap is `docs/ROADMAP.md`)
The old Phase 4 (tools) and Phase 5 (artificial lineage tools) move back by one. Order of the new **Phase 4 "Seed & Guava & Stats"** in
independently testable steps: 4a bred stats incl. Bloom (pure Java + NBT + attributes + baby growth hook + the End allele `starborn`; shown in `/bunnidogs genome`; quality
is only the parents' starborn bonus for now),
4b guava item + swap the food, 4c seed item (data component) + breeding drops a seed, 4d seed crop + harvest births (+ Fortune -> quality),
4e guava tiers -> quality, 4f juice + infusion, 4g warrens (structures) + guava trees, which also removes the Phase 3 random spawns.
Calipers/Pedigree Book (later) read seeds as well as animals. The Mutagen stays a late-phase item.

### Open questions (defaults chosen above, cheap to flip)
Guava renewable-but-slow (chosen) vs. only from natural trees. Infusion rolls a phantom pup (chosen) vs. deterministic effects from the parents'
genes. Juice by crafting (chosen) vs. a brewing stand / Juicer block. Drift set by the parents' genes (chosen, predictable and readable from the seed)
vs. by the pup's own genes. Fortune +0.5 quality per level (chosen, one number). Innate allele multipliers kept under the drift (chosen, no rewrite)
vs. removed with wild founders starting pre-drifted. Bloom: the name; the sqrt quality conversion factor 0.3; starborn at 40% per End slot,
fertility 0.7, toll -1/-1 (all tunable). Quality bonus averaged over both parents (chosen) vs. summed.

## Phase 4a decisions (implemented in code, session 4; made by Claude without asking, change freely)
Code: `genetics/Stat.java`, `Stats.java`, `Trends.java`, `Drift.java`, `Growth.java`, the `starborn` allele in `Gene.java`/`Founders.java`; glue in
`entity/Bunnidog.java` and `command/BunnidogsCommands.java`. The numbers are the ones in "4. Bred stats" and "5. Bloom" above; the self-check
verifies them (including the simulation table). Extra choices:
* Stats are saved as TEXT (`Stats:"vitality,swiftness,leap,senses,bloom"`), padded with zeros if short, so old saves and hand-written NBT work.
* Points are clamped to +-1,000,000 only to keep int maths safe. Attribute curves: +4% per point; Swiftness/Leap: x2 at 25 points then
  2 + 0.15 x sqrt(points - 25); nothing below x0.3. Growth: 1 + 0.05 x Bloom to 20 points, then 2 + sqrt(Bloom - 20)/4, capped at x20 (60 s), floored at x0.4.
* Wild founders start with 0 points (they have not drifted yet). The pup inherits the average of the parents' points, so a bred line builds on itself.
* No new command: stats for testing are set with NBT (`/summon ... {Stats:"..."}` or `/data merge entity ...`). `/bunnidogs genome` only reads.
* Until guavas exist, "quality" is only the starborn bonus of the parents; guava tiers and Fortune add to it in later steps.
* Not done in 4a: any visual cue for stats, anything that uses Bloom for juice potency (comes with juice), stats on the seed (comes with the seed).

## Phase 4b decisions (implemented in code, session 4; made by Claude without asking, change freely)
Code: `registry/ModItems.java`, `Bunnidog.isBunnidogFood`; data/assets under `assets/bunnidogs/{items,models/item,textures/item}` and `data/bunnidogs/tags/item`.
* The guava is a plain food item (3 hunger, 0.3 saturation modifier: "an okay food source"), light green fruit texture from `art/gen_guava.py`.
* What bunnidogs eat is the item TAG `bunnidogs:bunnidog_food`, not a hard-coded item: the tiers of step 4e will simply be added to the tag, and a
  modpack or the user can add carrots back with one line. Carrots are not in the tag (the user: "guavas instead of carrots").
* No temporary crafting recipe or loot: until warrens and guava trees (4g) it is creative tab + `/give`. Chosen so a temporary shortcut cannot
  leak into the design; the price is that survival testing of breeding needs the creative inventory for now.

## Phase 4c decisions (implemented in code, session 4 part 7; made by Claude without asking, change freely)
Code: `genetics/Seed.java`, `item/SeedItem.java`, `registry/ModItems.java` (component + item), the breeding block and `hatch` in `entity/Bunnidog.java`,
`/bunnidogs seed` and `/bunnidogs hatch` in `command/BunnidogsCommands.java`; assets `items/seed.json`, `models/item/seed.json`, `textures/item/seed.png` (`art/gen_seed.py`).
* **The payload is TEXT in a string data component** (`bunnidogs:seed_data`, canonical form `mother/stats|father/stats|quality`, stats and quality optional). Same reason as
  `Stats` in 4a: a string is the lowest-risk thing to save and sync, it is hand-writable in `/give`, and it is readable in the tooltip and `/data`. A typed codec
  (`Codec.STRING.comapFlatMap` + `StreamCodec.map`, which would also reject bad payloads at `/give` time) is a possible later upgrade; the text form would stay the same.
* **The seed remembers its parents and a quality, nothing rolled.** Quality = guava tier (0 until step 4e) + the parents' average starborn bonus, both fixed when the seed
  drops; a Fortune-style bonus is added at hatching (`Seed.hatch(extraQuality, ...)`, `/bunnidogs hatch <bonus>`). Litter, Mendel, mutation and drift are the Phase 2/4a rules, moved
  from the moment of mating to the moment of hatching, so what changed is only WHEN the dice roll.
* **A mating drops exactly one seed** at the "mother" (the parent whose breed goal fires first), with hearts and a vanilla-sized 1-7 XP orb. The parents leave love mode and rest by
  their own fertility, as before. Skipped on purpose: the vanilla "animals bred" statistic and advancement, and `BabyEntitySpawnEvent` (no baby exists at the mating any more;
  other mods that listen for it will not see hatchlings).
* **The crowding guard moved to the hatching** (as planned in "1. Seed"): 40 bunnidogs within 16 blocks means nothing hatches and the seed is kept.
* **A debug bridge, because nothing else can make pups yet:** without the crop of 4d a seed would be a dead end and breeding could not be tested. `/bunnidogs hatch [bonus]` hatches the
  seed in your main hand at your feet through `Bunnidog.hatch`, which is also what the crop will call, so 4d only adds a block and a call site. Like the other `/bunnidogs` commands it has
  no permission check yet (lock them all before a public release; the bonus argument is a cheat).
* **The tooltip stands in for the Calipers:** a short readable summary always (quality, litter, mutation chance, stat drift), both parents' exact genomes and points only with
  advanced tooltips (F3+H), so the Calipers of Phase 5 still have a job. `/bunnidogs seed` prints everything, gene by gene.
* A plain sample seed (two default genomes, quality 0) is in the creative Ingredients tab. Seeds are not bunnidog food (not in the tag). The seed art is a cream seed with a green sprout; there
  is only one look (seed quality tiers are not planned).
* Not done in 4c: planting and the crop (4d), guava tiers feeding the quality (4e), juice (4f), warrens (4g); no visual cue for a seed's quality; no check that a seed's parents are
  compatible (any two genomes are).

## Phase 4d decisions (implemented in code, session 4 part 8; made by Claude without asking, change freely)
Code: `block/SeedCropBlock.java`, `block/SeedCropBlockEntity.java`, `block/SeedCropHarvest.java`, `registry/ModBlocks.java`, `SeedItem.useOn`, `Seed.fortuneBonus`; assets `blockstates/seed_crop.json`,
`models/block/seed_crop_stage0-3.json`, `textures/block/seed_crop_stage0-3.png` (`art/gen_seed_crop.py`). [Session 6: now stage0-7, one per age, see the amendments below.]
* **The crop is its own block** (a plain `Block` + `EntityBlock`, not a vanilla `CropBlock`), so nothing depends on vanilla crop internals. It has no block item and no loot table: it exists only by
  planting a seed, and what breaking it gives is decided in code (`SeedCropHarvest`), because the drop depends on the payload, the age and the tool.
* **Planting** is `SeedItem.useOn`: only on the top face of farmland with air above, only for a seed that holds genes; the payload is copied into the block entity, one seed is used (not in creative).
* **Growth:** 8 ages on random ticks, 1 age per 3 random ticks on average (about twice as fast as wheat; roughly 15-20 minutes). No light requirement, moisture ignored. [Session 6: bone meal works, and each of the 8 ages has its own embryo picture; the ripe one is the only one with an open eye. Originally 4 looks and a pale pod with pink ears.]
* **Harvest is the break, not a right-click** (that is what makes Fortune meaningful): unripe -> the seed comes back untouched (digging it up early costs nothing); ripe -> the litter is born at the crop through
  `Bunnidog.hatch` and the seed is used up, quality = seed quality + 0.5 x Fortune level of the main-hand tool (`Seed.fortuneBonus`, 0.5 per level as designed). Too crowded (40 within 16 blocks): the break is
  cancelled, the player is told, the crop stays ripe until there is room ("the crowding guard at harvest").
* **Creative mode:** an unripe crop breaks without dropping the seed (like any block), a ripe one still hatches, so the whole loop can be tested in creative.
* **Soil:** farmland or any dirt-like block (`BlockTags.DIRT`), so a trampled field does not lose the seed. Without soil the crop pops off on its next random tick and returns the seed.
* **Known limits:** a crop removed by water, a piston or an explosion gives nothing (the seed is lost); a crop placed with `/setblock` holds no seed and gives nothing; no planting sound; no bone meal (deferred,
  the `BonemealableBlock` signatures could not be verified in the sources); no light requirement. Possible later fixes: a loot table with `copy_components` from the block entity so any removal returns the seed.
* Not done in 4d: guava tiers feeding the quality (4e), juice (4f), warrens and guava trees (4g). [4e and 4f are done since; only 4g is open.]

### Session 6 amendments to 4d (bone meal and looks; made by Claude after the user's requests, change freely)
* **Bone meal works** (the user asked): the crop implements vanilla's `BonemealableBlock`, so a player or a dispenser can use it. One bone meal grows 2 to 5 ages (wheat-like spread for an 8-age crop, `BONEMEAL_MIN_AGES`/`MAX_AGES`), never past ripe;
  it is refused on a ripe crop; it always succeeds. The planted seed stays (a state change only). It does NOT change what harvest gives: bone-mealing is not a quality source (Fortune and guavas remain the only ones).
* **Looks: the crop is an embryo** (the user: absurdness is the soul of the mod). The stem is an umbilical cord (a pale twisted rope with a red and a blue vessel), and what sits on it is a fetus developing, attached directly
  to the cord, no capsule or pod. One picture per age (the 4 pictures for 8 ages are gone): 0 a bead on the cord, 1 head bulb and tail, 2 an eye spot, 3 limb buds, 4 ear buds and a dark eye, 5 ears and fingers, 6 pale bunnidog skin and
  a closed eye, 7 (ripe) ears up, eye OPEN, rosy cheek. The ripe cue is the open eye with a white pixel. The model parent is `minecraft:block/cross` (ONE embryo in the middle of the block, in profile) instead of `minecraft:block/crop` (four plants side by side),
  because one fetus per block reads as a creature and four read as a field of them. The shape (hitbox) is unchanged.

## Phase 4e decisions (implemented in code, session 5; made by Claude without asking, change freely)
Code: `genetics/GuavaTier.java`, `genetics/GuavaMemory.java`, `item/GuavaItem.java`, `ModItems` (`registerGuava`, `GUAVAS`), `Bunnidog.mobInteract` and `spawnChildFromBreeding`; assets `items|models/item|textures/item/<tier>`, `data/bunnidogs/recipe/*.json`,
`tags/item/bunnidog_food.json`; art `art/gen_guava.py` (one palette per tier).
* **The ladder is the table above** (7 tiers; the quality of each tier is a table value since session 6, the tier's "level" 0..6 is its place on the ladder and is what a bunnidog remembers and saves). Each tier is its OWN item (not one item with a data component), so every tier has its own name, texture, recipe and tag entry and nothing has to be read from the stack.
* **Recipes:** golden apple style for tiers 1-3 (previous tier in the middle, 8 gold ingots / diamonds / netherite ingots around it, shaped); shapeless for tiers 4-6 (previous tier + nether star / beacon / dragon egg). **Session 6 changed 4 and 5:** nether star guava = 4 nether stars in a plus around the netherite guava, beacon guava = 8 beacons around the nether star guava (both shaped); the dragon egg stays shapeless. The plain guava has NO recipe: where it comes
  from is step 4g (for now creative tab and `/give`). Each tier consumes the whole ladder below it, as designed. No recipe-book unlock advancement (recipes are available from the start).
* **Quality = average of the two parents' tiers' qualities,** fractions allowed (`GuavaTier.average`; session 6: of the qualities, not of the ladder positions). Each parent's tier is the guava it was fed TO FALL IN LOVE; a parent in love by other means (a command, another mod) counts as tier 0. Any other food in the tag counts as tier 0 too
  (`GuavaItem.tierOf`).
* **Memory, not a lookup at mating time:** by the time they mate the guava is gone, so the parent remembers the tier for as long as vanilla's love lasts (600 ticks) and forgets it when the age leaves 0 (mating, cooldown), so a tier cannot leak into a later love. It is
  stored only when vanilla really accepted the food for love (adult, age 0, not already in love; feeding a pup only grows it and does not touch the memory). It is saved in NBT (`LoveTier`, `LoveTierTicks`, written only while it is live).
* **Quality keeps its ONE job** (stat drift): the checks assert that litter size and mutation chance are identical for every tier pair. At average 4 and above the drift costs are gone (design of DESIGN "4. Bred stats"); 6 is the absurd cap (plus x4, costs 0).
* **Eating:** every tier is ordinary food for players (3 hunger, 0.3 saturation): eating a dragon egg guava wastes it, deliberately (no `alwaysEdible`, no special protection). A right-click on a bunnidog should be handled before the item is eaten (vanilla's order of entity interaction vs item use; assumed, part of the 4e in-game test list).
* **Looks:** name colour by rarity (plain white, golden yellow, diamond and netherite aqua, the last three purple); no enchantment glint. Textures are recolors of the guava (golden, cyan, dark grey with red speckles, white with a sparkle, blue with a sparkle,
  purple with magenta dots and sparkles). Tooltip: "Seed quality N" + "Averaged over the guavas of both parents".
* Not done in 4e: the joke creative-only "Command Block Guava" (one enum constant + palette + lang + tag entry, but it would need an exception in the "every tier above the plain one has a recipe" check), any glint, and world sources of guavas (4g).

## Phase 4g decisions (session 6): warrens and guava trees (Claude's choices, change freely)
* **A warren is a worldgen Feature, not a jigsaw structure:** no NBT templates to write blind, the same Java builds it for the generator, for `/bunnidogs warren` and in any dimension. Its layout is pure Java (`layout/WarrenShape`): one guava tree in the middle, a 21-column coarse-dirt patch, 3-4 burrow holes 2 deep, 2-4 wild bunnidogs.
  The idea of "structure spawn data" is dropped: the bunnidogs are real persistent entities placed at generation, so they never despawn and do not respawn (breeding is how you get more; a warren is a one-time find).
* **Natural spawns are gone** (the design said warrens REPLACE them): `add_wild_bunnidogs.json` and `wild_spawns` deleted. The lineage of a bunnidog still comes from the biome it stands in (founder roll on its first tick), so a plain `/summon` still follows the biome.
* **Where:** surface (meadow, tundra, desert, swamp, mountain lineages) 1 in 60 chunks; caves 1 in 4 chunks tried at a random height (many tries fail: no cave there); nether 1 in 6; end islands 1 in 6 (most tries over void fail). Caves and the nether search up to 40 blocks down from their random height for the first place with 2 air blocks above solid ground. Surface and end warrens do NOT search (session 8 fix, see below): they build exactly on the terrain top or not at all, so water-covered swamp spots, ocean and cave ceilings are skipped quietly.
* **Guava tree:** vanilla jungle logs and persistent jungle leaves (looks like a small jungle tree; custom wood is polish), 1-3 hanging `guava_fruit` blocks. **Renewable:** a ripe fruit is picked with a right-click and grows back (green -> ripe in about 10 minutes at the default random tick speed), so one tree is a slow, steady, low supply; a sapling (one guava, plain only) grows into a tree on 1 in 20 random ticks.
  So the rarity is in FINDING the first warren, as designed. Yield per tree: up to 3 guavas per 10 minutes, 3 hunger each, or one tier-up step of the ladder per several trees.
* **Not done:** custom wood/leaves/saplings drops, a structure with a proper look (a hill, tunnels: now planned in `docs/WARREN_PLAN.md`), warren respawning, villages-style loot, light requirement for saplings, other mods' trees.

## Session 6 decisions: the quality of each guava tier (user: "make each tier have a proper quality add scaling")
Code: `genetics/GuavaTier.java` (the `quality` column and `level`), `GuavaMemory`/`Bunnidog` unchanged (they hold the LEVEL, the quality is looked up when the two mate), `item/GuavaItem.java`, `registry/ModItems.java` (rarity by level).
* **The problem, in numbers.** Quality does exactly one thing (scales the stat drift): gains x(1 + 0.5q) for ever, costs x max(0, 1 - 0.25q), gone at q = 4. For the standard +1/-1 trend that is a net drift of 0.75q per generation up to q = 4 and 1 + 0.5q beyond.
  So a point of quality is worth 0.75 up to 4 and only 0.5 after. The old table (quality = ladder position 0..6) therefore gave: netherite +0.75, nether star +0.75, beacon +0.5, dragon egg +0.5 net drift per generation,
  while the price went from 8 ancient-debris ingots to 4 and then 12 nether stars to a dragon egg. The top of the ladder was the WORST value. (The starborn End line reaches quality 3-9 for free by bred Bloom, see "5. Bloom", so a quality-6 dragon egg was not even absurd.)
* **The new table: 0, 1, 2, 4, 7, 12, 20.** Steps up: 1, 1, 2, 3, 5, 8 (each the sum of the two before). Net drift of a +1/-1 line per generation: 0, 0.75, 1.5, 3, 4.5, 7, 11, so each tier ADDS 0.75, 0.75, 1.5, 1.5, 2.5, 4 (never less than the tier before; the
  self-check enforces that rule and shows that the old table fails it). Netherite is where every cost disappears (q = 4: the no-downside milestone, for 32 ancient debris), nether star is x4.5 gain, beacon x7, dragon egg x11.
  In nether stars (the currency of the top): the nether star guava (4 stars) adds 3 quality = 0.75 per star, the beacon guava (8 more stars, plus obsidian and glass) adds 5 = 0.63 per star, the egg is one item (in vanilla Java Edition normally only one exists per world) and adds 8.
  Golden and diamond keep 1 and 2 so that Fortune (0.5 per level, III = +1.5) stays "about a golden-to-diamond guava".
* **Averaging is on the qualities.** One dragon egg guava + one plain guava = (20 + 0) / 2 = quality 10 (the old table said 3): a one-off dragon egg is now a real shortcut for one generation, which is what a one-of-a-kind item should be. Two beacon guavas = 12; a nether star + a diamond = 4.5.
* **Saves are untouched:** a bunnidog still remembers the tier's LEVEL (`LoveTier`, 0..6), so an old save in love on a tier-4 guava now simply reads as quality 7. Seeds store a quality number, so seeds already in the world keep the quality they were made with.
* **What this changes in the stat maths:** nothing in `Drift`; only which `q` the guavas feed it. The simulation table in "5. Bloom" is labelled by quality (the rows "guava tier 2 / 4" mean q = 2 and q = 4: a diamond or a netherite guava on BOTH parents now, not a nether star one).
* **Open knob:** 20 for the egg is a judgement call (a +1/-1 line gains 11 Vitality points per generation, i.e. +44% of base health each generation at +4% per point). If it is too much, change one number in `GuavaTier`; the self-check rule (non-shrinking net gain, and both absurd tiers above a Bloom-1000 starborn bonus of 9.5) still has room down to about 17 for the egg and 10 for the beacon.

## Rejected / parked skeletons (for reference)
Uncanny Meter, Splicer-as-whole-mod, Echo, Ears-as-antennae, Nursery Sim, Confidence Ranch, Litter Roulette,
The Shelter, Growth Stages, Mood Flip, Pocket Pet, Overpopulation, Tamagotchi — user preferred Generations
(with elements of Overpopulation via the Fertility gene).

## Phase 5a decisions (session 7, made without asking)
* **Calipers = read-only, chat output.** Three targets: a bunnidog (right-click), a seed (in the other hand), a planted crop (right-click). No screen/GUI: the chat read-out needs no new API and is easy to extend; a Genetics Table block can show the same `genetics/Readout` lines later.
* **They do not feed or ride:** with the Calipers in hand `Bunnidog.mobInteract` answers before vanilla's feeding code, so measuring never wastes a guava.
* **Cheap recipe:** 4 iron ingots + 1 copper ingot, no mod items, so they are available before the first warren.
* **The tooltip of the seed stays** (short summary always, parents with F3+H); the Calipers add the gene-by-gene lines, as the old design note said.

## Phase 5c decisions (session 7, made without asking)
* **A notebook, not a registry:** the book logs the animals YOU register (right-click), it does not spy on the world. Entries are identified by genome + recorded parents (the animal has no id); identical twins collapse into one entry, which is fine for a family list.
* **Parents are links, not copies:** only the parents' genomes are stored per animal (one generation, see 5b); the book connects them when they are logged too, which is what builds the tree and the generation numbers.
* **Recipe: vanilla book + plain guava** (cheap, available once you have a guava). **Naming a lineage** from the original design note is postponed until a text-input route is verified.

## Command Block Guava decisions (session 7, made without asking)
* **Quality 33, level 7:** the next step of the 1, 1, 2, 3, 5, 8 sequence is 13 (20 + 13). Keeps the session 6 rule (every tier adds at least the net drift of the one below: 6.5 vs 4) and keeps `average()` meaningful. Levels are saved, so it is appended, never inserted.
* **Recipe: dragon egg guava + command block, shapeless.** The command block cannot be obtained in survival: that is the joke, and the tier is effectively a creative / `/give` item. If you want it reachable, change one recipe ingredient (the check names `minecraft:command_block`).
* **Orange-brown with a green glow** (the command block's colours); the existing tiers' textures are untouched.

## Phase 5d decisions (session 8, made without asking)
* **A code, not a name you type.** There is no text-input API verified (the same reason the Pedigree Book cannot name lineages), so the Ear Tag gives a generated registration code, `BD-` + 6 hex digits. Vanilla name tags and anvils already cover free-text names; the tag's own job is a stable breeder ID that shows up in the tools.
* **Derived from the UUID, not a counter.** No world-wide number to store, the same animal always gets the same code, and 24 bits make a collision among a few hundred animals practically impossible (300 random UUIDs: 0 collisions in the check). A kennel could still have two animals with the same code after about 4,000 tagged animals (birthday bound); not a concern for a farm.
* **One tag per animal, permanent, consumed.** A second tag on the same animal is refused and not used up; nothing removes a tag (the Calipers would otherwise lie about history). Creative does not use tags up.
* **The code is also the name** (so you see it floating over the animal when you look at it) but only if the animal has no custom name yet: a name given with a vanilla name tag is never overwritten. Renaming it later does not remove the tag, the Calipers still show the code.
* **Pups are untagged**, even from tagged parents (a tag marks one animal, not a line). Recorded parents (5b) are still genomes, not tags.
* **Cheap recipe:** iron nugget + paper -> 2 tags (a utility item, not a progression gate).
* **Left for later:** the Pedigree Book showing tags (needs `Pedigree.Entry` and its text form to grow a tag field: its own chunk), a coloured/dyed tag, visual tag on the model's ear.

## Phase 5e decisions (session 8, made without asking; the UUID part is the user's suggestion)
* **UUID = identity.** First version identified tagged animals by tag; the user asked "can't you use UUID". A UUID identifies EVERY animal (tagged or not), is saved with the entity and needs no item, so the book no longer guesses about identical twins. The tag (24 bits derived from the same UUID) is only a readable label.
* **Genes-only entries stay valid.** Books from 5c have no UUIDs. They keep their old identity (genome + parents) until the animal is logged again, which takes the old entry over in place (same `#n`, so parent links elsewhere in the book stay valid). The one blur: two different animals that were one genes-only entry become one UUID entry.
* **Parent links stay genome-based.** A recorded parent is a genome (5b), so with identical animals a child points at the first one in the book. Exact links would need the seed to carry the parents' UUIDs (taken from the two animals in `spawnChildFromBreeding`): seed text form, `SeedItem`, crop block entity and `Bunnidog.hatch` all touch it, so it is a separate chunk.
* **Text form is backwards compatible:** `genome[#uuid][@tag]<mother+father`; the whole book stays one string in the item component (24 entries are under 4,000 characters).
* **Left for later:** exact parent links (above), a per-animal note or name in the book.
* **Session 8 fix: surface warrens no longer end up in caves.** Bug: the one feature searched 40 blocks down from the given position for a floor. On the surface that position is the first air block above the terrain; when the top was water (swamp ponds, lakes) or otherwise not standable, the search kept going through the ground and stopped at the first cave pocket below, so a warren of a SURFACE lineage (meadow, swamp...) was built in the dark. Fix: a second feature type `bunnidogs:surface_warren` (same class, `surfaceOnly` flag, `SURFACE_SEARCH_DEPTH = 1` = the start position only; below it nothing can be standable anyway). `warren_surface` and `warren_end` use it; `warren_underground` and `warren_nether` keep the 40-deep search of `bunnidogs:warren` on purpose (their position is a random height). `/bunnidogs warren` still uses the deep search so it works inside caves. Cave-LINEAGE warrens (lush and dripstone caves) are unchanged and intended; to remove them, delete `neoforge/biome_modifier/add_warren_underground.json` (and the self-check lines for kind `underground`). Warrens already in existing worlds stay; the fix only affects new chunks.

## Phase 6 decisions (session 8): settlements, development level = number of guava trees (Claude's choices, change freely)
The user's rule (session 8): wild bunnidogs spawn in STRUCTURES of different development, from a lone guava tree with a few bunnidogs up to something like a village designed for the bunnidogs to live in. **Development is directly correlated with the number of guava trees in the structure**, so the level IS the tree count. Done in small chunks:
* **6a (this chunk, no buildings yet):** `layout/Settlement` rolls a level 1-5 (weights 55/25/12/6/2 %, so a higher level is always rarer) and lays out that many clusters. A cluster is the old warren (tree + coarse-dirt patch + 3-4 burrows + 2-4 bunnidogs, `WarrenShape`), so a settlement has 2-4 bunnidogs PER tree: level 5 has 10-20. Clusters stand on a plus-shaped pattern, 8 blocks apart (centre, east, west, south, north), turned by a random quarter turn and jittered by 1 block, so crowns, patches and burrows never touch. The rate of settlements per chunk is unchanged (`warren_*` placed features); only the size of what appears changed, so the average number of trees per settlement went from 1 to about 1.7.
* **The real level is the number of trees BUILT:** the centre cluster decides (no room = no settlement, as before); every other cluster is skipped quietly when the ground at its spot is not flat or the trunk has no room. A settlement rolled as level 5 on rough ground can therefore end up with 3 trees; that is the level it really has.
* **Size limit:** a worldgen feature may only touch the 3x3 chunks around the chunk it runs in (at least 16 blocks from its start position), so a settlement stays within `MAX_EXTENT = 12` blocks of its centre (self-check asserts it for every random settlement). That is about 25x25 blocks, enough for the 5 trees and, in later chunks, small buildings around them. **If the top levels need to be bigger than that, they must become a real `Structure` (pieces, spread over several chunks), not a Feature.** That decision is for 6c.
* **Same rules in every biome kind:** surface, cave, nether and end warrens all use the same settlement code (the lineage comes from the biome as before). Cave settlements get the same layout; whether a 25x25 flat area exists in a cave is luck, so cave settlements will often be below their rolled level.
* **Debug:** `/bunnidogs warren [level]` builds a settlement at your feet; with a level 1-5 it is forced, without one it is rolled. The reply says how many trees were really built.
* **Planned next chunks (the user decides the order; each stays small):**
  * **6b burrow houses (written in session 8, see below):** 2 per tree beyond the first. Paths were cut from 6b to keep it small.
  * **6c paths and the village level:** paths (coarse dirt / dirt path) between the trees and the houses from level 3; for level 5 the village pieces: a well, a market (guava crates, a barrel), a big meeting tree, small farm plots with the seed crop, lanterns; decide here Feature or Structure (see the size limit).
  * **6d life in the settlement:** bunnidogs that stay home (a home position goal), guards for the village level, settlement loot (a chest with seeds or guavas, quality rising with level).
* **Left out on purpose in 6a:** any building, path or decoration; any change to the rates; guava tiers above the plain guava in settlements (the higher levels would be the natural place for golden guavas, undecided); respawning of settlements.

### Phase 6b decisions (session 8): burrow houses
* **Rule:** houses = 2 per tree beyond the first (0, 2, 4, 6, 8 for 1-5 trees), counted on the trees REALLY built, so the house count stays tied to the tree count (the user's rule). Level 1 stays what it was: a tree and a few bunnidogs.
* **Look:** a 3x3 mound of coarse dirt, 2 high, grass-block roof with a hill on top, a doorway 2 high (so the player can walk in) facing the nearest tree, a 1x1 hollow inside. Made only from blocks that already compile in the project, so the chunk adds no Minecraft name; a later look pass can swap in mud bricks, moss or hay.
* **Placement:** at least 6 blocks from every trunk and 4 from each other, nearest to a tree first, inside the 12-block limit (all asserted in the self-check). 8 candidate spots are rolled per settlement and the first ones that fit (flat 3x3, free space) are built, so rough ground gives fewer houses, never a half-built one.
* **Left out:** paths (6c), anything inside the houses, bunnidogs that go home (6d), any new block.

### Phase 6c decisions (session 10): footpaths (Claude's choices, change freely)
* **Rule:** footpaths from **3 trees** (really built) on, as planned in "Phase 6 decisions"; a lone tree or a pair stays a warren. This is the first half of 6c; the village level (well, market, meeting tree, farm plots, lanterns) is the second half.
* **Network:** a spoke from the centre tree to each other tree (the longer axis first, so the small jog of a jittered anchor lands at the far tree), then a side path from every built house to the nearest block of the network, leaving the door straight. Computed from what was REALLY built, so paths follow the real level and never lead to a missing house. Paths are one block wide.
* **Free of path:** house footprints (the roof is grass and would turn into path), burrow holes, the trunk and one block around it (the path ends at the edge of the tree).
* **Look:** vanilla `DIRT_PATH` laid on the existing grass / dirt / coarse dirt, so no new block, texture or lang. A path crossing the coarse-dirt patch of a tree turns those blocks into path as well (it reads as the way into the yard). Hills/forests: the path follows the same terrain window as the rest of the settlement and simply has gaps where the ground is not grass or dirt.
* **Nether / End:** no paths (netherrack and end stone are not path ground). A later look pass could use another block for them.
* **Left out:** the village level; wider streets; steps or slabs on slopes; lights; anything that makes a bunnidog use the paths (6d).

### Phase 6e decisions (session 11): landmarks, so the levels are not just more dirt huts (Claude's choices, change freely)
* **Rule (the user's request):** the village level, and the levels between, must look designed, not like more of the same hut. Each level from 2 trees on gets ONE new kind of thing (haystack, well, market stall, then farm plot + lamp plaza), still tied to the tree count.
* **Vanilla blocks only** (hay, barrels, cobble, mossy cobble, water, oak fence/slab, lantern, pumpkin, melon, farmland, wheat/carrots/potatoes), so there is no new texture or lang. Carrots appear in the farm as decoration only. Crops are placed as sprouts and mature by themselves.
* **Space:** landmarks are placed before the houses so level 5 always has room for all five; the houses move around them. Landmarks stand as near the centre as the 5-block clearance from the trunks allows (the well and the plaza read as the heart of the village).
* **Left out:** the house look per level, a meeting tree, landmarks in caves/Nether, life around them.

### Phase 6f decisions (session 11): styles by biome (Claude's choices, change freely)
* **Rule (the user's):** a settlement must be built of things that make sense where it stands. Style = the lineage of the biome at the settlement centre, so it reuses the biome tags that already exist (other mods' biomes join a style by joining a tag).
* **Same shapes, other blocks:** this chunk only swaps materials; shapes per biome are a later chunk. Vanilla blocks only, no textures. Nether and End use their own bricks; the End farm is mushrooms on mycelium because wheat does not grow there.
* **Paths everywhere:** non-meadow styles pave any solid ground, which also gives cave and Nether settlements paths.
* **Left out:** tree and patch unchanged; badlands use the desert style.

### Phase 6g decisions (session 11): house forms by biome (Claude's choices, change freely)
* **Rule:** the same earth mound everywhere read as one template with other blocks, so the dry and hard biomes get a different SHAPE: a flat-roofed tower with windows and a battlement (adobe in the desert, stone on mountains, nether bricks, end stone bricks). The soft biomes (meadow, tundra, swamp, caves) keep the mound. The footprint stays 3x3 on purpose, so the layout rules do not change.
* **Left out:** larger houses for the high levels (would need a new layout step), lighting inside.

## Ideas (not started; the user decides when)
* **A proper GUI for the Pedigree Book and the Calipers** (user, session 8: "not just printed text in chat"). Today both print to chat (`genetics/Readout`, `genetics/Pedigree` produce the text lines). Plan when it is time: keep those pure-Java classes as the data source and add a screen (a client-side `Screen` opened from the item, with the data sent from the server) showing a bunnidog's genes, stats and family as a layout instead of lines of chat; the Pedigree Book as a page-turning list of animals with the family tree. New APIs to verify first in the NeoForge sources: opening a screen from an item use, client/server data transfer (a payload or a menu), `Screen` rendering in 26.2. Split into small chunks: Calipers first (read-only, one animal), then the Pedigree Book.

## Session 9 decisions: warrens on uneven ground (Claude's choices, change freely)
* **Why:** the user reported that warrens do not work well in biomes that are not flat, forests included. Reading `WarrenFeature` showed the ground of every column around the centre was searched only 2 up / 3 down from the centre's floor and a house needed a perfectly level 3x3, so on slopes things were silently skipped. Not reproduced in game (Claude cannot run it), so the fix is aimed at that cause and the log line per settlement is there to show if something else is wrong.
* **Surface kind follows the terrain:** window 6 up / 8 down; the topmost block of the column decides (so water, lava, or rock without room above give "no ground"; this keeps the session 8 rule that a surface warren never ends up in a cave). **Cave kind unchanged** (2 up / 3 down): a wider window in a cave finds the next chamber.
* **Houses on slopes:** up to 3 blocks of difference over the 3x3; the low columns are FILLED with dirt up to the highest (cutting a hill would remove stone and ores and leave holes). The two blocks in front of the door must be free, or the house is skipped (the candidate list has spares). Cost: a dirt skirt on the downhill side; steps/terraces are polish for a later look pass.
* **Dogs:** a cluster whose bunnidog spots have no ground takes the missing ones, down to the warren minimum of 2, from its patch (never the trunk column, a burrow or a spot already used).
* **Not changed:** rarity, biomes, placed features, step. Possible next suspect if forests still look wrong: vanilla trees are generated AFTER `surface_structures`, so a trunk can grow on a spot where a bunnidog was put. Moving the step later would instead make the settlement meet the trees (many more skipped sites), so the better fix would be to spawn the dogs after generation (a pending-spawn marker), which is a chunk of its own.
* **Debug:** `/bunnidogs warren [level] surface` = the natural kind at your feet; the settlement message now also counts the bunnidogs. One INFO log line per settlement (built or given up, with the reason) in `run/logs/latest.log`.


### Phase 6d decisions: settlements are now a real Structure (surface + End)
* `WarrenStructure` (type `bunnidogs:warren_village`) + `WarrenPiece` (registered in `ModStructures`). Pieces: one per tree cluster, one per house, one for the footpaths. Each stores only (kind, index, layoutSeed, level, centre, y); the `Settlement` layout is re-derived from the seed, so every instance differs.
* Anchor-chunk rule: a piece builds everything in the single `postProcess` call of the chunk that holds its anchor column (path: column by column), so nothing is built or spawned twice and writes stay inside the 3x3 chunk region.
* Data: `worldgen/structure/{warren_village,warren_end_village}.json` (biomes = `#bunnidogs:warren_surface` / `#bunnidogs:lineage_end`), matching `structure_set` files (random_spread 8/3 and 3/1). The surface/End placed features and biome modifiers were removed. Cave and nether warrens stay Features. `/bunnidogs warren` still uses the Feature code; `/locate structure bunnidogs:warren_village` works.
* Differences from the Feature: no spare house spots when one fails; the tree count is the planned one (a cluster on water/steep ground is just missing); the 12-block limit no longer applies (a later chunk can add a well, market, farm plots).
* NOT COMPILED. Likeliest problems: `getIntOr`/`getLongOr` on CompoundTag, `StructurePiece(type, CompoundTag)`, `StructurePieceType.ContextlessType`, `Structure.GenerationStub(BlockPos, Consumer)`, `getBaseHeight` signature, `BoundingBox.minX()`.

## Phase 6h decisions (session 12, made without asking)
* **Only the centre tree of a level-5 village is grand.** The user asked for a village level that is more interesting; a bigger meeting tree was already on the list. One tree is a landmark; making every tree big would crowd the 12-block extent.
* **Size:** trunk 6-7 logs (the ordinary tree: 3-4), crown radius 3 as a rounded disc (a square-minus-corners would look like a box), a third, narrower layer to taper it, 3-6 fruits. The crown starts 3 logs below the top of the trunk, so the fruits hang 2-3 blocks above the ground: pickable without a ladder.
* **Spacing needed no change:** houses are at least 6 from a trunk and landmarks at least 5, the new crown reaches 3 (the self-check enforces the margin). The crown is high, so paths below it are unaffected.
* **Same dice for the ordinary tree:** `TreeShape.generate(rng)` is unchanged byte for byte. Level 5 settlements consume the dice differently (the grand tree draws different numbers), so a village in an already generated old world may not match a regenerated piece; new worlds are fine.

## Phase 7a decisions (session 12, made without asking)
* **Own blocks, not vanilla jungle wood with a fix:** the user wanted proper guava wood anyway; a custom log also lets us put it in `minecraft:logs`, which is what makes vanilla leaf decay work with no custom decay code.
* **Leaves are vanilla behaviour, pre-coloured:** no biome tint (a guava tree should be the same green in a desert warren and a swamp one), so the model has no tint and the texture carries the colour. Particle chance 0 (no falling leaves yet).
* **Decay needs the right `distance` at placement** because worldgen places blocks without neighbour updates: computed in pure Java (`TreeShape.leafDistances`) so it is tested here, not guessed in the world code.
* **Drops:** shears give the block; otherwise a sapling 5% and a stick 20% (vanilla oak is about 5% / 2 sticks at 2%..): the sapling is the renewable way to a tree, so it is not rare. No Fortune/silk touch (their predicate format is not verified).
* **Only log and leaves this chunk;** planks and the rest of the wood set are a separate, bigger chunk.

## Drops D7b decisions (session 18: made without asking, all small)
* **The dust ripens only an unripe crop and is only used then.** A ripe crop (or any other block) passes, so the dust is never wasted by a mis-click; in creative nothing is used up.
* **It jumps to the last age, nothing else.** The same state change bone meal makes (`setValue(AGE, MAX_AGE)`), the seed in the block entity untouched; so Fortune, the litter and the harvest behave exactly as for a naturally ripened crop. No particles or sound (no new Minecraft names for a small chunk).
* **No recipe, no special tooltip beyond the use.** Dust comes only from starborn animals; the row's numbers (1-2, 25% x Bloom factor) are the spec's and unchanged. With it, `Drops.NOT_YET` is empty: every row of the spec is live.

## Drops D7a decisions (session 17: made without asking, all small)
* **D7 is split in two.** The spec's D7 was three things (Tissue Sample + Calipers reading + tooltip, and Starlit Dust on the crop); the Starlit Dust is its own chunk, D7b.
* **The Calipers line shows the plain 4%.** `Drops.describe` uses the plain adult context, so every animal's line ends with `tissue sample 4%`; the guaranteed off-hand roll is explained by the tooltip line (decision 17), not by a second number. The sample is a single roll, so it prints as a percentage like a signature, and it is the LAST entry (layer order).
* **A sample is read like a seed:** the Calipers in one hand, the sample in the other, right-click the air. It prints genome gene by gene, the stats and the drift; there are no parents (a sample is the animal itself).
* **The off hand is read with `getItemInHand(InteractionHand.OFF_HAND)`**, not the spec's `getOffhandItem()`: the first is already compiled in `SeedItem.use`, so D7a adds no Minecraft name.
* **The engine stays pure:** `Drops.roll` only says "one tissue sample"; the glue builds the stack from the dying animal's genome and stats (it is the only drop that carries data).
* No recipe, no tab beyond one plain sample in Ingredients, UNCOMMON rarity, default stack size.

## Drops D6b decisions (session 16: made without asking, all small; the big picks are DROPS_SPECS section 14, accepted)
* **Looting applies to the marrow.** The spec says "flat 25%" (2.4) and also that layers 2 and 3 use the chance formula of 1.5; the two are consistent only if the flat 25% is the base and Looting still multiplies it: `min(0.95, 0.25 x (1 + 0.10 x looting))`, so Looting III is 32.5% per roll (about 1.95 marrow from a full mutant). One number to change if it feels too generous (`Drops.marrowChance`).
* **The marrow is the LAST entry of the Calipers line** (layer order: pelt, stat lines, signatures, marrow) and is shown as an average count (`mutant marrow 0.25 (25% for one)`, `mutant marrow 1.5` for a full mutant), as spec decision 26 says: there are several independent rolls, so one percentage would mislead. A pup, a mob kill and an animal with no rare allele list nothing.
* **"Rare" means EXPRESSED and rare** (`Allele.isRare()`: huge ears, spotted, springy, chonker, stoic, swarm). A rare allele is dominant, so one carried copy shows and counts; wild alleles (snow, starborn, ...) never count, and a hidden recessive one shows nothing, like every other layer of the drops.
* **No cap on the marrow count** besides the six genes themselves (at most 6 rolls). The cap field of the stat lines does not apply.
* **RARE item rarity, stack 64, Ingredients tab right before the Mutagen,** so the two sit together. No tooltip class: the marrow is a plain item like the D4 materials (a use-hint tooltip would need its own `Item` subclass; the Mutagen's own tooltip already explains the gesture).
* **The recipe is shapeless, one of each** (marrow + fermented spider eye + glass bottle), file `mutagen.json`. The Mutagen is still in the creative tab and `/give` for testing and for creative players.
* **The art reuses the Mutagen's green-to-violet** (a snapped bone with a glowing core), so "marrow -> Mutagen" reads at a glance.
* **The handler needed no change:** it resolves any item id from the registry, so the new id works as soon as the item is registered.

## Drops D6a decisions (session 15, third turn: the big picks are in DROPS_SPECS section 14 and were accepted; these are the small ones)
* **The treated line in the tooltip sits right after the mutation line** (`Mutation chance 50.0% per pup`, then `Treated with Mutagen`), so the explanation is next to the number.
* **A refused second Mutagen answers with a grey chat message** and returns SUCCESS (the arm swings), so the click does not fall through to the juice/planting code; nothing is used up.
* **The floor equals the cap, so a treated seed is always exactly 50%:** natural chance never exceeds 50% (`MAX_MUTATION_CHANCE`), so `max(natural, 0.5)` is 0.5 for every pair of parents. The `max` is kept so that the rule stays true if the cap is ever raised.
* **The gesture is not a crafting recipe:** the result carries data (like the infusion), so it lives in `SeedItem.use`.
* **The Mutagen's tooltip is its own tiny class** (`MutagenItem`), the same way the plain juice carries its hint, instead of a data-driven tooltip.

## Drops D5 decisions (session 15: made without asking, all small)
* **Names:** "Prime Bunnidog Haunch" and "Roast Bunnidog Haunch" (the same "Bunnidog" prefix as the flesh and the pelts; the tools keep the short label `prime haunch`).
* **Food values are the spec's, read the way D1 read them:** nutrition 6 / 10 and `saturationModifier` 0.4 / 1.0 (the spec's "saturation" column). Vanilla multiplies the modifier by the nutrition and by 2, so the roast saturates a lot (the best food of the mod, as specified); if it feels too strong in play it is one number in `ModItems`.
* **Cooking recipes:** the flesh's three (furnace 200, smoker 100, campfire 600 ticks) with 0.5 experience instead of 0.35, a small reward for the rarer cut.
* **No rarity, stack 64, Food & Drinks tab after the flesh:** the spec lists no rarity; the item is recognisable by its picture (a bigger ham with a twine tie).
* **Nothing else changed:** the line was already fully defined and tested in D1; D5 only adds the items and moves it into `Drops.LINES`, which is now every line of the spec. The handler is unchanged.

## Drops D4 decisions (session 14: made without asking, all small)
* **Names:** "Bunnidog Sinew", "Bunnidog Whisker", "Bunnidog Fang" (like "Raw Bunnidog Flesh" and the pelts: they cannot be mistaken for vanilla or other mods' items). The Calipers and the command keep the short labels `sinew`, `whisker`, `fang`.
* **Plain items, no rarity, stack 64:** the spec lists them as materials with no rarity (the marrow and the dust are the UNCOMMON/RARE ones). They sit in the creative **Ingredients** tab after the pelts.
* **E = 0 lines are not listed.** With sinew and whisker active, a plain animal would show `sinew 0.00 (0% for one), whisker 0.00 (0% for one)` in the Calipers. The spec's worked examples ("plain meadow animal: flesh 1.0, foot 0.10, bone meal 0.5, plus its pelt") list nothing for them, so `Drops.expected` and `simulate` skip a line whose expected count is 0 (`Drops.canDrop`). This also applies to a line bred against its stat with a floor of 0 (the foot at very negative Swiftness). D1's text for a plain animal is unchanged.
* **Recipe shapes:** the Brush is vanilla's with the feather replaced by a whisker (whisker / copper ingot / stick, one column), the arrows are vanilla's with the flint replaced by a fang (fang / stick / feather, one column, 4 arrows), the string is shapeless (1 sinew -> 2 string). All three results are vanilla items, so the new materials are useful at once and cannot upset the mod's own balance; the Leash and the Genetics Table are the later, bigger uses.
* **Art:** three small hand-made pictures generated by `art/gen_materials.py` (sinew a pink twisted cord, whisker three white hairs from a dark knot, fang a curved ivory tooth). A drawing that does not please is a one-character edit in the character maps.
* **The fang row is live, the dust row is not:** `Drops.NOT_YET` is now only `bunnidogs:starlit_dust` (D7).

## Drops D3 decisions (session 13, sixth turn: made without asking, all small)
* **22 of the 24 signature rows are active.** The fang row (D4) and the Starlit Dust row (D7) are defined in `Drops.ALL_SIGNATURES` but wait in `Drops.NOT_YET`: an item ships with its use. Shipping the item removes its id from that set.
* **The spec contradicted itself about common alleles:** it said they have no row, yet its table has `temperament:skittish` and `temperament:feral`, both COMMON alleles. The rows are kept: temperament has no numeric innate multiplier, so a common temperament has no other effect on drops. The self-check pins exactly these two as the only rows of common alleles, so a third cannot slip in unnoticed. (Effect on play: any skittish animal can give feathers, any feral one bone and later a fang; both are cheap.)
* **Repeated items are separate stacks:** bone (lean and feral), slime ball (moss and springy) and flint (slate and stoic) can come from two rows of one animal; each row rolls on its own and drops its own stack, and `simulate` adds them up.
* **Display:** a signature prints as a chance only (`gold nugget 13%`, the spec's form), after the stat lines, in table order (coat rows first). The count range is not printed; the average count is in `simulate`.
* **Nothing but the engine changed:** the handler adds whatever `Drops.roll` returns, so D3 needed no glue, item, texture, lang line or recipe.

## Drops D2 decisions (session 13, fifth turn: made without asking, all small)
* **Pelt names:** "<Coat> Bunnidog Pelt" (so they cannot be mistaken for another mod's pelts); ids `<coat>_pelt` as the spec says. They sit in the creative **Ingredients** tab (next to the sample seed), not the Food tab.
* **The Calipers and the command show the hide FIRST** (`pelt: tan, flesh 1.0, ...`), in the order the layers are evaluated; the spec's example put it in the middle but only as an illustration. The hide prints no numbers because it is certain (a Looting level only matters for a real kill, and the Calipers assume none).
* **Looting and the pelt:** the spec says "15% chance per level of one extra"; implemented as `min(1, 0.15 * level)` so a (modded) Looting level 7 or more makes the second pelt certain and nothing ever gives a third.
* **One generator for everything per coat:** `art/gen_pelts.py` imports `COATS` from `art/gen_bunnidog.py` and writes the textures, client items, models AND the tag, so those cannot get out of step with the coat table; only the 12 lang lines are by hand (the self-check fails when one is missing). A new coat = the allele in `Gene.java` + a `COATS` entry + run the script + a lang line.
* **No new Minecraft API at all in D2.**

## Drops D1 decisions (session 13, fourth turn: made without asking, all small)
* **D1 = layer 1 for three lines only.** The six stat lines are all DEFINED in `Drops.ALL_LINES` (so the spec's yield table is tested in full), but only flesh, rabbit's foot and bone meal are in `Drops.LINES` and can drop: an item ships with its use (spec 8), and sinew, whisker and the haunch have no item yet. Adding a line later is adding it to `LINES` together with its item.
* **The hide is NOT in D1.** The spec's chunk table says "layers 0 and 1" for D1 and "the hide layer" for D2; the pelts are the hide, so layer 0 comes with them in D2.
* **Readout text:** a mean of 1 or more prints one decimal (`flesh 1.0`), a smaller one prints two decimals and the chance of at least one (`bone meal 0.50 (50% for one)`). The spec's example showed `bone meal 0.5` without a percentage; the percentage is more useful and costs nothing.
* **`calipersOffHand` is a field of `Drops.Context` (as the spec says) but the handler passes `false` until D7**, when the Tissue Sample exists; that keeps D1 free of the `getOffhandItem` name.
* **The yield function is `Drops.yieldOf`:** `yield` is a restricted identifier in Java (found by the parse check).
* **Both meats are plain `Item`s with `FoodProperties`** (no custom class, no rarity), like the guava's food but without the bunnidog tag: bunnidogs do not eat each other's flesh; the food tag is unchanged.
* **Cooking recipe form** (plain-string ingredient, `category: food`, `experience: 0.35`): an assumption, see API_NOTES; one line to change if the game rejects it.

## Drops plan (session 13: SPECIFIED, nothing coded)
Full plan: `docs/DROPS_PLAN.md`; exact numbers and all decisions: `docs/DROPS_SPECS.md` (it wins where they differ: haunch is an ordinary stat line, pelts drop on any adult death, Looting multiplies E, marrow is adults only, legends sit behind a Trophy Pedestal and are decided at D8). In short (user's request: bunnidogs drop items whose odds depend on their genome and stats: a fast one drops rabbit's foot, several if very fast;
more Vitality, more bunnidog flesh; many modded items welcome):
* **Loot is a readout of the animal:** genes decide WHAT can drop, bred stats decide HOW MUCH and HOW OFTEN: `count = yield(stat points) x innate gene multiplier`, with the same
  linear-then-square-root shape as the attribute curves (a plain animal is a vanilla rabbit; nothing is unbounded in practice).
* **Four layers:** (1) one stat line per bred stat (Vitality -> Bunnidog Flesh, Swiftness -> Rabbit's Foot, Leap -> Sinew, Senses -> Whisker, Bloom -> Bone Meal);
  (2) gene signatures (coat = the lineage's signature drop; wild alleles; rare mutation alleles -> Mutant Marrow, the Mutagen's ingredient); (3) one "legend" per stat at 100+ points;
  (4) context (pups give a quarter, burning means cooked flesh, rare layers need a player kill, Looting).
* **No new stored data:** everything is derived from the Genome, the Stats and the circumstances of the death. Pure Java (`genetics/Drops.java`), glue = one `LivingDropsEvent` handler.
* **Closes the roadmap:** Sinew -> Leash, Whisker -> Genetics Table, Marrow -> Mutagen, Tissue Sample -> Gene Splicer. Every drop must have a use before it is added.
* **Balance:** rare loot needs stat points, stat points need seed quality, quality needs the guava ladder.

## Phase 8 S1 decisions (session 20, made without asking; change freely)
* **Words:** the plain word of an allele is its name; jargon (chonker, swarm, starborn, springy, fennec, bat, hoofed, woolly, lean, fleet, glow, the four temperaments) is explained by a note per allele in `Plain`, followed by the numbers it brings (computed from the tables, never typed) and, for rare and wild alleles, where it comes from. The self-check fails if an allele has no note.
* **Speed, not Swiftness,** is what players read (`Plain.statName`); the saved enum is unchanged.
* **Units:** hearts = health / 2, speed in blocks/s = attribute x 43.17 (ASSUMED, calibrate), jump height by simulating vanilla vertical motion, hearing = the follow-range in blocks, Bloom in minutes. More than 10 hearts: one icon and a multiple. Leap uses 2 decimals.

## Phase 8 S2 decisions (session 21, made without asking; change freely)
* **Save format v2 of a book entry:** `genome#uuid@tag$3,-1,0,2,12!<mother+father`. The snapshot is the five stat points; a `!` right after them means "was a pup". Only the canonical five numbers are accepted. Books from before read unchanged (no stats, no pup flag). Parents stay genomes only.
* **The animal's facts are a snapshot,** refreshed only by logging it again (UPDATED), like the ear tag.
* **Chat readouts:** words first (look phrase, gene rows, stats with real units), the raw code as the last line (`Code: ...`) or in `[brackets]` at the end of a family-list line.

## Phase 8 S3 decisions (session 22, made without asking; change freely)
* **The window is drawn, not textured.** The book (parchment, spine, frame), the portrait box and the hearts are plain `fill` rectangles; hearts come from a 7 x 6 bitmap in `SheetLayout.HEART`. This kept S3 free of art and of sprite/pipeline names. A real book texture is a later polish step.
* **One layout for the whole book,** pure Java: `BookContents.spread(book, n, metrics)` returns the two pages of spread n, each a list of text / hearts / portrait with pixel positions. The screen draws that list and nothing else, so the self-check's "everything stays inside its 130 x 156 px page, nothing overlaps" is a statement about the real screen (up to the font's true widths).
* **Contents index without ear tags** (`#n  look`): the look is the useful part and a tag would clip it. Parents are `Mom` / `Dad` on the sheet. Both are open to change once the player sees the real font.
* **A snapshot window:** it shows the book as it was when opened. Nothing can change the book while the window is open (you cannot right-click an animal with a screen up), so no refresh is needed.
* **Open questions for the S3 look:** is "one spread per animal" what "1 page per bunnidog" meant? Is the right page too dense (the first thing to move to a hover box in S5 is the `NEXT LITTER` block)?

## Phase 8 S4 decisions (session 23, made without asking; change freely)
* **The portrait is the real animal,** not a picture of it: a throwaway client-side Bunnidog with the logged genome (and pup size for a logged pup), so every gene that changes the model (ear length, leg length, build, coat) shows on the sheet for free, and a future gene needs no portrait work.
* **It sways, it does not follow the mouse,** and it is not animated otherwise (no tail wag). A turning animal reads as "a specimen on display", which suits a pedigree book; the mouse is kept free for the hover boxes of S5.
* **The coat swatch is only the fallback.** Behind a live animal the box stays the neutral parchment mat (a tan animal on a tan swatch would vanish).
* **Open question for the user's look:** size and position of the animal in its box (two constants, `PORTRAIT_SIZE` and `PORTRAIT_OFFSET_Y`) and the sway amount (`SWAY_RANGE`), judged by eye in game.

## Phase 8 S5 decisions (session 24, made without asking; change freely)
* **Hover regions are data, not screen code:** the layout lists every hover rectangle with its text and the code pair to light, so the self-check proves that every gene row, stat and parent has a box and that no two overlap; the screen only finds the rectangle under the mouse.
* **The page keeps clipping long lines; the box shows them whole** (NEXT LITTER, parents), which keeps the 17-line pages from growing.
* **Open question for the user's look:** are the boxes too wide / too long (gene rows can take 4-6 lines at 150 px)? Is the gold pair glow clear enough?

## Phase 8 S6 decisions (session 27, made without asking; change freely)
* **The window replaces the chat readout for a bunnidog.** The sheet carries everything the chat lines carried (words, raw code, stats in real units, drift, drops on hover, parents), and two outputs for one click is noise. A non-player (not a `ServerPlayer`) still gets the chat. The seed, planted crop and Tissue Sample keep the chat until S8 gives them windows.
* **One screen, two modes.** The Calipers window IS the book's screen with a single entry, so every hover box, the portrait and the pair glow are identical and any later change to the sheet reaches both. The cost is a `single` field and a few guards in `PedigreeScreen`.
* **The wire format is the entry's own text.** One UTF-8 string, about 150 characters, read with `Pedigree.parseEntry` (tested). A new field on the entry later reaches the Calipers window without a new packet format.
* **Left out rather than shown wrong:** `#n`, `generation n` and parent numbers do not exist for an animal that is in no book; the header is the ear tag (or `Bunnidog`).
* **Open question for the user:** is a window on every Calipers click too heavy for quick checks while breeding? If so, the cheap alternative is the chat readout back on a sneak-click (needs a verified name for sneaking) or a second Calipers mode.

## Phase 8 S7b decisions (session 29, made without asking; change freely)
* **Silent at the ends.** Next on the last spread, Previous on the first and a link to the spread you are already on do nothing audible: a page-turn sound for no page turned would be the one thing that sounds broken.
* **Vanilla's own sound, vanilla's own way.** Same sound and plain volume/pitch as the vanilla book; no custom sound file, no `sounds.json` (a custom one would need a registered `SoundEvent` and an asset, for no gain).
* **The button click stays.** Previous / Next are vanilla buttons, so they also click; removing that would mean a custom button class.

## Drops D8a decisions (session 30, made without asking; change freely)
* **D8 is five chunks, the block first.** The pedestal, its memory of one item, the drawing of that item, the legends and the worn tag each get their own message and compile, because the drawing is the least certain API of the whole drops plan and should not hold up the rest.
* **Stone bricks and gold.** A pedestal that looks like a small stone plinth with a gold-framed top plate; the recipe is the same picture (one gold ingot over a column of stone bricks over a row of them). Cheap, so a collection of trophies is possible.
* **A box outline, not the exact shape.** One `Shapes.box` (1 to 15 sixteenths, full height), the same call the guava blocks use, instead of an exact union of three boxes (a new name for a few pixels of outline).

## Drops D8b decisions (session 31, made without asking; change freely)
* **Any item, one at a time.** The pedestal shows ONE item of any kind (the spec's pelt, Tissue Sample, legend and worn tag are examples, not a whitelist); a click takes one from your stack.
* **One click to take it back.** No swapping and no empty-hand rule: whatever you hold, a click on an occupied pedestal returns its item. Fewer cases, and it cannot get stuck behind a full hand.
* **Temporary chat lines.** Until D8c draws the item, a line in the chat says what went on or came off; D8c removes them.
* **No automation.** No hoppers, comparators or redstone: a trophy stand, not a storage block.

## Drops D8c decisions (session 32, made without asking; change freely)
* **The item hovers and turns slowly.** A still item on a plinth looks stuck to it; a slow spin (8 s per turn) reads as a trophy. No bobbing, no glow, no name tag.
* **Ground pose.** Every item is drawn the way it looks when dropped, so pelts, tools and blocks all read as "an item" with no per-item tuning.
* **Real clock.** The spin uses the wall clock, like the portrait in the book: no tick or partial-tick bookkeeping in the render state.

## Drops D8d decisions (session 33, made without asking; change freely)
1. **The legends are plain items:** `Item::new` with `Rarity.EPIC` (purple name), no tooltip, no enchantment glint, no use of their own. Their only use is the Trophy Pedestal, as spec section 8 says (an item ships with its use); a glint or a tooltip line would each need a new Minecraft name, so they are left for later.
2. **Names, from the spec (section 4):** Titan Haunch (Vitality), Gilded Foot (Swiftness), Moon Sinew (Leap), Listening Shell (Senses), Everbloom (Bloom). The tools say them in lower case (`titan haunch 8%`), like every other drop label.
3. **The roll reads the bred POINTS of the stat** (`Stats.get`), not the displayed unit, and not the gene: the legend is the prize for breeding, so a lucky genome with no bred points never drops one.
4. **One independent roll per stat:** a line bred to 100+ in several stats can drop two or more legends from one kill (the tests check that it happens). Nothing is shared or capped across stats.
5. **No Looting, adult and player kill only, no pups** (spec 2.6 and decision 4), the same `playerKill` flag as the marrow. A pup, a mob kill or the `mobDrops` rule off give nothing.
6. **The tools list a legend only when its chance is above 0** (a stat at 100+), after the Tissue Sample (the order of the layers), as a single percentage like a signature. A plain animal's Calipers line is therefore unchanged and still ends with the sample.
7. **Creative tab:** the Ingredients tab, right after the Starlit Dust and in stat order (the Food tab would be odd for a shell). No recipe, no loot-table entry, no spawn.
8. **Art:** one 16x16 picture each, drawn as fills with an automatic outline (`art/gen_legends.py`): a glazed ham with a bone, a gold paw, a silver-blue crescent of twisted cord with a spark, a spiral conch with a pink mouth, a white and pink five-petal flower with a gold heart. All colours are in one palette per item at the top of the script.
9. **Reachability:** 100 points is an endgame line (stats are unbounded; plain guava bleeds Vitality for about 15 generations, tier 2 and up shortcut that). For testing, `/summon bunnidogs:bunnidog ~ ~ ~ {Stats:"400,0,0,0,0"}` makes one at once. If the legends turn out too rare or too common, the whole curve is five constants at the top of `Drops.java` (`LEGEND_ONSET`, `LEGEND_BASE`, `LEGEND_PER_POINT`, `LEGEND_CAP`).

## Drops D8e decisions (session 34, made without asking; change freely)
1. **It always drops, and only for a tagged animal.** The spec calls it a keepsake, not loot, so there is no dice, no Looting, no age rule and no "player kill" rule: if the animal wore an Ear Tag when it died (pup or adult, any cause), its tag is left behind. The Ear Tag costs an iron nugget and paper, so the keepsake is cheap on purpose and gives nothing but the display; it cannot be farmed for value.
2. **Not part of `genetics/Drops`.** Everything in the engine is a roll of the genome and stats; this is a fact about one animal ("it has a tag"). It is one statement in the glue, like the Tissue Sample's payload, and the Calipers `Drops` line does not mention it (the Calipers already show the tag code).
3. **Its own item, not an `EarTagItem`.** The worn tag keeps the code but cannot be clipped on an animal, so it never re-tags anything, never makes two animals share a code and never confuses the "one tag per animal" rule.
4. **The code is a string component** (`bunnidogs:worn_tag_code`), saved and synced like `tissue_data`, and read back through `EarTag.parse`, so a hand-written bad code just shows as `Blank and scuffed`.
5. **Looks:** the fresh tag aged: dull ochre plate, half-rubbed number, a scratch, a chipped corner, a dark bent stud with fur caught in it. UNCOMMON rarity (a grey-yellow name), stacks like any item (different codes never stack, because the component differs).
6. **Creative:** one sample (`BD-3F7A2C`) in the Tools tab, right after the Ear Tag, so the pedestal can be tried without breeding and killing.
7. **Left out on purpose:** reading the worn tag with the Calipers or the Pedigree Book (the code is already in the tooltip), a recipe, any use besides display.

## Genetics Table decisions (session 36, made without asking; change freely)
The plan, the four chunks and the T1 decisions are in `docs/TABLE_SPECS.md` (it wins over this note). In one line: the table is a bench that keeps Tissue Samples and (T3) forecasts the litter of two of them, with no container menu; T1 holds and reads one sample, sneak with empty hands takes it back, and the whiskers (Senses) are in its recipe as the drops plan promised. T2 (session 37): two slots, A and B, used like a stack (A first, a third sample swaps with B, taking back empties B first), and reading opens a window with one spread per sample (A's sheet, then B's); T3 (session 38) adds the forecast as the last spread when both are on: Mendel odds per gene (before mutation), litter range, mutation chance, seed quality on plain guavas and the expected pup points, all computed with the seed's own rules (`genetics/Cross`).

## Home H2 decisions (session 42, made without asking; change freely)
* A dog looks for a nest by timer, not by a goal; the first look comes within 100 to 140 ticks of spawning or losing a home, so a placed nest is taken in about 5 to 7 seconds.
* Only reachable nests are claimed; an unreachable one is skipped for a while (up to 8 remembered).
* A broken nest is noticed within 10 seconds (the check runs every 200 ticks); a dead dog frees its nest at once.
* One dog per nest (`NEST_TICKETS` = 1, unchanged). Two dogs sharing a nest is the one-number change in `Home`.
* The home is a position plus a go-back rule (more than 16 blocks away: walk home to within 8), not a hard limit on strolling: a dog can wander a little past 16 before it turns around.

## Home H3 decisions (session 43, made without asking; change freely)
* Every burrow hole of a warren gets one Nest right beside it, on the side toward the tree. Wild dogs claim them by themselves a few seconds after the warren appears.
* One dog per nest (unchanged), so a warren of 2-4 dogs with 3-4 nests has a spare one or two; the spare ones are there for a dog bred or lured in later.
* The nests of the burrow houses in villages wait for the houses to change (V1), as planned: until then houses have none.
* Nothing is written into the dogs at spawn: a wild dog's home is decided by the same rules as a player's nest (reachable, free, within 24 blocks).

## Home H4 decisions (session 44, made without asking; change freely)
* Night is tick 12500 to 22999 of the day (a little after sunset to just before sunrise). Rain does not count, as in the spec.
* An adult with a nest walks home and stays within 2 blocks; it does not "sleep" (no pose in the model). Dogs without a nest and pups behave as before.
* The Nether and the End have no clock, so nothing changes there.
* A scare, a fight, mating or a guava still wins over going home; the dog goes back afterwards.

## Village wood V1a decisions (session 44, made without asking; change freely)
* The plan's table (FIXES_PLAN 5.2) is followed for levels 2 and 3: level 2 houses keep their look and gain a guava-log door frame; level 3 and up become cottages (lineage wall below, planks above, log corners, slab roof). Levels 4 and 5 use the cottage until V1b adds their extras.
* Desert, mountain, Nether and End towers keep their form and only gain guava log posts (all four corners at level 3+): their roof is left alone for now.
* A house is always the same 3x3 footprint, so spacing, paths and the dogs' nests are unaffected.
* Doorways stay open: there is no guava door until W3.
* Lintels and posts are upright logs (a horizontal lintel needs an axis state; later polish).

## Village wood V1b decisions (session 44, made without asking; change freely)
* The posts and roofs of the well, the stall and the lamp plaza are guava fence and slab in EVERY lineage. This replaces the per-biome fences (spruce, acacia, mangrove, wall, nether brick...) the villages had; the stone rings and paving keep their biome styles. If you miss a biome fence, the roles are two lines in `WarrenPalette.get`.
* Level 4 adds a stone foundation (the lineage's first ring block) to cottages and towers; level 5 adds a loft to the mound cottage only (towers are already three high).
* The loft is one room with the ground floor (the hollow is open up through it), with three glass windows. No stairs or ladder: it is for looks, and a dog cannot use it.
* Lanterns at the door are left out (a lantern on the doorstep would block the way, and H3b will want the hollow for a Nest).

## Nests in houses H3b decisions (session 44, fourth turn, made without asking; change freely)
* The Nest replaces the FLOOR block under the hollow centre instead of standing on it, so the dog keeps the full height of the hollow (2 in a mound, 3 in a tower or loft) and the roof does not squash it.
* One nest per house, in every form and every tier (even the plain level 2 house), so a village always has more nests than its entrances alone.
* No furnishing around it (no bed, no hay): the Nest block is the whole look for now.

## Warren redesign decisions (session 45, third turn; the user said "good" to the plan, so Claude's picks stand; change freely)
* **Why:** the user took the warrens for a worldgen bug (2-block-deep holes, no context) and did not know the word. The cure is in the world (a mound with a doorway, a lit slope, dens that the dogs use), plus one data-only advancement; not more text.
* **Vocabulary fixed:** warren = dens + mouths + tunnels; settlement = warren + houses, paths, landmarks. The development level (= number of trees) and the odds (55/25/12/6/2) do not change.
* **Mouths replace the holes** (D1), **nests in the dens** (D2), **one lantern per room and per ~8 tunnel blocks** (D3), **an advancement for the surface structure** (D4), **cave/Nether/End later** (D5), **den 5x5x3 at dy -5, slope 1x2, hall 7x7x4** (D6), **a second floor only on request** (D7).
* **No ladders, no vertical shafts for dogs**: every way down is a slope of 1-block steps.
* **Old worlds keep their holes**; only new chunks change (same policy as V1).

## U1 decisions (session 45, fourth turn, made without asking; change freely)
* The door of a mouth faces away from the tree and is 7 blocks from the trunk, because the slope drops 5 blocks at one step per block and must end under the den's edge; ground higher than the trunk moves the door out to 8 or 9, ground lower makes the slope run level into the den.
* Slopes are 3 high (a 1-block step needs jump room under the ceiling); dens are 5x5x3 as planned.
* The old holes remain as the fallback when the ground does not allow a den, and for everything that is not a level 1 surface warren, until U2 and U6b.
* No trail from the door to the tree yet; the dogs spawn around the tree as before and claim the nests in the den.
