# Apex — lineage encounters and combat roles

The ground Brute and four new roles, plus flying variants of all five, are
implemented. The accepted **+60% outgoing damage** applies after the original
bounds, with **health unchanged** (180–720). Current protocol is **24**.
The Great Warren dimension, expanded equipment/loot economy and Crowned Apex
final dungeon are still future work in [APEX_EXPANSION_PLAN.md](APEX_EXPANSION_PLAN.md).

## Choosing an encounter

The original source-only Crucible ritual still summons `bunnidogs:apex`. Craft a
role sigil from paper, copper and its ingredient: Amethyst Shard (Prismmaw), Spider
Eye (Broodcaller), Gunpowder (Blastmaw), or Ghast Tear (Lifebinder). Add a Feather
to the same recipe for a flying version; the flying Brute uses Iron Ingot.
The nine recipes unlock normally, and sigils appear in the creative tab.

Use the actual sigil on a Crucible with a readable Tissue Sample in the other
hand. The preview names the chosen type and keeps the existing separate summon
confirmation. Ready previews omit repeated setup/ring instructions; requested
ring markers remain available. Survival pays **one sigil, one actual sample and five legendary
offerings** only after successful native spawning/revalidation. Creative retains
its supplies. No sigil means the original ground Brute; a lost selected sigil
invalidates its menu, and focused previews never substitute another inventory
sample. Actual stock/components, callback cancellation/reentry and protected
build checks remain authoritative. Replacement menus opened in callbacks survive.

| Role / entity ID | Behavior |
|---|---|
| `apex` | Existing warned stomp/pounce and melee |
| `prismmaw` | 1.5-second aim warning; three thick mouth-beam pulses over the next 1.5 seconds, solid cover blocks them; tracking slows during firing |
| `broodcaller` | Warned call of up to four hostile minions; six alive per caller and twelve locally; finite 30-second life, ending with the caller |
| `blastmaw` | Three-second fuse/radius tell, native power-10 explosion with 1.6× victim damage; other Apexes/minions receive neither blast damage nor knockback |
| `lifebinder` | Colliding hook, half-second movement stagger and shared eight-second hook recovery; four armor-bypassing drain pulses, finite healing for the caster and nearby Apex/brood allies |

Prefix any ID with `flying_` for its flying variant, including `flying_apex`.
All are separately registered entity types with the same source/genetics/health
rules and approved mesh. Flying types use collision-checked low flight, loaded
terrain height limits, visible lift particles and regular ground recovery.
Flying Brutes dive; the other roles retain their warned abilities in the air.
This is bounded direct flight with ground recovery, not a complete aerial maze
pathfinder. Actual terrain/flight feel remains client playtesting.

Melee is now **12.8–24** after genetic/enrage bounds; stomp uses 75% of that.
Prism pulses use `10 × melee / 12.8`, at most three per cast. Ordinary ray damage
honors armor/shields. Lifebinder pulses deal 2.4 raw damage with a dedicated
armor-bypassing damage type; other protections, cancellation and hurt rules remain.
Only actual accepted health/absorption loss supplies vitality. The pool starts at
eight, caps at twelve, and spends at most one health per half-second on a visible
wounded family ally. Cancelled healing spends no resource; cancelled damage earns
none. Hook victims retain aim, shielding and items during the brief movement stun.
Hooks hit walls, can miss/be shielded, expire, and never pursue unloaded chunks.

Blastmaw uses native explosion exposure and events. Ritual explosions preserve
terrain. Native start cancellation pays no backlash. A resolved blast charges an
armor-bypassing backlash of up to 15% max health, capped below lethal damage;
normal native damage/protection hooks still apply. It cannot arm below 16% health.
The repeat interval is fourteen seconds from charge start. Other Apex roles have
six/eight/twelve-second recovery budgets. No natural Apex population is added.

Temporary brood are dedicated `apex_minion` creatures, with eight health and 4.8
melee damage. They preserve the caller's visual genome, cannot become pets,
breed or supply normal material/XP/Crest drops. Minions expire or disappear when
the caller is removed or too far away. They are not extra full-strength Apexes.

All role attacks have server-owned geometry and outcomes. A bounded pulse packet
lets clients draw beams locally. Remaining cooldown, landing recovery and support
vitality save; active channels/hooks cancel on reload instead of replaying damage.
Variants drop an exact source-bearing Crest named for their type. Existing ground
Brute Crests and all genome/Tissue formats remain compatible.

## Native evidence

Java 25 build, localization and the existing headless model check pass. The suite
has **137 native scenarios**, including five new actual role/focus cases covering
all ten factories/saves, increased damage bounds, real flight/landing control,
warned/blocked beams, minion caps/cleanup/drop suppression, real power-10 blasts
and cancellation, cross-role/flying immunity, self-cost, true drain, cancelled
income, healing conservation, physical hooks/shared recovery and exact focused
spawn costs/callback replacement. The arena declares its full 64×64 chunk bounds.
The original 132 cases still pass. Sprite and native menu previews are separate
from actual client PvE balance, flight and multiplayer/performance review.

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
