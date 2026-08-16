/**
 * HoleInterior verification — falling into a black hole must be a journey of
 * real distance with a reachable destination, and the rare finds must be
 * genuinely rare AND genuinely stable.
 * Run: node tools/interior-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/HoleInterior.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/interior-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  interiorPlan, fallState, destinationFor, throughSingularity, describeFall,
  isGargantua, GARGANTUA_CHANCE, NESTED_CHANCE, MIN_DEPTH, MAX_DEPTH,
  GARGANTUA_DEPTH
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const SEEDS = Array.from({ length: 20000 }, (_, i) => (i * 2654435761) >>> 0);

console.log('\n— the interior is deep enough to be a journey —');
{
  const plans = SEEDS.slice(0, 4000).map(interiorPlan);
  const depths = plans.map((p) => p.depth);
  const min = Math.min(...depths), max = Math.max(...depths);
  ok('every interior is thousands of units deep', min >= MIN_DEPTH * 0.99,
     'shallowest ' + Math.round(min));
  ok('the user asked for "very very very deep": the shallowest is over 4000',
     min > 4000, String(Math.round(min)));
  // Gargantua is the long-timeline void: deliberately far deeper than the
  // ordinary range, so an idle fall through it lasts minutes. It is still
  // bounded, just by its own constant, which keeps every fall finite.
  ok('depth is bounded, so a fall always ends',
     max <= GARGANTUA_DEPTH * 1.01,
     'deepest ' + Math.round(max));
  ok('depths vary between holes', new Set(depths.map((d) => Math.round(d))).size > 3000);
}

console.log('\n— every hole leads somewhere; none is a dead end —');
{
  let bad = [];
  for (const s of SEEDS.slice(0, 5000)) {
    const plan = interiorPlan(s);
    const st = fallState(plan, plan.depth);
    if (!st.complete) { bad.push('seed ' + s + ' never completes'); continue; }
    const d = destinationFor(plan, false);
    if (!d || !d.kind || !Number.isFinite(d.seed) || !Number.isFinite(d.depth)) {
      bad.push('seed ' + s + ' has no destination');
    }
  }
  ok('falling the full depth always completes and yields a destination',
     bad.length === 0, bad.slice(0, 2).join(' | '));
}

console.log('\n— Gargantua is rare, stable, and the only route to the Library —');
{
  const hits = SEEDS.filter(isGargantua).length;
  const rate = hits / SEEDS.length;
  ok('Gargantua exists in the universe', hits > 0, String(hits));
  ok('Gargantua is rare (within 2x of the declared chance)',
     rate > GARGANTUA_CHANCE * 0.5 && rate < GARGANTUA_CHANCE * 2,
     'rate ' + rate.toFixed(5) + ' vs ' + GARGANTUA_CHANCE.toFixed(5));

  const g = SEEDS.find(isGargantua);
  ok('the same hole is Gargantua every time you return',
     isGargantua(g) && isGargantua(g) && isGargantua(g));
  ok('Gargantua always leads to the Library Realm',
     destinationFor(interiorPlan(g), false).realm === 'library');
  ok('Gargantua leads to the Library even through a singularity',
     destinationFor(interiorPlan(g), true).realm === 'library');
  ok('Gargantua is deeper than an ordinary hole',
     interiorPlan(g).depth > MIN_DEPTH * 2);
  ok('the Library is only ever reached from Gargantua',
     SEEDS.slice(0, 5000).every((s) => {
       const p = interiorPlan(s);
       const isLib = destinationFor(p, false).realm === 'library'
                  || destinationFor(p, true).realm === 'library';
       return isLib === p.gargantua;
     }));
}

console.log('\n— falling into Gargantua is dark before it is anything —');
{
  const g = SEEDS.find(isGargantua);
  const plan = interiorPlan(g);
  const late = fallState(plan, plan.depth * 0.9);
  ok('deep inside Gargantua the phase is darkness', late.phase === 'darkness',
     late.phase);
  ok('darkness still comes after a visible throat',
     fallState(plan, plan.depth * 0.02).phase === 'throat');
}

console.log('\n— some holes have a nested lens and a singularity, some do not —');
{
  const plans = SEEDS.slice(0, 8000).map(interiorPlan);
  const nested = plans.filter((p) => p.nested).length;
  const rate = nested / plans.length;
  ok('a meaningful share of holes contain a second lens',
     rate > NESTED_CHANCE * 0.7 && rate < NESTED_CHANCE * 1.3,
     'rate ' + rate.toFixed(3));
  ok('but plenty of holes are simple all the way down',
     plans.some((p) => !p.nested && !p.gargantua));

  const simple = plans.find((p) => !p.nested && !p.gargantua);
  const st = fallState(simple, simple.depth * 0.9);
  ok('a simple hole never shows a nested lens', st.nestedLens === 0);
  ok('a simple hole never shows a singularity dot', st.singularity === 0);
  ok('a simple hole cannot be entered through a singularity',
     throughSingularity(simple, st, 0) === false);
}

console.log('\n— the singularity is a white dot you can choose to fly through —');
{
  const plan = SEEDS.map(interiorPlan).find((p) => p.nested);
  ok('a nested hole has a singularity with a real radius',
     plan.singularityRadius > 0);
  ok('the nested lens appears partway down, not at the horizon',
     plan.nestedAt > plan.depth * 0.4 && plan.nestedAt < plan.depth);

  const early = fallState(plan, plan.nestedAt * 0.5);
  ok('before that point there is no second lens', early.nestedLens === 0);

  const atLens = fallState(plan, plan.nestedAt + 1);
  ok('the lens resolves before the dot does',
     atLens.nestedLens > 0 && atLens.singularity === 0);

  const deep = fallState(plan, plan.depth * 0.995);
  ok('close in, the white dot is visible', deep.singularity > 0.55, String(deep.singularity));
  ok('aiming at it passes through', throughSingularity(plan, deep, 0) === true);
  ok('flying past it misses',
     throughSingularity(plan, deep, plan.singularityCapture * 3) === false);
  ok('passing through the singularity reaches the Dust Stream',
     destinationFor(plan, true).realm === 'duststream');
  ok('missing it reaches an ordinary dimension instead',
     destinationFor(plan, false).kind === 'procedural');
}

console.log('\n— you can always look back at where you came from —');
{
  const plan = interiorPlan(SEEDS[7]);
  const at = (f) => fallState(plan, plan.depth * f);
  ok('just inside the horizon the way back is wide', at(0.01).exitWindow > 0.9);
  ok('the window shrinks as you fall',
     at(0.05).exitWindow > at(0.3).exitWindow &&
     at(0.3).exitWindow > at(0.7).exitWindow);
  ok('it is still non-zero deep inside, so the way back never vanishes',
     at(0.9).exitWindow > 0);
  ok('the interior takes over the view as you descend',
     at(0.6).inside > at(0.1).inside);
  ok('the view is fully interior by the bottom', at(1).inside >= 1);
}

console.log('\n— the descent reports itself honestly —');
{
  const plan = interiorPlan(SEEDS[3]);
  const st = fallState(plan, plan.depth * 0.5);
  const d = describeFall(plan, st);
  ok('the HUD knows how far is left', /u$/.test(d.Remaining));
  ok('the HUD says whether the way back is visible', /visible|gone/.test(d['Way back']));
  ok('remaining distance falls to zero at the bottom',
     fallState(plan, plan.depth).remaining === 0);
  ok('remaining distance is the full depth at the horizon',
     Math.abs(fallState(plan, 0).remaining - plan.depth) < 1e-6);
}

console.log('\n— the same hole is the same hole, always —');
{
  const s = SEEDS[42];
  const a = interiorPlan(s), b = interiorPlan(s);
  ok('interior plans are deterministic',
     a.depth === b.depth && a.nested === b.nested &&
     a.gargantua === b.gargantua && a.nestedAt === b.nestedAt);
  ok('destinations are deterministic',
     destinationFor(a, false).seed === destinationFor(b, false).seed);
  ok('different holes have different interiors',
     new Set(SEEDS.slice(0, 2000).map((x) => Math.round(interiorPlan(x).depth))).size > 1500);
}

console.log('\n— garbage input cannot break a descent —');
{
  let bad = [];
  for (const seed of [0, -1, NaN, 4294967296, 1.5, -2147483648]) {
    for (const fallen of [NaN, -500, 0, Infinity, 1e12]) {
      try {
        const p = interiorPlan(seed);
        const st = fallState(p, fallen);
        if (!st || !st.phase) { bad.push(seed + '/' + fallen + ' no phase'); continue; }
        for (const k of ['progress', 'inside', 'exitWindow', 'nestedLens',
                         'singularity', 'remaining']) {
          if (!Number.isFinite(st[k])) bad.push(seed + '/' + fallen + ' ' + k + '=' + st[k]);
        }
        if (st.progress < 0 || st.progress > 1) bad.push(seed + '/' + fallen + ' progress');
        if (st.inside < 0 || st.inside > 1) bad.push(seed + '/' + fallen + ' inside');
      } catch (e) {
        bad.push(seed + '/' + fallen + ' threw ' + e.message);
      }
    }
  }
  ok('every extreme seed/distance pair still yields a valid state',
     bad.length === 0, bad.slice(0, 3).join(' | '));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
