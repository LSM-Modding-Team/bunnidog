#!/usr/bin/env python3
"""Paints the Pedigree Book pages for a quick look (Phase 8 S3). Reads the output of tools/SheetDump.java on stdin and
writes art/book_preview.png. The colours are the ones PedigreeScreen uses; the font is NOT Minecraft's (Pillow's), so
widths are only close. Usage: see tools/SheetDump.java."""
import sys
from PIL import Image, ImageDraw, ImageFont

SCALE = 2
BOOK_W, BOOK_H, LEFT_X, RIGHT_X, TOP = 312, 200, 18, 164, 22
PARCH, EDGE, SPINE, MAT = (0xE8, 0xD8, 0xAE), (0x5B, 0x3A, 0x1E), (0xB5, 0x9B, 0x68), (0xD8, 0xC7, 0x9A)
TONES = {"INK": (0x2B, 0x1D, 0x0E), "HEAD": (0x5B, 0x3A, 0x1E), "MUTED": (0x8A, 0x7A, 0x5C), "GOOD": (0x2E, 0x7D, 0x32), "BAD": (0xB3, 0x26, 0x1E)}
HEART = [".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."]

def font():
    for name in ("DejaVuSans.ttf", "LiberationSans-Regular.ttf"):
        try:
            return ImageFont.truetype(name, 8 * SCALE + 1)
        except OSError:
            pass
    return ImageFont.load_default()

def parse(lines):
    spreads, cur, page = [], None, None
    for raw in lines:
        raw = raw.rstrip("\n")
        if raw.startswith("SPREAD"):
            cur = {"L": [], "R": []}
            spreads.append(cur)
        elif raw.startswith("PAGE"):
            page = cur[raw.split()[1]]
        elif raw:
            page.append(raw)
    return spreads

def draw_spread(d, ox, oy, spread, f):
    def box(x0, y0, x1, y1, c):
        d.rectangle([ox + x0 * SCALE, oy + y0 * SCALE, ox + x1 * SCALE - 1, oy + y1 * SCALE - 1], fill=c)
    box(-2, -2, BOOK_W + 2, BOOK_H + 2, EDGE)
    box(0, 0, BOOK_W, BOOK_H, PARCH)
    box(BOOK_W // 2 - 1, 0, BOOK_W // 2 + 1, BOOK_H, SPINE)
    for side, px in (("L", LEFT_X), ("R", RIGHT_X)):
        for item in spread[side]:
            k, rest = item[0], item[2:]
            if k == "P":
                x, y, w, h, argb = map(int, rest.split())
                col = ((argb >> 16) & 255, (argb >> 8) & 255, argb & 255)
                box(px + x, TOP + y, px + x + w, TOP + y + h, EDGE)
                box(px + x + 1, TOP + y + 1, px + x + w - 1, TOP + y + h - 1, MAT)
                box(px + x + 5, TOP + y + 5, px + x + w - 5, TOP + y + h - 5, col)
            elif k == "H":
                x, y, full, half, times = rest.split()
                icons = 1 if int(times) else int(full) + (1 if half == "true" else 0)
                for i in range(icons):
                    last_half = half == "true" and not int(times) and i == icons - 1
                    for r, row in enumerate(HEART):
                        for c, ch in enumerate(row):
                            if ch == "X":
                                col = (0x8A, 0x73, 0x60) if last_half and c >= 4 else (0xD0, 0x28, 0x28)
                                bx = px + int(x) + i * 8 + c
                                box(bx, TOP + int(y) + 1 + r, bx + 1, TOP + int(y) + 2 + r, col)
            elif k == "T":
                x, y, tone, right, text = rest.split(" ", 4)
                w = d.textlength(text, font=f)
                tx = ox + (px + int(x)) * SCALE - (w if right == "true" else 0)
                d.text((tx, oy + (TOP + int(y)) * SCALE), text, fill=TONES[tone], font=f)

def main():
    spreads = parse(sys.stdin.readlines())
    cols = 2
    rows = (len(spreads) + cols - 1) // cols
    cw, ch = (BOOK_W + 12) * SCALE, (BOOK_H + 12) * SCALE
    img = Image.new("RGB", (cols * cw, rows * ch), (40, 40, 44))
    d, f = ImageDraw.Draw(img), font()
    for i, s in enumerate(spreads):
        draw_spread(d, (i % cols) * cw + 6 * SCALE, (i // cols) * ch + 6 * SCALE, s, f)
    img.save("art/book_preview.png")
    print("wrote art/book_preview.png,", len(spreads), "spreads")

if __name__ == '__main__':
    main()
