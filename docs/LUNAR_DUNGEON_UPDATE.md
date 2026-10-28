# Lunar forge and Crown expedition update

The later material economy supersedes this update's original vanilla-heavy
supplies. See [LANTERN_WARRENS_AND_GUILD_PROJECTS.md](LANTERN_WARRENS_AND_GUILD_PROJECTS.md)
and the current Crown spec for exact room splits, crafts and evidence.

Implemented six-item play-review plan:

1. Lunar forge: nine shards → concentrate; nine Cinderite → billet; nine
   Moonstone → compressed Moonstone; compressed rock + lens + marrow → binder;
   four concentrates + four billets + binder → bloom; blast-smelt to ingot.
   Armour uses plating (four ingots + four billets + membrane) in smithing;
   tools retain one ingot for the first mining foothold. Armour totals: 576
   shards, 720 raw Cinderite, 144 Moonstone, 16 marrow, 24 lenses, 12 membranes,
   16 diamonds and four extra Netherite ingots. No dungeon ingredient gate.
2. Replace the weak architectural supply table, not just final rewards. Ship
   bulk generic reagents, apple/XP/mobility reserves and refined vanilla
   wealth, within 27 native slots. No forced refill of opened containers.
3. Launcher powers 3/6/9/12/15; cost/wear remains one charge per selected level.
4. Dungeon guards stay in their precinct/dungeon and trials in saved room bounds;
   filter targets, constrain navigation/flight/jumps and correct external pushes
   without deleting enemies or counting them as killed.
5. Implement real single-item body barding, native stacks/save tracking, visible
   approved tier geometry and textured materials. Ordinary wild-range genomes
   plus actual Astralite equipment replace fresh champions' genetic boosts.
   Preserve old fights, native health, equipment ownership and real costs.
6. New lunar/rootstone masonry: walls, roofs, stairs, slabs, beams, windows and
   lighting. Fully blast-proof, hardness 200, entity-destruction protection and
   Astralite harvest tier. Fresh templates use the new palette. Preserve old
   chunks and stock; any explicit repair must avoid overwriting player changes.

Verification: native recipes/loot, block strength and fresh templates, boundary
movement, equipment/saves/costs, launcher shots; actual exported mesh previews
and final native-template render. Live client appearance remains human review.

Generic supplies keep role parts exclusive to Apex kills. Entry freight cannot
replace first-set moon mining; completed wings and the foundry vault now pay
bulk forge materials for pack upgrades and useful projects. Vanilla supplies
are a declared whole-site budget, not repeated wealth piles in each barrel.
The maximum rolls fit native stock; opened barrels retain their exact contents.

For an existing castle, `/bunnidogs dungeon fortify` converts only still-matching
old structural blocks in loaded chunks, using actual native template coordinates.
It does not re-paste the building, refill containers or reset keys/progress.
The original architecture is retained. Current protocol 46 requires matching clients.

Evidence: native recipe-manager crafts/smelting/smithing, actual 27-slot supply
barrel, Blastmaw-resistant masonry, constrained encounter movement/targets,
paid barding swaps/wounds/save/load and boss/co-op profiles. The full 252-case
native suite passed. Five binary equipment previews and six final citadel views
inspect actual exported meshes/templates; provenance is in `art/citadel/`.
Live client rendering/clearance and natural Survival pacing remain human review.
