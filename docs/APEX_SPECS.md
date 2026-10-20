# Apex — the culmination of a breeding line

The five legendary stat drops now fund a deliberate boss encounter. A Tissue
Sample supplies the look and original bred points; its living donor is never
changed or consumed. This completes the endgame idea in DESIGN.md without
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
and natural encounter difficulty remain playtests; protocol **17** is current.

## Encounter

The Apex retains all seven source gene pairs, coat/body proportions and expressed
environmental adaptations. The existing approved creature mesh is reused with
native Scale 1.8. Source points are retained separately from bounded boss
attributes: health 180–720, speed .25–.42, jump .4–.8, damage 8–15 and awareness
24–64. Build, legs, ears, Vitality, Swiftness, Leap and Senses affect these values.
Bloom remains an adult breeding stat, with no boss growth effect. A3 also
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

Existing genome, animal saves, pedestal contents, model assets and protocol 6
remain compatible. The new entity uses existing genome sync and native boss
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
