# H12/H13 — trained trail companions and provisions

Implemented 2026-10-20; integrated with B1/B2 and the initial B3 combat party
2026-10-21. See [COMBAT_SPECS.md](COMBAT_SPECS.md) for fighting, rescue, party
limits and the changed Whistle gestures. This document describes current trail
behavior; older protocol/test counts are historical checkpoints.

## Training and ordinary travelling

Craft a reusable Trail Whistle from copper, two string/Sinew and a
feather/Whisker. Empty-hand care offers **Train trail partner** using a real
Whistle in inventory and one actual plain guava. Crouch-Whistle on an untrained
animal also opens care. All four adult temperaments can bond. Training needs
normal build permission, calm and at least half health. Pups, Apex, courting,
fighting and another keeper's animals refuse. Creative retains the required
food. A bowl still toggles provisions after bonding.

Training records the keeper UUID, starts Follow and persists the animal.
Genes, bred points, parentage, health, home claims and work preferences stay.
An existing partner advancement records housing/training without resetting
progress. Local named Follow, Wait, Return home/Resume roaming, provisions and
Release training operations need no held Whistle after bonding. Inspecting the
native view changes no animal or stock. Other players can provide ordinary care,
use existing leads/baskets and rescue, but cannot overwrite a bond or give
owner-only orders. Release is unavailable during injury/recent party combat.

**Training alone never enables fighting.** Deliberately join the combat party
through orders to opt in. The three slots, directed-order choice and deliberate
quiet-time removal are separate from ownership and ordinary carrying.

## Current Whistle controls

- Normal use at air, terrain or an entity recalls heard Follow companions and
  selected Wait party members, even during combat. Clears owned/refuge targets,
  starts regrouping and cannot be canceled by another hit. Ordinary residents
  and unrelated parked Wait animals stay. Native chest/villager interaction is
  intercepted so it cannot swallow an emergency call.
- Crouch-use on an animal opens its care/orders. Crouched aimed hostile use
  tries a single Focus task for the party's chosen member. Otherwise crouch
  nearby opens local orders; away from nearby owned animals it opens Combat
  party with remembered last sightings. No Whistle modifier releases training.
- Commands require a physically heard, same-level, loaded companion; there is
  no remote chunk ticket, cross-dimension pull or teleport. Normal recall has
  no item cooldown. Only its flute sound is throttled for forty ticks.
- Calls consider up to sixteen owned companions, prioritizing party members,
  with `clamp(16 × expressed ear scale + 0.2 × max(0, Senses), 8, 32)` hearing.
  Larger ears/points help; small ears remain deterministic.

## Walking, waiting and residence

Following uses native safe dry loaded paths near the living nonspectator keeper
within 64 blocks. Party members prefer separate rear/side places and respect
actual body width; unselected carriers retain ordinary following. Path hazards
and missing support interrupt progress. Legs, ears, defenses and excess-cargo
slowdown continue to matter. Follow never teleports or authorizes a dangerous
water route. Leads, riding and other native transport retain priority.

Wait parks an ordinary unselected animal. Wait + Assist gives a selected
fighter a saved local anchor with a four-block defense area and bounded return.
A heard normal Whistle recalls that held party member. Danger/withdrawal yields
the position. Transport/basket release preserves the order but a new basket
landing establishes a new local anchor instead of walking to old coordinates.

Orders suppress competing home chores while preserving preferences, clocks and
the Nest claim. The combat controller arbitrates panic/refuge/retaliation for
bonded ordered animals so these cannot cancel recall. Selected members stop
pursuit and Wait when the keeper is lost/dead/spectating/too far/in another
level. Unselected followers keep the prior 200-loaded-tick fallback to residence.
Wait remains parked. New courtship suspends ordinary trail behavior; injury
can travel while fighting, breeding and work remain paused.

Phase legs retain a real defensive blink and twenty-second rest after a
nonlethal attack. While ordered with a living keeper within 64 blocks, the old
Nest does not clip that escape. Native landing/protection/transport checks and
damage remain. A selected expedition fighter regroups afterward; ordinary
residents retain the old home boundary.

## H13: opt-in provisions

Use a vanilla bowl or the named care button to enable/disable provisions for
your bonded animal. The bowl stays. Training defaults off; another keeper cannot
change it. Follow and Wait can eat from actual equipment. Resident mode uses
home food systems; calling home retains the preference, releasing clears it.

An injured calm adult on supported dry ground can eat one suitable ordinary
food from its own bundles for `max(2 HP, 25% maximum HP)`, sharing the normal
600-tick automatic meal rest. Transport, passengers, water/lava, fire, courtship,
danger, downed state and recent attacks pause the routine. Upgraded guavas,
nested containers, unrelated items and malformed contents stay. The current
expressed Diet applies; there is no outside inventory or free terrain harvest.

The last equipped bag is searched first, with the first directly contained
suitable food paying. Empty bags stay equipped. Names/colors/components, item
order and other contents stay; normal content changes clear the native selected
item index. Cargo and meal rest are reserved before healing callbacks, with
an eating guard preventing reentry. Replaced/dropped equipment is not overwritten;
healing cancellation retains ordinary real meal cost/rest. Eligible unsuccessful
searches are bounded to once per 100 ticks. Healthy/resting animals do not rummage.

Paid direct calm hand care remains available and does not clear or wait on that
automatic meal rest. It spends actual stock, uses the same proportional healing
amount and has a callback guard. First aid is a separate downed rescue action.

## Saves, inspection and evidence

`TrailKeeper`, `TrailOrder` and optional `TrailProvisions` remain the saved care
fields. Invalid/missing keeper means ordinary untrained residence; unknown
orders mean Resident; absent provisions means off. Whole-animal baskets retain
care state while releasing the previous Nest. Walking under orders preserves
that claim. New combat fields/global party data are described in COMBAT_SPECS.
Nothing enters seed/sample/pedigree formats or a newborn's inherited state.

Sneak-Calipers reports keeper/order, provision preference and actual combat
intent without changing them. The genetic sheet describes adult potential from
the actual genome/points. **A Partner for the Path** retains its native one-time
25 XP reward. Protocol **18** requires updated client/server.

Java 25 build/model and all **116** native scenarios pass at the integration
checkpoint, including actual walking/braking, heard recall, terrain changes,
Phase/home preservation, all-temperament bonding, fighting/held defense, party
conservation and ordinary provisions/callback cases. Four native combat-panel
snapshots and actual-sculpt poses are inspected as substitute-font/headless
previews. Natural journeys, client controls/GPU and co-op balance remain play.
