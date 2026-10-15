#!/usr/bin/env python3
"""Generates the three body-part materials of the drops (chunk D4): sinew, whisker, fang.

  src/main/resources/assets/bunnidogs/textures/item/{sinew,whisker,fang}.png   (16x16)
  src/main/resources/assets/bunnidogs/items/{sinew,whisker,fang}.json          (client item)
  src/main/resources/assets/bunnidogs/models/item/{sinew,whisker,fang}.json    (model)

Run from the project root:  python3 art/gen_materials.py     (needs Pillow)      Also writes art/materials_preview.png (12x).

The pictures are drawn as small character maps (one letter = one colour), so a change is a one-character edit:
  sinew   a twisted cord with frayed ends, running up to the right (light and dark pairs along it make the twist)
  whisker three thin hairs fanning out of a small dark knot, greyer at the tips
  fang    a curved ivory tooth, wide at the root and pointed, with a shaded side
The lang lines (item.bunnidogs.sinew, ...) are written by hand in lang/en_us.json; the self-check fails until they exist.
"""
import json
import os

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
MODID = "bunnidogs"
ASSETS = os.path.join(ROOT, "src/main/resources/assets", MODID)
PREVIEW = os.path.join(HERE, "materials_preview.png")

# '.' = transparent; the other letters are looked up in the item's own palette
SINEW_MAP = [
    "................",
    "............o.o.",
    "...........oaao.",
    "..........oabbco",
    ".........oabcbo.",
    "........oabbco..",
    ".......oabcbo...",
    "......oabbco....",
    ".....oabcbo.....",
    "....oabbco......",
    "...oabcbo.......",
    "..oabbco........",
    "..oacbo.........",
    ".o.oo.o.........",
    "................",
    "................",
]
SINEW_PALETTE = {
    "o": (122, 78, 70),     # outline: dried reddish brown
    "a": (246, 222, 206),   # light fibre
    "b": (222, 170, 150),   # mid fibre
    "c": (176, 112, 98),    # dark fibre (the twist)
}

# the whiskers are three one-pixel hairs (polylines, drawn with ImageDraw) growing out of a dark knot; the last two pixels of each hair are greyer
WHISKER_STRANDS = [
    [(3, 11), (5, 7), (8, 4), (12, 2), (14, 2)],
    [(4, 11), (6, 8), (9, 7), (13, 6), (15, 7)],
    [(3, 11), (3, 7), (4, 4), (6, 1)],
]
WHISKER_HAIR = (238, 238, 246)
WHISKER_TIP = (176, 176, 196)
WHISKER_KNOT = [(2, 12), (3, 12), (4, 12), (5, 12), (2, 13), (3, 13), (4, 13), (5, 13), (3, 14), (4, 14)]
WHISKER_KNOT_COLOURS = [(86, 76, 92), (62, 54, 68)]

FANG_MAP = [
    "................",
    "..oooooo........",
    ".oaaaaaao.......",
    ".oaaaaaabo......",
    ".oaaaaaabbo.....",
    "..oaaaaabbo.....",
    "..oaaaaabbo.....",
    "...oaaaabbo.....",
    "....oaaabbo.....",
    "....oaaabo......",
    ".....oaabo......",
    "......oabo......",
    ".......oabo.....",
    "........obo.....",
    ".........o......",
    "................",
]
FANG_PALETTE = {
    "o": (104, 92, 74),     # outline
    "a": (246, 240, 222),   # ivory
    "b": (206, 194, 166),   # shaded side
}

ITEMS = {
    "sinew": lambda: draw(SINEW_MAP, SINEW_PALETTE),
    "whisker": lambda: draw_whiskers(),
    "fang": lambda: draw(FANG_MAP, FANG_PALETTE),
}


def draw(rows, palette):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), "a texture is 16x16 characters"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = palette[ch] + (255,)
    return img


def draw_whiskers():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_ = ImageDraw.Draw(img)
    for strand in WHISKER_STRANDS:
        draw_.line(strand, fill=WHISKER_HAIR + (255,), width=1)
        last = strand[-1]
        prev = strand[-2]
        draw_.point(last, fill=WHISKER_TIP + (255,))
        draw_.point(((last[0] + prev[0]) // 2, (last[1] + prev[1]) // 2), fill=WHISKER_TIP + (255,))
    px = img.load()
    for i, (x, y) in enumerate(WHISKER_KNOT):
        px[x, y] = WHISKER_KNOT_COLOURS[(x + y) % 2] + (255,)
    return img


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def main():
    os.makedirs(os.path.join(ASSETS, "textures/item"), exist_ok=True)
    images = {}
    for name, make in ITEMS.items():
        images[name] = make()
        images[name].save(os.path.join(ASSETS, "textures/item", f"{name}.png"))
        write_json(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{MODID}:item/{name}"}})
        write_json(os.path.join(ASSETS, "models/item", f"{name}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MODID}:item/{name}"}})

    scale = 12
    cell = 16 * scale + 12
    sheet = Image.new("RGBA", (cell * len(images), 16 * scale + 12), (58, 58, 64, 255))
    for i, img in enumerate(images.values()):
        big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.paste(big, (i * cell + 6, 6), big)
    sheet.save(PREVIEW)
    print("wrote", ", ".join(images), "and", PREVIEW)


if __name__ == "__main__":
    main()
