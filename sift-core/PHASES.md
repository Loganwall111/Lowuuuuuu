# Sift-Core phase plan

This file is the living acceptance checklist for the standalone sandbox. Each phase is designed to be playable and testable before the next one is expanded.

## Phase 1 — fall handshake (implemented)

- [x] Fabric 1.20.1 project scaffold and `0.1.0-SIFT` version.
- [x] `EntityMixin` server-side void hook at `Y < -64`.
- [x] Preserve position, rotation, head/body yaw, fall distance, gravity state, and velocity through `moveToWorld`.
- [x] Suppress only the terrain loading screen associated with the Sift respawn packet.
- [x] Add `/sift fall` reproducible test command.
- [x] Add a depth-aware procedural sky pass.

## Phase 2 — the empty abyss (implemented as a prototype)

- [x] Air-only dimension generator with no terrain layers or features.
- [x] Registered visual rift entity with no collision, damage, or fluid push.
- [x] Procedural camera-facing rift renderer.
- [x] Wavy interior displacement, particle stars, and colored emissive rim.
- [ ] Replace proximity spawner with authored rift formations and encounter choreography.
- [ ] Add a real screen-space capture/refraction target if the final rendering backend requires it.

## Phase 3 — sift fluids (implemented as a prototype)

- [x] Collision-free visual fluid sheets.
- [x] Animated teal, amethyst, magenta, cyan, and amber color ramps.
- [x] Refraction-like displacement and intersection foam approximation.
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
