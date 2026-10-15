#!/usr/bin/env python3
"""Generates the guava tree textures (Phase 4g): src/main/resources/assets/bunnidogs/textures/block/guava_fruit_stage0..2.png and guava_sapling.png (16x16).

Run from the project root:  python3 art/gen_guava_tree.py     (needs Pillow)      Also writes art/guava_tree_preview.png (12x).

The fruit hangs from the TOP of its block (a short stem, then the guava), in three ages that match the item's colours (art/gen_guava.py):
0 small and dark green, 1 bigger and lighter, 2 ripe: yellow with a pink blush and speckles (the picture that tells you it can be picked).
The sapling is a thin brown stem with three pairs of leaves. Both are drawn for minecraft:block/cross (two crossing planes).
"""
import os

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/guava_tree_preview.png"

STEM = [(94, 120, 52), (62, 88, 34)]
LEAF = [(120, 176, 66), (84, 138, 48), (52, 96, 36)]
BARK = [(142, 104, 64), (98, 68, 40)]

# per age: radius, skin (highlight, light, mid, dark, outline), speckle colour or None, blush or False
AGES = [
    dict(r=3, skin=[(176, 206, 104), (132, 176, 74), (104, 148, 58), (74, 112, 44), (44, 70, 30)], speckle=None, blush=False),
    dict(r=4, skin=[(226, 238, 150), (184, 214, 92), (152, 192, 72), (112, 152, 56), (62, 88, 34)], speckle=(120, 160, 60), blush=False),
    dict(r=5, skin=[(255, 246, 170), (246, 226, 100), (226, 198, 70), (182, 150, 48), (110, 82, 30)], speckle=(196, 164, 56), blush=True),
]


def fruit(age):
    p = AGES[age]
    r = p["r"]
    hi, light, mid, dark, outline = p["skin"]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 7.5, 4.5 + r
    # stem and a leaf at the top (the fruit hangs from the leaves above)
    for y in range(0, 5):
        px[7, y] = STEM[0] + (255,)
        px[8, y] = STEM[1] + (255,)
    px[9, 1] = LEAF[1] + (255,)
    px[10, 1] = LEAF[2] + (255,)
    px[6, 2] = LEAF[0] + (255,)
    for y in range(16):
        for x in range(16):
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d <= r + 0.2:
                if d > r - 0.9:
                    colour = outline
                elif (x + 0.5 - cx) + (y + 0.5 - cy) < -r * 0.55:
                    colour = hi
                elif (x + 0.5 - cx) + (y + 0.5 - cy) > r * 0.75:
                    colour = dark
                elif (x + 0.5 - cx) + (y + 0.5 - cy) > r * 0.25:
                    colour = mid
                else:
                    colour = light
                px[x, y] = colour + (255,)
    if p["speckle"]:
        for (dx, dy) in [(-1, 0), (1, 1), (0, -2), (2, -1), (-2, 2)][: r - 1]:
            x, y = int(cx + dx), int(cy + dy)
            if px[x, y][3] == 255 and px[x, y][:3] not in (outline, hi):
                px[x, y] = p["speckle"] + (255,)
    if p["blush"]:
        for (dx, dy) in [(1, 2), (2, 1), (2, 2), (1, 3)]:
            x, y = int(cx + dx), int(cy + dy)
            if px[x, y][3] == 255 and px[x, y][:3] != outline:
                px[x, y] = (244, 150, 140, 255)
    # a dark calyx dot at the bottom of the fruit
    px[int(cx), int(cy + r - 1)] = outline + (255,)
    return img


def sapling():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(7, 16):
        px[7, y] = BARK[0] + (255,)
        px[8, y] = BARK[1] + (255,)
    leaves = [(5, 12, 6, 12), (4, 11, 6, 11), (9, 10, 11, 10), (9, 9, 12, 9), (6, 8, 7, 8), (8, 8, 9, 8), (6, 7, 9, 7), (7, 6, 8, 6)]
    for (x0, y0, x1, y1) in leaves:
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                px[x, y] = LEAF[(x + y) % 3] + (255,)
    for (x, y) in [(3, 11), (12, 9), (7, 5), (8, 5)]:
        px[x, y] = LEAF[2] + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    scale = 12
    imgs = [("guava_fruit_stage%d" % a, fruit(a)) for a in range(3)] + [("guava_sapling", sapling())]
    sheet = Image.new("RGBA", (len(imgs) * (16 * scale + 8) + 8, 16 * scale + 16), (52, 52, 60, 255))
    for i, (name, img) in enumerate(imgs):
        img.save(os.path.join(OUT, name + ".png"))
        sheet.alpha_composite(img.resize((16 * scale, 16 * scale), Image.NEAREST), (8 + i * (16 * scale + 8), 8))
    sheet.save(PREVIEW)
    print("wrote %d guava tree textures" % len(imgs))


if __name__ == "__main__":
    main()
