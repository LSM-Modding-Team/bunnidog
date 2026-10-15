#!/usr/bin/env python3
"""Generates the Trophy Pedestal textures (drops D8a): textures/block/trophy_pedestal.png (pale stone bricks: base and column) and
trophy_pedestal_plate.png (the gold-trimmed top plate). Both 16x16.

Run from the project root:  python3 art/gen_pedestal.py     (needs Pillow)      Also writes art/pedestal_preview.png (12x).
Deterministic (fixed seed), so re-running gives the same files.
"""
import random

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/pedestal_preview.png"

STONE = [(206, 200, 188), (190, 184, 172), (174, 168, 156)]
MORTAR = (140, 134, 124)
GOLD = [(250, 214, 92), (226, 178, 52), (178, 128, 36)]
PLATE = [(222, 214, 198), (206, 198, 182)]


def stone(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = STONE[rng.randrange(3)] + (255,)
    # bricks 8 wide, 4 high, every other course shifted by 4
    for y in range(16):
        for x in range(16):
            course = y // 4
            if y % 4 == 3:
                px[x, y] = MORTAR + (255,)
            elif (x + (4 if course % 2 else 0)) % 8 == 7:
                px[x, y] = MORTAR + (255,)
    return img


def plate(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = PLATE[rng.randrange(2)] + (255,)
    for i in range(16):                       # a gold frame, lighter on the top-left, darker on the bottom-right
        for (x, y) in ((i, 0), (0, i)):
            px[x, y] = GOLD[0] + (255,)
        for (x, y) in ((i, 15), (15, i)):
            px[x, y] = GOLD[2] + (255,)
        for (x, y) in ((i, 1), (1, i), (i, 14), (14, i)):
            if 1 <= i <= 14:
                px[x, y] = GOLD[1] + (255,)
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):   # a small gold stud in the middle
        px[x, y] = GOLD[0] + (255,)
    return img


def main():
    rng = random.Random(8)
    tiles = {"trophy_pedestal": stone(rng), "trophy_pedestal_plate": plate(rng)}
    for name, img in tiles.items():
        img.save(f"{OUT}/{name}.png")
    sheet = Image.new("RGBA", (16 * 12 * 2 + 12, 16 * 12), (60, 60, 60, 255))
    for i, img in enumerate(tiles.values()):
        sheet.paste(img.resize((192, 192), Image.NEAREST), (i * (192 + 12), 0))
    sheet.save(PREVIEW)


if __name__ == "__main__":
    main()
