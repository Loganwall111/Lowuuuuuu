/**
 * GameModes verification — Sandbox and Explorer must genuinely differ, and
 * tidal disruption must be physics rather than an animation.
 * Run: node tools/modes-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/GameModes.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/modes-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  MODES, GAME_MODES, capabilities, can, tidalState, rocheRadii, isDoomed,
  describeTidal, SHIP, ROCKY_PLANET, CALM
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

console.log('\n— there are exactly two modes, and both are open world —');
{
  ok('explorer and sandbox both exist',
     GAME_MODES.length === 2 && MODES.explorer && MODES.sandbox);
  ok('both modes let you travel', can('explorer', 'travel') && can('sandbox', 'travel'));
  ok('both modes let you enter a black hole',
     can('explorer', 'enterHoles') && can('sandbox', 'enterHoles'));
  ok('every mode has copy for the UI',
     GAME_MODES.every((m) => MODES[m].name && MODES[m].blurb && MODES[m].glyph));
}

console.log('\n— physics belongs to sandbox mode —');
{
  const physics = ['throwing', 'grabbing', 'moveBlackHoles', 'timeTravel',
                   'spaghettification', 'destruction', 'spawning'];
  ok('sandbox allows every physics capability',
     physics.every((c) => can('sandbox', c)),
     physics.filter((c) => !can('sandbox', c)).join(','));
  ok('explorer allows none of them',
     physics.every((c) => !can('explorer', c)),
     physics.filter((c) => can('explorer', c)).join(','));
  ok('the user asked for throwing things at planets in sandbox',
     can('sandbox', 'throwing') && !can('explorer', 'throwing'));
  ok('the user asked to move black holes in sandbox',
     can('sandbox', 'moveBlackHoles') && !can('explorer', 'moveBlackHoles'));
  ok('the user asked to go back in time in sandbox',
     can('sandbox', 'timeTravel') && !can('explorer', 'timeTravel'));
}

console.log('\n— an unknown mode is safe, not broken —');
{
  ok('an unknown mode falls back to explorer capabilities',
     capabilities('nonsense').throwing === false &&
     capabilities('nonsense').travel === true);
  ok('capabilities never returns undefined',
     [undefined, null, '', 'EXPLORER'].every((m) => !!capabilities(m)));
}

console.log('\n— spaghettification is off in explorer mode —');
{
  const st = tidalState(SHIP, 1.2, 20, false);
  ok('a disabled tidal field leaves a ship untouched',
     st.stretch === 1 && !st.disrupting && !st.consumed);
  ok('the calm state is genuinely inert',
     CALM.stretch === 1 && CALM.stress === 0 && !CALM.consumed);
  ok('sitting on the horizon in explorer mode does nothing',
     tidalState(SHIP, 1, 20, false).consumed === false);
}

console.log('\n— in sandbox, a ship that gets too close is stretched —');
{
  const hz = 20;
  const far = tidalState(SHIP, hz * 400, hz, true);
  ok('far away the ship is intact', !far.disrupting && far.stretch < 1.05);
  ok('far away it is still being pulled', far.pull > 0);

  const near = tidalState(SHIP, hz * 2.05, hz, true);
  ok('near the horizon the ship is coming apart', near.disrupting, JSON.stringify(near));
  ok('and it is visibly elongated', near.stretch > 2, String(near.stretch));
  ok('stretching squeezes it sideways, preserving volume',
     Math.abs(near.stretch * near.squeeze * near.squeeze - 1) < 0.02);

  const inside = tidalState(SHIP, hz * 0.5, hz, true);
  ok('inside the horizon the ship is consumed', inside.consumed);
  ok('a consumed ship is fully shredded', inside.shredded === 1);
}

console.log('\n— stretching increases monotonically as you fall in —');
{
  const hz = 20;
  const ds = [500, 200, 90, 40, 20, 12, 8, 5, 3, 2.2].map((r) => r * hz);
  let mono = true;
  for (let i = 1; i < ds.length; i++) {
    const a = tidalState(SHIP, ds[i - 1], hz, true);
    const b = tidalState(SHIP, ds[i], hz, true);
    if (b.stretch < a.stretch - 1e-9) mono = false;
    if (b.pull < a.pull - 1e-9) mono = false;
  }
  ok('closer always means more stretched and more strongly pulled', mono);
  ok('stretch is capped so a renderer never gets an absurd scale',
     tidalState(SHIP, hz * 1.001, hz, true).stretch <= 40);
}

console.log('\n— a loosely bound planet is torn apart far outside a ship —');
{
  ok('a rocky planet has a wider Roche limit than a hull',
     rocheRadii(ROCKY_PLANET) > rocheRadii(SHIP),
     rocheRadii(ROCKY_PLANET).toFixed(2) + ' vs ' + rocheRadii(SHIP).toFixed(2));
  const hz = 60, d = hz * rocheRadii(SHIP) * 1.4;
  ok('at a distance where the ship survives, the planet does not',
     !tidalState(SHIP, d, hz, true).disrupting &&
      tidalState(ROCKY_PLANET, d, hz, true).disrupting);
  ok('dust comes apart even further out',
     rocheRadii({ size: 1, cohesion: 0.05, mass: 1 }) > rocheRadii(ROCKY_PLANET));
}

console.log('\n— the player can be warned before it is too late —');
{
  const hz = 30;
  ok('a ship well clear is not doomed', !isDoomed(SHIP, hz * 60, hz));
  ok('a ship inside its Roche limit is doomed',
     isDoomed(SHIP, hz * rocheRadii(SHIP) * 0.8, hz));
  ok('a ship inside the horizon is doomed', isDoomed(SHIP, hz * 0.3, hz));
  const d = describeTidal(tidalState(SHIP, hz * 2.05, hz, true));
  ok('the HUD describes what is happening', /apart|debris|straining/.test(d.Status), d.Status);
  ok('the HUD reports elongation', /×$/.test(d.Elongation));
}

console.log('\n— garbage cannot break the tidal model —');
{
  let bad = [];
  for (const dist of [NaN, -10, 0, Infinity, 1e18]) {
    for (const hz of [NaN, 0, -5, 1e-9, 1e9]) {
      for (const body of [SHIP, ROCKY_PLANET, { size: 0, cohesion: 0, mass: 0 }]) {
        try {
          const st = tidalState(body, dist, hz, true);
          for (const k of ['stretch', 'squeeze', 'shredded', 'pull']) {
            if (!Number.isFinite(st[k])) bad.push(`${dist}/${hz} ${k}=${st[k]}`);
          }
          if (st.stretch < 1) bad.push(`${dist}/${hz} stretch<1`);
        } catch (e) {
          bad.push(`${dist}/${hz} threw ${e.message}`);
        }
      }
    }
  }
  ok('every extreme distance/horizon pair yields a finite, sane state',
     bad.length === 0, bad.slice(0, 3).join(' | '));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
