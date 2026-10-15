#!/usr/bin/env python3
"""Generates the Starlit Dust picture: src/main/resources/assets/bunnidogs/textures/item/starlit_dust.png (16x16), plus its client item and model.

Run from the project root:  python3 art/gen_dust.py     (needs Pillow)      Also writes art/dust_preview.png (12x).

A small heap of violet-blue grains with pale-gold sparkles and two four-point stars above it (the starborn line lives on the End islands).
The shape is a character map, so a change is one edit.
"""
import json
import os
from PIL import Image

ROOT = "src/main/resources/assets/bunnidogs"
OUT = ROOT + "/textures/item"
PREVIEW = "art/dust_preview.png"

# '.' empty, 'd' dark grain, 'm' mid grain, 'l' light grain, 's' gold sparkle, 'w' white sparkle core
SHAPE = [
    "................",
    "....s...........",
    "...sws.......s..",
    "....s.......swS.",
    "............s...",
    "................",
    "......dmd.......",
    ".....dmlmd..s...",
    "....dmmlmmd.....",
    "...dmmlmlmmd....",
    "..dmmmmmmmmmd...",
    "..ddmmmmmmddd...",
    "...dddddddd.....",
    "................",
    "................",
    "................",
]
COL = {
    "d": (74, 56, 150),
    "m": (122, 100, 214),
    "l": (176, 160, 244),
    "s": (250, 226, 130),
    "w": (255, 255, 244),
    "S": (250, 226, 130),
}


def draw():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(SHAPE):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = COL[ch] + (255,)
    return img


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    img = draw()
    img.save(f"{OUT}/starlit_dust.png")
    write_json(f"{ROOT}/items/starlit_dust.json", {"model": {"type": "minecraft:model", "model": "bunnidogs:item/starlit_dust"}})
    write_json(f"{ROOT}/models/item/starlit_dust.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "bunnidogs:item/starlit_dust"}})
    scale = 12
    big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
    sheet = Image.new("RGBA", (16 * scale, 16 * scale), (58, 58, 64, 255))
    sheet.paste(big, (0, 0), big)
    sheet.save(PREVIEW)
    print("wrote", OUT, "and", PREVIEW)


if __name__ == "__main__":
    main()
