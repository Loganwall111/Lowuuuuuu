#!/usr/bin/env python3
"""Export selected shaded Blockbench storm bodies into runtime-safe shell JSON.

This strips the huge embedded base64 texture payloads out of the original .bbmodel
files and keeps only the geometry, UVs, normals, bounds, and texture names needed
by the in-game renderer.
"""

from __future__ import annotations

import json
import math
from pathlib import Path

WINDOWS_SAFE_RENAMES = {
    "1:1 flesh.png": "1_1_flesh.png",
    "this is 1:1 too.png": "this_is_1_1_too.png",
}

EXPORTS = {
    "stage_b_forest_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageB Forest (with traced shading textures).bbmodel",
    "stage_b_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageB (with traced shading textures).bbmodel",
    "stage_c_small_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageC_Small (with traced shading textures).bbmodel",
    "stage_c_big_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageC_Big (with traced shading textures).bbmodel",
    "stage_c_massive_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageC_Massive (with traced shading textures).bbmodel",
    "stage_d_center_massive_shaded": "src/main/resources/assets/devouringstorms/geo/Traced_shading_Textures/witherstormStageD_Center_Massive.bbmodel",
}

OUT_DIR = Path("src/main/resources/assets/devouringstorms/stage_shells")


def safe_name(name: str) -> str:
    return WINDOWS_SAFE_RENAMES.get(Path(name).name, Path(name).name)


def r(v: float) -> float:
    return round(float(v), 5)


def quad_normal(a, b, c):
    ux, uy, uz = b[0] - a[0], b[1] - a[1], b[2] - a[2]
    vx, vy, vz = c[0] - a[0], c[1] - a[1], c[2] - a[2]
    nx = uy * vz - uz * vy
    ny = uz * vx - ux * vz
    nz = ux * vy - uy * vx
    length = math.sqrt(nx * nx + ny * ny + nz * nz) or 1.0
    return [r(nx / length), r(ny / length), r(nz / length)]


def sanitize_tex_name(name):
    cleaned = name.replace(" ", "_").replace("!", "").replace(":", "_").lower()
    if cleaned != name:
        print(f"  [sanitized texture name] {name!r} -> {cleaned!r}")
    return cleaned


def face_texture(tex):
    if tex is None:
        return None
    if isinstance(tex, str) and tex.startswith("#"):
        tex = tex[1:]
    try:
        return int(tex)
    except Exception:
        return None


def export_one(name: str, source_path: Path):
    data = json.loads(source_path.read_text())
    textures = {}
    for i, t in enumerate(data.get("textures", [])):
        textures[i] = {
            "name": safe_name(t.get("name", "placeholder.png")),
            "uv_width": max(1.0, float(t.get("uv_width") or data.get("resolution", {}).get("width") or 16.0)),
            "uv_height": max(1.0, float(t.get("uv_height") or data.get("resolution", {}).get("height") or 16.0)),
        }

    groups = {}
    mins = [10**9, 10**9, 10**9]
    maxs = [-10**9, -10**9, -10**9]

    for element in data.get("elements", []):
        verts = element.get("vertices") or {}
        faces = element.get("faces") or {}
        if not verts or not faces:
            continue

        for face in faces.values():
            order = list(face.get("vertices") or [])
            if len(order) not in (3, 4):
                continue

            tex_index = face_texture(face.get("texture"))
            tex = textures.get(tex_index)
            # keep texture paths valid Identifier paths (Minecraft rejects
            # spaces, "!", and uppercase in resource locations)
            if tex is not None:
                tex = dict(tex)
                tex["name"] = sanitize_tex_name(tex["name"])
            if tex is None:
                continue

            group = groups.setdefault(
                tex["name"],
                {
                    "texture": f"textures/entity/{tex['name']}",
                    "emissive": tex["name"] in {"white_e.png"},
                    "quads": [],
                    "uvs": [],
                    "normals": [],
                    "faces": 0,
                },
            )

            pts = []
            uv_pts = []
            uv_map = face.get("uv") or {}
            for key in order:
                pos = verts.get(key)
                uv = uv_map.get(key)
                if pos is None or uv is None or uv[0] is None or uv[1] is None:
                    pts = []
                    break
                pts.append([r(pos[0]), r(pos[1]), r(pos[2])])
                uv_pts.append([r(uv[0] / tex["uv_width"]), r(uv[1] / tex["uv_height"])])
            if not pts:
                continue

            if len(pts) == 3:
                pts.append(list(pts[2]))
                uv_pts.append(list(uv_pts[2]))

            n = quad_normal(pts[0], pts[1], pts[2])
            group["faces"] += 1
            group["normals"].extend(n)
            for pt in pts:
                group["quads"].extend(pt)
                for i in range(3):
                    mins[i] = min(mins[i], pt[i])
                    maxs[i] = max(maxs[i], pt[i])
            for uv in uv_pts:
                group["uvs"].extend(uv)

    out = {
        "source": str(source_path).replace('\\', '/'),
        "min": [r(v) for v in mins],
        "max": [r(v) for v in maxs],
        "width": r(max(maxs[0] - mins[0], maxs[2] - mins[2])),
        "height": r(maxs[1] - mins[1]),
        "groups": [groups[k] for k in sorted(groups)],
    }
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out_path = OUT_DIR / f"{name}.json"
    out_path.write_text(json.dumps(out, separators=(",", ":")))
    return out_path, out


def main():
    for name, path in EXPORTS.items():
        out_path, out = export_one(name, Path(path))
        size_kb = out_path.stat().st_size / 1024.0
        print(f"{name}: {out_path} ({size_kb:.1f} KB, {len(out['groups'])} groups, {out['height']} high)")


if __name__ == "__main__":
    main()
