# CROWN OF THE UNMADE

*A reality-warping god-arena. You are the god who unmakes reality.*

This is a from-scratch, single-file-canvas game built by a swarm of **15 specialist sub-agents**, each owning one module and handing its output to the next so the whole thing fuses into one living organism. Everything — the nebula, the physics, the audio score, the 7 reality-bending abilities, the difficulty curve, the juice — is procedural and bespoke. There are no sprites, no libraries, no assets: it is all generated code.

## How to play

| Key | Action |
|-----|--------|
| `WASD` | drift through the wound |
| `MOUSE` | aim the omens |
| `LMB` (hold) | fire astral bolts |
| `Q` | **Singularity** — birth a black hole that devours matter, then goes supernova |
| `E` | **Temporal Rift** — stop their time (bullet-time; the soundtrack slows with you) |
| `R` | **Fractal Echo** — split across timelines; echoes mirror your fire |
| `SPACE` | **Gravitic Inversion** — flip the sky, fling everything upward, then slam it down |
| `F` | **Void Lash** — a searing tendril that severs reality in a line |
| `C` | **Prism Phase** — become nowhere; leave a damaging light trail |
| `X` | **GENESIS OVERDRIVE** — ultimate. Unmake everything. |

## The 15-agent swarm & what each one built

1. **ARCHITECT** — core state machine, frame clock, global time-warp, the update/render orchestrator.
2. **COSMOLOGY** — the living nebula world field: breathing starfield, drifting dust, layered radial nebula.
3. **ENTROPY** — the enemy hive & wave-genesis: drifters, shards, behemoths, wraiths, mines, and the sunmaw; soft-body separation, flocking, teleporting wraiths, mine proximity-arming.
4. **REALITY WEAVER** — the 7 abilities above.
5. **PARTICLEFORGE** — particle system: blasts, tracers, rings, glowing sparks, floating damage text.
6. **GRAVITON** — physics: black holes with accretion rings + event horizons and supernova implosions, gravity pull, beam geometry.
7. **TEMPORALIST** — bullet-time + rewind ghost trails of enemies frozen mid-motion; owns global `timeScale`.
8. **RENDERER** — canvas lighting engine: additive glow, radial gradients, shadow-blur bloom, layered draw order.
9. **FRACTALIST** — duplication & recursion: echo firing and the overdrive cascade.
10. **SYNTHESIZER** — fully procedural WebAudio score & SFX (tempo warps in bullet-time, chimes, booms, the overdrive fanfare).
11. **UMBRA** — post-processing: vignette, flash, film grain.
12. **HUDMIND** — interface: ability deck with cooldowns, vitals, wave/score/combo readouts.
13. **NARRATIVE** — lore, the cinematic title boot sequence, awakening lines.
14. **BALANCER** — difficulty scaling, spawn budgets, wave escalation, combo economy.
15. **POLISHER** — game-feel: screen shake, hit flashes, combo decay, final integration & review.

## Files
- `index.html` — shell & UI overlays
- `style.css` — the neon-cosmic interface
- `game.js` — the entire engine (all 15 agents' modules, tagged `[AGENT]`)

## Run
Open `index.html` in a browser (or serve the folder). Click **AWAKEN**. Hold on to reality.
