# Animal care and inherited adaptations

Current A11 supplies the Island's native oxygen/propulsion and A12 completes
useful pale-coat cave vision. A10 supplies coat camouflage; H11 adds
Build-based native bundle carriers, with cargo breaking cover. Current
client/server protocol **14** includes the coastal allele semantics; update both
sides. Earlier per-feature protocol/test counts are historical checkpoints.

Implemented 2026-10-16 under the user's standing design latitude. These additions
make a valuable breeding animal useful while alive, and give several wild traits
clear environmental benefits. Compiler/server evidence is separate from manual
client gameplay review.

## C1: Sampling Syringe

- Craft one iron ingot, glass bottle and stick on a diagonal: `  I /  G  / S  `.
  The reusable tool has 32 uses and appears in Tools & Utilities. Obtaining a
  glass bottle unlocks the recipe.
- Use on a living bunnidog at least half its maximum health. Adults and pups
  are both eligible. Take one ordinary Tissue Sample containing the exact
  current seven-gene genome and bred stats. The existing Calipers and Genetics
  Table read it without a new component or sample format.
- Sampling deals no damage and leaves age, love, genome and stats intact. It
  uses one tool durability and starts five minutes / 6,000 ticks of animal
  recovery. A refusal neither consumes durability nor restarts the timer.
- Samples go to inventory, dropping normally if full. Creative preserves tool
  durability but keeps animal recovery. A quiet bottle sound/action-bar message
  confirms success; refusal explains health or remaining time.
- `SampleWait` saves remaining ticks, clamped to the recovery interval. Missing
  old data means ready. As with forage recovery, time is paused across unloading.

## C2: Woolly fleece

- A healthy adult expressing BUILD `woolly` yields two wool in its coat palette
  with ordinary shears. Health must be at least half maximum. Pups keep their
  fleece; ordinary builds and hidden woolly alleles do not yield it.
  C7 below defines the colors; the original C2 yield was always white.
- NeoForge `IShearable` integrates with native shears, including tool wear and
  drop spawning. One successful use costs one shear durability and starts ten
  minutes / 12,000 ticks of regrowth. Refusals cost no durability.
- The animal remains alive and healthy. Gathering loose fleece keeps the
  approved sculpt intact; no new shaved model or coat atlas is introduced.
  Pelts and all death-drop yields remain the existing separate reward.
- `FleeceWait` saves clamped remaining ticks. A synced readiness boolean handles
  client prediction and becomes ready when server time reaches the deadline.
  Missing old data means ready; regrowth pauses across unloading. Network version
  5 introduced that sync; current protocol 8 also includes splicer selection and new wild leg semantics and
  requires matching updated clients and server.

## C3: Loose-whisker grooming

- Use an ordinary **vanilla Brush** on a living, healthy adult with at least
  **+10 bred Senses points**. Ear size and wild hearing multipliers do not replace
  this breeding requirement. Health must be at least half maximum.
- Collect **one existing bunnidog whisker**, with one Brush durability and
  **ten minutes / 12,000 ticks** of animal recovery. Inventory-full drops normally.
  Creative preserves the Brush but keeps animal recovery. Refusals cost nothing.
- This gathers loose whiskers without damage, changes to genes/stats/age/love,
  or edits to the approved sculpt. Existing death drops and their yields remain.
- Optional `GroomWait` saves clamped remaining ticks; old animals are ready and
  unloading pauses recovery. It is independent of sampling, fleece and splicing.
- Native brushing sound/action-bar feedback explains success, required Senses,
  health/adulthood and remaining recovery. No new item or synced field is needed.

## C4: Quick care readout

Sneak-use Calipers on an animal to print a server-side chat snapshot of the four
care activities: sampling, fleece, grooming and splicing. Each row gives the
current eligibility requirement or, if eligible, ready/recovery time. Recovery
uses rounded-up minutes:seconds, so a partial second is not reported as ready.
Splicing readiness describes the recipient; a donor sample and Mutagen are still
needed. Normal Calipers use opens the existing genetics sheet. Reading changes
no animal, items or timers and introduces no payload, saved data or sync field.
The actual Gradle build passes; client use remains a manual gameplay check.

Settlement L5 adds a fifth gardening row with its full-health/home/day/danger/
transport/mobGriefing requirements and saved recovery. It shares the AI's cheap
eligibility result, and states the enabled trough/ripe matching food/path needs
without performing a work search. See `SETTLEMENT_LIFE.md`.

## C5: Living dust

Rare lines can now supply modest materials while alive. Brushing an expressed
glow adult yields one vanilla glowstone dust. Brushing an expressed starborn
adult with at least +20 bred Bloom yields one existing Starlit Dust. These
requirements are independent of the +10 Senses requirement for loose whiskers.
Only expressed alleles count; hidden glow/starborn copies yield no dust.

Every qualifying product is gathered together, exactly one each, for one Brush
durability and the same ten-minute saved `GroomWait`. There are no separate
per-product timers to bypass by swapping genes or tools. All products require
a living adult at least half healthy; the Apex remains ineligible. Full
inventories drop each remainder normally, and creative preserves the Brush
while retaining recovery. Age, health, genes, bred stats and love are preserved.
Existing death-drop yields, marrow and legendary progression remain unchanged.

Calipers names the currently available products, or explains the alternative
trait/stat requirements. Normal Brush feedback names what was collected.
Gene hovers describe the renewable sources, and the Field Guide grows to 28
pages. The existing native grooming case covers hidden traits, Bloom +19/+20,
individual/combined products, shared refusal and inventory overflow alongside
the original age/health/save/creative/tool-breakage checks. Actual build and all
eighteen native scenarios pass; the updated guide preview is inspected. Client
interaction and long-term production balance still need gameplay review.

## C6: Resident needs snapshot

Sneak-Calipers now starts with the claimed Nest's coordinates and distance,
accepted foods, and the current resident-meal status. Food eligibility is shared
with the real trough/grazing/hunting goals: adulthood, injury, a Nest, day,
transport, combat, home boundary and saved meal recovery. Healthy adults say
"no healing needed", without inventing hunger. Recovery is still shown while
healthy or otherwise blocked, rounded up to minutes:seconds.

Ready means ready to search for reachable matching food; it does not claim that
stock or prey exists. Reading only uses existing animal state and coordinates,
with no POI/path/terrain search, chunk loading, ticket changes or timer resets.
The same `ForageWait` save field is retained. The Apex gets one clear domestic
care refusal rather than misleading care-ready rows. Normal Calipers sheets
remain available. The Field Guide expands to 31 pages.

The existing native settlement case covers missing/claimed homes, agreement with
AI readiness, a real trough meal, saved-length recovery, full health, the home
boundary and read-only inspection. Actual build and all twenty native scenarios
pass; updated guide preview inspected. Client chat formatting remains gameplay
review.

## C7: fleece palettes

Woolly fleece now follows the **expressed coat**, giving living decorative lines
a practical use. The native wool palette approximates the existing fur colors:

| Expressed coat | One successful shearing |
|---|---|
| tan, sand | 2 yellow wool |
| cream, snow | 2 white wool |
| brown | 2 brown wool |
| spotted | 1 yellow and 1 brown wool |
| moss | 2 green wool |
| glow | 2 cyan wool |
| pale | 2 light gray wool |
| slate, seal | 2 gray wool |
| ember | 2 red wool |
| void | 2 purple wool |

Only expressed colors count; a hidden recessive glow copy does not add cyan.
The total stays two, including spotted's mixed pair. Every product shares the
existing one shears durability and ten-minute saved `FleeceWait`; changing coat
or build cannot reset or bypass recovery. After regrowth, the next harvest uses
the then-current expressed coat. Age/health checks, native drop spawning and
creative shears behavior stay unchanged. Pelts/death drops keep their own roles.

Ready sneak-Calipers names each exact color/count from the same prospective
native product list used by shearing. Reading starts no recovery and changes
no animal or item. Ineligible/recovering animals retain their clear requirements
and timer. Gene hovers and the 36-page Field Guide explain the color source.
No gene IDs, stat rules, timers, save formats, network fields, sculpt or atlases
are changed; existing woolly animals use this palette at their next ready shear.

Actual Java 25 build and all twenty-four native cases pass. The extended fleece
case uses actual player/shears interactions for hidden glow versus tan, cyan
regrowth after a coat edit, mixed spotted drops and one durability per harvest.
It checks that a gene edit cannot bypass recovery, native save/load and ticks
retain/regrow fleece, Calipers is read-only and every palette resolves to real
wool colors while conserving the two-item total. The guide preview is inspected;
client interaction and decorative breeding balance remain gameplay review.

## C8: dispenser care stations

Face a native dispenser toward a bunnidog in the adjacent block and pulse it
with redstone. A vanilla Brush gathers all of one eligible adult's qualified
whisker/glowstone/Starlit products together. A Sampling Syringe produces one
exact Tissue Sample from an eligible adult or pup. Choose the nearest eligible
ordinary animal in that block; no remote search or terrain loading is needed.

Both paths use the same server animal transactions as hand tools, including
half-health requirements, expressed traits/bred stats, Apex refusal and saved
recovery. Switching between hand use and machines cannot bypass a timer.
One successful treatment wears its tool once; a last charge breaks normally.
Failed care retains the tool and costs no recovery or durability. Products
eject as ordinary item entities for players/hoppers, preserving sample data.
No animal health, age, genes or stats are changed by treatment.

Native armadillo dispenser brushing is retained as a fallback when no bunnidog
qualifies; its original scute behavior and sixteen-durability cost still apply.
Brush block/archaeology use is unchanged. Native dispenser shears already handle
the woolly fleece interface and retain their normal behavior. Existing manual
creative/tool-breakage/overflow messages remain in place. No recipes, items,
saved fields, native genetics rules or network version change. The Field Guide
grows to 38 pages and explains collecting the drops.

Actual Java 25 build and all twenty-six native scenarios pass. The new case
uses real redstone pulses and scheduled dispenser ticks for combined grooming,
shared-recovery refusal, an exact pup sample, last-charge breakage and unchanged
armadillo scute output/cost. Existing manual sampling/grooming/save/creative/
overflow cases also pass. The older shelter fixture now waits for native roof
lighting before its path assertions; route requirements remain intact. The
updated guide preview is inspected. Client stations and multiplayer automation
balance remain gameplay review.

## C9: loose moss from a living coat

Healthy adults expressing COAT `moss` with at least **+10 bred Bloom** yield
one ordinary **Moss Carpet** when brushed. This gathers loose coat growth;
the approved creature model and atlas remain intact. Hidden moss copies and
lower Bloom yield none. No climate scan, terrain change or new item is needed.

The product joins existing whiskers/dust in the same grooming transaction:
one of each matching product, one Brush wear and the same ten-minute saved
`GroomWait`. A moss/starborn line with Bloom +20 and Senses +10 consequently
yields moss carpet, Starlit Dust and a whisker together. Switching genes/tools
cannot bypass recovery. Existing adulthood/half-health/Apex, creative and
inventory-overflow rules apply equally to hand use and dispensers. Calipers,
gene hovers and the living-harvest guide page explain the source. Death drops,
inheritance, stats, protocol and saved formats remain unchanged.

C9 evidence (2026-10-17): actual Java 25 build/all 37 native scenarios pass.
The existing grooming case covers a moss copy hidden by expressed void,
Bloom +9/+10, one real hand harvest, gene-change recovery refusal, and combined
moss/whisker/Starlit yield for one wear with preserved health/genome/stats.
The real dispenser case also collects that combination without extra wear or
glow dust; original armadillo fallback and exact sampling remain passing.
The living-harvest guide preview is inspected. Long-term husbandry balance
and client use remain gameplay review.

## C10: cold-climate frost brushing

Healthy adults expressing a snow coat and bred Bloom +10 yield one ordinary
Snowball from brushing in a climate cold enough for native snowfall. The
resolved biome temperature/altitude rule determines cold; it is a live habitat
condition, never saved biology. Warm climates, hidden snow and Bloom +9 refuse
this product. All ordinary health/adult/care gates apply.

Frost combines with eligible whiskers and Starlit Dust for one Brush wear and
the same saved ten-minute `GroomWait`. Changing climate or genes cannot reset
that recovery. Hand tools, dispensers and Calipers share one native product
snapshot. Woolly fleece keeps its separate regrowth and native `IShearable`
dispenser behavior; no custom shears dispatcher replaces sheep/beehive logic.
There is no new item, allele, network format or coat texture.

C10 evidence (2026-10-19): Java 25 build/all 50 native scenarios pass. The new
case changes actual native biomes, checks warm/low-Bloom/hidden-snow refusal,
real off-hand brushing, exact one-product/one-wear/shared rest and no harm.
Real dispenser pulses collect frost/whiskers/Starlit together, then shear two
coat-colored wool while retaining independent care recoveries; repeats cost
nothing. New guide/Habitat preview inspected; client climate/care appearance
remains gameplay review.

## A1/A2: Expressed environmental adaptations

| Expressed trait | Benefit |
|---|---|
| COAT `ember` | Native fire immunity, including lava/magma/fire-tagged damage |
| COAT `snow` | No powder-snow freezing |
| BUILD `woolly` | No powder-snow freezing; renewable fleece above |
| LEGS `hoofed` | Half the usual fall-damage multiplier, with native rounding/hooks |
| COAT `moss` (A2) | Native Poison effect immunity; wither and other magic still apply |
| COAT `sand` (A2) | Native cactus damage immunity; ordinary damage/other thorns still apply |
| COAT `void` (A2) | Native Levitation effect immunity; projectile impacts/falls still hurt |

Benefits follow the expressed allele after dominance, so an ember/snow dog
expressing ember does not also gain snow's cold protection. Separate genes can
combine, for example ember coat with woolly build. Spawn biome and founder
lineage do not hardcode benefits. Effects update with the expressed genome;
native equipment/tag protection and damage hooks still apply.

`Plain.effect` explains these benefits on sheets and forecasts. Existing gene
slots, allele IDs, inheritance, stat drift, juice effects, genome formats and
body multipliers stay unchanged. Woolly protection prevents freezing damage;
A5 adds surface footing specifically to expressed snow coats. Hoofed legs
reduce falls rather than making them harmless.

A2 extends practical protection to swamp, desert and End coats. It follows
ordinary expression: a moss/void animal expressing void remains vulnerable to
poison, and hidden sand does not prevent cactus damage. These coat choices do
not stack, while independent genes (such as woolly or hoofed) still combine.

Poison/Levitation use native `canBeAffected` rules alongside existing effect
protection/hooks. Replacing the expressed genome with an immune coat also removes
its already-active blocked effect on the server, without altering stats/health.
Changing away restores ordinary susceptibility. Sand checks the exact cactus
damage type in native `isInvulnerableTo`; it respects bypass-invulnerability
damage tags and keeps native equipment/other damage rules. No new flags/timers,
allele IDs, sync fields, geometry or saved formats are introduced.

## A3: physical defenses and inherited glow

- An expressed **slate coat** adds four native Armor points. This uses native
  damage math, equipment and hooks; it does not stop armor-bypassing damage.
- Expressed **stocky/chonker builds** add .15/.30 native knockback resistance.
  Pushes are reduced, never categorically prevented. Other builds keep their
  existing health/body, fleece and environmental roles.
- The recessive **glow coat** is self-lit when expressed: the existing renderer
  uses block light 15 for that animal. Carriers retain ordinary lighting. It
  changes neither nearby block light nor spawning, and needs no extra geometry,
  texture, render pass or synced flag. The approved mesh and twelve atlases stay.

Named transient attribute modifiers apply the physical benefits alongside
external base values/equipment/modifiers. Genome edits update/remove only those
two genetic modifiers. Loading reapplies them after native attributes restore,
so legacy animals gain their current phenotype and reloads cannot stack bonuses.
Apex copies combine them with their existing base armor 4/resistance .5; a
slate/chonker Apex consequently has armor 8/resistance .8 before external changes.
Gene hover/readouts explain all benefits. Allele order, inheritance, bred points,
body proportions, saves, juice effects and protocol 6 remain compatible.

The combined Gradle build, headless model check and eighteen native server
scenarios pass. The extended adaptation case covers real attack damage and
knockback, hidden traits, genome edits, two native loads preserving external
armor without stacking, and Apex base/trait combinations. Headless checks cover
expressed/carried light selection and retain all approved mesh invariants.
The software lighting comparison was inspected; native in-game lighting and
combat balance still need gameplay review.

## A4: native terrain movement

Expressed hoofed legs add **0.4 native Step Height**, taking an ordinary adult
from 0.6 to 1.0 blocks so it can walk up a full ledge without jumping. Their
existing half fall damage remains. This does not confer climbing, a higher
jump, safe falls or passage through fences; native collision clearance applies.

Expressed fleet legs add **0.25 Movement Efficiency**, and lean builds add
**0.5**. Native slow-block math combines these independent genes: ordinary
soul-sand/honey speed factor 0.4 becomes 0.55 with fleet, 0.7 with lean and
0.85 with both. This reduces the lost speed by 25%, 50% or 75%; it is not an
equivalent increase to the creature's base speed. Cobweb entanglement, potion
Slowness, terrain damage and other vulnerabilities remain. Native attributes
also govern compatible modded blocks and retain their own caps.

Both use distinct named transient bonuses beside existing attributes, rather
than replacing external bases/equipment. They update with expressed genes and
restore once after native loading, alongside the existing defenses. Apex copies
inherit the same rules. No synced flag, gene/allele reorder, save field, body
geometry or network-version change is needed. Gene hovers and the new 29th
Field Guide page explain the benefits and limits.

The extended native adaptation case checks actual full-block collision stepping,
hidden hoofed refusal, native soul-sand/honey speed factors, retained Slowness,
two loads preserving external bases and gene edits removing only their bonuses.
Actual build and all nineteen server scenarios pass; guide preview inspected.
Client movement feel, natural terrain travel and long-term balance remain
gameplay review.

## A5: powder-snow footing

Expressed snow coats acquire native powder-snow surface collision, as native
snow-walkable animals do. Woolly builds retain freezing protection without this
footing; hidden snow copies gain neither coat benefit. The same per-animal
native tag query enables Minecraft's powder-snow escape jump goal. Falling into
snow, standing beneath its surface and descending retain native collision rules.
This does not make falls safe. A6 below extends domestic routes only to
supported native-footing surfaces while retaining other hazard checks.

Only the native snow-walkable query gains the expressed benefit; ordinary data
pack entity tags and equipment protections still apply. Gene edits take effect
immediately on both sides, using the existing synced genome. No item, attribute,
saved field, gene ID, network version or creature visual changes are required.

A5 evidence (2026-10-18): actual Java 25 build/all 38 native scenarios pass.
The existing adaptation case moves real native collisions for expressed/hidden
snow, woolly, descending, already-inside snow and expression removal. Native
escape-goal eligibility changes with expression. The physical-trait guide
preview is inspected; natural snowy travel remains client gameplay review.

## Evidence and follow-up

The combined Gradle build and real GameTest server pass. Tests use native entity
interaction for syringes/shears, sample components, item wear, creative abilities,
health/age preservation, recovery refusal/persistence, actual lava/fire/fall
damage and fleece regrowth. The new icon preview was inspected. Full client
use, natural travel/reload and long-term farm balance remain gameplay checks.
No large genetics rerun is needed for unchanged inheritance tables; focused
existing diet/layout checks and the native behavioral cases cover this batch.

A2's extended native case checks actual poison/levitation application and refusal,
retained wither/poison/general-damage vulnerabilities, real cactus damage, hidden
alleles, changes to active effects when genes change, preserved bred stats and
readout descriptions. The combined build/server suite passes. This evidence does
not establish manual long-distance travel or multiplayer gameplay.

C3's native case passes with Senses +9/+10 boundaries, pup/injury refusals,
actual Brush use, exact yield/wear, preserved animal state, repeated-use refusal,
saved/missing recovery, last-charge breakage and creative use. The combined
build/server suite passes; client input and long-term farm balance remain manual
playtests. No large genetics rerun is needed for this isolated care rule.

## A6: supported powder-snow home routes

Residents with native powder-snow footing, normally an expressed snow coat,
can use the top of powder snow for home routines, meals and work. A standing
place still needs two empty dry body cells in the loaded home area; only its
powder floor gains the native support exception. Woolly cold protection and
hidden snow alone still give no footing. Native equipment/data-pack footing
continues to count. Descending animals do not claim surface support.

Interior POWDER_SNOW stays unsafe. Native ON_TOP_OF_POWDER_SNOW is accepted only
while native footing is present, with an additional native neighbor-hazard
check: the ordinary surface classification skips that check. Adjacent fire,
damaging floors, water/lava, missing floor, other hazards and complete loaded
home-route rules remain. A changed expressed trait/footing or removed floor
invalidates an already cached route before the job commits. No movement
costs, AI priorities, genome format, equipment or collision rules are changed.

Java 25 build/all 70 native scenarios pass. The new route case claims a real
home and creates a complete native meal path through a corridor requiring
ON_TOP_OF_POWDER_SNOW nodes. It checks supported work cells, expressed-trait
loss during that cached route, woolly-without-footing refusal, unsafe powder
interiors, a newly damaging floor, neighboring fire, removed support and native
descending. Restored footing/safety permits a real one-food/two-HP/normal-rest
meal. Existing physics, collision, climate-care and all other route cases stay
passing. Guide preview inspected; natural tundra navigation remains playtest review.

## A7: the End lineage's Phase legs

This completes the missing Phase ability in `DESIGN.md`, Phase 3's original
lineage plan. LEGS `phase` is appended at saved allele **6**, dominance 12,
ordinary physical scale 1.0. New End founders receive it with 75% probability
per copy. Existing animals, seeds and samples keep their exact genomes; the
allele neither enters common founders nor becomes a mutation target. Springy
legs still dominate it. Its ordinary wild trend is **+2 Leap / -1 Vitality**;
infused juice gives 45 seconds of Slow Falling, scaling with quality/Leap up
to five minutes. Ordinary breeding, sheets, samples and splicing support it.

A grounded living adult expressing Phase can blink after an actual nonlethal
attack from another living entity. The original health/absorption damage is
paid first; fall, fire and other environmental hits do not trigger it. Pups,
hidden copies and the Apex do not blink. Leads, rides and passengers prevent
it. Forty-eight bounded candidate positions seek dry supported landings three
to six blocks away, at most one block higher/lower and at least two blocks
farther from the attacker. A resident's landing stays within sixteen blocks
of its recorded Nest. Homeless adults can also defend themselves. This is a
teleport, so ordinary fences/walls are not guaranteed to contain a Phase line;
leads or baskets retain transport control.

Checks cover the loaded whole body and neighboring native path hazards,
collision, liquid, full-block support and world border. There is no terrain
load, path search, home reassignment or damage immunity. Native
`EntityTeleportEvent.EnderEntity` can cancel or redirect the attempt. The final
target, current genes, transport, origin and home are rechecked after that
callback; unsafe redirects retain the callback's state and cost no recovery.
An in-progress guard prevents recursive attacks from recursively blinking.

Successful movement stops navigation/knockback, emits portal particles and a
quiet native teleport sound, and starts **400 ticks / twenty seconds** of
recovery. `PhaseWait` stores bounded remaining ticks, pausing while unloaded or
packed; missing old data means ready. Recovery is reserved before movement
callbacks can save the animal. Sneak-Calipers shows this line's recovery or
transport pause. The native guide adds Phase on page 90 and a seventh, linked
Lineage traits index at 89, retaining all old page numbers.

Java 25 build/all **71** native scenarios pass. The added case exercises actual
attacks and movement, retained damage/home, recovery and legacy saves, hidden
genes, pups, leads/rides, lethal/environmental hits, the Apex, native event
cancellation, unsafe/unloaded redirects, callback gene/position changes and
hazardous landing refusal. Focused inheritance/format/trend/juice checks pass.
The 90-page guide preview is inspected. Client combat/particles remain playtest
review; this does not change the approved creature meshes.

## A8: swamp swimming and webbed paws

This completes the swimming/webbed-paw feature left in the original Phase 3
lineage plan. LEGS `webbed` appends saved id **7**, dominance 13, physical
scale 0.95. Fresh swamp founders receive it with 80% probability per copy.
Existing genomes are never rewritten, and common founders/mutations do not
create this wild allele. Springy mutation legs still dominate it; webbed and
Phase compete in the same inherited gene. Its wild trend is **+2 Swiftness /
-1 Leap**, with ordinary physical land speed about 3% lower and jumps 6% lower.

Expressed animals, including pups, gain a named transient **+0.5 Swim Speed**
modifier. On the ordinary native base of 1 this gives **50% stronger native
water propulsion**. External attribute bases/modifiers survive gene changes and
reloads; the bonus cannot accumulate. Air, drowning, lava and native water
physics remain. Domestic routes continue requiring dry safe ground; swimming
is an inherited physical adaptation, not permission for workers to dive.
Webbed infusion gives 90 seconds of Dolphin's Grace, scaling with quality and
Swiftness up to ten minutes and merging normally with matching juice effects.

Only expressed webbed legs render thin photographic skin membranes between
existing toes. The four optional leg-child parts add **72 quads / 288 vertices**;
all **2,172 approved base faces retain their exact coordinates and normals**.
Ordinary animals still submit exactly 8,688 vertices; a webbed animal with unmarked fertility submits
8,976 before A9 adds any separate tail mark. Existing paw/toe/nail asymmetry, body, face, ears and coat sources stay.
The source pipeline regenerates the mesh, all twelve 512px atlases, skeleton
and editable Blockbench model together. Model setup resets membrane visibility
for every animal and gives webbed water movement a paddling stroke. This uses
existing synced genomes, without a new payload or saved appearance field.

Java 25 build, model gate and all **73 native scenarios** pass. Real native
water travel establishes the 1.5 propulsion ratio and hidden-copy refusal;
actual drowning still damages a swimmer. Native reloads preserve one genetic
bonus alongside an external base, and gene/pup changes apply immediately.
Focused allele-table, inheritance/mutation, old-format, trait/trend/readout and
juice checks pass. The model gate exercises optional visibility, paddling,
ordinary reset, head lighting/UVs, winding/normals and planted gene/pup poses.
Actual-geometry paw/body and 91-page/seven-index guide previews are inspected.
Native client swimming/animation and natural swamp populations remain playtest review.

## A9: visible fertility phenotypes

The original genome/Phase 2–3 design left Fertility's visible cue undefined.
Its existing alleles now have distinct small **tail-tip marks**:

| Expressed Fertility | Visible tail tip | Optional faces |
|---|---|---|
| average | Original unmarked tip | 0 |
| prolific | One open rose-colored rosette | 40 |
| shy | One closed green bud | 18 |
| swarm | Three small rose-colored rosettes | 120 |
| starborn | One violet star-shaped flower | 38 |

These complete the core rule “genes are visible; tools give exact measurements.”
They follow the existing expressed allele on adults/pups and the source-defined
Apex. A hidden copy never draws its mark; ordinary dominance/breeding/splicing
apply. They indicate **fertility phenotype**, independently of age, love,
recovery, current mating eligibility or bred Bloom points. No breeding number,
genome id/order, timer or saved appearance field changes.

Four optional tail-child parts inherit normal tail motion and pup transforms.
Model setup selects exactly one or none for every animal, resetting the shared
renderer before another animal is drawn. The source generator owns their geometry,
small matte color patches, all twelve atlases, binary mesh, skeleton and editable
Blockbench visibility. The fixed photographic face/body and all 2,172 approved
base faces retain exact positions and normals. An unmarked non-webbed animal
still submits 8,688 vertices. The combined source mesh stores **2,460 faces /
9,840 vertices**, including every mutually exclusive mark and optional webbing;
the largest rendered combination (swarm + webbed) is **2,364 faces / 9,456
vertices**. There are no distance/quality presets or extra rendering passes.

Gene hovers describe each real mark, and guide page **92** gives the complete
visual key, linked from Lineage traits. The guide now has **92 pages, seven
indices and 63 chapter links**. `art/preview_fertility.py` renders actual exported
tail-tip geometry/atlases; that preview and the current guide are inspected.

Java 25 build/model gate/all **73 native scenarios** pass. The model gate draws
all fertility/webbing combinations, verifies exact per-variant budgets and resets,
and retains winding, normals, UVs/head continuity, smooth rendering and planted
leg/pup checks. Focused genetics checks verify distinct existing alleles, hidden
copies, inherited starborn, old-format reading and real hover descriptions.
Client appearance remains playtest review.

### Client compatibility for A7–A9

Network protocol is **8**. Payload layouts and save formats stay the same, but
older clients cannot parse Phase/webbed allele ids. Requiring the updated client
and server prevents silent wrong traits or unreadable sheets. Existing genomes,
items, animals and worlds retain their data; new lineages supply the new legs.

## A10: coat camouflage

The original coat-camouflage plan now gives existing expressed colors a local
survival benefit. A **full-health, calm, grounded** animal on matching dry support
has **half normal native visual acquisition range**, and its **STEP** game events
are suppressed before native sculk/Warden listeners receive them. The ordinary
visible coat and walking sounds remain. Native targeting retains its two-block
minimum and any selector that explicitly ignores visibility keeps its behavior.
Already acquired targets, damage, collisions and Warden sniffing remain native.

| Expressed coat | Matching support |
|---|---|
| tan, cream, sand | Sand/red sand and sandstone forms |
| brown, spotted | Exposed dirt, coarse/rooted dirt, podzol, path/farmland, mud/roots |
| moss | Grass block, moss/pale moss and carpets, leaves |
| snow | Snow layers, snow blocks, powder snow with actual standing footing |
| pale, seal | Stone, diorite/polished diorite, calcite, tuff |
| slate | Deepslate forms, blackstone forms, basalt forms |
| ember | Netherrack, nether/red nether bricks, nether-wart block, crimson nylium |
| void | Obsidian/crying obsidian and purpur block/pillar |
| glow | Luminous; cannot blend |

The eight `bunnidogs:camouflage/*` native block tags are editable by datapacks.
Support is one actual loaded `getOnPos()` block; air and fluids do not qualify.
Natural ambient light and spawn biome do not impose additional requirements.
Hidden copies grant no second cover; a hidden glow copy behind pale does not
light the coat. Pups inherit the same expressed camouflage.

Injury, carried bundles, current hurt animation, target/refuge danger, love/guava memory, sprinting,
fire, Glowing effects, water/lava, jumps, leads, vehicles and passengers break
cover. Native recent attacker memory also disturbs a freshly healed animal for
the first five seconds after its attack. There is no new recovery timer to save.
The Apex is excluded. Changing genes, footing or readiness is effective at the
next actual visibility/event query; no per-tick environment scan or home claim
is added. Sneak-Calipers explains blended/exposed/disturbed/luminous status.

Only walking **STEP** events from that blended animal are canceled. Eating,
attacks, landings, tools, other entities and other vibrations still reach native
listeners. Native damage/effects and other mods' visibility multipliers compose
normally. No invisibility effect, altered navigation, save field or mesh is added.

Native fixtures cover every shipped coat/tag, hidden copies, pups, support and
health/love/Glowing/transport/combat changes, exact read-only inspection and
native save derivation. Actual native targeting demonstrates reduced long-range
acquisition, retained close detection, invisibility composition and real damage.
Actual movement and a real sculk sensor demonstrate silent blended STEP dispatch,
retained eating frequency eight and restored exposed STEP frequency one. This
passes with the full 85-case implementation checkpoint. Natural Warden encounters
and client gameplay remain manual review.

## A11: inherited Island oxygen and swimming

The isolated Island lineage appends COAT `seal` (ID 12), LEGS `paddle` (ID 8)
and DIET `shore_forager` (ID 12), preserving every existing saved ID. Expressed
seal adds a named native +1 Oxygen Bonus: ordinarily twice the air duration on
average, with normal 300 maximum air and retained drowning. Native Water
Breathing and external bonuses compose; expression/loading never refills air.
Paddle adds +1 Swim Speed, expressed webbing/paddling and shorter 0.85-scale
land legs. Its land speed/jump are 0.91×/0.82× before bred stats. Swamp webbed
keeps its existing independent allele and +0.5 Swim Speed.

Hidden copies stay hidden. Seal uses gray wool for woolly fleece, matching
pelt/rug/Jar art and stone camouflage. Its juice grants Water Breathing;
paddle juice grants Dolphin's Grace. Both use existing infusion quality rules.
Native movement, actual air depletion/drowning, effects and attribute
save/gene changes pass alongside the model gate. The approved sculpt and face
are preserved; coastal adult/pup previews are inspected. See `ISLAND_SPECS.md`
for full genetics, source, siting, supplies and dry-bank kelp care.

## A12: pale-coated cave guidance

In dim light, sneak-use **one glow berry** on an ordinary calm adult expressing
COAT `pale` with **Senses +20**. The keeper receives native **Night Vision for
60 seconds** and the animal rests for **30 seconds / 600 ticks**. This turns
the original cave-night-vision idea into a useful breeding goal: native mobs
already perceive targets in darkness, while their keeper benefits from sight.
Any keeper may ask; there is no home, ownership or cave-biome lock.

The animal must be alive, at least half health, in the same level and within
six blocks. Threat/target/refuge, love and the first five seconds of recent
attacker memory refuse. The player's native local brightness must be at most
seven. Daytime/sky checks are not substituted for actual light. Existing Night
Vision of any strength/duration refuses without shortening it or charging.
Leads and transport do not themselves forbid this stationary care interaction.
Ordinary interaction/build permission still applies; the Apex is excluded.

The cost comes from the actual interaction hand before native effect callbacks.
An in-progress guard prevents reentry while cost/effect/refund is unresolved.
Creative retains food. Only a successful native application starts recovery,
plays eating feedback and awards **By Pale Lantern**, with native once-only
25 XP. A denied effect returns the exact berry/components to the same suitable
hand, otherwise inventory/normal drop, without overwriting callback-replaced
items or starting recovery. There is no ambient aura or free repeated buff.

Optional `CaveSightWait` saves remaining recovery clamped to 600 ticks; missing
legacy data means ready. Whole-animal Travel Baskets retain it through existing
NBT storage. The timer is care state rather than an inherited seed/sample
field. Sneak-Calipers reports pale animals' readiness/recovery without spending
food or applying an effect. No new client entity-data slot or effect type is
needed. Current protocol **14** also requires the Island allele meanings.

The native case exercises a real enclosed dark room and local light engine,
Senses 19 refusal, retained existing stronger vision, actual payment before
reentrant native denial, hand replacement/exact refund, successful duration,
recovery/native save, Creative stock and bright refusal. Build/model/all **95**
native cases pass at the combined checkpoint. Guide previews are inspected;
native client controls/effect appearance and ordinary cave journeys remain
gameplay review.
