# P1 — Bunnidogs Field Guide

The growing progression needs an in-game reference. The Field Guide covers
founders, genes, breeding/seeds/quality, pedigree, Nests and transport, food,
bell gatherings, living care, adaptations, table/splicing, stable lines,
shows, materials, legendary drops, the Apex and infused juice.
The gardening pages include fitted corner troughs, Bloom cultivation, berry
regrowth and the existing work-rest/filter/protection rules.
Furnished warrens also explains the optional empty Cradle in new grand-tree nurseries.
Living-dust care has its own page explaining combined yields and shared recovery.
That living-harvest page now includes moss coats at Bloom +10 yielding moss
carpet alongside qualified whiskers/dust under one tool use and recovery.
The terrain page explains inherited stepping and partial slow-block resistance.
Physical traits also explains expressed snow coats walking on powder snow,
beside snow/woolly freezing protection; A5 updates that page without renumbering.
Shore fishing explains resident Fisher prey, needs, healing and recovery.
Resident needs explains the Calipers home/food/recovery snapshot.
Refuge explains non-feral residents' bounded response to visible threats.
Storm shelter explains idle covered waiting and its priority below care/work.
Trough automation explains hopper supply/drain, food retention and stock signals.
Bell calls explains daytime ringing, safe walking space and visit recovery.
Fleece colors explains expressed palettes, mixed spotted yield and Calipers.
Trough filters explains exact reservations, withdrawals and clearing while empty.
Care dispensers explains one-animal pulses, shared recovery and hopper collection.
Travel Basket explains packing, safe release, retained identity and Nest release.
Record/registry/basket pages explain deliberate sneak-use for carried records.
The litter page explains dispenser seed planting and Starlit Dust ripening.
Pages 41–42 explain the craftable Nursery Cradle, saved incubation, withdrawal,
growth tools, hopper/comparator controls and safe hatching with a fresh pulse.
The safe-hatch page also explains sneak-Calipers pad checks/temporary markers.
Page 43 connects surplus resident food to native composting, its eight-item
reserve, crop priority, shared work recovery and ordinary hopper bone meal.
Page 44 explains in-place hand/dispenser Mutagen, retained incubation and
success-only costs, including treatment of a ripe litter before hatching.
Page 45 explains the reusable bone command for feral Nest guards, their
half-health requirement, bounded defense and Calipers status.
Page 46 explains native journeyman-cartographer Warren Explorer Maps, price,
the village marker and Overworld structure limits. Page 2 points to it.
Page 47 explains bred bat-ear mineral surveying, six-block range, shared rest
including empty surveys and persistent compass places after mining. The first
index labels the exploration chapter **Warrens & scouts**; page 2 points to both.
Page 48 explains Senses +20 mineral cues and shared survey recovery. Page 47
points to it; all older page numbers and twenty chapter links remain stable.
Page 49 explains the redstone resident pause, preserved automation and the
independent hoe setting. The automation page distinguishes powering a hopper
from powering the trough and points to this new explanation.
S10 updates the existing record page with local trait/tag/stat filtering and
the input's syntax hover. Page count and existing links remain stable.
The home page also covers read-only Nest inspection and persistent compass marks.
The food page explains short safe nighttime trough meals near home.

Craft one with a vanilla book and any guava (the existing bunnidog-food tag).
Obtaining a guava unlocks its recipe. An additional independent 35% pantry loot
pool can supply one to explorers; it stays independent of food rolls. New
lineage-specific stores and unopened legacy pantries use it. Filled or emptied
containers are not refilled and player inventories are not edited. See
`SETTLEMENT_LIFE.md` L6 for local food and new surface feed stores/markets.

## Presentation and saves

Eighty-six pages use the ordinary written-book screen. Six linked contents pages
offer fifty-four clickable chapter links. Each chapter links back to its index. Local page changes
are the only click action. All text is translatable through en_us.json. The
book stacks to one and appears beside the Calipers in Tools & Utilities.

The item inherits native WrittenBookItem and supplies the existing
WRITTEN_BOOK_CONTENT component as an item default: native book packets, tooltips,
saves and filtering apply. There is no new screen, payload, server handler or
custom data component; current protocol 7 is shared with table planning. Default content follows future mod
updates unless a command explicitly overrides the component.

Art/content source: `art/gen_guide.py`; regenerate with Python 3/Pillow.
`FieldGuideItem.PAGE_COUNT` and its contents links correspond to the authored
numbered pages. Keep pages short enough for the native 114x128 text area; book
resource-pack fonts may differ. The source preview uses a substitute font.

## Evidence

The actual Java 25 build and current sixty-eight native server cases pass,
including loading the book/recipe/loot resources. The expanded 86-page
resident-pause/Cradle-control/Specialist/mode preview is inspected with a substitute font; all
three indices were also inspected for P4. Earlier
pages were shortened using the conservative native text-area estimate.
Native WrittenBookItem, ServerPlayer, ClientPacketListener and BookViewScreen
sources establish the ordinary book/packet/click behavior; no new screen or
server scenario is required for added page text. Each pushed GitHub build's
exact-commit artifact is checked.

Actual client navigation, font rendering and natural pantry discovery still
need gameplay review. Server loading does not establish these client details.

## P3 — Find the newer chapters

The original contents became crowded as new living-care and habitat mechanics
were added. Page 1 now links to eight core chapters and **More chapters** on
page 40. That second index links to ten newer chapter groups, from juice and
gardens through resident needs, care stations and Travel Baskets, plus a return
to **Main contents**. All existing content stays on pages 2–39.

Pages 2–23 return to page 1; pages 24–39 return to page 40. All navigation uses
native `ClickEvent.ChangePage`. The item default updates ordinary existing
guides; deliberately overridden written-book components retain their contents.
The Java/resource build validates this small presentation change. The preview
covers both indices and the basket chapter; actual native fonts remain client
review. No new server mechanics or saved format are introduced.

N3 appends pages 41–42 and a ninth main-index chapter, Nursery Cradle. Both
new pages return to page 1; the second contents page and all earlier chapter
numbers stay stable. The guide now has nineteen chapter links.
L14 appends page 43 under the same main-index chapter, renamed **Nursery &
compost**; it also returns to page 1. Earlier page numbers remain stable.
N5 appends treatment page 44 under the same index, returning to page 1.
L16 appends guard page 45 and a tenth main-index chapter. Both contents pages
now have ten chapters; earlier page numbers and native links remain stable.
E1 appends map page 46 under the warren/exploration chapter, returning to page 1.
E2 appends scout page 47 under the same chapter, also returning to page 1.

## P4 — Nursery, resident work and exploration index

Page 50 is a third contents page for the newer systems: Nursery & compost,
in-place litter treatment, Nest guards, Warren Maps, Mineral scouts and
the trough work pause. Main contents returns to its eight core chapters and
links to both **More chapters** and **New chapters**. Page 40 retains its ten
established links and return to Main contents. The new page has six chapters
and its own Main return, for twenty-four direct chapter links in total.

Content pages 41–49 now return to page 50; pages 2–23 still return to Main and
24–39 to More. Earlier content page numbers remain stable, including the
warren/map/scout references and automation/pause reference. Native local
`ClickEvent.ChangePage` links, written-book defaults and intentionally overridden
book components retain their existing behavior. There is no screen, packet or
save migration. The generated three-index preview is inspected; native font
and navigation remain client gameplay review.

E5 appends page 51, **Find a tagged animal**, with the renamed spare-tag
Calipers gesture, 24-block loaded range, native outline and shared search rest.
Page 50 gains its seventh chapter link; all older destinations remain stable.

H8 extends page 8's compass gesture to residents. L18 adds **Bell roll call**
on page 52; page 35 points to it and page 50 gains its eighth chapter link.
Existing destinations/native book components remain stable.

T7/T8 updates laboratory page 15 with A/B hopper ordering, powered hopper
retention and 0/8/15 comparator occupancy. Page count/links remain unchanged.

L19 appends **Recovery rest**, page 53, with serious-injury idle behavior,
meal/danger/weather priorities and no passive healing. Page 50 has nine
chapter links; the old destinations remain stable.

T9 appends **Plan a litter**, page 54, for held guava tiers/averaging and optional
Mutagen treatment. Cue/sample preservation, default clearing and before-Fortune
quality are explicit. Page 50 has ten chapter links; older page IDs remain.

T10 adds **Plan the harvest**, page 55, naming native held Fortune, highest-level
selection, 0.5 quality per level and the Cradle exclusion. Page 54 points onward;
the ten-entry New contents index and existing page destinations remain stable.

## P5 — Habitat chapters

The New index has ten entries. Page 56 adds a fourth **Habitat chapters**
index; Main contents links to it beside More and New while keeping eight
core chapters. Page 57 begins with **Cactus gardens**, also referenced by
existing gardening page 25. New habitat pages return to 56; earlier page
numbers, all twenty-eight existing chapter links and their returns stay stable.
The native book default, translations and click/page packets remain shared.
The new content and both new navigation views are previewed with a substitute
font. Build/all 44 server cases pass; native client fonts/navigation remain
playtest review.

L21 appends **Regional farms**, page 58, naming the five habitat-specific
plots, empty optional troughs, native growth and preserved old settlements.
The Habitat index gains its second link, for thirty total chapters over 58
pages. The region/cactus/Habitat text preview is inspected; Java 25 build and
all 45 native server cases pass. Earlier page destinations stay stable.

O1–O3 appends **Guava orchards**, page 59, covering plain-guava propagation
beneath any leaves, bone meal growth, repeat picking and shears/Silk Touch
canopy recovery. Habitat gains its third link, for thirty-one total chapter
links over 59 pages. The orchard/region/Habitat preview uses a substitute font.

L22 appends **Mushroom beds**, page 60, covering same-color seed conservation,
native spread, suitable soil/routes and shared work rest. Habitat gains its
fourth link, for thirty-two chapter links over 60 pages. The new text and
orchard/Habitat navigation preview is inspected; earlier destinations remain.

D6 appends **Fruit meals**, page 61, explaining injured residents' guavas and
diet-matching berries, preserved plants, 2 HP/shared recovery and safe native
work. Habitat gains its fifth link, for thirty-three chapter links across 61
pages. The new chapter/mushroom/Habitat preview is inspected with a substitute
font; native client text/controls remain gameplay review.

O4/O5 appends **Sapling stations**, page 62; L23 adds **Underground farms**,
page 63; N8 adds **Safe seedbeds**, page 64. Native planting costs/refusals,
support recovery/soil compatibility, developed cave/Nether plots and intact
litter fallback are explained. Habitat now has eight entries, for thirty-six
chapter links over 64 pages. The four-panel text/navigation preview is inspected;
old page IDs and returns remain stable.

C10 appends **Snow-coat care**, page 65, covering live cold climates, expressed
snow/Bloom +10, combined products/shared rest and separate native shearing.
Habitat has nine entries; the guide has thirty-seven chapter links over 65
pages. The new text and older living-care/dispenser/Habitat preview is inspected.

## P6 — Working residents

Page 66 is a fifth contents page, **Working residents**, linked from Habitat.
Main's eight chapters/three footers remain. Habitat retains nine chapters and
its Main footer, adding the Working link within the same fourteen-line budget.
Working links back to Main and Habitat. Older page IDs and chapter returns stay
stable; new work pages return to 66.

D7 adds **Loose food meals**, page 67, explaining grace/reservations, ordinary
food, Diet, healing/recovery and safe routes. L24 adds **Trough gathering**,
page 68, for Senses +10, explicit hoe opt-in, exact filters/power and shared
work rest. There are thirty-nine chapter links over 68 pages/five indices.
The meal/gathering/Habitat/Working preview is inspected with a substitute font;
Java 25 build and all 52 native cases pass. Native client book use remains
separate gameplay review.

L25/L26 appends **Melon beds**, page 69, and **Grow melon stems**, page 70.
Working gains its third chapter, pointing to 69 with a textual reference to 70;
both pages return to Working. The guide has 70 pages, five indices and forty
chapter links. Living attached stems, preserved decorative blocks, Diet feeding,
one-stage/no-food growth, native fruit regrowth and shared rest are explicit.
The melon/stem/gathering/Working preview is inspected with a substitute font.
Java 25 build/all 53 native cases pass; earlier destinations remain stable.

O6/O7 adds **Orchard pickers** (71) and **Repair a canopy** (74); D8 adds
**Habitat meals** (72), and H9 adds **Nest signals** (73). Working has six
chapter links, including 71–73; 71 points to repair page 74. The guide has
74 pages, five indices and forty-three chapter links, preserving earlier page
IDs/returns. L27 updates stem page 70 with meadow seed supplies. New content
and the six-link Working index are previewed with a substitute font; Java 25
build/all 56 native cases pass. Native client fonts/navigation remain gameplay
review; written-book defaults and protocol 7 stay shared.

L28–L30/N9 appends **Work modes** (75), **Restore a garden** (76) and
**Nursery workers** (77). Working has nine direct chapter links, retaining its
Main/Habitat footers within fourteen lines. All older destinations stay. There
are 77 pages/five indices/forty-six chapter links. The mode/restoration/nursery/
Working preview is inspected with a substitute font; Java 25 build/all 59 native
cases pass. Native client controls/fonts remain gameplay review.

L31 adds **Meadow melon farms** (78), reached from melon page 69 and returning
to Working. There are 78 pages; five indices/forty-six chapter links stay.
The farm cells and guide text preview are inspected; Java 25 build/all 59
native cases pass. Older guide destinations remain stable.

L32 adds **Work roll call** (79), referenced by Work modes (75). D9 adds
**Safe hunting** (80), referenced by Habitat meals (72). Both return to Working.
There are 80 pages; five indices/forty-six chapter links stay. The new content/
navigation preview is inspected; build/all 61 native cases pass. Native client
fonts/chat/navigation remain gameplay review.

D10/L33 and H10 append pages 81–82 for spent-chorus care and native basket
stations. Regional farms points to 81; Travel Basket points to 82. Basket
stations returns to More chapters; End care returns to Working residents.
All earlier numbers and the five indices/forty-six chapter links remain.

## P7 — Specialist work

L34 adds **Supply the troughs** at 83. Page 84 is a sixth contents page linking
Nursery workers, Work roll call, Safe hunting, Chorus gardens, Basket stations
and Supply. Working residents points to this group in its former Nursery row,
keeping its native text-area budget. These six chapters return to Specialist
work; its footer links to Working and Main. Older page numbers remain stable.
There are 84 pages and fifty-two chapter links. Supply/Specialist/mode/Working
previews are inspected with the substitute font; native client fonts and clicks
remain gameplay review.

L35/N10 append **Pause a resident** (85) and **Cradle controls** (86). Both join
the Specialist work index and return there; existing page numbers remain.
Work modes points to 85; Hatch safely points to 86. The eight-entry Specialist
index keeps its two footer links. The guide has 86 pages/six indices/fifty-four
chapter links; both new chapters, Specialist and modes are previewed.
