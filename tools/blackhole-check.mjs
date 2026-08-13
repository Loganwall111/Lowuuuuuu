/**
 * BlackHoleTypes verification — the varieties must differ by real physics,
 * not just by label and colour.
 * Run: node tools/blackhole-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/BlackHoleTypes.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/bh-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  BLACK_HOLES, HOLE_ORDER, horizonRadius, iscoRadius,
  photonSphere, deflectionScale, describeHole
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const all = HOLE_ORDER.map((k) => BLACK_HOLES[k]);

console.log('\n— the catalogue —');
{
  ok(`there are many varieties (${all.length})`, all.length >= 10);
  ok('every ordered kind resolves', all.every(Boolean));
  ok('every entry has identity and a blurb',
     all.every((t) => t.name && t.glyph && t.blurb && t.note));
  ok('kinds are unique', new Set(all.map((t) => t.kind)).size === all.length);
  ok('names are unique', new Set(all.map((t) => t.name)).size === all.length);
}

console.log('\n— parameters are physically sane —');
{
  ok('mass is always positive', all.every((t) => t.mass > 0));
  ok('charge is within [0,1]', all.every((t) => t.charge >= 0 && t.charge <= 1));
  ok('spin is non-negative', all.every((t) => t.spin >= 0));
  ok('only the naked singularity exceeds extremal spin',
     all.filter((t) => t.spin > 1).every((t) => t.kind === 'naked'));
  ok('disc brightness is never negative', all.every((t) => t.discBrightness >= 0));
  ok('every disc tint is a valid colour',
     all.every((t) => t.discTint.length === 3 && t.discTint.every((c) => c >= 0 && c <= 1)));
  ok('no disc tint is black (the no-black rule)',
     all.every((t) => t.discTint[0] + t.discTint[1] + t.discTint[2] > 0.5));
}

console.log('\n— the mass range spans nine orders of magnitude —');
{
  const masses = all.map((t) => t.mass);
  const span = Math.max(...masses) / Math.min(...masses);
  ok(`mass spans a huge range (${span.toExponential(1)}×)`, span > 1e9);
  ok('a primordial hole is tiny', BLACK_HOLES.primordial.mass < 0.01);
  ok('a quasar is enormous', BLACK_HOLES.quasar.mass > 1e8);
}

console.log('\n— horizon radius follows the physics —');
{
  ok('all horizons are finite and positive',
     all.every((t) => Number.isFinite(horizonRadius(t)) && horizonRadius(t) > 0));
  ok('a more massive hole has a bigger horizon',
     horizonRadius(BLACK_HOLES.supermassive) > horizonRadius(BLACK_HOLES.schwarzschild));
  ok('the tiny primordial hole has the smallest horizon of the classic types',
     horizonRadius(BLACK_HOLES.primordial) < horizonRadius(BLACK_HOLES.schwarzschild));

  // r+ = M + sqrt(M^2 - a^2 - Q^2): spin shrinks the horizon
  const slow = { ...BLACK_HOLES.kerr, spin: 0 };
  const fast = { ...BLACK_HOLES.kerr, spin: 0.99 };
  ok('spin shrinks the horizon, as Kerr requires',
     horizonRadius(fast) < horizonRadius(slow),
     `${horizonRadius(fast).toFixed(3)} vs ${horizonRadius(slow).toFixed(3)}`);

  const uncharged = { ...BLACK_HOLES.charged, charge: 0 };
  ok('charge shrinks the horizon, as Reissner-Nordström requires',
     horizonRadius(BLACK_HOLES.charged) < horizonRadius(uncharged));

  ok('an over-extremal spin does not produce NaN or a negative radius',
     Number.isFinite(horizonRadius(BLACK_HOLES.naked)) && horizonRadius(BLACK_HOLES.naked) > 0);
}

console.log('\n— ISCO and the photon sphere —');
{
  ok('a non-spinning ISCO sits at 3 Schwarzschild radii',
     Math.abs(iscoRadius(BLACK_HOLES.schwarzschild) / horizonRadius(BLACK_HOLES.schwarzschild) - 3) < 1e-9);
  const slow = { ...BLACK_HOLES.kerr, spin: 0 };
  ok('spin moves the ISCO inward (prograde orbits get closer)',
     iscoRadius(BLACK_HOLES['extremal-kerr']) / horizonRadius(BLACK_HOLES['extremal-kerr'])
     < iscoRadius(slow) / horizonRadius(slow));
  ok('a non-spinning photon sphere sits at 1.5 Schwarzschild radii',
     Math.abs(photonSphere(BLACK_HOLES.schwarzschild) / horizonRadius(BLACK_HOLES.schwarzschild) - 1.5) < 1e-9);
  ok('the photon sphere is always outside the horizon for classic holes',
     all.filter((t) => t.hasHorizon && t.spin <= 1)
        .every((t) => photonSphere(t) >= horizonRadius(t) * 0.99));
  ok('the ISCO is at or outside the photon sphere for non-extremal holes',
     all.filter((t) => t.hasHorizon && t.spin < 0.8)
        .every((t) => iscoRadius(t) >= photonSphere(t) * 0.95));
}

console.log('\n— lensing genuinely differs between types —');
{
  const scales = all.map((t) => deflectionScale(t));
  ok('every deflection scale is finite', scales.every(Number.isFinite));
  ok('deflection varies across types', new Set(scales.map((s) => s.toFixed(3))).size >= 9);
  ok('a supermassive hole bends light more than a stellar one',
     deflectionScale(BLACK_HOLES.supermassive) > deflectionScale(BLACK_HOLES.schwarzschild));
  ok('a quasar bends light the most', 
     deflectionScale(BLACK_HOLES.quasar) === Math.max(...scales));
  ok('spin increases deflection',
     deflectionScale({ ...BLACK_HOLES.kerr, spin: 0.9 }) >
     deflectionScale({ ...BLACK_HOLES.kerr, spin: 0.0 }));
  ok('charge reduces deflection',
     deflectionScale({ ...BLACK_HOLES.charged, charge: 0.9 }) <
     deflectionScale({ ...BLACK_HOLES.charged, charge: 0.0 }));
  ok('a white hole deflects light the other way (negative)',
     deflectionScale(BLACK_HOLES.white) < 0);
}

console.log('\n— horizonless objects behave differently —');
{
  const horizonless = all.filter((t) => !t.hasHorizon).map((t) => t.kind);
  ok(`some objects have no horizon (${horizonless.join(', ')})`, horizonless.length >= 3);
  ok('the wormhole has no horizon', !BLACK_HOLES.wormhole.hasHorizon);
  ok('the naked singularity has no horizon', !BLACK_HOLES.naked.hasHorizon);
  ok('the white hole has no horizon', !BLACK_HOLES.white.hasHorizon);
  ok('classic holes do have horizons',
     BLACK_HOLES.schwarzschild.hasHorizon && BLACK_HOLES.kerr.hasHorizon);
}

console.log('\n— jets and evaporation —');
{
  ok('spinning holes tend to have jets',
     all.filter((t) => t.spin >= 0.5).filter((t) => t.jets).length >=
     all.filter((t) => t.spin >= 0.5).length - 1);
  ok('the non-spinning Schwarzschild hole has no jets', !BLACK_HOLES.schwarzschild.jets);
  ok('only the primordial hole evaporates appreciably',
     all.filter((t) => t.evaporation > 0).every((t) => t.kind === 'primordial'));
}

console.log('\n— the UI description —');
{
  for (const t of all) {
    const d = describeHole(t);
    if (!d.Type || !d.Mass || !d.Horizon) {
      ok('every type describes cleanly', false, t.kind);
      break;
    }
  }
  ok('every type describes cleanly', true);
  ok('a huge mass is formatted readably',
     /million/.test(describeHole(BLACK_HOLES.quasar).Mass),
     describeHole(BLACK_HOLES.quasar).Mass);
  ok('a tiny mass is formatted readably',
     /e-/.test(describeHole(BLACK_HOLES.primordial).Mass),
     describeHole(BLACK_HOLES.primordial).Mass);
  ok('horizonless objects report no horizon',
     describeHole(BLACK_HOLES.wormhole).Horizon === 'none');
  ok('over-extremal spin is flagged',
     /over-extremal/.test(describeHole(BLACK_HOLES.naked)['Spin a*']));
}

console.log('\n— extreme parameters cannot produce NaN —');
{
  const edge = [
    { ...BLACK_HOLES.kerr, spin: 1 },
    { ...BLACK_HOLES.kerr, spin: 0 },
    { ...BLACK_HOLES.charged, charge: 1 },
    { ...BLACK_HOLES.kerr, spin: 0.7071, charge: 0.7071 },
    { ...BLACK_HOLES.schwarzschild, mass: 1e-12 },
    { ...BLACK_HOLES.schwarzschild, mass: 1e12 }
  ];
  const bad = edge.filter((t) =>
    !Number.isFinite(horizonRadius(t)) || horizonRadius(t) <= 0 ||
    !Number.isFinite(iscoRadius(t)) || !Number.isFinite(photonSphere(t)) ||
    !Number.isFinite(deflectionScale(t)));
  ok('extremal and degenerate cases stay finite and positive', bad.length === 0,
     `${bad.length} bad`);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
