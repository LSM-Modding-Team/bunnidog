#!/usr/bin/env python3
"""Generates the guava item texture(s): src/main/resources/assets/bunnidogs/textures/item/<name>.png (16x16).

Run from the project root:  python3 art/gen_guava.py     (needs Pillow)

Every guava tier is the same fruit with another palette, so adding a tier (Phase 4e added golden, diamond, netherite,
nether star, beacon and dragon egg) is one new entry in TIERS (plus the constant in genetics/GuavaTier.java). Also writes art/guava_preview.png (all tiers, 12x scale) for a quick look.
"""
import math
import os

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/item"
PREVIEW = "art/guava_preview.png"

# skin: (highlight, light, mid, dark, outline). flesh/leaf/stem are shared unless a tier overrides them.
# Optional per tier: speckle = colour of the skin speckles (default: the skin's mid tone),
#                    sparkle = pixels drawn last in white-ish (x, y, colour), for the absurd tiers.
# The keys are the item ids of genetics/GuavaTier.java (the self-check requires one entry per tier, in that order).
TIERS = {
    "guava": dict(
        skin=[(226, 238, 150), (184, 214, 92), (152, 192, 72), (112, 152, 56), (62, 88, 34)],
    ),
    "golden_guava": dict(
        skin=[(255, 244, 156), (252, 214, 74), (228, 170, 32), (178, 118, 22), (108, 68, 20)],
        speckle=(255, 236, 120),
    ),
    "diamond_guava": dict(
        skin=[(226, 255, 252), (132, 240, 236), (62, 202, 212), (36, 150, 172), (18, 88, 112)],
        speckle=(236, 255, 255),
    ),
    "netherite_guava": dict(
        skin=[(132, 116, 116), (94, 80, 82), (68, 56, 60), (46, 38, 42), (22, 18, 22)],
        speckle=(120, 74, 70),
    ),
    "nether_star_guava": dict(
        skin=[(255, 255, 255), (244, 244, 224), (218, 218, 190), (172, 172, 150), (98, 98, 86)],
        speckle=(255, 246, 170),
        sparkle=[(2, 4, (255, 255, 255)), (1, 4, (255, 250, 200)), (3, 4, (255, 250, 200)),
                 (2, 3, (255, 250, 200)), (2, 5, (255, 250, 200))],
    ),
    "beacon_guava": dict(
        skin=[(232, 255, 255), (152, 218, 252), (92, 154, 238), (70, 92, 204), (30, 30, 84)],
        speckle=(210, 246, 255),
        sparkle=[(13, 5, (255, 255, 255)), (12, 5, (200, 246, 255)), (14, 5, (200, 246, 255)),
                 (13, 4, (200, 246, 255)), (13, 6, (200, 246, 255))],
    ),
    "dragon_egg_guava": dict(
        skin=[(122, 76, 172), (66, 34, 98), (36, 18, 58), (22, 10, 36), (8, 4, 14)],
        speckle=(206, 64, 224),
        sparkle=[(2, 12, (226, 96, 240)), (13, 12, (226, 96, 240)), (2, 5, (170, 60, 220))],
    ),
    # session 7: the command block guava. Command-block orange-brown skin, the green "conditional" glow as sparkles.
    "command_block_guava": dict(
        skin=[(236, 172, 104), (204, 124, 62), (166, 90, 44), (118, 62, 34), (56, 30, 20)],
        speckle=(240, 214, 160),
        sparkle=[(13, 12, (110, 236, 120)), (12, 12, (70, 190, 90)), (14, 12, (70, 190, 90)),
                 (13, 11, (70, 190, 90)), (13, 13, (70, 190, 90)), (2, 12, (110, 236, 120))],
    ),
}
LEAF = [(96, 168, 64), (58, 118, 44)]
STEM = (104, 74, 42)
BLOSSOM = (86, 66, 40)


def draw(palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    hi, light, mid, dark, outline = palette["skin"]
    cx, cy, r = 7.6, 9.2, 5.6
    inside = set()
    for y in range(16):
        for x in range(16):
            # slightly squashed circle: a guava is a little wider than tall
            dx, dy = (x + 0.5 - cx) / 1.05, (y + 0.5 - cy)
            if math.hypot(dx, dy) <= r:
                inside.add((x, y))
    for (x, y) in inside:
        dx, dy = x + 0.5 - cx, y + 0.5 - cy
        light_dir = (-dx * 0.6 - dy * 0.8) / r          # +1 towards the upper left, -1 towards the lower right
        if light_dir > 0.55:
            color = hi
        elif light_dir > 0.05:
            color = light
        elif light_dir > -0.45:
            color = mid
        else:
            color = dark
        px[x, y] = color + (255,)
    for (x, y) in list(inside):
        if any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            px[x, y] = outline + (255,)
    # skin speckles
    speckle = palette.get("speckle", mid)
    for (x, y) in ((6, 8), (9, 10), (7, 12), (10, 8)):
        if (x, y) in inside and px[x, y][:3] != outline:
            px[x, y] = speckle + (255,)
    # blossom end (bottom) and stem + leaf (top)
    px[7, 13] = BLOSSOM + (255,)
    px[8, 13] = BLOSSOM + (255,)
    px[8, 4] = STEM + (255,)
    px[8, 3] = STEM + (255,)
    for (x, y, i) in ((9, 3, 0), (10, 2, 0), (11, 2, 1), (10, 3, 1), (9, 4, 1), (11, 3, 0)):
        px[x, y] = LEAF[i] + (255,)
    # sparkles of the absurd tiers (drawn last, may sit outside the fruit)
    for (x, y, colour) in palette.get("sparkle", ()):
        px[x, y] = colour + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    names = list(TIERS)
    sheet = Image.new("RGBA", (16 * len(names) * 12 + 8 * (len(names) + 1), 16 * 12 + 16), (52, 52, 60, 255))
    for i, name in enumerate(names):
        img = draw(TIERS[name])
        img.save(os.path.join(OUT, name + ".png"))
        big = img.resize((16 * 12, 16 * 12), Image.NEAREST)
        sheet.alpha_composite(big, (8 + i * (16 * 12 + 8), 8))
    sheet.save(PREVIEW)
    print("wrote", len(names), "guava texture(s):", ", ".join(names))


if __name__ == "__main__":
    main()
