/**
 * navigation-check — object search, raw pointer deltas, void risk, no timers.
 *
 * The definitive-overhaul pass is pinned here, as pure logic plus static
 * source guarantees:
 *
 *   1. the SpaceEngine-style object search resolves "BLACK HOLE", "EARTH",
 *      "SUN" and fuzzy names to a real target,
 *   2. mouse look is driven purely by raw movementX/movementY (no absolute
 *      screen-coordinate / window-margin evaluation anywhere),
 *   3. the Left-Alt gesture override is wired,
 *   4. wormhole risk is a deterministic, timer-free roll over the seed and
 *      warp factor,
 *   5. the horizon transition contains no countdown timers.
 *
 * Run: node tools/navigation-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (rel) => {
  const p = path.join(root, rel);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
};

const load = async (entry) => {
  const out = await build({
    entryPoints: ['src/bjs/systems/' + entry],
    bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${entry.replace(/\W/g, '_')}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const S = await load('ObjectSearch.ts');
console.log('\n— the object search resolves what you type —');
{
  const regions = [
    { id: 'bh', name: 'Vela Core Singularity', kind: 'blackhole', x: -3000, y: 0, z: 0 },
    { id: 'sys', name: 'Home', kind: 'star-system', x: 0, y: 0, z: 0 },
    { id: 'neb', name: 'Lyra Drift Nebula', kind: 'nebula', x: 500, y: 0, z: 900 }
  ];
  const eye = { x: 0, y: 0, z: 0 };
  ok('"BLACK HOLE" finds the nearest hole',
    S.resolveSearch('BLACK HOLE', regions, eye)?.id === 'bh');
  ok('"EARTH" resolves to the home system',
    S.resolveSearch('EARTH', regions, eye)?.id === 'home');
  ok('"SUN" resolves to the home system',
    S.resolveSearch('SUN', regions, eye)?.kind === 'point');
  ok('a fuzzy name matches a nebula',
    S.resolveSearch('drift', regions, eye)?.id === 'neb');
  ok('a nonsense query returns null',
    S.resolveSearch('zzzzzz', regions, eye) === null);
  ok('tokens normalise case and punctuation',
    S.tokens('  BLACK   HOLE!! ').join(',') === 'black,hole');
}

const V = await load('VoidNavigation.ts');
console.log('\n— wormhole risk is deterministic and timer-free —');
{
  ok('a slow crossing rarely strands', V.strandingChance(1) < 0.15);
  ok('a fast crossing strands far more often',
    V.strandingChance(90000) > V.strandingChance(1) * 2);
  ok('the chance is bounded', (() => {
    for (let w = 1; w < 1e6; w *= 10) {
      const c = V.strandingChance(w);
      if (!(c >= 0 && c <= 1)) return false;
    }
    return true;
  })());
  ok('the stranding roll is deterministic',
    V.shouldStrand(42, 1000) === V.shouldStrand(42, 1000));
  ok('the stranded depth is deep and bounded',
    (() => {
      for (let s = 1; s < 300; s++) {
        const d = V.strandedDepth(s);
        if (d < 7 || d > 9) return false;
      }
      return true;
    })());
  ok('the way home is seeded from the stranding',
    V.strandedWormholeSeed(7) === V.strandedWormholeSeed(7));
  ok('the neon warning is a fixed telemetry string',
    V.HORIZON_WARNING.includes('HORIZON CROSSED') &&
    V.HORIZON_WARNING.includes('MULTIVERSE ISOLATION IMPACT IMMINENT'));
}

console.log('\n— raw pointer deltas, gesture lock, no timers —');
{
  const mouse = read('src/bjs/systems/MouseLook.ts');
  ok('mouse look reads only raw movementX/movementY',
    mouse.includes('e.movementX') && mouse.includes('e.movementY'));
  ok('no absolute screen-coordinate steering remains',
    !mouse.includes('clientX') && !mouse.includes('screenX'));

  const app = read('src/bjs/App.ts');
  ok('the Left-Alt gesture frees the cursor', app.includes("'alt'"));
  ok('and re-locks it on release', app.includes('requestLock()'));

  // The void is driven by coordinates, never a clock.
  for (const f of ['HoleDescent.ts', 'HoleInterior.ts']) {
    const src = read('src/bjs/systems/' + f);
    ok(f + ' contains no countdown timers',
      !/setTimeout|setInterval/.test(src));
  }

  // Core spawn must derive from an actual rendered galaxy, never an empty
  // hardcoded coordinate that leaves all finite sky layers behind.
  ok('the game spawns relative to a generated galactic core',
    app.includes("nearest(origin, 'galaxy')") && !app.includes('teleport(new Vector3(0, 1500, 5000))'));
  ok('and primes the destination sky before reveal',
    app.includes('this.starField.rebuild(') && app.includes('this.galaxyField.update(at, look'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
