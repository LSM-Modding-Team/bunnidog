# Lunar forge and Crown expedition update

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

Generic supplies keep role parts exclusive to Apex kills and the forge minerals
exclusive to mining. A normal supply barrel now rolls 16–32 enchanted apples,
128–256 XP bottles, 64–128 golden apples, 2–3 Totems, 4–8 each diamond blocks/
Netherite ingots, pearls/Flight-1 rockets and bulk amethyst/gunpowder/copper/gold/
fire charges. Wing and finale rewards increase these quantities substantially.
The maximum rolls fit 27 slots, counting pearls as 16 and Totems as one per slot.
Opened barrels remain their exact saved contents.

For an existing castle, `/bunnidogs dungeon fortify` converts only still-matching
old structural blocks in loaded chunks, using actual native template coordinates.
It does not re-paste the building, refill containers or reset keys/progress.
The original architecture is retained. Protocol 44 requires matching clients.

Evidence: native recipe-manager crafts/smelting/smithing, actual 27-slot supply
barrel, Blastmaw-resistant masonry, constrained encounter movement/targets,
paid barding swaps/wounds/save/load and boss/co-op profiles. The full 252-case
native suite passed. Five binary equipment previews and six final citadel views
inspect actual exported meshes/templates; provenance is in `art/citadel/`.
Live client rendering/clearance and natural Survival pacing remain human review.
