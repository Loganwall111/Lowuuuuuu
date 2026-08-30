MCSM_TrueCoreVisuals — Standalone Resource Pack
================================================
Mod: DabyWitherStormMod  v1.9.60-26.2-beta
Minecraft: 26.2 (unobfuscated)  |  Fabric Loader: 0.19.3

This resource pack bundles the original custom visual assets that drive the
mod's signature atmospheric rendering, extracted as a standalone deployment
package separate from the mod JAR (build bloat fix, commit fdee9e8).

CONTENTS
--------
assets/dabywitherstormmod/
  textures/misc/
    mcsm_cloud.png     — 3D blocky MCSM-style cloud slab texture used by
                         StormCloudDeck to orbit the storm mass at all phases.
    halo_ring.png      — Volumetric shield/halo ring drawn by StormPresenceFX
                         at phase 5.8+ (cataclysm halos, blue-purple glow).
    star.png           — Lavender starfield star sprite (StormStarfield).

  textures/entity/
    tractor_beam.png   — Soft glow quad texture used for the atmospheric pulse
                         (StormPresenceFX) and tractor beam rendering.

  shaders/core/
    storm_glow.fsh          — Fragment shader for entity emissive glow pass.
    fogless_entity.fsh/vsh  — Fog-suppression shaders for storm body rendering.

  shaders/post/
    storm_bloom_*.fsh  — Multi-pass bloom pipeline (extract, blur, combine,
                         add) driving the HDR halo bloom.
    storm_hdr_*.fsh    — HDR composite + mask shaders for sky tone-mapping.
    storm_sun_glow.fsh — Sun glow / lavender sky haze post-pass.
    storm_impact_light.fsh — Impact flash lighting shader.
    storm_shadow.fsh   — Shadow projection fragment shader.

  post_effect/
    storm_bloom*.json  — Post-effect chain descriptors (Minecraft 26.x format).

INSTALLATION
------------
Place MCSM_TrueCoreVisuals.zip in your .minecraft/resourcepacks/ folder and
enable it in Options → Resource Packs. The mod JAR must also be installed.

BUILD NOTE
----------
These assets were previously bundled inside the fat JAR (causing a ~155 MB
bloated output). The fix in build.gradle (implementation → modImplementation
for fabric-loader, ref commit fdee9e8) reduces the JAR to ~36 MB and
eliminates the Fabric Loader black-screen timeout on client boot.
