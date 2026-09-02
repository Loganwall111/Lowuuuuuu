#!/usr/bin/env python3
"""
Apply the MCSM atmosphere patch to a fresh clone of the mod.

Idempotent: re-running on an already-patched tree is a no-op.
Usage:  python3 apply_patch.py /var/tmp/build/dabsrc
"""
import os
import shutil
import sys

SRC = "/home/user/dabby-patch/overlay"
ROOT = sys.argv[1] if len(sys.argv) > 1 else "/var/tmp/build/dabsrc"
JAVA = os.path.join(ROOT, "src/main/java")
RES = os.path.join(ROOT, "src/main/resources")


def say(*a):
    print("  ", *a)


# ---------------------------------------------------------------- 1. Java files
print("1. Java sources")
for dirpath, _, files in os.walk(os.path.join(SRC, "java")):
    for f in files:
        s = os.path.join(dirpath, f)
        rel = os.path.relpath(s, os.path.join(SRC, "java"))
        d = os.path.join(JAVA, rel)
        os.makedirs(os.path.dirname(d), exist_ok=True)
        shutil.copy2(s, d)
        say("->", rel)

# ---------------------------------------------------------------- 2. assets
print("2. Assets")
for dirpath, _, files in os.walk(os.path.join(SRC, "assets")):
    for f in files:
        s = os.path.join(dirpath, f)
        rel = os.path.relpath(s, os.path.join(SRC, "assets"))
        d = os.path.join(RES, "assets", rel)
        os.makedirs(os.path.dirname(d), exist_ok=True)
        shutil.copy2(s, d)
        say("->", rel)

# ---------------------------------------------------------------- 3. data (structures)
datadir = os.path.join(SRC, "data")
if os.path.isdir(datadir):
    print("3. Data")
    n = 0
    for dirpath, _, files in os.walk(datadir):
        for f in files:
            s = os.path.join(dirpath, f)
            rel = os.path.relpath(s, datadir)
            d = os.path.join(RES, "data", rel)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(s, d)
            n += 1
    say(n, "data files")

# ---------------------------------------------------------------- 4. config keys
print("4. Config keys")
cfg = os.path.join(JAVA, "net/dabicco/witherstormmod/config/DabyWSClientConfig.java")
s = open(cfg).read()

FIELDS = """   public static boolean stormBackdrop = true;
   public static boolean stormBackdropQuad = false;
   public static double stormBackdropStrength = 1.0;
   public static double stormBackdropSize = 6.0;
   public static double stormBackdropPulse = 1.0;
   public static boolean stormBackdropGrow = true;
   public static boolean stormBackdropBlack = true;
   public static double stormBackdropBlackStrength = 1.0;
   public static boolean stormBackdropPhase4 = true;
   public static double stormBackdropPhase4Strength = 1.0;
   public static boolean stormBackdropTurquoise = true;
   public static boolean stormBackdropPurple = true;
   public static boolean stormBackdropPink = true;
   public static boolean stormBackdropEmber = false;
   public static double stormBackdropEmberStrength = 0.6;
   public static boolean storyModeClouds = true;
   public static double storyModeCloudStrength = 1.0;
   public static boolean storyModeCloudFade = true;
   public static double storyModeCloudFadeAmount = 0.75;
   public static boolean storyModeSky = true;
   public static double storyModeSkyStrength = 0.85;
   public static double storyModeFogStrength = 0.15;
   public static boolean storyModeLighting = true;
   public static double storyModeLightingStrength = 0.7;
   public static boolean turquoiseTeeth = true;
   public static double turquoiseTeethIntensity = 1.6;
"""
anchor = "   public static boolean cataclysmHalos = true;"
if "public static boolean stormBackdrop" not in s:
    assert anchor in s, "field anchor missing"
    s = s.replace(anchor, FIELDS + anchor, 1)
    say("24 fields inserted")
else:
    say("fields already present")

KEYS = '''      key("stormBackdrop", "The gradient sky that hangs behind the Wither Storm and follows it. Not a halo - it recolours the patch of sky the storm stands in front of.", 0.0, 1.0, true, () -> stormBackdrop ? 1.0 : 0.0, (v) -> stormBackdrop = v >= 0.5);
      key("stormBackdropStrength", "Overall opacity of the backdrop.", 0.0, 2.0, false, () -> stormBackdropStrength, (v) -> stormBackdropStrength = v);
      key("stormBackdropSize", "Backdrop size as a multiple of the storm's body radius.", 2.0, 14.0, false, () -> stormBackdropSize, (v) -> stormBackdropSize = v);
      key("stormBackdropPulse", "How fast the backdrop breathes.", 0.0, 5.0, false, () -> stormBackdropPulse, (v) -> stormBackdropPulse = v);
      key("stormBackdropGrow", "Backdrop keeps growing past phase 5.5 as the storm grows.", 0.0, 1.0, true, () -> stormBackdropGrow ? 1.0 : 0.0, (v) -> stormBackdropGrow = v >= 0.5);
      key("stormBackdropBlack", "The black blur in the centre of the backdrop.", 0.0, 1.0, true, () -> stormBackdropBlack ? 1.0 : 0.0, (v) -> stormBackdropBlack = v >= 0.5);
      key("stormBackdropBlackStrength", "How dark the central blur gets.", 0.0, 2.0, false, () -> stormBackdropBlackStrength, (v) -> stormBackdropBlackStrength = v);
      key("stormBackdropPhase4", "Phase 4: the blue glow behind the storm.", 0.0, 1.0, true, () -> stormBackdropPhase4 ? 1.0 : 0.0, (v) -> stormBackdropPhase4 = v >= 0.5);
      key("stormBackdropPhase4Strength", "Brightness of the phase 4 blue glow.", 0.0, 2.0, false, () -> stormBackdropPhase4Strength, (v) -> stormBackdropPhase4Strength = v);
      key("stormBackdropTurquoise", "Phase 4.5 - 5.1: the dark turquoise haze.", 0.0, 1.0, true, () -> stormBackdropTurquoise ? 1.0 : 0.0, (v) -> stormBackdropTurquoise = v >= 0.5);
      key("stormBackdropPurple", "Phase 5.1+: the purple sky behind the storm.", 0.0, 1.0, true, () -> stormBackdropPurple ? 1.0 : 0.0, (v) -> stormBackdropPurple = v >= 0.5);
      key("stormBackdropPink", "Phase 5.5+: magenta/pink wrapping around the purple.", 0.0, 1.0, true, () -> stormBackdropPink ? 1.0 : 0.0, (v) -> stormBackdropPink = v >= 0.5);
      key("stormBackdropEmber", "Adds the orange ember tint from the sunset shots.", 0.0, 1.0, true, () -> stormBackdropEmber ? 1.0 : 0.0, (v) -> stormBackdropEmber = v >= 0.5);
      key("stormBackdropEmberStrength", "Strength of the ember tint.", 0.0, 2.0, false, () -> stormBackdropEmberStrength, (v) -> stormBackdropEmberStrength = v);
      key("storyModeClouds", "Flat Story Mode clouds: one solid colour per cloud instead of vanilla's per-face shading, tinted by time of day.", 0.0, 1.0, true, () -> storyModeClouds ? 1.0 : 0.0, (v) -> storyModeClouds = v >= 0.5);
      key("storyModeCloudStrength", "How far the clouds are pushed toward the Story Mode palette.", 0.0, 1.0, false, () -> storyModeCloudStrength, (v) -> storyModeCloudStrength = v);
      key("storyModeCloudFade", "Clouds turn transparent when the storm's black backdrop is behind them.", 0.0, 1.0, true, () -> storyModeCloudFade ? 1.0 : 0.0, (v) -> storyModeCloudFade = v >= 0.5);
      key("storyModeCloudFadeAmount", "How far the clouds fade out near the storm.", 0.0, 1.0, false, () -> storyModeCloudFadeAmount, (v) -> storyModeCloudFadeAmount = v);
      key("storyModeSky", "The lavender Story Mode sky and fog, shifting through day, dusk, night and dawn.", 0.0, 1.0, true, () -> storyModeSky ? 1.0 : 0.0, (v) -> storyModeSky = v >= 0.5);
      key("storyModeSkyStrength", "How far the SKY DOME is pushed toward the Story Mode palette.", 0.0, 1.0, false, () -> storyModeSkyStrength, (v) -> storyModeSkyStrength = v);
      key("storyModeFogStrength", "How far distance FOG is tinted. Keep this low: fog colour is what far terrain fades into, so high values wash the world flat.", 0.0, 1.0, false, () -> storyModeFogStrength, (v) -> storyModeFogStrength = v);
      key("storyModeLighting", "Tints world lighting with the Story Mode palette so shadows shift colour through the day.", 0.0, 1.0, true, () -> storyModeLighting ? 1.0 : 0.0, (v) -> storyModeLighting = v >= 0.5);
      key("storyModeLightingStrength", "Strength of the coloured lighting tint.", 0.0, 1.0, false, () -> storyModeLightingStrength, (v) -> storyModeLightingStrength = v);
      key("turquoiseTeeth", "Turquoise emissive glow on the Wither Storm's teeth.", 0.0, 1.0, true, () -> turquoiseTeeth ? 1.0 : 0.0, (v) -> turquoiseTeeth = v >= 0.5);
      key("turquoiseTeethIntensity", "Brightness of the turquoise teeth.", 0.0, 4.0, false, () -> turquoiseTeethIntensity, (v) -> turquoiseTeethIntensity = v);
'''
kanchor = '      key("cataclysmHalos"'
if '"stormBackdrop"' not in s:
    assert kanchor in s, "key anchor missing"
    s = s.replace(kanchor, KEYS + kanchor, 1)
    say("24 key() registrations inserted")
else:
    say("keys already present")
open(cfg, "w").write(s)

# ---------------------------------------------------------------- 5. GUI rows
print("5. GUI rows")
gui = os.path.join(JAVA, "net/dabicco/witherstormmod/client/gui/WitherStormConfigScreen.java")
g = open(gui).read()
ROWS = '''      this.header("Storm Backdrop (the sky behind the storm)");
      this.clientRow("stormBackdrop", "Storm Backdrop", (BooleanSupplier)null);
      this.clientRow("stormBackdropStrength", "Backdrop Opacity", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropSize", "Backdrop Size", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropPulse", "Backdrop Breathing", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropGrow", "Grow With Storm", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropBlack", "Central Black Blur", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropBlackStrength", "Black Blur Strength", () -> !DabyWSClientConfig.stormBackdrop || !DabyWSClientConfig.stormBackdropBlack);
      this.clientRow("stormBackdropPhase4", "Phase 4 Blue Glow", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropPhase4Strength", "Phase 4 Brightness", () -> !DabyWSClientConfig.stormBackdrop || !DabyWSClientConfig.stormBackdropPhase4);
      this.clientRow("stormBackdropTurquoise", "Phase 4.5 Turquoise", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropPurple", "Phase 5.1 Purple", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropPink", "Phase 5.5 Purple + Pink", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropEmber", "Ember Tint", () -> !DabyWSClientConfig.stormBackdrop);
      this.clientRow("stormBackdropEmberStrength", "Ember Strength", () -> !DabyWSClientConfig.stormBackdrop || !DabyWSClientConfig.stormBackdropEmber);
      this.header("Story Mode Clouds");
      this.clientRow("storyModeClouds", "Flat Story Mode Clouds", (BooleanSupplier)null);
      this.clientRow("storyModeCloudStrength", "Cloud Colour Strength", () -> !DabyWSClientConfig.storyModeClouds);
      this.clientRow("storyModeCloudFade", "Fade Near Storm", (BooleanSupplier)null);
      this.clientRow("storyModeCloudFadeAmount", "Fade Amount", () -> !DabyWSClientConfig.storyModeCloudFade);
      this.header("Story Mode Sky & Lighting");
      this.clientRow("storyModeSky", "Story Mode Sky & Fog", (BooleanSupplier)null);
      this.clientRow("storyModeSkyStrength", "Sky Strength", () -> !DabyWSClientConfig.storyModeSky);
      this.clientRow("storyModeFogStrength", "Fog Tint (keep low)", () -> !DabyWSClientConfig.storyModeSky);
      this.clientRow("storyModeLighting", "Coloured Lighting", (BooleanSupplier)null);
      this.clientRow("storyModeLightingStrength", "Lighting Strength", () -> !DabyWSClientConfig.storyModeLighting);
      this.header("Turquoise Teeth");
      this.clientRow("turquoiseTeeth", "Turquoise Teeth Glow", (BooleanSupplier)null);
      this.clientRow("turquoiseTeethIntensity", "Teeth Brightness", () -> !DabyWSClientConfig.turquoiseTeeth);
'''
ganchor = '      this.header("Cataclysm Halos (Phase 5.8+)");'
if '"stormBackdrop"' not in g:
    assert ganchor in g, "gui anchor missing"
    g = g.replace(ganchor, ROWS + ganchor, 1)
    open(gui, "w").write(g)
    say("27 rows + 4 headers inserted")
else:
    say("rows already present")

# ---------------------------------------------------------------- 6. register renderer
print("6. Renderer registration")
cl = os.path.join(JAVA, "net/dabicco/witherstormmod/DabyWitherStormModClient.java")
c = open(cl).read()
reg_anchor = "      LevelRenderEvents.COLLECT_SUBMITS.register(StormPresenceFX::submit);"
new_reg = "      LevelRenderEvents.COLLECT_SUBMITS.register(StormBackdrop::submit);\n" + reg_anchor
if "StormBackdrop::submit" not in c:
    assert reg_anchor in c, "registration anchor missing"
    c = c.replace(reg_anchor, new_reg, 1)
    imp_anchor = "import net.dabicco.witherstormmod.client.ClientConfigCache;"
    assert imp_anchor in c, "import anchor missing"
    c = c.replace(imp_anchor,
                  "import net.dabicco.witherstormmod.client.StormBackdrop;\n" + imp_anchor, 1)
    open(cl, "w").write(c)
    say("StormBackdrop::submit registered before StormPresenceFX (+import)")
else:
    say("already registered")

# ---------------------------------------------------------------- 7. Story Mode sky into fog
print("7. Story Mode sky tint -> FogRendererMixin")
fog = os.path.join(JAVA, "net/dabicco/witherstormmod/mixin/FogRendererMixin.java")
fs = open(fog).read()
if "StoryModeSkyTint" not in fs:
    # insert the baseline Story Mode tint BEFORE the storm darkening, so the
    # storm still wins when it is close.
    fanchor = "      StormSkyDarken.update(camera.position(), partialTick);"
    inject = '''      float stormSky = net.dabicco.witherstormmod.client.StormSkyDome.strength();
      if (stormSky > 0.0F) {
         float[] ssc = new float[3];
         net.dabicco.witherstormmod.client.StormSkyDome.skyColor(ssc);
         float fogAmt = stormSky * 0.75F;
         color.x = color.x * (1.0F - fogAmt) + ssc[0] * fogAmt;
         color.y = color.y * (1.0F - fogAmt) + ssc[1] * fogAmt;
         color.z = color.z * (1.0F - fogAmt) + ssc[2] * fogAmt;
      }

      float smStrength = StoryModeSkyTint.fogStrength();
      if (smStrength > 0.0F && level != null) {
         float[] sm = new float[3];
         StoryModeSkyTint.skyColor(level.getOverworldClockTime(), sm);
         color.x = color.x * (1.0F - smStrength) + sm[0] * smStrength;
         color.y = color.y * (1.0F - smStrength) + sm[1] * smStrength;
         color.z = color.z * (1.0F - smStrength) + sm[2] * smStrength;
      }

'''
    assert fanchor in fs, "fog anchor missing"
    fs = fs.replace(fanchor, inject + fanchor, 1)
    fs = fs.replace("import net.dabicco.witherstormmod.client.StormFog;",
                    "import net.dabicco.witherstormmod.client.StormFog;\nimport net.dabicco.witherstormmod.client.StoryModeSkyTint;", 1)
    open(fog, "w").write(fs)
    say("Story Mode sky blended into fog colour")
else:
    say("already patched")

# ---------------------------------------------------------------- 8. MCSM structures
print("8. MCSM structures")
SCHEM_SRC = "/home/user/dabby-patch/schematics"
if os.path.isdir(SCHEM_SRC):
    dst_root = os.path.join(RES, "assets/dabywitherstormmod/schematics")
    n = 0
    for dirpath, _, files in os.walk(SCHEM_SRC):
        for f in files:
            if not f.endswith(".schematic"):
                continue
            s_ = os.path.join(dirpath, f)
            rel = os.path.relpath(s_, SCHEM_SRC)
            d_ = os.path.join(dst_root, rel)
            os.makedirs(os.path.dirname(d_), exist_ok=True)
            shutil.copy2(s_, d_)
            n += 1
    say(n, "schematics copied into assets")
else:
    say("!! schematics dir missing, skipped")

# register /mcsm + the placement tick
main = os.path.join(JAVA, "net/dabicco/witherstormmod/DabyWitherStormMod.java")
m = open(main).read()
if "McsmCommand" not in m:
    cmd_anchor = "      SigeonNetwork.register();"
    inject = ("      CommandRegistrationCallback.EVENT.register("
              "(CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> "
              "net.dabicco.witherstormmod.structures.McsmCommand.register(dispatcher));\n"
              "      ServerTickEvents.END_LEVEL_TICK.register("
              "(ServerTickEvents.EndLevelTick)(level) -> "
              "net.dabicco.witherstormmod.structures.McsmWorldgen.tick(level));\n"
              "      ServerTickEvents.END_LEVEL_TICK.register("
              "(ServerTickEvents.EndLevelTick)(level) -> "
              "net.dabicco.witherstormmod.structures.McsmWorldgen.autoGenerate(level));\n")
    assert cmd_anchor in m, "command anchor missing"
    m = m.replace(cmd_anchor, inject + cmd_anchor, 1)
    open(main, "w").write(m)
    say("/mcsm command + placement tick registered")
else:
    say("already registered")

# ---------------------------------------------------------------- 9. mixin registration
print("9. Mixin registration")
mj = os.path.join(RES, "dabywitherstormmod.mixins.json")
import json as _json
mx = _json.load(open(mj))
added = []
for name in ("StoryModeSkyDomeMixin", "StoryModeLightmapMixin", "StormSkyGradientMixin"):
    if name not in mx.get("client", []):
        mx.setdefault("client", []).append(name)
        added.append(name)
if added:
    mx["client"] = sorted(set(mx["client"]))
    open(mj, "w").write(_json.dumps(mx, indent=2))
    say("registered:", ", ".join(added))
else:
    say("already registered")

# ---------------------------------------------------------------- 10. OG textures on by default
print("10. OG textures by default")
cfgp = os.path.join(JAVA, "net/dabicco/witherstormmod/config/DabyWSClientConfig.java")
cs = open(cfgp).read()
old = "   public static double stormSkin = (double)0.0F;"
new = "   public static double stormSkin = (double)1.0F;"
if old in cs:
    cs = cs.replace(old, new, 1)
    open(cfgp, "w").write(cs)
    say("stormSkin default 0 (Classic) -> 1 (OG Story Mode)")
elif new in cs:
    say("already OG by default")
else:
    say("!! stormSkin default not found")

# ---------------------------------------------------------------- 11. guidebook
print("11. Story Mode guidebook")
mainj = os.path.join(JAVA, "net/dabicco/witherstormmod/DabyWitherStormMod.java")
ms = open(mainj).read()
if "McsmGuidebook.register()" not in ms:
    anchor = "SigeonNetwork.register();"
    ms = ms.replace(anchor,
        "net.dabicco.witherstormmod.structures.McsmGuidebook.register();\n      " + anchor, 1)
    open(mainj, "w").write(ms)
    say("guidebook granted on first join")
else:
    say("already registered")

# ------------------------------------------------------- 12. emissive teeth pass
print("12. Turquoise teeth emissive pass")
wsr = os.path.join(JAVA, "net/dabicco/witherstormmod/entity/renderer/WitherStormRenderer.java")
rs = open(wsr).read()
if "dabyws$teethGlow" not in rs:
    # The body is only ever drawn with bodyCutout, so the baked _e texture was
    # never sampled. Add a second submit over the SAME model using eyes(),
    # which is full-bright and additive.
    anchor = "         super.submit(state, poseStack, submitNodeCollector, camera);"
    inject = '''         super.submit(state, poseStack, submitNodeCollector, camera);
         // dabyws$teethGlow: emissive overlay so the turquoise teeth actually light up
         if (!this.previewShadowPass && net.dabicco.witherstormmod.config.DabyWSClientConfig.turquoiseTeeth) {
            net.minecraft.resources.Identifier glow =
               net.dabicco.witherstormmod.client.StormSkins.teethGlow(state.phase);
            if (glow != null) {
               submitNodeCollector.submitModel(
                  this.previewShadowPass ? this.hunchbackShadowModel : this.hunchbackModel,
                  state, poseStack,
                  net.dabicco.witherstormmod.client.FoglessRenderTypes.eyes(glow),
                  15728880, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
            }
         }'''
    assert anchor in rs, "super.submit anchor missing"
    rs = rs.replace(anchor, inject, 1)
    open(wsr, "w").write(rs)
    say("emissive teeth pass added to WitherStormRenderer")
else:
    say("already present")

# StormSkins.teethGlow(phase) -> phase 5 uses the WHITE variant
sk = os.path.join(JAVA, "net/dabicco/witherstormmod/client/StormSkins.java")
ss = open(sk).read()
if "teethGlow" not in ss:
    helper = '''
   /** Emissive teeth texture. Phase 5 keeps them white; every other phase is turquoise. */
   public static Identifier teethGlow(double phase) {
      boolean og = DabyWSClientConfig.stormSkin >= 0.5;
      if (phase >= 5.0 && phase < 6.0) {
         return Identifier.fromNamespaceAndPath("dabywitherstormmod",
            og ? "textures/entity/wither_storm_og_p5_e.png" : "textures/entity/wither_storm_p5_e.png");
      }
      return Identifier.fromNamespaceAndPath("dabywitherstormmod",
         og ? "textures/entity/wither_storm_og_e.png" : "textures/entity/wither_storm_e.png");
   }
'''
    idx = ss.rindex("}")
    ss = ss[:idx] + helper + "}\n"
    if "import net.dabicco.witherstormmod.config.DabyWSClientConfig;" not in ss:
        ss = ss.replace("import net.minecraft.resources.Identifier;",
                        "import net.dabicco.witherstormmod.config.DabyWSClientConfig;\nimport net.minecraft.resources.Identifier;", 1)
    open(sk, "w").write(ss)
    say("StormSkins.teethGlow() added")
else:
    say("teethGlow already present")

# --------------------------------------------- 13. presets: MCSM OG + Netflix
print("13. Visual presets")
cfg = os.path.join(JAVA, "net/dabicco/witherstormmod/config/DabyWSClientConfig.java")
cs = open(cfg).read()
if "PRESET_NETFLIX" not in cs:
    # 13a. add the label
    cs = cs.replace(
        'public static final String[] PRESET_LABELS = new String[]{"Custom", "MCSM", "Legacy Java", "Cinematic"};',
        'public static final String[] PRESET_LABELS = new String[]{"Custom", "MCSM OG Visuals", "Legacy Java", "Cinematic", "Netflix"};', 1)

    # 13b. declare the field next to the others
    cs = cs.replace("   private static final Map<String, Double> PRESET_LEGACY;",
                    "   private static final Map<String, Double> PRESET_LEGACY;\n   private static final Map<String, Double> PRESET_NETFLIX;", 1)

    # 13c. route case 4
    cs = cs.replace("         case 3 -> var10000 = PRESET_CINEMATIC;",
                    "         case 3 -> var10000 = PRESET_CINEMATIC;\n         case 4 -> var10000 = PRESET_NETFLIX;", 1)

    # 13d. STORY MODE additions for the MCSM preset -- the OG look, everything on
    story = ('Map.entry("stormSkin", 1.0), Map.entry("storyModeSky", 1.0), '
             'Map.entry("storyModeSkyStrength", 0.85), Map.entry("storyModeFogStrength", 0.15), '
             'Map.entry("storyModeClouds", 1.0), Map.entry("storyModeCloudStrength", 1.0), '
             'Map.entry("storyModeLighting", 1.0), Map.entry("storyModeLightingStrength", 0.7), '
             'Map.entry("turquoiseTeeth", 1.0), Map.entry("turquoiseTeethIntensity", 1.6), '
             'Map.entry("stormBackdrop", 1.0), Map.entry("stormBackdropStrength", 1.0)')
    cs = cs.replace('Map.entry("stormSkin", (double)1.0F), Map.entry("stormStars", (double)1.0F), Map.entry("stormCloudDeck", (double)1.0F), Map.entry("atmospherePulse", (double)1.0F), Map.entry("cataclysmHalos", (double)1.0F), Map.entry("blackGlare", (double)1.0F), Map.entry("glareEjecta", (double)1.0F), Map.entry("debrisSize", 1.8), Map.entry("phaseFogPalettes", (double)1.0F));',
                    'Map.entry("stormStars", (double)1.0F), Map.entry("stormCloudDeck", (double)1.0F), Map.entry("atmospherePulse", (double)1.0F), Map.entry("cataclysmHalos", (double)1.0F), Map.entry("blackGlare", (double)1.0F), Map.entry("glareEjecta", (double)1.0F), Map.entry("debrisSize", 1.8), Map.entry("phaseFogPalettes", (double)1.0F), ' + story + ');', 1)

    # 13e. the Netflix preset: cooler, cleaner, less bloom than MCSM
    netflix = ('      PRESET_NETFLIX = Map.ofEntries('
        'Map.entry("reverseShading", 1.0), Map.entry("bloomStrength", 1.35), '
        'Map.entry("beamOpacity", 0.7), Map.entry("beamColorR", 0.42), '
        'Map.entry("beamColorG", 0.30), Map.entry("beamColorB", 0.98), '
        'Map.entry("stormSkin", 1.0), Map.entry("stormStars", 1.0), '
        'Map.entry("stormCloudDeck", 1.0), Map.entry("atmospherePulse", 1.0), '
        'Map.entry("pulseStrength", 0.85), Map.entry("cataclysmHalos", 1.0), '
        'Map.entry("haloStrength", 0.9), Map.entry("blackGlare", 1.0), '
        'Map.entry("glareEjecta", 1.0), Map.entry("ejectaRate", 1.1), '
        'Map.entry("debrisSize", 1.5), Map.entry("phaseFogPalettes", 1.0), '
        'Map.entry("storyModeSky", 1.0), Map.entry("storyModeSkyStrength", 0.70), '
        'Map.entry("storyModeFogStrength", 0.12), Map.entry("storyModeClouds", 1.0), '
        'Map.entry("storyModeLighting", 1.0), Map.entry("storyModeLightingStrength", 0.55), '
        'Map.entry("turquoiseTeeth", 1.0), Map.entry("turquoiseTeethIntensity", 1.3), '
        'Map.entry("stormBackdrop", 1.0), Map.entry("stormBackdropStrength", 0.85));\n')
    cs = cs.replace("      GSON = (new GsonBuilder()).setPrettyPrinting().create();",
                    netflix + "      GSON = (new GsonBuilder()).setPrettyPrinting().create();", 1)
    open(cfg, "w").write(cs)
    say("MCSM OG Visuals extended + Netflix preset added")
else:
    say("presets already present")

# ---------------------------------------------------- 14. config expansion
print("14. Expanded config")
import subprocess as _sp
_r = _sp.run(["python3", os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_config_expansion.py")],
             capture_output=True, text=True)
print(_r.stdout.rstrip() or _r.stderr.rstrip())

# ------------------------------------------- 15. authentic MCSM sky gradients
print("15. Authentic Story Mode sky textures")
skysrc = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "skytex")
skydst = os.path.join(RES, "assets/dabywitherstormmod/textures/environment")
if os.path.isdir(skysrc):
    os.makedirs(skydst, exist_ok=True)
    n = 0
    for f in ("day.png", "night.png", "sunset.png"):
        src = os.path.join(skysrc, f)
        if os.path.exists(src):
            shutil.copy2(src, os.path.join(skydst, "storymode_sky_" + f))
            n += 1
    say(f"{n} authentic sky gradients installed")
else:
    say("skytex/ not found, skipping")

# ------------------------------------- 16. Tainted's Accurate Models + death FX
print("16. Tainted's Accurate Models, panorama, death sequence")
import subprocess
_here = os.path.dirname(os.path.abspath(__file__))
subprocess.run([sys.executable, os.path.join(_here, "gen_taw_wiring.py"), ROOT], check=True)

# ------------------------------------- 17. phase-6 flashbang + MCSM autogen
print("17. Purple flashbang + MCSM auto-generation")
subprocess.run([sys.executable, os.path.join(_here, "gen_phase_fx.py"), ROOT], check=True)

print("\nDone.")
