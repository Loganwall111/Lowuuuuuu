#!/usr/bin/env python3
"""Offline acceptance checks for the Sift-Core standalone slice.

This intentionally does not pretend to be a Gradle/Javac replacement. It validates
all inputs that can be checked without downloading Minecraft, Fabric, or a JDK.
"""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def error(message: str) -> None:
    ERRORS.append(message)


def load_json(relative: str):
    path = ROOT / relative
    try:
        with path.open(encoding="utf-8") as handle:
            return json.load(handle)
    except FileNotFoundError:
        error(f"missing JSON: {relative}")
    except json.JSONDecodeError as exc:
        error(f"invalid JSON {relative}: {exc}")
    return None


def check_file(relative: str) -> None:
    if not (ROOT / relative).is_file():
        error(f"missing file: {relative}")


def balanced_glsl(path: Path) -> None:
    source = path.read_text(encoding="utf-8")
    if source.count("{") != source.count("}"):
        error(f"unbalanced braces: {path.relative_to(ROOT)}")
    if source.count("(") != source.count(")"):
        error(f"unbalanced parentheses: {path.relative_to(ROOT)}")
    if "#version 150" not in source:
        error(f"shader is missing GLSL 1.50 header: {path.relative_to(ROOT)}")


def compile_shader(path: Path, stage: str) -> None:
    validator = shutil.which("glslangValidator")
    if validator is None:
        # The repository also carries the validator used by the parent project.
        bundled = ROOT.parent / "glslcheck" / "bin" / "glslang"
        validator = str(bundled) if bundled.is_file() else None
    if validator is None:
        print(f"SKIP shader compiler (no glslang): {path.relative_to(ROOT)}")
        return

    result = subprocess.run(
        [validator, "-S", stage, str(path)],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if result.returncode != 0:
        error(f"GLSL {stage} failed for {path.relative_to(ROOT)}:\n{result.stdout.rstrip()}")


def check_png(relative: str) -> None:
    path = ROOT / relative
    check_file(relative)
    if not path.is_file():
        return
    header = path.read_bytes()[:24]
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        error(f"not a PNG: {relative}")
        return
    width = int.from_bytes(header[16:20], "big")
    height = int.from_bytes(header[20:24], "big")
    if width < 64 or height < 64:
        error(f"icon is too small ({width}x{height}): {relative}")


def main() -> int:
    for relative in (
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
        "gradle/wrapper/gradle-wrapper.jar",
        "gradle/wrapper/gradle-wrapper.properties",
        "src/main/resources/fabric.mod.json",
        "src/main/resources/mcsm.mixins.json",
        "src/main/resources/mcsm.client.mixins.json",
        "src/main/resources/data/mcsm/dimension/the_sift.json",
        "src/main/resources/data/mcsm/dimension_type/the_sift.json",
        "src/main/java/dev/siftcore/SiftCore.java",
        "src/main/java/dev/siftcore/transfer/SiftTransfer.java",
        "src/main/java/dev/siftcore/mixin/EntityMixin.java",
        "src/main/java/dev/siftcore/rift/SiftRiftRenderer.java",
    ):
        check_file(relative)

    wrapper_props = (ROOT / "gradle/wrapper/gradle-wrapper.properties").read_text(encoding="utf-8")
    if "gradle-8.7-bin.zip" not in wrapper_props:
        error("Gradle wrapper must use the CI-compatible Gradle 8.7 distribution")

    props = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    for key, expected in (
        ("minecraft_version", "1.20.1"),
        ("mod_version", "0.1.0-SIFT"),
    ):
        match = re.search(rf"^{re.escape(key)}=(.+)$", props, re.MULTILINE)
        if not match or match.group(1).strip() != expected:
            error(f"gradle.properties must set {key}={expected}")

    manifest = load_json("src/main/resources/fabric.mod.json")
    if manifest:
        if manifest.get("id") != "sift_core":
            error("fabric.mod.json id must be sift_core")
        if manifest.get("version") != "${version}":
            error("fabric.mod.json should use the Gradle version placeholder")
        if manifest.get("icon") != "assets/mcsm/icon.png":
            error("fabric.mod.json must point at the Sift icon")
        if "dev.siftcore.SiftCore" not in manifest.get("entrypoints", {}).get("main", []):
            error("common entrypoint is not registered")
        if "dev.siftcore.client.SiftCoreClient" not in manifest.get("entrypoints", {}).get("client", []):
            error("client entrypoint is not registered")

    dimension = load_json("src/main/resources/data/mcsm/dimension/the_sift.json")
    if dimension:
        if dimension.get("type") != "mcsm:the_sift":
            error("The Sift dimension must use mcsm:the_sift")
        generator = dimension.get("generator", {})
        settings = generator.get("settings", {})
        if generator.get("type") != "minecraft:flat":
            error("The Sift must use the vanilla flat generator for deterministic air chunks")
        if settings.get("layers") != []:
            error("The Sift generator must have an empty layers list")
        if settings.get("features") is not False:
            error("The Sift generator must disable features")

    dimension_type = load_json("src/main/resources/data/mcsm/dimension_type/the_sift.json")
    if dimension_type:
        if dimension_type.get("min_y", 0) % 16 != 0:
            error("dimension min_y must be a multiple of 16")
        if dimension_type.get("height", 0) % 16 != 0:
            error("dimension height must be a multiple of 16")
        if dimension_type.get("min_y", 0) + dimension_type.get("height", 0) > 2032:
            error("dimension top exceeds the 1.20.1 height limit")
        if dimension_type.get("effects") != "mcsm:sift":
            error("dimension type must use the registered mcsm:sift effects option")

    for relative in ("src/main/resources/mcsm.mixins.json", "src/main/resources/mcsm.client.mixins.json"):
        config = load_json(relative)
        if config:
            package = config.get("package", "")
            for class_name in config.get("mixins", []) + config.get("client", []):
                check_file("src/main/java/" + package.replace(".", "/") + "/" + class_name + ".java")

    check_png("src/main/resources/assets/mcsm/icon.png")
    for stem in ("sky", "rift", "final"):
        program = load_json(f"src/main/resources/assets/mcsm/shaders/core/{stem}.json")
        if program:
            for key in ("vertex", "fragment"):
                check_file(f"src/main/resources/assets/mcsm/shaders/core/{program[key]}.{ 'vsh' if key == 'vertex' else 'fsh'}")
        vertex = ROOT / f"src/main/resources/assets/mcsm/shaders/core/{stem}.vsh"
        fragment = ROOT / f"src/main/resources/assets/mcsm/shaders/core/{stem}.fsh"
        if vertex.is_file():
            balanced_glsl(vertex)
            compile_shader(vertex, "vert")
        if fragment.is_file():
            balanced_glsl(fragment)
            compile_shader(fragment, "frag")

    if ERRORS:
        print("SIFT validation: FAIL")
        for message in ERRORS:
            print(f"- {message}")
        return 1

    print("SIFT validation: PASS")
    print("Checked project metadata, dimension invariants, mixin layout, icon, and GLSL syntax.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
