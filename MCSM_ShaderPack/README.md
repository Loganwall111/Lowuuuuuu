# MCSM Shader Pack — Minecraft: Story Mode Atmosphere

Standalone Iris / OptiFine shader pack for the **Wither Storm Mod (Minecraft 26.2)**
and the standalone **MCSM Resource Pack**.

## What this pack does

- **Lavender-to-orange Story Mode sky** — `gbuffers_skybasic` paints the exact
  MCSM sky: lavender/periwinkle zenith with a warm orange horizon at
  day/sunset, royal-violet magenta at night. The gradient is sampled from the
  live `worldTime` clock (with a `sunAngle` fallback so Sodium can never freeze
  it at tick 0).
- **100% procedural GLSL clouds** — the old 8 PNG cloud sheets are gone. Cloud
  slabs are generated entirely with fractal value-noise (`gbuffers_clouds.fsh`,
  `rendertype_clouds.fsh`), extruded 2.5x in the vertex shaders, and their
  colour shifts with the time of day (white day → coral sunset → periwinkle
  night). No `sampler2D` cloud textures anywhere in the pack.
- **Sun-cast shadows on the ground and on water** — a real shadow map
  (`shadow.vsh`/`shadow.fsh`) is rendered from the sun every frame; terrain
  (`gbuffers_terrain.fsh`) and water (`gbuffers_water.fsh`) sample it, so the
  shadows sweep the world as the sun travels the day/night cycle.
- **Turquoise teeth aura** — `gbuffers_entities.fsh` detects the Wither
  Storm's turquoise pixels and drives them to a bright, pulsing emissive cyan
  (`#00E5FF`), with magenta accents.
- **Held-item transparency fix** — `gbuffers_hand*` discard transparent texels
  so held tools no longer render as solid black boxes.
- **Story Mode lighting** — warm sunlight, lavender ambient shadows and amber
  torchlight (`gbuffers_terrain.fsh`).

## Install

1. Drop `MCSM_ShaderPack.zip` into `.minecraft/shaderpacks/` and enable it in
   Video Settings → Shader Packs (Iris or OptiFine).
2. Recommended together with the MCSM Resource Pack and the Wither Storm Mod.
3. Shader Options → `MCSM_OPTIONS` exposes: CLOUD_EXTRUSION, CLOUDS_ACTIVE,
   DYNAMIC_SKY, MCSM_LIGHTING, EMISSIVE_TEETH_GLOW.
