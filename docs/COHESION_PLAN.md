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

## C3 — Consistent workshop interactions (implemented)

- Empty hands on a Genetics Table show A/B stock, the exact next inventory
  sample, how to collect samples and named placement/retrieval controls.
  Placement fills free slots only. Use a held sample for deliberate selection
  or the existing B swap; no menu silently replaces an occupied sample.
- Read breeding plans opens the existing detailed reader: forecasts, inherited
  copies and optional splice planning. Samples remain reusable. Held guavas,
  Mutagen and Fortune tools retain their existing direct preview path.
- Empty hands on a Judging Stand inspect the actual nearest led entrant without
  issuing an award. Eligibility identifies missing parents, mixed gene pairs,
  injury or insufficient stats. All five point targets are visible. Claim
  eligible ribbon deliberately uses the same rules as the held-lead shortcut.
- Sample contents, the shown next inventory sample and the entrant's complete
  winning record bind native menu clicks. Another player, hopper or changed
  entrant cannot turn an old click into an unseen action. Adventure may inspect
  Tables and scorecards; direct Table placement/withdrawal now respects that
  same mutation restriction. Complete saved samples remain recoverable.
- Tissue tooltips lead with their reusable Table purpose; raw codes and Apex
  reference move to advanced tooltips. Two existing guide pages describe the
  visible actions. No guide pages, currency or breeding gates are added.

The rest of the workshop now uses the same pattern:

- **Pedigree Comparator:** opening no longer changes mode. Named exact/analog,
  free sample-copy, read-reference and clear controls expose the live signal,
  hidden-copy matches and actual front pad. Routine power polling does not
  invalidate an unrelated mode choice; changed references still invalidate clicks.
- **Specimen Jar:** preserve the shown sample, read it or retrieve the complete
  original. Empty-hand opening is read-only, real off-hand insertion works, and
  direct/menu mutations share build permission, reach and live-target checks.
- **Trophy Pedestal:** actual-hand placement fills an empty display; empty hands
  inspect, named retrieval returns its complete item, and genetic records use the
  existing readers. Crouched-empty retrieval remains a shortcut.
- **Apex Crucible:** an actual chosen sample, combat forecast and five-offering
  checklist precede Review summon and a separate Summon Apex action. Material
  changes disarm confirmation. Requested ring markers show possible Pedestal
  positions. Native spawn cancellation, stock/source replacement and recursive
  rituals conserve actual items and discard an unpaid Apex. Payment extracts one
  of each legend together, retaining excess legacy stack counts and components.

This is inspection and control consistency, not a second inventory economy.
No animal, crop, material threshold, encounter rule or body mesh is replaced.

## C4 — Give a forecast a purpose (implemented)

A Genetics Table can keep one of the nine commission goals. With two samples,
Read breeding plans opens a sixth analysis spread: joint required-gene and point
odds per pup and at least one in an uncrowded litter, actual targets, expected
points and a useful next step. Impossible genes suggest another sample/splice;
insufficient points suggest better parents or a different quality plan. A viable
pair leads back to the Board's adult, Breeder, pad, paper and optional cargo work.

The real commission predicate supplies target metadata and evaluates possible
inheritance; no parallel set of target rules is invented. Points use the native
parent average, quality-dependent drift, stochastic rounding and signed limits.
Mutation is separate. Planned Fortune explicitly says crop-only. Samples and held
cues stay reusable; a forecast never approves an actual commission candidate.

The optional `BreedingGoal` key is saved on the Table. Legacy no-goal tables keep
five spreads and old A|B/plan payloads still parse. Unknown future saved keys stay
until deliberately replaced/cleared; unknown wire goals refuse interpretation.
Protocol **17** carries the optional goal, requiring matching client/server builds.

## C5 — Make ordinary animals useful and work spaces visible (implemented parts)

Hanging guavas now name picking, ripening and bone-meal actions at the crosshair,
including the native crouching/held-item bypass. This closes a first-food cue gap
without changing fruit growth, costs or harvest mechanics.

Useful roles & work shows actual pack capacity, temperament, cheap native harvest
readiness, expressed-trait benefits and canonical specialty thresholds. Basic
ripe-crop harvesting and carrying need no bred points. The native walking scenario
now starts with zero points and still completes its loaded trail journey. Work
pause/resume lives beside the readiness, retaining the direct hoe shortcut.

Show hatch area / Show front pad closes inspection and briefly marks the exact
Cradle, Board or gene-detector footprint in the world. Cradles reuse the actual
nine hatching positions and identify the first obstruction. These are requested
cues, not repeated chat or tutorial popups; they never hatch, move items or load
remote chunks. Actual marker visibility, GUI scales and natural-world first-family
pacing remain client playtests, rather than claims established by headless code.

## C6 — From a plan to its actual family (implemented)

The Table can compare the shown actual inventory seed with its goal, using saved
parents, points, quality and Mutagen treatment. Held preview cues cannot change
that seed. Existing seed readers retain their original overview/parent indices
and add actual pup forecast/copy spreads; Cradles expose the reader directly.

With Calipers, the Table lists nearby living offspring of the sampled genomes.
An actual seed has its own family entry, so stale/different Table samples cannot
silently supply another cross's offspring. Four named, positioned animals per
page show inherited gene/point targets before their controls. Native birth data
identify parent genomes rather than individuals, which the view states. Exact
row selections, samples, seed and goal bind stale clicks; glints identify actual
animals without moving or ordering them.

A selected animal opens its actual care session with that goal. Useful roles
also compares any animal against the nine requests. Real predicates and shared
Board living-condition checks separate inherited misses from actionable growth,
health, calm and cargo. A fitting wild pup immediately identifies absent birth
records. Comparisons are private/read-only, including Adventure. Losing Calipers
hides exact results. Care gives loaded growth time and explains native
golden-dandelion pauses; Bloom's extra aging now respects that pause.

The recipe audit found a circular dependency: Table, Board and Breeder's Pen
needed Whiskers before a Senses line existed. Ordinary feathers now substitute,
while old Whisker recipes remain valid. First-litter recipe reveals include the
Table, Syringe and Ear Tag. Seven guide entries changed; no chapters were added.
See `FAMILY_LOOP_SPECS.md` for contracts, evidence and compatibility.

## Follow-up priorities before more standalone content

1. **Natural-world first-session playtest.** With a new survival character, raise
   a litter without reading the guide or using commands. Record actual friction:
   finding guavas/animals, materials, leads, safe housing, time and space.
2. **Meaningful first companion.** Check whether ordinary founders and the first
   offspring provide a useful, visible benefit before optimized stat breeding.
   Tune time/material requirements from play evidence, not new feature count.
3. **Review the implemented purpose loop in Survival.** Table goal → actual seed
   → living offspring → growth → actual Board reward now has native coverage.
   It fast-forwards the long growth wait. Try that complete path in a natural
   client and record whether goals, quality, supplies, space and timing explain
   the next decision. Fix observed handoff gaps before another system.
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

The Java 25 combined build, model gate and all **107** native cases pass at the
integration checkpoint. The first three cohesion scenarios exercise real menu
packets/lifetime, station inventory/legacy stock/stale selections and two-parent courtship
through seed pickup, native recipes, farmland, bone meal and committed hatching.
Workshop coverage exercises native empty-hand opening/button packets, exact
sample/display recovery, free imprinting, signal polling, goal saves/legacy/future
keys, stale selections, Adventure inspection and explicit all-tier judging.
The added family case follows real seed offspring through measured selection,
native golden-dandelion pauses, paid feeding and a real Board payout. It checks
sampled versus actual-seed families, private goals, missing birth records and
lost tools. All nine actual-seed views fit; native payload/readers conserve
saved inputs. Real recipe checks cover feather and Whisker variants.
Apex coverage includes actual cancellation, recursive calls, replaced stock and
inventory sources, exact legacy stack costs, blocked spawns and creative keeps.
Focused breeding-lab checks cover combined recessive targets, stochastic point
boundaries, planned quality, cargo separation, all nine goal payloads and page
clearance. The ordinary partner walks at zero points in its existing native case.

Seventeen actual server view snapshots feed `art/cohesion_preview.png`,
`art/commission_choices_preview.png`, `art/workshop_controls_preview.png` and
`art/family_goals_preview.png` and `art/family_progression_preview.png`.
`art/breeding_goal_preview.png` and `art/actual_seed_forecast_preview.png` render
the exported goal/seed spreads. These use substitute fonts; they are layout reviews, not
client screenshots. Protocol **17** requires matching client/server; existing animal/item data stay compatible; only the Table goal is an optional new key.
