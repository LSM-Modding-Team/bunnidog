#!/usr/bin/env python3
"""Regenerate the native structure bounds used by the long walking test."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT/'art'))
from nbt_template import save

# TestInstanceBlockEntity only force-loads its structure's intersecting chunks.
# An 8-block template cannot keep a 31-block excursion entity-ticking reliably.
save(ROOT/'tools/gametest/resources/data/bunnidogs/structure/trail_corridor.nbt',
     {(0, 0, 0): ('minecraft:air', {}, None)}, (36, 5, 8), version=4903)
