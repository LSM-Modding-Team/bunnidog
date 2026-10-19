# Codex working instructions

Work in this checkout. Read `START_HERE.md` for current status, then only the
specifications and source relevant to the request. Historical documents preserve
past decisions; their Claude sandbox, chunk-size, upload and approval routines
are obsolete. The user's current request determines scope.

- Complete the requested change through implementation and appropriate validation.
  For continuations, prioritize unfinished accepted features in the plans over
  further quality-of-life additions. Audit implementation claims against code;
  distinguish missing implementation from features awaiting manual playtesting.
  Do not limit work to 2–6 files, one API, or one feature fragment per turn.
  Do not start unrelated roadmap work when fixing a reported issue.
- Continuation preference: when the user says "continue", advance through as much
  accepted roadmap work as the available turn/context budget reasonably permits,
  in coherent, validated batches. Do not stop after one small feature by default.
  Reserve enough budget to finish implementation, run checks, update the current
  status, review the diff, commit and push to `main`, and check the GitHub build
  artifact. These continuation requests authorize that commit/push without another
  confirmation. A completed feature, commit or push is a checkpoint, not by
  itself a reason to stop a continuation. Continue into the next useful work
  while budget permits; reserve time for final validation and delivery.
- Reported usage is a work target: when the user says "100% left", aim to use
  that available allowance in one sustained response, with a small delivery
  reserve. Apply the same preference to other reported percentages. Calibrate
  from the user's reported before/after usage. Feature and file
  counts are not quota measurements; more features alone do not establish a
  larger batch. Never claim exact consumption without a current tool reading.
  Keep advancing useful coherent work across checkpoints and context compaction;
  do not end at an arbitrary feature count or treat the first green build as
  completion. Use the live usage tool when exposed; without it, be candid that
  exact consumption cannot be metered and use the available turn budget as
  fully as practical. Do not pad work with redundant checks or filler features.
- Usage checks: discover and call `get_usage_limits` at the start of substantial
  continuations and between meaningful batches. Read the percentage used and
  reset time for both the 5-hour and weekly limits; use those results alongside
  the remaining turn/context budget to decide how far to advance. When usage
  approaches a limit, reserve enough capacity for validation, docs, review,
  commit/push and the GitHub artifact check. Do not stop merely because one
  batch is complete while usable budget remains. If this tool is not exposed
  in the current session, say so briefly, use the available work budget and do
  not invent quota values or present an old reading as current. Leave a precise
  handoff if a real limit prevents completion.
- Design latitude: the user authorizes and encourages substantial additions or
  changes beyond the written roadmap when they fit the mod, including custom
  loot tables, block behavior and mechanics. Use judgment and proceed without
  another approval loop; document the rationale, update affected plans and
  validate the result. This supersedes older planned-only or optional-feature
  approval gates. Preserve saves, established design goals and the current task
  scope; an explicit pause on development still applies. Do not add features
  merely to consume budget or divert a focused fix into unrelated development.
- Work directly on `main`. Do not create additional branches unless the user
  explicitly requests one.
- Preserve existing uncommitted work and player saves. Use Git diffs to review
  changes; do not reset the checkout or delete `run/`. Commit/push when explicitly
  requested or under the standing continuation authorization above; preserve
  unrelated user changes rather than including them in a development batch.
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
  The user prefers development progress over extra checking: reuse focused
  existing checks, combine validation at coherent batch checkpoints, and repeat
  only for changed behavior or to resolve an actual compile/test failure.
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
