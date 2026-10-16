# P1 — Bunnidogs Field Guide

The growing progression needs an in-game reference. The Field Guide covers
founders, genes, breeding/seeds/quality, pedigree, Nests and transport, food,
bell gatherings, living care, adaptations, table/splicing, stable lines,
shows, materials, legendary drops, the Apex and infused juice.
The gardening pages include Calipers work status and new fitted corner troughs.
Living-dust care has its own page explaining combined yields and shared recovery.
The terrain page explains inherited stepping and partial slow-block resistance.
Shore fishing explains resident Fisher prey, needs, healing and recovery.

Craft one with a vanilla book and any guava (the existing bunnidog-food tag).
Obtaining a guava unlocks its recipe. An additional independent 35% pantry loot
pool can supply one to explorers; it stays independent of food rolls. New
lineage-specific stores and unopened legacy pantries use it. Filled or emptied
containers are not refilled and player inventories are not edited. See
`SETTLEMENT_LIFE.md` L6 for local food and new surface feed stores/markets.

## Presentation and saves

Thirty pages use the ordinary written-book screen. Page 1 has eleven clickable
chapter links; every other page links back to the contents. Local page changes
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

Actual Gradle build passes. Icon and representative page/index preview inspected;
the existing conservative text-width estimate identified one long paragraph,
which was shortened. Native opening/click targets were checked against resolved
26.2 WrittenBookItem, ServerPlayer, ClientPacketListener and BookViewScreen.
The final combined build/server run passes all fifteen mod scenarios plus
native sanity, including loading the new book/recipe/loot resources. GitHub
delivery is checked for each pushed checkpoint.
Actual client book navigation, font rendering and pantry discovery need gameplay
review. No extra server scenario is added for this native-book content change.
