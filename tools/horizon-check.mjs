/**
 * horizon-check — seamless black hole entry + no high-altitude flicker.
 *
 * Two regressions are pinned here, as static-source guarantees:
 *
 *   1. The moment the ship is inside a black hole's horizon, every collision
 *      and braking block is bypassed. A black hole is a horizon, not a solid
 *      body - nothing may push, bounce or slow the ship back out of the fall.
 *      Black holes must never appear in the solid-body list, and the fall
 *      must carry the player to the multiverse transition, not fling them
 *      backward.
 *
 *   2. The volumetric galaxy, sky dome, star fields, space dust and comet
 *      layers all force-draw via alwaysSelectAsActiveMesh, and the scene
 *      clears every frame, so flying high above the galactic plane can never
 *      cull the backdrop into a flickering black blob.
 *
 * Run: node tools/horizon-check.mjs
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

const app = read('src/bjs/App.ts');

console.log('\n— black holes are never solid, so nothing can push the ship back —');
{
  // The solid-body list only ever accepts surface worlds (planet/ocean/
  // terrain). A horizon is not a surface; it must never enter the list.
  const solidsFn = (app.match(/private solidSpheres\(\)[\s\S]*?\n  \}/m) || [''])[0];
  ok('only surface worlds become solid bodies',
    /'planet'\s*\|\|\s*r\.kind\s*===\s*'ocean'\s*\|\|\s*r\.kind\s*===\s*'terrain'/.test(solidsFn));
  ok('a black hole is never turned into a solid sphere',
    solidsFn.length > 0 && !/'blackhole'/.test(solidsFn));

  // The collision resolver is bypassed the instant the ship is inside a
  // horizon, so no surface push-out or velocity cancellation can fire.
  ok('collision is bypassed inside a horizon',
    app.includes('this.universe.insideHorizon === null'));

  // The warp approach brake also stands down inside a horizon, or crossing
  // at speed would decelerate the ship and read as a rejection.
  ok('the warp brake never fires inside a horizon',
    app.includes('insideHole ? Infinity'));

  // And there is no velocity-inversion left anywhere in the horizon block:
  // the fall can only carry you inward.
  const holeBlock = (app.match(/const bh = this\.universe\.insideHorizon;[\s\S]*?spaghettification'\)/m) || [''])[0];
  ok('no velocity inversion in the horizon entry path',
    holeBlock.length > 0 && !/\.negate\(\)/.test(holeBlock) &&
    !/scaleInPlace\(-/.test(holeBlock) && !/velocity\.set\([^)]*-/.test(holeBlock));
}

console.log('\n— the scene always clears, so the viewport cannot stall —');
{
  ok('the scene clears explicitly at boot', /scene\.autoClear\s*=\s*true/.test(app));
  ok('the clear is re-asserted every frame',
    /if \(!this\.scene\.autoClear\) this\.scene\.autoClear = true/.test(app));
  ok('the galaxy layer also forces the clear',
    /autoClear\s*=\s*true/.test(read('src/bjs/systems/GalaxyField.ts')));
}

console.log('\n— every backdrop layer is permanently drawn —');
{
  const systems = [
    'CosmicSky', 'VerseRenderer', 'HoleFieldRenderer', 'GalaxyField',
    'StarFieldRenderer', 'LayeredSky', 'CelestialRenderer', 'PlanetField',
    'SpaceDust', 'CometSystem'
  ];
  for (const s of systems) {
    const src = read('src/bjs/systems/' + s + '.ts');
    ok(s + ' forces its meshes past frustum culling',
      src.includes('alwaysSelectAsActiveMesh = true'),
      'flying high above the galactic plane must never cull this layer');
  }
  // The raymarched hole quad specifically must never be culled.
  ok('the hole lens quad is always drawn',
    /quad\.alwaysSelectAsActiveMesh\s*=\s*true/.test(read('src/bjs/systems/HoleFieldRenderer.ts')));
}

console.log('\n— the entry still hands the player to the multiverse —');
{
  ok('crossing a horizon begins the native dimension fall',
    /descentInto\.begin\(/.test(app));
  ok('arrival triggers the dimension transition callback',
    /fall\.arrived/.test(app) && /enterRealm\(d\)/.test(app));
  ok('both modes may enter holes',
    read('src/bjs/systems/GameModes.ts').includes("enterHoles: true"));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
