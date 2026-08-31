# MCSM Ultimate Atmosphere — resource pack repair notes

`MCSM_Ultimate_Atmosphere .zip` (tracked in the repo root) was a 1.20.1 resource
pack that rendered incorrectly. This document records what was broken, why, and
what the fixed pack does instead. The repaired pack lives in
`mcsm-ultimate-atmosphere-fixed/` and is shipped as
`MCSM_Ultimate_Atmosphere_FIXED.zip`.

## Root causes found

| # | File in original pack | Problem | Why it broke rendering |
|---|----------------------|---------|------------------------|
| 1 | `assets/minecraft/shaders/core/rendertype_solid.fsh` / `.vsh` | Written in the pre-1.17 shader format (no `UV2`, no lightmap `Sampler2`, no `ChunkOffset`, no fog varyings, no `#moj_import`) | 1.20.1's `rendertype_solid` program declares `Position, Color, UV0, UV2, Normal` + fog/lightmap uniforms. The old sources don't match that interface, so every solid block (most of the world) renders broken. |
| 2 | `assets/minecraft/shaders/core/rendertype_clouds.fsh` | Targets a shader program that does not exist in 1.20.1 | 1.20.x renders clouds through `position_tex_color_normal`; the `rendertype_clouds` program only reappeared in 1.20.5. The file was inert (and its `position` varying was never even declared). Its "fade by Y" idea is also impossible: clouds are a flat plane at one fixed height. |
| 3 | `assets/fabricskyboxes/skyboxes/*.json` | Wrong folder | FabricSkyBoxes 0.7.x loads skybox JSONs from `assets/<namespace>/sky/` (`SkyboxResourceListener` uses `findResources("sky", …)`). The old path was never scanned, so the custom sky never loaded. |
| 4 | Skybox `"type": "square-textured"` + `"texture"` (singular) | Type/field mismatch | `square-textured` requires `"textures"` (6 side files). One image per time-of-day must use `single-sprite-square-textured` with a 3:2 image laid out as a 3×2 grid (verified against `SingleSpriteSquareTexturedSkybox.java`). The old JSON failed to deserialize. |
| 5 | Skybox `"blend": {"type": "add"}` | Additive blending over the vanilla sky | Washes the gradient skies out to near-white during the day. Now `"alpha"`: crossfades over the vanilla sky during the fade windows and fully covers it at full alpha. |
| 6 | `assets/colorful_lighting/lights.json` | Wrong path, wrong filename, wrong shape | The Colorful Lighting mod (erykczy, Fabric) reads `assets/<namespace>/light/emitters.json` as `{"minecraft:block": "#RRGGBB"}` and merges entries with its built-ins. The old object format (`{"color":…, "luminance":…}`) could not be parsed. |

## What the fixed pack contains

```
pack.mcmeta                                  pack_format 15 (1.20.1), unchanged
pack.png                                     generated icon
PACK_FIX_NOTES.txt                           in-game-adjacent notes (also on GitHub)
assets/fabricskyboxes/sky/{day,night,sunset}_sky.json   schemaVersion 2, single-sprite, alpha blend, sun/moon/stars decorations
assets/fabricskyboxes/textures/sky/{day,night,sunset}.png  1536×1024 (3:2) canvases, gradient on the 4 side faces, zenith color on top
assets/minecraft/shaders/core/rendertype_solid.{vsh,fsh}  vanilla 1.20.1 format + the pack's original soft ground-shading tweak
assets/mcsm_atmosphere/light/emitters.json  redstone torches #FF0000, soul lanterns #00FFFF, glowstone #FFCC44
```

The clouds shader override was removed on purpose: in 1.20.1 clouds render with
vanilla behavior, and any per-vertex "fade" is a no-op on a flat cloud plane.
Real cloud styling requires a shader pack (e.g. Iris) instead.

## Requirements for the fixed pack to work fully

- **FabricSkyBoxes 0.7.x for 1.20.1** (Fabric) — day/night/sunset skies.
- **Colorful Lighting by erykczy** (Fabric, client-side) — the three colored
  light entries. Not compatible with Sodium; without it only the lights are
  skipped, the rest of the pack still works.
- Vanilla 1.20.1: the pack still loads (format 15) and the fixed `rendertype_solid`
  shader works with no mods at all.

## Verification performed

- All JSON files parsed and checked field-by-field against the actual
  FabricSkyBoxes 0.7.x codecs (`Properties`, `Fade`, `Blend`, `Decorations`,
  `Texture`, `SkyboxType` registrations on branch `1.20.x/stable`).
- Skybox texture paths resolved against real files in the pack.
- Shaders rewritten line-by-line from the vanilla 1.20.1 sources
  (`rendertype_solid.fsh/.vsh/.json` from Minecraft's assets), so attribute,
  sampler and uniform interfaces match exactly.
- `emitters.json` matches the mod's own loader (`ConfigResourceManager`) and
  value syntax (`ColorEmitter`), placed under a separate namespace so the
  mod's built-in emitter list is preserved.

## v2 update — "Story Mode Visuals" (per request)

For a Forge 1.20.1 instance using **ForgeSkyboxes / Nuit**, the pack was refactored:

- All skybox assets moved from `assets/fabricskyboxes/` to `assets/forgeskyboxes/`
  (configs in `assets/forgeskyboxes/sky/`, textures in `assets/forgeskyboxes/textures/sky/`),
  with internal texture references renamed `forgeskyboxes:...`. Nuit scans
  `assets/*/sky/` in any namespace, so this works across Fabric/NeoForge/Forge ports.
- `blend.type` is `"alpha"` everywhere, with `"horizonBlend": true` included as
  requested (tolerated no-op in Nuit 1.20.x codecs; the horizon melt is done for
  real via alpha blend + `changeFog` + horizon-tinted textures).
- **45 skybox configs**: `default_*` fallbacks plus **14 biome families**
  (plains, forest, cherry grove, jungle, desert, badlands, savanna, swamp, snowy,
  taiga, mountains, ocean, mushroom fields, caves) × 3 times of day. Each biome
  family has its own fog color (`changeFog` + `fogColors`) and a horizon-tinted
  sky texture, gated with `conditions.biomes` / `dimensions: [minecraft:overworld]`,
  priorities day=10 / sunset=11 / night=12 for deterministic fog crossfades.
- **Story Mode clouds**: `position_tex_color_normal.{vsh,fsh}` (the cloud program
  in 1.20.1) rewritten — vanilla-faithful vertex stage; fragment stage rounds
  each cloud cell into soft puffs, dissolves cell bottoms, and melts clouds into
  distance fog. `rendertype_solid` is untouched apart from the atmosphere tweaks,
  so ground blocks are unaffected.
- **Story Mode shading** (`rendertype_solid.vsh`): warm sun / cool shade tint
  driven by the lightmap, directional shading, and AO-like contact darkening at
  block bases. True dynamic shadows are not possible in a resource pack — use a
  shader pack (Oculus/Iris) on top for those.
- **Emissive lighting**: OptiFine-format `assets/minecraft/optifine/emissive.properties`
  + `_e` overlays (torch, soul/redstone torch, lanterns, glowstone, sea lantern,
  shroomlight, redstone lamp, end rod, magma). Needs OptiFine or Continuity;
  delete the `optifine` folder if neither is installed.
- **Colored lighting**: `emitters.json` expanded to 20 entries.
- New generated **pack icon** (`story_mode_icon_raw.png` → `pack.png`).

Two zips ship at the repo root: `MCSM_Ultimate_Atmosphere_FIXED.zip` and
`Story_Mode_Visuals.zip` (identical content, different pack name/description).
Build/regenerate everything with `tools/build_story_mode_pack.py`.

## v3 — standalone SHADER PACK ("Story Mode Visuals")

The pack now also ships as a complete Iris/Oculus shader pack for 1.20.1
(`story-mode-visuals-shader/`, built by `tools/build_story_mode_shader.py`):

- **Pipeline** (old OptiFine-style, Iris-compatible): gbuffers_terrain (+cutout,
  mipped), entities, block, textured/textured_lit, water, clouds, skybasic,
  skytextured, composite, final, shadow.
- **Seamless procedural sky dome** in gbuffers_skybasic.fsh — ray-based gradient
  dome with horizon melt into biome fog, Story Mode sunset band (slider),
  procedural moon with craters/halo, twinkling stars, sun/moon god rays.
  gbuffers_skytextured discards vanilla skybox textures, permanently killing the
  "giant cube" artifact.
- **Per-biome fog**: 14 hardcoded profiles in lib.glsl (swamp = dense mossy
  mist, desert = golden heat-glare, snowy/taiga/mountains = lavender fade,
  forests/plains = clean cyan, cherry, jungle, badlands, savanna, ocean,
  mushroom, caves), crossfaded continuously via noise cells so borders melt.
- **Story Mode clouds**: procedural blocky noise cells, vertical alpha dissolve,
  celestial color clock (white noon / pink-lavender sunset / royal indigo
  midnight), wind-speed slider.
- **Shadows**: hard blocky directional shadows (shadow pass + no PCF),
  moving cloud footprint shadows (getCloudShadow), SSAO contact occlusion.
- **Telltale post**: Sobel depth+normal ink outlines, Season-1 LUT grade,
  vignette (night/underground boost), letterbox toggle, film grain, bloom.
- **Fluids**: flat saturated opaque water/lava with blocky glitter.
- **Settings UI**: art-style presets (Story Mode / Vibrant / Moody), sliders,
  and 10 toggle options via shader.properties + lang/en_US.lang.
- The resource-pack module (ForgeSkyboxes configs, emissives, Colorful Lighting
  emitters) is merged into the shader zip's `assets/` so the atmosphere remains
  even with the shader disabled.

Archive names at repo root: `StoryMode_Visuals.zip` and
`Story_Mode_Visuals_Shader.zip` (identical). The exact cloud GLSL reference
listing lives at `story-mode-visuals-shader/assets/minecraft/shaders/core/rendertype_clouds.vsh`.

NOTE: crafting-table interactions and block-step particles (Jesse-style
crafting UI) are mod territory, not shader territory — the pack's hand/block
passes style those surfaces, but implementing them needs a client mod.

## v4 — expanded feature set + huge config menu

- FIXED the cloud vertex shader compile crash: the reference listing's
  undeclared `BetterThirdPerson` identifier (west face) is now `BrightnessWest`.
- FIXED a hidden gbuffers compile issue: contact AO no longer reads the depth
  buffer inside gbuffers passes (not readable there — now pure geometry), and
  shadow uniforms (shadowProjection/shadowModelView/shadowtex0) are declared
  in every pass that queries the shadow map.
- NEW FEATURES: flat Story Mode lighting mode, optional soft shadows (9-tap),
  aurora borealis in snowy/taiga biomes, milky way band, desert/badlands heat
  shimmer, stylized shore foam, cloud cover/density/color-richness controls,
  entity contact AO, hand light boost, vignette strength, moody film grain,
  sky presets (Classic/Bright/Cinematic), moon size + moonshine.
- CONFIG MENU: 8 groups, 36 options (18 toggles + 16 sliders + 2 presets with
  6 preset values), all documented in shaders/lang/en_US.lang.
- Builder validates GLSL, properties/lang cross-references and repacks both
  shader zips automatically.
