#!/usr/bin/env python3
"""Generates the two bunnidog meat textures: src/main/resources/assets/bunnidogs/textures/item/{raw_bunnidog_flesh,cooked_bunnidog_flesh}.png (16x16).

Run from the project root:  python3 art/gen_flesh.py     (needs Pillow)      Also writes art/flesh_preview.png (12x).

Both are the same small drumstick-like cut (a meaty lump on a short bone), raw in pink with a pale fat rim, cooked in browns.
Drawn from one silhouette, so a change of the shape is one edit.
"""
from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item"
PREVIEW = "art/flesh_preview.png"

BONE = (240, 232, 214)
BONE_DARK = (190, 178, 156)
OUTLINE_RAW = (120, 54, 66)
OUTLINE_COOKED = (74, 40, 24)
RAW = dict(outline=OUTLINE_RAW, meat=[(248, 168, 176), (226, 120, 136), (190, 84, 106)], fat=(255, 226, 226))
COOKED = dict(outline=OUTLINE_COOKED, meat=[(206, 140, 84), (164, 100, 56), (120, 68, 36)], fat=(232, 188, 120))

# the meaty lump: row -> (left, right) inclusive columns
LUMP = {3: (5, 10), 4: (4, 11), 5: (3, 12), 6: (3, 12), 7: (3, 12), 8: (4, 11), 9: (5, 10)}
# the bone: a short stick going down-left from the lump, with a knob at the end
BONE_PIXELS = [(5, 10), (4, 11), (4, 12), (3, 13), (2, 13), (2, 14), (3, 14), (1, 14), (1, 13)]


def draw(look):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    hi, mid, lo = look["meat"]
    for (x, y) in BONE_PIXELS:
        px[x, y] = (BONE_DARK if (x + y) % 2 else BONE) + (255,)
    for y, (l, r) in LUMP.items():
        for x in range(l, r + 1):
            edge = x in (l, r) or y in (min(LUMP), max(LUMP))
            if edge:
                px[x, y] = look["outline"] + (255,)
            elif y == 4 or x == l + 1:
                px[x, y] = look["fat"] + (255,) if y == 4 else hi + (255,)
            elif y >= 8 or x == r - 1:
                px[x, y] = lo + (255,)
            else:
                px[x, y] = mid + (255,)
    # where the bone enters the lump
    px[5, 9] = look["outline"] + (255,)
    px[4, 10] = look["outline"] + (255,)
    return img


def main():
    raw, cooked = draw(RAW), draw(COOKED)
    raw.save(f"{OUT}/raw_bunnidog_flesh.png")
    cooked.save(f"{OUT}/cooked_bunnidog_flesh.png")
    scale = 12
    sheet = Image.new("RGBA", (16 * scale * 2 + 12, 16 * scale), (58, 58, 64, 255))
    for i, img in enumerate((raw, cooked)):
        sheet.paste(img.resize((16 * scale, 16 * scale), Image.NEAREST), (i * (16 * scale + 12), 0), img.resize((16 * scale, 16 * scale), Image.NEAREST))
    sheet.save(PREVIEW)
    print("wrote", OUT, "and", PREVIEW)


if __name__ == "__main__":
    main()
