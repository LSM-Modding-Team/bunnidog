#!/usr/bin/env python3
"""Generates the Ear Tag item texture: src/main/resources/assets/bunnidogs/textures/item/ear_tag.png (16x16).

Run from the project root:  python3 art/gen_ear_tag.py     (needs Pillow)      Also writes art/ear_tag_preview.png (12x).

A small yellow livestock tag hanging at a slant: a rounded plate with a printed number (dark dashes) and a metal stud
at the top where it clips through the ear.
"""
from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item/ear_tag.png"
PREVIEW = "art/ear_tag_preview.png"

OUTLINE = (84, 66, 20)
PLATE = (244, 208, 72)
PLATE_LIGHT = (252, 232, 128)
PLATE_DARK = (206, 164, 40)
INK = (70, 52, 24)
STUD = (200, 204, 210)
STUD_DARK = (120, 126, 136)

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()


def dot(x, y, c):
    px[x, y] = c + (255,)


# the plate: x 3..12, y 4..13, corners cut
plate = {(x, y) for x in range(3, 13) for y in range(4, 14)}
for corner in ((3, 4), (12, 4), (3, 13), (12, 13)):
    plate.discard(corner)
for (x, y) in plate:
    dot(x, y, PLATE)
# a lit top edge and a shaded bottom edge
for x in range(4, 12):
    dot(x, 4, PLATE_LIGHT)
    dot(x, 13, PLATE_DARK)
for y in range(5, 13):
    dot(12, y, PLATE_DARK)
    dot(3, y, PLATE_LIGHT)
# the printed number: two rows of dashes
for x in (5, 6, 8, 9, 10):
    dot(x, 8, INK)
for x in (5, 7, 8, 10):
    dot(x, 10, INK)
# the stud / clip at the top
for (x, y) in ((7, 1), (8, 1), (7, 2), (8, 2), (7, 3), (8, 3)):
    dot(x, y, STUD)
dot(8, 2, STUD_DARK)
dot(8, 3, STUD_DARK)
# outline pass: any transparent pixel next to a solid pixel becomes the outline colour
solid = {(x, y) for x in range(16) for y in range(16) if px[x, y][3]}
for (x, y) in solid:
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        nx, ny = x + dx, y + dy
        if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in solid:
            dot(nx, ny, OUTLINE)

img.save(OUT)
img.resize((16 * 12, 16 * 12), Image.NEAREST).save(PREVIEW)
print("wrote", OUT)
