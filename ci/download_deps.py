#!/usr/bin/env python3
"""Download the exact Minecraft 26.2 compile dependencies, with integrity checks."""

import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import zipfile

MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
MINECRAFT_VERSION = "26.2"
MIXIN_URL = (
    "https://maven.fabricmc.net/net/fabricmc/sponge-mixin/"
    "0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar"
)


def download(url, path, sha1=None):
    """Only cache complete downloads; never reuse a truncated JAR or HTML error."""
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    if sha1 and path.is_file() and hashlib.sha1(path.read_bytes()).hexdigest() == sha1:
        return path
    partial = path.with_name(path.name + ".part")
    try:
        subprocess.run([
            "curl", "--fail", "--silent", "--show-error", "--location",
            "--retry", "3", "--connect-timeout", "20", "--max-time", "300",
            "--output", str(partial), url,
        ], check=True)
        if sha1 and hashlib.sha1(partial.read_bytes()).hexdigest() != sha1:
            raise ValueError(f"SHA-1 mismatch for {url}")
        if path.suffix == ".jar":
            with zipfile.ZipFile(partial) as jar:
                bad = jar.testzip()
                if bad:
                    raise ValueError(f"Corrupt archive entry {bad} in {url}")
        partial.replace(path)
    finally:
        partial.unlink(missing_ok=True)
    print(f"[deps] {path.name}: {path.stat().st_size:,} bytes")
    return path


def resolve(cache):
    cache = Path(cache).resolve()
    manifest = json.loads(download(MANIFEST_URL, cache / "manifest.json").read_text())
    version = next((v for v in manifest["versions"] if v["id"] == MINECRAFT_VERSION), None)
    if version is None:
        raise ValueError(f"Minecraft {MINECRAFT_VERSION} is absent from the version manifest")
    metadata = json.loads(download(
        version["url"], cache / "version.json", version.get("sha1")
    ).read_text())
    if metadata["id"] != MINECRAFT_VERSION:
        raise ValueError("Minecraft version metadata does not match the requested version")

    client = metadata["downloads"]["client"]
    jars = [download(client["url"], cache / "client.jar", client["sha1"])]
    # Use the game's own versions instead of guessing JOML/DFU/Guava versions.
    # Native classifiers are not needed by javac. Plain library artifacts are
    # platform-independent and are safe to include in this compile-only path.
    for library in metadata["libraries"]:
        artifact = library.get("downloads", {}).get("artifact")
        if not artifact or "natives-" in library["name"]:
            continue
        jars.append(download(
            artifact["url"], cache / "libraries" / artifact["path"], artifact["sha1"]
        ))

    mixin_hash = download(MIXIN_URL + ".sha1", cache / "mixin.sha1").read_text().split()[0]
    if not re.fullmatch(r"[0-9a-fA-F]{40}", mixin_hash):
        raise ValueError("Invalid Mixin checksum")
    jars.append(download(MIXIN_URL, cache / "mixin.jar", mixin_hash.lower()))
    (cache / "classpath.txt").write_text(os.pathsep.join(map(str, jars)), encoding="utf-8")
    return jars


if __name__ == "__main__":
    try:
        resolve(sys.argv[1])
    except (OSError, ValueError, KeyError, zipfile.BadZipFile, subprocess.CalledProcessError) as exc:
        sys.exit(f"[deps] FAILED: {exc}")
