# Bunnidogs (NeoForge, Minecraft 26.2)

A small creature with a baby's face, a bunny's ears and a dog's body, and a full breeding/genetics mod around it: seeds and guavas, warrens and villages, drops, a Pedigree Book, a Genetics Table, homes.

**Run:** `./gradlew runClient`, then in a creative world `/summon bunnidogs:bunnidog`. **Build:** `./gradlew build` (JDK 25; first run downloads a lot).
**Play guide:** `docs/FEATURES.md` (how to use every feature; start with "Breed them").
**Dev client options:** `dev/options.txt` is copied to `run/options.txt` the first time the client runs from a fresh checkout (only if that file is missing).

Checks that need no Minecraft: `java tools/ParseAll.java src/main/java` (syntax of every source) and the genetics/layout self-check (commands in `docs/REFERENCE.md`, "Genetics self-check").
Regenerate art: `pip install pillow`, then `python3 art/gen_bunnidog.py` (other textures: `art/gen_*.py`).

## Continuing with Claude
Upload this whole zip in a new conversation and say what happened with the last build, in your own words: **"it worked, continue"**, **"it compiled"**, or paste the compile errors. Claude reads `START_HERE.md` (current status, what to do, where everything is) and does the next small chunk.
Claude cannot compile: you build and run, Claude writes code and does syntax/logic checks only.

## Docs at a glance
`START_HERE.md` (read first) / `docs/CHUNKS.md` (status of every chunk) / `docs/ROADMAP.md` (what is next) / `docs/REFERENCE.md` (layout, conventions, process) / `docs/DESIGN.md` (game design) / `docs/API_NOTES.md` (verified Minecraft API facts) / `docs/*_SPECS.md` and `docs/WARREN_PLAN.md` (feature plans) / `docs/history/` (everything old).
