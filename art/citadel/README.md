# Crown citadel V2 inspections

These images render the **shipped native structure NBT**, with its actual block
positions and source textures. They are architectural inspections, not Minecraft
client screenshots or generated concept art. Blender Cycles uses neutral ambient
material fill for readable interiors; minor block shapes are approximated. Native
light propagation, terrain, actors, client animation and frame rate are not simulated.
`provenance.json` records the exact SHA-256 of every input template.

From the repository root, after Gradle has resolved Minecraft and Blender 4.3 is
on PATH:

```sh
python art/gen_citadel_details.py
python art/crown_citadel.py
python art/inspect_citadel.py
python art/render_citadel.py
```

Set `BUNNIDOG_CLIENT_JAR` if the resolved Minecraft client JAR is outside the usual
Gradle cache. An optional view name, such as `library`, renders only that view.
The last delivery uses the full view set. Each renderer invocation has its own
immutable temporary mesh/input cache; do not label old views with new provenance.

The authoring script builds reusable rooms, wall bays, arches, stairs, towers,
roofs and furnishing into 188 chunk-sized native templates. Its metadata supplies
the actual encounter positions, pads, gate boxes and reward niches. Runtime
controllers save their resolved geometry and piece revision. Treat this layout's
coordinate contract as V2; a future incompatible layout needs a new revision and
new template paths, while retaining V1/V2 for old saves and repair.

`../planning/crown_current_geometry.png` preserves the original V1 inspection.

## Views

[Overview](overview.png) · [Exterior](exterior.png) · [Gatehouse](gatehouse.png) ·
[Courtyard](courtyard.png) · [Library](library.png) · [Stair/gallery](stair_gallery.png) ·
[Observatory](observatory.png) · [Brood court](nurseries.png) ·
[Nursery room](nursery_room.png) · [Furnace](furnace.png) · [Crown hall](crown_hall.png)
