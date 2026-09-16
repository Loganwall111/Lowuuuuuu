# 7000.0.0-M — Build #416: the dome is gone, the sky actually fades, one traced palette everywhere

This build deletes the "sky with a top on it" and replaces it with a sky that is
painted as a continuous fade, then makes every other storm visual read its
colours from the same three reference sheets. Nothing in this build is a dome,
a cap, a card or a hard-edged oval.

## 1. The sky is a fade again, not a dome

- **New program: `core/sky.vsh` / `core/sky.fsh`.** The sky is drawn from the
  view ray, not from geometry: colour is a function of the ray's elevation, so
  there is no top to hit, no silhouette and no clipped edge -- look up, look
  down, fly through it, and it is still just a gradient. Six stop columns are
  sampled per pixel and cross-faded by phase, then folded into the live fog
  colour at the horizon so no seam can open between sky and ground.
- **The dark disc is off.** `McsmNativeSkyRenderer` no longer asks the platform
  for the dark cap (`shouldRenderDarkDisc = false`); it pins the sky colour to
  the phase's own horizon stop and suppresses the stars from phase 5.0 on. That
  flat disc the screenshots showed as a lid over the world is what this removes.
- **The horizon stops come from the sheets**: teal `"phase 5 turquoise sky"` for
  5.0-5.1, `"phase5sky0purple sky"` for 5.5-5.9, `"phase6sky 6 witherstorm"` for
  6.0-7.0, then the storyboard's ember tail to 8.05.

## 2. One traced palette, checked by a tool

- **New `ci/palette_tables.py`** traces the stop tables ONCE and hands them to
  everything: the sky program, the position program, the Java that colours the
  halo, and every baked texture. It also *verifies* them -- a drift of more than
  1e-4 between the GLSL, the Java and the storm-visual constants fails the build
  instead of reaching the player as a sky and a halo that disagree. It caught
  two real drifts the moment it was first run.
- **`mcsm_visuals.glsl`'s storm palette is now derived**, not hand-picked: the
  mass's core/mid/edge/bleed are samples of the traced columns (zenith x0.22,
  t=0.30, t=0.60, t=0.85) and the phase-6 horizon glow is the *orange* stop out
  of the ember trace. `ci/palette_tables.py` re-derives all eleven and fails the
  build if any hex stops matching its source.

## 2b. The trace is a tool, and the clouds come out of it too

- **`ci/trace_sky_sheets.py`** is the "extract it with a tool" half of the brief.
  Give it the three delivered sheets (drop them in `ci/sky_sheets/`, `uploads/`,
  or `/home/user/uploads/`, or pass `--dir`) and it samples each one down its
  centre band into the six-stop column the shaders use, rewrites **every**
  consumer in place -- `sky.fsh`, `position.fsh`, `McsmStormPhase.java`, and the
  derived constants in `mcsm_visuals.glsl` -- then re-runs the parity gate. It
  decodes PNGs itself (all five filter types, no Pillow) and `--self-test`
  round-trips a synthetic sheet through the whole path, so the trace maths is
  verified before it ever touches a real sheet. `--verify` re-traces and FAILS
  if the shipped tables disagree with checked-in sheets (it reports a skip, not
  a pass, when the sheets are absent).
- **The cloud deck is now part of the palette.** The deck used to be tinted with
  hand-picked hexes, which is why it never matched the sky. It is now four
  constants derived the same way as everything else -- the traced column's mid
  row lifted toward white by a per-phase amount -- so the pale puffs under the
  turquoise sky, the dark violet banks under the purple sky and the warm mauve
  under the rose sky all come off the same sheets the sky does, and cross-fade
  on exactly the same phase ramps.

## 3. Backdrops re-baked to those palettes

- The six sky-plane sheets are regenerated from the traced tables -- purple
  canvas, teal and salmon cloud filters, and the three phase decks. The
  phase-6 sheet now carries the **black-smudge mass**: a near-black core ringed
  with purple/magenta, then pink, then the salmon/orange fringe, dissolving into
  the sheet's own column colour so it has no border of its own. The mass outline
  is a noisy field (a broad lobe plus a quantized shred), which is what keeps it
  reading as soot rather than as a starburst.

## 4. The halo, rebuilt on the glowing-teeth shader

- `McsmHaloSkyRenderer` no longer builds a curved cap. It lays **40 camera-facing
  copies of the storm's own additive glow quad** (`core/storm_glow.fsh` -- the
  one shader whose falloff is per pixel) around the storm's head: a real ring of
  thrown light with per-pixel softness, a wider inner hole than the head so the
  mouth stays visible, and a world-space tether to the storm's tracked position,
  so the halo travels with the creature exactly like the backdrop sheets do.
- Its colour is `McsmStormPhase.columnFor(phase, t)` -- the same traced tables
  again, so a halo pixel and the sky behind it are the same colour by
  construction.
- **The teeth glow is phase-exact now.** `core/fogless_entity.fsh` reads the
  phase straight out of the `FogSkyEnd` carrier it already binds (no new
  uniform -- a slot the bind group lacks is a hard Vulkan crash) and picks the
  mouth band from it, keeping the vertex-colour hue only as a fallback. The two
  storm-bound shaders that cannot take that carrier carry an explicit
  `MCSM_PHASE_SOURCE:` annotation naming where their phase comes from.

## 4b. TEETH ARE WHITE; THE AURA CARRIES THE PHASE COLOUR

This is the correction that mattered most, and it is a real behaviour change:

| phase | teeth | aura |
|-------|-------|------|
| 4 | white | bluish `#8CCCFF` |
| 5 | pure white (brightest) | pure white `#FFFFFF` |
| 5.2 - 5.9 | glowing white | bluish `#80C7FF` |
| 6 | white | pure blue `#386BFF` |
| 7 | white | toxic green `#53D977` |
| 8 | pure white | blue `#5994FF` |

Every previous build painted the teeth THEMSELVES cyan / blue / green, which is
why the mouths never matched the reference frames. Now:

- `McsmTeethPhaseTint` carries **two** tracks: the TEETH track (white at every
  storm phase, fed to `eyeColorR/G/B` -> the model's emissive layers) and the
  AURA track, cross-faded on the user's own boundaries (white is reached exactly
  at 5.0 and holds to 5.15; the bluish aura is back by 5.25; blue at 6.0; green
  at 7.0; blue at 8.0).
- `mcsm_visuals.glsl` gained `mcsm_teeth_color()` / `mcsm_aura_color()`, and
  `mcsm_mouth_emissive()` splits them **per pixel by luminance**: a bright tooth
  pixel goes white, the dimmer emissive skirt around it takes the aura -- which
  is exactly what "white teeth with a bluish aura" looks like in the frames.
- `storm_glow.fsh` -- the additive pool drawn AROUND the mouths -- now reads the
  phase from the `FogSkyEnd` carrier (safe there: that pipeline is built from the
  same entity-emissive snippet as `fogless_entity`, which already binds the fog
  block, so the import adds no uniform) and paints the aura at 0.88 strength.
- `McsmStormBlob`'s mouth detail: the dashed teeth are hard white, the inner
  mouth square takes the aura, and the magenta emitter cube is untouched.
- The sky's horizon air is lit by the aura too, so phase 6 / 7 / 8 horizons read
  blue / green / blue.
- `ci/check_phase_uniform.py` grew twelve checkpoints for this (34/34 now):
  the aura ramp must exist in the include and in each storm-bound program, and
  **no shader may multiply the teeth by a phase colour** any more.

## 5. Wiring, so none of this is inert

- **`core/position.vsh` / `core/position.fsh` are authored files now.** The
  build used to fabricate them by copying `block.*`, so every change made to the
  position pass was silently discarded. The copy is gone; the pipeline fails if
  the authored pair is missing, and the jar audit now **fails the build if
  `position.*` is byte-identical to `block.*`** or if the sky branch
  (`MCSM_SKY_POSITION`) has been lost. The default path stays block-safe, so no
  platform can end up with world geometry painted as sky.
- **Two new hard gates.** `ci/check_phase_uniform.py` proves the phase feed:
  one canonical accessor, no phase uniform declared in any core shader (the
  crash guard), every storm-aware module resolving it, the Iris
  `witherstorm_Phase` uniform declared, and the Java side publishing it. 22/22.
  `ci/palette_tables.py` adds the palette parity gate above. Both run before
  javac.
