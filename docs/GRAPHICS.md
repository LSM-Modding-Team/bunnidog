# Bunnidog rendering

The former Low model is the sole model: **2,172 quads, 8,688 submitted vertices,
twelve 512×512 coat atlases**. The user compared Low and Ultra in-game and chose
Low for its performance and nose shape. The presets, client config screen,
distance switching and unused assets are removed. An old graphics config file
is ignored; no world or player data needs migration.

## Horizontal bands

Two independent problems made rows visible:

- Area-weighted vertex normals favored the larger adjacent polygons. Rings
  inserted at contour landmarks can be extremely close to the regular rings,
  causing their normals to differ sharply despite nearly coincident positions.
  Head normals now come from derivatives of the authored surface, independent
  of ring spacing. Across close front rings, the maximum normal difference
  fell from 0.478 to 0.038 without moving any mesh vertices.
- Each face previously had an independently sampled photo patch. Nearest texture
  filtering could expose mismatched samples between patches. A single continuous
  head chart now gives adjacent polygons shared UVs and texels. It occupies a
  508×224 region inside the same 512px atlas, with duplicated edge texels.

The mesh positions are byte-identical to the approved Low model; face count,
nose, ears, stance, bones and animation remain unchanged. The skin/photo and
coat sources are unchanged. Body patches use the remaining atlas space.

## Development and delivery

`python art/gen_bunnidog.py` regenerates the one model and all twelve coats.
`python art/preview_bunnidog.py` writes standard views plus
`art/bunnidog_smooth_head.png`, using nearest filtering for seam review.
`./gradlew build checkBunnidogModel` checks real mesh loading/rendering, shared
head normals and UVs, lighting continuity across nearby rings, atlas sizes,
winding and gene/baby ground contact. New in-game screenshots are needed to
confirm the final lighting under the user's setup.

GitHub Actions builds and uploads the JAR after successful validation. Download
it from the successful **Build** run's **Artifacts** section; no JAR delivery
through chat. The prior four-tier comparison is historical, not current setup.
