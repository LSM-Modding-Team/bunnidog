#!/usr/bin/env python3
"""Generates everything that exists ONCE PER COAT for the pelts (drops D2), from ONE loop over the entity's coat table (COATS in art/gen_bunnidog.py):

  src/main/resources/assets/bunnidogs/textures/item/<coat>_pelt.png   (16x16)
  src/main/resources/assets/bunnidogs/items/<coat>_pelt.json          (client item)
  src/main/resources/assets/bunnidogs/models/item/<coat>_pelt.json    (model)
  src/main/resources/data/bunnidogs/tags/item/pelts.json              (the tag "any pelt", used by the leather recipe)

Run from the project root:  python3 art/gen_pelts.py     (needs Pillow)      Also writes art/pelts_preview.png (8x, all coats).

A new coat = a new entry in COATS (art/gen_bunnidog.py) + the allele in genetics/Gene.java: running this script then gives it its pelt files, and ModItems
registers the item from the same allele table. The lang line (item.bunnidogs.<coat>_pelt) is written by hand in lang/en_us.json; the self-check fails until it exists.

The pelt is a folded hide in the coat's own colours: light on the folded-over top half, a dark crease, the base colour below, four small paw stubs and a tail tip.
Coats with "patches" get the patch colour as a few spots, so a spotted, ember, void or glow pelt can be told from its plain neighbour.
"""
import json
import os
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
from gen_bunnidog import COATS  # noqa: E402  (the single source of truth for the coat colours)

MODID = "bunnidogs"
ASSETS = os.path.join(ROOT, "src/main/resources/assets", MODID)
DATA = os.path.join(ROOT, "src/main/resources/data", MODID)
PREVIEW = os.path.join(HERE, "pelts_preview.png")

# the folded hide: rows 3-12 are the body, rounded at the corners; the stubs and the tail are single pixels
BODY = {y: (3, 12) for y in range(3, 13)}
BODY[3] = (4, 11)
BODY[12] = (4, 11)
STUBS = [(2, 4), (13, 4), (2, 11), (13, 11), (7, 13), (8, 13)]
CREASE_ROW = 8
PATCH_SPOTS = [(5, 5), (6, 5), (6, 6), (10, 6), (9, 10), (10, 10), (5, 10), (11, 11)]


def shade(rgba, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in rgba[:3]) + (255,)


def draw(look):
    base, dark, light = look["base"], look["dark"], look["light"]
    outline = shade(dark, 0.62)
    shape = set(STUBS)
    for y, (l, r) in BODY.items():
        shape.update((x, y) for x in range(l, r + 1))
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for (x, y) in shape:
        edge = any((x + dx, y + dy) not in shape for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            px[x, y] = outline
        elif y < CREASE_ROW:
            px[x, y] = light
        elif y == CREASE_ROW:
            px[x, y] = dark
        elif x >= BODY[y][1] - 1:
            px[x, y] = dark
        else:
            px[x, y] = base
    if "patches" in look:
        for (x, y) in PATCH_SPOTS:
            if (x, y) in shape and px[x, y][:3] not in (outline[:3],):
                px[x, y] = look["patches"]
    return img


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def main():
    os.makedirs(os.path.join(ASSETS, "textures/item"), exist_ok=True)
    images = {}
    for coat, look in COATS.items():
        name = f"{coat}_pelt"
        images[coat] = draw(look)
        images[coat].save(os.path.join(ASSETS, "textures/item", f"{name}.png"))
        write_json(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{MODID}:item/{name}"}})
        write_json(os.path.join(ASSETS, "models/item", f"{name}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MODID}:item/{name}"}})
    write_json(os.path.join(DATA, "tags/item/pelts.json"), {"replace": False, "values": [f"{MODID}:{coat}_pelt" for coat in COATS]})

    scale, per_row = 8, 6
    cell = 16 * scale + 8
    rows = (len(images) + per_row - 1) // per_row
    sheet = Image.new("RGBA", (cell * per_row, cell * rows), (58, 58, 64, 255))
    for i, img in enumerate(images.values()):
        big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.paste(big, ((i % per_row) * cell + 4, (i // per_row) * cell + 4), big)
    sheet.save(PREVIEW)
    print("wrote", len(images), "pelts (", ", ".join(images), ") and the pelts tag; preview", PREVIEW)


if __name__ == "__main__":
    main()
