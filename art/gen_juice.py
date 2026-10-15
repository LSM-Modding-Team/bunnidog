#!/usr/bin/env python3
"""Generates the two juice item textures: src/main/resources/assets/bunnidogs/textures/item/{guava_juice,infused_guava_juice}.png (16x16).

Run from the project root:  python3 art/gen_juice.py     (needs Pillow)      Also writes art/juice_preview.png (12x).

Both are the same glass bottle with a cork. The plain guava juice is light green. The INFUSED juice (Phase 4f: a seed that was
dropped into the juice) is guava pink and has a tiny pale creature floating in it: two ears, a head, a dark eye.
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item"
PREVIEW = "art/juice_preview.png"

GLASS = (214, 232, 238)
GLASS_DARK = (150, 176, 188)
OUTLINE = (70, 84, 96)
CORK = (176, 132, 84)
CORK_DARK = (126, 90, 56)

PLAIN = dict(liquid=[(226, 242, 150), (184, 214, 92), (148, 188, 66)], creature=False)
INFUSED = dict(liquid=[(255, 190, 200), (240, 124, 160), (196, 80, 128)], creature=True)

# bottle silhouette: row -> (left, right) inclusive pixel columns of the OUTSIDE of the glass
ROWS = {
    1: (6, 9), 2: (6, 9), 3: (6, 9), 4: (6, 9),          # cork rows 1-2, neck rows 3-4
    5: (5, 10), 6: (4, 11), 7: (3, 12), 8: (3, 12), 9: (3, 12), 10: (3, 12), 11: (3, 12), 12: (3, 12), 13: (4, 11), 14: (5, 10),
}
LIQUID_TOP = 7


def draw(look):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    hi, mid, lo = look["liquid"]
    for y, (l, r) in ROWS.items():
        for x in range(l, r + 1):
            edge = x in (l, r) or y == 14 or (y == 1)
            if y in (1, 2):
                px[x, y] = (CORK_DARK if (x in (l, r) or y == 1) else CORK) + (255,)
            elif edge:
                px[x, y] = OUTLINE + (255,)
            elif y >= LIQUID_TOP:
                colour = hi if x == l + 1 else (lo if (x == r - 1 or y == 13) else mid)
                px[x, y] = colour + (255,)
            else:
                px[x, y] = (GLASS if x == l + 1 else GLASS_DARK) + (255,)
    # a surface line on top of the liquid
    l, r = ROWS[LIQUID_TOP]
    for x in range(l + 1, r):
        px[x, LIQUID_TOP] = hi + (255,)
    if look["creature"]:
        cream, ear, dark = (255, 244, 226), (255, 150, 176), (60, 30, 44)
        for (x, y, c) in [(6, 9, cream), (6, 10, ear), (9, 9, cream), (9, 10, ear),        # ears
                          (6, 11, cream), (7, 11, cream), (8, 11, cream), (9, 11, cream),   # head
                          (6, 12, cream), (7, 12, cream), (8, 12, cream), (9, 12, cream),
                          (7, 11, dark)]:                                                   # an eye
            px[x, y] = c + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    scale = 12
    sheet = Image.new("RGBA", (2 * (16 * scale + 8) + 8, 16 * scale + 16), (52, 52, 60, 255))
    for i, (name, look) in enumerate((("guava_juice", PLAIN), ("infused_guava_juice", INFUSED))):
        img = draw(look)
        img.save(os.path.join(OUT, name + ".png"))
        sheet.alpha_composite(img.resize((16 * scale, 16 * scale), Image.NEAREST), (8 + i * (16 * scale + 8), 8))
    sheet.save(PREVIEW)
    print("wrote 2 juice textures")


if __name__ == "__main__":
    main()
