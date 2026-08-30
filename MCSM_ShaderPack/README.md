# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2 / 26.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Active Iris Shader Options**: Configurable toggles for Cloud Thickness, Story Mode Clouds, Dynamic Skybox, MCSM Colored Lighting, and Emissive Teeth Bloom. Standalone `block.properties` ensures menu ungrays immediately.
- **Pipeline Cloud Routing**: `shaders.properties` with `clouds=fast` explicitly instructs Iris to intercept the cloud rendering loop and route geometry directly through `gbuffers_clouds`.
- **Modern Engine Alignment**: `uniform long worldTime` everywhere (Iris uniform type check), no reserved `texture` sampler names, and world-anchored `fract()` cloud UVs so the 8 sheets tile seamlessly with square texels and shift with the time of day.
- **8 Story Mode Cloud Presets**: Authentic 256x256 Story Mode cloud sheets mapped locally via `customTexture.cloudTex0` through `customTexture.cloudTex7`.
- **2.5x Chunk Extrusion**: Vertex shaders vertically scale mesh bounds by 2.5x with GLSL 120 coordinate checking.
- **Identical Precision Headers**: Both `.vsh` and `.fsh` use `precision highp float; precision highp int;` to prevent GPU compiler crashes.
- **Story Mode Colored Lighting & Shadows**: Warm golden sunlight, lavender shadow tint, amber torchlight, surface normal diffuse shading, NO reflections.
- **Dynamic Story Mode Sky Dome**: Smooth Day, Noon, Sunset, and Night transitions with zero void horizon black bands.
- **Teeth Turquoise Glow**: Vibrant cyan/turquoise bloom (#00E5FF) pulsing on the Wither Storm teeth.
- **Hand Item Lighting**: Dedicated gbuffers_hand shaders ensuring items never render solid black.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
