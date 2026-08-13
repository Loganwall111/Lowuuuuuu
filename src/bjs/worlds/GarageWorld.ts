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
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { LESSONS } from '../systems/IntroSequence';

interface Speaker {
  mesh: Mesh;
  head: Mesh;
  visor: Mesh | null;
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
  private doorParts: Mesh[] = [];
  private portal: Mesh | null = null;
  private portalMat: StandardMaterial | null = null;
  private p = { brightness: 1.0, doorOpen: 0 };
  private doorRise = 0;

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    this.scene = scene;

    // A white void. Fog is the same colour as the background, so the floor
    // fades into nothing and the room reads as endless.
    const WHITE = new Color3(0.94, 0.95, 0.97);
    scene.clearColor = new Color4(WHITE.r, WHITE.g, WHITE.b, 1);
    scene.fogMode = Scene.FOGMODE_EXP2;
    scene.fogColor = WHITE;
    scene.fogDensity = 0.0052;

    const amb = new HemisphericLight('garageAmb', new Vector3(0, 1, 0), scene);
    amb.intensity = 1.25;
    amb.diffuse = new Color3(1, 1, 1);
    amb.groundColor = new Color3(0.82, 0.84, 0.9);

    // ---- floor ----
    // A floating slab rather than an endless plane. Fog still dissolves the
    // edge, but the room now reads as a platform suspended in a white void -
    // you can fly off it and look back at it.
    const floor = MeshBuilder.CreateGround('garageFloor',
      { width: 220, height: 220, subdivisions: 2 }, scene);
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
    for (let i = -16; i <= 16; i++) {
      const line = MeshBuilder.CreateBox('grid_' + i,
        { width: 0.05, height: 0.01, depth: 200 }, scene);
      line.position.set(i * 6, 0.01, 0);
      const lm = new StandardMaterial('gridM_' + i, scene);
      lm.emissiveColor = new Color3(0.80, 0.82, 0.87);
      lm.disableLighting = true;
      line.material = lm;
      line.isPickable = false;
      this.meshes.push(line);
      this.mats.push(lm);

      const cross = MeshBuilder.CreateBox('gridx_' + i,
        { width: 200, height: 0.01, depth: 0.05 }, scene);
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

    // A real sectional garage door: horizontal panels that read as a door
    // rather than a white slab. The portal is set INTO it, so the way out is
    // the door itself rather than a hoop standing in an empty room.
    const doorM = new StandardMaterial('doorM', scene);
    const doorTex = new Texture('/art/garage-door.jpg', scene);
    doorTex.uScale = 1;
    doorTex.vScale = 1;
    doorM.diffuseTexture = doorTex;
    doorM.emissiveTexture = doorTex;
    // Emissive is kept low so the panels still catch the room light and read
    // as geometry, but never fall to black.
    doorM.emissiveColor = new Color3(0.34, 0.35, 0.38);
    doorM.specularColor = new Color3(0.22, 0.22, 0.25);
    doorM.specularPower = 48;
    this.mats.push(doorM);

    // The door is built from panels with a gap in the middle for the portal,
    // so the opening is genuinely part of the door.
    const PANEL_H = 1.42;
    const DOOR_W = 5.6;
    const doorParts: Mesh[] = [];
    for (let row = 0; row < 6; row++) {
      const y = 0.72 + row * PANEL_H;
      // Rows 2 and 3 are split either side of the portal opening.
      const isPortalRow = row === 2 || row === 3;
      if (isPortalRow) {
        for (const side of [-1, 1]) {
          const w = (DOOR_W - 3.1) / 2;
          const panel = MeshBuilder.CreateBox('doorPanel_' + row + '_' + side,
            { width: w, height: PANEL_H - 0.05, depth: 0.26 }, scene);
          panel.position.set(side * (DOOR_W / 2 - w / 2), y, 25.7);
          panel.material = doorM;
          doorParts.push(panel);
          this.meshes.push(panel);
        }
      } else {
        const panel = MeshBuilder.CreateBox('doorPanel_' + row,
          { width: DOOR_W, height: PANEL_H - 0.05, depth: 0.26 }, scene);
        panel.position.set(0, y, 25.7);
        panel.material = doorM;
        doorParts.push(panel);
        this.meshes.push(panel);
      }
    }
    // The first panel doubles as the door reference the rest of the world
    // animates and measures against.
    const door = doorParts[0];
    this.door = door;
    this.doorParts = doorParts;

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
      // Real fabric rather than flat plastic. Each person tints the same
      // sheet differently so the cast reads as individuals in uniform.
      const suit = new Texture('/art/npc-suit.jpg', scene);
      suit.uScale = 1;
      suit.vScale = 1;
      bm.diffuseTexture = suit;
      bm.diffuseColor = new Color3(0.72 + i * 0.08, 0.78, 0.92 - i * 0.06);
      bm.emissiveColor = new Color3(0.16, 0.18, 0.24);
      bm.specularColor = new Color3(0.3, 0.32, 0.38);
      bm.specularPower = 64;
      body.material = bm;

      const head = MeshBuilder.CreateSphere('npcHead_' + nm,
        { diameter: 0.52, segments: 12 }, scene);
      head.position.set(x, 1.98, z);
      const hm = new StandardMaterial('npcHeadM_' + nm, scene);
      // Skin tones vary across the cast rather than everyone being identical.
      const tone = [
        new Color3(0.88, 0.74, 0.62), new Color3(0.62, 0.45, 0.33),
        new Color3(0.94, 0.82, 0.72), new Color3(0.45, 0.32, 0.24),
        new Color3(0.78, 0.62, 0.48)
      ][i % 5];
      hm.diffuseColor = tone;
      hm.emissiveColor = tone.scale(0.30);
      hm.specularColor = new Color3(0.18, 0.16, 0.15);
      hm.specularPower = 96;
      head.material = hm;

      // A visor so they read as technicians, not featureless dummies.
      const visor = MeshBuilder.CreateBox('npcVisor_' + nm,
        { width: 0.42, height: 0.14, depth: 0.06 }, scene);
      visor.position.set(x, 2.02, z - 0.24);
      const vm = new StandardMaterial('npcVisorM_' + nm, scene);
      vm.emissiveColor = new Color3(0.35, 0.78, 1.0);
      vm.disableLighting = true;
      visor.material = vm;
      this.meshes.push(visor);
      this.mats.push(vm);

      this.meshes.push(body, head);
      this.mats.push(bm, hm);
      this.speakers.push({
        mesh: body, head, visor, home: new Vector3(x, 0.9, z),
        phase: i * 1.7, name: nm
      });
    });

    // ---- the portal to the ship ----
    const portal = MeshBuilder.CreateTorus('introPortal',
      { diameter: 5.2, thickness: 0.42, tessellation: 48 }, scene);
    // Set into the garage door itself, in the gap left by the panels.
    portal.position.set(0, 3.15, 25.7);
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
    disc.position.set(0, 3.15, 25.55);
    const dmm = new StandardMaterial('introPortalDiscM', scene);
    dmm.emissiveColor = new Color3(0.10, 0.30, 0.55);
    dmm.disableLighting = true;
    dmm.alpha = 0.78;
    dmm.backFaceCulling = false;
    disc.material = dmm;
    this.meshes.push(disc);
    this.mats.push(dmm);

    const glow = new PointLight('portalGlow', new Vector3(0, 3.15, 24.4), scene);
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
      const turn = Math.sin(this.t * 0.6 + s.phase) * 0.5;
      s.head.rotation.y = turn;
      s.mesh.rotation.z = Math.sin(this.t * 0.8 + s.phase) * 0.02;
      // The visor is a separate mesh, so it has to follow the head it
      // belongs to - both the bob and the turn - or it drifts off the face.
      if (s.visor) {
        s.visor.position.y = s.home.y + 1.12 + b;
        s.visor.position.x = s.home.x + Math.sin(turn) * 0.24;
        s.visor.position.z = s.home.z - Math.cos(turn) * 0.24;
        s.visor.rotation.y = turn;
      }
    }

    if (this.portal) {
      this.portal.rotation.z += dt * 0.6;
      const pulse = 0.75 + Math.sin(this.t * 2.2) * 0.25;
      if (this.portalMat) {
        this.portalMat.emissiveColor = new Color3(
          0.28 * pulse, 0.62 * pulse, 1.0 * pulse);
      }
    }

    // The whole sectional door rises as the tutorial progresses. Every panel
    // moves together, keeping its own offset, so it reads as one door rather
    // than a single slab sliding out of a wall.
    if (this.doorParts.length) {
      this.doorRise += (this.p.doorOpen * 9.2 - this.doorRise) * Math.min(1, dt * 2);
      for (const panel of this.doorParts) {
        const base = (panel as any)._baseY as number | undefined;
        const home = base ?? panel.position.y;
        if (base === undefined) (panel as any)._baseY = home;
        panel.position.y = home + this.doorRise;
      }
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
