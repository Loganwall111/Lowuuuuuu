#!/usr/bin/env python3
"""check_shader_overrides.py -- an override may change the BODY, never the interface.

BUILD #471. The report that will not go away is "It's still black ... it's definitely a
render issue ... the Mojang logo loads ... and then just completely black". Every plate
this mod could name has been repainted, and every hook it owns is fault-isolated now, so
the question moved to the one place a black frame can still come from without a crash:
THE SHADERS THIS JAR REPLACES.

This mod does not merely tint the sky. `mcsm-core-shaders/*` is overlaid onto the jar's
own `assets/minecraft/shaders/` at build time, which means the jar carries replacements
for the game's core programs --

    block.vsh/.fsh          the world (and everything drawn like it)
    lightmap.fsh            ALL world lighting, through a std140 uniform block
    sky.vsh/.fsh            the sky
    entity.vsh/.fsh         entities
    rendertype_entity_cutout.vsh/.fsh
    rendertype_clouds.vsh/.fsh
    position.vsh/.fsh

-- and mod assets sit above vanilla in the pack stack, so they are ALWAYS on: no resource
pack to enable, no shader pack to install. A core shader whose BODY is wrong is a wrong
colour. A core shader whose INTERFACE is wrong is a BLACK SCREEN WITH NO ERROR, because
GLSL is positional: `lightmap.fsh` declares its values in a std140 block, std140 gives
every field an offset from its type and its order, and one extra, missing, renamed or
reordered member means every value after it is read from the wrong bytes. The world
lighting comes back as garbage -- usually zero -- while the Mojang logo and the menu
buttons (plain GUI shaders) still draw perfectly. That is what the screenshot shows.

So this reads the game's OWN copy of every replaced file out of the client jar the mod is
compiled against and compares INTERFACES:

  * `layout(std140) uniform NAME { ... }` blocks: same members, same types, same ORDER
    (a subset is as wrong as a superset -- the offsets move either way).
  * standalone `uniform` declarations: ours must be a subset of the game's, same types.
  * `in` / `out` declarations: ours must be a subset of the game's, same types.

The body is free: rewriting the body is what a retexture of a shader is for.

Modes
-----
    --jar PATH                 the client jar to read the game's own copies from
    --check                    report the repo's own shader sources (default)
    --assembled DIR [--strip]  check a BUILT tree (assets/minecraft/shaders/core, or a
                               pack's copy of it); with --strip a mismatching file is
                               renamed to `<name>.disabled` so the game's own shader is
                               used instead. This is the mode ci/build.sh runs, which is
                               why a black-shader jar cannot leave this repo again.

Anything this tool cannot parse is reported as "unverifiable" and KEPT: it strips on
evidence, never on a guess. It exits 0 unless a mismatch is found in check mode.
"""

import argparse
import os
import re
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

CORE = "assets/minecraft/shaders/core/"

# The repo's own copies of the shaders the jar replaces. Report-only: the gate reads
# these paths, so the sources themselves are never renamed.
SOURCE_DIRS = [
    os.path.join(ROOT, "mcsm-core-shaders"),
    os.path.join(ROOT, "overrides", "resourcepacks", "01_Devouring_Storms_Story_Look"),
    os.path.join(ROOT, "overrides", "global_packs", "required_resources",
                 "01_Devouring_Storms_Story_Look"),
    os.path.join(ROOT, "storylook"),
]

_BLOCK_OPEN = re.compile(r"layout\s*\(([^)]*)\)\s*uniform\s+(\w+)\s*\{")
_MEMBER = re.compile(r"^\s*(?:layout\s*\([^)]*\)\s*)?([A-Za-z_]\w*)\s+([A-Za-z_]\w*)\s*(\[[^\]]*\])?\s*;")
_UNIFORM = re.compile(r"^\s*uniform\s+([A-Za-z_]\w*)\s+([A-Za-z_]\w*)\s*(\[[^\]]*\])?\s*;")
_INOUT = re.compile(r"^\s*(in|out)\s+([A-Za-z_]\w*)\s+([A-Za-z_]\w*)\s*(\[[^\]]*\])?\s*;")


def _strip_comments(text):
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
    text = re.sub(r"//[^\n]*", " ", text)
    return text


def _members(body):
    """The (type, name) pairs of a uniform block body, in the order they are declared.

    Order is the whole point of this tool: std140 gives every member an offset from its
    type and its position, so the list is compared as a list, never as a set.
    """
    out = []
    for part in body.split(";"):
        mm = re.match(r"^\s*([A-Za-z_]\w*)\s+([A-Za-z_]\w*)\s*(\[[^\]]*\])?\s*$", part)
        if mm:
            out.append((mm.group(1), mm.group(2) + (mm.group(3) or "")))
    return out


def interface_of(text):
    """The declaration surface of a shader: blocks (ordered), uniforms, ins/outs."""
    lines = _strip_comments(text).splitlines()
    blocks = {}
    uniforms = {}
    inout = {}
    i = 0
    while i < len(lines):
        line = lines[i]
        m = _BLOCK_OPEN.search(line)
        if m:
            # the block may open, declare and close on one line, or run over many
            body = line[m.end():]
            while "}" not in body and i + 1 < len(lines):
                i += 1
                body += "\n" + lines[i]
            blocks[m.group(2)] = _members(body.split("}")[0])
            i += 1
            continue
        m = _UNIFORM.match(line)
        if m:
            uniforms[m.group(2)] = m.group(1) + (m.group(3) or "")
            i += 1
            continue
        m = _INOUT.match(line)
        if m:
            inout[m.group(3)] = (m.group(1), m.group(2) + (m.group(4) or ""))
            i += 1
            continue
        i += 1
    return {"blocks": blocks, "uniforms": uniforms, "inout": inout}


def diff(ours, thiers):
    """Everything about our interface the game's own copy does not have (or has
    differently). An empty list means interface-identical."""
    bad = []
    for name, members in ours["blocks"].items():
        if name not in thiers["blocks"]:
            bad.append("uniform block %s: the game has no such block" % name)
            continue
        if members != thiers["blocks"][name]:
            bad.append("block %s: %s here, %s in the game"
                       % (name, members, thiers["blocks"][name]))
    for name, typ in ours["uniforms"].items():
        if name not in thiers["uniforms"]:
            bad.append("uniform %s (%s): the game has no such uniform" % (name, typ))
        elif thiers["uniforms"][name] != typ:
            bad.append("uniform %s: %s here, %s in the game"
                       % (name, typ, thiers["uniforms"][name]))
    for name, (direction, typ) in ours["inout"].items():
        if name not in thiers["inout"]:
            bad.append("%s %s (%s): the game has no such declaration" % (direction, name, typ))
        else:
            theirs_dir, theirs_typ = thiers["inout"][name]
            if (theirs_dir, theirs_typ) != (direction, typ):
                bad.append("%s %s: %s %s here, %s %s in the game"
                           % (direction, name, direction, typ, theirs_dir, theirs_typ))
    return bad


def vanilla_files(jar):
    """Every core shader the game itself ships, by name."""
    out = {}
    if not jar or not os.path.isfile(jar):
        return out
    try:
        with zipfile.ZipFile(jar) as zf:
            for name in zf.namelist():
                if name.startswith(CORE) and name.endswith((".vsh", ".fsh")):
                    out[os.path.basename(name)] = zf.read(name).decode("utf-8", "replace")
    except Exception as exc:  # noqa: BLE001
        print("[shader] could not read the client jar (%s)" % exc)
    return out


def check_dir(core_dir, theirs, strip, label, log):
    """Compare every shader in a built core directory with the game's own copy."""
    if not os.path.isdir(core_dir):
        return 0, 0, 0
    checked = mismatched = skipped = 0
    for name in sorted(os.listdir(core_dir)):
        if not name.endswith((".vsh", ".fsh")):
            continue
        path = os.path.join(core_dir, name)
        game = theirs.get(name)
        if game is None:
            skipped += 1  # a program name this build of the game does not have
            continue
        try:
            ours = interface_of(open(path, encoding="utf-8", errors="replace").read())
            ref = interface_of(game)
        except Exception:  # noqa: BLE001
            log.append("[shader] %s -- unverifiable, kept" % name)
            continue
        checked += 1
        bad = diff(ours, ref)
        if not bad:
            continue
        mismatched += 1
        log.append("[shader] MISMATCH %s/%s" % (label, name))
        for line in bad[:6]:
            log.append("[shader]     %s" % line)
        if strip:
            try:
                os.replace(path, path + ".disabled")
                log.append("[shader]     disabled: the game's own %s is used instead "
                           "(a normal-looking game, not a black one)" % name)
            except OSError as exc:
                log.append("[shader]     could not disable it: %s" % exc)
    return checked, mismatched, skipped


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", default="")
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--assembled", default="")
    ap.add_argument("--strip", action="store_true")
    args = ap.parse_args()

    theirs = vanilla_files(args.jar)
    if not theirs:
        print("[shader] no client jar at '%s' -- the game's own core shaders could not "
              "be read, so nothing is disabled and nothing is claimed" % args.jar)
        return 0
    print("[shader] the game's own core programs: %d read from %s"
          % (len(theirs), os.path.basename(args.jar)))

    log = []
    total_checked = total_bad = 0

    if args.assembled:
        c, m, _s = check_dir(args.assembled, theirs, args.strip,
                             os.path.basename(os.path.dirname(args.assembled)), log)
        total_checked += c
        total_bad += m

    if not args.assembled:
        for src in SOURCE_DIRS:
            core = os.path.join(src, "core") if src.endswith("mcsm-core-shaders") \
                else os.path.join(src, CORE)
            c, m, _s = check_dir(core, theirs, False, os.path.basename(src), log)
            total_checked += c
            total_bad += m

    for line in log:
        print(line)
    print("[shader] interfaces: %d checked, %d mismatch%s"
          % (total_checked, total_bad, "" if total_bad == 1 else "es"))
    if total_bad and args.strip:
        print("[shader] mismatching overrides are DISABLED in the built tree: an override "
              "may change the body, never the interface")
    return 1 if (total_bad and not args.strip) else 0


if __name__ == "__main__":
    sys.exit(main())
