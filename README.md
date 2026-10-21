# Bunnidogs

A NeoForge mod for Minecraft 26.2: baby-faced creatures, living warrens and
inherited traits that shape companions, gardens and breeding lines. Client and
server both need the current mod (network **18**).

Start with **empty hands on an animal or care station**. A short live panel shows
its needs, the next useful step and buttons that use your real supplies. The
crosshair hint names the action; normal tools and direct item interactions work too.

1. Find guavas and a bunnidog. Care heals with a plain guava; healthy adults need
   no meals. Each adult can claim its own Nest, made from wheat and any planks.
2. Feed a guava normally to each of two ready adults, or choose Courtship in
   their panels. Keep them close; they produce one litter seed.
3. Plant that seed on farmland. Wait or use bone meal, then break the ripe crop
   to hatch pups. A Cradle is an optional alternative with visible hatch controls.
4. Raise a useful line: equip bundles, train partners of any temperament with a Whistle,
   help a home garden, or choose a Breeder commission and see what is missing.

Six native milestones connect the first session. They do not lock any care
interaction. Basic play needs no manual; the **Field Guide is optional reference**
for deeper genetics and systems. Book + guava makes the Pedigree Book; add paper
for the Field Guide. Saved books, animals and inventories remain compatible.

Minecraft's language setting also selects the mod language. All Spanish options
share one neutral translation based on Español (Perú). The optional guide keeps
short pages; hover its chapter text for the full details.

Useful roles & work shows what an ordinary animal can do now. Later, a Genetics
Table breeding goal compares two reusable samples toward a chosen commission.
Compare a real seed at its saved quality, find matching living offspring and use
Calipers to check a pup before raising it. Food helps growth; it adds no bred
points. Tables, Boards and Breeder's Pens accept feathers or Whiskers.
Workshops inspect before acting; named controls and requested area markers make
stock, signals, judging and optional Apex preparation visible where they happen.

See [the short feature overview](docs/FEATURES.md) for optional depth and
[the cohesion plan](docs/COHESION_PLAN.md) for the current development priorities.

## Build and play

Build with JDK 25.

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

The proposed [intelligence and civilization update](docs/INTELLIGENCE_PLAN.md)
is currently planning only; it is not part of the shipped behavior above.

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
