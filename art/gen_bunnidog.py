#!/usr/bin/env python3
"""
Bunnidog art pipeline: ONE source of truth -> three outputs.

    python3 art/gen_bunnidog.py          (needs: pip install pillow)

Outputs
  1. src/main/resources/assets/bunnidogs/textures/entity/bunnidog_<coat>.png   (64x64 textures, one per COAT)
  2. art/bunnidog.bbmodel                                               (open in Blockbench; uses the tan coat)
  3. src/main/java/com/bunnidogs/mod/client/model/BunnidogModelLayer.java (GENERATED, do not hand-edit)

To change the model shape: edit PARTS below and re-run.
To change the painting:    edit paint_texture() below and re-run.
To add/change a coat:      edit COATS below (names MUST match the Coat alleles in genetics/Gene.java,
                           tools/GenomeCheck.java verifies that every coat has a texture) and re-run.
Animation lives in the hand-written BunnidogModel.java, NOT here.

Coordinate conventions
  * PARTS use BLOCKBENCH space: 1 unit = 1 pixel (16 = 1 block), y is UP, feet at y=0,
    front of the creature faces -Z. Rotations are in degrees (Blockbench sign convention).
  * Java entity-model space is y-DOWN with the ground at y=24, so the Java conversion is
        java_y = 24 - bb_y     and rotations about X and Z flip sign (Y rotation is unchanged).
  * Box-UV layout is the standard Minecraft one: for a cube w x h x d at (u, v)
        top (u+d, v)   bottom (u+d+w, v)   east (u, v+d)   north/front (u+d, v+d)
        west (u+d+w, v+d)   south/back (u+2d+w, v+d)
"""
import base64, io, json, math, os, uuid
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX_W = TEX_H = 64
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
}

# --------------------------------------------------------------------------------------
# MODEL SPEC (Blockbench space).  Each part = a bone/group with a pivot, optional rotation,
# optional parent, and cubes.  Cube: (name, from[x,y,z], to[x,y,z], uv[u,v])
# --------------------------------------------------------------------------------------
PARTS = [
    dict(name="body", parent=None, pivot=(0, 6, 3), rot=(0, 0, 0),
         cubes=[("body", (-3, 4, -1), (3, 9, 7), (0, 16))]),
    dict(name="head", parent=None, pivot=(0, 8, -1), rot=(0, 0, 0),
         cubes=[("head", (-4, 6, -8), (4, 14, -1), (0, 0)),
                ("nose", (-1, 8, -9), (1, 10, -8), (48, 0))]),
    dict(name="ear_r", parent="head", pivot=(3, 14, -4.5), rot=(0, 0, -15),
         cubes=[("ear_r", (2, 14, -5), (4, 19, -4), (32, 0))]),
    dict(name="ear_l", parent="head", pivot=(-3, 14, -4.5), rot=(0, 0, 15),
         cubes=[("ear_l", (-4, 14, -5), (-2, 19, -4), (40, 0))]),
    dict(name="leg_fr", parent=None, pivot=(2, 4, 1), rot=(0, 0, 0),
         cubes=[("leg_fr", (1, 0, 0), (3, 4, 2), (0, 32))]),
    dict(name="leg_fl", parent=None, pivot=(-2, 4, 1), rot=(0, 0, 0),
         cubes=[("leg_fl", (-3, 0, 0), (-1, 4, 2), (0, 32))]),
    dict(name="leg_br", parent=None, pivot=(2, 4, 6), rot=(0, 0, 0),
         cubes=[("leg_br", (1, 0, 5), (3, 4, 7), (0, 32))]),
    dict(name="leg_bl", parent=None, pivot=(-2, 4, 6), rot=(0, 0, 0),
         cubes=[("leg_bl", (-3, 0, 5), (-1, 4, 7), (0, 32))]),
    # negative BB x-rotation tilts the tail UP (Java equivalent is +xRot)
    dict(name="tail", parent=None, pivot=(0, 7, 7), rot=(-25, 0, 0),
         cubes=[("tail", (-1, 7, 7), (1, 9, 9), (32, 32))]),
]


# --------------------------------------------------------------------------------------
# TEXTURE
# --------------------------------------------------------------------------------------
def box_faces(u, v, w, h, d):
    return {"east": (u, v + d, d, h), "north": (u + d, v + d, w, h),
            "west": (u + d + w, v + d, d, h), "south": (u + 2 * d + w, v + d, w, h),
            "up": (u + d, v, w, d), "down": (u + d + w, v, w, d)}


def paint_texture(coat="tan"):
    pal = COATS[coat]
    img = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    px = img.load()

    def rect(x, y, w, h, c):
        for i in range(x, x + w):
            for j in range(y, y + h):
                px[i, j] = c

    SKIN = (236, 203, 188, 255); SKIN_D = (222, 186, 172, 255); SKIN_L = (244, 216, 203, 255)
    TAN, TAN_D, TAN_L = pal["base"], pal["dark"], pal["light"]
    PINK = (226, 150, 150, 255); DARK = (60, 70, 80, 255); BLUE = (96, 120, 135, 255)

    # HEAD 8x8x7 @ (0,0): bald pale skin
    hf = box_faces(0, 0, 8, 8, 7)
    for f, (x, y, w, h) in hf.items():
        rect(x, y, w, h, SKIN)
    x, y, w, h = hf["up"]; rect(x, y, w, h, SKIN_L)
    x, y, w, h = hf["down"]; rect(x, y, w, h, SKIN_D)
    fx, fy, _, _ = hf["north"]
    rect(fx, fy + 1, 8, 1, SKIN_D)                       # faint brow line
    for ex in (1, 5):                                    # wide-set heavy-lidded eyes
        rect(fx + ex, fy + 2, 2, 1, SKIN_D)
        rect(fx + ex, fy + 3, 2, 1, DARK)
        px[fx + ex + (0 if ex == 1 else 1), fy + 3] = BLUE
        rect(fx + ex, fy + 4, 2, 1, SKIN_D)              # eye bags
    rect(fx + 2, fy + 6, 4, 1, (196, 140, 135, 255))     # tiny flat mouth
    rect(fx + 3, fy + 7, 2, 1, SKIN_D)                   # chin
    for f in ("east", "west"):
        x, y, w, h = hf[f]; rect(x + 3, y + 2, 2, 2, SKIN_D)

    # BODY 6x5x8 @ (0,16): tan, pale belly, pale chest
    bf = box_faces(0, 16, 6, 5, 8)
    for f, (x, y, w, h) in bf.items():
        rect(x, y, w, h, TAN)
    x, y, w, h = bf["up"]; rect(x, y, w, h, TAN_L)
    x, y, w, h = bf["down"]; rect(x, y, w, h, SKIN_L)
    x, y, w, h = bf["north"]; rect(x, y, w, h, SKIN); rect(x, y + 3, w, 2, TAN)
    for f in ("east", "west"):
        x, y, w, h = bf[f]
        rect(x, y + h - 1, w, 1, TAN_D)
        rect(x, y, 2, h, SKIN_D) if f == "east" else rect(x + w - 2, y, 2, h, SKIN_D)
    x, y, w, h = bf["south"]; rect(x, y + h - 1, w, 1, TAN_D)

    # EARS 2x5x1 @ (32,0) and (40,0)
    for ox in (32, 40):
        ef = box_faces(ox, 0, 2, 5, 1)
        for f, (x, y, w, h) in ef.items():
            rect(x, y, w, h, TAN)
        x, y, w, h = ef["north"]; rect(x, y, w, h, TAN_L); rect(x, y + 1, w, 4, PINK)
        x, y, w, h = ef["up"]; rect(x, y, w, h, TAN_D)

    # NOSE 2x2x1 @ (48,0)
    nf = box_faces(48, 0, 2, 2, 1)
    for f, (x, y, w, h) in nf.items():
        rect(x, y, w, h, (232, 160, 160, 255))
    x, y, w, h = nf["north"]; rect(x, y, w, h, (214, 132, 140, 255)); px[x, y] = (240, 180, 180, 255)

    # LEG 2x4x2 @ (0,32), shared by all four legs (optional per-coat leg colour and hoof/pad colour)
    LEG = pal.get("legs", TAN); HOOF = pal.get("hoof", SKIN_D)
    lf = box_faces(0, 32, 2, 4, 2)
    for f, (x, y, w, h) in lf.items():
        rect(x, y, w, h, LEG)
    for f in ("east", "west", "north", "south"):
        x, y, w, h = lf[f]; rect(x, y + h - 1, w, 1, HOOF)
    x, y, w, h = lf["down"]; rect(x, y, w, h, HOOF)

    # TAIL 2x2x2 @ (32,32)
    tf = box_faces(32, 32, 2, 2, 2)
    for f, (x, y, w, h) in tf.items():
        rect(x, y, w, h, TAN_L)
    # OPTIONAL PATCHES (coats with "patches": spotted, glow, ember, void): patches on the back and flanks, plus one patched ear
    if pal.get("patches"):
        PATCH = pal["patches"]
        x, y, w, h = bf["up"]; rect(x + 1, y + 1, 3, 3, PATCH); rect(x + 3, y + 5, 2, 2, PATCH)
        x, y, w, h = bf["east"]; rect(x + 2, y + 1, 3, 2, PATCH)
        x, y, w, h = bf["west"]; rect(x + 4, y + 2, 3, 2, PATCH)
        x, y, w, h = box_faces(32, 0, 2, 5, 1)["south"]; rect(x, y, w, 3, PATCH)
    return img


# --------------------------------------------------------------------------------------
# BLOCKBENCH (.bbmodel) EXPORT
# --------------------------------------------------------------------------------------
def build_bbmodel(img):
    buf = io.BytesIO(); img.save(buf, "PNG")
    b64 = "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()
    uid = lambda: str(uuid.uuid4())
    elements, groups = [], {}
    for part in PARTS:
        g = {"name": part["name"], "origin": list(part["pivot"]), "rotation": list(part["rot"]),
             "color": 0, "uuid": uid(), "export": True, "isOpen": True, "visibility": True,
             "autouv": 0, "children": []}
        groups[part["name"]] = g
        for (cname, frm, to, uv) in part["cubes"]:
            e_uuid = uid()
            elements.append({"name": cname, "box_uv": True, "rescale": False, "locked": False,
                             "from": list(frm), "to": list(to), "autouv": 0, "color": 0,
                             "origin": list(part["pivot"]), "rotation": [0, 0, 0],
                             "uv_offset": list(uv), "uuid": e_uuid, "type": "cube"})
            g["children"].append(e_uuid)
    outliner = []
    for part in PARTS:                       # nest children groups inside their parent group
        if part["parent"] is None:
            outliner.append(groups[part["name"]])
        else:
            groups[part["parent"]]["children"].append(groups[part["name"]])
    return {"meta": {"format_version": "4.10", "model_format": "bedrock", "box_uv": True},
            "name": "bunnidog", "model_identifier": "bunnidog", "visible_box": [1, 1, 0],
            "resolution": {"width": TEX_W, "height": TEX_H}, "elements": elements,
            "outliner": outliner,
            "textures": [{"path": "", "name": "bunnidog_tan.png", "folder": "", "namespace": "",
                          "id": "0", "particle": False, "render_mode": "default",
                          "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
                          "frame_order": "", "frame_interpolate": False, "visible": True,
                          "mode": "bitmap", "saved": True, "uuid": uid(),
                          "relative_path": "bunnidog_tan.png", "source": b64}]}


# --------------------------------------------------------------------------------------
# JAVA LAYER EXPORT
# --------------------------------------------------------------------------------------
def fnum(v):
    """Format a float as a Java float literal."""
    v = round(float(v), 4) + 0.0          # +0.0 turns -0.0 into 0.0
    s = ("%g" % v)
    if "." not in s and "e" not in s:
        s += ".0"
    return s + "F"


def build_java():
    by_name = {p["name"]: p for p in PARTS}
    lines = []
    w = lines.append
    w("// GENERATED by art/gen_bunnidog.py -- DO NOT EDIT BY HAND. Edit PARTS in the script and re-run it.")
    w("package %s;" % JAVA_PKG)
    w("")
    w("import net.minecraft.client.model.geom.PartPose;")
    w("import net.minecraft.client.model.geom.builders.CubeListBuilder;")
    w("import net.minecraft.client.model.geom.builders.LayerDefinition;")
    w("import net.minecraft.client.model.geom.builders.MeshDefinition;")
    w("import net.minecraft.client.model.geom.builders.PartDefinition;")
    w("")
    w("public final class BunnidogModelLayer {")
    w("    private BunnidogModelLayer() {}")
    w("")
    w("    public static LayerDefinition createBodyLayer() {")
    w("        MeshDefinition mesh = new MeshDefinition();")
    w("        PartDefinition root = mesh.getRoot();")
    for part in PARTS:
        px_, py_, pz_ = part["pivot"]
        if part["parent"] is None:
            ppx, ppy, ppz = 0, 24, 0                       # root pivot in BB space (Java y = 0 is BB y = 24)
            parent_var = "root"
        else:
            ppx, ppy, ppz = by_name[part["parent"]]["pivot"]
            parent_var = by_name[part["parent"]]["name"].replace("-", "_")
        off = (px_ - ppx, -(py_ - ppy), pz_ - ppz)
        rx, ry, rz = part["rot"]
        jr = (-math.radians(rx), math.radians(ry), -math.radians(rz))   # flip X/Z sign for Java
        if any(abs(a) > 1e-9 for a in jr):
            pose = "PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)" % tuple(
                fnum(a) for a in (*off, *jr))
        else:
            pose = "PartPose.offset(%s, %s, %s)" % tuple(fnum(a) for a in off)
        cube_calls = []
        for (cname, frm, to, uv) in part["cubes"]:
            size = (to[0] - frm[0], to[1] - frm[1], to[2] - frm[2])
            rel = (frm[0] - px_, -(to[1] - py_), frm[2] - pz_)
            cube_calls.append(".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (
                uv[0], uv[1], *[fnum(a) for a in (*rel, *size)]))
        var = part["name"]
        has_children = any(q["parent"] == part["name"] for q in PARTS)
        decl = ("PartDefinition %s = " % var) if has_children else ""
        w("        %s%s.addOrReplaceChild(\"%s\",\n"
          "                CubeListBuilder.create()%s,\n"
          "                %s);" % (decl, parent_var, var, "".join("\n                        " + c for c in cube_calls), pose))
    w("        return LayerDefinition.create(mesh, %d, %d);" % (TEX_W, TEX_H))
    w("    }")
    w("}")
    return "\n".join(lines) + "\n"


def main():
    tex_dir = os.path.join(ROOT, "src/main/resources/assets", MODID, "textures/entity")
    os.makedirs(tex_dir, exist_ok=True)
    stale = os.path.join(tex_dir, "bunnidog.png")          # pre-coat texture name; superseded by bunnidog_tan.png
    if os.path.exists(stale):
        os.remove(stale)
    imgs = {}
    for coat in COATS:
        imgs[coat] = paint_texture(coat)
        imgs[coat].save(os.path.join(tex_dir, "bunnidog_%s.png" % coat))
    with open(os.path.join(ROOT, "art/bunnidog.bbmodel"), "w") as f:
        json.dump(build_bbmodel(imgs["tan"]), f, indent=1)
    java_path = os.path.join(ROOT, "src/main/java/com/bunnidogs/mod/client/model/BunnidogModelLayer.java")
    os.makedirs(os.path.dirname(java_path), exist_ok=True)
    with open(java_path, "w") as f:
        f.write(build_java())
    # preview: all coats side by side, 4x
    sheet = Image.new("RGBA", (TEX_W * 4 * len(imgs), TEX_H * 4), (40, 40, 48, 255))
    for i, coat in enumerate(imgs):
        sheet.alpha_composite(imgs[coat].resize((TEX_W * 4, TEX_H * 4), Image.NEAREST), (i * TEX_W * 4, 0))
    sheet.save(os.path.join(ROOT, "art/bunnidog_texture_preview.png"))
    print("wrote %d coat textures (%s), bbmodel, preview, and BunnidogModelLayer.java" % (len(imgs), ", ".join(imgs)))


if __name__ == "__main__":
    main()
