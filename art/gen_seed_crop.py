#!/usr/bin/env python3
"""Generates the planted-seed crop textures: src/main/resources/assets/bunnidogs/textures/block/seed_crop_stage0..7.png (16x16).

Run from the project root:  python3 art/gen_seed_crop.py     (needs Pillow)

The crop is an EMBRYO (session 6, the user's wish: "the stem should look like an umbilical cord, and the thing at the top
should look like a fetus developing, directly attached, without a capsule"). It has 8 ages (0-7) and 8 pictures, one per
age, so every step of growth and every bone meal is visible:

  age 0  a bead on the cord                      age 4  ear buds, limbs, a big dark eye
  age 1  a comma: head bulb and a tail           age 5  small ears, fingers, a face
  age 2  an eye spot appears, tail curls         age 6  pale baby skin, ears up, eye closed
  age 3  head and body, limb buds                age 7  RIPE: ears up, eye OPEN, rosy cheek (the readable "done" cue)

The stem is an umbilical cord: a twisted two-pixel rope with a red and a blue vessel, wobbling a little, and it runs
straight into the belly of the fetus (no pod, no capsule). The skin goes from saturated embryo pink through fetus pink to
pale bunnidog cream. The model is minecraft:block/cross (two crossing planes), so ONE embryo stands in the middle of the
block, seen in profile, facing left. Also writes art/seed_crop_preview.png (all 8 ages, 10x).
"""
import math
import os

from PIL import Image

OUT = "src/main/resources/assets/bunnidogs/textures/block"
PREVIEW = "art/seed_crop_preview.png"
STAGES = 8

# ---- cord (umbilical): pale twisted rope, one red and one blue vessel
CORD_L = (222, 208, 226)
CORD_M = (178, 150, 192)
CORD_R = (214, 84, 102)
CORD_B = (96, 104, 188)

# ---- skin: (main, highlight, shade, outline) for the three development looks
SKIN_EMBRYO = ((234, 112, 128), (252, 172, 172), (186, 74, 98), (128, 50, 78))
SKIN_FETUS = ((242, 148, 150), (255, 202, 192), (202, 104, 118), (140, 64, 84))
SKIN_BABY = ((250, 214, 196), (255, 242, 226), (226, 162, 156), (150, 82, 92))
EYE = (44, 22, 36)
EYE_WHITE = (255, 255, 255)
EAR_INNER = (238, 118, 148)
CHEEK = (246, 150, 160)

# Per age: skin look, head radius, body radius (0 = no body yet), where the belly ends (the cord starts there), ear height
# in pixels, eye kind, and how many limb nubs. The embryo grows upwards from a cord of roughly constant length, so early
# ages are a bead on a long stalk and the ripe one is a big-headed baby with ears. Pixels: 16x16, y grows downwards, ground y = 15.
AGES = [
    dict(skin=SKIN_EMBRYO, hr=1.7, br=0.0, belly=9.6, ears=0, eye=None, limbs=0, tail=0),
    dict(skin=SKIN_EMBRYO, hr=2.1, br=1.2, belly=9.9, ears=0, eye=None, limbs=0, tail=1),
    dict(skin=SKIN_EMBRYO, hr=2.5, br=1.6, belly=10.2, ears=0, eye="spot", limbs=0, tail=2),
    dict(skin=SKIN_EMBRYO, hr=2.8, br=1.9, belly=10.6, ears=0, eye="spot", limbs=1, tail=2),
    dict(skin=SKIN_FETUS, hr=3.1, br=2.1, belly=10.8, ears=2, eye="dot", limbs=2, tail=2),
    dict(skin=SKIN_FETUS, hr=3.3, br=2.2, belly=11.0, ears=2, eye="dot", limbs=3, tail=2),
    dict(skin=SKIN_BABY, hr=3.5, br=2.3, belly=11.0, ears=3, eye="closed", limbs=3, tail=2),
    dict(skin=SKIN_BABY, hr=3.6, br=2.4, belly=11.2, ears=3, eye="open", limbs=3, tail=2),
]


def inside(x, y, cx, cy, r):
    return ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) <= r * r


def geometry(age):
    """Centres of head and body for an age: the body sits low and to the right, the head is big and up-left of it."""
    p = AGES[age]
    hr, br, belly = p["hr"], p["br"], p["belly"]
    bcx = 9.0 + age / 7.0
    if br > 0:
        bcy = belly - br
        hcx = bcx - (0.9 * hr + 0.1 * br)
        hcy = bcy - 0.78 * hr
    else:
        bcy = None
        hcx = 8.0
        hcy = belly - hr
    return hcx, hcy, bcx, bcy


def draw(age):
    p = AGES[age]
    main, hi, shade, outline = p["skin"]
    hr, br = p["hr"], p["br"]
    hcx, hcy, bcx, bcy = geometry(age)
    grid = {}                                   # (x, y) -> colour, only the creature (no cord)

    def put(x, y, colour):
        if 0 <= x < 16 and 0 <= y < 16:
            grid[(x, y)] = colour

    head_px = {(x, y) for y in range(16) for x in range(16) if inside(x, y, hcx, hcy, hr)}
    body_px = {(x, y) for y in range(16) for x in range(16) if br > 0 and inside(x, y, bcx, bcy, br)}
    shape = head_px | body_px

    # tail: a short curl off the lower right of the body (part of the silhouette, so it gets an outline)
    if p["tail"] and br > 0:
        tx = int(bcx + br) - 1
        ty = int(bcy + br * 0.5)
        for i in range(p["tail"]):
            shape.add((tx + 1 + i, ty + (1 if i == 1 else 0)))

    # ears: two 3-pixel-wide stalks standing on the head (outline on both sides, pink in the middle); the right one
    # flops over at its tip when it is tall enough
    ear_inner = set()
    ears = p["ears"]
    if ears:
        for centre, is_right in ((hcx - hr * 0.55, False), (hcx + hr * 0.5, True)):
            ex = int(round(centre)) - 1
            mid_top = min(y for (x, y) in head_px if x == ex + 1)       # first head row under the middle column
            top_row = mid_top - ears
            for col in (ex, ex + 1, ex + 2):
                col_top = min([y for (x, y) in head_px if x == col] or [mid_top])
                for y in range(top_row, col_top):
                    lean = 1 if (is_right and ears >= 3 and y == top_row) else 0
                    shape.add((col + lean, y))
                    if col == ex + 1 and y > top_row:
                        ear_inner.add((col + lean, y))

    # fill: main colour; highlight on the upper-left of the head; shade along the bottom and right edges
    for (x, y) in shape:
        colour = main
        if (x, y) in head_px and (x + 0.5 - hcx) + (y + 0.5 - hcy) < -hr * 0.6:
            colour = hi
        elif (x, y + 1) not in shape or (x + 1, y) not in shape:
            colour = shade
        put(x, y, colour)

    # pink inside of the ears
    for (x, y) in ear_inner:
        put(x, y, EAR_INNER)

    # outline: every creature pixel that touches empty space (4-neighbourhood) becomes the outline colour
    for (x, y) in list(shape):
        if any((x + dx, y + dy) not in shape for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            put(x, y, outline)

    # limb nubs: hands at the chin, a foot at the front of the belly
    limbs = p["limbs"]
    if limbs and br > 0:
        hx = int(hcx + hr * 0.1)
        hy = int(hcy + hr * 0.85)
        put(hx, hy, hi)
        put(hx, hy + 1, outline)
        put(hx - 1, hy, outline)
        if limbs >= 2:
            fx = int(bcx - br * 0.5)
            fy = int(bcy + br * 0.6)
            put(fx, fy, hi)
            put(fx - 1, fy, outline)
            put(fx, fy + 1, outline)
        if limbs >= 3:
            put(hx + 1, hy, hi)
            put(hx + 1, hy + 1, outline)

    # the face (left side of the head)
    ex = int(hcx - hr * 0.6)
    ey = int(hcy - hr * 0.15)
    eye = p["eye"]
    if eye == "spot":
        put(ex, ey, EYE)
    elif eye == "dot":
        put(ex, ey, EYE)
        put(ex, ey + 1, EYE)
    elif eye == "closed":
        put(ex, ey + 1, EYE)
        put(ex + 1, ey + 1, EYE)
    elif eye == "open":
        put(ex, ey, EYE_WHITE)
        put(ex + 1, ey, EYE)
        put(ex, ey + 1, EYE)
        put(ex + 1, ey + 1, EYE)
        put(ex + 1, ey + 2, CHEEK)                               # rosy cheek under the eye: the "ripe" cue
        put(ex + 2, ey + 2, CHEEK)
        put(ex, ey + 3, outline)                                 # a tiny mouth
        put(ex + 1, ey + 3, outline)

    # ---- the cord: from the belly down to the ground, a twisted rope with a red and a blue vessel
    if br > 0:
        attach_x = int(round(bcx - 1.0))
        attach_y = int(bcy + br)
    else:
        attach_x = int(round(hcx - 1.0))
        attach_y = int(hcy + hr)
    cord = {}
    rows = list(range(attach_y - 1, 16))                 # from just inside the belly to the ground
    for y in rows:
        d = y - (attach_y - 1)                           # 0 at the belly, grows downwards
        t = d / max(1, len(rows) - 1)                    # 0 at the belly .. 1 at the ground
        base = attach_x * (1 - t) + 7 * t
        wobble = math.sin(d * 1.3) * 1.0 * math.sin(math.pi * t)
        x0 = int(round(base + wobble))
        left = [CORD_L, CORD_L, CORD_M, CORD_M][d % 4]
        right = [CORD_M, CORD_M, CORD_L, CORD_L][d % 4]
        if d % 4 == 1:
            left = CORD_R
        if d % 4 == 3:
            right = CORD_B
        cord[(x0, y)] = left
        cord[(x0 + 1, y)] = right
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for (x, y), colour in cord.items():
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = colour + (255,)
    for (x, y), colour in grid.items():                  # the creature is painted over the top of the cord (the belly wins)
        px[x, y] = colour + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    scale = 10
    gap = 8
    sheet = Image.new("RGBA", (STAGES * (16 * scale + gap) + gap, 16 * scale + 2 * gap), (52, 52, 60, 255))
    for age in range(STAGES):
        img = draw(age)
        img.save(os.path.join(OUT, "seed_crop_stage%d.png" % age))
        sheet.alpha_composite(img.resize((16 * scale, 16 * scale), Image.NEAREST), (gap + age * (16 * scale + gap), gap))
    sheet.save(PREVIEW)
    print("wrote %d seed crop textures" % STAGES)


if __name__ == "__main__":
    main()
