# Minecraft: Story Mode — Official Visual Overhaul & Mod Integration (Minecraft 1.21.2 & 26.2)

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games, integrated directly into both standalone packs (**Resource Pack** and **Shader Pack**) and the core **Wither Storm Mod** (`Lowuuuuuu`).

---

## 📦 Direct Downloads & Deliverables Summary

| Deliverable | Installation Location | Status |
| :--- | :--- | :--- |
| **[MCSM_ShaderPack.zip](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ShaderPack.zip)** | `.minecraft/shaderpacks/` (DO NOT unzip) | **Ready & Updated** |
| **[MCSM_ResourcePack.zip](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/MCSM_ResourcePack.zip)** | `.minecraft/resourcepacks/` (DO NOT unzip) | **Ready & Updated** |
| **[dabywitherstormmod-1.9.60-26.2-beta.jar](https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a04054-lowuuuuuu/dabywitherstormmod-1.9.60-26.2-beta.jar)** | `.minecraft/mods/` | **Ready & Updated** |

---

## 🛠️ Visual Fixes & Updates Applied

### 1. pack.mcmeta Exact Schema (`MCSM_ResourcePack`)
* Declared exact requested schema:
  ```json
  {
    "pack": {
      "pack_format": 46,
      "supported_formats": {
        "min_format": 42,
        "max_format": 50
      },
      "description": "Minecraft: Story Mode Authentic Visuals"
    }
  }
  ```
* Ensures 100% compatibility across Minecraft 1.21.2 and 26.2 snapshot without `JsonParseException`.

### 2. Thick Extruded Story Mode Clouds & Active Shader Options (`gbuffers_clouds.vsh` & `shaders.properties`)
* **Active Iris Shader Options Menu**: Defined `screen=MCSM_OPTIONS` with configurable `#define` toggles (`CLOUD_EXTRUSION`, `CLOUDS_ACTIVE`, `DYNAMIC_SKY`, `MCSM_LIGHTING`, `EMISSIVE_TEETH_GLOW`), standalone `block.properties`, and language mappings (`lang/en_US.lang`) so Iris unlocks the "Shader Options..." button immediately.
* **GLSL 120 Cross-GPU Compatibility**: Replaced GLSL 130 `gl_VertexID` in `gbuffers_clouds.vsh` with standard normal and UV coordinate checks (`gl_Normal` and `gl_MultiTexCoord0`), preventing shader compilation errors and fallback to vanilla flat cloud strips.
* **Root & Shader Directives**: Placed `shaders.properties` at root level (`MCSM_ShaderPack/shaders.properties`) and in `shaders/shaders.properties`:
  ```properties
  clouds=fast
  customTexture.cloudTex0=shaders/textures/clouds/cloud0.png
  customTexture.cloudTex1=shaders/textures/clouds/cloud1.png
  customTexture.cloudTex2=shaders/textures/clouds/cloud2.png
  customTexture.cloudTex3=shaders/textures/clouds/cloud3.png
  customTexture.cloudTex4=shaders/textures/clouds/cloud4.png
  customTexture.cloudTex5=shaders/textures/clouds/cloud5.png
  customTexture.cloudTex6=shaders/textures/clouds/cloud6.png
  customTexture.cloudTex7=shaders/textures/clouds/cloud7.png
  screen=MCSM_OPTIONS
  screen.MCSM_OPTIONS=CLOUD_EXTRUSION CLOUDS_ACTIVE DYNAMIC_SKY MCSM_LIGHTING EMISSIVE_TEETH_GLOW
  ```
* **True 2.5x Vertical Extrusion**: `gbuffers_clouds.vsh` unprojects vector arrays into camera-relative world coordinates and scales geometry bounds vertically by **2.5x** (extruding top and side-top vertices upwards by 6 blocks for a solid 10-block slab thickness).
* **Identical Headers**: Both `.vsh` and `.fsh` use `precision highp float; precision highp int;` headers to eliminate GPU compiler crashes.
* **Core Cloud Shaders**: `core/rendertype_clouds.vsh` included in `MCSM_ResourcePack` for vanilla Minecraft rendering when shaders are off.

### 3. Dynamic Day/Noon/Sunset/Night Skybox (Zero Black Void Bands)
* **Smooth Time-of-Day Transitions**: `gbuffers_skybasic.fsh` dynamically blends between:
  - **Day**: Signature MCSM periwinkle lavender zenith -> soft lilac -> mauve -> golden amber horizon.
  - **Noon**: Vivid Story Mode azure blue zenith -> soft horizon.
  - **Sunset / Twilight**: Royal violet zenith -> vivid magenta -> fiery coral -> golden orange horizon.
  - **Night**: Deep obsidian midnight -> dark royal purple -> glowing indigo horizon.
* **Zero Black Void Horizon Band**: Removed hardcoded brown/black void floor (`cVoid`). Below the horizon smoothly clamps and fades into the horizon tint, completely eliminating the dark band moving across the sky when looking around.
* Removed broken rotating OptiFine custom sky overlays that created skybox artifacts.

### 4. Boss-Anchored Halo & Dark Roiling Shroud (Phase 5.1+)
* **Strictly Anchored to Boss Entity**: The cataclysm halo and dark roiling cloud shroud now anchor directly to the Wither Storm boss position in Phase 5.1+ and move strictly with the storm.
* **Normal Pre-Summon Sky**: Before the storm is summoned, no storm entities exist, so the sky remains completely normal with zero black bands.
* **ShaderPack Compatibility**: Updated `GlowRenderTypes.java` so `glow()` and `translucent()` route to `RenderTypes.eyes()` and `RenderTypes.entityTranslucent()` when `ShaderPackCompat.active() == true`, allowing Iris to render them cleanly.

### 5. Luminescent Turquoise Teeth Glow (#00E5FF) & Shaded OG Visuals
* Generated `phase_4_assets_og_e.png`, `phase_4_assets_e.png`, `devourer_assets_og_e.png`, and `wither_storm_og_e.png` with glowing turquoise teeth (`#00E5FF`) and glowing purple eyes (`#D81B60` / `#A800FF`).
* In `gbuffers_entities.fsh`, amplified teeth emissive bloom to 3.5x intensity with radiant turquoise glow, and purple eyes to 3.0x intensity.
* Shaded OG visuals wired in with matte near-black body and shaded edge outlines.

### 6. Fixed Solid Black Hand Items
* **32-Bit RGBA Conversion**: Converted all palette/indexed textures (`super_tnt_lava.png`, `tnt.png`, `tnt_bottom.png`, `tnt_top.png`) to 32-bit RGBA PNG with alpha transparency.
* **Dedicated Hand Shaders**: Created `gbuffers_hand.vsh` and `gbuffers_hand.fsh` in `MCSM_ShaderPack` with proper lightmap illumination (`max(lm.rgb, vec3(0.55))`), ensuring held items and hands never render solid black.

### 7. Story Mode Command Block & Grass Textures
* Included authentic Story Mode command block textures (`command_block_front.png`, `command_block_back.png`, `command_block_side.png`, `command_block_conditional.png`, `repeating_...`, `chain_...`) in `MCSM_ResourcePack`.
* Included authentic Story Mode vibrant grass block textures (`grass_block_top.png`, `grass_block_side.png`, `grass_block_side_overlay.png`).

---

## 🚀 Installation Instructions

1. **Shader Pack**:
   - Download **`MCSM_ShaderPack.zip`**
   - Place into `.minecraft/shaderpacks/` (DO NOT unzip)
   - Enable via **Options -> Video Settings -> Shader Packs -> MCSM_ShaderPack**

2. **Resource Pack**:
   - Download **`MCSM_ResourcePack.zip`**
   - Place into `.minecraft/resourcepacks/` (DO NOT unzip)
   - Enable via **Options -> Resource Packs -> Move to Right (Top priority)**

3. **Mod JAR**:
   - Download **`dabywitherstormmod-1.9.60-26.2-beta.jar`**
   - Place into `.minecraft/mods/` (replace any older beta jar)
