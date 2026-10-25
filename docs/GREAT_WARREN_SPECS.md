# The Great Warren — AX6 implementation

> The player’s later loot review supersedes the initial rewards and equipment
> values below. See [APEX_REWARDS_SPECS.md](APEX_REWARDS_SPECS.md) for current
> parts, upgraded vanilla gear, weapon/container behavior and generous caches.

The first Great Warren expedition loop now ships. The 2026-10-25 user review
closed Apex balancing and resumed autonomous development. This content builds on
those accepted fights; their damage, health, timings, XP and minion limits stay.
The Crown Warren dungeon, Crowned Apex and Sovereign upgrades remain future AX7.

## Enter, explore, return

Craft a **Warren Anchor** from crying obsidian, copper blocks and an Ender Pearl.
Attune it with a **Resonant Eye** while showing an actual **Apex Crest** in the
other hand. Either hand arrangement works. The eye recipe uses an Eye of Ender,
amethyst, Starlit Dust and a netherite scrap. The Crest's complete source record
is retained. There is no Dragon-kill requirement or hidden progression flag.

Attunement pays one eye once. The active block is visibly lit; ordinary use
crosses either way without another item. New arrivals get a seven-by-seven
Rootstone platform, corner lights, protected return anchor and a real ladder to
the terrain, a solid canopy or the water surface. The platform is constructed only in air above existing terrain;
its support/ladder route does not erase leaves, water, containers or buildings.
A blocked candidate tries four nearby alternatives, then refuses without payment.

The portal records its exact counterpart dimension and block position. Different
source anchors create distinct returns, including for simultaneous players.
Endpoints survive native block-entity saves. The generated return block is not
Survival-mineable; it and Rootstone resist Apex terrain destruction and blasts.
Removing the original source anchor does not erase the return destination.
Return travel requires no held items and remains available in Adventure mode.
Normal inventory, death, bed and item-recovery rules apply.

Explicit travel loads only a bounded three-by-three endpoint chunk neighborhood
(normal generator dependencies and native portal tickets still apply). It never
searches/loads a distant region or keeps an expedition force-loaded. Arrival
checks standing-player collision, fluids, world border and solid support in an
eight-block neighborhood, with bounded vertical alternatives. Failed crossings
leave an activated anchor usable. A short per-player cooldown prevents repeated
same-tick crossings.

Only server-owned, nearby, permitted interactions can attune or cross. Activation
reserves its anchor and rechecks the actual held eye/Crest after generation.
The portal is a direct world interaction; no station menu or tutorial HUD exists.
Recipes unlock naturally, the Crest/eye have a brief resonance tooltip and the
normal **Beyond the Roots** advancement records actual entry.

## Three initial regions

Dimension ID: `bunnidogs:great_warren`. It uses its own noise settings, biome
source, dimension type, structures and spawn tables. Existing dimensions/warrens
are untouched. The first terrain uses native Overworld density with a lower sea
level (40), its own exposed materials and three climate-selected regions:

| Region | Terrain and landmarks |
|---|---|
| Crownroot Groves | Moss, enormous guava trunks, arching roots, broad persistent canopies, shroomlights and real harvestable hanging guavas |
| Glassbloom Flats | Pale calcite ground and tall calcite/amethyst clusters, supplying the shared Prism Cell economy |
| Thunder Hollows | Dark basalt ground, tall basalt pillars and gilded tips over the realm's caves/ridges |

Beds work normally. Hostiles can spawn under the realm's ambient daylight;
normal block-light rules still help a built base. World height is -64 through
319. Large landmark writes are bounded to the native generation region and
replace only air. Feature and noise data are authored by
`art/gen_realm_worldgen.py`; runtime landmark silhouettes use `RealmLandmarkFeature`.

## Native Apex ecology

The ten existing Apex types are 90% of hostile spawn-table weight; Endermen are
10%. These weights are choices under Minecraft's ordinary monster cap, not a
promise that every view contains a particular number. Ground choices form small
packs of two to three; aerial choices are singles. Region weights emphasize
Prismmaws in the Flats and Blastmaws in the Hollows. There is no parallel spawning
loop, global pack manager or mob-owned chunk loading.

Natural initialization assigns the **wild** profile: source-line founder genes,
bounded bred points, adult size and inherited Wings for aerial forms. The actual
rolled genome/stats are saved and never rerolled on load. Wild Apexes despawn
normally at distance and have no boss bars. Named wild ones can be kept.
Ritual Apexes retain their persistent boss bars, source copy, costs and guaranteed
player-credited Crest. Old saves without the new `ApexWild` field remain ritual
encounters. Accepted combat behavior and **500 XP** apply to all forms.

Idle wild flyers take off and cruise loaded terrain even without a target;
combat controllers are unchanged. Temporary brood retain their finite lifetime,
no-loot/no-XP/no-Crest rules and source ownership.

**Warren Wards** suppress new natural monsters in a 24-block sphere. Active portal
anchors also ward their immediate refuge. Native POI queries inspect loaded block
states; removing the block ends protection. This suppresses ordinary Endermen as
well as Apexes. It does not delete existing enemies, stop their attacks, prevent
scripted/dungeon spawns, or stop a Broodcaller calling its real finite brood.
Rootstone can be crafted eight at a time from tuff and Apex Marrow for persistent
fortifications. It remains mineable by players.

## Expedition rewards

A player-credited wild kill with mob drops enabled pays two to four of its role
reagent, four to twelve emeralds and two to five Starlit Dust. Aerial enemies add
one to two Aerial Membranes. Full source Crests are a 1/16 bonus, rather than an
unstackable mandatory drop on every ordinary kill. Wild kills do not replace the
ritual victory advancement or become domestic legendary-drop sources.

| Role | Reagent | Current use |
|---|---|---|
| Brute | Apex Marrow | Rootstone and outpost Wards |
| Prismmaw | Prism Lens | Prism Lance crafting/repair |
| Broodcaller | Chorus Heart | Wards |
| Blastmaw | Volatile Heart | Efficient batches of 24 Prism Cells |
| Lifebinder | Heart Spool | Wards and Apex Wing Harness |
| Any aerial role | Aerial Membrane | Lance, harness and Ward crafting; harness repair |

### Rich ruins

`bunnidogs:great_ruin` uses its own random-spread structure set (20-chunk spacing,
eight separation) restricted to the realm. A bounded native terrain fit rejects
water-level sites and slopes over nine blocks. The initial open, columned hall
has varied low walls, crossbeams, a lit glass roof and two separate caches.
Its serialized piece owns chunk intersections and completion bits. Existing
container/protected columns are preserved; replay does not restore looted stock.

The wealth chest uses native deferred loot. Each cache includes **16–32 diamonds,
2–6 netherite ingots and 2–4 enchanted golden apples together**, alongside emeralds,
Mutant Marrow, Starlit Dust, cells, lenses, membranes and food. These are additive
pools, keeping the planned generous economy.

The archive chest freezes an actual quality-20–33 seed and matching reusable
Tissue Sample, bred points 40–70, Mutagen and supplies. Parents are End/Cave
founders, with a possible winged mother. It preserves actual full genome/seed
components through chest saves; inspecting, reopening, unloading or replaying
never rerolls the find. Offspring still learn their own routines.

### Prism Lance

A Lens, Membrane and netherite ingot craft the lance. Holding use charges for
32 ticks, then fires a cyan 32-block beam with a .75-block hit expansion through
blocks and a line of targets. Each accepted shot spends one actual Prism Cell,
one durability and starts a 30-tick cooldown. It deals 24 raw damage; shields do
not block it, armor and native damage cancellation still apply. An interrupted
charge spends nothing. A missing cell, stale held item, cooldown or unloaded
shot bounds cannot produce a free shot.

Cells craft in batches of eight from amethyst/copper; Volatile Hearts provide
batches of 24. The lance has 512 durability and repairs with Prism Lenses.

### Apex Wing Harness

Four Membranes, two netherite ingots and a Heart Spool craft a native chest-slot
glider. It has 864 durability, original feathered equipment art, no armor bonuses
and ordinary gliding/deployment rules. Use a held Prism Cell during a glide for a
forward/climbing burst: one cell, four harness durability, 40-tick shared cell
cooldown. Ground use cannot launch flight. A nearly broken harness refuses the
boost. Consuming the last cell still starts cooldown on newly supplied cells.
Normal vanilla flight durability applies; Aerial Membranes repair it. Both pieces
support native durability enchantments without pretending unrelated damage
or armor enchantments affect their special mechanics.

## Evidence and remaining work

Java 25 build, localization/model gates and **163 native scenarios** pass. Relevant coverage:

- Paid real entry/empty-handed return using the shipped dimension definition;
  two independent portal pairs, source-block removal and native endpoint saves.
- Standing arrival, protected Rootstone/refuge machinery, native ladder access,
  loaded ward POI insertion and actual monster spawn-denial events, with scripted
  spawn exceptions and native natural Apex initialization.
- All ten wild profile saves, normal despawning/named retention, role rewards and
  the unchanged ritual profile; idle wild aerial ascent.
- Detached ProtoChunk/WorldGenRegion chest creation, frozen seed/tissue saves,
  serialized piece replay, actual native wealth-table rolls and all three landmark
  generators, including attached Crownroot lamps/fruit. A native seed-zero ruin
  candidate at chunk `[-32, -31]` fits the shipped terrain/placement.
- Actual paid lance damage to two targets, interrupted/no-ammo refusal and finish
  replay; native harness/gliding components and paid/last-cell/broken-item boosts.

Minecraft's GameTestServer deliberately omits datapack dimensions. A **test-only**
fixture mounts the actual shipped LevelStem with native ServerLevel/noise
terrain, then removes/closes it; production does not inject dimensions at runtime.
The same fixture checks native structure placement/terrain candidacy. The
normal client/dedicated-server loader owns the dimension in ordinary play.

`art/realm_items_preview.png` inspects original assets.
`art/realm_world_preview.png` uses actual native arrival and room-cell snapshots.
These are headless geometry/texture studies, not graphical client or performance
captures. Natural broad biome distribution, first-trip latency, mixed-pack client
performance and long multiplayer expedition pacing still need gameplay evidence.
The other two proposed regions, additional gear branches and the Crown Warren
finale remain planned; the initial realm is playable without them.
