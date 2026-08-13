/**
 * GrabSystem verification — you must be able to grab a black hole and drag
 * it around, and nothing you do with it may corrupt the physics.
 * Run: node tools/grab-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['tools/fixtures/grab-entry.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/grab-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { GrabSystem, Vector3 } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const obj = (id, pos, radius = 10, withVel = true) => ({
  id, name: id, position: pos.clone(),
  velocity: withVel ? new Vector3(0, 0, 0) : undefined,
  radius
});

console.log('\n— picking what the cursor is over —');
{
  const g = new GrabSystem();
  const a = obj('a', new Vector3(0, 0, 100));
  const b = obj('b', new Vector3(0, 0, 300));
  const off = obj('off', new Vector3(500, 0, 100));
  const list = [a, b, off];

  const hit = g.pick(new Vector3(0, 0, 0), new Vector3(0, 0, 1), list);
  ok('the ray picks the object in front of it', hit === a, hit ? hit.id : 'none');
  ok('it does not pick something off to the side', hit !== off);

  const behind = g.pick(new Vector3(0, 0, 0), new Vector3(0, 0, -1), list);
  ok('nothing behind the camera is picked', behind === null);

  const miss = g.pick(new Vector3(0, 0, 0), new Vector3(0, 1, 0), list);
  ok('a ray pointing at nothing picks nothing', miss === null);

  // a big distant object should still be grabbable
  const huge = obj('huge', new Vector3(0, 0, 90000), 4000);
  const gotHuge = g.pick(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [huge]);
  ok('a huge distant object can still be grabbed', gotHuge === huge);

  ok('a zero-length ray direction is safe',
     g.pick(new Vector3(0, 0, 0), new Vector3(0, 0, 0), list) === null);
  ok('an empty candidate list is safe',
     g.pick(new Vector3(0, 0, 0), new Vector3(0, 0, 1), []) === null);
}

console.log('\n— grabbing and carrying —');
{
  const g = new GrabSystem();
  const bh = obj('blackhole', new Vector3(0, 0, 200), 20);
  ok('nothing is held initially', !g.isHolding());

  const got = g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);
  ok('grabbing picks up the object', got === bh);
  ok('the system reports holding it', g.isHolding());
  ok('the grab counter increments', g.grabs === 1);

  // looking elsewhere drags it along
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('the held object follows where you look', bh.position.x > 100,
     `x=${bh.position.x}`);
  ok('it keeps roughly its original distance',
     Math.abs(bh.position.length() - 200) < 1, `${bh.position.length()}`);

  // moving the camera carries it too
  g.update(1 / 60, new Vector3(0, 500, 0), new Vector3(1, 0, 0));
  ok('moving the camera carries it', bh.position.y > 100);
  ok('the position stays finite',
     [bh.position.x, bh.position.y, bh.position.z].every(Number.isFinite));
}

console.log('\n— a held object does not drift —');
{
  const g = new GrabSystem();
  const bh = obj('bh', new Vector3(0, 0, 200), 20);
  bh.velocity.set(500, 500, 500);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);
  ok('grabbing zeroes existing velocity', bh.velocity.length() === 0);
  for (let i = 0; i < 60; i++) g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  ok('a held object stays put while the camera is still',
     Math.abs(bh.position.z - 200) < 1, `${bh.position.z}`);
  ok('velocity stays zero while held', bh.velocity.length() === 0);
}

console.log('\n— adjusting how far away you hold it —');
{
  const g = new GrabSystem();
  const bh = obj('bh', new Vector3(0, 0, 200), 20);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);

  g.adjustDistance(1);
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  ok('pushing moves it further away', bh.position.z > 200, `${bh.position.z}`);

  const far = bh.position.z;
  g.adjustDistance(-1);
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  ok('pulling brings it closer', bh.position.z < far);

  // it must never be pulled inside the camera
  for (let i = 0; i < 200; i++) g.adjustDistance(-1);
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  ok('it can never be pulled through the camera',
     bh.position.z >= bh.radius, `${bh.position.z}`);

  ok('adjusting with nothing held is safe',
     (() => { const e = new GrabSystem(); e.adjustDistance(5); return true; })());
}

console.log('\n— releasing —');
{
  const g = new GrabSystem();
  const bh = obj('bh', new Vector3(0, 0, 200), 20);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);
  const where = bh.position.clone();
  const released = g.release();
  ok('release returns the object', released === bh);
  ok('nothing is held afterwards', !g.isHolding());
  ok('it stays where it was left', bh.position.equals(where));
  ok('it is left stationary', bh.velocity.length() === 0);
  ok('releasing with nothing held is safe', g.release() === null);

  // updating after release must not move anything
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('updating after release moves nothing', bh.position.equals(where));
}

console.log('\n— throwing —');
{
  const g = new GrabSystem();
  const bh = obj('bh', new Vector3(0, 0, 200), 20);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);
  // swing it hard
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  const thrown = g.throwIt();
  ok('throw returns the object', thrown === bh);
  ok('nothing is held afterwards', !g.isHolding());
  ok('it leaves with real velocity', bh.velocity.length() > 1,
     `${bh.velocity.length()}`);
  ok('the thrown velocity is finite',
     [bh.velocity.x, bh.velocity.y, bh.velocity.z].every(Number.isFinite));
  ok('the throw counter increments', g.throws === 1);
  ok('throwing with nothing held is safe', g.throwIt() === null);

  // a gentle carry should produce a gentle throw
  const g2 = new GrabSystem();
  const b2 = obj('b2', new Vector3(0, 0, 200), 20);
  g2.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [b2]);
  for (let i = 0; i < 30; i++) g2.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  g2.throwIt();
  ok('holding still produces a near-zero throw', b2.velocity.length() < 1,
     `${b2.velocity.length()}`);
}

console.log('\n— throw strength is tunable —');
{
  const mk = (strength) => {
    const g = new GrabSystem();
    g.throwStrength = strength;
    const b = obj('b', new Vector3(0, 0, 200), 20);
    g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [b]);
    g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
    g.throwIt();
    return b.velocity.length();
  };
  const weak = mk(0.25), strong = mk(4);
  ok(`throw strength scales the result (${weak.toFixed(0)} -> ${strong.toFixed(0)})`,
     strong > weak * 4 - 1);
}

console.log('\n— objects without velocity can still be moved —');
{
  const g = new GrabSystem();
  // a region has a position but no velocity field
  const region = obj('region', new Vector3(0, 0, 200), 50, false);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [region]);
  ok('a velocity-less object can be grabbed', g.isHolding());
  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('it still moves', region.position.x > 100);
  let err = null;
  try { g.throwIt(); } catch (e) { err = e; }
  ok('throwing a velocity-less object does not crash', !err, err ? err.message : '');
}

console.log('\n— robustness —');
{
  const g = new GrabSystem();
  const bh = obj('bh', new Vector3(0, 0, 200), 20);
  g.grab(new Vector3(0, 0, 0), new Vector3(0, 0, 1), [bh]);

  const good = bh.position.clone();
  g.update(0, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('zero dt does not move it', bh.position.equals(good));
  g.update(NaN, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('NaN dt does not move it', bh.position.equals(good));
  g.update(-1, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
  ok('negative dt does not move it', bh.position.equals(good));

  g.update(1 / 60, new Vector3(NaN, 0, 0), new Vector3(1, 0, 0));
  ok('a NaN camera position never corrupts the object',
     [bh.position.x, bh.position.y, bh.position.z].every(Number.isFinite));

  g.update(1 / 60, new Vector3(0, 0, 0), new Vector3(0, 0, 0));
  ok('a zero look direction never corrupts the object',
     [bh.position.x, bh.position.y, bh.position.z].every(Number.isFinite));

  ok('stats render', !!g.stats().Holding);
  g.release();
  ok('stats render with nothing held', g.stats().Holding === '—');
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
