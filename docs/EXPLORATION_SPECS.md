# Exploration and useful sensory lines

## E1: Warren Explorer Maps

Journeyman cartographers may offer an ordinary filled **Warren Explorer Map**
for fourteen emeralds and one compass, with twelve uses, ten villager XP and
native reputation/demand pricing. The offer joins their existing level-three
pool through an appended vanilla trade tag; two native selections mean it is
not guaranteed. Existing generated offers, uses and inventories remain intact.

Use Minecraft 26.2's native `villager_trade` data and exploration-map modifier,
not an obsolete trade event or a new merchant screen. The target tag
`bunnidogs:on_warren_maps` contains the natural Overworld `warren_village`
structure. Underground feature dens and End settlements are excluded. Data
packs can extend the structure tag normally. Search radius fifty and the
native skip-known-structures rule follow ordinary map behavior; failed
searches discard the blank result and omit the offer. No emeralds/compass can
be exchanged for an unlocated map. The normal village marker identifies its
target, with a translated item name, native biome preview, map data and updates.

This adds an ordinary survival discovery route to warrens without commands,
forced generation or changes to structure frequency and existing starts. The
Field Guide's warren chapter points to map instructions. Actual successful
natural-world discovery and client map rendering remain gameplay review.

E1 evidence (2026-10-18): actual Java 25 build/all 39 native scenarios pass.
The extended pantry/exploration case reads the loaded native trade set, confirms
the appended map and retained ordinary compass trade, and calls the real offer
factory without a located origin: its native filters return no blank-map offer.
The guide preview is inspected. This does not simulate a successful natural
structure search or establish client map rendering.

## E2: bat-ear mineral scouting

Sneak-use a compass on a living, at least half-healthy adult expressing **bat
ears** and with at least **+10 bred Senses**. It marks the nearest loaded tagged
ore within a six-block sphere around the animal, through ordinary rock. The
action reads terrain and changes no block, drop, stat, health, age, love, path
or Nest. Hidden bat copies, pups, lower Senses, combat/refuge and the Apex
refuse without changing the compass or starting recovery. Native build
permission is required, as with Nest compass marking. Leads are allowed:
this is a deliberate measurement, not an autonomous journey.

Use `bunnidogs:surveyable_ores`: native coal/iron/copper/gold/redstone/lapis/
diamond/emerald ore tags, quartz ore and ancient debris. Blocks such as raw
metal stores and mineral blocks are not ore. Data packs can adjust the tag.
The bounded deterministic search only reads already loaded chunks inside the
world border; ties are stable. It never requests new chunks or performs a
continuous/per-tick scan. A successful or empty survey both start thirty
seconds / 600 ticks of saved `ScoutWait`, clamped on loading and paused while
unloaded. Gene/stat/tool changes cannot bypass this shared animal recovery.
Old animals start ready. Inventory-full markers drop normally; creative and
stacked/off-hand compasses share the existing Nest marker transaction.

The marker preserves all compass components and uses native dimension-aware
Lodestone Tracker place data with tracking disabled. It stays at the surveyed
place after mining, just as a Nest marker stays after Nest removal. It does
not continually find new ore or replace an existing marker on a refused/empty
survey. A translated result names the ore and all three coordinates, including
depth; a quiet chime confirms the survey. Calipers shares cheap eligibility/
remaining recovery without a terrain search or timer changes. Gene hovers
and the guide describe the bred cave role. No extra item, client renderer,
synced field, gene ID, saved genome format or network-version change is needed.

E2 evidence (2026-10-18): actual Java 25 build/all 40 native scenarios pass.
The new survey case checks real off-hand named stacked compasses, nearest
iron versus farther diamond, expressed/hidden bat ears, Senses +9/+10,
pup/health/Adventure/combat refusals, lead compatibility and unchanged animal/
ore state. Mining and gene/stat edits retain the marked place/recovery;
Calipers is read-only. Native saves retain/clamp recovery and legacy data is
ready. Six-block sphere edges, raw-metal exclusion, an unloaded remote search
with no new chunk and an empty survey preserving an older marker pass.
Existing Nest marker stacking/creative/overflow/native-save tests still pass
through the shared marker helper. The guide preview is inspected. Client
compass rendering, natural cave use and mining balance remain gameplay review.
