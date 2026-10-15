#!/usr/bin/env python3
"""Generates the Worn Ear Tag item texture: src/main/resources/assets/bunnidogs/textures/item/worn_ear_tag.png (16x16).

Run from the project root:  python3 art/gen_worn_ear_tag.py     (needs Pillow)      Also writes art/worn_ear_tag_preview.png
(12x, next to the fresh Ear Tag for comparison).

The same tag as art/gen_ear_tag.py after a life on an ear: the yellow has faded to a dull ochre, the number is half rubbed
out, a corner is chipped, a scratch crosses the plate, the stud is dark and bent, and a few strands of fur are still caught in it.
"""
from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item/worn_ear_tag.png"
FRESH = "src/main/resources/assets/bunnidogs/textures/item/ear_tag.png"
PREVIEW = "art/worn_ear_tag_preview.png"

OUTLINE = (66, 54, 30)
PLATE = (196, 164, 84)
PLATE_LIGHT = (214, 186, 110)
PLATE_DARK = (150, 120, 56)
SCRATCH = (120, 98, 52)
INK = (96, 78, 48)
STUD = (150, 152, 156)
STUD_DARK = (92, 96, 104)
FUR = (222, 214, 196)

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()


def dot(x, y, c):
    px[x, y] = c + (255,)


# the plate: x 3..12, y 4..13, corners cut (as the fresh tag), plus a chipped bottom-right and a nick on the left edge
plate = {(x, y) for x in range(3, 13) for y in range(4, 14)}
for corner in ((3, 4), (12, 4), (3, 13), (12, 13), (11, 13), (12, 12), (3, 9)):
    plate.discard(corner)
for (x, y) in plate:
    dot(x, y, PLATE)
# a dulled top edge and a darker bottom edge
for x in range(4, 12):
    dot(x, 4, PLATE_LIGHT)
for x in range(4, 11):
    dot(x, 13, PLATE_DARK)
for y in range(5, 12):
    dot(12, y, PLATE_DARK)
for y in (5, 6, 7, 8, 10, 11, 12):
    dot(3, y, PLATE_LIGHT)
# the number, half rubbed out: a gap in each row
for x in (5, 6, 9):
    dot(x, 8, INK)
for x in (5, 8, 10):
    dot(x, 10, INK)
# a diagonal scratch
for (x, y) in ((5, 5), (6, 6), (7, 7), (8, 9), (9, 11)):
    dot(x, y, SCRATCH)
# the stud / clip at the top, dark and slightly bent to the left
for (x, y) in ((7, 1), (8, 1), (7, 2), (7, 3), (8, 3)):
    dot(x, y, STUD)
dot(8, 2, STUD_DARK)
dot(7, 3, STUD_DARK)
# a few strands of fur caught in the stud
dot(9, 1, FUR)
dot(6, 2, FUR)
dot(9, 2, FUR)
# outline pass: any transparent pixel next to a solid pixel becomes the outline colour
solid = {(x, y) for x in range(16) for y in range(16) if px[x, y][3]}
for (x, y) in solid:
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        nx, ny = x + dx, y + dy
        if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in solid:
            dot(nx, ny, OUTLINE)

img.save(OUT)

# preview: the fresh tag on the left, the worn one on the right, both 12x on a grey background
sheet = Image.new("RGBA", (16 * 12 * 2 + 36, 16 * 12 + 24), (92, 92, 100, 255))
for i, im in enumerate((Image.open(FRESH).convert("RGBA"), img)):
    big = im.resize((16 * 12, 16 * 12), Image.NEAREST)
    sheet.paste(big, (12 + i * (16 * 12 + 12), 12), big)
sheet.save(PREVIEW)
print("wrote", OUT)
