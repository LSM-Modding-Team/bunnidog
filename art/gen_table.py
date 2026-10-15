#!/usr/bin/env python3
"""Generates the Genetics Table textures (T1): textures/block/genetics_table.png (wooden sides and legs with a brass band),
genetics_table_top.png (a green work mat in a wooden frame with a brass ring where the sample stands) and genetics_table_metal.png
(the copper scope). All 16x16.

Run from the project root:  python3 art/gen_table.py     (needs Pillow)      Also writes art/table_preview.png (12x).
Deterministic (fixed seed), so re-running gives the same files.
"""
import random

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/table_preview.png"

WOOD = [(122, 84, 52), (110, 74, 44), (96, 64, 38)]
WOOD_LINE = (74, 48, 28)
BRASS = [(236, 196, 84), (200, 154, 52), (150, 108, 32)]
MAT = [(58, 116, 84), (50, 104, 74)]
MAT_LINE = (36, 80, 58)
COPPER = [(214, 128, 82), (188, 106, 64), (150, 80, 46)]


def side(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = WOOD[rng.randrange(3)] + (255,)
    for y in (5, 10):                                   # plank seams
        for x in range(16):
            px[x, y] = WOOD_LINE + (255,)
    for x in range(16):                                 # a brass band along the top edge
        px[x, 0] = BRASS[0] + (255,)
        px[x, 1] = BRASS[1] + (255,)
        px[x, 2] = BRASS[2] + (255,)
    return img


def top(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = MAT[rng.randrange(2)] + (255,)
    for i in range(16):                                 # the wooden frame, two pixels wide
        for (x, y) in ((i, 0), (i, 1), (0, i), (1, i), (i, 14), (i, 15), (14, i), (15, i)):
            px[x, y] = WOOD[(x + y) % 3] + (255,)
    for i in range(2, 14):                              # a thin dark line where the mat meets the frame
        for (x, y) in ((i, 2), (2, i), (i, 13), (13, i)):
            px[x, y] = MAT_LINE + (255,)
    cx = cy = 7.5                                       # the brass ring for the sample: radius about 3.5
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if 3.0 <= d <= 4.2:
                px[x, y] = BRASS[0 if (x + y) % 2 else 1] + (255,)
    return img


def metal(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = COPPER[0 if x < 5 else 1 if x < 11 else 2] + (255,)
            if rng.randrange(8) == 0:
                px[x, y] = COPPER[1] + (255,)
    return img


def main():
    rng = random.Random(36)
    tiles = {"genetics_table": side(rng), "genetics_table_top": top(rng), "genetics_table_metal": metal(rng)}
    for name, img in tiles.items():
        img.save(f"{OUT}/{name}.png")
    sheet = Image.new("RGBA", (16 * 12 * 3 + 24, 16 * 12), (60, 60, 60, 255))
    for i, img in enumerate(tiles.values()):
        sheet.paste(img.resize((192, 192), Image.NEAREST), (i * (192 + 12), 0))
    sheet.save(PREVIEW)
    print("wrote", ", ".join(tiles))


if __name__ == "__main__":
    main()
