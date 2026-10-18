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
The home page also covers read-only Nest inspection and persistent compass marks.
The food page explains short safe nighttime trough meals near home.

Craft one with a vanilla book and any guava (the existing bunnidog-food tag).
Obtaining a guava unlocks its recipe. An additional independent 35% pantry loot
pool can supply one to explorers; it stays independent of food rolls. New
lineage-specific stores and unopened legacy pantries use it. Filled or emptied
containers are not refilled and player inventories are not edited. See
`SETTLEMENT_LIFE.md` L6 for local food and new surface feed stores/markets.

## Presentation and saves

Forty-nine pages use the ordinary written-book screen. The two linked contents pages
offer twenty clickable chapter links. Each chapter links back to its index. Local page changes
are the only click action. All text is translatable through en_us.json. The
book stacks to one and appears beside the Calipers in Tools & Utilities.

The item inherits native WrittenBookItem and supplies the existing
WRITTEN_BOOK_CONTENT component as an item default: native book packets, tooltips,
saves and filtering apply. There is no new screen, payload, server handler or
custom data component; protocol 6 persists. Default content follows future mod
updates unless a command explicitly overrides the component.

Art/content source: `art/gen_guide.py`; regenerate with Python 3/Pillow.
`FieldGuideItem.PAGE_COUNT` and its contents links correspond to the authored
numbered pages. Keep pages short enough for the native 114x128 text area; book
resource-pack fonts may differ. The source preview uses a substitute font.

## Evidence

The actual Java 25 build and current forty native server cases pass,
including loading the book/recipe/loot resources. The expanded 49-page
automation/focus/pause preview is inspected with a substitute font. Earlier
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
