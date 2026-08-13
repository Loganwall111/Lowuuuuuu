/**
 * QualitySystem verification — presets and the adaptive controller.
 * The adaptive loop must defend the framerate without oscillating.
 * Run: node tools/quality-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/QualitySystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/qual-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { QualitySystem, QUALITY, QUALITY_ORDER } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const feed = (q, fps, seconds) => {
  const dt = 1 / fps;
  const n = Math.ceil(seconds / dt);
  let last = null;
  for (let i = 0; i < n; i++) { const r = q.sample(dt); if (r !== null) last = r; }
  return last;
};

console.log('\n— presets are ordered and coherent —');
{
  ok('all five presets exist', QUALITY_ORDER.length === 5);
  ok('every preset name resolves', QUALITY_ORDER.every((n) => !!QUALITY[n]));
  const scalings = QUALITY_ORDER.map((n) => QUALITY[n].scaling);
  ok(`scaling decreases monotonically toward higher quality (${scalings.join(' > ')})`,
     scalings.every((v, i) => i === 0 || v < scalings[i - 1]));
  const details = QUALITY_ORDER.map((n) => QUALITY[n].detail);
  ok('detail increases monotonically',
     details.every((v, i) => i === 0 || v > details[i - 1]));
  const bodies = QUALITY_ORDER.map((n) => QUALITY[n].maxBodies);
  ok('body budget increases monotonically',
     bodies.every((v, i) => i === 0 || v > bodies[i - 1]));
  ok('performance renders below native', QUALITY.performance.scaling > 1);
  ok('cinematic supersamples', QUALITY.cinematic.scaling < 1);
  ok('performance disables expensive effects',
     !QUALITY.performance.bloom && !QUALITY.performance.grain);
  ok('every preset keeps antialiasing on so edges never look broken',
     QUALITY_ORDER.every((n) => QUALITY[n].fxaa));
}

console.log('\n— selecting presets —');
{
  const q = new QualitySystem('balanced');
  ok('constructor honours the initial preset', q.current === 'balanced');
  ok('scaling follows the preset', q.scaling === QUALITY.balanced.scaling);
  q.set('cinematic');
  ok('set switches preset', q.current === 'cinematic');
  ok('set updates scaling', q.scaling === QUALITY.cinematic.scaling);
  q.set('nonsense');
  ok('an unknown preset is ignored', q.current === 'cinematic');
  const bad = new QualitySystem('garbage');
  ok('an invalid initial preset falls back to high', bad.current === 'high');
}

console.log('\n— shifting up and down the ladder —');
{
  const q = new QualitySystem('performance');
  q.shift(-1);
  ok('cannot shift below the lowest preset', q.current === 'performance');
  q.shift(1);
  ok('shift up moves one step', q.current === 'balanced');
  q.set('experimental');
  q.shift(1);
  ok('cannot shift above the highest preset', q.current === 'experimental');
  q.shift(-1);
  ok('shift down moves one step', q.current === 'cinematic');
}

console.log('\n— adaptive scaling is off unless enabled —');
{
  const q = new QualitySystem('high');
  const r = feed(q, 12, 10);
  ok('no adaptation while disabled', r === null);
  ok('scaling is untouched', q.scaling === QUALITY.high.scaling);
}

console.log('\n— adaptive scaling defends a low framerate —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  const before = q.scaling;
  const after = feed(q, 20, 12);       // sustained 20fps
  ok('sustained low fps raises scaling (renders smaller)', after !== null && after > before,
     `${before} -> ${after}`);
  ok('scaling never exceeds the cap', q.scaling <= q.opts.maxScaling + 1e-9);
}

console.log('\n— adaptive scaling reclaims quality when fast —');
{
  const q = new QualitySystem('performance');   // starts at 1.6
  q.adaptive = true;
  const before = q.scaling;
  const after = feed(q, 144, 20);
  ok('sustained high fps lowers scaling (renders sharper)',
     after !== null && after < before, `${before} -> ${after}`);
  ok('scaling never drops below the floor', q.scaling >= q.opts.minScaling - 1e-9);
}

console.log('\n— a steady target framerate produces no changes —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  feed(q, 60, 20);
  ok('60fps sits in the dead band and changes nothing', q.changes === 0, `${q.changes} changes`);
}

console.log('\n— one stutter cannot trigger a change —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  for (let i = 0; i < 29; i++) q.sample(1 / 60);
  const r = q.sample(1 / 4);           // a single 250ms hitch
  ok('a lone spike is rejected by the median filter', r === null);
  ok('scaling is unchanged after a spike', q.scaling === QUALITY.high.scaling);
}

console.log('\n— cooldown prevents oscillation —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  let changes = 0;
  const dt = 1 / 20;
  for (let i = 0; i < 20 / dt; i++) if (q.sample(dt) !== null) changes++;
  const maxPossible = Math.ceil(20 / q.opts.cooldown);
  ok(`changes are rate-limited by the cooldown (${changes} in 20s, max ~${maxPossible})`,
     changes <= maxPossible, `${changes}`);
}

console.log('\n— bad input cannot corrupt the controller —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  ok('zero dt is ignored', q.sample(0) === null);
  ok('negative dt is ignored', q.sample(-1) === null);
  ok('NaN dt is ignored', q.sample(NaN) === null);
  ok('Infinity dt is ignored', q.sample(Infinity) === null);
  ok('scaling is still finite', Number.isFinite(q.scaling));
}

console.log('\n— reset —');
{
  const q = new QualitySystem('high');
  q.adaptive = true;
  feed(q, 15, 12);
  q.reset();
  ok('reset restores the preset scaling', q.scaling === QUALITY.high.scaling);
  ok('reset clears the sample window', q.medianFps() === 0);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
