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

--- v5 CHANGELOG (Oculus 1.8.0 / Embeddium boot fix) ---
- FIXED pipeline compile failures reported on Oculus 1.8.0 + Embeddium:
  * Every shader now ships SELF-CONTAINED: all #include files are inlined
    at build time, so no loader include-handling quirks can break the pack.
  * Every uniform used in a program is DECLARED in that same file
    (no reliance on loader auto-injection).
  * All gbuffer passes now write the legacy buffer layout explicitly with
    "/* DRAWBUFFERS:012 */": color -> gcolor, depth -> gdepth, normals ->
    gnormal. (Previously normals were written into the depth slot, which
    corrupted depth-based effects.)
  * gbuffers_block.fsh now includes the contact-AO library it calls
    (undeclared-function crash removed).
  * Removed loader-risky uniforms: moonPhase (now computed procedurally),
    aspectRatio (now derived from view size), biome (int).
  * shader.properties: shadow buffers now use the OptiFine colon syntax
    (buffers=shadow:shadowcolor0:shadowcolor1) and colortex2 (gnormal) is
    registered in the textures list.
- The 36-option menu (18 toggles + 16 sliders + 2 presets), aurora toggle,
  biome fogs, Story Mode clouds, ink outlines etc. are all preserved.

v7 CHANGELOG - STRICT-DRIVER LINK HARDENING (Intel UHD)
- shaders/composite.fsh: the four mandatory pipeline uniforms
  (depthtex0, colortex1, gbufferProjectionInverse, cameraPosition) are now
  declared at the VERY TOP of the file, right after #version 120, so every
  strict driver identifies them immediately (they are also redeclared,
  identically, in the main uniform block - legal GLSL 1.20, same trick as
  BSL/SEUS).
- shaders/composite.fsh line 126 is now a proper three-component vec3
  assignment:  vec3 rays = vec3(0.0, 0.0, 0.0);  (no single-float vec3
  math remains anywhere).
- EVERY single-argument vec2/vec3/vec4 constructor across the whole
  pipeline (god rays, bloom, ACES tonemap, LUT grade, style presets,
  sky presets, cloud colorize, torch tint, water glitter, shadow clamp,
  fog accumulator) was rewritten in explicit component form to remove
  the last float->vec3 implicit-replication edge case.
- shaders/shader.properties: added iris.patch.colorful_lighting=true
  (COLORFUL LIGHTING BRIDGE). It marks the pack's native colored-lighting
  path (DEFINE.TORCH_TINT + SETTINGS.TORCH_SAT + OptiFine-format emissive
  overlays) as active through Oculus, so nothing ever depends on the
  Colorful Lighting mod's auto-patcher script. Note: the Colorful Lighting
  MOD itself is Sodium-only and cannot run under Forge - the pack carries
  its own colored-lighting implementation, which is what this key routes.

v8 CHANGELOG - SKY / CLOUD LAYER DECOUPLING (mixin cancellation fix)
- The pack's sky programs are DELETED from shaders/: gbuffers_skybasic and
  gbuffers_skytextured (.vsh + .fsh). Oculus was generating its internal
  shaders/core/sky_basic.json wrapper for our skybasic pass, and its
  native-sky mixin hook then crashed with
  'ChainedJsonException: Invalid shaders/core/sky_basic.json: The call
  m_166612_ is not cancellable' under the installed Embeddium/Oculus
  pairing. With no sky program registered, Oculus never generates that
  JSON and never cancels the native sky call: the sky now renders through
  Oculus's NATIVE path (vanilla dome, sun, moon) and still receives the
  pack's composite grading, per-biome fog, god rays and bloom. (Vanilla
  sun/moon needed skytextured removed too - its only job was discarding
  textured sky elements under the custom dome, which would have hidden
  the native sun/moon.)
- Clouds now render STRICTLY through the main pipeline:
  shaders/gbuffers_clouds.vsh + shaders/gbuffers_clouds.fsh (procedural
  blocky cells, vertical dissolve, celestial color clock, biome fog tint,
  rain overcast, all CLOUD_* settings). The vanilla core-shader overrides
  that touched clouds (assets/minecraft/shaders/core/rendertype_clouds.vsh
  and position_tex_color_normal.vsh/.fsh) are no longer shipped in the
  shader zip - no vanilla JSON layer is involved in cloud rendering. The
  user's verbatim cloud GLSL listing is preserved at
  assets/mcsm_atmosphere/clouds_reference/rendertype_clouds.vsh
  (inert path - nothing loads it).
- shader.properties: the menu wrapper id is locked as a clean lowercase
  system tag for Embeddium's pagination filters:
    id=story_mode_menu
- Note: SKY_PRESET and MOON_SIZE settings are temporarily inert (they fed
  the old sky program); MOONSHINE still drives composite moonlight.

v9 CHANGELOG - LEGACY RESOURCE-PACK FOLDER TRAIL REMOVED
- The shader pack no longer ships ANY vanilla core-shader overrides:
  the whole assets/minecraft/shaders/ subtree (rendertype_solid.vsh/.fsh
  trail) is deleted from the shader zip. That trail belonged to the
  resource pack (shader-off mode) and, inside a shader pack, it becomes
  a legacy resource-pack folder fighting the mod loader over environment
  hooks. The standalone resource packs keep it unchanged.
- The pack's sky programs were already deleted in v8 (gbuffers_skybasic /
  gbuffers_skytextured .vsh + .fsh), and files named sky_basic.json /
  sky_basic.vsh / sky_basic.fsh never existed in this pack: the
  'shaders/core/sky_basic.json' named in the crash log is an INTERNAL
  json wrapper that Oculus generates while compiling a shader program -
  it is not a file inside the pack.
- IMPORTANT - the actual crash is a documented OCULUS x EMBEDDIUM MIXIN
  INCOMPATIBILITY, not a pack bug: the same error
  ('ChainedJsonException: ... the call m_166612_ is not cancellable')
  with different blamed program names is reported upstream with several
  unrelated packs, including the exact version pair this pack was tested
  against (MC 1.20.1, Oculus 1.8.0, Embeddium 0.3.31 - Asek3/Oculus
  issue #764, still open). The blamed json is simply whichever program
  Oculus is compiling when its mixin injection fails. Pack-side, all
  sky/vanilla-layer hooks are now removed so nothing routes through the
  patched path, but the durable fix is the mod pair: try a different
  Embeddium build (e.g. 0.3.21) or a matched Oculus/Embeddium pair, and
  Java 17 (upstream reports used Java 21/22).
- The user's verbatim cloud GLSL now lives at pack-root
  clouds_reference/rendertype_clouds.vsh - OUTSIDE assets/, so the
  resource manager never loads it (zero hook surface).
- Live clouds remain 100% self-contained in shaders/gbuffers_clouds.vsh
  + gbuffers_clouds.fsh: zero #include after build, zero texture
  samplers, no vanilla asset dependencies.

TROUBLESHOOTING
- "Id must be specified in OptionPage 'Shader Packs...'" in the log:
  this warning comes from the OCULUS mod's own options-page button inside
  Embeddium's video settings (Oculus creates that page without an id).
  It cannot be defined from inside a shader pack, it is harmless, and it
  is unrelated to shader loading. The pack's settings menu (Shader Pack
  Settings) appears once the pipeline compiles.
- If a pack named "is not valid": delete every old copy of the zip from
  .minecraft/shaderpacks, re-download a fresh one, and press F3+R (or
  /reloadShaders) after selecting it.
- If the settings screen is missing: the shader failed to compile and
  Oculus fell back - fix the compile issue (this build has none) and the
  menu returns.
- 'ChainedJsonException: Invalid shaders/core/*.json: The call m_166612_
  is not cancellable': Oculus x Embeddium mixin incompatibility,
  reproduced upstream with several unrelated packs and with the exact
  Oculus 1.8.0 + Embeddium 0.3.31 pair (Asek3/Oculus #764, open). The
  json named in the log is an internal wrapper Oculus generates while
  compiling a program - it is NOT a file in the pack. v8 removed all
  sky programs and v9 removed the last core-shader trail so nothing
  routes through the patched path; if the error still appears, switch
  the MOD pair (different Embeddium build, e.g. 0.3.21, or a matched
  Oculus/Embeddium combo) and use Java 17 - upstream reports all ran
  Java 21/22.
