# Household courier culture

This batch connects personal route memory with I4's shared household and I6's
teaching/records. A working courier can teach another resident by doing the job;
a physical book can preserve that procedure after the teacher is gone. It does
not implement a general school, profession economy or settlement planner.

## Real apprenticeship

An adult resident with Senses +20, at least supported Delivery capacity, no route,
no parcel and no selected player lesson can accompany a nearby expert of its own
Hearth. The expert must independently know Delivery and have independent Teaching
capacity. One pupil follows each expert. Both must be calm, available for work
and within the actual loaded household; ordinary orders, danger and care win.
The route owner must be the Hearth owner, with its two original authorized
stores inside 24 blocks of the Hearth and no more than 32 blocks apart.

The pupil walks safe native paths alongside its teacher. It gains Delivery
practice only after witnessing both a committed collection and a committed
arrival of that same parcel. Both animal and store must be nearby and visible.
Previewed extraction, refused protection, missing stock, merely being a resident,
watching one leg or hearing about an absent shipment award nothing. An interrupted
or reloaded half-example is discarded. Repeated genuine examples use ordinary
personal learning gains, rather than a second skill system.

After mastering Delivery, the pupil remembers the witnessed source, target,
exact item components and bounded batch. No goods or points are copied. Its
resident-taught receipt becomes useful for Hearth continuity only when it later
does the routine itself under the existing independent-work rules. Capacity and
knowledge never become inherited experience.

An already trained supported courier can also work with a nearby visible
household expert who knows that same procedure. Its guide requirement remains
real: leaving the guide pauses work. The existing authorized player-guidance
path remains available. This is local guidance, not distant supervision.

## Written route notes

After completing a delivery itself, a qualified expert can walk to the Hearth
and write its route in a blank vanilla Book and Quill from that inventory. The
original book is consumed and replaced in the same native slot transaction.
Named covers survive; existing written journal pages are never overwritten.
Native mob-griefing protection can refuse the action without spending the book.
Food and patient care precede this archival work.

The result is a normal readable written-book item, **Route Notes / Notas de
recorrido**, with a concise translated item/source/destination page. No new
recipe, menu, setup screen, ambient hint or mandatory manual is required.
Leaving a blank book in the actual common store provides the opportunity.
There are at most eight route records per Hearth, with duplicate exact routes
refused. The record stores knowledge of places, never inventory contents.

A resident that already knows Delivery, meets its inherited requirements and
has no route can walk to and study a live available record. Reading does not
award practice, consume the book, complete work or make a supported mind
independent. It supplies the missing local procedure. A competent independent
resident can therefore recover a route after the teacher dies or leaves.

Records belong to the original Hearth site/owner and original storage identities,
including chest halves. A copied book in another Hearth grants no access, even
with the same keeper. Replacing a store invalidates future collections/study.
Removing the book does not erase an individual's already remembered route.
Actual parcels retain their existing save, reserve, filter and recovery rules.

## Bounds and compatibility

There is still one personal route per animal. Container, bundle, projectile-
container, private-book, custom-data and nested-record filters are not archived;
the original live personal route remains usable. Record data is bounded at
32,768 characters and parsed through the actual registry. A bounded cache avoids
reparsing the same book on every job search. Unsupported future record versions
remain opaque physical items instead of being discarded by native item decoding.
No chunks are loaded or generated for lessons, records or guidance.

The saved `DeliveryProven` flag records an actual completed delivery. Missing
fields in older saves mean unproved, without erasing the route, skill or parcel;
one real delivery makes an older route eligible for writing. Apprenticeships and
partial witnessed examples are transient. Existing genotype and l2 practice
formats are unchanged. Protocol **37** requires matching clients/servers.

Native scenarios cover actual following/learning/own delivery, teacher loss,
written-item save round trips, exact named stock, paid writing in a full store,
private journals, protection vetoes, duplicate and future records, interrupted
examples, limited/supported minds, household isolation and replacement stores.
First-person discoverability, natural classroom crowding and long-term mixed
household pacing remain player review.

## Next gaps

This addresses courier knowledge redundancy, not all I6. Need/dependency planning,
explicit cooperating professions, construction-plan handoff and repair, wildcard
wild-family bootstrap, settlement institutions and inter-community exchange
remain open. See [COMPLETION_AUDIT.md](COMPLETION_AUDIT.md) for the baseline that
prioritizes those gaps over more convenience controls or accepted Apex tuning.
