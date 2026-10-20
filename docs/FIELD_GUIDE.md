# Optional Field Guide

The core game must be playable without this book. Empty-hand animal and station
panels, short context hints and native milestones teach care, housing, courtship,
seed planting and hatching. The guide keeps exact genetics and advanced systems
available for players who want them. See [the cohesion plan](COHESION_PLAN.md).

## Acquisition

Craft **book + paper + any guava** for the Field Guide. **Book + plain guava**
unambiguously makes the Pedigree Book. The extra paper removes the old overlapping
recipes. Guavas unlock the guide recipe. Existing pantry loot and Breeder offers
remain; opened containers are never refilled.

## Current contents and compatibility

The preset still has **141 pages, thirteen indices and 114 chapter links**. C1/C2
adds no pages. Its opening says Start playing and points to live controls; the
Cradle, Pen, partner and Board chapters now describe visible menu actions. Normal
held-item and crouched shortcuts remain useful optional paths.

| Reference | Index page |
|---|---|
| Main contents / first steps | 1 / 2 |
| More chapters / newer systems | 40 / 50 |
| Habitats / resident work / specialist work | 56 / 66 / 84 |
| Lineage traits / breeder workshop / fieldcraft | 89 / 98 / 101 |
| Sites / wild origins | 112 / 117 |
| Trail partners / commissions | 125 / 130 |

Earlier destinations remain stable. Native WrittenBookItem, WRITTEN_BOOK_CONTENT,
book packets, local page links and filtering stay in use. The default content
updates with the mod; deliberately overridden saved contents keep their own pages.
There is no book migration and no new book-specific payload or screen.
The item tooltip explicitly calls it optional reference. Current protocol **16**
belongs to the care menus; the book retains its existing native save format.

Art/text source: `art/gen_guide.py`. `art/guide_preview.png` uses a substitute
font and covers the revised introduction and affected chapters. Native customized
book serialization retains all 141 pages in the integration suite. A preview and
headless serialization do not establish client typography or navigation comfort.

Historical chapter expansions and their older evidence are preserved in
[the pre-cohesion ledger](history/FIELD_GUIDE_PRE_COHESION.md). Those historical
page counts and protocol numbers are not current gameplay instructions.
