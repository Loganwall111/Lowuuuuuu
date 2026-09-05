# MCSM Wither Storm — Phase 29–30 build

**Install:** put this jar in `mods/` for Minecraft 26.2 (Fabric, with
fabric-api, Sodium, Iris, cloth-config — the usual stack). Remove any older
`dabywitherstormmod` jar first. Everything needed (textures, shaders, mixins)
is embedded in the jar.

## Download

The jar is committed right in this repository (binary uploads to GitHub
Releases were blocked from the build environment, so the repo IS the store):

- **Direct jar link:** https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a06df7-lowuuuuuu/delivery/dabywitherstormmod-1.9.98-26.2-beta-mcsm.jar
- File page (Download button): https://github.com/Loganwall111/Lowuuuuuu/blob/arena/01a06df7-lowuuuuuu/delivery/dabywitherstormmod-1.9.98-26.2-beta-mcsm.jar
- Integrity list (sha256): https://github.com/Loganwall111/Lowuuuuuu/raw/arena/01a06df7-lowuuuuuu/delivery/sha256.txt

## Highlights in this build

- **Storm glare lives in the skybox, centred on the storm** — antipode bug
  fixed (it used to render at the mirrored point of the sky and clip through
  the storm). It is painted into the sky itself, follows the storm always, and
  is a touch larger than 1.9.95. A size slider follows in Phase 30.
- **Phase 5.5–5.9 palette** — deep purple with a tinge of blue for the glare;
  light-pink horizon → dark-pink zenith sky stops; no more orange halo (the
  wide post-pass halo is ~8× tighter and re-hued dark red / magenta).
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

Full phase log and the Phase 30+ roadmap live in `delivery/HANDOFF.md`.
