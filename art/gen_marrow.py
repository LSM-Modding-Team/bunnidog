#!/usr/bin/env python3
"""Generates the Mutant Marrow picture (chunk D6b): src/main/resources/assets/bunnidogs/textures/item/mutant_marrow.png (16x16), plus its client item and model.

Run from the project root:  python3 art/gen_marrow.py     (needs Pillow)      Also writes art/marrow_preview.png (12x).

A short bone lying diagonally, snapped open in the middle so its core shows: the core glows from toxic green to violet, the same two colours as the Mutagen
(art/gen_mutagen.py), so the player reads "marrow -> Mutagen" at a glance. The shape is a character map, so a change is a one-character edit.
"""
import json
import os

from PIL import Image

ROOT = "src/main/resources/assets/bunnidogs"
OUT = ROOT + "/textures/item"
PREVIEW = "art/marrow_preview.png"
NAME = "mutant_marrow"

# '.' empty, 'o' outline, 'a' bone, 'b' shaded bone, 'w' bone highlight, 'M' marrow (colour follows the position along the bone)
SHAPE = [
    "................",
    "..........oo.oo.",
    ".........oaawaao",
    ".........oawaaao",
    "..........oaaao.",
    ".........oabbo..",
    "........oMMMo...",
    ".......oMMMMo...",
    "......oMMMMo....",
    ".....oMMMo......",
    "....obaabo......",
    "...oaaaawo......",
    "..oawaaaao......",
    "..ooaaoooo......",
    "................",
    "................",
]
OUTLINE = (74, 64, 56)
BONE = (240, 232, 212)
BONE_SHADE = (200, 188, 160)
BONE_LIGHT = (255, 252, 240)
GREEN = (120, 236, 84)
TEAL = (64, 214, 190)               # a middle stop, so green -> violet does not pass through muddy grey
VIOLET = (150, 64, 204)


def mix(t):
    a, b, u = (GREEN, TEAL, t * 2) if t < 0.5 else (TEAL, VIOLET, t * 2 - 1)
    return tuple(round(a[i] + (b[i] - a[i]) * u) for i in range(3))


def draw():
    assert len(SHAPE) == 16 and all(len(r) == 16 for r in SHAPE), "a texture is 16x16 characters"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cells = [(x, y) for y, row in enumerate(SHAPE) for x, ch in enumerate(row) if ch == "M"]
    xs = [x for x, _ in cells]
    first, last = min(xs), max(xs)
    for y, row in enumerate(SHAPE):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "o":
                px[x, y] = OUTLINE + (255,)
            elif ch == "a":
                px[x, y] = BONE + (255,)
            elif ch == "b":
                px[x, y] = BONE_SHADE + (255,)
            elif ch == "w":
                px[x, y] = BONE_LIGHT + (255,)
            elif ch == "M":
                px[x, y] = mix((last - x) / max(1, last - first)) + (255,)   # violet at the lower left, green at the upper right
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    img = draw()
    os.makedirs(OUT, exist_ok=True)
    img.save(os.path.join(OUT, NAME + ".png"))
    write_json(os.path.join(ROOT, "items", NAME + ".json"), {"model": {"type": "minecraft:model", "model": f"bunnidogs:item/{NAME}"}})
    write_json(os.path.join(ROOT, "models/item", NAME + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"bunnidogs:item/{NAME}"}})
    scale = 12
    big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
    sheet = Image.new("RGBA", (16 * scale + 12, 16 * scale + 12), (58, 58, 64, 255))
    sheet.paste(big, (6, 6), big)
    sheet.save(PREVIEW)
    print("wrote", NAME, "and", PREVIEW)


if __name__ == "__main__":
    main()
