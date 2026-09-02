#!/usr/bin/env python3
"""Wire Tainted's Accurate models + the death sequence into the renderer.

Run by apply_patch.py as step 16.  Idempotent: every edit is guarded by a
marker comment and skipped if already present.
"""
import os, sys, re

ROOT = sys.argv[1] if len(sys.argv) > 1 else "/var/tmp/build/dabsrc"
SRC = os.path.join(ROOT, "src/main/java/net/dabicco/witherstormmod")

def say(m): print("   " + m)

# ---------------------------------------------------------------- 1. config
cfg = os.path.join(SRC, "config/DabyWSClientConfig.java")
s = open(cfg).read()
if "tawAccurateModels" not in s:
    # insert new fields next to an existing public static boolean
    m = re.search(r'( *)public static boolean turquoiseTeeth\s*=[^\n]*\n', s)
    if m:
        ind = m.group(1)
        add = (
            f"{ind}// dabyws$taw: Tainted's Accurate Wither Storm Models v3.0.2.2\n"
            f"{ind}public static boolean tawAccurateModels = true;\n"
            f"{ind}public static boolean tawOgTextures = true;\n"
            f"{ind}public static boolean bbModelPreset = false;\n"
            f"{ind}public static boolean deathWhiteFlash = true;\n"
            f"{ind}public static boolean deathPurpleEmbers = true;\n"
            f"{ind}public static boolean deathCrumbleDebris = true;\n"
            f"{ind}public static double tawModelScale = 1.0;\n"
        )
        s = s[:m.end()] + add + s[m.end():]
        open(cfg, "w").write(s)
        say("7 config fields added")
    else:
        say("WARN could not find anchor field in DabyWSClientConfig")
else:
    say("config fields already present")

# ---------------------------------------------------------------- 2. renderer
rnd = os.path.join(SRC, "entity/renderer/WitherStormRenderer.java")
s = open(rnd).read()

if "dabyws$tawModels" not in s:
    anchor = "         super.submit(state, poseStack, submitNodeCollector, camera);\n"
    if anchor not in s:
        say("WARN renderer anchor not found -- TAW pass NOT wired")
    else:
        block = anchor + '''         // dabyws$tawModels: Tainted's Accurate Wither Storm Models.
         // Drawn as an additional pass so the vanilla ModelPart rig keeps
         // driving animation/hitboxes while these supply the silhouette.
         if (!this.previewShadowPass
               && net.dabicco.witherstormmod.config.DabyWSClientConfig.tawAccurateModels
               && !net.dabicco.witherstormmod.config.DabyWSClientConfig.bbModelPreset) {
            net.dabicco.witherstormmod.client.TawStormLayer.submit(
               state, poseStack, submitNodeCollector);
         }
'''
        s = s.replace(anchor, block, 1)
        open(rnd, "w").write(s)
        say("TAW render pass wired")
else:
    say("TAW render pass already wired")

# ---------------------------------------------------------------- 3. death fx tick
cl = os.path.join(SRC, "DabyWitherStormModClient.java")
s = open(cl).read()
if "dabyws$deathFx" not in s:
    # hook the existing client tick registration if present
    m = re.search(r'ClientTickEvents\.END_CLIENT_TICK\.register\(\(ClientTickEvents\.EndTick\)\(\w+\) -> \{', s)
    if m:
        ins = m.end()
        s = s[:ins] + '''
         // dabyws$deathFx: drive the whiteout / ember / crumble sequence
         net.dabicco.witherstormmod.client.StormDeathTicker.tick();
''' + s[ins:]
        open(cl, "w").write(s)
        say("death FX ticker hooked into END_CLIENT_TICK")
    else:
        say("WARN no END_CLIENT_TICK found -- ticker not hooked")
else:
    say("death FX ticker already hooked")


# ---------------------------------------------------------------- 4. assets
import shutil
HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(ROOT, "src/main/resources")

mesh_src = os.path.join(HERE, "..", "tawmesh")
mesh_dst = os.path.join(RES, "assets/dabywitherstormmod/tawmesh")
if os.path.isdir(mesh_src):
    os.makedirs(mesh_dst, exist_ok=True)
    n = 0
    for f in os.listdir(mesh_src):
        if f.endswith(".taw"):
            shutil.copy2(os.path.join(mesh_src, f), os.path.join(mesh_dst, f))
            n += 1
    say(f"{n} .taw meshes installed")

tex_src = os.path.join(HERE, "..", "tawtex")
tex_dst = os.path.join(RES, "assets/dabywitherstormmod/textures/entity/taw")
if os.path.isdir(tex_src):
    os.makedirs(tex_dst, exist_ok=True)
    n = 0
    for f in os.listdir(tex_src):
        if f.endswith(".png"):
            shutil.copy2(os.path.join(tex_src, f), os.path.join(tex_dst, f))
            n += 1
    say(f"{n} TAW textures installed")

pan_src = os.path.join(HERE, "..", "tawpanorama")
pan_dst = os.path.join(RES, "assets/minecraft/textures/gui/title/background")
if os.path.isdir(pan_src):
    os.makedirs(pan_dst, exist_ok=True)
    n = 0
    for f in os.listdir(pan_src):
        if f.startswith("panorama_"):
            shutil.copy2(os.path.join(pan_src, f), os.path.join(pan_dst, f))
            n += 1
    say(f"{n} panorama faces installed")


# ---------------------------------------------------------------- 5. config keys + GUI
cfg2 = os.path.join(SRC, "config/DabyWSClientConfig.java")
s = open(cfg2).read()
if 'key("tawAccurateModels"' not in s:
    anchor = re.search(r'( *)key\("turquoiseTeeth",[^\n]*\n', s)
    if anchor:
        ind = anchor.group(1)
        keys = (
            f'{ind}key("tawAccurateModels", "Use Tainted\'s Accurate Wither Storm models (v3.0.2.2). The screen-accurate shapes.", 0.0, 1.0, true, () -> tawAccurateModels ? 1.0 : 0.0, (v) -> tawAccurateModels = v >= 0.5);\n'
            f'{ind}key("tawOgTextures", "Use the OG Story Mode textures on the accurate models.", 0.0, 1.0, true, () -> tawOgTextures ? 1.0 : 0.0, (v) -> tawOgTextures = v >= 0.5);\n'
            f'{ind}key("bbModelPreset", "BB Model preset: revert to the original Blockbench models instead of the accurate ones.", 0.0, 1.0, true, () -> bbModelPreset ? 1.0 : 0.0, (v) -> bbModelPreset = v >= 0.5);\n'
            f'{ind}key("tawModelScale", "Size multiplier for the accurate models.", 0.05, 12.0, false, () -> tawModelScale, (v) -> tawModelScale = v);\n'
            f'{ind}key("deathWhiteFlash", "On death the whole Wither Storm flashes white before it goes.", 0.0, 1.0, true, () -> deathWhiteFlash ? 1.0 : 0.0, (v) -> deathWhiteFlash = v >= 0.5);\n'
            f'{ind}key("deathPurpleEmbers", "Purple particles stream off the Wither Storm as it dies.", 0.0, 1.0, true, () -> deathPurpleEmbers ? 1.0 : 0.0, (v) -> deathPurpleEmbers = v >= 0.5);\n'
            f'{ind}key("deathCrumbleDebris", "Chunks break off the dying Wither Storm and fall to the ground.", 0.0, 1.0, true, () -> deathCrumbleDebris ? 1.0 : 0.0, (v) -> deathCrumbleDebris = v >= 0.5);\n'
        )
        s = s[:anchor.end()] + keys + s[anchor.end():]
        open(cfg2, "w").write(s)
        say("7 config keys registered")
    else:
        say("WARN no key() anchor found")
else:
    say("config keys already registered")

gui = os.path.join(SRC, "client/gui/WitherStormConfigScreen.java")
s = open(gui).read()
if '"Accurate Models (Tainted\'s v3.0.2.2)"' not in s and 'tawAccurateModels' not in s:
    anchor = re.search(r'( *)this\.clientRow\("turquoiseTeeth",[^\n]*\n', s)
    if anchor:
        ind = anchor.group(1)
        rows = (
            f'{ind}this.clientRow("tawAccurateModels", "Accurate Models (Tainted\'s v3.0.2.2)", (BooleanSupplier)null);\n'
            f'{ind}this.clientRow("tawOgTextures", "OG Story Mode Textures", () -> !DabyWSClientConfig.tawAccurateModels);\n'
            f'{ind}this.clientRow("bbModelPreset", "BB Model Preset (original models)", (BooleanSupplier)null);\n'
            f'{ind}this.clientRow("tawModelScale", "Accurate Model Scale", () -> !DabyWSClientConfig.tawAccurateModels);\n'
            f'{ind}this.clientRow("deathWhiteFlash", "Death: White Flash", (BooleanSupplier)null);\n'
            f'{ind}this.clientRow("deathPurpleEmbers", "Death: Purple Particles", (BooleanSupplier)null);\n'
            f'{ind}this.clientRow("deathCrumbleDebris", "Death: Crumbling Debris", (BooleanSupplier)null);\n'
        )
        s = s[:anchor.end()] + rows + s[anchor.end():]
        open(gui, "w").write(s)
        say("7 GUI rows added")
    else:
        say("WARN no clientRow anchor found")
else:
    say("GUI rows already added")


cc_src = os.path.join(HERE, "..", "cloudcore")
cc_dst = os.path.join(RES, "assets/minecraft/shaders/core")
if os.path.isdir(cc_src):
    os.makedirs(cc_dst, exist_ok=True)
    n = 0
    for f in os.listdir(cc_src):
        if f.startswith("rendertype_clouds."):
            shutil.copy2(os.path.join(cc_src, f), os.path.join(cc_dst, f))
            n += 1
    say(f"{n} cloud core shaders installed")

print("   taw wiring done")
