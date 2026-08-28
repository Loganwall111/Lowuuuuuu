# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2 / 26.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Active Iris Shader Options**: `shaders.properties` (+ `shaders/shaders.properties`) ships the pipeline
  routing (`clouds=fast`, `customSkies=true`, `shadowMapResolution=2048`) and three custom material
  bindings (`witherFlesh`, `tornFlesh`, `blueHalo`) so the Shader Options menu stays unlocked.
- **100% Procedural Clouds**: `gbuffers_clouds` / `rendertype_clouds` generate the cloud pattern
  mathematically from fractal noise. There are **no** `cloudTex0-7` image variables and **no** PNG
  cloud sheets anywhere in this pack.
- **Cloud Vertex Re-Anchoring**: the vertex stage decodes the `CloudFaces` buffer, builds the 2.5x
  extruded face quad per cell and passes `worldPosCoord` cleanly into the fragment channels along
  with `vertexColor` and `vertexDistance`.
- **Live Time-of-Day**: `uniform long worldTime` with `sunAngle` / `sunPosition` fallbacks drives the
  lavender day, coral sunset and periwinkle night palettes so the sky and clouds never freeze.
- **Shiny Materials**: `gbuffers_terrain` binds the `witherFlesh` / `tornWitheredFlesh` custom
  textures and paints a soft specular metallic sheen + fresnel rim over the black voxel sheets so
  they catch light highlights dynamically.
- **Story Mode Colored Lighting & Shadows**: Warm golden sunlight, lavender shadow tint, amber
  torchlight, and live sun-cast shadows that sweep with the clock.
- **Hand Item Lighting**: Dedicated gbuffers_hand shaders with explicit alpha masking so held items
  never render as a solid black box.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
