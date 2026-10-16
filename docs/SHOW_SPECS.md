# Show Ring G3: Judging Stand and ribbons

Implemented 2026-10-16 under standing continuation/design authorization. A stand
marks a player-built show area; no prescribed multiblock ring or village rewrite
is required. This adds a non-lethal goal after stable-line registration.

## Rules

- Craft a Judging Stand: ` B /PGP/PWP` (B vanilla book, P any planks, G gold
  ingot, W any wool). Obtaining a Purebred Registry unlocks the recipe.
- Use the stand with an empty main hand or a lead. The nearest living adult
  led by that player within six blocks is judged; distance ties use UUID order.
  Keep its leash, home, health, genes, breeding state and stats intact.
- Sneak-use with **both hands empty** for a read-only action-bar scorecard:
  current stat tier, a limiting stat/point value and fixed-pair count. This
  preview can examine unfinished lines without awarding or changing anything.
- Require recorded birth parents, all seven gene pairs fixed (including Diet),
  and at least half health. The animal need not already be logged in a Registry.
- **Bronze:** every one of the five bred stats at least +5. **Silver:** every
  stat at least +20. **Gold:** every stat at least +50. This judges balanced
  points rather than body multipliers or a single heavily bred specialty.
- Award just the highest eligible ribbon. Save the animal's highest awarded
  tier as optional `ShowAward` (0 absent, 1 bronze, 2 silver, 3 gold). Rejudging
  the same/lower tier gives nothing, even at a different stand or after reload.
  A later higher qualifying tier can earn one new ribbon. Unknown positive
  future tier IDs remain saved, preventing downgrade/reward loops.
  Judging never increases points; improving a line normally means breeding a
  new representative, rather than repeatedly entering the same animal.
- No per-judging material cost, XP payout or recovery timer. Each successful
  gold award can grant the judging player the one-time **Champion Line**
  husbandry advancement (100 XP). Awards are trophies, not substitutes for death
  legends or sources of their materials.
- Refusals explain missing lead/adulthood, birth parents, mixed genes, health,
  minimum points or already-awarded tier. No animal is damaged or consumed.

## Ribbons

Three unstackable items hold the winning representative's exact genome, UUID,
ear tag, five bred stats and birth-parent genomes using existing `pedigree_data`.
Use air to read the snapshot; tooltips show its points and tier requirement.
Display on an existing Trophy Pedestal or a native item frame. Dropping, saving,
moving or displaying a ribbon must preserve its snapshot. The stand stores no
item or block entity; breaking it drops the stand itself.

Old animals load as unjudged; no genome/allele/item-payload format or protocol
change is required. Approved creature geometry stays intact. Art comes from
`art/gen_show.py`.

## Evidence

The actual Gradle build and GameTest server pass. Focused pure checks cover each
tier boundary and a limiting stat in each of the five slots. The native show
case exercises lead/empty-hand interactions, missing parents, mixed Diet,
injury, insufficient points, read-only preview, bronze/silver/gold awards,
repeated claims, animal preservation, highest-tier save/load, legacy/future
tags, native Champion Line progress and the winning snapshot on a saved Trophy
Pedestal. New assets and the model-box/texture preview were inspected; this is
not an in-game screenshot. Client tooltips/readers, physical stand lighting and
multiplayer input remain gameplay review. No large genetics rerun was needed
for unchanged inheritance and body rules.
