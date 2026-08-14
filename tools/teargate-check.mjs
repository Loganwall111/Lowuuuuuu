/**
 * TearGate verification — descending through the infinite dimensions must be
 * something you FLY through, reliably, at any speed. A rift you can tunnel
 * through at warp is not a door.
 * Run: node tools/teargate-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/TearGate.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/teargate-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { TearGate, crossesTear } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const V = (x, y, z) => ({ x, y, z });
/** A ring at the origin, facing down +Z, radius 8. */
const ring = {
  id: 'tear-0', position: V(0, 0, 0), radius: 8, normal: V(0, 0, 1)
};

console.log('\n— flying through a tear is detected —');
{
  ok('straight through the middle counts',
     !!crossesTear(ring, V(0, 0, -10), V(0, 0, 10)));
  ok('the crossing reports how close to centre you were',
     crossesTear(ring, V(0, 0, -10), V(0, 0, 10)).offset < 1e-9);
  ok('off-centre but inside the ring counts',
     !!crossesTear(ring, V(5, 0, -10), V(5, 0, 10)));
  ok('and reports the real offset',
     Math.abs(crossesTear(ring, V(5, 0, -10), V(5, 0, 10)).offset - 5) < 1e-9);
  ok('going the other way works too - a hole has no front',
     !!crossesTear(ring, V(0, 0, 10), V(0, 0, -10)));
  ok('a diagonal path through the opening counts',
     !!crossesTear(ring, V(-3, -2, -10), V(3, 2, 10)));
}

console.log('\n— missing a tear does nothing —');
{
  ok('passing wide of the ring misses',
     crossesTear(ring, V(40, 0, -10), V(40, 0, 10)) === null);
  ok('just outside the rim misses',
     crossesTear(ring, V(8.01, 0, -10), V(8.01, 0, 10)) === null);
  ok('just inside the rim counts',
     !!crossesTear(ring, V(7.99, 0, -10), V(7.99, 0, 10)));
  ok('flying parallel to the ring never crosses it',
     crossesTear(ring, V(-50, 0, -5), V(50, 0, -5)) === null);
  ok('approaching but stopping short misses',
     crossesTear(ring, V(0, 0, -50), V(0, 0, -1)) === null);
  ok('moving away from the front never crosses',
     crossesTear(ring, V(0, 0, 5), V(0, 0, 90)) === null);
}

console.log('\n— you cannot tunnel through a tear at warp —');
{
  // The whole reason this is a segment test: at speed a frame can cover
  // thousands of units, and a proximity check would simply never fire.
  ok('a single 20,000-unit frame still registers the crossing',
     !!crossesTear(ring, V(0, 0, -10000), V(0, 0, 10000)));
  ok('and it reports where along the frame it happened',
     Math.abs(crossesTear(ring, V(0, 0, -10000), V(0, 0, 10000)).t - 0.5) < 1e-9);
  ok('an enormous frame that misses still misses',
     crossesTear(ring, V(500, 0, -10000), V(500, 0, 10000)) === null);
}

console.log('\n— the gate needs two positions before it can fire —');
{
  const g = new TearGate();
  g.arm([ring]);
  ok('arming registers the tears', g.count === 1);
  // Burn off the cooldown.
  for (let i = 0; i < 30; i++) g.update(0.1, V(0, 0, -200));
  ok('the gate arms after its cooldown', g.armed);

  const g2 = new TearGate();
  g2.arm([ring]);
  ok('the very first frame cannot report a crossing',
     g2.update(0.1, V(0, 0, 50)) === null);
}

console.log('\n— flying through fires exactly once —');
{
  const g = new TearGate();
  g.arm([ring]);
  for (let i = 0; i < 30; i++) g.update(0.1, V(0, 0, -200));
  g.update(0.016, V(0, 0, -20));
  const hit = g.update(0.016, V(0, 0, 20));
  ok('the crossing is reported', !!hit, JSON.stringify(hit));
  ok('it names which tear', hit && hit.id === 'tear-0');

  // Immediately flying back must not fire again during the cooldown.
  const again = g.update(0.016, V(0, 0, -20));
  ok('flying straight back does not immediately fire again', again === null);
}

console.log('\n— a rebuilt world cannot report a phantom crossing —');
{
  // Arriving somewhere new teleports the player. Without arm() clearing the
  // previous position, that jump would be treated as a movement segment and
  // could cross a tear in the NEW world instantly.
  const g = new TearGate();
  g.arm([ring]);
  for (let i = 0; i < 30; i++) g.update(0.1, V(0, 0, -900));
  g.update(0.016, V(0, 0, -900));
  g.arm([ring]);                      // world rebuilt, player teleported
  const hit = g.update(0.016, V(0, 0, 900));
  ok('teleporting across a tear does not count as flying through it',
     hit === null);
}

console.log('\n— several tears can be watched at once —');
{
  const tears = [
    { id: 'a', position: V(0, 0, 100), radius: 8, normal: V(0, 0, 1) },
    { id: 'b', position: V(0, 0, -100), radius: 8, normal: V(0, 0, 1) },
    { id: 'c', position: V(100, 0, 0), radius: 8, normal: V(1, 0, 0) }
  ];
  const g = new TearGate();
  g.arm(tears);
  ok('every tear is watched', g.count === 3);
  for (let i = 0; i < 30; i++) g.update(0.1, V(0, 0, 0));
  g.update(0.016, V(0, 0, 90));
  const hit = g.update(0.016, V(0, 0, 110));
  ok('the correct tear is identified', hit && hit.id === 'a',
     JSON.stringify(hit));
}

console.log('\n— an unnormalised or odd tear still behaves —');
{
  const fat = { id: 'f', position: V(0, 0, 0), radius: 8, normal: V(0, 0, 7) };
  ok('a non-unit normal is normalised internally',
     !!crossesTear(fat, V(0, 0, -10), V(0, 0, 10)));
  const tilted = {
    id: 't', position: V(0, 0, 0), radius: 8,
    normal: V(0.577, 0.577, 0.577)
  };
  ok('a tilted tear can be flown through',
     !!crossesTear(tilted, V(-10, -10, -10), V(10, 10, 10)));
  ok('and missed', crossesTear(tilted, V(-60, 40, 0), V(-50, 50, 10)) === null);
}

console.log('\n— garbage cannot break the gate —');
{
  let bad = [];
  const junkTears = [
    null, undefined, {},
    { id: 'x', position: V(NaN, 0, 0), radius: 8, normal: V(0, 0, 1) },
    { id: 'y', position: V(0, 0, 0), radius: 0, normal: V(0, 0, 1) },
    { id: 'z', position: V(0, 0, 0), radius: -4, normal: V(0, 0, 1) },
    { id: 'w', position: V(0, 0, 0), radius: 8, normal: V(0, 0, 0) },
    { id: 'v', position: V(0, 0, 0), radius: NaN, normal: V(0, 0, 1) }
  ];
  for (const t of junkTears) {
    try { crossesTear(t, V(0, 0, -10), V(0, 0, 10)); }
    catch (e) { bad.push('tear threw ' + e.message); }
  }
  for (const p of [V(NaN, 0, 0), V(0, Infinity, 0), null, undefined]) {
    try { crossesTear(ring, p, V(0, 0, 10)); crossesTear(ring, V(0, 0, -10), p); }
    catch (e) { bad.push('pos threw ' + e.message); }
  }
  const g = new TearGate();
  g.arm(junkTears);
  for (const dt of [NaN, -1, 0, 1e9]) {
    for (const p of [V(NaN, 0, 0), null, V(0, 0, 5)]) {
      try { g.update(dt, p); } catch (e) { bad.push('update threw ' + e.message); }
    }
  }
  try { g.arm(null); g.update(0.1, V(0, 0, 0)); }
  catch (e) { bad.push('null arm threw ' + e.message); }
  ok('malformed tears, positions and deltas are all survived',
     bad.length === 0, bad.slice(0, 3).join(' | '));
  ok('a degenerate tear never reports a crossing',
     crossesTear({ id: 'w', position: V(0, 0, 0), radius: 8, normal: V(0, 0, 0) },
                 V(0, 0, -10), V(0, 0, 10)) === null);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
