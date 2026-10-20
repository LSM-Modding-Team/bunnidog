# Gameplay cohesion

The player should be able to raise a useful companion without reading a manual.
The Field Guide is an optional reference for exact genetics and advanced systems.
Adding another chapter does not count as making an interaction discoverable.

## The core journey

| Player's goal | Discover it through play | Success condition |
|---|---|---|
| Meet an animal | A short contextual interaction hint; empty-hand care panel | See its food, health and next useful action without tools |
| Set up a home | Nest panel, actual resident and lead controls | Understand one Nest per adult and assign a led resident deliberately |
| Care for residents | Animal and Trough panels | Distinguish healing food, guava courtship and optional crop work |
| Raise the first litter | Guava feedback, seed tooltip, crop/Cradle status | Know a mating drops a seed; plant it, wait and hatch with room |
| Choose a purpose | Existing traits, cargo, Whistle and commissions | Connect an inherited trait to something useful in the world |
| Improve a line | Calipers/Book/Table and visible commission requirements | Choose parents with an observable target; exact reference is optional |

## C1 — Controls where the work happens (implemented)

- A native, live care/station menu: short next step, compact state and named
  buttons. Empty-hand opening is read-only; opening a station never spends food,
  starts mating, changes a request or hatches a litter.
- Preserve direct item interactions and deliberate crouching shortcuts. Put the
  same operations behind visible buttons rather than inventing a second economy.
- Actual inventory stock, native paths, protection and loaded-world limits remain
  authoritative. Close menus when their exact target is removed or out of reach.
- Show nearby-context hints without filling chat or interrupting normal feedback.
- Show commission targets and actual per-requirement progress at the Board.

The common actions use real inventory: plain-guava care/courtship, required
Whistle training, Brush grooming, Trough/manger stock, Cradle seed/bone meal and
press inputs. Primary controls precede the full readout; Board request selection
has its own short chooser. Native container sessions bind buttons to the shown
shared contents, request and animal. Stale clicks refresh before spending.
Adventure can inspect; mutation remains permission/range checked. Empty main-hand
dispatch defers to real off-hand ingredients. Leads retain their native release
gesture and have a matching context hint.

The cargo view keeps inspection within care: real bundle names and bounded
contents summaries, exact last-first recovery, and a visible return to care.
Full native bundle contents remain available after retrieval. A full trained,
groomable, equipped adult still fits the bounded view codec. Existing home claims
can be confirmed for a led resident without spending another POI ticket.

Ordinary meals now agree across hands, Troughs and trail provisions. Upgraded
guavas are breeding stock: new Trough inputs refuse them, old saved stock stays
withdrawable and cannot be eaten. Opening a ready station never performs its job.

## C2 — A playable first session (implemented)

- Native milestones connect first guava, observation, housing, paid courtship,
  seed planting and successful hatching. Unlock useful recipes at those moments.
- Teach the seed's unusual role at the handoff itself. Plain farmland remains a
  complete starter route; advanced stations and genetics tools are optional.
- Replace the guide's opening with a short playable route and links to the
  reference. Do not add more pages as a substitute for game feedback.

Six native milestones cover discovery, led housing, courtship, the actual seed,
incubation/planting and real pups. Housing leads to the existing partner goal;
the litter leads to the existing commission goals, retaining their IDs/progress.
Nests accept ordinary planks. Whistles accept string/feather alternatives to
Sinew/Whisker, making a suitable founder useful before developed-line materials.
Book + guava unambiguously crafts the Pedigree Book; the optional Field Guide
adds paper. No new pages were appended and no goal gates ordinary care.

## Follow-up priorities before more standalone content

1. **Natural-world first-session playtest.** With a new survival character, raise
   a litter without reading the guide or using commands. Record actual friction:
   finding guavas/animals, materials, leads, safe housing, time and space.
2. **Meaningful first companion.** Check whether ordinary founders and the first
   offspring provide a useful, visible benefit before optimized stat breeding.
   Tune time/material requirements from play evidence, not new feature count.
3. **Workshop consistency.** Extend the same concise interaction pattern to the
   advanced genetics, judging, Apex and redstone stations where it helps. Keep
   detailed forecasts in the existing sheets rather than duplicating them.
4. **Readable world cues.** Review entrances, safe walking, station orientation,
   empty/stocked states and animal intent in a real client. Prioritize cues that
   communicate a decision; avoid mandatory tutorial popups or global checklists.
5. **Pacing and optional depth.** Audit recipe acquisition and progression so
   transport, care, commissions and shows reinforce one breeding loop. Preserve
   free play and existing saves; milestones never gate ordinary animal care.

Start the next continuation with these review goals, rather than another content
extension. Practical client questions: does the first empty-hand hint get noticed;
are primary buttons visible at the player's GUI scale; do request selection and
scroll/focus remain comfortable; is the crop's ripe state legible; and does a
young family become useful soon enough? The current batch implements a path;
it does not establish that an unfamiliar player understands it.

## Acceptance and evidence

- Opening an inspection must not mutate its target or consume inventory.
- Refused actions give a concrete local reason and conserve the real items.
- Menu buttons recheck the live target, range, permission and current readiness.
- A completed native first-litter path is stronger evidence than a list of hints.
- Headless tests establish mechanics and packet/layout contracts, not how a new
  player understands the client. Record the natural/client playtest separately.

The Java 25 combined build, model gate and all **103** native cases pass at the
integration checkpoint. Three new scenarios exercise real menu packets/lifetime,
station inventory/legacy stock/stale selections, and genuine two-parent courtship
through seed pickup, native recipes, farmland, bone meal and committed hatching.
Existing cases cover the updated explicit Pen/Cradle controls and all nine targets.
`art/cohesion_preview.png` and `art/commission_choices_preview.png` render actual
server view snapshots with a substitute font. They are layout review, not client
screenshots. Protocol **16** requires matching client/server; save formats stay.
