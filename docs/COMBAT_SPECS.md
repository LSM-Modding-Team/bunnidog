# Combat companions — B1, B2 and the first B3 party

Implemented 2026-10-21. This describes the actual behavior; the broader
[design plan](COMBAT_COMPANION_PLAN.md) and [player scenarios](COMBAT_SCENARIOS.md)
also include future work. Values below are prototype tuning, with native
server evidence rather than natural Survival/client balance evidence.

## A partner to take adventuring

Train a healthy adult through its care view with a reusable Trail Whistle and
one actual plain guava. All four expressed temperaments can bond. Then use
**Orders & combat → Join combat party**. Training alone keeps offense off;
ordinary carriers and existing saved followers are not silently recruited.
Zero-point adults work, with no combat XP, obedience grind or new genetic locus.

| Action | Actual result |
| --- | --- |
| Normal Whistle use, including at a block or entity | Recall heard Follow companions and selected held party members. Clears combat/refuge targets immediately and begins regrouping, even after damage. Ordinary residents and unrelated parked Wait animals stay. |
| Crouch + Whistle aimed at a hostile | Server view trace gives the chosen partner a single Focus task if target, health, hearing, position and terrain allow it. Refusal explains the reason. The previous Assist/Stay safe policy is retained. |
| Crouch + Whistle on a bunnidog or near an owned partner | Open local orders; untrained animals expose care/training. No modifier releases a bond. |
| Crouch + Whistle away from a nearby partner | Open Combat party, including remembered last sightings. |
| Direct this partner / party recipient button | Change which one of the selected three receives Focus. It does not recruit another fighter or change that animal's Assist policy. |
| Wait with Assist enabled | Hold a small area around the chosen place. Targets must remain within four blocks of the anchor and within the keeper leash. Recall, danger or withdrawal yields the position. |
| Stay safe | Stop fighting and regroup; membership is retained. Follow/Wait/Resident, provisions and bond release remain named operations. |
| Leave combat party / Remove partner | Remove a slot after disengaging and the shared recent-combat reservation ends. Packed or away members can be removed through the party view. Bond, injury and equipment stay. |

Recall is unconditional when the physical command is valid; there is no
obedience roll or item cooldown locking it out after Focus. Only its sound has
a 40-tick throttle. Calls affect up to sixteen loaded owned companions, with
selected party members considered first. Hearing is
`clamp(16 × expressed ear scale + 0.2 × max(0, Senses), 8, 32)` blocks.
No command loads distant chunks, crosses dimensions or teleports an animal.

The server intercepts Whistle block/entity interactions before chest/villager
interfaces. Crouched use on a bunnidog deliberately opens that animal's orders;
ordinary use at it still recalls. Returning to residence suspends expedition
combat while retaining the chosen membership and existing home assignment.

## Command conversation handoff (2026-10-22)

A committed local command closes its actual conversation so following,
regrouping and fighting can begin. Failure leaves the choice open; provision/work
settings and cargo remain open. Native booleans determine success, and closing
never dismisses a different menu installed by a callback. A normal-AI regression
uses actual menu training, Wait, then Follow and observes the real companion walk.

Bond release first reviews its concrete consequence, then uses a separate
confirmed mutation. Back preserves the keeper. Quiet-time, ownership, range,
party and stale-generation checks still apply at the final operation; this adds
no command cost or new genetic gate. Party settings retain their own original
membership/removal semantics. The damage, injury, rescue and whistle rules below
are unchanged.

## Three deliberate fighters

One world-level SavedData roster reserves up to three UUIDs per keeper, shared
across dimensions, basket travel and unloaded animals. Capturing or downing one
does not free a battle slot. Actual targets, damage and the keeper's recent
combat refresh a shared five-second reservation; native attack memory can keep
it active longer. Recall and genuinely quiet time make changes available again.
An unseen hostile's mere existence does not reserve a slot forever.

The latest explicit Join/Assist choice becomes the directed-order recipient;
the party view can change it independently. One Focus commands one member.
Others keep their current policy. Automatically chosen targets prefer uncovered
actual threats and admit at most two automatic partners against one victim.
An explicit Focus can add a third; native hurt invulnerability still applies.
This is a bounded prototype, not a promise of perfect co-op formations.

Following members prefer separate rear/side places near the keeper, using
actual body bounds and safe loaded paths. Ordinary carriers retain their existing
following behavior. A held partner returns to its local anchor when free; a
displacement beyond eight blocks abandons that anchor and regroups. A basket
release clears only the old position's anchor, establishing Wait at the new
place. Injury, orders, bond and party membership are preserved.

Nearby sightings remember name, dimension and block position when selected and
near a present keeper, at most every five seconds or on joining. Away/packed
entries display that saved location, explicitly labeled as last seen. The party
view never searches remote chunks for live coordinates. Keeper death, absence,
spectator state, excessive distance or a dimension change stops pursuit and
leaves selected partners waiting locally. Ordinary unselected followers retain
their prior 200-loaded-tick fallback to residence.

## Inherited fighting differences

| Temperament | Automatic Assist behavior | Base bite bias / withdrawal |
| --- | --- | --- |
| Loyal | Own attackers, the keeper's attacked target and nearby actual keeper attackers | ×1.0 / 40% health |
| Stoic | Similar protection; ambient keeper threats must be within six blocks. Held area remains bounded. | ×1.0 / 35% |
| Feral | Own attackers and the keeper's deliberate attacks; less automatic keeper-watch coverage. Focus is useful. | ×1.2 / 25% |
| Skittish | Warns about visible threats; does not automatically charge for the keeper. Self-defense or Focus lands one successful bite, then withdraws. | ×0.9 / 55% |

Warnings use visible loaded enemies, never through-wall detection. A selected
skittish lookout checks within its hearing, capped at twenty blocks. All ordered
bonded followers can recognize a visible primed Creeper within six blocks and
withdraw. Notifications share a keeper throttle; an urgent fuse can upgrade a
recent ordinary alert. A short bell, overlay and ear/look pose provide the cue;
there is no permanent alert chatter.

Adult base bite is `2 × Build × temperament × Vitality fitness × sqrt(Leap multiplier)` HP. Slim/lean
use ×0.95/×0.9 and 20 ticks; average uses ×1 and 20 ticks; stocky/woolly use ×1.1
and a 24-tick starting interval; chonker uses ×1.2 and 28 ticks.
Actual interval divides that starting value by expressed leg speed and the square
root of bred Swiftness, bounded 10–36 ticks. Wings allow aerial Follow/combat,
with Wait/residence landing and fall protection; wing art is explicitly deferred. Positive Vitality fitness is
`1 + 0.2 × sqrt(points / 20)`; negative fitness is `max(0.65, 1 + 0.02 × points)`.
At extreme points, base bite respects the native 2048 attack-attribute limit,
so readouts agree with actual native attributes. Swiftness improves cadence within the native half-second hurt window. Every ordinary attack caller,
including hunting/home guards, shares the saved actual bite delay. Native armor,
effects, shields and hurt immunity still decide the hit. The Apex retains its
separate attributes and combat behavior.

Existing health, speed, terrain, coat protections and Phase defense remain
genetic. Phase escape by an ordered selected partner regroups instead of
immediately charging back. New pounces, braced impacts, armor equipment and
combat-specific breeding goals are future work. Current care orders show actual
style, base bite, interval, intent, health and recovery; Calipers/sample/parent
sheet hovers use the actual recorded genome and points. No universal combat
score or estimate made from averaged nonlinear stats is introduced.

## Target and movement boundaries

Offense requires a living nonspectator keeper in the same level within 64
blocks, an ordinary adult, an explicit roster slot and Assist or accepted Focus.
Courting, injury, transport and residence suspend offense. The practical pursuit
leash is `clamp(hearing − 3, 5, 20)` from the keeper, with a ten-second task cap.
Targets must be visible native hostile mobs. Players, pets, bunnidogs, allies,
named/leashed/passenger/vehicle mobs, Creepers, Wardens, Withers and Dragons are
excluded. Focus refuses an unprovoked neutral hostile.

The fighting goal owns movement ahead of ordinary following and home chores;
bonded retaliation cannot overwrite its target or cancel recall. Routes use
real safe dry native walking; a brief path interruption gets a bounded retry,
then a refusal and regroup. Locked doors, holes and unsafe terrain remain real.
Fire/water danger or the genetic health threshold triggers retreat. Re-entry
requires a higher health margin, proximity to the keeper and quiet safe time,
or an explicit accepted Assist/Focus. Incoming damage never cancels regrouping.

## Defeat, extraction and aftercare

A bonded adult defeated by ordinary combat collapses at one HP instead of
dying. This also covers non-party carriers, combat explosions, Wither/magic and
recent residual burning. Collapse removes offensive targets, breeding, work,
Wither/poison/fire and projectile/push targeting; it produces no death loot or
XP and does not release the Nest. Repeated combat hits cannot use it as a shield
or kill it while downed. There is no hidden bleed-out timer.

Terrain remains real: void/bypass damage, falls, contact lava/fire, drowning,
suffocation, freezing, cactus, magma, cramming and the border retain native death.
Unbonded animals, pups and Apex retain ordinary death. Routine player melee and
projectiles cannot injure an ordinary bonded animal; friendly projectiles pass
through it, including another player's pet without scoreboard setup. Player TNT
still causes combat damage and may create genuine terrain hazards.

Spend one actual plain guava on first aid on supported dry ground, or capture
the downed animal with an empty Travel Basket for extraction. Only downed rescue
bypasses the basket's recent-combat refusal. Actual whole-animal saves conserve
UUID, genes, points, parents, name, equipment, bond and injury. Care by another
player does not transfer ownership. Existing build/protection gates still apply.

After first aid the animal can travel and carry, with fighting, breeding and
work paused. Recovery advances only during safe loaded time, including quiet
walking: five minutes at zero Bloom, shortened by its growth multiplier with a
two-minute floor, and at least half health. No home/hospital is mandatory.
Unloading/capture does not advance or reset recovery. A second combat defeat
collapses into the same injury cycle, preserving progress and spending real
first aid again; it does not silently enable permanent death after rescue.

Real calm meals heal `max(2 HP, 25% maximum health)`. Automatic Trough/bundle
meals retain their shared 30-second rest, reserved before healing callbacks.
Paid direct hand care retains its previous behavior: it requires calm, spends
real food and neither clears nor waits on that automatic meal rest. A reentrant
hand-care lock prevents callback spending/healing twice. First aid pays once
and reserves automatic meal rest. Zero-heal timer reservations remain zero.

Only a local participating keeper receives native responsible-player credit:
a recent attack, received enemy attack or the accepted current Focus. Mere
ownership/old Assist is insufficient. Normal enemy loot/XP runs once; the
keeper's Looting/Fortune is not copied and no synthetic advancement kill fires.

## Save and delivery boundaries

The seven-gene allele order and five-stat/sample/seed formats stay unchanged.
New care fields (`CombatAssist`, injury/downed/recovery/danger/regroup, warning
rest, bite wait and local hold position) default safely for legacy animals.
Targets/Focus/temporary alerts are not serialized or inherited. The global roster
stores members, command choice, shared reservation and remembered sightings.
It also reads the initial single-fighter prototype format. Protocol **18**
requires matching updated client/server because combat pose state is synced.

Native tests cover actual walking/bites, recall during combat and at a chest,
allied arrows continuing to an enemy, genetic attributes, four temperaments,
Focus and lookout/fuse behavior, injury/capture/first aid/recovery, held defense,
party spacing/distribution, roster/save conservation and stale party buttons.
The recovery test fast-forwards the long wait before checking actual safe ticks;
it does not establish natural pacing. Headless model checks and actual-sculpt /
native-panel previews cover transforms/layout separately. No client/GPU session,
natural encounter balance, keyboard comfort or six-animal co-op claim is made.

The maintained Gradle breeding/layout checks include expressed/hidden combat
potential, extreme signed points, native damage/cadence bounds, recovery floors
and proportional meals. The older standalone `tools/GenomeCheck.java` fails on
unchanged Island allele-count/trend assumptions before its later checks; that
legacy tool is not a passing validation claim for this batch.
