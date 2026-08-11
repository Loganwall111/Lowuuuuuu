# LOW — Realtime Simulation Suite

Two hyperspace-grade Babylon.js (WebGL2) experience machines, fully controllable in real time:

- **🌊 Ocean Worlds** (`/water/`) — a playable water playground: Gerstner ocean with currents, whirlpools,
  flood & drain, rain and storms, cloth banners, and drag-and-drop buoyant props on procedural planets.
- **🕳 Singularity Vault** (`/blackhole/`) — a black hole customizer and endless-dimension journey:
  real-time geodesic ray-marching with gravitational lensing, movable singularity, fly camera,
  summonable planets/sun/belt/comet, a twin black hole, and 4 billion forgeable singularities.

A word on the README's original ask: **Vulkan** isn't available in a browser — the honest equivalents are **WebGL2**
(what this uses; fine) or **WebGPU**. And "ray tracing" here is **ray marching photon geodesics** on the GPU —
genuinely real-time, per-pixel, every frame.

## Run it

Any static file server works. From the repo root:

```bash
python3 -m http.server 8000
# open http://localhost:8000/            — launcher
# open http://localhost:8000/water/      — Ocean Worlds
# open http://localhost:8000/blackhole/  — Singularity Vault
```

No build step, no dependencies to install (Babylon.js comes from the official CDN).

---

## 🌊 Ocean Worlds

**A literal water-play simulator.** Orbit the camera (or fly it), then play god with the ocean itself.

### Play physics (the fun part)

- **Click the ocean** → drops the selected prop (crate, ball, barrel, boat, rubber duck, plank, stone, iceberg, banner raft, anchored buoy…).
- **Drag any floater with the mouse** → it becomes kinematic and follows your ray on a water plane;
  **scroll while holding** → raise/lower it; **flick and release** → throw it with real velocity.
- **Buoyancy per-shape**: boats self-right, icebergs ride deep, barrels roll, planks lie flat,
  rubber ducks capsize-proof. **Stones honestly sink** and rest on the seabed.
- **Soft body-vs-body collision** (crates pile up, ducks scatter), splash particles + expanding ripple rings.
- **Currents ("flowing water")**: strength + direction sliders advect foam/ripples in the shader *and* push floaters downstream.

### Whirlpools 🌀

- Up to **4 simultaneous whirlpools**. Arm placement and click the water, or spawn a random one.
- The sea surface **funnels downward** (vertex shader + JS mirror agree exactly).
- Floaters get **sucked in, spun around, swallowed** — and resurface far away.
- Whirlpool edges glow with **swirling foam** in the fragment shader.

### Flood & drain 🌡

- **Sea level slider** — flood the island waist-deep, or drain until floaters ground on the exposed seabed.
- **Auto flood/drain rates** — watch the world drown or dry up while you keep playing inside it.

### Rain, storms & ambience 🌧

- Rain slider → streak particles that **follow the camera** + raindrop ripples on the surface.
- Thunder & lightning toggle → random lightning flashes (brightens sun/hemisphere/clear color) with
  delayed WebAudio rumble. Ocean/roar ambience toggle (procedural noise bed, no audio files).

### Cloth physics 🚩

- Spawn a **banner raft**: a verlet-cloth flag (10×13 points, constraint iterations) pinned to a yard-arm,
  gusting with the wind, colliding with the waves, with live normal recomputation. Drag the raft around and
  watch the banner trail.

### Camera 🎥

- **Orbit** (default) and **Free fly** (WASD + QE/Space/Ctrl up-down, drag to look, scroll to dolly, speed slider).
- Cinematic auto-orbit, FOV, render-scale, shadows, FXAA/bloom/vignette/exposure.
- Freeze time + slow-motion sliders.

### One-click scenarios

Zen pond · Perfect storm · Flash flood · Drain the sea · Whirlpool bay · Regatta — each reshapes waves,
weather, sea level and inhabitants instantly.

### Planets

8 handcrafted worlds — Terran (Earthlike), Cobalt Falls, Arctic Expanse (bonus icebergs), Magma Tide
(**lava ocean**), Abyss Of Nyx (bioluminescent), Tidus Lagoon (with a **gas giant in the sky**), Flat Sea
(no island), Azure Dream. Every planet retunes terrain, palette, clouds, fog, wave field and water chemistry.

---

## 🕳 Singularity Vault

A black hole you can **move with the mouse**, fly *into*, and redecorate at will.
The main render is a single full-screen **geodesic raymarch** pass (Schwarzschild-style `1.5·h²/r⁵` bending,
frame-dragging swirl, relativistic doppler beaming on the accretion disk).

### Full free will

- **Camera modes**: Orbit · **Look** (orbit + free yaw/pitch look-around) · **Fly** (WASD + drag, scroll dolly —
  fly straight into the horizon to auto-start the descent!) · **Hole control** (drag the black hole itself
  through the frame with the mouse; scroll dollies it toward/away).
- "Photon ring zoom" snaps the camera to 2.35·R_s.

### Summon worlds 🪐

Real 3D meshes composited over the raymarch (alpha-aware):

- **Companion star** (color + size) that lights everything.
- **Planets** (up to 4): gas giants and cratered rocky worlds with procedurally painted textures,
  configurable orbit radius/size/speed; most ride the disk plane, some go polar.
- **Asteroid belt**: 320+ thin-instanced tumbling rocks.
- **Comet** with a streaming additive tail blowing away from the hole.
- **Twin singularity**: a second black hole orbiting in the disk plane — its gravity term bends light
  exactly like the primary (watch the Einstein rings warp as they pass).

### Customize absolutely everything

Mass, spin (frame-dragging), lensing strength, photon-ring glow **and tint**, disk inner/outer radius,
temperature, brightness, beaming, orbital speed, **disk hue tint**, dual tilt, star density, nebula amount
and **texture scale**, galactic band, nebula tints, exposure/contrast/bloom/chromatic-aberration/grain/
vignette, kaleidoscope-void constants, integration quality… then save/load named snapshots (localStorage).

### 4 billion singularities

- **24 curated presets** (Gargantua, M87*, Sgr A*, TON 618, Kraken, Carnival, Sovereign, Mourning Star…).
- **Forge from any 32-bit seed** — deterministic unique black hole with a generated name
  (`#seed=1337` URL-sharing supported).

### The endless journey 🚀

`ENTER THE BLACK HOLE` — descent → **Event Horizon** (approach, photon sphere warning) → **The Bloodstream**
(crimson artery flight with hemoglobin cells) → **The Hollow House** (a separate first-person three-dimensional
dimension on its own engine — walk with WASD, find the door) → **The Kaleidoscope Void** (fractal traversal) →
warp → **a freshly forged singularity** every single loop. Dive-deeper/skip/return controls,
speed slider, procedural heartbeat drone per dimension.

---

## Controls cheat sheet

| | Ocean Worlds | Singularity Vault |
|---|---|---|
| Orbit camera | drag | drag (Orbit mode) |
| Zoom | scroll | scroll |
| Move things | drag a floater (scroll = height) | "Move the hole" camera mode |
| Free fly | Camera → Free fly | Camera → Fly (WASD) |
| Spawn | click water / prop buttons | Summon Worlds folder |
| Panel | H | H |

## Architecture

```
index.html            launcher portal
shared/   util.js     PRNG, math, color helpers
          ui.js       XUI — dependency-free glass control panel (folders, sliders, colors…)
water/    world.js    seeded terrain heights + 8 planet configs
          physics.js  Gerstner wave mirror, buoyancy, whirlpools, currents, verlet cloth
          glsl.js     sky + ocean shaders (Gerstner vertex, funnel/whirl dip, foam, lava, glitter)
          main.js     engine wiring, drag&drop, rain/storm audio+, GUI, scenarios
blackhole/glsl.js     geodesic raymarch + bloodstream + kaleidoscope shaders
          presets.js  24 curated + deterministic 32-bit forge (~4.3e9 singularities)
          house.js    the Hollow House first-person dimension (own WebGL2 engine)
          main.js     journey state machine, camera modes, hole dragging, summons, GUI
```

Everything is client-side ES2017+ vanilla JS. Works in any evergreen browser with WebGL2.
