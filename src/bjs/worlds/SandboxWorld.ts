/**
 * SandboxWorld — the CREATE → MODIFY → EXPERIMENT → OBSERVE → DESTROY → RESET
 * loop in its most direct form.
 *
 * Real Newtonian n-body gravity integrated with velocity Verlet (symplectic,
 * so orbits stay stable instead of spiralling out the way naive Euler does).
 * Bodies genuinely collide and merge, conserving mass and momentum. Click to
 * place new bodies, drag to set their velocity, and watch the system evolve.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { LinesMesh } from '@babylonjs/core/Meshes/linesMesh';
import { Plane } from '@babylonjs/core/Maths/math.plane';
import { Matrix } from '@babylonjs/core/Maths/math.vector';
import { PointerEventTypes } from '@babylonjs/core/Events/pointerEvents';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Observer } from '@babylonjs/core/Misc/observable';
import type { PointerInfo } from '@babylonjs/core/Events/pointerEvents';
import { starfieldTexture } from '../Textures';
import { PLANET_SHADER, registerPlanetShader, PlanetKind } from '../shaders/PlanetShader';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

const G = 42.0;                 // tuned so orbits read well at this scale
const MERGE_SPEED_LIMIT = 999;  // always merge on contact

interface Body {
  mesh: Mesh;
  mat: ShaderMaterial;
  pos: Vector3;
  vel: Vector3;
  acc: Vector3;
  mass: number;
  radius: number;
  kind: PlanetKind;
  seed: number;
  isStar: boolean;
  trail: Vector3[];
  trailMesh: LinesMesh | null;
  alive: boolean;
}

export class SandboxWorld implements World {
  id = 'sandbox';
  name = 'Gravity Sandbox';

  private bodies: Body[] = [];
  private ctx!: WorldContext;
  private t = 0;
  private starMesh: Mesh | null = null;
  private light!: PointLight;
  private sky!: Mesh;
  private pointerObs: Observer<PointerInfo> | null = null;
  private ghost: Mesh | null = null;
  private dragStart: Vector3 | null = null;
  private aimLine: LinesMesh | null = null;
  private nextSeed = 1;
  private collisions = 0;
  private paused = false;

  private p = {
    gravity: 1.0,
    timeScale: 1.0,
    spawnMass: 1.0,
    trails: 1,
    trailLength: 90,
    elasticity: 0.0,
    softening: 0.35
  };

  async build(ctx: WorldContext): Promise<void> {
    this.ctx = ctx;
    const scene = ctx.scene;
    scene.clearColor = new Color4(0.002, 0.004, 0.012, 1);

    registerPlanetShader();

    this.sky = MeshBuilder.CreateSphere('sky', { diameter: 2400, segments: 24, sideOrientation: 1 }, scene);
    const sm = new StandardMaterial('skyMat', scene);
    sm.emissiveTexture = starfieldTexture(scene);
    sm.diffuseColor = Color3.Black();
    sm.specularColor = Color3.Black();
    sm.disableLighting = true;
    sm.backFaceCulling = false;
    this.sky.material = sm;
    this.sky.infiniteDistance = true;
    this.sky.isPickable = false;

    const hemi = new HemisphericLight('hemi', new Vector3(0, 1, 0), scene);
    hemi.intensity = 0.12;

    this.light = new PointLight('starLight', Vector3.Zero(), scene);
    this.light.intensity = 1.6;
    this.light.range = 2000;

    this.seedSystem();
    this.installPointer();

    ctx.setCameraTarget(Vector3.Zero(), 90);
  }

  /* ------------------------------ creation ------------------------------ */

  private makeBody(opts: {
    pos: Vector3; vel: Vector3; mass: number; kind: PlanetKind;
    isStar?: boolean; tintA?: Color3; tintB?: Color3;
  }): Body {
    const scene = this.ctx.scene;
    // radius from mass, assuming constant density
    const radius = Math.cbrt(opts.mass) * 1.5;
    const seed = this.nextSeed++ * 3.77;

    const mesh = MeshBuilder.CreateSphere('body' + seed, { diameter: radius * 2, segments: 48 }, scene);
    mesh.position.copyFrom(opts.pos);

    const mat = new ShaderMaterial('bm' + seed, scene, PLANET_SHADER, {
      attributes: ['position', 'normal', 'uv'],
      uniforms: ['world', 'worldViewProjection', 'camPos', 'sunPos', 'time',
                 'seed', 'ptype', 'tintA', 'tintB', 'detail', 'cloudAmt',
                 'cityLights', 'radius', 'isStar']
    });
    mat.setFloat('seed', seed);
    mat.setFloat('ptype', opts.kind);
    mat.setColor3('tintA', opts.tintA ?? new Color3(0.36, 0.30, 0.26));
    mat.setColor3('tintB', opts.tintB ?? new Color3(0.68, 0.60, 0.52));
    mat.setFloat('radius', radius);
    mat.setFloat('isStar', opts.isStar ? 1 : 0);
    mat.setFloat('detail', 1.0);
    mat.setFloat('cloudAmt', opts.kind === PlanetKind.Terran ? 0.7 : 0);
    mat.setFloat('cityLights', opts.kind === PlanetKind.Terran ? 1 : 0);
    mesh.material = mat;

    const b: Body = {
      mesh, mat,
      pos: opts.pos.clone(), vel: opts.vel.clone(), acc: Vector3.Zero(),
      mass: opts.mass, radius, kind: opts.kind, seed,
      isStar: !!opts.isStar, trail: [], trailMesh: null, alive: true
    };
    this.bodies.push(b);
    return b;
  }

  private seedSystem(): void {
    // central star
    const star = this.makeBody({
      pos: Vector3.Zero(), vel: Vector3.Zero(), mass: 900,
      kind: PlanetKind.Star, isStar: true,
      tintA: new Color3(1.0, 0.55, 0.12), tintB: new Color3(1.0, 0.98, 0.86)
    });
    this.starMesh = star.mesh;

    // planets on near-circular orbits: v = sqrt(GM/r)
    const kinds = [PlanetKind.Rocky, PlanetKind.Terran, PlanetKind.Desert, PlanetKind.Gas, PlanetKind.Ice];
    const tints: [Color3, Color3][] = [
      [new Color3(0.42, 0.26, 0.18), new Color3(0.66, 0.44, 0.30)],
      [new Color3(0.20, 0.40, 0.15), new Color3(0.50, 0.45, 0.30)],
      [new Color3(0.72, 0.52, 0.28), new Color3(0.92, 0.78, 0.50)],
      [new Color3(0.72, 0.58, 0.40), new Color3(0.90, 0.80, 0.62)],
      [new Color3(0.66, 0.78, 0.88), new Color3(0.88, 0.94, 0.99)]
    ];
    for (let i = 0; i < 5; i++) {
      const r = 26 + i * 15;
      const a = Math.random() * Math.PI * 2;
      const pos = new Vector3(Math.cos(a) * r, (Math.random() - 0.5) * 1.5, Math.sin(a) * r);
      const speed = Math.sqrt((G * 900) / r);
      const vel = new Vector3(-Math.sin(a), 0, Math.cos(a)).scale(speed);
      this.makeBody({
        pos, vel, mass: 1.2 + i * 0.9, kind: kinds[i],
        tintA: tints[i][0], tintB: tints[i][1]
      });
    }
  }

  /* ------------------------------ interaction ------------------------------ */

  private groundPoint(px: number, py: number): Vector3 | null {
    const scene = this.ctx.scene;
    // intersect the pointer ray with the y=0 plane (the orbital plane)
    const ray = scene.createPickingRay(px, py, Matrix.Identity(), this.ctx.camera);
    const plane = Plane.FromPositionAndNormal(Vector3.Zero(), new Vector3(0, 1, 0));
    const d = ray.intersectsPlane(plane);
    if (d === null || d === undefined) return null;
    return ray.origin.add(ray.direction.scale(d));
  }

  private installPointer(): void {
    const scene = this.ctx.scene;
    this.pointerObs = scene.onPointerObservable.add((pi) => {
      const ev = pi.event as PointerEvent;
      // Only the left button creates; the others stay free for camera control.
      if (ev.button !== 0 && pi.type === PointerEventTypes.POINTERDOWN) return;
      if (!ev.shiftKey) return;   // Shift is the "create" modifier

      if (pi.type === PointerEventTypes.POINTERDOWN) {
        const p = this.groundPoint(scene.pointerX, scene.pointerY);
        if (!p) return;
        this.dragStart = p;
        const r = Math.cbrt(this.p.spawnMass) * 1.5;
        this.ghost = MeshBuilder.CreateSphere('ghost', { diameter: r * 2, segments: 20 }, scene);
        const gm = new StandardMaterial('ghostMat', scene);
        gm.emissiveColor = new Color3(0.4, 0.7, 1.0);
        gm.alpha = 0.45;
        this.ghost.material = gm;
        this.ghost.position.copyFrom(p);
        this.ghost.isPickable = false;
        // suppress camera orbit while aiming
        this.ctx.camera.detachControl();
      } else if (pi.type === PointerEventTypes.POINTERMOVE && this.dragStart) {
        const p = this.groundPoint(scene.pointerX, scene.pointerY);
        if (!p) return;
        this.aimLine?.dispose();
        this.aimLine = MeshBuilder.CreateLines('aim', {
          points: [this.dragStart, p]
        }, scene);
        this.aimLine.color = new Color3(0.4, 0.75, 1.0);
        this.aimLine.isPickable = false;
      } else if (pi.type === PointerEventTypes.POINTERUP && this.dragStart) {
        const p = this.groundPoint(scene.pointerX, scene.pointerY) ?? this.dragStart;
        // drag vector sets launch velocity
        const vel = p.subtract(this.dragStart).scale(0.9);
        const kinds = [PlanetKind.Rocky, PlanetKind.Terran, PlanetKind.Desert, PlanetKind.Gas, PlanetKind.Ice];
        this.makeBody({
          pos: this.dragStart.clone(), vel, mass: this.p.spawnMass,
          kind: kinds[Math.floor(Math.random() * kinds.length)],
          tintA: new Color3(Math.random() * 0.5 + 0.2, Math.random() * 0.5 + 0.2, Math.random() * 0.5 + 0.2),
          tintB: new Color3(Math.random() * 0.5 + 0.4, Math.random() * 0.5 + 0.4, Math.random() * 0.5 + 0.4)
        });
        this.ghost?.dispose(); this.ghost = null;
        this.aimLine?.dispose(); this.aimLine = null;
        this.dragStart = null;
        this.ctx.camera.attachControl(scene.getEngine().getRenderingCanvas(), true);
      }
    });
  }

  /* ------------------------------ physics ------------------------------ */

  private accelerations(): void {
    const n = this.bodies.length;
    const soft = this.p.softening * this.p.softening;
    for (let i = 0; i < n; i++) this.bodies[i].acc.setAll(0);

    for (let i = 0; i < n; i++) {
      const a = this.bodies[i];
      if (!a.alive) continue;
      for (let j = i + 1; j < n; j++) {
        const b = this.bodies[j];
        if (!b.alive) continue;
        const dx = b.pos.x - a.pos.x;
        const dy = b.pos.y - a.pos.y;
        const dz = b.pos.z - a.pos.z;
        const d2 = dx * dx + dy * dy + dz * dz + soft;
        const inv = 1 / Math.sqrt(d2);
        const f = (G * this.p.gravity) / d2;
        const fx = dx * inv * f, fy = dy * inv * f, fz = dz * inv * f;
        a.acc.x += fx * b.mass; a.acc.y += fy * b.mass; a.acc.z += fz * b.mass;
        b.acc.x -= fx * a.mass; b.acc.y -= fy * a.mass; b.acc.z -= fz * a.mass;
      }
    }
  }

  private step(dt: number): void {
    // velocity Verlet: symplectic, so orbital energy does not drift
    this.accelerations();
    for (const b of this.bodies) {
      if (!b.alive || b.isStar) continue;
      b.vel.x += b.acc.x * dt * 0.5;
      b.vel.y += b.acc.y * dt * 0.5;
      b.vel.z += b.acc.z * dt * 0.5;
      b.pos.x += b.vel.x * dt;
      b.pos.y += b.vel.y * dt;
      b.pos.z += b.vel.z * dt;
    }
    this.accelerations();
    for (const b of this.bodies) {
      if (!b.alive || b.isStar) continue;
      b.vel.x += b.acc.x * dt * 0.5;
      b.vel.y += b.acc.y * dt * 0.5;
      b.vel.z += b.acc.z * dt * 0.5;
    }
    this.collide();
  }

  /** Inelastic merge conserving mass and momentum. */
  private collide(): void {
    for (let i = 0; i < this.bodies.length; i++) {
      const a = this.bodies[i];
      if (!a.alive) continue;
      for (let j = i + 1; j < this.bodies.length; j++) {
        const b = this.bodies[j];
        if (!b.alive) continue;
        const d = Vector3.Distance(a.pos, b.pos);
        if (d > a.radius + b.radius) continue;

        // larger body absorbs the smaller
        const [big, small] = a.mass >= b.mass ? [a, b] : [b, a];
        const m = big.mass + small.mass;
        big.vel = big.vel.scale(big.mass).add(small.vel.scale(small.mass)).scale(1 / m);
        if (!big.isStar) {
          big.pos = big.pos.scale(big.mass).add(small.pos.scale(small.mass)).scale(1 / m);
        }
        big.mass = m;
        const nr = Math.cbrt(m) * 1.5;
        big.mesh.scaling.setAll(nr / big.radius);
        big.radius = nr;
        big.mat.setFloat('radius', nr);

        small.alive = false;
        small.mesh.dispose();
        small.trailMesh?.dispose();
        this.collisions++;
      }
    }
    this.bodies = this.bodies.filter((b) => b.alive);
  }

  update(dt: number, ctx: WorldContext): void {
    const scaled = Math.min(dt, 1 / 30) * this.p.timeScale;
    this.t += scaled;

    // fixed substeps keep close encounters stable
    const sub = 4;
    const h = scaled / sub;
    if (!this.paused) for (let s = 0; s < sub; s++) this.step(h);

    const cam = ctx.camera;
    for (const b of this.bodies) {
      b.mesh.position.copyFrom(b.pos);
      if (!b.isStar) b.mesh.rotation.y += dt * 0.25;
      b.mat.setVector3('camPos', cam.position);
      b.mat.setVector3('sunPos', this.starMesh ? this.starMesh.position : Vector3.Zero());
      b.mat.setFloat('time', this.t);

      if (this.p.trails > 0.5 && !b.isStar) {
        b.trail.push(b.pos.clone());
        if (b.trail.length > this.p.trailLength) b.trail.shift();
        if (b.trail.length > 2) {
          b.trailMesh?.dispose();
          b.trailMesh = MeshBuilder.CreateLines('t', { points: b.trail }, ctx.scene);
          b.trailMesh.color = new Color3(0.35, 0.55, 0.85);
          b.trailMesh.alpha = 0.5;
          b.trailMesh.isPickable = false;
        }
      } else if (b.trailMesh) {
        b.trailMesh.dispose();
        b.trailMesh = null;
        b.trail.length = 0;
      }
    }
  }

  /* ------------------------------ UI surface ------------------------------ */

  getParams(): WorldParam[] {
    return [
      { key: 'spawnMass', label: 'New Body Mass', min: 0.2, max: 60, step: 0.2, value: this.p.spawnMass },
      { key: 'gravity', label: 'Gravity Strength', min: 0, max: 4, step: 0.05, value: this.p.gravity, unit: '×G' },
      { key: 'timeScale', label: 'Time Scale', min: 0, max: 5, step: 0.05, value: this.p.timeScale, unit: '×' },
      { key: 'trails', label: 'Orbit Trails', min: 0, max: 1, step: 1, value: this.p.trails },
      { key: 'trailLength', label: 'Trail Length', min: 20, max: 400, step: 10, value: this.p.trailLength },
      { key: 'softening', label: 'Softening', min: 0.05, max: 3, step: 0.05, value: this.p.softening }
    ];
  }

  getActions(): WorldAction[] {
    return [
      { key: 'planet', label: 'Add Planet', glyph: '🪐' },
      { key: 'giant', label: 'Add Giant', glyph: '🟠' },
      { key: 'binary', label: 'Binary Star', glyph: '⭐' },
      { key: 'shower', label: 'Asteroid Shower', glyph: '☄' },
      { key: 'chaos', label: 'Chaos', glyph: '🎲' },
      { key: 'destroy', label: 'Destroy Last', glyph: '💥' },
      { key: 'clear', label: 'Clear All', glyph: '🧹' }
    ];
  }

  runAction(key: string, ctx: WorldContext): void {
    const rnd = (a: number, b: number) => a + Math.random() * (b - a);
    const orbitAt = (r: number, mass: number, kind: PlanetKind) => {
      const a = Math.random() * Math.PI * 2;
      const centre = this.bodies.find((b) => b.isStar);
      const cm = centre ? centre.mass : 900;
      const pos = new Vector3(Math.cos(a) * r, rnd(-2, 2), Math.sin(a) * r);
      const speed = Math.sqrt((G * this.p.gravity * cm) / r);
      const vel = new Vector3(-Math.sin(a), 0, Math.cos(a)).scale(speed);
      this.makeBody({
        pos, vel, mass, kind,
        tintA: new Color3(rnd(0.2, 0.6), rnd(0.2, 0.6), rnd(0.2, 0.6)),
        tintB: new Color3(rnd(0.4, 0.9), rnd(0.4, 0.9), rnd(0.4, 0.9))
      });
    };

    if (key === 'planet') orbitAt(rnd(22, 95), this.p.spawnMass, PlanetKind.Terran);
    else if (key === 'giant') orbitAt(rnd(50, 110), Math.max(this.p.spawnMass, 25), PlanetKind.Gas);
    else if (key === 'binary') {
      // two stars orbiting their barycentre
      const r = 22, m = 420;
      const v = Math.sqrt((G * this.p.gravity * m) / (4 * r)) * 1.0;
      this.makeBody({
        pos: new Vector3(-r, 0, 0), vel: new Vector3(0, 0, -v), mass: m,
        kind: PlanetKind.Star, isStar: false,
        tintA: new Color3(1.0, 0.5, 0.1), tintB: new Color3(1.0, 0.95, 0.8)
      });
      this.makeBody({
        pos: new Vector3(r, 0, 0), vel: new Vector3(0, 0, v), mass: m,
        kind: PlanetKind.Star, isStar: false,
        tintA: new Color3(0.4, 0.6, 1.0), tintB: new Color3(0.9, 0.95, 1.0)
      });
    } else if (key === 'shower') {
      for (let i = 0; i < 14; i++) orbitAt(rnd(30, 120), rnd(0.15, 0.7), PlanetKind.Rocky);
    } else if (key === 'chaos') {
      for (let i = 0; i < 8; i++) {
        this.makeBody({
          pos: new Vector3(rnd(-70, 70), rnd(-25, 25), rnd(-70, 70)),
          vel: new Vector3(rnd(-9, 9), rnd(-4, 4), rnd(-9, 9)),
          mass: rnd(0.5, 14),
          kind: Math.floor(rnd(0, 6)) as PlanetKind,
          tintA: new Color3(Math.random(), Math.random(), Math.random()),
          tintB: new Color3(Math.random(), Math.random(), Math.random())
        });
      }
    } else if (key === 'destroy') {
      for (let i = this.bodies.length - 1; i >= 0; i--) {
        if (!this.bodies[i].isStar) {
          this.bodies[i].mesh.dispose();
          this.bodies[i].trailMesh?.dispose();
          this.bodies.splice(i, 1);
          break;
        }
      }
    } else if (key === 'clear') {
      for (const b of this.bodies) {
        if (!b.isStar) { b.mesh.dispose(); b.trailMesh?.dispose(); }
      }
      this.bodies = this.bodies.filter((b) => b.isStar);
    }
  }

  setParam(key: string, value: number): void {
    (this.p as any)[key] = value;
  }

  getStats(): Record<string, string> {
    let ke = 0, mass = 0;
    for (const b of this.bodies) {
      ke += 0.5 * b.mass * b.vel.lengthSquared();
      mass += b.mass;
    }
    return {
      'Bodies': String(this.bodies.length),
      'Total mass': mass.toFixed(1),
      'Kinetic energy': ke.toExponential(2),
      'Merges': String(this.collisions),
      'Integrator': 'Velocity Verlet',
      'Pairs / step': String((this.bodies.length * (this.bodies.length - 1)) / 2)
    };
  }

  dispose(): void {
    if (this.pointerObs) this.ctx.scene.onPointerObservable.remove(this.pointerObs);
    this.pointerObs = null;
    this.ghost?.dispose();
    this.aimLine?.dispose();
    for (const b of this.bodies) { b.mesh.dispose(); b.trailMesh?.dispose(); }
    this.bodies = [];
    this.sky?.dispose();
    this.light?.dispose();
  }
}
