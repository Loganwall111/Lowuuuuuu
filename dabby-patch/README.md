# MCSM Atmosphere Patch

A **patch overlay**, not a copy of the mod. Run it against a fresh clone and it reproduces every change.

## Why a patch and not the full source tree

The mod's `src/` is **368 MB** (the `.bbmodel` files alone are 20 MB each). The workspace snapshot caps out around 128 MB, so a full checkout **cannot survive between turns** — that is exactly why the previous `/home/user/dabby/src` and the built jar disappeared. This directory is ~2 MB and always survives.

## Rebuild from scratch

```bash
git clone --depth 1 --branch arena/01a05ccd-lowuuuuuu \
    https://github.com/Loganwall111/Lowuuuuuu.git /var/tmp/build/dabsrc

python3 /home/user/dabby-patch/tools/apply_patch.py /var/tmp/build/dabsrc
python3 /home/user/dabby-patch/tools/daby_teeth_glow.py     # teeth emissive maps

cd /var/tmp/build/dabsrc
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./gradlew build --no-daemon
```

Build on **real disk, not `/tmp`** — `/tmp` is a 993 MB tmpfs that eats the same RAM Gradle needs. The sandbox has 1.98 GB total, so `gradle.properties` is retuned from `-Xmx4G` to `-Xmx1100m`, serial GC, one worker.

`apply_patch.py` is idempotent — safe to re-run.

---

## What the patch changes

### 1. `StormBackdrop.java` — the sky *behind* the storm  ← the correction

Previously I built halos as **rings around** the storm. Wrong. These are **gradient backdrops behind** it: a huge soft blob, black in the middle, fading to fully transparent at the rim so it melts into the real sky. The ordinary skybox keeps its own colour; only the patch of sky the storm stands in front of is recoloured, and it **follows the storm**.

Drawn on a camera-facing plane pushed `bodyRadius × 2.4 + 24` blocks *beyond* the storm, so the body always occludes its core. Registered **before** `StormPresenceFX` so every other effect sits on top.

| Phase | Backdrop |
|---|---|
| < 4.5 | nothing |
| 4.5 → 5.1 | dark turquoise/green haze, black blur in the centre |
| 5.1 → 5.5 | swings to the purple sky |
| 5.5 + | purple wraps out into magenta/pink, grows with the storm |

Five 1024² textures, colours sampled directly off your screenshots:

| Texture | Sampled from |
|---|---|
| `backdrop_black.png` | pure black falloff, used under every phase |
| `backdrop_turquoise.png` | `144558.png` — `#172426 #1D2D2F #243535 #37453E` |
| `backdrop_purple.png` | `073325.png` + `072359.png` — `#09060F → #381D52 → #653469` |
| `backdrop_purple_pink.png` | `145046.png` + `145348.png` — `#5F1148 → #9C1A8A` |
| `backdrop_ember.png` | `145217.png` — `#925F52 → #F19267` (off by default) |

### 2. `StoryModeClouds.java` + rewritten `CloudColorMixin` — flat time-of-day clouds

**I found the real bug.** The mod already ships `rendertype_clouds.fsh`/`.vsh`, but they are **dead files**:

1. they sit in `assets/dabywitherstormmod/shaders/core/` — vanilla only ever reads `assets/minecraft/shaders/core/`;
2. nothing in the Java references them;
3. the `.fsh` reads `texCoord0` and `position`, but the `.vsh` only outputs `vertexDistance` and `vertexColor` — **it would not even link** if loaded.

So the Story Mode clouds have never actually been running.

You asked for this built into the mod rather than a resource pack, "because it will turn off and reset the vanilla clouds". That is right, and it's why I did **not** ship your `.fsh`: a pack overriding `clouds.fsh` is bypassed the moment Iris/OptiFine takes over the cloud pipeline, and is lost on any pack reorder. Instead the same result is computed in Java and pushed through `CloudColorMixin`, which survives shader packs and pack ordering.

Your shader's core idea — *ignore `vertexColor`, use one flat colour for every face* — is preserved exactly; it just happens on the CPU. Palette:

| Time | Colour |
|---|---|
| day | `#F6F5FF` lavender-white |
| sunset | `#FAC29E` peach |
| night | `#4C5CAD` deep blue |
| dawn | `#F0CCDB` pale rose |

Clouds also go transparent as the storm's black backdrop approaches, which is the fade you described.

### 3. Story Mode skies — `day` / `night` / `sunset`

Replaces the FabricSkyboxes textures (256×1024, matching the existing format) with gradients sampled from your last three screenshots:

- **day** — `#867FF1` zenith → `#CCAAFB` horizon (the lavender Story Mode sky)
- **night** — `#10114A` → `#4A67EC`
- **sunset** — violet cap → `#F19267` ember horizon

### 4. Turquoise teeth

Unchanged and re-applied: 28 px per atlas on `wither_storm.png` and `wither_storm_og.png`.

### 5. Config — 18 new options

Three new headers before "Cataclysm Halos": **Storm Backdrop**, **Story Mode Clouds**, **Turquoise Teeth**. Sub-options grey out under their masters.

---

## Not done yet

- **Coloured lighting / dynamic shadows changing with time of day** — the mod has `storm_shadow`, `storm_sun_glow` and a `colorful_lighting` hook already; I have not touched them.
- **Nether pink backdrop** — sampled (`#5F1148 → #9C1A8A`) but not wired to a dimension check.
- **Panorama, guidebook, schematic worldgen** — still outstanding.

---

## Build result (this session)

```
BUILD SUCCESSFUL in 1m 8s
```

- `dist/dabywitherstormmod-1.9.60-26.2-beta-slim.jar` — **34 MB**, 371 classes, zip-verified
- Full jar was 135 MB; **292 MB of `.bbmodel` Blockbench project files are never read by any code**
  (`grep -rn bbmodel --include=*.java` → no hits), so the slim jar strips `assets/dabywitherstormmod/geo/**`
  and the loose `.dae`. Runtime geometry comes from `meshes/` + `models/`, both kept.
- Verified inside the slim jar: `StormBackdrop.class`, `StoryModeClouds.class`, `CloudColorMixin.class`,
  all 5 backdrops, all 3 skies, both `_e` teeth maps, `fabric.mod.json`, `dabywitherstormmod.mixins.json`.

Preview of the phase progression: `refimg/backdrop_ingame_preview.png`
Texture sheet: `refimg/mcsm_atmosphere_preview.png`

## Environment gotchas (cost real time — do not rediscover)

| Trap | Fix |
|---|---|
| Workspace prunes >128 MB | keep source in `/var/tmp/build`, ship only the slim jar |
| Installed packages do not persist | `apt-get install -y openjdk-25-jdk-headless` each session |
| `/tmp` is a 993 MB **tmpfs** eating the same RAM Gradle needs | build in `/var/tmp` |
| Sandbox has 1.98 GB RAM, repo asks `-Xmx4G` → daemon killed | `-Xmx1100m`, serial GC, 1 worker, `--no-daemon` |
| `getDayTime()` does not exist in 26.2 | `Level.getOverworldClockTime()` |
