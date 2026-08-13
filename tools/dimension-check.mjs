/**
 * DimensionSystem verification — the infinite-dimension generator must be
 * genuinely varied AND perfectly reproducible, or "go back through the same
 * tear" cannot work.
 * Run: node tools/dimension-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/DimensionSystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/dim-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  generateDimension, dimensionChain, descend, tearSideways,
  makeRng, hashSeed, describeDimension, ALL_TRAITS
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

console.log('\n— the RNG is deterministic and well distributed —');
{
  const a = makeRng(12345), b = makeRng(12345);
  ok('same seed gives the same sequence',
     Array.from({ length: 50 }, () => a()).join() ===
     Array.from({ length: 50 }, () => b()).join());
  const c = makeRng(12346);
  ok('a different seed diverges', makeRng(12345)() !== c());

  const r = makeRng(7);
  const vals = Array.from({ length: 20000 }, () => r());
  ok('all values are in [0,1)', vals.every((v) => v >= 0 && v < 1));
  const mean = vals.reduce((x, y) => x + y, 0) / vals.length;
  ok(`mean is near 0.5 (${mean.toFixed(4)})`, Math.abs(mean - 0.5) < 0.02);
  // chi-square-ish bucket check
  const buckets = new Array(10).fill(0);
  vals.forEach((v) => buckets[Math.floor(v * 10)]++);
  const expected = vals.length / 10;
  const maxDev = Math.max(...buckets.map((b2) => Math.abs(b2 - expected) / expected));
  ok(`distribution is uniform (max bucket deviation ${(maxDev * 100).toFixed(1)}%)`,
     maxDev < 0.08);
}

console.log('\n— string seeds —');
{
  ok('hashing is stable', hashSeed('bloodstream') === hashSeed('bloodstream'));
  ok('different strings hash differently', hashSeed('a') !== hashSeed('b'));
  ok('hash is a uint32',
     Number.isInteger(hashSeed('x')) && hashSeed('x') >= 0 && hashSeed('x') <= 0xffffffff);
}

console.log('\n— dimensions are reproducible —');
{
  const a = generateDimension(999, 3);
  const b = generateDimension(999, 3);
  ok('the same seed and depth give an identical dimension',
     JSON.stringify(a) === JSON.stringify(b));
  ok('the id encodes the seed', a.id.includes((999).toString(36)));
  const c = generateDimension(1000, 3);
  ok('a different seed gives a different dimension',
     JSON.stringify(a) !== JSON.stringify(c));
}

console.log('\n— dimensions are actually varied —');
{
  const specs = Array.from({ length: 400 }, (_, i) => generateDimension(i * 7919, 6));
  const names = new Set(specs.map((s) => s.name));
  ok(`many distinct archetypes appear (${names.size})`, names.size >= 8, [...names].join(', '));
  const ids = new Set(specs.map((s) => s.id));
  ok('every dimension has a unique id', ids.size === specs.length);
  const traitUnion = new Set(specs.flatMap((s) => s.traits));
  ok(`a wide range of traits is reachable (${traitUnion.size}/${ALL_TRAITS.length})`,
     traitUnion.size >= 15);
  const grav = specs.map((s) => s.gravity);
  ok('gravity varies', new Set(grav.map((g) => g.toFixed(2))).size > 50);
  ok('some dimensions invert gravity', grav.some((g) => g < 0));
  ok('some dimensions keep gravity normal', grav.some((g) => g > 0));
}

console.log('\n— every generated spec is structurally valid —');
{
  let bad = [];
  for (let i = 0; i < 600; i++) {
    const s = generateDimension(i * 104729, i % 12);
    if (!s.name || !s.id || !s.glyph) bad.push('missing identity @' + i);
    if (!Array.isArray(s.traits) || s.traits.length === 0) bad.push('no traits @' + i);
    if (!Array.isArray(s.palette) || s.palette.length < 3) bad.push('thin palette @' + i);
    if (!s.palette.every((c) => c.length === 3 && c.every((v) => v >= 0 && v <= 1)))
      bad.push('palette out of range @' + i);
    if (!Array.isArray(s.shapes) || s.shapes.length === 0) bad.push('no shapes @' + i);
    if (!Number.isFinite(s.gravity) || !Number.isFinite(s.timeScale)) bad.push('bad physics @' + i);
    if (s.timeScale <= 0) bad.push('non-positive timeScale @' + i);
    if (s.objectCount < 1) bad.push('no objects @' + i);
    if (s.objectScale <= 0) bad.push('bad scale @' + i);
    if (s.weirdness < 0 || s.weirdness > 1) bad.push('weirdness out of range @' + i);
    if (s.fogDensity < 0) bad.push('negative fog @' + i);
  }
  ok('600 generated dimensions are all valid', bad.length === 0, bad.slice(0, 4).join(' | '));
}

console.log('\n— palettes are never black (the no-black-UI rule) —');
{
  let dark = 0;
  for (let i = 0; i < 500; i++) {
    const s = generateDimension(i * 31337, i % 10);
    // at least one palette entry must be visibly bright
    const brightest = Math.max(...s.palette.map((c) => c[0] + c[1] + c[2]));
    if (brightest < 0.35) dark++;
  }
  ok('no dimension renders as an all-black palette', dark === 0, `${dark} too dark`);
}

console.log('\n— depth gates the strange archetypes —');
{
  const shallow = Array.from({ length: 300 }, (_, i) => generateDimension(i * 613, 0));
  ok('depth 0 never yields the deepest archetypes',
     !shallow.some((s) => s.name === 'The Primordial' || s.name === 'The Gap'),
     [...new Set(shallow.map((s) => s.name))].join(','));
  const deep = Array.from({ length: 300 }, (_, i) => generateDimension(i * 613, 8));
  ok('deep dives can reach The Primordial', deep.some((s) => s.name === 'The Primordial'));
  ok('deep dives can reach The Gap', deep.some((s) => s.name === 'The Gap'));
}

console.log('\n— going deep enough sends time backwards —');
{
  const shallow = Array.from({ length: 200 }, (_, i) => generateDimension(i * 37, 0));
  const deep = Array.from({ length: 200 }, (_, i) => generateDimension(i * 37, 9));
  const shallowRev = shallow.filter((s) => s.timeDirection === -1).length / shallow.length;
  const deepRev = deep.filter((s) => s.timeDirection === -1).length / deep.length;
  ok(`reversed time is far more common when deep (${(shallowRev * 100).toFixed(0)}% -> ${(deepRev * 100).toFixed(0)}%)`,
     deepRev > shallowRev + 0.25);
  ok('time-reversed dimensions are set in the past',
     deep.filter((s) => s.timeDirection === -1).every((s) => s.timeEra < 0));
  ok('forward dimensions are set in the present',
     deep.filter((s) => s.timeDirection === 1).every((s) => s.timeEra === 0));
  const eras = deep.filter((s) => s.timeEra < 0).map((s) => s.timeEra);
  const deeper = Array.from({ length: 200 }, (_, i) => generateDimension(i * 37, 14))
    .filter((s) => s.timeEra < 0).map((s) => s.timeEra);
  if (eras.length && deeper.length) {
    const avg = (a) => a.reduce((x, y) => x + y, 0) / a.length;
    ok('deeper dives reach further into the past', avg(deeper) < avg(eras),
       `${avg(eras).toFixed(0)} vs ${avg(deeper).toFixed(0)}`);
  }
}

console.log('\n— weirdness escalates with depth —');
{
  const avgW = (d) => {
    const a = Array.from({ length: 200 }, (_, i) => generateDimension(i * 71, d).weirdness);
    return a.reduce((x, y) => x + y, 0) / a.length;
  };
  const w0 = avgW(0), w5 = avgW(5), w10 = avgW(10);
  ok(`weirdness grows with depth (${w0.toFixed(2)} < ${w5.toFixed(2)} < ${w10.toFixed(2)})`,
     w0 < w5 && w5 < w10);
  ok('weirdness stays clamped to 1', avgW(60) <= 1);
}

console.log('\n— the descent chain —');
{
  const chain = dimensionChain(4242, 7);
  ok('the chain has one entry per level', chain.length === 8);
  ok('depths increase monotonically', chain.every((s, i) => s.depth === i));
  ok('every level in the chain is distinct',
     new Set(chain.map((s) => s.id)).size === chain.length);
  const again = dimensionChain(4242, 7);
  ok('the same journey replays identically',
     JSON.stringify(chain) === JSON.stringify(again));
  const other = dimensionChain(4243, 7);
  ok('a different origin gives a different journey',
     JSON.stringify(chain) !== JSON.stringify(other));
}

console.log('\n— descend and sideways tears —');
{
  const a = generateDimension(888, 2);
  const deeper = descend(a);
  ok('descend increases depth', deeper.depth === 3);
  ok('descend is deterministic', descend(a).id === deeper.id);
  ok('descend lands somewhere new', deeper.id !== a.id);
  ok('descend matches the chain',
     dimensionChain(888, 3)[3].depth === 3);

  const side = tearSideways(a);
  ok('a sideways tear keeps the same depth', side.depth === a.depth);
  ok('a sideways tear lands in a different reality', side.id !== a.id);
  ok('sideways tears are deterministic', tearSideways(a).id === side.id);
  ok('tearing twice returns somewhere consistent',
     tearSideways(tearSideways(a)).id === tearSideways(tearSideways(a)).id);
}

console.log('\n— the UI description —');
{
  const s = generateDimension(555, 8);
  const d = describeDimension(s);
  ok('description includes the name', !!d.Dimension);
  ok('description includes a readable seed', /^[0-9A-Z]+$/.test(d.Seed));
  ok('description includes traits', d.Traits.length > 0);
  ok('description explains the time direction', /forwards|backwards/.test(d.Time));
  const rev = Array.from({ length: 60 }, (_, i) => generateDimension(i * 13, 12))
    .find((x) => x.timeDirection === -1);
  if (rev) ok('reversed dimensions say so in the era field',
              /before now/.test(describeDimension(rev).Era));
}

console.log('\n— extreme inputs cannot break generation —');
{
  const cases = [0, -1, 4294967296, -2147483648, 1.5, NaN];
  let bad = [];
  for (const seed of cases) {
    for (const depth of [0, -5, 1000, 2.7, NaN]) {
      try {
        const s = generateDimension(seed, depth);
        if (!s || !s.name || !Number.isFinite(s.gravity) || s.depth < 0) {
          bad.push(`${seed}/${depth}`);
        }
      } catch (e) {
        bad.push(`${seed}/${depth} threw ${e.message}`);
      }
    }
  }
  ok('every extreme seed/depth combination still produces a valid dimension',
     bad.length === 0, bad.slice(0, 3).join(' | '));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
