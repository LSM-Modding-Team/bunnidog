#!/usr/bin/env python3
"""Generates the Mutagen picture: src/main/resources/assets/bunnidogs/textures/item/mutagen.png (16x16), plus its client item and model.

Run from the project root:  python3 art/gen_mutagen.py     (needs Pillow)      Also writes art/mutagen_preview.png (12x).

A small round-bellied vial with a cork, glass outline and a liquid that shades from toxic green at the top to violet at the bottom, with a few
bubbles. The shape is a character map, so a change is one edit.
"""
import json
import os
from PIL import Image

ROOT = "src/main/resources/assets/bunnidogs"
OUT = ROOT + "/textures/item"
PREVIEW = "art/mutagen_preview.png"

# '.' empty, 'C' cork, 'o' glass outline, 'g' empty glass, 'L' liquid (shaded by row), 'h' glass highlight, 'b' bubble
SHAPE = [
    "................",
    ".....CCCC.......",
    ".....CCCC.......",
    ".....oggo.......",
    ".....ogho.......",
    "....oggggo......",
    "...oghLLLLo.....",
    "..ohLLLLLLLo....",
    "..oLLbLLLLLo....",
    "..oLLLLLbLLo....",
    "..oLLLLLLLLo....",
    "..oLbLLLLLLo....",
    "...oLLLLLLo.....",
    "....oooooo......",
    "................",
    "................",
]
CORK = (150, 108, 64)
CORK_DARK = (112, 78, 44)
OUTLINE = (58, 66, 76)
GLASS = (206, 226, 232)
HIGHLIGHT = (255, 255, 255)
BUBBLE = (226, 255, 220)
GREEN = (120, 236, 84)
TEAL = (64, 214, 190)               # a middle stop, so green -> violet does not pass through muddy grey
VIOLET = (150, 64, 204)


def mix(t):
    a, b, u = (GREEN, TEAL, t * 2) if t < 0.5 else (TEAL, VIOLET, t * 2 - 1)
    return tuple(round(a[i] + (b[i] - a[i]) * u) for i in range(3))


def draw():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    rows = [y for y, row in enumerate(SHAPE) if "L" in row]
    first, last = min(rows), max(rows)
    for y, row in enumerate(SHAPE):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "C":
                px[x, y] = (CORK_DARK if y == 2 else CORK) + (255,)
            elif ch == "o":
                px[x, y] = OUTLINE + (255,)
            elif ch == "g":
                px[x, y] = GLASS + (255,)
            elif ch == "h":
                px[x, y] = HIGHLIGHT + (255,)
            elif ch == "b":
                px[x, y] = BUBBLE + (255,)
            elif ch == "L":
                px[x, y] = mix((y - first) / max(1, last - first)) + (255,)
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    img = draw()
    img.save(f"{OUT}/mutagen.png")
    write_json(f"{ROOT}/items/mutagen.json", {"model": {"type": "minecraft:model", "model": "bunnidogs:item/mutagen"}})
    write_json(f"{ROOT}/models/item/mutagen.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "bunnidogs:item/mutagen"}})
    scale = 12
    big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
    sheet = Image.new("RGBA", (16 * scale, 16 * scale), (58, 58, 64, 255))
    sheet.paste(big, (0, 0), big)
    sheet.save(PREVIEW)
    print("wrote", OUT, "and", PREVIEW)


if __name__ == "__main__":
    main()
