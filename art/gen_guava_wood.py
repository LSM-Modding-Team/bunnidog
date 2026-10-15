#!/usr/bin/env python3
"""Generates the guava wood textures (Phase 7a): textures/block/guava_log.png (bark, the sides), guava_log_top.png (the cut end, rings)
and guava_leaves.png (opaque leaf clumps with see-through holes, pre-coloured: no biome tint is applied). All 16x16.
W1 (session 40) adds, AFTER the three above so their random stream (and files) stay byte-identical: guava_planks.png, stripped_guava_log.png
(the bare wood of a stripped log, grain running up) and stripped_guava_log_top.png (rings without the bark edge).

Run from the project root:  python3 art/gen_guava_wood.py     (needs Pillow)      Also writes art/guava_wood_preview.png (12x).
Deterministic (fixed seed), so re-running gives the same files.
"""
import random

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/guava_wood_preview.png"

# guava bark is smooth and reddish-grey-brown (the real tree peels in flakes); the heartwood is a pale pink-brown
BARK = [(150, 112, 80), (128, 94, 66), (106, 76, 54), (84, 60, 44)]
WOOD = [(214, 170, 128), (196, 150, 108), (176, 130, 92)]
LEAF = [(150, 200, 84), (118, 172, 66), (88, 140, 52), (60, 108, 42), (42, 80, 34)]


def log_side(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    cols = [rng.randrange(len(BARK)) for _ in range(16)]
    for x in range(16):
        for y in range(16):
            c = cols[x]
            if rng.random() < 0.18:
                c = min(len(BARK) - 1, max(0, c + rng.choice((-1, 1))))
            if y % 8 == 0 and x % 3 == 0:          # short horizontal flake marks
                c = len(BARK) - 1
            px[x, y] = BARK[c] + (255,)
    return img


def log_top(rng):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))        # square rings: it is a block
            if d >= 6.5:
                c = BARK[1] if (x + y) % 2 else BARK[2]
            elif d >= 5.5:
                c = BARK[3]
            else:
                ring = int(d) % 2
                c = WOOD[ring] if rng.random() > 0.12 else WOOD[2]
            px[x, y] = c + (255,)
    px[7, 7] = px[8, 8] = WOOD[2] + (255,)
    return img


def leaves(rng):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for x in range(16):
        for y in range(16):
            if rng.random() < 0.17:
                continue                                 # a hole
            shade = rng.choices(range(len(LEAF)), weights=[2, 4, 5, 3, 1])[0]
            px[x, y] = LEAF[shade] + (255,)
    for x in range(16):                                  # the border is always filled on two sides, so neighbouring blocks tile without gaps in a row
        px[x, 0] = px[x, 0] if px[x, 0][3] else LEAF[2] + (255,)
        px[0, x] = px[0, x] if px[0, x][3] else LEAF[2] + (255,)
    return img


def planks(rng):
    """Horizontal boards 4 px high with a darker seam between them and a little grain; two nail dots per board end."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for board in range(4):
        base = rng.randrange(len(WOOD))
        for x in range(16):
            for y in range(board * 4, board * 4 + 4):
                c = base
                if y == board * 4 + 3:                      # the seam under the board
                    c = len(WOOD) - 1
                elif rng.random() < 0.2:
                    c = min(len(WOOD) - 1, max(0, c + rng.choice((-1, 1))))
                col = WOOD[c]
                if y == board * 4 + 3:
                    col = tuple(max(0, v - 28) for v in col)
                px[x, y] = col + (255,)
        for x in (1, 14):                                   # nails
            px[x, board * 4 + 1] = tuple(max(0, v - 40) for v in WOOD[2]) + (255,)
    return img


def stripped_side(rng):
    """The bare wood under the bark: pale, with fine vertical grain (a column keeps its tone)."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    cols = [rng.randrange(len(WOOD)) for _ in range(16)]
    for x in range(16):
        for y in range(16):
            c = cols[x]
            if rng.random() < 0.15:
                c = min(len(WOOD) - 1, max(0, c + rng.choice((-1, 1))))
            px[x, y] = WOOD[c] + (255,)
    return img


def stripped_top(rng):
    """The cut end of a stripped log: square rings all the way to the edge (the bark ring is gone)."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            c = WOOD[int(d) % 2] if rng.random() > 0.12 else WOOD[2]
            if d >= 7:
                c = WOOD[2]
            px[x, y] = c + (255,)
    px[7, 7] = px[8, 8] = WOOD[2] + (255,)
    return img


def main():
    rng = random.Random(7)
    imgs = {"guava_log": log_side(rng), "guava_log_top": log_top(rng), "guava_leaves": leaves(rng)}
    imgs["guava_planks"] = planks(rng)
    imgs["stripped_guava_log"] = stripped_side(rng)
    imgs["stripped_guava_log_top"] = stripped_top(rng)
    for name, img in imgs.items():
        img.save(f"{OUT}/{name}.png")
    sheet = Image.new("RGBA", (len(imgs) * (16 * 12 + 2 * 12), 16 * 12), (60, 60, 70, 255))
    for i, name in enumerate(imgs):
        big = imgs[name].resize((16 * 12, 16 * 12), Image.NEAREST)
        sheet.paste(big, (i * (16 * 12 + 2 * 12), 0), big)
    sheet.save(PREVIEW)


if __name__ == "__main__":
    main()
