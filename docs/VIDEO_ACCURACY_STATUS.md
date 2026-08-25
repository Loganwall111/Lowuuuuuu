# Devouring Storms — Video Accuracy & Completion Status

Last updated: 2026-08-24
Branch: `arena/01a0354e-lowuuuuuu` (continuation of arena/01a02fba-lowuuuuuu, PR #12)

This file is the current truth for how closely the mod matches the user's MCSM references.

## Legend

- **✅ Done** — implemented and wired in source.
- **⚠️ Implemented, needs playable validation** — code is present, but this sandbox could not launch Minecraft or run a local Gradle build.
- **🧩 Partial** — present, but still missing some fidelity or supporting logic.
- **🔴 Missing** — not implemented yet.

---

## Batch 17 visual accuracy status

| Item | Status | Notes |
|---|---|---|
| 0. Recover original textures from BBModels/resources | ✅ | Recovery pass completed; extracted textures are preserved under `src/main/resources/assets/devouringstorms/textures/entity/`. See `docs/BATCH_17_ASSET_AUDIT.md`. |
| 1. Storm sky / palette / dome rework | ⚠️ | `StormPalettes`, `StormSkyDarken`, `StormStarfield`, `SkyRendererMixin`, and the new `StormSkyCanopy` now aim for the screenshot-led progression: normal phase 4, green at 4.5, turquoise at 5.0, then a later pink/purple drift into cataclysm, with an upper-sky canopy helping the top of the sky fully fade over without manual slider cranking. Client join/leave now also hard-clear cached sky takeover state so fresh worlds do not inherit storm palette bleed. It still needs in-game comparison against the references. |
| 2. Split permanent HALO from one-shot PULSE | ⚠️ | Completed in source. Permanent halo now rides with `WitherStormRenderer`; one-shot pulse is server-driven through `StormPulsePayload` + `StormPulseFX`. Needs runtime tuning/verification. |
| 3. Restore glowing white death dissolve | ⚠️ | Main body + severed halves now have collapse whiteout/fade state in their render states/renderers, and phase-4 body rendering now also reuses `phase_4_assets_e.png` as a pale-to-purple emissive overlay. Visual timing still needs a real in-game check. |
| 4. Replace vanilla clouds with stylized MCSM cloud layer | ⚠️ | `StormCloudDeck`, `StormSkyCanopy`, and `CloudColorMixin` now handle storm cloud takeover with blocky slabs, a pale inner core, stronger upper-sky coverage, and a later purple swing so early phase 5 can stay closer to turquoise. The exact runtime mixin/descriptor behavior still could not be validated locally. |
| 5. Update this document once coherent/playable | ✅ | This file now reflects the current source checkpoint honestly rather than the earlier stale blocker list. |


---

## Batch 17 master pass — round one + round two combined (2026-08-24)

Continues from safe checkpoint `4c5fed7`. CI-validated per commit via the Build workflow (Java 25 / Gradle 9.5.1); the sandbox still cannot run Gradle locally (no JDK, blocked Maven/Gradle hosts), so GitHub Actions remains the compile gate.

| Item | Status | Notes |
|---|---|---|
| Phase 6 is Formidibomb-only | ✅ | `addSubGrowth` hard-locks any non-devourer storm below 6.0 forever. Growth by eating at phase 5 / 5.5+ can never promote to 6. |
| Late phase-5 growth goes to the BACK | ✅ | Everything eaten past 5.0 feeds `expansionPhase` (unbounded). Overall body/head scale is capped (`bodyScaleForPhase`) so the storm stops ballooning; the outward back/cube mass (`backScaleForPhase`) keeps expanding — huge-back shell, debris ring radii and grapple back volumes all grow. |
| Growth speed modifier | ✅ | `infiniteGrowthSpeed` scales the back/outward expansion pace at late phase 5 AND continues unbounded through phase 6 at max settings. |
| Traced BBModel shells actually load | ✅ | **Bug fix:** every stage shell JSON referenced textures with invalid Identifier characters (spaces / `!` / uppercase), so `Identifier.fromNamespaceAndPath` threw and ALL shaded shells silently failed to load. Assets renamed to identifier-safe names, the six shell JSONs rewired, the missing `hold_the_elevator` phase-6 texture extracted from `witherstormStageD_Center_Massive.bbmodel`, and both export tools hardened. |
| Shaded preset vs OG default | ✅ | Default skin stays OG MCSM textures (`stormSkin=1`); the shaded BBModel presentation is preset 2 and now defaults its stage shells ON (`stormStageShells` only ever applies inside the shaded preset). |
| Static bright center / pixelated wall removed | ✅ | `blackGlare` (the big flat backdrop planes) is now OFF by default and marked legacy. The replacement attached aura is camera-billboarded additive glow + translucent dark washes that ride the storm's silhouette and follow it across the sky — never a free-standing wall. |
| Per-phase aura story | ⚠️ | Implemented: phase 4 blue-white ring of light; dark wash when turquoise fog starts (~5.0); black+purple aura 5.15-5.45 with drift toward blue at 5.5 (blue on faces, dark/light purple sides); phase 6 steady layered halos (orange underneath, red at the bottom, purple above red, black above purple); purple/dark-pink vortex rings at 7.5+/8. Needs in-game tuning against the videos. |
| Phase-6 split "bang" | ✅ | Distinct one-shot `KIND_SPLIT` pulse (purple core + white ring + orange band, short strong timing) fired only by the Formidibomb split; rings now render on the additive no-depth-write glow pipeline. |
| Aura/beam pipeline hygiene | ✅ | All aura passes use additive (`ONE, ONE`) or translucent pipelines with depth-write disabled and per-frame camera billboarding (`StormGlowRenderer.submitBillboardPlane`); pulse rings moved off `debugQuads`. |
| Sky/fog timeline | ⚠️ | `stageWeights`: green 4.5, turquoise at 5.0 and purged again past 5.0, purple 5.15-5.45, pink-horizon mix 5.45-5.9, cosmic purple `#1A002B` at 5.95+; phase-6 crossing dips the sky black for ~4 s while the storm rises, then returns pinkish-purple. Needs in-game check. |
| Default game clouds | ⚠️ | New `ambientMcsmClouds` (default ON): the chunky voxel deck renders as the game's normal clouds even with no storm — near-white by day, deep indigo/storm-blue at night, semi-transparent bottoms. Only the palette changes when a storm arrives. Needs in-game feel check. |
| Teeth / eye emissives | ⚠️ | Teeth emissive tint default is mint-cyan (#7FFFD4→#00FFFF family), eyes stay glowing purple, both on full-bright emissive layers (existing). Config v14 migration resets the tint keys for existing users. |
| Phase-6 big swarming cubes | ⚠️ | Devourer swarm cube sizes roughly doubled-plus with a biased-big distribution and reduced count — big cubes swarming the sides instead of a haze of purple/black dots. Needs in-game look. |
| Shadows / terrain lighting | 🧩 | Built-in shadow map (`StormShadowMap`), beam impact lights (`beamImpactLight`, `StormImpactLights`), bloom and Iris companion bridge remain from earlier batches; defaults on. No new work this pass. |

---

## Major storm gameplay / boss-flow status

| Feature | Status | Notes |
|---|---|---|
| Phase-based growth and progression | ✅ | `WitherStormEntity`, `WitherStormPhase`, growth thresholds, and phase sync are in place. |
| Tractor beams / block & mob pulling | ✅ | Beam renderers, sound, and support systems are present. |
| Super skulls / storm combat abilities | ✅ | Phase-driven ability framework is in place. |
| Tentacle slam terrain attack | ✅ | Already wired from earlier batches. |
| Formidibomb transition to collapse | ✅ | `formidibombed()` + collapse timer path are present. |
| Post-bomb collapse visuals | ⚠️ | White body dissolve and pulse split are now in source, but still need play validation. |
| Bowels / interior finale path | 🧩 | Interior systems exist, but full finish-to-end play validation was not possible in this sandbox. |

---

## Atmosphere / presentation status

| Feature | Status | Notes |
|---|---|---|
| Sky darkening and palette ownership | ✅ | Dominant-storm sky takeover is now centralized and phase-aware. |
| Starfield / night dome | ✅ | Present and palette-gated. |
| Flat MCSM storm cloud deck | ⚠️ | Implemented, but not runtime-checked here. |
| Black rim glare + ejecta | ✅ | Present in `StormPresenceFX`. |
| One-shot command pulse bloom | ⚠️ | Implemented in `StormPulseFX`; needs feel/timing check in game. |
| Optional summon shockwave | ⚠️ | New first-summon purple shockwave path is wired through `StormPulsePayload`, `StormPulseFX`, summon flow, and command spawning. It is off by default and still needs runtime tuning. |
| Permanent attached halo | ⚠️ | Implemented in `WitherStormRenderer`, with the purple halo now held back until later phase 5 so early takeover stays less prematurely violet. Needs composition check against screenshots/video. |
| White death bloom / vanish | ⚠️ | Implemented in renderer state/tint flow; needs in-game confirmation. |

---

## Rebrand status

| Rebrand area | Status | Notes |
|---|---|---|
| Mod id / resource namespace | ✅ | Renamed to `devouringstorms`. |
| Java package namespace | ✅ | Source now lives under `net.dabicco.devouringstorms`. |
| Main entrypoint classes | ✅ | `DevouringStormsMod` and `DevouringStormsModClient`. |
| Artifact / project naming | ✅ | Gradle project/artifact names now use `devouringstorms`. |
| Visible branding strings | ⚠️ | Main config/control-center text, reset notice, metadata author label, and command-facing branding are being moved to **Devouring Storms**; a broader sweep still remains for full consistency. |
| Main menu branding pass | ⚠️ | New custom title-screen overlay/logo/icon path is in source, but still needs in-game visual validation and likely refinement once the official user-supplied logo can be integrated directly. |
| Command naming | ⚠️ | New root command path is `devouringstorms`, while legacy `dabyws` is still kept as a compatibility alias for now. |
| Historical original-jar references | ✅ | Kept intentionally as `dabywitherstormmod-1.9.60-26.2-beta.zip` where they refer to the preserved upstream binary. |

---

## Resource recovery status

| Resource area | Status | Notes |
|---|---|---|
| Embedded BBModel texture extraction | ✅ | Recovery script: `tools/extract_bbmodel_textures.py`. |
| Recovered storm/body/block texture preservation | ✅ | Preserved with original filenames, including duplicates written as `__1`, `__2`, etc. |
| Runtime use of recovered Stage A-D tiles on current Java atlases | 🧩 | Preserved, but many recovered textures are tile-sized and are not direct drop-in replacements for the current atlas-based models. |
| Archive/joke placeholder preservation | ✅ | Kept on purpose so no original bytes were lost; documented in the asset-audit file. |

---

## Validation status

### What was validated in this sandbox
- Source edits were completed for the Batch 17 phase-2 work.
- `git diff --check` passed on the touched files.
- Resource namespaces, package paths, and metadata were updated to the new **Devouring Storms** branding.

### What could **not** be validated locally here
- No Java/JDK is installed in this Arena sandbox.
- That means **no local Gradle compile**, **no local client launch**, and **no full playable runtime check** were possible here.

### Required follow-up validation route
- Use GitHub Actions to compile/package the branch.
- If Actions fails, inspect the inline logs and capture the exact `error:` lines before fixing.
- After a green build, the remaining open question for this pass is visual/runtime feel, not source wiring.

---

## Honest remaining gaps after this pass

1. **Playable validation is still outstanding.** The menu overlay, Ctrl+O shortcut, summon shockwave feel, halo placement, white collapse timing, and cloud takeover all need real in-game eyes.
2. **Recovered Stage A-D textures are preserved, but not yet fully repacked into the current atlas expectations.**
3. **The new Story-Mode structure expansion is still code-side scaffolding, not a full authored world set.** The new `undertown`, `watchtower`, `courtyard`, and `street` generators need taste-testing in play.
4. **`docs/VIDEO_ACCURACY_STATUS.md` is now current for the source tree, but not yet backed by a local Minecraft launch in this sandbox.**

In short: the source pass is now materially further along and the old stale blocker text has been cleared, but the final acceptance for Batch 17 still depends on a successful build plus in-game visual verification.

---

## Pass 2 — MCSM sky & cloud overhaul (2026-08-24)

**Pass 1 recap (commit `20310cb`, CI green run `32787833767`):** all entity
auras/rings/bangs are world-space gradient geometry (cylinders, backplates,
flat rings) — camera-billboarded halos are gone from the codebase.

**Pass 2 changes:**

1. **`client/StormCloudDeck.java` (full rewrite)** — elevated blocky MCSM
   ceiling: ambient deck anchored at Y = 258 (+`stormCloudAltitude`), snapped
   to a 24-block world grid so the chunky silhouette is stable as you move.
   Slabs are true shaded prisms now (`cloudPrismShaded`, 19-param): top faces
   full-bright, side faces shaded by the real sun/moon vector
   (`sunDirection` from the overworld clock), bottom faces deep ambient shadow
   with reduced alpha (semi-transparent undersides). Storm decks keep wrapping
   their storm but are spread tall and wide `(420+260·min(phase,7))·(0.9+0.5·growth)`.
   Legacy 15-arg `cloudPrism` and 12-arg `slab` remain as delegating wrappers
   for `StormCataclysmFX`.
2. **`client/StormSkyCanopy.java` (full rewrite)** — the skybox is a multi-stop
   gradient dome (world-space cylinder + zenith cap at r=640 around the
   camera): day = warm pale-blue horizon → vibrant azure zenith; sunset =
   glowing orange/pink horizon → deep teal → indigo (Season 2 palette); night
   = bright cyan/teal horizon glow → deep cosmic blue zenith (the twinkling
   starfield still layers on top). When a storm claims the sky the dome's
   stops drift toward `StormPalettes` and a soft additive horizon back-glow
   ring wraps the world (turquoise 5.0–5.1, purple/pink 5.12–5.98,
   orange/red ≥5.9), silhouetting the storm body against glowing sky.
3. **`client/StormPalettes.java`** — the turquoise fog window now closes at
   **5.1** exactly (transition 5.0→5.1): from 5.1 upward the sky hands off to
   the purple story with no residual teal haze.
4. **Vanilla cloud shader override** — `assets/minecraft/shaders/core/`
   `rendertype_clouds.vsh`/`.fsh` ship in the mod (fetched the exact vanilla
   26.2 sources and kept the pipeline contract byte-compatible: same
   `CloudInfo` UBO, `CloudFaces` buffer, fog outputs). Only the face shading
   changed: tops crisp white, N/S sides soft shade, W/E sides sun-catching,
   bottoms deep shadow at 55% alpha. Depth stays LEQUAL-equivalent with
   translucent blending (reversed-Z engine semantics).
5. **`mixin/VanillaCloudHeightMixin`** — lifts the vanilla cloud plane
   192→256 when the vanilla renderer is active (ambient deck off). Uses
   `@ModifyConstant(..., require = 0)` so it can never crash a boot: if the
   constant isn't found it silently no-ops. Gated by new config key
   `elevateVanillaClouds` (default on).
6. **Built-in world texture pack** — `assets/minecraft/textures/` now carries
   Story-Mode grass/foliage colormaps (lush saturated greens, warm-dry to
   cool-wet gradient), `grass_block_side.png` with a baked green fringe, and
   a tintable grayscale `short_grass.png`. Generator:
   `tools/gen_builtin_resource_pack.py`. Being under `minecraft:` inside the
   mod jar, they apply below any user resource pack.
7. **Stars** — `stormStars` default moved to 2 (every night) so the twinkling
   night starfield shows on the new night gradient by default.
8. **Cleanup** — stale `infiniteGrowth` config description corrected (body
   expansion is unbounded by default now; the toggle only adds whole-body
   inflation past phase 5).

**Verification in this sandbox:** all cross-file symbols were checked against
the known-good HEAD sources (`StormSkyDarken`/`StormPalettes`/
`ClientDistantStormManager.StormData`/`WitherStormEntity.clientGrowthScaleForPhase`
/config fields/fabric `LevelRenderContext` API surface), brace/paren balance
checked, all `cloudPrismShaded` (19) and legacy `cloudPrism` (15) call sites
arg-counted, and the generated PNGs decoded back and sampled. CI remains the
compile gate (no local JDK).

---

## Pass 3 — built-in world shader: natural shadows + emissive glow (2026-08-24)

**The ask:** the game should look like a shader pack by itself — real sun
shadows on grass/water/everything even with no storm, plus a coloured glow on
torches and other emitters — all built into the mod.

**What shipped:**

1. **`client/WorldShadows.java`** — natural directional sun shadows for the
   ordinary world, reusing the (battle-tested) storm shadow machinery:
   the height surface around the player is sampled (throttled 400 ms cache,
   min-of-neighbourhood like the lid so spikes can't punch holes) and emitted
   into the sun-facing depth map; nearby living entities contribute padded
   caster boxes; the shared screen-space pass then shades every scene pixel
   the sun can't reach — grass, water, walls, snow, all of it, because the
   receiver comes from the scene depth buffer. Cool blue-grey tint (what
   remains in a real shadow is skylight), fades out at sunset automatically,
   and steps aside entirely while a phase-4+ storm owns the shadow pass.
2. **`StormShadow`** — the screen-space pass extracted into a shared
   `drawShadowPass(...)` used by both the storm and world paths (no behaviour
   change for the storm side).
3. **`StormShadowMap`** — new `worldActive` frame flag lets the world pass
   capture without needing the storm's shadow config.
4. **Emissive glow** — new `post_effect/world_emissive_bloom.json` chain
   (high threshold 0.82, gain 4, gentle combine) built on the existing
   bloom shaders: torch flames, lava, glowstone and beacons get a soft
   coloured halo that carries their light into the air. Runs whenever
   `worldEmissiveGlow` is on (default), including alongside the storm's
   heads-only bloom; skipped when whole-screen storm bloom already ran.
5. **New config keys** (Effects): `worldShadows` (default on),
   `worldShadowStrength` (0.55), `worldEmissiveGlow` (default on).
6. **Inner-glow core wired** — `fx_witherCubeInnerGlow.png` (from the traced
   Blockbench FX project) had been extracted but never rendered; it is now the
   storm's emissive breathing core inside the shaded-shell body (phase 4+,
   shaded preset). That closes the "all traced forms wired" list: 6 stage
   shells + the inner-glow FX.
7. **`auraRadius` now follows `backScale`** (the outward back/cube mass) as
   requested, instead of the whole-body growth scale.

---

## Pass 4 — in-game feedback round 1 (2026-08-25)

User tested build `+build.N.be6018a`/`4c949b0`-era jars: textures landed,
black screen fixed, but (1) no visible world shadows, (2) sky "super messed
up" vs the reference shots, (3) billboard-looking glow still present.

**Fixes:**

1. **Billboard eliminated for real** — `StormGlowRenderer.submitLight` (the
   summon/pulse flash) was still building its quads from the camera view
   vector: a camera-facing billboard, exactly what the user was seeing. It is
   now a WORLD-SPACE glow cross: three orthogonal world-axis-aligned gradient
   planes per layer (XZ/XY/ZY) through the centre — additive, volumetric-ish
   from any angle, never swings with the camera.
2. **Sky canopy redesigned to compose with the vanilla sky, not replace it.**
   The pass-2 full-replacement dome (alpha ~0.9 cylinder + zenith cap) is
   gone. New canopy is a horizon haze band (r=480, bottom 46 below eye to 108
   above, alpha fading to 0 well above the horizon): warm pale-blue by day,
   orange/pink at sunset, faint teal at night — with the vanilla blue zenith,
   sun, moon and twinkling stars showing through untouched. The storm
   back-glow is now an ARC behind the nearest storm's bearing (±0.62 rad,
   raised-cosine falloff), not a 360-degree wall — the ring was reading as a
   giant billboard wrapped around the world.
3. **World shadows made visible** — casters now use exact per-cell heights
   (the old min-of-neighbourhood lowering erased 2-4 block features: walls,
   hedges, young trees — on ordinary terrain nothing was left to cast);
   default strength 0.55 → 0.65 with a deeper cool tint (0.44/0.49/0.60);
   world shadows now only yield to a phase-4+ storm within 260 blocks (a
   distant storm no longer switches the whole world's shadows off); the pass
   fails loudly once with a full stack trace instead of silently.
4. **Night cloud glare fixed** — the sun/moon brightness curve treated the
   full moon as 80% of daylight, so the ambient cloud ceiling glared
   near-white at midnight. New ramp: 1.0 at noon → ~0.37 at the horizon →
   0.16 floor at midnight.

---

## Pass 5 — official assets + phase skybox (2026-08-25)

User uploaded the official Telltale-tweak cloud shader, a Stage D COLLADA
model, the blue energy gradient and the sky-only-no-clouds anomaly plate
(commit 217682f also carried an older snapshot of the whole tree, which was
replaced by the current branch head; only the new assets were kept, filed
under textures/sky + geo + tools/official).

**Shipped:**

1. **Official cloud shader ported** into the vanilla-override
   `rendertype_clouds.vsh` (26.2 contract): official knobs — 2.5x vertical
   cloud scaling, Y offset, per-face brightness, vertical alpha fade
   `a = base * (0.8 - fade)`. Reference copy kept at
   `tools/official/rendertype_clouds_OFFICIAL.vsh`.
2. **StormSkyDome** — the entity-tethered phase skybox: dome centred on the
   storm core sampling the uploaded plates vertically by view-ray elevation
   and horizontally by azimuth; blue/cyan energy at phase 4+ (phase4_energy),
   crossfading 5.5→6.0 into the deep purple/black/orange anomaly
   (phase59_anomaly), then tinted through vibrant orange→red→magenta across
   phases 6→8; additive, depth-write off, terrain never clipped; intensity
   driven by player↔storm distance.
3. **Clean sky**: ambient cloud deck now defaults OFF, cluster-ejecta "dust"
   (glareEjecta) defaults OFF, and the storm deck prisms fade out under the
   anomaly dome (phase ≥ ~5.5) so the no-clouds plate reads clean.
4. **OG traced models by default**: the Blockbench stage shells no longer
   require the Shaded preset — they render in every skin, so the traced
   bodies + original textures are what you see out of the box.
5. **World shadows**: worldShadowStrength default 0.70 and the pass now lets
   tall-terrain pixels (cliffs/towers the coarse lid misclassifies) take the
   shaded path instead of being skipped — the "no shadows on hills" hole.
6. **Coloured lighting**: the emissive bloom now saturates each glow pixel
   toward its dominant hue before adding, so torch halos read orange, lava
   red, enchanting blue — not white.
7. **Palettes 6-8**: cataclysm sky/cloud colours shifted toward the vibrant
   red/orange/magenta story beats (fog + lightmap grading follow the same
   palette system).
