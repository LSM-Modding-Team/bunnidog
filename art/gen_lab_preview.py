"""Layout preview only: reads LabDump, uses the existing book painter and a substitute font."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw
from gen_book_preview import BOOK_W, BOOK_H, SCALE, parse, draw_spread, font


def main():
    spreads = parse(sys.stdin.readlines())
    cell_w, cell_h = (BOOK_W + 12) * SCALE, (BOOK_H + 12) * SCALE
    img = Image.new('RGB', (cell_w * 2, cell_h * ((len(spreads) + 1) // 2)), (40, 40, 44))
    draw, face = ImageDraw.Draw(img), font()
    for i, spread in enumerate(spreads):
        draw_spread(draw, i % 2 * cell_w + 6 * SCALE, i // 2 * cell_h + 6 * SCALE, spread, face)
    path = Path(__file__).with_name('lab_preview.png')
    img.save(path)
    print('Wrote lab_preview.png (layout only, substitute font).')


if __name__ == '__main__': main()
