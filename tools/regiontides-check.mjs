/**
 * RegionTides verification — in sandbox mode, worlds that stray too close to
 * a black hole must actually be dragged in and torn apart; in explorer mode
 * nothing may move at all.
 * Run: node tools/regiontides-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/RegionTides.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/rtides-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  RegionTides, cohesionOf, bodyFor, isVulnerable, describeRegionTide,
  INFLUENCE_RADII
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const hole = (x = 0) => ({ id: 'bh-1', position: { x, y: 0, z: 0 }, horizon: 40 });
const planet = (d, kind = 'planet') => ({
  id: 'p1', kind, name: 'Test World',
  position: { x: d, y: 0, z: 0 }, mass: 5e3, radius: 400, surfaceRadius: 90
});

console.log('\n— explorer mode never touches anything —');
{
  const t = new RegionTides();
  const p = planet(60);
  const before = { ...p.position };
  const res = t.update(0.016, [p], [hole()], false);
  ok('a disabled field reports nothing', res.length === 0);
  ok('and moves nothing', p.position.x === before.x);
  ok('nothing is ever consumed', t.drainConsumed().length === 0);
}

console.log('\n— a world far away is left alone —');
{
  const t = new RegionTides();
  const p = planet(40 * INFLUENCE_RADII * 2);
  const before = p.position.x;
  const res = t.update(0.016, [p], [hole()], true);
  ok('a distant world is not reported', res.length === 0);
  ok('and is not moved', p.position.x === before);
}

console.log('\n— a world too close is dragged in and torn apart —');
{
  const t = new RegionTides();
  const p = planet(40 * 6);
  const before = p.position.x;
  const res = t.update(0.05, [p], [hole()], true);
  ok('a nearby world is reported', res.length === 1);
  ok('it is being pulled toward the hole', p.position.x < before,
     `${before} -> ${p.position.x}`);
  ok('the report says how far it moved', res[0].drawnIn > 0);
  ok('it names which hole is doing it', res[0].holeId === 'bh-1');

  // Closer in, it comes apart.
  const q = planet(40 * 2.2);
  const r2 = new RegionTides().update(0.05, [q], [hole()], true);
  ok('very close, the world is disrupting', r2[0].disrupting, JSON.stringify(r2[0]));
  ok('and it is visibly shredded', r2[0].shredded > 0);
}

console.log('\n— crossing the horizon consumes a world —');
{
  const t = new RegionTides();
  const p = planet(10);   // inside the 40u horizon
  const res = t.update(0.05, [p], [hole()], true);
  ok('a world inside the horizon is consumed', res[0].consumed);
  const eaten = t.drainConsumed();
  ok('it is reported as eaten exactly once', eaten.length === 1);
  ok('draining clears the list', t.drainConsumed().length === 0);
  ok('the HUD line says what happened',
     /past the horizon/.test(describeRegionTide(res[0])));
}

console.log('\n— loosely bound worlds come apart first —');
{
  ok('a nebula is the least bound', cohesionOf('nebula') < cohesionOf('planet'));
  ok('a gas-poor rocky world outlasts an ocean',
     cohesionOf('terrain') > cohesionOf('ocean'));
  ok('a star system is the most tightly bound of the worlds',
     cohesionOf('star-system') > cohesionOf('planet'));

  // At one distance, the nebula should be shredding while the star is not.
  const d = 40 * 9;
  const t = new RegionTides();
  const neb = { ...planet(d, 'nebula'), id: 'n', name: 'Neb' };
  const star = { ...planet(d, 'star-system'), id: 's', name: 'Star' };
  const res = t.update(0.05, [neb, star], [hole()], true);
  const byId = Object.fromEntries(res.map((r) => [r.id, r]));
  ok('at the same distance the nebula is disrupting',
     byId.n && byId.n.disrupting, JSON.stringify(byId.n));
  ok('while the star system is not',
     !byId.s || !byId.s.disrupting, JSON.stringify(byId.s));
}

console.log('\n— black holes are not eaten by other black holes —');
{
  ok('a black hole is not vulnerable', !isVulnerable('blackhole'));
  ok('a dimension is not a place in this space', !isVulnerable('dimension'));
  ok('deep space cannot be torn apart', !isVulnerable('deep-space'));
  ok('a planet is vulnerable', isVulnerable('planet'));

  const t = new RegionTides();
  const bh = {
    id: 'bh-2', kind: 'blackhole', name: 'Other',
    position: { x: 50, y: 0, z: 0 }, mass: 9e3, radius: 620
  };
  const before = bh.position.x;
  const res = t.update(0.05, [bh], [hole()], true);
  ok('another black hole is never reported', res.length === 0);
  ok('and is never moved', bh.position.x === before);
}

console.log('\n— the strongest field wins, not merely the first —');
{
  const t = new RegionTides();
  const far = { id: 'bh-far', position: { x: 4000, y: 0, z: 0 }, horizon: 40 };
  const near = { id: 'bh-near', position: { x: 300, y: 0, z: 0 }, horizon: 40 };
  const p = planet(340);
  const res = t.update(0.05, [p], [far, near], true);
  ok('the closer hole is the one acting on it',
     res[0] && res[0].holeId === 'bh-near', JSON.stringify(res[0]));
}

console.log('\n— a world is never dragged past the hole it falls into —');
{
  const t = new RegionTides();
  const p = planet(41);   // just outside a 40u horizon
  for (let i = 0; i < 400; i++) t.update(0.1, [p], [hole()], true);
  const d = Math.abs(p.position.x);
  ok('it converges on the hole rather than shooting through it',
     Number.isFinite(p.position.x) && d < 41 && d >= 0,
     'ended at x=' + p.position.x.toFixed(3));
}

console.log('\n— garbage cannot break the field —');
{
  const t = new RegionTides();
  let bad = [];
  const junkRegions = [
    null, undefined, {},
    { id: 'a', kind: 'planet', name: 'a', position: { x: NaN, y: 0, z: 0 }, mass: 1, radius: 1 },
    { id: 'b', kind: 'planet', name: 'b', position: { x: 0, y: 0, z: 0 }, mass: 0, radius: 0 }
  ];
  const junkHoles = [
    null, undefined, {},
    { id: 'h', position: { x: 0, y: 0, z: 0 }, horizon: NaN },
    { id: 'h2', position: { x: 0, y: 0, z: 0 }, horizon: 0 },
    { id: 'h3', position: { x: 0, y: 0, z: 0 }, horizon: -5 }
  ];
  for (const dt of [NaN, -1, 0, 1e9]) {
    try {
      const res = t.update(dt, junkRegions, junkHoles, true);
      for (const r of res) {
        if (!Number.isFinite(r.stress) && r.stress !== Infinity) {
          bad.push('stress=' + r.stress);
        }
      }
    } catch (e) { bad.push('dt=' + dt + ' threw ' + e.message); }
  }
  try { t.update(0.016, null, null, true); } catch (e) { bad.push('null lists threw'); }
  ok('junk regions, holes and deltas are all survived',
     bad.length === 0, bad.slice(0, 3).join(' | '));

  // A NaN position must never be written back into a region.
  const p = planet(200);
  new RegionTides().update(0.05, [p], [{ id: 'x', position: { x: NaN, y: 0, z: 0 }, horizon: 40 }], true);
  ok('a hole with a NaN position cannot corrupt a world',
     Number.isFinite(p.position.x), 'x=' + p.position.x);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
