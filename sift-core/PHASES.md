# Sift-Core phase plan

This file is the living acceptance checklist for the standalone sandbox. Each phase is designed to be playable and testable before the next one is expanded.

## Phase 1 — fall handshake (implemented)

- [x] Fabric 1.20.1 project scaffold and `0.1.0-SIFT` version.
- [x] `EntityMixin` server-side void hook at `Y < -64`.
- [x] Preserve position, rotation, head/body yaw, fall distance, gravity state, and velocity through `moveToWorld`.
- [x] Prewarm the empty destination chunk window before the respawn packet.
- [x] Suppress only the terrain loading screen associated with the Sift respawn packet, including stale-screen clearing.
- [x] Add `/sift fall` reproducible test command.
- [x] Add `/sift status` diagnostics for position, velocity, and rotation preservation.
- [x] Add a depth-aware procedural sky pass with vertical color gradients, aurora curtains, shafts, and star dust.

## Phase 2 — the empty abyss (implemented as a prototype)

- [x] Sparse custom-block floor generator using Siftstone and Sift Moss with vanilla features disabled.
- [x] Deterministic floating terrain shelves using the Sift block palette.
- [x] Registered visual rift entity with no collision, damage, or fluid push.
- [x] Procedural camera-facing rift renderer.
- [x] Wavy interior displacement, particle stars, and colored emissive rim.
- [x] Add an atmospheric pressure haze and animated filament veil around each rift.
- [x] Add a distant visual abyss floor above the generated custom-block floor.
- [x] Add the first free-flying ambient Sift Drifter mob with procedural rendering.
- [x] Register the first custom terrain palette: Siftstone, Sift Moss, and Rift Crystal.
- [x] Replace random proximity placement with a deterministic twelve-slot rift formation prototype.
- [x] Add authored swell, snap, and settle choreography around the formation without adding collisions.
- [ ] Add a real screen-space capture/refraction target if the final rendering backend requires it.

## Phase 3 — sift fluids (implemented as a prototype)

- [x] Collision-free visual fluid sheets.
- [x] Animated teal, amethyst, magenta, cyan, and amber color ramps.
- [x] Refraction-like displacement and intersection foam approximation.
- [x] Add flowing caustic bands, Fresnel-like edge light, and animated liquid glints.
- [x] Visual flow direction shares the deterministic field used by server-side current physics.
- [x] Horizontal current band aligned to the same mathematical sheet heights, without changing vertical velocity.
- [ ] Add authored pool silhouettes and current/velocity volumes.
- [ ] Integrate depth and normal textures for physically correct intersection foam.

## Phase 4 — merge readiness

- [x] Resource namespace and dimension key are stable (`mcsm`).
- [x] Icon copied into the standalone mod resource tree.
- [x] Offline JSON, shader, and project-layout validation script.
- [x] Compile against the pinned toolchain on Java 17 in GitHub Actions (run `35166210842`).
- [ ] Test multiplayer respawn ordering and reconnect behavior.
- [ ] Move the sandbox into the main dimensions project after a visual and gameplay review.
