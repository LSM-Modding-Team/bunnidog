# Gene Splicer G1

Implemented 2026-10-16 under the user's standing design latitude. The splicer
helps introduce an existing donor allele into a breeding line. It preserves
the need to breed homozygotes and develop bred stats.

## Crafting and use

Craft `DMD / CIC /  I `: two diamonds, one Mutant Marrow, two copper ingots and
two iron ingots. The rare tool has 16 uses, appears in Tools & Utilities and
unlocks when Mutant Marrow is obtained. Command: `/give @s bunnidogs:gene_splicer`.

1. Sneak-use in air or on a bunnidog to cycle the seven genes. The default is
   Ears. Tooltip/action-bar text shows the selection; it persists on the tool.
2. Hold a donor Tissue Sample in the other hand. Its **expressed** allele of
   the selected gene is the source, whether wild, common or rare. Hidden donor
   alleles must first be expressed through breeding to become a source.
3. Carry one Mutagen and use normally on a living adult at least half healthy.
   One successful splice consumes the sample, one Mutagen and one durability.
   The recipient needs one minute / 1,200 ticks of recovery between splices.

Only the selected gene changes. Replace the recipient copy with lower dominance;
ties replace its first copy. The remaining copy is kept. If the donor allele
is already in either copy, refuse without cost or recovery. A second splice
therefore cannot force a homozygous pair: breed carriers to fix the trait.
The inserted allele may be hidden by the retained copy's dominance.

Bred stats and birth-parent records remain the recipient's. Changing Build
preserves its percentage of maximum health. Other gene effects, expressed
adaptations and normal inheritance follow the resulting genome immediately.
No special allele IDs or altered inheritance rules are introduced.

## Persistence and refusals

- `bunnidogs:splice_gene` is a persistent, synchronized integer item component.
  Missing selection means Ears; invalid integer values wrap safely across the
  seven choices. Current network protocol 7 requires matching updated clients
  and server. Existing genome/sample/seed formats are unchanged.
- `SpliceWait` saves remaining recovery, clamped to the interval. Missing old
  data means ready; recovery pauses across unloading, like other care timers.
- A pup, injured recipient, recovery, missing donor, missing Mutagen or an
  already-carried allele is refused before edits, consumption or durability.
  Sneak-use selects a gene rather than editing an animal.
- Creative still needs a donor sample carrying the source data, but preserves
  items/durability and does not require carried Mutagen. Animal recovery applies.
- Successful and refused actions explain their outcome in the action bar.

## Evidence and next work

The real Gradle build and GameTest server pass. The focused case checks exactly
one copy changes across all seven genes, repeated-splice refusal, native item
interaction, material preflight/costs, high donor stats staying uncopied,
relative health, edited-genome/recovery saves and persisted tool selection.
The icon preview was inspected. Full client controls/multiplayer use remain
gameplay review. T5 now adds a read-only Genetics Table preview: place recipient
and donor samples in A/B, then use the table with a Gene Splicer to jump to exact
before/after pairs in both directions. All normal table reads can reach it with
Next. Samples are not consumed; a live recipient can have changed since sampling.
See `TABLE_SPECS.md` 7. Native creative selection on animals now writes to the
actual tool rather than the temporary stack copy supplied by Minecraft; a focused
server regression case covers it.
