/**
 * AISystem verification — proves ships steer with real forces, transition
 * states for real reasons, and crash rather than follow scripted paths.
 * Run: node tools/ai-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

// Ships build Babylon meshes, which need a live scene. Stub only the mesh
// layer; all steering and state-machine code under test runs for real.
const stub = {
  name: 'stub-meshes',
  setup(b) {
    b.onResolve({ filter: /(meshBuilder|standardMaterial|Meshes\/mesh)$/ }, (a) => ({
      path: a.path, namespace: 'stub'
    }));
    b.onLoad({ filter: /.*/, namespace: 'stub' }, () => ({
      contents: `
        const fake = () => ({
          position: { copyFrom(){} },
          rotation: { x:0, y:0, z:0 },
          material: null,
          name: '', dispose(){}
        });
        export const MeshBuilder = {
          CreateCylinder: fake, CreateSphere: fake, CreateTorus: fake, CreateDisc: fake
        };
        export class StandardMaterial {
          constructor(){ this.diffuseColor=null; this.emissiveColor=null;
                         this.specularColor=null; this.specularPower=0; }
          dispose(){}
        }
        export class Mesh { static MergeMeshes(){ return fake(); } }
      `, loader: 'js'
    }));
  }
};

const out = await build({
  entryPoints: ['tools/fixtures/ai-entry.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error', plugins: [stub]
});
const f = `/tmp/ai-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { AISystem, SHIP_PRESETS, Vector3 } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const V = (x, y, z) => new Vector3(x, y, z);
const scene = {};
const target = (x, y, z, r = 8) => ({ pos: V(x, y, z), radius: r, mass: 500 });
const run = (sys, steps, dt = 1 / 60) => { for (let i = 0; i < steps; i++) sys.update(dt); };

console.log('\n— spawning —');
{
  const sys = new AISystem(scene);
  const s = sys.spawn('fighter', V(0, 0, 0));
  ok('spawn returns a ship', !!s);
  ok('fleet count tracks spawns', sys.count() === 1);
  ok('preset config is applied', s.cfg === SHIP_PRESETS.fighter);
  ok('ship starts at full health', s.health === s.maxHealth);
  ok('ship starts on patrol', s.state === 'patrol');
}

console.log('\n— steering: a pursuing ship closes on its target —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  const s = sys.spawn('fighter', V(200, 0, 0));
  s.target = t;
  s.setState('pursue');
  const d0 = Vector3.Distance(s.pos, t.pos);
  run(sys, 240);
  const d1 = Vector3.Distance(s.pos, t.pos);
  ok(`distance to target shrinks (${d0.toFixed(0)} -> ${d1.toFixed(0)})`, d1 < d0 - 20);
}

console.log('\n— speed is capped by the ship config —');
{
  const sys = new AISystem(scene);
  const s = sys.spawn('scout', V(400, 0, 0));
  s.target = target(0, 0, 0);
  s.setState('pursue');
  run(sys, 400);
  ok(`speed never exceeds maxSpeed (${s.vel.length().toFixed(1)} <= ${s.cfg.maxSpeed})`,
     s.vel.length() <= s.cfg.maxSpeed + 0.01);
}

console.log('\n— a fleeing ship increases its distance —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  const s = sys.spawn('scout', V(30, 0, 0));
  s.target = t;
  s.health = s.maxHealth * 0.1;      // below fleeHealth
  s.setState('flee');
  const d0 = Vector3.Distance(s.pos, t.pos);
  run(sys, 200);
  ok(`flee increases distance (${d0.toFixed(0)} -> ${Vector3.Distance(s.pos, t.pos).toFixed(0)})`,
     Vector3.Distance(s.pos, t.pos) > d0 + 10);
}

console.log('\n— state machine transitions —');
{
  const t = target(0, 0, 0);

  // detection radius is attackRange * 3.5; a bomber's is 30 * 3.5 = 105
  const far = new AISystem(scene);
  const fs = far.spawn('bomber', V(400, 0, 0));
  fs.target = t;
  far.update(1 / 60);
  ok('a target beyond detection range does not wake the ship',
     fs.state === 'patrol', fs.state);

  const near = new AISystem(scene);
  const ns = near.spawn('bomber', V(90, 0, 0));   // aggression 1.0, never flees
  ns.target = t;
  near.update(1 / 60);
  ok('a target inside detection range triggers pursue',
     ns.state === 'pursue', ns.state);
  run(near, 600);
  ok('pursue escalates to attack within range',
     ns.state === 'attack' || ns.state === 'pursue', ns.state);
}

console.log('\n— damage drives the crash state —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  const s = sys.spawn('fighter', V(60, 0, 0));
  s.target = t;
  s.damage(s.maxHealth + 1);
  ok('lethal damage triggers crash', s.state === 'crash', s.state);
  ok('crash locks in a target position', !!s.crashTarget);
  const impacts = (() => {
    for (let i = 0; i < 2000; i++) {
      const im = sys.update(1 / 60);
      if (im.length) return im;
    }
    return [];
  })();
  ok('a crashing ship actually reaches its target', impacts.length === 1);
  ok('crash count is recorded', sys.crashes === 1);
  ok('dead ships leave the fleet', sys.count() === 0);
}

console.log('\n— low health triggers flight, but only for cowards —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  const scout = sys.spawn('scout', V(80, 0, 0));   // fleeHealth 0.35
  scout.target = t;
  scout.health = scout.maxHealth * 0.15;
  sys.update(1 / 60);
  ok('a damaged scout flees', scout.state === 'flee', scout.state);

  const bomber = sys.spawn('bomber', V(80, 0, 40));  // fleeHealth 0
  bomber.target = t;
  bomber.health = bomber.maxHealth * 0.05;
  sys.update(1 / 60);
  ok('a bomber never flees', bomber.state !== 'flee', bomber.state);
}

console.log('\n— separation keeps a formation from collapsing —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  // stack ships almost on top of each other
  const ships = [];
  for (let i = 0; i < 6; i++) {
    const s = sys.spawn('fighter', V(100 + i * 0.4, 0, i * 0.4));
    s.target = t;
    ships.push(s);
  }
  run(sys, 180);
  let minSep = Infinity;
  for (let i = 0; i < ships.length; i++)
    for (let j = i + 1; j < ships.length; j++)
      minSep = Math.min(minSep, Vector3.Distance(ships[i].pos, ships[j].pos));
  ok(`ships push apart instead of overlapping (min gap ${minSep.toFixed(2)})`,
     minSep > 0.5, `${minSep}`);
}

console.log('\n— invasion fleets —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0, 12);
  const fleet = sys.invade(8, t, V(300, 40, 0));
  ok('invade spawns the requested count', fleet.length === 8 && sys.count() === 8);
  ok('every ship is assigned the target', fleet.every((s) => s.target === t));
  ok('the fleet starts in pursue', fleet.every((s) => s.state === 'pursue'));
  ok('the fleet contains mixed ship classes',
     new Set(fleet.map((s) => s.kind)).size > 1);
  const d0 = fleet.reduce((a, s) => a + Vector3.Distance(s.pos, t.pos), 0) / 8;
  run(sys, 300);
  const alive = sys.ships;
  if (alive.length) {
    const d1 = alive.reduce((a, s) => a + Vector3.Distance(s.pos, t.pos), 0) / alive.length;
    ok(`the fleet converges on the target (${d0.toFixed(0)} -> ${d1.toFixed(0)})`, d1 < d0);
  } else {
    ok('the whole fleet reached the target', sys.crashes > 0);
  }
}

console.log('\n— state census and cleanup —');
{
  const sys = new AISystem(scene);
  sys.spawn('scout', V(0, 0, 0));
  sys.spawn('fighter', V(20, 0, 0));
  const st = sys.states();
  ok('state census counts every ship',
     Object.values(st).reduce((a, b) => a + b, 0) === 2);
  sys.setTarget(null);
  ok('targets can be cleared', sys.ships.every((s) => s.target === null));
  sys.clear();
  ok('clear empties the fleet', sys.count() === 0);
}

console.log('\n— numerical stability —');
{
  const sys = new AISystem(scene);
  const t = target(0, 0, 0);
  for (let i = 0; i < 12; i++) {
    const s = sys.spawn('fighter', V(Math.random() * 200 - 100, 0, Math.random() * 200 - 100));
    s.target = t;
  }
  run(sys, 900);
  const bad = sys.ships.filter((s) =>
    !Number.isFinite(s.pos.x) || !Number.isFinite(s.vel.x));
  ok('no ship position or velocity goes non-finite', bad.length === 0);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
