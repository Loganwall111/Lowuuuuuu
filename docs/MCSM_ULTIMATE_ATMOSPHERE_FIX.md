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

## v5 — Oculus 1.8.0 / Embeddium boot fix (real crash repair)

User log diagnosis: Oculus 1.8.0 reported `ShaderCompileException` in
composite.fsh ("depthtex0/gbufferProjectionInverse/cameraPosition undeclared")
and fell back, which is why the settings menu disappeared. Repairs:

- **Self-contained shipped GLSL**: the builder now inlines every `#include`
  into the shaders inside the zip (zero include processing at load time).
- **All uniforms declared in-file** in every program; risky loader-dependent
  uniforms removed (moonPhase -> procedural 8-day cycle, aspectRatio ->
  viewWidth/viewHeight, biome int removed).
- **Legacy gbuffer layout fixed**: every gbuffers program writes
  color/gl_FragData[0], depth/gl_FragData[1], normals/gl_FragData[2] with
  explicit `/* DRAWBUFFERS:012 */` (previously normals overwrote the depth
  slot, corrupting SSAO/outlines/fog).
- **gbuffers_block.fsh** include bug fixed (called getContactAO without the
  library include - would have been the next compile crash).
- **shader.properties**: colon buffer syntax + colortex2 registered; menu
  identity token `story_mode_menu` documented next to the settings screen.
- Builder validator now checks: #version 120 first, brace/ifdef balance,
  no GLSL-150 `texture()`, every risky uniform used is declared, every
  helper used is defined, DRAWBUFFERS covers all written indices.
- The Embeddium "Id must be specified in OptionPage 'Shader Packs...'"
  warning is Oculus's own options-page integration and cannot be set from
  a shader pack; it is benign (documented in the pack README too).

## v7 - Strict-driver link hardening (Intel UHD) + fully lowercase tree
- `composite.fsh`: the four mandatory uniforms (depthtex0, colortex1,
  gbufferProjectionInverse, cameraPosition) now sit at the VERY TOP of the
  file (lines 3-6, right after `#version 120`); they are redeclared
  identically in the main uniform block (legal GLSL 1.20, BSL/SEUS trick).
- `composite.fsh` line 126 is now a proper 3-component vec3 assignment:
  `vec3 rays = vec3(0.0, 0.0, 0.0);` - and EVERY single-argument vec
  constructor across the whole pipeline was rewritten in explicit component
  form (god rays, bloom, ACES, LUT grade, style presets, sky presets, cloud
  colorize, torch tint, water glitter, shadow clamp, fog accumulator).
- `shader.properties`: added `iris.patch.colorful_lighting=true`
  (COLORFUL LIGHTING BRIDGE marker - the pack's native colored lighting
  runs through Oculus; the Sodium-only Colorful Lighting mod itself can
  never load under Forge and is not needed).
- The whole shader zip is now a 100% lowercase file tree (`en_US.lang` ->
  `en_us.lang`, which Oculus's LanguageMap handles identically, and
  `README.txt` -> `readme.txt`); the builder now asserts this on every build.

## v8 - Sky/cloud layer decoupling (Oculus mixin cancellation fix)
- Deleted the pack's sky programs (`gbuffers_skybasic.vsh/.fsh`,
  `gbuffers_skytextured.vsh/.fsh`). Oculus only generates its internal
  `shaders/core/sky_basic.json` wrapper - and only fires its native-sky
  cancellation mixin (`m_166612_`) - when a pack defines a sky program.
  With none registered, the ChainedJsonException path is unreachable and
  the sky renders natively (still graded/fogged/bloomed by composite).
  `gbuffers_skytextured` had to go with it: its sole purpose was
  discarding textured sky elements under the custom dome, which would
  have hidden the native sun/moon.
- Clouds are now strictly pipeline-mapped: `shaders/gbuffers_clouds.vsh`
  + `gbuffers_clouds.fsh` only. The vanilla-layer cloud overrides
  (`assets/minecraft/shaders/core/rendertype_clouds.vsh`,
  `position_tex_color_normal.vsh/.fsh`) are excluded from the shader zip;
  the verbatim user cloud GLSL is preserved at
  `assets/mcsm_atmosphere/clouds_reference/rendertype_clouds.vsh`.
- `shader.properties`: locked lowercase menu wrapper tag
  `id=story_mode_menu` for Embeddium's pagination filters; sky programs
  removed from the `shaders=` registration list.
- Builder now permanently asserts: no sky program entries in the zip,
  clouds pair present, no active cloud overrides, verbatim reference
  present, menu id tag present.

## v9 - Legacy resource-pack folder trail removed
- Deleted the entire `assets/minecraft/shaders/` subtree from the shader
  pack (the `rendertype_solid.vsh/.fsh` trail). Those vanilla core-shader
  overrides belong to the resource-pack module (shader-off mode) and are
  kept there unchanged; inside a shader pack they are a legacy
  resource-pack folder that fights the mod loader over environment hooks.
- Confirmed the files named in the user's log do not exist in the pack:
  `shaders/core/sky_basic.json`, `sky_basic.vsh`, `sky_basic.fsh` (the
  pack's sky programs were `gbuffers_skybasic.*`/`gbuffers_skytextured.*`
  and were deleted in v8). The `sky_basic.json` in the error is an
  internal wrapper Oculus generates while compiling a program.
- Ground truth: the crash is a documented Oculus x Embeddium mixin
  incompatibility (`the call m_166612_ is not cancellable`), reproduced
  upstream with unrelated packs and with the exact Oculus 1.8.0 +
  Embeddium 0.3.31 pair (Asek3/Oculus #764, open; also #670 with
  Oculus 1.7.0 + Embeddium 0.3.27; and an Iris-side report with
  Chocapic13 v8). The durable fix is the mod pair / Java version, not the
  pack - but v8+v9 remove every sky/vanilla hook surface pack-side.
- Verbatim cloud GLSL moved to pack-root `clouds_reference/` (outside
  `assets/` - invisible to the resource manager). Live clouds remain fully
  self-contained in `shaders/gbuffers_clouds.vsh/.fsh` (zero includes
  after build, zero samplers). Builder now asserts all of the above.
