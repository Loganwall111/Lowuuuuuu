# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Features
- **Official MCSM Core Cloud Shader**: Integrated GLSL 150 core cloud shader (`rendertype_clouds.vsh` with `CloudFaces`, `CloudInfo`, `BrightnessTop/Bottom/Sides = 1.0`, `CloudHeight = 2.5`, `CloudFadeAlpha = 0`).
- **Iris / OptiFine Cloud Pipeline**: `gbuffers_clouds` renders flat crisp Story Mode clouds without raymarched noise.
- **Story Mode Daytime Sky Dome**: Signature MCSM periwinkle lavender -> lilac -> mauve -> peach -> amber horizon gradient.
- **Teeth Turquoise Glow**: Vibrant cyan/turquoise glow (#00E5FF) pulsing on the Wither Storm teeth.
- **No Dynamic Reflections or Heavy Shadows**: Authentic clean Story Mode diffuse lighting.
- **100% Crash-Free**: Standardized uniform declarations (`gtexture`) avoiding GLSL keyword collisions.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine for Minecraft 1.21.2.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
