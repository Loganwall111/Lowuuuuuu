# Minecraft: Story Mode Official Visuals & Packs (26.2)

Complete visual recreation of **Minecraft: Story Mode** by Telltale Games and
Mojang Studios, engineered for modern Minecraft **26.2** (Fabric / Iris /
Sodium / OptiFine).

---

## 📦 r1 Deliverables

The **r1** release rebuilds the whole atmosphere stack. Downloads live in
[`docs/releases/r1/`](docs/releases/r1/) (repo-staged artifacts; GitHub
release-asset uploads are blocked from the build sandbox, so the CI-built jar
is published as an Actions artifact):

| Package | File | Install into | What it is |
| :--- | :--- | :--- | :--- |
| **Wither Storm Mod** | `devouring-storms-point-of-no-return-1.9.62-26.2-beta-r1.jar` | `.minecraft/mods/` | Gameplay mod with mod-owned runtime assets. Global skybox, cloud, and shader overrides are excluded and distributed separately. Renamed per build (`-r{run}`). |
| **MCSM Resource Pack** | `MCSM_ResourcePack.zip` | `.minecraft/resourcepacks/` | Shaded OG Story Mode textures, 4-point time-of-day custom skyboxes (lavender → orange), `rendertype_clouds.vsh` (2.5x extrusion), `emissive.properties` for the turquoise teeth aura. |
| **MCSM Shader Pack** | `MCSM_ShaderPack.zip` | `.minecraft/shaderpacks/` | **100% procedural GLSL clouds** (no PNG sheets), sun-cast shadows on ground & water that sweep with the day/night clock, dynamic sky, colored lighting, turquoise teeth bloom. |

Checksums: `docs/releases/r1/SHA256SUMS.txt`.

---

## ✨ What r1 actually changed

### Clouds are now real shaders — zero PNG cloud sheets
* **No more `cloudTex0..7` texture bindings.** The 8 PNG cloud sheets and all
  `customTexture.cloudTex*` entries were deleted from the shader pack.
* `gbuffers_clouds.fsh` / `rendertype_clouds.fsh` are **procedural**: fractal
  value-noise (`hash13`/`fbm`), world-anchored, drifting with the game clock.
* `gbuffers_clouds.vsh` / `rendertype_clouds.vsh` **unproject** vertices to
  world space and apply the Story Mode 2.5x vertical extrusion
  (`scaledVertex.y *= 2.5`, `CloudHeight = 2.5`) with per-face brightness
  (sunlit top, lavender ambient sides, shadowed bottom).
* **Live time-of-day colour**: day reads white/coral, sunset orange, night
  periwinkle — driven by the running `worldTime` clock.

### Restored Story Mode skybox loop
* `SkyRendererMixin` re-tints the vanilla sky dome: **lavender zenith with a
  warm orange horizon** that follows the storm's phase — green at phase 4.5,
  turquoise at phase 5, purple/magenta/black through the cataclysm.
* The 4-point OptiFine custom skyboxes (`sky1..4.png` + properties) ship in
  both the resource pack and the mod jar; the world clock keeps running (never
  frozen at tick 0).

### Storm atmosphere as a true post-effect
* `post_effect/storm_atmosphere.json` + `shaders/post/storm_atmosphere.fsh`
  run a **full-screen post pass** (`StormAtmosphere`, hooked into
  `LevelRendererBloomMixin`) — purple → dark-magenta atmospheric fog gradient.
* The old solid 3D spherical shell (`WitherShieldSphere`) and its halo
  billboard were **deleted**. No solid shells, no texture walls: every storm
  atmosphere element is translucent/glow geometry or a screen-space pass.

### Phase FX (all GLSL/shader-style, no 3D assets)
* Light-blue centre halo at the storm core from phase 4 to the very end.
* Giant colour-shifting centre blob (phase 5.1 → 5.9): dark purple → magenta →
  pink/blue/black-purple nested soft shells.
* Heavy magenta/purple/pink/black rear fog layer attached to the storm's back
  (phase 5.1+), moving with it.
* Phase 6+: bright pulse **directly above the storm every 2 minutes**
  (2400-tick window, quick rise / slow fade).
* Phases 7/8: the **Vortex model mesh** (converted from `Vortex.bbmodel`)
  renders additively on top of the storm, rotating and tumbling.

### Fog / sky per phase
* Palette anchors: purple gloom → **green (4.5)** → turquoise (5+) →
  purple/pink drained cataclysm sky (5.45+). Driven by `StormPalettes`
  (4-anchor blend) and read by fog, sky tint, cloud tint, and the post pass.

### Shadows, lighting, held items
* New `shadow.vsh/fsh` sun shadow-map pass; `gbuffers_terrain.fsh` and
  `gbuffers_water.fsh` sample `shadowtex0` — **cast shadows sweep the ground
  and water** with the day/night cycle.
* `gbuffers_entities.fsh` keeps the **turquoise emissive teeth glow** (+
  magenta accents).
* Held-item alpha transparency preserved (`gbuffers_hand*` discard).

---

## 🎮 Recommended In-Game Settings

1. **Video Settings -> Quality -> Custom Sky**: `ON`
2. **Video Settings -> Quality -> Sky / Sun & Moon**: `ON`
3. **Video Settings -> Shader Packs -> MCSM_ShaderPack -> Shader Options**:
   - **Story Mode Clouds**: `ON` (procedural)
   - **Dynamic Skybox**: `ON`
   - **Story Mode Lighting**: `ON`
   - **Wither Storm Teeth Glow**: `ON`

---

## 🏗️ Building & release

* `.github/workflows/build.yml` compiles the mod jar (Java 25 + Fabric Loom),
  renames it per build, and uploads it as a CI artifact.
* `.github/workflows/mcsm-release.yml` (on `main`) additionally rebuilds the
  two packs through `tools/build_mcsm_packs.py` and force-uploads the three
  artifacts over the release tag.
* `tools/build_mcsm_packs.py` packages the committed pack directories as flat
  zips and **fails hard if any PNG cloud sheet or `cloudTex` binding sneaks
  back in**.
* `tools/merge_release_jar.py` produces the repo-staged r1 jar (original
  classes + merged resources, no `geo/` Blockbench sources, no `ffmpeg`).
