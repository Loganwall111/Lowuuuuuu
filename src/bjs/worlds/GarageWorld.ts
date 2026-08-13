/**
 * GarageWorld — the infinite white room you wake up in.
 *
 * Deliberately the opposite of the rest of the game: no stars, no horizon,
 * no scale cues. A white void with a floor that fades out, a door, the
 * people who teach you the rules, and the portal that takes you to the ship.
 *
 * The "infinite" read comes from fog matched exactly to the background, so
 * the floor dissolves rather than ending at an edge.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { LESSONS } from '../systems/IntroSequence';

interface Speaker {
  mesh: Mesh;
  head: Mesh;
  home: Vector3;
  phase: number;
  name: string;
}

export class GarageWorld implements World {
  id = 'garage';
  name = 'Garage';

  private scene!: Scene;
  private t = 0;
  private meshes: Mesh[] = [];
  private mats: StandardMaterial[] = [];
  private speakers: Speaker[] = [];
  private door: Mesh | null = null;
  private portal: Mesh | null = null;
  private portalMat: StandardMaterial | null = null;
  private p = { brightness: 1.0, doorOpen: 0 };

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    this.scene = scene;

    // A white void. Fog is the same colour as the background, so the floor
    // fades into nothing and the room reads as endless.
    const WHITE = new Color3(0.94, 0.95, 0.97);
    scene.clearColor = new Color4(WHITE.r, WHITE.g, WHITE.b, 1);
    scene.fogMode = Scene.FOGMODE_EXP2;
    scene.fogColor = WHITE;
    scene.fogDensity = 0.0075;

    const amb = new HemisphericLight('garageAmb', new Vector3(0, 1, 0), scene);
    amb.intensity = 1.25;
    amb.diffuse = new Color3(1, 1, 1);
    amb.groundColor = new Color3(0.82, 0.84, 0.9);

    // ---- floor ----
    const floor = MeshBuilder.CreateGround('garageFloor',
      { width: 900, height: 900, subdivisions: 2 }, scene);
    const fm = new StandardMaterial('garageFloorM', scene);
    fm.diffuseColor = new Color3(0.90, 0.91, 0.94);
    fm.specularColor = new Color3(0.16, 0.16, 0.18);
    fm.specularPower = 64;
    // Never black, even with every light off.
    fm.emissiveColor = new Color3(0.42, 0.43, 0.46);
    floor.material = fm;
    this.meshes.push(floor);
    this.mats.push(fm);

    // A faint grid so movement is legible in a room with no other features.
    for (let i = -20; i <= 20; i++) {
      const line = MeshBuilder.CreateBox('grid_' + i,
        { width: 0.05, height: 0.01, depth: 240 }, scene);
      line.position.set(i * 6, 0.01, 0);
      const lm = new StandardMaterial('gridM_' + i, scene);
      lm.emissiveColor = new Color3(0.80, 0.82, 0.87);
      lm.disableLighting = true;
      line.material = lm;
      line.isPickable = false;
      this.meshes.push(line);
      this.mats.push(lm);

      const cross = MeshBuilder.CreateBox('gridx_' + i,
        { width: 240, height: 0.01, depth: 0.05 }, scene);
      cross.position.set(0, 0.01, i * 6);
      cross.material = lm;
      cross.isPickable = false;
      this.meshes.push(cross);
    }

    // ---- the door ----
    const frame = MeshBuilder.CreateBox('doorFrame',
      { width: 6.4, height: 9.4, depth: 0.6 }, scene);
    frame.position.set(0, 4.7, 26);
    const dm = new StandardMaterial('doorFrameM', scene);
    dm.diffuseColor = new Color3(0.20, 0.22, 0.26);
    dm.emissiveColor = new Color3(0.06, 0.07, 0.09);
    frame.material = dm;
    this.meshes.push(frame);
    this.mats.push(dm);

    const door = MeshBuilder.CreateBox('door',
      { width: 5.6, height: 8.6, depth: 0.3 }, scene);
    door.position.set(0, 4.3, 25.7);
    const doorM = new StandardMaterial('doorM', scene);
    doorM.diffuseColor = new Color3(0.86, 0.87, 0.9);
    doorM.emissiveColor = new Color3(0.30, 0.31, 0.34);
    door.material = doorM;
    this.door = door;
    this.meshes.push(door);
    this.mats.push(doorM);

    // ---- the people who teach you ----
    // One per distinct speaker in the lesson list, so the cast is derived
    // from the script rather than hardcoded twice.
    const names = [...new Set(LESSONS.map((l) => l.speaker))];
    names.forEach((nm, i) => {
      const a = (i / Math.max(names.length, 1)) * Math.PI - Math.PI * 0.5;
      const x = Math.sin(a) * 7;
      const z = 10 + Math.cos(a) * 3;

      const body = MeshBuilder.CreateCapsule('npc_' + nm,
        { radius: 0.42, height: 1.8, tessellation: 12 }, scene);
      body.position.set(x, 0.9, z);
      const bm = new StandardMaterial('npcM_' + nm, scene);
      bm.diffuseColor = new Color3(0.22 + i * 0.14, 0.34, 0.62 - i * 0.1);
      bm.emissiveColor = bm.diffuseColor.scale(0.28);
      body.material = bm;

      const head = MeshBuilder.CreateSphere('npcHead_' + nm,
        { diameter: 0.52, segments: 12 }, scene);
      head.position.set(x, 1.98, z);
      const hm = new StandardMaterial('npcHeadM_' + nm, scene);
      hm.diffuseColor = new Color3(0.86, 0.76, 0.66);
      hm.emissiveColor = new Color3(0.3, 0.26, 0.23);
      head.material = hm;

      this.meshes.push(body, head);
      this.mats.push(bm, hm);
      this.speakers.push({
        mesh: body, head, home: new Vector3(x, 0.9, z),
        phase: i * 1.7, name: nm
      });
    });

    // ---- the portal to the ship ----
    const portal = MeshBuilder.CreateTorus('introPortal',
      { diameter: 5.2, thickness: 0.42, tessellation: 48 }, scene);
    portal.position.set(0, 3.0, 18);
    portal.rotation.x = Math.PI / 2;
    const pm = new StandardMaterial('introPortalM', scene);
    pm.emissiveColor = new Color3(0.35, 0.75, 1.0);
    pm.diffuseColor = new Color3(0.1, 0.3, 0.5);
    pm.disableLighting = true;
    portal.material = pm;
    this.portal = portal;
    this.portalMat = pm;
    this.meshes.push(portal);
    this.mats.push(pm);

    const disc = MeshBuilder.CreateDisc('introPortalDisc',
      { radius: 2.35, tessellation: 48 }, scene);
    disc.position.set(0, 3.0, 18.02);
    const dmm = new StandardMaterial('introPortalDiscM', scene);
    dmm.emissiveColor = new Color3(0.10, 0.30, 0.55);
    dmm.disableLighting = true;
    dmm.alpha = 0.78;
    dmm.backFaceCulling = false;
    disc.material = dmm;
    this.meshes.push(disc);
    this.mats.push(dmm);

    const glow = new PointLight('portalGlow', new Vector3(0, 3, 17), scene);
    glow.diffuse = new Color3(0.4, 0.7, 1.0);
    glow.intensity = 0.8;
    glow.range = 40;

    ctx.setCameraTarget(new Vector3(0, 2, 8), 14);
  }

  update(dt: number, _ctx: WorldContext): void {
    this.t += dt;

    // The people shift their weight and glance about, so the room is not
    // populated by statues.
    for (const s of this.speakers) {
      const b = Math.sin(this.t * 1.1 + s.phase) * 0.04;
      s.mesh.position.y = s.home.y + b;
      s.head.position.y = s.home.y + 1.08 + b;
      s.head.rotation.y = Math.sin(this.t * 0.6 + s.phase) * 0.5;
      s.mesh.rotation.z = Math.sin(this.t * 0.8 + s.phase) * 0.02;
    }

    if (this.portal) {
      this.portal.rotation.z += dt * 0.6;
      const pulse = 0.75 + Math.sin(this.t * 2.2) * 0.25;
      if (this.portalMat) {
        this.portalMat.emissiveColor = new Color3(
          0.28 * pulse, 0.62 * pulse, 1.0 * pulse);
      }
    }

    // The door slides open as the tutorial progresses.
    if (this.door) {
      const target = 4.3 + this.p.doorOpen * 8.4;
      this.door.position.y += (target - this.door.position.y) * Math.min(1, dt * 2);
    }
  }

  /** Flat white floor: the walk mode can stand anywhere. */
  sampleGround(_x: number, _z: number): { height: number; normal: Vector3 } {
    return { height: 0, normal: new Vector3(0, 1, 0) };
  }

  /** Where the portal is, so the app can tell when you have stepped through. */
  portalPosition(): Vector3 {
    return this.portal ? this.portal.position.clone() : new Vector3(0, 3, 18);
  }

  getParams(): WorldParam[] {
    return [
      { key: 'brightness', label: 'Room Light', min: 0.2, max: 2, step: 0.05,
        value: this.p.brightness },
      { key: 'doorOpen', label: 'Door', min: 0, max: 1, step: 0.01,
        value: this.p.doorOpen }
    ];
  }

  setParam(key: string, value: number): void {
    if (key === 'brightness') {
      this.p.brightness = value;
      const l = this.scene?.lights?.[0];
      if (l) l.intensity = 1.25 * value;
    } else if (key === 'doorOpen') {
      this.p.doorOpen = value;
    }
  }

  getActions(): WorldAction[] {
    return [
      { key: 'door:open', label: 'Open Door', glyph: '⌸' },
      { key: 'intro:skip', label: 'Skip Intro', glyph: '»' }
    ];
  }

  runAction(key: string, _ctx: WorldContext): void {
    if (key === 'door:open') this.p.doorOpen = 1;
  }

  getStats(): Record<string, string> {
    return {
      'Room': 'Garage (unbounded)',
      'People present': String(this.speakers.length)
    };
  }

  dispose(): void {
    this.meshes.forEach((m) => m.dispose());
    this.mats.forEach((m) => m.dispose());
    this.meshes = [];
    this.mats = [];
    this.speakers = [];
    if (this.scene) this.scene.fogMode = Scene.FOGMODE_NONE;
  }
}
