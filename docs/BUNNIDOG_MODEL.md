# Bunnidog: contour-led sculpt (2026-10-15)

The head is authored from manually sampled points on the user's blue contour,
with independent left and right temples, cheek corners, jaw changes and chin.
`art/sculpt_bunnidog.py` contains these points in the annotated image's pixel
coordinates. The surface adds recessed sockets, different lower-lid folds,
fuller unequal cheek masses, a stronger bridge and rounded infant nose with alar wings and nostril
recesses, the philtrum, lip rolls and chin. The animal is built from authored
surface sections and anatomical displacement.

Human ears have rolled irregular helices, recessed conchae and lobules. Rabbit
ears have broader fleshy outlines, closer/upright centerlines, rolled rims,
deeper cupped interiors and rounded closed tips. The image-left ear shows more
of its tawny outer face; the slightly taller image-right ear exposes the pink
interior. Their different shapes and twist follow the original photo. Their bases sit inside the skull. Front legs bend
and spread differently; paws turn outward and have four unequal digits with
short nails. Shoulder and hip roots extend inside the torso. The coat combines
short fine fur texture without individual strand geometry; the chest and inner forelegs
show pink, sparsely-haired skin.

The default build follows the reference's stocky proportions: the chest and
flanks extend slightly beyond the head silhouette, the sternum reaches forward
under the jaw, and the forearms stay fleshy down to the wrists. Paws are 25%
wider with more depth and thickness. A half-pixel lower stance shortens the
exposed leg length while keeping the toes planted; the generated skeleton and
animation share the new 5.1-pixel hip height. The abdomen remains clear of the
ground. Painting uses the authored reference-space coordinates before this pose
adjustment, preserving the placement of facial features, skin and fur. Build and
leg genes operate on these baseline proportions.

The two texture-source images in `art/reference/` were generated from the supplied
reference for texture projection. They are texture assets, not screenshots or
renders of the delivered model. The head texture is warped to the traced contour
and facial landmarks; rear/side skin is blended separately to avoid repeating
facial features. Fur uses a dedicated seamless short-coat image. Skin remains
fixed across the twelve coat variants; coat colors and patches apply to fur.

Regenerate (Python + Pillow + NumPy):

```sh
python3 -m pip install -r art/requirements.txt
python3 art/gen_bunnidog.py
python3 art/preview_bunnidog.py
```

The generator exports twelve 512×512 atlases, the bundled `bunnidog.mesh`, an
editable Free Model mesh in `art/bunnidog.bbmodel`, and the Java animation
skeleton in `BunnidogModelLayer.java`. `BunnidogMesh.java` loads the mesh onto the
skeleton and renders interpolated vertex normals, reusing render scratch storage.
The base model has the approved former Low geometry: 2,172 quads and 8,688
vertices. A8 adds four optional, expressed-only webbed-paw children totaling
72 faces / 288 vertices. A9 also adds mutually exclusive tail-tip fertility phenotypes. The stored mesh
contains 2,460 faces / 9,840 vertices; an unmarked non-webbed animal still
renders the original 2,172 faces with exact approved coordinates and normals.
The largest sculpt-only rendered combination is webbed/swarm: 2,364 faces.
There is one model and no graphics preset or distance switching.

H11 adds separate native bundle panniers from `art/gen_packs.py`. Their 64×32
material and equipment layer leave this mesh/atlas intact; unadorned animals
submit no extra pass. One through four bundles add 78/96/114/132 quads, making
the largest currently equipped combination 2,496 faces / 9,984 vertices.
Shared body/root transforms follow the exact approved pivot, leg lift, Build
proportions and pup ground scaling. Native model checks cover actual extra
draws, per-animal visibility reset and body pose; the fitted actual-sculpt
preview is `art/bunnidog_packs_preview.png`. Client playtesting remains separate.

The head uses a continuous UV chart, shared texels across neighboring polygons,
and normals derived from the authored surface instead of uneven adjacent faces.
This removes the sources of horizontal texture/lighting bands without increasing
geometry. See `docs/GRAPHICS.md` for the diagnosis and measured normal continuity.

`art/bunnidog_front.png`, `art/bunnidog_model_preview.png`,
`art/bunnidog_face_depth.png` and `art/bunnidog_smooth_head.png` are software views
of the actual model. The last view uses nearest texture filtering to help review
seams. They are not Minecraft captures or image-generation concept art.

The binary mesh uses big-endian types: magic `BDM2`, bone count (int), then for
each bone its name and parent name (`writeUTF` compatible ASCII), and face count
(int). Each face stores four vertices (x, y, z, u, v, nx, ny, nz floats) followed by
the face normal (3 floats). Coordinates and normals use Java Y-down model space;
UVs are normalized. The loader reverses vertex order after the Y conversion.

Validation: Gradle build with Java 25 / NeoForge 26.2.0.88; load and render through
real Minecraft classes; vertex/UV/normal/winding checks; ground contact with short
and long leg genes; puppy-to-adult reset; visual inspection of all three views.
In-game lighting, motion, and performance with large groups remain unreviewed.
Try `/summon bunnidogs:bunnidog` in a creative world and compare adult/puppy and
different ear, leg and build traits.

## Temple projection fix

The in-game screenshot exposed a pale strip where the frontal photograph's
edge hairs were stretched around the left temple. Scalp painting now smoothly
blends away from the photo before its silhouette, using normalized head width
as well as depth. Facial landmarks stay projected; side skin uses the same
forehead donor. All twelve atlases were regenerated. Geometry and body fur
remain unchanged. `art/bunnidog_temple_fixed.png` is an elevated three-quarter
software preview, generated by `art/preview_bunnidog.py`.

Run `./gradlew checkBunnidogModel` for the reproducible headless Minecraft check.
The user's screenshot confirms the model renders in-game; confirmation of this
texture fix in-game is still pending.

## Current facial depth

The approved Low geometry retains deeper sockets/lid rolls, full asymmetric
cheeks, integrated bridge and infant nose tip, lip rolls and chin. The shading
fix does not move these vertices. The user's Low/Ultra screenshots confirm both
previous tiers ran in-game; confirmation of the continuity fix remains pending.

## Expressed webbed-paw variant (A8)

`art/preview_webbed.py` compares actual ordinary/webbed paw geometry and a
swamp animal, using the regenerated atlas. Only the extra skin membranes
between the existing toes are conditional; nails, paws and all base anatomy
remain. Native render-state extraction follows expressed genes, and model setup
resets visibility and paddling between animals. The real model gate checks both
render budgets, pose resets and the existing lighting/ground-contact invariants.
Preview inspected; native client appearance remains playtest review.

## Visible fertility (A9)

Four optional tail-child groups show the existing expressed Fertility allele:
prolific has one rosette, shy a closed green bud, swarm three rosettes and
starborn a violet star; average keeps the original tip. They inherit normal
tail motion/pup scaling, never indicate current breeding readiness and leave
the fixed face/body unchanged. Only one mark or none renders per animal.
`art/preview_fertility.py` renders the actual exported parts/atlas, and the model
gate checks every mark with webbing and a complete ordinary reset.
