# Bunnidogs

A NeoForge mod for Minecraft 26.2: a baby-faced creature with rabbit ears and a
dog's body, plus breeding, genetics, guavas, warrens, homes and pedigree tools.
Requires JDK 25. The checked-in Gradle wrapper selects the project Gradle version.

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
./gradlew packageProject     # optional portable source ZIP in build/distributions
```

The headless model check covers mesh integrity, smooth normals, rendering and
gene/baby ground contact; use `runClient` for appearance, motion and performance.
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
