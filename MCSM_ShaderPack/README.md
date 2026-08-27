# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2 / 26.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Pipeline Cloud Routing**: `shaders.properties` with `clouds=fast` explicitly instructs Iris to intercept the cloud rendering loop and route geometry directly through `gbuffers_clouds`.
- **8 Story Mode Cloud Presets**: All 8 authentic cloud presets (Day, Sunset, Night, Storm Gathering, Awakening Cyan Rim, Cataclysm Magenta, Volcanic Horizon, Twilight Purple) forced to render globally without external map dependencies.
- **Identical Precision Headers**: Both `.vsh` and `.fsh` use `precision highp float; precision highp int;` to prevent GPU compiler crashes on load.
- **Dynamic Story Mode Sky Dome**: Smooth Day, Noon, Sunset, and Night transitions with zero void horizon black bands.
- **Story Mode Colored Lighting & Shadows**: Warm golden sunlight, lavender shadow tint, amber torchlight, NO reflections.
- **Teeth Turquoise Glow**: Vibrant cyan/turquoise glow (#00E5FF) pulsing on the Wither Storm teeth.
- **Hand Item Lighting**: Dedicated gbuffers_hand shaders ensuring items never render solid black.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
