# CROWN OF THE UNMADE — 3D EDITION

*A full WebGL reality-warping god-arena. You are the god who unmakes reality.*

This is a from-scratch, fully-3D game built by a swarm of **15 specialist sub-agents**, retooled from a 2D canvas game into a genuine Babylon.js + Havok physics experience. It loads **Babylon.js** and **Havok Physics** from the official CDN (needs an internet connection in the browser) and builds everything else — enemies, the world, particles, audio, post-processing — procedurally in code. No sprites, no downloaded models, no assets: it is all generated.

## What's in it (AAA-era features)
- **Full 3D WebGL scene** with an orbit camera (drag to orbit, scroll to zoom), a procedural starfield/nebula skybox, and volumetric star particles.
- **Real Havok physics** — a physics world with a collidable arena floor plus physics-driven drifting asteroid debris you can interact with.
- **Gravitational lensing** — a bespoke full-screen post-process fragment shader that warps the rendered scene around each black hole's projected screen position, exactly like light bending around a singularity.
- **Black holes** with glowing accretion disks, spiraling accretion particles, gravity that pulls enemies *and* the player, and a **supernova** on collapse.
- **Volumetric particle systems** for every burst, shockwave, trail, and explosion (additive-blended, glowing).
- **Bloom** via GlowLayer, plus vignette, screen flash, camera shake and film grain.
- Procedural 3D enemy assets: drifters, shards, behemoths (spiked icos", wraiths, proximity mines, and the Sunmaw boss.

## Abilities (each bends a law of reality)
| Key | Ability |
|-----|---------|
| `Q` | **Singularity** — birth a black hole with gravitational lensing + supernova |
| `E` | **Temporal Rift** — bullet time; enemies leave glowing ghost trails |
| `R` | **Fractal Echo** — split across timelines; echoes mirror your fire |
| `G` | **Tractor Grab** — seize a foe with a gravity tether and hurl it |
| `SPACE` | **Gravitic Inversion** — flip the sky, fling everything, then slam |
| `F` | **Void Lash** — a searing tendril that severs reality in a line |
| `C` | **Prism Phase** — become nowhere, invulnerable, leaving a wound of light |
| `X` | **Genesis Overdrive** — ultimate. unmake everything |

Left-click fires astral bolts. Drag orbits the camera, scroll zooms.

## Menus
Main · Controls · Abilities · Settings (difficulty, sound, sensitivity, visual fidelity) · Pause · Death. Full procedural score, combo and wave HUD with ability cooldown deck.

## The 15-agent swarm
1. **ARCHITECT** — Babylon Engine/Scene, camera, input, state machine, main loop
2. **COSMOLOGY** — procedural skybox, star field, ambient world
3. **ENTROPY** — procedural 3D enemy assets, spawning, boids
4. **REALITY WEAVER** — the 8 abilities above
5. **PARTICLEFORGE** — volumetric bursts, trails, shockwaves
6. **GRAVITON** — black holes, gravitational-lensing shader, gravity, beams, physics debris
7. **TEMPORALIST** — bullet-time & rewind ghost trails
8. **LIGHTING/RENDERER** — lights, GlowLayer bloom, PBR-style materials
9. **FRACTALIST** — echo firing & overdrive cascade
10. **SYNTHESIZER** — procedural WebAudio score & SFX
11. **UMBRA** — vignette, flash, lens post-fx
12. **HUDMIND** — menus & HUD
13. **NARRATIVE** — lore & boot sequence
14. **BALANCER** — waves, budgets, combo economy
15. **POLISHER** — camera shake, game-feel, performance, final review

## Files
- `index.html` — shell, CDN scripts, menu DOM
- `style.css` — the neon-cosmic interface
- `game.js` — the entire 3D engine (all 15 agents' modules, tagged `[AGENT]`)
- `test_harness.js` — a Node stub harness that boots the scene and runs 900+ frames of gameplay (all abilities, pause, death, retry) to catch runtime errors

## Run
Serve the folder (e.g. `python3 -m http.server 8000`) and open it in a browser **with internet access** (Babylon.js + Havok load from CDN). Click **AWAKEN**. Hold on to reality.
