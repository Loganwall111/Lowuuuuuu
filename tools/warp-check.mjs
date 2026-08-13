/**
 * warp-check — speed readouts, the warp effect, and rare exotic planets.
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry, tag) => {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${tag}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const { rollExotic, EXOTICS, EXOTIC_CHANCE } = await load('src/bjs/PlanetMaps.ts', 'maps');

console.log('— rare exotic planets —');
{
  ok('there are several oddities to find', EXOTICS.length >= 4);
  ok('the default chance is 1%', Math.abs(EXOTIC_CHANCE - 0.01) < 1e-9);

  for (const e of EXOTICS) {
    ok(`"${e.name}" has art and a description`,
       !!e.url && !!e.blurb && e.weight > 0);
    ok(`"${e.name}" art exists on disk`,
       fs.existsSync('public' + e.url) && fs.statSync('public' + e.url).size > 20000);
  }

  // Deterministic: the same planet is always the same planet.
  const a = rollExotic(12345, 1);
  const b = rollExotic(12345, 1);
  ok('a given seed always rolls the same surface',
     a && b && a.id === b.id);

  // Actually rare.
  let hits = 0;
  const N = 200000;
  for (let i = 1; i <= N; i++) if (rollExotic(i)) hits++;
  const rate = hits / N;
  ok(`oddities really are ~1% (${(rate * 100).toFixed(2)}%)`,
     rate > 0.005 && rate < 0.02, `${hits}/${N}`);

  // chance=0 disables, chance=1 forces
  ok('chance 0 never rolls one',
     [1, 2, 3, 99, 1000].every((s) => rollExotic(s, 0) === null));
  ok('chance 1 always rolls one',
     [1, 2, 3, 99, 1000].every((s) => rollExotic(s, 1) !== null));

  // every oddity must be reachable
  const seen = new Set();
  for (let i = 1; i < 4000; i++) {
    const e = rollExotic(i, 1);
    if (e) seen.add(e.id);
  }
  ok(`every oddity can actually be found (${seen.size}/${EXOTICS.length})`,
     seen.size === EXOTICS.length, [...seen].join(','));

  const cube = EXOTICS.find((e) => e.shape === 'cube');
  ok('at least one planet is not even round', !!cube);
}

console.log('\n— speed and distance readouts —');
{
  // Pull the formatters off Shell without a DOM by reading the source: they
  // are static and pure, so evaluate them directly.
  const src = fs.readFileSync('src/bjs/ui/Shell.ts', 'utf8');
  // Flight telemetry moved out of Shell's chip strip into FlightHUD, which
  // is a real instrument panel rather than three chips. The requirement is
  // that the readouts exist and are fed - not which file holds them.
  const hud = fs.readFileSync('src/bjs/ui/FlightHUD.ts', 'utf8');
  ok('the HUD has a speed field', hud.includes('id="fhSpd"'));
  ok('the HUD has a distance field', hud.includes('id="fhLocD"'));
  ok('the HUD names where you are', hud.includes('id="fhLoc"'));
  ok('the HUD shows navigation coordinates',
    hud.includes('id="fhX"') && hud.includes('id="fhY"') && hud.includes('id="fhZ"'));
  ok('the HUD shows warp charge', hud.includes('id="fhWrp"'));
  ok('every HUD group can be switched off individually',
    /setElement\(/.test(hud) && /DEFAULT_HUD_ELEMENTS/.test(hud));
  ok('there is a setFlight entry point', /setFlight\(/.test(src));

  // Import the real statics rather than re-parsing the source: a
  // hand-rolled extractor was fragile and tested itself, not the app.
  const shellSrc = await build({
    entryPoints: ['src/bjs/ui/Shell.ts'], bundle: true, format: 'esm',
    write: false, logLevel: 'error'
  });
  const sf = `/tmp/shell-fmt-${Date.now()}.mjs`;
  fs.writeFileSync(sf, shellSrc.outputFiles[0].text);
  let Shell = null;
  try { ({ Shell } = await import(sf)); } catch (e) { /* needs a DOM */ }

  const fsp = Shell?.formatSpeed;
  const fd = Shell?.formatDistance;
  ok('the formatters are importable statics', typeof fsp === 'function' && typeof fd === 'function');
  if (typeof fsp === 'function' && typeof fd === 'function') {
    ok('slow speeds read in units/sec', /u\/s/.test(fsp(4)));
    ok('very fast speeds read as a fraction of c', /c$/.test(fsp(3000)));
    ok('speed never renders NaN', !/NaN/.test(fsp(NaN)) && !/NaN/.test(fsp(Infinity)));
    ok('near distances read in units', /u$/.test(fd(50)));
    ok('mid distances read in AU', /AU$/.test(fd(5000)));
    ok('huge distances read in light years', /ly$/.test(fd(500000)));
    ok('distance never renders NaN', !/NaN/.test(fd(NaN)) && !/NaN/.test(fd(Infinity)));
    ok('readouts are monotonic', fd(10) !== fd(100000) && fsp(1) !== fsp(9000));
  }
}

console.log('\n— the warp effect —');
{
  const src = fs.readFileSync('src/bjs/systems/WarpSystem.ts', 'utf8');
  ok('warp engages on speed alone', /update\(\s*dt[^)]*speed/.test(src));
  ok('it eases rather than popping on', src.includes('this.amount +='));
  ok('streaks recycle instead of allocating', src.includes('Recycle'));
  ok('it is thin-instanced for one draw call', src.includes('thinInstanceSetBuffer'));
  ok('it can be switched off', src.includes('setEnabled'));
  ok('it reports itself in telemetry', src.includes('stats()'));

  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('the app owns a warp system', app.includes('new WarpSystem'));
  ok('warp is driven from the render loop', /this\.warp\.update\(/.test(app));
  ok('the HUD is fed real measured speed', app.includes('this.shell.setFlight('));
  ok('speed comes from actual travel, not a throttle value',
     app.includes('Vector3.Distance(eye, this.prevEye)'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
