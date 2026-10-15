# Codex working instructions

Work in this checkout. Read `START_HERE.md` for current status, then only the
specifications and source relevant to the request. Historical documents preserve
past decisions; their Claude sandbox, chunk-size, upload and approval routines
are obsolete. The user's current request determines scope.

- Complete the requested change through implementation and appropriate validation.
  Do not limit work to 2–6 files, one API, or one feature fragment per turn.
  Do not start unrelated roadmap work when fixing a reported issue.
- Work directly on `main`. Do not create additional branches unless the user
  explicitly requests one.
- Preserve existing uncommitted work and player saves. Use Git diffs to review
  changes; do not reset the checkout, delete `run/`, or commit/push unasked.
- Run actual Gradle compilation for Java/resource/model changes when available:
  `./gradlew build` (Windows: `gradlew.bat build`), JDK 25. Report the actual
  result. Syntax parsing is a fallback, not evidence that the mod compiles.
- For mesh/renderer/animation changes, also run `./gradlew checkBunnidogModel`.
  For visual changes regenerate the affected art and inspect a relevant preview.
  This headless check does not establish in-game appearance or performance.
- Keep verification proportional. Run the large `tools/GenomeCheck.java` suite
  only for meaningful changes to pure genetics/layout logic; avoid repetitive
  runs, mutation exercises, source-string assertions and inflated check counts.
  Documentation-only edits need no game build.
- Check Minecraft 26.2/NeoForge APIs against this checkout's resolved sources
  and compiler, using `docs/API_NOTES.md` as a starting index. Do not assume
  cached notes from older versions are authoritative.
- Pure rules belong in `genetics/` or `layout/`; keep Minecraft glue thin and
  client classes client-only. Preserve saved-genome allele order and formats.
- Art source of truth: `art/sculpt_bunnidog.py`, `art/gen_bunnidog.py`, and
  `art/reference/`. Regenerate derived meshes/atlases/Blockbench files together.
  Preserve approved anatomy and photographic imperfections; recognition of the
  reference matters more than a Minecraft-like style. Respect the requested scope.
- Update current status and directly affected docs when behavior or validation
  changes. No mandatory edits to every spec, design ledger and session log.
  Record build, headless, preview and actual gameplay evidence separately;
  never turn a vague approval into verification of unrelated features.
- Prefer a reviewable workspace diff and concise handoff. Build the JAR in
  `build/libs/`. A source ZIP is optional for transfers (`packageProject`),
  not a requirement for continuing in Codex. Exclude caches, saves and secrets.
- Delivery preference: the user downloads JARs from GitHub. Do not attach or
  link JAR downloads in chat; use GitHub Actions build artifacts.
- Answer in the user's current language; keep code/docs English. Ask only for
  material missing decisions. A request to plan only means plan only; ordinary
  implementation requests authorize implementation without another approval loop.
