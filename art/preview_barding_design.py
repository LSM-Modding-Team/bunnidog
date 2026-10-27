#!/usr/bin/env python3
"""Planning-only fitted barding concept over the actual unchanged Bunnidog sculpt.

Writes art/barding_design.png. No Java, shipped meshes, textures or item data are
generated. The surface studies do not establish runtime animation/wing clearance.
"""
from pathlib import Path
import math
import numpy as np
from PIL import Image, ImageDraw

from gen_bunnidog import ROOT
from preview_bunnidog import world_faces, render, font
from sculpt_bunnidog import catmull, STANCE_DROP

TIERS = [
    ("Leather", (124, 76, 46), (205, 163, 95)),
    ("Iron", (174, 186, 192), (225, 231, 227)),
    ("Diamond", (49, 163, 183), (151, 230, 227)),
    ("Netherite", (65, 60, 77), (155, 147, 163)),
    ("Astralite", (75, 84, 115), (171, 239, 227)),
]
BG = (238, 234, 226)


def face(points, colour_slot):
    points = np.asarray(points, dtype=float)
    normal = np.cross(points[2] - points[0], points[3] - points[1])
    normal /= np.linalg.norm(normal)
    return points, normal, (4 + colour_slot * 12, 516), (4, 4), np.tile(normal, (4, 1))


# The approved torso's authored sections, used only as a fitting reference.
SECTIONS = ((-4.05, 5.85, .18, .25), (-3.35, 5.8, 2.6, 1.6),
            (-2.2, 5.95, 3.9, 2.45), (-.35, 6.05, 4.5, 2.9),
            (1.8, 6, 4.25, 2.9), (3.8, 5.9, 3.75, 2.65),
            (5.4, 5.65, 2.9, 2.2), (6.35, 5.45, 1.3, 1.12), (6.65, 5.4, .1, .15))


def study(tier, build=1):
    original = world_faces(build_scale=build)
    plates = []
    pivot = np.array((0, 5.1, 1.7))

    def surface(t, angle, offset):
        z, cy, rx, ry = catmull(SECTIONS, t)
        x = (rx + offset) * math.sin(angle)
        y = cy + (ry + offset) * math.cos(angle)
        y += .045 * math.sin(z * 2.7 + x * 1.2) * math.sin(angle)**2
        point = np.array((x + .08 * math.sin(z), y - STANCE_DROP, z))
        return pivot + (point - pivot) * (build, 1+(build-1)*.6, 1+(build-1)*.3)

    def shell(t0, t1, a0, a1, slot, offset=.25, bevel=True):
        nt, na = 12, 12
        for j in range(nt):
            for k in range(na):
                coords = [(j/nt,k/na), (j/nt,(k+1)/na),
                          ((j+1)/nt,(k+1)/na), ((j+1)/nt,k/na)]
                points = []
                for u, v in coords:
                    rim = min(u,1-u,v,1-v)
                    height = offset if not bevel else offset - .10 * max(0,1-rim/.1)
                    points.append(surface(t0+(t1-t0)*u,a0+(a1-a0)*v,height))
                # Increasing angle around the torso gives an inward winding.
                if a1 > a0:
                    points.reverse()
                edge = bevel and (j in (0,nt-1) or k in (0,na-1))
                plates.append(face(points, 2 if edge else slot))

    for side in (-1,1):
        # Leather foundation and complete narrow belly straps.
        shell(1.35,5.65,side*.95,side*2.0,3,.13,False)
        for t in (1.7,4.8):
            shell(t,t+.16,side*.2,side*3.0,3,.16,False)
        # Shoulder guards stay below the wing root. Flank panels overlap like lames.
        shell(1.0,1.6,side*.85,side*2.3,0,.23)
        if tier > 0:
            runs = [(1.85,2.9),(3.08,4.2)]
            if tier >= 2:
                runs.append((4.4,5.55))
            for t0,t1 in runs:
                shell(t0,t1,side*.98,side*1.98,0,.30)
                # Paired small fasteners on each independent plate.
                for t in (t0+.15,t1-.15):
                    for angle in (1.16,1.80):
                        shell(t-.035,t+.035,side*(angle-.045),side*(angle+.045),4 if tier==4 else 1,.36,False)
            if tier >= 3:
                shell(5.62,6.12,side*1.0,side*1.85,0,.27)
        if tier == 4:
            # A restrained inset follows the side of the middle plate.
            shell(3.55,3.72,side*1.15,side*1.78,1,.32,False)
    if tier > 0:
        for t0,t1 in [(3.2,3.85),(4.02,4.65),(4.82,5.45)][:1 if tier==1 else 3]:
            shell(t0,t1,-.48,.48,0,.23)
    # Split breastplate leaves the face clear and gives a visible frontal buckle.
    for side in (-1,1):
        points = [(side*.12, 4.2, -3.85), (side*2.0, 4.65, -3.1),
                  (side*1.6, 3.6, -3.25), (side*.22, 3.45, -3.9)]
        if side < 0:
            points.reverse()
        plates.append(face(points, 0))
    plates.append(face([(-.36, 4.15, -3.99), (-.36, 3.65, -3.99),
                        (.36, 3.65, -3.99), (.36, 4.15, -3.99)], 1))
    return original + plates


def main():
    root = Path(ROOT)
    fur = Image.open(root / 'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png').convert('RGB')
    width, height = 510, 515
    sheet = Image.new('RGB', (width * 3, height * 2 + 130), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((26, 18), 'BUNNIDOG BARDING — DESIGN STUDY', font=font(27), fill=(43, 42, 38))
    draw.text((26, 55), 'One wearable item · five tiers · fitted plates over the existing sculpt', font=font(18), fill=(94, 89, 82))
    cases = [(i, -72, 1, TIERS[i][0]) for i in range(5)]
    cases.append((4, -145, 1.3, 'Astralite — heavy build / rear'))
    for index, (tier, yaw, build, label) in enumerate(cases):
        texture = Image.new('RGB', (512, 528), BG)
        texture.paste(fur, (0, 0))
        tex_draw = ImageDraw.Draw(texture)
        palette = (*TIERS[tier][1:], tuple(int(c*.65) for c in TIERS[tier][1]),
                   (93, 58, 39), (196, 143, 99))
        for slot, colour in enumerate(palette):
            tex_draw.rectangle((slot*12, 512, slot*12+11, 527), fill=colour)
        image = render(study(tier, build), np.asarray(texture, dtype=float),
                       yaw=yaw, elevation=16, width=width, height=height-40, zoom=23)
        x, y = (index % 3) * width, 90 + (index // 3) * height
        sheet.paste(image, (x, y))
        draw.text((x+26, y+height-38), label, font=font(21), fill=(43, 42, 38))
    draw.text((26, sheet.height-30),
              'Concept geometry, not shipped armour or a client capture. Wing/pack clearance and animated fit remain implementation work.',
              font=font(15), fill=(94, 89, 82))
    output = root / 'art/barding_design.png'
    sheet.save(output)
    print(output)


if __name__ == '__main__':
    main()
