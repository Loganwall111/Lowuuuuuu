# MINECRAFT: STORY MODE — Official Atmosphere Shaderpack (1.21.2 / 26.2)
Standalone atmosphere shaderpack for **Iris** (Fabric) and **OptiFine** (Java Edition).

## Namespace unification
Every custom sky/environment asset resolves from ONE synchronized directory so
Sodium and Iris can never flash between mismatched namespaces:
- time-of-day skyboxes: `assets/minecraft/optifine/sky/world0/` (sky1-4 + the
  dark backdrop sheets + blue shield halo live there too);
- shader-pack custom bindings: `shaders/textures/...` (halos, flesh sheets,
  environment backdrop).
`gbuffers_skybasic` + `gbuffers_skytextured` sample the LIVE `worldTime`
uniform (with `sunAngle`/`sunPosition` fallbacks) so the clock never locks at
tick 0. The pack ships a single lowercase `shaders/lang/en_us.lang` and a
single `shaders/block.properties` — root-level duplicates are removed.

## How the packs fit together (single, zero-conflict pipeline)
The cloud and sky look is delivered by **two coordinated layers**:

1. **The mod JAR itself** (`assets/dabywitherstormmod/shaders/core/rendertype_clouds.{vsh,fsh}`)
   runs the vanilla-core cloud pipeline: the vertex stage decodes the vanilla
   `CloudFaces` buffer with the 2.5x extrusion and passes `worldPosCoord` to a
   100% mathematical fragment stage. This is the layer that always renders,
   with or without a shader pack.
2. **This shader pack** (`gbuffers_clouds.{vsh,fsh}` + `rendertype_clouds.{vsh,fsh}`)
   replaces the cloud program *inside Iris* with the same procedural noise
   math in Iris's own gbuffers dialect. No PNG cloud sheets, no `cloudTex0-7`
   samplers — the shader itself is the cloud, so turning the pack on can never
   blank the sky. `shaders.properties` keeps `clouds=fast` routing.

## Features
- **Iris Shader Options unlocked**: root `shaders.properties` + `shaders/shaders.properties`
  route the pipeline (`clouds=fast`, `customSkies=true`, `shadowMapResolution=2048`) and
  bind the custom materials (`witherFlesh`, `tornFlesh`, `blueHalo`, `darkBackdrop`).
- **100% procedural clouds**: blocky fbm noise mapped over `worldPosCoord`, live
  `uniform long worldTime` day/night palettes, distance haze via `vertexDistance`.
- **Dark backdrop hardcoded in-pack**: `gbuffers_skytextured` blends the bound
  dark purple-and-black atmosphere sheet (`shaders/textures/environment/sky/`) into
  the lower sky dome on shader initialization — no resource pack required.
- **Shiny materials**: `gbuffers_terrain` paints a soft specular metallic sheen
  + fresnel rim over the `witherFlesh` / `tornWitheredFlesh` voxel sheets, with
  the 2048px sun shadow map (`shadowtex0`) sweeping across terrain and water.
- **Story Mode Colored Lighting**: warm golden sunlight, lavender ambient
  shadows, amber torchlight, emissive turquoise teeth aura on entities.
- **Hand item alpha masking**: `gbuffers_hand` discards transparent texels so
  held tools never render as solid black voids.

## Installation
1. Install **Iris + Sodium** (recommended) or OptiFine.
2. Copy `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` (DO NOT unzip).
3. In Minecraft: Video Settings -> Shader Packs -> select **MCSM_ShaderPack**.
4. For the full Story Mode look keep the MCSM resource pack enabled too — the
   mod's own assets (skyboxes, skins, presets) now live inside the mod JAR.
