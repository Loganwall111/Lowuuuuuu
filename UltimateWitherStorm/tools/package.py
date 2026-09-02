#!/usr/bin/env python3
"""Zip the resource pack + datapack and write the install manifest."""
import os, zipfile, json, hashlib

ROOT = "/home/user/UltimateWitherStorm"
DIST = f"{ROOT}/dist"
os.makedirs(DIST, exist_ok=True)


def zipdir(src, out, root_inside=""):
    n = 0
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as z:
        for base, _, files in os.walk(src):
            for f in files:
                p = os.path.join(base, f)
                rel = os.path.relpath(p, src)
                z.write(p, os.path.join(root_inside, rel) if root_inside else rel)
                n += 1
    return n, os.path.getsize(out)


items = []
n, s = zipdir(f"{ROOT}/resourcepack", f"{DIST}/UltimateMCSM-ResourcePack.zip")
items.append(("UltimateMCSM-ResourcePack.zip", n, s, "resourcepacks/"))

n, s = zipdir(f"{ROOT}/datapack", f"{DIST}/UltimateMCSM-Datapack.zip")
items.append(("UltimateMCSM-Datapack.zip", n, s, "<world>/datapacks/"))

# schematics bundle (WorldEdit / Litematica)
n, s = zipdir(f"{ROOT}/schematics", f"{DIST}/UltimateMCSM-Schematics.zip")
items.append(("UltimateMCSM-Schematics.zip", n, s, "config/worldedit/schematics/"))

# configs
n, s = zipdir(f"{ROOT}/config", f"{DIST}/UltimateMCSM-Configs.zip")
items.append(("UltimateMCSM-Configs.zip", n, s, "config/"))

print(f"{'FILE':<42}{'FILES':>7}{'SIZE':>12}   INSTALL TO")
for name, cnt, size, dest in items:
    print(f"{name:<42}{cnt:>7}{size/1048576:>10.1f}MB   {dest}")

json.dump({
    "name": "Ultimate MCSM Wither Storm",
    "builtFor": "Minecraft 1.20.1",
    "artifacts": [{"file": a, "files": b, "bytes": c, "installTo": d} for a, b, c, d in items],
}, open(f"{DIST}/manifest.json", "w"), indent=2)
