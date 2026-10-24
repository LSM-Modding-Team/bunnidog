# Apex — lineage encounters and combat roles

All five ground roles and their flying variants ship. Protocol **26** requires
matching clients/servers. The +60% outgoing baseline remains; the player's latest
combat review adds **20% Blastmaw health** and **double flying-Brute bite damage**.
The dimension, lavish new loot economy and Crowned Apex dungeon remain planned in
[APEX_EXPANSION_PLAN.md](APEX_EXPANSION_PLAN.md).

## Choosing an encounter

The source-only Crucible ritual still summons `bunnidogs:apex`. Craft a role
sigil from paper, copper and Amethyst Shard (Prismmaw), Spider Eye (Broodcaller),
Gunpowder (Blastmaw), or Ghast Tear (Lifebinder). Feather recipes select flying
versions; flying Brute uses Iron Ingot. Use the sigil with the actual source
sample in the other hand. The compact review names that exact type/source and
includes the Blastmaw/flying-Brute stat modifiers. Survival pays one sigil, one
sample and five legendary offerings only after a successful, revalidated spawn.
Creative keeps supplies. Lost/replaced sources/foci cannot substitute another
stack or create an unpaid boss. The existing native callback, permission and
menu-replacement protections remain.

| Role | Actual behavior |
| --- | --- |
| `apex` | Ground Brute keeps warned stomp/melee and chooses a destructive colliding jump arc to its target, up to 48 blocks within awareness/loaded bounds. |
| `prismmaw` | No melee. Holds 16–24 blocks on the ground. Thirty-tick aim warning, then sixty ticks of damaging beam (six pulses), followed by 160 ticks of recovery. Above 25% health the firing direction locks; below 25% it follows the target. Fixed beams keep their absolute world origin, even while the caster moves. |
| `broodcaller` | No melee. Holds 10–16 blocks. Thirty-tick call, then up to six elite brood, capped at ten per caller and twenty locally, followed by 320 ticks of recovery. Recalled pack members become thrown body projectiles or meals. |
| `blastmaw` | No melee. Rushes into a five-block fuse range, arms for thirty ticks, explodes and backs away for eighty ticks during its 280-tick recovery, shooting power-four fireballs. A lethal next self-cost starts a longer red power-twenty final blast. |
| `lifebinder` | No melee. Holds 10–16 blocks. Thrown anvils start the look-to-escape stun and queue a hook eight ticks after impact. Regular hooks retain their thirty-tick windup/160-tick recovery. Hooks remove one heart container; drains convert damage into seven-times healing. |

The base Apex health band is 180–720; Blastmaw is **216–864**. Normal Brute bites
are 12.8–24; flying-Brute bites are **25.6–48**. Enrage retains the existing bounded
speed/damage changes. Prefix each role ID with `flying_`, including `flying_apex`.

## Beams and support

Prismmaw beams travel thirty-two blocks **through blocks**, with a 1.25-block
collision expansion, larger dust and a visible broad particle cylinder. Each
pulse deals `12.75 × bounded attack / 12.8`, **15% less than the prior build**. Dedicated
beam damage bypasses armor and shields; native other protections/events still
apply. Warning geometry never damages. The damaging channel is twice its former
length; firing above 25% locks its direction and absolute world origin on the first damaging pulse. Moving the caster cannot move that beam. Reaching 25% activates tracking from the current mouth; that cast stays in tracking mode if healed.

Lifebinder hooks travel at four blocks/tick (formerly 1.5), with a two-block
projectile box, an actual 1.2-block swept entity-hit expansion and four-times-size full-bright lead renderer and a particle trail.
The swept outbound corridor breaks ordinary blocks. Pulling clears a corridor
for the victim's body as well. Unbreakable blocks, unloaded terrain, disabled
mob griefing and native protection vetoes stop the hook/tether. No forced chunks.
A facing shield still counters the hook; the brief stagger does not disable aim
or item use. Shared per-victim recovery prevents repeated chain-stuns.

Drain deals **3 raw HP** per pulse (+25%), bypassing armor. Accepted victim
health/absorption loss becomes **seven times that amount** of healing income: a full three-HP pulse can heal twenty-one HP. Income heals the caster first. Surplus alone enters a
finite vitality pool (initial eight, cap twelve), paying up to three HP every
half-second to another visible wounded family member within sixteen blocks.
Other Apexes take priority over brood. Cancelled damage funds nothing; cancelled
healing spends nothing. Self healing and allied healing cannot spend the same
converted income twice. A green beam identifies allied healing.

## Blastmaw terrain, fireballs and final blast

Normal fuse is thirty ticks / 1.5 seconds on the ground and fifty ticks / 2.5
seconds in the air. White flashing/swelling uses vanilla creeper curves. Normal
power-ten blasts use native exposure, drops/fire, mob griefing and explosion
start/detonate events, with 1.6× native victim damage and family Apex/brood immunity.
Self-cost is now **22.5% maximum health**, a 25% reduction from the former 30%.

If that cost would kill it, including damage received during an existing fuse,
Blastmaw commits to a **power-twenty final explosion**, flashes **red** and adds
another twenty ticks / one second to the fuse. Ground final fuse is fifty ticks;
aerial final fuse is seventy. A resolved final blast consumes the caster's
remaining life. Native cancellation pays no self-cost. Healing does not undo a
committed final blast. Its full forty-block blast area must already be loaded;
normal terrain/protection hooks remain authoritative.

During explosion recovery, power-four fireballs launch roughly every 55 ticks
at a valid target within thirty-two blocks. They use native fireball flight and
mob terrain policy. Their damage and knockback spare **all bunnidogs**, including
ordinary player companions; they cannot directly hit that family either.
Fireballs have bounded loaded-world lifetime and no material drops.

## Anvil stun and heart containers

Lifebinder throws a visible physical anvil (2.4-block/tick launch, broad swept
hit volume) at up to twenty-five blocks. Cover and shields can intercept it.
An accepted hit immobilizes walking/jumping while keeping aim/items usable and
queues a destructive hook after eight ticks. A small conditional bar reads
**Shake your view** / **Sacude la vista**. Actual accumulated crosshair rotation
fills it; cursor motion in menus does not. The server accepts at most eighteen
angular degrees per tick toward a 360-degree target: about one second of intense
shaking, slower for gentle movement. A single large/repeated stale packet cannot
skip the tick budget. The stun/progress survive ordinary saving/rejoining; death
or native effect removal clears it. No ambient tutorial layer is introduced.

Each successful hook removes **one full heart container (two maximum HP)**, to a
floor of one container. The world-owned debt ledger stores victims and caster
UUIDs across logout, dimensions and saves; ordinary attribute saves cannot erase
it. A successfully consumed native totem restores **three containers**. Death
restores all. Killing a marking Lifebinder clears all heart debt for its victims,
including offline victims; the next attribute sync restores their containers.
Restored containers increase maximum health, rather than granting free healing.
Other maximum-health modifiers are preserved. Shared hook recovery still applies.

## Continuous flight and brood genetics

Flying roles remain aerial without scheduled landings, including between casts.
Ranged roles orbit at roughly seven to nine blocks above the target, maintaining
horizontal spacing. Flying Blastmaw moves overhead to fuse and detonate in the
air. Only the flying Brute descends: orbit/ascent, one dive strike, then climb and
eighty-tick recovery before another dive. Dives now accelerate toward a 2.2-block/tick stoop and clear ordinary obstructing terrain through the same protected collision corridor. Collision avoidance rises around local
obstacles and stops at unloaded bounds. This is bounded steering, not a complete
maze pathfinder. Natural terrain/client flight feel still needs play review.

Elite `apex_minion` brood have **adult scale 1**, varied Build/Legs genes, feral
temperament and saved bred-point variation. Health derives from normal traits
and Vitality, scaled to 85% of the previous raw value and bounded 16–40; damage
5.5–12, movement .4–.65 and the existing fast gene/Swiftness-derived bite timing. Speed, bite cadence and damage are retained; wave size and frequency take the larger reductions requested in play review. Both grounded
and flying brood honor their actual bite interval rather than vanilla's fixed
one-second melee timer. They expire after thirty seconds or when the caller is
lost/too far away, and cannot breed, become pets or supply materials/XP/Crests.

Broodcallers recall an actual owned pack member, visibly hold it for fifteen
ticks, then throw it. The colliding body deals **40% of its remaining HP** as
native damage and strong knockback, allowing it to reach a three-block pillar.
The minion survives a throw and resumes fighting after landing or impact; it is
not copied into extra ammunition. Below half health, the caller alternates throws
with eating those actual minions: accepted healing is proportional to their
remaining HP (1:1), and the consumed creature is removed without drops. A damaged
caller retains ranged pressure. Cancelled healing does not consume the creature.
Recall/holds/throws expire and cancel safely on load or loss of the caller.

All Apexes now have a higher swim attribute, floating navigation and direct
loaded-world water propulsion/ascent, including aerial types escaping water.
Brute jumps choose both travel duration and arc height, clear their actual body
corridor using native grief/destruction vetoes, and collide rather than teleport.
Existing nearby stomp remains; elevated targets select a jump instead.

Flying Broodcallers summon airborne brood with inherited Wings alleles. Wings
is the appended eighth body locus: grounded id 0, dominant mutation winged id 1.
The canonical complete form is `g3.<eight body pairs>.<four mind pairs>`.
Six/seven-pair and g2 saves migrate to grounded/grounded without moving existing
alleles or changing minds. Body sync now uses two full 32-bit words; the third
word retains cognition. Existing cognitive locus IDs and saved splicer selections
retain their order; Wings is appended. Samples, seeds, names, books, forecasts,
splicing and registry qualification include Wings. Redstone deliberately retains
its original seven-body-pair comparison/range. Old body-mutation seeds retain
their seven original targets; new seeds include all twelve loci.

Ordinary winged companions can also fly while following a keeper or fighting;
Wait/resident routines land, and Wings prevent fall damage. Bite timing now combines
Build, Legs and bred Swiftness, bounded 10–36 ticks; Leap contributes to bite force
alongside Build, temperament and Vitality. Requested tool readings use these real
values. No automatic measurements/tutorial HUD return.

**Wing models and textures remain planned**, for both aerial Apexes and ordinary
winged animals. Approved body/face geometry is unchanged.

Cooldown, retreat and support vitality save; active casts/hooks cancel on reload.
Every variant retains its source-bearing Crest and localized type name.

## Validation

The maintained Java 25 build, localization, reader bounds, genetic compatibility
and model gates cover this revision. The native suite contains **152 scenarios**,
including all ten actual AI three-block-pillar fights, every role swimming, native final blasts/fireball family immunity, saved heart debt/real totem/death repayment, conditional stun escape, real thrown brood/consumption, exact backlash, penetrating
beams/shields, longer warning/channel, full-size varied elite brood, winged
pursuit, direct life steal/finite ally healing, destructive fast hooks, no specialist
bite and real aerial AI/physics past the former landing cycle. Existing splicer
selection IDs and redstone signals have compatibility regressions.
`art/apex_review_preview.png` shows equivalent normal/final fuse tints and the
conditional escape bar; `art/apex_fuse_preview.png` retains the initial fuse review.
The approved sculpt/atlas are used without new wing geometry; `art/wing_genetics_reader_preview.png` shows the eight-pair Spanish
reader and corrected complete-purity labels. Headless checks/previews do not establish client appearance or balance.

## Original ritual and encounter foundation

The five legendary stat drops now fund a deliberate boss encounter. A Tissue
Sample supplies the look and original bred points; its living donor is never
changed or consumed. This implements the original ritual idea in DESIGN.md without
allowing unbounded breeding points to break boss movement or health.

## Ritual

Craft an Apex Crucible: `OMO / ODO / OOO` (obsidian, Mutant Marrow, diamond).
Place five Trophy Pedestals at the same height around it. Valid ring slots are
three blocks north/east/south/west and two blocks along both axes diagonally.
Any five of the eight slots work. Display one of each: Titan Haunch, Gilded Foot,
Moon Sinew, Listening Shell and Everbloom. Extra duplicate displays are left alone.
Open the Crucible empty-handed or use a valid Tissue Sample to choose that
actual source. Read its forecast and offerings, choose **Review summon**, then
**Summon Apex** to start the hostile encounter. Opening and reviewing spend
nothing. One of each selected legend and one sample are paid only after the
native world accepts the boss; creative preserves them. Pedestals remain, and
excess counts in old stacked displays retain their complete components.

Require building permission, non-Peaceful difficulty, a loaded ring/spawn area,
dry collision-free space above the crucible and no living Apex within 48 blocks.
Refusal consumes nothing. No entity, terrain or container is cleared to make room.
The ritual has no ticking block entity, global search or automatic spawning.

## C3: preparation, forecast and committed cost

The live panel shows the chosen sample, bounded health, ordinary/enraged melee,
armor, knockback resistance, awareness, pursuit and pounce forecast. A five-item
checklist uses the same loaded eight-position scan as summoning. A held sample
chooses that exact inventory object; empty hands use the shown inventory source.
Calipers remain an inspection route. Repeated chat instructions are replaced by
this read-only view. Adventure can inspect; it cannot prepare a paid encounter.

**Show ring positions** returns to the world and briefly marks all eight loaded
Pedestal locations. Material changes disarm confirmation; old packets cannot
summon from replacement samples or offerings. No recipe or combat balance changes.

`ApexRitual` reserves the Crucible and selected displays against recursive calls.
After native spawn callbacks, it rechecks the actual sample ownership/components,
exact Pedestals/stocks, permissions, live target, collision, loading, difficulty
and competing boss. A cancelled or changed unpaid spawn is discarded. Selected
Pedestal journals extract one legend each in one root transaction, and sample
payment precedes commit notifications. Externally changed stock is preserved as
it is; the ritual never takes replacement items or refunds over another change.
The runtime guards are released on every exit and add no saved lock or inventory.

Native preparation cases cover read-only opening, separate confirmation,
Adventure, blocked space, changed qualifying displays/samples, spawn cancellation,
recursive entry, replaced stock/ownership, one-item costs from stacked displays,
exact source stats and creative keeps. The existing real survival encounter case
retains combat, capabilities, crest and save/load coverage. Actual client feel
and natural encounter difficulty remain playtests; protocol **24** is current.

## Encounter

The Apex retains the full current source genome (seven body and four mind pairs),
coat/body proportions and expressed
environmental adaptations. The existing approved creature mesh is reused with
native Scale 1.8. Source points are retained separately from bounded boss
attributes: health 180–720, speed .25–.42, jump .4–.8, damage 12.8–24 and awareness
24–64. Build, legs, ears, Vitality, Swiftness, Leap and Senses affect these values.
Bloom remains an adult breeding stat, with no boss growth effect. Cognitive
source genes remain recorded; current boss combat does not grant learned jobs. A3 also
inherits the source's slate armor and stocky/chonker knockback resistance,
adding to base armor 4/resistance .5. The glow coat remains self-lit.

The persistent hostile boss hunts players and retaliates, with melee plus a
short warned pounce at range or stomp nearby. Charging stops navigation, plays
a warning and emits particles before acting; attacks do not edit blocks. Stomp
damages visible survival/adventure players in four blocks and throws them back.
Below half health the boss bar turns red, movement/damage increase within the
same caps and charges become shorter. A native boss bar follows tracked players.
Saved remaining ability recovery prevents reloads resetting the attack interval.

An Apex never claims a Nest, breeds, eats, accepts care tools, takes a lead or
boards a vehicle. Calipers give its current combat health and explain its crest.
It does not naturally despawn; switching to Peaceful removes it as a hostile mob.

## Reward and compatibility

A credited player kill drops one unstackable Epic Apex Crest, preserving the
exact original Tissue payload. It opens the source line's sheet and can be
displayed on an ordinary Trophy Pedestal. The boss does not roll ordinary
bunnidog materials or refund the five legends. Mob drops game rule applies.
The first player victory grants the Apex of the Line advancement (200 XP);
the boss itself grants 50 XP under native kill-credit rules.

The original feature introduced protocol 6. Current matching builds use protocol
24; existing genome/animal saves, pedestal contents and model assets remain
compatible. The new entity uses existing genome sync and native boss
packets; the crest uses the existing tissue component.

## Evidence

Actual Gradle build and the native encounter case pass (fifteen mod scenarios
plus native sanity). It covers partial/blocked/active ritual refusals, successful
costs, exact source data and bounded attributes, immediate scaled dimensions,
care/transport/home refusal, enrage/health/recovery saves, warned stomp damage,
pounce motion, player-kill-only crest, advancement and native pedestal saves.
Headless model check passed during the batch; the approved mesh is unchanged.
Crucible/crest texture preview inspected (`art/apex_preview.png`).
Client encounter pacing, sounds, multiplayer and appearance need gameplay review.


P2 evidence: the actual build/server run passes all eighteen scenarios. Native
empty-hand and Calipers block interactions preserve all offerings and the sample
with no spawned entity; the later real boss matches forecast health, damage,
armor/resistance and enraged attack values. Existing partial/blocked/active
refusals, kill reward and persistence still pass. The expanded guide preview
was inspected; actual client message layout and hand use remain gameplay review.
