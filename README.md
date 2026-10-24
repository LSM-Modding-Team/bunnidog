# Bunnidogs

A NeoForge mod for Minecraft 26.2: baby-faced creatures, living warrens and
inherited traits that shape companions, gardens and breeding lines. Client and
server both need the current mod (network **24**).

Interact with a bunnidog to choose **Care, Orders or Work** beside the visible
animal. Calm animals stay and face you while you interact. Ordinary held items
work too; guavas open food choices, and other food/tools retain their direct uses.
Cargo appears when it carries bundles. Calipers offer **Measure** for genetics;
crouch-use opens requested health, home, work, field and combat readings. There are no automatic crosshair instructions. Nests work directly
in the world. Troughs, Pens, Cradles, Juicers, Tables, Jars and Pedestals use
ordinary inventory slots: place exactly the items you choose or shift-click
supplies. Stations use the familiar gray container background; work modes and
commission choices have separate pages. Cargo uses the bundles’ native hover
contents. Orders and committed care return to the world; settings stay open.
Plain held materials can inspect stations, with normal crouch-placement preserved.
Every action uses real supplies.

1. Find guavas and a bunnidog. Care heals with a plain guava; healthy adults need
   no meals. Each adult can claim its own Nest, made from wheat and any planks.
2. Offer a guava to each of two ready adults and choose Courtship. Keep them
   close; they produce one litter seed. Opening the choices spends nothing.
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

**Work → Lessons** teaches real harvest, garden, nursery and nearby
trough transfers. Four inherited mind genes set learning potential; practice is
personal. Work/Lessons show actual practice activity, waiting and recovery.
Some minds need guidance; others learn to work alone. Existing adults
keep familiar chores. Calipers and the Genetics Table have a separate **Mind**
view for cognitive copies, capacities and exact pup forecasts.

A Genetics Table breeding goal compares two reusable samples toward a chosen commission.
Compare a real seed at its saved quality, find matching living offspring and use
Calipers to check a pup before raising it. Food helps growth; it adds no bred
points. Tables, Boards and Breeder's Pens accept feathers or Whiskers.
Workshops inspect before acting. Native slots handle stock; concise controls
handle analysis, work settings and requested markers for judging, hatching and
optional Apex preparation.

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

Download the installable **JAR** from [GitHub Releases](https://github.com/LSM-Modding-Team/bunnidog/releases).
Every successful push to `main` publishes its exact checked CI JAR as a development
prerelease, with a SHA-256 checksum. The Build run's **Publish direct JAR download**
summary also has a **Download JAR** link. Install that file directly in `mods/`.
GitHub's Actions artifact button always downloads a ZIP; use the release JAR/link
for sharing. Chrome accepted the direct-release JAR with protection enabled.

## Apex encounters

The original Apex now has 60% more damage, with health unchanged. Craft a sigil
for Prismmaw (mouth beam), Broodcaller (swarm), Blastmaw (repeated huge explosions)
or Lifebinder (hook/drain/healing). Flying versions exist for all five roles.
Use the sigil on a Crucible with the actual Tissue Sample in your other hand;
reviewing is free, and a successful Survival summon spends the sigil alongside
the existing sample/five offerings. [Actual mechanics](docs/APEX_SPECS.md).

## Development plans

[The Apex expansion plan](docs/APEX_EXPANSION_PLAN.md) tracks the combat roles and
flying variants, plus the future rich Apex-populated realm and final dungeon boss. The
combat roles and flying types are implemented; the realm, expanded equipment/loot
and final dungeon remain planned.

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
