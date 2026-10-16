# P1 — Bunnidogs Field Guide

The growing progression needs an in-game reference. The Field Guide covers
founders, genes, breeding/seeds/quality, pedigree, Nests and transport, food,
bell gatherings, living care, adaptations, table/splicing, stable lines,
shows, materials, legendary drops, the Apex and infused juice.

Craft one with a vanilla book and any guava (the existing bunnidog-food tag).
Obtaining a guava unlocks its recipe. An additional independent 35% pantry loot
pool can supply one to explorers; food rolls are unchanged. New or still-unopened
containers with the warren-pantry table use it. Already-filled containers are
not refilled and player inventories are not edited.

## Presentation and saves

Twenty-seven pages use the ordinary written-book screen. Page 1 has eleven clickable
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
