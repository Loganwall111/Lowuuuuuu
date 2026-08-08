# GRAVITON · a psychedelic odyssey — 3D Edition

A fully self-contained 3D space game rendered in the browser with **Babylon.js**
(WebGL) and **Havok physics** (WebAssembly), driven by a re-architected
"swarm" of specialist modules. Real Newtonian gravity, black holes with
volumetric particle accretion + gravitational lensing, temporal abilities,
procedural WebAudio music, and gate puzzles of math and alien language.

## Play

```bash
python3 -m http.server 8000
# open http://localhost:8000  → 3D edition (index.html)
# open http://localhost:8000/index2d.html → original 2D edition
```

The 3D edition pulls Babylon.js + Havok from the Babylon CDN, so it needs a
network connection on first load.

## Controls

- **Mouse** — aim (raycast to the flight plane)
- **`W` / `↑`** — thrust
- **`Space`** — pulse boost
- **`1` / `2` / `3`** — abilities: **Singularity** · **Temporal Rift** · **Void Lash**

Collect prism shards to refuel and score. Reach the **jump gate**, solve its
sigil (math or cipher) to unlock it, then fly through to warp to the next of
**6 sectors** ending at **The Singularity**.

- **Black holes** bend light, drag you into an accretion swirl, and slow time.
  Cross the event horizon and you're re-knitted from nothing.
- **Planets** are solid (Havok collision) — hull breach on impact.
- **Energy** drains; refuel on shards or slingshot past stars.

## Architecture — the "swarm" of agents

| agent | module | responsibility |
|-------|--------|----------------|
| Config | `js3d/config.js` | constants, palettes, ability specs |
| Core | `js3d/core.js` | Babylon engine, scene, environment, procedural assets |
| Camera | `js3d/camera.js` | chase cam, pointer aim, input |
| Lighting | `js3d/lighting.js` | star lights, flicker |
| Bodies | `js3d/bodies.js` | planets, black holes + volumetric particle accretion |
| Ship | `js3d/ship.js` | player vessel mesh + state |
| Abilities | `js3d/abilities.js` | Singularity, Temporal Rift, Void Lash, cooldowns |
| FX | `js3d/fx.js` | particle bursts, engine trail, warp |
| Physics | `js3d/physics.js` | Havok integration, gravity wells, collisions |
| Puzzles | `js3d/puzzles.js` | math gates & Caesar/substitution ciphers |
| Audio | `js3d/audio.js` | reuses the procedural WebAudio engine |
| UI | `js3d/ui.js` | HUD, abilities bar, overlays |
| Game | `js3d/game.js` | the crown orchestrator / swarm leader |

Zero bundler, zero external game libraries except the Babylon + Havok CDNs.
