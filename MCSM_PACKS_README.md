# Minecraft: Story Mode — Official Visual Overhaul & Mod Integration

Complete authentic visual recreation of **Minecraft: Story Mode** by Telltale Games, integrated directly into both standalone packs (**Resource Pack** and **Shader Pack**) and the core **Wither Storm Mod** (`Lowuuuuuu`).

---

## 📦 Deliverables Summary

1. **Newest Compiled Mod JAR (`dabywitherstormmod-1.9.60-26.2-beta.jar`)**
   - **Download Link**: [GitHub Actions Build #33100672013 Artifact](https://github.com/Loganwall111/Lowuuuuuu/actions/runs/33100672013)
   - Click **`dabywitherstormmod`** under the **Artifacts** section at the bottom of the page to download `dabywitherstormmod.zip`, unzip it once to get the `.jar`, and place `dabywitherstormmod-1.9.60-26.2-beta.jar` into your `.minecraft/mods/` directory.
   - Contains all new Java features:
     - Presets: **"Minecraft: Story Mode OG"** (Default) and **"Minecraft: Story Mode Netflix"** (Secondary).
     - Phase 4 Cyan 3D Spherical Shield Halo (`#00E5FF`) with Fresnel glow & depth testing.
     - Atmospheric post-processing shader & screen-space overlay suite (Phase 5 pink glare, Phase 6 volcanic dithered horizon, Phase 6.5 purple flashbang with 45-tick decay, 2-minute periodic End-flash).
     - Atmospheric Vortex renderer for Phases 7 & 8.
     - Unified `/devouringstorms` command.
     - Vibrant turquoise teeth glow (`#00E5FF`).
2. **`MCSM_ResourcePack.zip`** (19.6 MB) — Flat root structure (`pack.mcmeta` at root, zero nested folders). Contains authentic Story Mode textures, OG obsidian-gloss skins, electric turquoise teeth glow textures, 3D cyan shield textures, seamless 256×256 clouds, custom sounds, and fixed block models.
3. **`MCSM_ShaderPack.zip`** (6.4 KB) — Flat root structure (`shaders/` at root). Features the official periwinkle-to-golden-amber daytime sky dome, procedural 3D roiling shader clouds (no solid meshes/textures needed), warm direct sun lighting, cool lavender ground shadows, and neon bloom.

---

## 🛠️ Critical Bug Fixes & Architecture

### 1. Crash on World Load / Silent Termination (Completely Fixed)
* **Root Cause**: Previous builds inadvertently included internal mod core/post-processing shaders (`assets/dabywitherstormmod/post_effect/storm_atmosphere.json`) inside the resource pack. When Minecraft loads a world, vanilla's resource loader tries to compile any post-effects found in active resource packs; because vanilla Minecraft lacks the custom attributes and uniforms that mod code supplies, the render pipeline suffered an instant unhandled OpenGL fault, crashing to desktop with **no Java crash report**. In addition, shaderpacks referencing non-existent uniforms (`lightningBolt`) triggered driver link aborts.
* **Fix**:
  - `MCSM_ResourcePack.zip` is now strictly decoupled: it contains **only** pure textures, OptiFine skies (`sky1`, `sky3`, `sky4` with `blend=alpha`), and audio. All experimental post-effects, core shaders, and blockstates are stripped from the resource pack. Pure textures can never crash the game.
  - `MCSM_ShaderPack.zip` uses 100% compliant GLSL 120 / OptiFine / Iris syntax with zero illegal uniforms.

### 2. Why the "Netflix" Preset Wasn't Appearing in Your Settings
* **Root Cause**: The visual presets menu in the mod settings (Game Menu -> Mod Options -> Daby's Wither Storm Mod) is powered by compiled Java code in the mod `.jar` file inside your `.minecraft/mods/` folder. If you are running an older pre-built binary (`dabywitherstormmod-1.9.60-26.2-beta.jar`), that binary was compiled before the "Netflix" preset and "OG" default were added to `DabyWSClientConfig.java`, so its config screen still shows the old options (`MCSM`, `Legacy Java`, `Cinematic`).
* **Solution**:
  - The updated `MCSM_ResourcePack.zip` automatically applies the authentic **Story Mode OG** look (OG textures, bone teeth with turquoise glow `#00E5FF`, 3D cyan halo, and Story Mode clouds) regardless of which preset is selected in the older mod jar!
  - When selecting preset **MCSM** in your existing mod settings with `MCSM_ResourcePack.zip` enabled, you get the exact authentic Story Mode visual experience.
  - Rebuilding the mod JAR from this repo (`./gradlew build`) will update the in-game config menu labels to show **Minecraft: Story Mode OG** (Default) and **Minecraft: Story Mode Netflix**.

### 3. The Blinding White Dome Bug (Completely Resolved)
* **Root Cause**: `fabricskyboxes/sky/mcsm_twilight.json` had `"alwaysOn": true` with `"type": "add"`, and its bottom texture was an opaque amber block `(248, 182, 72, 255)`. When rendered additively over the player's view 24/7, `source + destination` clamped to `1.0` (blinding pure white `#FFFFFF`), creating a giant glowing white dome over the world.
* **Fix**: Completely eliminated `fabricskyboxes/` additive cubemaps and OptiFine's additive cloud ceiling `sky2.png`. Custom skies now utilize clean `blend=alpha` with correct diurnal fade schedules, and clouds are rendered procedurally via the shader without solid objects or additive whiteout.

### 2. Zero Nested Folders
* `MCSM_ResourcePack.zip` and `MCSM_ShaderPack.zip` have their contents (`pack.mcmeta` and `shaders/`) directly at the root of each `.zip`.
* **Important**: Do **not** unzip the `.zip` files. Drop them directly into `.minecraft/resourcepacks/` and `.minecraft/shaderpacks/`.

---

## ⚡ Technical Features Breakdown

### 1. Dynamic 3D Spherical Shell Halo (Phase 4 Wither Shield)
* **Mesh**: Full 3D hollow UV sphere mesh (`WitherShieldSphere.java`) with latitude rings and longitude sectors, automatically scaling to `bodyRadius * 1.75` to encapsulate the entire body of the boss.
* **Material**: Translucent glowing cyan-blue energy shield (`#00E5FF`).
* **Fresnel Glow**: Fragment shader (`wither_shield.fsh`) calculates `pow(1.0 - dot(N, V), 2.5)`: bright blue borders with a see-through center (~0.10 opacity) so the dark boss structure inside is clearly visible.
* **Scrolling Matrix**: Scrolling blocky voxel hex-grid texture pulsing with `sin(time * 3.2)`.
* **Depth Testing**: Rendered with `glEnable(GL_DEPTH_TEST)` (`CompareOp.GREATER_THAN_OR_EQUAL`) so that when the player or tentacles pass inside the shield, back-faces are correctly masked out.

### 2. Screen-Space Atmospheric Shader Suite (`storm_atmosphere.fsh`)
* **Phases 5.1–5.9 (Pink Atmosphere & Shadow Occlusion)**:
  - Stretched anamorphic ellipsoid ambient glare centered on upper back spine coordinates (`WitherPosition + vec3(0, 18, 0)`).
  - Intensely saturated pink-magenta (`#D81B60`) and deep void-violet (`#4A148C`) high-altitude fog.
  - High-contrast black shadow silhouette dynamic occlusion following the boss.
* **Phase 6 (Volcanic Red-Orange & Purple Fusion Gradient)**:
  - Blends deep void-black and electric purple sky matrix with volcanic fire-orange (`#FF6D00`) and blood-red (`#D50000`).
  - Originates from the lower horizon/bottom half of the viewport and radiates upward.
  - Noisy blocky 4×4 dithered step function preserving the authentic voxel-aligned pixelated edge style.
* **Phase 6.5 (Purple Flashbang with Exponential Decay)**:
  - Instantly forces screen viewport to maximum white-violet exposure saturation (`#E0B0FF`), blanking out world geometry.
  - Exponential decay function over exactly 45 game ticks (`flashIntensity = exp(-elapsed / 15.0)`), fading through deep twilight purple before returning to normal baseline visibility.
  - Automated 2-minute periodic end flash (`AUTO_FLASH_INTERVAL = 2400` ticks).

### 3. Teeth Color & Vibrant Turquoise Glow
* **Base Teeth**: Retains the clean, original white/bone teeth texture.
* **Glow Layer**: Purplish/dark tint removed and replaced with vibrant **turquoise / teal glow** (`#00E5FF` / `rgb(0, 229, 255)`).
* Both OptiFine/Iris emissive mapping (`phase_4_assets_e.png`) and standard head rendering (`WitherStormHeadRenderer.java`) emit this turquoise glow.
* Eyes remain vivid glowing magenta-pink (`#FF20FF`).

### 4. Presets & Models
* Preset 1: **Minecraft: Story Mode OG** (Default) — Uses OG obsidian-gloss skins, side tentacles, 3D cyan shield, turquoise teeth glow, and dynamic skies.
* Preset 2: **Minecraft: Story Mode Netflix** — Netflix visual style variant.
* Preset 3: **Legacy Java** — Brighter legacy visuals.
* Preset 4: **Cinematic** — High dynamic range bloom and enhanced debris.

### 5. Consolidated Commands
* Summon and manage the storm directly with:
  ```bash
  /devouringstorms spawn
  /devouringstorms setphase <phase>
  /devouringstorms grow <amount>
  /devouringstorms roar
  /devouringstorms locate
  ```
  Aliases `/devouringstorm` and `/dabyws` point to the exact same tree with no duplicates.

---

## 🚀 Installation

1. Copy `MCSM_ResourcePack.zip` into `.minecraft/resourcepacks/` (DO NOT unzip).
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft:
   - **Options -> Resource Packs**: Enable **MCSM_ResourcePack.zip** (place at the top).
   - **Video Settings -> Shader Packs**: Select **MCSM_ShaderPack**.
