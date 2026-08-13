/**
 * descent-check — the physics of falling onto a world.
 *
 * These assertions are written against known real-world values wherever
 * possible (skydiver terminal velocity, the density of the air at the top of
 * Everest, the angular size of the Earth from the ISS) so that a passing run
 * means the numbers are actually right, not merely self-consistent.
 */

import { execFileSync } from 'node:child_process';
import { mkdtempSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

// Compile the TS module to a temporary ESM bundle and import it.
const dir = mkdtempSync(join(tmpdir(), 'descent-'));
const out = join(dir, 'descent.mjs');
execFileSync('./node_modules/.bin/esbuild', [
  'src/bjs/systems/DescentSystem.ts',
  '--bundle', '--format=esm', '--platform=neutral', '--outfile=' + out
], { stdio: 'pipe' });

const M = await import(out);
const {
  EARTHLIKE, densityAt, terminalVelocity, heatFlux,
  apparentDiameter, skyCoverage, phaseFor, skyColorAt, sampleDescent, Descent
} = M;

console.log('\natmospheric descent');

/* ---------------- density: the barometric curve ------------------------- */

ok('sea level density is the quoted value',
   Math.abs(densityAt(EARTHLIKE, 0) - 1.225) < 1e-9);

// One scale height up, density must fall by exactly 1/e.
ok('density falls by 1/e over one scale height',
   Math.abs(densityAt(EARTHLIKE, 8.5) - 1.225 / Math.E) < 1e-9);

// Everest, 8.85 km: real air density is about 0.44 kg/m3.
{
  const d = densityAt(EARTHLIKE, 8.85);
  ok('density at the summit of Everest is realistic',
     d > 0.38 && d < 0.50, d.toFixed(3) + ' kg/m3');
}

ok('density keeps falling with altitude',
   densityAt(EARTHLIKE, 50) < densityAt(EARTHLIKE, 20));
ok('the upper atmosphere is nearly vacuum',
   densityAt(EARTHLIKE, 100) < 1e-4);
ok('an airless world has no air anywhere',
   densityAt({ ...EARTHLIKE, seaLevelDensity: 0 }, 0) === 0);
ok('below the surface density does not blow up',
   densityAt(EARTHLIKE, -50) === 1.225);
ok('a NaN altitude does not poison the result',
   densityAt(EARTHLIKE, NaN) === 0);

/* ---------------- terminal velocity ------------------------------------- */

// A belly-down skydiver presents ~0.7 m2 at Cd~1.0, which gives ~43 m/s
// (~96 mph). The often-quoted 120 mph figure is a *head-down* dive, where
// the frontal area is roughly half as much - checked here as well, because
// getting both right is what proves the area term is doing real work.
{
  const belly = { mass: 80, area: 0.7, dragCoefficient: 1.0, noseRadius: 0.3 };
  const v = terminalVelocity(EARTHLIKE, belly, 0);
  ok('a belly-down skydiver falls at about 95 mph',
     v > 38 && v < 48, v.toFixed(1) + ' m/s');

  const dive = { mass: 80, area: 0.32, dragCoefficient: 0.9, noseRadius: 0.3 };
  const vd = terminalVelocity(EARTHLIKE, dive, 0);
  ok('a head-down dive reaches roughly 120 mph',
     vd > 50 && vd < 72, vd.toFixed(1) + ' m/s');
}

// Same person in thin air falls much faster.
{
  const sky = { mass: 80, area: 0.7, dragCoefficient: 1.0, noseRadius: 0.3 };
  ok('terminal velocity is far higher in thin air',
     terminalVelocity(EARTHLIKE, sky, 30) > terminalVelocity(EARTHLIKE, sky, 0) * 3);
}

ok('in vacuum there is no terminal velocity at all',
   terminalVelocity({ ...EARTHLIKE, seaLevelDensity: 0 },
     { mass: 80, area: 0.7, dragCoefficient: 1, noseRadius: 0.3 }, 0) === Infinity);

// A heavier object with the same shape falls faster - the classic result.
{
  const light = { mass: 50, area: 1, dragCoefficient: 1, noseRadius: 0.3 };
  const heavy = { mass: 200, area: 1, dragCoefficient: 1, noseRadius: 0.3 };
  ok('a denser object has a higher terminal velocity',
     terminalVelocity(EARTHLIKE, heavy, 0) > terminalVelocity(EARTHLIKE, light, 0));
  // Specifically it should scale as the square root of mass: 4x mass = 2x speed.
  const ratio = terminalVelocity(EARTHLIKE, heavy, 0) / terminalVelocity(EARTHLIKE, light, 0);
  ok('terminal velocity scales with the square root of mass',
     Math.abs(ratio - 2) < 1e-6, 'ratio ' + ratio.toFixed(4));
}

/* ---------------- entry heating ----------------------------------------- */

const capsule = { mass: 5000, area: 12, dragCoefficient: 1.4, noseRadius: 3 };

// At 200 km the air is ~7e-11 kg/m3: not identically zero, but utterly
// negligible. Assert negligibility, and use a truly airless world for zero.
ok('heating is negligible in the exosphere',
   heatFlux(EARTHLIKE, capsule, 200, 7800) < 0.05,
   heatFlux(EARTHLIKE, capsule, 200, 7800).toExponential(2));
ok('an airless world produces exactly no heating',
   heatFlux({ ...EARTHLIKE, seaLevelDensity: 0 }, capsule, 10, 7800) === 0);
ok('there is no heating at rest',
   heatFlux(EARTHLIKE, capsule, 10, 0) === 0);

// Heating goes as v^3: doubling speed must give 8x the flux.
{
  const a = heatFlux(EARTHLIKE, capsule, 60, 2000);
  const b = heatFlux(EARTHLIKE, capsule, 60, 4000);
  ok('heating rises with the cube of speed',
     Math.abs(b / a - 8) < 1e-6, 'ratio ' + (b / a).toFixed(3));
}

// A blunter nose is cooler - the reason re-entry capsules are not pointy.
{
  const blunt = heatFlux(EARTHLIKE, { ...capsule, noseRadius: 6 }, 60, 6000);
  const sharp = heatFlux(EARTHLIKE, { ...capsule, noseRadius: 0.5 }, 60, 6000);
  ok('a blunt nose heats less than a sharp one', blunt < sharp);
}

// Peak heating happens high up, not at the ground: fast and thin beats slow
// and thick. This is the signature shape of a real entry profile.
{
  const high = heatFlux(EARTHLIKE, capsule, 60, 7000);
  const low = heatFlux(EARTHLIKE, capsule, 2, 200);
  ok('entry heating peaks high in the atmosphere, not at landing', high > low);
}

// Heating must switch on with the shock layer, not with mere movement.
{
  const { hypersonicFactor } = M;
  ok('subsonic flow is not hypersonic', hypersonicFactor(100) === 0);
  ok('Mach 1 is still not hypersonic', hypersonicFactor(340) === 0);
  ok('Mach 5 and above is fully hypersonic', hypersonicFactor(340 * 5) === 1);
  ok('the transition is gradual', hypersonicFactor(340 * 3) > 0 && hypersonicFactor(340 * 3) < 1);
  ok('a subsonic capsule has no entry heating',
     heatFlux(EARTHLIKE, capsule, 5, 200) === 0);
  ok('a hypersonic capsule certainly does',
     heatFlux(EARTHLIKE, capsule, 60, 7000) > 1);
}

/* ---------------- how big the planet looks ------------------------------ */

// From the ISS (~420 km) the Earth subtends about 140 degrees.
{
  const deg = apparentDiameter(EARTHLIKE, 420) * 180 / Math.PI;
  ok('from orbit the planet subtends a realistic angle',
     deg > 130 && deg < 150, deg.toFixed(1) + ' deg');
}

ok('at the surface the planet fills the lower sky',
   apparentDiameter(EARTHLIKE, 0) > Math.PI * 0.98);
ok('the planet grows steadily as you fall',
   apparentDiameter(EARTHLIKE, 10) > apparentDiameter(EARTHLIKE, 1000));
ok('from very far away it is a dot',
   apparentDiameter(EARTHLIKE, 4e6) < 0.01);

ok('sky coverage approaches one half at the surface',
   Math.abs(skyCoverage(EARTHLIKE, 0) - 0.5) < 0.02,
   skyCoverage(EARTHLIKE, 0).toFixed(4));
ok('sky coverage is small from deep space',
   skyCoverage(EARTHLIKE, 4e6) < 1e-4);
ok('sky coverage never exceeds one', skyCoverage(EARTHLIKE, 0) <= 1);

/* ---------------- sky colour -------------------------------------------- */

ok('the sky is coloured at the surface',
   skyColorAt(EARTHLIKE, 0)[2] > 0.5);
ok('the sky fades toward black in space',
   skyColorAt(EARTHLIKE, 200).every((c) => c < 0.02));
ok('an airless world has a black sky at ground level',
   skyColorAt({ ...EARTHLIKE, seaLevelDensity: 0 }, 0).every((c) => c === 0));
ok('the sky darkens monotonically with altitude',
   skyColorAt(EARTHLIKE, 5)[2] > skyColorAt(EARTHLIKE, 20)[2]);

/* ---------------- phases ------------------------------------------------ */

ok('above the atmosphere you are in space',
   phaseFor(EARTHLIKE, 300, 7000) === 'space');
ok('fast and high is entry',
   phaseFor(EARTHLIKE, 80, 6000) === 'entry');
ok('slow and low is approach',
   phaseFor(EARTHLIKE, 3, 80) === 'approach');
ok('the ground is landed',
   phaseFor(EARTHLIKE, 0, 0) === 'landed');

/* ---------------- integrating a whole fall ------------------------------ */

// Drop a skydiver from 4 km and let them fall. They must reach terminal
// velocity and land, not accelerate forever.
{
  const sky = { mass: 80, area: 0.7, dragCoefficient: 1.0, noseRadius: 0.3 };
  const d = new Descent(EARTHLIKE, sky, 4);
  let t = 0, peak = 0;
  while (!d.landed && t < 600) { d.step(1 / 60); t += 1 / 60; peak = Math.max(peak, d.speed); }

  ok('the skydiver lands', d.landed, 'after ' + t.toFixed(0) + 's');
  ok('the fall takes a believable time',
     t > 40 && t < 130, t.toFixed(0) + ' s');
  // The cap is terminal velocity *where the fall began* - the air is thinner
  // up there, so a jumper genuinely exceeds sea-level terminal velocity
  // early on and is then decelerated by the thickening air. Comparing
  // against the sea-level figure alone would be wrong.
  ok('speed never exceeds terminal velocity at the release altitude',
     peak <= terminalVelocity(EARTHLIKE, sky, 4) * 1.02,
     'peak ' + peak.toFixed(1) + ' vs ' + terminalVelocity(EARTHLIKE, sky, 4).toFixed(1));
  ok('the jumper does slow down into the thicker air below',
     d.speed < peak, 'landing ' + d.speed.toFixed(1) + ' peak ' + peak.toFixed(1));
  ok('altitude ends exactly at the surface', d.altitude === 0);
}

// In vacuum the same drop is pure free fall: v = sqrt(2gh).
{
  const airless = { ...EARTHLIKE, seaLevelDensity: 0 };
  const rock = { mass: 10, area: 1, dragCoefficient: 1, noseRadius: 0.3 };
  const d = new Descent(airless, rock, 1);
  while (!d.landed) d.step(1 / 240);
  const expected = Math.sqrt(2 * airless.gravity * 1000);
  ok('an airless drop matches sqrt(2gh)',
     Math.abs(d.speed - expected) / expected < 0.02,
     d.speed.toFixed(1) + ' vs ' + expected.toFixed(1));
}

// A long frame must not tunnel through the planet.
{
  const sky = { mass: 80, area: 0.7, dragCoefficient: 1, noseRadius: 0.3 };
  const d = new Descent(EARTHLIKE, sky, 2);
  d.step(5);
  ok('a huge timestep cannot fall through the surface', d.altitude >= 0);
}

// Nonsense timesteps are inert.
{
  const d = new Descent(EARTHLIKE, { mass: 80, area: 0.7, dragCoefficient: 1, noseRadius: 0.3 }, 10);
  const before = d.altitude;
  d.step(0); d.step(-1); d.step(NaN); d.step(Infinity);
  ok('bad timesteps do not move the faller', d.altitude === before);
}

// Once landed, stepping does nothing more.
{
  const d = new Descent(EARTHLIKE, { mass: 80, area: 0.7, dragCoefficient: 1, noseRadius: 0.3 }, 0.001);
  while (!d.landed) d.step(1 / 60);
  const s = d.step(10);
  ok('a landed faller stays landed', s.landed && d.altitude === 0);
  ok('the phase reads as landed', s.phase === 'landed');
}

/* ---------------- the state a renderer consumes ------------------------- */

// Validate the absolute scale against a real flight, not just ratios.
{
  const apollo = { mass: 5560, area: 12, dragCoefficient: 1.35, noseRadius: 4.69 };
  const q = heatFlux(EARTHLIKE, apollo, 65, 11000);
  ok('Apollo-class re-entry heating lands in the published range',
     q > 120 && q < 600, q.toFixed(0) + ' W/cm2');
}

{
  const s = sampleDescent(EARTHLIKE, capsule, 60, 6000);
  ok('re-entry glows', s.reentryGlow > 0.05, 'glow ' + s.reentryGlow.toFixed(3));
  ok('the glow is bounded to 0-1', s.reentryGlow <= 1);
  const calm = sampleDescent(EARTHLIKE, capsule, 1, 50);
  ok('a gentle subsonic approach does not glow at all',
     calm.reentryGlow === 0, String(calm.reentryGlow));
  ok('every field is finite',
     Object.values(s).every((v) =>
       typeof v !== 'number' || Number.isFinite(v)));
  ok('the atmosphere fraction is 0-1 at all altitudes',
     [0, 10, 100, 5000].every((a) => {
       const f = sampleDescent(EARTHLIKE, capsule, a, 100).atmosphereFraction;
       return f >= 0 && f <= 1;
     }));
  const st = new Descent(EARTHLIKE, capsule, 120, 7000).stats();
  ok('the HUD reports altitude, speed, sky and phase',
     'Altitude' in st && 'Fall speed' in st && 'Sky filled' in st && 'Phase' in st);
}

rmSync(dir, { recursive: true, force: true });
console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
