# Current project status

Read `AGENTS.md` for the continuation workflow, `README.md` for build/run commands,
and only the specifications/source relevant to the next batch. Historical sandbox,
upload and approval routines in `docs/history/` do not apply. User requests and
standing design latitude determine scope; a feature or push is a checkpoint.

## Planned — visible whole-animal equipment

`docs/BUNNIDOG_ARMOUR_PLAN.md` designs one barding item in Leather, Iron,
Diamond, Netherite and Astralite tiers for adult bunnidogs and all Apexes.
The revised upper tiers are full plate, including helmet/visor, ear protection,
enclosed body/belly/rump, all four legs and paws, and an articulated tail guard.
The light harness is retained only for Leather/transitional Iron.
Equipment provides protection, health, damage and modest mobility separately
from genes. Future dungeon actors wear actual Astralite gear instead of the
current enhanced champion genetics/modifiers. The Crowned Apex is rebased to
avoid doubling its already increased stats. Owner handling, native stack
ownership, repairs/drops, basket/vacuum boundaries and old-fight compatibility
are specified. `art/barding_design.png` is a fitted concept over the existing
sculpt, not a shipped model or client capture. Gameplay implementation is pending;
the released dungeon balance below remains current.

## Current — dungeon combat above the Great Warren baseline

Crown trials now have four then five mixed Apex champions, with trained genetic
lines, 1.5× health and 1.15× attack. Party size freezes on entry and adds 15%
health per additional player, within ordinary native health limits. Finite guard
pairs introduce each approach. Wild/ritual Apex tuning and realm spawn density
remain unchanged.

The Crowned Apex has 6,000 solo HP (15,900 with four), 40/56 melee, 32 beam damage
and shorter recovery with the original warnings. Its middle phase has four
champion Apexes plus three brood, and its final phase has two finite flying
Prismmaw escorts. Support healing remains finite. Wing/final generic caches are
richer; existing stock, keys, personal Cores and rematch payment are preserved.
See `docs/CROWN_WARREN_SPECS.md`. This is a dungeon-specific balance pass;
actual solo/co-op Survival pacing remains player review.

## Current — authored Crown citadel

Fresh Crown Warrens use the architectural redesign in
`docs/CROWN_WARREN_REDESIGN_PLAN.md`: a gatehouse with unequal stair towers,
furnished wayhouse/granary, inner court, vaulted keep, two-storey library and
octagonal observatory, terraced nurseries, and a workshop above the lower Heart
Furnace. Independent walking routes reach every wing before the boss gate.
Two actual key seats open that gate; the optional third opens a separate archive.
Accepted Apex/finale values, personal rewards and replacement keys are retained.

`art/crown_citadel.py` authors reusable room/bay/arch/stair/tower/roof modules and
bakes 188 native chunk-sized templates. Controls save explicit world-space
encounter/reward/door geometry and finite one-attempt guards. Old dungeons retain
V1 geometry and progress; loaded-only repair dispatches by saved revision and
never refills containers. Local root feet meet terrain without grading the island.

Final native-template previews are in `art/citadel/`; render provenance records
the exact template hashes. These are headless Blender inspections, with neutral
ambient fill and approximate minor block shapes, not Minecraft client captures.
Native traversal/spawn/stock/save evidence and natural solo/co-op visual review
are separate. See the Crown spec for validation and remaining manual review.

## Current — expedition play-review corrections

Entry is now gated by physical ingredients: fresh ground-role drops mark their
actual origin, and all five plus an Aerial Membrane craft the Resonant Eye.
Anchors consume the Eye/preserve the Crest; linked portals have no personal hunt
check. Historical hunt data and existing items/links are preserved.

Late kits contain 64 golden/5 enchanted golden apples; endgame contains 256/20,
counting the Shulker. All carried/boxed rockets are Flight 1. Moonstone and both
ore blocks require Astralite tools, resist TNT, and can be broken by actual
Blastmaw self-explosions for the first upgrade. Lunar item drops resist fire/blasts.
Blastmaws are rare singles on ordinary land and remain the exclusive moon mobs.

New realm terrain has independent irregular islands, layered hills/hollows and
height-graded routes through intermediate islands. Crown candidates are 9× rarer
by area; only their actual footprint is flat. Fresh arrivals are at least 700
blocks from a Crown. Old chunks/paired returns remain intact. Read the updated
`docs/ASTRALITE_AND_SKY_SPECS.md` and `docs/HEARTH_PROGRESSION_SPECS.md`.

## Current — wild family glades

Read `docs/WILD_FAMILY_SPECS.md`. New qualifying Plains/Sunflower Plains glades
have an empty root cache, finite natural resources and three ordinary frozen
genomes. Founders start with zero bred points and no learned skills. Genetically
capable residents can discover slow primitive harvesting, one-item hauling,
paid native tools, material crops and a soil nursery through actual work. Broad
founder bodies change the room entrance. No tutorial HUD or new setup menu.

Actual parental seeds travel to farmland, grow normally and hatch through native
safe-pad/spawn checks. Pups inherit membership and genes, not education. Native
saves retain exact cargo, finite founder attempts and the site's own identity;
old sites/player households keep their existing gates. The starting cache/Nests
are natural infrastructure, not resident-crafted achievements. Primitive cache
construction, stone/furnace/light, specialist replacement, adoption and trade
remain unfinished. This advances the original Generations direction without
claiming a complete autonomous civilization.

## Current — paid resident first aid

A Family care resident can walk a real guava ration from the Hearth to a quiet,
downed, same-owner companion resting at its original claimed Nest. Actual native
healing permission and callback revalidation precede payment. Mobility returns;
injury recovery remains. Saved ration/patient/owner references, native inventory
journals, inherited limits and original-owner recovery stay authoritative.
Read `docs/CARE_AND_TACTICS_SPECS.md`. Full civic pickup/transport and ordinary
citizen downed policy remain unfinished; this covers paid first aid at home.

## Current — shared professions, public plans and physical crews

Read `docs/HOUSEHOLD_COMMONS_SPECS.md`. Residents now choose from bounded useful
job offers with started-only shared claims. Native recipes, actual stock and
urgent food/care/tool needs drive work; genes and practiced skills remain real.
Capable planners save finite need/dependency instructions in the physical Hearth,
which a trained craftsperson can follow after the planner leaves.

Builders and farmers carry saved materials from the common store to actual
worksites. New surveys fit cardinal rooms and can deliberately authorize further
rooms; supported builders need a nearby master. Paid explosion repair respects
placed-cell receipts, while player-removed walls wait for explicit repair consent.
Farmland can be linked with the existing Staff. Farmers grow material crops
outside their own Diet; native crop loot and Coppice wood/food remain physical
household-marked drops until haulers walk them home. No new setup menu or HUD.

The latest goal is to close the entire accepted roadmap. The first wild
bootstrap now ships as described above; broader workshops, civic downed rescue,
living settlements/trade and advanced public works remain unfinished. `docs/COMPLETION_AUDIT.md` keeps the baseline and concrete remaining
acceptance criteria. Native validation does not replace natural/client/co-op
pacing and performance evidence.

## Current — courier apprentices and household route notes

Read `docs/COURIER_CULTURE_SPECS.md` and the accepted reference in
`docs/COMPLETION_AUDIT.md`. A skilled resident courier can take one walking
pupil, teaching only through actual witnessed pickup and arrival. Mastered
pupils remember the procedure and perform real deliveries themselves. Supported
couriers retain a nearby qualified guide requirement; knowledge is not inherited.

After its own successful delivery, an expert can turn a blank Book and Quill
from the Hearth store into physical readable Route Notes. Skilled residents can
study them after the teacher is gone. Books preserve exact authorized endpoints
and item components, not skill or stock. Private journals, original site/store
identities, protection vetoes and unknown future records stay safe. No new HUD,
configuration menu, remote transfer or forced chunk loading.

This advances local logistics/culture. Shared need/dependency plans, physical
construction and material fields now ship in the current commons spec above.
Primitive wild-family bootstrap and broader civilization remain unfinished. The audit's baseline percentages are reference
estimates, not automatically raised each batch.

## Current — household carers and learned group tactics

Read `docs/CARE_AND_TACTICS_SPECS.md`. Hearth residents can notice a visible
injured housemate resting at its claimed Nest, craft real dressings, collect one,
walk to the patient and apply paid wound care. The existing Nursery skill
is now labeled Family care without changing its saved ID/progress. Its learning
path also learns from actual manual treatment. Unused supplies return physically;
stranded supplies retain their original owner's native Cargo recovery control.
Basic feeding, guava first aid and bonded injury recovery remain unchanged.

Chosen fighters learn group tactics from successful native combat exchanges
with a participating keeper. Learned capable minds spread across visible legal
threats and react to visible chosen partners' attackers. Supported minds need a
recent local combat cue. Instinct, direct focus, held orders and heard recall
remain reliable; body damage/attack cadence and all ordinary Apex values stay
accepted. Requested Calipers/Mind/forecasts expose experience and capacity.

## Current — watched field lessons and learned scouting

Read `docs/FIELD_LEARNING_SPECS.md`. Existing lessons now recognize actual player
harvesting, planting, bone-meal growth, composting and paid Cradle care. Healthy
loose-food collection uses Harvest learning instead of its old stat-only bypass;
injured meals remain instinctive. Scouting is a fifth personally taught routine:
mine visible ore during its field lesson, then use the existing compass survey.
Field lessons need no Nest/daylight; supported minds still require their guide.
Old adult l1 records keep exact progress/familiar surveying; old pups gain no
adult scouting experience. Current practice uses l2, without changing DNA.

## Current — watched deliveries and remembered routes

Read `docs/DELIVERY_LEARNING_SPECS.md`. Lessons now include Watch a delivery:
an eligible adult accompanies a player and observes real native storage pickup
and deposit gestures. It remembers one exact item/filter and two authorized
stores, then walks actual saved parcels between them. Independent learned minds
work alone; supported minds retain their guide requirement. Food reserves,
filters, locks, pauses, store replacement, cargo recovery and safety remain real.
No added tool, route-configuration menu or permanent HUD. The native animal
menu, native container demonstration and unsupervised walking all have coverage.

## Current — society-first Apex progression

Read `docs/HEARTH_PROGRESSION_SPECS.md`. A Hearth household now performs real
renewable harvesting, hauling, crafting, authorized shelter building, meals,
courtship/nursery work and resident-to-offspring instruction. An offspring must
repeat a resident-taught routine independently before the paid Living Keystone
cultivation can finish. The Keystone installs once in a Crucible; repeat fights
use renewable incense, a source sample and optional role sigil. The old five rare
offerings are optional incense ingredients/keepsakes, no longer required kills.

Five actual ground-role remnants and an Aerial Membrane now craft the Eye.
Anchor activation/linked travel use physical resources, with no personal hunt
check. Return travel remains unconditional. Hearth supplies keep vanilla chest
UI and the Staff keeps its existing explicit local gestures. The initial sustainable household is not the entire civilization roadmap.

## Current — Astralite and sky archipelago

Read `docs/ASTRALITE_AND_SKY_SPECS.md`. The Great Warren now generates real void
islands, dungeon-connected minimum-spanning causeways and ore-rich Blastmaw moons
around Y244. Two realm ores and wild Apex drops craft Astralite smithing upgrades,
fully obtainable before the dungeon. Armor has triple Netherite durability and
toughness 5; tools gain modest durability, faster mining and higher attack.
Heartbound artifacts now require Astralite armor. Existing artifact IDs and
paired anchors survive, and generated chunks are not rewritten. Kits explicitly
include Unbreaking III and Mending I on durable late/endgame gear.

## Current gear follow-up

Sneak-use cycles persistent power 1–5 on the Launcher, Sovereign Lance and
Concord Standard. Launcher uses explosion strength 4×power; Lance deals 45×power;
Standard heals five hearts×power and gives Resistance level=power for four
seconds. Fuel and wear scale per level. Native armor durability tags and tool
enchantability now support Mending; Gene Siphon inventory accepts only Bunnidog
Seeds. Scaling juice levels have no mod balance cap except Resistance; native
engine bounds remain. Endgame kits carry Strength XXX infusions and power-five
gear. Read `docs/APEX_REWARDS_SPECS.md` for current values.

## Current dungeon usability fix

Fresh Crown Warrens clear terrain/features throughout their footprint and reserve
that space against later realm landmarks. Cleared halls visibly drop their keys;
matching colored routes/frames lead back to the golden Crown Seal. Two distinct
site-bound keys open the gate. For old obstructed sites, an operator may use
`/bunnidogs dungeon repair` inside the dungeon: loaded chunks only, preserving
container stock and existing seal/encounter state. It restores missing seals with
the original identity, so already collected keys remain valid.

## Operator testing kits

`/kit vanilla|bunnidog early|mid|late|endgame replace` explicitly replaces the
executing operator's inventory and equipment. Tab completion exposes all choices;
omitting `replace` prints the exact command first. Endgame supplies enchanted
armor/tools/weapons (no Thorns/curses), pearls, gliding rockets, blocks, stocked
Shulker and Totems. Bunnidog tiers upgrade actual gear and provide a world-backed
Flask: one long combat blend to two shorter regeneration blends, with genuine
elite genes/points and quality 116 (Strength XXX) at endgame. Native loadout coverage verifies
replacement, real Flask contents and equipped max health.

## Current — Crown Warren finale (AX7)

The latest continuation resumes the accepted roadmap. Read
`docs/CROWN_WARREN_SPECS.md`: three branching halls with site-bound keys, an
optional third-key archive, saved central gate and paid rematches, a three-phase
Crowned Apex, personal Core/trophy entitlements, lavish physical vaults and
frozen genetic archives. Sovereign smithing upgrades extend Heartbound armor,
Lance and Armored Elytra; the Concord Standard adds paid party healing. Existing
ordinary Apex tuning stays accepted. No new tutorial/menu layer.

Protocol **43**, **2,535 authored / 2,663 packaged** language keys, English/shared
neutral Spanish. The native suite now has **248 scenarios**, alongside actual
model/localization/build gates. Save/kill-payment ordering, native health above
1,024, short chunk leases, finite actors, exact smithing components and separate
core/trophy delivery retries are invariants. Native and inspected preview evidence
remain distinct from graphical client play, natural chunk order/performance and
full solo/co-op boss pacing. See the current spec for those practical limits.

Remaining broader roadmap: general shared procedures/plans, expanded
professions/construction/civilization, including full civic downed rescue. The
Hearth household implements a focused cross-stage journey through those systems. Address any newly reported
expedition issues first; do not rebalance the accepted ordinary Apex fights
without player feedback. Further realm regions/gear are optional extensions.

## Previous player review — substantial Apex rewards

Read `docs/APEX_REWARDS_SPECS.md` before the original realm specs. Latest steering
calls for powerful multipart gear and much more generic ruin stock, with unique
parts from Apex kills. All seven requested directions now ship: Heartbound
Netherite with +1.5 equipped hearts/piece and independent 60/45/30/15s heart-loss
recovery; an Elytra smithing upgrade with actual armor/Protection; native power-4
launcher; power-20 rescue Totem with owner immunity/5s stun; stronger armor-piercing
Lance; native-bundle genetic vacuum/exact-copy seeds; ordered world-backed
unbounded potion/juice flask. Rituals also pay parts for pre-realm preparation.

New ruins add a third bulk-supply chest. Existing stock/structures stay untouched.
Native smithing preserves wear, names and enchantments. The old wing-harness ID
stays compatible, now acquired by upgrading Elytra. Existing enemy fight values
stay accepted. Protocol **29**, English/shared neutral Spanish; native suite
**169 cases** plus model/localization/build gates. Runtime/client appearance and
natural economy still need actual play evidence. The subsequent continuation resumes the Crown finale; civilization remains
queued behind the existing intelligence stages.

## Anchor Crest compatibility fix

Attunement accepts the Apex Crest item, including Creative/menu Crests without
a genetic record. Full recorded Crests retain their exact source data. Native
travel coverage exercises both plain and recorded Crests. The later continuation supersedes that review-time roadmap pause.

## Wing art (2026-10-25)

Flying Apexes and genetically winged bunnidogs now render paired feathered wings
with shoulder/outer-wing joints, overlapping flight feathers and a dedicated
cream/brown atlas. Rest folds, flight flaps and descending dive sweeps use render
snapshots. Wings share the exact gene/body/pup pose and Apex renderer scale.
Ground Apex forms remain wingless. Source: `art/gen_wings.py`; top-view geometry
preview: `art/wings_preview.png`. Headless previews do not establish in-game appearance.

## Latest combat timing follow-up (2026-10-25)

Hook aim now locks half a second before firing. Blastmaws stack another 20%
outgoing damage reduction, add ten fuse ticks and twenty fireball cooldown ticks,
and cannot fire during leaps/fuses. Flying Brute dives slow to 1.35 with an
eight-tick particle warning; winged brood now orbit/dive/ascend using genetic
speed, bite damage and recovery. Build and all 157 native scenarios pass, including
the late hook dodge and brood warning/strike/ascent cycle. The headless timing
schematic `art/dive_timing_preview.png` is not an in-game appearance check. See the latest section of `docs/APEX_SPECS.md`.

## Latest balance follow-up

All Apex types now drop 500 base XP (formerly 50). Ground Brute alone gains
30% attack damage, with its ritual forecast updated. On finishing a leap it
clears charging immediately and resumes navigation toward its live target.

## Project and delivery

Bunnidogs targets Minecraft 26.2 / NeoForge 26.2.0.88, Java 25 and Gradle 9.2.1.
Pure rules live in `genetics/` and `layout/`; native glue and client code remain
separate. A warren contains dens, mouths and tunnels; a settlement includes its
surface trees, houses, paths and landmarks (levels 1–5).

Work directly on `main`, preserving player saves and unrelated changes. Push
coherent validated batches under the standing continuation authorization.
The user downloads direct JARs from GitHub Releases; never attach/link a local
JAR in chat. Discover `get_usage_limits` at substantial starts/checkpoints. It
is not exposed in this session: use available work budget without inventing quota.
Reported percentages are sustained-run targets, including 100%; continue across
checkpoints/compaction and reserve only the capacity needed to deliver safely.

## Current focus — Great Warren expeditions (AX6, 2026-10-25)

The user has finished Apex balance review and resumed substantial autonomous
continuations. Keep the accepted combat numbers. The first Great Warren now
ships; see [GREAT_WARREN_SPECS.md](docs/GREAT_WARREN_SPECS.md) for actual rules.

- A new namespaced noise dimension has Crownroot Groves, Glassbloom Flats and
  Thunder Hollows, giant roots/guava growth, crystals and basalt landmarks.
- Crafted Warren Anchors attune with one Resonant Eye and a shown, retained Apex
  Crest. Saved paired returns support independent players, empty-handed return
  and source-block loss. New refuges use protected Rootstone and a real ladder.
  Travel uses bounded endpoint neighborhoods and standing collision checks.
- All ten natural Apex forms use normal wild despawning and no boss bars;
  their frozen genomes/stats save. Ritual encounters retain their old profile.
  Wards suppress only new natural monsters in 24 blocks, including Endermen;
  they do not erase enemies or suppress scripts/brood. Wild flyers cruise at idle.
- Native rich ruins carry additive diamonds/netherite/enchanted apples and exact
  quality-20–33 genetic archives. Chunk ownership/saves prevent refills/rerolls.
- Role reagents and aerial Membranes craft a charged Prism Lance, native-gliding
  Apex Wing Harness, shared Prism Cell ammunition, Wards and Rootstone. Gear
  uses real fuel/durability/cooldowns and vanilla repair/durability enchantments.
- Protocol **28**; **2,290 authored / 2,418 packaged** language keys. English and
  shared neutral Spanish ship together. Existing source records, genomes,
  inventories, heart debt, gray station menus and MCA conversations remain.
- Native tests mount the shipped dimension definition because GameTestServer
  deliberately omits datapack dimensions. Production uses the normal loader.
  Current evidence: Java 25 build/model/localization gates and 163 native cases,
  including two portal pairs, real terrain/ward events, wild saves/rewards,
  detached ProtoChunk archives/all three landmark generators, native ruin placement
  and loot, paid lance and last-cell flight boost.
- Art sources: `art/gen_realm.py`, `art/gen_realm_worldgen.py`; actual native
  geometry/terrain snapshots produce `art/realm_world_preview.png`. Original
  gear/block art is inspected in `art/realm_items_preview.png`. These are headless
  studies; client appearance, first-trip latency and broad multiplayer population
  performance still need gameplay evidence.

AX7 now ships: the Crown Warren, Crowned Apex and initial Sovereign rewards
remain planned in `docs/APEX_EXPANSION_PLAN.md`. More regions/gear branches remain
optional AX6 expansion. Resolve observed realm issues before the next large stage.
The intelligence/civilization plan stays queued behind this accepted expansion.

## Direct JAR downloads

The user confirmed Chrome accepts the unchanged direct release JAR with download
protection enabled and explicitly requested JAR delivery. Successful main-push CI
now publishes its checked bytes as a public development prerelease, with checksum
and a direct **Download JAR** link in its summary. Actions' artifact button itself
always wraps downloads in ZIPs; use Releases to download/share the JAR.
The earlier diagnostic draft remains historical. No further publication approval
is needed for this delivery. [DOWNLOAD_DIAGNOSTICS.md](docs/DOWNLOAD_DIAGNOSTICS.md)
keeps the actual observations; Google's exact ZIP/URL verdict is still unknown.

## Previous focus — client interaction review (2026-10-24)

The user rejected the persistent crosshair instructions, animal-panel information
dump and measurements that made information tools redundant. This human review
overrides earlier claims of completed discovery; prioritize usability corrections
over new intelligence features. [COHESION_PLAN.md](docs/COHESION_PLAN.md) records
the revised design and the old MCA 1.12 interaction reference.

Latest review response (2026-10-24):

- Storage/station menus now have Minecraft's normal opaque gray container frame,
  using the vanilla crafting texture's empty border and real slot art. Settings,
  the Board, gene-signal and Apex preparation cards, Calipers purpose/readings
  and the lineage-name form use the same treatment. Text uses dark readable
  colors; native item tooltips and actual slot operations remain intact.
  MCA animal conversations retain their borderless view; book pages stay pages.
- English/Spanish panel previews are regenerated from existing native snapshots,
  including the 320×240 viewport. `art/container_panels_preview.png` shows the
  requested Trough, its nested work choices and Apex preparation. This is layout
  evidence with substitute fonts, not a new client playtest.
- `docs/APEX_EXPANSION_PLAN.md` is the new future-content direction. It plans
  Brute/beam/swarm/blast/support Apexes at the revised damage baseline, all flying variants, the Great
  Warren realm, lavish loot and the Crowned Apex dungeon finale. It covers native
  power-10 repeated blasts with family immunity, bounded hook/stun/true drain and
  healing, wild-vs-ritual persistence, source/save compatibility and phased AX1–AX7
  implementation. **No new Apex, item, dimension or combat behavior ships now.**
  Further player testing updates the plan to +60% outgoing damage, with health
  unchanged; the shipped combat code still uses its existing values.
  Later Apex implementation starts at AX1 when requested. Existing cognition
  plans remain valid but do not override this explicit steering.
- Runtime changes are client presentation only. Protocol **23**, authored and
  packaged language counts, inventories, saved data and the 132 native scenarios
  are unchanged. Java 25 `build` passes; verify exact pushed CI/artifact as usual.

Previous native-inventory continuation:

- Seven storage stations now expose real Minecraft slots and the player inventory:
  Trough, Pen, Cradle, Juicer, Table, Jar and populated Trophy Pedestal. Players
  choose the actual named sample, seed or tiered ingredient with ordinary mouse
  gestures or shift-click. Automatic first-match Add/Withdraw button banks and
  their mutation branches are removed; old button IDs are explicitly rejected.
- Native slots wrap existing saved stacks/resource journals. Input validation,
  output-only Juicer stock, sixteen-item food buffers and one-item sample/seed
  limits remain. Player retrieval keeps full components and recoverable legacy
  over-capacity stock; hopper port directions are unchanged. No save schema or
  secondary stock store is introduced. Closing returns actual cursor items;
  native callbacks can replace the menu without losing/duplicating that cursor.
  Block/neighbor notifications flush after the gesture owns its cursor/remainders,
  inside the same reentry guard; direct hand/hopper paths retain their behavior.
- Storage uses a 300×224 layout with stable Back/Close positions, short sidebar
  actions, input labels and visual growth/processing bars. It fits Minecraft's
  320×240 minimum GUI viewport. Work/goal/family choices hide and reject all
  inventory slots until Back. Animal controls and the menu-free Nest stay as
  reviewed. Trough filter icons remain non-retrievable templates.
- English/Spanish labels and optional-guide pages describe the slot controls.
  Trough work toggles now describe all resident work rather than only crops;
  bone meal costs and pair-feed costs remain local to their actions/hover.
  Native cases exercise exact sample choice/swapping, initial slot data,
  protected/hidden/out-of-range gestures and occupied-slot merges, callback
  reentry/menu replacement,
  Cradle growth/milestones, saved-stock recovery, Juicer output and tiered mangers.

Earlier interaction corrections retained:

- Successful care, courtship, befriending, grooming, Follow/Wait/Home and combat
  commands dismiss their actual conversation, allowing their world behavior to
  run. Refused actions stay open; work/provision settings and cargo stay available
  for further changes. Bond release has a short deliberate confirmation; Back
  preserves the bond. Real native operations and callback/reentry guards remain
  authoritative, and a callback's replacement menu is never closed accidentally.
- Plain held materials inspect the Trough, Pen, Cradle, Juicer, Table and Board;
  Nests still work directly in the world. Item-owned tools, enchanted preview
  cues, real inputs, off-hand use and vanilla crouch-placement retain priority.
  Adventure may inspect, while server mutation restrictions still apply.
- Selected practice reports actual searching/approaching/working, waiting for its
  guide and shared recovery. Work/Lessons identify physical blockers such as a
  pause, active order, night or missing home without guessing genes. A stopped
  chosen session gives its nearby guide one brief event; personal knowledge stays
  saved. No new automatic hints, lesson-progress HUD or gene/profession features.
- Crouch-using actual Calipers now opens purposes (health, home/food, work, field
  traits, combat, genes), then only the requested bounded readings. It no longer
  dumps the full care report into chat. Normal Calipers use retains its genetics
  reader. Losing the tool dismisses measured views and rejects their old clicks.
  Work readings distinguish actual personal learning from physical stat targets;
  no availability claim is inferred from points alone.
- Structural client refreshes clear detached focus paths and retain focused
  Back/Close buttons. Actual keyboard/GUI-scale feel still needs a client pass.

- The automatic crosshair hint layer is removed, including its obsolete strings.
  No replacement tutorial popup or one-time cue has been identified as necessary.
- The complete initial snapshot now travels in the menu-opening payload and is
  decoded by the registered client factory before screen construction, eliminating
  the old station-layout fallback while waiting for a view.
- Bunnidog interaction opens short Care / Orders / Work groups directly over the
  visible world, with no enclosing panel, blur or initial scrolling readout.
  Back and Close occupy fixed separate footer positions outside the action list.
  Ordinary held items work; other food, off-hand tools, shears, leads, name tags, bundles
  and purpose-built mod items retain their direct interactions.
- Calm animals stay and face the nearby player while their actual menu is open.
  Closing it releases attention. Anger, fear and safety AI retain priority;
  conversation neither disables AI nor saves an artificial order.
- Nests have no menu: use them in the world to assign a led adult or inspect
  occupancy. Guavas open deliberate food choices, spending nothing on opening.
  A valid choice uses the offered hand's real stack and preserves breeding tier;
  replacing that hand rejects stale clicks. Successful choices dismiss the menu.
- Cargo appears for actual carried bundles. Calipers add a deliberate Measure
  action; losing them rejects stale measurement clicks. Ordinary menus expose
  no inherited copies/points, exact health, combat calculations or thresholds.
- Unavailable actions explain their own immediate constraint on hover. Work and
  lessons show personal practice and live activity, without a theory/setup wall.
  Exact body/mind forecasts remain with the existing tools and stations.
- Station interaction now uses compact pages over the world, without the large
  gold/brown tutorial panel or full-screen blur. The screenshot review adds the
  usual gray container frame for stations. Troughs, Pens, Cradles, Juicers,
  Tables, Jars and Pedestals now use real stock slots instead of stock
  paragraphs or automatic transfer buttons. Working stations show state, without
  repeated setup instructions.
  Trough work modes and Board requests have deliberate separate choice pages.
- Actions depend on real contents: ripe Cradles omit growth, and readers appear
  for readable stock. Empty inventory slots remain available for deliberate
  placement. Empty Trophy Pedestals have no menu. Cargo still uses copied bundle
  icons with native hover contents; these displays are never payment sources.
- All care pages share fixed Back/Close positions. Back follows actual page
  history, including Work → Cargo, seed → offspring and Combat → Party. Page
  changes reset scrolling and invalidate old action stamps. Ordinary live state
  updates retain widgets, focus and current scroll when their structure permits.
- Combat omits duplicated everyday orders and irrelevant nonmember controls.
  Party actions name the animal. Lessons no longer repeat each routine in a
  second status list; a successful start dismisses conversation so real work can
  begin. Practice feedback and busy inventory tooltips are shorter. Exact seed
  calculations remain in requested tools/readers and advanced debug tooltips.
- Protocol **23** adds station slot kind and visual progress to the existing
  first-view/update payload; native container packets synchronize actual slots.
  Client/server builds must match; saved data is unchanged.
  English and neutral Peruvian Spanish have **2,210 authored keys** and
  **2,338 packaged entries per locale** after guide previews.

The headless suite has **132 scenarios**, including the registered opening factory,
normal-AI conversation, actual hand-bound guavas, copied stock/component roundtrips,
page-transition stale denial, current-content controls and real Back navigation.
The family fixture now declares its actual 17×43-block horizontal work area;
writing beyond an 8-block template did not keep all its births entity-ticking.
The older trail fixture retains its 36-block bounds. These are fixture corrections,
not production chunk-loading changes. Added native cases cover held station materials,
normal-AI Wait/Follow after menu dismissal, chosen-practice feedback and real
Calipers dispatch, group bounds, displayed values and lost-tool denial.
Java 25 `build` and all 132 native scenarios pass. Verify the exact pushed GitHub
build/artifact as the final delivery gate.

`art/interaction_preview.png`, `art/companion_activity_preview.png`,
`art/care_readings_preview.png`, `art/station_interactions_preview.png` and
`art/station_inventory_preview.png` and `art/container_panels_preview.png` render
English/Spanish native snapshots with substitute fonts/backgrounds. They are not
in-client captures. Actual GUI scales, accessibility, natural discovery and play
pacing remain human review work. Continue auditing friction found by play before
new intelligence features; do not replace removed manuals with another UI guide.
The next selection audit should cover the Table's first inventory seed preview
and the Comparator's first inventory sample copy; native storage placement is
now exact, but these separate non-consuming readers still use that older rule.

## Previous fix — exploration crash (2026-10-21)

The user's first recent client exploration exposed a real chunk-decoration crash
in `WarrenSigns.place`: native `SignBlockEntity.setText` sends a live block update,
but signs created in a `WorldGenRegion`/`ProtoChunk` have no attached `Level`.
The report identifies Overworld seed `3314416676720181443`, chunk `(-1, -24)` and
the `bunnidogs:warren_underground` feature. This was fixed before new features.

Generated signs now initialize both faces through the native saved-text codec;
only placement directly in a live `ServerLevel` sends an explicit client update.
The shared helper covers cave features and saved structure pieces, preserving
translated labels, dye/glow, chunk ownership and existing player-edited signs.
The new `worldgen_signs` regression uses an actual `WorldGenRegion` with a detached
`ProtoChunk` at the reported chunk coordinate, rather than the loaded-world-only
sign fixture. It reproduces the report's exact null-Level exception before the
fix and checks all room labels plus native block-entity save/load afterwards.
This is a generation-stage regression, not a replay of the seed's full terrain
generation or a client/GPU playtest. Java 25 `build runGameTestServer` passes,
including all **123 native scenarios**. Verify the exact pushed GitHub build
artifact as the final delivery gate.

The user clarified that the previous week's continuations were requested while
away and had not been run in a client. Treat earlier unspecific approvals as
feedback, not exploration/Survival verification. Resume intelligence work only
after handling the reported stability issue; its next implementation step stays
I2 as recorded below.

## Implemented focus — cognitive genetics and first learning (2026-10-21)

The user explicitly authorized implementation after the intelligence planning
batch. **I1 and initial I2 are implemented**, with some garden/nursery I3
integration. [COGNITION_SPECS.md](docs/COGNITION_SPECS.md) is the actual-behavior
reference; the three intelligence design documents remain the longer direction.

- Four diploid cognitive loci, Memory/Reasoning/Handling/Communication, combine
  overlapping contributions from both copies into capacity. Seven body genes
  and five bred stats retain their roles. Full `g2.body.mind` serialization,
  equality, inheritance, parent/item/basket/archive paths and three-word sync
  are implemented. Client/server protocol is **19**.
- Old genomes consistently gain the heritable helper mind. Old adults receive
  familiar current chores once; old seeds retain their body-only mutation law.
  New founders vary genetically, and newborns inherit potential without lessons.
- Separate Mind views cover copies, capacity, exact joint breeding odds,
  full eleven-pair Registry purity and explicit donor-copy splicing. Existing
  body-based shows, commissions, Jar/Comparator signals keep seven-pair meaning.
- Empty-hand animal care → **Skills and roles → Lessons** teaches real harvest,
  garden work, nursery tending and local trough transfer. Capacity plus personal
  practice distinguishes independent, supported-only and genuinely limited work.
  The guide must be nearby and see the work; real items, physical thresholds,
  safe routes, permissions, redstone and shared rest still govern actions.
- Lessons survive as personal knowledge through save/travel/splicing. Active
  instruction is transient; recall/danger/pause/reload stops it. Basic care,
  home, cargo and trusted companion recall remain available to limited minds.
- English/neutral Peruvian Spanish now have **2,138 matching authored entries**,
  **2,266 packaged per locale** after guide previews; all Spanish variants share
  the canonical translation. No manual is required for lesson setup/status.

Next: finish I2's observed demonstrations, remembered source/destination routes
and interruption feedback. The current delivery is the existing short-range,
transactional trough transfer, not a walking courier. Then finish I3's remaining
gather/scouting/care/tactics integration before I4 professions/shared jobs,
construction, teachers/culture and I7 real autonomous family continuity. See
[the engineering sequence](docs/INTELLIGENCE_ENGINEERING.md#13-implementation-sequence)
and [player scenarios](docs/INTELLIGENCE_SCENARIOS.md). Keep implemented combat
and the cohesion/optional-guide requirement reliable.

Validation: Java 25 build/pure/layout/localization checks and the native
integration suite are the delivery gates (122 cognition-batch scenarios, now
123 with the generation-stage sign regression above). Natural client/GPU, Survival
pacing, supported-work balance and multiplayer teaching remain unverified.
GitHub Actions must pass and publish the artifact for the exact pushed commit.

## Previous batch — shared Spanish localization (2026-10-21)

The requested Spanish translation is implemented. Minecraft's language setting
selects the mod text automatically: one neutral Peruvian Spanish source supplies
all Spanish variants, including all seven currently shipped Minecraft choices.
The source has **2,035 matching English/Spanish entries**; the build adds 128
short guide previews for **2,163 packaged entries per locale**. That batch preserved canonical commands/filter expressions, DNA and records
on protocol **18**; current cognition uses protocol **19** as described above.

Care/combat/workshop panels, feedback, items, advancements, signs, Calipers,
Pedigree/Registry, samples, real seeds and all breeding/splicing forecasts are
localized. Pure pages translate before measuring; native server feedback stays
translatable per recipient. Long optional guide chapters show a brief preview;
hovering reveals the complete translated chapter. The 141 pages and their links
stay numbered as before. Opening an authored old guide refreshes its pages;
customized title/author copies are retained.

[LOCALIZATION.md](docs/LOCALIZATION.md) records maintenance and checks.
The Spanish sheet/forecast preview uses a substitute font, not a running client.
Java 25 `build runGameTestServer` passes, including the localization/layout
gates and all **116 native scenarios**. The GitHub Build workflow repeats its
gates and publishes the JAR for the exact pushed commit. That implementation batch added localization without changing intelligence.

## Implemented foundation — gene-driven combat companions (2026-10-21)

The planning-only stage is complete; the user explicitly authorized continuing
into implementation. [COMBAT_SPECS.md](docs/COMBAT_SPECS.md) records actual B1/B2
and the first B3 party. [The broader plan](docs/COMBAT_COMPANION_PLAN.md) and
[ten player scenarios](docs/COMBAT_SCENARIOS.md) distinguish future work and
prototype balance. Core play is learned through care, Whistle orders and animal
feedback; no manual or kill grind is required.

- **Useful ordinary fighters:** all four temperaments bond with actual stock.
  Deliberately choose up to three fighters; training/carriers alone stay safe.
  Existing Build, temperament and Vitality supply actual native bite attributes
  and a saved attack interval. Other genetic defenses/movement remain useful.
- **Distinct partnerships:** loyal keeper watch, close stoic defense, directed
  feral strikes and skittish visible warnings/one-bite skirmishes. Real hearing,
  native target/terrain checks, bounded pursuits and keeper loss govern combat.
- **Control under pressure:** normal Whistle at air/blocks/entities always
  recalls heard party members, including held defenders, without an item lock.
  Crouched aimed use directs one chosen partner; nearby crouch opens orders.
  Wait + Assist holds a small area. Recall/danger yields the position and incoming
  attacks cannot cancel regrouping. Native player arrows pass bonded pets.
- **Party and supplies:** three global UUID slots survive capture/unloading;
  quiet-time removal works for packed/away members. Actual following positions,
  uncovered-target preference, shared warnings and saved last sightings help
  the group. Resident guards remain separate; ordinary carriers are not recruited.
- **Bring them home:** bonded adults collapse after ordinary combat defeat,
  including non-party carriers. Real first aid or downed basket rescue conserves
  identity/cargo/bond/injury. Quiet loaded recovery (Bloom shortens it) and half
  health restore fighting; no invisible bleed timer or capture reset. Terrain
  hazards remain real. Calm real meals heal proportionally; automatic meals
  retain shared rest and paid hand care preserves its prior cooldown behavior.
- **Readouts and saves:** care/party views show actual intent, style, bite and
  recovery. Actual Calipers/sample/parent hovers describe inherited potential.
  Seven genes/five stats and old sample/seed formats stay. New care defaults keep
  existing saved followers out of combat. Protocol **18** needs both sides updated.

Java 25 build, maintained Gradle genetics/layout checks, the model gate and all
**116 native scenarios** pass at the integration checkpoint. Four actual-sculpt
combat poses and four native care/party snapshots are inspected. The standalone
legacy `GenomeCheck.java` still fails on unchanged Island-era allele assumptions
before reaching these features; it is not the maintained Gradle gate. Native scenarios
and substitute-font / actual-sculpt previews do not establish client/GPU feel,
natural Survival balance or six-companion co-op behavior. The next accepted work
is B3 combat-focused breeding comparisons/handoffs and B4 a few physical moves
(pounce/braced interception), followed by natural/client encounters and tuning.
Do not replace these unfinished features with another unrelated QoL batch.

## Implemented foundation — cohesive family and workshop play (2026-10-20)

The user wants gameplay to hang together without studying a manual.
`docs/COHESION_PLAN.md` records this foundation. C1–C4, the implemented C5 parts
and C6 join care, a useful ordinary family, breeding targets and workshop
feedback. A manual is optional; unfamiliar-player understanding and natural/client
pacing need play.

- **Care and first family:** real inventory-backed feeding, grooming, training,
  orders, cargo and six native milestones. Basic farmland remains a complete
  first-litter route. Nests accept ordinary planks; string/feather Whistles work
  before developed-line materials. The optional guide retains **141/13/114**.
- **Ordinary usefulness:** Useful roles & work exposes packs, temperament,
  native harvest readiness, inherited benefits and specialty thresholds. Basic
  harvesting/carrying need no bred points; the native partner walks at zero.
  Work pause/resume moves into this view; direct hoe use remains available.
- **Consistent workshops:** empty-hand inspection at Table, Stand, Jar, detector,
  display, Crucible and care stations. Explicit controls never auto-spend stock,
  change a circuit or claim a trophy just by opening. Direct tools/shortcuts stay;
  actual-hand transfers and mutations share native permission/range checks.
- **Purposeful breeding:** Tables save a commission goal and add a sixth reader
  spread with combined gene/point/litter odds, actual requirements and next steps.
  Actual commission checks and hatching maths supply the forecast. Mutation stays
  separate; Fortune says crop-only. Samples/cues remain reusable. Legacy Tables
  keep five spreads; unknown future saved goals are retained, not reinterpreted.
- **Plan to real family:** compare an actual inventory seed at its saved quality,
  follow sampled-parent or actual-seed offspring, then measure real pups against
  the Table goal with Calipers. Private comparisons change no shared selection.
  Seed readers retain overview/parent indices and add actual forecast/copy spreads.
  Growth time and native golden-dandelion pauses are visible; Bloom respects pause.
  Table/Board/Pen recipes accept feathers or Whiskers before a developed line.
- **Apex preparation:** chosen sample, bounded combat forecast and missing
  offerings precede Review summon → Summon Apex. Material changes disarm it.
  Reentrant/cancelled native spawns and replaced inputs conserve actual stock;
  a root transaction pays one legend each, preserving excess legacy counts.
- **World cues:** requested Cradle/Board/detector working-pad and Apex ring
  markers show the actual positions, without loading remote chunks or performing
  the job. Cradles mark their first obstruction. Client visibility still needs play.
- **Evidence:** Java 25 build/model/all **107** native cases at the final
  integration checkpoint; focused target-maths, signed rounding, save/payload and
  page checks. Seventeen native view snapshots and exported goal/seed spreads feed
  substitute-font previews. Client/GPU, keyboard comfort and natural-world
  first-session balance remain unverified. Protocol **17** requires matching
  client/server; existing animals, inventory components and allele order stay.

The native purpose loop now reaches an actual raised offspring's Board reward
(the long growth wait is fast-forwarded). Natural Survival/client review should
still address observed pacing, supply, housing or UI gaps. Combat should extend
this existing care/breeding journey; another guide chapter cannot establish
that players understand it.

### Prior H12/H13/V5 checkpoint

- **H12/H13, trained trail companions and provisions:** a reusable Whistle
  trains loyal/stoic adults with one real plain guava. Owner-only Follow/Wait,
  heard calls and resident return retain the Nest and use safe loaded native
  walking; transport/panic retain priority. Bowl opt-in lets injured calm
  partners eat suitable ordinary food from their actual native bundles with
  shared meal rest. Payment/recovery precede healing callbacks; components,
  replaced cargo and baskets conserve state. Trained Phase partners can defend
  beyond the old home boundary while travelling. See `docs/TRAIL_SPECS.md`.
- **V5, practical Breeder commissions:** a crafted saved-selection Board
  checks a real Breeder and one calm parented adult on its clear front pad.
  Nine targets cover all eight wild lineages and a combined stable line.
  Actual paper pays once per keeper through native reward loot/XP; animals,
  gear and birth data stay. Native comparator readiness, read-only inspection,
  callback/reentry/removed-board conservation and every live target pass.
  See `docs/COMMISSION_SPECS.md`.
- **Combined checkpoint:** Java 25 build/all **100** native cases pass. The
  model gate also passes after Phase integration; no creature mesh/pass changed.
  Whistle/Board and guide previews are inspected separately. The guide has
  **141 pages, thirteen indices and 114 chapter links**, with all earlier page
  IDs retained. Protocol **15** requires updated client/server. Natural journeys,
  multiplayer feel, client fonts/GPU rendering and reward balance remain review.

### Previous checkpoints

- **I1/I2/A11, isolated Island lineage and ocean refuge:** appended seal,
  paddle and shore-forager alleles supply native oxygen/propulsion with real
  land tradeoffs, coastal food and full material/infusion/readout integration.
  A rooted ocean island supplies three one-time founders, sheltered Nests,
  guavas, a tide garden, supplies, native maps and discovery. Saved chunk/spawn
  masks preserve player changes; oceans do not become global founder biomes.
  **D13/L36** adds sustainable dry-bank kelp meals/work and paid Garden-mode
  restoration with actual stock/protection. See `docs/ISLAND_SPECS.md`.
- **A12/G7, useful cave vision and genetic discoveries:** calm trained pale
  adults trade one glow berry for 60 seconds of keeper Night Vision in dim
  light, with saved 30-second recovery and exact cancellation refunds. Saved
  Pedigree observations award Nature Finds a Way and Wild Gene Library,
  including hidden genes and the new eighth wild origin; native XP pays once.
  See `docs/CARE_SPECS.md` A12 and `docs/DISCOVERY_SPECS.md`.
- **Combined checkpoint:** Java 25 build/model gate and all **95** native
  cases pass. This includes a real default-Overworld noise/placement site,
  clipped island generation/replay, native housing and paddle stair navigation,
  air/drowning/propulsion, kelp costs/protection, actual sight effects and saved
  book rewards. Actual-layout/trait/material/guide previews are inspected;
  natural frequency, map journeys, GPU rendering and client controls remain
  gameplay review. The guide has **124 pages, eleven indices and 96 chapter
  links**. Update both client and server: protocol **14**.

- **V3/V4, planned regional kennels and Show Ring arena:** three intact kennel
  styles provide fitted two-parent yards, floor Nests and empty care workshops.
  A larger public arena supplies holding bays, native judging and trophy space.
  Shared placement keeps the new venues apart and excludes nearby warren
  candidates. Native maps, discoveries and one-time supplies connect exploration;
  animals, stocked stations and ribbons remain player-earned. Build/all **88**
  native cases pass at the implementation checkpoint, including all sixteen
  template/rotation combinations and actual paid pairing/gold judging. Four cell
  layout previews and the **116-page, ten-index, 88-link** guide are inspected.
  Protocol **13** requires updated client/server. See `docs/VENUE_SPECS.md`.
  Final combined build/all **88** native cases also pass, with actual planned
  floor heights corrected for venue/new barn anchors and earned-pedestal replay
  preservation checked. Saved existing starts keep their recorded positions.

- **H11, planned Build carrying capacity:** ordinary adults equip one to four
  native bundles according to expressed Build. Whole contents/names/colors,
  last-first retrieval, excess retention and native equipment death/basket saves
  conserve actual cargo. Excess bundles impose a named 25%-each movement penalty;
  visible cargo breaks camouflage. Separate fitted panniers add no geometry/pass
  to unadorned animals and preserve the approved sculpt. Build/model/all **87**
  native cases pass at the cargo checkpoint; actual-sculpt preview inspected.
  Fresh guide: **111 pages, nine indices, 82 links**. Protocol **12** requires
  updated client and server. See `docs/CARRYING_SPECS.md`.

- **A10/V1/V2/R1, fieldcraft:** finishes coat camouflage, Breeder villagers,
  abandoned breeding barns and the live Pedigree Comparator from the original
  plans. Healthy calm coats blend on matching ground with reduced native sight
  and STEP vibrations. Native careers buy regional pelts and supply workshops;
  dry-site versioned barns have real preserved one-time loot and empty stations.
  Sample-programmed redstone reads all seven live gene pairs, including hidden
  copies. Build/all **85** native scenarios pass at the implementation checkpoint.
  The guide now has **109 pages, nine indices and 82 chapter links**. Update both
  sides: protocol **11**. See `docs/BREEDER_SPECS.md`, `CARE_SPECS.md` A10.

- **G6/S11, planned Breeding Pen and seed quality colors:** a two-manger Pen
  spends one actual guava on each isolated, healthy housed parent, then leaves
  mating/seeds/rest to the native BreedGoal. Directional hoppers, saved stock,
  one-shot redstone, player/Calipers controls and callback-safe costs are in
  place. Eight seed palettes read existing stored quality directly, including
  old seeds; no save migration or hatching math changes. The guide has 100 pages,
  eight indices and 73 chapter links. Pen/front-label, seed and guide previews
  inspected. Update both sides: protocol **10**. See `docs/BREEDING_PEN_SPECS.md`.

- **J1/J2/G5/D12/T5, breeder workshop:** the planned Juicer presses all eight
  guava tiers and performs paid random seed infusion, with directional hoppers,
  saved five-second work, output waiting and redstone pause. Rich carriers lift
  effect quality while preserving seed inheritance/drift. Books/Registries
  name exact recorded genotypes through acknowledged edits, with preserved
  snapshots and searchable labels. Twelve pelt rugs and sample Jars complete
  the collection; jars preserve samples and expose fixed-pair signals. The guide
  reaches 98 pages/eight indices/72 chapter links. Workshop/guide previews are
  inspected. Update both sides: protocol **9**. See `docs/WORKSHOP_SPECS.md`.

- **A9, planned fertility visibility:** existing fertility alleles now show
  distinct tail-tip phenotypes: bare, one rosette, closed green bud, three
  rosettes or violet star. Hidden copies stay hidden; pups inherit the look.
  Breeding/readiness/Bloom values stay intact. Combined build/model/all 73
  native cases pass; all mark/foot variants reset correctly and exact approved
  base coordinates/normals remain. Actual geometry and 92-page/seven-index
  guide previews inspected. Update client and server together: protocol **8**
  also prevents older clients misreading the new Phase/webbed allele ids.

- **A8, planned swamp swimming/webbed paws:** appended LEGS `webbed` gives
  fresh swamp lines native +0.5 Swim Speed, expressed toe membranes and water
  paddling, with land/jump tradeoffs and retained drowning. Native movement,
  hidden copies and external-attribute save/gene changes pass with all 73
  cases and the model gate. All 2,172 base faces retain exact coordinates/
  normals; only the expressed variant adds 72 faces. Geometry and 91-page/
  seven-index guide previews inspected. See `docs/CARE_SPECS.md` A8.

- **U1 completion, entrance trails:** new version-seven small surface warrens
  fit complete worn trails from each actual mouth round its mound to the tree
  patch. Unsafe sides fall back or stay untouched; old starts keep their plans.
  Native new/legacy piece generation, mound/tree/Nest preservation and blocked
  container/fluid/ore cases pass with all 72 scenarios. The actual-layout
  preview is inspected. See `docs/WARREN_PLAN.md` 25.

- **A7, planned End Phase ability:** appended LEGS `phase` gives fresh End
  founders and bred descendants a defensive blink after real nonlethal attacks.
  Damage remains; safe loaded landings, home/transport limits and native
  cancellation apply. Twenty-second recovery survives native saves/baskets.
  Stat tradeoffs, juice and readouts are connected. Java 25 build/all 71 native
  scenarios pass; the 90-page/seven-index guide preview is inspected. See
  `docs/CARE_SPECS.md` A7. Client combat remains unverified.

- **A6, snow surface home routes:** animals with actual native powder-snow
  footing can use its supported dry surface for domestic routes. Deep powder,
  changed/lost footing or floor, magma and neighboring fire still stop jobs.
  Woolly/hidden snow alone give no surface exception. Native costs/collision,
  saves and equipment remain unchanged. Java 25 build/all 70 native cases
  pass, including a real powder-only corridor path and meal. The 88-page
  guide/six-index/fifty-five-link preview is inspected. See `docs/CARE_SPECS.md` A6.

- **D11, plain-guava hand care:** sneak-feed plain guavas for ordinary two-HP
  healing/pup growth without starting breeding. Healthy adults and upgraded
  guavas keep food; existing love quality/time, rests and work preference stay.
  Normal feeding still breeds. Manual food pays from the actual hand before
  healing callbacks, preserving callback-refilled stock. Java 25 build/all 69
  native cases pass; 87-page/six-index/fifty-five-link guide preview inspected.
  See `docs/FORAGING_SPECS.md` D11.

- **L35/N10, keeper controls:** sneak-hoe an individual to pause/resume chores
  while retaining meals, home and care; the optional preference survives saves
  and Travel Baskets. Sneak-hoe a Cradle for manual-only hatching. Incubation,
  tending, stock and manual hatching continue; queued redstone respects the
  switch, and re-enabling under power creates no new edge. Defaults preserve
  old behavior. Java 25 build/all 68 native cases pass; 86-page guide/six
  indices/fifty-four chapter links preview inspected. See `docs/RESIDENT_WORK.md`
  L35 and `docs/NURSERY_SPECS.md` N10.

- **L34, resident supply:** appended Supply mode lets healthy Senses +20 adults
  move one ordinary surplus food to a nearby meal dish below eight food, keeping
  eight at source. Both exact stacks share one native transaction, safe paths,
  power/filter/protection rechecks and GardenWait. Callback removal of both
  dishes conserves their committed stock. Compost now also rechecks the entire
  job after reserving food and refunds interrupted mode/Bloom changes without
  work rest. Java 25 build/all 66 native cases pass. The 84-page guide has six
  indices/fifty-two chapter links; Supply/Specialist/Working previews inspected.
  See `docs/RESIDENT_WORK.md` L34 and `docs/SETTLEMENT_LIFE.md` L14.


- **D10/L33/H10, End care and basket stations:** Bloom +20 chorus-compatible
  adults revive only spent flowers, preserving branches and yielding one fruit
  to a work trough or direct meal. Native dispensers pack/release one existing
  animal, preserving paused care and exact identity. Full-body dry placement,
  reentrant UUID guards and whole-slot reservation conserve baskets through
  cancelled spawns, removed sources and callback refills. Java 25 build/all 64
  native cases pass; the 82-page guide preview is inspected. See
  `docs/FORAGING_SPECS.md` D10 and `docs/HOME_SPECS.md` H10.


- **D9/L32, safe hunts and work roll call:** hunters/fishers choose bounded
  dry approach/shore routes, interrupt on changed homes/terrain/protection
  and leave named, led or transported prey alone. Only an eligible chosen
  prey kill pays meal healing. Shared routes now recheck current native
  hazard types, including a changed floor under cached nodes. Sneak-Calipers
  on a trough reports selected-mode eligibility, food/filter, traits and
  pause/stock limits for loaded residents without paths, tags or mutations.
  Java 25 build/all 61 native cases pass; 80-page guide preview inspected.
  See `docs/FORAGING_SPECS.md` D9 and `docs/RESIDENT_WORK.md` L32.

- **L31, meadow melon plots:** fresh version-six meadow farms mix two young
  melon stems with crops, one native water source and two reserved dirt fruit
  gaps. Optional corner troughs and the original 3×3 reservation remain.
  Version-five/older plans keep their prior crops; other regional layouts
  remain unchanged. Java 25 build/all 59 native cases and focused rotated
  layout checks pass, including real native fruit growth, protected gap
  refusal and exact generation-version saves. Farm/78-page guide previews
  inspected. See `docs/SETTLEMENT_LIFE.md` L31 and `docs/WARREN_PLAN.md` 24.

- **L28–L30/N9, selected resident jobs:** sneak-hoe selects/enables Mixed,
  Garden, Gather, Compost or Nursery work, with default/legacy Mixed behavior.
  Garden Bloom +10 adults replant valid gaps from real self-planting food;
  Nursery Bloom +20 adults spend a plain guava for one exact Cradle stage.
  Both keep eight food, share GardenWait, preserve components and refund
  callback-interrupted costs. Nurses preserve the whole litter and never
  hatch it. Calipers names the nursery bonus. The guide has 77 pages/five
  indices/forty-six chapter links. Java 25 build/all 59 native cases pass;
  guide preview inspected. See `docs/RESIDENT_WORK.md` and N9 in
  `docs/NURSERY_SPECS.md`.

- **O6/O7/D8/H9/L27, orchard and habitat routines:** shears dispensers pick
  ripe guavas with one wear while retaining native animal/beehive shearing.
  Plain guavas plant canopy gaps; invalid planting keeps ordinary food
  ejection, with higher tiers untouched. Injured matching residents eat spare
  cactus tops/same-color mushrooms, ripe wart and linked melons, preserving
  growing stock through shared work/meal rules and protection rechecks.
  Nests expose claimed/free comparator signals through normal POI tickets;
  rehoming, travel and death notify without loading remote terrain. Meadow
  stores add melon food and an independent 35% chance of one/two seeds.
  The guide reaches 74 pages/five indices/forty-three chapter links. Java 25
  build/all 56 native cases pass; guide preview inspected. See
  `docs/ORCHARD_SPECS.md` O6–O7, `docs/FORAGING_SPECS.md` D8,
  `docs/HOME_SPECS.md` H9 and `docs/SETTLEMENT_LIFE.md` L27.

- **L25/L26, renewable melon beds:** foragers/omnivores harvest one slice
  only beside a living attached melon stem, leaving decorative melons intact.
  Native neighbor updates restore the mature stem. Bloom +10 advances young
  stems by one age without food or forced fruit. Diet, exact filters, native
  protection rechecks and shared work rest apply. Matching hand-fed slices
  heal adults/grow pups without breeding. The guide reaches 70 pages with
  forty chapter links. Java 25 build/all 53 native cases pass; new chapter/
  Working-index preview inspected. See `docs/SETTLEMENT_LIFE.md` L25–L26.

- **D7/L24/P6, loose-food use:** injured adults take one old ordinary loose
  food as a normal daytime meal. Healthy Senses +10 adults collect it beside
  an enabled trough with shared garden rest. Ten-second grace, native pickup
  delays/reservations, Diet, exact components, power and safe loaded paths
  apply; upgraded guavas stay. Native journals conserve source/destination
  through abort and reentrant removal. The guide adds a fifth Working index.
  Java 25 build/all 52 native cases pass; text/navigation preview inspected.
  See `docs/FORAGING_SPECS.md` D7, `docs/SETTLEMENT_LIFE.md` L24
  and `docs/FIELD_GUIDE.md` P6.

- **C10, cold-climate care:** healthy expressed snow coats at Bloom +10
  yield one snowball when brushed in a climate cold enough for native snow.
  Live climate, hand/dispenser care and Calipers share the same yield rules;
  whiskers/Starlit products combine under one wear and saved grooming rest.
  Native shearing remains independent. Java 25 build/all 50 native cases pass;
  guide preview inspected. See `docs/CARE_SPECS.md` C10.

- **O4/O5/L23/N8, habitat completion:** native dispenser sapling stations
  retain blocked stock; saplings follow 26.2 vegetation soil, recover after
  support loss and grow only supported, loaded, bounded trees. Litter crops
  restore grass/podzol/mycelium fallback without changing planting or saved
  litters. Fresh five-tree cave/Nether features can fit regional farms using
  their actual terrain mode. Java 25 build/all 49 native cases pass; guide
  preview inspected. See `docs/ORCHARD_SPECS.md` O4–O5,
  `docs/SETTLEMENT_LIFE.md` L23 and `docs/NURSERY_SPECS.md` N8.

- **D6, fruit meals and safer grazing:** injured adults eat ripe hanging
  guavas, plus diet-matching sweet/glow berries, near home by day. Plants
  remain, meals heal two HP and share normal food rest. All field grazing
  now uses complete safe loaded routes and rechecks after protection callbacks;
  a callback-created chest is preserved without healing. Legacy crop/hay
  meals remain. Java 25 build/all 48 native cases pass; guide preview inspected.
  See `docs/FORAGING_SPECS.md` D6.

- **L22, sustainable mushroom beds:** healthy mushroom eaters gather one
  spare red/brown mushroom into enabled troughs only while another living
  mushroom of that color remains in the loaded home patch. Native spread,
  protected safe work, exact filters, power and shared rest apply. A companion
  lost inside protection callbacks prevents the harvest without cost. Java 25
  build/all 47 native cases pass; guide preview inspected. See
  `docs/SETTLEMENT_LIFE.md` L22.

- **O1–O3, guava orchards:** a plain guava establishes green fruit beneath
  any leaves; native bone meal advances one age by hand/dispenser. Silk Touch
  now recovers guava leaves alongside shears. Actual-hand survival/creative,
  Adventure/protection rollback, exact loot and scheduled support loss pass.
  Java 25 build/all 46 native cases pass; orchard/region/Habitat guide preview
  inspected. See `docs/ORCHARD_SPECS.md`.

- **L21, regional farms:** new version-five starts fit desert cactus beds,
  soul-sand wart, swamp mushrooms, cave glow-berry trellises and End chorus
  within the old 3×3 plot. Optional empty enabled troughs, safe terrain fitting
  and existing containers are preserved; older starts retain their exact plans.
  Java 25 build/all 45 native cases pass; actual-layout and guide previews
  inspected. See `docs/SETTLEMENT_LIFE.md` L21 and `docs/WARREN_PLAN.md` 22.

- **L20/P5, cactus gardens and habitat index:** desert browsers trim one
  spare cactus top into an enabled trough, preserving bases/flowers and
  rechecking native protection callbacks. Safe loaded routes, filters,
  power and saved work recovery apply. The guide gains a fourth Habitat
  index and cactus chapter. Java 25 build/all 44 native cases pass;
  cactus/Main/Habitat preview inspected. See `docs/SETTLEMENT_LIFE.md` L20
  and `docs/FIELD_GUIDE.md` P5.

- **T9/T10, litter plans:** held guavas, optional Mutagen and Fortune tools are
  reusable forecast cues. The server captures their tiers/flag; seed maths
  combines feed/starborn once and previews real harvest drift/mutation without
  spending anything. Plain A|B remains readable; optional plan suffix moves
  the network to **7**, with all saved items/animals/slots unchanged. Java 25
  build/all 43 cases and focused plan/page checks pass; forecast/guide/index
  previews inspected. See `docs/TABLE_SPECS.md` 9–10.

- **L19, recovery rest:** adults below half health seek a safe place near
  their real Nest instead of strolling by day. Meals and other important
  goals preempt; thunder releases them for shelter. No passive healing or
  saved state. Calipers reports readiness. Java 25 build/all 43 native cases
  pass; recovery/index preview inspected. See `docs/SETTLEMENT_LIFE.md` L19.

- **T7/T8, connected lab bench:** hoppers fill A then B and drain A first,
  retaining whole sample components with native transaction rollback. Actual
  comparators read 0/8/15. Manual placement now uses the real hand, including
  off-hand survival/creative. Save keys, forecasts and B-first manual take
  remain. Java 25 build/all 42 cases pass; lab guide preview inspected. See
  `docs/TABLE_SPECS.md` 8.

- **H8/L18, resident wayfinding and roll call:** normal compass use on a
  resident marks its recorded Nest without requesting its terrain. Sneak-use
  Calipers on a bell for loaded neighbors, injured first, with tags/health/
  foods/homes. Both preserve tickets and animal state. Java 25 build/all 41
  cases pass; home/roll-call/index preview inspected. See `docs/HOME_SPECS.md`
  H8 and `docs/SETTLEMENT_LIFE.md` L18.

- **E5, resident finder:** rename a spare Ear Tag to an animal code and
  sneak-use Calipers with it opposite. Loaded matches within 24 blocks get
  ten seconds of native outline; five-second native tool rest also follows
  empty searches. Items, biology, homes and care recovery persist. Java 25
  build/all 41 native cases pass; finder/index preview inspected. See
  `docs/EXPLORATION_SPECS.md` E5.

- **S10, breeding-record filters:** native contents inputs in Pedigree Books
  and Registries query tags, expressed/hidden traits, recorded age and bred
  stat comparisons. Original numbers/parents remain intact; arrows follow
  matches, including after a parent-link detour. No item/record changes or
  new packet. Java 25 build/focused query/layout checks pass; filtered/empty
  record and guide previews inspected. Native input/font use remains client
  review. See `docs/SHEET_SPECS.md` S10.

- **P4/E4, navigation and working-line goals:** the 50-page guide has three
  linked contents pages/twenty-four chapter links, including direct nursery,
  guard, map, scout and trough-pause entries. Native one-time XP milestones
  reward a successful guard command, found-ore survey and focused survey.
  Refusals grant none. Build/all 40 native cases pass; three-index preview
  inspected. See `docs/FIELD_GUIDE.md` P4 and `docs/EXPLORATION_SPECS.md` E4.

- **L17, trough redstone pause:** power reserves a trough from resident
  meals and crop/compost work; manual/automation stock, exact filters and
  comparator output remain available. Jobs recheck before commitment and
  after native protection callbacks. Build/all 40 native cases pass,
  including interrupted meals, a powered crop callback and surplus refusal.
  Guide preview inspected. See `docs/SETTLEMENT_LIFE.md` L17.

- **E3, focused surveys:** Senses +20 mineral scouts can select an ore family
  from a reusable mineral in the other hand. Native family tags include
  deepslate; every focus shares normal range/rest and empty searches never
  fall back to unwanted ore. Coordinates persist in chat. Build/all 40 cases
  pass; updated guide preview inspected. See `docs/EXPLORATION_SPECS.md` E3.

- **E2, mineral scouts:** sneak-use a compass on a half-healthy bat-eared
  adult at Senses +10 to mark the nearest ore within six blocks. Through-rock
  survey reads loaded/bordered terrain only, with saved thirty-second rest
  even when empty. Whole compass details and animal/ore state persist;
  Calipers is read-only. Build/all 40 native cases pass; guide preview
  inspected. See `docs/EXPLORATION_SPECS.md` E2.

- **E1, Warren Explorer Maps:** journeyman cartographers may sell a native
  map for fourteen emeralds and a compass. The appended trade pool preserves
  ordinary offers; native map modifiers omit unlocated results. Targets are
  natural Overworld warrens, using the normal village marker. See
  `docs/EXPLORATION_SPECS.md` E1.

- **L16, Nest guards:** sneak-use a bone to opt a feral adult with a Nest
  into bounded defense. Saved preference, selected visible hostiles, complete
  dry routes, eight-block home boundary, native bites and lead/health/gene
  interruptions; Calipers reports status without a search. Old animals start
  off. Build/all 39 server cases pass; guide/index preview inspected. See
  `docs/SETTLEMENT_LIFE.md` L16.

- **A5, snow footing:** expressed snow coats use native powder-snow surface
  collision and escape jumping. Woolly keeps freezing protection without
  footing, hidden snow gains none, and gene edits update immediately. Native
  descending/inside-snow rules and conservative home routes remain. Actual
  build/all 38 server cases pass; guide preview inspected. See
  `docs/CARE_SPECS.md` A5.

- **N7, grand-tree nurseries:** new level-five halls may fit an empty Cradle
  in an old hay station's footprint with a complete hatch pad. Its fit also
  requires the ordinary trough/bell; floor Nests, labels, broad aisles and
  room bounds stay intact. Saved fixture IDs append; old starts and occupied
  blocks/incubation remain preserved. See `docs/NURSERY_SPECS.md` N7.

- **N6, hatch-pad planning:** sneak-Calipers reads Cradle incubation/signal,
  crowding and actual pad obstructions, with temporary native markers. The
  same loaded-world preflight gates real hatching; inspection consumes/rolls
  nothing. Normal Calipers retains the litter forecast. Native structure
  rotation/mirroring updates its facing. See `docs/NURSERY_SPECS.md` N6.

- **N5, in-place Mutagen:** use or dispense a vial at a planted seed/occupied
  Cradle. One success updates only its existing Mutagen flag, preserving whole
  item components, age and remaining incubation. Already treated/invalid
  targets retain the vial; native hand permissions/creative costs apply.
  See `docs/NURSERY_SPECS.md` N5.

- **C9/L15, living materials and orchard recycling:** brushing an expressed
  moss adult at Bloom +10 yields one Moss Carpet alongside any qualified
  whisker/Starlit products, sharing one wear and saved recovery. Native
  compost data adds plain guavas at 65% and leaves/saplings at 30%; higher-tier
  guavas remain excluded. Plain Bloom gardeners can recycle orchard surplus.
  See `docs/CARE_SPECS.md` C9 and `docs/SETTLEMENT_LIFE.md` L15.

- **L14, surplus composting:** Bloom +10 residents work a vanilla composter
  beside a hoe-enabled trough after harvesting/cultivation. One real suitable
  food per job, native chances, eight-item reserve and shared saved garden
  recovery. Existing protection, safe routes, Diet and domestic restrictions
  apply; ordinary maturation/hopper output supply bone meal for the nursery.
  See `docs/SETTLEMENT_LIFE.md` L14.

- **N4, committed crop harvest:** break protection and native permissions finish
  before seed/pup side effects. Cancelled breaks preserve everything; ordinary
  environmental removal returns the saved seed. Crop/command spawn refusal
  rolls back siblings; a removed crop refunds its original seed. Fortune and
  creative ripe hatching remain. See `docs/NURSERY_SPECS.md` N4.

- **N3, Nursery Cradle:** craft wool/glass/Nest/planks into a saved one-seed
  incubator. Loaded minutes, bone meal or Starlit Dust advance growth; hoppers,
  comparators, dispensers and Calipers share native controls. Empty-hand use
  or a fresh redstone edge hatches onto a clear dry 3×3 pad in front. Blocked,
  crowded or externally refused litters retain the seed; partial spawns roll
  back. Sneak-empty withdraws and resets growth. See `docs/NURSERY_SPECS.md` N3.

- **N2, seed provenance:** crops retain named seeds and arbitrary item
  components through native saves, unripe harvest and support loss. Legacy
  crops reconstruct their functional seed. Mutagen changes only its seed data.
  See `docs/NURSERY_SPECS.md` N2.

- **N1, nursery dispensers:** plant a saved litter into empty space above
  farmland or ripen one valid planted crop with Starlit Dust. Hand/machine
  actions share success-only costs and preserve the full functional seed.
  Refusals retain items. Seed Crops now maintain dry farmland. See
  `docs/NURSERY_SPECS.md`.

- **S9, carried records:** sneak-use a Pedigree Book or Purebred Registry with
  a filled basket in the other hand to log its native identity snapshot without
  unpacking. Registry eligibility, first representative and advancement stay
  shared with live registration. Air/block/off-hand/creative use and malformed
  identity refusal are covered. See `docs/SHEET_SPECS.md` S9.

- **L13, nighttime home care:** injured adults may eat real trough stock along
  safe routes wholly within four blocks of their Nest at night, then resume
  normal night waiting. Outdoor foraging/work remain daytime; the existing
  meal recovery is shared. Calipers reports trough-only readiness. Daytime
  troughs also use safe paths/replans. See `docs/SETTLEMENT_LIFE.md` L13.

- **H7, Nest wayfinding:** use a compass to mark a Nest in its dimension.
  Markers persist if the Nest is removed, preserve components and handle stacks,
  creative/off-hand use and inventory overflow. Empty-hand inspection reports
  ticket/resident without changing homes. See `docs/HOME_SPECS.md` H7.

- **L12, bred cultivation:** Bloom +10 adults advance matching unripe crops by
  one stage between ripe harvests. Growth and harvest share existing work rest,
  trough capacity/filters and protected safe routes. Ripe sweet berries now
  yield one food while keeping the bush. Calipers names the Bloom bonus. See
  `docs/SETTLEMENT_LIFE.md` L12.

Established earlier systems include native guide navigation (P3), full-state
Travel Baskets (H6), dispenser care with native armadillo fallback (C8), saved
trough filters (L11), coat-colored fleece (C7), actual bell calls (L10) and
transactional hopper/comparator troughs (L9). Earlier home/food Calipers, refuge,
storm shelter, fishing, terrain traits, local supplies, living dust and fitted
farms are also complete. Their rules/evidence remain in the specifications
listed below; do not redo them.

The native Field Guide has 88 translated pages and fifty-five chapter links
across six linked contents pages.
`art/gen_guide.py` owns text/icon/preview; `art/gen_pantries.py` owns eight
lineage food tables and shares the guide pool. See `docs/FIELD_GUIDE.md`.

## Implemented systems — do not redo

| Area | Current behavior and specification |
|---|---|
| Cohesion | C1/C2 live controls, actual inventory, ordinary meals, six native milestones and a complete starter loop. `docs/COHESION_PLAN.md` |
| Genetics and breeding | Seven genes, inherited pairs, bred stats/Bloom, eight guava tiers, litter seeds/crops, infused juice. `docs/FEATURES.md`, `docs/FIXES_PLAN.md` 6 |
| Sheets and records | Calipers, Pedigree Book, Ear Tags, seed/sample sheets, forecasts, carried-animal records, local record filters and actual saved genetic-discovery rewards. `docs/SHEET_SPECS.md` S1–S10, `docs/DISCOVERY_SPECS.md` G7 |
| Nursery | Whole saved seeds, protected crop harvest/recovery, dirt/grass soil fallback, in-place Mutagen, dispenser stations and a Cradle with incubation, native automation, checked hatching/pad planning and fitted grand-tree nurseries plus exact paid resident tending. `docs/NURSERY_SPECS.md` N1–N10 |
| Laboratory | Two sample slots, native hopper/comparator automation, actual-hand placement, vial display, planned feed/treatment/harvest forecasts, pup/copy odds and both-direction splice plans. `docs/TABLE_SPECS.md` T1–T10 |
| Gene Splicer | One donor expressed copy, chosen gene, sample/Mutagen/tool cost, recipient recovery; breeding fixes pairs. `docs/SPLICER_SPECS.md` |
| Registry and shows | Distinct stable bred lines, linked snapshots, balanced-stat bronze/silver/gold judging, per-animal awards, ribbons and Champion Line. `docs/REGISTRY_SPECS.md`, `docs/SHOW_SPECS.md` |
| Care | Live samples, fleece palettes, whiskers/dust/moss/cold-climate frost, dispenser care, saved recoveries and sneak-Calipers home/food/care/work status. `docs/CARE_SPECS.md` C1–C10 |
| Adaptations | Expressed environmental defenses, native terrain/snow footing, End Phase, swamp/coastal swimming, oxygen retention, fertility marks, camouflage and paid pale keeper sight. `docs/CARE_SPECS.md` A1–A12 |
| Outdoor food | Home-bounded crops/fruit and sustainable habitat/kelp meals, ordinary loose food, bounded dry hunting/shore fishing, chorus care, reservations and callback-safe commits. `docs/FORAGING_SPECS.md` D6–D13 |
| Homes/travel | Claimable Nests, native leads/rides, baskets, bundle carrying, trained Follow/Wait/heard calls and opted-in cargo meals. Existing home, work and care state persist. `docs/HOME_SPECS.md`, `docs/CARRYING_SPECS.md`, `docs/TRAIL_SPECS.md` H12/H13 |
| Settlement life | Trough automation/filters/power, Diet healing/night meals, bell visits, regional farms, refuge/storm/rest/guards and work roll calls. Selected harvest/cultivation/compost/gather/restoration/nursery/supply and dry-bank kelp jobs share exact stock, safe routes and work rest. `docs/SETTLEMENT_LIFE.md`, `docs/RESIDENT_WORK.md` L28–L36 |
| Warrens | Fitted dens/mouths/tunnels/cellars, landmark rooms, grand-tree hall/loop/lower commons, cave/Nether/End entrances and saved labels. `docs/WARREN_PLAN.md` U1–U8 |
| Exploration | Native cartographer Warren Maps, bred bat-ear compass surveys with optional mineral-family focus at Senses +20, native working-line milestones and a loaded tagged-resident finder. `docs/EXPLORATION_SPECS.md` E1–E5 |
| Breeder sites and Island | Native Breeders/barns/kennels/arena, maps and earned judging; nine functional breeding commissions with real native one-time rewards; ocean refuges with one-time coastal founders. `docs/BREEDER_SPECS.md`, `docs/VENUE_SPECS.md`, `docs/COMMISSION_SPECS.md`, `docs/ISLAND_SPECS.md` |
| Wood and travel | Guava building/redstone set, doors/shutters/lamps, four sign forms, native boats/chest boats with custom assets and drops. `docs/FIXES_PLAN.md` 4–5 |
| Orchards | Plain-guava hanging fruit propagation, native bone meal, shears/Silk Touch leaf recovery, dispenser saplings/fruit picking/canopy repair and supported/bounded tree growth. `docs/ORCHARD_SPECS.md` O1–O7 |
| Drops and endgame | Pelts/materials, Mutagen/Marrow/Tissue/Dust, five stat legends and pedestals. Sample-defined Apex ritual, inherited combat, warned attacks/enrage, boss bar and source-line crest. `docs/DROPS_SPECS.md`, `docs/APEX_SPECS.md` |
| Apex preparation | Crucible missing-offering status; main-hand Calipers/off-hand sample forecasts real boss values without spending or spawning. `docs/APEX_SPECS.md` P2 |

## Save and implementation constraints

- Keep gene/allele IDs append-only. Old six-pair data loads with plain/plain
  Diet; current genomes use seven pairs / 56 bits and mask the signed high sync
  word. Current network protocol is **16**; matching client/server versions apply.
- Preserve whole item components in samples, books, table slots, vehicles,
  troughs and trophies. Existing inventories and containers are never restocked.
- Structures save fitted tunnel cells, path exclusions, labels and optional
  `LifeFixtures`; column-owned underground writes preserve later containers,
  fluids and hard blocks. Surface props remain anchor-owned. `WoodDetails` and
  `WarrenVersion` preserve old starts; do not regenerate existing villages.
- Native terrain fitting can omit unsafe rooms, ramps, furniture or networks.
  Floor Nests, exits, water containment and existing player edits must stay safe.
- Named transient genetic modifiers preserve external attributes and reapply
  after native load. Care/food/work rest saves remaining ticks and pauses while
  unloaded. Apex is excluded from domestic tools, breeding, Nests and transport.

## Approved visuals

The sole base model is approved **Low**, 2,172 quads / 8,688 vertices with thirteen
512px coat atlases. Expressed webbed legs add 72 optional membrane faces / 288
vertices, leaving all approved base coordinates/normals exact. The four mutually exclusive fertility marks add 0/40/18/120/38 faces.
The stored mesh has 2,460 faces / 9,840 vertices; an unmarked ordinary animal
retains the original render budget. The largest webbed/swarm combination renders
2,364 faces / 9,456 vertices. Preserve the photographic baby face, human/rabbit ears,
compact robust body, outward paws, short fur and source imperfections. Do not
restore tiers, distance switching or graphics configuration. Sources:
`art/sculpt_bunnidog.py`, `art/gen_bunnidog.py`, `art/reference/`.

H11's fitted bundle panniers are separate optional equipment from
`art/gen_packs.py`: zero through four bundles add 0/78/96/114/132 quads.
Unadorned animals skip the pass; the largest equipped combination is 2,496
faces / 9,984 vertices. Native model checks verify exact shared body/pup/leg
poses and per-animal reset; the actual-sculpt accessory preview is inspected.

The latest head-lighting fix uses authored surface-derivative normals and one
continuous front UV chart; no geometry was added. Closely spaced front-ring
normal change fell from 0.478 to 0.038. Software preview inspected. The user's
prior Low/Ultra screenshots establish earlier rendering; final client lighting
and performance of the fix remain gameplay review. See `docs/BUNNIDOG_MODEL.md`
and `docs/GRAPHICS.md`. A8/A9 add expressed webbing/paddling and tail-tip variants; the approved base
geometry and face remain exact.

## Validation and evidence

Use actual Java 25 compilation. In the current workspace:

```sh
source /workspace/.bunnidog-setup/activate.sh
./gradlew build runGameTestServer --no-daemon
```

`build` includes focused diet/Island, breeding-lab, workshop/wood/boat asset and warren checks.
Add `checkBunnidogModel` for mesh/renderer/animation changes. GitHub Actions runs
`build checkBunnidogModel runGameTestServer` before uploading its commit artifact.
Avoid the large GenomeCheck suite unless substantial pure genetics/layout work
requires it; do not repeat passing checks without a relevant change or concern.

The cohesion Java 25 build and all **103** native scenarios pass (102 mod
cases plus native sanity). Latest coverage:

- **C1/C2:** native live care/six-station menus, inventory-backed meals, tools
  and inputs, readonly Adventure inspection, target removal/reach, packet codec,
  shared-content and request replacement, recovered legacy upgraded food and
  first-family recipe ambiguity. Genuine courtship through paid feeding, actual
  seed pickup, farmland/Cradle incubation and real pup hatching completes the
  native journey. Client usability is a separate review goal.

- **H12/H13/V5:** real held Whistle/guava/bowl/paper interactions, owner-only
  controls, actual walking outside retained housing, heard calls, waiting,
  loaded route hazards/transport, native Phase damage and recovery; saved
  baskets, exact native bundle meals, old/refilled/dropped cargo and heal reentry.
  All nine live breeding targets earn native one-time XP/materials; real
  advancement callbacks cannot duplicate rewards across removed/reentered
  stations. Customized book saves retain all 141 pages. The model gate passes;
  item/station/guide previews are inspected separately from native gameplay.

- **I1/I2/A11/D13/L36:** saved coastal IDs/inheritance, native oxygen/drowning
  and propulsion, clipped/protected/replayed island generation and founder
  housing, actual paddle stair navigation, real default-Overworld noise and
  monument exclusion, sustainable kelp meals, stock/reserve/native protection
  and paid Garden restoration. Actual-layout/trait/material previews inspected.
- **A12/G7:** actual local light, held-berry costs/native Night Vision,
  reentrant denial/refund/hand replacement, Creative and saved recovery;
  actual held-book observations, eight wild signatures, parented rare records,
  native one-time XP and saved off-hand reading. Customized guides retain
  all 124 pages. Earlier case counts below are historical checkpoints.

- **H11:** native colored bundles/full item components, actual hand controls,
  all expressed capacities/hidden copies, smaller-build cargo retention and
  one overload multiplier with native/external-attribute reloads; immutable care
  inspection, last-first return, Adventure/pup refusal and creative behavior.
  Real equipment death with mob loot off/reentrant callback and real basket
  capture/release conserve whole cargo/identity. Optional geometry resets and
  exact shared body/pup/leg poses pass; gameplay/GPU review remains separate.

- **A10/V1/V2/R1:** actual native targeting and real sculk STEP/eating signals;
  native Pen job assignment, all career ranks/regional offers, payments, stock
  and merchant saves; authored NBT/native start, four rotated barn pieces, real
  deferred loot and preserved-container replay; actual sample controls, unphased/
  hidden gene pairs and native comparator/lamp responses. Final guide count is
  111 after H11. Client art, natural barn distribution/map searches remain gameplay review.

- **G6/S11:** real hopper-fed redstone courtship, actual native mating with the
  correct two parental genomes, tier average and cooldown; held-power refusal;
  exact food saves/hand controls, read-only Calipers, hazards/third adults/pups/
  protection and unloaded boundaries; parent-change cancellation, recursive
  feeding and commit-time controller removal conserve paid food/love. Every
  Pen visual state and quality threshold/fraction maps to shipped assets; native
  guide item codec retains all 100 pages. The existing animal model gate passes.
  Native client item rendering and Pen gameplay remain manual review.

- **J1/J2/G5/D12/T5:** real top/side/output hoppers, every tier's crafting and
  pressing, blocked/aborted output, saved partial work, power pause and unchanged
  seed inheritance; bounded acknowledged name edits, Unicode/snapshot/native
  item/guide saves and changed-book refusal; all twelve native rug recipes,
  placement/support drops; jar coats, full samples, access and actual comparator
  outputs. Named page bounds/search, visual states/texture references and the
  unchanged animal model gate pass. Native client GUI/art playtests remain open.
  Historical GenomeCheck source assertions were not rerun.

- **A8/A9/U1 completion:** real new/legacy structure pieces and fully fitted
  trails preserve mouths, tree, Nests, containers, fluids and ores. Native
  swimming establishes stronger propulsion with retained drowning and exact
  external-attribute saves. Actual model draws cover every optional phenotype
  and shared-render resets. Focused genetics/inheritance/legacy/trend/readout/
  juice checks pass. Current optional geometry and guide previews inspected.

- **A7:** actual attacks and defensive blinks, retained damage/home, recovery
  saves, hidden-copy/pup/transport exclusions, native cancellation, unsafe
  redirects and callback gene/position changes. Focused pure inheritance,
  legacy-format, range and juice checks pass. The historical full GenomeCheck
  also reports 32 stale assertions against older source-code shapes; those
  source-string checks do not establish current native behavior and were not
  rewritten in this feature batch.

- **D9/L32:** real wild-prey kill/heal/rest, named/leashed/boat exclusions,
  new magma under an existing route, renamed prey, relocation and target
  ownership, with legacy shore fishing passing. Native off-hand work reports
  preserve whole stock/filter/mode, animal NBT/tag/navigation and tool; trait,
  pup, power/enable and remote loaded-only boundaries pass.

- **L28–L30/N9:** actual crouched off-hand mode cycling/toggle/Adventure,
  policy/native legacy saves, exact named stock/filter, real paths and Bloom/
  reserve thresholds, native placement refusal and replacement containers,
  source removal/refund conservation, paid carrot/guava restoration, cactus
  occupant/embryo exclusions, stock-callback litter swaps, exact nursery
  stage/cost/pedigree preservation and no autonomous hatch.

- **O6/O7:** real picking/planting pulses, exact wear/stock/components, stale/
  unripe/unsupported/unloaded refusal, hopper collection, ordinary beehive
  fallback, blocked-container food ejection and upgraded-guava exclusion.
- **D8:** actual safe paths, three-high cactus top/flower preservation,
  same-color mushroom seed stock, callback invalidation, ripe wart reset,
  native melon-stem reset, exact healing/shared rest and no loose duplicates.
- **H9/L27:** real comparator claim/failed-move/save/travel/rehome/death
  transitions, scheduled stale-signal recovery and non-loading remote notifications; native seeded
  chest rolls cover optional seeds with preserved food/guide pools.

- **L25/L26:** living/wrong-facing/decorative melon cases, Diet/filter refusals,
  protection-callback stem loss, exact slice/native stem reset, no loose copy,
  Bloom thresholds, one-stage/no-food growth, mature refusal and hand feeding.
- **D7/L24:** grace/pickup reservations, upgraded-food refusal, safe paths,
  callback reservation/power, exact components, shared recovery, native journal
  abort/full-destination rollback and reentrant trough removal conserving food.
- **C10:** actual warm/cold biomes, Bloom +9/+10, hidden snow, off-hand brush
  cost/recovery, combined products and independent native dispenser shearing.
- **N1:** real redstone pulses, malformed/blocked/wrong-soil/occupied planting,
  exact parental genomes/stats/quality/Mutagen and native crop saves, dust cost,
  ripe/empty/other-plant refusals, no ejection, real hand survival/creative use
  and native dry-farmland upkeep beneath a Seed Crop.
- **N2:** whole named/custom-data seeds, defensive copies, native new/legacy
  saves, actual survival unripe break, support loss and Mutagen split costs.
- **N3:** actual hopper supply/drain and transaction abort, native save/ticker/
  comparator, hand insertion/growth/withdrawal/break drops, real dispenser
  seed/bone-meal/dust pulses, dry/clear/unoccupied pad and crowd refusals,
  actual empty-hand/rising-edge hatching and held-power refusal. External
  spawn cancellation plus reentrant removal rolls back siblings and returns
  exactly the original named seed; retry succeeds.
- **N4:** actual late break cancellation for unripe/ripe crops, Adventure
  restriction, survival/creative harvests, crowd retention, native partial-spawn
  refusal with no remaining siblings/one seed refund, and water replacement.
- **L14:** real claimed home/path, Bloom/reserve thresholds, committed transfer
  interruption, lost Bloom, griefing/protection refusal, crop priorities,
  exact named reserve/filter, shared work rest, non-compostable refusal,
  deterministic native failed/successful chances, actual maturation and hopper
  output of exactly one bone meal.
- **C9/L15:** actual hidden/expressed moss and Bloom +9/+10 hand care,
  combined moss/whisker/Starlit products, gene-change recovery refusal and
  real dispenser collection. Native compost data loads correct values,
  excludes all seven higher guava tiers and lets a plain resident spend one
  surplus guava while keeping its eight-item reserve.
- **N5:** native off-hand ripe-crop treatment, creative Cradle preservation,
  Adventure refusal, whole-item native saves and unchanged stage/remaining
  time. Real dispenser Cradle/crop treatment, repeat/empty/wrong-target refusal
  and no vial ejection pass; existing held-seed Mutagen remains covered by N2.
- **N6:** actual native sneak-Calipers on clear unripe/blocked ripe Cradles
  preserves seed/stage/time/tool and creates no pups; shared inspection reports
  matching crowd refusal. Existing native spawn/redstone cases remain passing.
- **N7:** focused layout checks preserve complete pads, ordinary troughs/bells,
  all Nests/labels and route connectivity, omitting the Cradle where needed.
  Native owner-column placement, empty initial BE, repeat seed/incubation
  preservation and actual optional/legacy structure saves pass.
- **S9/H6:** exact carried records through air/block/off-hand/creative use,
  duplicate protection, registry award, invalid UUID/pup refusal and unchanged
  payload. Basket release still preserves all six paused timers, identity,
  ancestry/tag/name/health/age and home-ticket transfer.
- **L13:** native night waiting/meal arbitration, exact food/healing/shared
  rest, short night bounds, ordinary daytime reach, outdoor/work exclusions,
  lead interruption, damaging/blocked routes and Apex exclusion.
- **H7:** server free/claimed inspection, stack conservation, named off-hand
  rebinding, native marker save/tick after Nest removal, creative, permission
  and full-inventory cases.
- **L12:** Bloom threshold, one-stage/no-food growth, shared rest, ripe priority,
  Diet/filter/griefing refusals, interrupted growth, berries, guavas/vines and
  embryo exclusion. Care-dispenser drops now stay in a physical test catchment.

The Cradle textures and current guide/forecast previews are inspected with a substitute
font; the approved
creature model is unchanged. GitHub additionally checks that model before the
exact-commit artifact upload. All earlier care/lab/show/registry, diet, home,
transport, refuge/weather, fixture/garden, pantry, automation/filter/bell,
sign/boat and Apex cases remain in the suite; detailed evidence lives in their
specifications. Fresh mob pathfinding needs a native physics tick on the floor.
Native path regions use non-loading getChunkNow and sight checks loaded bounds.

Native cases establish server behavior. Client interaction/book fonts/lighting,
natural chunk order/travel/unload, long multiplayer routines, modded transfer
devices and progression/decorative breeding balance remain gameplay review.

Earlier user feedback: U2/U3 received “good, works well”; the six-stage `d7f39a9`
release (U4/U5/U6b/T4/W3/V1c) received “everything compiled and worked this run”.
Individual edge cases were not itemized. These reports do not confirm newer
features. U6a's entry advancement needs a natural structure rather than a
command feature; U1/H3b natural home/den review remains useful.

## Continuing

`docs/ROADMAP.md` tracks unfinished planned work as well as optional candidates.
Prioritize completing those features over further comfort controls. Substantial
coherent additions remain authorized; define bounded rules and complete meaningful
batches through build and delivery. Keep missing work distinct from manual playtest
evidence: a pending gameplay check does not make an implemented system a stub.
For terrain review use `/bunnidogs warren 1 surface`, `3 surface`, `5 surface`,
cave/Nether `1`, End `5 surface`, and natural structure lookup:
`/locate structure bunnidogs:warren_village`, `bunnidogs:warren_end_village`
or `bunnidogs:island_refuge` in new ocean terrain.

API lookups: `docs/API_NOTES.md`. Player reference: `docs/FEATURES.md`.
Conventions/art/environment: `docs/REFERENCE.md`. Original decisions and old
chunk evidence: `docs/DESIGN.md`, `docs/CHUNKS.md`, `docs/history/`.
