#!/usr/bin/env python3
"""
merge_release_jar.py — Build the r1 release JAR.

Strategy: the mod's compiled classes come from the original release binary
(dabywitherstormmod-1.9.60-26.2-beta.zip, 352 classes — no JDK is available in
this sandbox, so we cannot recompile). The r1 visuals live entirely in assets,
so the release jar = original jar entries + every file under src/main/resources
(which carries the merged MCSM resource-pack assets: skyboxes, procedural cloud
vertex shader, vortex mesh, storm_atmosphere post chain, OG shaded textures and
_e emissive pairs).

The CI workflow (build.yml / mcsm-release.yml) additionally compiles the source
tree with Java 25 + Loom and renames the jar per build (r<run-number>); that
compiled jar supersedes this one when a build succeeds.
"""

import hashlib
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_JAR = os.path.join(ROOT, "dabywitherstormmod-1.9.60-26.2-beta.zip")
RES_DIR = os.path.join(ROOT, "src", "main", "resources")
VERSION = "1.9.61-26.2-beta-r1"
OUT_JAR = os.path.join(ROOT, "docs", "releases", "r1", f"dabywitherstormmod-{VERSION}.jar")


def main() -> int:
    if not os.path.isfile(SRC_JAR):
        print(f"missing source jar: {SRC_JAR}")
        return 1
    if not os.path.isdir(RES_DIR):
        print(f"missing resources dir: {RES_DIR}")
        return 1

    entries: dict[str, bytes] = {}

    # 1. Original jar entries (classes, original assets). Dev/reference
    #    artifacts already present in the source jar (ffmpeg binary + docs)
    #    are dropped here so the release jar never ships them.
    with zipfile.ZipFile(SRC_JAR) as z:
        for info in z.infolist():
            name = info.filename
            if name.startswith("assets/dabywitherstormmod/sounds/ffmpeg/"):
                continue
            if name.endswith(".bbmodel") or name.endswith("/ffmpeg.exe"):
                continue
            entries[name] = z.read(name)

    # 2. Overlay the merged mod resources.
    added: list[str] = []
    for dirpath, _dirnames, filenames in os.walk(RES_DIR):
        for name in filenames:
            full = os.path.join(dirpath, name)
            rel = os.path.relpath(full, RES_DIR).replace(os.sep, "/")
            # Dev/reference artifacts that must never ship in a release jar:
            # Blockbench sources (geo/) and the bundled ffmpeg binary.
            if rel.startswith("assets/dabywitherstormmod/geo/"):
                continue
            if rel.startswith("assets/dabywitherstormmod/sounds/ffmpeg/"):
                continue
            if rel.endswith(".bbmodel") or name == "ffmpeg.exe":
                continue
            with open(full, "rb") as fh:
                data = fh.read()
            if entries.get(rel) != data:
                added.append(rel)
            entries[rel] = data

    # 3. Write the new jar (entries in original order first, then additions).
    os.makedirs(os.path.dirname(OUT_JAR), exist_ok=True)
    with zipfile.ZipFile(OUT_JAR, "w", zipfile.ZIP_DEFLATED) as z:
        for name, data in entries.items():
            z.writestr(name, data)

    with open(OUT_JAR, "rb") as fh:
        digest = hashlib.sha256(fh.read()).hexdigest()

    print(f"wrote {OUT_JAR}")
    print(f"entries: {len(entries)}  (added/updated: {len(added)})")
    print(f"sha256: {digest}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
