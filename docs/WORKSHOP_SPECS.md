# Breeder workshop — J1/J2/G5/D12/T5

**Current (2026-10-24):** native inventory slots cover Juicer, Table, Jar and
populated Pedestal stock. Compact live controls handle analysis, signals and Apex
preparation. Protocol **23**; optional
Field Guide **141 pages**. Table goals connect samples to real commission targets;
requested pad/ring markers expose the working spaces. Historical J1/G5 counts
below describe older checkpoints, not current compatibility.

This batch completes older `DESIGN.md` ideas for a Juicer, richer juice, named
lineages and Specimen Jars, and develops the pelt-rug hook in `DROPS_PLAN.md`.
The keeper's workshop connects existing fruit, litter seeds, sample collections
and hides. These are implemented features; native client playtesting is separate
from the build, server tests and generated art previews below.

## J1 — Juicer

Craft `PIP / PBP / CCC`: four planks, one iron ingot, one glass bottle and three
copper ingots. The screw press faces its placer, rotates/mirrors normally and
needs no fuel. There are two recipes:

| First input | Second input | One output |
|---|---|---|
| Any guava | Glass bottle | Matching-tier uninfused guava juice |
| Valid litter seed | Any uninfused guava juice | One frozen infused juice |

- Each pair takes **100 loaded ticks (five seconds)**. One seed costs its whole
  potential litter, exactly as hand infusion does. Infusion rolls once at
  completion, with the seed's mutation/Mutagen rules and inherited stat drift.
  Reading a station never rolls or previews a particular offspring.
- Each input holds one exact component-sensitive stack, up to sixteen items
  or the item's own smaller limit. Output holds up to sixteen matching carriers,
  or one infused bottle. Incompatible/full output waits without consuming input.
  Invalid pairings also wait. There is no rerolling a completed bottle.
- Right-click inserts as much of the actual used hand as fits. Survival pays
  before commit notifications; creative keeps the actual stack. Empty-hand use
  opens its actual input/output slots and player inventory, with a visual
  processing bar. Choose exact items or shift-click; output rejects insertion.
  Opening never withdraws or spends ingredients. Sneak with **both hands empty**
  recovers both input stacks. Adventure use cannot move stock.
- Hoppers above insert fruit/seeds; horizontal hoppers insert bottles/carriers;
  lower hoppers extract only output. Inputs cannot be extracted by automation,
  nor can output be inserted. Every port wraps the same three saved stacks.
- The two costs and output share one native root transaction. Aborted transfers
  restore exact components. An output reservation cannot expose an unpaid
  product in the inventory's root-commit notifications.
- Redstone **pauses processing**, retaining current progress. Hands and hoppers
  continue working. Changing an input's item/components resets progress; count
  changes alone do not. Chunk unloading pauses the clock. Saves carry stock,
  bounded progress and the exact one-item working pair.
- Comparators read **output stock**: zero empty, otherwise
  `1 + floor(14 * count / min(16, item maximum))`. One infused bottle gives 15.
  The green press indicator marks active processing, not completion.
- Breaking/replacing the station drops its remaining three stacks plus normal
  block loot. The station resists piston movement.

The existing plain-juice crafting recipe and two-hand seed infusion remain
available. A custom random crafting-result recipe is unnecessary: the Juicer
provides the original plan's alternative without rerolling crafting previews.

## J2 — Tiered juice

All eight guava tiers have matching carriers. Preserve the original
`guava_juice` and `infused_guava_juice` ids; append seven richer carrier ids
`<tier>_guava_juice`. Each crafts shapelessly from its own guava and a bottle,
or presses in J1. Icons keep the shared bottle and use distinct liquids/labels.
Crafting unlocks follow the corresponding fruit; carriers appear in Food/Drinks.

**Infusion quality = max(seed quality, carrier tier quality).** The tier qualities
remain 0, 1, 2, 4, 7, 12, 20 and 33. The seed alone supplies the Mendelian roll,
mutation chance, parental stats and drift quality. Thus a rich carrier improves
the potion's duration/amplifier within the existing effect caps, while stronger
inherited stats still require breeding. A weaker carrier never lowers a seed.
Both hand infusion and J1 use the same pure `Infusion.roll` overload.

All uninfused carriers restore the same little hunger and return their bottle.
Their investment is useful for infusion. Survival consumes the seed and carrier;
creative keeps both and receives the result, following the existing hand rule.
Infused juice cannot be infused again. Existing infusion serialization is
unchanged: the final quality already fits `genome/stats|quality`.

## G5 — Keeper-named lineages

Open a Pedigree Book or Purebred Registry, choose a record, then **Name lineage**.
Enter at most **40 Unicode code points**; empty text clears it. Names are local
to that book and exact recorded genome, including hidden copies. Every matching
record in the book uses the name. A different genotype needs its own label.
These are personal breeding labels, separate from wild habitat lineages, entity
names and generated Ear Tag codes. They make no inheritance or registry claims.

Names appear in contents and sheet headings, with full hovers when clipped.
Ordinary words search names alongside traits; `line:text` searches only names.
Age, stat, shown/carried and tag predicates retain their usual meanings. The
name editor's save waits for a server acknowledgement. Escape/Cancel closes it.
Edits already submitted to the server may complete after closing the window.

The server accepts only the indicated actual held Pedigree Book/Registry, with
exactly the expected record and previous-name components, and a genotype already
present in its valid records. Changed books or concurrent edits refuse and ask
the player to reopen. The client never supplies new pedigree records. Names trim
outer space, normalize Unicode to NFC and reject controls/formatting or excess
length. The edit/result payload fields have explicit codec bounds.

An optional persisted/synced **`lineage_names`** component stores at most 24
`genome~Base64url(UTF-8 name)` entries separated by semicolons. Names can contain
punctuation used in older formats. Malformed entries are skipped; only named
genomes in the current book are read. Pedigree text, animal snapshots, UUIDs,
parents, stats and registry qualification stay unchanged. Re-logging preserves
names. Copies/trades preserve the item component. Old books simply have no names.

G5 introduced two packets at **protocol 9**. Current G6/S11 content requires
**protocol 10** on both sides; these edit/result packet shapes are unchanged.
No genome/animal or existing item format is replaced.

## D12 — Coat pelt rugs

Two **matching** coat pelts plus one string craft one `<coat>_pelt_rug`, with
all twelve coats available. Mixed pelts retain their original two-pelts-to-leather
recipe; they cannot recolor a rug. Rugs keep pelt rarity and appear under
Functional Blocks. Native recipe unlocks follow each corresponding pelt.

Rugs use native carpet height, support, placement and support-loss drops, with
four facing variants for the hide's paws/tail. A cutout fur outline and each
coat's palette/pattern distinguish the set. `pelt_rugs` block/item tags and native
carpet tags describe them. There are no inherited animal defenses or light
emission from a hide. The rug is decorative and consumes real matching materials.

## C3 — Current preserved-sample and display controls

Empty hands inspect Jars and populated Pedestals without withdrawing anything.
Native slots preserve the chosen sample/display item and retrieve the complete
original; a concise Read action opens its existing reader. Direct actual-hand
insertion remains. Displays accept one held item when empty; an empty display
has no menu. Crouched-empty retrieval
remains for both. Permission, reach and exact live-target checks apply to direct
and menu transfers. Complete components, legacy counts and saved samples stay.

Pedigree Comparators likewise inspect without switching mode. Free copy/read,
explicit exact/analog modes and clearing expose the same real gene-match signal;
ordinary power polls do not invalidate a mode click. Show front pad marks the
actual one-animal block. See `COHESION_PLAN.md` for current evidence and scope.

## T5 — Specimen Jars (original mechanics; current controls above)

Craft ` P / G G /  G ` from one wooden slab and three glass for **two jars**.
Insert one readable Tissue Sample with either hand; creative keeps its real
sample stack. Full jars retain the original sample and open its full sheet on
use, including Calipers/other item use. Empty-hand reading also opens it. Sneak
with **both hands empty** to recover the sample. Adventure can read but cannot
store/withdraw. No hoppers or sample copying are added.

The small capped glass jar visibly holds a vial in the sample's expressed coat.
Opening its sheet shows the complete inherited phenotype, genes and stat
snapshot. The static cutout model needs no ticking display entity/renderer.
Visual state ids follow the saved `Gene.COAT` allele order, independently of the
art palette's dictionary order. Empty is zero; coat id + 1 selects the vial.

Comparators read zero empty, otherwise **`1 + 2 * fixed gene pairs`**, reaching
15 for seven matching pairs, including Diet. The signal describes the sample,
not an animal's age, birth parents or Registry eligibility. Samples retain
their entire item/components through storage and native saves. Breaking the
jar drops both the ordinary empty jar and its exact original sample. Invalid
legacy stored samples remain recoverable but do not supply a phenotype/signal.

## P8 — Guide and evidence

Fresh Field Guides have **98 pages, eight indices and 72 chapter links**.
Pages 93–97 explain J1/J2/G5/D12/T5; 98 indexes the breeder workshop. More chapters
and Lineage traits link there. Older numbers remain; page 24 and new workshop
chapters return to 98. Existing written guides retain their saved pages; craft
a fresh guide for the appended chapters.

`art/gen_workshop.py` generates tier carriers, recipes/unlocks, all block/item
models, rugs, jar states, tags, text and `workshop_preview.png`. Its model preview
uses the generated geometry/textures with a substitute native iron color.
`gen_guide.py` owns translated guide text/preview. The previews are inspected;
native client appearance, text entry and reading are still manual playtests.

Native scenarios cover every tier recipe/press, real three-direction hoppers,
output waiting/abort, saved partial work and power pause; acknowledged edit
validation, punctuation/Unicode names and native book/guide codecs; all twelve
rug recipes/placement/support drops; jar coats, exact sample saves, creative/
adventure/off-hand use, real comparator outputs and exact destruction drops.
`checkWorkshopAssets` validates shipped model/texture references, complete states
and saved coat-id colors. The breeding-lab check covers named page bounds,
hovered clipping and search semantics. See `START_HERE.md` for the current result.
