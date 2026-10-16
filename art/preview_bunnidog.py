#!/usr/bin/env python3
"""Render the actual generated Bunnidog mesh/atlas, without Minecraft or Blender.

    python3 art/preview_bunnidog.py

Requires Pillow and NumPy. No geometry or textures are approximated/redrawn.
"""
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from gen_bunnidog import PARTS, ROOT, TILES, build_geometry, rotation
from sculpt_bunnidog import HIP_HEIGHT


def world_faces(ear_scale=1, leg_scale=1, build_scale=1, baby=False):
    build_geometry()
    bones = {p['name']: p for p in PARTS}
    matrices = {}
    lift = HIP_HEIGHT * (leg_scale - 1)
    def matrix(name):
        if name in matrices:
            return matrices[name]
        p = bones[name]
        scale = np.eye(3)
        if name.startswith('ear_'):
            scale = np.diag((1 + (ear_scale - 1) * .4, ear_scale, 1))
        elif name.startswith('leg_'):
            thick = 1 + (build_scale - 1) * .6
            scale = np.diag((thick, leg_scale, thick))
        elif name == 'body':
            scale = np.diag((build_scale, 1 + (build_scale - 1) * .6, 1 + (build_scale - 1) * .3))
        elif name == 'head' and baby:
            scale = np.eye(3) * 1.08
        r = np.asarray(rotation(p['rot'])) @ scale
        pivot = np.asarray(p['pivot'], dtype=float)
        offset = pivot - r @ pivot
        if p['parent']:
            pr, po = matrix(p['parent'])
            r, offset = pr @ r, pr @ offset + po
        else:
            offset[1] += lift
        matrices[name] = r, offset
        return r, offset
    faces = []
    for t in TILES:
        r, off = matrix(t['parent'])
        corners = np.asarray(t['corners']) @ r.T + off
        if baby:
            corners *= .65
        a, b = corners[2] - corners[0], corners[3] - corners[1]
        normal = np.cross(a, b)
        normal /= np.linalg.norm(normal)
        vertex_normals = np.asarray(t['normals']) @ np.linalg.inv(r)
        vertex_normals /= np.linalg.norm(vertex_normals, axis=1)[:, None]
        faces.append((corners, normal, t['uv'], t['size'], vertex_normals))
    return faces


def render(faces, texture, yaw=0, elevation=7, width=650, height=820, zoom=35, plain=False, nearest=False,
           scene_light=1.0, background_rgb=None):
    yaw, elevation = math.radians(yaw), math.radians(elevation)
    camera = np.array((math.sin(yaw) * math.cos(elevation), math.sin(elevation), -math.cos(yaw) * math.cos(elevation)))
    right = np.cross((0, 1, 0), camera)
    right /= np.linalg.norm(right)
    up = np.cross(camera, right)
    basis = np.asarray((right, -up, camera))
    center = np.array((0, 9.4, -.3))
    background = np.array(background_rgb if background_rgb is not None else (255,255,255) if plain else (238,234,226),dtype=float)
    pixels = np.full((height, width, 3), background)
    depth = np.full((height, width), -np.inf)
    # A soft ground contact shadow, outside the model geometry.
    if not plain:
        yy, xx = np.mgrid[:height, :width]
        ground_y = height / 2 + (9.4 * math.cos(elevation)) * zoom
        shadow = np.exp(-2 * (((xx - width / 2) / (zoom * 4.7)) ** 2 + ((yy - ground_y) / (zoom * .65)) ** 2))
        pixels *= 1 - shadow[:, :, None] * .15
    light = np.array((-.4, .8, -.65))
    light /= np.linalg.norm(light)
    for corners, normal, uv, size, vertex_normals in faces:
        if normal @ camera < -.02:
            continue
        projected = (corners - center) @ basis.T
        projected[:, :2] = projected[:, :2] * zoom + (width / 2, height / 2)
        u, v = uv
        w, h = size
        texcoords = np.asarray(((u, v), (u + w, v), (u + w, v + h), (u, v + h)))
        for indices in ((0, 1, 2), (0, 2, 3)):
            tri, tex = projected[list(indices)], texcoords[list(indices)]
            tri_normals = vertex_normals[list(indices)]
            lo = np.maximum((0, 0), np.floor(tri[:, :2].min(axis=0)).astype(int))
            hi = np.minimum((width - 1, height - 1), np.ceil(tri[:, :2].max(axis=0)).astype(int))
            if np.any(lo > hi):
                continue
            x0, y0, x1, y1 = tri[0, 0], tri[0, 1], tri[1, 0], tri[1, 1]
            x2, y2 = tri[2, :2]
            denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
            if abs(denom) < 1e-8:
                continue
            ys, xs = np.mgrid[lo[1]:hi[1] + 1, lo[0]:hi[0] + 1]
            xs, ys = xs + .5, ys + .5
            a = ((y1 - y2) * (xs - x2) + (x2 - x1) * (ys - y2)) / denom
            b = ((y2 - y0) * (xs - x2) + (x0 - x2) * (ys - y2)) / denom
            c = 1 - a - b
            z = a * tri[0, 2] + b * tri[1, 2] + c * tri[2, 2]
            region = np.s_[lo[1]:hi[1] + 1, lo[0]:hi[0] + 1]
            keep = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6) & (z > depth[region])
            if not np.any(keep):
                continue
            ut = a * tex[0, 0] + b * tex[1, 0] + c * tex[2, 0]
            vt = a * tex[0, 1] + b * tex[1, 1] + c * tex[2, 1]
            # Bilinear sampling for the preview (the game controls its own filtering).
            ut, vt = ut - .5, vt - .5
            ui, vi = np.floor(ut).astype(int), np.floor(vt).astype(int)
            uf, vf = ut - ui, vt - vi
            ui = np.clip(ui, 0, texture.shape[1] - 2)
            vi = np.clip(vi, 0, texture.shape[0] - 2)
            col = ((texture[vi, ui, :3] * (1 - uf[:, :, None]) + texture[vi, ui + 1, :3] * uf[:, :, None]) * (1 - vf[:, :, None]) +
                   (texture[vi + 1, ui, :3] * (1 - uf[:, :, None]) + texture[vi + 1, ui + 1, :3] * uf[:, :, None]) * vf[:, :, None])
            if nearest:
                col = texture[np.clip(np.floor(vt + .5).astype(int), 0, texture.shape[0] - 1),
                              np.clip(np.floor(ut + .5).astype(int), 0, texture.shape[1] - 1), :3]
            depth[region][keep] = z[keep]
            n = a[..., None] * tri_normals[0] + b[..., None] * tri_normals[1] + c[..., None] * tri_normals[2]
            n /= np.maximum(.001, np.linalg.norm(n, axis=2))[..., None]
            shade = np.ones(a.shape) if plain else (.87 + .13 * np.maximum(0, n @ light))
            pixels[region][keep] = col[keep] * shade[keep, None] * scene_light
    return Image.fromarray(np.clip(pixels, 0, 255).astype('uint8'))


def font(size):
    try:
        return ImageFont.truetype('DejaVuSans.ttf', size)
    except OSError:
        return ImageFont.load_default(size=size)


def main():
    root = Path(ROOT)
    texture = np.asarray(Image.open(root / 'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'), dtype=float)
    faces = world_faces()
    views = [(0, 4, 'FRENTE'), (-34, 10, 'TRES CUARTOS'), (-82, 6, 'PERFIL')]
    sheet = Image.new('RGB', (1800, 910), (238, 234, 226))
    draw = ImageDraw.Draw(sheet)
    draw.text((48, 30), 'BUNNIDOG', fill=(43, 42, 38), font=font(34))
    draw.text((48, 78), 'Vista previa de la geometría y textura del mod', fill=(108, 102, 92), font=font(19))
    for i, (yaw, elevation, label) in enumerate(views):
        img = render(faces, texture, yaw, elevation, 600, 760, 32)
        sheet.paste(img, (i * 600, 110))
        draw.text((i * 600 + 42, 862), label, fill=(108, 102, 92), font=font(17))
    path = root / 'art/bunnidog_model_preview.png'
    sheet.save(path)
    render(faces, texture, 0, 4, 900, 1080, 49).save(root / 'art/bunnidog_front.png')
    render(faces, texture, -45, 22, 850, 1000, 43).save(root / 'art/bunnidog_temple_fixed.png')
    render(faces, texture, -78, 2, 850, 1000, 43).save(root / 'art/bunnidog_face_depth.png')
    # Match the game's default nearest filtering when reviewing seams.
    render(faces, texture, 40, 20, 700, 900, 40, nearest=True).save(root / 'art/bunnidog_smooth_head.png')
    print(path)


if __name__ == '__main__':
    main()
