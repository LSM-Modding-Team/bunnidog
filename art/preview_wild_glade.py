#!/usr/bin/env python3
"""Cell-map inspection of actual generated and worked native glade snapshots; not a client render."""
import hashlib
import json
from collections import defaultdict
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent / "wild_glade"
PALETTE = {
    "grass_block": (96, 130, 76), "dirt": (113, 94, 65), "farmland": (107, 80, 53),
    "water": (61, 116, 169), "stone": (139, 145, 145), "hearth_tree": (137, 92, 50),
    "nest": (197, 166, 101), "guava_log": (128, 91, 57), "guava_coppice": (77, 141, 73),
    "guava_leaves": (66, 111, 65), "guava_fruit": (190, 107, 88), "wheat": (197, 179, 86),
    "furnace": (100, 106, 111), "torch": (255, 213, 113), "guava_planks": (164, 121, 75),
}
GLYPHS = {"hearth_tree": "H", "nest": "N", "guava_log": "L", "guava_coppice": "C",
          "guava_leaves": "g", "guava_fruit": "g", "wheat": "w", "furnace": "F", "torch": "T"}


def main():
    sources = [("Generated ProtoChunk glade", ROOT / "generated.json"),
               ("Controlled site before work", ROOT / "start.json"),
               ("Same site after the paid work chain", ROOT / "completed.json")]
    canvas = Image.new("RGB", (1550, 810), (32, 42, 38))
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.truetype("DejaVuSans.ttf", 16)
    small = ImageFont.truetype("DejaVuSans.ttf", 13)
    title_font = ImageFont.truetype("DejaVuSans.ttf", 24)
    draw.text((28, 20), "Wild glade: native generation and paid workshop work", font=title_font, fill=(239, 232, 208))
    provenance = {"renderer": "Pillow native block-cell map; schematic colors/glyphs, not client geometry, textures or lighting.", "sources": []}
    for panel, (title, path) in enumerate(sources):
        raw = path.read_bytes()
        snapshot = json.loads(raw)
        columns = defaultdict(list)
        for cell in snapshot["cells"]:
            columns[(cell["x"], cell["z"])].append(cell)
        ox, oy, size = 85 + panel * 510, 126, 23
        draw.text((38 + panel * 510, 76), title, font=font, fill=(239, 232, 208))
        draw.text((ox + 145, 103), "N ↑", font=small, fill=(199, 213, 197))
        for x in range(-7, 8):
            for z in range(-8, 11):
                column = columns.get((x, z), [])
                px, py = ox + (x + 7) * size, oy + (z + 8) * size
                if not column:
                    draw.rectangle((px, py, px + size - 1, py + size - 1), fill=(25, 32, 28))
                    continue
                top = max(column, key=lambda cell: cell["y"])
                name = top["block"].split(":", 1)[1]
                color = PALETTE.get(name, (162, 116, 163))
                draw.rectangle((px, py, px + size - 1, py + size - 1), fill=color, outline=(50, 67, 51))
                if name == "torch":
                    draw.ellipse((px + 2, py + 2, px + size - 3, py + size - 3), fill=(255, 235, 166))
                glyph = str(top["y"] + 1) if name == "stone" else GLYPHS.get(name, "")
                if glyph:
                    draw.text((px + 6, py + 3), glyph, font=small, fill=(24, 32, 27))
        draw.text((ox, oy + 19 * size + 13), "West ←        native 15 × 19 footprint        → East", font=small, fill=(201, 214, 195))
        count = sum(cell["block"].endswith(":stone") for cell in snapshot["cells"])
        furnaces = sum(cell["block"].endswith(":furnace") for cell in snapshot["cells"])
        torches = sum(cell["block"].endswith(":torch") for cell in snapshot["cells"])
        draw.text((38 + panel * 510, 612), f"{count} stone blocks · {furnaces} furnace · {torches} torches", font=font, fill=(239, 232, 208))
        provenance["sources"].append({"file": path.name, "sha256": hashlib.sha256(raw).hexdigest(),
                                      "evidence": snapshot["evidence"], "native_cells": len(snapshot["cells"])})
    draw.text((30, 670), "H: original cache  N: nest  L: fallen log  C: coppice  g: fruit with leaf support  w: wheat", font=small, fill=(225, 223, 198))
    draw.text((30, 696), "F: paid native furnace  T: supported torch  1 / 2: actual stone-column height", font=small, fill=(225, 223, 198))
    draw.text((30, 735), "Generated: actual WorldGenRegion / unfinished ProtoChunks. Controlled pair: the same empty-store site before and after native work.", font=small, fill=(184, 202, 187))
    draw.text((30, 759), "Schematic block map only. Natural founder selection, Survival duration and client appearance remain separate evidence.", font=small, fill=(184, 202, 187))
    ROOT.mkdir(parents=True, exist_ok=True)
    canvas.save(ROOT / "overview.png")
    (ROOT / "provenance.json").write_text(json.dumps(provenance, indent=2) + "\n")


if __name__ == "__main__":
    main()
