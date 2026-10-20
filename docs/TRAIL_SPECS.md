# H12/H13 — trained trail companions and provisions

Implemented 2026-10-20. The original concrete proposals are
implemented. This extension uses the standing design latitude to connect
existing cargo, ore scouting and cave guidance to ordinary exploration with a
living bred animal. It adds deliberate training, rather than changing wild
spawns, native leads or the existing home/care systems.

## Rules

- Craft a Trail Whistle from copper, Sinew and a Whisker. It is reusable and
  uses normal interaction permissions. Train a calm, at-least-half-health
  ordinary adult expressing loyal or stoic temperament with the whistle and
  one plain guava in the other hand. Creative keeps the required guava.
- Training records the keeper UUID and starts Follow. A pup, Apex, frightened,
  fighting or courting animal refuses. Hidden loyal/stoic copies do not help.
  Training does not rewrite genes, bred stats, parents, health, work preferences
  or an existing Nest claim. There is no inherited taming or new diet need.
- The keeper uses the whistle on that companion to switch Follow/Wait. Wait
  means holding its current safe position, subject to escape/swimming/combat.
  Sneak-use on the animal releases the training and restores resident behavior.
  Other players can still use existing care tools, leads and baskets; they
  cannot use this whistle to overwrite the keeper or command the companion.
- Air use calls nearby loaded trained partners to Follow. Sneak-air use sends
  them back to resident/home behavior while retaining training. Calls are
  bounded, audible, subject to hearing range, and have a short native item
  cooldown. No remote lookup, chunk ticket, cross-dimension pull or teleport.
- Following walks near the actual living keeper on safe loaded dry ground.
  Current native path hazards and lost support interrupt a route. Breeding
  legs, ears, defenses and cargo continue to affect the actual animal. The
  first version retains water avoidance; swimming traits do not authorize
  unsafe routes. A led/ridden/carried animal yields to native transport.
  Follow itself never teleports. An expressed Phase partner can still make its
  normal defensive blink and twenty-second recovery after a real attack: while trained
  and ordered with a living keeper within 64 blocks, its preserved old Nest no
  longer clips that escape. Full native landing/protection/transport checks and
  damage remain; ordinary residents keep their existing home boundary.
- Follow/Wait suppress competing home chores while ordered, preserving their
  preferences, clocks and claim. Panic/refuge remain able to interrupt. New
  courting or unsuitable temperament returns the animal to resident behavior.
  Follow returns to resident behavior after 200 loaded ticks without a living
  nearby keeper; Wait remains a deliberate parked order until changed.
  Commands never force combat or immunity.
- Optional keeper/order data saves on the animal and travels in whole-animal
  baskets. Missing or invalid legacy data means ordinary resident behavior.
  Seeds, samples, pedigrees and newly born animals do not inherit training.
  Sneak-Calipers reports training/order without changing it.

## H13: opt-in provisions

Use a vanilla bowl on your trained partner to enable/disable provisions, with
either hand and normal build permission. The bowl stays. Training defaults to
disabled; other keepers cannot change it. This preference applies to Follow
and Wait. Resident mode uses the existing home food systems instead. Releasing
training clears the preference; calling a partner home retains it for later.

An injured calm adult on safe dry ground can eat one suitable ordinary food
from its own carried bundles for two HP, sharing the normal 600-tick meal
recovery. Transport, passengers, water/lava, fire, courtship, danger and recent
attacks pause the routine. There is no terrain harvest or inventory search
outside the animal's own equipment. Manual feeding remains available.

The last equipped bag is searched first; the first directly contained suitable
ordinary food pays. Current expressed Diet applies. Upgraded guavas, unrelated
items, nested containers and unsupported malformed bundle contents stay. Empty
bags remain equipped. Named/colored bundles, remaining item order and complete
components stay intact. Eating updates native BundleContents, clearing its
temporary selected-item index as a normal content change does.

Cargo is reserved before native healing callbacks. Shared food recovery also
starts before those callbacks, and a transient eating guard prevents reentry.
A callback can drop/replace equipment without restoring the paid food or
overwriting replacement cargo. Native healing cancellation keeps ordinary meal
cost/recovery semantics. Eligible unsuccessful searches are bounded to once
per 100 ticks; healthy/resting animals do not rummage through the bags.

## Save and inspection details

`TrailKeeper` is the UUID string, `TrailOrder` a stable resident/follow/wait key
and optional `TrailProvisions` an opt-in boolean. Invalid/missing keeper data
means no training; missing/unknown orders mean resident, and missing provision
data means disabled. Care state travels in the existing whole-animal basket.
Baskets retain their existing home-release behavior; walking under trail orders
preserves the claim. No seed/sample/pedigree format or allele ID changes.

Whistle calls have 40 ticks of native item cooldown, hear at most sixteen
loaded partners, and use `clamp(16 × expressed ear scale + 0.2 × nonnegative
Senses, 8, 32)` blocks of hearing. Follow walks near the keeper and can make
bounded safe partial-path progress; it does not teleport to catch up. A follower
keeps its order for 200 loaded ticks without a living nonspectator keeper in
the same level within 64 blocks, then resumes residence. Wait stays parked;
panic and transport can relocate it. None of these actions claims passing Nests.

Sneak-Calipers reports keeper relation, order and provision preference, with
home food/work paused under active orders. The separate work-pause preference
is retained. **A Partner for the Path** awards native once-per-player 25 XP
after successful paid training. Native client/server content protocol **15**
requires updated versions on both sides.

## Evidence

Java 25 build and all **100** native cases pass at the combined checkpoint.
Native training covers real held-item payment, age/temperament/permission
refusals, owner-only controls, read-only inspection and native save/basket
conservation. A real AI goal walks beyond a retained Nest boundary, waits after
native braking, respects heard calls/cooldown, notices a changed damaging floor,
yields to leads and resumes residence after losing its keeper. An actual attack
also proves that a trained Phase partner retains damage/recovery and can escape
beyond the old home boundary without relinquishing its Nest.

The provision case covers disabled/healthy/transport/other-keeper refusal,
actual named native bundle contents, Diet/upgraded-food protection, two-HP
healing and shared recovery before reentrant heal callbacks. Legacy defaults,
saved baskets, exact remaining cargo, dropped old bags and callback refills
conserve food and components. The model gate passed after the Phase integration;
no creature mesh/render pass changed. Item/control and guide previews are
inspected separately. Natural journeys, multiplayer feel, native UI fonts and
GPU appearance remain gameplay review.
