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
## 4c. THE EYES KEEP THEIR VIOLET

Answering "is there a purple glow on the eyes too": **there is now, and there
was not before this fix.** `McsmExactGlowTintMixin` overrides
`WitherStormHeadRenderer.eyeTint()` -- the tint the base mod derives from the
beam colour, i.e. the show's violet -- and it was pointing at the TEETH track.
The moment the teeth were corrected to white, the eye lenses and every eye
bloom layer went white with them and the violet glow disappeared.

- New EYE track in `McsmTeethPhaseTint`, separate from the teeth on purpose:
  violet `#9E6BFF` at 4, pale violet at 5, magenta `#D63DFF` from 5.2 to 5.9,
  bluish violet `#7A6BFF` at 6, magenta at 7, blue violet at 8. `eyeTintArgb()`
  now returns THAT, lifted to full brightness the way the base renderer lifts
  the beam colour.
- The magenta pupil / emitter cube is now single-sourced as
  `PUPIL_R/G/B` instead of being typed twice, and our own emitter draws the
  phase's eye glow around it.
- **The emissive snap no longer bleaches eyes.** `mcsm_mouth_emissive()` and
  `fogless_entity.fsh` now skip the teeth-band snap for emitters that are
  already saturated and mid-bright (`eyeLike`) -- that is the eye lens, and its
  violet is the whole point. Teeth are white/grey, so their saturation is
  ~zero and they are always caught by the snap. The 4.0x gain still applies to
  the eye: it is emitted light, only the hue snap is skipped.

`ci/check_phase_uniform.py` verifies all of this (40/40 now) -- including a
symbol check that every `McsmTeethPhaseTint.*` call site resolves to a real
declaration, which is exactly how it caught a missing `PUPIL_*` constant.

- `ci/check_phase_uniform.py` grew twelve checkpoints for this (the checkpoint count is 40/40 now):
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

## 6. Light-adaptive body shading, and the sky's true hexes (build #416, 7000.0.0-M)

### The supplied hexes are now the source of truth

The nine anchors supplied with this build (ceiling / middle / horizon per sheet)
are ingested in `ci/palette_tables.py` as `SHEET_HEX` and expanded to the six-stop
columns the shaders read:

| sheet | ceiling | middle | horizon | expansion |
|---|---|---|---|---|
| `phase 5 turquoise sky.png` | `#0C1216` | `#172228` | `#202E34` | `#0C1216 #10181D #151F24 #19242A #1C292F #202E34` |
| `phase5sky0purple sky.png` | `#160A21` | `#3A184E` | `#5A2474` | `#160A21 #241033 #331545 #401A56 #4D1F65 #5A2474` |
| `phase6sky 6 witherstorm.png` | `#1D1519` | `#422D37` | `#644354` | `#1D1519 #2C1F25 #3B2831 #49313D #563A48 #644354` |

`python3 ci/trace_sky_sheets.py --from-hex --apply` writes them into `sky.fsh`,
`position.fsh`, `McsmStormPhase.java` and the derived constants in
`mcsm_visuals.glsl`; the older trace drifted by **0.4984 / 0.3373 / 0.4212** and is
gone. The twelve backdrop sheets were re-baked from the new columns, and
`ci/REPORT_sky_columns_shipped.png` was regenerated (it had still been showing the
superseded bright sky).

### Two real defects fixed on the way

- **The whole body-shading block was dead code.** `MCSM_VOID_BODY` was defined by
  *no pipeline in the mod* — so the void-black creases, the structural band, the
  glint sheen and the new light-adaptive plates would never have run in game
  (only `glslcheck` ever passed that define). The body cutout pipelines now stamp
  it, and `shimcheck` grew three combinations for the paths that actually run
  (**93/93 -> 147/147**).
- **The table rewriter corrupted its own output.** Two earlier versions of
  `patch()` left a doubled `);` (a GLSL syntax error) and could eat a *second*
  array declaration while looking for the first one's terminator. It is now
  index-based, self-heals duplicated terminators, and refuses to write unless the
  declaration survives exactly once. `--from-hex --check` was also a fake gate —
  it printed a site count that could never fail; it now dry-runs every rewrite and
  exits non-zero on real drift, and `ci/build.sh` enforces it.

### The shading itself

Four tone keys, all read from the ingested atlas or supplied by the brief:

| role | hex | where |
|---|---|---|
| night / navy key | `#0A0E14` | ambient light low (night, cave) |
| twilight blue | `#0D1B2A` | cool light (overcast, blue hour) |
| ash gray | `#242A36` | warm light (day, sunset, lightning) |
| void | `#000000` | crevices, structural joints, deep AO — permanent |

The masks are computed from the **unlit albedo**, captured before
`lightMapColor` multiplies in: keyed off the lit colour, midnight would have
dragged every pixel under the crease floor and crushed the whole hull to void
black. The ambient term is the platform lightmap scaled by the world clock using
the *same sun maths as the sky shader* — the storm head is submitted full-bright,
so the lightmap alone would have read "noon" at midnight, and the hull could never
have taken its night key. Measured on lit faces: midnight `#0E1218` (torch-lit
`#0B0F14`), noon `#1F2733`, sunset `#1F2229`, neutral ambient the exact midpoint
of the two permitted daylight tones, and crevices `#000000` at noon, at midnight
and under torchlight alike. The glint sheen is now light-aware too: invisible on a
black midnight hull, visible where daylight or a flash has lifted the plates.

`python3 ci/make_report_sky_columns.py --check` keeps the report honest, and is a
build gate. `ci/sky_sheets/` is still not in the repository, so the image trace
gate still reports a skip rather than a pass — the anchor gate above carries the
ground truth until the PNGs are checked in.

### The uploaded sheets are the superseded set — checked, not assumed

`ci/sky_sheets/` was empty until the three sky PNGs landed in the repository root
(URL-encoded names, one commit). Tracing them was worth doing: they are the
**washed-out sheets these anchors replace**, not the new ones — teal climbs to
`#8D9687` at the horizon, purple to `#A64CB5`, rose to `#BE929E`, i.e. the exact
bright trace the brief rejects. So `--apply` against them would have undone the
ingestion; they were moved to `ci/sky_sheets/superseded/` with their measured
trace pinned in `SUPERSEDED_TRACE`, alongside a README that says which folder is
the authority. `ci/sky_sheets/superseded/` is now verified on every build ("the
superseded set is still the superseded set"), while the shipped tables are still
proven against the anchors — the gate reports what it checked instead of skipping
silently. When the real current sheets are added to the top level of
`ci/sky_sheets/`, `--verify` compares the shipped tables against those images
directly, which is stricter and will catch an anchor typo.

## 7. The white conic light column, cloud blobs, and the death finale (build #416, 7000.0.0-M)

### The halo is an independent, body-tethered light column

The white halo is now completely separate from the purple backdrop, as required.
The backdrop sheets stay flat 2D sky accents on the deep skybox layer; the white
atmosphere is its own render pass, tethered to the storm's own coordinates:

- **The flat bottom circle is gone.** The white under-halo oval (a horizontal disc
  pinned to the lower chassis layers, drawn only from phase 5.82 up, which read as
  a sticker and never followed the body) no longer draws, and neither does the old
  ring: no cap, no disc, no ring geometry remains in the pass.
- **The column.** `McsmHaloSkyRenderer` now builds a stack of 26 overlapped,
  camera-facing additive bands through the teeth/eye glow shader, so the falloff is
  computed per PIXEL and the column has no polygonal edge. It runs from inside the
  lowest core blocks up past the top tentacle attachments, and its base alpha is
  zero — the cone is open at the bottom by construction.
- **White-locked.** A new `MCSM_GLOW_WHITE` branch, `pipeline/storm_glow_white` and
  `GlowRenderTypes.glowWhite()` pin the pool to white. The aura around the teeth
  and eyes keeps carrying the phase colour; the atmosphere can never pick it up.
- **The growth weld.** Radius, height AND the cone's spread angle come from one
  shared size model (`McsmStormPhase.growth/bodyRadius/bodyHeight/scaleMultiplier/
  columnHalfAngleDeg`) instead of private per-pass curves. The multiplier reads the
  live entity state — the phase ramp plus the heads currently attached — so a
  severed head pulls the column in rather than leaving it inflated around air.
- **Soft cloud blobs.** `ci/make_cloud_blobs.py` re-bakes the six phase glare
  assets with a domain-warped silhouette and a long dissolve inside the texture
  (border alpha is exactly 0, so there is no rim left to see), with the palette
  ramp reversed so the light comes from the middle instead of the black-cored ring.
  At draw time each layer is five offset, rotated, anisotropically squashed lobes
  that roll slowly with the world clock — a rigid circular card cannot be read
  anywhere in the result.

### The death cinematic had never rendered — and now it does, cool-white

`mcsm_death` and its whole stack (direction warp, white cracks, implosion, supernova
rings, flash) sat in `mcsm_visuals.glsl` with **no caller**, exactly like the
void-body block in `fogless_entity.fsh` before it: the Java driver stamped the
1906..2906 carrier and nothing ever read it, so the finale was invisible in every
build. `core/sky.fsh` now reads the band, warps its sampling direction, and adds the
stack — and the palette is the **cool-white** finish: cracks `#E0F0FF`, the implosion
whitening to `#E6F2FF`, and the six expanding rings walking a cold blue-to-white ramp
instead of the old rainbow. The finale also holds the late-storm sky while the band
owns the phase carrier, so it cannot snap to a calm sky mid-flash.

`ci/check_phase_uniform.py` went **40 -> 60 checkpoints**, including the ones that keep
this honest: the death driver must stamp and release the band, the sky pass must call
every part of the stack, the finale must be cool-white, the no-dome shell must stay
inert (no geometry submission, zero strengths) — and a shader-include reachability
analysis that fails on any NEW unreachable helper.

### The "defs-only" question, answered with a call graph

The block below `mcsm_blob_color` is **not** dead: `core/rendertype_clouds.fsh` calls
`mcsm_mass_cover`, and the `mcsm_inf_*` helpers are its internals — 43 of the include's
60 functions are reachable from shipped shaders. The other 17 are dormant by design and
are now an explicit allowlist: the port's live entry point must stay wired, and a new
orphan fails the build instead of accumulating quietly.
