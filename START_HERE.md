# Current project status

Read `AGENTS.md` for the Codex workflow and `README.md` for build/run commands.
This file records current evidence and pending work; historical process rules
in `docs/history/` do not apply. Follow the current request, not an automatic
next-chunk loop. Code and documentation use English; replies follow the user.

## Project

Bunnidogs is a NeoForge 26.2.0.88 mod for Minecraft 26.2, Java 25 and Gradle
9.2.1. Pure breeding and layout rules live in `genetics/` and `layout/`.
A **warren** includes dens, entrance mounds (mouths) and tunnels; a **settlement**
is the warren plus its surface structures (levels 1–5).

## Current visual work — 2026-10-15

The approved sculpt has a photographic baby face, separate human and rabbit
ears, compact robust body, outward paws and short fur. Keep those proportions
and imperfections; recognizability takes priority over a Minecraft-like style.
Sources and regeneration: `docs/BUNNIDOG_MODEL.md`.

The user compared Low and Ultra in Minecraft, prefers Low (including its nose),
and requested removing the other tiers and the graphics configuration. The
sole model is now the approved Low geometry: 2,172 quads / 8,688 vertices, twelve
512px coat atlases. Presets, distance switching and unused runtime assets are
removed. Face/ear/body vertex positions match the previous Low model exactly.

Horizontal bands had two causes: area-weighted head normals biased by unequal
ring spacing, and independently sampled photo patches. Head normals now follow
derivatives of the authored surface; neighboring head polygons share one
continuous UV chart. Maximum normal change across closely spaced front rings
fell from 0.478 to 0.038. No geometry was added. See `docs/GRAPHICS.md` and
`art/bunnidog_smooth_head.png` (nearest-filtered software preview).

Validation: real Gradle build and headless checks passed for the lightweight
mesh. The check covers winding, coat atlases, rendering, normals, gene/baby foot
contact and regression checks for head UV/lighting continuity. The user's
screenshots establish prior in-game Low/Ultra rendering, not confirmation of
this fix. Final in-game lighting and performance still need gameplay review.

**Delivery preference:** do not attach or link JARs in chat. The user downloads
builds from GitHub. Actions now uploads the validated JAR as a run artifact.

## Gameplay status and pending work

The current art work does not establish that every older gameplay feature works.
The historical ledger is `docs/CHUNKS.md`; its old NOT COMPILED labels describe
evidence at delivery, not the current ability to build the project.

- U6a adds the warren-entry advancement. Test in a naturally generated structure
  (use `/locate structure bunnidogs:warren_village`); the feature generated with
  `/bunnidogs warren 1 surface` does not trigger a structure advancement.
- U1 adds a level-1 surface den, 3–4 Nests, lantern and entrance mounds around
  the tree, falling back to old holes on unsuitable ground. H3b adds house Nests.
  Runtime pathfinding and home claims remain unconfirmed. Test
  `/bunnidogs warren 1 surface` on flat grass: enter the mounds, descend to the
  lit den, inspect nearby dogs' `NestX/Y/Z`, and set time to 13000 for sleeping.
  Levels 3–5 should also have claimable house Nests.
- Earlier user reports are preserved in the ledger. D7b, 7a and 6h had compile
  confirmation without itemized gameplay reports. Do not infer new evidence.
- The warren redesign plan is accepted; U2 (higher-level dens/tunnels) and U6b
  (other dimensions) remain planned. Other candidates are W3 wood components,
  diets and T4 table sample display. See `docs/ROADMAP.md`. Work on these only
  when the user requests continuation or a relevant feature.

## Document map
| Job | Open |
|---|---|
| Status of any chunk, one line each | `docs/CHUNKS.md` (detail: grep the id in `docs/history/CHUNKS_FULL.md`) |
| What to build next, prerequisites | `docs/ROADMAP.md` |
| A Minecraft/NeoForge API name | `docs/API_NOTES.md` (QUICK INDEX, top; grep the lookup table) |
| Project layout, conventions, self-check details, art pipeline, dev environment, user preferences in full | `docs/REFERENCE.md` |
| How a feature is used in game (player view) | `docs/FEATURES.md` |
| Why the game is designed this way, decisions per chunk | `docs/DESIGN.md` (Contents at the top; `grep -n '^## ' docs/DESIGN.md`) |
| Warren redesign: mouths, dens, tunnels, cellars (U1-U7) | `docs/WARREN_PLAN.md` (U1 delivered; later stages planned) |
| Homes / Nests / H-chunks | `docs/HOME_SPECS.md` (ACTIVE: H1-H4 and H3b done) |
| Genetics Table | `docs/TABLE_SPECS.md` (T1-T3 done, T4 left; it wins over any other statement) |
| Drops / loot | `docs/DROPS_PLAN.md` (why) + `docs/DROPS_SPECS.md` (numbers, wins). All of D1-D8 done |
| Pedigree Book, Calipers window, "the sheet" | `docs/SHEET_SPECS.md` (S1-S8 done; wins over older Phase 8 notes) |
| Wood (W3-W4), villages in wood (V1), diets (D1-D4 of that plan) | `docs/FIXES_PLAN.md` sections 4, 5, 6 (F1-F3, W1, W1b, W2 done; V2 superseded by HOME_SPECS) |
| Old phase status, old session notes, old roadmap, old API research | `docs/history/` (never needed to continue) |
