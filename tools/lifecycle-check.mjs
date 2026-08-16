/**
 * lifecycle-check — stars that age, and a fuller celestial zoo.
 *
 * The two pure additions to "true space" this pass are verified here:
 *
 *   - StellarLifecycle: a star moves through main sequence, subgiant, red
 *     giant, then collapses into a mass-dependent remnant, deterministically.
 *   - CelestialCatalog: the new archetypes (hypernova, kilonova, stellar
 *     engine, ringworld) exist, the weights stay internally consistent, and
 *     the rarest/most-common invariants the rest of the suite pins still hold.
 *
 * Run: node tools/lifecycle-check.mjs
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

const S = await load('StellarLifecycle.ts');
console.log('\n— a star lives and dies on a schedule —');
{
  ok('a newborn star is main sequence', S.starPhaseAt(1, 0).id === 'main');
  ok('it swells into a red giant late in life',
    S.starPhaseAt(1, S.STAR_LIFETIME * 0.85).id === 'redgiant');
  ok('a low-mass star leaves a white dwarf',
    S.starPhaseAt(2, S.STAR_LIFETIME * 1.1).id === 'white-dwarf');
  ok('the phase is deterministic',
    S.starPhaseAt(9, 1234).id === S.starPhaseAt(9, 1234).id);
  ok('the red giant is the largest phase', (() => {
    const red = S.STAR_PHASES.find((p) => p.id === 'redgiant');
    return red && S.STAR_PHASES.every((p) => p.size <= red.size);
  })());
  ok('remnants are far smaller than the living star',
    S.starPhaseAt(1, S.STAR_LIFETIME).size < 0.2);
  ok('the colour drifts with age',
    S.stellarColor(1, 0).tintA.join() !== S.stellarColor(1, S.STAR_LIFETIME).tintA.join());
  ok('every phase is a valid colour and size',
    S.STAR_PHASES.every((p) =>
      p.tintA.length === 3 && p.tintB.length === 3 &&
      p.size > 0 && p.tintA.every((v) => v >= 0 && v <= 1)));
}

const C = await load('CelestialCatalog.ts');
console.log('\n— the celestial zoo grew, and its rules held —');
{
  const kinds = C.CELESTIAL_KINDS;
  ok('the new archetypes exist',
    ['hypernova', 'kilonova', 'stellar-engine', 'ringworld']
      .every((k) => kinds.includes(k)));
  ok('every archetype is still fully specified', kinds.every((k) => {
    const s = C.CELESTIALS[k];
    return s.label && s.glyph && s.weight > 0
      && s.minRadius > 0 && s.maxRadius >= s.minRadius
      && s.luminosity >= 0 && s.luminosity <= 1
      && s.tint.length === 3 && s.blurb.length > 10;
  }));
  ok('labels stay distinct',
    new Set(kinds.map((k) => C.CELESTIALS[k].label)).size === kinds.length);
  ok('total weight still matches the table',
    C.TOTAL_WEIGHT === kinds.reduce((s, k) => s + C.CELESTIALS[k].weight, 0));
  ok('dyson swarms are still the rarest',
    C.CELESTIALS['dyson-swarm'].weight === Math.min(...kinds.map((k) => C.CELESTIALS[k].weight)));
  ok('ordinary debris is still the most common',
    C.CELESTIALS['meteor-swarm'].weight === Math.max(...kinds.map((k) => C.CELESTIALS[k].weight)));
  ok('quasars stay genuinely rare',
    C.CELESTIALS['quasar'].weight / C.TOTAL_WEIGHT < 0.01);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
