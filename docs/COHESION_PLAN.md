# Gameplay cohesion

The player should be able to raise a useful companion without reading a manual.
The Field Guide is an optional reference for exact genetics and advanced systems.
Adding another chapter does not count as making an interaction discoverable.

## Screenshot review: normal container backgrounds (2026-10-24)

The user likes the native-slot arrangement but rejects its floating appearance.
Stations now use the normal opaque gray Minecraft container background, with
native beveled edges and slot artwork. The same treatment covers their nested
choices, Board/Comparator/Apex preparation, Calipers inspection and the name form.
Dark labels and status colors stay readable on gray; no setup text or tutorial
layer is added. MCA animal conversations remain borderless and books retain their
page presentation. This explicitly supersedes the earlier backgroundless-station
choice while preserving the interaction and real-stock corrections below.

`ContainerBackdrop` slices only the empty frame/slot art from Minecraft's existing
crafting texture; it does not stretch a crafting grid into every menu or ship a
copy of vanilla assets. Existing permissions, focus, Back/Close positions and
native carried-item behavior are untouched. Protocol/save formats are unchanged.
English/Spanish previews include the minimum viewport; actual native-font/client
appearance remains review work. Java 25 build/localization gates pass.

The same request plans future Apex roles, flight, an Apex-populated dimension,
rich loot and a true final dungeon boss in
[APEX_EXPANSION_PLAN.md](APEX_EXPANSION_PLAN.md). Those are proposals only in this
batch, and do not quietly add combat or worldgen while fixing the shown UI.

## Native station inventories (2026-10-24)

A player with several samples or seed records must be able to choose which one
enters a station. The old Add/Withdraw buttons silently chose the first matching
inventory item, even after replacing the text dump with icons. The Trough, Pen,
Cradle, Juicer, Genetics Table, Specimen Jar and populated Trophy Pedestal now use
real Minecraft inventory slots, backed by their existing saved stock. The player's
inventory is visible beside concise actions; normal pickup, placement, deliberate
swaps and shift-click move the actual item. No additional guide layer is added.

There is no second menu inventory, changed recipe cost or new saved format. The
original journals enforce component-sensitive stacks, input limits and retrieval;
Juicer output rejects insertion, while players can retrieve input stock that
hopper ports intentionally cannot drain. Saved invalid/overfilled Trough stock
can still be recovered intact. Cradle insertion retains the incubation milestone;
only actual removal resets growth. Native close handling returns cursor items.
Reentry, lifetime, reach and permissions still apply to real slot gestures, and
callbacks may replace the menu without that replacement being closed. Native
station notifications are deferred until the complete gesture owns its cursor,
so replacement during a block-neighbor callback also returns that item safely.

The storage layout is 300×224 GUI pixels, fitting Minecraft's minimum 320×240
viewport. Captions identify inputs; visual bars replace raw growth/timing numbers.
The press has a simple input + carrier → output diagram. Analysis and settings
stay in a short sidebar. Nested work/goal/family pages hide and reject all slots
without changing footer positions. Read-only filters and cargo retain copied
icons; a virtual template must never become an item the player can withdraw.
English and neutral Spanish labels fit the narrower sidebar. Optional-guide
pages describe actual controls instead of retired transfer buttons.

Protocol **23** carries the station kind/progress before screen initialization;
vanilla container packets synchronize actual slots. The **132 native scenarios**
include exact item selection/swapping, first-frame slot contents, cursor refund,
recursive listener/menu replacement, protected/hidden/distant gestures, actual
Juicer output, Cradle insertion/growth and saved legacy recovery. The ordinary
Java 25 build also validates localization. English/Spanish ordinary and minimum-
viewport previews use substitute fonts/backgrounds; mouse/keyboard feel, native
font scaling and natural first-session discovery still need client review.

Next audit: the Table's non-consuming actual-seed preview and the Comparator's
reference copy still select their first inventory candidate. They should gain
an explicit selection path without turning copied references into payable stock
or replacing familiar inventory placement with another instruction menu.
Continue fixing observed interaction friction before new intelligence features.

## World actions and requested information (2026-10-22)

Closing the last UI gaps requires behavior as well as shorter text. A successful
care/order now returns to the world, releasing the conversation goal so following,
courtship and combat can happen. Failure stays open for its local reason;
settings and cargo stay open for further changes. Native success is checked before
closing, and a callback's replacement menu is preserved. Releasing the companion
has a concise consequence/confirmation page; cancellation changes nothing.

Plain held materials inspect care stations rather than placing against them or
printing setup instructions. Purposeful tool subclasses, enchanted preview cues,
actual ingredient handlers and real off-hand uses retain native priority.
Crouch-placement still uses Minecraft's own path. This is a conservative fallback,
not a global input hook or a claim that every other mod's item is generic.

Work/Lessons now identify actual pauses, orders, night, missing/outside homes and
other physical blockers. Chosen practice reports searching, approaching, working,
waiting for its guide and actual shared rest. Goal callbacks supply activity;
no hypothetical work or inherited mastery is invented. An interrupted chosen
session gives its nearby guide one brief event and keeps learned knowledge.
There is no automatic tutorial or new status overlay for ordinary animals.

Crouch-Calipers now first offers purposes, then shows the requested measurements
in bounded pages. Health, home/food, work, field traits and combat keep their
information; ordinary Calipers retains the genetic reader. Measured work separates
learning from physical point requirements. The former work report used some label
keys without placeholders, silently dropping their statuses; the new view displays
actual values and the native regression checks component arguments remain visible.
The tool must remain in inventory: losing it hides the readings and retires old
buttons. No free measurement path is added to ordinary interaction.

Native coverage includes real held-block inspection, direct off-hand inputs,
crouch-placement, a normal AI companion waiting/following after real menu clicks,
practice activity and interrupted knowledge, actual Calipers dispatch and tool
loss. Full Java 25 build and 130 native scenarios pass. English/Spanish snapshot
previews are layout evidence; actual discovery, scales and keyboard feel remain
client review work.

## Proactive interaction audit (2026-10-21)

The follow-up review applies to stations and nested actions as well as animals.
That audit removed the remaining always-present station instructions, the large
bordered station panel and blur, and textual inventory dumps. Actual stock uses
item icons/counts; hover gives the native item or bundle contents. That original
read-only stock display is superseded for the seven storage stations by native
slots above. Cargo, filters and other preview-only items remain copied displays.

Work policies and commission selection are separate pages. Back/Close have the
same footer positions throughout; Back follows the route actually taken. A
page change resets scroll and retires its buttons even when no items changed.
Routine live refreshes update existing widgets, labels, reasons and stocks,
avoiding the old animal-screen rebuild on each timed change.

The original audit hid impossible transfer buttons; native slots now replace
those buttons. Mature Cradles still omit growth controls. Cradle growth and hatch-area clearance are distinct
states. Empty Trophy Pedestals have no inspection menu. Cargo shows real bundles
with native hover contents, including the next bag actually retrieved. Exact
measurements stay in the tools and explicit analysis stations; requesting a
forecast is still allowed to show useful detail. Warnings about real consequences
remain local (for example, seed withdrawal resets growth).

Combat no longer repeats Follow/Wait/Home, provisions or bond release. It omits
controls for membership an animal does not have. Party actions name the actual
animal and return to their source page. Lessons offer each routine once, with
learned/guide/teach action labels. Starting a lesson closes conversation, allowing
the animal to move into real work. Practice feedback is brief. Calipers, Registry,
Basket, Syringe and seed tooltips no longer present the full secondary-operation
manual by default; tools/readers and advanced tooltips keep the exact reference.

Native regressions cover initial and updated stock/component payloads, detached
stock, read-only choice opening, page-transition stamps, Work/Cargo and seed-family
Back paths, current-content actions, empty displays and lesson-start release.
The complete suite has 126 scenarios. The family case now declares the real
horizontal fixture extent so newborns are in entity-ticking chunks. Substitute-
font previews are layout evidence only; first-client usability remains open.

## Client review corrections (2026-10-21)

The user found that persistent crosshair instructions and verbose animal panels
had moved the manual into the UI. That is failed discovery, even when a headless
test proves the controls work. This review overrides earlier claims of completed
first-session cohesion.

The follow-up review identified six concrete failures. The current corrections:

- The first bounded snapshot is encoded in NeoForge's menu-opening data and
  decoded by the registered client factory before screen construction. A pending
  snapshot never renders the old station UI.
- Animal controls draw directly over the world, without an enclosing box.
  Back (left arrow) and Close have fixed separate footer positions; Back never
  appears among the action buttons or moves with their content.
- A temporary conversation goal keeps calm animals in place, facing an actual
  nearby viewer of their menu. Closing/replacing it releases attention; hostile
  or frightened animals keep their response. Combat, refuge and native safety
  behavior retain priority; no AI-disable flag, teleport or persistent order.
- Nests have no menu. Direct use assigns a nearby led adult or gives brief
  occupancy feedback. Compass marking and actual POI ownership remain intact.
- Offering any guava, including from the off hand, opens its food choices and
  spends nothing. A valid selection uses that offered hand's actual stack;
  upgraded courtship keeps its tier. A changed hand rejects the stale choice.
  Successful guava choices close the interaction, allowing courtship to proceed.

The native factory/opening-data test runs before any incremental view message;
the normal-AI conversation scenario checks a skittish animal over 20 ticks,
immediate menu-close release and response to the actual viewer's attack.
The full suite has **125 scenarios**. The preview remains a substitute-font
layout illustration; these checks do not establish the client's first frame.

- Remove the automatic crosshair tutorial layer completely. No replacement
  popup or repeating instruction; no essential one-time hint has been identified.
- Use the old [MCA 1.12 interaction pattern](https://github.com/WildBamaBoy/minecraft-comes-alive/blob/1.12.2-6.0.1/src/main/java/mca/client/gui/GuiInteract.java):
  short named action groups beside the actual visible animal/world, without a
  full-screen blur or a scrolling first-contact readout. This is an interaction
  reference, not imported MCA code or a new social/relationship system.
- Ordinary held items may open interaction. Guavas open deliberate food choices;
  preserve other direct food, both hands,
  leads, shears, name tags, bundles and the mod's purpose-built tools.
- First contact offers Care, Orders and Work; Cargo appears for actual bundles.
  Calipers add a deliberate Measure action. No initial instructions, inferred
  genes, bred points, exact health, combat calculations or specialty thresholds.
- Care and Orders contain actual verbs; unavailable actions have short local
  hover reasons. Work reports personally practiced routines. Lessons retain
  choices and live status, without a theory/threshold/setup wall of text.
- Exact body/mind measurements and forecasts remain in the existing tools and
  stations. Combat calculations move to the explicit Calipers care reading.

Native tests cover ordinary-item opening, off-hand priorities, data-free first
contact, hidden-point changes, real inventory use and stale/lost-tool denial.
The existing complete-family, cargo, shearing and companion cases remain gates.
`art/interaction_preview.png` illustrates English/Spanish native snapshots with a
substitute font and the existing sculpt; it is not a Minecraft client capture.
Natural discovery, GUI scale, accessibility and pacing still need client review.

The user-directed [combat plan](COMBAT_COMPANION_PLAN.md) and
[player scenarios](COMBAT_SCENARIOS.md) now have an implemented B1/B2 and initial
B3 party in [COMBAT_SPECS.md](COMBAT_SPECS.md), following the 2026-10-20 planning
batch and explicit 2026-10-21 implementation authorization. Combat extends this journey: an ordinary adult helps immediately,
clear commands and rescue make it worth bringing along, and real adventures
give the player a reason to improve a family. No mandatory manual, loyalty grind
or separate gear prerequisite replaces the existing care/breeding loop.

## The core journey

| Player's goal | Discover it through play | Success condition |
|---|---|---|
| Meet an animal | Try ordinary interaction; short action groups beside the animal | Choose care or orders without a manual or unsolicited measurements |
| Set up a home | Lead an adult to a Nest and use it directly | Assign a real resident in the world, with brief occupancy feedback |
| Care for residents | Animal and Trough panels | Distinguish healing food, guava courtship and optional crop work |
| Raise the first litter | Guava feedback, seed tooltip, crop/Cradle status | Know a mating drops a seed; plant it, wait and hatch with room |
| Choose a purpose | Existing traits, cargo, Whistle and commissions | Connect an inherited trait to something useful in the world |
| Improve a line | Calipers/Book/Table and visible commission requirements | Choose parents with an observable target; exact reference is optional |

## C1 — Controls where the work happens (implemented)

- Native, live animal action groups and station controls. Animal opening is
  read-only; opening a station menu never spends food,
  starts mating, changes a request or hatches a litter.
- Preserve direct item interactions and deliberate crouching shortcuts. Put the
  same operations behind visible buttons rather than inventing a second economy.
- Actual inventory stock, native paths, protection and loaded-world limits remain
  authoritative. Close menus when their exact target is removed or out of reach.
- Explain unavailable actions locally, without automatic crosshair instructions.
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

Hanging guavas retain native picking, ripening and bone-meal behavior, including
the crouching/held-item bypass. The automatic crosshair instructions were removed
after the first client review; ordinary use does not need a tutorial overlay.

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
extension. Practical client questions: are primary buttons visible at the player's
GUI scale; do request selection and scroll/focus remain comfortable; is the crop's ripe state legible; and does a
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
