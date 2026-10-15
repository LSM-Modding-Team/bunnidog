#!/usr/bin/env python3
"""Generates the five legends of the drops (chunk D8d): titan_haunch, gilded_foot, moon_sinew, listening_shell, everbloom.

  src/main/resources/assets/bunnidogs/textures/item/<name>.png   (16x16)
  src/main/resources/assets/bunnidogs/items/<name>.json          (client item)
  src/main/resources/assets/bunnidogs/models/item/<name>.json    (model)

Run from the project root:  python3 art/gen_legends.py     (needs Pillow)      Also writes art/legends_preview.png (12x).

Each picture is the FILL of the shape (character maps or a small formula); the dark outline ('o' in the item's palette) is added
automatically around every filled pixel, so keep the fill inside columns/rows 1..14. '.' = transparent.
  titan_haunch      a huge glazed ham with a bone, gold glints            (Vitality)
  gilded_foot       a paw dipped in gold: three toes and a heart-shaped pad (Swiftness)
  moon_sinew        a crescent of silver-blue twisted cord                  (Leap)
  listening_shell   a spiral conch with a pink mouth                        (Senses)
  everbloom         a five-petal flower on a stem with two leaves           (Bloom)
The lang lines (item.bunnidogs.titan_haunch, ...) are written by hand in lang/en_us.json; the self-check fails until they exist.
"""
import json
import math
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
MODID = "bunnidogs"
ASSETS = os.path.join(ROOT, "src/main/resources/assets", MODID)
PREVIEW = os.path.join(HERE, "legends_preview.png")


# ---------------------------------------------------------------- titan_haunch
HAUNCH_MAP = [
    "................",
    "................",
    "....aaaaaaa.....",
    "...aaggaaaabb...",
    "..aaggaaaabbbb..",
    "..aagaaaabbbbbb.",
    "..aaaaaabbbbbbc.",
    "..abaaabbbbbbcc.",
    "..abbbbbbbbbccc.",
    "...bbbbbbbbccwv.",
    "....bbbbbccwwwv.",
    ".....ccbbcwwwv..",
    "........wwwv....",
    "........vvv.....",
    "................",
    "................",
]
HAUNCH_PALETTE = {
    "o": (88, 46, 26),       # outline
    "a": (252, 216, 128),   # gold glaze
    "b": (226, 152, 72),    # golden brown
    "c": (166, 92, 48),     # dark brown (the underside)
    "g": (255, 248, 214),   # glint
    "w": (248, 242, 224),   # bone
    "v": (208, 196, 170),   # bone shade
}

# ---------------------------------------------------------------- gilded_foot
FOOT_MAP = [
    "................",
    "................",
    "..aa..aa..aa....",
    ".aaba.aaba.aaba.",
    ".aabb.aabb.aabb.",
    ".abbb.abbb.abbb.",
    "..bb...bb...bb..",
    "....aaaaaaaa....",
    "...aaggaaabbb...",
    "..aaggaaaabbbb..",
    "..aagaaaabbbbc..",
    "..abaaabbbbbcc..",
    "...bbbbbbbbcc...",
    "....bbccbbcc....",
    ".....cccccc.....",
    "................",
]
FOOT_PALETTE = {
    "o": (104, 68, 14),
    "a": (255, 238, 150),   # light gold
    "b": (240, 196, 70),    # gold
    "c": (186, 140, 40),    # dark gold
    "g": (255, 255, 235),   # glint
}


# ---------------------------------------------------------------- moon_sinew
def moon_sinew():
    """A crescent: the big disc minus a second disc shifted right. The cord's twist = three shades along the ring."""
    shades = {0: "a", 1: "b", 2: "c"}
    rows = []
    for y in range(16):
        row = ""
        for x in range(16):
            outer = (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 6.4 ** 2
            inner = (x - 10.6) ** 2 + (y - 6.2) ** 2 <= 5.3 ** 2
            if outer and not inner and 1 <= x <= 14 and 1 <= y <= 14:
                # the twist runs around the crescent: shade by the angle around the centre, in bands
                ang = math.atan2(y - 7.5, x - 7.5)
                row += shades[int((ang + math.pi) / (2 * math.pi) * 14) % 3]
            else:
                row += "."
        rows.append(row)
    # two glints on the upper tip and a spark inside the curve
    grid = [list(r) for r in rows]
    for (x, y) in [(7, 2), (4, 4)]:
        if grid[y][x] != ".":
            grid[y][x] = "g"
    for (x, y) in [(11, 8), (11, 7), (11, 9), (10, 8), (12, 8)]:   # the spark (a plus), in the hollow of the crescent
        if grid[y][x] == ".":
            grid[y][x] = "g"
    return ["".join(r) for r in grid]


MOON_PALETTE = {
    "o": (52, 64, 112),
    "a": (236, 244, 255),   # light fibre
    "b": (176, 200, 236),   # mid fibre
    "c": (112, 140, 196),   # dark fibre (the twist)
    "g": (255, 255, 255),   # glint / spark
}


# ---------------------------------------------------------------- listening_shell
def listening_shell():
    """A conch: a spiral disc (rings that wind outwards) plus a pink mouth flaring to the lower right."""
    cx, cy = 7.0, 7.0
    grid = [["."] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            if r <= 6.0 and 1 <= x <= 14 and 1 <= y <= 14:
                ang = math.atan2(dy, dx) / (2 * math.pi)       # -0.5 .. 0.5
                band = int(math.floor(r * 0.9 + ang * 2.0)) % 3   # the winding spiral
                grid[y][x] = "abc"[band]
    # the mouth: a flared pink ellipse at the lower right, with a dark inside
    for y in range(16):
        for x in range(16):
            if ((x - 11.0) / 3.4) ** 2 + ((y - 11.0) / 2.8) ** 2 <= 1.0 and x <= 14 and y <= 14:
                grid[y][x] = "p"
            if ((x - 11.6) / 1.9) ** 2 + ((y - 11.4) / 1.4) ** 2 <= 1.0:
                grid[y][x] = "d"
    for (x, y) in [(5, 4), (4, 5)]:
        grid[y][x] = "g"
    return ["".join(r) for r in grid]


SHELL_PALETTE = {
    "o": (96, 56, 70),
    "a": (255, 240, 230),   # cream
    "b": (242, 198, 188),   # pink
    "c": (214, 150, 150),   # deeper pink (the spiral groove)
    "p": (250, 176, 176),   # the lip of the mouth
    "d": (150, 84, 104),    # inside the mouth
    "g": (255, 255, 255),
}


# ---------------------------------------------------------------- everbloom
def everbloom():
    """Five round petals around a gold heart, a stem with two leaves, a few sparkles."""
    grid = [["."] * 16 for _ in range(16)]
    cx, cy = 7.5, 5.0
    for k in range(5):
        a = -math.pi / 2 + k * 2 * math.pi / 5
        px, py = cx + 3.0 * math.cos(a), cy + 3.0 * math.sin(a)
        for y in range(0, 11):
            for x in range(1, 15):
                if (x - px) ** 2 + (y - py) ** 2 <= 2.3 ** 2:
                    grid[y][x] = "a" if (x + y) % 4 else "b"
    for y in range(16):
        for x in range(16):
            if (x - cx) ** 2 + (y - cy) ** 2 <= 1.9 ** 2:
                grid[y][x] = "y" if (x - cx) + (y - cy) < 0.6 else "d"
    for y in range(9, 15):                   # the stem
        grid[y][7] = "s"
        grid[y][8] = "t"
    for (x, y) in [(5, 11), (4, 10), (3, 10), (5, 12), (4, 11), (10, 12), (11, 11), (12, 11), (10, 13), (11, 12)]:
        grid[y][x] = "s" if (x + y) % 2 else "t"     # the two leaves
    return ["".join(r) for r in grid]


FLOWER_PALETTE = {
    "o": (74, 44, 70),
    "a": (255, 244, 250),   # white petal
    "b": (250, 196, 214),   # pink petal
    "y": (255, 214, 64),    # gold heart
    "d": (214, 150, 30),    # heart shade
    "s": (92, 176, 76),     # green
    "t": (52, 124, 56),     # dark green
}


# ---------------------------------------------------------------- drawing
def draw(rows, palette):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), "a texture is 16x16 characters"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    filled = set()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = palette[ch] + (255,)
                filled.add((x, y))
    for (x, y) in filled:                                   # the outline: every empty 4-neighbour of a filled pixel
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in filled:
                px[nx, ny] = palette["o"] + (255,)
    return img


ITEMS = {
    "titan_haunch": lambda: draw(HAUNCH_MAP, HAUNCH_PALETTE),
    "gilded_foot": lambda: draw(FOOT_MAP, FOOT_PALETTE),
    "moon_sinew": lambda: draw(moon_sinew(), MOON_PALETTE),
    "listening_shell": lambda: draw(listening_shell(), SHELL_PALETTE),
    "everbloom": lambda: draw(everbloom(), FLOWER_PALETTE),
}


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
