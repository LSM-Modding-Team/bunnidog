#!/usr/bin/env python3
"""Generates the two bunnidog haunch textures: src/main/resources/assets/bunnidogs/textures/item/{prime_haunch,roast_haunch}.png (16x16),
plus their client items and models.

Run from the project root:  python3 art/gen_haunch.py     (needs Pillow)      Also writes art/haunch_preview.png (12x, next to the flesh for size).

Drawn from the SAME idea as art/gen_flesh.py (a meaty lump on a bone) but bigger, rounder, with a twine tie and a fat cap, so it reads as the
"prize cut" next to the small drumstick of the plain flesh. Prime is raw (pink, pale fat cap), roast is cooked (glazed browns, a few dark crisp spots).
One silhouette for both: a change of the shape is one edit.
"""
import json
import os
from PIL import Image

ROOT = "src/main/resources/assets/bunnidogs"
OUT = ROOT + "/textures/item"
PREVIEW = "art/haunch_preview.png"

BONE = (240, 232, 214)
BONE_DARK = (190, 178, 156)
PRIME = dict(outline=(120, 54, 66), meat=[(250, 176, 184), (228, 122, 138), (190, 84, 106)], fat=(255, 232, 230), twine=(196, 164, 112), spot=None)
ROAST = dict(outline=(74, 40, 24), meat=[(222, 156, 96), (172, 104, 56), (122, 68, 34)], fat=(244, 206, 140), twine=(236, 214, 150), spot=(84, 44, 24))

# the lump: row -> (left, right) inclusive columns (9 rows, 13 wide at the middle)
LUMP = {1: (6, 11), 2: (4, 12), 3: (3, 13), 4: (2, 14), 5: (2, 14), 6: (2, 14), 7: (3, 13), 8: (4, 12), 9: (6, 11)}
# the bone: a short shank going down-left from the lump, with a knob at the end
BONE_PIXELS = [(5, 9), (4, 10), (5, 10), (3, 11), (4, 11), (2, 12), (3, 12), (1, 13), (2, 13), (1, 14), (2, 14), (3, 13), (0, 13), (0, 14)]
TWINE_COL = 9                       # a tie running down the lump
SPOTS = [(5, 3), (11, 4), (6, 7), (12, 6)]   # crisp spots of the roast


def draw(look):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    hi, mid, lo = look["meat"]
    for (x, y) in BONE_PIXELS:
        px[x, y] = (BONE_DARK if (x + y) % 2 else BONE) + (255,)
    top, bottom = min(LUMP), max(LUMP)
    for y, (l, r) in LUMP.items():
        for x in range(l, r + 1):
            edge = x in (l, r) or y in (top, bottom)
            if edge:
                px[x, y] = look["outline"] + (255,)
            elif y == top + 1:
                px[x, y] = look["fat"] + (255,)                  # the fat cap along the top
            elif x == l + 1:
                px[x, y] = hi + (255,)                           # lit left side
            elif y >= bottom - 1 or x == r - 1:
                px[x, y] = lo + (255,)                           # shaded underside and right side
            else:
                px[x, y] = mid + (255,)
    for y in range(top + 1, bottom):                             # the twine tie
        if LUMP[y][0] < TWINE_COL < LUMP[y][1]:
            px[TWINE_COL, y] = look["twine"] + (255,)
    if look["spot"]:
        for (x, y) in SPOTS:
            if px[x, y][3] and px[x, y][:3] in (look["meat"][1], look["meat"][2], look["meat"][0]):
                px[x, y] = look["spot"] + (255,)
    # where the bone enters the lump
    px[5, 9] = look["outline"] + (255,)
    px[4, 10] = BONE_DARK + (255,)
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    items = {"prime_haunch": draw(PRIME), "roast_haunch": draw(ROAST)}
    for name, img in items.items():
        img.save(f"{OUT}/{name}.png")
        write_json(f"{ROOT}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"bunnidogs:item/{name}"}})
        write_json(f"{ROOT}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"bunnidogs:item/{name}"}})
    # preview: raw flesh, cooked flesh (for size), prime haunch, roast haunch
    scale = 12
    names = [f"{OUT}/raw_bunnidog_flesh.png", f"{OUT}/cooked_bunnidog_flesh.png", f"{OUT}/prime_haunch.png", f"{OUT}/roast_haunch.png"]
    sheet = Image.new("RGBA", (len(names) * (16 * scale + 12) - 12, 16 * scale), (58, 58, 64, 255))
    for i, path in enumerate(names):
        big = Image.open(path).convert("RGBA").resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.paste(big, (i * (16 * scale + 12), 0), big)
    sheet.save(PREVIEW)
    print("wrote", OUT, "and", PREVIEW)


if __name__ == "__main__":
    main()
