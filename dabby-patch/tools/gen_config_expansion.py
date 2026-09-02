#!/usr/bin/env python3
"""
Expand the config with a large block of fine-grained Story Mode controls.

The user asked for a "gigantic customizable overhaul" -- far more than the
~50 options they were seeing. This generates:

  * per-phase storm sky colour (RGB + enable, phases 4.5 / 5 / 6 / 6.5 / 7)
  * per-time-of-day sky, horizon, cloud and light colour (day/dusk/night/dawn)
  * per-biome sky tint (plains, desert, snowy, swamp, jungle, savanna,
    badlands, ocean, mushroom, nether, end)
  * per-phase fog density and colour override
  * dynamic sky behaviour (range, falloff, core size, blend curve)

Each entry becomes both a DabyWSClientConfig field + key() registration and a
GUI row, so they are all live and persisted.
"""

import os
import re

SRC = "/var/tmp/build/dabsrc/src/main/java/net/dabicco/witherstormmod"
CFG = os.path.join(SRC, "config/DabyWSClientConfig.java")
GUI = os.path.join(SRC, "client/gui/WitherStormConfigScreen.java")

MARK = "// dabyws$expandedConfig"


def rgb(prefix, label, r, g, b, desc):
    """Three colour sliders + a master toggle."""
    out = []
    out.append((f"{prefix}Enabled", f"{label}", 0.0, 1.0, True, 1.0,
                f"Enable {desc}."))
    for ch, val in (("R", r), ("G", g), ("B", b)):
        out.append((f"{prefix}{ch}", f"{label} {ch}", 0.0, 1.0, False, val,
                    f"{desc} - {ch} channel."))
    return out


def build_entries():
    e = []

    # ---- per-phase storm sky colours ------------------------------------
    e += rgb("phaseSky45", "Phase 4.5 Sky", 0.094, 0.184, 0.180,
             "the dark turquoise sky at phase 4.5")
    e += rgb("phaseSky50", "Phase 5 Sky", 0.110, 0.210, 0.200,
             "the green sky held through phase 5")
    e += rgb("phaseSky60", "Phase 6 Sky", 0.220, 0.145, 0.325,
             "the purple sky after phase 5 ends")
    e += rgb("phaseSky65", "Phase 6.5 Sky", 0.463, 0.102, 0.404,
             "the magenta sky as it grows")
    e += rgb("phaseSky70", "Phase 7 Sky", 0.639, 0.180, 0.573,
             "the final violet-pink gradient")
    e += rgb("phaseSkyRed", "Embedded Red", 0.400, 0.075, 0.145,
             "the red mixed into the late gradient")

    # ---- time of day ----------------------------------------------------
    e += rgb("todSkyDay", "Day Sky", 0.596, 0.549, 0.965, "the daytime sky")
    e += rgb("todSkyDusk", "Dusk Sky", 0.690, 0.400, 0.400, "the dusk sky")
    e += rgb("todSkyNight", "Night Sky", 0.098, 0.114, 0.400, "the night sky")
    e += rgb("todSkyDawn", "Dawn Sky", 0.760, 0.560, 0.660, "the dawn sky")
    e += rgb("todHorizonDay", "Day Horizon", 0.760, 0.720, 0.980, "the daytime horizon")
    e += rgb("todHorizonDusk", "Dusk Horizon", 0.945, 0.573, 0.404, "the dusk horizon")
    e += rgb("todHorizonNight", "Night Horizon", 0.290, 0.404, 0.925, "the night horizon")
    e += rgb("todHorizonDawn", "Dawn Horizon", 0.960, 0.660, 0.760, "the dawn horizon")
    e += rgb("todCloudDay", "Day Clouds", 0.965, 0.961, 1.000, "daytime cloud colour")
    e += rgb("todCloudDusk", "Dusk Clouds", 0.980, 0.760, 0.620, "dusk cloud colour")
    e += rgb("todCloudNight", "Night Clouds", 0.298, 0.361, 0.678, "night cloud colour")
    e += rgb("todCloudDawn", "Dawn Clouds", 0.941, 0.800, 0.859, "dawn cloud colour")
    e += rgb("todLightDay", "Day Light", 1.000, 0.980, 1.000, "daytime light tint")
    e += rgb("todLightDusk", "Dusk Light", 1.000, 0.780, 0.640, "dusk light tint")
    e += rgb("todLightNight", "Night Light", 0.560, 0.640, 1.000, "night light tint")
    e += rgb("todLightDawn", "Dawn Light", 1.000, 0.860, 0.900, "dawn light tint")

    # ---- biome skies -----------------------------------------------------
    biomes = [
        ("Plains", 0.596, 0.549, 0.965),
        ("Desert", 0.980, 0.820, 0.580),
        ("Snowy", 0.720, 0.840, 1.000),
        ("Swamp", 0.420, 0.550, 0.400),
        ("Jungle", 0.380, 0.640, 0.520),
        ("Savanna", 0.900, 0.760, 0.480),
        ("Badlands", 0.880, 0.560, 0.360),
        ("Ocean", 0.420, 0.620, 0.940),
        ("Mushroom", 0.700, 0.520, 0.780),
        ("Nether", 0.610, 0.070, 0.480),
        ("End", 0.180, 0.140, 0.240),
    ]
    for name, r, g, b in biomes:
        e += rgb(f"biomeSky{name}", f"{name} Sky", r, g, b, f"the {name.lower()} sky")

    # ---- fog per phase ---------------------------------------------------
    for ph in ("45", "50", "60", "65", "70"):
        pretty = ph[0] + "." + ph[1] if ph[1] != "0" else ph[0]
        e.append((f"phaseFog{ph}", f"Phase {pretty} Fog Density", 0.0, 2.0, False, 1.0,
                  f"Fog density multiplier at phase {pretty}."))

    # ---- dome behaviour --------------------------------------------------
    e.append(("stormSkyRange", "Sky Reaction Range", 100.0, 3000.0, False, 900.0,
              "How far away the storm still colours the sky, in blocks."))
    e.append(("stormSkyFalloff", "Sky Falloff", 0.0, 1.0, False, 0.55,
              "Fraction of the range held at full strength before easing out."))
    e.append(("stormSkyCore", "Black Core Size", 0.0, 2.0, False, 0.65,
              "How strongly the black core darkens the dome."))
    e.append(("stormSkySmooth", "Sky Blend Speed", 0.005, 0.5, False, 0.05,
              "How quickly the sky eases toward its target colour."))
    return e


def main():
    entries = build_entries()
    cs = open(CFG).read()
    if MARK in cs:
        print("  already expanded")
        return

    fields, keys, rows = [], [], []
    for name, label, lo, hi, toggle, default, desc in entries:
        if toggle:
            fields.append(f"   public static boolean {name} = {'true' if default >= 0.5 else 'false'};")
            keys.append(f'      key("{name}", "{desc}", 0.0, 1.0, true, '
                        f"() -> {name} ? 1.0 : 0.0, (v) -> {name} = v >= 0.5);")
        else:
            fields.append(f"   public static double {name} = {default};")
            keys.append(f'      key("{name}", "{desc}", {lo}, {hi}, false, '
                        f"() -> {name}, (v) -> {name} = v);")
        rows.append((name, label))

    # inject fields
    anchor = "   public static boolean turquoiseTeeth = true;"
    cs = cs.replace(anchor, MARK + "\n" + "\n".join(fields) + "\n" + anchor, 1)

    # inject key() registrations next to the existing ones
    kanchor = '      key("turquoiseTeeth",'
    cs = cs.replace(kanchor, "\n".join(keys) + "\n" + kanchor, 1)
    open(CFG, "w").write(cs)

    # ---- GUI rows --------------------------------------------------------
    gs = open(GUI).read()
    gui_lines = ['      this.header("Story Mode: Per-Phase Sky");']
    section_at = {
        "phaseSky45": "Story Mode: Per-Phase Sky",
        "todSkyDay": "Story Mode: Time of Day",
        "biomeSkyPlains": "Story Mode: Biome Skies",
        "phaseFog45": "Story Mode: Per-Phase Fog",
        "stormSkyRange": "Story Mode: Dynamic Sky Behaviour",
    }
    for name, label in rows:
        if name in section_at and not gui_lines[-1].endswith(f'"{section_at[name]}");'):
            gui_lines.append(f'      this.header("{section_at[name]}");')
        gui_lines.append(f'      this.clientRow("{name}", "{label}", (BooleanSupplier)null);')

    ganchor = '      this.header("Story Mode Clouds");'
    gs = gs.replace(ganchor, "\n".join(gui_lines) + "\n" + ganchor, 1)
    open(GUI, "w").write(gs)

    print(f"  added {len(entries)} config keys + {len(rows)} GUI rows")


if __name__ == "__main__":
    main()
