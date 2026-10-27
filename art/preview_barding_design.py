#!/usr/bin/env python3
"""Planning-only fitted barding concept over the actual unchanged Bunnidog sculpt.

Writes art/barding_design.png. No Java, shipped meshes, textures or item data are
generated. The surface studies do not establish runtime animation/wing clearance.
"""
from pathlib import Path
import math
import numpy as np
from PIL import Image, ImageDraw

from gen_bunnidog import ROOT, TILES, PARTS
from preview_bunnidog import world_faces, render, font
from sculpt_bunnidog import catmull, STANCE_DROP, SCALE, Y_ORIGIN

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

    if tier >= 2:
        # Diamond closes the torso with lighter plates and flexible joints.
        # Netherite/Astralite then add complete enclosing helmets/ear guards,
        # deeper pauldrons, hip guards and heavier reinforcement.
        for side in (-1,1):
            for t0,t1 in [(0.90,2.0),(2.04,3.25),(3.29,4.6),(4.64,5.8),(5.84,6.6),(6.64,7.94)]:
                shell(t0,t1,side*.015,side*(math.pi-.015),0,.29+.075*(tier-2))
            # Raised overlapping pauldrons, cuisses and a rolled lower skirt.
            raised=.39 if tier==2 else .58+.05*(tier-3)
            shell(1.1,2.25,side*.64,side*2.35,0,raised)
            shell(5.15,6.48,side*.65,side*2.3,0,raised-.02)
            shell(1.04,6.5,side*2.26,side*2.4,5 if tier==2 else 2,raised-.08,False)
            if tier==4:
                shell(2.5,4.5,side*1.28,side*1.38,1,.50,False)
            for t in (1.35,2.45,3.7,5.35,6.15):
                for angle in (1.0,2.05):
                    shell(t-.045,t+.045,side*(angle-.05),side*(angle+.05),4,.68,False)
        # A separate raised gorget meets the helmet without moving the jaw.
        shell(.65,1.1,-math.pi,math.pi,2,.38,False)
        if tier==4:
            # The final upgrade reinforces an already complete Netherite suit.
            # Raised spine lames and a double gorget add shape as well as colour.
            shell(.58,1.22,-math.pi,math.pi,0,.53)
            for t0,t1 in [(2.3,3.2),(3.3,4.4),(4.5,5.5)]:
                shell(t0,t1,-.28,.28,0,.62)
            shell(2.6,5.2,-.07,.07,1,.65,False)
        plates += extremities(tier,build)
        return original + plates

    for side in (-1,1):
        # Leather foundation and complete narrow belly straps.
        shell(1.35,5.65,side*.95,side*2.0,3,.13,False)
        for t in (1.7,4.8):
            shell(t,t+.16,side*.2,side*3.0,3,.16,False)
        # Shoulder guards stay below the wing root. Flank panels overlap like lames.
        shell(1.0,1.6,side*.85,side*2.3,0,.23)
        if tier == 1:
            # Iron already has a breast/belly plate, a small rear plate, an open
            # helm and greaves. Diamond extends this existing equipment rather
            # than suddenly introducing a helmet and all four armoured limbs.
            shell(1.1,4.3,side*1.9,side*(math.pi-.03),0,.22)
            runs = [(1.65,2.9),(3.08,4.2),(4.4,5.4)]
            for t0,t1 in runs:
                shell(t0,t1,side*.82,side*2.05,0,.27)
                # Paired small fasteners on each independent plate.
                for t in (t0+.15,t1-.15):
                    for angle in (1.16,1.80):
                        shell(t-.035,t+.035,side*(angle-.045),side*(angle+.045),1,.33,False)
    if tier == 1:
        for t0,t1 in [(3.1,4.15),(4.3,5.4)]:
            shell(t0,t1,-.6,.6,0,.23)
    else:
        shell(2.8,5.4,-.5,.5,0,.12,False)
    # Split breastplate leaves the face clear and gives a visible frontal buckle.
    for side in (-1,1):
        points = [(side*.12, 4.2, -3.85), (side*2.0, 4.65, -3.1),
                  (side*1.6, 3.6, -3.25), (side*.22, 3.45, -3.9)]
        if side < 0:
            points.reverse()
        plates.append(face(points, 0))
    plates.append(face([(-.36, 4.15, -3.99), (-.36, 3.65, -3.99),
                        (.36, 3.65, -3.99), (.36, 4.15, -3.99)], 1))
    return original + plates + extremities(tier,build)


def extremities(tier,build):
    """Concept-only padding, open helm/greaves, then closed plate equipment.

    Fits the actual sculpt surfaces; does not recolour or replace the animal.
    Each tier extends coverage of the previous tier; upper-tier openings retain
    eyes, breathing/bite clearance, joints and claw tips.
    """
    result=[]
    bones={p['name']:p for p in PARTS}
    for tile in TILES:
        name,parent=tile['name'],tile['parent']
        points=np.asarray(tile['corners'],dtype=float)
        normals=np.asarray(tile['normals'],dtype=float)
        slot,offset=0,.12+.05*tier
        visor=False
        if parent=='head':
            if name.startswith(('human_ear_','helix_edge_')):
                if tier==0:
                    continue
                slot=2
            elif not name.startswith('face_'):
                continue
            else:
                # Reference-space openings are in the front surface only.
                paint=np.asarray(tile['paint_corners']).mean(axis=0)
                px,py=181-paint[0]/SCALE,(Y_ORIGIN-paint[1])/SCALE
                front=normals.mean(axis=0)[2]<-.32
                if tier==0 and py>175:
                    continue  # soft crown cap, with the face and ears free
                if tier==1 and not (py<210 or (not front and py<315)
                        or (front and 200<py<300 and abs(px-174)<10)):
                    continue  # open helmet with temple and nasal protection
                eye=front and (210<py<252 if tier==2 else 212<py<244) and (84<px<137 or 204<px<259)
                mouth=front and (306<py<346 and 126<px<218 if tier==2 else 319<py<335 and 139<px<205)
                if eye or mouth:
                    continue
                visor=tier>=1
                if front and 195<py<214:
                    slot=2  # brow rim over the eye openings
                if front and 258<py<310 and abs(px-174)<14:
                    slot=2  # reinforced nasal ridge
                if tier==4 and 139<py<158:
                    slot=1
        elif parent in ('ear_r','ear_l'):
            if tier==0:
                continue
            if not (name.startswith('rabbit_') or name.startswith('ear_rim_')):
                continue
            # Tapered articulated ear sleeves keep the rabbit silhouette.
            j=int(name.split('_')[-2 if name.startswith('rabbit_') else -1])
            if (tier==1 and j>1) or (tier==2 and j>5):
                continue
            slot=2 if j in (1,4,6) else 0
            offset=.12
        elif parent.startswith('leg_') and not parent.endswith('_web'):
            if '_nail_' in name or '_toe_' in name:
                continue
            if '_paw_' in name:
                if tier<2:
                    continue
                if points.mean(axis=0)[1]<.22 or normals.mean(axis=0)[1]<-.1:
                    continue
                offset=.19;slot=2 if tier==3 else 0
            else:
                j=int(name.split('_')[-2])
                if (tier==0 and j!=5) or (tier==1 and j<4 and j!=2):
                    continue
                offset=.09 if tier==0 else .25 if j not in (2,4) else .12
                slot=(5 if tier==2 else 2) if tier>0 and j in (2,4,5) else 0
                if tier==0 and int(name.split('_')[-1])==2:
                    slot=1
                if tier==4 and j==5:
                    slot=1
        elif parent=='tail' and name.startswith('tail_'):
            if tier<2:
                continue
            if tier==2 and int(name.split('_')[1])>1:
                continue
            slot=2 if int(name.split('_')[1])%2 else 0
            offset=.12
        else:
            continue
        points=points+normals*offset
        if visor:
            # Shared vertices use identical warping, keeping the cheek/dome
            # continuous. A rigid visor bridges the face, with actual eye holes.
            py=(Y_ORIGIN-np.asarray(tile['paint_corners'])[:,1])/SCALE
            gate=np.clip((-normals[:,2]-.05)/.4,0,1)*np.clip((py-175)/30,0,1)*np.clip((368-py)/20,0,1)
            target=-7.55+.045*points[:,0]**2
            points[:,2]=points[:,2]*(1-gate)+target*gate
        if parent.startswith('leg_'):
            pivot=np.asarray(bones[parent]['pivot'])
            thick=1+(build-1)*.6
            points=pivot+(points-pivot)*(thick,1,thick)
        result.append(face(points,slot))
    return result


def main():
    root = Path(ROOT)
    fur = Image.open(root / 'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png').convert('RGB')
    width, height = 510, 515
    sheet = Image.new('RGB', (width * 3, height * 3 + 130), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((26, 18), 'BUNNIDOG BARDING — DESIGN STUDY', font=font(27), fill=(43, 42, 38))
    draw.text((26, 55), 'One wearable item · padding → partial plate → fitted armour → full plate → reinforced plate', font=font(18), fill=(94, 89, 82))
    captions=['Leather — padded','Iron — partial plate','Diamond — fitted armour',
              'Netherite — full plate','Astralite — reinforced plate']
    cases = [(i, -55, 1, captions[i]) for i in range(5)]
    cases += [(4,0,1,'Astralite — front / visor'),
              (4,-145,1.3,'Astralite — heavy build / rear'),
              (4,-85,.8,'Astralite — slim build / side'),
              (4,-30,1.25,'Astralite — Apex suit')]
    for index, (tier, yaw, build, label) in enumerate(cases):
        texture = Image.new('RGB', (512, 528), BG)
        texture.paste(fur, (0, 0))
        tex_draw = ImageDraw.Draw(texture)
        palette = (*TIERS[tier][1:], tuple(int(c*.65) for c in TIERS[tier][1]),
                   (93, 58, 39), (196, 143, 99), (76, 83, 92))
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
