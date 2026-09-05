#!/usr/bin/env python3
"""Validate the staged JAR before allowing packaging; record fresh-class hashes."""

import hashlib
import json
from pathlib import Path
import struct
import subprocess
import sys


def sha256(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def verify(staged, classes, base, version, compiler):
    staged, classes, base = map(Path, (staged, classes, base))
    fresh = sorted(classes.rglob("*.class"))
    if not fresh:
        raise ValueError("No newly compiled classes")

    java_root = Path("mcsm-extras/java")
    sources = sorted(java_root.rglob("*.java"))
    for source in sources:
        expected = classes / source.relative_to(java_root).with_suffix(".class")
        if not expected.is_file():
            raise ValueError(f"javac did not emit {expected}")

    hashes = {}
    for compiled in fresh:
        name = compiled.relative_to(classes).as_posix()
        data = compiled.read_bytes()
        if len(data) < 8 or data[:4] != b"\xca\xfe\xba\xbe" or struct.unpack(">H", data[6:8])[0] != 69:
            raise ValueError(f"Not Java 25 bytecode: {name}")
        if (staged / name).read_bytes() != data:
            raise ValueError(f"Stale class in assembled JAR: {name}")
        hashes[name] = hashlib.sha256(data).hexdigest()

    for source_root, destination in [
        (Path("mcsm-core-shaders"), staged / "assets/minecraft/shaders"),
        (Path("jar-overrides"), staged),
    ]:
        for source in source_root.rglob("*"):
            if source.is_file() and source.read_bytes() != (destination / source.relative_to(source_root)).read_bytes():
                raise ValueError(f"Asset overlay mismatch: {source}")

    mod_path = staged / "fabric.mod.json"
    mod = json.loads(mod_path.read_text())
    for entry in mod["mixins"]:
        config_name = entry if isinstance(entry, str) else entry["config"]
        config = json.loads((staged / config_name).read_text())
        for side in ("mixins", "client", "server"):
            for name in config.get(side, []):
                path = (config["package"] + "." + name).replace(".", "/") + ".class"
                if not (staged / path).is_file():
                    raise ValueError(f"Registered mixin is missing: {path}")
    mod["version"] = version
    mod_path.write_text(json.dumps(mod, indent=2) + "\n", encoding="utf-8")

    commit = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    dirty = bool(subprocess.check_output(["git", "status", "--porcelain", "--untracked-files=no"], text=True))
    report = {
        "verdict": "FULL BUILD",
        "version": version,
        "minecraft": "26.2",
        "source_commit": commit,
        "source_dirty": dirty,
        "compiler": compiler,
        "release": 25,
        "base_jar": base.name,
        "base_sha256": sha256(base),
        "source_files": len(sources),
        "fresh_class_count": len(fresh),
        "fresh_classes_sha256": hashes,
        "source_sha256": {str(p): sha256(p) for p in sources},
        "mixin_registry": "PASS",
        "asset_overlays": "PASS",
    }
    evidence = json.dumps(report, indent=2) + "\n"
    (staged / "META-INF").mkdir(exist_ok=True)
    (staged / "META-INF/mcsm-build.json").write_text(evidence, encoding="utf-8")
    print(f"[verify] {len(fresh)} fresh Java 25 classes; all source classes, mixins and asset overlays present")
    return report


if __name__ == "__main__":
    verify(*sys.argv[1:])
