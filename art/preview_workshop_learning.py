#!/usr/bin/env python3
"""Render actual exported pure reader pages; this is headless layout evidence, not a client screenshot."""
from pathlib import Path
import argparse
from PIL import Image, ImageDraw, ImageFont


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pages", type=Path, help="checkLocalization -PlocalizationPreview=<file> export")
    parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parent / "workshop_learning")
    args = parser.parse_args()
    sections = []
    for line in args.pages.read_text().splitlines():
        if line.startswith("SPREAD "):
            sections.append([])
        if sections and len(sections) <= 2:
            sections[-1].append(line)
    if len(sections) < 2:
        parser.error("Need the actual individual and forecast page exports")
    exported = "\n".join(line for section in sections[:2] for line in section) + "\n"
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output / "native-pages.txt").write_text(exported)
    scale = 3
    canvas = Image.new("RGB", (900, 1125), (43, 46, 39))
    draw = ImageDraw.Draw(canvas)
    normal = ImageFont.truetype("DejaVuSansMono.ttf", 8 * scale)
    bold = ImageFont.truetype("DejaVuSansMono-Bold.ttf", 8 * scale)
    caption = ImageFont.truetype("DejaVuSans.ttf", 13)
    colors = {"HEAD": "#4d3020", "MUTED": "#716954", "INK": "#30251e", "GOOD": "#315624", "BAD": "#882e28"}
    for row, section in enumerate(sections[:2]):
        origin_y = 48 + row * 530
        draw.text((24, origin_y - 23), "Actual Spanish individual capacities" if row == 0 else "Actual Spanish offspring forecast", fill="white", font=caption)
        for column in range(2):
            x = 24 + column * 444
            draw.rectangle((x - 9, origin_y - 9, x + 130 * scale + 9, origin_y + 156 * scale + 9), fill="#e2d1a4", outline="#9e825b", width=3)
        column = 0
        for line in section:
            if line.startswith("PAGE "):
                column = int(line.endswith("R"))
            elif line.startswith("T "):
                _, x, y, tone, right, label = line.split(" ", 5)
                font = bold if tone == "HEAD" else normal
                px = 24 + column * 444 + int(x) * scale
                if right == "true":
                    px -= draw.textlength(label, font=font)
                draw.text((px, origin_y + int(y) * scale), label, fill=colors[tone], font=font)
    draw.text((24, 1100), "Native pure exports; estimated font rendering. Client acceptance pending.", fill="white", font=caption)
    canvas.save(args.output / "mind-pages.png")


if __name__ == "__main__":
    main()
