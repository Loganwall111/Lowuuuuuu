# MCSM Visuals — Release r1 (v1.9.61-26.2)

Download all three from the GitHub UI (open each file, then *Download raw file*),
or pull this folder's contents.

| File | SHA-256 |
| :--- | :--- |
| `dabywitherstormmod-1.9.61-26.2-beta-r1.jar` | `8ade143f77dcbf392ed65ee422edba382a43ec93e63b9b3c4caa2aa1413e8fa2` |
| `MCSM_ResourcePack.zip` | `f72f1ab6c0434958fd24b45f127215d573b962dc4916670a77cf470432bcdab0` |
| `MCSM_ShaderPack.zip` | `4631616129eaf05363044dbd3750f5951d61252b84399a605f5f35b4c8707ba6` |

## Install
1. `dabywitherstormmod-1.9.61-26.2-beta-r1.jar` → `.minecraft/mods/`
2. `MCSM_ResourcePack.zip` → `.minecraft/resourcepacks/` (enable it)
3. `MCSM_ShaderPack.zip` → `.minecraft/shaderpacks/` (enable it in Iris / OptiFine)

## What's in r1
- **Procedural GLSL clouds** — no PNG cloud sheets; fractal-noise clouds with the
  2.5x extrusion and live time-of-day colour (the author's `rendertype_clouds.vsh`
  is bundled in the resource pack and the mod jar).
- **Restored lavender→orange skybox** clock loop (mod + resource pack custom skies;
  green at phase 4.5, turquoise at 5+, purple-black cataclysm).
- **Sun-cast shadows on the ground and on water** that sweep with the day/night
  cycle (new shadow map + water program in the shader pack).
- **Storm atmosphere post-effect** (`post_effect/storm_atmosphere.json`) — true
  full-screen purple→dark-magenta fog pass; no solid shells or texture walls.
- **New shader-style phase FX** — light-blue centre halo (4+), colour-shifting
  centre blob (5.1→5.9), heavy rear fog cloud (5.1+), 2-minute flash above the
  storm (6+), Vortex model mesh on top (7/8).
- **Shaded OG Story Mode textures** in the default preset + `_e` emissive pairs
  for the turquoise teeth aura.
