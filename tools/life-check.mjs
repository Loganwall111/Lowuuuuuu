/**
 * life-check — surface fauna.
 *
 * A planet you can stand on should feel inhabited. These assert the creature
 * system actually moves things around, respects the terrain, and cleans up.
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

const { LifeSystem, speciesFor } = await load('src/bjs/systems/LifeSystem.ts', 'life');
const { Vector3 } = await load('node_modules/@babylonjs/core/Maths/math.vector.js', 'vec');

console.log('— species generation —');
{
  const a = speciesFor(1234, 4);
  const b = speciesFor(1234, 4);
  const c = speciesFor(9999, 4);
  ok('asks for four species and gets four', a.length === 4);
  ok('the same seed gives the same life every time',
     JSON.stringify(a.map((s) => s.name + s.plan)) ===
     JSON.stringify(b.map((s) => s.name + s.plan)));
  ok('a different seed gives different life',
     JSON.stringify(a.map((s) => s.name)) !== JSON.stringify(c.map((s) => s.name)));

  for (const s of a) {
    ok(`"${s.name}" has a sane body`,
       s.size > 0 && s.speed > 0 && s.herd >= 1 && s.shy > 0);
    ok(`"${s.name}" is never invisible black`,
       s.colour.r + s.colour.g + s.colour.b > 0.2);
  }
}

console.log('\n— planets have genuinely strange natives —');
{
  const { PLANS_BY_CLIMATE } = await load('src/bjs/systems/LifeSystem.ts', 'life2');
  ok('climates have their own life', Object.keys(PLANS_BY_CLIMATE).length >= 5);
  ok('ocean worlds get jellyfish', PLANS_BY_CLIMATE.ocean.includes('jelly'));
  ok('volcanic worlds get centipedes', PLANS_BY_CLIMATE.volcanic.includes('centipede'));

  // Climate must actually constrain what spawns.
  const oceanKinds = new Set();
  for (let i = 1; i < 400; i++) {
    speciesFor(i, 4, 'ocean').forEach((sp) => oceanKinds.add(sp.plan));
  }
  ok('ocean life is drawn from the ocean pool',
     [...oceanKinds].every((k) => PLANS_BY_CLIMATE.ocean.includes(k)),
     [...oceanKinds].join(','));

  // The headline creatures must be reachable and appropriately huge.
  let big = 0, longOnes = 0, swarms = 0;
  for (let i = 1; i < 600; i++) {
    for (const sp of speciesFor(i, 5)) {
      if (sp.plan === 'colossus' && sp.size > 8) big++;
      if (sp.plan === 'centipede' && sp.size > 3) longOnes++;
      if (sp.plan === 'swarm' && sp.herd > 30) swarms++;
    }
  }
  ok(`colossi are genuinely enormous (${big} found)`, big > 0);
  ok(`centipedes are large (${longOnes} found)`, longOnes > 0);
  ok(`swarms come in big flocks (${swarms} found)`, swarms > 0);
}

console.log('\n— creatures live on the terrain —');
{
  // A simple hilly ground so we can check clamping.
  const height = (x, z) => Math.sin(x * 0.1) * 3 + Math.cos(z * 0.1) * 2;
  const probe = (x, z) => {
    if (Math.abs(x) > 50 || Math.abs(z) > 50) return null;   // edge of the world
    return { height: height(x, z), normal: new Vector3(0, 1, 0) };
  };

  const scene = { onDisposeObservable: { add() {} } };
  // The system only touches meshes through Babylon; in this harness we care
  // about the simulation, so stub the mesh side.
  const life = new LifeSystem(scene, probe, 40);
  life.buildBody = () => ({
    setEnabled() {}, dispose() {}, thinInstanceSetBuffer() {},
    thinInstanceCount: 0, material: null
  });
  // Replace mesh construction wholesale for the headless run.
  const origPopulate = life.populate.bind(life);
  life.syncInstances = () => {};

  try {
    origPopulate(4242, 3);
    ok('populating creates a population', life.population > 0);
    ok('and reports its species', life.speciesCount === 3);

    const before = life.critters.map((c) => ({ x: c.pos.x, z: c.pos.z }));
    for (let i = 0; i < 120; i++) life.update(1 / 60, null);
    const moved = life.critters.filter((c, i) =>
      Math.abs(c.pos.x - before[i].x) > 1e-3 || Math.abs(c.pos.z - before[i].z) > 1e-3);
    ok(`creatures actually wander (${moved.length}/${life.population} moved)`,
       moved.length > life.population * 0.5);

    const offGrid = life.critters.filter((c) => !probe(c.pos.x, c.pos.z));
    ok('none of them walk off the edge of the world', offGrid.length === 0,
       offGrid.length + ' escaped');

    // Some plans are meant to be airborne: jellies climb like bubbles,
    // swarms hover, floaters drift. Only ground-dwellers must stay clamped.
    const AIRBORNE = new Set(['floater', 'jelly', 'swarm']);
    const floating = life.critters.filter((c) => {
      const sp = life.species[c.species];
      if (AIRBORNE.has(sp.plan)) return false;
      return Math.abs(c.pos.y - height(c.pos.x, c.pos.z)) > sp.size * 2.5;
    });
    ok('walkers stay clamped to the ground', floating.length === 0,
       floating.length + ' detached');

    // ...and the airborne ones must actually leave the ground.
    const risers = life.critters.filter((c) => {
      const sp = life.species[c.species];
      return AIRBORNE.has(sp.plan) && c.pos.y > height(c.pos.x, c.pos.z) + sp.size;
    });
    const airborneCount = life.critters.filter((c) =>
      AIRBORNE.has(life.species[c.species].plan)).length;
    ok(`airborne species actually fly (${risers.length}/${airborneCount})`,
       airborneCount === 0 || risers.length > 0);

    ok('every position stays finite',
       life.critters.every((c) =>
         Number.isFinite(c.pos.x) && Number.isFinite(c.pos.y) && Number.isFinite(c.pos.z)));

    // fleeing
    const target = life.critters[0];
    const player = new Vector3(target.pos.x + 0.5, target.pos.y, target.pos.z);
    const d0 = Math.hypot(target.pos.x - player.x, target.pos.z - player.z);
    for (let i = 0; i < 40; i++) life.update(1 / 60, player);
    const d1 = Math.hypot(target.pos.x - player.x, target.pos.z - player.z);
    ok(`a creature flees when crowded (${d0.toFixed(2)} -> ${d1.toFixed(2)})`, d1 > d0);

    const st = life.stats();
    ok('reports population in telemetry', st['Life: population'] === String(life.population));
    ok('names the species it spawned', st['Life: nearby'] !== '—');

    life.clear();
    ok('clearing removes every creature', life.population === 0 && life.speciesCount === 0);
    ok('and can be repopulated afterwards',
       (origPopulate(77, 2), life.population > 0 && life.speciesCount === 2));
  } catch (e) {
    ok('life system runs headless', false, e.message);
  }
}

console.log('\n— wired into a world you can walk on —');
{
  const tw = fs.readFileSync('src/bjs/worlds/TerraformWorld.ts', 'utf8');
  ok('terraform hosts life', tw.includes('new LifeSystem'));
  ok('life is ticked each frame', /this\.life\?\.update\(/.test(tw));
  ok('life is disposed with the world', /this\.life\?\.dispose\(\)/.test(tw));
  ok('life can be turned off', tw.includes("key === 'life'"));
  ok('species count is adjustable', tw.includes('lifeSpecies'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
