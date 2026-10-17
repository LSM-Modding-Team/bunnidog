# Bunnidogs

A NeoForge mod for Minecraft 26.2: a baby-faced creature with rabbit ears and a
dog's body, plus breeding, genetics, guavas, warrens, homes and pedigree tools.
Requires JDK 25. The checked-in Gradle wrapper selects the project Gradle version.

Craft a vanilla book with any guava for the **40-page Field Guide**, with two
linked contents pages covering its chapters. Sneak-use
Calipers for care/work readiness, use a hoe on a Feeding Trough to enable
resident gardening, and explore food stores for supplies suited to each lineage.
Living glow/starborn lines yield renewable dust, while inherited terrain traits
and resident shore fishing give more reasons to develop distinct breeds.
Residents also seek safer ground around threats and shelter from thunderstorms.
Hoppers supply/drain troughs, and comparators read their stock.
Sneak-use food to reserve a trough for its exact type/components.
Ring a nearby bell to call healthy residents for a daytime visit.
Woolly adults yield wool in their expressed coat's palette; Calipers names it.
Dispensers use Brushes/Sampling Syringes with the same animal recovery as hand use.
Travel Baskets move individual animals while preserving identity and care state.
Bloom +10 residents cultivate matching unripe crops beside enabled troughs;
ordinary foragers also gather sweet berries while keeping the bush.
Use a compass on a Nest to mark the place, or inspect it empty-handed.
Injured adults can take safe trough meals within four blocks of home at night.
Sneak-use record books with a filled basket in the other hand to log its animal.
Nursery dispensers plant litter seeds above farmland and ripen saved crops with
Starlit Dust, retaining items on refusal. Seed Crops maintain dry farmland.

## Build and play

```sh
./gradlew build
./gradlew runClient
```

On Windows use `gradlew.bat` in place of `./gradlew`. The first build downloads
Minecraft/NeoForge dependencies. Output: `build/libs/bunnidogs-1.0.0.jar`.
Install it in a compatible NeoForge instance, or summon in the development client
with `/summon bunnidogs:bunnidog`. Client settings and worlds persist in `run/`;
`dev/options.txt` supplies initial settings only when none exist.

## Model and downloads

The former Low model is now the only model: 2,172 quads, 8,688 vertices and
512px coat atlases. Graphics presets and distance switching have been removed.
The head uses continuous UVs and surface-derived normals to avoid horizontal
texture and lighting bands, preserving the approved Low geometry and nose.
See [rendering details](docs/GRAPHICS.md).

Download builds from GitHub **Actions → Build → a successful run → Artifacts**.
The artifact ZIP contains the installable JAR. The workflow runs on pushes and
pull requests, and can also be started manually. A green build establishes
compilation and headless checks; final appearance still needs gameplay review.

## Working with Codex

Open this folder in Codex and describe the change. Keep using the same checkout;
there is no need to upload the project again after each message. Codex reads
`AGENTS.md` and `START_HERE.md`, edits the project, runs relevant checks, and
reports what passed and what still needs gameplay review. Review the workspace
Git diff. Existing uncommitted work and saves are preserved.

```sh
./gradlew checkBunnidogModel  # real mesh loading/render path, no game window
./gradlew checkWarrenNetwork  # terrain, connectivity and chunk ownership (also part of build)
./gradlew checkWoodAssets    # guava mechanisms/signs, attachments, resources and crafting (also part of build)
./gradlew checkGuavaBoats    # native boat layers/paddles, atlases, icons and crafting (also part of build)
./gradlew checkDiets         # legacy data, inherited diets, food rules and seven-gene pages (also part of build)
./gradlew runGameTestServer  # native server: care, food, structures, vehicles, lab, shows and Apex
./gradlew packageProject     # optional portable source ZIP in build/distributions
```

The headless model check covers mesh integrity, smooth normals, rendering and
gene/baby ground contact; use `runClient` for appearance, motion and performance.
Development-only GameTests live in `tools/gametest` and run in `build/game-test`,
separate from player saves. Their classes and fixture are excluded from the
shipped JAR. GitHub Actions runs these server checks before uploading the artifact.
See [current status](START_HERE.md), [player guide](docs/FEATURES.md),
[roadmap](docs/ROADMAP.md), and [architecture/reference](docs/REFERENCE.md).

## Art

Install Python 3 with NumPy and Pillow, preferably in a virtual environment:

```sh
python -m pip install -r art/requirements.txt
python art/gen_bunnidog.py
python art/preview_bunnidog.py
```

Regeneration updates the twelve 512px coat atlases, binary mesh, generated Java skeleton
and editable `art/bunnidog.bbmodel`. Inspect `art/bunnidog_model_preview.png`.
See [model details](docs/BUNNIDOG_MODEL.md). Other art has its own `art/gen_*.py`
script. Generated outputs are committed so ordinary Java builds need no Python.
