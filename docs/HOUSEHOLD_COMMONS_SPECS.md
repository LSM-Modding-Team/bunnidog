# Shared household work, public plans and physical crews

This advances I4–I7 from the courier-culture checkpoint. The existing Hearth,
skills, genes, native storage and work goals remain the foundation. No profession
selection screen, new currency, automatic stat panel or tutorial HUD is added.

## Cooperating professions and shared needs

Residents consider a bounded set of concrete available jobs rather than all
walking to the first entry in the same fixed list. Real food shortages, patients,
occupied nurseries, missing tools and worksite materials determine priorities.
Several inherited capacities and relevant physical stats affect practical
preferences, alongside familiarity with the worker's last successful specialty.
These preferences grant neither education nor permission. A capable generalist
can cover another role; a specialist does not become a permanently locked title.

A resident claims one operation when its movement goal actually starts. Native
inventory inspection and reading offers claim nothing. Workbench claims prevent
two craftspeople pursuing competing recipes at the same bench. Patient and
work-front claims keep crews separate. Claims release on interruption, reload,
departure or death and expire in 260 ticks; they own no virtual materials.
Real saved construction/planting cargo continues to identify an occupied work
front during the carrier's rest, independently of a transient movement claim.

Unreachable offers get short retries followed by bounded backoff, allowing other
useful work. Actual safety, conversations, recall and night rest retain priority.
A household job holds the movement intent against temptation and personal route
work until it finishes or yields. A worker may abandon a goal safely without
abandoning its owned parcel.

Long work still spends the existing six-operation ration budget. A worker with
no food at the Hearth can collect reachable edible surplus or prune a ready
orchard instead of deadlocking. Essential wooden-tool preparation is allowed
when it is the missing link to that food. A ration is one actual compatible
ordinary food item from the common inventory, not a breeding gesture or a new
universal starvation timer.

## Crafting uses actual recipes

The finite civic recipe vocabulary currently includes guava planks, native
sticks/wooden axes, Grafting Frames, Nests, Hearth Incense, dressings and Coppices.
Actual loaded recipe IDs, matching grids, results and remainders are authoritative.
This corrects the old approximate axe/incense costs: an axe uses planks and
sticks; incense follows the shipped three-guava recipe. No ritual or arbitrary
recipe search is permitted.

Ingredients and outputs use the original common inventory's native transaction
journal. Outputs can use the slot freed by the last ingredient; a full output
rolls the extraction back. Native griefing protection, commit-time ownership and
reentrant-call guards apply. Named or otherwise customized keepsakes are excluded
from recipe inputs. Internal farm provenance is the sole harmless component
stripped for that classification, and exact source components still determine
the extraction. No parallel profession inventory or credited resource counter
exists.

## A capable planner can leave instructions

A resident with mastered independent Garden knowledge and independent Planning
capacity can work at the Hearth and record its current observed need. The saved
public instruction identifies its author, a finite purpose and up to eight known
recipe dependencies. Food/tool, care, housing, family and grafting needs use
actual loaded stock, residents, plots and known construction cells.

For example, a food-short household missing an axe can record planks → sticks →
axe. An ordinary trained craftsperson can execute those actual affordable recipes
after the planner leaves. Satisfied prerequisites stop receiving priority; a
changed need makes the old instruction stop affecting work. Removing the planner
does not erase the record or create ingredients. Plans do not grant skill,
capacity, household progression receipts, remote inventory access or offspring
knowledge. This is a finite dependency planner, not arbitrary invention.

## Fitted rooms and real building parcels

New initial surveys can fit the first room on any clear cardinal side of the
Hearth. Existing authorized south-facing homes retain their exact site/layout.
The entrance faces the common store and requires a clear standing approach.
Rooms use a pure ordered plan: low walls and actual Nests precede the roof, with
a two-block entrance and useful interior. There is no terrain excavation or
replacement of player blocks/containers. Foundations, fluids, border and loaded
chunk checks remain mandatory.

After the first room is complete, the existing requested Staff survey can mark
one additional clear room. Confirming that specific survey authorizes it. There
are at most four cardinal plots. Small rooms have four Nests; a larger household
can survey a wider room with six. The saved orientation, radius and approved
plots form a public work front that survives staff/planner loss. Keystone
qualification continues to depend on the original room and actual work/family
evidence; extra rooms are not a new mandatory progression gate.

Builders first walk to the Hearth and withdraw one exact ordinary building
material into saved owned cargo. They then walk to the particular work cell,
revalidate it and pay for the actual placement. Collection proves both legs
reachable before taking the material. Supported builders need a real nearby,
visible, independent qualified builder; limited minds cannot bypass their genes
by holding a plan. Losing a supervisor, changing a cell or vetoing a placement
retains the parcel for physical return. No material teleports to a distant crew.

Placed-cell receipts distinguish the crew's work. A known wall later removed
by the player waits for an explicit repair survey rather than being rebuilt
repeatedly. Confirmed explosion losses can authorize paid repair after the actual
block is observed missing. The observation queue is bounded and never loads a
chunk. Unknown loss causes and occupied replacement cells remain conservative.
Repair needs a fresh actual material and uses the same crew/permission path.

Original owners can recover all stranded household supplies through the existing
native Cargo controls, including after source loss. Exact item components and
normal partial/full inventory behavior remain. Actual death drops remaining
owned cargo once. World edits and entity/chunk saves are not an atomic disk
transaction; cross-chunk hard-power-loss reconciliation remains an I5 gap.

## Material farms and physical orchard harvests

The Staff can explicitly link a Farmland block or the crop above it. This
donates only that particular planting cell. Existing block entities, soil edits,
unloaded boundaries and powered pauses are respected; workers do not silently
till surrounding player land.

Farmers carry a real ordinary wheat/beetroot/carrot/potato seed from the common
store to an empty valid plot. Planting pays the saved parcel after native
protection checks. Crops grow normally. A learned harvester can produce materials
it cannot personally eat: a guava-only farmer may grow wheat for Nests. Personal
Diet remains authoritative for meals and existing instinctive food behaviors.

Harvesting uses the actual native crop loot. One real returned seed replants the
same crop; a yield without a seed leaves the cell empty for a later paid sowing.
Remaining drops exist as physical items in the field. Coppice pruning likewise
leaves its actual two logs/four guavas at the worksite instead of remotely adding
them to Hearth storage. Tool wear and actual harvesting evidence remain.

An internal bounded household component preserves drop provenance through native
item saves and prevents different households' output merging under a shared
keeper. Haulers collect only their original home's marked output near an actual
linked field or authorized Coppice, retain exact cargo and walk it home through
the original inventory transaction. Provenance is removed when haulers collect stock;
it creates no genetic information. Nearby player-thrown goods and another site's
output are not permissions. Ordinary block-drop rules and normal item lifetime
apply; production does not run in unloaded land.

## Compatibility, evidence and remaining work

Protocol **38** requires matching clients and servers. Old genomes, l2 practice,
personal routes, original Hearth identities, initial south-facing permissions,
stock, cargo and household progression survive. New fields are bounded optional
records. Existing stored cargo without new purpose fields follows its old path.
Claims/native paths are transient; actual parcels, plans and built-cell receipts
are saved. All added text ships in English and shared neutral Peruvian Spanish.

The native suite covers contested claims, actual recipe costs, private ingredients,
full journals, recursion/protection vetoes, scarcity recovery, exact saved building
parcels, original-owner recovery, fitted rooms, supported builders, deliberate
versus explosion repair, native walking construction, real field/orchard loot,
crop seed transport, foreign loose-item refusal, native item merging, native
farmer/hauler AI, saved plans and a real dependency handoff after planner loss.

This is substantial local-commons implementation, not 100% of civilization.
Natural/client/co-op pacing and profiling still need evidence. Primitive wild
bootstrap, broader workshop professions, richer plan composition, full civic
downed rescue, functioning wild communities, inter-community agreements/trade and
advanced public works remain open in [COMPLETION_AUDIT.md](COMPLETION_AUDIT.md).
