# Animal care and inherited adaptations

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
  5 introduced that sync; current protocol 7 also includes splicer selection and
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
| slate | 2 gray wool |
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
