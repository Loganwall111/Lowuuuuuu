/**
 * phase78-check — continuous distance-adaptive shaders + pre-warm + visor.
 *
 * Pins the rendering re-core and the cockpit integration as static-source
 * guarantees:
 *
 *   1. the planet shader displaces vertices into real terrain relief from
 *      the same noise field it shades from (no more flat hollow ghost meshes),
 *   2. relief is distance-adaptive: detail deepens as the camera closes,
 *   3. the pre-warm cache covers every expensive material class,
 *   4. the world's parameters are mirrored into a cockpit visor console,
 *   5. pointer lock, gravitational lookback and force-draw invariants that
 *      earlier phases shipped are still intact.
 *
 * Run: node tools/phase78-check.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const read = (rel) => {
  const p = path.join(root, rel);
  return fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : '';
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const planet = read('src/bjs/shaders/PlanetShader.ts');
const world = read('src/bjs/worlds/PlanetaryWorld.ts');
const warmup = read('src/bjs/systems/ShaderWarmup.ts');
const shell = read('src/bjs/ui/Shell.ts');
const css = read('src/bjs/ui/styles.ts');
const app = read('src/bjs/App.ts');

console.log('\n— planets now have real terrain, not hollow meshes —');
{
  ok('the vertex shader declares a displacement uniform',
    /uniform float displace;/.test(planet));
  ok('the vertex shader declares a displacement scale',
    /uniform float displaceScale;/.test(planet));
  ok('the vertex shader lifts vertices along the surface normal',
    /position \+ normal \*/.test(planet));
  ok('the displacement is gated so gas giants and stars stay smooth',
    /if \(displace > 0\.5\)/.test(planet));
  ok('the same noise field shades and displaces',
    /fbm\(position \* \(6\.0 \+ detail/.test(planet));
  ok('planets bind the displacement uniforms',
    /'displace', 'displaceScale'/.test(world));
  ok('moons get real relief too',
    /mm\.setFloat\('displace', 1\)/.test(world));
  ok('the star stays a smooth disc',
    /this\.starMat\.setFloat\('displace', 0\)/.test(world));
}

console.log('\n— detail is distance-adaptive —');
{
  ok('the camera closes and the terrain deepens',
    /1 - \(d - b\.visualR\) \/ Math\.max\(b\.visualR \* 2\.4/.test(world));
}

console.log('\n— the pre-warm cache covers every expensive program —');
{
  ok('the warmup prefix table is a single named constant',
    /WARMUP_PREFIXES/.test(warmup));
  for (const p of ['holeField', 'galaxyFogM', 'galaxyPtM', 'celestialBody', 'am_', 'bh']) {
    ok(`the cache warms "${p}" materials`, warmup.includes("'" + p + "'"));
  }
}

console.log('\n— the visor console melts the parameters into the cockpit —');
{
  ok('the shell builds a visor console', /buildVisorConsole\(\)/.test(shell));
  ok('the world parameters are mirrored into it', /visorSlider\(p\)/.test(shell));
  ok('Reset World and Pause are physical visor nodes', /data-vc="reset"/.test(shell));
  ok('the console is styled as frosted pylon glass',
    /\.visor-console/.test(css) && /backdrop-filter:blur\(15px\)/.test(css));
  ok('it is hidden in photomode', /data-photo="1"\] \.visor-console/.test(css));
  ok('it is hidden in cinematic mode', /data-cinematic="1"\] \.visor-console/.test(css));
}

console.log('\n— the shipped invariants still hold —');
{
  const mouse = read('src/bjs/systems/MouseLook.ts');
  ok('mouse look stays raw-delta only',
    mouse.includes('movementX') && !mouse.includes('clientX'));
  ok('the Left-Alt gesture stays wired', app.includes("'alt'"));
  for (const s of ['CosmicSky', 'GalaxyField', 'StarFieldRenderer', 'SpaceDust']) {
    ok(s + ' still force-draws past culling',
      read('src/bjs/systems/' + s + '.ts').includes('alwaysSelectAsActiveMesh = true'));
  }
  ok('the scene still clears every frame',
    /autoClear = true/.test(app));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
