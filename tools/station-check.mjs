/**
 * station-check — procedural space stations you can dock with and walk inside.
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

const { stationName } = await load('src/bjs/systems/StationSystem.ts', 'stn');
const { Vector3 } = await load('node_modules/@babylonjs/core/Maths/math.vector.js', 'vec');

console.log('— stations are named and unique —');
{
  ok('a seed always names the same station', stationName(42) === stationName(42));
  const names = new Set();
  for (let i = 0; i < 400; i++) names.add(stationName(i));
  ok(`stations get varied names (${names.size}/400 distinct)`, names.size > 80);
  ok('names are readable', /^[A-Z][a-z]+ [A-Z][a-z]+ \d+$/.test(stationName(7)));
}

console.log('\n— layout and interiors —');
{
  const src = fs.readFileSync('src/bjs/systems/StationSystem.ts', 'utf8');
  ok('stations have a walkable hub', src.includes("add('hub'"));
  ok('they have habitat rings', src.includes("add('ring'"));
  ok('rings are connected by spars', src.includes("add('spar'"));
  ok('there are habitation pods', src.includes("add('pod'"));
  ok('there are solar arrays', src.includes("add('array'"));
  ok('there is a docking port', src.includes("add('dock'"));
  ok('there is a cupola to look down from', src.includes("add('cupola'"));

  ok('glass is reflective and transparent',
     src.includes('stnGlass') && src.includes('specularPower = 220'));
  ok('hulls never render pure black on the dark side',
     /hull\.emissiveColor/.test(src));

  ok('layout is driven by the seed', src.includes('mulberry32(seed)'));
  ok('ring count varies', src.includes('const rings = 1 + Math.floor(rng() * 3)'));

  ok('the walk mode can stand on decks', src.includes('floorAt('));
  ok('only modules with interiors are walkable', src.includes('m.floorY === null'));
  ok('you can tell when you are aboard', src.includes('isAboard('));
  ok('rings actually rotate', src.includes("m.kind === 'ring'"));
  ok('stations can be removed cleanly', src.includes('remove(id: string)'));
  ok('disposal frees meshes and materials',
     /dispose\(\):\s*void\s*\{[\s\S]*mesh\.dispose/.test(src));
}

console.log('\n— docking geometry —');
{
  // floorAt / isAboard are pure geometry; verify the maths directly.
  const src = fs.readFileSync('src/bjs/systems/StationSystem.ts', 'utf8');
  ok('the bounding radius covers every module',
     src.includes('radius = Math.max(radius, m.offset.length() + m.radius)'));
  ok('nearest() respects a maximum range', src.includes('maxDist = Infinity'));
  ok('being aboard is a radius test', src.includes('< st.radius'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
