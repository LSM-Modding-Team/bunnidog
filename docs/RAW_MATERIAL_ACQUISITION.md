# Raw-material acquisition: routes, difficulty and stage

This corrects the recipe-centred conclusions in `PROGRESSION_SCALE_AUDIT.md`.
An ingredient count is demand. It does not measure obtaining the ingredient.
The first audit omitted ordinary ruin loot, finite merchant exchange, direct
archive seeds and actual mineral recovery, then called some expenses
"disproportionate" without establishing their acquisition cost. That conclusion
was not justified by its bill comparison.

This follow-up changes the analysis and evidence, not gameplay values. The
previous Regeneration change remains implemented, but its native healing rate
alone is not evidence that it was overpowered in a full expedition. Incoming
bursts, armour-bypassing damage, Resistance, heart debt, duration and the ability
to finish drinking still need encounter evidence. No further balance change
should be justified merely by comparing effect numerals or recipe quantities.

[Native observations](audits/acquisition-observations.md) and
[their JSON/method limits](audits/acquisition-observations.json) accompany this
report. Reproduce with the `scale_acquisition` native scenario, then
`python3 tools/acquisition_audit.py <GameTest log>`. Sources are the installed
Minecraft 26.2 data/source, this mod's actual generation/drop/merchant code and
the actual loot tables—not assumed generic Minecraft prices.

## Compare routes available to this player

For each material ask: **Which stage? Which obtainable source? What setup? What
active work? What loaded waiting? What danger and supply loss? What recovered
yield? Can the source repeat?** Keep those dimensions explicit rather than
inventing one ore/XP/rarity exchange rate.

A source may be expensive to establish and cheap to use. A single exploration
trip may pay several materials together. A later source cannot bootstrap the gate
that gives access to it. Enough stock in a moon is different from safely exposing,
harvesting and carrying that stock home. These distinctions change the conclusions
below.

## 1. The omitted open-ruin acquisition route

A Great Warren **ordinary ruin** has three distinct chests: one `great_ruin`
treasure table, one `warren_supplies` table and one frozen archive. It has no
Crown seal, required key, encounter-wave ledger or compulsory boss kill. Reaching
and looting it still exposes the player to the realm's ordinary enemies.

One fitting, unlooted ruin supplies:

| Material / item | Actual amount | Acquisition consequence |
| --- | ---: | --- |
| Netherite Ingots | 12–24 | After realm entry, an exploration outing can replace a large Ancient Debris mining trip |
| Diamonds | 48–64 | The sixteen armour-template diamonds need not come from new Overworld mining |
| Gold Blocks | 16–32 | 144–288 ingots; gold is an incidental reward of the same outing |
| Emerald Blocks | 16–32 | 144–288 emeralds, usable in a prepared vanilla trading hall |
| Enchanted Golden Apples | 8–16 | A single treasure chest can cover the eight apples for a complete Heartbound set |
| Totems | 2–4 | A finite exploration supply, with no separate raid inside the ruin |
| Elytra | 1 | The mod provides a post-entry route in addition to vanilla End City exploration |
| Amethyst Shards | 128–192 | A direct ammunition shipment, independent of geode growth |
| Copper Blocks | 32–64 | 288–576 ingots; often a surplus relative to the same chest's amethyst |
| Gunpowder | 128–192 | Carries a meaningful launcher-ammunition input without a dedicated creeper farm |
| Fire Charges | 32–64 | Carries another launcher input alongside the powder |
| Ender Pearls / Crying Obsidian | 32–48 / 32–64 | Travel and ward materials arrive with the same supplies |
| Starlit Dust in archive | 16 | A post-entry alternative to waiting for a small domestic grooming herd |
| Frozen seed quality | 20–33 | High-quality infusion material is obtainable without consuming a Dragon Egg |
| Archive parent stats | 40–70 each | A breeding starting point that does not require building those points from zero |

These are finite contents, not regenerating cupboards. The structure set uses
20-chunk spacing and eight-chunk separation: a candidate in each 320-block-wide
region, **not a guaranteed ruin every 320 blocks**. It checks nine heights,
requires a surface at least Y43 and at most nine blocks of height variation,
and later preserves obstructed columns. In the small native geometry sample,
only **4 of 48 candidates** passed the height test across three seeds. That is
not a measured ruin density, complete-placement success rate or search time.
Finding a ruin remains seed/terrain/travel dependent.

However, this route is sufficient to overturn the assumption "Netherite always
means mining four new Ancient Debris per ingot." The current comparison is also
revealing: **an ordinary ruin gives 12–24 Netherite Ingots, while an entire Crown
site's generic chests give 1–3**. The ordinary ruin gives 8–16 enchanted apples
versus the Crown chests' 4–8. That is an acquisition/reward inversion to review
before changing guava prices from a crafting bill. It does not automatically
mean either table must be nerfed; it means the earlier valuation missed the
sources that players can choose between.

## 2. Netherite, XP and mundane supplies change value with progression

| Material | Before an established source | With existing infrastructure / realm access | Real remaining bottleneck |
| --- | --- | --- | --- |
| Netherite Ingot | Mine/expose four Ancient Debris, smelt and combine with four gold; or finite Bastion loot | Ordinary Great Warren ruins give 12–24 each | Finding/extracting new deposits or surviving another exploration outing; no conventional infinite Netherite farm |
| XP Bottle | Reach master Cleric and secure emerald income, or find suitable loot | Native Cleric offer is three emeralds for one bottle, stock twelve; prepared halls repeat restocks | Villager setup, emerald throughput, stock refresh, trade/pickup labour and loaded time |
| Direct XP | Mine, smelt, trade or fight | Mob/XP farms and regular realm Apex kills can supply it repeatedly | Collection and useful Mending distribution; it cannot refill a carried bottle stack automatically |
| Gold | Mining / Nether extraction | Zombie Piglin farms, and 16–32 blocks in each ordinary ruin | Setup, collection/storage and travel rather than intrinsic per-ingot rarity |
| Emeralds | Early selling and villager access | Repeatable crop/iron/trade infrastructure, plus ruin blocks | Throughput, demand, workstation/restock access and trade labour |
| Diamond | Search, mine, use Fortune, or explore | 48–64 per ordinary ruin | New exploration; gear obtained through villagers reduces some diamond demand but does not create raw diamonds |
| Totem | Find/defeat an Evoker or obtain suitable loot | Repeatable raid infrastructure; 2–4 per ordinary ruin | Farm setup/raid mechanics or exploration danger, then replenishment rate |
| Enchanted Golden Apple | Finite vanilla exploration loot | 8–16 per ordinary ruin | Finding fresh sites and surviving collection; not a conventional renewable crop |
| Amethyst | Find a geode, grow clusters; Fortune helps | Stationary geode harvesting, native material alternatives, ruin shipments | Growth/collection before infrastructure; amethyst shipment can become the ammo limiter beside surplus copper |
| Copper / iron | Mine and refine | Large deposits, respective mob-farm routes, ruin copper, trading infrastructure | Initial setup and throughput; an ingot count alone says little at endgame |
| Gunpowder / fire charges | Hunt, craft, barter or loot | Appropriate mob farms/Nether infrastructure and ruin shipments | Farm/ingredient throughput or a dangerous resupply outing |
| Nether Star / Beacon | First obtain a Wither fight and its skull/soul-sand inputs | Repeatable Wither-skeleton and Wither setups can change repeat cost; trap-based and open fights differ | Farm setup, skull yield and actual kill/replacement work; not a finite Egg-like item |
| Dragon Egg | First vanilla Java dragon defeat | No normal repeatable vanilla egg farm; archived mod seeds provide an alternate high-quality route | Finite trophy availability, rather than another assumed scalable consumable |
| Wood / Root Fibre | Find/fell trees; one guava log makes four fibre | Guava orchards, coppice/hauling and tree infrastructure | Growing, harvesting, hauling and stock handling; a renewable labour stream |
| Stone / Tuff / Clay | Find deposits, excavate and move bulk | Existing extraction routes and warehouses; some have renewable conversion routes | Bulk handling and collection speed, not the value of a named rare catalyst |

The installed vanilla Cleric has twelve bottle uses; normal villagers can
restock twice per Minecraft day when their actual work conditions permit it.
That gives a **steady stock ceiling of 24 bottles per 20-minute game day,
72/hour per Cleric**, assuming full work/restocks and every use purchased.
It is a capacity calculation, not a measured automated farm rate. First stocked
inventory, discounts/demand, trade rebalance settings, emerald supply and player
interaction change the realised result. Eight ready Clerics provide 576/hour of
potential stocked output, while they require up to 1,728 emeralds/hour at the
base price. Eight villagers with no emerald source do not deliver that rate.

The earlier "500 XP equals 71.4 bottles" statement compares **experience
content**, not acquisition value. Bottles are portable, pre-collected inventory
reserves. A kill pays immediate XP in a dangerous location; it does not produce
71 bottles. A prepared XP farm can make repair XP easy without making Ancient
Debris, captured seed quality or carried emergency supplies easy.

Vanilla debris and moon bootstrap also have different explosion economics:
Ancient Debris survives the mining explosions that expose it; the player then
mines it. The native moon exception deliberately lowers resistance of both
Moonstone and its ores, so the Blastmaw destroys ore and invokes explosion-decay
loot. "Both are blast mining" does not mean equal recovery or risk.

## 3. Lunar acquisition has a major before/after boundary

Moons are common relative to Crown dungeons: one planned centre per
1,024-block grid cell, jittered within it, radius 42–64, centred near Y244.
The generator uses a flattened sphere and rich ore distribution, rather than
scattering a tiny prize over each entire moon.

Three actual generator snapshots contained:

| Seed | Radius | Total rock | Astralite ore | Cinderite ore |
| --- | ---: | ---: | ---: | ---: |
| 1379 | 43 | 273,079 | 6,599 | 8,811 |
| 49157 | 53 | 511,499 | 12,265 | 16,295 |
| 926481503 | 43 | 273,079 | 6,458 | 8,819 |

A single moon therefore contains many armour sets' worth of ore. That is
**stock availability**, not proof that a player can obtain it quickly.

### Before the first Astralite pickaxe

- Netherite cannot obtain the lunar mined drops. Ordinary TNT/player fireballs
  do not get the Blastmaw self-explosion resistance exception.
- The player must reach a high moon and manoeuvre an actual dangerous Blastmaw.
  The native moon spawn table contains ground/flying Blastmaws; nights increase
  aerial selection and can equip them. This is a sustained hostile workplace.
- Normal power-ten bursts cost the actor 22.5% of its max health. Its final
  power-twenty detonation is not an unlimited repeatable button. Fuse, approach,
  attack recovery, terrain shape and the finite remaining moon all matter.
- The default `mob_explosion_drop_decay` rule is **true**. Destroyed ore is not
  recovered ore. Fire/blast resistance on the resulting item entity does not
  cancel the loot table's earlier explosion-decay roll.

Four native-calculator observations, on copied real generated substrate with
that default rule, illustrate the loss:

| Position | Power | Astralite ore destroyed | Shards spawned | Cinderite ore destroyed | Raw Cinderite spawned |
| --- | ---: | ---: | ---: | ---: | ---: |
| Surface | 10 | 15 | 2 | 17 | 1 |
| Surface | 20 | 42 | 2 | 50 | 3 |
| Interior | 10 | 15 | 1 | 21 | 2 |
| Interior | 20 | 65 | 5 | 89 | 4 |

These four random observations are not average yields or predictions of how
many Blastmaws a player needs. The controlled patches omit player danger,
other creatures, baiting, fuse time and item collection. They do show why
"2.4% ore" was not an adequate acquisition analysis. The first tool's 36 Shards require actual recovered drops, not merely
destroying 1,500 nominal rock cells. Its Cinderite can instead come from a
previously found Lantern smith: three fibre trades supply all 36 ingots. A
player choosing the moon-only route must also recover that Cinderite; the
crafting bill is not a requirement that both minerals must be mined.

### Once the mining foothold exists

The same moon is now a dense, visible local resource that an Astralite tool can
harvest directly, with Fortune and Mending available. The bottleneck changes to
safe access, exposing/reaching veins, actual breaking and collection speed,
combat interruptions, equipment wear and bringing the haul back. Explosions can
expose veins but still destroy/pay poorly for any ore caught in their blast.
Moonstone is plentiful bulk rock rather than another equally rare ore.

Moonstone has hardness four; both lunar ores have hardness six. Efficiency,
Haste, instant-mining thresholds and the native block-breaking delay affect
real throughput. A nominal ore percentage is not a blocks-per-minute benchmark,
and a player following exposed ore does not excavate every cell in a random
volume sample. The previous 10,909/10,227 "rock sampled" calculation was only a
density estimate, not a number of blocks the player must break.

Mineral drops resist fire/explosion damage but still have gravity: void loss,
death/retrieval and ordinary item lifetime can make recovery costly. Wards,
blast-proof shelter, flight and route knowledge can reduce some hazards once
the player has built the infrastructure. They do not remove already living
opponents. The first return trip and the tenth established mining session must
not be assigned the same difficulty.

## 4. Apex parts are ongoing combat income, with role bottlenecks

A qualifying ordinary Apex kill pays **4–8 role parts**, **2–4 Marrow** and
**500 XP**; a flyer also pays **2–4 Membranes**. Dungeon Apexes now pay the same
ordinary drops. The Crowned bounty/Core are separate sources.

Five eligible kills therefore pay **10–20 Marrow and 2,500 XP**, with 20–40
parts split across the actual killed roles. These outputs arrive together.
A player fighting several Apexes during an ordinary journey acquires parts as
incidental combat income; the first ritual kill and post-entry stockpiling are
very different acquisition problems. The fights still consume real supplies.

| Material | Actual source restriction | Consequence |
| --- | --- | --- |
| Marrow | Every ordinary Apex role | Often a shared incidental stream; allocating the entire trip to Marrow overstates its separate acquisition work |
| Fang | Basic Apex kills | Needs that role, not simply any five kills |
| Prism Lens | Prismmaw kills | Lenses can be the limiting role supply even while other parts accumulate |
| Chorus Heart | Broodcaller kills | Killing its temporary brood does not pay extra Hearts |
| Volatile Heart | Blastmaw kills | Rare mainland role; common native moon role, so mining and launcher resupply can overlap |
| Heart Spool | Lifebinder kills | Specific dangerous matchup, including hook/debt/armour-bypassing drain; a needed Spool is not a generic free mob drop |
| Aerial Membrane | Flying Apex kills | Can arrive with a needed role's material; not a separate mandatory outing in every bill |
| Ground-marked remnant | Ground version of each role | Eye gate cannot substitute the same unmarked flying ingredient |
| Sovereign Core | Actual eligible Crowned victory | Solo one per qualifying victory; a participating four-player party can earn four personal Cores and choose to pool them |

Do not sum independent material kill estimates when one killed flying Prismmaw
pays Lens, Marrow, Membrane and XP together. Do not assume a fixed kills/minute:
role mix, equipment, range, healing, crowd control, native damage immunity and
whether the player loots or flees all change it. Ordinary native monster
replenishment makes outdoor pressure repeat; a finite cleared room has a
recovery opportunity that a travelling player may not get.

## 5. Living harvest, domestic slaughter and NPC exchange are distinct

**Whiskers:** healthy adults at Senses +10 can give one through grooming every
ten minutes. Once the line exists, that is six per loaded hour per adult;
eight eligible animals have a theoretical 48/hour capacity. Actual brushing/
dispenser collection, tool wear, health and loaded time still apply. Slaughter
is an alternative with a different loss: in the native Starborn/default-other-
genes samples, Looting III adults at 20/50/100 stat points averaged about
2.32/4.70/6.21 Whiskers per death. Parent/offspring production and lost animals
must be counted; that is not a free repeatable harvest of one individual.

**Sinew:** has a similar stat-based Leap death yield; it does not have the same
living whisker brush route. The sampled 20/50/100-point adults averaged
2.32/4.70/6.21 with Looting III. Some finite barn loot can start a stock;
Crown shipments give another later route. A string farm is not a reverse Sinew
recipe just because Sinew can craft string.

**Starlit Dust:** the first Eye needs one before realm entry, so later ruins or
Lantern traders cannot solve that first gate. The confirmed domestic routes are
Starborn death drops and Starborn/Bloom +20 living grooming. The latter gives
one per ten-minute recovery, with the other eligible grooming products in the
same operation. After entry, each ordinary ruin archive gives sixteen, a Crown
archive gives sixty-four, and traders offer a finite exchange route. One must
not price every later dust item as another early search for a rare genotype.

**Capture:** the Gene Siphon can save actual ordinary animals/brood as exact
captured seeds for one real Prism Cell and durability per completed capture.
That can acquire already developed genes/stats rather than breeding them all
from zero, but gives no kill parts/XP and the captured seed has quality zero.
Hatching/raising that animal and consuming its seed as juice are different
routes. Capture does not magically grant a renewable high-quality infusion.

Native fresh Lantern offers and goods establish these alternatives:

- **32 Root Fibre → 12 Cinderite Ingots.** Eight Guava Logs can supply that fibre.
  Each fresh smith starts with 64 ingots and can initially fund five such
  trades (60 ingots). This is a real post-discovery alternative to ore extraction.
- **24 Cinderite → 16 Dust**, or **16 Sinew + 16 Fibre → 16 Dust** from a kitchen
  merchant. These spend different player surpluses, and share its actual dust.
- **16 Cinderite + 16 Dust → 16 Prism Cells.** Compare that convenience against
  direct amethyst/copper and the nearby supply-chest route; do not treat the
  NPC's asking price as the item's inherent raw value.
- **One native Totem + 48 Shards → one town-bound Homeward Totem.** The Shards
  compete with an early lunar-tool/armour supply; this is a safety/travel purchase.

Initial goods are finite and production consumes stock. Rebuilding the offer
list does not create a new 64-ingot deposit. Later Cinderite taken in payment
can finance further trades; smiths do not manufacture raw Cinderite from fibre.
Kitchen bedding work also consumes Dust. These NPCs are not vanilla villagers
with an inexhaustible emerald-to-everything refresh. Town finding, routes and
what other players have sold determine subsequent availability.

## 6. Established ammo and drink acquisition

The ordinary ruin's supply chest already contains enough Amethyst/Copper for
**256–384 Prism Cells**, with amethyst the limit at those native ranges. That
is four to six stacks from one successfully recovered supply chest, rather
than a presumed long geode/copper mining session for every refill.

The same chest's Gunpowder/Fire Charges support **256–512 Volatile Charges** if
the player supplies the needed **16–32 Volatile Hearts**. Those Hearts require
eligible Blastmaw kills; the vanilla ingredients are often the incidental
shipment. Power five spends five charges per shot, so those large counts are
51–102 complete maximum-power shots, not hundreds of them. Actual ammunition
consumption per expedition is still a missing player measurement.

For drinks, the expensive-looking guava ladder is not the only acquisition
route. An ordinary ruin provides a quality **20–33** seed and the Crown archive
provides **36–47**, with actual developed parent records. These are one-shot
frozen seeds: inspecting/reloading does not reroll them, and hatching does not
make descendants inherit the literal archive quality. Breeding computes a new
seed's quality from feeding and the actual parents' bonus. A player must choose
between drinking that particular seed and investing it into a line.

Starborn breeding, rich carriers, captured animals and archive exploration
therefore have different costs, yields and repeatability. The kit's quality
116 still lacks an explained acquisition route at its supplied stats, but it
was wrong to analyse all high-quality drinks as if their only source were
consuming high-tier guavas. The target remains the user's requested endgame
potency; any revised route needs to be evaluated against these existing choices.

## 7. What changes in the earlier conclusions

1. **Withdraw the count-only "Netherite Guava is overpriced" conclusion.**
   Before realm entry, sixteen Netherite Ingots per pair means a substantial
   new debris commitment if that is the player's chosen route. After entry,
   one ordinary treasure chest can cover most or all of it. Which stage the
   recipe should serve and whether players would choose it over archived
   seeds must be resolved first.
2. **Separate the first lunar tool from repeat extraction.** The former has a
   dangerous destructive-recovery gate; the latter has dense ore stock and
   efficient-tool options. Making each take the same supposed hours is wrong.
3. **Audit the reward inversion before increasing/nerfing individual prices.**
   The normal ruin currently pays much more vanilla treasure than a complete
   Crown site's chest ledger. The Crown instead pays exclusive Cores, equipped
   opponents' parts and mod minerals; compare the actual outing, not just table
   names or total item counts.
4. **Price joint outputs and infrastructure honestly.** Apex parts arrive with
   XP; lunar operations can pay Volatile Hearts and minerals; ruin treasure
   comes with supplies/archive access; a developed grooming herd yields
   multiple products. Assigning each its own full search/combat cost double-counts
   the same outing. Conversely, setup and ongoing replacement are not free.
5. **Replace guessed hours with a source-specific record.** A useful play record
   states stage, route, existing farms/traders/gear, active time, loaded waiting,
   actual recovered materials, consumables, deaths/void loss and whether the
   source was depleted. Native counts and the small controlled extraction
   sample do not establish those human rates.

Next work should reconcile the realm's actual source/payout hierarchy and the
intended repeatable drink route with those source-specific observations. The
recipe appendix remains useful bookkeeping, not an economic valuation model.
