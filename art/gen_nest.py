#!/usr/bin/env python3
"""Generates the Nest textures (H1, session 41): textures/block/nest_top.png (a ring of woven straw round a hollow lined with down)
and textures/block/nest_side.png (woven straw bands). Both 16x16. The bottom of the block reuses guava_planks.png (see the block model).

Run from the project root:  python3 art/gen_nest.py     (needs Pillow)      Also writes art/nest_preview.png (12x).
Deterministic (fixed seed), so re-running gives the same files.
"""
import math
import random

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/nest_preview.png"

STRAW = [(226, 196, 106), (208, 174, 84), (186, 150, 66), (150, 118, 52)]
HOLLOW = [(104, 80, 46), (124, 96, 54)]
DOWN = [(240, 232, 214), (222, 212, 188)]


def side(rng):
    """Woven straw: horizontal bands 4 px high, each a little darker in the gaps, with short vertical stalks crossing them."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        band = y // 4
        for x in range(16):
            base = STRAW[(band + (x // 4 + band) % 2) % 3]
            if y % 4 == 3:
                base = STRAW[3]  # the gap under each band
            elif rng.random() < 0.22:
                base = STRAW[rng.randrange(3)]
            px[x, y] = base + (255,)
    return img


def top(rng):
    """A rounded ring of straw (outer radius 8, inner 4.2) with a hollow lined with brown and a few tufts of down."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 4.6:
                # the rim: straw, darker on the outer edge, with a lighter twist
                c = STRAW[0] if rng.random() < 0.4 else STRAW[1]
                if d > 7.2:
                    c = STRAW[2] if rng.random() < 0.7 else STRAW[3]
                elif (x + y) % 5 == 0:
                    c = STRAW[2]
            else:
                c = HOLLOW[0] if rng.random() < 0.6 else HOLLOW[1]
                if d < 3.0 and rng.random() < 0.3:
                    c = DOWN[rng.randrange(2)]
            px[x, y] = c + (255,)
    # two feathers of down in the hollow
    for (x, y) in ((6, 7), (9, 8), (8, 6)):
        px[x, y] = DOWN[0] + (255,)
    return img


def main():
    rng = random.Random(41)
    s = side(rng)
    t = top(rng)
    s.save(f"{OUT}/nest_side.png")
    t.save(f"{OUT}/nest_top.png")
    plank = Image.open(f"{OUT}/guava_planks.png").convert("RGBA")
    scale = 12
    sheet = Image.new("RGBA", (16 * scale * 3 + 40, 16 * scale + 20), (60, 60, 64, 255))
    for i, im in enumerate((t, s, plank)):
        sheet.paste(im.resize((16 * scale, 16 * scale), Image.NEAREST), (10 + i * (16 * scale + 10), 10))
    sheet.save(PREVIEW)
    print("wrote", f"{OUT}/nest_side.png", f"{OUT}/nest_top.png", PREVIEW)


if __name__ == "__main__":
    main()
