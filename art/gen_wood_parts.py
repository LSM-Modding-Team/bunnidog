#!/usr/bin/env python3
"""Generates the JSON of the guava wood PARTS (W2): blockstates, models, client items, loot tables and recipes for
guava_stairs, guava_slab and guava_fence (all made of the planks texture; no new image). Later chunks add to PARTS (fence gate, door...).

Run from the project root:  python3 art/gen_wood_parts.py      Deterministic: re-running rewrites the same files.
The stairs blockstate is the 40-variant table of vanilla (facing x half x shape); the rule is in stairs_state().
"""
import json
import os

RES = "src/main/resources"
ASSETS = RES + "/assets/bunnidogs"
DATA = RES + "/data"
PLANKS = "bunnidogs:block/guava_planks"


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(json.dumps(obj, indent=2) + "\n")


def stairs_state():
    """facing east = 0 degrees, south 90, west 180, north 270. Bottom half: straight / inner_right / outer_right use the base angle,
    inner_left / outer_left turn 90 back. Top half (x = 180): straight / inner_left / outer_left use the base angle,
    inner_right / outer_right turn 90 on."""
    variants = {}
    for facing, base in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                model = {"straight": "guava_stairs", "inner_left": "guava_stairs_inner", "inner_right": "guava_stairs_inner",
                         "outer_left": "guava_stairs_outer", "outer_right": "guava_stairs_outer"}[shape]
                left = shape.endswith("left")
                if half == "bottom":
                    y = (base - 90 if left else base) % 360
                    v = {"model": "bunnidogs:block/" + model}
                    if shape == "straight":
                        y = base
                else:
                    y = (base if (left or shape == "straight") else base + 90) % 360
                    v = {"model": "bunnidogs:block/" + model, "x": 180}
                if y:
                    v["y"] = y
                v["uvlock"] = True
                variants[f"facing={facing},half={half},shape={shape}"] = v
    return {"variants": variants}


def main():
    # ---- stairs
    write(ASSETS + "/blockstates/guava_stairs.json", stairs_state())
    for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
        write(ASSETS + f"/models/block/guava_stairs{suffix}.json",
              {"parent": "minecraft:block/" + parent, "textures": {"bottom": PLANKS, "top": PLANKS, "side": PLANKS}})
    # ---- slab
    write(ASSETS + "/blockstates/guava_slab.json", {"variants": {
        "type=bottom": {"model": "bunnidogs:block/guava_slab"},
        "type=double": {"model": "bunnidogs:block/guava_planks"},
        "type=top": {"model": "bunnidogs:block/guava_slab_top"}}})
    write(ASSETS + "/models/block/guava_slab.json",
          {"parent": "minecraft:block/slab", "textures": {"bottom": PLANKS, "top": PLANKS, "side": PLANKS}})
    write(ASSETS + "/models/block/guava_slab_top.json",
          {"parent": "minecraft:block/slab_top", "textures": {"bottom": PLANKS, "top": PLANKS, "side": PLANKS}})
    # ---- fence
    write(ASSETS + "/blockstates/guava_fence.json", {"multipart": [
        {"apply": {"model": "bunnidogs:block/guava_fence_post"}},
        {"when": {"north": "true"}, "apply": {"model": "bunnidogs:block/guava_fence_side", "uvlock": True}},
        {"when": {"east": "true"}, "apply": {"model": "bunnidogs:block/guava_fence_side", "y": 90, "uvlock": True}},
        {"when": {"south": "true"}, "apply": {"model": "bunnidogs:block/guava_fence_side", "y": 180, "uvlock": True}},
        {"when": {"west": "true"}, "apply": {"model": "bunnidogs:block/guava_fence_side", "y": 270, "uvlock": True}}]})
    for suffix, parent in (("_post", "fence_post"), ("_side", "fence_side"), ("_inventory", "fence_inventory")):
        write(ASSETS + f"/models/block/guava_fence{suffix}.json", {"parent": "minecraft:block/" + parent, "textures": {"texture": PLANKS}})
    # ---- client items (the fence shows its inventory model)
    for name, model in (("guava_stairs", "guava_stairs"), ("guava_slab", "guava_slab"), ("guava_fence", "guava_fence_inventory")):
        write(ASSETS + f"/items/{name}.json", {"model": {"type": "minecraft:model", "model": "bunnidogs:block/" + model}})
    # ---- loot: stairs and fence drop themselves, a double slab drops two
    for name in ("guava_stairs", "guava_fence"):
        write(DATA + f"/bunnidogs/loot_table/blocks/{name}.json",
              {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "bunnidogs:" + name}]}]})
    write(DATA + "/bunnidogs/loot_table/blocks/guava_slab.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{
        "type": "minecraft:item", "name": "bunnidogs:guava_slab",
        "functions": [{"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{
            "condition": "minecraft:block_state_property", "block": "bunnidogs:guava_slab", "properties": {"type": "double"}}]},
            {"function": "minecraft:explosion_decay"}]}]}]})
    # ---- recipes (vanilla shapes, guava planks)
    write(DATA + "/bunnidogs/recipe/guava_stairs.json", {"type": "minecraft:crafting_shaped", "key": {"P": "bunnidogs:guava_planks"},
          "pattern": ["P  ", "PP ", "PPP"], "result": {"id": "bunnidogs:guava_stairs", "count": 4}})
    write(DATA + "/bunnidogs/recipe/guava_slab.json", {"type": "minecraft:crafting_shaped", "key": {"P": "bunnidogs:guava_planks"},
          "pattern": ["PPP"], "result": {"id": "bunnidogs:guava_slab", "count": 6}})
    write(DATA + "/bunnidogs/recipe/guava_fence.json", {"type": "minecraft:crafting_shaped",
          "key": {"P": "bunnidogs:guava_planks", "S": "minecraft:stick"}, "pattern": ["PSP", "PSP"],
          "result": {"id": "bunnidogs:guava_fence", "count": 3}})
    # ---- tags (data only): vanilla's own wooden tags decide fuel, fence joining and the stairs / slabs families
    def tag(path, add):
        if os.path.exists(path):
            t = json.load(open(path))
        else:
            t = {"replace": False, "values": []}
        for v in add:
            if v not in t["values"]:
                t["values"].append(v)
        write(path, t)
    for kind in ("block", "item"):
        tag(f"{DATA}/minecraft/tags/{kind}/wooden_stairs.json", ["bunnidogs:guava_stairs"])
        tag(f"{DATA}/minecraft/tags/{kind}/wooden_slabs.json", ["bunnidogs:guava_slab"])
        tag(f"{DATA}/minecraft/tags/{kind}/wooden_fences.json", ["bunnidogs:guava_fence"])
        tag(f"{DATA}/minecraft/tags/{kind}/fences.json", ["bunnidogs:guava_fence"])
    tag(f"{DATA}/minecraft/tags/block/mineable/axe.json", ["bunnidogs:guava_stairs", "bunnidogs:guava_slab", "bunnidogs:guava_fence"])


if __name__ == "__main__":
    main()
