#!/usr/bin/env python3
"""Generates the Pedigree Book texture: src/main/resources/assets/bunnidogs/textures/item/pedigree_book.png (16x16).

Run from the project root:  python3 art/gen_pedigree_book.py     (needs Pillow)      Also writes art/pedigree_book_preview.png (12x).

A guava-green cover with pale pages at the edge and, on the cover, two tall ears over a round face (the bunnidog emblem).
"""
from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item/pedigree_book.png"
PREVIEW = "art/pedigree_book_preview.png"

OUTLINE = (40, 56, 36)
COVER = (96, 154, 72)
COVER_DARK = (66, 112, 52)
PAGES = (240, 234, 214)
PAGES_DARK = (206, 196, 168)
EMBLEM = (244, 222, 200)
EYE = (60, 40, 40)

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()


def dot(x, y, c):
    px[x, y] = c + (255,)


# book body: x 2..13, y 1..14
for y in range(1, 15):
    for x in range(2, 14):
        edge = x in (2, 13) or y in (1, 14)
        if edge:
            dot(x, y, OUTLINE)
        elif x == 3:
            dot(x, y, COVER_DARK)          # the spine
        elif x >= 12:
            dot(x, y, PAGES if y % 2 else PAGES_DARK)   # page edge on the right
        else:
            dot(x, y, COVER)
# emblem: ears (two columns, 3 high) over a 4x3 face with two eyes
for (x, y) in ((6, 3), (6, 4), (6, 5), (9, 3), (9, 4), (9, 5)):
    dot(x, y, EMBLEM)
for y in (6, 7, 8):
    for x in range(5, 11):
        dot(x, y, EMBLEM)
dot(6, 7, EYE)
dot(9, 7, EYE)
# a ribbon bookmark
for y in (11, 12, 13):
    dot(10, y, (214, 90, 90))

img.save(OUT)
img.resize((16 * 12, 16 * 12), Image.NEAREST).save(PREVIEW)
print("wrote", OUT)
