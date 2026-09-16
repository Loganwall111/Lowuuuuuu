# 7000.0.0-M — Build #415: phase-keyed emissive mouth track, 2D purple canvas + cloud-filter LERP, void-black/glint single-sourcing, model bytecode split, version-stamp gate

Built on the crash-free **ds-1.9.390** foundation (commit `17f6000`, the
stabilised line) merged forward onto the current **7000.0.0-M** baseline, which
already carries the #403-#412 migration. All four mandate phases were audited
item by item against the tree; what was already correct is left alone, and the
gaps are implemented below.

**Phase 1 — version stamp + build gates.**  `VERSION`, `BUILD_VERSION` (file),
`gradle.properties:mod_version`, `fabric.mod.json:version` and
`McsmExtrasConfig.BUILD_VERSION` all read **7000.0.0-M**, and `ci/build.sh` now
has a *version stamp gate* that fails the build if any of the five ever
disagrees again (the four in-code labels had each drifted at least once, which
is how a fresh jar reads as an old one in the Mods screen).

**Phase 2 — model + void-black + glint.**
- `WitherStormP4.createBodyLayer` was ONE method holding 1259 `.addBox(...)`
  calls and ~224 KB of source — a ~176 KB method against javac's hard 64 KB
  per-method bytecode limit ("code too large"). It is now split into 16 chunk
  methods sharing a per-call `P4Parts` context (303 fields, names and types
  preserved), produced by the new, self-verifying `ci/split_p4_model.py`:
  every original statement must survive token-identical, string literals
  (the bone names) byte-identical, and no context field may be read before it
  is assigned -- otherwise the script refuses to write. `ci/build.sh` gained a
  model method budget gate so it can never silently regress.
- Void-black is now single-sourced: `MCSM_VOID_BLACK`/`MCSM_NAVY_BLACK` (the
  split-atlas key #0A0E14 / #000000) plus `mcsm_void_black()` live in
  `mcsm_visuals.glsl`, and the body shader's creases, AO bands and structural
  block gaps collapse onto #000000 with no lift.
- The animated glint is a real moving sheen now: two drifting streak fields
  plus a slow cross-roll band (`mcsm_glint()` and its in-shader twin), and the
  flat plastic gloss pass stays deleted.

**Phase 3 — emissive teeth/eyes + 4.0x bloom + cosmic blue.**
- The phase palette is now ONE table in four places that must agree:
  `McsmTeethPhaseTint.PHASE_TRACK` (Java, authoritative),
  `mcsm_mouth_color()` in `mcsm_visuals.glsl`, the storm-glow shader and the
  Iris final pass — P4 cyan-white, P5 white, P5.5 cyan-blue, P6 cinematic blue,
  P7 toxic green, P8 blinding white.
- `core/entity.fsh` (the program `RenderTypes.eyes()` actually runs) snaps the
  emissive teeth/eye pixels onto that palette and applies the 4.0x
  amplification. `final.fsh` (v5) keeps the 4.0x and now keys it per phase,
  decoding the phase from the mod's own phase-monotonic fog palette because a
  pack cannot read the mod's FogSkyEnd carrier. `storm_glow.fsh` and
  `fogless_entity.fsh` use the same six-band table for the additive mouth
  pools and the pack-less eyes pass. Super Duper's `final.glsl`/`composite6.glsl`
  keep the plain 4.0x luminance boost on purpose: that pack is third-party code
  this repo cannot compile-validate offline, and the boost is already
  phase-correct because the emissive layer it amplifies is phase-tinted by Java.
- **Fixed:** `McsmTeethPhaseTint.tick()` stamped the beam colours twice and the
  second block ran last with the TEETH track, so every phase flew
  `#00A877` sea-green tractor beams. The stale block is gone: the core cones
  are show purple again (`#8C26FF`), bluish through phase 6 only, and the
  lower auxiliary spotlight emitters keep their separate cosmic blue
  `#4D4DFF` in `StormImpactLights`.

**Phase 4 — 2D sky canvas + cloud filters.**
- The world-space dome/card paths stay deleted; the backdrop is the flat
  camera-locked sky sticker queue. Added the cinematic **purple sky canvas**
  and the transparent **phase-5 teal** and **phase-6 salmon-pink cloud
  filters** as new layers on that queue, cross-fading through the same
  smoothstep LERP (`ramp()`) as the existing smoke sheets — canvas deepest,
  then the filters, then the smoke and the additive glare glow.
- New `ci/make_backdrop_sheets.py` bakes the three sheets (256x128 RGBA8,
  filter-0 rows, deterministic) from the ds-1.9.390 gradient stop tables into
  both overlay roots on every build; the jar audit now fails if any of the six
  sticker sheets is missing from the assembled jar.

**Also in this build.**  The GLSL gate now compiles the *extras* under the same
define variants as the core shaders (before, the storm-body shader's EMISSIVE
and void-black branches were never validated — only their define-free fallback),
and it validates `fogless_entity.fsh`, which was outside the gate entirely.

**Not done, on purpose.**  The four community `.zip` archives named in the
mandate are not present in this workspace (`zips/` does not exist and the repo
has no `.zip` binaries), so no archive was extracted: the palette, sheet and
model data above come from the repository's own traced sources and the pinned
ds-1.9.390 lineage, regenerated deterministically instead of copied.

# 7000.0.0-M — master version label re-lock + full 403-412 lineage migration

- Master version string re-locked to **7000.0.0-M** in `VERSION`, `McsmExtrasConfig.BUILD_VERSION`, `gradle.properties` and `fabric.mod.json`; every HUD/title/panel surface reads the single constant.
- **Migrated the entire build #403-#412 lineage** from `arena/01a0a123-lowuuuuuu` into this version: native sky renderer (MCSM palette, aurora, stars, 3 cloud decks), halo sky renderer with the bright glare assets, atmospheric horizon cloud + directional alpha smear, cloud crown above the storm, muted reference-calibrated palette, white-core wide-cone beam textures, shader-less additive mouth glow, indigo story water, and the Iris skyRender uniform fix.
- Restored the missing atmosphere + halo glow the 7000.0.0-M test frame showed absent: the halo/atmospheric passes are registered and default-on, and the 2D skybox sticker layer had its depth test fixed (LESS instead of GREATER_THAN_OR_EQUAL) so the smoky phase sheets actually paint the cleared-sky pixels.
- Teeth glow keeps the show-spec hues with the #403 intensity fix (3.6-4.3) so the arcs read as glowing on the native renderer.
- Kept from this line: void-black body palette lock + rolling glint, native emissive eyes/teeth channel, 4.0x emissive face bloom, cosmic-blue spotlight nodes, 90 diamond-dot sliders + chapter rail.

# 7000.0.0-MCSM-CINEMATIC-FINAL.412 — Build #412: dark disc + purple card + thin beams fixed against ref 065011

- DARK DISC = base mod stormProximityVignette (screen-space radial darkening
  centred on the storm; it darkened terrain too). Force-OFF at submit.
- RAZOR HORIZON LINE = bottom edge of the base mod stormBackdrop sky card
  (main flag, still on after #410 killed only its quad variant). Force-OFF.
- BEAMS: #411 falloff was too steep (thin wisps). Regenerated tractor_beam
  with a wide uniform cone (bright to 70% half-width, soft outer 30%) plus a
  hot core stripe -- matches the wide glowing cones of ref 065011.
- With vignette+card gone the #411 calibrated sky gradient, soft clouds,
  glare halo and teeth glow are finally unobstructed.

# 7000.0.0-MCSM-CINEMATIC-FINAL.411 — Build #411: reference calibration pass (Sept 6/8 stills), no new systems

User verdict on .404-.410: "none of this is accurate, only worse". This build
changes ONLY four visual parameters, each matched to the reference stills:

- SKY PALETTE re-calibrated to the muted cinematic hues of the refs: day =
  overcast teal-sage (not lavender), dusk = purple zenith/mauve horizon,
  sunset = purple zenith/pink-salmon horizon, noon = muted teal. The candy
  lavender/salmon keys are gone.
- CLOUDS: razor rectangle plates replaced with soft-penumbra slabs
  (smoothstep borders over 30% of each plate) like the fuzzy chunky clouds
  in the refs.
- BEAMS: tractor_beam texture regenerated with a white-hot core column and
  smooth cone falloff so beams read saturated with a bloomed white centre
  like the reference cones (blue at phase 6, purple elsewhere, unchanged).
- BODY: neutral tint lifted 0xFF6A6A6A -> 0xFF948F8A so the traced block
  shading reads as textured warm-grey (reference dome shot) instead of a
  flat dark blob; dusk silhouettes stay dark via lighting.

Everything removed in .408-.410 stays removed (outlines, vein wires, black
glare, cataclysm halo, backdrop quad, native sky tint, purple specks).
Rollback points if any single change is wrong: .410 (pre-calibration),
.405 (pre-sky-renderer), 5aa1de7 (exact old reference build).

# 7000.0.0-MCSM-CINEMATIC-FINAL.410 — Build #410: storm backdrop quad OFF (the last fake-sky layer), purple-sky specks default OFF

- The flat purple sky card with the razor horizon edge and the dark core that
  survived #409 was the base mod's world-anchored STORM BACKDROP quad
  (stormBackdropQuad). It is now force-OFF at submit together with the black
  cylinder variant -- the real sky (superduper gradient or vanilla) is the
  only sky, per the standing "regular sky, no bands" directive.
- McsmExtrasConfig.purpleSky (the 5.5+ purple storm-sky specks, toggle in
  Story Mode Controls) now defaults OFF; the "no purple tint" mandate wins.
  Toggle it back on in Story Mode Controls if ever wanted.
- With the backdrop gone, the #409 bright phase glare layers, the shaded
  cloud decks and the pack sky are finally unobstructed.

# 7000.0.0-MCSM-CINEMATIC-FINAL.409 — Build #409: regular sky (no dome/band), bright glare, no dark disc, no vein wires

- REGULAR SKY, PER USER DIRECTIVE: the mod-side native sky tint and storm fog
  tint are fully retired (McsmNativeSkyRenderer is a no-op). No dome, no
  horizon band, no seam -- the superduper pack gradient (or plain vanilla
  without a pack) is the only sky.
- DARK CIRCLE GONE, TWO CAUSES KILLED: (1) the base pass's Black Glare ring
  and Cataclysm halo pair are now force-OFF at submit (they painted the
  giant dark disc / purple ring); (2) the phase glare PNGs themselves had a
  near-opaque BLACK core -- regenerated as bright radial glares (hot tinted
  centre, transparent fringe) so the halo finally READS as coloured glare
  over the shader sky instead of a void.
- PURPLE LINT GONE: transparent override for textures/entity/wither_veins.png
  (the base mod's violet vein-wire overlay on the storm body), on top of
  .408's OUTLINES 0.
- WATER horizon blend: rough sheen 0.30 -> 0.45 so the sea catches the sky
  colour at grazing angles and the sea/sky line stops reading as a band.

# 7000.0.0-MCSM-CINEMATIC-FINAL.408 — Build #408: outlines off (purple wire glint), shaded chunky clouds, no white-void sky

- PURPLE WIREFRAME GLINT REMOVED: the pack's Dungeons-style outline pass
  (OUTLINES 2) was drawing violet edge wires over the Wither Storm body and
  masking the traced Story Look shading. OUTLINES is now 0 -- the Story Mode
  frames have no block outlines, and the OG shaded textures read as-is.
- CLOUDS NO LONGER FLAT CARDBOARD: plates get softened borders, per-plate
  brightness variation and sun-lit tops / shaded undersides; decks tightened
  (scale 46/70/110, coverage 0.42/0.30/0.18) so they read as chunky puffs;
  grazing-angle streaks faded out harder (0.02-0.14 |dir.y|).
- WHITE VOID FIX: noon/day horizon keys were near-white, so looking down at
  the cloud sea filled the upper screen with white haze. Horizons are now
  saturated blue/lavender and the zenith gradient steepens earlier (pow 0.42).
- Reminder: the dark straight-edged wedge sometimes crossing the sky is the
  BASE mod's "The Sun Shadow" storm shadow volume (config category in the
  Devouring Storms screen), not a shader artifact.

# 7000.0.0-MCSM-CINEMATIC-FINAL.407 — Build #407: shaderpack compiles again (uniform redefinition fix)

- .406 FAILED TO LOAD in Iris: the restored skyRender.glsl re-declared
  dayCycle / dayCycleAdjust / rainStrength / fragmentFrameTime, which the
  including programs already declare -- GLSL treats that as a redefinition
  error and Iris dropped the entire pack ("The shaderpack failed to load").
- .407 declares NOTHING that the hosts declare; dimensions that force-disable
  the day cycle/weather (Nether) get compile-time constants instead.
- With the pack loading, .406's content finally reaches the screen: MCSM
  colourful sky palette, aurora + stars at night, sun/moon glow, three
  fly-above-able chunky cloud decks (y=176/224/288), indigo story-mode water
  with sky sheen, on top of the .405 shader-less mouth glow and phase-6 blue
  beams.
- NOTE FOR INSTALLS: 10000.0.0-alpha.338 is the ORIGINAL upstream Devouring
  Storms jar, not this build. If it stays in mods/ Minecraft may load it
  instead and none of the 7000.0.0-MCSM changes appear. Keep ONLY the
  devouringstorms-7000.0.0-MCSM-CINEMATIC-FINAL.407 jar; the mod prints a
  warning when the mods-list number and panel header disagree.

# 7000.0.0-MCSM-CINEMATIC-FINAL.406 — Build #406: restored sky renderer — MCSM skies, aurora, stars, 3 cloud decks, indigo water

- ROOT CAUSE FIXED: the imported Super Duper pack was MISSING its sky
  renderer (getSkyBasic/getFullSkyRender/getSkyFogRender/getSkyReflection
  were called but never defined), so Iris dropped deferred1/composite and
  every atmospheric VFX silently vanished. New lib/atmospherics/skyRender.glsl
  implements them in the Story Mode look.
- COLOURFUL SKIES: per-time-of-day gradient palette — teal-black night,
  deep-blue dusk with cyan horizon band, salmon sunset, lavender day, bright
  noon — matching the reference frames.
- NIGHT VFX: teal aurora curtains + twinkling stars; soft sun/moon discs with
  warm sunset tinting.
- CLOUDS PART OF THE SKY, FLY-ABOVE-ABLE: three chunky rectangular decks at
  y=176 (infinite sea), y=224 (blocky) and y=288 (wispy) via ray/plane
  layers, distance- and horizon-faded — fly above one and the next is there.
- WATER: re-graded to saturated Story Mode indigo/violet (deep 0.145,0.135,
  0.520 -> shallow 0.300,0.310,0.820) with a rough 0.30 sheen so lakes catch
  the colourful sky at grazing angles like the cutscene mirrors.

# 7000.0.0-MCSM-CINEMATIC-FINAL.405 — Build #405: shader-less glow, phase-6 blue beams, story-look bloom

- SHADER-LESS GLOW: additive radial billboards (teeth_glow_white/cyan/blue)
  now sit at the mouth cluster per phase band -- emissive texture + additive
  blending + full-bright light, so teeth/eye glow reads even with NO shader
  pack installed. With the shipped superduper pack on HIGH/ULTRA you also get
  true bloom on top (strengths raised 0.12->0.20 / 0.14->0.22).
- PHASE 6 BEAMS BLUISH (user override of the purple-everywhere policy):
  6.0-6.9 writes beam colour (0.30,0.42,1.0); all other phases stay show
  purple (0.55,0.15,1.0).
- Halos remain six cross-faded colour-gradient layers (blue/green/bridge/
  purple/salmon/ember) at full blend -- multi-colour gradients exactly like
  the reference frames.
- The shipped superduper shader pack IS the story-mode look-alike: shadows
  and lighting on, reflections/god-rays off, colourful grade; use profile
  HIGH or ULTRA for bloom.

# 7000.0.0-MCSM-CINEMATIC-FINAL.404 — Build #404: glossy charcoal body, one-colour sky, no ring artifact

- BODY: dark neutral vertex tint (0x6A6A6A) keeps the traced body glossy
  charcoal at every hour -- no more daylight brown; the emissive islands
  supply the show's sheen. Night stays black.
- PURPLE SPECKLE SPRAY: glareEjecta and devourerDebrisGlow disabled -- the
  violet ejecta particles were the "purple tint on top of it"; reference
  debris is black/white native blocks.
- SKY: both vanilla gradient anchors now receive the SAME tinted value, so
  the storm sky is one continuous colour with no zenith/horizon band; the
  blend still fades to regular vanilla sky with distance.
- HALO: both halo_ring cards retired (they rendered as a concentric artifact
  floating in the sky); halos are now purely the soft per-phase glare
  gradients + the white under-halo from 5.82.
- TEETH/EYES: glowStrength floor raised 1.0 -> 2.5 so the emissive teeth and
  eye lenses bloom like the reference frames (with the shipped superduper
  shader pack installed the bloom matches the frames one-for-one).

# 7000.0.0-MCSM-CINEMATIC-FINAL.403 — Build #403: lineage tip + charcoal skins + glowing teeth + continuous sky

- Re-based onto the reference lineage TIP 97f8a92 (three commits past
  5aa1de7): native block-particle debris swirls (the missing animations),
  client-safe particle random source, native atlas routing hooks.
- BODY SKIN FIX: the 512 phase atlases were teal-tinted checkers, which lit
  green-teal in daylight. All eight phase_4_assets atlases re-encoded as
  luminance-preserving NEUTRAL charcoal -- black at night, tan in daylight,
  exactly like the reference frames; per-phase identity now comes from the
  emissive/teeth decks and halo hues.
- TEETH GLOW FIX: show-spec hues kept but intensities raised to the
  renderer's real magnitudes (3.6-4.3 instead of 1.1-2.45) -- the old numbers
  rendered dark on this teethBoost pass ("teeth do not glow").
- CONTINUOUS SKY FIX: no more flat dome + deleted fan (the hard band at the
  top). Vanilla's own sky renderer now interpolates zenith -> horizon between
  two tinted anchors (1:1 extracted storm colours), and the blend weight
  carries distanceInfluence() so the storm sky fades back to regular vanilla
  sky far from the storm.
- Carried: accurate per-phase halo hues at full-blend alpha, 1:1 zenith and
  horizon palettes, hysteresis-stable skins, no-screen rail handoff, story
  HUD/intro/borders native.

# Devouring Storms 7000.0.0-MCSM-CINEMATIC-FINAL -- procedural cinematic atmosphere, native Halo cleanup, and phase-7 Vortex

This candidate includes the 1.9.315 active-sky ownership and transparent
cloud changes, plus the same exact phase maps in the built-in core shader and
the shader-pack-v5 fallback. It also removes the old artificial zenith rim and
darkening overlay that could appear as a black discontinuity at the top.

This remains a CI artifact until the active scene is confirmed in Minecraft.

# Devouring Storms 1.9.315 -- active sky ownership and transparent exact-map pass

- Suppress the opaque FabricSkyBoxes backdrop before the sky pass only while a
  phase-5+ storm is active; calm and ordinary summon scenes keep their skybox.
- Reduce the directional cloud alpha target to 0.58 (below the 0.80 cap), so
  vanilla clouds and background motion remain visible through the core.
- Apply the requested exact phase maps in Java and shader paths.
- Write both uStormPos and u_StormPos carrier names.
- Request blurred/clamped sampling for storm_white.png.

This remains a CI artifact until the active scene is confirmed in Minecraft.

# Devouring Storms 1.9.314 -- stability follow-up for the confirmed cloud

The 1.9.311 log confirms the new purple/pink atmospheric layer is visually
close to the reference, but the run still dies in the optional shadow path:
Intel UHD reaches 145,000 storm vertices plus more than 80,000 ground vertices
per shadow submission before the JVM fails to reserve 2.16 GB of G1 virtual
space.

- Keep the confirmed Atmospheric W's Cloud and its fixed far directional
  projection unchanged.
- Disable only the optional trailer/storm/terrain shadow passes that cause the
  native-memory failure on this graphics path.
- The phase sky, storm model, beams, eyes, and atmospheric cloud remain active.

The previous 1.9.313 artifact remains superseded by this CI-only stability
build until the game survives a ground-level phase 5.9 test.

# Devouring Storms 1.9.312 -- remove the 3D circle and unify active sky

The 1.9.311 in-game report identified two separate problems: the remaining
world-space glare/ring geometry still looked like a giant circle that could be
approached, and the normal sky colour remained peach/black when the custom sky
route was enabled.

- Removed the remaining structured glare, ground pool, vortex meshes, and
  orbiting ring submissions. Atmospheric W's Cloud is now the only replacement
  atmosphere geometry.
- Moved the Java cloud projection to a fixed camera-relative far shell instead
  of the storm's physical distance, so it cannot become a 3D object beside the
  storm.
- Corrected the angular mapping that was using tan(58..88 degrees), which
  spread the patch around the sphere as a giant circular shape.
- Applied the active phase 5/5.5/6/8 sky deck to the vanilla SkyRenderState and
  suppress the regular dynamic skybox during the active storm; regular summon
  skyboxes return outside the storm.
- Removed the expensive ring/vortex geometry that contributed to the Intel UHD
  native-memory crash.

This remains CI-only until the active sky is visually checked in Minecraft.

# Devouring Storms 1.9.311 -- remove legacy circular backdrop

The 1.9.310 screenshot exposed one remaining old render path: the purple
round world-anchored Vortex Backdrop card was still being submitted beside
the storm. It was not Atmospheric W's Cloud and made the result look like a
distant circle.

- Removed the legacy VORTEX_BACKDROP quad from the structured glare pass.
- Atmospheric W's Cloud remains only in the infinite directional sky paths.
- The phase-colored cloud and its semi-transparent horizon smog are no longer
  competing with that old circular card.

This build is CI-only until the circle is gone and the pink/purple cloud is
visibly wrapped behind the storm in Minecraft.

# Devouring Storms 1.9.310 -- crown placement for Atmospheric W's Cloud

This placement correction follows the visual clarification that the cloud is
not merely centered on the storm's midpoint. The dense cloud crown now sits
above the Wither Storm while its broad, flat lower shoulder wraps across the
horizon behind the storm.

- Added a shared `0.22` top lift to the core GLSL, managed shader-pack, and
  Java fallback paths.
- Kept the 4x horizontal spread and 0.5x vertical compression, so the cloud
  remains massive around the centered horizon rather than becoming a small
  spot or a detached upper blob.
- Preserved infinite directional projection, semi-transparent 0.80 density
  cap, phase-color bleed-through, and shredded 3-octave FBM edges.

This build is CI-only until the crown/back/horizon placement is checked in
Minecraft.

# Devouring Storms 1.9.309 -- Atmospheric W's Cloud

This pass replaces the old geometric glare/smudge interpretation with the
actual visual structure from the supplied Wither Storm screenshots: a wide,
flat, asymmetric atmospheric cloud wrapping across the horizon.

- Renamed the visual concept to `Atmospheric W's Cloud`; it remains an
  infinite directional skybox effect, not a finite world object.
- Rewrote the active GLSL cloud field as a 4x horizontal / 0.5x vertical
  stretched weather layer using a non-circular box field and 3-octave FBM
  edge shredding. No `length(uv)` radial oval or doughnut bounds remain.
- Added semi-transparent dark matter density capped at `0.80`, with the
  required `pow(noise, 2.0)` falloff and `mix(phaseColor, vec3(0.02),
  densityAlpha)` phase-color bleed-through.
- Updated both the core sky shader and managed shader-pack path, including
  correct alpha compositing instead of the previous opaque occlusion formula.
- Renamed the control-panel labels while retaining old config keys for
  compatibility.

This build is CI-only until the new atmospheric-cloud result is checked in
Minecraft.

# Devouring Storms 1.9.308 -- enlarged storm-attached sky smear

This corrective build follows the 1.9.307 in-game check. The storm sky was
visible and correctly shaped, but its angular footprint still read as a small
dot floating behind the Wither Storm.

- Enlarged the directional smear substantially in every render path so its
  color mass sits directly behind and around the complete storm silhouette.
- Kept the 2.5x horizontal multiplier, phase 5/5.5/6 ramps, asymmetrical
  feeding tongues, and 2D FBM/torn contour.
- Broadened the alpha transition and reduced the side/outer coverage so the
  enlarged field still dissolves into the regular sky instead of becoming a
  solid dome.
- Retained the normalized `u_StormProximity` carrier and separate regular
  summon skybox textures.
- Bumped the installed build identity to `1.9.308-26.2-beta-ds`.

This build is intentionally CI-only until the enlarged result is checked in
Minecraft.

# Devouring Storms 1.9.307 -- alpha storm-sky patch, no legacy Fabric sky

This corrective build addresses the 1.9.306 screenshots. The previous build
painted a camera-centred full dome, which appeared as a giant green/purple
sphere and could cover the storm with the legacy FabricSkyBoxes backdrop.

- Removed the opaque full-sky dome from the no-shader/Fabric path. The storm
  backdrop is now only the large, alpha-feathered organic patch attached just
  behind the storm, so the reference sky can blend through its edges instead
  of ending at a hard sphere boundary.
- Temporarily suppresses the legacy FabricSkyBoxes day/night/sunset override
  only while an in-range phase-5+ storm is active. The phase storm patch then
  owns the backdrop; regular summon skyboxes return outside that storm window.
- Kept the asymmetric FBM silhouette, unequal side feeding, internal colour
  tongues, and close-to-storm shell placement; expanded the patch rather than
  expanding an opaque full-screen dome.
- Bumped the installed build identity to `1.9.307-26.2-beta-ds`.

Delete older Devouring Storms jars and old shader/resource-pack copies before
launching. Confirm the Mods screen shows `1.9.307-26.2-beta-ds`.

# Devouring Storms 1.9.306 -- reference-shaped infinite skybox smear

This build is based on the verified 1.9.305 line, not the older 1.9.200
shader-only jar. It targets the screenshots showing a clean circular colour
grade instead of the irregular, layered paint mass around the Wither Storm.

- Reworked the core GLSL, FabricSkyBoxes, and active shader-pack blob paths
  to use the same asymmetric FBM/domain-warped silhouette: unequal side
  tongues, a low shoulder, an upper notch, torn edges, and internal colour
  tongues. It is no longer a direct circle or a single radial grade.
- Pulled the angular footprint closer to the storm bearing (38..60 degrees
  before the size setting) while retaining the direction-only/infinite
  behaviour: flying toward the skybox cannot reach a back face.
- Kept the corrected phase palettes and blended them through the broken
  contour: phase 5 teal/moss, phase 5.5-5.9 violet/magenta, and phase 6
  dusty rose/amber vertical split.
- Bumped the player-facing build to 1.9.306 so the installed jar cannot be
  confused with the old 1.9.200 or the unmodified 1.9.305 release.

Install the 1.9.306 jar by deleting older `devouringstorms-*.jar` files from
`mods/` first. The Minecraft mods screen must show
`1.9.306-26.2-beta-ds`.

# Devouring Storms 1.9.305 (prior) -- the blob becomes a true infinite skybox smear

**Why 1.9.305:** the blob looked like a flat disc floating in the world.

**The problem:** on profiles where iris (or another shader mod) is merely
INSTALLED with its pack turned off, the mod routed the blob to
McsmBlobOval's four world-anchored oval quads -- 2D cards pinned at the
storm's 3D position. That is exactly the "giant purple disc with a dark
centre floating in mid air, extremely far away" you reported. A shader
mod being present is not the same as a shader pack being IN USE.

**The fix -- the blob is now a separate infinite skybox layer:**
* the pack gate now asks IrisApi whether a pack is actually rendering the
  sky, so vanilla pipelines (including iris with shaders disabled and
  FabricSkyBoxes) always get the true sky layer;
* the world-anchored quads are GONE on every path. The blob is painted as
  an organic, noise-warped smear (an oval with two warped side lobes,
  feathered edges, uniform body alpha, internal streak shading) on a
  camera-centred sky sphere -- the sunrise-band / aurora construction you
  described: infinite, tethered to the storm's bearing, gliding with it,
  never approaching, with the vanilla sky visible above and around it;
* the corrected 2026-09-11 hex decks (dark core, phase-5 greenish rim,
  phase-5.5 royal magenta, phase-6 sunset split) are baked into the smear
  on a noise-jittered radius, so the bands read as smeared paint, not
  clean rings;
* with a real shader pack active, the same smear draws at the storm's
  distance (skybox depth), so every configuration now shows one identical
  sky-blob.

**After updating:** load any world during phases 5.0-6.95 and look at the
storm. The blob is a huge messy purple smear across the sky behind the
storm -- walk sideways and it stays glued to the storm; fly at it and it
never gets closer (that is the infinite-skybox tell). Beyond ~1600 blocks
from the storm it fades out and the regular sky returns.

## 1. Everything from the other line (1.9.201-1.9.220) is IN
- **Story Mode NPCs (McsmNpcs)**: the canonical cast spawns at towns and
  sites, with dialogue, talk/laugh acting and phase-crossing voice lines.
- **StoryCharacter humanoid entity** with 22 cast skins and its renderer.
- **Real Telltale models**: Stage A / Stage B / Stage C Small/Big/Massive /
  Stage D massive, the severed storm, the head, voxelised from the real
  meshes (bbmodel -> jem), plus the per-phase growth slider.
- **REAL Telltale Vortex model ported** (McsmVortexMesh) over the
  procedural swirls, debris vortex, glacier tornado flakes, cube rings.
- **OG look skins + charcoal-indigo atlas pass** on all 18 skin atlases.
- **Teeth/eye glow root fix**: bloomStrength hard-floor 2.5, glowStrength
  1.0, forced turquoiseTeeth/headEyeGlow, mini-storm teeth stay lit.
- **Built-in shader pack auto-select + install fix**: the new pack replaces
  the old one on update -- this is what previously left the OLD
  orange/yellow sky on screen even after a new jar was installed.
- shader default+auto-select, structured glare revamp (sphere deleted),
  story water, per-phase model growth, tentacle girth, debris maxing.

## 2. This line's sky work is IN (and stays the sky you approved)
- **Infinite Skybox Blob** with the CORRECTED hexes: phase 5
  `#161A1D/#2D423F/#6A9A78` (green), phase 5.5-5.9
  `#0B0410/#2D1442/#581C6E/#87529C` (purple & pink void), phase 6
  `#1A1226/#462A52/#966173/#D89874` (four-colour sunset split).
- **Opaque, alpha-blended**: the dark core is near-opaque and truly darkens
  the sky; the smudge rings bleed outward with smoothstep. NO vanilla
  distance fog on the storm sky, ever (opaque cinematic layer).
- Blob on EVERY path: core GLSL sky pass (vanilla), McsmStormSkyLayer
  (FabricSkyBoxes on), McsmBlobOval (Iris shader-pack path -- the built-in
  pack). Fully procedural, GL_LINEAR-equivalent, zero pixels.
- **Day sky**: lavender-blue per the reference. **Night sky**: deep NAVY,
  never purple. 700-1600 block distance fade back to vanilla sky.
- **Teeth/eyes**: phase 5 = pure white with white aura, 5.5-5.9 cyan-white,
  6 = greenish-blue (more blue), 7+ = green-white. Eye glow no longer dims
  to black.

## 3. Version
Mods screen shows **1.9.305-26.2-beta-ds**; title screen, config screens
and `/ds` show build **1.9.305**. Verified by CI annotation on the build.

## Assets
- `devouringstorms-1.9.305-26.2-beta-ds.jar` — the mod
- `devouringstorms-shaderpack-v5-1.9.305.zip` — Iris shader pack
- `devouringstorms-storylook-1.9.305.zip` — Story Look resource pack
- `devouringstorms-superduper-default-1.9.305.zip` — Super Duper pack
- `.sha256` checksums for verification

## Installing (so the old jar/pack can never win again)
1. Delete EVERY `devouringstorms-*.jar` from your `mods/` folder.
2. Download `devouringstorms-1.9.305-26.2-beta-ds.jar` from this release
   and put it in `mods/` alone.
3. Delete the old pack folders from `shaderpacks/` (any folder starting
   with `devouringstorms`), then toggle "Built-in Shader Pack" OFF and ON
   once in the MCSM Control Panel -- the new pack reinstalls.
4. In game: the mods screen must show `1.9.305-26.2-beta-ds`. If it shows
   ANY other number, that jar is not this build.
