# Bunnidog rendering

The former Low model is the sole base: **2,172 quads, 8,688 submitted vertices,
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

For that lighting change, mesh positions stayed identical to the approved Low model; face count,
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

## Optional inherited webbing (A8)

The model now also stores 72 optional membrane faces on four leg-child parts.
Only an expressed webbed animal renders them (2,244 faces / 8,976 vertices
before any fertility mark);
ordinary animals keep the exact approved base geometry and original 2,172-face
render budget. Gene selection and water paddling reset per animal. The twelve
atlases, mesh, skeleton and editable Blockbench model are regenerated together.
Head chart/lighting and photographic source assets remain. Actual-geometry
previews and native mesh/render checks pass; client review is still separate.

A9 adds four mutually exclusive small fertility marks on tail children, costing
0/40/18/120/38 faces for average/prolific/shy/swarm/starborn. The mesh stores
2,460 faces / 9,840 vertices, while the largest sculpt-only combination is 2,364
faces / 9,456 vertices. An unmarked non-webbed animal retains the original
2,172-face render budget and all original coordinates/normals. The renderer
selects and resets variants in the existing draw pass; there is no new level
of detail or global character redesign. Actual geometry previews are inspected.

H11's optional bundle panniers use their own native equipment pass and 64×32
atlas, adding 0/78/96/114/132 faces for zero through four bundles. Empty animals
skip the pass. The largest equipped webbed/swarm animal is 2,496 faces / 9,984
vertices. The approved head, smooth fur normals/UVs and stored sculpt remain
exact; shared body/pup transforms and actual equipment visibility/draw budgets
pass the model gate. Fitted actual-sculpt preview inspected; native GPU appearance
and performance still need gameplay review. Source: `art/gen_packs.py`.
