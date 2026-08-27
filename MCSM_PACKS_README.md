# Minecraft: Story Mode — Official Visual Overhaul & Mod Integration (Minecraft 1.21.2 & 26.2)

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games, integrated directly into both standalone packs (**Resource Pack** and **Shader Pack**) and the core **Wither Storm Mod** (`Lowuuuuuu`).

---

## 📦 Deliverables Summary

1. **`MCSM_ShaderPack.zip`** (Flat root structure with `shaders/` directly at root)
   - **Thick Extruded Story Mode Clouds**: `gbuffers_clouds.vsh` transforms cloud geometry in world space using `gbufferModelViewInverse`, extrudes the cloud deck vertically by **2.5x** (`worldPos.y *= 2.5`), and sets flat uniform Story Mode brightness (all faces 1.0) with crisp blocky edges.
   - **User GLSL Core Cloud Shader**: Includes `rendertype_clouds.vsh` (GLSL 150 with `CloudFaces`, `CloudInfo`, `BrightnessTop/Bottom/Sides = 1.0`, `CloudHeight = 2.5`, `CloudFadeAlpha = 0`), `rendertype_clouds.fsh`, and `rendertype_clouds.json` wired into `shaders/core/` and `shaders/`.
   - **Authentic Story Mode Colored Lighting & Shadows**: Warm golden direct sunlight, cool lavender ambient shadow tint, and warm amber torchlight with zero reflections and diffuse ground shading.
   - **Story Mode Daytime Sky Dome**: Signature MCSM periwinkle lavender zenith -> soft lilac -> mauve -> peach -> golden amber horizon gradient.
   - **Teeth Turquoise Glow**: Electric turquoise/cyan glow (`#00E5FF`) on the Wither Storm teeth.
   - **100% Crash-Free GLSL**: Standardized uniform declarations (`uniform sampler2D gtexture;` replacing illegal `texture` keyword declarations) eliminating driver crashes on Iris and OptiFine.

2. **`MCSM_ResourcePack.zip`** (Flat root structure with `pack.mcmeta` directly at root)
   - **Core Cloud Shader Fallback**: `assets/minecraft/shaders/core/rendertype_clouds.vsh`, `.fsh`, and `.json` included for vanilla Minecraft rendering when shaders are toggled off.
   - **Zero Cloud PNGs**: Pure shader-based clouds; all `clouds.png` and `mcsm_cloud.png` textures completely deleted.
   - **Universal `pack.mcmeta`**: Simplified pack format avoiding `JsonParseException` on Minecraft 26.2 and 1.21.2.
   - **Crash-Free OptiFine Custom Skies**: Consecutive `sky1`, `sky2`, `sky3` numbering with explicit `source=./sky*.png` tags and universal `blend=add` to prevent `NullPointerException` crashes in Skyboxify / OptiFine.
   - **Story Mode Textures & Sounds**: Authentic OG obsidian-gloss textures, sound effects, and UI clicks.

3. **Mod JAR (`dabywitherstormmod-1.9.60-26.2-beta.jar`)**
   - **Presets**: **"Minecraft story mode OG"** (Default) and **"Minecraft story mode netflix"** (Secondary).
   - **Phase 4 Cyan 3D Spherical Shield Halo (`#00E5FF`)**: True 3D spherical shell wrapped entirely around the boss bounding box with depth testing (`glEnable(GL_DEPTH_TEST)`).
   - **Atmospheric Post-Processing**: Phase 5 pink-magenta fog/glare, Phase 6 volcanic dithered horizon, Phase 6.5 purple flashbang (`#E0B0FF`) with 45-tick exponential decay and periodic 2-minute End-flash.
   - **Vortex Renderer**: Dynamic swirling atmospheric disc for Phases 7 & 8.
   - **Unified Command**: `/devouringstorms` root command only.

---

## 🛠️ Diagnostics & Solutions from Your Latest Game Log

### 1. Crash on Load: `Failed to parse post chain at dabywitherstormmod:post_effect/storm_atmosphere.json`
* **Root Cause**: In Minecraft 1.21.2 / 26.2 snapshot, Mojang restructured post chains: `outtarget` and `intarget` were replaced with `output` and `inputs`. The mod JAR active in your `.minecraft/mods/` directory is an older build that still had `storm_atmosphere.json` inside it.
* **Solution**: Download the latest mod JAR built by GitHub Actions CI and replace your existing JAR in `.minecraft/mods/`. In the latest build, `storm_atmosphere.json` has been completely removed from mod resources, preventing `ShaderManager.loadPostChain` from failing.

### 2. Regular Vanilla Clouds Rendering Instead of Story Mode Clouds
* **Root Cause**: Two factors caused this:
  1. Your log showed an external pack loaded: `Resource pack 'Story Mode Clouds.zip' indicates the following shaders should be ignored: rendertype_clouds.vsh`. This pack explicitly commanded Iris to skip custom cloud shaders.
  2. When an Iris shaderpack is enabled, Iris bypasses vanilla core shaders and routes clouds through `gbuffers_clouds.vsh`. The previous `gbuffers_clouds.vsh` in `MCSM_ShaderPack` only called `ftransform()`, rendering vanilla-sized clouds.
* **Solution**:
  1. Remove or uncheck `Story Mode Clouds.zip` in your Resource Packs menu so it doesn't conflict.
  2. Update `MCSM_ShaderPack.zip`. In `MCSM_ShaderPack`, `gbuffers_clouds.vsh` now calculates the cloud positions in world space using `gbufferModelViewInverse`, scales the cloud height vertically by **2.5x** (`worldPos.y *= 2.5`), and sets uniform 1.0 face brightness to match the Story Mode blocky cloud look.

### 3. Resource Pack Metadata Warning: `missing mandatory fields min_format and max_format`
* **Root Cause**: Minecraft 26.2 changed the schema validation for version ranges.
* **Solution**: Simplified `pack.mcmeta` in `MCSM_ResourcePack.zip` to standard integer format `pack_format: 64`.

---

## 🚀 Step-by-Step Installation Instructions

1. **Update Mod JAR**:
   - Download the latest mod JAR artifact from the GitHub Actions CI run.
   - Place `dabywitherstormmod-1.9.60-26.2-beta.jar` into `.minecraft/mods/` (overwrite the old one).

2. **Update Shaderpack**:
   - Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (do NOT unzip).
   - In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.

3. **Update Resource Pack**:
   - Copy `MCSM_ResourcePack.zip` into `.minecraft/resourcepacks/` (do NOT unzip).
   - In Minecraft: Options -> Resource Packs -> enable **MCSM_ResourcePack** and move it to the top.
   - Disable any conflicting external cloud packs such as `Story Mode Clouds.zip`.
