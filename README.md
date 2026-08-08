# GRAVITON · a psychedelic odyssey

A fully self-contained, zero-dependency browser game built as a multi-agent-style
"behemoth": real Newtonian gravity, black holes with gravitational lensing and
time dilation, procedural music & synth SFX, generative nebula backdrops, and a
gate system gated behind math **and** alien-language cipher puzzles.

## Play

Run a static server in this folder and open the page:

```bash
python3 -m http.server 8000
# open http://localhost:8000
```

## How to play

- **Aim / move** — the ship always faces your cursor.
- **`W` / `↑`** — thrust (burns energy).
- **`Space`** — pulse boost.
- **`G`** — toggle gravitational field lines.
- **`P`** — pause.

Fly around the sector collecting **prism shards** (restore energy + score).
Reach the **jump gate**, solve its sigil (a math equation or an Ancient-language
cipher) to unlock it, then fly through to warp to the next sector.

Six sectors await — the last is **The Singularity**. Be careful:

- **Black holes** bend light and drag you in. Slingshot past them for a speed
  bonus, but cross the event horizon and you'll be re-knitted from nothing.
- **Planets** are impassable — hull breach on impact.
- **Energy** drains constantly. Refuel on shards or slingshot off stars.

## Architecture

| module | role |
|--------|------|
| `js/config.js` | constants, palettes, shared utils |
| `js/audio.js`  | procedural WebAudio ambient music + SFX |
| `js/render.js` | canvas: nebula, starfield, bodies, lensing, ship |
| `js/physics.js`| Newtonian gravity, ship integration, collisions |
| `js/puzzles.js`| math gates & Caesar/substitution ciphers |
| `js/ui.js`     | HUD + overlay puzzle forms |
| `js/main.js`   | state machine, world gen, game loop |

Zero external libraries. Just HTML, CSS, and vanilla JS.
