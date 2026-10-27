# Astralite and the sky archipelago

The latest player request replaces the original Overworld-density realm terrain
and Netherite artifact base. This is the pre-Crown preparation loop. It needs no
Crown key, Sovereign Core, dungeon clear or dungeon chest.

## Expedition economy

Astralite Shards and Raw Cinderite are mined from two unique ores. Smelt/blast
Raw Cinderite into Cinderite Ingots. Four shards, four Cinderite Ingots and one
Apex Marrow craft one Astralite Ingot. An Astralite Upgrade Template costs four
diamonds, two Aerial Membranes, two Prism Lenses and one Netherite Ingot.
Every item consumes one template and one Astralite Ingot in a normal Smithing
Table, upgrading the corresponding Netherite item. Templates are deliberately
crafted from repeatable wild-Apex drops rather than requiring dungeon access.
All recipes unlock from their ingredients. Ore drops support native Fortune,
Silk Touch and explosion decay. Moonstone and both ores require **Astralite**
tools for mined drops; Netherite and every lower vanilla tier are insufficient.
The first Astralite is obtained by baiting actual Blastmaw self-explosions into
lunar rock, then collecting the real ore drops. This deliberate first-tool loop
uses no dungeon item. Their resistance is 1,200 (TNT and player launchers cannot
mine it); only the Blastmaw self-blast calculator treats lunar rock as resistance
1.5. Mob-griefing, explosion cancellation and protection still apply. Blastmaw
fireballs have no such exception. Moonstone, ore blocks, shards, raw Cinderite
and both refined ingots resist native fire and explosion damage as items.

The four-piece set therefore requires sixteen shards, sixteen refined Cinderite,
four Marrow, eight Membranes, eight Lenses, sixteen diamonds and four additional
Netherite Ingots, besides the original Netherite gear. Tools cost the same per
upgrade. No generic ruin chest hands out the new tier or its unique ores.

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
Mending I and Unbreaking III; native applicable enchantments remain otherwise.

## Terrain and travel

`bunnidogs:realm_islands` is a real chunk-generator codec. It preserves the realm
ID and climate-selected three biomes, but writes its own complete solid columns,
matching heightmaps/base-column queries, no cave carvers, oceans or bedrock floor.
Void is real air down through minimum Y. Decoration cannot reintroduce vanilla
Overworld caves, geodes or ore generation. Vegetation is restricted to low island
surfaces and excluded from bridges and Crown footprints.

Dungeon placement and foundations share spacing **192 chunks**, separation **96**
and salt 926481503: one ninth the former candidate density. Only the actual
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
Seven-block-wide roads/bridges follow varying heights, grade their narrow island
approaches and join Crown plateaus. This is a bounded local network, not one
global minimum tree over an infinite world. Tile/route caches are bounded; no
world access, chunk loading or search proportional to explored territory occurs.

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

Native checks cover real shipped LevelStem decoding/realm entry and return,
unfinished ProtoChunk terrain, rich moons, void and bridge placement, agreeing
heightmap/base-column queries, moon spawn tables, seed variation, actual Crown
candidates, ordinary ruins, all nine native smithing upgrades, artifact-base
rejection and exact enchantment/name/wear preservation. Geometry/texture previews
are headless evidence; first-person appearance, natural encounter density and
Survival acquisition pacing still need player review. The maintained native
sample map and height study are `art/realm_islands_preview.png`; they are not
a graphical client capture.
