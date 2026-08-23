# Devouring Storms — Video Accuracy & Completion Status

Last updated: 2026-08-23
Branch: `arena/01a02fba-lowuuuuuu`

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
| 4. Replace vanilla clouds with stylized MCSM cloud layer | ⚠️ | `StormCloudDeck`, `StormSkyCanopy`, and `CloudColorMixin` now handle storm cloud takeover with blocky slabs, a pale inner core, and stronger upper-sky coverage, but the exact runtime mixin/descriptor behavior still could not be validated locally. |
| 5. Update this document once coherent/playable | ✅ | This file now reflects the current source checkpoint honestly rather than the earlier stale blocker list. |

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
| Permanent attached halo | ⚠️ | Implemented in `WitherStormRenderer`; needs composition check against screenshots/video. |
| White death bloom / vanish | ⚠️ | Implemented in renderer state/tint flow; needs in-game confirmation. |

---

## Rebrand status

| Rebrand area | Status | Notes |
|---|---|---|
| Mod id / resource namespace | ✅ | Renamed to `devouringstorms`. |
| Java package namespace | ✅ | Source now lives under `net.dabicco.devouringstorms`. |
| Main entrypoint classes | ✅ | `DevouringStormsMod` and `DevouringStormsModClient`. |
| Artifact / project naming | ✅ | Gradle project/artifact names now use `devouringstorms`. |
| Visible branding strings | ✅ | Fabric metadata and item-group branding now say **Devouring Storms**. |
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

1. **Playable validation is still outstanding.** The pulse timing, halo placement, white collapse timing, and cloud takeover all need real in-game eyes.
2. **Recovered Stage A-D textures are preserved, but not yet fully repacked into the current atlas expectations.**
3. **`docs/VIDEO_ACCURACY_STATUS.md` is now current for the source tree, but not yet backed by a local Minecraft launch in this sandbox.**

In short: the source pass is now materially further along and the old stale blocker text has been cleared, but the final acceptance for Batch 17 still depends on a successful build plus in-game visual verification.
