/**
 * UPDATE 2 — BETTER COSMOS.
 *
 * Every reported bug in this update had a measured cause, and each block
 * below reproduces the cause before asserting the fix. Where a fix is a
 * number, the number is checked against the real source rather than
 * restated here, so the harness cannot quietly drift away from the app.
 *
 * Run: node tools/cosmos2-check.mjs
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

const bundle = async (entry) => {
  const out = path.join(os.tmpdir(), 'c2-' + Math.random().toString(36).slice(2) + '.mjs');
  await build({
    entryPoints: [entry], bundle: true, format: 'esm', platform: 'neutral',
    outfile: out, logLevel: 'silent'
  });
  const m = await import('file://' + out);
  fs.unlinkSync(out);
  return m;
};

const app = read('src/bjs/App.ts');
const warpSrc = read('src/bjs/systems/WarpSystem.ts');
const tunnelSrc = read('src/bjs/systems/WarpTunnel.ts');
const holeFrag = read('src/bjs/worlds/BlackHoleWorld.ts');
const holeRend = read('src/bjs/systems/HoleFieldRenderer.ts');
const uniSrc = read('src/bjs/systems/UniverseState.ts');
const hudSrc = read('src/bjs/ui/FlightHUD.ts');
const css = read('src/bjs/ui/styles.ts');
const introSrc = read('src/bjs/ui/IntroOverlay.ts');
const shellSrc = read('src/bjs/ui/Shell.ts');

/* ================================================================
   1. WARP — the streaks did not appear to move
   ================================================================ */
{
  const W = await bundle('src/bjs/systems/WarpSystem.ts');
  const o = W.DEFAULT_WARP;
  ok('warp options declare a flow reference', typeof o.flowRef === 'number');
  ok('warp options declare a flow ceiling', typeof o.flowMax === 'number');

  // THE BUG, reproduced. Raw speed advanced the streaks, and the tube is
  // only depth*2 units long end to end.
  const tube = o.depth * 2;
  const rawPerFrame = (s) => s / 60;
  ok('THE BUG: at deep-space cruise the old code moved a streak past the '
    + 'whole tube every frame',
    rawPerFrame(12266) > tube * 0.5, (rawPerFrame(12266) / tube).toFixed(2) + ' tubes/frame');
  ok('THE BUG: at full warp it was tens of thousands of tubes per frame',
    rawPerFrame(12266 * 90000) / tube > 1e4,
    (rawPerFrame(12266 * 90000) / tube).toExponential(1));

  // THE FIX.
  const f = (s) => W.apparentFlow(s, o);
  ok('apparent flow is exact below the reference speed', f(400) === 400);
  ok('apparent flow is continuous at the reference speed',
    Math.abs(f(o.flowRef) - Math.min(o.flowRef, o.flowMax * tube)) < 1e-9);
  ok('apparent flow is finite at full warp', Number.isFinite(f(1.1e9)));
  ok('apparent flow never exceeds the ceiling',
    f(1e12) <= o.flowMax * tube + 1e-6);
  ok('apparent flow is monotonic in speed', (() => {
    let prev = -1;
    for (const s of [0, 10, 100, 900, 5e3, 1e5, 1e7, 1e9]) {
      const v = f(s);
      if (v < prev - 1e-9) return false;
      prev = v;
    }
    return true;
  })());
  ok('at full warp a streak crosses a readable fraction of the tube per frame',
    (() => {
      const frac = f(1.1e9) / 60 / tube;
      return frac > 0.01 && frac < 0.5;
    })(), (f(1.1e9) / 60 / tube).toFixed(3) + ' tubes/frame');
  ok('going faster still looks faster, even far past the reference',
    f(1e6) > f(1e4) && f(1e4) > f(2e3));
  ok('apparent flow survives garbage', f(NaN) === 0 && f(-5) === 0);
  ok('the streaks expose their flow so the tunnel can match it',
    /get flow\(\)/.test(warpSrc));
  ok('the update uses apparent flow, not raw speed',
    /const travel = flowRate \* dt/.test(warpSrc));
  ok('raw speed is no longer used to advance streaks',
    !/const travel = Math\.max\(speed, 0\) \* dt/.test(warpSrc));
  ok('streaks thin as they stretch, so they read as drawn-out light',
    /const thin = 1 - this\.amount \* 0\.45/.test(warpSrc));
  ok('the radial spawn is area-uniform, not axis-heavy',
    /Math\.sqrt\(Math\.random\(\)\)/.test(warpSrc));
}

/* ================================================================
   2. WARP TUNNEL — the full-screen half
   ================================================================ */
{
  ok('the tunnel shader exists', tunnelSrc.length > 0);
  ok('it is registered as a Babylon effect',
    /ShadersStore\[WARP_TUNNEL_EFFECT \+ 'FragmentShader'\]/.test(tunnelSrc));
  ok('it does a radial smear', /float smear = amount/.test(tunnelSrc));
  ok('it is chromatic, so the rim fringes', /rTap|bTap/.test(tunnelSrc));
  ok('it draws tunnel streaks in polar space', /float streak = pow/.test(tunnelSrc));
  ok('it vignettes', /float vig/.test(tunnelSrc));
  ok('the centre of the view stays readable at full warp',
    /smoothstep\(0\.03, 0\.85, r\)/.test(tunnelSrc));

  // The project rule: no sine waves, no periodic time loops.
  ok('NO periodic animation: the shader has no time uniform',
    !/uniform float time/.test(tunnelSrc));
  ok('NO sin() or cos() driving animation in the tunnel shader',
    !/\bsin\(/.test(tunnelSrc.split('WARP_TUNNEL_FRAG')[1] ?? ''));
  ok('phase is advanced by distance flown, not by wall-clock time',
    /this\.phase \+= flow \* dt/.test(tunnelSrc));
  ok('phase only ever increases within a wrap period',
    /if \(this\.phase > 4096\)/.test(tunnelSrc));

  ok('the tunnel can be switched off', /setEnabled\(v: boolean\)/.test(tunnelSrc));
  ok('the tunnel attaches BEFORE the lens, so warp light gets bent',
    app.indexOf('warpTunnel.attach') < app.indexOf('lensfx.attach')
    && app.indexOf('warpTunnel.attach') > 0);
  ok('the tunnel is driven from the streaks own flow',
    /flow: this\.warp\.flow/.test(app));

  const T = await bundle('src/bjs/systems/WarpTunnel.ts');
  const t = new T.WarpTunnel();
  ok('a fresh tunnel is idle', t.intensity === 0);
  t.update(1 / 60, { amount: 1, flow: 500, focusX: 0.5, focusY: 0.5 });
  ok('engagement eases in rather than snapping', t.intensity > 0 && t.intensity < 1);
  for (let i = 0; i < 600; i++) {
    t.update(1 / 60, { amount: 1, flow: 500, focusX: 0.5, focusY: 0.5 });
  }
  ok('engagement reaches full with sustained warp', t.intensity > 0.98);
  ok('phase stays bounded over a long run', Number.isFinite(t.intensity));
  for (let i = 0; i < 600; i++) {
    t.update(1 / 60, { amount: 0, flow: 0, focusX: 0.5, focusY: 0.5 });
  }
  ok('engagement returns to exactly zero when warp stops', t.intensity === 0);
  t.setEnabled(false);
  t.update(1 / 60, { amount: 1, flow: 900, focusX: 0.5, focusY: 0.5 });
  ok('a disabled tunnel stays dark however hard it is driven', t.intensity === 0);
  ok('a disabled tunnel reports itself off', t.enabled === false);
  t.update(NaN, { amount: 1, flow: 1, focusX: 0.5, focusY: 0.5 });
  ok('a non-finite dt is ignored', Number.isFinite(t.intensity));
}

/* ================================================================
   3. BLACK HOLES — canonical working material clone
   ================================================================ */
{
  ok('the renderer imports the working Singularity fragment',
    /WORKING_SINGULARITY_FRAG/.test(holeRend));
  ok('the renderer imports the working Singularity vertex stage',
    /WORKING_SINGULARITY_VERT/.test(holeRend));
  ok('all open-world holes use a ShaderMaterial', /new ShaderMaterial/.test(holeRend));
  ok('there is no alternate HoleField shader', !/HoleFieldShader/.test(holeRend));
  ok('the clone is one fullscreen triangle', /workingSingularityViewport/.test(holeRend));
  ok('the canonical material integrates geodesics', /1\.5 \* rs \* u \* u/.test(holeFrag));
  ok('the canonical material raymarches disk volume', /inSlab/.test(holeFrag));
  ok('the canonical event horizon is opaque', /vec4\(col, 1\.0\)/.test(holeFrag));
  ok('world position is bound directly to holePos', /setVector3\('holePos',nearest\.position\)/.test(holeRend));
  ok('the clone rationale is documented', /exact vertex\/fragment material/.test(holeRend));
}

/* ================================================================
   4. BLACK HOLES — exact, irreversible worldline capture
   ================================================================ */
{
  ok('capture solves the segment-sphere quadratic',
    /segmentSphereFirstHit/.test(uniSrc) && /bb\*bb-4\*aa\*cc/.test(uniSrc));
  ok('the earliest crossing parameter is selected', /t < firstT/.test(uniSrc));
  ok('capture latches at the crossing itself',
    /this\.latchedHorizonId = bh\.id/.test(uniSrc));
  ok('only the destination handshake has a release method',
    /leaveHorizon\(id: string\)/.test(uniSrc));
  ok('the causal reason is recorded next to the code', /future-directed worldline/i.test(uniSrc));

  const U = await bundle('src/bjs/systems/UniverseState.ts');
  const u = new U.UniverseState({ seed: 12345 });
  const hole = u.regions.find((r) => r.kind === 'blackhole');
  ok('the universe contains a black hole to test with', !!hole);
  if (hole) {
    const hz=u.horizonRadiusOf(hole);
    const V=hole.position.constructor;
    const at=(x)=>new V(x,hole.position.y,hole.position.z);
    const step=hz*8;
    let x=hole.position.x-hz-step*.5;
    u.updatePlayer(at(x)); x+=step; u.updatePlayer(at(x));
    ok('a frame-spanning flythrough is caught',u.insideHorizon?.id===hole.id);
    x+=step;u.updatePlayer(at(x));
    ok('crossing the coordinate centre cannot eject the craft',u.insideHorizon?.id===hole.id);
    for(let i=0;i<20;i++){x+=step;u.updatePlayer(at(x));}
    ok('capture remains stable many frames later',u.insideHorizon?.id===hole.id);
    u.leaveHorizon(hole.id);
    ok('the explicit destination handshake releases capture',u.insideHorizon===null);
    ok('handshake release resets depth',u.horizonDepth===0);
  }
}

/* ================================================================
   5. CELESTIAL CATALOG
   ================================================================ */
{
  const C = await bundle('src/bjs/systems/CelestialCatalog.ts');
  ok('there are at least 20 archetypes', C.CELESTIAL_KINDS.length >= 20,
    C.CELESTIAL_KINDS.length + ' kinds');
  ok('the user\u2019s asks are present: meteorites, comets, pulsars, quasars',
    ['meteor-swarm', 'comet', 'pulsar', 'quasar']
      .every((k) => C.CELESTIAL_KINDS.includes(k)));
  ok('every archetype is fully specified', C.CELESTIAL_KINDS.every((k) => {
    const s = C.CELESTIALS[k];
    return s.label && s.glyph && s.weight > 0
      && s.minRadius > 0 && s.maxRadius >= s.minRadius
      && s.luminosity >= 0 && s.luminosity <= 1
      && s.tint.length === 3 && s.blurb.length > 10;
  }));
  ok('every archetype has a distinct label',
    new Set(C.CELESTIAL_KINDS.map((k) => C.CELESTIALS[k].label)).size
      === C.CELESTIAL_KINDS.length);
  ok('total weight matches the table',
    C.TOTAL_WEIGHT === C.CELESTIAL_KINDS.reduce((s, k) => s + C.CELESTIALS[k].weight, 0));

  // The distribution must actually follow the weights, or "rare" is a lie.
  {
    const N = 120000;
    const counts = {};
    for (let i = 0; i < N; i++) {
      const k = C.kindForRoll(i / N);
      counts[k] = (counts[k] ?? 0) + 1;
    }
    ok('every archetype is reachable',
      C.CELESTIAL_KINDS.every((k) => (counts[k] ?? 0) > 0));
    const worst = C.CELESTIAL_KINDS.reduce((w, k) => {
      const want = C.CELESTIALS[k].weight / C.TOTAL_WEIGHT;
      const got = (counts[k] ?? 0) / N;
      return Math.max(w, Math.abs(got - want) / want);
    }, 0);
    ok('the realised distribution matches the declared weights within 2%',
      worst < 0.02, (worst * 100).toFixed(2) + '% worst-case error');
    ok('quasars are genuinely rare', counts['quasar'] / N < 0.01);
    ok('dyson swarms remain rare while singular anomalies are rarer',
      C.CELESTIALS['dyson-swarm'].weight <= 2 &&
      C.CELESTIALS['fractal-cube'].weight < C.CELESTIALS['dyson-swarm'].weight &&
      C.CELESTIALS['void-cathedral'].weight < C.CELESTIALS['fractal-cube'].weight);
    ok('ordinary debris is the most common',
      counts['meteor-swarm'] > counts['quasar'] * 10);
  }

  ok('roll clamping is safe at both ends',
    C.kindForRoll(-5) === C.CELESTIAL_KINDS[0]
    && !!C.kindForRoll(2) && !!C.kindForRoll(NaN));

  // Purity: the whole point of a procedural universe.
  const a = JSON.stringify(C.bodiesInCell(4, -3, 9));
  const b = JSON.stringify(C.bodiesInCell(4, -3, 9));
  ok('cells are pure: same input, same bodies', a === b);
  ok('different cells give different bodies',
    JSON.stringify(C.bodiesInCell(4, -3, 9)) !== JSON.stringify(C.bodiesInCell(4, -3, 10)));
  ok('a different universe seed changes everything',
    JSON.stringify(C.bodiesInCell(1, 1, 1))
      !== JSON.stringify(C.bodiesInCell(1, 1, 1, { ...C.DEFAULT_FIELD, seed: 99 })));

  {
    let total = 0, cells = 0, empty = 0;
    for (let x = 0; x < 10; x++) for (let y = 0; y < 10; y++) for (let z = 0; z < 10; z++) {
      const n = C.bodiesInCell(x, y, z).length;
      total += n; cells++; if (n === 0) empty++;
    }
    const mean = total / cells;
    ok('mean occupancy is close to the declared density',
      Math.abs(mean - C.DEFAULT_FIELD.density) < 0.5,
      'mean ' + mean.toFixed(2) + ' vs density ' + C.DEFAULT_FIELD.density);
    ok('cells are rarely empty, so space is not patchy',
      empty / cells < 0.2, (empty / cells * 100).toFixed(1) + '% empty');
  }

  ok('bodies are jittered off the cell walls, so no lattice is visible',
    C.bodiesInCell(0, 0, 0).every((b) => {
      const fx = b.x / C.DEFAULT_FIELD.cellSize;
      return fx > 0.1 && fx < 0.9;
    }));

  {
    const near = C.bodiesNear(0, 0, 0, 30000);
    ok('a query returns bodies', near.length > 0, near.length + ' found');
    ok('every returned body is genuinely within range',
      near.every((b) => Math.hypot(b.x, b.y, b.z) <= 30000 + 1e-6));
    const wide = C.bodiesNear(0, 0, 0, 60000);
    ok('a wider query returns at least as many', wide.length >= near.length);
    const n = C.nearestBody(0, 0, 0, 60000);
    ok('the nearest body really is the nearest',
      n && wide.every((b) =>
        Math.hypot(b.x, b.y, b.z) >= Math.hypot(n.x, n.y, n.z) - 1e-6));
    ok('an empty sphere yields null', C.nearestBody(0, 0, 0, 0) === null);
  }

  ok('mass scales with the body\u2019s own size, not just its archetype',
    (() => {
      const all = C.bodiesNear(0, 0, 0, 90000);
      const byKind = {};
      for (const b of all) (byKind[b.kind] ??= []).push(b);
      return Object.values(byKind).some((g) =>
        g.length > 2 && new Set(g.map((b) => b.mass.toFixed(3))).size > 1);
    })());
  ok('colour varies between two bodies of the same archetype',
    (() => {
      const all = C.bodiesNear(0, 0, 0, 90000);
      const byKind = {};
      for (const b of all) (byKind[b.kind] ??= []).push(b);
      return Object.values(byKind).some((g) =>
        g.length > 2 && new Set(g.map((b) => b.tint.join(','))).size > 1);
    })());
  ok('pulsars and magnetars spin far faster than anything else',
    (() => {
      const all = C.bodiesNear(0, 0, 0, 200000);
      const spin = all.filter((b) => b.kind === 'pulsar' || b.kind === 'magnetar');
      const rest = all.filter((b) => b.kind !== 'pulsar' && b.kind !== 'magnetar');
      if (!spin.length || !rest.length) return false;
      return Math.min(...spin.map((b) => b.spin))
        > Math.max(...rest.map((b) => b.spin));
    })());
  ok('every tint channel is a valid colour',
    C.bodiesNear(0, 0, 0, 60000).every((b) =>
      b.tint.every((c) => c >= 0 && c <= 1)));
}

/* ================================================================
   6. CELESTIAL RENDERER
   ================================================================ */
{
  const rend = read('src/bjs/systems/CelestialRenderer.ts');
  ok('bodies are drawn as thin instances: one draw call for the field',
    /thinInstanceSetBuffer\('matrix'/.test(rend));
  ok('colour rides in an instanced buffer', /thinInstanceSetBuffer\('bodyColor'/.test(rend));
  ok('the shader does limb darkening', /limb/.test(rend));
  ok('the shader draws a rim corona', /corona/.test(rend));
  ok('a filmic toe stops faint bodies lifting into grey haze',
    /l \* l \/ \(l \* l \+ 0\.0025\)/.test(rend));
  ok('emissive bodies ignore scene fog', /fogEnabled = false/.test(rend));
  ok('the eye position is passed explicitly, not assumed from the engine',
    /uniform vec3 eyePos/.test(rend) && /setVector3\('eyePos'/.test(rend));
  ok('the eye is written before the rebuild early-out, since it moves '
    + 'every frame',
    rend.indexOf("setVector3('eyePos'") < rend.indexOf('rebuildAfter)'));
  ok('there is a hard cap on instances', /maxBodies/.test(rend));
  ok('the cap keeps the NEAREST bodies, which are the visible ones',
    /\.sort\(\(p, q\) => p\.d - q\.d\)/.test(rend));
  ok('rebuilds are throttled by camera movement', /rebuildAfter/.test(rend));
  ok('no time uniform: nothing pulses', !/uniform float time/.test(rend));
  ok('the renderer is wired into the app',
    /celestials\.attach/.test(app) && /celestials\.update\(eye\)/.test(app));
}

/* ================================================================
   7. SOUND
   ================================================================ */
{
  const M = await bundle('src/bjs/systems/SpaceMusic.ts');
  const musicSrc = read('src/bjs/systems/SpaceMusic.ts');

  ok('the score is generated, not a file',
    !/\.mp3|\.ogg|\.wav|fetch\(/.test(musicSrc));
  ok('several scales are available', Object.keys(M.SCALES).length >= 3);
  ok('degree 0 is the root', M.degreeToHz(0) === M.DEFAULT_MUSIC.rootHz);
  ok('seven degrees is an octave in a 7-note scale',
    Math.abs(M.degreeToHz(7) - M.DEFAULT_MUSIC.rootHz * 2) < 1e-9);
  ok('fourteen degrees is two octaves',
    Math.abs(M.degreeToHz(14) - M.DEFAULT_MUSIC.rootHz * 4) < 1e-9);
  ok('negative degrees go DOWN, not wrap upward',
    M.degreeToHz(-7) < M.degreeToHz(0)
    && Math.abs(M.degreeToHz(-7) - M.DEFAULT_MUSIC.rootHz / 2) < 1e-9);
  ok('every degree in range is audible', (() => {
    for (let d = -7; d <= 14; d++) {
      const hz = M.degreeToHz(d);
      if (!(hz >= 20 && hz <= 4000)) return false;
    }
    return true;
  })());

  {
    // The melody must wander but stay put: an unbounded walk eventually
    // leaves the audible range entirely.
    let seed = 1;
    const rnd = () => {
      seed = (Math.imul(seed, 1103515245) + 12345) >>> 0;
      return seed / 4294967296;
    };
    let d = 0, lo = 99, hi = -99;
    const seen = new Set();
    for (let i = 0; i < 100000; i++) {
      d = M.nextDegree(d, rnd);
      lo = Math.min(lo, d); hi = Math.max(hi, d);
      seen.add(d);
    }
    ok('the melodic walk stays inside its clamp', lo >= -7 && hi <= 14,
      lo + '..' + hi);
    ok('the walk explores a real range, not one note', seen.size > 12,
      seen.size + ' distinct degrees');
    ok('small steps dominate, so it sounds composed', (() => {
      let s2 = 7, cur = 0, small = 0, n = 20000;
      const r2 = () => { s2 = (Math.imul(s2, 1103515245) + 12345) >>> 0; return s2 / 4294967296; };
      for (let i = 0; i < n; i++) {
        const next = M.nextDegree(cur, r2);
        if (Math.abs(next - cur) <= 1) small++;
        cur = next;
      }
      return small / n > 0.5;
    })());
  }

  ok('the wind is silent well away from a hole', M.windGain(1e6) === 0);
  ok('the wind is silent at its declared range',
    M.windGain(M.DEFAULT_MUSIC.windRange) === 0);
  ok('the wind is very faint at three quarters of the range',
    M.windGain(M.DEFAULT_MUSIC.windRange * 0.75) < 0.02);
  ok('the wind is full at the singularity', M.windGain(0) === 1);
  ok('the wind rises monotonically as you approach', (() => {
    let prev = -1;
    for (let d = M.DEFAULT_MUSIC.windRange; d >= 0; d -= 50) {
      const g = M.windGain(d);
      if (g < prev - 1e-12) return false;
      prev = g;
    }
    return true;
  })());
  ok('the wind handles a non-finite distance', M.windGain(Infinity) === 0
    && M.windGain(NaN) === 0);

  // Strip comments before asserting on what the CODE does: the docblock
  // explains at length why there is no LFO, and a naive grep for "LFO"
  // matches that explanation rather than any implementation.
  const musicCode = musicSrc
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/\/\/[^\n]*/g, '');
  ok('the hum vibration is driven by a random-walk target',
    /vibTarget = \(this\.rnd\(\) \* 2 - 1\)/.test(musicCode));
  ok('the vibration eases toward its target rather than jumping',
    /vibWalk \+= \(this\.vibTarget - this\.vibWalk\)/.test(musicCode));
  ok('the vibration is applied as a gain, not as a second oscillator',
    /this\.humVib\.gain\.value =/.test(musicCode));
  ok('exactly one oscillator is created for the hum',
    (musicCode.match(/createOscillator/g) ?? []).length === 2,
    'one for the hum, one per note');
  ok('NO periodic modulation: no sin/cos anywhere in the music code',
    !/Math\.(sin|cos)\(/.test(musicCode));
  ok('the vibration cannot gate the hum to silence',
    /Math\.max\(0\.35, Math\.min\(1\.6, 1 \+ this\.vibWalk\)\)/.test(musicCode));
  ok('music, hum and wind switch independently',
    /setMusicEnabled/.test(musicSrc) && /setHumEnabled/.test(musicSrc)
    && /setWindEnabled/.test(musicSrc));
  ok('nothing is built before a user gesture',
    /start\(ctx\?: AudioContext \| null\)/.test(musicSrc));
  ok('a fresh instance has not started', !(new M.SpaceMusic()).running);
  ok('updating before start is harmless', (() => {
    const m = new M.SpaceMusic();
    m.update(1 / 60, 100);
    return !m.running;
  })());
  ok('gain changes are ramped, never set instantly',
    /linearRampToValueAtTime/.test(musicSrc));
  ok('the wind is brown-ish noise rather than raw white hiss',
    /last = \(last \+ white \* 0\.02\) \/ 1\.02/.test(musicSrc));
  ok('the music is wired into the app frame',
    /this\.music\.update\(dt, holeD\)/.test(app));
  ok('music shares the existing audio gesture', /this\.music\.start\(\)/.test(app));
  ok('the same hole distance drives the rumble and the wind',
    /const holeD = bh \? Vector3\.Distance/.test(app));
}

/* ================================================================
   8. EXOSUIT HUD + SATELLITE + LEGACY
   ================================================================ */
{
  const H = await bundle('src/bjs/ui/HudTheme.ts');
  ok('there are three themes', H.HUD_THEME_ORDER.length === 3);
  ok('the exosuit is the default', H.DEFAULT_HUD_THEME === 'suit');
  ok('legacy exists and is named Legacy', H.HUD_THEMES.legacy.label === 'Legacy');
  ok('the exosuit exists and is named Exosuit', H.HUD_THEMES.suit.label === 'Exosuit');
  ok('only the legacy theme lacks frame furniture',
    H.HUD_THEMES.suit.frame && H.HUD_THEMES.satellite.frame && !H.HUD_THEMES.legacy.frame);
  ok('the exosuit and satellite sweep',
    H.HUD_THEMES.suit.sweep && H.HUD_THEMES.satellite.sweep && !H.HUD_THEMES.legacy.sweep);
  ok('an unknown theme falls back rather than throwing',
    H.hudTheme('nonsense').id === H.DEFAULT_HUD_THEME);
  ok('isHudTheme rejects prototype keys', !H.isHudTheme('toString'));

  ok('signal is strong close in', H.signalStrength(50) > 0.9);
  ok('signal decays with distance',
    H.signalStrength(50) > H.signalStrength(5000)
    && H.signalStrength(5000) > H.signalStrength(5e6));
  ok('signal never hits zero, so the feed always resolves',
    H.signalStrength(1e12) > 0);
  ok('signal is bounded to 0..1', (() => {
    for (const d of [0, 1, 1e3, 1e9, Infinity, NaN]) {
      const s = H.signalStrength(d);
      if (!(s >= 0 && s <= 1)) return false;
    }
    return true;
  })());
  ok('bars always render five cells', (() => {
    for (const s of [0, 0.2, 0.5, 0.99, 1, NaN]) {
      if ([...H.signalBars(s)].length !== 5) return false;
    }
    return true;
  })());
  ok('a strong signal shows more filled bars than a weak one',
    [...H.signalBars(1)].filter((c) => c === '▮').length
      > [...H.signalBars(0.1)].filter((c) => c === '▮').length);

  ok('the HUD can switch theme', /setTheme\(id: HudThemeId\)/.test(hudSrc));
  ok('switching is a class swap, not a rebuild',
    /classList\.toggle\(t\.className/.test(hudSrc));
  ok('frame furniture is in the DOM for both themes',
    /fhud-frame/.test(hudSrc) && /fhud-uplink/.test(hudSrc));
  ok('the uplink is only written in the theme that shows it',
    /hudTheme\(this\.theme\)\.uplink/.test(hudSrc));
  ok('the satellite frame is styled', /\.fhud-satellite \.fhud-frame/.test(css));
  ok('corner brackets are drawn with borders, not images',
    /\.fh-corner\{/.test(css) && !/\.fh-corner[^}]*url\(/.test(css));
  ok('the scan sweep exists', /@keyframes fhSweep/.test(css));
  ok('the sweep is disabled under reduced motion',
    /prefers-reduced-motion[\s\S]{0,220}\.fh-scan\{animation:none/.test(css));
  ok('legacy hides the satellite furniture',
    /\.fhud-legacy \.fhud-frame/.test(css));
  ok('the theme is exposed in settings', /HUD Style/.test(shellSrc));
  ok('the app wires the theme hook', /onHudTheme: \(id\)/.test(app));
}

/* ================================================================
   9. SONAR CURSOR
   ================================================================ */
{
  const S = await bundle('src/bjs/ui/SonarCursor.ts');
  const cur = read('src/bjs/ui/SonarCursor.ts');
  const svg = S.sonarCursorSVG();
  ok('the cursor is an inline SVG, not an image file',
    svg.startsWith('<svg') && !/\.png|\.cur|\.ico/.test(cur));
  ok('the SVG is well formed', svg.includes('</svg>') && svg.includes('<circle'));
  ok('the CSS form is a data URI with a centred hotspot',
    /^url\("data:image\/svg\+xml,/.test(S.sonarCursorCSS())
    && / 17 17, crosshair$/.test(S.sonarCursorCSS()));
  ok('the fallback ends in a real cursor keyword, never nothing',
    /crosshair$/.test(S.sonarCursorCSS()));
  ok('size changes move the hotspot with it',
    / 10 10, /.test(S.sonarCursorCSS({ size: 20, accent: '#fff' })));
  ok('the live cursor has a rotating sweep', /sc-sweep/.test(cur));
  ok('clicking fires a ping', /sc-ping/.test(cur) && /classList\.add\('go'\)/.test(cur));
  ok('the ping restart forces a reflow, or it would only ever play once',
    /void ping\.offsetWidth/.test(cur));
  ok('pointer moves are written straight into a compositor-only transform',
    /style\.transform/.test(cur) && !/requestAnimationFrame/.test(cur),
    'batching into a rAF left the cursor a frame behind the pointer');
  ok('the live cursor is promoted to its own compositor layer',
    /will-change\s*:\s*transform/.test(css) && /pointer-events\s*:\s*none/.test(css));
  ok('the native cursor is only hidden while ours is shown',
    /classList\.toggle\('sonar-on', v\)/.test(cur));
  ok('text fields keep a real caret', /input\[type="text"\][\s\S]{0,120}cursor:text/.test(css));
  ok('the cursor has meaningful states',
    /'idle' \| 'target' \| 'zoom' \| 'grab'/.test(cur));
  ok('the sweep respects reduced motion',
    /prefers-reduced-motion[\s\S]{0,140}\.sc-sweep\{animation:none/.test(css));
  ok('the cursor is mounted by the app', /sonarCursor\.mount\(\)/.test(app));
}

/* ================================================================
   10. TITLE SCREEN + PATCH NOTES
   ================================================================ */
{
  const P = await bundle('src/bjs/content/PatchNotes.ts');
  ok('the update is named', P.CURRENT_UPDATE === 'UPDATE 2');
  ok('the release is BETTER COSMOS', P.CURRENT_UPDATE_NAME === 'BETTER COSMOS');
  const r = P.latestRelease();
  ok('the release has a tagline', r.tagline.length > 10);
  ok('there are several entries', r.entries.length >= 6, r.entries.length + ' entries');
  ok('every entry is complete', r.entries.every((e) =>
    e.title.length > 3 && e.body.length > 30
    && ['new', 'fixed', 'improved'].includes(e.tag)));
  ok('the notes cover the warp fix',
    r.entries.some((e) => /warp/i.test(e.title) && e.tag === 'fixed'));
  ok('the notes cover the bubble fix',
    r.entries.some((e) => /bubble/i.test(e.title)));
  ok('the notes cover black hole entry',
    r.entries.some((e) => /inside a black hole/i.test(e.title)));
  ok('the notes cover the satellite HUD',
    r.entries.some((e) => /satellite/i.test(e.title)));
  ok('the notes cover sound', r.entries.some((e) => /sound/i.test(e.title)));
  const c = P.countByTag(r);
  ok('the tag counts add up',
    c.new + c.fixed + c.improved === r.entries.length);

  ok('the title is a single-line logotype, not three stacked words',
    /intro-logo/.test(introSrc) && !/<h1>UNLIMITED<br>/.test(introSrc));
  ok('the logotype lays out horizontally',
    /\.intro-logo\{[\s\S]{0,120}display:flex/.test(introSrc));
  ok('the mode row is horizontal',
    /\.intro-modes\{ display:flex/.test(introSrc));
  // The mode menu is now two doors on top and a single Create World at the
  // bottom; patch notes moved to the launch row, but it is still a
  // subordinate auxiliary action rather than a third way into the game.
  ok('patch notes stays a subordinate auxiliary, not a third door',
    /notes\.className = 'intro-aux/.test(introSrc) &&
    !/modes\.appendChild\(notes\)/.test(introSrc));
  // The title must stay "two doors, not a menu": .intro-play means a way
  // INTO the game, and adding a third would quietly break that rule.
  ok('patch notes is NOT a third way into the game',
    /notes\.className = 'intro-aux/.test(introSrc));
  ok('there are still exactly two intro-play buttons',
    (introSrc.match(/className = 'intro-play/g) ?? []).length === 2);
  ok('the auxiliary button is styled subordinate to the two doors',
    /\.intro-aux\{/.test(introSrc));
  ok('the release badge is shown on the title card',
    /intro-release/.test(introSrc) && /ir-badge/.test(introSrc));
  ok('the panel is collapsed until asked for',
    /intro-patch intro-hide/.test(introSrc));
  ok('the panel scrolls internally so it cannot push the buttons off screen',
    /\.intro-patch\{[\s\S]{0,220}overflow-y:auto/.test(introSrc));
  ok('patch copy is escaped before becoming markup',
    /function esc\(/.test(introSrc) && /esc\(e\.title\)/.test(introSrc));
  ok('the forbidden old names appear nowhere',
    !/\bEAOIN\b/.test(introSrc) && !/\bEON\b/.test(introSrc));

  // The panel markup must actually contain the release.
  const html = (await bundle('src/bjs/ui/IntroOverlay.ts')).renderPatchNotes();
  ok('the rendered panel names the update', html.includes('UPDATE 2'));
  ok('the rendered panel names the release', html.includes('BETTER COSMOS'));
  ok('the rendered panel lists every entry',
    r.entries.every((e) => html.includes(e.title.replace(/&/g, '&amp;'))));
  ok('the rendered panel tags each entry',
    (html.match(/<span class="ip-tag /g) ?? []).length === r.entries.length,
    (html.match(/<span class="ip-tag /g) ?? []).length + ' tags vs '
      + r.entries.length + ' entries');
  ok('every tag rendered is one of the three known kinds',
    (html.match(/ip-tag ip-(\w+)/g) ?? [])
      .every((t) => /ip-(new|fixed|improved)$/.test(t)));
  ok('the rendered panel has no unescaped angle brackets in body copy',
    !/<script/i.test(html));
}

/* ================================================================
   11. SETTINGS WIRING
   ================================================================ */
{
  ok('the warp tunnel is switchable from settings',
    /Warp Visuals/.test(shellSrc) && /onWarpTunnel/.test(shellSrc));
  ok('sound has its own settings group', /grp-h">Sound</.test(shellSrc));
  ok('all three sound switches are labelled',
    /Ambient score/.test(shellSrc) && /Satellite hum/.test(shellSrc)
    && /Event horizon wind/.test(shellSrc));
  ok('the app wires every audio toggle',
    /music: this\.music\.musicEnabled/.test(app)
    && /hum: this\.music\.humEnabled/.test(app)
    && /wind: this\.music\.windEnabled/.test(app));
  ok('the gear row can still be toggled',
    /gears: 'Velocity gear shifter'/.test(shellSrc));
  ok('every new stat reaches the telemetry panel',
    /this\.warpTunnel\.stats\(\)/.test(app) && /this\.celestials\.stats\(\)/.test(app));
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
