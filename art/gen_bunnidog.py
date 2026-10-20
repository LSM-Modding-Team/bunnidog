#!/usr/bin/env python3
"""Reference-led Bunnidog art pipeline: rounded baby face, rabbit ears, dog anatomy.

    python3 art/gen_bunnidog.py  (Pillow)
    python3 art/preview_bunnidog.py  (Pillow + NumPy; actual exported geometry)

Generates the lightweight model with twelve 512px coat atlases, editable art/bunnidog.bbmodel and the
vanilla BunnidogModelLayer.java. Geometry, UVs and painting share this source.
Coordinates: Blockbench Y-up, feet at 0, face toward -Z. Java Y-down, ground 24.
Animation, gene scales and baby proportions stay in BunnidogModel.java.
"""
import base64, io, json, math, os, uuid
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX_W = TEX_H = 512
HEAD_W, HEAD_H = 508, 224
HEAD_X = HEAD_Y = 2
MODID = "bunnidogs"
JAVA_PKG = "com.bunnidogs.mod.client.model"

# Coat variants = fur palette swaps (the baby-like face is fixed and never changes).
# base/dark/light are the fur colours. Optional keys: "patches" (a second colour painted as patches on the back,
# flanks and one ear), "legs" (colour of the four legs, default = base) and "hoof" (colour of the foot pads, default = the
# pale skin shade). Coat names MUST match the Coat alleles in genetics/Gene.java (tools/GenomeCheck.java checks it).
# First four = ordinary and mutation coats; the rest belong to wild lineages (see Gene.java / Founders.java).
COATS = {
    "tan":     dict(base=(224, 180, 112, 255), dark=(200, 155, 92, 255), light=(238, 204, 148, 255)),
    "cream":   dict(base=(240, 224, 186, 255), dark=(216, 198, 158, 255), light=(248, 238, 210, 255)),
    "brown":   dict(base=(139, 97, 63, 255), dark=(110, 75, 48, 255), light=(168, 126, 90, 255)),
    "spotted": dict(base=(224, 180, 112, 255), dark=(200, 155, 92, 255), light=(238, 204, 148, 255),
                    patches=(150, 104, 66, 255)),
    # ---- wild lineages ----
    "snow":    dict(base=(238, 243, 250, 255), dark=(206, 217, 234, 255), light=(252, 253, 255, 255)),          # tundra
    "sand":    dict(base=(232, 208, 148, 255), dark=(206, 180, 120, 255), light=(246, 230, 184, 255)),          # desert
    "moss":    dict(base=(112, 132, 74, 255), dark=(84, 102, 54, 255), light=(142, 162, 98, 255)),              # swamp
    "glow":    dict(base=(72, 150, 140, 255), dark=(48, 118, 112, 255), light=(132, 226, 208, 255),             # swamp (recessive)
                    patches=(176, 255, 236, 255)),
    "pale":    dict(base=(202, 202, 212, 255), dark=(170, 170, 186, 255), light=(228, 228, 236, 255)),          # cave
    "slate":   dict(base=(122, 126, 134, 255), dark=(94, 98, 106, 255), light=(152, 156, 164, 255),             # mountain
                    legs=(84, 88, 96, 255), hoof=(44, 44, 50, 255)),
    "ember":   dict(base=(142, 52, 38, 255), dark=(108, 34, 28, 255), light=(196, 92, 50, 255),                 # nether
                    patches=(246, 152, 42, 255)),
    "void":    dict(base=(46, 32, 68, 255), dark=(30, 20, 48, 255), light=(72, 52, 102, 255),                   # end
                    patches=(202, 84, 224, 255)),
    "seal":    dict(base=(113, 123, 124, 255), dark=(62, 76, 80, 255), light=(185, 192, 181, 255),             # isolated ocean refuge
                    patches=(64, 77, 81, 255)),
}

# Exports share the hand-shaped surfaces in sculpt_bunnidog.py.
ART_SCALE = 8
PARTS = []
TILES = []


def bone(name, pivot, parent=None, rot=(0, 0, 0)):
    PARTS.append(dict(name=name, pivot=pivot, parent=parent, rot=rot))


def quad(name, parent, corners, material):
    import numpy as np
    c = np.asarray(corners)
    n = np.cross(c[2] - c[0], c[3] - c[1])
    if np.linalg.norm(n) < 1e-10:
        return None
    def distance(a, b):
        return math.sqrt(sum((a[k] - b[k]) ** 2 for k in range(3)))
    w = max(2, math.ceil(max(distance(corners[0], corners[1]), distance(corners[2], corners[3])) * ART_SCALE))
    h = max(2, math.ceil(max(distance(corners[0], corners[3]), distance(corners[1], corners[2])) * ART_SCALE))
    if material == 'hair':
        w, h = 2, 4
    rec = dict(name=name, parent=parent, corners=corners, size=(w, h), material=material)
    TILES.append(rec)
    return rec


def build_geometry():
    if PARTS:
        return
    from sculpt_bunnidog import define, compact_stance
    define(bone, quad, TILES)
    compact_stance(PARTS, TILES)
    smooth_normals()
    pack_uvs()


def smooth_normals():
    import numpy as np
    normals = {}
    def key(t, p):
        return (t['parent'], *(round(float(v), 5) for v in p))
    for t in TILES:
        c = np.asarray(t['corners'])
        n = np.cross(c[2] - c[0], c[3] - c[1])
        t['normal'] = tuple(n / np.linalg.norm(n))
        for p in c:
            k = key(t, p)
            normals[k] = normals.get(k, np.zeros(3)) + n
    for t in TILES:
        if 'surface_normals' in t:
            t['normals'] = t['surface_normals']
            continue
        t['normals'] = []
        for p in t['corners']:
            n = np.asarray(t['normal']) if t.get('flat') else normals[key(t, p)]
            length = np.linalg.norm(n)
            t['normals'].append(tuple(n / length if length > 1e-8 else t['normal']))


def pack_uvs():
    x, y, shelf = 2, HEAD_H + 4, 0
    # Height-sorted shelves avoid wasting most of the compact atlas on
    # a few tall patches. Face order in the mesh remains unchanged.
    for t in TILES:
        if t['material'] == 'face':
            u0, u1, v0, v1 = t['head_uv']
            t['uv'] = (HEAD_X + .5 + u0 * (HEAD_W - 1), HEAD_Y + .5 + v0 * (HEAD_H - 1))
            t['size'] = ((u1 - u0) * (HEAD_W - 1), (v1 - v0) * (HEAD_H - 1))
    for t in sorted((q for q in TILES if q['material'] != 'face'),
                    key=lambda tile: (-tile['size'][1], -tile['size'][0])):
        w, h = t['size']
        tw, th = w + 4, h + 4
        if x + tw >= TEX_W:
            x, y, shelf = 2, y + shelf, 0
        if y + th >= TEX_H:
            raise ValueError('Texture atlas overflow')
        t['uv'] = (x + 1, y + 1)
        x += tw
        shelf = max(shelf, th)


def rotation(rot):
    rx, ry, rz = [math.radians(v) for v in rot]
    sx, cx, sy, cy, sz, cz = math.sin(rx), math.cos(rx), math.sin(ry), math.cos(ry), math.sin(rz), math.cos(rz)
    return ((cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx),
            (sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx),
            (-sy, cy * sx, cy * cx))


_BASE_ATLAS = None
_COAT_WEIGHT = None
_PATCH_WEIGHT = None


def paint_texture(coat='tan'):
    import numpy as np
    from sculpt_bunnidog import surface_color
    global _BASE_ATLAS, _COAT_WEIGHT, _PATCH_WEIGHT
    if not PARTS:
        build_geometry()
    if _BASE_ATLAS is None:
        _BASE_ATLAS = np.zeros((TEX_H, TEX_W, 4), dtype=np.uint8)
        _COAT_WEIGHT = np.zeros((TEX_H, TEX_W), dtype=np.float32)
        _PATCH_WEIGHT = np.zeros((TEX_H, TEX_W), dtype=np.float32)
        from sculpt_bunnidog import head_chart_points
        points = head_chart_points(HEAD_W, HEAD_H)
        color, _ = surface_color('face', points, {}, None, None)
        # One continuous chart, including duplicated boundary texels. Adjacent
        # head polygons now address the same pixels with nearest filtering.
        head = np.pad(color.astype(np.uint8), ((1, 1), (1, 1), (0, 0)), mode='edge')
        _BASE_ATLAS[HEAD_Y-1:HEAD_Y+HEAD_H+1, HEAD_X-1:HEAD_X+HEAD_W+1, :3] = head
        _BASE_ATLAS[HEAD_Y-1:HEAD_Y+HEAD_H+1, HEAD_X-1:HEAD_X+HEAD_W+1, 3] = 255
        for t in TILES:
            if t['material'] == 'face':
                continue
            w, h = t['size']; u, v = t['uv']
            qy, qx = np.mgrid[-1:h+1, -1:w+1]
            fx, fy = np.clip((qx + .5) / w, 0, 1), np.clip((qy + .5) / h, 0, 1)
            corners = np.asarray(t.get('paint_corners', t['corners']))
            points = ((1 - fy[..., None]) * ((1 - fx[..., None]) * corners[0] + fx[..., None] * corners[1]) +
                       fy[..., None] * ((1 - fx[..., None]) * corners[3] + fx[..., None] * corners[2]))
            color, weight = surface_color(t['material'], points, t, fx, fy)
            _BASE_ATLAS[v-1:v+h+1, u-1:u+w+1, :3] = color.astype(np.uint8)
            _BASE_ATLAS[v-1:v+h+1, u-1:u+w+1, 3] = 255
            _COAT_WEIGHT[v-1:v+h+1, u-1:u+w+1] = weight
            spot = np.sin(points[..., 0] * 1.3 + points[..., 2] * .8) * np.cos(points[..., 2] * 1.6 - points[..., 1] * .7)
            _PATCH_WEIGHT[v-1:v+h+1, u-1:u+w+1] = np.clip((spot - .35) * 5, 0, 1) * weight
    result = _BASE_ATLAS.copy()
    if coat != 'tan':
        pal = COATS[coat]
        ratio = np.array(pal['base'][:3]) / np.array(COATS['tan']['base'][:3])
        recolored = np.clip(result[:, :, :3].astype(float) * ratio, 0, 255)
        weight = _COAT_WEIGHT[..., None]
        result[:, :, :3] = (result[:, :, :3] * (1 - weight) + recolored * weight).astype(np.uint8)
        if pal.get('patches'):
            patch = _PATCH_WEIGHT[..., None]
            result[:, :, :3] = (result[:, :, :3] * (1 - patch) + np.array(pal['patches'][:3]) * patch).astype(np.uint8)
    return Image.fromarray(result)


def stable_uuid(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'bunnidogs/model/' + name))


def build_bbmodel(img):
    buf = io.BytesIO()
    img.save(buf, 'PNG')
    groups, elements = {}, []
    for part in PARTS:
        groups[part['name']] = dict(name=part['name'], origin=part['pivot'], rotation=part['rot'],
                                    uuid=stable_uuid(part['name']), children=[], export=True,
                                    isOpen=False, visibility=not (part['name'].endswith('_web') or part['name'].startswith('fertility_')))
        vertices, faces = {}, {}
        for ti, t in enumerate(q for q in TILES if q['parent'] == part['name']):
            keys = [f'v{ti}_{i}' for i in range(4)]
            vertices.update(zip(keys, t['corners']))
            u, v = t['uv']
            w, h = t['size']
            uv = dict(zip(keys, ((u, v), (u + w, v), (u + w, v + h), (u, v + h))))
            faces[f'f{ti}'] = dict(vertices=keys, uv=uv, texture=0)
        element = dict(name=part['name'] + '_surface', type='mesh',
                       uuid=stable_uuid(part['name'] + '_surface'),
                       origin=(0, 0, 0), rotation=(0, 0, 0), vertices=vertices, faces=faces)
        elements.append(element)
        groups[part['name']]['children'].append(element['uuid'])
    outliner = []
    for part in PARTS:
        if part['parent']:
            groups[part['parent']]['children'].append(groups[part['name']])
        else:
            outliner.append(groups[part['name']])
    return dict(meta=dict(format_version='4.10', model_format='free', box_uv=False),
                name='bunnidog', model_identifier='bunnidog',
                resolution=dict(width=TEX_W, height=TEX_H), elements=elements,
                outliner=outliner, textures=[dict(name='bunnidog_tan.png', id='0',
                    uuid=stable_uuid('texture'), mode='bitmap', saved=True,
                    source='data:image/png;base64,' + base64.b64encode(buf.getvalue()).decode())])


def fnum(v):
    s = '%g' % (round(float(v), 5) + 0.0)
    if '.' not in s and 'e' not in s:
        s += '.0'
    return s + 'F'


def build_java():
    from sculpt_bunnidog import HIP_HEIGHT
    lines = ['// GENERATED by art/gen_bunnidog.py. Edit the generator, not this file.',
             'package ' + JAVA_PKG + ';', '',
             'import net.minecraft.client.model.geom.ModelPart;',
             'import net.minecraft.client.model.geom.PartPose;',
             'import net.minecraft.client.model.geom.builders.CubeListBuilder;',
             'import net.minecraft.client.model.geom.builders.LayerDefinition;',
             'import net.minecraft.client.model.geom.builders.MeshDefinition;',
             'import net.minecraft.client.model.geom.builders.PartDefinition;', '',
             'public final class BunnidogModelLayer {',
             f'    public static final float LEG_LENGTH = {fnum(HIP_HEIGHT)};',
             '    private BunnidogModelLayer() {}', '',
             '    public static LayerDefinition createBodyLayer() {',
             '        MeshDefinition mesh = new MeshDefinition();',
             '        PartDefinition root = mesh.getRoot();']
    by_name = {p['name']: p for p in PARTS}
    for p in PARTS:
        parent = by_name.get(p['parent'])
        pp = parent['pivot'] if parent else (0, 24, 0)
        off = [(p['pivot'][k] - pp[k]) * (-1 if k == 1 else 1) for k in range(3)]
        angles = [math.radians(p['rot'][k]) * (-1 if k != 1 else 1) for k in range(3)]
        pose = ', '.join(fnum(v) for v in off + angles)
        lines.append(f'        PartDefinition {p["name"]} = {(p["parent"] or "root")}.addOrReplaceChild("{p["name"]}",')
        lines.append(f'                CubeListBuilder.create(), PartPose.offsetAndRotation({pose}));')
    lines += [f'        return LayerDefinition.create(mesh, {TEX_W}, {TEX_H});', '    }', '',
              '    public static ModelPart sculpt(ModelPart skeleton) {',
              '        return BunnidogMesh.load(skeleton);', '    }', '}', '']
    return '\n'.join(lines)


def write_mesh(path):
    import struct
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(struct.pack('>ii', 0x42444D32, len(PARTS)))
        for p in PARTS:
            name = p['name'].encode('ascii')
            faces = [t for t in TILES if t['parent'] == p['name']]
            parent = (p['parent'] or 'root').encode('ascii')
            f.write(struct.pack('>H', len(name)) + name + struct.pack('>H', len(parent)) + parent + struct.pack('>i', len(faces)))
            for t in faces:
                corners = t['corners']
                u, v = t['uv']
                w, h = t['size']
                for point, tex, normal in zip(corners, ((u, v), (u + w, v), (u + w, v + h), (u, v + h)), t['normals']):
                    rel = [(point[k] - p['pivot'][k]) * (-1 if k == 1 else 1) for k in range(3)]
                    n = [normal[k] * (-1 if k == 1 else 1) for k in range(3)]
                    f.write(struct.pack('>8f', *rel, tex[0] / TEX_W, tex[1] / TEX_H, *n))
                # Use two edges that remain non-degenerate at either pole.
                a = [corners[2][k] - corners[0][k] for k in range(3)]
                b = [corners[3][k] - corners[1][k] for k in range(3)]
                n = [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
                length = math.sqrt(sum(q * q for q in n))
                if length < 1e-9:
                    raise ValueError('Degenerate face ' + t['name'])
                normal = [n[k] / length * (-1 if k == 1 else 1) for k in range(3)]
                f.write(struct.pack('>3f', *normal))


def main():
    build_geometry()
    tex_dir = os.path.join(ROOT, 'src/main/resources/assets', MODID, 'textures/entity')
    os.makedirs(tex_dir, exist_ok=True)
    imgs = {}
    for coat in COATS:
        imgs[coat] = paint_texture(coat)
        imgs[coat].save(os.path.join(tex_dir, f'bunnidog_{coat}.png'))
        print('painted', coat, flush=True)
    write_mesh(os.path.join(ROOT, 'src/main/resources/assets/bunnidogs/models/entity/bunnidog.mesh'))
    with open(os.path.join(ROOT, 'art/bunnidog.bbmodel'), 'w') as f:
        json.dump(build_bbmodel(imgs['tan']), f, separators=(',', ':'))
    java_path = os.path.join(ROOT, 'src/main/java/com/bunnidogs/mod/client/model/BunnidogModelLayer.java')
    with open(java_path, 'w') as f:
        f.write(build_java())
    sheet = Image.new('RGBA', (256 * 4, 256 * ((len(imgs)+3)//4)), (34, 35, 38, 255))
    for i, img in enumerate(imgs.values()):
        sheet.paste(img.resize((256, 256), Image.Resampling.LANCZOS), ((i % 4) * 256, (i // 4) * 256))
    sheet.save(os.path.join(ROOT, 'art/bunnidog_texture_preview.png'))
    print(f'{len(COATS)} coats, {len(TILES)} quads, {TEX_W}px atlases, continuous head UVs.')


if __name__ == '__main__':
    main()
