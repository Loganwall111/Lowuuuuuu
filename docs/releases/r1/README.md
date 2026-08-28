# MCSM Visuals — Release r1 (v1.9.61-26.2)

Download all three from the GitHub UI (open each file, then *Download raw file*),
or pull this folder's contents.

| File | SHA-256 |
| :--- | :--- |
| `dabywitherstormmod-1.9.61-26.2-beta-r1.jar` | `4763dcdbdb144d615eab025bb7e7c5b6974624186f715f09e5f9684df8ca18c3` |
| `MCSM_ResourcePack.zip` | `ea9afc5a0853b995f88519369b6227c986001f2b8aa046c31ac7c2a1b062386c` |
| `MCSM_ShaderPack.zip` | `16763152ee75c670244388dbd1410e85a4fb49875d7fe24353fc1f7fca0de556` |

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
