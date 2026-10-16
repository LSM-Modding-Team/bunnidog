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
  5 requires matching updated clients and server.

## A1: Expressed environmental adaptations

| Expressed trait | Benefit |
|---|---|
| COAT `ember` | Native fire immunity, including lava/magma/fire-tagged damage |
| COAT `snow` | No powder-snow freezing |
| BUILD `woolly` | No powder-snow freezing; renewable fleece above |
| LEGS `hoofed` | Half the usual fall-damage multiplier, with native rounding/hooks |

Benefits follow the expressed allele after dominance, so an ember/snow dog
expressing ember does not also gain snow's cold protection. Separate genes can
combine, for example ember coat with woolly build. Spawn biome and founder
lineage do not hardcode benefits. Effects update with the expressed genome;
native equipment/tag protection and damage hooks still apply.

`Plain.effect` explains these benefits on sheets and forecasts. Existing gene
slots, allele IDs, inheritance, stat drift, juice effects, genome formats and
body multipliers stay unchanged. Cold protection prevents freezing damage, not
sinking in powder snow. Hoofed legs reduce falls rather than making them harmless.

## Evidence and follow-up

The combined Gradle build and real GameTest server pass. Tests use native entity
interaction for syringes/shears, sample components, item wear, creative abilities,
health/age preservation, recovery refusal/persistence, actual lava/fire/fall
damage and fleece regrowth. The new icon preview was inspected. Full client
use, natural travel/reload and long-term farm balance remain gameplay checks.
No large genetics rerun is needed for unchanged inheritance tables; focused
existing diet/layout checks and the native behavioral cases cover this batch.
