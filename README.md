# UNLIMITED POSSIBILITIES SANDBOX

A single, continuous, procedurally generated universe you can fly through — planets you can land on and walk around, galaxies you can fly *into*, a raymarched black hole with real gravitational lensing, and physics toys to throw at all of it.

Built with **Babylon.js 9**, **TypeScript** and **Vite**. Everything is procedural: there are no downloaded 3-D models.

---

## Running it locally

### 1. Prerequisites

You need **Node.js 18 or newer** (this repo is developed on Node 22) and npm, which ships with Node.

```bash
node -v    # must print v18.x or higher
npm -v
```

If you don't have Node, get the LTS build from [nodejs.org](https://nodejs.org) or use `nvm`:

```bash
nvm install --lts && nvm use --lts
```

You also need a browser with **WebGL 2** — any current Chrome, Edge, Firefox or Safari. The app checks for this and will tell you if it's missing or if hardware acceleration is switched off.

### 2. Get the code

```bash
git clone https://github.com/Loganwall111/Low.git
cd Low
git checkout arena/019ff838-low
```

> The work lives on the `arena/019ff838-low` branch, **not** on `main`. `main` still holds an unrelated older project, so skipping the `checkout` will run the wrong thing.

### 3. Install dependencies

```bash
npm install
```

This pulls one runtime dependency (`@babylonjs/core`) plus the dev toolchain. It takes about 10–30 seconds.

### 4. Start it

```bash
npm run dev
```

Then open **<http://localhost:8080>**.

That's it. Hot reload is on — save a file and the browser updates.

---

## The commands

| Command | What it does |
|---|---|
| `npm run dev` | Dev server with hot reload on port 8080 |
| `npm run build` | Production build into `dist/` |
| `npm run preview` | Serve the built `dist/` on port 8080 |
| `npm run check` | Full verification suite — 2000+ assertions |

`npm run check` runs `tsc --noEmit` first, then every module's test file. It takes a couple of minutes and needs no browser or GPU. Run it before committing.

To run a single group:

```bash
node tools/render-check.mjs      # rendering: tone mapping, sky, atmospheres
node tools/boot-check.mjs        # the app boots against a mocked WebGL2 context
node tools/cosmos-check.mjs      # universe generation and the endless-space fold
```

---

## Troubleshooting

**Port 8080 already in use.** Either free it, or run on another port:

```bash
npx vite --host 0.0.0.0 --port 3000
```

**A blank or black screen.** Open the browser console (F12) and look for a WebGL error. The most common cause is hardware acceleration being disabled — in Chrome, check `chrome://gpu`. The app has a watchdog that samples real pixels and will report a genuinely black frame rather than leaving you guessing.

**`command not found: vite`.** Dependencies aren't installed. Run `npm install`. Always use `npm run dev` rather than a bare `vite`, so the local copy is used.

**Edits don't hot-reload.** Make sure you're loading `http://localhost:8080` directly and not through a proxy. See the note below if you *are* behind one.

**`npm install` fails or `node_modules` looks broken.** Delete and reinstall:

```bash
rm -rf node_modules package-lock.json && npm install
```

---

## Running behind an HTTPS proxy

Only relevant if you're serving this through a tunnel or cloud preview rather than opening `localhost` directly.

The hot-reload socket needs to be told which port the *browser* should connect on, which isn't the dev server's port when a proxy sits in between. Set:

```bash
HMR_CLIENT_PORT=443 npm run dev
```

Without it the page still loads fine — only live reload is affected. Locally you should leave it unset, otherwise the browser tries `wss://localhost:443` and hot reload silently stops working.

---

## Layout

```
Low/
├── index.html                  # boot overlay + failure reporting
├── vite.config.ts              # port 8080, host 0.0.0.0
├── public/art/                 # generated planet and surface textures
├── src/bjs/
│   ├── App.ts                  # scene, main loop, system wiring
│   ├── PostFX.ts               # bloom, grade, adaptive resolution
│   ├── SafeUniforms.ts         # NaN/zero guards for shader inputs
│   ├── shaders/                # planet, sun and portal GLSL
│   ├── worlds/                 # black hole, planetary, ocean, garage, ship…
│   ├── systems/                # ~40 modules: universe streaming, sky,
│   │                           #   audio, fleets, throwables, NPCs…
│   └── ui/                     # HUD, window manager, styles
└── tools/                      # the verification suite
```

Systems are deliberately separate modules rather than one large file, so each can be tested on its own.

---

## Notes on the tests

The suite runs headlessly against a mocked WebGL2 context. That's enough to catch logic, wiring and numerical faults — including several real ones, like a NaN aspect ratio blanking the screen and a blend mode Babylon was silently ignoring.

It cannot prove the picture looks right. jsdom's WebGL stub reports every shader compile as successful, so a green suite means "nothing is provably broken", not "it renders correctly". Visual changes still need a human to look at them.
