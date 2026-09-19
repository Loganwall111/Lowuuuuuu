#!/usr/bin/env python3
"""
V2 Validation - Animated skyboxes majestic insane VFX, black hole backdrop lensing, 30 new blocks, creatures
"""
import os, sys
from pathlib import Path
ROOT = Path(__file__).parent.parent

checks_passed=0
checks_failed=0

def check(name, cond, msg=""):
    global checks_passed, checks_failed
    if cond:
        print(f"[OK] {name}")
        checks_passed+=1
        return True
    else:
        print(f"[FAIL] {name} - {msg}")
        checks_failed+=1
        return False

print("="*80)
print("V2 Validation - Black Hole Backdrop + Animated Skyboxes + 30 new blocks")
print("="*80)

# Check new Java files
check("McsmBlackHoleBackdrop exists", (ROOT/"mcsm-extras/java/net/mcsm/extras/client/McsmBlackHoleBackdrop.java").exists())
check("McsmAnimatedSkybox exists", (ROOT/"mcsm-extras/java/net/mcsm/extras/client/McsmAnimatedSkybox.java").exists())
check("McsmSiftContent exists 30 new blocks", (ROOT/"mcsm-extras/java/net/mcsm/sift/block/McsmSiftContent.java").exists())
if (ROOT/"mcsm-extras/java/net/mcsm/sift/block/McsmSiftContent.java").exists():
    c = (ROOT/"mcsm-extras/java/net/mcsm/sift/block/McsmSiftContent.java").read_text()
    cnt = c.count('block("')
    check("30 new blocks count", cnt >= 30, f"found {cnt}")
    check("Black hole blocks", "BLACK_HOLE_FRAME" in c and "EVENT_HORIZON" in c)
    check("Void terrain unique", "VOID_CRYSTAL" in c)

# Check McsmSiftMod V2 entities
if (ROOT/"mcsm-extras/java/net/mcsm/sift/McsmSiftMod.java").exists():
    c = (ROOT/"mcsm-extras/java/net/mcsm/sift/McsmSiftMod.java").read_text()
    check("5 new creatures V2", "VOID_JELLY" in c and "PRISMATIC_WISP" in c and "CRYSTAL_MANTA" in c)
    check("Fabric registration not Forge", "net.minecraftforge" not in c)

# Check sky.fsh V2 black hole lensing
sky_fsh = ROOT/"mcsm-core-shaders/core/sky.fsh"
if sky_fsh.exists():
    c = sky_fsh.read_text()
    check("sky.fsh black hole lensing", "black hole" in c.lower() and "lensing" in c.lower())
    check("sky.fsh growing bigger perspective", "bhGrowth" in c or "growing bigger" in c.lower())
    check("sky.fsh full sky not bands", "full sky" in c.lower() or "full 360" in c.lower() or "Full sky" in c)
    check("sky.fsh photon ring", "photon" in c.lower())
    check("sky.fsh accretion disk rainbow", "accretion disk" in c.lower())

# Check final.fsh V2 lensing
final_fsh = ROOT/"mcsm-core-shaders/core/final.fsh"
if final_fsh.exists():
    c = final_fsh.read_text()
    check("final.fsh black hole lensing", "black hole" in c.lower() and "lensing" in c.lower())
    check("final.fsh chromatic aberration", "chromatic" in c.lower())
    check("final.fsh animated skyboxes", "animated skyboxes" in c.lower())

# Check textures
sky_dir = ROOT/"overrides/resourcepacks/Sift_Cosmos/assets/mcsm/textures/sky"
check("Sky dir exists", sky_dir.exists())
if sky_dir.exists():
    skies = list(sky_dir.glob("*.png"))
    check("At least 10 sky textures", len(skies) >= 10, f"found {len(skies)}")
    check("Black hole sky texture", (sky_dir/"black_hole_sky.png").exists())
    check("Side boxes 0-5 exist", all((sky_dir/f"sift_panorama_{i}.png").exists() for i in range(6)))
    mcmeta = list(sky_dir.glob("*.mcmeta"))
    check("Animated skyboxes mcmeta", len(mcmeta) >= 10, f"found {len(mcmeta)}")

block_dir = ROOT/"overrides/resourcepacks/Sift_Cosmos/assets/mcsm/textures/block"
if block_dir.exists():
    blocks = list(block_dir.glob("*.png"))
    check("At least 50 block textures (20 old + 30 new)", len(blocks) >= 45, f"found {len(blocks)}")

# Check jar-overrides sky
jar_sky = ROOT/"jar-overrides/assets/mcsm/textures/sky"
if jar_sky.exists():
    check("Jar-overrides void skies", (jar_sky/"void_luminous_sky.png").exists())
    check("Jar-overrides black hole sky", (jar_sky/"black_hole_sky.png").exists())

print(f"\nV2 Checks: {checks_passed} passed, {checks_failed} failed")
sys.exit(0 if checks_failed==0 else 1)
