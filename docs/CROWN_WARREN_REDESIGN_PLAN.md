# Crown Warren: architectural redesign plan

**Status: implemented after the subsequent explicit implementation request.**
The original planning review inspected `2aa300a`. This plan supersedes the claim that
the initial functional encounter layout is finished dungeon architecture. It
preserves the accepted combat, progression and reward foundation.

## 1. What the current render actually shows

[Current geometry inspection](../art/planning/crown_current_geometry.png) includes
an exterior, the Crown hall at player height, and the Glass Gallery at player
height. [Provenance](../art/planning/crown_current_geometry_provenance.json).

The existing `CrownLayout.cell` was evaluated unchanged with symbolic block APIs;
14,463 floor/wall observations were checked against the native exported layout.
The image is a headless geometry inspection, not a Minecraft client screenshot.
Lighting is neutral, glass is opaque for this inspection, stair shapes are
approximated by their occupied cells, and surrounding terrain is omitted.
These limitations affect presentation, not the observed room/roof/wall layout.

The current building is a large encounter prototype. Its architecture has not
reached the standard implied by the names cathedral, gallery, vault or furnace.

| Aspect | Observed problem | Required change |
| --- | --- | --- |
| Silhouette | One 85×85 rectangular hall, three 37×37 boxes and a low staging box. | A recognizable gatehouse, surrounding precincts, distinct towers and dominant keep. |
| Facades | Single planes of Rootstone; a few corner posts. | Wall thickness, recessed bays/windows, buttresses, ledges, parapets and deliberate roof edges. |
| Roofs | Flat sheets at heights 31, 15 and 9. | Vaults, pitched subsidiary roofs, terraces, tower caps and purposeful openings. |
| Interior hierarchy | Most of a room is empty floor under a high lid. | Rooms within buildings: stairs, galleries, service spaces, alcoves and specific activities. |
| Circulation | Almost everything shares one floor; long exposed connectors. | A courtyard hub, multilevel loops, understandable choices and usable shortcuts. |
| Progression | The northern wing's intended path crosses the arena; side openings weaken the front gate's meaning. | All three wings accessible before the boss; intended walking routes respect a clearly separate throne threshold. |
| Identity | Floor color and material largely carry the wing theme. | Each wing has a distinct silhouette, construction method, room sequence and tactical layout. |
| Scale | The player sees enormous repeated surfaces with few scale cues. | Human/bunnidog-scale furnishings and smaller spaces contrasted with genuinely monumental halls. |

The correction starts with the building plan. Adding stairs around the old
rectangles, random cracked blocks or more decorative pillars would leave the
main problem intact.

## 2. Comparisons inspected

These are observations of the authors' published images, not claims of having
played or profiled their latest releases. Take architectural principles rather
than their assets, exact floor plans, textures or encounter systems.

| Reference | Visible strength | Application to the Crown Warren |
| --- | --- | --- |
| [When Dungeons Arise gallery](https://www.curseforge.com/minecraft/mc-mods/when-dungeons-arise/gallery), including the [Keep Kayra interior](https://media.forgecdn.net/attachments/537/24/kayra_1.jpeg) and [Shiraz tower stair](https://media.forgecdn.net/attachments/379/476/imagen0.png) | Deep framing, repeated structural bays, multiple gallery levels and stairs integrated into a building with a clear material hierarchy. | A monumental keep assembled from believable supported bays; layered routes and tower silhouettes rather than a decorated arena shell. |
| [YUNG's Better Dungeons gallery](https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons/gallery): [Fortress of the Undead](https://media.forgecdn.net/attachments/384/541/2021-06-14_03.png), [forgotten study](https://media.forgecdn.net/attachments/384/542/2021-06-14_03.png) | Landings and stairs overlook usable space; arches and beams organize the volume; small studies contain purposeful fittings. | Alternate large fighting spaces with furnished support rooms, and make balconies connect to places the player can actually reach. |
| [The Twilight Forest gallery](https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest/gallery): [Lich Tower outside](https://media.forgecdn.net/attachments/1264/986/lich-tower.png), [inside](https://media.forgecdn.net/attachments/1264/987/lich-inside.png) | A strong vertical destination rises above its surroundings; the interior has tall supports, galleries and a distinct central focus. | Recognizable arrival silhouette and a clear destination inside; vertical exploration with memorable landmarks. |

Shader darkness and decorative block count are not the quality bar. These
buildings also have spatial composition, wall depth, circulation and meaningful
changes of scale. Our review must work in ordinary Minecraft lighting.

## 3. Design direction: a root-built citadel

The Crown Warren becomes an abandoned bunnidog citadel, built around a monumental
petrified Hearth/root system and occupied by Apexes. Its architecture should show
that a society once stored food, raised young, kept records, trained companions
and maintained workshops here. This is environmental history, not a claim that
the unfinished civilization simulation can already build castles autonomously.

The main image is pale, weathered stone and massive root buttresses holding a
crowned keep above lower courtyards. Warm timber service buildings nestle into
that structure. One side rises into a glass observatory; another spreads into
terraced brood courts; the furnace occupies the darker lower foundations.
Silhouettes and construction express those roles before a label does.

Bunnidog identity comes through broad arched openings, rounded nursery alcoves,
seed-like vault forms, nesting courts and interwoven root supports. Avoid a
novelty rabbit-shaped building or a generic human castle recolored green.
Repeated crest forms can unify the architecture without making every facade a
huge face or every roof a crown.

### Overall composition

Target an approximately **210–230 block** precinct, subject to the room/terrain
blockout. Keep the boss's existing approximately 85×85 playable envelope as the
starting combat constraint. Add perimeter depth outside it; do not fill its
required dodge space with furniture. Size should follow usable rooms, not an
arbitrary goal of making the dungeon larger.

The keep dominates, flanked by unequal tower groups. Lower wall walks and
subsidiary roofs provide a middle scale; the gatehouse gives a distinct entrance.
A broken wall, terraces and roots anchor the complex to its island rather than
leaving it on a floating square building slab.

Suggested vertical bands, relative to the principal courtyard:

- Cellars and furnace channels: roughly −10 to −4.
- Courtyard, workshops and ordinary rooms: 0 to +8.
- Galleries, nursery terraces and wall walks: +8 to +20.
- Main hall vault: approximately +35 to +45.
- Tallest architectural crown/tower cap: approximately +60 to +70.

These are design ranges. They must be resolved together with headroom, spawn
pads, the island and flight clearance. Keep occupied roofs below the realm's
Y170 moon-only spawn band. Do not solve the height problem by increasing it
until towers intersect moons or accidentally become Blastmaw-only spawn ground.

## 4. The visit, as a player

### Arrival and the outer precinct

After the island journey, the player can identify the keep and gatehouse from
the bridge. The last approach bends around a buttress or broken curtain wall,
then reveals the gate. There are alternate building/flight approaches, but the
intended entrance is unmistakable from its scale, road, lighting and framing.

The outer precinct contains a guard post, a damaged training yard and a small
supply building. It establishes danger without opening with every Apex role
at once. A retained warded preparation room provides the existing practical
crafting/smithing/Ender Chest functions in a recognizable wayhouse. It remains
a room in the world, without a tutorial panel or mandatory conversation.

### The inner courtyard

Passing the gatehouse reveals the central keep beyond a courtyard. All three
wing entrances are reachable from this hub before opening the boss gate.
The player can see their distinct architecture and choose a direction. No wing
requires going through the boss chamber to reach another wing.

A cloister and wall walk create a second route around the court. Stairs, windows
and overlooks show their relationship to the ground route. A player completing
a wing emerges at a recognizable view of the courtyard and keep instead of
being deposited in another anonymous corridor.

### The first wing, return and choice

Each wing contains a short sequence of inhabited-looking spaces, one major
encounter chamber, a meaningful reward and a route back. One or two smaller
threat nodes can precede the chamber, within a fixed shared actor budget. Leave
quiet rooms between threats. Twenty rooms must not mean twenty arenas or twenty
rounds of the same waves.

The first key immediately appears after its actual encounter, preserving the
glowing pickup/recovery behavior. Returning through a newly opened shortcut is
shorter and presents the keep's key mechanism naturally. The second wing is a
choice, not a compulsory order disguised as branching.

### The throne threshold and optional third route

The main door shows **two large key seats**, rather than three empty sockets
that imply all three are compulsory. Either two distinct wing keys fill them,
showing the inserted symbols/colors. Keep the original matching-site/distinct-key
rules. Normal weapon-in-hand/offhand-key interaction remains supported.

A separate archive entrance visibly has the extra key seat. The third wing is
therefore an optional expedition for substantial extra wealth and a genetic
archive, rather than an unexplained leftover key or an invisible prerequisite.
Losing a key must retain the existing replacement route without refilling loot.

Flight, mining or breaking an ordinary wall can bypass travel. Boss activation
and final rewards still require their actual keys/fight. Do not use invisible
anti-flight walls, silently disable equipment or fill exposed windows with
unexplained barriers to force the intended tour.

## 5. Three wings with architectural identities

| Wing | Actual building/use | Planned room sequence | Tactical character |
| --- | --- | --- | --- |
| Glass Gallery | Observatory and resonant study above a tall, light-filled hall. | Map/study vestibule → real stair tower → overlooking gallery → observatory encounter; optional damaged roof walk returns to the court. | Multiple lateral crossing lanes, conspicuous aiming directions, lower and upper routes. Prismmaw beams still pierce walls: glass/pillars are never sold as beam-proof cover. |
| Brood Vaults | Former communal nurseries, grain stores and terraced nesting courts. | Seed store and feeding room → divided nursery → raised keeper passage → brood court/encounter; service gate opens back into the cloister. | Broad movement around connected nesting islands, visible spawn space and meaningful flanking choices. Avoid a cramped maze that turns brood into unavoidable body blocking. |
| Heart Furnace | Lower workshop, water/cooling works and a support nexus built into the foundations. | Loading store → workshop/forge → cooling gallery → chained heart chamber/encounter; freight stair returns toward the courtyard. | Intersecting sightlines, height transitions and room to respond to hooks/anvils. Keep hot channels legible, with more than one usable exit. |

The three wings must also differ in plan and volume. Make the observatory a
narrow, tall gallery/stair-tower cluster; spread the nurseries across staggered
courts and smaller roofed rooms; let the furnace descend beneath the keep's
foundations. Do not put a different tower on top of each of the old three squares
and call that a new floor plan. The main hall is the one deliberately monumental
open room; its surrounding occupied ranges and roof structure break up the mass.

A named room needs evidence of its purpose: a store has shelving and bins; a
nursery has recognizable nests and keeper access; an observatory has instruments,
a sky view and a working circulation route. A few blocks placed in an otherwise
empty square do not qualify.

Use around **18–24 purposeful spaces** across the whole precinct as a first
content target, including stairs, stores, studies, service rooms and the major
halls. Many are small. The number is an estimate for a coherent castle, not a
completion metric or a reason to add redundant rooms.

## 6. Architectural language and detail

### Structural depth before surface noise

Design one complete structural bay: foundation/plinth, real pier, arch or beam,
recessed wall/window, usable gallery and roof connection. Repeat it with corner,
entrance and damaged variants. Structural rhythms should be readable across
approximately 10–14 blocks rather than stretching an undecorated plane across
an entire hall. Long retaining walls can remain massive where that makes sense.

Walls should have meaningful depth: inset openings, projecting supports,
cornices, sill levels and changes of material at floor/roof transitions. Thick
openings must have reveals and readable thresholds. Roofs get ridges, eaves,
vault ribs, parapets, gutters or deliberate collapse appropriate to the building.
Corner posts sticking above a flat sheet are not towers.

Use a disciplined material hierarchy: Rootstone as the major load-bearing root
frame, tuff/deepslate as stone construction, guava/dark timber as inhabited
infill, with calcite/glass, moss/wood and basalt/copper identifying the wings.
Weathering follows leaks, ground contact, collapse and roots. Avoid randomized
block noise applied equally to every wall and roof.

### Furnishing and lighting

Furnishings define activity and scale without obstructing every route. Use
workbenches, shelves, food stores, old nest recesses, records, chains and supply
containers where they belong. Floors vary between broad halls, stair landings,
carpet runners, service paving and small dropped-level courts.

Put light in fixtures: wall brackets, suspended frames, lantern niches, windows,
forge apertures and root cavities. Reserve floor lighting for a deliberate inlay
or technical installation. Do not use the current sea-lantern runway as the
main navigation system. Ordinary lighting must reveal edges and stairs; darkness
is atmosphere, not a substitute for architecture or a blindness challenge.

### Navigation through architecture

Use a tower visible above roofs, the keep's silhouette, repeated window views,
wing-specific materials, distinct emblems and warm/cool lighting. Retain small
matching key symbols and short useful feedback. Avoid requiring signs, arrows,
colored highway strips, a minimap, a HUD checklist or reading a manual.

Color is a supporting cue. Shape/material and the connection back to the hub
must also work for someone who cannot distinguish the wing colors.

## 7. The Crown hall

The boss hall becomes the inside of an actual keep. Deep wall bays, an arcade,
upper galleries, thick buttresses and roof ribs surround the required combat
space. A raised throne/root dais gives the view a destination. Broken vault
sections or an open lantern expose the sky above the flight space, while the
outer roofline and supports still read as a constructed hall.

Keep the accepted boss phase rules, health/damage, contribution eligibility,
finite support pack, warning times and reward conservation. Architecture must
support the existing movement and warning visibility, rather than requiring
another balancing pass to excuse a poorly fitted room.

Cover and routes have honest functions. Ordinary cover can affect sight,
movement or suitable blast/projectile interactions. It cannot stop an active
block-piercing beam. Upper routes need real stairs and useful connections; they
must not be a permanent safe ledge against actors unable to reach them. Test
actual actor dimensions and paths rather than assuming human door proportions.

Give the room controlled, selective destructibility. Critical gates, controllers,
return machinery, essential floor/support paths and structural root cores retain
their protections. Breakable infill, shutters and incidental scenery can show
the violence of the encounter. Do not let one explosion erase the only stair or
seal, and do not automatically refill stock or reconstruct player-altered walls.

## 8. Exploration and rewards

The castle must reward looking around without turning every cupboard into a
mandatory collectible. The existing generous ingredient economy remains the
baseline. Food and workshop stores contain useful bulk supplies; studies hold
records and archives; major wing rewards sit in an identifiable secure room.
The final vault is visibly part of the keep's foundations/treasury.

Include a few readable discoveries: a broken gallery reveals another floor; a
service hatch leads behind a nursery store; a damaged parapet exposes an optional
roof crossing; a root cellar contains a frozen line archive. Each should have a
visual clue or logical location. Avoid one unmarked button that is essential to
progress, arbitrary hidden-key hunts or mandatory lore puzzles.

Unique parts, paid rematches, keys, one-time caches and personal Core/trophy
entitlements keep their existing authoritative sources and journals. Moving a
chest or adding a room must not create extra free rewards on load/repair.

## 9. Implementation approach when development resumes

### Author buildings, then assemble a bounded plan

Replace the growing single coordinate-predicate function with authored modules
and a deterministic site plan: gatehouse, wall bays/corners, towers, stairs,
cloisters, roof sections, furnished rooms and major encounter halls. Native
structure templates are the preferred representation, with processors for a
small amount of appropriate variation. The room graph is curated and validated;
do not use unrestricted random room assembly and hope it resembles a castle.

Freeze the site's chosen pieces, transforms, connectors, elevations, encounter
anchors and revision in saved structure data. Keep variation subordinate to a
coherent silhouette and route graph. A castle should still be legible across
seeds, orientations and optional damaged sections.

### Old saves and controller geometry

Introduce an explicit layout revision. Old sites retain their old geometry,
identities, keys, stock, cleared wings and encounters. Repairs must dispatch to
the site's original layout; they must not convert a player's old dungeon into
an incompatible new one. Do not reuse an old seal's coordinates for a different
room or interpret missing revision data as the new layout.

New controllers need explicit saved encounter centers, bounds, spawn pads and
door positions, replacing assumptions such as a fixed `wing()` offset or radius
19. Preserve existing key role IDs. Detached ProtoChunk configuration must remain
pure data, with no live Level dependency. Use actual native inventory journals
and protection/reentry checks for rewards and repair.

### Island integration and loading

Resolve the castle's footprint and levels against the island plan. Retain the
latest rare Crown placement and intermediate-island journey. Grade local terraces
and entrances; clear occupied interiors and reserve canopy clearance. Do not
flatten/sterilize the whole island or move a saved portal return to suit a new
layout. New architecture only belongs in new eligible structure generation.

Keep loaded-area work bounded. Use finite saved patrol/encounter groups and
shared local actor budgets; unused wings should not all stay active. Short
leases retain only relevant already-loaded fighting areas. A larger castle is
not authorization to force-load the whole site or an outer terrain ring.

## 10. Work order and evidence required

1. **Architectural blockout.** Produce an exterior composition, a measured
   floor/section plan and a traversal graph. Resolve all three independent wing
   approaches, the two-key threshold, optional archive and actual boss clearance.
2. **One finished slice.** Build a gatehouse/courtyard connection and one complete
   wing, including stairs, usable gallery, wall/roof sections, furnishings and
   lighting. Inspect player-height views in neutral lighting before replicating
   its construction grammar across a whole castle.
3. **Complete the precinct.** Add the other wings, supporting rooms, shortcuts,
   root foundations and keep shell. Review silhouette from the approaching bridge
   and check that the island remains visibly an island around it.
4. **Connect actual mechanics.** Wire the revised bounds/spawn pads and existing
   encounter/key/reward behavior. Verify the finite groups, safe exits, unloaded
   neighbors and old-site repair/versioning. Keep checks focused on those risks.
5. **Visual and gameplay review.** Capture the built exterior, each wing at player
   height, a furnished service room, a stair/gallery transition and the boss hall.
   Inspect routes with ordinary gear, actual scaled Apexes and solo/co-op movement.
   No build/test count alone establishes that the architecture is good.

A diagram of labeled rectangles is a planning aid, not visual acceptance.
The finished slice must already look like part of a real castle from the doorway.
If it still looks like the current empty box with added trim, revise that slice
before expanding the generator. This is an internal quality checkpoint, not a
new requirement to ask permission for every routine design choice.

The original review turn ended with planning/evidence only. The subsequent
implementation request authorized the runtime architecture described above.

## 11. Implementation record

`art/crown_citadel.py` now authors a fixed 240-block precinct from reusable
architectural modules and exports 188 chunk-sized native StructureTemplates.
The repeatable geometry/traversal audit reads those shipped NBTs, not an alternate
preview layout. `art/render_citadel.py` renders them through Blender at the end of
implementation; see [the final previews](../art/citadel/) and their provenance.
The original [V1 inspection](../art/planning/crown_current_geometry.png) remains
available for comparison. Neutral ambient material fill aids architectural
inspection; terrain, actors and native client lighting are not simulated.

The library has two usable floors, two stair routes and an atrium; its upper
entrance reaches the octagonal observatory. The roofwalk ends outside the keep,
so it does not open an intended bypass around the throne gate. Keep galleries
have their own internal stairs. Brood stores, seed kitchen, four small nurseries
and an open court form a loop. The furnace has an upper loading/workshop hall,
a real freight stair, lower cooling gallery, chained heart chamber and vents.
The courtyard's preparation buildings, recessed archive/treasury, rose window,
framed thresholds and uneven gate towers provide distinct arrival scales.

Two physical seats flank the throne gate. They preserve the actual order of the
first two distinct keys; the remaining key opens the separate archive. A finite saved guard group per wing/entry precinct uses the existing encounter budget,
pays no wing cache/key and never rearms on reset/reload. Trials retain two finite waves and the same key/payment journals. The later
dungeon-specific balance feedback supersedes their initial rosters and values;
see `CROWN_WARREN_SPECS.md` for the current four/five champion waves and finale. Protected Rootstone floors retain the fighting
space, with small recessed warm light grains rather than a colored route.

New pieces save revision 2; missing revision means V1. Each seal saves its
resolved encounter center, full room bounds, pads, patrol, gates and reward
niches. Detached ProtoChunk placement is data-only. Each pass writes its own
chunk, guards world reads before template processing, respects containers and
completed chunks, and extends only authored root feet to nearby terrain. Repairs
follow the saved revision, preserve progress/stock and cannot revive paid doors.

Headless geometry and native tests support implementation correctness. Natural
approach/island appearance, client frame rate, multiplayer movement and the
finished solo/co-op experience still require actual play review; they are not
established by a render or test count.
