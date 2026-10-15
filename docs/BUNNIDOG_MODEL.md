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
The model has the approved former Low geometry: 2,172 quads and 8,688 vertices.
There is one model and no graphics preset or distance switching.

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
