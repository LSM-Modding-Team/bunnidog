#!/usr/bin/env python3
"""Generates the bunnidog seed item texture: src/main/resources/assets/bunnidogs/textures/item/seed.png (16x16).

Run from the project root:  python3 art/gen_seed.py     (needs Pillow)

A plump cream seed lying diagonally, with a groove down its length and a tiny green sprout at the tip (so it reads as
"plant me"). Also writes art/seed_preview.png (12x scale) for a quick look. One palette entry per look, like the guava:
quality tiers of the seed are not planned, so there is only one.
"""
import math
import os

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item"
PREVIEW = "art/seed_preview.png"

# body: (highlight, light, mid, dark, outline)
BODY = [(255, 244, 214), (238, 218, 172), (214, 186, 132), (176, 146, 96), (92, 66, 40)]
GROOVE = (150, 118, 76)
SPROUT = [(120, 190, 78), (68, 138, 50)]

CENTER = (7.4, 8.6)
LONG, SHORT = 6.6, 4.1          # semi-axes of the seed
ANGLE = math.radians(-38)       # long axis tilts from the lower left to the upper right


def draw():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = CENTER
    ca, sa = math.cos(ANGLE), math.sin(ANGLE)
    hi, light, mid, dark, outline = BODY

    inside = {}
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            u = dx * ca + dy * sa            # along the long axis
            v = -dx * sa + dy * ca           # across it
            # a seed is a little pointier at one end (the sprout end, +u): squeeze the short axis there
            taper = 1.0 - 0.22 * max(0.0, u) / LONG
            if (u / LONG) ** 2 + (v / (SHORT * taper)) ** 2 <= 1.0:
                inside[(x, y)] = (u, v)

    for (x, y), (u, v) in inside.items():
        dx, dy = x + 0.5 - cx, y + 0.5 - cy
        light_dir = (-dx * 0.6 - dy * 0.8) / LONG      # +1 towards the upper left, -1 towards the lower right
        if light_dir > 0.32:
            color = hi
        elif light_dir > -0.02:
            color = light
        elif light_dir > -0.34:
            color = mid
        else:
            color = dark
        px[x, y] = color + (255,)

    # the groove: a thin darker line along the long axis, not touching either end
    for (x, y), (u, v) in inside.items():
        if abs(v) < 0.55 and -LONG * 0.55 < u < LONG * 0.55:
            px[x, y] = GROOVE + (255,)

    # outline: every seed pixel that touches the outside
    for (x, y) in list(inside):
        if any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            px[x, y] = outline + (255,)

    # sprout: two little leaves growing from the pointed (upper right) end
    tip = max(inside, key=lambda p: inside[p][0])
    tx, ty = tip
    for (dx, dy, i) in ((0, -1, 1), (1, -1, 0), (1, -2, 0), (2, -2, 0), (0, -2, 1)):
        x, y = tx + dx, ty + dy
        if 0 <= x < 16 and 0 <= y < 16 and (x, y) not in inside:
            px[x, y] = SPROUT[i] + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    img = draw()
    img.save(os.path.join(OUT, "seed.png"))
    sheet = Image.new("RGBA", (16 * 12 + 16, 16 * 12 + 16), (52, 52, 60, 255))
    sheet.alpha_composite(img.resize((16 * 12, 16 * 12), Image.NEAREST), (8, 8))
    sheet.save(PREVIEW)
    print("wrote seed texture")


if __name__ == "__main__":
    main()
