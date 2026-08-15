/**
 * The velocity gearbox.
 *
 * THE REPORTED BUG. "Movement is permanently locked to warp speed - any
 * forward tap rockets straight through the galaxy."
 *
 * THE MEASURED CAUSE. Free-flight speed came only from
 * VehicleController.setScaleSpeed(d), which is
 *
 *     flySpeed = clamp(sqrt(d)*3.2 + d*0.02 + 3, 2, 60000)
 *
 * with d the distance to the nearest body. That is fine up close and
 * absurd in deep space: with the nearest body 500,000 units away it hands
 * out 12,266 u/s, and the galaxy disc slab is only 12,000 units thick. One
 * second of thrust and you are through it. The warp drive then multiplies
 * that by up to 90,000x off held thrust alone, with no way to say no.
 *
 * So the tests here are not "does a field exist". They reproduce the real
 * autoscale formula and the real warp curve and assert on the thing the
 * player actually complained about: how long you get to spend inside the
 * galaxy before you are out the other side.
 *
 * Run: node tools/gears-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';
import path from 'path';
import os from 'os';

let pass = 0, fail = 0;
const ok = (n, c, e) => {
  if (c) pass++; else { fail++; console.log('FAIL: ' + n + (e ? ' — ' + e : '')); }
};
const read = (p) => (fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '');

const src = read('src/bjs/systems/SpeedGears.ts');
const app = read('src/bjs/App.ts');
const hud = read('src/bjs/ui/FlightHUD.ts');
const css = read('src/bjs/ui/styles.ts');
const veh = read('src/bjs/systems/VehicleSystem.ts');
const deep = read('src/bjs/systems/DeepSkySystem.ts');

// ---------------------------------------------------------------- module
ok('SpeedGears module exists', src.length > 0);
ok('gearbox is engine-free so it is testable without a GPU',
  !/from ['"]@babylonjs/.test(src));

let G = null;
{
  const out = path.join(os.tmpdir(), 'gears-' + process.pid + '.mjs');
  try {
    await build({
      entryPoints: ['src/bjs/systems/SpeedGears.ts'],
      bundle: true, format: 'esm', platform: 'neutral',
      outfile: out, logLevel: 'silent'
    });
    G = await import('file://' + out);
    ok('SpeedGears bundles', true);
  } catch (e) {
    ok('SpeedGears bundles', false, e.message);
  } finally {
    if (fs.existsSync(out)) fs.unlinkSync(out);
  }
}

if (!G) {
  console.log(pass + ' passed, ' + fail + ' failed');
  process.exit(1);
}

const { GEARS, GEAR_ORDER, DEFAULT_GEAR, SpeedGearbox, gearForKey, isGearId } = G;

// ------------------------------------------------------- the three gears
ok('there are exactly three gears', GEAR_ORDER.length === 3);
ok('they run slow to fast', GEAR_ORDER.join(',') === 'impulse,cruise,hyper');
ok('impulse is 0.05x', GEARS.impulse.speedMul === 0.05);
ok('cruise is 1.0x — the unmodified autoscaled baseline', GEARS.cruise.speedMul === 1);
ok('hyper is 100x', GEARS.hyper.speedMul === 100);
ok('gear multipliers strictly increase',
  GEARS.impulse.speedMul < GEARS.cruise.speedMul
  && GEARS.cruise.speedMul < GEARS.hyper.speedMul);
ok('every gear is labelled', GEAR_ORDER.every((id) => GEARS[id].label.length > 2));
ok('every gear has a blurb for the shift notice',
  GEAR_ORDER.every((id) => GEARS[id].blurb.length > 4));
ok('labels are IMPULSE / CRUISE / HYPER',
  GEARS.impulse.label === 'IMPULSE' && GEARS.cruise.label === 'CRUISE'
  && GEARS.hyper.label === 'HYPER');

// -------------------------------------------------------------- hotkeys
ok('1 selects impulse', gearForKey('1') === 'impulse');
ok('2 selects cruise', gearForKey('2') === 'cruise');
ok('3 selects hyper', gearForKey('3') === 'hyper');
ok('unrelated keys are not gear keys',
  gearForKey('w') === null && gearForKey('4') === null && gearForKey('') === null);
ok('keys are unique per gear',
  new Set(GEAR_ORDER.map((id) => GEARS[id].key)).size === 3);
ok('gear keys do not collide with the existing c / z bindings',
  !GEAR_ORDER.some((id) => ['c', 'z', ' ', 'w', 'a', 's', 'd'].includes(GEARS[id].key)));
ok('isGearId accepts real gears and rejects junk',
  isGearId('hyper') && !isGearId('plaid') && !isGearId('toString'));

// --------------------------------------------------------- gearbox state
{
  const gb = new SpeedGearbox();
  ok('starts in cruise, the neutral 1.0x gear', gb.current === DEFAULT_GEAR
    && DEFAULT_GEAR === 'cruise');
  ok('selecting the current gear reports no change', gb.select('cruise') === false);
  ok('selecting a new gear reports a change', gb.select('impulse') === true);
  ok('the change actually took', gb.current === 'impulse');
  ok('a change is pending after a shift', gb.consumeChange() === true);
  ok('the pending flag clears once consumed', gb.consumeChange() === false);
  ok('a bogus id is rejected outright', gb.select('warp9') === false
    && gb.current === 'impulse');
  ok('handleKey shifts', gb.handleKey('3') === true && gb.current === 'hyper');
  ok('handleKey on a non-gear key does nothing',
    gb.handleKey('q') === false && gb.current === 'hyper');
  ok('the shift message names the gear and its multiplier',
    gb.message().includes('HYPER') && gb.message().includes('100x'));
  gb.select('impulse');
  ok('the impulse message says warp is held off',
    /warp held off/i.test(gb.message()));
  ok('the impulse message shows a readable 0.05x, not 5e-2',
    gb.message().includes('0.05x'));
}

// ------------------------------------- the actual autoscale, reproduced
// Mirrors VehicleController.setScaleSpeed exactly. Asserted against the
// source below so this cannot drift into testing a fiction.
const autoscale = (d) => {
  const near = Math.max(0, Number.isFinite(d) ? Math.abs(d) : 100);
  return Math.max(2, Math.min(60000, Math.sqrt(near) * 3.2 + near * 0.02 + 3));
};

{
  const m = veh.match(/Math\.sqrt\(near\)\s*\*\s*([\d.]+)/);
  ok('the harness mirrors the real sqrt coefficient', m && Number(m[1]) === 3.2,
    m ? 'source says ' + m[1] : 'coefficient not found in VehicleSystem.ts');
  const lin = veh.match(/near\s*\*\s*([\d.]+)\s*\+\s*3/);
  ok('the harness mirrors the real linear term', lin && Number(lin[1]) === 0.02,
    lin ? 'source says ' + lin[1] : 'linear term not found');
  ok('the harness mirrors the real clamp',
    /Math\.max\(2,\s*Math\.min\(60000,/.test(veh));
}

ok('the diagnosis reproduces: deep space autoscales to ~12,000 u/s',
  Math.round(autoscale(500000)) === 12266, 'got ' + autoscale(500000).toFixed(0));

// The galaxy disc slab: SLAB_H 0.12 of a 50,000 outer radius, y +/- 6,000.
const SLAB = 12000;

{
  const gb = new SpeedGearbox();
  const base = autoscale(500000);

  gb.select('cruise');
  const tCruise = SLAB / gb.applySpeed(base);
  ok('BEFORE: ungeared, the 12,000u galaxy slab is crossed in about a second',
    tCruise < 1.2, tCruise.toFixed(2) + 's');

  gb.select('impulse');
  const tImpulse = SLAB / gb.applySpeed(base);
  ok('AFTER: impulse gives you 15+ seconds inside the galaxy',
    tImpulse > 15, tImpulse.toFixed(1) + 's');
  ok('impulse is exactly 20x longer than cruise, as 0.05x implies',
    Math.abs(tImpulse / tCruise - 20) < 1e-6);

  gb.select('hyper');
  ok('hyper still crosses the 260,000u cell grid quickly',
    260000 / gb.applySpeed(base) < 0.3);
}

{
  // Close quarters: the low gear must stay flyable, not become a dead stick.
  const gb = new SpeedGearbox();
  gb.select('impulse');
  const landing = gb.applySpeed(autoscale(100));
  ok('impulse near a surface is slow but not frozen',
    landing >= 0.5 && landing < 5, landing.toFixed(2) + ' u/s');
  ok('a per-frame step at landing speed is sub-unit',
    landing / 60 < 0.1);
  // A non-finite base speed means the autoscaler broke; the safe answer is
  // a full stop, never an infinite one.
  ok('applySpeed refuses to propagate a non-finite speed',
    gb.applySpeed(NaN) === 0 && gb.applySpeed(Infinity) === 0
    && gb.applySpeed(-Infinity) === 0);
}

// ---------------------------------------------- the warp ceiling, the fix
{
  const gb = new SpeedGearbox();
  ok('impulse refuses warp entirely', gb.select('impulse') !== null
    && gb.clampWarp(90000) === 1);
  ok('impulse reports warp as not allowed', gb.warpAllowed === false);
  gb.select('cruise');
  ok('cruise is sub-warp by definition', gb.clampWarp(90000) === 1);
  ok('cruise reports warp as not allowed', gb.warpAllowed === false);
  gb.select('hyper');
  ok('hyper passes the full 90,000x drive through', gb.clampWarp(90000) === 90000);
  ok('hyper reports warp as allowed', gb.warpAllowed === true);
  ok('a warp multiplier never falls below 1 in any gear',
    GEAR_ORDER.every((id) => { gb.select(id); return gb.clampWarp(0.001) === 1; }));
  ok('clampWarp survives garbage', gb.clampWarp(NaN) === 1);
}

{
  // The whole point: the ceiling, not the speed scale, is what stops
  // held-thrust warp from undoing a low gear.
  const gb = new SpeedGearbox();
  gb.select('cruise');
  const geared = gb.applySpeed(autoscale(500000)) * gb.clampWarp(90000);
  const ungeared = autoscale(500000) * 90000;
  ok('cruise is over four orders of magnitude slower than full warp',
    ungeared / geared > 1e4, (ungeared / geared).toExponential(1) + 'x');
  ok('without a ceiling even impulse would still be warp speed',
    autoscale(500000) * 0.05 * 90000 > 5e7);
}

{
  // The warp curve from DeepSkySystem, so the ceiling is checked against
  // the multiplier the drive really produces rather than a guessed one.
  const top = Number((deep.match(/topMultiplier:\s*(\d+)/) || [])[1]);
  ok('the real drive tops out at 90,000x', top === 90000);
  const raw = (c) => 1 + c * c * c * (top - 1);
  const gb = new SpeedGearbox();
  gb.select('cruise');
  ok('a fully spooled drive is still clamped to 1x in cruise',
    gb.clampWarp(raw(1)) === 1);
  ok('a half-spooled drive is still clamped to 1x in cruise',
    gb.clampWarp(raw(0.5)) === 1);
  gb.select('hyper');
  ok('hyper leaves the cubic curve untouched',
    Math.abs(gb.clampWarp(raw(0.5)) - raw(0.5)) < 1e-9);
}

// -------------------------------------------------------------- wiring
ok('the app owns a gearbox', /gearbox\s*=\s*new SpeedGearbox\(\)/.test(app));
ok('the gearbox scales the autoscaled speed at the call site',
  /gearbox\.applySpeed\(this\.vehicle\.flySpeed\)/.test(app));
ok('the gear is applied after setScaleSpeed, which recomputes every frame',
  app.indexOf('setScaleSpeed(d * this.mouse.throttleScale)')
    < app.indexOf('gearbox.applySpeed'));
ok('the warp multiplier is clamped by the gear', /gearbox\.clampWarp\(warping\.multiplier\)/.test(app));
ok('warp only engages when the gear allows it', /gearbox\.warpAllowed/.test(app));
ok('1/2/3 are handled in the keydown listener',
  /gearbox\.handleKey\(e\.key\)/.test(app));
ok('a shift fires a notification', /onGearShift\(\)/.test(app)
  && /flightHud\.notify\(this\.gearbox\.message\(\)\)/.test(app));
ok('dropping out of hyper dumps stored warp charge',
  /warpAllowed\)\s*this\.warpDrive\.disengage\(\)/.test(app));
ok('clicking a gear button goes through the same path as the key',
  /flightHud\.onGear\s*=/.test(app) && /gearbox\.select\(id\)/.test(app));

{
  // The warp multiply must be undone with the SAME value it was applied
  // with, or flySpeed compounds into nonsense within a second.
  const up = app.match(/this\.vehicle\.flySpeed \*= (\w+);/);
  const down = app.match(/this\.vehicle\.flySpeed = baseFly \/ (\w+);/);
  ok('warp is applied and removed using the identical clamped value',
    up && down && up[1] === down[1] && up[1] === 'warpMul',
    up && down ? up[1] + ' vs ' + down[1] : 'pattern not found');
  ok('the apply and the undo are guarded by the same flag',
    /if \(warpOn\) \{/.test(app) && /if \(warpOn\) this\.vehicle\.flySpeed = baseFly/.test(app));
  ok('no stale unclamped warping.multiplier is left in the flight path',
    !/flySpeed[^\n]*warping\.multiplier/.test(app));
}

// ------------------------------------------------------------------ HUD
ok('the HUD declares a gears element group', /gears:\s*boolean/.test(hud));
ok('the gear row is on by default', /gears:\s*true/.test(hud));
ok('FlightData carries the active gear', /gear\?:\s*GearId/.test(hud));
ok('the HUD imports the gear table rather than duplicating it',
  /from '\.\.\/systems\/SpeedGears'/.test(hud));
ok('the row is built from GEAR_ORDER, so it cannot drift from the gearbox',
  /GEAR_ORDER\.map/.test(hud));
ok('each button carries its gear id', /data-gear="/.test(hud));
ok('each button shows its hotkey', /fh-gear-key/.test(hud));
ok('each button shows its multiplier', /fh-gear-mul/.test(hud));
ok('the active gear is marked', /classList\.toggle\('on'/.test(hud));
ok('the gear readout is redrawn only on change', /cache\.get\('gear'\)/.test(hud));
ok('the HUD exposes a notify() for the shift telemetry',
  /notify\(msg: string\)/.test(hud));
ok('the notice auto-clears', /classList\.remove\('on'\)/.test(hud)
  && /1900/.test(hud));
ok('a repeated notify resets the timer rather than stacking them',
  /clearTimeout\(this\.noticeTimer\)/.test(hud));
ok('the app reports the clamped multiplier, not the raw one',
  /warpMultiplier: this\.gearbox\.clampWarp/.test(app));
ok('the warp bar reads zero when the gear forbids warp',
  /warpCharge: this\.gearbox\.warpAllowed \? w\.charge : 0/.test(app));
ok('the gear group can be toggled from the settings shell',
  /gears: 'Velocity gear shifter'/.test(read('src/bjs/ui/Shell.ts')));

// ---------------------------------------------------------------- styles
ok('the shifter is styled', /\.fhud-gears\{/.test(css));
ok('the shifter is top centre', /\.fhud-gears\{[^}]*top:14px/.test(css)
  && /\.fhud-gears\{[^}]*left:50%/.test(css));
ok('the shifter is a translucent dark status row, not an opaque slab',
  /\.fhud-gears\{[^}]*rgba\(10,16,28,\.74\)/.test(css));
ok('it is frosted like the rest of the instruments',
  /\.fhud-gears\{[^}]*backdrop-filter:blur/.test(css));
ok('the buttons opt back into pointer events, since .fhud is none',
  /\.fhud-gears\{[^}]*pointer-events:auto/.test(css));
ok('the HUD as a whole still ignores the pointer',
  /\.fhud\{[^}]*pointer-events:none/.test(css));
ok('the shift notice is green', /\.fhud-notice\{[^}]*color:var\(--ok\)/.test(css));
ok('the notice is hidden until a shift', /\.fhud-notice\{[^}]*opacity:0/.test(css));
ok('the notice shows on .on', /\.fhud-notice\.on\{[^}]*opacity:1/.test(css));
ok('the notice never blocks clicks', /\.fhud-notice\{[^}]*pointer-events:none/.test(css));
ok('the engaged gear is visibly lit', /\.fh-gear\.on\{/.test(css));

{
  // Two absolutely-positioned things at the top centre will overlap unless
  // someone checks the numbers, and nothing else in the app will catch it.
  const top = (sel) => {
    const m = css.match(new RegExp(sel.replace('.', '\\.') + '\\{[^}]*top:(\\d+)px'));
    return m ? Number(m[1]) : NaN;
  };
  const gears = top('.fhud-gears'), notice = top('.fhud-notice'),
    descent = top('.fhud-descent');
  ok('the gear row, the notice and the descent readout are stacked, not piled',
    gears < notice && notice < descent,
    gears + ' / ' + notice + ' / ' + descent);
  ok('the descent readout was moved clear of the shifter', descent >= 120);
  ok('the shifter clears the top edge', gears >= 8);
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
