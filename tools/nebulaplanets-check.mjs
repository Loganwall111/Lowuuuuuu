/**
 * nebulaplanets-check — the coloured nebulae and the per-cell planet roster.
 *
 * Two defects are pinned here.
 *
 * 1. The gas clouds lost their colour. `nebulaColor` interpolated a single
 *    warm/cool pair whose endpoints were BOTH magenta-violet, so every point
 *    in the galaxy landed on one straight line through colour space. Measured
 *    mean hue was (1.00, 0.33, 0.82) across 36 quantised colours - a flat pink
 *    wash. Real emission nebulae are several ionisation species at fixed
 *    wavelengths, and they separate in space.
 *
 * 2. The gas also became invisible. Point size was cut 90 -> 4 to kill the
 *    magenta saturation, which worked, but overshot: 4px covers 1.6% of the
 *    screen. The fix is size, not exposure - raising the colour instead barely
 *    moves coverage while driving peak intensity straight back into clipping.
 *
 * 3. The planet roster was a hardcoded array of seven named worlds, so every
 *    system in an infinite universe was the same system.
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { build } from 'esbuild';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
let pass = 0, fail = 0;
const ok = (label, cond, note) => {
  if (cond) { pass++; console.log('  PASS  ' + label + (note ? '  (' + note + ')' : '')); }
  else { fail++; console.log('FAIL: ' + label + (note ? ' — ' + note : '')); }
};
const read = (rel) => {
  const p = path.join(root, rel);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
};
const bundle = async (rel) => {
  const out = path.join('/tmp', 'np-' + path.basename(rel, '.ts') + '-' + Date.now() + '.mjs');
  await build({ entryPoints: [path.join(root, rel)], bundle: true, format: 'esm',
    platform: 'node', logLevel: 'error', outfile: out });
  const m = await import(out);
  fs.unlinkSync(out);
  return m;
};

console.log('\n--- 1. the nebulae are coloured again ---');

const shape = await bundle('src/bjs/systems/GalaxyShape.ts');
const { nebulaColor, nebulaDensity, MILKY_WAY } = shape;

/** Sample the gas field exactly the way GalaxyField populates it. */
function sampleGas(seed = 12345, n = 9000) {
  let x = seed >>> 0;
  const rnd = () => ((x = (Math.imul(x, 1664525) + 1013904223) >>> 0) / 4294967296);
  const cfg = MILKY_WAY, pts = [];
  for (let i = 0; i < n; i++) {
    for (let t = 0; t < 24; t++) {
      const r = 2000 + rnd() * 48000;
      const th = rnd() * Math.PI * 2;
      const h = (rnd() - 0.5) * 2 * r * cfg.thickness * 2.2;
      const px = Math.cos(th) * r, pz = Math.sin(th) * r;
      const d = nebulaDensity(px, h, pz, cfg);
      if (rnd() < d) {
        pts.push({ p: [px, h, pz], c: nebulaColor(d, px, h, pz, cfg),
          a: Math.min(0.5, d * 0.6) });
        break;
      }
    }
  }
  return pts;
}

const gas = sampleGas();
ok('the gas field still places points', gas.length > 5000, gas.length + ' points');

function families(pts) {
  const f = { crimson: 0, teal: 0, orange: 0, blue: 0, n: 0 };
  for (const q of pts) {
    const m = Math.max(...q.c);
    if (!(m > 1e-4)) continue;
    f.n++;
    const [R, G, B] = q.c.map((v) => v / m);
    if (G > 0.55 && B > 0.45 && R < 0.65) f.teal++;
    else if (R > 0.8 && G < 0.45 && B < 0.5) f.crimson++;
    else if (R > 0.7 && G > 0.35 && G < 0.75) f.orange++;
    else if (B > 0.65 && R < 0.75) f.blue++;
  }
  return f;
}
const fam = families(gas);
ok('H-alpha crimson gas exists', fam.crimson > 0,
  (100 * fam.crimson / fam.n).toFixed(1) + '%');
ok('O-III teal gas exists — the line that breaks the pink wash', fam.teal > 0,
  (100 * fam.teal / fam.n).toFixed(1) + '%');
ok('S-II orange gas exists', fam.orange > 0,
  (100 * fam.orange / fam.n).toFixed(1) + '%');
ok('at least three emission species are present at once',
  [fam.crimson, fam.teal, fam.orange, fam.blue].filter((v) => v > 0).length >= 3);

// The specific regression: a single violet line through colour space.
{
  const sum = [0, 0, 0];
  for (const q of gas) for (let k = 0; k < 3; k++) sum[k] += q.c[k];
  const mean = sum.map((v) => v / gas.length);
  const mx = Math.max(...mean);
  const norm = mean.map((v) => v / mx);
  // Old value was green/red = 0.33. Anything that low is the flat pink wash.
  ok('green is no longer crushed out of the mean hue', norm[1] > 0.45,
    'normalised mean ' + norm.map((v) => v.toFixed(2)).join(', '));
}
{
  const uniq = new Set(gas.map((q) => q.c.map((v) => Math.round(v * 12)).join(',')));
  ok('the palette is genuinely varied, not a single ramp', uniq.size > 80,
    uniq.size + ' distinct quantised colours');
}
ok('colours stay in gamut', gas.every((q) =>
  q.c.every((v) => Number.isFinite(v) && v >= 0 && v <= 1)));
ok('denser gas is still brighter', (() => {
  const lo = nebulaColor(0.15, 5000, 0, 5000, MILKY_WAY).reduce((a, b) => a + b, 0);
  const hi = nebulaColor(0.95, 5000, 0, 5000, MILKY_WAY).reduce((a, b) => a + b, 0);
  return hi > lo;
})());
ok('nebula colour is deterministic', (() => {
  const a = nebulaColor(0.7, 1234, 56, -7890, MILKY_WAY);
  const b = nebulaColor(0.7, 1234, 56, -7890, MILKY_WAY);
  return a.every((v, i) => v === b[i]);
})());

// Species must run in STRIPS, not flicker point to point. Neighbouring
// samples should usually share a hue; if the colour field were high
// frequency the nebula would look like static rather than like structure.
ok('ionisation runs in flowing strips, not per-point static', (() => {
  const cfg = MILKY_WAY;
  let same = 0, tot = 0;
  const hue = (x, y, z) => {
    const c = nebulaColor(0.8, x, y, z, cfg);
    const m = Math.max(...c);
    if (!(m > 1e-4)) return -1;
    const [R, G, B] = c.map((v) => v / m);
    return G > 0.55 && B > 0.45 && R < 0.65 ? 0 : R > 0.8 ? 1 : 2;
  };
  for (let i = 0; i < 1200; i++) {
    const x = (i * 137.5) % 30000 - 15000, z = (i * 311.7) % 30000 - 15000;
    const h0 = hue(x, 0, z), h1 = hue(x + 260, 0, z + 260);   // one cell apart
    if (h0 < 0 || h1 < 0) continue;
    tot++; if (h0 === h1) same++;
  }
  return tot > 0 && same / tot > 0.7;
})());

console.log('\n--- 2. the gas is visible without saturating ---');

const field = read('src/bjs/systems/GalaxyField.ts');
const gasSize = Number((field.match(/this\.applyState\(gasMesh, ([\d.]+)\)/) || [])[1]);
ok('the gas point size is read from source', Number.isFinite(gasSize), String(gasSize));

// Rasterise the real point set through the real proxy projection and count
// how much of the frame lights up and how much clips. This is the measurement
// the size was chosen from, so it is the one worth pinning.
const gfield = await bundle('src/bjs/systems/GalaxyField.ts');
function raster(size, gain) {
  const W = 480, H = 270, f = (H / 2) / Math.tan(0.4);
  const buf = new Float64Array(W * H * 3);
  const s = size * (H / 1080);           // same angular size, cheaper buffer
  for (const q of gas) {
    const d = Math.hypot(...q.p);
    const pr = gfield.proxyRadius(d), k = pr / d;
    const X = q.p[0] * k, Y = q.p[1] * k, Z = q.p[2] * k;
    if (Z <= 1) continue;
    const sx = Math.round(W / 2 + f * X / Z), sy = Math.round(H / 2 - f * Y / Z);
    const h = Math.max(0, Math.floor(s / 2));
    for (let dy = -h; dy <= h; dy++) for (let dx = -h; dx <= h; dx++) {
      const px = sx + dx, py = sy + dy;
      if (px < 0 || py < 0 || px >= W || py >= H) continue;
      const o = (py * W + px) * 3;
      for (let c = 0; c < 3; c++) buf[o + c] += q.c[c] * q.a * gain;
    }
  }
  let lit = 0, sat = 0;
  for (let i = 0; i < W * H; i++) {
    const m = Math.max(buf[i * 3], buf[i * 3 + 1], buf[i * 3 + 2]);
    if (m > 0.004) lit++;
    if (m >= 1) sat++;
  }
  return { lit: 100 * lit / (W * H), sat: 100 * sat / (W * H) };
}

const now = raster(gasSize, 1);
ok('the nebulae actually cover a visible part of the sky', now.lit > 4,
  now.lit.toFixed(2) + '% of frame lit');
ok('and they do not blow out into the magenta smear', now.sat < 0.2,
  now.sat.toFixed(3) + '% of frame clipped');

{
  const tiny = raster(4, 1);
  ok('the 4px setting really was too small to see', tiny.lit < now.lit,
    '4px lights ' + tiny.lit.toFixed(2) + '% vs ' + now.lit.toFixed(2) + '%');
  const huge = raster(90, 1);
  ok('the original 90px setting really did saturate', huge.sat > 1,
    '90px clips ' + huge.sat.toFixed(1) + '%');
  // The brief proposed an 8x exposure multiplier. Show why size was chosen.
  const exposed = raster(4, 8);
  ok('an 8x exposure boost would clip instead of revealing', exposed.sat > now.sat,
    '4px@8x clips ' + exposed.sat.toFixed(3) + '% and lights only ' +
    exposed.lit.toFixed(2) + '%');
}

ok('the pink-glitch reasoning is still recorded for future edits',
  /THE PINK GLITCH/.test(field));

console.log('\n--- 3. planets vary per 260,000-unit cell ---');

const grid = await bundle('src/bjs/systems/IntergalacticGrid.ts');
ok('the cell matrix is still 260,000 units', grid.CELL_SIZE === 260000,
  String(grid.CELL_SIZE));

const world = await bundle('src/bjs/worlds/PlanetaryWorld.ts');
const { planetsForCell } = world;
ok('planetsForCell is exported', typeof planetsForCell === 'function');

const cells = [[0, 0, 0], [1, 0, 0], [0, 1, 0], [0, 0, 1], [-3, 2, 7], [12, -5, 9]];
const rosters = cells.map((c) => planetsForCell(...c));

ok('every cell produces a populated system',
  rosters.every((r) => r.length >= 5 && r.length <= 8),
  rosters.map((r) => r.length).join(','));

ok('the same cell always returns the same system', (() => {
  const a = JSON.stringify(planetsForCell(5, -2, 9));
  const b = JSON.stringify(planetsForCell(5, -2, 9));
  return a === b;
})());

ok('neighbouring cells produce different systems', (() => {
  const seen = new Set(rosters.map((r) => JSON.stringify(r)));
  return seen.size === rosters.length;
})());

ok('a cell far away is different again', (() => {
  const a = JSON.stringify(planetsForCell(0, 0, 0));
  const b = JSON.stringify(planetsForCell(400, -177, 903));
  return a !== b;
})());

// The variety the brief asked for, by name.
{
  const types = {};
  for (let i = 0; i < 400; i++) {
    for (const p of planetsForCell(i % 17 - 8, (i * 7) % 13 - 6, (i * 3) % 11 - 5)) {
      types[p.type] = (types[p.type] || 0) + 1;
    }
  }
  // PlanetKind: 0 rocky, 1 terran, 2 ice, 3 gas, 4 lava, 5 desert
  ok('gas giants are generated', (types[3] || 0) > 0, String(types[3] || 0));
  ok('volcanic worlds are generated', (types[4] || 0) > 0, String(types[4] || 0));
  ok('lush terra worlds are generated', (types[1] || 0) > 0, String(types[1] || 0));
  ok('frozen ice worlds are generated', (types[2] || 0) > 0, String(types[2] || 0));
  ok('rocky and desert worlds are generated',
    (types[0] || 0) > 0 && (types[5] || 0) > 0);
  ok('no single class dominates the universe', (() => {
    const vals = Object.values(types);
    const tot = vals.reduce((a, b) => a + b, 0);
    return Math.max(...vals) / tot < 0.45;
  })());
}

ok('gas giants get thick rings more often than rocky worlds do', (() => {
  let gasRing = 0, gasTot = 0, rockRing = 0, rockTot = 0;
  for (let i = 0; i < 300; i++) {
    for (const p of planetsForCell(i, i * 3 - 40, i * 7 + 11)) {
      if (p.type === 3) { gasTot++; if (p.ring) gasRing++; }
      if (p.type === 0) { rockTot++; if (p.ring) rockRing++; }
    }
  }
  return gasTot > 0 && rockTot > 0 && (gasRing / gasTot) > (rockRing / rockTot);
})());

ok('every system has exactly one inhabited world', (() => {
  for (let i = 0; i < 200; i++) {
    const r = planetsForCell(i, i * 2, i * 3);
    if (r.filter((p) => p.inhabited).length !== 1) return false;
  }
  return true;
})());

ok('only the inhabited world lights up at night', (() => {
  for (let i = 0; i < 120; i++) {
    for (const p of planetsForCell(i * 5, -i, i + 3)) {
      if (!p.inhabited && p.lights > 0) return false;
      if (p.inhabited && !(p.lights > 0)) return false;
    }
  }
  return true;
})());

ok('the inhabited world is always a terran world', (() => {
  for (let i = 0; i < 120; i++) {
    const h = planetsForCell(i * 3 + 1, i, -i).find((p) => p.inhabited);
    if (!h || h.type !== 1) return false;
  }
  return true;
})());

ok('orbits increase outward and never collide', (() => {
  for (let i = 0; i < 200; i++) {
    const r = planetsForCell(i, -i * 2, i * 5);
    for (let k = 1; k < r.length; k++) {
      if (!(r[k].orbit > r[k - 1].orbit)) return false;
      // Must clear both bodies, or the spheres intersect on screen.
      if (r[k].orbit - r[k - 1].orbit < r[k].r + r[k - 1].r) return false;
    }
  }
  return true;
})());

ok('outer worlds orbit more slowly than inner ones', (() => {
  for (let i = 0; i < 120; i++) {
    const r = planetsForCell(i + 7, i, i * 2);
    for (let k = 1; k < r.length; k++) if (!(r[k].speed < r[k - 1].speed)) return false;
  }
  return true;
})());

ok('every generated field is finite and sane', (() => {
  for (let i = 0; i < 200; i++) {
    for (const p of planetsForCell(i * 11 - 90, i - 40, i * 2)) {
      if (!(p.r > 0.3 && p.r < 4)) return false;
      if (!Number.isFinite(p.orbit) || !Number.isFinite(p.speed)) return false;
      if (!(p.clouds >= 0 && p.clouds <= 1.6)) return false;
      if (!(p.moons >= 0 && p.moons <= 4)) return false;
      if (![p.a, p.b].every((c) => c.length === 3 &&
        c.every((v) => v >= 0 && v <= 1))) return false;
      if (typeof p.name !== 'string' || !p.name.length) return false;
    }
  }
  return true;
})());

ok('planet colours are jittered, not copied from the class template', (() => {
  const gasA = [];
  for (let i = 0; i < 200; i++) {
    for (const p of planetsForCell(i, i * 2 + 1, i * 3)) {
      if (p.type === 3) gasA.push(p.a.join(','));
    }
  }
  return new Set(gasA).size > gasA.length * 0.5;
})());

ok('the world file no longer carries a hardcoded planet table',
  !/name: 'Terrapor'/.test(read('src/bjs/worlds/PlanetaryWorld.ts')));

ok('city lights are keyed to the inhabited world, not a literal name',
  !/b\.name === 'Terrapor'/.test(read('src/bjs/worlds/PlanetaryWorld.ts')));

console.log('\n--- 4. drifting cloud bands ---');

const shader = read('src/bjs/shaders/PlanetShader.ts');
ok('the cloud layer exists', /cloudAmt > 0\.01/.test(shader));
ok('a second fBm octave layer drives the cloud detail',
  /float cl2\s*=\s*fbm/.test(shader));
ok('the cloud field translates with the engine clock',
  /vec3 cp = p \* [\d.]+ \+ vec3\(time/.test(shader));
ok('the second layer drifts at a different rate, so bands shear',
  /cl2\s*=\s*fbm\(cp \* [\d.]+ - time/.test(shader));

const pworld = read('src/bjs/worlds/PlanetaryWorld.ts');
ok('planet materials are actually fed time every frame',
  /b\.mat\.setFloat\('time', this\.t\)/.test(pworld));
ok('and the clock advances with the world time scale',
  /this\.t \+= dt \* this\.p\.timeScale/.test(pworld));

console.log('\n' + pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
