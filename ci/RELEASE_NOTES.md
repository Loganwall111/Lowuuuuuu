# MCSM Wither Storm — Phase 29–30 build

- **1.9.103 halo correction** — reverted the new map-pin/heart silhouette back to a round, slightly oval halo. Rebuilt the radial halo gradient from the supplied reference images: phase 5.5–5.9 uses the measured blue core/navy falloff (#6A8FF7 → #627FE3 → #263165), while phase 4/5.3 uses the measured purple-black ramp (#3F255A → #2D1C41 → #140B1B).

- **1.9.103 config/menu unblock** — made the MCSM Extras button open the real panel with a second fallback path, exposed the missing force-look/world/command-wire/instructions toggles inside that panel, and changed the gate code so one renamed upstream config field can no longer silently block the rest of the Story Mode visuals.

**Install:** put this jar in `mods/` for Minecraft 26.2 (Fabric, with
fabric-api, Sodium, Iris, cloth-config — the usual stack). Remove any older
`dabywitherstormmod` jar first. Everything needed (textures, shaders, mixins)
is embedded in the jar.

## Download

GitHub Actions uploads the built 1.9.103 jar as an artifact from this branch. SHA-256 is emitted next to the artifact and recorded in `out/BUILD_INFO.txt`.

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

Plus in this build: **the death-sequence engine** (distortion, white cracks,
shaking whitening implosion, in-rushing motes, flash, six-colour supernova
rings, settling dust) — dormant until the Java driver (source already in
`mcsm-extras/java`, compiles via `ci/build.ps1` or GitHub Actions) stamps the
carrier. Glare size is slider-ready (same carrier). Dormant = invisible until
activated; nothing changes in normal play.

