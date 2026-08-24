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
