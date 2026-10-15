"""Hand-shaped Bunnidog surfaces and reference-calibrated photographic projection.

The head outline is sampled from the user's blue contour, in reference pixels.
Depth, eyelids, ear folds, body sections and paw paths are authored separately.
No sphere/ellipsoid primitives are used to build the animal.
"""
import math
from pathlib import Path

import numpy as np
from PIL import Image

# Coordinates on the user's 396x541 annotated reference. Left/right below refer
# to the image, not to the creature. Keep their different cheek/jaw transitions.
HEAD = np.array([
    (75, 169, 173, .02), (78, 150, 199, .85), (85, 129, 222, 1.55),
    (98, 108, 247, 2.25), (112, 94, 265, 2.65), (130, 84, 279, 2.95),
    (148, 78, 289, 3.12), (167, 75, 296, 3.25), (187, 74, 294, 3.34),
    (210, 73, 293, 3.32), (230, 72, 292, 3.25), (250, 70, 291, 3.42),
    (271, 69, 288, 3.51), (291, 70, 285, 3.48), (311, 73, 280, 3.42),
    (327, 78, 273, 3.32), (340, 87, 260, 3.15), (348, 110, 236, 2.97),
    (356, 131, 214, 2.67), (365, 149, 197, 1.85), (369, 166, 181, .8),
    (370.2, 173, 174, .02),
], dtype=float)
SCALE = 10.45 / (369 - 75)
Y_ORIGIN = 15.25 + 75 * SCALE
STANCE_DROP = .5
HIP_HEIGHT = 5.6 - STANCE_DROP
ANKLE_HEIGHT = .7

# Horizontal bounds on the generated texture source; these are NOT the target
# silhouette. The source is warped to the traced reference rather than vice versa.
SOURCE = np.array([
    (163, 610, 630), (175, 545, 698), (200, 468, 758), (250, 399, 815),
    (300, 370, 841), (400, 347, 858), (500, 344, 862), (600, 346, 858),
    (700, 365, 839), (750, 405, 804), (790, 507, 733), (815, 601, 637),
], dtype=float)
Y_MAP = np.array([(75, 163), (150, 330), (210, 462), (228, 502),
                  (281, 616), (330, 723), (369, 813), (420, 970), (504, 1190)], dtype=float)
PHOTO = np.asarray(Image.open(Path(__file__).parent / 'reference/bunnidog_texture_source.png').convert('RGB'), dtype=float)
FUR = np.asarray(Image.open(Path(__file__).parent / 'reference/bunnidog_fur_source.png').convert('RGB'), dtype=float)


def catmull(values, t):
    """C1 interpolation through authored sections, with no analytic primitive."""
    values = np.asarray(values, dtype=float)
    t = max(0, min(len(values) - 1, t))
    i = min(len(values) - 2, int(t))
    f = t - i
    a, b, c, d = values[max(0, i - 1)], values[i], values[i + 1], values[min(len(values) - 1, i + 2)]
    return .5 * ((2 * b) + (-a + c) * f + (2 * a - 5 * b + 4 * c - d) * f * f + (-a + 3 * b - 3 * c + d) * f ** 3)


def profile(y, column):
    # Hermite slopes are clamped at extrema to keep the traced cheek corners.
    xs, ys = HEAD[:, 0], HEAD[:, column]
    i = int(np.clip(np.searchsorted(xs, y) - 1, 0, len(xs) - 2))
    span = xs[i + 1] - xs[i]
    f = (y - xs[i]) / span
    def slope(k):
        if k == 0:
            return (ys[1] - ys[0]) / (xs[1] - xs[0])
        if k == len(xs) - 1:
            return (ys[-1] - ys[-2]) / (xs[-1] - xs[-2])
        left = (ys[k] - ys[k - 1]) / (xs[k] - xs[k - 1])
        right = (ys[k + 1] - ys[k]) / (xs[k + 1] - xs[k])
        return 0 if left * right <= 0 else 2 * left * right / (left + right)
    return ((2 * f ** 3 - 3 * f * f + 1) * ys[i] + (f ** 3 - 2 * f * f + f) * span * slope(i) +
            (-2 * f ** 3 + 3 * f * f) * ys[i + 1] + (f ** 3 - f * f) * span * slope(i + 1))


def g(x, y, cx, cy, rx, ry):
    exponent = -2 * (((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2)
    return np.exp(exponent) if np.ndim(exponent) else math.exp(exponent)


def face_depth(px, py):
    # Recessed sockets, rolled lower lids and different cheek masses.
    d = 0
    for x, y, amount in ((111, 228, .67), (231, 229, .75)):
        d += amount * g(px, py, x, y, 26, 19)
        d -= .28 * g(px, py, x, y - 17, 27, 8)
        d -= .20 * g(px, py, x, y + 16, 25, 6)
        d += .10 * g(px, py, x + 3, y + 24, 23, 5)
        d -= .29 * g(px, py, x, y, 13, 8)
    d -= .67 * g(px, py, 108, 285, 37, 38)
    d -= .79 * g(px, py, 248, 287, 31, 36)
    # One continuous nose: root, bridge, alar wings, unequal nostril recesses.
    d -= .48 * g(px, py, 172, 259, 10, 25)
    # Broad rounded infant tip; depth must not turn it into a pointed beak.
    d -= .92 * g(px, py, 170, 279, 18, 14)
    d -= .56 * g(px, py, 155, 283, 8, 8)
    d -= .51 * g(px, py, 186, 283, 9, 8)
    d += .23 * g(px, py, 155, 288, 4.2, 3)
    d += .25 * g(px, py, 184, 289, 4, 3.2)
    # Philtrum, lip rolls, mouth slit and a chin offset from the forehead axis.
    d += .075 * g(px, py, 171, 309, 3.3, 15)
    d -= .065 * g(px, py, 165, 308, 3.2, 12)
    d -= .055 * g(px, py, 178, 309, 3, 12)
    d -= .25 * g(px, py, 169, 325, 26, 4.8)
    d += .08 * g(px, py, 170, 330, 27, 2.3)
    d -= .34 * g(px, py, 172, 337, 23, 5.5)
    d += .07 * g(px, py, 172, 345, 22, 3)
    d -= .38 * g(px, py, 170, 357, 27, 9)
    return d


def head_point(py, angle):
    left, right, depth = profile(py, 1), profile(py, 2), profile(py, 3)
    px = (left + right) / 2 + (right - left) / 2 * math.sin(angle)
    front = max(0, math.cos(angle))
    back = max(0, -math.cos(angle))
    z = -2.95 - depth * front ** .72 + depth * .82 * back ** .9
    if front > 0:
        z += face_depth(px, py) * front ** 1.1
    return ((181 - px) * SCALE, Y_ORIGIN - py * SCALE, z)


def head_normal(py, angle):
    # Mesh rings include closely spaced contour landmarks. Area-weighted face
    # normals bias each vertex toward its larger neighbor, creating a sharp
    # lighting jump between nearly coincident rings. Differentiate the authored
    # surface instead, so lighting does not depend on tessellation density.
    dp = np.subtract(head_point(py + .01, angle), head_point(py - .01, angle))
    da = np.subtract(head_point(py, angle + .0001), head_point(py, angle - .0001))
    n = np.cross(dp, da)
    return tuple(n / np.linalg.norm(n))


def head_chart_points(width, height):
    angles = np.linspace(-math.pi / 2, 3 * math.pi / 2, width)
    front = np.maximum(0, np.cos(angles))
    back = np.maximum(0, -np.cos(angles))
    rows = []
    for py in np.linspace(370.2, 75, height):
        left, right, depth = profile(py, 1), profile(py, 2), profile(py, 3)
        px = (left + right) / 2 + (right - left) / 2 * np.sin(angles)
        z = -2.95 - depth * front ** .72 + depth * .82 * back ** .9
        z += face_depth(px, py) * front ** 1.1
        rows.append(np.stack(((181 - px) * SCALE,
                              np.full(width, Y_ORIGIN - py * SCALE), z), axis=-1))
    return np.asarray(rows)


def define(bone, quad, tiles):
    # The former Low budget is now the sole authored model.
    (head_rows, sides, human_rows, human_sides, ear_rows, ear_cols,
     body_rows, body_sides, leg_rows, leg_sides, paw_rows, paw_sides,
     toe_rows, toe_sides, nail_rows, nail_sides, tail_rows, tail_sides,
     strands) = (20, 28, 3, 12, 8, 4, 10, 12, 7, 8, 4, 8, 2, 4, 2, 4, 4, 6, 0)
    bone('body', (0, 5.6, 1.7))
    bone('head', (.15, 8.2, -1.7))
    bone('ear_r', ((181 - 101) * SCALE, Y_ORIGIN - 143 * SCALE, -2.5), 'head')
    bone('ear_l', ((181 - 277) * SCALE, Y_ORIGIN - 136 * SCALE, -2.2), 'head')
    for name, x, z in (('leg_fr', 3.1, -1.4), ('leg_fl', -3.1, -1.35),
                        ('leg_br', 2.3, 4.3), ('leg_bl', -2.3, 4.2)):
        bone(name, (x, 5.6, z))
    bone('tail', (0, 5.6, 6))

    ys = np.unique(np.concatenate((np.linspace(75, 370.2, head_rows), HEAD[:, 0])))
    for j in range(len(ys) - 1):
        for i in range(sides):
            a, b = i * 2 * math.pi / sides - math.pi / 2, (i + 1) * 2 * math.pi / sides - math.pi / 2
            rec = quad(f'face_{j}_{i}', 'head',
                 (head_point(ys[j + 1], a), head_point(ys[j + 1], b), head_point(ys[j], b), head_point(ys[j], a)), 'face')
            if rec:
                rec['surface_normals'] = (head_normal(ys[j + 1], a), head_normal(ys[j + 1], b),
                                          head_normal(ys[j], b), head_normal(ys[j], a))
                rec['head_uv'] = (i / sides, (i + 1) / sides,
                                  (370.2 - ys[j + 1]) / 295.2, (370.2 - ys[j]) / 295.2)

    # Human pinnae: an irregular rolled helix, recessed concha and fleshy lobule.
    for side, sign, cy in (('r', 1, 9.15), ('l', -1, 8.98)):
        outline = np.array(((.1, .77), (.46, .78), (.69, .45), (.66, .03),
                            (.47, -.46), (.24, -.68), (-.05, -.52), (-.13, .12), (.1, .77)))
        def ear_point(r, a, back=False):
            q = catmull(outline, (a % (2 * math.pi)) / (2 * math.pi) * (len(outline) - 1))
            x = sign * (3.87 + q[0] * r)
            y = cy + q[1] * r * (1 if side == 'r' else .93)
            z = -3.28 + .22 * (1 - r) - .2 * math.exp(-((r - .79) / .13) ** 2)
            z -= .09 * g(q[0] * r, q[1] * r, -.03, -.1, .15, .25)
            if back:
                z = -3.08 + .12 * (1 - r)
            return x, y, z
        for back in (False, True):
            for j in range(human_rows):
                r0, r1 = .001 + .999 * j / human_rows, .001 + .999 * (j + 1) / human_rows
                for i in range(human_sides):
                    a, b = i * 2 * math.pi / human_sides, (i + 1) * 2 * math.pi / human_sides
                    points = (ear_point(r1, a, back), ear_point(r1, b, back), ear_point(r0, b, back), ear_point(r0, a, back))
                    # The reference outline runs clockwise in image space.
                    n = np.cross(np.subtract(points[2], points[0]), np.subtract(points[3], points[1]))
                    if (n[2] > 0) != back:
                        points = tuple(reversed(points))
                    quad(f'human_ear_{side}_{back}_{j}_{i}', 'head', points, 'human_ear')
        for i in range(human_sides):
            a, b = i * 2 * math.pi / human_sides, (i + 1) * 2 * math.pi / human_sides
            quad(f'helix_edge_{side}_{i}', 'head', (ear_point(1, a), ear_point(1, b), ear_point(1, b, True), ear_point(1, a, True)), 'human_ear')

    # Broad, fleshy pinnae from the photo: the image-left ear shows its tawny
    # outer face; the slightly taller right ear exposes a folded pink interior.
    # Both tips sit nearer the skull instead of splaying like narrow blades.
    ears = {
        'r': ((143, 101, 10, -.8), (119, 99, 18, -.8), (94, 92, 22, -.65),
              (70, 83, 19, -.45), (46, 77, 12, -.2), (29, 74, 1.8, .05)),
        'l': ((136, 277, 10, -.5), (112, 284, 17, -.35), (90, 293, 22, -.15),
              (67, 303, 21, .05), (44, 312, 16, .2), (28, 316, 8, .4),
              (18, 318, 1.8, .5)),
    }
    for side, sections in ears.items():
        def ear_surface(t, u, back=False):
            py, px, half, z = catmull(sections, t * (len(sections) - 1))
            px += u * half
            # A rolled rim and deep longitudinal cup give the ear real volume.
            z += .45 * (1 - u * u) * math.sin(math.pi * (.1 + .85 * t))
            z -= .12 * math.exp(-((abs(u) - .86) / .13) ** 2)
            z -= .09 * math.cos(t * math.pi)
            z += (.45 if side == 'r' else -.16) * u * (1 - .4 * t)
            z += .13 * math.sin(t * 9 + u * 2) * u * u
            z -= 1.7 * (1 - t) ** .6
            # A small curved terminal edge replaces the razor-thin needle tip.
            tip_rounding = max(0, (t - .8) / .2)
            py += 3 * tip_rounding * tip_rounding * u * u
            if back:
                # Front and back meet at the terminal edge to close the tip.
                z += .3 * (1 - .55 * t) * min(1, max(0, (1 - t) * 10))
            return ((181 - px) * SCALE, Y_ORIGIN - py * SCALE, z)
        for back in (False, True):
            for j in range(ear_rows):
                for i in range(ear_cols):
                    t0, t1, u0, u1 = j / ear_rows, (j + 1) / ear_rows, -1 + i * 2 / ear_cols, -1 + (i + 1) * 2 / ear_cols
                    points = (ear_surface(t1, u0, back), ear_surface(t1, u1, back), ear_surface(t0, u1, back), ear_surface(t0, u0, back))
                    if not back:
                        points = tuple(reversed(points))
                    rec = quad(f'rabbit_{side}_{back}_{j}_{i}', 'ear_' + side, points,
                               'ear_back' if back else 'rabbit_' + side)
                    if rec:
                        rec['ear_uv'] = (side, back, t0, t1, u0, u1)
        for edge in (-1, 1):
            for j in range(ear_rows):
                t0, t1 = j / ear_rows, (j + 1) / ear_rows
                points = (ear_surface(t1, edge), ear_surface(t0, edge), ear_surface(t0, edge, True), ear_surface(t1, edge, True))
                if edge == 1:
                    points = tuple(reversed(points))
                quad(f'ear_rim_{side}_{edge}_{j}', 'ear_' + side, points, 'ear_back')

    # The photo's flank silhouette projects beyond the head. Bring the sternum
    # forward under the jaw and fill the shoulders, not just the rear abdomen.
    sections = ((-4.05, 5.85, .18, .25), (-3.35, 5.8, 2.6, 1.6),
                (-2.2, 5.95, 3.9, 2.45), (-.35, 6.05, 4.5, 2.9),
                (1.8, 6, 4.25, 2.9), (3.8, 5.9, 3.75, 2.65),
                (5.4, 5.65, 2.9, 2.2), (6.35, 5.45, 1.3, 1.12), (6.65, 5.4, .1, .15))
    def torso(t, a):
        z, cy, rx, ry = catmull(sections, t * (len(sections) - 1))
        x = rx * math.sin(a)
        y = cy + ry * math.cos(a)
        # A spine groove and uneven skin/fur over the ribs, never regular bands.
        y += .045 * math.sin(z * 2.7 + x * 1.2) * math.sin(a) ** 2
        return x + .08 * math.sin(z), y, z
    for j in range(body_rows):
        for i in range(body_sides):
            a, b = i * 2 * math.pi / body_sides, (i + 1) * 2 * math.pi / body_sides
            points = (torso(j / body_rows, a), torso(j / body_rows, b), torso((j + 1) / body_rows, b), torso((j + 1) / body_rows, a))
            quad(f'torso_{j}_{i}', 'body', tuple(reversed(points)), 'body')

    # Broad, bent forelegs. Their wrists and paws follow different outward paths.
    limbs = {
        'leg_fr': ((7.1, 1.65, -1.15, .22, .35), (6.5, 2.45, -1.35, .67, .85),
                   (5.9, 2.8, -1.5, .95, 1.15), (4.9, 3.25, -1.65, 1.04, 1.05),
                   (3.75, 3.35, -1.95, .84, .9), (2.65, 3.7, -2.4, .63, .71),
                   (1.25, 4.1, -3.0, .55, .65), (.55, 4.35, -3.4, .64, .68)),
        'leg_fl': ((7.1, -1.65, -1.15, .22, .35), (6.5, -2.4, -1.35, .7, .87),
                   (5.9, -2.75, -1.5, 1.02, 1.2), (4.7, -3.2, -1.6, .99, 1.08),
                   (3.5, -3.35, -1.85, .82, .95), (2.3, -3.55, -2.35, .7, .75),
                   (1.15, -3.8, -2.85, .59, .67), (.55, -4.0, -3.25, .66, .7)),
        'leg_br': ((6.9, 1.5, 4.5, .2, .3), (6.3, 1.95, 4.6, .7, .9),
                   (5.7, 2.2, 4.6, 1.1, 1.5), (4.5, 2.65, 4.8, 1.28, 1.46),
                   (3.2, 2.6, 4.7, .85, 1.05), (2.1, 2.35, 5, .57, .7),
                   (.55, 2.7, 4.1, .55, .62)),
        'leg_bl': ((6.9, -1.5, 4.4, .2, .3), (6.3, -1.95, 4.5, .73, .93),
                   (5.7, -2.2, 4.5, 1.2, 1.5), (4.5, -2.7, 4.7, 1.22, 1.43),
                   (3.2, -2.6, 4.6, .86, 1.02), (2, -2.5, 4.8, .58, .68),
                   (.55, -2.9, 3.9, .55, .6)),
    }
    for name, rings in limbs.items():
        # Fill out the shoulders/thighs and forearms while keeping the wrists
        # narrower and the same planted paw positions. Root tips stay buried.
        def fuller_ring(ring):
            y, x, z, rx, rz = ring
            if y > 6.8:
                gain = 1.0
            elif y > 4.2:
                gain = 1.3
            elif y > 1.5:
                gain = 1.6 if name.startswith('leg_f') else 1.45
            else:
                gain = 1.45 if name.startswith('leg_f') else 1.3
            return y, x, z, rx * gain, rz * gain
        rings = tuple(fuller_ring(ring) for ring in rings)
        def leg(t, a):
            y, x, z, rx, rz = catmull(rings, t * (len(rings) - 1))
            crease = .025 * math.sin(y * 12 + a * 2) * math.exp(-((y - 1.4) / .65) ** 2)
            return x + (rx + crease) * math.sin(a), y, z - (rz + crease) * math.cos(a)
        for j in range(leg_rows):
            for i in range(leg_sides):
                a, b = i * 2 * math.pi / leg_sides, (i + 1) * 2 * math.pi / leg_sides
                quad(f'{name}_{j}_{i}', name, (leg(j / leg_rows, a), leg(j / leg_rows, b), leg((j + 1) / leg_rows, b), leg((j + 1) / leg_rows, a)), 'leg')
        _, x, z, _, _ = rings[-1]
        sign = 1 if x > 0 else -1
        front = name.startswith('leg_f')
        yaw = sign * (.49 if name == 'leg_fr' else .37 if front else .12)
        def paw_point(lateral, forward, y):
            lateral *= 1.25
            forward *= 1.15
            y *= 1.12
            return (x + math.cos(yaw) * lateral + math.sin(yaw) * forward,
                    y, z + math.sin(yaw) * lateral - math.cos(yaw) * forward)
        # Dorsum is one low irregular pad, not a flattened sphere.
        paw_sections = ((-.55, .6, .4, .15), (-.1, .86, .36, .34),
                        (.4, .91, .3, .3), (.8, .75, .25, .25), (1.05, .45, .22, .21))
        def paw(t, a):
            f, w, cy, h = catmull(paw_sections, t * (len(paw_sections) - 1))
            return paw_point(w * math.sin(a), f, max(0, cy + h * math.cos(a)))
        for j in range(paw_rows):
            for i in range(paw_sides):
                a, b = i * 2 * math.pi / paw_sides, (i + 1) * 2 * math.pi / paw_sides
                quad(f'{name}_paw_{j}_{i}', name, (paw(j / paw_rows, a), paw(j / paw_rows, b), paw((j + 1) / paw_rows, b), paw((j + 1) / paw_rows, a)), 'paw')
        for toe, (lateral, length, width) in enumerate(((-.67, .73, .25), (-.22, 1, .28), (.25, .93, .27), (.67, .63, .23))):
            def digit(t, a):
                forward = .43 + t * length
                bend = lateral + .13 * math.sin(t * 1.7) * (1 if lateral > 0 else -1)
                radius = width * (.78 + .22 * math.sin(t * math.pi))
                if t > .86:
                    radius *= max(.02, (1 - t) / .14)
                return paw_point(bend + radius * math.sin(a), forward, .22 + .22 * math.cos(a) * (.95 if t > .85 else 1))
            for j in range(toe_rows):
                for i in range(toe_sides):
                    a, b = i * 2 * math.pi / toe_sides, (i + 1) * 2 * math.pi / toe_sides
                    quad(f'{name}_toe_{toe}_{j}_{i}', name, (digit(j / toe_rows, a), digit(j / toe_rows, b), digit((j + 1) / toe_rows, b), digit((j + 1) / toe_rows, a)), 'paw')
            # Short curved nail emerging from each toe, with a darker root.
            start = paw_point(lateral, .43 + length * .84, .23)
            end = paw_point(lateral, .43 + length + .17, .075)
            nail_sections = ((0, .095), (.45, .08), (1, .008))
            for j in range(nail_rows):
                def nail(t, a):
                    r = max(.008, .095 * (1 - t) ** .7)
                    center = tuple(start[k] * (1 - t) + end[k] * t for k in range(3))
                    return (center[0] + r * math.sin(a), center[1] + r * math.cos(a), center[2])
                for i in range(nail_sides):
                    a, b = i * 2 * math.pi / nail_sides, (i + 1) * 2 * math.pi / nail_sides
                    quad(f'{name}_nail_{toe}_{j}_{i}', name, (nail(j / nail_rows, a), nail(j / nail_rows, b), nail((j + 1) / nail_rows, b), nail((j + 1) / nail_rows, a)), 'nail')

    # Short, naturally tapering dog tail, mostly hidden from the frontal photo.
    def tail(t, a):
        r = .55 * (1 - t) + .035
        return (.18 * math.sin(t * 2) + r * math.sin(a), 5.6 + t * 1.05 + r * math.cos(a), 6 + t * 1.8)
    for j in range(tail_rows):
        for i in range(tail_sides):
            a, b = i * 2 * math.pi / tail_sides, (i + 1) * 2 * math.pi / tail_sides
            quad(f'tail_{j}_{i}', 'tail', (tail((j + 1) / tail_rows, a), tail((j + 1) / tail_rows, b), tail(j / tail_rows, b), tail(j / tail_rows, a)), 'fur')



def compact_stance(parts, tiles):
    """Lower the animal and shorten the exposed legs while keeping feet planted.

    Painting keeps the reference-space coordinates, so changing the stance does
    not slide the facial features, chest skin or fur across the sculpt.
    """
    leg_ratio = (HIP_HEIGHT - ANKLE_HEIGHT) / (5.6 - ANKLE_HEIGHT)
    def lowered(name, point):
        x, y, z = point
        if name.startswith('leg_'):
            if y > ANKLE_HEIGHT:
                y = ANKLE_HEIGHT + (y - ANKLE_HEIGHT) * leg_ratio
        else:
            y -= STANCE_DROP
        return x, y, z
    for part in parts:
        part['pivot'] = lowered(part['name'], part['pivot'])
    for tile in tiles:
        tile['paint_corners'] = tile['corners']
        tile['corners'] = tuple(lowered(tile['parent'], p) for p in tile['corners'])


def source_sample(x, y, source=PHOTO):
    x, y = np.clip(x, 0, source.shape[1] - 1.001), np.clip(y, 0, source.shape[0] - 1.001)
    xi, yi = np.floor(x).astype(int), np.floor(y).astype(int)
    fx, fy = (x - xi)[..., None], (y - yi)[..., None]
    return ((source[yi, xi] * (1 - fx) + source[yi, xi + 1] * fx) * (1 - fy) +
            (source[yi + 1, xi] * (1 - fx) + source[yi + 1, xi + 1] * fx) * fy)


def face_source(points):
    px = 181 - points[..., 0] / SCALE
    py = (Y_ORIGIN - points[..., 1]) / SCALE
    sy = np.interp(py, Y_MAP[:, 0], Y_MAP[:, 1])
    left = np.interp(py, HEAD[:, 0], HEAD[:, 1])
    right = np.interp(py, HEAD[:, 0], HEAD[:, 2])
    sl = np.interp(sy, SOURCE[:, 0], SOURCE[:, 1])
    sr = np.interp(sy, SOURCE[:, 0], SOURCE[:, 2])
    sx = sl + (px - left) / np.maximum(1, right - left) * (sr - sl)
    # The reference's image-left eye sits farther toward the temple. Anchor
    # the source landmarks to that individual placement, not a mirrored pair.
    sx += 15 * np.exp(-2 * (((px - 112) / 28) ** 2 + ((py - 228) / 24) ** 2))
    return sx, sy


def mirror(v):
    return 1 - np.abs((v % 2) - 1)


def surface_color(material, points, tile, fx, fy):
    """Bilinear photographic projection, with coat tint only on actual fur."""
    x, y, z = points[..., 0], points[..., 1], points[..., 2]
    shape = x.shape
    fur_x = ((x * .16 + z * .13) % 1) * (FUR.shape[1] - 1)
    fur_y = ((-y * .18 + z * .03) % 1) * (FUR.shape[0] - 1)
    fur = source_sample(fur_x, fur_y, FUR)
    weight = np.ones(shape)
    if material in ('face', 'human_ear'):
        sx, sy = face_source(points)
        color = source_sample(sx, sy)
        if material == 'face':
            # A frontal photograph contains pale edge hairs/background at the
            # silhouette. Blending by depth alone stretches that fringe into a
            # bright vertical stripe on the temple. Fade it BEFORE the photo's
            # edge, using the traced head width at this height as well as depth.
            py = (Y_ORIGIN - y) / SCALE
            left = np.interp(py, HEAD[:, 0], HEAD[:, 1])
            right = np.interp(py, HEAD[:, 0], HEAD[:, 2])
            px = 181 - x / SCALE
            lateral = np.abs((2 * px - left - right) / np.maximum(1, right - left))
            edge = np.clip((lateral - .76) / .22, 0, 1)
            depth = np.clip((z + 4.0) / 1.2, 0, 1)
            # Smoothstep has no sudden change in slope at either end.
            edge = edge * edge * (3 - 2 * edge)
            depth = depth * depth * (3 - 2 * depth)
            side = (1 - (1 - edge) * (1 - depth))[..., None]
            skin = source_sample(540 + mirror(x * .12 + z * .07) * 110,
                                 245 + mirror(y * .11 + z * .03) * 115)
            color = color * (1 - side) + skin * side
        else:
            background = ((color.min(axis=-1) > 185) & (color.max(axis=-1) - color.min(axis=-1) < 28))[..., None]
            color = np.where(background, (222, 171, 149), color)
        weight[:] = 0
    elif material.startswith('rabbit_'):
        side = material[-1]
        # Reconstruct t/u at the corners in the same order as the front patch.
        _, _, t0, t1, u0, u1 = tile['ear_uv']
        t = t0 * (1 - fy) + t1 * fy
        u = u0 * (1 - fx) + u1 * fx
        if side == 'r':
            sy = 273 - t * 230
            cx = np.interp(t, (0, .35, .7, 1), (432, 392, 354, 337))
            width = np.interp(t, (0, .3, .7, 1), (28, 48, 37, 2))
        else:
            sy = 330 - t * 316
            cx = np.interp(t, (0, .35, .7, 1), (838, 887, 929, 952))
            width = np.interp(t, (0, .25, .65, 1), (18, 47, 38, 2))
        color = source_sample(cx + u * width * .88, sy)
        weight = np.clip(np.abs(u) * 2.2 - .8, 0, 1)
    elif material in ('ear_back', 'fur', 'hair'):
        color = fur
    elif material == 'nail':
        grain = .5 + .5 * np.sin(x * 36 + z * 21)
        color = np.stack((107 + grain * 25, 94 + grain * 22, 74 + grain * 18), axis=-1)
        weight[:] = 0
    else:
        # Front-facing torso/limb surfaces inherit the fine hair and folds from
        # the photo; rear surfaces use a real fur patch from the same source.
        px = 181 - x / SCALE
        py = (Y_ORIGIN - y) / SCALE
        sy = np.interp(py, Y_MAP[:, 0], Y_MAP[:, 1])
        sx = 620 + (px - 181) * 2.77
        projected = source_sample(sx, sy)
        use = np.clip((-z - .6) / 1.5, 0, 1)
        if material == 'body':
            use = np.clip((-z - .8) / 1.1, 0, 1)
        if material == 'paw':
            # A wrinkled paw-skin patch avoids projecting the floor between toes.
            name = tile['parent']
            cx, cz, yaw, sx, sy = {
                'leg_fr': (4.35, -3.4, .49, 270, 1105),
                'leg_fl': (-4, -3.25, -.37, 955, 1120),
                'leg_br': (2.7, 4.1, .12, 955, 1120),
                'leg_bl': (-2.9, 3.9, -.12, 955, 1120),
            }[name]
            lateral = math.cos(yaw) * (x - cx) + math.sin(yaw) * (z - cz)
            forward = math.sin(yaw) * (x - cx) - math.cos(yaw) * (z - cz)
            paw_x = sx - lateral * 88 + forward * (25 if cx < 0 else -25)
            paw_y = sy + forward * 55 - y * 20
            projected = source_sample(paw_x, paw_y)
            use[:] = 1
        background = np.clip((projected.min(axis=-1) - 190) / 25, 0, 1) * np.clip(1 - (projected.max(axis=-1) - projected.min(axis=-1)) / 40, 0, 1)
        replacement = np.broadcast_to((172, 137, 108), projected.shape) if material == 'paw' else fur
        projected = projected * (1 - background[..., None]) + replacement * background[..., None]
        if material == 'paw':
            gray = np.clip((23 - (projected[..., 0] - projected[..., 1])) / 12, 0, 1)
            brown = np.array((171, 135, 104)) * np.clip(projected.mean(axis=-1) / 153, .7, 1.2)[..., None]
            projected = projected * (1 - gray[..., None]) + brown * gray[..., None]
            projected *= np.minimum(1, 160 / np.maximum(1, projected.mean(axis=-1)))[..., None]
        color = projected * use[..., None] + fur * (1 - use[..., None])
        # Pink/sparsely-haired chest and inner foreleg are not coat-swapped.
        pink = np.clip((color[..., 2] / np.maximum(1, color[..., 1]) - .78) * 8, 0, 1)
        weight = 1 - pink * use
    # Colors are linear-ish photographic RGB; no drawn eyes, mouths or stripes.
    return np.clip(color, 0, 255), weight
