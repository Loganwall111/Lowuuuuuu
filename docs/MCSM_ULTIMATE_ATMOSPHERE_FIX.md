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
