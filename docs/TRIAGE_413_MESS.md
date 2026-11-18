# Triage of the Sept-8/12 "total mess" shots (18 images) — build .413

Installed jar in the shots: **devouringstorms-1.9.158 / 1.9.160-26.2-beta-ds** =
our own port under the OLD `1.9.<build>` numbering (~230 builds before .413).
The `-26.2-beta-ds` suffix is our release naming; `ds-1.9.390..392` on this
branch prove the numbering lineage.

| # | Shot evidence | Defect | Causal system | Status in .413 |
|---|---------------|--------|---------------|----------------|
| 1 | white void sky, faint panel seams, X-seams (dormant, 1.9.160) | "mesh floating in sky" with NO storm | OptiFine sky sheets `assets/minecraft/optifine/sky/world0/sky1-4.png` bundled in-jar; dark radial blob baked into sky4.png | removed at 1.9.199 (ci/SKYBOX_AUDIT_1.9.199.md); not present in .41x jars |
| 2 | hard diagonal panel edges at sunset (1.9.160) | sky sheet quads intersecting | same sheets | gone since 1.9.199 |
| 3 | floating halo orb + wireframe dot-sphere, storm absent | halo drawn without storm | base halo layers (`StormGlowRenderer.submitLight`) textured with OUR tractor_beam.png cone override | .413 deletes the override -> base halo sprite restored; base code gates it on presence |
| 4 | purple confetti dots exploding around storm | particle/sphere spam | blob stack `McsmStormBlob` (atmosphere pulse spheres, spotlights) | .413: `McsmStormBlob.submit` early-returns |
| 5 | "tater-top" spheres / faces off the head at phase 6-8 | body-glued quads at radius 40+30*(p-6) | `McsmStormBlob` + `McsmPhaseSky` ring (#390 addenda) | .413: same early-return |
| 6 | brown/blank boxes, detached head groups ("EMGs not put together") | CEM .jem phase ladder vs base 1.9.16x model | ogs-cem DEFAULT_ENABLED built-in pack (build.sh embed + McsmBuiltinPack register) | .413: pack not embedded, not registered; audits updated |
| 7 | "skin texture completely broken mangled" | traced skin overrides vs newer base UVs | jar-overrides/.../textures/entity/* (wither_storm*, devourer_assets*, phase_4_assets*) | .413: staged entity overrides deleted at build; base skins ship as-is |
| 8 | big purple ring over halo, purple horizon band (1.9.160 ACTIVE 18h) | `McsmPhaseSky` ring + sky volume | blob stack | .413 early-return |
| 9 | overlapping menus / stuck buttons | our console/config panels vs base screens | McsmExtrasScreen layout | OPEN — needs a fresh .413 GUI shot to lay out against current base screens |
| 10 | "only halos meant to remain are the original ones" | added halo hues/rings | our halo window table + glare quads | glare layer already cancelled by McsmPresenceFxPatch; blob/phase-sky off in .413; base night-layer halo untouched |

Policy locked by this triage (user's newest mandate): **our jar adds UI,
gating and the shader pack only. Models, skins, beams, halos, particles =
base art untouched.** Reference stills are base-mod video frames, so base
art IS the reference.

Dead-code note: `McsmStormBodyNeutralTintMixin` (body tint .411) existed only
in the stale pre-reset baseline; .411/.412 never shipped it. No tint ships in
.413 — base body colouring as-is.
