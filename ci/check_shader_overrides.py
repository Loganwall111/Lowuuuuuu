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
compiled against and compares INTERFACES, in two severities, because only one of them can
blacken a frame:

  HARD -- something the GAME supplies and we would read differently. A replacement is
          disabled for these, because shipping one is shipping a black screen:
    * a `layout(std140) uniform NAME { ... }` block whose members, types or ORDER differ
      from the game's (a subset is as wrong as a superset -- the offsets move either way),
      or a block of ours the game does not declare at all;
    * a uniform name we share with the game but declare as a different type -- and any
      SAMPLER we declare that the game does not bind (an unbound sampler is an incomplete
      texture: it reads black on most drivers);
    * an `in` attribute in a vertex shader that the game's vertex format does not supply,
      or a declaration whose `layout(location = N)` disagrees with the game's (26.2 binds
      attributes by location, so a shifted attribute collapses the geometry, not the
      colour).

  TOLERATED -- legal, and reported but never a reason to strip:
    * a uniform of ours the game does not bind (it reads 0.0 -- the feature is simply off
      without a shader pack, which is this mod's stated design);
    * an extra `out` in a vertex shader (the opposite half simply does not declare the
      matching `in`);
    * a declaration of the game's that we do not have, when it is an input we do not read.

The body is free: rewriting the body is what a retexture of a shader is for.

Modes
-----
    --jar PATH                 the client jar to read the game's own copies from
    --check                    report the repo's own shader sources (default)
    --dump-out PATH            write the game's own declaration surface for every
                               program in --assembled (the interface to author against)
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
# `layout(location = 0) in vec3 Position;`, `flat out vec4 normal;`,
# `noperspective centroid in vec2 uv;` -- the qualifier is not part of the interface
# identity (a missing `flat` interpolates differently, it does not misread a block), so
# it is parsed and printed but not compared.
_LOCATION = re.compile(r"location\s*=\s*(\d+)")
_INOUT = re.compile(
    r"^\s*(layout\s*\(([^)]*)\)\s*)?"
    r"(?:(?:flat|smooth|noperspective|centroid|sample|patch|invariant|precise)\s+)*"
    r"(in|out)\s+([A-Za-z_]\w*)\s+([A-Za-z_]\w*)\s*(\[[^\]]*\])?\s*;")


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
            loc = None
            if m.group(2):
                lm = _LOCATION.search(m.group(2))
                if lm:
                    loc = int(lm.group(1))
            inout[m.group(5)] = {"direction": m.group(3),
                                 "type": m.group(4) + (m.group(6) or ""),
                                 "location": loc}
            i += 1
            continue
        i += 1
    return {"blocks": blocks, "uniforms": uniforms, "inout": inout}


SAMPLER = re.compile(r"^sampler")


def diff(ours, thiers):
    """(hard, tolerated) -- HARD is what can blacken a frame, and only HARD strips."""
    hard = []
    soft = []

    # ---- std140 blocks: positional, so exact, member by member, in order ----
    for name, members in ours["blocks"].items():
        if name not in thiers["blocks"]:
            hard.append("block %s: our file declares it, the game's own file does not "
                        "-- an unbound block is garbage" % name)
            continue
        if members != thiers["blocks"][name]:
            hard.append("block %s: %s here, %s in the game -- std140 reads by position, "
                        "so every value after the first difference is wrong"
                        % (name, members, thiers["blocks"][name]))
    for name in thiers["blocks"]:
        if name not in ours["blocks"]:
            soft.append("block %s: the game declares it and this file does not -- the "
                        "body cannot read those values (check what the body needs)"
                        % name)

    # ---- uniforms: a shared name must mean the same thing; a sampler the game does
    # ---- not bind is an incomplete texture, and that reads black
    for name, typ in ours["uniforms"].items():
        if name in thiers["uniforms"]:
            if thiers["uniforms"][name] != typ:
                hard.append("uniform %s: %s here, %s in the game"
                            % (name, typ, thiers["uniforms"][name]))
        elif SAMPLER.match(typ):
            hard.append("sampler %s (%s): the game binds no such sampler, and an unbound "
                        "sampler is an incomplete texture -- it reads black" % (name, typ))
        else:
            soft.append("uniform %s (%s): the game binds no such uniform -- it reads 0.0 "
                        "(the feature is off without a shader pack, by design)"
                        % (name, typ))
    for name in thiers["uniforms"]:
        if name not in ours["uniforms"]:
            soft.append("uniform %s: the game binds it and this file does not read it"
                        % name)

    # ---- in/out: locations are the interface in 26.2, the direction must not flip ----
    for name, mine in ours["inout"].items():
        if name not in thiers["inout"]:
            if mine["direction"] == "in" and mine["location"] is not None:
                hard.append("%s %s (%s, location = %d): the game supplies no such "
                            "attribute, so it reads as the default and the geometry "
                            "collapses" % (mine["direction"], name, mine["type"],
                                           mine["location"]))
            else:
                soft.append("%s %s (%s): the game declares no such name" % (mine["direction"],
                                                                           name, mine["type"]))
            continue
        theirs = thiers["inout"][name]
        if theirs["type"] != mine["type"] or theirs["direction"] != mine["direction"]:
            hard.append("%s %s: %s %s here, %s %s in the game"
                        % (mine["direction"], name, mine["direction"], mine["type"],
                           theirs["direction"], theirs["type"]))
            continue
        if (mine["location"] is not None and theirs["location"] is not None
                and mine["location"] != theirs["location"]):
            hard.append("%s %s: location = %d here, location = %d in the game -- 26.2 "
                        "binds by location, so this is a different value entirely"
                        % (mine["direction"], name, mine["location"], theirs["location"]))
    for name, theirs in thiers["inout"].items():
        if name not in ours["inout"]:
            soft.append("%s %s: the game declares it and this file does not"
                        % (theirs["direction"], name))
    return hard, soft


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


def dump_interfaces(names, theirs, path, ours_by_name=None):
    """Write the game's own declaration surface for every replaced program.

    This is what turns a mismatch from a mystery into a fix: the build publishes the
    interface the game itself declares, so the shader sources can be written to match it
    instead of being guessed at.
    """
    lines = ["# the game's own core-shader interfaces (from the client jar this build "
             "compiled against)", ""]
    for name in sorted(names):
        text = theirs.get(name)
        if text is None:
            lines.append("== %s: this build of the game does not ship it" % name)
            lines.append("")
            continue
        iface = interface_of(text)
        lines.append("== %s" % name)
        for bname, members in sorted(iface["blocks"].items()):
            lines.append("  block %s" % bname)
            for typ, mname in members:
                lines.append("    %s %s" % (typ, mname))
        for uname, utype in sorted(iface["uniforms"].items()):
            lines.append("  uniform %s %s" % (utype, uname))
        for ioname, decl in sorted(iface["inout"].items()):
            loc = "" if decl["location"] is None else " location = %d" % decl["location"]
            lines.append("  %s %s %s%s"
                         % (decl["direction"], decl["type"], ioname, loc))
        lines.append("")
    try:
        with open(path, "w", encoding="utf-8") as fh:
            fh.write("\n".join(lines))
        print("[shader] the game's own interfaces written to %s (%d programs)"
              % (path, len(names)))
    except OSError as exc:
        print("[shader] could not write %s: %s" % (path, exc))
    return lines


def check_dir(core_dir, theirs, strip, label, log):
    """Compare every shader in a built core directory with the game's own copy.

    Returns (checked, hard, tolerated, unknown). Only HARD disables a file: HARD is
    "something the game supplies and this shader would read differently", which is the
    class that renders a frame black without a single error message.
    """
    if not os.path.isdir(core_dir):
        return 0, 0, 0, 0
    checked = hard_count = soft_count = unknown = 0
    for name in sorted(os.listdir(core_dir)):
        if not name.endswith((".vsh", ".fsh")):
            continue
        path = os.path.join(core_dir, name)
        game = theirs.get(name)
        if game is None:
            unknown += 1  # a program name this build of the game does not have
            continue
        try:
            ours = interface_of(open(path, encoding="utf-8", errors="replace").read())
            ref = interface_of(game)
        except Exception:  # noqa: BLE001
            log.append("[shader] %s/%s -- unverifiable, kept" % (label, name))
            continue
        checked += 1
        hard, soft = diff(ours, ref)
        soft_count += len(soft)
        for line in soft:
            log.append("[shader] tolerated %s/%s: %s" % (label, name, line))
        if not hard:
            log.append("[shader] %s/%s -- interface matches the game's own copy%s"
                       % (label, name,
                          "" if not soft else " (%d tolerated difference%s)"
                          % (len(soft), "" if len(soft) == 1 else "s")))
            continue
        hard_count += 1
        log.append("[shader] MISMATCH %s/%s" % (label, name))
        for line in hard[:8]:
            log.append("[shader]     %s" % line)
        if strip:
            try:
                os.replace(path, path + ".disabled")
                log.append("[shader]     DISABLED: the game's own %s is used instead "
                           "(a normal-looking game, not a black one)" % name)
            except OSError as exc:
                log.append("[shader]     could not disable it: %s" % exc)
    return checked, hard_count, soft_count, unknown


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", default="")
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--assembled", default="")
    ap.add_argument("--strip", action="store_true")
    ap.add_argument("--dump-out", default="")
    args = ap.parse_args()

    theirs = vanilla_files(args.jar)
    if not theirs:
        print("[shader] no client jar at '%s' -- the game's own core shaders could not "
              "be read, so nothing is disabled and nothing is claimed" % args.jar)
        return 0
    print("[shader] the game's own core programs: %d read from %s"
          % (len(theirs), os.path.basename(args.jar)))

    log = []
    total_checked = total_hard = total_soft = 0

    if args.dump_out:
        # every program this jar replaces, in the game's own words. Evidence only:
        # if THIS breaks, the verdict below still has to run -- run 590 lost its whole
        # file-by-file comparison (and the disable pass that goes with it) to a stale
        # unpack in the dump helper, and a build that says "0 checked, 0 HARD" is worse
        # than one that says nothing at all.
        assembled = args.assembled or ""
        try:
            if assembled and os.path.isdir(assembled):
                names = set(n for n in os.listdir(assembled)
                            if n.endswith((".vsh", ".fsh")))
                dump_interfaces(names, theirs, args.dump_out)
        except Exception as exc:  # noqa: BLE001
            print("[shader] the interface dump could not be written (%s: %s) -- the "
                  "comparison below is unaffected" % (type(exc).__name__, exc))

    if args.assembled:
        c, h, so, _u = check_dir(args.assembled, theirs, args.strip,
                                 os.path.basename(os.path.dirname(args.assembled)), log)
        total_checked += c
        total_hard += h
        total_soft += so

    if not args.assembled:
        for src in SOURCE_DIRS:
            core = os.path.join(src, "core") if src.endswith("mcsm-core-shaders") \
                else os.path.join(src, CORE)
            c, h, so, _u = check_dir(core, theirs, False, os.path.basename(src), log)
            total_checked += c
            total_hard += h
            total_soft += so

    for line in log:
        print(line)
    print("[shader] interfaces: %d checked, %d HARD mismatch%s, %d tolerated difference%s"
          % (total_checked, total_hard, "" if total_hard == 1 else "es",
             total_soft, "" if total_soft == 1 else "s"))
    if total_hard and args.strip:
        print("[shader] the HARD ones are the black ones: they are DISABLED in the built "
              "tree, so the game's own program is used and the frame is wrong in colour at "
              "worst, never black")
    return 1 if (total_hard and not args.strip) else 0


if __name__ == "__main__":
    sys.exit(main())
