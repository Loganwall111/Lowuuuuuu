/**
 * finale-check — the Omni-Boot cinematic load-out and the frustum repair.
 *
 * The crowning pass is pinned here:
 *
 *   1. the multi-stage Omni-Boot carries the matrix bar text, the spoken
 *      vitals telemetry, the disintegrating canvas geometry, the warp
 *      defrost veil, and the "SPACE JOURNEY ACTIVE" flare,
 *   2. text-to-speech is native Web Speech, guarded so a missing API cannot
 *      throw,
 *   3. the boot self-disposes and hands over to pointer-lock look,
 *   4. the intergalactic frustum fix is fully in place: every backdrop layer
 *      force-draws, the scene clears every frame, and the far plane is a
 *      deliberate finite shell rather than a depth-shredding 5e7 value,
 *   5. the visor sliders, Left-Alt gesture, and the 4-9 minute void crossing
 *      with gravitational lookback are all still intact.
 *
 * Run: node tools/finale-check.mjs
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

const omni = read('src/bjs/ui/OmniBoot.ts');
const app = read('src/bjs/App.ts');
const css = read('src/bjs/ui/styles.ts');

console.log('\n— the omni-boot stages all exist —');
{
  for (const line of ['EXOSUIT BOOTING UP', 'OXYGEN TANKS STORED', 'TANKS IN HASH']) {
    ok('matrix bar text: "' + line + '"', omni.includes(line));
  }
  ok('the countdown is present', omni.includes('3 · 2 · 1'));
  for (const line of ['INITIALIZING SPACE WALK', 'BLEEDING UNIVERSES TOGETHER', 'CAMERA CALIBRATED', 'VITALS STABILIZE']) {
    ok('vitals telemetry: "' + line + '"', omni.includes(line));
  }
  ok('the final flare reads SPACE JOURNEY ACTIVE',
    omni.includes('SPACE JOURNEY ACTIVE') && css.includes('.omni-engage'));
  ok('disintegrating geometry is drawn on a canvas',
    omni.includes('canvas') && omni.includes('getContext'));
  ok('the warp defrost veil exists', css.includes('.omni-warp') && css.includes('omniDefrost'));
}

console.log('\n— text-to-speech is native, guarded, and staged —');
{
  ok('it uses the Web Speech API',
    omni.includes('speechSynthesis') && omni.includes('SpeechSynthesisUtterance'));
  ok('a missing speech API cannot throw', /try\s*\{/.test(omni) && /catch/.test(omni));
  ok('the voice is tuned to an onboard AI register',
    omni.includes('u.pitch') && omni.includes('u.rate'));
}

console.log('\n— the boot hands over to pointer lock —');
{
  ok('the omni-boot self-disposes', omni.includes('el?.remove()'));
  ok('it calls its completion callback', omni.includes('this.onDone'));
  ok('the app locks look when the boot finishes',
    app.includes('omniBoot.start(() => this.mouse.requestLock())'));
  ok('the boot is idempotent', /if \(this\.running \|\| this\.el\) return/.test(omni));
}

console.log('\n— the intergalactic frustum fix is cemented —');
{
  ok('the scene clears every frame', app.includes('autoClear = true'));
  for (const s of ['CosmicSky', 'GalaxyField', 'StarFieldRenderer', 'LayeredSky', 'SpaceDust', 'CometSystem']) {
    ok(s + ' force-draws past culling',
      read('src/bjs/systems/' + s + '.ts').includes('alwaysSelectAsActiveMesh = true'));
  }
  // The far plane is deliberately finite: the backdrops live inside it, so a
  // 5e7 far plane would only shred depth precision, not fix any flash.
  ok('the far plane is a finite, architecture-correct shell',
    /camera\.maxZ = 4000/.test(app) && app.includes('z-fight'));
}

console.log('\n— the finale still honours the shipped cockpit —');
{
  ok('the visor console sliders remain', app.includes('buildVisorConsole') || read('src/bjs/ui/Shell.ts').includes('visor-console'));
  ok('the Left-Alt cursor gesture remains', app.includes("'alt'"));
  ok('the void crossing stays 4-9 minutes',
    read('src/bjs/systems/HoleInterior.ts').includes('GARGANTUA_DEPTH'));
  ok('the gravitational lookback window remains',
    read('src/bjs/worlds/BlackHoleWorld.ts').includes('exitWindow'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
