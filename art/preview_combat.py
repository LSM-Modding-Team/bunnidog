#!/usr/bin/env python3
"""Inspect the unchanged sculpt with the combat model's poses; this is not a client capture."""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw
from preview_bunnidog import ROOT, world_faces, render, font


def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--care-log', help='Native GameTest log, for the care-panel layout preview')
    args = parser.parse_args()
    root = Path(ROOT)
    texture = np.asarray(Image.open(root/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'), dtype=float)
    sheet = Image.new('RGB', (1840, 710), (238, 234, 226))
    draw = ImageDraw.Draw(sheet)
    draw.text((24, 15), 'Combat poses — actual sculpt and atlas, equivalent model transforms', font=font(23), fill=(43, 42, 38))
    cases = [('Standing', None, 1), ('Bite', 'bite', 1), ('Downed', 'downed', 1), ('Heavy downed', 'downed', 1.6)]
    for i, (label, pose, build) in enumerate(cases):
        faces = world_faces(build_scale=build, combat_pose=pose)
        sheet.paste(render(faces, texture, -45, 10, 460, 600, 23), (460*i, 55))
        draw.text((460*i+24, 660), label, font=font(20), fill=(43, 42, 38))
        print(label, 'lowest mesh point:', round(min(float(c[:, 1].min()) for c, *_ in faces), 3))
    path = root/'art/combat_poses_preview.png'
    sheet.save(path)
    print(path)
    if args.care_log:
        from gen_cohesion import preview_from_log
        preview_from_log(args.care_log, combat_only=True)


if __name__ == '__main__':
    main()
