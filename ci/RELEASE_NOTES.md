# MCSM Wither Storm — Phase 29–30 build

- **1.9.108 no-more-old-Java pass** — the CI build now hard-fails if the Java mixins do not compile, instead of publishing a shader-only jar with stale behavior. The Extras title shows the runtime build number, Force MCSM Look also forces the built-in Obsidian Gloss/OG skin, the legacy 1.18 glare default is migrated again, and direct hooks were added to `addSubGrowth()` and `die()` so phase-rise shockwaves and the death/supernova cinematic fire from the actual gameplay methods rather than relying only on tick polling.

- **1.9.103 halo correction** — reverted the new map-pin/heart silhouette back to a round, slightly oval halo. Rebuilt the radial halo gradient from the supplied reference images: phase 5.5–5.9 uses the measured blue core/navy falloff (#6A8FF7 → #627FE3 → #263165), while phase 4/5.3 uses the measured purple-black ramp (#3F255A → #2D1C41 → #140B1B).

- **1.9.107 atmosphere + config-scroll pass** — made the MCSM Extras panel scrollable, changed the default glare size to roughly half, tightened the storm halo again, made the black core more opaque, boosted 5.5-5.9 into dark-pink/purple instead of orange/over-purple, strengthened cold-biome aurora, made always-on cloud/tree-like ground shadows visibly move, and added shader-side emissive pop for torches/glowing blocks.

- **1.9.106 halo scale pass** — phase-5 storm halo is much smaller vertically, wider only around the storm sides, and phase-6 sun/glare bloom is heavily tightened/dimmed so it no longer fills the whole ceiling. Config changes now reset the MCSM gates so toggling options can re-apply without restarting.

- **1.9.105 visible/clickable config button + release** — the **MCSM Extras** button is now drawn by our own render injection and opens through our own mouse handler, so it no longer depends on Dabicco's custom child-widget renderer. The workflow now publishes a proper `mcsm-1.9.105` GitHub Release from this Arena branch so you do not have to hunt for old artifacts.

- **1.9.104 direct config access** — added a fixed **MCSM Extras** button to the bottom-left of Dabicco's config screen through `Screen.addWidget`, independent of the mod's folding row layout. This is meant to fix the dead/vanishing extras entry visible as an off-screen black rectangle on some GUI scales.

- **1.9.103 config/menu unblock** — made the MCSM Extras button open the real panel with a second fallback path, exposed the missing force-look/world/command-wire/instructions toggles inside that panel, and changed the gate code so one renamed upstream config field can no longer silently block the rest of the Story Mode visuals.

**Install:** put this jar in `mods/` for Minecraft 26.2 (Fabric, with
fabric-api, Sodium, Iris, cloth-config — the usual stack). Remove any older
`dabywitherstormmod` jar first. Everything needed (textures, shaders, mixins)
is embedded in the jar.

## Download

GitHub Actions uploads the built 1.9.108 jar as an artifact from this branch. SHA-256 is emitted next to the artifact and recorded in `out/BUILD_INFO.txt`.

## Highlights in this build

- **Storm glare lives in the skybox, centred on the storm** — antipode bug
  fixed (it used to render at the mirrored point of the sky and clip through
  the storm). It is painted into the sky itself, follows the storm always, and
  is a touch larger than 1.9.95. A size slider follows in Phase 30.
- **Phase 5.5–5.9 halo palette** — measured blue centre and navy falloff from
  the supplied reference; no orange, no map-pin silhouette.
- **Blue silhouette glow** around the storm; turquoise teeth now actually read
  as turquoise (white-core washout reduced 0.22 → 0.08).
- **Aurora borealis in the mod itself** — appears at night, strongest in cold
  biomes. (The Iris pack has its own independent aurora toggle.)
- **More vivid/contrasty world grade** (saturation 1.06→1.14, contrast
  1.04→1.08 — capped deliberately to protect the reference sky gradients).
- **Devourer body slightly more opaque** (semi-transparent texels lifted).
- Validated by the offline GLSL gate (42/42) in `glslcheck/shimcheck.py`.

Plus in this build: the death/rise sequence is wired to the storm methods directly. If Java mixins fail to compile, no release is published.

