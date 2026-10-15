#!/usr/bin/env python3
"""Generates the Calipers item texture: src/main/resources/assets/bunnidogs/textures/item/calipers.png (16x16).

Run from the project root:  python3 art/gen_calipers.py     (needs Pillow)      Also writes art/calipers_preview.png (12x).

Two iron legs crossing at a copper rivet, pointed tips at the bottom and two grips at the top: a pair of measuring calipers.
"""
from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item/calipers.png"
PREVIEW = "art/calipers_preview.png"

IRON = (200, 204, 210)
IRON_DARK = (120, 126, 136)
OUTLINE = (58, 62, 72)
COPPER = (214, 130, 84)
COPPER_DARK = (150, 82, 52)

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()


def dot(x, y, c):
    px[x, y] = c + (255,)


# the two legs cross at (7,7)/(8,8): leg A runs top-right -> bottom-left, leg B top-left -> bottom-right
for i in range(0, 13):
    ya = 1 + i
    xa = 13 - i
    xb = 2 + i
    for (x, y) in ((xa, ya), (xb, ya)):
        if 0 <= x < 16 and 0 <= y < 15:
            dot(x, y, IRON if i % 2 == 0 else IRON_DARK)
# outline pass: any transparent pixel next to an iron pixel becomes the outline colour
solid = {(x, y) for x in range(16) for y in range(16) if px[x, y][3]}
for (x, y) in solid:
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        nx, ny = x + dx, y + dy
        if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in solid:
            dot(nx, ny, OUTLINE)
# grips at the top, tips at the bottom
for x in (13, 14):
    dot(x, 0, COPPER_DARK)
for x in (1, 2):
    dot(x, 0, COPPER_DARK)
dot(13, 1, COPPER)
dot(2, 1, COPPER)
dot(2, 13, IRON)
dot(13, 13, IRON)
# the rivet where they cross
for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
    dot(x, y, COPPER)
dot(7, 8, COPPER_DARK)
dot(8, 8, COPPER_DARK)

img.save(OUT)
img.resize((16 * 12, 16 * 12), Image.NEAREST).save(PREVIEW)
print("wrote", OUT)
