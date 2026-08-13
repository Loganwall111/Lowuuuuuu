/**
 * VehicleSystem verification — flight must be inertial and walking must
 * respect gravity and the ground. Both are player-facing, so bad feel here
 * is immediately obvious.
 * Run: node tools/vehicle-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['tools/fixtures/vehicle-entry.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/veh-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  VehicleController, SHIPS, emptyInput, inputFromKeys, Vector3
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const run = (v, input, seconds, dt = 1 / 60, ground) => {
  for (let i = 0; i < Math.ceil(seconds / dt); i++) v.update(dt, input, ground);
};
const flatGround = () => ({ height: 0, normal: new Vector3(0, 1, 0) });

console.log('\n— modes —');
{
  const v = new VehicleController();
  ok('starts in orbit mode', v.mode === 'orbit');
  const p0 = v.position.clone();
  run(v, { ...emptyInput(), forward: 1 }, 1);
  ok('orbit mode ignores vehicle input', v.position.equals(p0));
  v.setMode('fly');
  ok('mode can be changed', v.mode === 'fly');
  ok('changing mode zeroes velocity', v.velocity.length() === 0);
}

console.log('\n— flight: thrust accelerates, and inertia persists —');
{
  const v = new VehicleController();
  v.setMode('fly');
  run(v, { ...emptyInput(), forward: 1 }, 1);
  const cruising = v.speed();
  ok(`forward thrust builds speed (${cruising.toFixed(1)} u/s)`, cruising > 5);

  // release the throttle: an inertial ship keeps moving
  const before = v.position.clone();
  run(v, emptyInput(), 0.5);
  ok('the ship keeps moving with no input (inertial, not arcade)',
     Vector3.Distance(v.position, before) > 1);
  ok('drag bleeds speed off gradually', v.speed() < cruising);
  ok('drag never reverses direction', v.speed() >= 0);
}

console.log('\n— flight: speed is capped —');
{
  for (const id of Object.keys(SHIPS)) {
    const v = new VehicleController();
    v.setMode('fly');
    v.setShip(id);
    run(v, { ...emptyInput(), forward: 1 }, 30);
    const cap = SHIPS[id].maxSpeed;
    if (v.speed() > cap * 1.02) {
      ok(`${id} respects its speed cap`, false, `${v.speed().toFixed(1)} > ${cap}`);
    }
  }
  ok('every ship respects its speed cap', true);

  const v = new VehicleController();
  v.setMode('fly');
  v.setShip('interceptor');
  run(v, { ...emptyInput(), forward: 1, boost: true }, 30);
  ok('boost raises the cap above the cruise limit',
     v.speed() > SHIPS.interceptor.maxSpeed);
}

console.log('\n— flight: braking —');
{
  const v = new VehicleController();
  v.setMode('fly');
  run(v, { ...emptyInput(), forward: 1 }, 3);
  const fast = v.speed();
  run(v, { ...emptyInput(), brake: true }, 3);
  ok(`braking sheds speed (${fast.toFixed(1)} -> ${v.speed().toFixed(1)})`,
     v.speed() < fast * 0.5);
  ok('braking never produces negative speed', v.speed() >= 0);
}

console.log('\n— flight: ships feel different —');
{
  const results = {};
  for (const id of Object.keys(SHIPS)) {
    const v = new VehicleController();
    v.setMode('fly');
    v.setShip(id);
    run(v, { ...emptyInput(), forward: 1 }, 2);
    results[id] = v.speed();
  }
  ok('the saucer is faster than the hauler', results.saucer > results.hauler);
  ok('the interceptor is faster than the shuttle', results.interceptor > results.shuttle);
  ok('all four ships reach different speeds',
     new Set(Object.values(results).map((s) => s.toFixed(1))).size === 4,
     JSON.stringify(results));
}

console.log('\n— flight: rotation and orientation stay valid —');
{
  const v = new VehicleController();
  v.setMode('fly');
  run(v, { ...emptyInput(), yaw: 1, pitch: 0.5, roll: 1, forward: 1 }, 20);
  const q = v.orientation;
  const len = Math.hypot(q.x, q.y, q.z, q.w);
  ok(`orientation stays normalised after heavy rotation (${len.toFixed(6)})`,
     Math.abs(len - 1) < 1e-4);
  ok('position stays finite', [v.position.x, v.position.y, v.position.z].every(Number.isFinite));
  const ax = v.axes();
  ok('local axes stay unit length',
     Math.abs(ax.fwd.length() - 1) < 1e-4 && Math.abs(ax.up.length() - 1) < 1e-4);
  ok('local axes stay orthogonal',
     Math.abs(Vector3.Dot(ax.fwd, ax.up)) < 1e-4 &&
     Math.abs(Vector3.Dot(ax.fwd, ax.right)) < 1e-4);
}

console.log('\n— free fly: direct motion with no inertia —');
{
  const v = new VehicleController();
  v.setMode('freefly');
  ok('free fly is a real mode', v.mode === 'freefly');

  const start = v.position.clone();
  run(v, { ...emptyInput(), forward: 1 }, 1);
  const moved = Vector3.Distance(v.position, start);
  ok(`holding forward moves you (${moved.toFixed(1)} u)`, moved > 5);

  // the defining difference from ship flight: releasing stops you dead
  const stopped = v.position.clone();
  run(v, emptyInput(), 2);
  ok('releasing the key stops you immediately (no inertia)',
     Vector3.Distance(v.position, stopped) < 1e-6,
     `drifted ${Vector3.Distance(v.position, stopped)}`);

  // and unlike walking there is no gravity
  const y = v.position.y;
  run(v, emptyInput(), 3);
  ok('you do not fall in free fly', Math.abs(v.position.y - y) < 1e-6);
}

console.log('\n— free fly: speed scales to what you are near —');
{
  const v = new VehicleController();
  v.setMode('freefly');

  v.setScaleSpeed(10);
  const slow = v.flySpeed;
  v.setScaleSpeed(100000);
  const fast = v.flySpeed;
  ok(`speed scales with distance (${slow.toFixed(0)} -> ${fast.toFixed(0)} u/s)`,
     fast > slow * 50);
  ok('speed never drops to zero, so you are never stuck', slow >= 6);
  ok('speed is capped so you cannot lose the universe', fast <= 60000);

  v.setScaleSpeed(NaN);
  ok('a NaN scale falls back to something usable',
     Number.isFinite(v.flySpeed) && v.flySpeed > 0);
  v.setScaleSpeed(-500);
  ok('a negative scale is handled', Number.isFinite(v.flySpeed) && v.flySpeed > 0);
}

console.log('\n— free fly: boost and brake —');
{
  // measure distance TRAVELLED, not distance from the origin - the
  // controller does not start at (0,0,0)
  const mk = () => { const v = new VehicleController(); v.setMode('freefly'); v.flySpeed = 100; return v; };
  const travel = (input) => {
    const v = mk();
    const s0 = v.position.clone();
    run(v, input, 1);
    return Vector3.Distance(v.position, s0);
  };
  const da = travel({ ...emptyInput(), forward: 1 });
  const db = travel({ ...emptyInput(), forward: 1, boost: true });
  const dc = travel({ ...emptyInput(), forward: 1, brake: true });
  ok(`boost is much faster (${da.toFixed(0)} -> ${db.toFixed(0)})`, db > da * 5);
  ok(`brake is much slower (${da.toFixed(0)} -> ${dc.toFixed(0)})`, dc < da * 0.5);
  ok('braking still allows precise movement', dc > 0);
}

console.log('\n— walking: gravity and ground clamping —');
{
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 50, 0));
  run(v, emptyInput(), 5, 1 / 60, flatGround);
  ok(`falls and lands on the ground (y=${v.position.y.toFixed(2)})`,
     Math.abs(v.position.y - v.walk.eyeHeight) < 0.01);
  ok('reports grounded after landing', v.grounded);
  ok('vertical velocity is cleared on landing', v.velocity.y === 0);
  ok('never sinks below the ground', v.position.y >= v.walk.eyeHeight - 1e-6);
}

console.log('\n— walking: movement —');
{
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 1.7, 0));
  run(v, emptyInput(), 0.5, 1 / 60, flatGround);
  const start = v.position.clone();
  run(v, { ...emptyInput(), forward: 1 }, 2, 1 / 60, flatGround);
  const walked = Math.hypot(v.position.x - start.x, v.position.z - start.z);
  ok(`walking moves horizontally (${walked.toFixed(1)} u)`, walked > 5);
  ok('walking does not change height on flat ground',
     Math.abs(v.position.y - start.y) < 0.01);

  const v2 = new VehicleController();
  v2.setMode('walk');
  v2.teleport(new Vector3(0, 1.7, 0));
  run(v2, emptyInput(), 0.5, 1 / 60, flatGround);
  const s2 = v2.position.clone();
  run(v2, { ...emptyInput(), forward: 1, run: true }, 2, 1 / 60, flatGround);
  const ran = Math.hypot(v2.position.x - s2.x, v2.position.z - s2.z);
  ok(`running is faster than walking (${ran.toFixed(1)} vs ${walked.toFixed(1)})`, ran > walked);
}

console.log('\n— walking: looking up must not launch you —');
{
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 1.7, 0));
  run(v, emptyInput(), 0.5, 1 / 60, flatGround);
  // pitch fully up, then walk forward
  run(v, { ...emptyInput(), pitch: -1 }, 2, 1 / 60, flatGround);
  const y0 = v.position.y;
  run(v, { ...emptyInput(), forward: 1 }, 2, 1 / 60, flatGround);
  ok('walking while looking up keeps you on the ground',
     Math.abs(v.position.y - y0) < 0.01, `${v.position.y}`);
}

console.log('\n— walking: pitch is clamped so the view cannot flip —');
{
  const v = new VehicleController();
  v.setMode('walk');
  run(v, { ...emptyInput(), pitch: 1 }, 20, 1 / 60, flatGround);
  const up = v.axes().up;
  ok('the up axis never inverts, no matter how far you look', up.y > 0, `${up.y}`);
}

console.log('\n— walking: jumping —');
{
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 1.7, 0));
  run(v, emptyInput(), 0.5, 1 / 60, flatGround);
  ok('grounded before jumping', v.grounded);
  v.update(1 / 60, { ...emptyInput(), jump: true }, flatGround);
  ok('jumping leaves the ground', !v.grounded && v.velocity.y > 0);
  let peak = v.position.y;
  for (let i = 0; i < 300; i++) {
    v.update(1 / 60, emptyInput(), flatGround);
    peak = Math.max(peak, v.position.y);
  }
  ok(`the jump reaches a sensible height (${(peak - 1.7).toFixed(2)} u)`,
     peak - 1.7 > 1 && peak - 1.7 < 20);
  ok('you land again', v.grounded);

  // you must not be able to jump in mid-air
  v.update(1 / 60, { ...emptyInput(), jump: true }, flatGround);
  const vy = v.velocity.y;
  for (let i = 0; i < 5; i++) v.update(1 / 60, { ...emptyInput(), jump: true }, flatGround);
  ok('no infinite mid-air jumping', v.velocity.y < vy + 1e-6 || v.grounded);
}

console.log('\n— walking: uneven terrain —');
{
  const hilly = (x, z) => ({
    height: Math.sin(x * 0.2) * 4 + Math.cos(z * 0.2) * 3,
    normal: new Vector3(0, 1, 0)
  });
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 40, 0));
  run(v, emptyInput(), 6, 1 / 60, hilly);
  const expected = hilly(v.position.x, v.position.z).height + v.walk.eyeHeight;
  ok('lands correctly on uneven ground', Math.abs(v.position.y - expected) < 0.05,
     `${v.position.y.toFixed(2)} vs ${expected.toFixed(2)}`);
  run(v, { ...emptyInput(), forward: 1, run: true }, 6, 1 / 60, hilly);
  const exp2 = hilly(v.position.x, v.position.z).height + v.walk.eyeHeight;
  ok('stays on the surface while walking over hills',
     Math.abs(v.position.y - exp2) < 0.6,
     `${v.position.y.toFixed(2)} vs ${exp2.toFixed(2)}`);
}

console.log('\n— ground snapping must not break jumping —');
{
  const hilly = (x, z) => ({
    height: Math.sin(x * 0.2) * 4 + Math.cos(z * 0.2) * 3,
    normal: new Vector3(0, 1, 0)
  });
  const v = new VehicleController();
  v.setMode('walk');
  v.teleport(new Vector3(0, 40, 0));
  run(v, emptyInput(), 6, 1 / 60, hilly);
  ok('grounded on a hill', v.grounded);
  v.update(1 / 60, { ...emptyInput(), jump: true }, hilly);
  ok('can still jump on a slope (snapping does not glue you down)',
     !v.grounded && v.velocity.y > 0);
  let peak = v.position.y;
  for (let i = 0; i < 400; i++) {
    v.update(1 / 60, emptyInput(), hilly);
    peak = Math.max(peak, v.position.y);
  }
  const groundY = hilly(v.position.x, v.position.z).height + v.walk.eyeHeight;
  ok(`the jump still clears real height (${(peak - groundY).toFixed(2)} u)`,
     peak - groundY > 0.8);

  // and a genuine fall off a cliff must NOT be snapped
  const cliff = (x) => ({ height: x > 5 ? -60 : 0, normal: new Vector3(0, 1, 0) });
  const c = new VehicleController();
  c.setMode('walk');
  c.teleport(new Vector3(0, 1.7, 0));
  run(c, emptyInput(), 0.5, 1 / 60, cliff);
  run(c, { ...emptyInput(), forward: 0, right: 1, run: true }, 1.2, 1 / 60, cliff);
  ok('walking off a cliff still falls (snapping is limited to small steps)',
     c.position.y < 0 || !c.grounded, `y=${c.position.y.toFixed(1)} grounded=${c.grounded}`);
}

console.log('\n— key mapping —');
{
  const i = inputFromKeys(new Set(['w', 'd', 'shift']));
  ok('W drives forward', i.forward === 1);
  ok('D strafes right', i.right === 1);
  ok('Shift boosts', i.boost === true);
  const j = inputFromKeys(new Set(['w', 's']));
  ok('opposing keys cancel out', j.forward === 0);
  const k = inputFromKeys(new Set());
  ok('no keys means no input', k.forward === 0 && k.yaw === 0 && !k.jump);
  ok('space jumps', inputFromKeys(new Set([' '])).jump === true);
}

console.log('\n— robustness —');
{
  const v = new VehicleController();
  v.setMode('fly');
  ok('zero dt is ignored', (() => { v.update(0, { ...emptyInput(), forward: 1 }); return v.speed() === 0; })());
  ok('NaN dt is ignored', (() => { v.update(NaN, { ...emptyInput(), forward: 1 }); return v.speed() === 0; })());
  v.update(-1, { ...emptyInput(), forward: 1 });
  ok('negative dt is ignored', v.speed() === 0);

  // walking with no ground probe must not fall forever into NaN
  const w = new VehicleController();
  w.setMode('walk');
  run(w, { ...emptyInput(), forward: 1 }, 10);
  ok('walking without a ground probe stays finite',
     [w.position.x, w.position.y, w.position.z].every(Number.isFinite));

  v.reset();
  ok('reset clears velocity and odometer', v.speed() === 0 && v.odometer === 0);
  ok('stats render without throwing', !!v.stats().Mode);
}

console.log('\n— odometer —');
{
  const v = new VehicleController();
  v.setMode('fly');
  run(v, { ...emptyInput(), forward: 1 }, 3);
  ok('the odometer accumulates distance', v.odometer > 1);
  const o = v.odometer;
  v.setMode('orbit');
  run(v, { ...emptyInput(), forward: 1 }, 3);
  ok('orbit mode does not add distance', v.odometer === o);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
