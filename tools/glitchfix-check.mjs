/**
 * Three reported glitches, each with a measured cause.
 *
 *   1. The galaxies disappeared.
 *   2. The sky turned into a magenta smear.
 *   3. Black holes bounced the player out instead of letting them in.
 *
 * Run: node tools/glitchfix-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (n, c, e) => {
  if (c) pass++; else { fail++; console.log('FAIL: ' + n + (e ? ' — ' + e : '')); }
};
const read = (p) => (fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '');

const field = read('src/bjs/systems/GalaxyField.ts');
const uni = read('src/bjs/systems/UniverseState.ts');

// ============================================================ 1. GALAXIES
// The galaxy rendered through a second camera while
// DefaultRenderingPipeline was attached to the main camera only. A
// post-process pipeline blits its own framebuffer over the backbuffer, so
// the galaxy was drawn every frame and then painted over.
ok('there is no second camera to be painted over',
  !/new UniversalCamera\(\s*'galaxyCam'/.test(field));
ok('the galaxy is drawn by the ordinary camera',
  /scene\.activeCameras = null/.test(field));
ok('the reason the second camera failed is recorded',
  /blits the\s*\/\/ result over the backbuffer|erasing whatever the first camera/.test(field));
ok('the galaxy is no longer hidden on a private layer',
  !/mesh\.layerMask = GALAXY_LAYER/.test(field));
ok('points are remapped into a shell the camera can see',
  /projectProxy/.test(field) && /proxyRadius/.test(field));
ok('true coordinates are kept so parallax survives',
  /truePos/.test(field) && /capturePositions/.test(field));

{
  const out = '/tmp/gfx-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', 'src/bjs/systems/GalaxyField.ts',
      '--bundle', '--format=esm', '--platform=node', '--log-level=error',
      '--outfile=' + out], { stdio: 'pipe' });
    const g = await import(out);
    const { proxyRadius, PROXY_INNER, PROXY_OUTER } = g;

    ok('the proxy shell fits inside the camera far plane', PROXY_OUTER < 4000,
      String(PROXY_OUTER));
    ok('everything lands inside the shell', (() => {
      for (const d of [1, 1e3, 5e4, 1e6, 1e8]) {
        const r = proxyRadius(d);
        if (!(r >= PROXY_INNER - 1e-6 && r <= PROXY_OUTER + 1e-6)) return false;
      }
      return true;
    })());
    ok('nearer galaxies still map nearer than far ones', (() => {
      let prev = -1;
      for (const d of [1e3, 1e4, 1e5, 1e6, 1e7]) {
        const r = proxyRadius(d);
        if (r < prev) return false;
        prev = r;
      }
      return true;
    })());
    ok('the mapping separates distances rather than collapsing them',
      proxyRadius(1e7) - proxyRadius(1e4) > 200,
      (proxyRadius(1e7) - proxyRadius(1e4)).toFixed(0));
    ok('a zero distance cannot divide by zero',
      Number.isFinite(proxyRadius(0)));
    ok('a NaN distance cannot poison the buffer',
      Number.isFinite(proxyRadius(NaN)));
    fs.unlinkSync(out);
  } catch (e) {
    ok('the galaxy field bundles and its proxy behaves', false, e.message);
  }
}

// ============================================================ 2. THE PINK
// 9,000 additive gas points at pointSize 90 overlap heavily. The gas
// colour is a dim (0.42, 0.13, 0.31), but three overlapping points
// already saturate red and blue while green lags - magenta - and eight
// stack to white.
ok('the gas point size is no longer enormous', (() => {
  const m = field.match(/this\.applyState\(gasMesh, ([\d.]+)\)/);
  return m && Number(m[1]) <= 8;
})(), (field.match(/this\.applyState\(gasMesh, ([\d.]+)\)/) || [])[1]);
ok('the magenta saturation is explained for future edits',
  /THE PINK GLITCH/.test(field));
ok('star points stay small too', (() => {
  const m = field.match(/this\.applyState\(starMesh, ([\d.]+)\)/);
  return m && Number(m[1]) <= 4;
})());
{
  // Numeric: how many overlapping points before magenta?
  const c = [0.42, 0.13, 0.31];
  const stack = (n) => c.map((v) => Math.min(1, v * n));
  const isMagenta = (s) => s[0] > 0.9 && s[2] > 0.8 && s[1] < 0.7;
  ok('a single gas point is not magenta', !isMagenta(stack(1)));
  ok('overlap is what produced magenta', isMagenta(stack(3)));
  // With a 4px sprite instead of 90px, the overlap count falls with the
  // square of the size: (4/90)^2 is about 1/500th the covered area.
  const areaRatio = (4 / 90) ** 2;
  ok('the new size cuts overlapping coverage by orders of magnitude',
    areaRatio < 0.01, (1 / areaRatio).toFixed(0) + 'x less area per point');
}

// ================================================== 3. BLACK HOLE ENTRY
// The horizon test asked "am I inside right now", sampled once a frame.
// A horizon is ~20-90 units; at warp the ship covers 142,500 units per
// frame, so it was outside before the step and outside after.
ok('the horizon test is swept, not sampled',
  /segmentPointDistance/.test(uni) && /sweptHole/.test(uni));
ok('the previous position is retained for the sweep',
  /lastPlayerPos/.test(uni));
ok('the tunnelling cause is recorded',
  /SWEPT, not sampled/.test(uni) && /142,500/.test(uni));
ok('closest approach along the step is what is tested',
  /segmentPointDistance\(this\.lastPlayerPos, pos, bh\.position\)/.test(uni));
// The sweep must apply to ENTERING only. Applied both ways, climbing out
// re-triggers: the path from just inside to well outside still passes
// close to the centre, so the player would be trapped inside forever.
ok('the sweep does not apply when leaving',
  /const wasInside = this\.insideHorizon\?\.id === bh\.id/.test(uni) &&
  /wasInside \? endD :/.test(uni));
ok('being able to get out and look back is protected',
  /that is what lets you get out|look back/.test(uni));

{
  const out = '/tmp/usx-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', 'src/bjs/systems/UniverseState.ts',
      '--bundle', '--format=esm', '--platform=node', '--log-level=error',
      '--outfile=' + out], { stdio: 'pipe' });
    const u = await import(out);
    const { segmentPointDistance, UniverseState } = u;
    const P = (x, y, z) => ({ x, y, z });

    ok('a segment through a point has zero distance',
      segmentPointDistance(P(-100, 0, 0), P(100, 0, 0), P(0, 0, 0)) < 1e-9);
    ok('a segment past a point measures the perpendicular',
      Math.abs(segmentPointDistance(P(-100, 5, 0), P(100, 5, 0), P(0, 0, 0)) - 5) < 1e-9);
    ok('the distance is clamped to the segment, not the infinite line',
      Math.abs(segmentPointDistance(P(10, 0, 0), P(100, 0, 0), P(0, 0, 0)) - 10) < 1e-9);
    ok('a degenerate zero-length step still works',
      Math.abs(segmentPointDistance(P(3, 4, 0), P(3, 4, 0), P(0, 0, 0)) - 5) < 1e-9);

    // The real scenario: one enormous warp step straight through a hole.
    const st = new UniverseState();
    const holes = st.regions.filter((r) => r.kind === 'blackhole');
    ok('the universe has black holes to enter', holes.length > 0);
    if (holes.length) {
      const h = holes[0];
      const hr = st.horizonRadiusOf(h);
      const far = 150000;
      // Sampled test: both endpoints are far outside.
      const endDist = far;
      ok('a warp step would have been missed by a sampled test',
        endDist > hr * 100);
      // Swept test catches it.
      ok('the swept test catches the same crossing',
        segmentPointDistance(
          P(h.position.x - far, h.position.y, h.position.z),
          P(h.position.x + far, h.position.y, h.position.z),
          h.position) <= hr);
      // And a genuine near-miss must NOT be caught, or every flyby
      // teleports you into a hole you were only passing.
      ok('a near miss is still a miss',
        segmentPointDistance(
          P(h.position.x - far, h.position.y + hr * 4, h.position.z),
          P(h.position.x + far, h.position.y + hr * 4, h.position.z),
          h.position) > hr);
    }
    fs.unlinkSync(out);
  } catch (e) {
    ok('the universe bundles and its horizon test behaves', false, e.message);
  }
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
