#!/usr/bin/env python3
"""
Integrated release validator — run by .github/workflows/mcsm-release.yml before
assets are pushed to the v1.9.60 GitHub Release, but usable standalone.

Checks (all hard failures):
  * Zips are FLAT: no single nested parent folder wrapping the pack contents.
  * MCSM_ResourcePack.zip contains the custom time-of-day skyboxes under
    assets/minecraft/optifine/sky/world0/ (sky1..4 png+properties), the #version 150
    extruded-cloud core vertex shader, split-range pack.mcmeta, and leak-free lang files.
  * MCSM_ShaderPack.zip is the modern-engine aligned PROCEDURAL pack:
      - uniform long worldTime in every sky/cloud program (Iris uniform type check)
      - NO reserved-keyword `uniform sampler2D texture;` samplers
      - gbuffers_clouds.fsh / rendertype_clouds.fsh are fully procedural (hash13/fbm)
      - 2.5x cloud extrusion in the cloud vertex shaders
      - sun shadow map (shadow.vsh/fsh) + shadowtex0 sampling in terrain/water
      - ZERO PNG cloud sheets and ZERO cloudTex bindings (hard failure otherwise)
      - no GLSL120 files under shaders/core (invalid for the #version 150 pipeline)
  * The mod JAR is a FRESH compile of the latest branch, i.e. it carries the storm
    atmosphere backdrop class (StormAtmospherePost), the sky/cloud mixins, the post
    processing filters, the post_effect jsons, AND the bundled skybox assets.
  * fabric.mod.json inside the JAR declares the expected version.

Also prints size + sha256 per artifact so the log can be matched against the
GitHub release asset `digest` field afterwards.
"""

import argparse
import hashlib
import json
import os
import sys
import zipfile

FAILURES = []


def fail(msg: str) -> None:
    FAILURES.append(msg)
    print(f"  FAIL  {msg}")


def ok(msg: str) -> None:
    print(f"  ok    {msg}")


def names(z: zipfile.ZipFile):
    return set(z.namelist())


def read(z: zipfile.ZipFile, name: str) -> bytes:
    return z.read(name)


def check_no_nested_parent(z: zipfile.ZipFile, label: str) -> None:
    tops = {n.split("/", 1)[0] for n in names(z) if n and not n.endswith("/")}
    # A "nested parent folder" zip wraps everything in exactly one directory
    # (e.g. MCSM_ResourcePack/pack.mcmeta). Flat packs open pack.mcmeta or
    # shaders.properties at the archive root.
    if len(tops) == 1 and next(iter(tops)) not in ("assets", "shaders", "lang", "pack.mcmeta"):
        fail(f"{label}: single top-level entry '{next(iter(tops))}/' — nested parent folder detected")
    else:
        ok(f"{label}: flat root structure (top-level: {', '.join(sorted(tops))[:120]})")


def check_leak_free(z: zipfile.ZipFile, label: str, lang_names) -> None:
    bad_tokens = ["http://", "https://", "| ---", "|:--", "```", "## ", "[1](", "](http"]
    for ln in lang_names:
        if ln not in names(z):
            continue
        text = read(z, ln).decode("utf-8", "replace")
        hits = [tok for tok in bad_tokens if tok in text]
        if hits:
            fail(f"{label}: metadata leak markers {hits} found in {ln}")
        else:
            ok(f"{label}: {ln} clean")


def validate_rp(path: str) -> None:
    print(f"[RP] {path}")
    with zipfile.ZipFile(path) as z:
        n = names(z)
        check_no_nested_parent(z, "RP")
        if "pack.mcmeta" not in n:
            fail("RP: pack.mcmeta missing at zip root")
        else:
            meta = json.loads(read(z, "pack.mcmeta").decode("utf-8"))
            pf = meta.get("pack", {}).get("pack_format")
            sf = meta.get("pack", {}).get("supported_formats", {})
            if pf != 46 or sf.get("min_format") != 42 or sf.get("max_format") != 50:
                fail(f"RP: pack.mcmeta split-range schema wrong: pack_format={pf} supported={sf}")
            else:
                ok("RP: pack.mcmeta modern split range (46 / 42..50)")
        for i in range(1, 5):
            for ext in ("png", "properties"):
                want = f"assets/minecraft/optifine/sky/world0/sky{i}.{ext}"
                if want not in n:
                    fail(f"RP: missing custom skybox file {want}")
        if all(f"assets/minecraft/optifine/sky/world0/sky{i}.png" in n for i in range(1, 5)):
            ok("RP: custom time-of-day skyboxes present (sky1..4 + properties)")
        for key, must in {
            "assets/minecraft/optifine/sky/world0/sky1.properties": "startFadeIn=5:30",
            "assets/minecraft/optifine/sky/world0/sky3.properties": "startFadeIn=17:30",
            "assets/minecraft/optifine/sky/world0/sky4.properties": "startFadeIn=20:30",
        }.items():
            if key in n and must not in read(z, key).decode("utf-8"):
                fail(f"RP: {key} missing fade spec '{must}'")
        want_core = "assets/minecraft/shaders/core/rendertype_clouds.vsh"
        if want_core not in n:
            fail(f"RP: {want_core} missing (3D cloud deck path)")
        elif "#version 150" not in read(z, want_core).decode("utf-8"):
            fail("RP: core rendertype_clouds.vsh is not #version 150 (vanilla pipeline)")
        else:
            ok("RP: #version 150 extruded cloud core vsh present")
        for cm in ("assets/minecraft/textures/colormap/grass.png",
                   "assets/minecraft/textures/colormap/foliage.png",
                   "assets/minecraft/textures/environment/clouds.png"):
            if cm not in n:
                fail(f"RP: expected texture {cm} missing")
        check_leak_free(z, "RP", ["lang/en_us.lang", "shaders/lang/en_us.lang"])


def _check_aligned_program(label: str, z: zipfile.ZipFile, name: str, *, need_long_time: bool,
                           forbid_texture_sampler: bool, extra_markers=()) -> None:
    n = names(z)
    if name not in n:
        fail(f"{label}: {name} missing")
        return
    text = read(z, name).decode("utf-8")
    if need_long_time and "worldTime" in text:
        if "uniform long worldTime" not in text:
            fail(f"{label}: {name} declares worldTime without `long` type — Iris will reject it")
    if "uniform int worldTime" in text:
        fail(f"{label}: {name} still declares `uniform int worldTime` (modern engines need long)")
    if forbid_texture_sampler and "uniform sampler2D texture;" in text:
        fail(f"{label}: {name} declares reserved-keyword sampler `texture` — must sample gtexture only")
    for marker in extra_markers:
        if marker not in text:
            fail(f"{label}: {name} missing alignment marker `{marker}`")
    ok(f"{label}: {name} aligned with modern engine spec")


def validate_sp(path: str) -> None:
    print(f"[SP] {path}")
    with zipfile.ZipFile(path) as z:
        n = names(z)
        check_no_nested_parent(z, "SP")
        props_name = "shaders/shaders.properties"
        if props_name not in n:
            fail(f"SP: {props_name} missing")
        else:
            props = read(z, props_name).decode("utf-8")
            if "clouds=fast" not in props:
                fail("SP: shaders.properties missing clouds=fast routing")
            if "customTexture.cloudTex" in props:
                fail("SP: shaders.properties still binds PNG cloud sheets (cloudTex) — clouds must be procedural GLSL")
            else:
                ok("SP: clouds=fast routing present, no cloudTex PNG bindings (procedural)")
        cloud_pngs = [x for x in n if "cloud" in x.lower() and x.lower().endswith(".png")]
        if cloud_pngs:
            fail(f"SP: {len(cloud_pngs)} PNG cloud sheet(s) shipped — clouds must be procedural GLSL ({cloud_pngs[:2]})")
        else:
            ok("SP: zero PNG cloud sheets (100% procedural GLSL clouds)")
        _check_aligned_program("SP", z, "shaders/gbuffers_clouds.fsh",
                               need_long_time=True, forbid_texture_sampler=True,
                               extra_markers=("hash13", "fbm", "worldTime"))
        _check_aligned_program("SP", z, "shaders/rendertype_clouds.fsh",
                               need_long_time=True, forbid_texture_sampler=True,
                               extra_markers=("hash13", "fbm", "worldTime"))
        for f in ("shaders/gbuffers_clouds.vsh", "shaders/rendertype_clouds.vsh"):
            if f not in n:
                fail(f"SP: {f} missing")
                continue
            text = read(z, f).decode("utf-8")
            if "worldPos.y *= 2.5" not in text and "scaledVertex.y *= 2.5" not in text:
                fail(f"SP: {f} missing the 2.5x Story Mode cloud extrusion")
            else:
                ok(f"SP: {f} 2.5x cloud extrusion present")
        # Sun shadow map on ground + water.
        for f in ("shaders/shadow.vsh", "shaders/shadow.fsh"):
            if f not in n:
                fail(f"SP: {f} missing (sun shadow map pass)")
            else:
                ok(f"SP: {f} present")
        for f in ("shaders/gbuffers_terrain.fsh", "shaders/gbuffers_water.fsh"):
            if f not in n:
                fail(f"SP: {f} missing (shadow-receiving surface)")
                continue
            text = read(z, f).decode("utf-8")
            if "shadowtex0" not in text:
                fail(f"SP: {f} does not sample shadowtex0 — ground/water shadows missing")
            else:
                ok(f"SP: {f} samples shadowtex0 (sun-cast shadows)")
        _check_aligned_program("SP", z, "shaders/gbuffers_skybasic.fsh",
                               need_long_time=True, forbid_texture_sampler=True,
                               extra_markers=("getSunsetSky",))
        _check_aligned_program("SP", z, "shaders/gbuffers_skybasic.vsh",
                               need_long_time=True, forbid_texture_sampler=True)
        _check_aligned_program("SP", z, "shaders/gbuffers_skytextured.fsh",
                               need_long_time=True, forbid_texture_sampler=True,
                               extra_markers=("warmTint", "lavenderNight"))
        _check_aligned_program("SP", z, "shaders/gbuffers_skytextured.vsh",
                               need_long_time=True, forbid_texture_sampler=True)
        for f in ("shaders/gbuffers_terrain.fsh", "shaders/gbuffers_entities.fsh",
                  "shaders/gbuffers_hand.fsh", "shaders/gbuffers_hand_water.fsh"):
            _check_aligned_program("SP", z, f, need_long_time=False, forbid_texture_sampler=True)
        core_bad = [x for x in n if x.startswith("shaders/core/") and x.endswith((".fsh", ".vsh"))]
        for x in core_bad:
            if "#version 150" not in read(z, x).decode("utf-8", "replace"):
                fail(f"SP: {x} is a non-#version-150 core override — breaks the vanilla core pipeline")
        if not core_bad:
            ok("SP: no stale GLSL120 core overrides shipped")
        else:
            ok("SP: core overrides are all #version 150")


def validate_jar(path: str, expect_version: str) -> None:
    print(f"[JAR] {path}")
    with zipfile.ZipFile(path) as z:
        n = names(z)
        for cls in (
            "net/dabicco/witherstormmod/client/StormAtmospherePost.class",
            "net/dabicco/witherstormmod/client/StormCloudDeck.class",
            "net/dabicco/witherstormmod/client/StormPresenceFX.class",
            "net/dabicco/witherstormmod/mixin/SkyRendererMixin.class",
            "net/dabicco/witherstormmod/mixin/CloudColorMixin.class",
        ):
            if cls not in n:
                fail(f"JAR: missing class {cls} — JAR was not compiled from the latest master sources")
        ok("JAR: storm atmosphere backdrop + cloud deck + sky/cloud mixin classes present")
        post_fsh = [x for x in n if x.startswith("assets/dabywitherstormmod/shaders/post/") and x.endswith(".fsh")]
        if len(post_fsh) < 11:
            fail(f"JAR: only {len(post_fsh)} post-processing filter files bundled (expected >= 11)")
        else:
            ok(f"JAR: {len(post_fsh)} post-processing filters bundled")
        pe = [x for x in n if x.endswith(".json") and "/post_effect/" in x]
        if not pe:
            fail("JAR: no post_effect json definitions bundled")
        else:
            ok(f"JAR: {len(pe)} post_effect definitions bundled")
        jar_sky_fail = 0
        for i in range(1, 5):
            for ext in ("png", "properties"):
                want = f"assets/minecraft/optifine/sky/world0/sky{i}.{ext}"
                if want not in n:
                    jar_sky_fail += 1
                    fail(f"JAR: lavender/orange skybox asset {want} not bundled into the mod")
        if jar_sky_fail == 0:
            ok("JAR: custom skyboxes bundled under assets/minecraft/optifine/sky/world0/")
        if "assets/fabricskyboxes/sky/mcsm_twilight.json" not in n:
            fail("JAR: FabricSkyboxes mcsm_twilight.json missing")
        if "pack.mcmeta" not in n:
            fail("JAR: pack.mcmeta missing (mod assets will not merge as a resource tree)")
        fmj = json.loads(read(z, "fabric.mod.json").decode("utf-8"))
        if fmj.get("version") != expect_version:
            fail(f"JAR: fabric.mod.json version {fmj.get('version')!r} != expected {expect_version!r}")
        else:
            ok(f"JAR: fabric.mod.json version == {expect_version}")
        if "${version}" in read(z, "fabric.mod.json").decode("utf-8"):
            fail("JAR: fabric.mod.json still contains an unexpanded ${version} template token")


def digest(path: str) -> None:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    print(f"  size={os.path.getsize(path)} bytes  sha256={h.hexdigest()}  {os.path.basename(path)}")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--rp", required=True)
    ap.add_argument("--sp", required=True)
    ap.add_argument("--jar", required=True)
    ap.add_argument("--expect-version", required=True)
    a = ap.parse_args()
    for p in (a.rp, a.sp, a.jar):
        if not os.path.exists(p):
            fail(f"artifact missing: {p}")
    if FAILURES:
        print("\n".join(FAILURES))
        return 1
    validate_rp(a.rp)
    validate_sp(a.sp)
    validate_jar(a.jar, a.expect_version)
    print("\n[digests]")
    for p in (a.rp, a.sp, a.jar):
        digest(p)
    if FAILURES:
        print(f"\nVALIDATION FAILED ({len(FAILURES)} problem(s))")
        return 1
    print("\nVALIDATION PASSED — integrated build is release-ready")
    return 0


if __name__ == "__main__":
    sys.exit(main())
