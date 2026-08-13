/**
 * scale-check — zooming out of the universe into the tiers above it.
 *
 * The risk with a ladder like this is flapping at a boundary, or ascending
 * without being able to come back. Both are asserted here.
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry, tag) => {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${tag}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const { CosmicScaleSystem, TIERS } =
  await load('src/bjs/systems/CosmicScaleSystem.ts', 'scale');

console.log('— the ladder —');
{
  ok('there are several tiers to find', TIERS.length >= 5);
  ok('it starts in the universe', TIERS[0].id === 'universe');
  ok('the multiverse is above it', TIERS[1].id === 'multiverse');
  ok('then the metaverse', TIERS[2].id === 'metaverse');
  ok('and it keeps getting stranger',
     TIERS.some((t) => t.id === 'molecular'));

  for (const t of TIERS) {
    ok(`"${t.name}" is a described place`,
       !!t.tagline && !!t.inhabitants && t.boundary > 0);
    ok(`"${t.name}" has a distinct look`,
       Array.isArray(t.tint) && t.tint.length === 3 &&
       t.tint.every((c) => c >= 0 && c <= 1));
  }

  // Boundaries must grow outward, or you could never reach the far tiers.
  let rising = true;
  for (let i = 1; i < TIERS.length; i++) {
    if (TIERS[i].boundary <= TIERS[i - 1].boundary) rising = false;
  }
  ok('each tier is further out than the last', rising);

  const tints = new Set(TIERS.map((t) => t.tint.join(',')));
  ok('every tier looks different', tints.size === TIERS.length);
}

console.log('\n— ascending —');
{
  const s = new CosmicScaleSystem();
  ok('you start in the universe', s.tier.id === 'universe');

  let st = s.update(1000);
  ok('normal flight does not move you up a tier', st.depth === 0 && !st.changed);

  st = s.update(TIERS[0].boundary + 1);
  ok('passing the edge takes you to the multiverse',
     st.depth === 1 && st.changed && st.direction === 1);
  ok('crossing is announced', st.tier.id === 'multiverse');
  ok('the world shrinks as you ascend', s.scale < 1);

  st = s.update(TIERS[1].boundary + 1);
  ok('and further out is the metaverse', st.tier.id === 'metaverse');

  // Climb the whole ladder.
  const seen = new Set([0, 1, 2]);
  for (let i = 0; i < 40; i++) {
    const cur = s.tier;
    st = s.update(cur.boundary + 1);
    seen.add(st.depth);
  }
  ok(`every tier is reachable (${seen.size}/${TIERS.length})`,
     seen.size === TIERS.length);
}

console.log('\n— it loops —');
{
  const s = new CosmicScaleSystem();
  s.setDepth(TIERS.length - 1);
  ok('can jump to the outermost tier', s.tierDepth === TIERS.length - 1);

  const st = s.update(TIERS[TIERS.length - 1].boundary + 1);
  ok('going past the end wraps back to the universe',
     st.depth === 0 && st.tier.id === 'universe');
  ok('and the scale resets with it', Math.abs(s.scale - 1) < 1e-9);
}

console.log('\n— coming back down —');
{
  const s = new CosmicScaleSystem();
  s.update(TIERS[0].boundary + 1);
  ok('ascended', s.tierDepth === 1);

  // Falling back toward the middle must return you.
  let st = null;
  for (let i = 0; i < 30; i++) st = s.update(10);
  ok('flying back inward returns you to the universe', s.tierDepth === 0);
  ok('the world scale is restored', Math.abs(s.scale - 1) < 1e-6);
}

console.log('\n— it must never flicker at a boundary —');
{
  // Hovering exactly on a boundary used to be the obvious failure mode:
  // ascend, descend, ascend, dozens of times a second.
  const s = new CosmicScaleSystem();
  const edge = TIERS[0].boundary;
  let changes = 0;
  for (let i = 0; i < 400; i++) {
    // jitter around the boundary the way a drifting camera would
    const d = edge + Math.sin(i * 0.7) * (edge * 0.001);
    const st = s.update(d);
    if (st.changed) changes++;
  }
  ok(`sitting on the edge does not thrash (${changes} crossings)`, changes <= 2);

  ok('depth stays valid throughout',
     s.tierDepth >= 0 && s.tierDepth < TIERS.length);
  ok('scale stays finite and positive',
     Number.isFinite(s.scale) && s.scale > 0);
}

console.log('\n— bad input cannot break it —');
{
  const s = new CosmicScaleSystem();
  [NaN, Infinity, -Infinity, -50000, 0].forEach((v) => s.update(v));
  ok('nonsense distances are survived',
     Number.isFinite(s.scale) && s.scale > 0 &&
     s.tierDepth >= 0 && s.tierDepth < TIERS.length);

  s.setDepth(9999);
  ok('depth is clamped high', s.tierDepth === TIERS.length - 1);
  s.setDepth(-5);
  ok('depth is clamped low', s.tierDepth === 0);

  s.reset();
  ok('reset returns you home', s.tierDepth === 0 && s.scale === 1);

  const st = s.stats();
  ok('reports which tier you are in', !!st['Scale tier']);
  ok('reports how deep the ladder goes', /\d+ \/ \d+/.test(st['Tier depth']));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
