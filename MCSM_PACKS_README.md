# Minecraft: Story Mode — Official Visual Overhaul & Mod Integration (Minecraft 1.21.2)

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games, integrated directly into both standalone packs (**Resource Pack** and **Shader Pack**) and the core **Wither Storm Mod** (`Lowuuuuuu`).

---

## 📦 Deliverables Summary

1. **`MCSM_ShaderPack.zip`** (Flat root structure with `shaders/` directly at root)
   - **User GLSL Core Cloud Shader**: Includes `rendertype_clouds.vsh` (GLSL 150 with `CloudFaces`, `CloudInfo`, `BrightnessTop/Bottom/Sides = 1.0`, `CloudHeight = 2.5`, `CloudFadeAlpha = 0`), `rendertype_clouds.fsh`, and `rendertype_clouds.json` wired into `shaders/core/` and `shaders/`.
   - **Iris / OptiFine Cloud Pipeline**: `gbuffers_clouds.vsh` and `gbuffers_clouds.fsh` provide uniform 1.0 brightness, flat blocky Story Mode cloud lighting, and zero raymarched noise.
   - **Clean MCSM Lighting & Shadows**: Removed fake dynamic ground shadows and tinted lighting filters per user direction. Diffuse lighting authentic to Story Mode, zero reflections.
   - **Story Mode Daytime Sky Dome**: Signature MCSM periwinkle lavender zenith -> soft lilac -> mauve -> peach -> golden amber horizon gradient.
   - **Teeth Turquoise Glow**: Electric turquoise/cyan glow (`#00E5FF`) on the Wither Storm teeth.
   - **100% Crash-Free GLSL**: Standardized uniform declarations (`uniform sampler2D gtexture;` replacing illegal `texture` keyword declarations) eliminating driver crashes on Iris and OptiFine in 1.21.2.

2. **`MCSM_ResourcePack.zip`** (Flat root structure with `pack.mcmeta` directly at root)
   - **Deleted Cloud PNGs**: Pure shader-based clouds; all `clouds.png` and `mcsm_cloud.png` textures completely removed.
   - **pack_format: 42**: Native Minecraft 1.21.2 format with `supported_formats: 15-60`.
   - **Crash-Free OptiFine Custom Skies**: Consecutive `sky1`, `sky2`, `sky3` numbering with explicit `source=./sky*.png` tags and universal `blend=add` to prevent `NullPointerException` crashes in Skyboxify / OptiFine.
   - **Story Mode Textures & Sounds**: Authentic OG obsidian-gloss textures, sound effects, and UI clicks.

3. **Mod JAR (`dabywitherstormmod-1.9.60-26.2-beta.jar`)**
   - **Presets**: **"Minecraft: Story Mode OG"** (Default) and **"Minecraft: Story Mode Netflix"** (Secondary).
   - **Phase 4 Cyan 3D Spherical Shield Halo (`#00E5FF`)**: True 3D spherical shell wrapped entirely around the boss bounding box with depth testing (`glEnable(GL_DEPTH_TEST)`).
   - **Atmospheric Post-Processing**: Phase 5 pink-magenta fog/glare, Phase 6 volcanic dithered horizon, Phase 6.5 purple flashbang (`#E0B0FF`) with 45-tick exponential decay and periodic 2-minute End-flash.
   - **Vortex Renderer**: Dynamic swirling atmospheric disc for Phases 7 & 8.
   - **Unified Command**: `/devouringstorms` root command only.

---

## 🛠️ Root Cause Analysis & Fixes

### 1. Shaderpack Crash on Load in Minecraft 1.21.2
* **Root Cause**: Modern GLSL in Minecraft 1.21.2 Core Profile reserves `texture` as a built-in function name (`texture(sampler, coord)`). Declaring `uniform sampler2D texture;` in `gbuffers_terrain.fsh`, `gbuffers_entities.fsh`, and `gbuffers_skytextured.fsh` caused shader compilation errors and immediate renderer crashes.
* **Fix**: Replaced all instances of `uniform sampler2D texture;` with the standard OptiFine / Iris identifier `uniform sampler2D gtexture;`.

### 2. Resource Pack Reload Crash (NullPointerException)
* **Root Cause**: The OptiFine custom sky properties files lacked the mandatory `source=./sky1.png` path, and skipped `sky2.properties` (`sky1` jumped directly to `sky3`). Parsers like Skyboxify and FabricSkyBoxes crashed with a `NullPointerException` when trying to resolve the missing source string.
* **Fix**: Numbered sky layers consecutively (`sky1`, `sky2`, `sky3`) and added explicit `source=./sky*.png` parameters to every `.properties` file with `blend=add`.

### 3. Replacement of Volumetric Clouds with Core Cloud Shader
* **Root Cause**: Procedural noise raymarching in `gbuffers_skybasic.fsh` produced unintended volumetric clouds.
* **Fix**: Volumetric noise functions were deleted. Wired the user-supplied `rendertype_clouds.vsh` (GLSL 150 std140 uniform `CloudInfo`, `uniform isamplerBuffer CloudFaces`, `CloudHeight = 2.5`, `CloudFadeAlpha = 0`, `Brightness = 1.0`) directly into `MCSM_ShaderPack`, along with `gbuffers_clouds.vsh` and `gbuffers_clouds.fsh` for Iris/OptiFine shader pipeline support. Deleted all `clouds.png` files.

### 4. Removal of Custom Ground Shadows & Lighting Filters
* **User Requirement**: *"Delete add shadows and lighting to the game to make it exactly like Minecraft story mode No reflections"*
* **Fix**: Removed dynamic shadow tinting and colored lighting passes from `gbuffers_terrain.fsh` and `composite.fsh`. Diffuse lighting now authentically matches Minecraft: Story Mode.

---

## 🚀 Installation Instructions

1. **Shaderpack**:
   - Copy `MCSM_ShaderPack.zip` into your `.minecraft/shaderpacks/` directory (do NOT unzip).
   - In Minecraft: Options -> Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
2. **Resource Pack**:
   - Copy `MCSM_ResourcePack.zip` into your `.minecraft/resourcepacks/` directory (do NOT unzip).
   - In Minecraft: Options -> Resource Packs -> move **MCSM_ResourcePack** to the top of the selected list.
3. **Mod JAR**:
   - Place the compiled mod JAR into your `.minecraft/mods/` directory.
