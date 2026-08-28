# Minecraft: Story Mode Official Visuals & Packs (1.21.2 & 26.2)

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games and Mojang Studios, engineered for modern Minecraft **1.21.2** and **26.2** (Fabric / Iris / Sodium / OptiFine).

---

## 📦 Deliverables & 1-Click Direct Downloads

| Package | Permanent Download (Release Asset) | Target Directory | Description |
| :--- | :--- | :--- | :--- |
| **MCSM Shader Pack** | [MCSM_ShaderPack.zip](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ShaderPack.zip) / [mirror](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a048fa-lowuuuuuu/MCSM_ShaderPack.zip) | `.minecraft/shaderpacks/` | Standalone atmosphere shaderpack: 2.5x chunky 3D extruded clouds, active Iris Shader Options, dynamic sky dome, colored lighting & shadows, and luminescent turquoise teeth bloom. |
| **MCSM Resource Pack** | [MCSM_ResourcePack.zip](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ResourcePack.zip) / [mirror](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a048fa-lowuuuuuu/MCSM_ResourcePack.zip) | `.minecraft/resourcepacks/` | Standalone authentic visual resourcepack: original author custom textures, 4-point time-of-day custom skyboxes (lavender→orange), 8 Story Mode cloud sheets, 32-bit RGBA items, and sounds. |
| **Wither Storm Mod** | [latest dabywitherstormmod JAR](https://github.com/Loganwall111/Lowuuuuuu/releases/tag/v1.9.60-26.2-mcsm) | `.minecraft/mods/` | Freshly compiled in CI on every release (`dabywitherstormmod-1.9.61-26.2-beta-r{N}.jar`, renamed per build). Bundles the storm atmosphere purple backdrop, post-processing filters, sky/cloud mixins, and the custom skyboxes. |
| **Resource Pack + Mod bundle** | [MCSM_ResourcePack_and_Mod.zip](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ResourcePack_and_Mod.zip) | split | One download with both. |
| **Shader Pack + Mod bundle** | [MCSM_ShaderPack_and_Mod.zip](https://github.com/Loganwall111/Lowuuuuuu/releases/download/v1.9.60-26.2-mcsm/MCSM_ShaderPack_and_Mod.zip) | split | One download with both. |

---

## 🛠️ Complete 4-Protocol Visual Pipeline Repair

### Protocol 1: Shader Mappings & Disappeared Skybox Restoration
* **Custom Time-of-Day Skyboxes Restored**: Reinstated authentic 1536×1024 author skyboxes in `assets/minecraft/optifine/sky/world0/` (`sky1.png` - `sky4.png`).
* **Complete 4-Point Fade Specifications**: Fixed OptiFine custom sky properties with all 4 required fade times (`startFadeIn`, `endFadeIn`, `startFadeOut`, `endFadeOut`) and `blend=alpha` for smooth cross-fading across Day, Noon, Sunset, and Twilight.
* **Active live game time uniform (`worldTime`) Sampling**: Both `gbuffers_skybasic.fsh` and `gbuffers_skytextured.fsh` actively sample `worldTime` (and vertex-interpolated `vLiveTime`).
* **Sodium Tick 0 Anti-Freeze Protection**: If Sodium / Iris locks `worldTime` at tick 0 or delays initialization, shaders dynamically fall back to computing live celestial time from `sunAngle` and `sunPosition` coordinates, preventing frozen dawn/night loops.
* **World Space Background Definitions**: `gbuffers_skybasic.vsh` unprojects view vectors using `mat3(gbufferModelViewInverse)` into true camera-relative world direction (`worldDir.y`), guaranteeing the sky dome stays fixed to the world horizon rather than pitching with player head movements.
* **Iris / OptiFine Shader Options**: Root and shader `shaders.properties` configure `clouds=fast`, `customTexture.cloudTex0` through `customTexture.cloudTex7` mapping to `shaders/textures/clouds/cloud0.png` .. `cloud7.png`, and define interactive menus for all options. Standalone `block.properties` ensures Iris immediately ungrays the "Shader Options..." button.

### Protocol 2: Rebuilt Extruded 3D Story Mode Clouds
* **Explicit Texture Samplers**: `gbuffers_clouds.fsh` and `rendertype_clouds.fsh` declare all 8 texture samplers (`uniform sampler2D cloudTex0;` through `uniform sampler2D cloudTex7;`), eliminating empty texture register bugs and preventing fallback to flat unshaded geometry.
* **2.5x Chunky Mesh Extrusion**: `gbuffers_clouds.vsh` and `rendertype_clouds.vsh` unproject coordinates to world space and scale mesh geometry height by 2.5x (`worldPos.y *= 2.5`) for thick, boxy Minecraft: Story Mode cloud slabs.
* **Universal GPU Stability**: Unified `precision highp float; precision highp int;` headers declared identically across all vertex and fragment files to stop driver compiler errors.
* **Dynamic 3D Shading**: 3-tier directional lighting across top faces (warm sunlight highlight), side faces (ambient contrast), and bottom faces (soft lavender shadow tint).

### Protocol 3: Purged Corrupted Metadata Text Leaks
* **Clean Localization**: Cleaned `lang/en_us.lang`, `lang/en_US.lang`, `shaders/lang/en_us.lang`, and `shaders/lang/en_US.lang`.
* **Zero Leaked Markdown or URLs**: Deleted all leaked compilation outputs, download URLs, markdown `#` headers, and table syntax from UI and language files.
* **Standard Key Mappings**: Clean localization mappings for options menus, items, hotbars, blocks, subtitles, and death messages.

### Protocol 4: Held Item Transparency & Mod ZIP Schema
* **Transparency Masking Registered**: Cleaned and converted all item textures in `assets/dabywitherstormmod/textures/item/` (`grapple.png`, `command_circuit.png`, `super_tnt.png`, `formidibomb.png`, `rocket_retriever.png`, etc.) to 32-bit RGBA with zeroed-out transparent channels (`(0, 0, 0, 0)`).
* **Dual-Sampler Hand Shaders**: `gbuffers_hand.fsh` and `gbuffers_hand_water.fsh` declare both `texture` and `gtexture` samplers with explicit alpha discard (`if (col.a < 0.1) discard;`), preventing held items from rendering as solid black boxes over the viewport.
* **Modern Split Range Schema**: Declared in `pack.mcmeta` across resource pack, shader pack, and mod JAR:
  ```json
  {
    "pack": {
      "pack_format": 46,
      "supported_formats": {
        "min_format": 42,
        "max_format": 50
      },
      "description": "Minecraft: Story Mode Authentic Visual Pack"
    }
  }
  ```
* **Flat Archive Layout**: Zero nested wrapper folders in all packages.

---

## 🎮 Recommended In-Game Settings

1. **Video Settings -> Quality -> Custom Sky**: `ON`
2. **Video Settings -> Quality -> Sky / Sun & Moon**: `ON`
3. **Video Settings -> Shader Packs -> MCSM_ShaderPack -> Shader Options**:
   - **Story Mode Clouds**: `ON`
   - **Cloud Thickness**: `ON (2.5x Extrusion)`
   - **Dynamic Skybox**: `ON`
   - **Story Mode Lighting**: `ON`
   - **Wither Storm Teeth Glow**: `ON`

---

## 🔧 Protocol 5 — Modern Engine Alignment (this finalized build)

* **`uniform long worldTime`** — every sky/cloud program now declares `worldTime` with the
  modern engine's actual type. The old `uniform int` declaration failed Iris's uniform type
  check and silently disabled the sky programs — the "missing time-of-day skybox" symptom.
* **Reserved-keyword sampler purge** — `uniform sampler2D texture;` is illegal in the GLSL 3.3
  core profile Iris compiles against; all programs sample `gtexture` only. (Unbound `texture`
  samplers previously returned opaque white over terrain/entities.)
* **Stale GLSL120 core overrides removed from the shader pack** — `shaders/core/*` in a shader
  pack must be `#version 150`; the extruded 3D cloud core vsh lives in the *resource pack* where
  the vanilla pipeline accepts it.
* **Cloud pattern re-alignment in `gbuffers_clouds.fsh`** — the eight square 256×256 sheets are
  now sampled from camera-relative **world position** (`SHEET_BLOCKS`), so texels are square
  (no more 2:1 vertical squash from the legacy atlas UV), drift is `fract()`-wrapped (seamless,
  no clamp-edge smearing), extruded side faces share the top face's horizontal phase (zero
  seams), and per-preset weights bias toward the current time of day, so clouds shift with the
  sky.
* **`gbuffers_skytextured.fsh` really shifts now** — sun/moon/custom-sky quads are tinted warm
  orange at sunrise/sunset and lavender at night using the live time, instead of computing the
  time and discarding it.

## 🏗️ Protocol 6 — Integrated Repository Build

* `.github/workflows/mcsm-release.yml` recompiles the mod JAR from the latest branch sources
  (Java 25 + Fabric Loom on GitHub-hosted runners — no stale committed binaries anymore),
  rebuilds both packs through `tools/build_mcsm_packs.py`, renames the JAR per build,
  runs `tools/validate_release_artifacts.py`, and force-uploads everything over
  `v1.9.60-26.2-mcsm` with regenerated notes + sha256 digests via `tools/make_release_notes.py`.
* Zips are validated to be **flat-rooted** (no nested parent folder wrappers).

---

## 🔄 r1 Update (current build)

**Release:** `v1.9.61-26.2-mcsm-r1` — three downloads: the mod JAR, the resource
pack and the shader pack (each renamed per build).

* **Clouds are now 100% procedural GLSL.** The 8 PNG cloud sheets
  (`cloud0.png`–`cloud7.png` + the `customTexture.cloudTex*` entries) have been
  removed from the shader pack. `gbuffers_clouds.fsh` and
  `rendertype_clouds.fsh` generate the Story Mode cloud slabs with fractal
  value-noise, keep the 2.5x mesh extrusion (`worldPos.y *= 2.5`), and shift
  colour live with the game clock. The author's
  `rendertype_clouds.vsh` (CloudHeight 2.5 / CloudFadeAlpha 0 core shader) is
  bundled in the resource pack *and* the mod JAR.
* **Day/night moving shadows on ground + water.** New `shadow.vsh`/`shadow.fsh`
  sun shadow map; `gbuffers_terrain.fsh` and the new `gbuffers_water.fsh`
  sample it so cast shadows sweep with the sun (shadow strength gated by sun
  elevation).
* **Storm atmosphere post-effect.** The mod now ships
  `post_effect/storm_atmosphere.json` + `shaders/post/storm_atmosphere.fsh` — a
  true full-screen purple→dark-magenta fog pass (modern post-chain schema),
  gated behind a storm being present. No physical spheres or texture walls.
* **Restored lavender skybox clock loop.** The mod's `SkyRendererMixin`
  re-tints the vanilla sky dome to lavender with a warm orange horizon that
  follows the storm phase (green at 4.5 → turquoise at 5 → purple-black
  cataclysm) while the world clock keeps running; the 4-point OptiFine
  custom-skyboxes (`assets/minecraft/optifine/sky/world0/sky1..4`) are bundled
  in the resource pack AND the mod JAR.
* **New pure-shader atmosphere elements** (mod): light-blue centre halo
  (phase 4+ to the end), giant colour-shifting centre blob (5.1→5.9: dark
  purple → magenta → pink/blue/black-purple), heavy magenta/purple/black rear
  fog cloud (5.1+, moves with the storm), a flash above the storm every two
  minutes (phase 6+), and the Vortex model mesh on top (phases 7/8).
* **Shaded OG Story Mode textures** merged into the mod's default preset,
  including the `_e` emissive pairs for the turquoise teeth glow.
