/**
 * progression-check — purpose systems for a sandbox that had none.
 *
 * Verifies the pure modules behind the field guide, milestones, challenges,
 * civilization watch, ecology, supernova timing and black-hole feeding:
 *
 *   - discoveries log once and never twice,
 *   - milestones unlock once and report a count,
 *   - challenges progress toward a target and complete exactly once,
 *   - a civilization advances through stages and its city lights brighten,
 *   - predator/prey populations stay bounded and out of phase,
 *   - a supernova flash rises then falls,
 *   - feeding flares and decays.
 *
 * Run: node tools/progression-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

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

const P = await load('Progression.ts');
const { DiscoveryLog, Milestones, Challenges, MILESTONES, CHALLENGES } = P;

console.log('\n— the field guide logs once —');
{
  const log = new DiscoveryLog();
  const e = { id: 'x', kind: 'species', glyph: '🧬', title: 'Thing', blurb: 'A thing.' };
  ok('a new discovery is accepted', log.discover(e) === true);
  ok('the same discovery is not logged twice', log.discover(e) === false);
  ok('the count reflects one entry', log.countOf() === 1 && log.countOf('species') === 1);
  ok('entries can be listed by kind', log.of('species').length === 1);
  ok('stats report the guide size', 'Discoveries' in log.stats());
}

console.log('\n— milestones are one-time —');
{
  const m = new Milestones();
  ok('a milestone unlocks once', m.unlock('first-landing') === true);
  ok('and never again', m.unlock('first-landing') === false);
  ok('the count tracks unlocks', m.count === 1);
  ok('every milestone is defined with a glyph and blurb',
    MILESTONES.every((x) => x.glyph && x.title && x.blurb && x.id));
}

console.log('\n— challenges progress and complete —');
{
  const c = new Challenges();
  ok('progress does not complete early', c.add('land-3', 1) === false);
  ok('reaching the target completes', c.add('land-3', 2) === true);
  ok('a completed challenge cannot re-complete', c.add('land-3', 5) === false);
  ok('absolute progress can be set', (() => {
    const d = new Challenges();
    return d.set('log-20', 20) === true && d.completed('log-20');
  })());
  ok('every challenge has a positive target',
    CHALLENGES.every((x) => x.target > 0 && x.id && x.title));
}

const C = await load('CivilizationSystem.ts');
console.log('\n— a civilization grows up —');
{
  const civ = new C.CivilizationSystem(42, { collapseChance: 0 });
  ok('starts in the stone age', civ.stage === 'stone');
  ok('city lights are faint at first', civ.lights() < 0.1);
  // Advance a long time: it must reach the later stages.
  let stages = new Set([civ.stage]);
  for (let i = 0; i < 6000; i++) {
    const s = civ.step(1 / 30);
    if (s) stages.add(s);
  }
  ok('it reaches the radio age', stages.has('radio'), [...stages].join(','));
  ok('it reaches spaceflight', stages.has('spaceflight'), [...stages].join(','));
  ok('city lights brighten with technology', civ.lights() > 0.3, String(civ.lights()));
  ok('the state is coherent', (() => {
    const s = civ.state();
    return s.population >= 0 && s.population <= 1.001
      && s.progress >= 0 && s.progress <= 1
      && s.cityLights >= 0 && s.cityLights <= 1;
  })());
}

console.log('\n— collapse is possible but rare —');
{
  let collapsed = 0;
  for (let i = 0; i < 200; i++) {
    const civ = new C.CivilizationSystem(i, { collapseChance: 0.05 });
    for (let k = 0; k < 9000 && !civ.isCollapsed; k++) civ.step(1 / 30);
    if (civ.isCollapsed) collapsed++;
  }
  ok('some civilizations collapse over a full run', collapsed > 0, collapsed + ' collapsed');
  ok('collapse is not the norm', collapsed < 200, collapsed + ' collapsed');
}

const E = await load('EcologySystem.ts');
console.log('\n— predator/prey stays bounded and alive —');
{
  const eco = new E.EcologySystem(0.6, 0.2);
  let sawPreyDrop = false;
  let prevPrey = eco.state().prey;
  for (let i = 0; i < 4000; i++) {
    eco.step(1 / 60);
    const s = eco.state();
    if (s.prey < prevPrey - 0.01) sawPreyDrop = true;
    prevPrey = s.prey;
  }
  const s = eco.state();
  ok('both populations stay positive and bounded',
    s.prey > 0.001 && s.prey < 3.01 && s.predator > 0.001 && s.predator < 3.01);
  ok('the prey population rises and falls (not flat)',
    sawPreyDrop, 'prey fell at some point');
  ok('migration peaks at dawn and dusk',
    E.EcologySystem.migration(0.25) > E.EcologySystem.migration(0.0) &&
    E.EcologySystem.migration(0.75) > E.EcologySystem.migration(0.5));
  ok('migration is always 0..1',
    [0, 0.5, 1].every((d) => {
      const v = E.EcologySystem.migration(d);
      return v >= 0 && v <= 1;
    }));
}

const N = await load('SupernovaSystem.ts');
console.log('\n— a supernova flashes and fades —');
{
  const n = new N.SupernovaSystem(7);
  ok('starts quiet', n.now().phase === 'quiet');
  n.trigger('Betelgeuse');
  ok('triggering enters the flare phase', n.now().phase === 'flaring');
  ok('the event carries the star name', n.now().name === 'Betelgeuse');
  n.tick(0.2);
  ok('the flash brightens as it rises', n.now().flash > 0);
  // Run past the rise into afterglow and out the far side.
  for (let i = 0; i < 60; i++) n.tick(0.1);
  const late = n.now();
  ok('the flash eventually dies away', late.phase === 'quiet' && late.flash === 0);
}

const B = await load('BlackHoleFeeding.ts');
console.log('\n— feeding flares and decays —');
{
  const b = new B.BlackHoleFeeding();
  ok('starts with no flare', b.now().flare === 0);
  b.feed();
  ok('a feed arms the flare', b.now().flare === 1);
  b.tick(1);
  ok('the flare decays over time', b.now().flare < 1 && b.now().flare > 0);
  ok('the fed count accumulates', b.count === 1);
  for (let i = 0; i < 60; i++) b.tick(0.1);
  ok('the flare reaches zero', b.now().flare === 0);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
