# Observed deliveries and personal route memory

This continuation implements a concrete unfinished I2 journey: an animal watches
an actual player delivery, learns the source/item/destination, and repeats it by
walking a real parcel between those stores. Existing generic delivery lessons,
Hearth jobs, hereditary limits and safety/recall behavior remain intact.

## Learn through actual play

In Skills and roles → Lessons, **Watch a delivery** closes the conversation and
lets a healthy eligible adult accompany the player for up to two minutes. It
requires Senses +20 and at least supported Delivery capacity, with normal
ownership, day/safety, work pause, transport and explicit-order checks. There is
no new training item, permanent HUD or route-configuration screen.

Open a nearby supported container, move one kind of item into your inventory,
close it, then deposit that same item into another supported container. The
learner must remain nearby with sight of the player and the interacted storage.
The native menu must actually belong to that block entity. A pickup alone,
opening/closing without moving stock, an occluded operation, an inventory swap,
a different item or returning items to the same store does not invent a route.

The observer compares complete before/after storage and player inventory/cursor
balances at the actual native open/close events. It accepts one exact component-
preserving resource change with the matching counterparty change. This is a
conservative balanced-gesture observation, not a claim of per-click transaction
provenance across arbitrary third-party automation. Ambiguous changes are ignored.

Chests (including both halves), barrels, Hearth stores and main Trough storage
are supported. Ender storage, arbitrary machines, private crafting outputs and
nursery seed use are deliberately outside this first delivery adapter. Locked
containers are not courier endpoints. The two stops must be in the same dimension,
at most 32 blocks apart and physically distinct inventories. One personal route
is remembered; an incomplete replacement demonstration leaves the old route.

Real observed work earns ordinary Delivery practice. Genetically independent
animals can work alone once that skill is mastered; supported animals still need
their authorized guide nearby. Knowledge/coordinates never become DNA. Existing
Hearth residents retain their owner authority; an unrelated visitor cannot use
lessons or companion training to take over another household's courier.

## A parcel really travels

The route records exact item components and a batch of up to eight, also bounded
by the item's native stack size. At pickup, the destination is checked for room
before extracting. Ordinary food keeps an eight-item source reserve. Only the
explicitly demonstrated item matches: other names, genes, potion components and
other keepsakes are not interchangeable stock.

The actor proves both stops reachable before taking a parcel, walks to the
pickup, performs its work, takes real items into a private saved cargo field,
walks to the destination and commits the real insertion. Chest halves use the
original containers' shared journals, not separate inventories. Trough filters,
manual Tending disable/redstone pause, locks, Hearth ownership and border checks
remain authoritative. Redstone can pause generic storage endpoints as well.
Pickup and deposit also honor the native mob-griefing protection event; a veto
keeps stock and the owned parcel intact for a later retry.
No distant chunk is generated or force-loaded to service a route.

Combat, injury, panic, night, recall, follow/wait orders, transport and deliberate
work pause can interrupt the job. The parcel stays owned and saved. A full target
or blocked path does not teleport goods or delete them. A confirmed delivery
provides practice and a short rest before another collection. Existing society
cargo and learned-route cargo cannot operate simultaneously; animal safety and
conversation goals retain movement priority.

If the pickup store changes after a parcel was taken, the already-owned shipment
can still reach its original valid destination. A later pickup remains blocked.
An actual death drops the exact remaining parcel once, alongside existing cargo.
Basket/entity saves retain the route, filter, owner, rest and in-flight goods.
The record is personal experience, not part of samples, breeding or captured
seed genetics.

## Authorization and deliberate recovery

Each demonstrated block entity receives a saved identity stamp only during an
explicit allowed observation. Both halves of double chests are recorded. Breaking,
replacing, splitting or merging a remembered store invalidates the old endpoint;
a newly placed chest cannot silently inherit authorization. No opaque provider
or copied virtual stock can become retrievable items.

The Lessons page shows the shipment name and one current state. **Show route**
briefly marks the two loaded stops in the world. Pause/resume preserves knowledge;
Forget removes an idle route. A carried parcel must first be delivered or explicitly
unloaded into the authorized player's inventory, with ordinary full-inventory
drops. This recovery pauses work and preserves exact components. Another player
cannot overwrite, forget or unload someone else's demonstrated route.

Ordinary blocked feedback occurs only on a change of problem near the route owner,
never as an always-on tutorial or repeated status spam. Detailed places are not
broadcast to unrelated players. Full route/filter/cargo data stays server-side
in native saves; the normal menu sends concise translated text, not a growing
route payload.

## Compatibility, scope and evidence

Protocol **37** requires matching clients/servers. Existing genome/seed allele
orders, cognition practice strings and old learned routines do not change.
Missing route fields mean no assigned route; legacy animals keep their existing
work. Transient player observation sessions are not restored after reconnect.
Existing Hearth-to-trough logistics remain distinct from this personal route.

Native coverage drives the actual animal-menu buttons and actual vanilla storage
quick-moves, then native unsupervised two-leg walking/delivery. It also checks
no-pickup/same-store/occluded demonstrations, exact names/components, ordinary food
reserves, double-chest identity and changes, inherited supported/limited minds,
owner authority, redstone/full destinations, native protection vetoes at both
ends, in-flight reload, and deliberate
parcel recovery. The full prior society/ritual/hunt/worldgen suites stay active.
First-person lesson discovery, natural pacing and long-lived mixed workloads
remain player review; native fixtures are not evidence of those.

Watched farming/nursery examples and learned gathering/scouting now ship in
[FIELD_LEARNING_SPECS.md](FIELD_LEARNING_SPECS.md). Remaining work includes the rest
of general shared procedures, longer or multi-stop courier programs, profession
coordination and broader autonomous civilization. Household care/tactics now ship
in [CARE_AND_TACTICS_SPECS.md](CARE_AND_TACTICS_SPECS.md). Resident route
apprenticeship and paid physical records now ship in
[COURIER_CULTURE_SPECS.md](COURIER_CULTURE_SPECS.md). They retain personal learning,
original authorization and actual walking parcels, without marking all I2–I10 complete.
