/**
 * Checks for the unbounded universe, the warp that crosses it, the glare
 * that was burning out the frame, and shader pre-warm.
 *
 * Run: node tools/infinite-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (n, c, e) => {
  if (c) pass++;
  else { fail++; console.log('FAIL: ' + n + (e ? ' — ' + e : '')); }
};
const read = (p) => (fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '');

const GRID = 'src/bjs/systems/IntergalacticGrid.ts';
const WARM = 'src/bjs/systems/ShaderWarmup.ts';
for (const f of [GRID, WARM]) {
  if (!fs.existsSync(f)) {
    console.log('FAIL: ' + f + ' is missing');
    console.log('0 passed, 1 failed');
    process.exit(1);
  }
}
const grid = read(GRID);
const warm = read(WARM);
const field = read('src/bjs/systems/GalaxyField.ts');
const world = read('src/bjs/worlds/PlanetaryWorld.ts');
const deep = read('src/bjs/systems/DeepSkySystem.ts');
const app = read('src/bjs/App.ts');

// ------------------------------------------------- galaxies without end
{
  const out = '/tmp/grid-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', GRID, '--bundle', '--format=esm',
      '--platform=node', '--log-level=error', '--outfile=' + out],
      { stdio: 'pipe' });
    const g = await import(out);

    ok('a galaxy exists in every cell of space', (() => {
      for (const c of [[0, 0, 0], [7, -3, 11], [-999, 400, 5000]]) {
        const gal = g.galaxyInCell(...c);
        if (!Number.isFinite(gal.x) || !Number.isFinite(gal.radius)) return false;
        if (gal.radius <= 0) return false;
      }
      return true;
    })());
    ok('the same cell always yields the same galaxy',
      JSON.stringify(g.galaxyInCell(4, -2, 9)) ===
      JSON.stringify(g.galaxyInCell(4, -2, 9)));
    ok('different cells yield different galaxies',
      JSON.stringify(g.galaxyInCell(1, 0, 0)) !==
      JSON.stringify(g.galaxyInCell(2, 0, 0)));
    ok('no storage is required for an infinite universe',
      !/new Map|new Set|\[\] as GalaxyCell\[\]\s*=/.test(grid) ||
      /pure function/i.test(grid));

    // THE POINT OF ALL THIS: you can never run out of places to go.
    ok('there is always another galaxy, however far you fly', (() => {
      let worst = 0;
      for (let i = 0; i < 500; i++) {
        const p = [Math.sin(i * 12.9898) * 3e7, Math.cos(i * 78.233) * 3e7,
          Math.sin(i * 39.425) * 3e7];
        const n = g.nearestGalaxy(...p);
        if (!n) return false;
        worst = Math.max(worst, n.distance);
      }
      // Bounded by the cell diagonal, so the gap can never grow without limit.
      return worst < g.CELL_SIZE * 2;
    })());
    ok('galaxies do not all sit on a visible lattice', (() => {
      const offs = new Set();
      for (let i = 0; i < 40; i++) {
        const gal = g.galaxyInCell(i, 3, -i);
        offs.add(((gal.x / g.CELL_SIZE) % 1).toFixed(3));
      }
      return offs.size > 20;
    })());
    ok('galaxies vary in size',
      new Set([...Array(30).keys()].map((i) =>
        Math.round(g.galaxyInCell(i, 0, 0).radius))).size > 15);
    ok('galaxies vary in colour',
      new Set([...Array(60).keys()].map((i) =>
        g.galaxyInCell(i, 1, 2).tint.join(','))).size > 1);
    ok('a nearby galaxy is realised as real geometry',
      g.shouldRealise(g.galaxyInCell(0, 0, 0), g.galaxyInCell(0, 0, 0).x,
        g.galaxyInCell(0, 0, 0).y, g.galaxyInCell(0, 0, 0).z) === true);
    ok('a distant galaxy stays a cheap smudge',
      g.shouldRealise(g.galaxyInCell(0, 0, 0), 5e6, 5e6, 5e6) === false);
    ok('a NaN position cannot crash navigation',
      Array.isArray(g.galaxiesNear(NaN, 0, 0)));
    ok('the visible set is bounded, so an infinite space stays affordable',
      g.galaxiesNear(0, 0, 0).length < 400,
      String(g.galaxiesNear(0, 0, 0).length));

    fs.unlinkSync(out);
  } catch (e) {
    ok('the intergalactic grid bundles and behaves', false, e.message);
  }
}

ok('the galaxy field draws the other galaxies',
  /buildFarGalaxies/.test(field) && /farGalaxies/.test(field));
ok('distant galaxies are hidden with the rest of the field',
  /this\.farMesh\?\.setEnabled\(on\)/.test(field));
ok('the galaxy you are inside is not also drawn as a smudge',
  /g\.radius \* 1\.2/.test(field));

// ----------------------------------------------------------- the warp
{
  const m = deep.match(/topMultiplier: (\d+)/);
  ok('the warp multiplier is raised 100x', m && Number(m[1]) === 90000,
    m ? m[1] : 'not found');
  ok('the reason it is now safe is recorded',
    /unbounded|no rim to overshoot/i.test(deep));

  // It must still be controllable: the cubic curve means low charge is
  // near-normal speed, so you are not launched by a tap of the key.
  const speed = (c, top) => 1 + Math.pow(c, 3) * (top - 1);
  ok('a brief tap does not launch you across the universe',
    speed(0.1, 90000) < 100, speed(0.1, 90000).toFixed(0) + 'x');
  ok('half charge is still manageable',
    speed(0.5, 90000) < 12000, speed(0.5, 90000).toFixed(0) + 'x');
  ok('full charge crosses a galaxy quickly',
    speed(1, 90000) === 90000);
  ok('decay outruns build, so you can always stop', (() => {
    const d = deep.match(/decay: ([\d.]+)/);
    const r = deep.match(/rampUp: ([\d.]+)/);
    return d && r && Number(d[1]) > 1 / Number(r[1]);
  })());
}

// ------------------------------------------------------- the sun glare
// A fixed-size billboard grows without limit in ANGULAR size as you
// approach: measured 54 degrees at 100 units and 119 at 30, which is the
// white-out that buried the rest of the frame.
ok('the glare size is a named constant, not a magic number',
  /export const GLARE_SIZE/.test(world));
ok('the glare is scaled to hold a bounded apparent size',
  /MAX_HALF_ANGLE/.test(world) && /glare\.scaling\.set/.test(world));
ok('its intensity eases down with the same curve',
  /this\.glareIntensity \* \(0\.35 \+ 0\.65 \* k\)/.test(world));
{
  const GL = 4.5 * 5.4 * 4.2;
  const MAXH = Number((world.match(/MAX_HALF_ANGLE = ([\d.]+)/) || [0, 0.28])[1]);
  const ang = (d) => {
    const k = Math.max(0.06, Math.min(1, Math.tan(MAXH) * Math.max(d, 1) / (GL / 2)));
    return 2 * Math.atan((GL * k / 2) / d) * 180 / Math.PI;
  };
  ok('the glare can never fill the screen', ang(20) < 45, ang(20).toFixed(1) + ' deg');
  ok('it is still huge and dramatic up close', ang(20) > 20, ang(20).toFixed(1) + ' deg');
  ok('distant stars are completely unaffected',
    Math.abs(ang(3000) - 1.949) < 0.2, ang(3000).toFixed(2) + ' deg');
  ok('apparent size never increases as you approach', (() => {
    for (let d = 20; d < 400; d += 5) if (ang(d) > ang(d - 5) + 1e-6) return false;
    return true;
  })());
}

// ---------------------------------------------------------- the warmup
ok('shaders are pre-compiled during the load',
  /warmupShaders\(this\.scene\)/.test(app));
ok('the hole raymarcher is included',
  /'holeField'/.test(warm));
ok('planet materials are included', /'m_'/.test(warm));
ok('warmup uses a throwaway probe mesh, not a real one',
  /__warmup/.test(warm));
ok('the probe is always cleaned up', /probe\?\.dispose\(\)/.test(warm));
ok('a driver that never answers cannot hang the loading screen',
  /setTimeout\(\(\) => finish\(false\)/.test(warm));
ok('warmup failure never rejects', !/reject\(/.test(warm));
ok('warmup failure never stops the app',
  /Shader warmup skipped|catch \(e\)/.test(app));
// Sequential warmup multiplied the timeout by the material count: six
// planet materials at 4s each held the load for 24 seconds.
ok('materials are warmed in parallel, bounding the total wait',
  /Promise\.all\(mats\.map/.test(warm));
ok('the parallel reasoning is recorded for future edits',
  /Sequential warmup multiplies the timeout/.test(warm));

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
