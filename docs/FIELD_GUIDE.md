# P1 — Bunnidogs Field Guide

The growing progression needs an in-game reference. The Field Guide covers
founders, genes, breeding/seeds/quality, pedigree, Nests and transport, food,
bell gatherings, living care, adaptations, table/splicing, stable lines,
shows, materials, legendary drops, the Apex and infused juice.
The gardening pages include fitted corner troughs, Bloom cultivation, berry
regrowth and the existing work-rest/filter/protection rules.
Living-dust care has its own page explaining combined yields and shared recovery.
The terrain page explains inherited stepping and partial slow-block resistance.
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
The home page also covers read-only Nest inspection and persistent compass marks.

Craft one with a vanilla book and any guava (the existing bunnidog-food tag).
Obtaining a guava unlocks its recipe. An additional independent 35% pantry loot
pool can supply one to explorers; it stays independent of food rolls. New
lineage-specific stores and unopened legacy pantries use it. Filled or emptied
containers are not refilled and player inventories are not edited. See
`SETTLEMENT_LIFE.md` L6 for local food and new surface feed stores/markets.

## Presentation and saves

Forty pages use the ordinary written-book screen. The two linked contents pages
offer eighteen clickable chapter links. Each chapter links back to its index. Local page changes
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

The actual Java 25 build and current twenty-nine native server cases pass,
including loading the new book/recipe/loot resources. The expanded 40-page
contents/home/gardening preview is inspected with a substitute font. Earlier
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
