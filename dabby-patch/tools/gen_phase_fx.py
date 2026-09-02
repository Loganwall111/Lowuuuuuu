#!/usr/bin/env python3
"""Phase-6 purple flashbang + MCSM auto-generation config.

Run by apply_patch.py as step 17. Idempotent.
"""
import os, re, sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else "/var/tmp/build/dabsrc"
SRC = os.path.join(ROOT, "src/main/java/net/dabicco/witherstormmod")

def say(m): print("   " + m)

# ---------------------------------------------------------------- config fields
cfg = os.path.join(SRC, "config/DabyWSClientConfig.java")
s = open(cfg).read()
if "purpleFlashbang" not in s:
    m = re.search(r'( *)public static boolean turquoiseTeeth\s*=[^\n]*\n', s)
    if m:
        ind = m.group(1)
        s = s[:m.end()] + (
            f"{ind}// dabyws$flash: phase-6 purple flashbang\n"
            f"{ind}public static boolean purpleFlashbang = true;\n"
            f"{ind}public static double purpleFlashbangStrength = 1.0;\n"
            f"{ind}public static double purpleFlashbangPeriod = 2400.0;\n"
            f"{ind}public static boolean mcsmAutoGenerate = true;\n"
        ) + s[m.end():]
        open(cfg, "w").write(s)
        say("4 config fields added")
    else:
        say("WARN anchor missing")
else:
    say("config fields present")

# ---------------------------------------------------------------- config keys
s = open(cfg).read()
if 'key("purpleFlashbang"' not in s:
    a = re.search(r'( *)key\("turquoiseTeeth",[^\n]*\n', s)
    if a:
        ind = a.group(1)
        keys = (
            f'{ind}key("purpleFlashbang", "From phase 6 the storm sets off a blinding purple flash above itself every two minutes.", 0.0, 1.0, true, () -> purpleFlashbang ? 1.0 : 0.0, (v) -> purpleFlashbang = v >= 0.5);\n'
            f'{ind}key("purpleFlashbangStrength", "Brightness of the purple flashbang.", 0.0, 2.0, false, () -> purpleFlashbangStrength, (v) -> purpleFlashbangStrength = v);\n'
            f'{ind}key("purpleFlashbangPeriod", "Ticks between purple flashbangs (2400 = two minutes).", 200.0, 24000.0, false, () -> purpleFlashbangPeriod, (v) -> purpleFlashbangPeriod = v);\n'
            f'{ind}key("mcsmAutoGenerate", "Generate the Story Mode structures automatically the first time a world loads.", 0.0, 1.0, true, () -> mcsmAutoGenerate ? 1.0 : 0.0, (v) -> mcsmAutoGenerate = v >= 0.5);\n'
        )
        s = s[:a.end()] + keys + s[a.end():]
        open(cfg, "w").write(s)
        say("4 config keys registered")
else:
    say("config keys present")

# ---------------------------------------------------------------- GUI rows
gui = os.path.join(SRC, "client/gui/WitherStormConfigScreen.java")
s = open(gui).read()
if "purpleFlashbang" not in s:
    a = re.search(r'( *)this\.clientRow\("turquoiseTeeth",[^\n]*\n', s)
    if a:
        ind = a.group(1)
        rows = (
            f'{ind}this.clientRow("purpleFlashbang", "Phase 6+: Purple Flashbang", (BooleanSupplier)null);\n'
            f'{ind}this.clientRow("purpleFlashbangStrength", "Flashbang Brightness", () -> !DabyWSClientConfig.purpleFlashbang);\n'
            f'{ind}this.clientRow("purpleFlashbangPeriod", "Flashbang Interval (ticks)", () -> !DabyWSClientConfig.purpleFlashbang);\n'
            f'{ind}this.clientRow("mcsmAutoGenerate", "Auto-Generate MCSM Structures", (BooleanSupplier)null);\n'
        )
        s = s[:a.end()] + rows + s[a.end():]
        open(gui, "w").write(s)
        say("4 GUI rows added")
else:
    say("GUI rows present")

# ---------------------------------------------------------------- screen flash
ov = os.path.join(SRC, "client/StormAtmosphereOverlay.java")
s = open(ov).read()
if "dabyws$flashbang" not in s:
    m = re.search(r'public static void render\(GuiGraphicsExtractor g, DeltaTracker delta\) \{', s)
    if m:
        ins = m.end()
        block = '''
      // dabyws$flashbang: phase-6 purple flash, drawn over everything
      float flashA = net.dabicco.witherstormmod.client.PurpleFlashbang.intensity();
      if (flashA > 0.001F) {
         int fw = g.guiWidth();
         int fh = g.guiHeight();
         int fa = (int) (Math.min(1.0F, flashA) * 235.0F);
         g.fill(0, 0, fw, fh, (fa << 24) | 0xD98CFF);
      }
'''
        s = s[:ins] + block + s[ins:]
        open(ov, "w").write(s)
        say("flashbang screen overlay wired")
    else:
        say("WARN render() anchor missing")
else:
    say("flashbang overlay present")

# ---------------------------------------------------------------- ticker
cl = os.path.join(SRC, "DabyWitherStormModClient.java")
s = open(cl).read()
if "dabyws$flashTick" not in s:
    m = re.search(r'ClientTickEvents\.END_CLIENT_TICK\.register\(\(ClientTickEvents\.EndTick\)\(\w+\) -> \{', s)
    if m:
        s = s[:m.end()] + '''
         // dabyws$flashTick: drive the phase-6 purple flashbang
         net.dabicco.witherstormmod.client.PurpleFlashbangTicker.tick();
''' + s[m.end():]
        open(cl, "w").write(s)
        say("flashbang ticker hooked")
    else:
        say("WARN client tick anchor missing")
else:
    say("flashbang ticker present")


# ---------------------------------------------------------------- backdrop shrink
# The user liked the halo attached behind the storm and asked for it to be
# "just a tiny bit" smaller, layered ON TOP of the purple directional sky.
# Both layers stay on: StormBackdrop (attached, follows the storm) and
# StormSkyGradient (the wide purple sky wedge behind it).  backdropShrink
cfg3 = os.path.join(SRC, "config/DabyWSClientConfig.java")
s = open(cfg3).read()
import re as _re
m = _re.search(r'public static double stormBackdropSize = ([\d.]+);', s)
if m and m.group(1) != "5.1":
    s = s.replace(m.group(0), "public static double stormBackdropSize = 5.1;", 1)
    open(cfg3, "w").write(s)
    say(f"backdrop size {m.group(1)} -> 5.1 (15% smaller)")
else:
    say("backdrop size already 5.1")

print("   phase fx done")
