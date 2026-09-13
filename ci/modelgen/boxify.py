#!/usr/bin/env python3
"""
ci/modelgen/boxify.py — Build #370 model rewrite pipeline.

Reconstructs the WitherStormP4 entity geometry (phases 4-5.5) from the CLEAN
MCSM Blockbench source (Stage_B/witherstormStageB.bbmodel, triangulated
mesh), replacing the scrambled/deficient hand-transcribed box list in
WitherStormP4.java.

Facts that make this safe:
  * MC 26.2 entity geometry is box-only (ModelPart/CubeListBuilder); the
    clean Blockbench file stores trimesh -> we rebuild a gap-free box set
    from its solid silhouette.
  * Phases <6 render the canonical SOLID deep-black sheet, so BoxUV values
    are visually irrelevant -> geometry-only rewrite is appearance-complete.
  * The original 302-part tree, constructor, keyframe animations
    (WitherStormP4Anim, name-baked) and TentaclePhysics/SnatchGrab
    (generic tree walk) are preserved verbatim; only cube lists change:
      - 'bone' subtree parts  -> emptied (the old pancake geometry)
      - 'bone' itself         -> the reconstructed box set
      - 'DebrisRing' subtree  -> untouched (orbiting debris keeps working)

Usage:  python3 ci/modelgen/boxify.py [--apply]
  (default: dry run, prints stats only)
"""
import json, math, re, sys, time, os
import numpy as np

ZZ = "/tmp/gggggrff/ALL wither storm assets from MCSM for blockbench you'll ever need V0.9 4.zip"
STAGEB = f"{ZZ}/Stage_B/witherstormStageB.bbmodel"
SRC = "src-recon/net/dabicco/witherstormmod/entity/model/WitherStormP4.java"
OUT = "mcsm-extras/java/net/dabicco/witherstormmod/entity/model/WitherStormP4.java"

CELL = 4.0  # px per voxel cell (silhouette step on a ~650px model)

# ---------------------------------------------------------------- trimesh
def load_tris(path):
    d = json.load(open(path))
    tris = []
    for e in d["elements"]:
        if e.get("type") != "mesh":
            continue
        verts = e.get("vertices") or {}
        faces = e.get("faces") or {}
        if not verts or not faces:
            continue
        ox, oy, oz = e.get("origin", (0, 0, 0))
        r = e.get("rotation", (0, 0, 0))
        def rotx(v, a):
            a = math.radians(a); x, y, z = v
            return (x, y*math.cos(a)-z*math.sin(a), y*math.sin(a)+z*math.cos(a))
        def roty(v, a):
            a = math.radians(a); x, y, z = v
            return (x*math.cos(a)+z*math.sin(a), y, -x*math.sin(a)+z*math.cos(a))
        def rotz(v, a):
            a = math.radians(a); x, y, z = v
            return (x*math.cos(a)-y*math.sin(a), x*math.sin(a)+y*math.cos(a), z)
        V = {}
        for k, v in verts.items():
            p = (v[0], v[1], v[2])
            if r[0]: p = rotx(p, r[0])
            if r[1]: p = roty(p, r[1])
            if r[2]: p = rotz(p, r[2])
            V[k] = (p[0]+ox, p[1]+oy, p[2]+oz)
        for f in faces.values():
            vs = f["vertices"]
            for i in range(0, len(vs) - 2, 3):
                a, b, c = vs[i], vs[i+1], vs[i+2]
                if a in V and b in V and c in V:
                    tris.append(V[a] + V[b] + V[c])
    return np.asarray(tris, dtype=np.float64).reshape(-1, 9)

# ---------------------------------------------------------------- fit
def fit(T):
    """Map trimesh space -> P4 model space (anchor: match original bbox
    centre in x/z, bottom in y; uniform scale from the x-span)."""
    tb = T.reshape(-1, 3)
    tmin, tmax = tb.min(0), tb.max(0)
    P4 = dict(xmin=-159.9, xmax=489.9, ymin=-319.0, ymax=-18.0, zmin=-116.0, zmax=74.0)
    s = (P4["xmax"] - P4["xmin"]) / (tmax[0] - tmin[0])
    cx, cz = (P4["xmin"]+P4["xmax"])/2, (P4["zmin"]+P4["zmax"])/2
    tcx, tcz = (tmin[0]+tmax[0])/2, (tmin[2]+tmax[2])/2
    x_m = (tb[:,0] - tcx) * s + cx
    y_m = (tmin[1] - tb[:,1]) * s + P4["ymax"]   # trimesh bottom -> model bottom
    z_m = (tb[:,2] - tcz) * s + cz
    M = np.column_stack([x_m, y_m, z_m])
    return M, s

# ---------------------------------------------------------------- hull
def rasterize_planes(M, gmin, gsz):
    """3 silhouette planes via per-triangle patch barycentric marking."""
    gmin = np.asarray(gmin, float); gsz = np.asarray(gsz, int)
    # each plane array is indexed [second-axis cell, first-axis cell]
    # (a/c index the first axis -> columns, b/d the second -> rows)
    occ_xz = np.zeros((gsz[2], gsz[0]), bool)   # project along y
    occ_yz = np.zeros((gsz[2], gsz[1]), bool)   # project along x
    occ_xy = np.zeros((gsz[1], gsz[0]), bool)   # project along z
    tri = M.reshape(-1, 3, 3)
    n = len(tri)
    planes = (
        (occ_xz, 0, 2, gsz[0], gsz[2]),
        (occ_yz, 1, 2, gsz[1], gsz[2]),
        (occ_xy, 0, 1, gsz[0], gsz[1]),
    )
    for occ, ax, ay, gw, gh in planes:
        off = np.array([gmin[ax], gmin[ay]]) / CELL
        P = (tri[:, :, [ax, ay]] - off) / CELL      # (n,3,2) cell coords
        cmin = P.min(1); cmax = P.max(1)
        for t in range(n):
            a = int(max(0, math.floor(cmin[t,0])))
            b = int(max(0, math.floor(cmin[t,1])))
            c = int(min(gw, math.ceil(cmax[t,0])))
            d = int(min(gh, math.ceil(cmax[t,1])))
            if a >= c or b >= d or c - a > 600 or d - b > 600:
                continue
            xs = np.arange(a+0.5, c+0.5)
            ys = np.arange(b+0.5, d+0.5)
            X, Y = np.meshgrid(xs, ys)
            p0, p1, p2 = P[t,0], P[t,1], P[t,2]
            d0x, d0y = p1[0]-p0[0], p1[1]-p0[1]
            d1x, d1y = p2[0]-p1[0], p2[1]-p1[1]
            d2x, d2y = p0[0]-p2[0], p0[1]-p2[1]
            s0 = d0x*(Y-p0[1]) - d0y*(X-p0[0])
            s1 = d1x*(Y-p1[1]) - d1y*(X-p1[0])
            s2 = d2x*(Y-p2[1]) - d2y*(X-p2[0])
            occ[b:d, a:c] |= ((s0 >= 0) | (s1 >= 0) | (s2 >= 0)) & ((s0 <= 0) | (s1 <= 0) | (s2 <= 0))
    return occ_xz, occ_yz, occ_xy

def boxes_from_hull(solid, gmin, gsz):
    """Maximal box decomposition via z-sweep (deterministic).

    run[i,j,k] = current run of consecutive remaining cells in +z from k.
    At each z: H = run[:,:,z]. For each x-row i (ascending), each uncovered
    y-run [j,j1): grow x while the run stays uncovered, height = min H over
    the window; place the box and zero its run volume. z advances by 1.
    """
    X, Y, Z = gsz
    rem = solid
    run = np.zeros((X, Y, Z + 1), np.int32)
    for k in range(Z - 1, -1, -1):
        live = rem[:, :, k] & (run[:, :, k + 1] > 0)
        run[:, :, k] = np.where(live, run[:, :, k + 1] + 1,
                                rem[:, :, k].astype(np.int32))
    out = []
    for z in range(Z):
        H = run[:, :, z]
        if not H.any():
            continue
        placed_this_z = 0
        for i in range(X):
            row = H[i]
            if not row.any():
                continue
            j = 0
            while j < Y:
                if row[j] == 0:
                    j += 1
                    continue
                j1 = j
                while j1 < Y and row[j1] > 0:
                    j1 += 1
                i2 = i
                while i2 + 1 < X and np.all(H[i2 + 1, j:j1] > 0):
                    i2 += 1
                h = int(H[i:i2+1, j:j1].min())
                out.append((i, j, z, i2 - i + 1, j1 - j, h))
                run[i:i2+1, j:j1, z:z+h] = 0
                H[i:i2+1, j:j1] = 0
                placed_this_z += 1
                j = j1
        if placed_this_z:
            print(f"[boxify]   z={z}: +{placed_this_z} boxes (total {len(out)})", flush=True)
    gmin = np.asarray(gmin, float)
    return [(gmin[0]+i*CELL, gmin[1]+j*CELL, gmin[2]+k*CELL, w*CELL, h*CELL, d*CELL)
            for (i, j, k, w, h, d) in out]

# ---------------------------------------------------------------- java emit
def emit_cubes(px_boxes, bone_off=(0.0, -15.0, 7.0)):
    lines = []
    chunk = []
    for (x, y, z, w, h, d) in px_boxes:
        lx = x - bone_off[0]; ly = y - bone_off[1]; lz = z - bone_off[2]
        def f(v):
            v = round(v, 1)
            return f"{v:.1f}F"
        chunk.append(f".addBox({f(lx)}, {f(ly)}, {f(lz)}, {f(w)}, {f(h)}, {f(d)}, new CubeDeformation(0.0F))")
        if len(chunk) >= 3:
            lines.append("            " + "".join(chunk))
            chunk = []
    if chunk:
        lines.append("            " + "".join(chunk))
    return "\n".join(lines)

# ---------------------------------------------------------------- file transform
def transform2(src_text, bone_cubes_java):
    seg0 = src_text.index("createBodyLayer")
    seg1 = src_text.index("public void setupAnim", seg0)
    head, seg, tail = src_text[:seg0], src_text[seg0:seg1], src_text[seg1:]
    parts, order = {}, []
    for m in re.finditer(r'PartDefinition\s+(\w+)\s*=\s*(\w+)\.addOrReplaceChild\(\s*"([^"]+)"\s*,', seg):
        var, parent, name = m.group(1), m.group(2), m.group(3)
        buf = seg[m.end():]
        depth = 1; argstr = None
        for idx, ch in enumerate(buf):
            if ch == '(': depth += 1
            elif ch == ')':
                depth -= 1
                if depth == 0:
                    argstr = buf[:idx]; break
        if argstr is None:
            continue
        cb = argstr.index("CubeListBuilder.create()")
        pp_start = argstr.rindex("PartPose.")
        pp = argstr.rindex(",", 0, pp_start)
        parts[var] = dict(parent=parent,
                          cb_abs=m.end()+cb,
                          cb_end_abs=m.end()+cb+len("CubeListBuilder.create()"),
                          pp_comma_abs=m.end()+pp+1,
                          pp_start_abs=m.end()+pp_start)
        order.append(var)
    def subtree(v):
        out = {v}; changed = True
        while changed:
            changed = False
            for x in order:
                if parts[x]['parent'] in out and x not in out:
                    out.add(x); changed = True
        return out
    bone_st = subtree("bone")
    edits = []
    for v in order:
        p = parts[v]
        if v == "bone":
            repl = "CubeListBuilder.create()\n" + bone_cubes_java + ",\n      "
            edits.append((p['cb_abs'], p['pp_comma_abs'], repl))
        elif v in bone_st:
            edits.append((p['cb_abs'], p['pp_comma_abs'], "CubeListBuilder.create(),"))
    edits.sort()
    for a, b in zip(edits, edits[1:]):
        assert a[1] <= b[0], f"edit overlap at {a[0]}/{b[0]}"
    out = []; pos = 0
    for cs, ce, repl in edits:
        out.append(seg[pos:cs]); out.append(repl); pos = ce
    out.append(seg[pos:])
    return head + "".join(out) + tail

# ---------------------------------------------------------------- main
def main():
    t0 = time.time()
    print("[boxify] loading StageB trimesh...")
    T = load_tris(STAGEB)
    print(f"[boxify] triangles: {len(T)}")
    M, s = fit(T)
    mb2 = M.reshape(-1, 3)
    mn = mb2.min(0); mx = mb2.max(0)
    print(f"[boxify] scale={s:.4f} model bbox: x[{mn[0]:.1f},{mx[0]:.1f}] "
          f"y[{mn[1]:.1f},{mx[1]:.1f}] z[{mn[2]:.1f},{mx[2]:.1f}]")
    print(f"[boxify] extents: {mx[0]-mn[0]:.1f} x {mx[1]-mn[1]:.1f} x {mx[2]-mn[2]:.1f}")

    gmin = [float(mn[0]), float(mn[1]), float(mn[2])]
    gsz = [int(math.ceil((mx[c]-mn[c]) / CELL)) + 1 for c in range(3)]
    print(f"[boxify] grid: {gsz[0]}x{gsz[1]}x{gsz[2]} = {gsz[0]*gsz[1]*gsz[2]/1e6:.1f}M cells")

    print("[boxify] rasterizing silhouettes...")
    occ_xz, occ_yz, occ_xy = rasterize_planes(M, gmin, gsz)
    solid = np.zeros((gsz[0], gsz[1], gsz[2]), bool)
    solid |= occ_xz.T[:, None, :]     # (X,Z) -> (X,1,Z)
    solid |= occ_yz.T[None, :, :]     # (Y,Z) -> (1,Y,Z)
    solid |= occ_xy.T[:, :, None]     # (X,Y) -> (X,Y,1)
    print(f"[boxify] solid cells: {solid.sum()}/{solid.size} ({100*solid.sum()/solid.size:.1f}%)")

    print("[boxify] extracting boxes...")
    px_boxes = boxes_from_hull(solid, gmin, gsz)
    print(f"[boxify] total boxes: {len(px_boxes)}")

    src_text = open(SRC).read()
    bone_java = emit_cubes(px_boxes)
    new_text = transform2(src_text, bone_java)
    n_add = len(re.findall(r'addBox\(', new_text))
    n_old = len(re.findall(r'addBox\(', src_text))
    print(f"[boxify] addBox calls: {n_old} -> {n_add}; output {len(new_text)/1e6:.2f} MB")
    if "--apply" in sys.argv:
        os.makedirs(os.path.dirname(OUT), exist_ok=True)
        open(OUT, "w").write(new_text)
        print(f"[boxify] WROTE {OUT}")
    else:
        print("[boxify] dry run (pass --apply to write)")
    print(f"[boxify] done in {time.time()-t0:.1f}s")

if __name__ == "__main__":
    main()
