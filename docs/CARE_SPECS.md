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

- A healthy adult expressing BUILD `woolly` yields two `minecraft:white_wool`
  with ordinary shears. Health must be at least half maximum. Pups keep their
  fleece; ordinary builds and hidden woolly alleles do not yield it.
- NeoForge `IShearable` integrates with native shears, including tool wear and
  drop spawning. One successful use costs one shear durability and starts ten
  minutes / 12,000 ticks of regrowth. Refusals cost no durability.
- The animal remains alive and healthy. Gathering loose fleece keeps the
  approved sculpt intact; no new shaved model or coat atlas is introduced.
  Pelts and all death-drop yields remain the existing separate reward.
- `FleeceWait` saves clamped remaining ticks. A synced readiness boolean handles
  client prediction and becomes ready when server time reaches the deadline.
  Missing old data means ready; regrowth pauses across unloading. Network version
  5 introduced that sync; current protocol 6 also includes splicer selection and
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

Sneak-use Calipers on an animal to print a server-side chat snapshot of all four
care activities: sampling, fleece, grooming and splicing. Each row gives the
current eligibility requirement or, if eligible, ready/recovery time. Recovery
uses rounded-up minutes:seconds, so a partial second is not reported as ready.
Splicing readiness describes the recipient; a donor sample and Mutagen are still
needed. Normal Calipers use opens the existing genetics sheet. Reading changes
no animal, items or timers and introduces no payload, saved data or sync field.
The actual Gradle build passes; client use remains a manual gameplay check.

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
body multipliers stay unchanged. Cold protection prevents freezing damage, not
sinking in powder snow. Hoofed legs reduce falls rather than making them harmless.

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
