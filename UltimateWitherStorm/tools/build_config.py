#!/usr/bin/env python3
"""Generate MAX-SPECTACLE config files for Cracker's Wither Storm Mod 4.2.1,
plus an expanded "Ultimate" config layer for this pack's own toggles.

Defaults follow the user's choice: everything cranked to Max Spectacle, with
every other preset still present in the file so it can be switched in-game.
"""
import json, os, re

OUT = "/home/user/UltimateWitherStorm/config"
os.makedirs(OUT, exist_ok=True)
OPTS = json.load(open("/home/user/UltimateWitherStorm/tools/_cwsm_options.json"))

# --------------------------------------------------------------- heuristics
MAXOUT_HI = re.compile(
    r"size|scale|radius|amount|count|max|speed|rate|distance|health|damage|"
    r"explosion|power|strength|multiplier|modifier|limit|range|density|quality|detail",
    re.I)
KEEP_LOW = re.compile(r"interval|delay|cooldown|time|seconds|minutes|chance|threshold", re.I)
DISABLE = re.compile(r"aprilFools|lowRes|lodEnabled|useLowRes|ultraLow|disable", re.I)


def pick(key, meta):
    """Choose the Max-Spectacle value for one option."""
    rng = meta.get("range")
    title = meta["title"]
    if rng:
        lo, hi = rng
        f = "." in lo or "." in hi
        lo_v, hi_v = (float(lo), float(hi)) if f else (int(lo), int(hi))
        if DISABLE.search(key):
            return lo_v
        if KEEP_LOW.search(key):
            # fast intervals = more action, so bias LOW for time-based values
            v = lo_v + (hi_v - lo_v) * 0.12
        elif MAXOUT_HI.search(key):
            v = lo_v + (hi_v - lo_v) * 0.92
        else:
            v = lo_v + (hi_v - lo_v) * 0.70
        return round(v, 3) if f else int(round(v))
    # boolean-ish
    if DISABLE.search(key):
        return False
    return True


client, server, common = {}, {}, {}
CLIENT_RE = re.compile(
    r"render|gui|screen|shader|particle|camera|shake|fog|sound|music|volume|lod|"
    r"model|texture|overlay|blind|effect|hud|fps|async|buffer|distance|debris|"
    r"panorama|halo|sky|glow|bloom|april", re.I)

for k, meta in sorted(OPTS.items()):
    v = pick(k, meta)
    (client if CLIENT_RE.search(k) else server)[k] = v

# hard overrides that define the "Max Spectacle / mass destruction" feel
server.update({
    "autoSpawnWitherStorm": True,
    "witherStormCanDestroyBlocks": True,
    "blockClustersDropItems": False,      # avoids world-lag while still shredding builds
    "lowerBlockResistance": True,
    "canPickupMobClusters": True,
    "convertFallingBlocks": True,
    "clustersRemoveItems": True,
    "tractorBeamsRemoveFluids": True,
    "amuletOverride": True,
})
client.update({
    "renderDebrisCloud": True,
    "renderDebrisRings": True,
    "renderTractorBeams": True,
    "renderTractorBeamOverlay": True,
    "blockClusterRendering": True,
    "renderDistantDebris": True,
    "hideDebrisRingsUntilSplit": False,
    "renderDebrisTwoDimensional": False,
    "blindingEffects": True,
    "aprilFools": False,
    "asyncBufferBuilders": True,
})


def toml(section, d, header):
    out = [f"# {header}", ""]
    out.append(f"[{section}]")
    for k, v in sorted(d.items()):
        meta = OPTS.get(k)
        if meta:
            desc = meta["desc"].strip()
            if desc:
                for line in re.findall(r".{1,96}(?:\s|$)", desc):
                    if line.strip():
                        out.append(f"\t# {line.strip()}")
            if meta.get("range"):
                out.append(f"\t# Range: {meta['range'][0]} ~ {meta['range'][1]}")
        if isinstance(v, bool):
            out.append(f"\t{k} = {str(v).lower()}")
        elif isinstance(v, str):
            out.append(f'\t{k} = "{v}"')
        else:
            out.append(f"\t{k} = {v}")
        out.append("")
    return "\n".join(out)


open(f"{OUT}/witherstormmod-client.toml", "w").write(
    toml("client", client, "Cracker's Wither Storm Mod - CLIENT  (Ultimate MCSM :: MAX SPECTACLE)"))
open(f"{OUT}/witherstormmod-server.toml", "w").write(
    toml("server", server, "Cracker's Wither Storm Mod - SERVER  (Ultimate MCSM :: MAX SPECTACLE)"))

# ------------------------------------------------- the pack's own mega-config
def opt(id_, title, default, kind="bool", rng=None, desc="", tags=()):
    return {"id": id_, "title": title, "default": default, "type": kind,
            "range": rng, "description": desc, "tags": list(tags)}


CATS = {}


def cat(name, icon, *entries):
    CATS[name] = {"icon": icon, "options": list(entries)}


cat("Halos & Auras", "\u2728",
    opt("halo.enabled", "Enable Procedural Halos", True, desc="Master switch for all phase halos."),
    opt("halo.phase4.white", "Phase 4 White Rim Glow", True, desc="Omissive white glow hugging the storm's sides."),
    opt("halo.phase4.intensity", "Phase 4 Intensity", 1.0, "float", (0.0, 4.0)),
    opt("halo.phase5.blackblur", "Phase 5 Black Blur", True, desc="Dark bruised blur around the silhouette."),
    opt("halo.phase5.purple", "Phase 5 Purple Bleed", True),
    opt("halo.phase5.intensity", "Phase 5 Intensity", 1.15, "float", (0.0, 4.0)),
    opt("halo.phase5_1.blue", "Phase 5.1 Blue Aura", True, desc="The blue aura that fades in first."),
    opt("halo.phase5_1.intensity", "Phase 5.1 Blue Intensity", 1.0, "float", (0.0, 4.0)),
    opt("halo.phase5_5.purpleWrap", "Phase 5.5 Purple Wrap", True,
        desc="Purple aura wrapped AROUND the blue aura, growing as the storm grows."),
    opt("halo.phase5_5.intensity", "Phase 5.5 Intensity", 1.35, "float", (0.0, 4.0)),
    opt("halo.growWithSize", "Halo Scales With Storm Size", True),
    opt("halo.pulseSpeed", "Halo Pulse Speed", 1.0, "float", (0.0, 5.0)),
    opt("halo.rotateSpeed", "Halo Rotation Speed", 0.35, "float", (-3.0, 3.0)),
    opt("halo.boil", "Halo Turbulence / Boil", 0.45, "float", (0.0, 1.0)),
    opt("halo.bloom", "Additive Bloom Strength", 1.2, "float", (0.0, 3.0)),
    opt("halo.layers", "Max Halo Layers", 5, "int", (1, 8)),
    )

cat("Teeth & Emissives", "\U0001F9B7",
    opt("teeth.turquoise", "Turquoise Glowing Teeth", True),
    opt("teeth.color", "Teeth Glow Colour", "#40F0E0", "color"),
    opt("teeth.intensity", "Teeth Glow Intensity", 1.6, "float", (0.0, 5.0)),
    opt("teeth.pulse", "Teeth Pulse With Roar", True),
    opt("eyes.color", "Eye Glow Colour", "#FF3CEB", "color"),
    opt("eyes.intensity", "Eye Glow Intensity", 1.4, "float", (0.0, 5.0)),
    opt("emissive.decals", "Emissive Body Decals", True),
    opt("emissive.tentacleTips", "Glowing Tentacle Tips", True),
    )

cat("Dynamic Sky", "\U0001F30C",
    opt("sky.enabled", "Dynamic Storm Skybox", True),
    opt("sky.followStorm", "Skybox Follows The Storm", True,
        desc="The skybox is anchored behind the storm and moves when it moves."),
    opt("sky.colorTop", "Sky Zenith Colour", "#1A0630", "color"),
    opt("sky.colorMid", "Sky Mid Colour", "#6822A8", "color"),
    opt("sky.colorLow", "Sky Low Colour", "#CE4EA8", "color"),
    opt("sky.colorHorizon", "Horizon / Bottom Pink", "#FF9696", "color"),
    opt("sky.rotationSpeed", "Sky Rotation Speed", 0.35, "float", (-5.0, 5.0)),
    opt("sky.fogTint", "Tint Fog To Match Sky", True),
    opt("sky.stormProximityBlend", "Blend Sky By Storm Distance", True),
    opt("sky.lightning", "Purple Lightning", True),
    opt("sky.godRays", "Volumetric God Rays", True),
    opt("sky.godRayStrength", "God Ray Strength", 1.3, "float", (0.0, 4.0)),
    )

cat("Shaders & Lighting", "\U0001F4A1",
    opt("shader.groundShadows", "Ground Shadows", True),
    opt("shader.shadowsMoveWithSun", "Shadows Cast & Move With Sun", True),
    opt("shader.shadowSoftness", "Shadow Softness", 0.6, "float", (0.0, 1.0)),
    opt("shader.sunGlow", "Sun Glow / Bloom", True),
    opt("shader.sunGlowStrength", "Sun Glow Strength", 1.4, "float", (0.0, 4.0)),
    opt("shader.stormShadow", "Storm Casts A Shadow On The World", True),
    opt("shader.colorGrading", "Story Mode Colour Grading", True),
    opt("shader.vignette", "Cinematic Vignette", True),
    opt("shader.chromatic", "Chromatic Aberration Near Storm", True),
    opt("shader.screenShake", "Screen Shake", True),
    opt("shader.screenShakeStrength", "Screen Shake Strength", 1.25, "float", (0.0, 4.0)),
    opt("clouds.storyMode", "Story Mode Clouds", True),
    opt("clouds.height", "Cloud Height", 192, "int", (64, 320)),
    opt("clouds.speed", "Cloud Speed", 1.4, "float", (0.0, 6.0)),
    )

cat("Tentacle Interaction", "\U0001F419",
    opt("tentacle.grabPlayers", "Tentacles Can Grab Players", True),
    opt("tentacle.grabMobs", "Tentacles Can Grab Mobs", True),
    opt("tentacle.throwPlayers", "Tentacles Can Throw You", True),
    opt("tentacle.throwPower", "Throw Power", 2.4, "float", (0.0, 8.0)),
    opt("tentacle.touchDamage", "Touch Damage", 6.0, "float", (0.0, 40.0)),
    opt("tentacle.grabRange", "Grab Range (blocks)", 42, "int", (4, 256)),
    opt("tentacle.grabDuration", "Grab Duration (ticks)", 70, "int", (5, 600)),
    opt("tentacle.slamBlocks", "Tentacles Slam Into Blocks", True),
    opt("tentacle.ripStructures", "Tentacles Rip Apart Structures", True),
    opt("tentacle.count", "Max Active Tentacles", 14, "int", (1, 64)),
    opt("tentacle.reachMultiplier", "Reach Multiplier", 1.8, "float", (0.1, 10.0)),
    opt("tentacle.escapeStruggle", "Struggle To Escape", True),
    )

cat("Mass Destruction", "\U0001F4A5",
    opt("destroy.enabled", "Mass Destruction", True),
    opt("destroy.ripHouses", "Rip Apart Houses", True),
    opt("destroy.demolishRadius", "Demolition Radius", 34, "int", (1, 256)),
    opt("destroy.slamForce", "Slam Force", 2.6, "float", (0.0, 10.0)),
    opt("destroy.suckItems", "Vacuum Up Items", True),
    opt("destroy.terrainDeformation", "Terrain Deformation", True),
    opt("destroy.debrisPhysics", "Flying Debris Physics", True),
    opt("destroy.debrisCount", "Debris Particle Count", 2400, "int", (0, 20000)),
    opt("destroy.fireOnImpact", "Fires On Impact", True),
    opt("destroy.craterDepth", "Crater Depth", 9, "int", (0, 64)),
    )

cat("Storm Growth & Size", "\U0001F300",
    opt("size.unlimited", "UNLIMITED Growth (no size cap)", True,
        desc="Removes the size ceiling entirely - the storm can keep growing forever."),
    opt("size.maxPhase", "Max Phase", 7.0, "float", (1.0, 12.0)),
    opt("size.scaleMultiplier", "Global Scale Multiplier", 1.0, "float", (0.05, 100.0)),
    opt("size.growthRate", "Growth Rate", 1.6, "float", (0.0, 20.0)),
    opt("size.hungerRate", "Consumption Rate", 1.8, "float", (0.0, 20.0)),
    opt("size.phaseThresholdScale", "Phase Threshold Scale", 1.0, "float", (0.01, 10.0)),
    opt("size.allowFractionalPhases", "Fractional Phases (5.1 / 5.5)", True),
    opt("size.renderDistanceBoost", "Force Render At Any Distance", True),
    )

cat("World & Structures", "\U0001F3D8",
    opt("world.mcsmStructures", "Generate MCSM Structures", True),
    opt("world.beaconTown", "Beacon Town", True),
    opt("world.orderTemple", "Order Of The Stone Temple", True),
    opt("world.bouldervale", "Bouldervale / Wilderness", True),
    opt("world.netherStation", "Nether Train Station", True),
    opt("world.structureRarity", "Structure Rarity", 4, "int", (1, 100)),
    opt("world.taintedBiome", "Tainted Biome Spread", True),
    opt("world.spawnGuidebook", "Give Guidebook On First Join", True),
    opt("world.storyModeTerrain", "Story Mode Terrain Palette", True),
    )

cat("Audio", "\U0001F50A",
    opt("audio.mcsmMusic", "MCSM Music Pack", True),
    opt("audio.stormRoars", "Storm Roars", True),
    opt("audio.roarVolume", "Roar Volume", 1.0, "float", (0.0, 2.0)),
    opt("audio.ambientDread", "Ambient Dread Layer", True),
    opt("audio.distanceRumble", "Low-Frequency Rumble", True),
    )

cat("Performance", "\u26A1",
    opt("perf.preset", "Preset", "MAX_SPECTACLE", "enum",
        None, "MAX_SPECTACLE / HIGH / BALANCED / LOW / POTATO"),
    opt("perf.haloResolution", "Halo Texture Resolution", 512, "int", (64, 2048)),
    opt("perf.skyResolution", "Sky Texture Resolution", 1024, "int", (128, 4096)),
    opt("perf.cullDistantDebris", "Cull Distant Debris", False),
    opt("perf.asyncBuild", "Async Buffer Building", True),
    opt("perf.maxParticles", "Max Particles", 12000, "int", (100, 100000)),
    )

total = sum(len(c["options"]) for c in CATS.values())
json.dump({
    "_comment": "Ultimate MCSM Wither Storm - master config. Defaults = MAX SPECTACLE.",
    "version": 1,
    "activePreset": "MAX_SPECTACLE",
    "presets": {
        "MAX_SPECTACLE": "Everything on, cranked. Built for cinematics.",
        "HIGH": "Nearly everything, minor culling.",
        "BALANCED": "All signature looks, tuned for mid-range PCs.",
        "LOW": "Cheap halos, reduced debris.",
        "POTATO": "Bare minimum.",
    },
    "categories": CATS,
}, open(f"{OUT}/ultimate_witherstorm.json", "w"), indent=2)

print(f"config: {len(client)} client + {len(server)} server CWSM options")
print(f"config: {total} Ultimate options across {len(CATS)} categories")
