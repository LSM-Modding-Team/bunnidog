# Exploration and useful sensory lines

## E1: Warren Explorer Maps

Journeyman cartographers may offer an ordinary filled **Warren Explorer Map**
for fourteen emeralds and one compass, with twelve uses, ten villager XP and
native reputation/demand pricing. The offer joins their existing level-three
pool through an appended vanilla trade tag; two native selections mean it is
not guaranteed. Existing generated offers, uses and inventories remain intact.

Use Minecraft 26.2's native `villager_trade` data and exploration-map modifier,
not an obsolete trade event or a new merchant screen. The target tag
`bunnidogs:on_warren_maps` contains the natural Overworld `warren_village`
structure. Underground feature dens and End settlements are excluded. Data
packs can extend the structure tag normally. Search radius fifty and the
native skip-known-structures rule follow ordinary map behavior; failed
searches discard the blank result and omit the offer. No emeralds/compass can
be exchanged for an unlocated map. The normal village marker identifies its
target, with a translated item name, native biome preview, map data and updates.

This adds an ordinary survival discovery route to warrens without commands,
forced generation or changes to structure frequency and existing starts. The
Field Guide's warren chapter points to map instructions. Actual successful
natural-world discovery and client map rendering remain gameplay review.

E1 evidence (2026-10-18): actual Java 25 build/all 39 native scenarios pass.
The extended pantry/exploration case reads the loaded native trade set, confirms
the appended map and retained ordinary compass trade, and calls the real offer
factory without a located origin: its native filters return no blank-map offer.
The guide preview is inspected. This does not simulate a successful natural
structure search or establish client map rendering.
