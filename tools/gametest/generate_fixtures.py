#!/usr/bin/env python3
"""Regenerate native structure bounds for tests with extended physical fixtures."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT/'art'))
from nbt_template import save

# TestInstanceBlockEntity only force-loads its structure's intersecting chunks.
# An 8-block template cannot keep a 31-block excursion entity-ticking reliably.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/trail_corridor.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (36, 5, 8), version=4903)

# Family queries and births take place around z=31, not inside the default 8x8 template.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/family_workshop.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (17, 5, 43), version=4903)

# A native power-10 blast reaches twenty blocks; every queried chunk must be forced.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/apex_arena.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (64, 8, 64), version=4903)

# Power twenty needs its actual forty-block victim/terrain bounds loaded.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/apex_blast_arena.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (112, 8, 112), version=4903)

# The Crown arena includes its physical south seal and the entire 64-block ray neighborhood.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/crown_arena.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (128, 8, 128), version=4903)
