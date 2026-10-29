# Progression, acquisition time and encounter scale

**Acquisition correction:** the first version over-weighted crafting bills and
omitted important raw-material routes. Read [Raw-material acquisition](RAW_MATERIAL_ACQUISITION.md)
first. It documents open ruins paying 12–24 Netherite Ingots, actual Blastmaw
ore recovery, finite NPC exchange, domestic yields and direct archive seeds.
The count-only conclusion that guava is overpriced is withdrawn pending
stage/source comparison. This document remains the recipe/effect/encounter
appendix; its item totals are not material difficulty or measured player time.

This audit responds to the player's night/death/scale review. It replaces the
assumption that a bigger numeric level, a rarer-looking mineral, or a smaller
number of enemies is enough to price or balance a feature. The player's field
experience—several Apex types at once, consuming endgame supplies rapidly—is the
baseline. A build passing does not establish good Survival pacing.

The reproducible [quantity appendix](audits/progression-quantities.md) expands
**32** actual equipment/infrastructure bills and **23** finite physical chest
tables. Its [JSON](audits/progression-data.json) also records actual native effect
healing and matched-line enemy attributes. Regenerate with
`python3 tools/progression_audit.py <passing scale_audit/full GameTest log>`.
Recipes share their intermediate requirements before rounding batch sizes;
reversible storage unpacking is excluded as an acquisition route. Existing
Netherite gear is counted as already owned, not mined a second time.

## 1. The path a player actually takes

| Stage | What the player must do | What consumes time or makes failure costly |
| --- | --- | --- |
| First animals | Find guava/animals, collect fruit, establish homes, discover useful lines | Search and travel; visible food supply, not a genetics UI |
| A useful household | Feed/breed, train suitable individuals, connect real stock, grow food/wood, build shelter | Personal practice, genetically limited learners, walking jobs, real materials, pup maturation |
| Sustainable society | Renew food/wood/tools, transport supplies, breed/nurture offspring and teach an offspring to work independently | This is an actual dependency chain; a full chest or a timer alone cannot qualify |
| First Apex | Earn/install a Living Keystone, pay incense/sample/focus, fight | Four final graft operations alone cost 32 guavas, eight bone meal and four frames, after the household evidence; the Keystone is reusable |
| Six hunts / realm | Defeat all five ground roles plus a flyer; craft the Eye using physical marked remnants and a membrane | Role discovery, mixed equipment and multiple fights; party resources can be shared |
| First lunar tool | Bait actual Blastmaw self-explosions, collect minerals, refine an ingot/template and upgrade a Netherite pickaxe | Initial lunar extraction is hazardous; TNT, ordinary Netherite mining and player fireballs cannot substitute |
| Astralite expedition set | Repeated moon trips, bulk ore refining and role-material hunts | Hundreds of minerals, not a few lucky ore blocks; keep travel/resupply and first-tool cost separate |
| Heartbound / weapons | Spend specialist drops, enchanted apples and further materials | This is preparation for the dungeon, not a reward locked behind it |
| Dungeon journey | Find a rare Crown site, follow bridges/islands or fly, survive the realm en route | Dungeon candidates are spaced in 8,192-block regions; this is not a short walk between farm rooms |
| Dungeon | Finite patrols, three two-wave encounters, keys and then the Crowned fight | Four/five Astralite-equipped opponents per wave, mixed ranged/healing/displacement and room bounds; nine wave enemies per wing |
| Post-Crown | Choose Core upgrades, repeat paid fights, use bulk shipments for projects and companions | One personal Core per eligible victory; four Sovereign armour pieces alone require four victories unless others share their Cores |

The authored Lantern trader culture is a separate source of paid production,
travel and rescue. It must not be mistaken for the player having built an
independent learned household, or for an infinite free stock source.

## 2. Time: clocks, throughput and costs must stay separate

These are code-derived loaded-world times, not observed hours-to-completion:

- Breeding rests **6,000 ticks divided by fertility**, with a 1,200-tick minimum.
  Average fertility is five minutes; prolific 1.5 is about 3m20s; swarm 2 is
  2m30s. Many pairs operate in parallel, subject to real crowding/space/food.
- Pup baseline growth is **24,000 ticks / growth multiplier** (20 minutes at
  multiplier one), with the existing one-minute lower bound. Bloom, actual
  feeding and paid Nursery bonuses matter. One cannot price a generation as
  just its breeding cooldown.
- An unassisted seed crop takes seven successful stages, each averaging three
  random-tick visits. At randomTickSpeed 3, a block receives a visit every
  4096/3 game ticks on average: about **23.9 minutes** to maturity while ticking.
  Bone meal adds 2–5 stages per success; Cradles and real workers change that
  route. This is an expectation, not a countdown or guarantee.
- Guava branches use two stages. Isolated fruit averages **11.4 minutes** for
  one; rooted pairs **9.1 minutes**; mulched triples **4.55 minutes**. Mulch
  triples per-branch throughput, because it improves growth and yield. Trees
  have several branches; orchards and workers scale production in parallel.
- The Juicer runs **100 ticks per pressing or infusion**, five seconds each.
  A stack of 64 pressing operations is 5m20s in one station; pressing and then
  infusing 64 is 10m40s of station work. Additional stations parallelize it.
- One Astralite ingot requires **200 serial furnace seconds**: 36 raw Cinderite
  at five seconds in a blast furnace plus its 20-second bloom. Four armour
  platings require **3,920 furnace seconds** (65m20s) in aggregate. That is
  machine occupancy; eight furnaces can divide much of it. Fuel remains extra.
- An 8,192-block route would take about **24m20s of uninterrupted level
  sprinting** at 5.612 blocks/s. This illustrates scale, not actual dungeon
  distance: islands, route detours, fights and elevation add time; Elytra and
  known Lantern connections change it substantially. No nearest-dungeon travel
  time has been measured in natural Survival.

Harvesting a seed for animals and consuming that same seed for juice are
exclusive choices. An apparently cheap infusion also costs the future litter
and the time to replace it. Transport, crowding, rejected practice and death
cannot be represented by multiplying item rarity labels.

## 3. Effects are different mathematical systems

Native Minecraft 26.2 Regeneration heals one HP at interval `50 >> amplifier`,
with an every-tick fallback. Levels I–VI therefore give approximately
**0.4, 0.8, 1.67, 3.33, 6.67 and 20 HP/s**. VII does not mean another linear
increment. Very high amplifiers also inherit Java's shift wrapping; native
representation is not an argument that arbitrarily high numbers are useful.

Strength adds **3 melee damage per level**. Speed adds 20% of base movement per
level. Absorption adds four temporary HP per level; it does not repeatedly heal
those hearts. Resistance removes 20% of applicable damage per level; III leaves
40%, while V provides temporary immunity to applicable damage. Armour-bypassing
attacks and damage-type exceptions must be assessed individually. Level numbers
cannot be compared across those systems.

Before this review, quality 20 gave Strength VI (+18 damage) **and Regeneration
VI (20 HP/s)**, up to a long duration. The endgame kit's quality 116 recovery
bottles had the same every-tick healing despite their much larger numeral.
That hid a major power breakpoint in an otherwise linear quality rule.

Implemented correction: Regeneration uses amplifier
`floor(log2(1 + quality/4))`. Its level thresholds are quality **0, 4, 12, 28,
60, 124, 252...**. Strength and the other existing curves are unchanged;
Regeneration receives no additional level cap. At quality 20 it is now III;
at quality 116 it is V. Duration/stat rules, saved infusion payloads, native
healing hooks and Resistance's separate limit remain. The native appendix tests
actual healing ticks rather than merely asserting that the formula returned a
number. Existing bottles derive the corrected effects from their saved payload.

This correction is not a claim that healing versus real multi-Apex burst damage
is now fully play-balanced. Drain, heart debt, shield disable, explosions,
interruptions and the 1.6-second drinking commitment all matter. The Standard's
power-five Resistance V lasts only four seconds after a 1.5-second use and a
five-second shared cooldown; it spends five real Spools. It is a deliberate short
rescue window, not equivalent to long-duration Resistance III.

## 4. Mineral value is a production chain, not a universal currency

| Upgrade from already owned Netherite | Shards | Raw Cinderite | Moonstone | Extra Netherite ingots |
| --- | ---: | ---: | ---: | ---: |
| One mining foothold pickaxe | 36 | 36 | 9 | 1 |
| Full Astralite armour | 576 | 720 | 144 | 4 |

The armour also costs 24 Lenses, 16 Marrow, 12 Membranes and 16 Diamonds. The
foothold adds three Lenses, one Marrow, two Membranes and four Diamonds. These
are separate bills: do not silently include the pickaxe in the four-armour total.
One armour plating is four ingots plus extra Cinderite and a membrane, not one
Astralite Shard, and not one ordinary Astralite Ingot.

At 2.4% shard ore and 3.2% Cinderite ore, the unmodified 576/720 mineral demand
corresponds to roughly **24,000 / 22,500 moon rock cells sampled**, before
Fortune, luck, exposure and selection. Fortune III's average 2.2 ore-drop
multiplier reduces the illustrative expectations to about 10,909 / 10,227.
These are density estimates, not minimum excavations or minutes. Blastmaw
bootstrap has no Fortune tool, but its self-explosions now pay full normal lunar
loot; dangerous collection/void loss still matter. Both minerals must be mined:
the smith now refines Raw Cinderite instead of selling metal for wood alone.
Mainland densities of 0.16%/0.24% make the equivalent search roughly 15x/13.3x
larger. That is why actual repeated moon extraction matters.

The vanilla mining route to a Netherite Ingot uses four Ancient Debris-derived
scraps and four Gold Ingots. After realm entry, ordinary ruins instead supply
12–24 ingots in one treasure chest; the mining route is not the sole valuation. It is not a
unit to equate to one shard because both are near the end of a crafting ladder.
An XP Bottle averages seven XP; a player-attributed ordinary Apex pays **500 XP**
(about 71.4 bottles of experience). Bottles are portable reserve repair/levels;
kills supply risky immediate XP. Neither buys back Ancient Debris or moon ore.
Mending converts useful collected XP into equipment repair at two durability per
XP, shared with other eligible equipment. Actual wear, full durability and XP
collection determine how much is used. Ingots used for native repair have a
large opportunity cost compared with that already abundant combat XP.

## 5. Outdoors and the dungeon have different pressures

The realm has a shared native monster cap, not an isolated boss slot per player.
Biome entries already produce **2–3 ground Apexes per selected group**; most
flyers were singletons. Multiple groups, role combinations and replenishment
make the fields a continuous encounter. Broodcallers can add five elites every
nineteen seconds within their finite nearby limit; those minions are additional
actors, not new Apex loot/XP factories. Lifebinders and crossfire materially
change how dangerous the same headcount is. Damage immunity frames also mean
one cannot just sum five nominal attack DPS values.

Matched-line native data (ordinary genome / 40 physical points) makes the armour
comparison concrete:

| Ground Brute | HP | Attack attribute | Armour | Toughness |
| --- | ---: | ---: | ---: | ---: |
| Bare field line | 288 | 19.968 | 4 | 0 |
| Same line with Astralite | 576 | 31.949 | 18 | 5 |

Thus four dungeon fighters are not four ordinary field mobs. They start with
roughly twice the health and 60% more outgoing damage, plus substantial native
armour/toughness. Under modest melee damage, their effective health rises by
more than 2x; a block-piercing weapon changes that comparison. Special damage
uses its own coefficients. Companion gear and player Heartbound hearts also
matter: a full Heartbound set adds twelve max HP; Sovereign adds twenty-four.
Astralite alone keeps Netherite armour points and improves toughness/durability;
it does not by itself grant all artifact benefits.

Dungeon waves are finite, room-bound and deliberately complementary. Each wing
requires nine equipped kills over two waves; the full three wings require 27,
before optional two-actor patrols, the boss and its phase escorts. Outdoors can
keep replenishing across a journey. Treat finite clearing/recovery opportunities
as part of the dungeon reward, rather than deciding that its fewer actors imply
it needs another arbitrary health multiplier.

Implemented night correction: Apexes no longer inherit the prey-animal spawn
check that rejects dark stone. Native nighttime aerial selection weights are
quadrupled and flyer groups can contain two. A fresh natural night Apex has a
35% armour chance: half of equipped results Iron, 35% Diamond, 15% Netherite.
No natural Astralite. Native caps, ordinary despawning, block-light requirements,
wards, town shelter and biome/moon selection remain. No second spawn loop,
forced chunks, daytime stat inflation or upgrades to already living enemies.

## 6. Rewards must pay for the actual trip and danger

The entire current authored chest site, not each barrel, contains:

- **288–448 Shards**, **576–896 raw-equivalent Cinderite**, **512–896 Moonstone**.
- **384–768 Fibre**, **192–256 Dust**, **96–128 Sinew**, **16–32 Whiskers**.
- **32–64 golden apples**, **4–8 enchanted apples**, **4–6 Totems**, **8–16
  Diamonds**, **1–3 Netherite Ingots**, **80–112 XP Bottles** and modest travel
  supplies, plus four Dressings and sixteen Bedding.

These are finite containers and earned wing/vault access, not regenerated loot.
Opened stock never refills. Mineral rewards can fund later companion suits,
repairs and projects; they cannot be counted toward the set required before a
first dungeon. Decorations divide the stock rather than multiplying it.

The previous exclusion of dungeon Apex role drops/XP was wrong: an expensive
equipped fight was being paid less than an ordinary field kill. Dungeon Apexes
now use ordinary wild role drops: 4–8 matching parts, 2–4 Marrow, 2–4 Membranes
for flyers, and the existing small Crest chance, with normal native equipment
and 500 XP. The Crowned retains its own material bounty, 10,000 XP and personal
Core journal. Equipment is not converted back into freely reclaimable player
plating; a body suit is useful companion equipment in its own right.

Confirmed deaths retain native death ticking. Cleanup skips corpses so the last
kill of a patrol or final resolution cannot erase its animation. Temporary brood
minions retain their ordinary no-part/no-XP rules, but a killed minion also keeps
its animation even if its caller dies during those ticks. Reset/abandonment and
phase retirement remain encounter cleanup, not fictitious rewarded kills.

A complete potential 27-wing + eight-patrol + six-boss-escort set would pay
20,500 ordinary XP plus the 10,000 boss XP, and substantial role parts. This is
an upper encounter inventory, not a promise all patrols/escorts are present or
that every death is player-attributed. The role materials are an earned second
income alongside the containers, not interchangeable with their XP bottles.

## 7. The remaining progression contradictions

These are identified design debts, not fixes falsely declared complete:

1. **Guava investment must be valued by stage/source.** A single Netherite Guava recursively
   costs eight Netherite Ingots, eight Diamonds, eight Gold Ingots and a fruit.
   A pair at that tier can consume sixteen Netherite Ingots for one breeding
   event. The earlier comparison to four extra armour-template ingots did not
   establish acquisition difficulty. One ordinary ruin pays 12–24 ingots; raw
   source, intended stage, supply replacement and archived-seed alternatives
   must be assessed before calling the guava recipe overpriced.
2. **Upper guava tiers are not a renewable ration economy.** Beacon/egg recipes
   consume their inputs; the vanilla Java Dragon Egg is not a repeatable normal
   farming output. However, ordinary ruin archives directly provide quality
   20–33 seeds and Crown archives provide 36–47. Those missed sources change
   the practical choice. Starborn breeding is another route, with its
   own real generations, costs and time. These routes must be priced separately.
3. **Kit quality is not proof of obtainable progression.** Kits create quality
   116 bottles with Bloom 60 and ordinary fertility, without the parents/costs
   explaining that quality. Even with egg-tier parents, Starborn's
   `0.3*sqrt(Bloom)` bonus would require Bloom **102,400** for quality 116.
   At Bloom 60 it contributes only about 2.32, and ordinary fertility contributes
   zero. Strength XXX is the user's desired endgame baseline, not a cap; the
   Survival potency/acquisition curve must reach that honestly instead of making
   a kit counterfeit its affordability. This batch preserves that requested kit
   baseline and exposes the contradiction rather than silently weakening it.
4. **Breeding statistics mostly lengthen drinks, not their potency.** Duration
   doubles at 50 relevant bred points and then caps. Amplifiers use quality.
   Captured seeds preserve their actor's genes/stats but have quality zero;
   their innate strength is not automatically a Strength XXX drink. The intended
   advanced-breeding payoff and capture economics need a deliberate shared rule.
5. **Projects need throughput comparisons.** A Pack Armoury alone expands to
   117 Moonstone, 72 raw Cinderite, 14 Guava Logs and two Marrow. Its bench job
   costs are additional. A full site supplies roughly 4–7 such lunar stone bills,
   but those same shipments also fund Binding/Bastion/Aerie/Nursery. Do not call
   a project cheap by looking only at its last recipe. Nor should every room
   supply another entire set of every project.
6. **Sustained ammo and rescue have different prices.** Sixty-four Prism Cells
   use 32 Amethyst Shards and eight Copper Ingots; 64 Volatile Charges use four
   Volatile Hearts, eight Fire Charges and 24 Gunpowder. Power five spends five
   at a time: only twelve full shots per stack plus four charges. A Standard
   spends five scarce Spools for a rescue window. A flask saves slots, not
   ingredients, seed production, drinking time or replacement time.
7. **Natural timings are still missing.** Neither tests nor a recipe census
   measures a person's first self-sustaining household, first successful lunar
   extraction, an eight-island night expedition, or a full solo dungeon with
   consumed supplies. Record equipment/effects, roles simultaneously engaged,
   actual minutes, deaths, supply loss and haul over those journeys before
   making another global enemy/loot multiplier claim.

Next design priority starts with the actual source/payout hierarchy documented
in RAW_MATERIAL_ACQUISITION.md, then the repeatable bred-seed/guava/infusion
route to the desired endgame potency. Recipe totals alone cannot price it.
Then compare whole-expedition consumption against finite room rewards and
shared-material projects. Keep mining and six-hunt gates, the user's combat
roles and permanent save/stock ownership intact while correcting that economy.
Do not call the accepted intelligence roadmap complete from this equipment audit.

## Validation and limits

Java 25 build/model/localization gates and all **270 native server scenarios**
pass. New real-dimension cases exercise night attributes, native spawn-list
weights, natural non-Astralite armour, normal wild ownership/despawn rules,
actual dungeon kills/drops, multi-tick corpse lifetime and native effect healing.
This is server evidence and a code/data audit. It is not a client death-animation
capture, a natural multi-hour Survival playthrough, or a measured hours-to-endgame
claim. Existing loot, active encounters, genomes and saved items are preserved;
this batch does not rewrite old terrain, refill containers or grant resources.
