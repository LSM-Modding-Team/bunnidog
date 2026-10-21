# Combat companions — design and implementation plan

Planned 2026-10-20; implementation authorized and started 2026-10-21.
**B1/B2 and the initial B3 party are implemented**: see
[the actual specification](COMBAT_SPECS.md). The source audit below records the
pre-combat baseline, not today's code. Three-member selection, spacing, bounded
held defense and saved sightings now exist. B3 combat-specific breeding/seed/pup
handoffs and B4 signatures remain unfinished. B5 natural/client/co-op evidence
also remains; prototype numbers are not validated encounter balance.

The aim is a companion the player chooses, breeds, commands and brings home.
A common adult should help in an ordinary fight. Later breeding should change
how the partnership works, with visible strengths and situations that expose
its weaknesses. The player should learn by fighting alongside it, not by
memorizing a manual or maintaining a hidden loyalty meter.

Read [the player scenarios](COMBAT_SCENARIOS.md) alongside the mechanics below.
Those are the experiences this plan should produce, rather than an extra list
of systems to implement.

## Recommended direction

- Use the seven existing genes and five bred stats. Add no Attack gene or
  sixth stat for the initial combat design.
- Separate inherited temperament from the individually learned keeper bond.
  Eventually all four temperaments can bond, with distinct combat habits.
- A small, explicitly selected party: **three active fighters per keeper** as
  the first balance target. One companion must be enjoyable on its own.
- Keep emergency recall dependable. Personality affects initiative, commitment,
  positioning and withdrawal; it does not roll whether a heard recall works.
- Start with native melee, useful existing adaptations and readable movement.
  Add a few physical signature moves after the basic encounter feels good.
- Recommend recoverable combat defeat for bonded adults, with real injury and
  aftercare. A prized breeding animal should be worth taking on an adventure.
- Teach through the existing care/Whistle interfaces, animal intent and actual
  behavior. Training grants no inherited points; combat is not an XP grind.

These choices preserve the ordinary breeder, farmer, carrier and explorer.
Joining a combat party is deliberate. Existing followers do not silently become
fighters when a save is opened with a new version.

## Pre-combat baseline — source audit at `b34e90d`

| Shipped behavior | Implication for this proposal |
| --- | --- |
| Ordinary base health is 8 HP; Build and Vitality scale it. Attack damage is a fixed 2 HP attribute. | Fragile early companions and damage progression need encounter tuning; a new damage calculation is real implementation work. |
| Feral animals retaliate with native melee/HurtByTarget. Other temperaments use panic; skittish also avoids players. | Adding a keeper target selector alone will not produce a usable loyal fighter. Movement, panic and target ownership need one coordinated decision. |
| Opted-in feral Nest guards use loaded safe paths within eight blocks, an explicit hostile allowlist and one-second bites. | Keep a useful home-defense path and distinguish it from expedition combat. Current guards do not attack Creepers or everything implementing `Enemy`. |
| Loyal/stoic adults train with a Whistle and one plain guava. Keeper/order/provision state is individual and saved. | Reuse the bond; broaden temperament eligibility deliberately in a later slice. Pups inherit neither their parent's keeper nor its orders. |
| Follow/Wait and most commands require calm; normal calls can skip a fighting animal. | Emergency recall must be a separate high-priority path that works during danger. |
| Current Whistle air-use calls Follow; crouch-air returns to residence. On an animal it trains/toggles Follow/Wait; crouch-use releases training. | The proposed simple combat gestures change these shortcuts. Preserve the named care operations and document the changed gestures. |
| Coats/Build already give armor, immunities and knockback resistance; legs supply mobility, swimming and defensive Phase. | Much of the genetic identity already exists. Do not advertise another resistance or a new offensive ability as already shipped. |
| Trail provisions use real ordinary food from equipped bundles, after danger, with shared meal recovery. | Build recovery around actual care stock. No virtual ammunition, conjured food or instant health reset. |
| Travel Baskets preserve the whole animal but refuse active/recent danger and release its old home claim on capture. | Rescue needs an explicit narrow danger exception, while retaining state and conservation. There is no current basket health-floor requirement to remove. |
| Carrying slowdown applies to excess bundles beyond the expressed Build's capacity. | Do not invent an existing weight penalty for normally equipped or full bundles. |
| Apex has its own amplified combat rules and is not an ordinary care animal. | Companion calculations must not overwrite Apex attributes; an Apex can be a deliberate enemy, never a recruit. |

Relevant sources: [Bunnidog](../src/main/java/com/bunnidogs/mod/entity/Bunnidog.java),
[GuardGoal](../src/main/java/com/bunnidogs/mod/entity/GuardGoal.java),
[TrailCompanion](../src/main/java/com/bunnidogs/mod/entity/TrailCompanion.java),
[HuntGoal](../src/main/java/com/bunnidogs/mod/entity/HuntGoal.java),
[Adaptations](../src/main/java/com/bunnidogs/mod/genetics/Adaptations.java),
[Carrying](../src/main/java/com/bunnidogs/mod/genetics/Carrying.java) and
[TravelBasketItem](../src/main/java/com/bunnidogs/mod/item/TravelBasketItem.java).
The established [trail](TRAIL_SPECS.md), [care](CARE_SPECS.md) and
[family](FAMILY_LOOP_SPECS.md) specifications describe their current integrated
behavior; [COMBAT_SPECS.md](COMBAT_SPECS.md) records the new combat rules.

## The first playable journey

1. Meet an ordinary adult, give it a keeper bond through existing care, and see
   a short description of its temperament. No rare trait, bred-point minimum,
   recorded parents or registered pedigree is required for basic combat help.
2. Deliberately add it to the travelling combat party. This enables **Assist**;
   ordinary training/following alone keeps combat assistance off.
3. Fight a Zombie together. A loyal partner helps the player's attack or reacts
   to a nearby attacker. Its windup, bite and return to position are visible.
4. Use the Whistle to disengage it. It returns towards a safe nearby position
   even while hurt or currently pursuing something.
5. Heal or rest it with ordinary care. Later, look at a pup's actual combat
   qualities and decide whether to raise it for a different job.

The first successful experience is helping with a real fight and coming home,
not unlocking a combat-stat menu or completing a training grind. New players
should need two Whistle gestures and clear buttons, with exact genetics optional.
If an ordinary partnered fight repeatedly downs a common adult, fix baseline
survival/positioning rather than requiring armor or several bred generations.
Conversely, allow a developed line to outgrow everyday Zombies; earned breeding
progress need not be erased by scaling every enemy to the party.

## Temperament, loyalty and trust

The keeper bond answers **whose commands and protection matter**. Temperament
answers **how the animal fights and how much direction it benefits from**.
Food can establish the bond; repeated treats do not turn a feral genome loyal.
Hidden alleles affect offspring, not unpredictable moments of rage or obedience.

| Expressed temperament | Automatic behavior in Assist | Player's useful role for it | Cost/weakness |
| --- | --- | --- | --- |
| Loyal | Watches the keeper, helps a legal target the keeper attacks and reacts to a nearby real attacker. Returns promptly after the threat. | Easy first bodyguard; reliable partner when the player is busy aiming or mining. | Moderate bite; will give up an exposed pursuit sooner than a feral fighter. |
| Stoic | Prioritizes a threat near the keeper or held position; prefers a stable approach over a long chase. Can accept an explicit distant focus within its leash. | Hold a doorway, intercept a melee approach, protect the player's working space. | Slow to capitalize on a fleeing target; modest offensive pressure. |
| Feral | Retaliates and commits strongly to the chosen nearby enemy. Provides less automatic keeper-watch coverage; Focus is useful for directing it. | Deliberate striker or fierce territorial guard. | More exposed between committed attacks; poorer rescue/bodyguard initiative. Never attacks its keeper or ignores recall as a personality penalty. |
| Skittish | Notices threats, warns and keeps an escape route. A safe explicit focus produces short attacks followed by repositioning. | Rear watch, danger warning and a cautious fast skirmisher. | Lower sustained pressure and earlier withdrawal. Its fleeing is purposeful and legible, rather than aimless running away from its own keeper. |

All four can ultimately be bonded. The first vertical slice may retain the
existing loyal/stoic training gate while the wider behavior is being built;
the completed design should not leave feral combat potential permanently
uncommandable or require stoic mutations for a useful fighter.
Bonding does not require surviving a new combat trial or repeatedly feeding a
loyalty meter. Keep deliberate calm training; a bonded skittish animal must stop
using its own keeper as an ordinary player-avoidance target.

An initial withdrawal comparison could be loyal 40%, stoic 35%, feral 25% and
skittish 55% health. **These are tuning hypotheses**, not a courage score or a
reason to delay emergency escape. Obvious fire, drowning, a fuse or the keeper's
recall can require withdrawal above those thresholds. Re-entry needs a higher
health margin and a safe opportunity, so the animal does not oscillate every tick.

## Genes should create fighting choices

| Gene | Combat contribution | A meaningful choice it preserves |
| --- | --- | --- |
| Build | Existing health, size, pack capacity and some knockback resistance; proposed bite weight and commitment. | A heavy defender survives and lands a weightier hit, but a longer committed attack and larger body make tight or mobile fights harder. Slim/lean lines reset more nimbly. |
| Temperament | Initiative, keeper attention, attack commitment, withdrawal style and a modest damage bias. | A feral striker and loyal bodyguard should both be worth breeding. Stoic is useful without being the universal best allele. |
| Legs | Existing ground speed/jump/terrain benefits; later signature approach or escape options. | Springy can support a short pounce; Phase keeps its defensive blink. One expressed leg allele cannot supply every movement tool. |
| Ears | Awareness, hearing and how far it can work while remaining commandable. | Large ears cover more space. A small-eared tough line stays closer; it is still useful in a tight defensive role. |
| Coat | Existing armor, environment protections, camouflage and practical regional advantages. | Slate armor, moss poison protection and ember fire protection are different choices. No single expressed coat provides all three. |
| Diet | Which real food the travelling group can use. | A hunter, omnivore and regional forager change logistics; Diet does not secretly decide whether the animal is loyal or allowed to bite. |
| Fertility | Access to more candidates or better-quality future generations, as now. | Swarm is not an in-combat summon ability; starborn does not become a free resurrection. |

Keep the existing five points meaningful:

- **Vitality:** health remains its main effect. It can contribute a smaller,
  diminishing offensive benefit, rather than scaling bite damage at the full
  existing health rate.
- **Swiftness:** arrival, repositioning and getting out of reach. Do not turn it
  into dozens of damage events per second or bypass native hurt invulnerability.
- **Leap:** terrain and, when a suitable move exists, a modestly larger safe
  gap-closing opportunity. Good legs matter at zero points; points improve them.
- **Senses:** noticing relevant threats, keeper awareness and hearing. Better
  awareness does not identify every mob behind walls or make recall probabilistic.
- **Bloom:** proposed recovery between fights, alongside its existing growth,
  nursery and breeding-quality uses. It grants no combat-healing aura.

Points still come from breeding. Combat, exercise and repeated practice grant
no permanent genetic stats. Mutations/splicing must update the actual profile
and available moves immediately, while keeping the keeper bond and current
health fraction. A changed temperament changes habits, not allegiance.

Combat should make existing recessive genetics interesting: loyal/feral carrier
parents can produce visibly feral offspring. A hidden feral copy does not grant
the loyal parent a secret damage bonus. Existing inheritance odds and Calipers
already provide the right language for understanding this.

### Combinations, not four fixed classes

Temperament is one layer, not a rule that every defender must be large or every
skittish animal must be slim. These examples need no class unlock, matching-set
bonus or additional allele. Their strengths come from the same shared rules.

| Example family | Why a player might want it | What still costs it |
| --- | --- | --- |
| Feral + chonker + slate | A heavy close-range brawler with a strong bite, armor and knockback resistance. Useful when deliberately directed at something nearby. | Large body, long commitment and weaker keeper-watch initiative; not automatically the fastest killer or easiest bodyguard. |
| Loyal + slim + springy | A light protector that arrives promptly and returns to its keeper; a later short pounce adds an interception opportunity. | Less health and bite weight. Mobility must actually keep it useful, rather than forcing every protector to be a tank. |
| Stoic + lean + fleet | A disciplined partner that repositions quickly around a held area in open terrain. | Very little health for absorbing hits; “holds here” cannot mean standing still and taking every blow. |
| Skittish + stocky + moss | A durable, poison-resistant cave lookout with a more forgiving escape margin. | Withdraws early and contributes less sustained pressure; poison protection does not make it Wither-proof. |

Good breeding questions are “what do I want it to do on my next trip?” and
“which weakness am I willing to work around?” Some cosmetic coats can remain
cosmetic. Not every combination must be equally strong in every encounter,
and the whole party need not match one ideal pedigree.

## Commands the player can trust

Keep three separate concepts internally, while showing simple words to players:

| Concept | Proposed values | Purpose |
| --- | --- | --- |
| Movement | Follow, Wait/hold position, Resident/home | Where this animal belongs. Existing home claims remain separate. |
| Engagement policy | Stay safe, Assist | Whether it starts cooperative combat. |
| Current activity | Approaching, attacking, regrouping, withdrawing, downed, recovering | What it is doing now and why; not another menu of persistent settings. |

**Normal Whistle use: Come back.** Air, terrain and entity crosshairs all give
the same emergency action. Do not accidentally focus an enemy, release a bond
or start training because the player's aim crossed an entity during panic.
Recall applies to the active party and existing travelling followers within
hearing; unrelated residents and parked non-party animals stay put. A command
panel can deliberately call a larger group outside combat.

**Crouched Whistle use: a small contextual order panel.** It can offer Focus the
looked-at legal enemy, Hold this safe position, Assist, Stay safe and existing
movement/home operations. Party selection is a short secondary view. Keep the
existing bounded server-authored menu approach; do not add thirteen controls
to an already full animal panel. Releasing a bond stays a deliberate care action
outside combat, rather than a Whistle modifier that can be hit under pressure.
Existing inventory-backed training remains available in care.

With several partners, show the scope beside an order: one named companion or
the whole party. A keeper can send a feral striker at an archer while leaving
a loyal bodyguard in Assist. Default to the last visibly selected companion
for Focus; give a new one-member party that selection automatically. Whole-party
Focus/Hold is deliberate, while emergency recall always covers the party.
No command should depend on an invisible, stale selection.
Use a custom name when present or a short appearance label otherwise; naming
an ordinary adult is not a prerequisite for directing it.

Prototype Focus while actually moving and defending. A short contextual choice
is the initial recommendation, not permission for a multi-page combat menu.
If it interrupts fighting, try a quick crouch-use on a valid enemy as the
explicit Focus shortcut, keeping crouch-air for the panel and normal use always
recall. Pick one discoverable interaction after that comparison; do not ship
both competing gesture schemes or require a new keybinding for basic combat.

The gesture migration is intentional: old crouch-air Return home and direct
Whistle training/toggling/release move to visible named operations. Saved keeper,
home and order data do not reset. Update hints at the same time as the gestures.

An explicit **Focus** is one task, not a permanent switch from Stay safe to
Assist. After that target is lost/dead/unreachable, the previous policy resumes.
It cannot order a downed animal or override a withdrawal into a lethal route.
Wait plus Assist is a held defense anchored to the chosen place; it does not
chase halfway across a cave because the keeper shot something far away.
Resident/home suspends expedition assistance and excludes the animal from
party recall, while preserving the bond, Nest and deliberately assigned home
role. Returning to residence uses a viable local route, not a promised journey
through unloaded terrain. An active party member holding position is recalled;
an unrelated parked Wait animal is not automatically recruited.

After **Come back**, clear the owned combat target and suppress immediate
re-engagement with the same encounter. New incoming attacks do not cancel the
withdrawal. Re-arm ordinary Assist after regrouping and a genuinely quiet
interval, or with an explicit Assist/Focus action. Start by trying five quiet
seconds near the keeper; the important result is an actual opportunity to leave.
Stored targets, damage memory or an independent retaliation goal must not drag
the animal straight back in. Show “Regrouping” while this suppression is active.

Command hearing remains physical, same-level and loaded. Set pursuit leashes
inside the animal's current hearing envelope with a margin; small ears should
not routinely run beyond the distance at which the keeper can recall them.
Commands never need a hidden obedience roll. No item-wide cooldown should lock
out recall immediately after an attack order; rate-limit other orders separately.

## A small party that leaves room for the player

Recommend three active fighters, with all slots available once the keeper has
three eligible bonded adults. This is a combat command budget, not a limit on
breeding, residents, existing pack carriers or how many pets someone can keep.
Joining/removing fighters is explicit and cannot cycle fresh reserves into an
ongoing battle. Downed/captured party members do not immediately free a battle
slot for an unlimited replacement stream.
Once the keeper actually disengages to safety, ordinary party changes are
available again; an unseen hostile behind a wall does not lock the roster.

Three allows interesting combinations: a defender plus striker plus lookout,
or three imperfect siblings with complementary traits. It should not require
the player to fill an MMO tank/healer/DPS template. Ordinary followers outside
the combat party retain transport/care; they do not all inherit the keeper's
attacker or receive Focus as an unintended sixteen-animal army.
For bonded non-party animals, Stay safe favors escape/regrouping over ordinary
feral retaliation. Preserve unbonded feral self-defense and explicitly assigned
territorial guards; neither is a back door for remote party commands.

Give companions nearby approach positions and room to disengage. Against one
small enemy, start by allowing two comfortable contact positions; the third
can watch for another threat or reposition. Never make a queue that prevents
a reachable companion from acting when its partner withdraws. Respect native
damage invulnerability; simultaneous bites are not guaranteed additive DPS.

They should leave the keeper's movement and aiming space clear. Follow offsets,
reasonable contact spacing and an escape path matter more than elaborate
formations. A ranged player should be able to shoot past their partners.
Existing excess-bundle slowdown still applies; do not add a new full-pack
weight simulation to make carriers artificially awkward.
Actual body width still matters: a large chonker cannot be promised the same
one-block passage as a lighter animal. Resolve positions using its real size.

Home guards remain territorial and separate. Their eight-block Nest defense
does not become a remote Focus army, and expedition orders do not consume their
Nest claims. Audit guard/retaliation interactions so a trained guard cannot
bypass its intended boundary merely by changing target ownership.

## Target choice and fighting in an actual world

One controller should own a companion combat target and arbitrate the competing
panic, refuge, hunt, guard and follow behaviors. Do not let independent goals
continually overwrite each other's target or suppress emergency movement.

Priority: immediate environmental survival → rescue/withdrawal/recall → a legal
combat task → ordinary follow/hold → home routines. A fighting animal does not
break off to garden, breed, gather food or chase edible wildlife.

| Situation | Recommended response |
| --- | --- |
| Keeper attacks/is attacked by an ordinary hostile nearby | Relevant Assist temperaments respond within awareness, their leash and a viable route. They do not sweep every visible room. |
| Neutral Enderman, Piglin or other unprovoked neutral | Do not initiate. A real active attacker can be considered with normal danger rules. |
| Creeper | Warn and create distance; no automatic melee rush. Initial Focus refuses with a short explanation. A later specialized intercept needs its own successful escape prototype before becoming a promise. Every temperament recognizes an obvious fuse; a lookout adds earlier attention, not exclusive access to basic survival. |
| Warden, Wither, Dragon or Apex | No automatic engagement. Later boss support is an explicit high-risk task, limited by reach and terrain; an airborne target does not cause endless futile jumping. Ordinary bunnidogs remain excluded as prey/targets, while Apex is classified separately. |
| Hostile behind a closed door, pit, lava or unsafe drop | Stay on the safe side; show “Can't reach safely.” Never chew through player blocks or treat immunity as permission to use an unvalidated route. |
| Drowned/fish in deep water | Initial ground combat stops at the safe shore. Aquatic fighting requires a later route/air/return-to-shore design; paddle legs alone do not establish that AI. |
| Player, owned animal, ordinary bunnidog, named/led/passenger livestock | Excluded from ordinary combat targeting. Hunting retains its separate existing eligibility. PvP/duels require their own later explicit design. |
| Keeper dies, logs out or leaves the dimension | Stop cooperative offense. Seek a safe nearby waiting place instead of continuing a vendetta or walking blindly towards a distant Nest. |

Use what the animal can reasonably notice: relevant visible or locally heard
events, actual keeper damage/attack events and recent targets. No global hostile
lookup or permanent tracking of an attacker through terrain. Forget invalid,
old, allied, unreachable and out-of-leash targets. No remote chunk tickets or
teleporting across terrain/portals to maintain a party.

Preserve individual waiting/last-seen information for recovery. The Whistle can
show a party member's last observed place/dimension and reuse existing compass
marker conventions. This is a breadcrumb, not a live remote entity scanner.
A deliberate Wait remains distinct from waiting because the keeper disappeared.

## Damage and physical fighting — prototype, not balance evidence

Use ordinary native damage, armor, effects, knockback and cancellation rules.
One bite produces at most one real hit. Keep Apex's separate bounded encounter
profile. Audit all ordinary attack callers: a changed bite attribute also affects
feral retaliation, home guards and food hunting, not just the new companion goal.

A first mathematical prototype could be:

`bite HP = 2 × Build factor × Temperament factor × Vitality factor`

| Expressed Build | Starting factor | Starting interval | Physical distinction |
| --- | --- | --- | --- |
| Average | 1.00 | 20 ticks | A useful common reference animal. |
| Slim / lean | 0.95 / 0.90 | 20 ticks | Light hit and short commitment; easier skirmishing. |
| Stocky / woolly | 1.10 | 24 ticks | More weight, with existing health/terrain/carrying differences retained. |
| Chonker | 1.20 | 28 ticks | Heaviest hit, long commitment and a large body; not automatically the best sustained damage. |

Starting temperament factors: loyal/stoic 1.00, feral 1.20, skittish 0.90.
These modest factors must not eclipse the behavioral differences.

To respect existing upward breeding while avoiding full health-rate damage
scaling, try `1 + 0.20 × sqrt(Vitality / 20)` for nonnegative Vitality: +20 gives
20% extra base bite, +100 about 45%, +500 100%. For negative Vitality, a smooth
floor such as `max(0.65, 1 + 0.02 × Vitality)` retains a usable bite. This is a
candidate curve, not an adopted combat cap. Do not clamp saved points, rewrite
existing health curves or quietly invalidate long-developed animals.

Keep a readable windup and recovery, with at least twenty ticks between real
basic hits. Swiftness helps contact and repositioning rather than removing that
floor. The player should see why a heavy hit missed. Short, limited knockback
can interrupt pressure; three pets must not permanently stun-lock one enemy.
Native knockback resistance and bosses still matter. Test the curve against
the actual engine's hurt window before interpreting any paper DPS.

Later signatures should be a small set of physical actions:

- **Springy pounce:** a visible, short single-target leap over a verified clear
  gap. Useful at zero Leap, with modest extra reach from points; an initial
  2–4 block envelope and several-second recovery are enough to test. It cannot
  home through walls, repeatedly stagger a boss or leap without a safe landing.
- **Braced interception:** a stoic/heavy defender contests a nearby melee
  approach with position and a short body check. This is not a universal taunt
  that rewrites every mob's target or makes projectiles harmless.
- **Skirmish and warn:** quick contact followed by a safe reset, with skittish
  warning behavior especially useful before a Creeper reaches the player.
- **Phase escape:** retain the existing nonlethal-hit defensive blink, damage
  taken and recovery. Prevent an immediate suicidal re-path to the same threat.
  Do not turn it into an offensive teleport or guaranteed rescue from the void.

Most coats already have a combat story. An ember partner can survive accidental
fire, a moss partner ignores poison, slate resists ordinary hits and a void coat
avoids Levitation. None implies Wither immunity or void survival. A pale guide's
existing Night Vision can help the player before fighting. Glow remains self-lit,
not a world light source or an automatic taunt. Ordinary cosmetic coats remain
valid choices; every color does not need a spell.

## Surviving defeat and caring for the family

**Recommend a downed/recovery system as part of the first public combat slice.**
The meaningful loss is a fighter leaving the encounter, a rescue decision and
aftercare. Permanent loss of a rare, named, many-generation animal from one
ordinary burst would encourage players to leave their best companions home.

Recommended default:

1. A bonded ordinary adult that would die from ordinary combat becomes downed
   and starts an injury/recovery cycle. This applies to bonded travellers as
   well as selected fighters, so a pack carrier is not disposable. Wild animals
   and Apex retain their existing death/loot rules; pups are never recruited.
2. A downed animal cannot fight, work, breed or supply drops/XP. Ordinary hostile
   combat stops targeting/damaging it. It does not block movement or intercept
   projectiles as an immortal shield. Clear its outgoing combat state.
3. No hidden bleed-out countdown: the player can finish the fight and rescue it.
   Clear residual burning/Wither on defeat and protect against ordinary combat
   after-effects while downed, so the apparent rescue window is real. Actual
   continued lava/flame-block contact, drowning, unsafe falls, the void and
   explicit kill/bypass damage remain terrain hazards according to the native
   damage types and expressed immunities. A Creeper's explosion is combat, not
   automatically an environmental exclusion. Distinguish residual burning from
   standing in fire in the actual API before implementing this boundary.
4. On safe ground, first aid with a real plain guava lets it recover into an
   injured, non-fighting state. An empty Travel Basket is the deliberate emergency
   extraction option when remaining there is unsafe. Only downed rescue bypasses
   the ordinary recent-combat capture refusal; capture does not cure anything.
5. Start by trying five loaded minutes of safe recuperation, shortened by Bloom
   with a two-minute floor, plus at least half health before combat readiness
   returns. A safe resting spot near the keeper is enough; a Nest/Trough can
   automate care but is not a mandatory field hospital. Actual meals supply HP.

Safe rest means a calm animal on a viable resting surface without current
danger/damage; it does not scan every cave behind a wall for an unrelated mob.
The player should be able to build, sort supplies or continue a quiet camp task
while partners recover concurrently. Calm safe walking can count too; there is
no required sit toggle or need to watch a timer. Frequent collapse/recovery
loops are a balance failure, not the activity between every ordinary fight.

While recovering, the animal can be brought home without fighting. Its genome,
parents, name, keeper, cargo, injury and remaining recovery travel together.
It can follow/wait and carry its existing cargo, but breeding and home jobs
remain paused until recovery, rather than competing with recuperation.
Walking preserves its home; basket capture retains the current home-release
rule. Unloaded/basketed time does not quietly heal it or refresh the protection.
Switching party slots, reloading, renaming, rebonding or repeated capture/release
must not turn an injured animal into a fresh fighter. Ordinary attacks cannot
be re-enabled until recovery is complete. A classic permanent-death server
option can be considered after the default feels coherent.

If an injured non-fighter suffers another lethal ordinary combat hit during
extraction, it collapses into the same downed cycle. It does not earn a fresh
fight, lose its saved recovery progress or become secretly eligible for instant
permanent death after first aid. That would make leaving it downed safer than
helping it stand. Environmental hazards still apply, and repeated first aid
still spends actual stock; a basket is the safer deliberate extraction tool.

High-health lines should not require an absurd number of two-HP meals after
every battle. Aim for about four real ordinary meals from near-empty to full
health during safe care; try `max(2 HP, 25% of max HP)` per meal, retaining the
existing automatic meal recovery and danger refusals. Paid calm direct hand
care preserves its earlier behavior: it spends actual stock, does not wait on
that automatic cooldown, and does not clear it. This leaves an ordinary
eight-HP animal's meal unchanged and keeps unbounded health progression from
creating unbounded feeding chores. Hands, Troughs and bundle provisions must
agree. Bloom can shorten recuperation; do not add simultaneous permanent
regeneration, an aura and free food efficiency. First aid spends its real guava
once, without double-paying healing through two care callbacks. Tune ordinary
healing and injured recovery together, not as unrelated economies.

## Multiplayer, rewards and existing systems

- The keeper bond and native team relationships establish allies. Do not assume
  the current Animal subclass inherits a wolf's owner/alliance behavior.
- In the initial PvE design, routine player melee, sweeps and projectiles should
  not injure any ordinary bonded bunnidog. Casual co-op should not require
  scoreboard setup to avoid shooting a friend's companion. Friendly projectiles
  pass rather than being consumed by an invulnerable pet. Keep native team/owner
  rules for recognizing other allied pets; companions never attack players.
  Wild animals/Apex retain their ordinary interactions. TNT is still real
  explosion damage, subject to ordinary downed protection; a blast can also
  create genuine falling/lava hazards. Do not make “crouch with a sword” a
  hidden permission to kill a pet.
- Keep deliberate release outside combat/recovery for players using existing
  breeding/material harvesting. Ordinary unbonded death drops remain possible;
  recovery does not generate legendary offerings or substitute for an Apex ritual.
- Other players can provide ordinary care/rescue under existing interaction
  protections without acquiring the bond or issuing combat orders. Preserve the
  current basket/lead model; no new combat action grants permission to steal
  another keeper's party slots or bypass protection.
- Attribute cooperative kills through native responsible-player mechanisms
  where applicable. Start with a same-level keeper within the local command
  envelope who recently attacked that enemy, received its hostile attack or
  issued the accepted current Focus task; do not invent remote credit or a
  general AFK detector. Mere ownership, an old Assist toggle or an absent
  home-guard keeper is not participation. Award normal loot/XP once; do not
  copy the keeper's held Looting/Fortune onto every pet bite or bypass
  native advancement/reward checks with blanket synthetic player kills.
- Combat cannot claim Show ribbons, commission success or discovery rewards by
  itself. The living animal still meets each system's real requirements.
- No combat breeding, work or food-seeking interruptions. Existing care and
  transport retain their purpose, real items and reentrant/cancelled-event
  conservation. Leads, passengers and baskets retain higher transport priority.

## Showing the player what matters

Use a short role sentence and live intent in care/Whistle views: “Defends you,”
“Fights best with direction,” “Holding here,” “Hurt — falling back,” “Regrouping,”
“Can't reach safely,” “Recovering.” Show why an order was refused at the point
of use. Warnings are local and bounded, not a repeated chat checklist.

Represent commitment, alarm and withdrawal with the animal's existing head,
ears, tail and body pose. Preserve approved anatomy and textures. An optional
small party health/intent display can appear for an active encounter; it does
not need a permanent screen of damage meters, ability bars or threat tables.

Calipers should explain the actual bite, commitment, protections, hearing and
withdrawal style. Exact damage is base potential before the target's defenses
and native effects, not a guaranteed DPS rating. Reuse the Table's actual
inheritance, seed and living-family handoffs when comparing potential offspring.
Nonlinear combat curves must not present `profile(expected points)` as an exact
expected result if rounding/phenotypes make it different. Prefer honest ranges
and trait odds over a single universal “combat score.”

Combat preferences, bonds, party selection and injury are care state, not genes.
They must not enter Tissue Samples, seeds or newly born pups. Preserve seven-gene
allele order and existing five-stat formats. Future saved fields need legacy
defaults that keep existing partners out of newly initiated combat, preserve
homes/cargo and retain injured state across basket/save transitions.

## Implementation sequence — each slice proves a player experience

The original batch wrote the plan only. The subsequent continuation explicitly
authorized implementation. B1/B2 and initial B3 party behavior now have native
server evidence; read the actual specification for limits. B3's combat-specific
breeding handoffs, B4 signatures and B5 encounter/client evidence remain work.
The success criteria below continue to include player experiences that headless
checks cannot prove.

| Slice | Work | Done when |
| --- | --- | --- |
| B1 — One dependable partner | Current loyal/stoic bond; explicit combat opt-in; basic gene-derived bite and native assist/defense; one combat target owner; emergency recall/withdrawal; downed, first aid and basket recovery; essential ally/owner-loss/save rules. Audit all changed attack callers, including hunting/guards, and preserve Apex. | A zero-point ordinary adult helps in a Zombie fight, can be called out while actually fighting, can survive an ordinary combat defeat and can be rescued without item/animal duplication. Build/points affect its actual profile. Do not publicly ship the offense half while rescue/control are unfinished. |
| B2 — Genes change the partnership | All four bondable temperaments; fuller shared profile/readout; meaningful movement/awareness/withdrawal and lookout warnings; actual care/Calipers descriptions; native adaptation integration. | The same encounter plays differently with loyal, stoic, feral and skittish partners, and each has a useful situation. Ordinary, hidden/mixed and developed lines match their actual genetics. |
| B3 — A useful small party and breeder loop | Three-member selection; spacing and target distribution; held positions; deduplicated party warnings; actual seed/pup comparisons; complete recovery, supplies and last-seen handoffs. | A melee and a ranged keeper can use three partners in a mixed fight without body blocking, endless pursuit, unsafe roster replacement or reading a manual. Returning to breeding offers a clear improvement to pursue. |
| B4 — Signature movement and regional encounters | Start with springy pounce and braced interception; strengthen defensive Phase integration. Add aquatic combat only with air/route/shore-return proof. | Each added move produces a distinctive good moment and a readable failure/counter; it does not replace the ordinary bite, create permanent stun or promise unavailable routes. Dangerous-target interception is an experiment here, not an initial entitlement. |
| B5 — Pacing and optional depth | Natural Survival/co-op play; common versus developed line balance; optional practice/sparring, fitted gear or further advanced roles only when useful. | Players bring animals on adventures, understand recall and recovery, can prefer different lines, and spend more time playing than maintaining menus. |

Owner loss, friendly fire, current-world/distance checks, transport conservation
and legacy defaults belong in the first usable slice; they are not polish left
until the end. Avoid accumulating inaccessible stubs or dozens of optional
abilities before the first actual encounter works.

Validation when implementation exists should be proportional: focused pure
profile/inheritance checks, native fight/recall/ally/downed/rescue/save scenarios,
and a real client encounter for animation, control and path feel. The existing
107 passing native scenarios verify the current mod, not any proposed combat
feature. No build or combat tests were run for this documentation-only batch.

## Alternatives and boundaries

| Idea | Recommendation and reason |
| --- | --- |
| A separate jaw/Attack gene now | Use current genes first. There is already enough visible body, temperament and regional variation, while another locus expands every genome/readout/save contract. Revisit only if real breeding choices are too shallow. |
| Random obedience failures or a decaying loyalty bar | Use deterministic temperament and a stable bond. The player needs to understand why the animal acted and trust an emergency command. |
| Combat levels that overpower genes | Keep progression genetic. Experience could later supply a cosmetic history, never a mandatory kill grind or inherited training. |
| Automatic resurrection/full heal | Use incapacity, rescue and paid recovery. The battle loses a member and the next expedition needs preparation. |
| Mandatory bleed-out timer | Omit it. An invisible countdown encourages panic and makes continuing the player's own fight feel like abandoning a pet. Terrain and aftercare provide the consequences. |
| Every coat gets an attack spell | Start with existing benefits. Fireproof, poison-proof, armored and visually self-lit animals already feel different; retain ordinary cosmetic choices. |
| Unlimited commanded army | Start with three active fighters. Preserve breeding and household freedom while leaving the keeper a role in combat. |
| PvP immediately | Defer. Friendly teams, explicit participation, damage credit and recovery need a dedicated duel ruleset; the first party must be safe in co-op. |
| Armor as a prerequisite | Defer fitted armor. A common adult must help before another gear grind. Optional later protection should use a simple slot, visible fit and a genuine mobility/bulk tradeoff, preserving the approved model. |
| Practice/sparring unlocks stats | Practice can teach the player and show moves without risking a long-bred line, but must not be required for stats or command reliability. Use existing venues where practical. |

The largest tuning questions are how fragile the common adult feels, how long
recovery interrupts an outing, how much feral damage compensates for its weaker
keeper initiative, whether skittish warning/skirmishing earns a party slot, and
whether three companions complement rather than replace the player. Resolve
those through the scenarios before expanding the ability list.
