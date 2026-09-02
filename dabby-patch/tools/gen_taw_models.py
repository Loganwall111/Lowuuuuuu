#!/usr/bin/env python3
"""Convert Tainted's Accurate Wither Storm .jem (OptiFine CEM) models into a
compact binary mesh the mod can load at runtime.

Why not ModelPart?  Vanilla's ModelPart$Cube constructor accepts a single
textureOffset and cannot express per-face UVs (uvNorth/uvEast/...), which
37,261 of the 157,697 boxes rely on.  So we bake the hierarchy ourselves and
emit explicit quads, the same approach BakedMesh already uses in this mod.

Output format (little-endian), one .taw file per model:
    magic  "TAWM"            4 bytes
    version                  u16  = 2
    partCount                u16
    for each part:
        nameLen u8, name utf8
        quadCount u32
        quads: 4 x (px,py,pz int16 @1/16 unit, u,v uint16 @1/65535)  = 20 bytes
        normal: nx,ny,nz int8 @1/127                                 =  3 bytes
Quantised: positions to 1/16 of a model unit (range +-2048, actual max 415),
UVs to 1/65535 of the atlas.  Halves the payload with no visible error --
a model unit is 1/16 block, so 1/16 unit is 1/256 block.
Coordinates are already baked into model space (translate/rotate applied).
Parts are kept separate so the renderer can animate heads/jaws independently.
"""
import json, os, struct, sys, math, glob

SRC = "/var/tmp/taw/models/assets/witherstormmod/optifine/cem"
DST = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "tawmesh")

# ---------------------------------------------------------------- math utils
def mat_ident():
    return [1,0,0,0, 0,1,0,0, 0,0,1,0]          # 3x4 row-major

def mat_mul(a, b):
    o = [0.0]*12
    for r in range(3):
        for c in range(3):
            o[r*4+c] = a[r*4+0]*b[0*4+c] + a[r*4+1]*b[1*4+c] + a[r*4+2]*b[2*4+c]
        o[r*4+3] = a[r*4+0]*b[3] + a[r*4+1]*b[7] + a[r*4+2]*b[11] + a[r*4+3]
    return o

def mat_translate(x, y, z):
    return [1,0,0,x, 0,1,0,y, 0,0,1,z]

def mat_rot(rx, ry, rz):
    """OptiFine CEM applies rotations in X, Y, Z order, in degrees."""
    m = mat_ident()
    if rz:
        c, s = math.cos(math.radians(rz)), math.sin(math.radians(rz))
        m = mat_mul(m, [c,-s,0,0, s,c,0,0, 0,0,1,0])
    if ry:
        c, s = math.cos(math.radians(ry)), math.sin(math.radians(ry))
        m = mat_mul(m, [c,0,s,0, 0,1,0,0, -s,0,c,0])
    if rx:
        c, s = math.cos(math.radians(rx)), math.sin(math.radians(rx))
        m = mat_mul(m, [1,0,0,0, 0,c,-s,0, 0,s,c,0])
    return m

def xform(m, x, y, z):
    return (m[0]*x + m[1]*y + m[2]*z + m[3],
            m[4]*x + m[5]*y + m[6]*z + m[7],
            m[8]*x + m[9]*y + m[10]*z + m[11])

# ---------------------------------------------------------------- box -> quads
# face order: down, up, north, south, west, east  (matches OptiFine uv* keys)
FACES = [
    # name,  corner indices (as x/y/z picks), normal
    ("down",  ((0,0,1),(1,0,1),(1,0,0),(0,0,0)), (0,-1,0)),
    ("up",    ((0,1,0),(1,1,0),(1,1,1),(0,1,1)), (0, 1,0)),
    ("north", ((1,0,0),(1,1,0),(0,1,0),(0,0,0)), (0,0,-1)),
    ("south", ((0,0,1),(0,1,1),(1,1,1),(1,0,1)), (0,0, 1)),
    ("west",  ((0,0,0),(0,1,0),(0,1,1),(0,0,1)), (-1,0,0)),
    ("east",  ((1,0,1),(1,1,1),(1,1,0),(1,0,0)), ( 1,0,0)),
]

def box_quads(box, mat, tw, th, mirror):
    co = box["coordinates"]
    x0, y0, z0, dx, dy, dz = (float(v) for v in co[:6])
    add = box.get("sizeAdd", 0.0)
    if add:
        add = float(add)
        x0 -= add; y0 -= add; z0 -= add
        dx += add*2; dy += add*2; dz += add*2
    x1, y1, z1 = x0+dx, y0+dy, z0+dz

    # UV per face: explicit uv<Face> wins, else unwrap from textureOffset
    uvs = {}
    if "uvNorth" in box:
        for f, _, _ in FACES:
            k = "uv" + f.capitalize()
            if k in box:
                a = box[k]
                uvs[f] = (float(a[0]), float(a[1]), float(a[2]), float(a[3]))
    if not uvs and "textureOffset" in box:
        tx, ty = float(box["textureOffset"][0]), float(box["textureOffset"][1])
        w, h, d = abs(dx), abs(dy), abs(dz)
        uvs = {
            "down":  (tx+d+w,     ty,     tx+d+w*2, ty+d),
            "up":    (tx+d,       ty+d,   tx+d+w,   ty),
            "north": (tx+d+w,     ty+d,   tx+d*2+w, ty+d+h),
            "south": (tx+d*2+w*2, ty+d,   tx+d*2+w, ty+d+h),
            "west":  (tx+d,       ty+d,   tx,       ty+d+h),
            "east":  (tx+d*2+w,   ty+d,   tx+d+w,   ty+d+h),
        }
    if not uvs:
        return []

    out = []
    for fname, corners, nrm in FACES:
        if fname not in uvs:
            continue
        u0, v0, u1, v1 = uvs[fname]
        u0 /= tw; u1 /= tw; v0 /= th; v1 /= th
        if mirror:
            u0, u1 = u1, u0
        # quad UV corners follow the corner winding
        quv = [(u0, v1), (u0, v0), (u1, v0), (u1, v1)]
        verts = []
        for i, (cx, cy, cz) in enumerate(corners):
            px = x1 if cx else x0
            py = y1 if cy else y0
            pz = z1 if cz else z0
            wx, wy, wz = xform(mat, px, py, pz)
            verts.append((wx, wy, wz, quv[i][0], quv[i][1]))
        nx, ny, nz = xform(mat, nrm[0], nrm[1], nrm[2])
        ox, oy, oz = xform(mat, 0, 0, 0)
        nx, ny, nz = nx-ox, ny-oy, nz-oz
        ln = math.sqrt(nx*nx + ny*ny + nz*nz) or 1.0
        out.append((verts, (nx/ln, ny/ln, nz/ln)))
    return out

# ---------------------------------------------------------------- walk a model
def walk(node, mat, tw, th, sink, mirror=False):
    t = node.get("translate") or [0, 0, 0]
    # CEM invertAxis "xy" means the X and Y axes are negated
    local = mat_translate(-float(t[0]), -float(t[1]), float(t[2]))
    r = node.get("rotate")
    if r:
        local = mat_mul(local, mat_rot(-float(r[0]), -float(r[1]), float(r[2])))
    m = mat_mul(mat, local)
    mir = mirror or bool(node.get("mirrorTexture"))
    ts = node.get("textureSize")
    if ts:
        tw, th = float(ts[0]), float(ts[1])
    for b in (node.get("boxes") or []):
        sink.extend(box_quads(b, m, tw, th, mir))
    for s in (node.get("submodels") or []):
        walk(s, m, tw, th, sink, mir)

def convert(path):
    d = json.load(open(path))
    ts = d.get("textureSize") or [64, 32]
    tw, th = float(ts[0]), float(ts[1])
    parts = []
    for top in d.get("models", []):
        sink = []
        # invertAxis xy -> flip X/Y at the root
        root = [-1,0,0,0, 0,-1,0,0, 0,0,1,0]
        walk(top, root, tw, th, sink)
        if sink:
            parts.append((top.get("part") or top.get("id") or "part", sink))
    return parts

def write(parts, out):
    buf = bytearray()
    buf += b"TAWM" + struct.pack("<HH", 2, len(parts))
    for name, quads in parts:
        nb = name.encode("utf8")[:255]
        buf += struct.pack("<B", len(nb)) + nb
        buf += struct.pack("<I", len(quads))
        for verts, n in quads:
            for (px, py, pz, u, v) in verts:
                buf += struct.pack("<3h2H",
                    max(-32768, min(32767, int(round(px * 16.0)))),
                    max(-32768, min(32767, int(round(py * 16.0)))),
                    max(-32768, min(32767, int(round(pz * 16.0)))),
                    max(0, min(65535, int(round(u * 65535.0)))),
                    max(0, min(65535, int(round(v * 65535.0)))))
            buf += struct.pack("<3b",
                max(-127, min(127, int(round(n[0] * 127.0)))),
                max(-127, min(127, int(round(n[1] * 127.0)))),
                max(-127, min(127, int(round(n[2] * 127.0)))))
    open(out, "wb").write(buf)
    return len(buf)

def main():
    if not os.path.isdir(SRC):
        print("source models not found:", SRC); return 1
    os.makedirs(DST, exist_ok=True)
    total = 0; nq = 0
    for f in sorted(glob.glob(os.path.join(SRC, "*.jem"))):
        name = os.path.basename(f)[:-4]
        parts = convert(f)
        q = sum(len(p[1]) for p in parts)
        sz = write(parts, os.path.join(DST, name + ".taw"))
        total += sz; nq += q
        print(f"   {name:<26} {len(parts):>3} parts {q:>7} quads {sz/1024:>8.0f} KB")
    print(f"\n{nq} quads total, {total/1048576:.1f} MB")
    return 0

if __name__ == "__main__":
    sys.exit(main())
