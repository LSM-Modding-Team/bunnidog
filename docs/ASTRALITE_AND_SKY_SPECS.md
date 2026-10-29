# Astralite and the sky archipelago

The latest player request replaces the original Overworld-density realm terrain
and Netherite artifact base. This is the pre-Crown preparation loop. It needs no
Crown key, Sovereign Core, dungeon clear or dungeon chest.

## Expedition economy

The lunar forge now requires bulk moon extraction before a complete armour set:

1. Nine Astralite Shards craft one **Astralite Concentrate**.
2. Smelt/blast Raw Cinderite into ingots; nine craft one **Cinderite Billet**.
3. Nine Moonstone craft **Compressed Moonstone**. Combine one with a Prism Lens
   and Apex Marrow to make a **Lunar Binder**.
4. Four concentrates, four billets and one binder craft an **Astralite Bloom**.
5. Blast-smelt the bloom for 400 ticks into one **Astralite Ingot**.
6. Four ingots, four more billets and an Aerial Membrane craft **Astralite Plating**.
7. Smith each Netherite armour piece with one plating and one existing Astralite
   Upgrade Template. Tools still use one ingot plus one template.

The template remains four diamonds, two membranes, two lenses and one Netherite
Ingot. All stages are available before the dungeon and unlock through the native
recipe book. There is no bypassing old cheap ingot recipe. This changes future
crafting, not existing gear, enchantments, names, wear or saves.

| Starting from an existing Netherite armour set | Required raw stock |
| --- | ---: |
| Astralite Shards | 576 |
| Raw Cinderite, subsequently smelted | 720 |
| Moonstone | 144 |
| Apex Marrow | 16 |
| Prism Lenses | 24 |
| Aerial Membranes | 12 |
| Diamonds | 16 |
| Additional Netherite Ingots | 4 |

Those four extra Netherite Ingots use 16 Ancient Debris and 16 Gold Ingots. Add
fuel for refining Cinderite/blooms and any vanilla smelting. Counts are item
requirements, not fixed ore-block or kill counts; Fortune and collection losses
matter. Starting with a Netherite pickaxe, upgrading it first adds 36 shards,
36 raw Cinderite, nine Moonstone, one Marrow, three Lenses, two Membranes, four
diamonds and one Netherite Ingot. The first tool is deliberately cheaper than
full armour plating so it provides a mining foothold.

Moonstone and the ores require Astralite for mined drops and resist TNT (1,200).
Before the first tool, bait actual Blastmaw **self-explosions** into lunar rock.
Only the dedicated `blastmaw_mineable_moonstone` tag gets resistance 1.5 in that
narrow calculator. It contains exactly Moonstone and the two ores, not fortress
blocks that also require an Astralite harvest tool. Fireballs/player launchers
have no mining exception. Mob-griefing, native protection and explosion
cancellation remain. Both ground and flying Blastmaw self-explosions use native
`DESTROY`, so every destroyed lunar block pays its full normal loot even when
`mob_explosion_drop_decay` is true. This TNT-like recovery does not change the
world rule or extend the mining exception/full-drop override to fireballs.
Lunar drops and refined materials resist fire/explosions; collecting them before
void loss/despawn or dying remains part of extraction.

Cinderite is a mined mineral too. The Lantern smith refines **12 Raw Cinderite
+ 4 Root Fibre → 12 Ingots** from finite stock; fibre alone no longer buys metal.
Saved old shortcut offers retire without restocking or resetting other trades.
Later dungeon mineral shipments remain earned rewards, not a first-tool bypass.

Moons retain 2.4% Astralite and 3.2% Cinderite against 0.16%/0.24% on lower
islands. Bulk ore demand now makes those rich concentrations valuable; no
ordinary open dungeon cupboard supplies these bottleneck ores or forge stages.
The first foothold and complete armour are separate resource goals. Natural
Survival duration and co-op costs still need player review.

## Equipment

Astralite armor has the Netherite armor values, toughness **5** per piece,
knockback resistance **0.15** per piece, enchantability **18**, fire resistance
and exactly **3x Netherite durability**: helmet 1,221, chestplate 1,776, leggings
1,665, boots 1,443. Native Astralite Ingots repair it. Mending/Unbreaking and other
applicable native enchantments work through ordinary tags, books and enchanting.

Tools: sword, pickaxe, axe, shovel and hoe; durability **2,437** (about +20%),
mining speed **12** against Netherite's 9, material attack bonus **6** against 4,
enchantability **18**, fire resistance and the unique Astralite mining tier. The sword
has total base damage **10**, before enchantments/effects. No tripled tool wear.

Vitality Artifacts now smith **Astralite** armor into Heartbound Astralite;
Netherite no longer matches those recipes. Heartbound and its Sovereign artifact
upgrade keep the Astralite foundation, while their existing max-health and
independent debt-timer benefits remain. Existing item IDs/components and equipped
benefits survive; old Heartbound/Sovereign stacks get the new material defaults.
Existing saved wear/enchantments/names remain owned by the native stack.
Elytra's separate aerial-artifact route remains an Elytra upgrade, not a Netherite
armor exception. This change does not alter accepted enemy fight values.

Late/endgame Bunnidog kits supply enchanted Astralite tools and the corresponding
Heartbound/Sovereign armor. All durable late/endgame kit items explicitly receive
Mending I and Unbreaking III, except the requested Infinity bows.

## Terrain and travel

`bunnidogs:realm_islands` is a real chunk-generator codec. It preserves the realm
ID and climate-selected three biomes, but writes its own complete solid columns,
matching heightmaps/base-column queries, no cave carvers, oceans or bedrock floor.
Void is real air down through minimum Y. Decoration cannot reintroduce vanilla
Overworld caves, geodes or ore generation. Vegetation is restricted to low island
surfaces and excluded from bridges and Crown footprints.

Dungeon placement and foundations share spacing **512 chunks**, separation **256**
and salt 926481503: one candidate per 8,192-block-square region. This is about
**7.1x rarer by area** than the preceding 192/96 placement (64x rarer than the original 64/32). Ordinary
island and moon density are unchanged; those use independent grids. Only the actual
192×192 footprint around each Crown is flat at Y80, with its dungeon at Y81;
approaches blend into the surrounding hills. It no longer flattens the entire
island. Ordinary islands are independent of dungeon placement.

The resolved 26.2 `DensityFunctions.EndIslandDensityFunction` and native End
noise settings were inspected. The design uses the End's seeded Simplex candidate
field, varying island sizes and density variation as its reference, without its
central-island/1,000-block-ring layout. Independent irregular elongated landforms,
small fragments, coast warping, layered mountain/ridge relief and three-dimensional
rock hollows replace disks. Existing climate biomes and their landmarks/ruins
supply ordinary exploration islands. Void remains real air; there are no
Overworld oceans or continuous underground floor. Land peaks stay below moons.

Neighboring Crown groups retain their local minimum trees, but each link now
uses bounded Dijkstra routes through intermediate island candidates. Actual
travel distance, climbs and missing-land costs select a chain of islands. Only
selected missing waypoints become stepping islands; many unrelated islands remain
unconnected. Seeded triangulation avoids crossing incompatible diagonal spans.
Architectural roads/bridges follow varying heights, grade their narrow island
approaches and join Crown plateaus. The deck height is independent of the island
surface: when terrain rises above it, generation carves only **five blocks of
headroom** over a five-wide carriageway. Rock and moss above the
corridor remain, producing tunnels instead of deep open trenches. Shallow entry
edges open naturally; void spans remain bridges. Decks raised above lower
island terrain preserve the air gap rather than filling a solid wall down to
the ground. Outside spans now have double-thick masonry arch ribs, hanging
piers, slab-capped parapets and root-beam crossframes with recessed lights.
Native stairs/slabs make grade changes walkable. Tunnels have periodic ribs and
side lights. Their structural extents agree with native height/base-column
queries, while the natural island roof remains independent. Nearby natural hollow noise
cannot erase the immediate tunnel roof/floor. Crown footprints remain excluded.
Native heightmaps/base-height/base-column queries follow the actual exterior
roof, while route traversal follows the deck. This is a bounded local network, not one
global minimum tree over an infinite world. Tile/route caches are bounded; no
world access, chunk loading or search proportional to explored territory occurs.
Canonical span endpoints and a complete coordinate sort make overlapping routes
independent of regional enumeration order. Road heights round only after the
final Crown-approach blend; rounding twice could turn a gentle grade into a
two-block step. Failed route reconstruction cannot silently return an
endpoint-only path. Native route inspection uses the same
immutable plan as generation, and every sampled hop remains local even when
Crown destinations are thousands of blocks farther apart.

Moonstone ellipsoids sit around Y235–253, horizontal radii42–64. They contain
Astralite **2.4%**, Cinderite **3.2%** of their rock, versus **0.16% / 0.24%** on
lower islands. Coordinate hashing preserves chunk-order-independent ore placement.
Above Y170 the native monster table remains exclusively Blastmaws/flying
Blastmaws (3:2). Both have ground-biome weight **1**, single-actor groups, so they
are rare on ordinary land. Native caps, wards, despawning and spawn checks remain.

Fresh portal arrivals choose an ordinary island at least **700 blocks** from the
nearest Crown, outside overhead moons. They no longer start beside a dungeon.
The refuge still has its original bounded construction, ward, real ladder and
safe landing. Existing paired endpoints and generated builds remain intact.

## Compatibility and evidence

Existing generated chunks, structures, containers, anchors and player builds are
not rewritten. Newly generated chunks use the new generator; old/new borders can
have abrupt terrain seams. No automatic chunk deletion or terrain migration.
The explicit dungeon repair command retains its earlier scope.
An explicit `/bunnidogs bridges renovate` upgrades only loaded surviving former
road cells within 64 blocks; it preserves natural terrain, containers and other
player blocks. This never stitches old/new route borders automatically.

Lantern Warrens can fit inside suitable ordinary islands, with surface entrances,
peaceful trade and inter-island portals. Their 32/16-chunk candidate set rejects
thin/low rock and Crown islands; it changes neither independent island/moon
density nor the 512/256 Crown placement. See
`LANTERN_WARRENS_AND_GUILD_PROJECTS.md` for exact native fitting/network rules.

Native checks cover real shipped LevelStem decoding/realm entry and return,
unfinished ProtoChunk terrain, rich moons, void and bridge placement, agreeing
heightmap/base-column queries, moon spawn tables, seed variation, actual Crown
candidates, connected local Crown networks and full block-by-block island paths
across four seeds (including negative regions), native ProtoChunk roads/headroom
at Crown-region seams, ordinary-island tunnel deck/headroom/roof/surface and
matching native height queries, cold-cache determinism, ordinary ruins, all nine native
smithing upgrades, artifact-base rejection and exact enchantment/name/wear
preservation. Geometry/texture previews are headless evidence; first-person appearance, natural encounter density and
Survival acquisition pacing still need player review. The maintained native
sample map and height study are `art/realm_islands_preview.png`; they are not
a graphical client capture.

The tunnel fixture exports a longitudinal section from actual generated
ProtoChunks. `art/preview_realm_tunnel.py` renders `art/realm_tunnel_preview.png`;
it is a block inspection, not simulated client lighting. Final architectural
bridge views in `art/bridges/` render an actual generated ProtoChunk export.
Bulk-material projects and room-specific rewards now ship as recorded in
`LANTERN_WARRENS_AND_GUILD_PROJECTS.md`; they do not change mineral density or
the pre-dungeon Astralite forge path.
