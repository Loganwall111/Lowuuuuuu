STORY MODE VISUALS - a Minecraft: Story Mode style SHADER PACK
Minecraft 1.20.1, Forge / NeoForge / Fabric via Iris or Oculus
====================================================================

INSTALL
1. Copy this zip into .minecraft/shaderpacks (or the instance's shaderpacks folder).
2. Open Options > Video Settings > Shader Packs and select "Story Mode Visuals".
3. Options are available in Shader Pack Settings (presets, sliders, toggles).

WHAT IT DOES
- Seamless PROCEDURAL SKY DOME - no cube edges, no seams. Biome-tinted
  gradients melt into the fog. Story Mode sunset band, procedural moon with
  craters + halo, twinkling stars, sun/moon god rays.
- 14 BIOME FOG PROFILES crossfaded smoothly at biome borders:
  swamps get dense mossy mist, deserts get a golden heat-glare, snowy
  peaks/taiga and mountains get a crisp lavender fade, forests/plains get
  clean cyan horizons, plus cherry, jungle, badlands, savanna, ocean,
  mushroom and cave profiles.
- STORY MODE CLOUDS: blocky procedural noise clouds with a vertical alpha
  dissolve (soft bottoms, crisp tops) that drift with the wind and tint with
  the celestial clock - white at noon, pinkish-lavender at sunset, deep royal
  indigo at midnight.
- DYNAMIC SHADOWS: hard, flat, blocky sun/moon directional shadows plus
  moving cloud footprint shadows that sweep across the terrain, and SSAO
  contact shading where blocks meet the ground.
- TELLTALE INK OUTLINES: Sobel depth+normal edge detection drawing clean
  black comic lines around blocks, entities and held items.
- CINEMATIC GRADE: warm high-contrast Season-1 LUT, film grain, vignette
  (stronger at night/underground) and an optional letterbox toggle.
- BLOCKY FLUIDS: water and lava render as flat, saturated, opaque Story Mode
  planes with blocky sun glitter and drifting cloud shadows.
- EMISSIVE LIGHTING: saturated RGB light from torches, soul flames, lanterns,
  glowstone and lava (color profile from the bundled resource-pack module).

SHADER PACK SETTINGS (in-game)
- Art style preset: Story Mode / Vibrant / Moody
- Sunset intensity, cloud speed sliders
- Toggles: ink outlines, vignette, letterbox, SSAO, god rays, bloom,
  cel lighting bands, cloud shadows, terrain AO, torch tint

The zip also bundles the complete Story Mode Visuals RESOURCE PACK module
(assets/ with ForgeSkyboxes biome sky configs, emissive overlays, Colorful
Lighting emitters) - it stays active when the shader is switched off, so
the world never loses its Story Mode atmosphere.

Built by tools/build_story_mode_shader.py - full source of truth.

--- v4 CHANGELOG ---
- FIXED the cloud vertex shader compile error: the reference listing used an
  undeclared "BetterThirdPerson" identifier on the west face; it is now
  "BrightnessWest" (assets/minecraft/shaders/core/rendertype_clouds.vsh).
- FIXED a hidden gbuffers compile issue: contact AO no longer samples the
  depth buffer inside gbuffers passes (not readable there) and the shadow
  uniforms (shadowProjection / shadowModelView / shadowtex0) are now declared
  in every pass that looks up the shadow map.
- HUGE EXPANDED CONFIG MENU (36 options across 8 groups):
    Art Style preset (Story Mode / Vibrant / Moody) + master saturation
    Sky preset (Classic / Bright / Cinematic), sunset intensity, sky-fog
      blend, moon size, moonshine intensity, aurora toggle
    Clouds: speed, cover, density, color richness, overcast strength
    Biome fog strength (14 profiles)
    Terrain & shadows: dynamic shadows on/off, soft shadows, flat Story Mode
      lighting, cel bands, cloud shadow footprints, terrain AO + strength,
      entity soft shadows, entity AO, torch tint + saturation, desert heat
      shimmer
    Water: shore foam toggle
    Hand: held-item light boost
    Post: ink outlines, SSAO, god rays, bloom, film grain, vignette + strength,
      cinematic letterbox
- New features wired to the menu:
    * Flat Story Mode lighting mode (pure lightmap, no directional shading)
    * Soft shadow option (9-tap PCF) vs. hard blocky shadows
    * Aurora borealis ribbons in snowy/taiga biomes at night
    * Milky way band that rotates with the celestial clock
    * Desert/badlands heat shimmer (golden UV distortion)
    * Stylized biome-tinted shore foam on water
    * Cloud cover / density / color-richness controls
    * Entity contact AO + hand light boost
    * Vignette strength + moody film grain boost
