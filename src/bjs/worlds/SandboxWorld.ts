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
import { findObject, MATERIALS, type ObjectDef } from '../content/ObjectCatalog';
import { buildObjectMesh } from '../content/ObjectFactory';
import { BeamSystem, BEAMS, type BeamKind, type BeamTarget } from '../systems/BeamSystem';
import { DestructionSystem } from '../systems/DestructionSystem';
import { PortalSystem } from '../systems/PortalSystem';
import { Wormhole, Galaxy, Nebula, type GalaxyKind } from '../systems/CosmicObjects';
import { AISystem } from '../systems/AISystem';
import { PLANET_SHADER, registerPlanetShader, PlanetKind } from '../shaders/PlanetShader';
import { applyPlanetMap, PLANET_MAP_UNIFORMS, PLANET_MAP_SAMPLERS } from '../PlanetMaps';
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
  def?: ObjectDef;
  restitution: number;
  heat: number;
  fracture: number;
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
  private spawned = 0;
  private beams!: BeamSystem;
  private destruction!: DestructionSystem;
  private portals!: PortalSystem;
  private lastCam = new Vector3(0, 0, 0);
  private beamKind: BeamKind = 'laser';
  private destroyed = 0;
  private wormholes: Wormhole[] = [];
  private galaxies: Galaxy[] = [];
  private nebulae: Nebula[] = [];
  private ai!: AISystem;
  private paused = false;

  private p = {
    beamWidth: 1.6,
    beamPower: 1.0,
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
    // INK-BLACK VACUUM. All the colour in ordinary space comes from the
    // galaxy fog volume, never from the clear colour - lifting this to fake
    // a "space blue" washes the whole frame and buries the faint stars.
    scene.clearColor = new Color4(0, 0, 0, 1);

    registerPlanetShader();

    // No sky mesh: the star volume (LayeredSky) is the sky. A wrapped sphere
    // silhouettes its own triangles against it.

    const hemi = new HemisphericLight('hemi', new Vector3(0, 1, 0), scene);
    hemi.intensity = 0.12;

    this.light = new PointLight('starLight', Vector3.Zero(), scene);
    this.light.intensity = 1.6;
    this.light.range = 2000;

    this.ai = new AISystem(scene);
    this.beams = new BeamSystem(scene);
    this.destruction = new DestructionSystem(scene);
    this.portals = new PortalSystem(scene);

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
                 'cityLights', 'radius', 'isStar', ...PLANET_MAP_UNIFORMS],
      samplers: PLANET_MAP_SAMPLERS
    });
    mat.setFloat('seed', seed);
    mat.setFloat('ptype', opts.kind);
    mat.setColor3('tintA', opts.tintA ?? new Color3(0.36, 0.30, 0.26));
    mat.setColor3('tintB', opts.tintB ?? new Color3(0.68, 0.60, 0.52));
    mat.setFloat('radius', radius);
    mat.setFloat('isStar', opts.isStar ? 1 : 0);
    // Stars stay procedural; everything else gets the photoreal surface art.
    if (opts.isStar) { mat.setFloat('useMap', 0); mat.setFloat('oceanDepth', 0); }
    else applyPlanetMap(mat, opts.kind as PlanetKind, scene, Math.floor(seed * 100000));
    mat.setFloat('detail', 1.0);
    mat.setFloat('cloudAmt', opts.kind === PlanetKind.Terran ? 0.7 : 0);
    mat.setFloat('cityLights', opts.kind === PlanetKind.Terran ? 1 : 0);
    mesh.material = mat;

    const b: Body = {
      mesh, mat,
      pos: opts.pos.clone(), vel: opts.vel.clone(), acc: Vector3.Zero(),
      mass: opts.mass, radius, kind: opts.kind, seed,
      isStar: !!opts.isStar, trail: [], trailMesh: null, alive: true,
      restitution: 0, heat: 0, fracture: 0.5
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

  /**
   * Spawns a catalogue object on an intercept course with the primary body.
   * The object is a first-class physics body: it attracts, is attracted,
   * collides and merges exactly like a planet does.
   */
  spawnObject(id: string, scale: number, ctx: WorldContext): void {
    const def = findObject(id);
    if (!def) return;
    const scene = ctx.scene;

    const mp = MATERIALS[def.material];
    const radius = def.radius * scale;
    const mass = def.mass * Math.pow(scale, 3) * mp.density * 0.35;

    const mesh = buildObjectMesh(scene, def);
    mesh.scaling.setAll(scale);

    // Aim it at the largest body from a random direction outside the system.
    const target = this.bodies.reduce<Body | null>(
      (best, b) => (!best || b.mass > best.mass ? b : best), null);
    const tp = target ? target.pos : Vector3.Zero();

    const a = Math.random() * Math.PI * 2;
    const dist = 120 + Math.random() * 60;
    const pos = new Vector3(
      tp.x + Math.cos(a) * dist,
      tp.y + (Math.random() - 0.5) * 40,
      tp.z + Math.sin(a) * dist
    );
    // velocity toward the target, with a little lateral offset so some shots miss
    const toward = tp.subtract(pos).normalize();
    const lateral = new Vector3(-toward.z, 0, toward.x).scale((Math.random() - 0.5) * 6);
    const speed = 12 + Math.random() * 10;
    const vel = toward.scale(speed).add(lateral);

    mesh.position.copyFrom(pos);

    const b: Body = {
      mesh: mesh as unknown as Mesh,
      mat: null as any,
      pos, vel, acc: Vector3.Zero(),
      mass, radius, kind: PlanetKind.Rocky, seed: this.nextSeed++,
      isStar: false, trail: [], trailMesh: null, alive: true,
      def, restitution: mp.restitution, heat: 0, fracture: mp.fracture
    };
    this.bodies.push(b);
    this.spawned++;
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
        big.mesh.scaling.setAll((big.mesh.scaling.x || 1) * (nr / big.radius));
        big.radius = nr;
        big.mat?.setFloat('radius', nr);

        // impact energy from relative velocity drives the visuals
        const relV = small.vel.subtract(big.vel).length();
        const impactE = 0.5 * small.mass * relV * relV;
        this.destruction.impact(small.pos, Math.max(small.radius, 1), impactE);

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

    // ---- beams act on the same bodies the gravity solver uses ----
    const targets = this.bodies.filter((b) => !b.isStar) as unknown as BeamTarget[];
    const hits = this.beams.update((scaled || dt) * this.p.beamPower, targets);
    for (const h of hits) {
      const body = h.target as unknown as Body;
      if (body.heat >= 1 && body.alive) this.destroyBody(body, 'burned');
    }
    // ---- alien fleet ----
    if (this.ai.count() > 0) {
      const prey = this.bodies.reduce<Body | null>(
        (best, b) => (!b.isStar && (!best || b.mass > best.mass) ? b : best), null);
      if (prey) this.ai.setTarget(prey as any);
      const impacts = this.ai.update(dt);
      for (const ship of impacts) {
        // a ship hitting a planet is a real impact event
        this.destruction.impact(ship.pos, 3.5, 4000);
        if (prey) {
          prey.heat = Math.min(1.2, prey.heat + 0.22);
          prey.vel.addInPlace(ship.vel.scale(ship.cfg.maxSpeed * 0.0006));
          if (prey.heat >= 1) this.destroyBody(prey, 'invaded');
        }
      }
      // beams shoot ships down too - same generic hit test
      for (const ship of this.ai.ships) {
        for (const h of hits) {
          if (Vector3.Distance(h.point, ship.pos) < ship.radius + 2) {
            ship.damage(h.energy * 0.5);
          }
        }
      }
    }

    this.destruction.update(dt);

    // ---- portals. Bodies that fly into a mouth come out the other end. ----
    const camPos = ctx.camera?.position ?? Vector3.Zero();
    this.portals.update(dt, camPos);

    // The player flying into a tear travels to the dimension the portal has
    // been showing them through its lens.
    if (this.portals.count() > 0 && ctx.enterDimension) {
      // 'key' gives the camera a stable identity across frames; without it the
      // per-traveller cooldown cannot recognise the player and the jump would
      // re-fire on every frame spent inside the tear.
      const cam = {
        position: camPos.clone(),
        velocity: camPos.subtract(this.lastCam),
        key: 'player'
      };
      const hit = this.portals.tryTransit(cam, 2);
      if (hit && hit.kind === 'tear' && hit.destination) {
        ctx.enterDimension(hit.destination.seed, hit.destination.depth);
      }
      this.lastCam.copyFrom(camPos);
    }
    if (this.portals.count() > 0) {
      for (const b of this.bodies) {
        if (b.isStar) continue;
        // Bodies use pos/vel; the portal system speaks position/velocity.
        // Adapt rather than renaming the physics fields.
        const t = { position: b.pos, velocity: b.vel };
        const used = this.portals.tryTransit(t, b.radius);
        if (used) {
          b.pos.copyFrom(t.position);
          b.vel.copyFrom(t.velocity);
          // keep the mesh with its body, or it visibly lags a frame behind
          b.mesh.position.copyFrom(b.pos);
        }
      }
    }
    const nowSec = this.t;
    for (const w of this.wormholes) {
      w.update(dt);
      w.process(this.bodies.filter((b) => !b.isStar) as any, nowSec);
    }

    const cam = ctx.camera;
    for (const b of this.bodies) {
      b.mesh.position.copyFrom(b.pos);
      // heated bodies glow before they break apart
      if (b.heat > 0.02 && b.mesh.material) {
        const m: any = b.mesh.material;
        if (m.emissiveColor) {
          const g = Math.min(1, b.heat);
          m.emissiveColor.set(g, g * 0.35, g * 0.08);
        }
        b.heat = Math.max(0, b.heat - dt * 0.05);   // radiative cooling
      }
      if (!b.isStar) b.mesh.rotation.y += dt * 0.25;
      if (b.mat) {
        b.mat.setVector3('camPos', cam.position);
        b.mat.setVector3('sunPos', this.starMesh ? this.starMesh.position : Vector3.Zero());
        b.mat.setFloat('time', this.t);
      }

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

  /** Destroys a body, spawning debris from the destruction system. */
  private destroyBody(b: Body, _reason: string): void {
    if (!b.alive) return;
    const energy = 0.5 * b.mass * b.vel.lengthSquared() + b.mass * 8;
    const frags = this.destruction.fragment(
      b.pos, b.vel, b.mass, b.radius, energy, b.fracture);

    this.destruction.impact(b.pos, b.radius, energy);

    b.alive = false;
    b.mesh.dispose();
    b.trailMesh?.dispose();
    this.destroyed++;

    // debris keeps participating in gravity
    for (const f of frags) {
      if (this.bodies.length > 260) break;      // keep performance sane
      const m = MeshBuilder.CreateSphere('frag', { diameter: f.radius * 2, segments: 8 }, this.ctx.scene);
      const mm = new StandardMaterial('fragMat', this.ctx.scene);
      mm.diffuseColor = new Color3(0.35, 0.3, 0.28);
      mm.emissiveColor = new Color3(0.25, 0.09, 0.03);
      mm.specularColor = Color3.Black();
      m.material = mm;
      m.position.copyFrom(f.pos);
      m.isPickable = false;
      this.bodies.push({
        mesh: m, mat: null as any,
        pos: f.pos.clone(), vel: f.vel.clone(), acc: Vector3.Zero(),
        mass: f.mass, radius: f.radius, kind: PlanetKind.Rocky,
        seed: this.nextSeed++, isStar: false, trail: [], trailMesh: null,
        alive: true, restitution: 0.2, heat: 0.5, fracture: 0.3
      });
    }
    this.bodies = this.bodies.filter((x) => x.alive);
  }

  /**
   * God powers: direct authorship of the universe. Each one acts through the
   * same body list the solver uses, so nothing here is a special case.
   */
  private runGodPower(power: string, ctx: WorldContext): void {
    const rnd = (a: number, b: number) => a + Math.random() * (b - a);

    if (power === 'lightning') {
      // strike every body with a bolt of energy: heat plus a violent kick
      for (const b of this.bodies) {
        if (b.isStar || Math.random() > 0.55) continue;
        const from = b.pos.add(new Vector3(rnd(-6, 6), 90, rnd(-6, 6)));
        this.beams.fire('plasma', from, b.pos.subtract(from).normalize(),
          0.7, 140, 0.35);
        b.heat = Math.min(1.1, b.heat + 0.35);
        b.vel.addInPlace(new Vector3(rnd(-4, 4), rnd(-8, -2), rnd(-4, 4)));
        this.destruction.flash(b.pos, b.radius * 1.4, [0.8, 0.9, 1]);
      }
    } else if (power === 'planet') {
      const a = Math.random() * Math.PI * 2;
      const r = rnd(35, 95);
      const speed = Math.sqrt((42 * 900) / r);
      this.makeBody({
        pos: new Vector3(Math.cos(a) * r, rnd(-8, 8), Math.sin(a) * r),
        vel: new Vector3(-Math.sin(a), 0, Math.cos(a)).scale(speed),
        mass: rnd(60, 420),
        kind: [PlanetKind.Terran, PlanetKind.Rocky, PlanetKind.Ice,
               PlanetKind.Desert, PlanetKind.Gas][Math.floor(Math.random() * 5)]
      });
    } else if (power === 'star') {
      const a = Math.random() * Math.PI * 2;
      const r = rnd(120, 200);
      this.makeBody({
        pos: new Vector3(Math.cos(a) * r, rnd(-20, 20), Math.sin(a) * r),
        vel: new Vector3(-Math.sin(a), 0, Math.cos(a)).scale(rnd(4, 10)),
        mass: rnd(700, 1600), kind: PlanetKind.Star, isStar: true,
        tintA: new Color3(1, 0.85, 0.5), tintB: new Color3(1, 0.97, 0.85)
      });
    } else if (power === 'life') {
      // scatter creatures across the largest world
      const host = this.bodies.reduce<Body | null>(
        (best, b) => (!b.isStar && (!best || b.mass > best.mass) ? b : best), null);
      const ids = ['duck', 'chicken', 'teddy', 'brain', 'alienjellyfish', 'gianteyeball'];
      for (let i = 0; i < 12; i++) {
        this.spawnObject(ids[i % ids.length], 1, ctx);
        const nb = this.bodies[this.bodies.length - 1];
        if (nb && host) {
          const d = new Vector3(rnd(-1, 1), rnd(-1, 1), rnd(-1, 1)).normalize();
          nb.pos.copyFrom(host.pos.add(d.scale(host.radius + 6)));
          nb.vel.copyFrom(host.vel);
          nb.mesh.position.copyFrom(nb.pos);
        }
      }
    } else if (power === 'rapture') {
      // everything gently ascends
      for (const b of this.bodies) {
        if (b.isStar) continue;
        b.vel.y += 18;
      }
    } else if (power === 'freeze') {
      for (const b of this.bodies) b.vel.setAll(0);
      this.p.timeScale = 0;
    } else if (power === 'reverse') {
      for (const b of this.bodies) b.vel.scaleInPlace(-1);
    } else if (power === 'cube') {
      // every planet becomes a cube, because you are allowed to do that
      for (const b of this.bodies) {
        if (b.isStar) continue;
        b.mesh.scaling.scaleInPlace(0.82);
        (b.mesh as any).convertToFlatShadedMesh?.();
      }
    } else if (power === 'giant') {
      for (const b of this.bodies) {
        if (b.isStar) continue;
        b.mass *= 2.2;
        b.radius = Math.cbrt(b.mass) * 1.5;
        b.mesh.scaling.scaleInPlace(1.5);
      }
    } else if (power === 'shrink') {
      for (const b of this.bodies) {
        if (b.isStar) continue;
        b.mass = Math.max(0.05, b.mass / 2.2);
        b.radius = Math.cbrt(b.mass) * 1.5;
        b.mesh.scaling.scaleInPlace(1 / 1.5);
      }
    }
  }

  /** Fires a beam from the camera toward the biggest body. */
  fireBeam(kind: BeamKind, ctx: WorldContext): void {
    const cam = ctx.camera;
    const target = this.bodies.reduce<Body | null>(
      (best, b) => (!best || b.mass > best.mass ? b : best), null);
    const aim = target ? target.pos : Vector3.Zero();
    const origin = cam.position.clone();
    const dir = aim.subtract(origin).normalize();
    const def = BEAMS[kind];
    this.beams.fire(kind, origin, dir,
      this.p.beamWidth, 700, 1.8);
    // instant visual feedback at the target
    this.destruction.flash(aim, 1.4, def.color);
  }

  /* ---------------------------- state capture ---------------------------- */

  /** Serialisable snapshot of every simulated body plus tuning parameters. */
  captureState(): any {
    return {
      p: { ...this.p },
      bodies: this.bodies.map((b) => ({
        pos: [b.pos.x, b.pos.y, b.pos.z],
        vel: [b.vel.x, b.vel.y, b.vel.z],
        mass: b.mass, radius: b.radius, kind: b.kind,
        isStar: b.isStar, defId: b.def?.id ?? null,
        heat: b.heat, fracture: b.fracture, restitution: b.restitution
      }))
    };
  }

  restoreState(state: any): void {
    if (!state) return;
    Object.assign(this.p, state.p ?? {});

    for (const b of this.bodies) { b.mesh.dispose(); b.trailMesh?.dispose(); }
    this.bodies = [];
    this.starMesh = null;

    for (const sb of state.bodies ?? []) {
      const pos = new Vector3(sb.pos[0], sb.pos[1], sb.pos[2]);
      const vel = new Vector3(sb.vel[0], sb.vel[1], sb.vel[2]);
      if (sb.defId) {
        // rebuild a catalogue object at its recorded transform
        const before = this.bodies.length;
        this.spawnObject(sb.defId, 1, this.ctx);
        const nb = this.bodies[before];
        if (nb) {
          nb.pos.copyFrom(pos); nb.vel.copyFrom(vel);
          nb.mass = sb.mass; nb.radius = sb.radius;
          nb.heat = sb.heat ?? 0;
          nb.mesh.position.copyFrom(pos);
        }
      } else {
        const nb = this.makeBody({
          pos, vel, mass: sb.mass, kind: sb.kind, isStar: sb.isStar
        });
        nb.heat = sb.heat ?? 0;
        nb.fracture = sb.fracture ?? 0.5;
        if (sb.isStar) this.starMesh = nb.mesh;
      }
    }
    this.spawned = 0;
  }

  /* ------------------------------ UI surface ------------------------------ */

  getParams(): WorldParam[] {
    return [
      { key: 'beamWidth', label: 'Beam Width', min: 0.3, max: 12, step: 0.1, value: this.p.beamWidth },
      { key: 'beamPower', label: 'Beam Power', min: 0.2, max: 4, step: 0.05, value: this.p.beamPower, unit: '×' },
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
      { key: 'weird', label: 'Make It Weird', glyph: '✨' },
      { key: 'rain', label: 'Asteroid Rain ×40', glyph: '🌧' },
      { key: 'ducks', label: '100 Rubber Ducks', glyph: '🦆' },
      { key: 'donotpress', label: 'DO NOT PRESS', glyph: '🚨' },
      { key: 'beam:laser', label: 'Laser', glyph: '🔴' },
      { key: 'beam:plasma', label: 'Plasma Beam', glyph: '🟣' },
      { key: 'beam:heat', label: 'Heat Ray', glyph: '🟠' },
      { key: 'beam:freeze', label: 'Freeze Ray', glyph: '🔵' },
      { key: 'beam:tractor', label: 'Tractor Beam', glyph: '🔗' },
      { key: 'beam:repulsor', label: 'Repulsor', glyph: '💨' },
      { key: 'beam:push', label: 'Planet Punch', glyph: '👊' },
      { key: 'beam:disintegrate', label: 'Disintegrator', glyph: '☠' },
      { key: 'smash', label: 'Planet Smasher', glyph: '💢' },
      { key: 'portal:wormhole', label: 'Open a Wormhole', glyph: '🌐' },
      { key: 'portal:tear', label: 'Rip a Space Tear', glyph: '🕳' },
      { key: 'portal:close', label: 'Close All Portals', glyph: '✖' },
      { key: 'god:lightning', label: 'Summon Lightning', glyph: '⚡' },
      { key: 'god:planet', label: 'Create a Planet', glyph: '🌍' },
      { key: 'god:star', label: 'Ignite a Star', glyph: '☀' },
      { key: 'god:life', label: 'Seed Life', glyph: '🌱' },
      { key: 'god:rapture', label: 'Lift Everything', glyph: '🕊' },
      { key: 'god:freeze', label: 'Freeze Time', glyph: '❄' },
      { key: 'god:reverse', label: 'Reverse Time', glyph: '⏪' },
      { key: 'god:cube', label: 'Cube Everything', glyph: '🧊' },
      { key: 'god:giant', label: 'Embiggen All', glyph: '🔎' },
      { key: 'god:shrink', label: 'Shrink All', glyph: '🔬' },
      { key: 'ufo', label: 'Send a UFO', glyph: '🛸' },
      { key: 'invasion', label: 'ALIEN INVASION', glyph: '👽' },
      { key: 'mothership', label: 'Mothership', glyph: '🛰' },
      { key: 'wormhole', label: 'Wormhole Pair', glyph: '🕳' },
      { key: 'galaxy', label: 'Spawn Galaxy', glyph: '🌌' },
      { key: 'nebula', label: 'Spawn Nebula', glyph: '☁' },
      { key: 'blackhole', label: 'Drop Black Hole', glyph: '⚫' },
      { key: 'supernova', label: 'Supernova', glyph: '💥' },
      { key: 'destroy', label: 'Destroy Last', glyph: '💥' },
      { key: 'clear', label: 'Clear All', glyph: '🧹' }
    ];
  }

  runAction(key: string, ctx: WorldContext): void {
    if (key.startsWith('portal:')) {
      const what = key.slice(7);
      const rndv = (a: number, b: number) => a + Math.random() * (b - a);
      if (what === 'wormhole') {
        // link two points on opposite sides of the system
        const a = new Vector3(rndv(-70, -30), rndv(-10, 20), rndv(-40, 40));
        const b = new Vector3(rndv(30, 70), rndv(-10, 20), rndv(-40, 40));
        this.portals.createWormhole(a, b, 6, 1.4);
      } else if (what === 'tear') {
        const at = new Vector3(rndv(-50, 50), rndv(0, 30), rndv(-50, 50));
        this.portals.createTear(at, at.clone().normalize().scale(-1), 7,
          this.portals.lastDestination);
      } else if (what === 'close') {
        this.portals.closeAll();
      }
      return;
    }
    if (key.startsWith('god:')) {
      this.runGodPower(key.slice(4), ctx);
      return;
    }
    if (key.startsWith('beam:')) {
      this.fireBeam(key.slice(5) as BeamKind, ctx);
      return;
    }
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
    } else if (key === 'weird') {
      // randomise the whole simulation into something absurd
      this.p.gravity = rnd(0.2, 3.2);
      this.p.timeScale = rnd(0.3, 3);
      this.p.spawnMass = rnd(0.5, 30);
      this.p.trailLength = Math.round(rnd(60, 380));
      for (let i = 0; i < 5; i++) {
        this.spawnObject(
          ['duck', 'piano', 'banana', 'toilet', 'ufo', 'whale', 'donut', 'anvil'][
            Math.floor(Math.random() * 8)],
          rnd(2, 14), ctx);
      }
    } else if (key === 'rain') {
      for (let i = 0; i < 40; i++) this.spawnObject('asteroid', rnd(0.4, 1.6), ctx);
    } else if (key === 'ducks') {
      for (let i = 0; i < 100; i++) this.spawnObject('duck', rnd(0.6, 2.2), ctx);
    } else if (key === 'donotpress') {
      // absurd but safely contained
      for (let i = 0; i < 24; i++) {
        this.spawnObject(
          ['duck', 'toilet', 'piano', 'chicken', 'cart', 'dice', 'penguin', 'pizza'][i % 8],
          rnd(3, 18), ctx);
      }
      this.p.gravity = 2.8;
      this.p.timeScale = 2.2;
    } else if (key === 'smash') {
      const target = this.bodies.reduce<Body | null>(
        (best, b) => (!best || (b.mass > best.mass && !b.isStar) ? b : best), null);
      const tp = target ? target.pos : Vector3.Zero();
      const a = Math.random() * Math.PI * 2;
      const pos = new Vector3(tp.x + Math.cos(a) * 150, tp.y + rnd(-20, 20), tp.z + Math.sin(a) * 150);
      const vel = tp.subtract(pos).normalize().scale(34);
      this.makeBody({
        pos, vel, mass: Math.max(this.p.spawnMass * 6, 120), kind: PlanetKind.Rocky,
        tintA: new Color3(0.5, 0.25, 0.15), tintB: new Color3(0.8, 0.5, 0.3)
      });
    } else if (key === 'ufo' || key === 'invasion' || key === 'mothership') {
      const prey = this.bodies.reduce<Body | null>(
        (best, b) => (!b.isStar && (!best || b.mass > best.mass) ? b : best), null);
      const tgt = prey ?? this.bodies[0];
      if (tgt) {
        const from = new Vector3(rnd(-260, 260), rnd(40, 140), rnd(-260, 260));
        if (key === 'ufo') {
          const sh = this.ai.spawn('scout', from);
          sh.target = tgt as any;
        } else if (key === 'mothership') {
          const sh = this.ai.spawn('mothership', from);
          sh.target = tgt as any;
          this.ai.invade(6, tgt as any, from);
        } else {
          this.ai.invade(14, tgt as any, from);
        }
      }
    } else if (key === 'wormhole') {
      const a2 = Math.random() * Math.PI * 2;
      const posA = new Vector3(Math.cos(a2) * 70, rnd(-15, 15), Math.sin(a2) * 70);
      const posB = new Vector3(Math.cos(a2 + Math.PI) * 70, rnd(-15, 15), Math.sin(a2 + Math.PI) * 70);
      this.wormholes.push(new Wormhole(ctx.scene, posA, posB, 7));
    } else if (key === 'galaxy') {
      const kinds: GalaxyKind[] = ['spiral', 'barred', 'elliptical', 'irregular', 'ring'];
      const k = kinds[Math.floor(Math.random() * kinds.length)];
      const c = new Vector3(rnd(-900, 900), rnd(-350, 350), rnd(-900, 900));
      this.galaxies.push(new Galaxy(ctx.scene, k, c, rnd(160, 300), 12000));
    } else if (key === 'nebula') {
      const c = new Vector3(rnd(-700, 700), rnd(-260, 260), rnd(-700, 700));
      this.nebulae.push(new Nebula(ctx.scene, c, rnd(120, 240), 6000,
        [Math.random() * 0.6 + 0.3, Math.random() * 0.5 + 0.2, Math.random() * 0.5 + 0.5]));
    } else if (key === 'blackhole') {
      const a3 = Math.random() * Math.PI * 2;
      const r3 = rnd(60, 110);
      this.makeBody({
        pos: new Vector3(Math.cos(a3) * r3, rnd(-10, 10), Math.sin(a3) * r3),
        vel: new Vector3(-Math.sin(a3), 0, Math.cos(a3)).scale(Math.sqrt((42 * 900) / r3) * 0.8),
        mass: 4000, kind: PlanetKind.Star,
        tintA: new Color3(0.02, 0.0, 0.05), tintB: new Color3(0.35, 0.1, 0.6)
      });
    } else if (key === 'supernova') {
      // blow the star apart into a violent expanding shell
      const star = this.bodies.find((b) => b.isStar) ?? this.bodies[0];
      if (star) {
        this.destruction.impact(star.pos, star.radius * 3, 90000);
        for (let i = 0; i < 26; i++) {
          const dir = new Vector3(rnd(-1, 1), rnd(-1, 1), rnd(-1, 1)).normalize();
          this.makeBody({
            pos: star.pos.add(dir.scale(star.radius * 1.4)),
            vel: dir.scale(rnd(28, 52)),
            mass: rnd(0.6, 5), kind: PlanetKind.Lava,
            tintA: new Color3(1.0, 0.5, 0.1), tintB: new Color3(1.0, 0.85, 0.4)
          });
        }
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
      'Objects thrown': String(this.spawned),
      'Destroyed': String(this.destroyed),
      'Active beams': String(this.beams?.count ?? 0),
      'Impacts': String(this.destruction?.getStats().craters ?? 0),
      'Fixed wormholes': String(this.wormholes.length),
      'Galaxies': String(this.galaxies.length),
      'Alien ships': String(this.ai?.count() ?? 0),
      'Ships crashed': String(this.ai?.crashes ?? 0),
      ...this.portals.stats(),
      'Integrator': 'Velocity Verlet',
      'Pairs / step': String((this.bodies.length * (this.bodies.length - 1)) / 2)
    };
  }

  dispose(): void {
    this.portals?.dispose();
    this.ai?.dispose();
    this.wormholes.forEach((w) => w.dispose());
    this.galaxies.forEach((g) => g.dispose());
    this.nebulae.forEach((n) => n.dispose());
    this.wormholes = []; this.galaxies = []; this.nebulae = [];
    this.beams?.dispose();
    this.destruction?.dispose();
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
