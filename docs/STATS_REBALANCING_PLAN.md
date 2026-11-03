# Stats rebalancing: earned power across the whole journey

Status: **the implementation candidate is complete; human balance acceptance is
pending.** [INFUSION_SPECS.md](INFUSION_SPECS.md) records current formulas and
save policy. This revision expands the initial potency plan with recovery
duration, attainable specialist references, flask order, actual native defensive
semantics and the Prism/Sovereign comparison. Its evidence is in
[rebalance observations](audits/rebalance-observations.md), with exact native
records in the adjacent JSON. No natural Survival run is claimed.

The [scale audit](PROGRESSION_SCALE_AUDIT.md) retains earlier findings;
[raw-material acquisition](RAW_MATERIAL_ACQUISITION.md) supplies the actual
sources and limits. The latest user request ends after this finite batch is
validated and pushed. Do not turn the human-test queue into an endless numeric
retuning loop or restart unrelated roadmap work during delivery.

| Decision | Implemented candidate | Acceptance |
| --- | --- | --- |
| Earned infusion power | Rolled effect-specific stats, real plain-seed/golden-carrier kit donors, Strength XXX retained | Human founder selection, setup and refill journey pending |
| Recovery investment | Senses contributes half its value to potency; bat-ear/short-leg donors reach III/V at S24/S120 | Human progression and VI breakpoint pending |
| Recovery upkeep | Loyal duration 40–240 seconds before ordinary stat/quality rules; endgame reference 88 seconds | Human travel/drinking pressure pending |
| Flask scheduling | New kits alternate Strength/recovery, 16/16 late and 48/48 endgame | Human stock use and manual refill pending |
| Lance upgrade | Sovereign deals 56×mode; Prism retains better damage per cell, Sovereign better cycle damage/range | Human interruptions, line targets and ammo burn pending |
| Enemy/companion attributes, acquisition and rewards | Retain actual current values after source/mitigation review; no unsupported global multiplier or bill change | Human field/night/moon/wing/finale comparisons pending |

## 1. What the findings change

Balance the first household, first hunts, first lunar foothold, established
expeditions and repeat Crown clears separately. For each, record setup, active
work, loaded waiting, danger, supply loss, recovered yield and repeatability.
An established farm changes the price of its output; a later source cannot
bootstrap the gate needed to reach it. Count outputs from one trip together.

| Finding | Rebalancing consequence |
| --- | --- |
| Ordinary realm ruins supply 12–24 Netherite Ingots, 48–64 Diamonds and 8–16 enchanted apples, plus supplies/archive access | Do not price post-entry Netherite as fresh debris mining or nerf a guava solely from its ingot count. Finding and surviving a fresh site still costs something. |
| XP bottles require stocked Clerics, emerald income and restocks; Apexes instead pay immediate XP | Compare portable reserve acquisition separately from repairs made during a fight. The 500-XP kill is not 71 free bottled items. |
| Moons contain thousands of each ore, but require high-altitude access, extraction and collection | Ore stock is not a measured mining rate. First-tool baiting and later Fortune mining are different activities. |
| Ground/flying Blastmaw self-blasts now pay full normal lunar drops | Remove explosion-decay loss from every proposed mining budget. A sample breaking 46 Astralite/59 Cinderite ores paid 46 Shards/59 Raw Cinderite. These are controlled payouts, not player recovery rates. |
| Cinderite must be mined too; smiths require 12 Raw Cinderite + 4 Fibre for 12 ingots | No wood-for-metal bootstrap in the plan. Keep both mineral requirements; refining changes processing, not raw acquisition. Later earned dungeon shipments remain separate. |
| Ordinary Apex kills jointly pay 4–8 role parts, 2–4 Marrow and 500 XP; flyers also pay 2–4 Membranes | Do not assign a separate full hunt to every ingredient. Retain role scarcity: moon Blastmaws, land Prismmaws and dangerous Lifebinders are different sources. |
| Dust has early genotype/harvest gates, then finite ruin/trader shipments; living grooming has a shared recovery | Do not value every later Dust as another rare-founder search. Herd size, loaded time, collection and stock matter. |
| Open archives provide seed quality 20–33; Crown archives 36–47 | Include direct drinks and investment into parents. A finite powerful seed and an established renewable supply are different rewards. Archive parent stats are not the final rolled infusion stats. |
| Outdoor travel already involves several roles, reinforcements and supply consumption | An isolated Apex duel cannot set realm-wide healing or reward budgets. Night and dungeon comparisons must include actual overlapping roles. |
| Dungeon Astralite barding adds health, damage, armour and toughness | Fewer enemies does not imply an easier encounter. Room bounds and finite waves also provide recovery opportunities absent from replenishing fields. |

These findings withdraw the old count-only guava-overpricing conclusion. They
also remove the premise that poor blast recovery justifies cheaper mineral bills.
Neither correction proves that every current recipe, reward or fight is balanced.

## 2. Intended progression and the first priority

The intended endgame baseline remains **Strength XXX**, with room above it. No
new mod level cap is planned for scaling effects other than Resistance. An
earned modded loadout should support expeditions that consume vanilla endgame
supplies quickly; making that loadout strong is the point of progressing.

| Player position | Intended experience | What must establish the power |
| --- | --- | --- |
| First household / first hunts | Ordinary animals, tools and modest infusions are useful; physical hunt materials open the realm | Actual breeding/training and renewable food, with optional vanilla farms and better feed |
| First realm / lunar tool | Can make a dangerous outing, recover both ores and establish a mining foothold | Existing gear, actual mixed-role combat, an escape route and recovered materials |
| Established Astralite / Heartbound expedition | Sustain ordinary field encounters, mine and explore; night still changes decisions | Real mineral extraction, specialist drops, developed donor lines and replenishable ammunition/drinks |
| Prepared Crown attempt | Strong infusions, reliable rescue and ranged options; equipped room combinations demand attention | A demonstrated renewable Strength-XXX route and distinct recovery supplies, not administrative quality overrides |
| Repeated Crown victories | Sovereign equipment, companion suits and infrastructure make later trips more capable | Personal Core rewards, earned shipments and established production; not another identical first-tool bottleneck |

These are reference journeys, not hidden stage locks or maximum permitted
power. A player investing in farms, breeding or exploration earlier may exceed
the reference. One exceptional archive drink may also exceed it without proving
that a player can replenish a whole flask that way.

**First implementation priority: connect bred stats to useful infusion potency
and then make the kits represent that actual route.** Before this pass, stats largely
lengthened duration only up to 50 points. The kit instead injected quality 116 into
ordinary-fertility samples with Bloom 60. That does not demonstrate a breedable
line or affordable supply. Do not fix the discrepancy by weakening the user's
requested Strength baseline or by making every parent require Dragon Eggs.

## 3. Implemented candidate stat-to-infusion rules

Keep the actual rolled genome, stats and feeding quality as separate data.
Genes continue to decide which effects exist. A stat must never grant an absent
effect, copy the parent's unexpressed benefit or convert learned intelligence
into potion power. Duplicate effects keep their strongest level/longest duration;
they do not add together.

Use an **effect-specific potency input** rather than raising seed quality
globally. This must not feed back into litter size, drift or mutation. Feeding
quality still accelerates development and improves the actual seed/carrier.
Developed stats then let an established line produce useful drinks from ordinary
renewable food. The first slice held duration steady. The subsequent supply
comparison exposed a recovery refill mismatch, so this candidate doubles only
loyal Regeneration's base/maximum duration from 20/120 to **40/240 seconds**.
Its existing nonnegative stat duration bonus and quality multiplier still apply;
Strength and the other effects retain their durations.

The following is the implemented first prototype, not play-validated final tuning.
`Q` is the infusion's actual saved quality, including the existing richer-carrier
rule. `V`, `W`, `L`, `S`, `B` are nonnegative Vitality, Swiftness, Leap, Senses
and Bloom of its **rolled individual**, not invented kit parents.

| Effect | Candidate potency input P | Level rule / reason |
| --- | --- | --- |
| Strength | Q + V | 1 + floor(P/4). Makes the existing Vitality association affect potency and supplies an honest route to XXX. Ember-coat Strength uses the same potency stat as feral Strength. |
| Regeneration | Q + S/2 | 1 + floor(log2(1 + P/4)), retaining a separate healing curve. This halves the stat investment relative to the first prototype; it does not make Strength investment heal. Human acceptance is pending. |
| Resistance | Q + V/16 | Existing level rule with the existing III limit for juice. Strong animals can support ordinary quality without making long Resistance V available. |
| Absorption | Q + V/4 | Existing linear level rule, four buffer HP per level. Measure actual refresh behaviour and consumed buffer; do not count it as healing per second. Snow-coat Absorption also gets this potency association. |
| Speed / Jump Boost | Q + W/4 / Q + L/4, respectively | Existing linear level rule initially. Test travel, room control and recovery from knockback; do not give them Strength's stat coefficient or impose a new ceiling. |
| Luck | Q + B/4 | Existing level rule initially, evaluated against tables that actually use luck. Do not promise a universal ore, gene or Apex-drop multiplier. |
| Fixed utility and drawbacks | Existing rules | Fire Resistance, Night Vision, Slow Falling, Levitation and Slowness keep their identities; no indiscriminate amplifier growth. |

The potency-stat mapping is separate from the duration-stat mapping. A currently
statless coat effect need not silently gain a new duration bonus too. Negative
stats retain their ordinary animal drawbacks; the prototype only adds a
nonnegative potency contribution. Native representation bounds still apply.

Conditional arithmetic examples, not claims that these complete animals already
exist in Survival:

| Actual infusion inputs | Prototype result | Progression meaning |
| --- | --- | --- |
| Q0, Vitality 0 | Strength I | Plain founders do not become endgame from a new formula |
| Q4, Vitality 60 | Strength XVII | Developed physical stats are useful before the final line |
| Q0, Vitality 116 | Strength XXX | Once established, the donor line does not need an Egg or sixteen Netherite Ingots for every drink |
| Q4, Vitality 112 | Strength XXX | Higher feed/carrier quality remains an alternative contribution |
| Q0, Senses 56 | Regeneration IV, about 3.33 HP/s | The offensive milestone does not automatically mean maximum recovery |
| Q0, Senses 120 | Regeneration V, about 6.67 HP/s | A specialised recovery line has its own investment |
| Q1, Vitality 116, with a Resistance allele | Resistance III | A mature defensive line can use a renewable golden carrier; genes still gate the effect |

Do not turn these rows into a kit with every stat copied from the largest row.
Strength and recovery can be separate donor families and bottles. Ember + loyal
can combine Strength/Regeneration if actually bred; feral and loyal cannot be
expressed simultaneously. Captured brood retain their real stats/quality zero
and remain a valid paid source, without being declared endgame merely because
their combat AI was dangerous. No capture-specific deletion or fake quality tax.

The native source check rolls four actual ordinary and four Crown archive
records through the candidate, retaining every exact parent/child record in
[the source observations](audits/infusion-observations.md). Exact captured
individuals and carrier independence also have focused coverage. These are
samples, not population frequencies. Archive stats listed in loot are parent records; drift
can make the rolled individual much stronger. A rare single-use XXX drink is
acceptable; accidental unlimited endgame supply from an assumed inheritance of
archive quality is not an implementation rule. Descendants recompute quality.

## 4. Price developing the line separately from filling the flask

Breeding has two distinct investments:

1. Establish the required expressed genes and inherited stats. Selection,
   introgression, pup maturation, failed siblings, food and occupied space are
   real costs. Quality can accelerate drift; ordinary ruins or captures can
   supply developed starting stock. Their genomes may still need substantial
   crossing before they express the required combination.
2. Once suitable parents exist, produce consumable seeds without hatching each
   replacement litter. Every infusion still consumes one actual seed and one
   juice. It need not pay a fresh animal-maturation cycle on every refill.

For a conditional check on the Strength prototype, take identical, otherwise
default parents expressing stocky build, short ears and feral temperament.
Each of those three alleles contributes +1 Vitality before quality. At Q4 the
combined gain is +9 Vitality per generation and the ordinary
cost terms disappear. Starting from selected Vitality-60 parents, six rolled
generation steps reach 114; a Q4 infusion then reaches XXX. This excludes the
work of assembling that line, other stat trends, mutation/selection losses and
raising the replacement parents. It is not a six-breeding-cooldown completion
time. Alternatively, hatch that sixth Q4 litter instead of drinking it: if the
same expressed line is retained, its Vitality-114 parents can later produce a
plain-quality Vitality-117 infusion, which also reaches XXX in the prototype.
Those are exclusive uses of a seed, not two outputs from one item. Once the
line exists, expensive feed is optional for routine drinks. Keep the parents
and consume their later seeds.

At average fertility, an otherwise unobstructed established pair has a loaded
capacity of **12 seeds/hour**, or twelve drinks if all are infused. Four pairs
provide at most 48/hour; 96 drinks take two loaded hours at that capacity. This
is a conditional capacity calculation, not a measured farm rate. Actual genes,
cooldowns, crowding, food, stock movement, hatching some seeds and tool/station
access reduce or change it. One seed remains one infusion, not a drink per pup.

The original recovery reference needed 39 Q4 plus two plain selected parent
generations and then produced S240/V for only 44 seconds with a golden carrier.
The candidate instead uses bat ears, short legs, loyal temperament and average
fertility: +3 Senses at Q0 or +9 at Q4, with genuine negative movement drift.
Thirteen retained Q4 generations give S117 parents; their next plain seed rolls
S120/V. Late recovery needs two Q4 plus one plain parent generation and rolls
S24/III. This changes the kit's reference genotype, not breeding or mutation rules.
The separate Strength reference remains nine Q4 plus one plain generation,
followed by a plain production seed rolling V116/XXX.

Those zero-founder routes are conditional accounting, not a requirement or a
speedrun. Starting both endgame lines this way spends 22 Q4 breeding events:
44 Netherite guavas, requiring 352 Netherite Ingots, 352 Diamonds, 352 Gold and
44 guavas, before selection, maturation, other supplies and losses. Ordinary
ruins/archives/captures can supply developed stock; assembling their necessary
expressed genes remains real work. Do not describe every player as freshly
mining all those ingots, or assume a useful archive genome appears on demand.
No Q4 payment is required on subsequent plain-fed refill seeds.

Current golden-carrier Strength lasts **99 seconds** and recovery **88 seconds**.
Independent full uptime needs 36.36 Strength and 40.91 recovery drinks/hour.
The flask is a fixed stack, so independent selection is not its operating model.
New kits alternate the two drinks; repeating the pair every 88 seconds needs
40.91 drinks of **each** family/hour. At the conditional twelve-seed capacity,
round each family separately: **four Strength pairs plus four recovery pairs**
(sixteen adults), not seven interchangeable pairs. Plain carriers give 90/80
seconds and need 45 of each/hour in the alternating flask, also four plus four
ideal pairs, but their combat Resistance is II instead of golden-carrier III.

The previous Strength/recovery/recovery stack would waste the longer recovery
buff and require extra paid drinking, so retaining that order would defeat the
duration change. New late/endgame kits contain **16/16 and 48/48** respectively;
the totals remain **32/96**, along with all apples, ammo, gear and real boxes.
The 48 endgame pairs have a **70.4-minute ideal overlap budget** at 88 seconds
per pair. Startup, imperfect refreshes, interruptions, losses and expeditions
without continuous buff use change it. This is arithmetic, not measured survival
time or farm throughput. Each drink still commits 32 native use ticks. Existing
flasks keep their original frozen order; deliberately refill or replace a test
kit to obtain the new order.

Seed capacity is not the whole refill bill. A golden carrier's ordinary recipe
uses one fruit and **eight Gold Ingots**; two plain parent feedings add two more
fruits per produced seed. At 81.82 total drinks/hour in the ideal alternating
golden reference, that is about **245.45 guavas and 654.55 Gold Ingots/hour**,
before any waste, other food or replacement parents. Filling a fresh 96-drink
flask from scratch accordingly uses 288 guavas and 768 Gold, breeding 96 seeds
and preparing 96 carrier bottles. Glass is returned on drinking, including the
real flask path; full inventories drop it. Recovered glass can be reused, while
discarded/lost bottles need replacement. Carriers can be crafted by the existing shapeless fruit/bottle
recipe or pressed in a Juicer; infusion still takes five machine seconds per
drink (another five if pressing there). Parallel stations reduce machine
occupancy, not fruit/gold demand. Compare established orchard/gold supply and
plain-carrier Resistance II before calling sustained golden III inexpensive.

Keep Dragon Egg guavas exceptional accelerators/consumables. Nether Stars and
Beacons have repeatable infrastructure routes, unlike ordinary Java Eggs; price
their setup and throughput separately. Do not add an Egg duplication economy
or a new mineral-guava ladder merely to repair a potency formula.

## 5. Healing, protection and lethal pressure need their own pass

Native Regeneration I–VI heals about **0.4 / 0.8 / 1.67 / 3.33 / 6.67 / 20 HP/s**.
The V→VI step triples throughput. VII and later initially share the every-tick
plateau, and extreme native amplifiers have shift-wrap issues. A large numeral
is neither a healing benchmark nor a promise of continuing useful growth.
Keep the no-new-cap policy; test native effects at their actual breakpoints.

The focused native comparison uses III/IV/V/VI profiles with the same actual
endgame kit, two paid flask drinks and two Astralite Brutes, a Prismmaw and a
Lifebinder. It drives the mock player's real native body tick once per world
tick and records actual accepted damage, capped healing, debt, buffer and stock.
Positions are pinned and attacks scheduled; the player does not attack, eat
apples, use emergency gear or retreat. Only active Regeneration is overridden
for the comparison. This checks rate/mitigation interaction; it is neither
autonomous combat nor a field/wing/finale difficulty verdict. In the initial
controlled run, III lost substantial health, IV recovered, and V/VI held near
full health; V/VI being similar under this pressure does not prove VI harmless.

Human testing must compare current recovery in the same
loadout and actual mixed encounter, without changing enemy stats between trials.
Include max health, health debt, the ability to finish drinking, damage types,
effect uptime, hit immunity, absorption, emergency items and retreat options.
The existing logarithmic change is provisional balance, not proof that the old
20-HP/s drink was excessive in this realm.

Resistance III leaves 40% of applicable damage. Regeneration V can offset about
16.7 HP/s of continuous damage measured **after other mitigation but before
Resistance**, versus 50 HP/s at VI. Neither survives a lethal burst by itself.
Armour bypass is not automatically Resistance or enchantment bypass: current
beam/drain/lance tags bypass armour, and beams/lances bypass shields. Check each
native damage path instead of labelling all of them universally true damage.

Concord's power-five use heals up to 50 HP and grants Resistance V for four
seconds, after 1.5 seconds of use, with five seconds of shared cooldown and
five paid Spools/dressing equivalents. Actual missing health limits useful
healing. Four-second immunity to applicable damage is not long-duration juice
Resistance III. Keep the unique rescue role and price group use with actual
affected allies rather than assuming every activation restores 50 HP to sixteen.

Absorption, ordinary/enchanted apples, Heartbound hearts, Sovereign hearts,
Homeward/Cataclysm/normal Totems and temporary heart-debt recovery must be compared
as different defenses. Native checks confirm that Regeneration heals HP, not
spent absorption. A newly paid combat drink **does refill its 32-HP absorption
buffer even while the previous effect is active**. At a theoretical uninterrupted
32-tick drinking cadence that is 20 buffer HP/s, costing 37.5 combat bottles/minute;
real item commitment, damage tags, ammo/seed supply and interruptions matter.
Keep this existing native behaviour in this candidate; test deliberate buffer
spam before deciding whether it needs a separate change. A Totem is
not an extra permanent health pool. Preserve the user's requested apple stocks,
two Shulkers and mostly-Totem endgame inventories in every reference run.

## 6. Enemy and equipment decisions follow the corrected player supply

Start from the accepted role behaviour and existing day/ritual numbers. Do not
inflate every enemy to cancel better earned gear or compensate for a false kit.
Keep base animal genetics, learned intelligence and real equipment distinct.

The matched ordinary-genome/40-point Brute is **288 HP / 19.968 attack / 4 armour**.
With Astralite barding it is **576 HP / 31.949 attack / 18 armour / 5 toughness**.
Armour changes effective health differently against melee and a piercing lance;
special attacks have their own coefficients. Dungeon equipment must not acquire
another hidden genetic multiplier. The armour tier remains physical/drop-owned.

Use these distinct encounter references:

| Reference | What to compare |
| --- | --- |
| Daytime land journey | Several actual role combinations, renewed packs, stopping to mine/loot and getting home; include Lifebinder heart debt |
| Night journey | Current fourfold aerial **selection weight**, small flying packs and 35% non-Astralite armour chance; native caps mean this is not four times the total population |
| Moon extraction | Both minerals recovered, flying/ground Blastmaws, baiting versus Fortune mining, existing enemies and escape/void loss |
| Crown wing | Four then five Astralite-equipped opponents, mixed abilities, nine kills per wing, room bounds and recovery between finite waves |
| Crowned fight | The real phase rotation, summons, interruptions and movement, with the player's remaining expedition stock rather than an automatic refill |

Measure damage actually accepted, time exposed, healing/drinks, ammo, deaths,
Totems and recovery windows. Do not add nominal DPS from every actor or infer
difficulty from headcount. Solo/co-op and pre-stocked repeat runs are separate.

Companion health currently scales more directly than its bite, and Swiftness/
Leap have movement tails. Inspect real attacks, injury/withdrawal and barding
before applying the infusion coefficient to animal attributes too. An advanced
companion should contribute without becoming mandatory disposable ammunition;
limited animals must retain practical jobs. This pass does not grant cognitive
genes, skills or civilization from physical points.

Keep the launcher at explosion power **3×mode**, shooter/ally immunity and real
ammo. Prism/Sovereign beams have charge/recovery, penetration and line targets,
not melee Strength scaling. A power-five shot spends five units, so 64 units
are twelve complete shots plus four. Compare actual targets hit, interrupted
uses, area control and refilling trips before changing weapon damage or ammo.
The initial Sovereign I's 45 damage over a 24+24-tick cycle gave **18.75 raw
DPS**, below Prism's 64 over 32+30 ticks (**20.65**). The candidate changes only
Sovereign damage to **56×mode** (56/112/168/224/280). Its mode-I cycle now gives
**23.33 raw DPS**, a real speed/range advantage; Prism still spends one cell
for 64 rather than 56 damage. Range/charge/recovery, costs, wear, penetration,
native cancellation and mode selection remain. There is no melee Strength
multiplier on either beam. Native equipment cases check actual targets and all
five exact payments. Human tests must include multiple line targets, charge
interruptions, recovery, bosses and per-cell supply, not nominal DPS alone.

## 7. Raw supply and rewards: direction, not automatic blanket changes

Keep current lunar densities, the Astralite-only mining gate, full Blastmaw
block payouts and the first-tool/full-set separation. The first pick needs
36 Shards/36 Raw Cinderite; armour needs a further 576/720. A successful blast
may cover much of the first foothold. That is allowed: the design task is
learning and surviving extraction, not losing most of the ore. Repeated mining
for armour still needs recovered stock, fuel and role materials. Do not raise
all bills tenfold to undo the corrected drops.

An ordinary ruin's vanilla treasure exceeds an entire Crown site's current
1–3 Netherite Ingots/4–8 enchanted apples. Reassess the reward hierarchy through
actual travel/access and **modded** rewards, not by increasing the boss dungeon's
vanilla treasure until it wins that comparison. Retain generous expedition
resupply where it is useful. Audit paired chest/archive payouts as one site.

The Crown's 288–448 Shards, 576–896 raw-equivalent Cinderite, equipment drops,
role parts, archives and Core rewards have distinct jobs. Compare shipments to
an established moon run and to real companion suits, Binding/Armoury work,
Bastion/Aerie/Nursery, ammo and transport projects. Keep first-set mining before
the dungeon; later shipments may reduce repeated mining and fund expansion.
Opened stock never refills and decorative barrels do not multiply the budget.

Spools, Lenses, Hearts and Membranes are role-dependent income, not equally rare
coins. Bulk Fibre, Moonstone and established farm outputs should have credible
construction/maintenance uses without making each healing activation a full
building bill. Core rewards remain separate from generic material resupply:
solo repeat victories and a party sharing its personal Cores have different
upgrade pacing. Keep the existing physical gate and reward ownership rules.

## 8. Implementation record and pending human acceptance

1. **Implemented: donor families and reference inventories.** Use actual
   expressed genes, starting routes and breeding/capture/archive records. Include
   ordinary renewable feeding, replacement parents and the split between buffs,
   recovery, movement and utility. Expose current and proposed effect results
   to development tools; do not add a player tutorial/HUD or reveal animal stats
   without the real instrument.
2. **Implemented candidate: stat-to-infusion table and honest kits together.** Demonstrate
   a renewable Strength-XXX supply with a source account. Keep kits' requested
   power and inventory organisation; replace fake quality 116 with attainable
   records and appropriate specialised donor stats. Preserve one frozen rolled
   child per bottle, exact capture identity and seed/juice payment. Never claim
   that changing the kit proves the line's natural acquisition time.
3. **Implemented candidate, human calibration pending: recovery and production.** Compare healing breakpoints
   in real mixed-role fights, then duration/refill capacity. Do not simultaneously
   nerf recovery, raise food costs and strengthen enemies; that obscures the
   cause. Keep regen's provisional status explicit until this comparison exists.
4. **Implemented lance correction; encounter acceptance pending.** Use the field/night/moon/
   room/boss references above. Change a demonstrated problem's coefficient,
   cadence or reward rather than adding another global health multiplier.
5. **Reviewed and retained: rewards, recipes and enemy/companion attributes.**
   The native acquisition audit and bills already include rich ruins, full
   self-blast payouts, finite archive alternatives and earned shipments. These
   do not establish a specific reward shortage or excess once corrected drink
   supply is available. Changing them now would mix several unsupported causes.
   Record actual burn/recovered yield in the journeys below before reallocating
   a named payout. These retained values are part of the candidate, not an
   unimplemented promise to multiply them later.

All rows below remain **pending human test**. Use a fresh matching protocol-50
client/server and normal stocked inventories; the operator kit is a combat
reference, not a substitute for the first row's acquisition history.

| Human trial | Record / acceptance question |
| --- | --- |
| First household through renewable drinks | Founder acquisition/genes, ordinary guavas/orchard, real training/building, failed selections, parent raising, loaded waiting, pressing/infusion and seed transport; can useful early buffs be made before realm loot exists? |
| Establish endgame donor families, then refill | Keep setup separate from consumable seeds; record actual archive/capture inputs, Q4 acceleration if used, golden-carrier supply and output of each family; is III/V progression useful without compulsory dozens of rich generations? |
| Full daytime and night journeys | Use III/IV/V/VI recovery with the same gear; mine/loot while new mixed roles arrive, finish both 1.6-second drinks, count real overlap gaps, food, ammo, Totems, damage, debt and returned stock. Night weight is not population. |
| First lunar foothold and repeat extraction | Recover both mined minerals, include flight/void risk and Blastmaw collection; compare first tool with later Fortune mining without counting a later dungeon shipment as bootstrap. |
| Crown wings and actual finale, solo and co-op | Keep the real remaining expedition stock, nine kills per wing, equipment, warnings, waves/summons and emergency options; record roles, duration, interruptions and deaths. No refill between phases unless the player physically retrieves stock. |
| Defence and weapon stress cases | Try repeated combat-bottle absorption refresh, finite high-quality archive recovery/VI, overlapping Standard uses, line targets and Sovereign modes I–V versus Prism; record actual payments and useful results. |

For each comparison record stage, gear/enchants, effect levels and duration,
simultaneous roles/equipment, actual active/loaded time, materials recovered,
supplies consumed, deaths and stock left on returning. Record setup separately
from repeat runs. Report uncontrolled player runs honestly; native generated
volumes, theoretical station capacity and healing fixtures are different evidence.

Use focused pure/native checks for changed potency arithmetic, inheritance,
capture/archive inputs, real effect ticks, kit contents and payment/save behaviour.
Do not repeat every passing check for each numeric candidate or run a large
automated combat matrix as a substitute for ordinary play. One coherent build
and native release gate follows this implementation batch. Passing those gates
establishes mechanics and regressions, not human acceptance of these trials.

The implemented save policy is recorded in `INFUSION_SPECS.md`: old bottles are
evaluated from their exact unchanged inputs using the current potency rules.
For future changes, state how old saved bottles are evaluated under changed
potency rules. Preserve their actual genomes, stats, contents and ownership;
never rewrite a legitimate high value into the new reference kit or silently
claim that a legacy/admin bottle has a Survival production history. No container
restock, world regeneration, gene reorder or forced chunk loading is part of
rebalancing.
