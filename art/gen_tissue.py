#!/usr/bin/env python3
"""Generates the Tissue Sample picture: src/main/resources/assets/bunnidogs/textures/item/tissue_sample.png (16x16), plus its client item and model.

Run from the project root:  python3 art/gen_tissue.py     (needs Pillow)      Also writes art/tissue_preview.png (12x).

A slim test tube leaning to the right (so it does not read as the round Mutagen vial): a blue stopper, a glass outline, a rose-pink sample
that is darker at the bottom, a white label with one dark stripe and a tuft of tan fur floating in the liquid. The shape is a character map.
"""
import json
import os
from PIL import Image

ROOT = "src/main/resources/assets/bunnidogs"
OUT = ROOT + "/textures/item"
PREVIEW = "art/tissue_preview.png"

# '.' empty, 'S' stopper, 'o' glass outline, 'g' empty glass, 'L' sample (shaded by row), 'h' glass highlight, 'l' label, 'k' label stripe, 'f' fur
SHAPE = [
    "..........SS....",
    ".........SSSS...",
    ".........oggo...",
    "........oghgo...",
    "........ogglo...",
    ".......oLLkko...",
    ".......oLLllo...",
    "......oLLfLLo...",
    "......oLfffLo...",
    ".....oLLLfLLo...",
    ".....oLLLLLo....",
    "....oLLLLLLo....",
    "....oLLLLLo.....",
    ".....ooooo......",
    "................",
    "................",
]
STOPPER = (70, 110, 200)
STOPPER_DARK = (46, 74, 150)
OUTLINE = (58, 66, 76)
GLASS = (206, 226, 232)
HIGHLIGHT = (255, 255, 255)
LABEL = (244, 240, 226)
STRIPE = (96, 70, 56)
FUR = (200, 156, 104)
ROSE_TOP = (255, 150, 170)
ROSE_BOTTOM = (206, 58, 104)


def mix(t):
    return tuple(round(ROSE_TOP[i] + (ROSE_BOTTOM[i] - ROSE_TOP[i]) * t) for i in range(3))


def draw():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    rows = [y for y, row in enumerate(SHAPE) if "L" in row]
    first, last = min(rows), max(rows)
    for y, row in enumerate(SHAPE):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "S":
                px[x, y] = (STOPPER_DARK if y == 0 else STOPPER) + (255,)
            elif ch == "o":
                px[x, y] = OUTLINE + (255,)
            elif ch == "g":
                px[x, y] = GLASS + (255,)
            elif ch == "h":
                px[x, y] = HIGHLIGHT + (255,)
            elif ch == "l":
                px[x, y] = LABEL + (255,)
            elif ch == "k":
                px[x, y] = STRIPE + (255,)
            elif ch == "f":
                px[x, y] = FUR + (255,)
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
    img.save(f"{OUT}/tissue_sample.png")
    write_json(f"{ROOT}/items/tissue_sample.json", {"model": {"type": "minecraft:model", "model": "bunnidogs:item/tissue_sample"}})
    write_json(f"{ROOT}/models/item/tissue_sample.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "bunnidogs:item/tissue_sample"}})
    scale = 12
    big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
    sheet = Image.new("RGBA", (16 * scale, 16 * scale), (58, 58, 64, 255))
    sheet.paste(big, (0, 0), big)
    sheet.save(PREVIEW)
    print("wrote", OUT, "and", PREVIEW)


if __name__ == "__main__":
    main()
