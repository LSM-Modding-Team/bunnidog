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
Silk Touch and explosion decay; ores require diamond-tier mining or better.

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
enchantability **18**, fire resistance and the same native mining tier. The sword
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

Dungeon random-spread candidates and the terrain share spacing 64, separation
24 and salt 926481503. Each candidate gets a large tapered island with a flat
125-block-radius plateau at Y80; Crown placement uses Y81. Main islands extend
roughly 190–238 blocks with irregular shorelines and tapered undersides, and
smaller disconnected islands supply outlying exploration. The site layout uses
the actual world seed, negative-coordinate floor division and no live world
queries. Every neighboring group of four Crown sites uses its Euclidean minimum
spanning tree; their union connects the archipelago with shortest straight local
routes, rather than building every diagonal. Seven-block-wide, lit End-stone-brick
causeways cross the void at Y80. This describes local minimum spanning routes,
not a claim of one global minimum spanning tree for an infinite world.

Floating Moonstone ellipsoids sit around Y237–251, radii34–42 horizontally with
slightly flattened poles. They contain both unique ores: Astralite **2.4%**,
Cinderite **3.2%** of their rock, versus **0.16% / 0.24%** on lower islands.
Ore locations use a world-seeded coordinate hash, independent of chunk order.
The native monster spawn table above Y170 is exclusively Blastmaws and flying
Blastmaws (3:2 weight); ordinary loaded-area monster caps, native spawn checks,
ward protection and wild despawning still apply. No force-loaded spawn loop.

New unlinked arrivals choose a nearby dungeon-bearing island's outer ledge,
outside its 181-block dungeon footprint, before the existing bounded refuge
construction. A paired return retains its exact original endpoint. Construction
still preserves existing blocks and must produce a real ladder/safe landing.

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
Survival acquisition pacing still need player review.
