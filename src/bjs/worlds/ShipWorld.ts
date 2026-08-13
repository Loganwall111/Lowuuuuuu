/**
 * ShipWorld — the ship that *is* the main menu.
 *
 * There is no menu screen. You stand on a deck, and the things a menu would
 * have listed are objects in the room: a launch console, a reseed pillar, a
 * graphics terminal. Walk up to one and use it. You can jump on them.
 *
 * Through the windows is the universe you are about to enter, so the ship
 * never feels like a loading screen with a floor.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { starfieldTexture } from '../Textures';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { SHIP_STATIONS, ShipStation } from '../systems/IntroSequence';

interface Console3D {
  station: ShipStation;
  plinth: Mesh;
  panel: Mesh;
  mat: StandardMaterial;
  /** Set while the player is close enough to use it. */
  near: boolean;
}

const DECK_RADIUS = 13;
const DECK_Y = 0;

export class ShipWorld implements World {
  id = 'ship';
  name = 'Ship';

  private scene!: Scene;
  private t = 0;
  private meshes: Mesh[] = [];
  private mats: StandardMaterial[] = [];
  private consoles: Console3D[] = [];
  private p = { lights: 1.0, viewport: 1.0 };
  /** Set by the app each frame so consoles can light up as you approach. */
  private playerPos = new Vector3(0, 1.7, 0);

  async build(ctx: WorldContext): Promise<void> {
    const scene = ctx.scene;
    this.scene = scene;
    scene.clearColor = new Color4(0.006, 0.010, 0.022, 1);

    const amb = new HemisphericLight('shipAmb', new Vector3(0, 1, 0), scene);
    amb.intensity = 0.55;
    amb.diffuse = new Color3(0.72, 0.80, 0.95);
    amb.groundColor = new Color3(0.14, 0.16, 0.24);

    // ---- starfield outside ----
    const sky = MeshBuilder.CreateSphere('shipSky',
      { diameter: 1600, segments: 24, sideOrientation: 1 }, scene);
    const sm = new StandardMaterial('shipSkyM', scene);
    sm.emissiveTexture = starfieldTexture(scene);
    sm.diffuseColor = Color3.Black();
    sm.specularColor = Color3.Black();
    sm.disableLighting = true;
    sm.backFaceCulling = false;
    sky.material = sm;
    sky.infiniteDistance = true;
    sky.isPickable = false;
    this.meshes.push(sky);
    this.mats.push(sm);

    // ---- deck ----
    const deck = MeshBuilder.CreateCylinder('shipDeck',
      { diameter: DECK_RADIUS * 2, height: 0.4, tessellation: 48 }, scene);
    deck.position.y = DECK_Y - 0.2;
    const dm = new StandardMaterial('shipDeckM', scene);
    dm.diffuseColor = new Color3(0.20, 0.23, 0.29);
    dm.specularColor = new Color3(0.42, 0.46, 0.56);
    dm.specularPower = 72;
    dm.emissiveColor = new Color3(0.045, 0.052, 0.07);
    deck.material = dm;
    this.meshes.push(deck);
    this.mats.push(dm);

    // Inlaid light strips: they give the deck scale and read as a real room.
    for (let i = 0; i < 8; i++) {
      const a = (i / 8) * Math.PI * 2;
      const strip = MeshBuilder.CreateBox('strip_' + i,
        { width: 0.14, height: 0.02, depth: DECK_RADIUS * 0.92 }, scene);
      strip.position.set(
        Math.cos(a) * DECK_RADIUS * 0.5, 0.011, Math.sin(a) * DECK_RADIUS * 0.5);
      strip.rotation.y = -a;
      const smat = new StandardMaterial('stripM_' + i, scene);
      smat.emissiveColor = new Color3(0.24, 0.62, 0.92);
      smat.disableLighting = true;
      strip.material = smat;
      strip.isPickable = false;
      this.meshes.push(strip);
      this.mats.push(smat);
    }

    // ---- hull ring and windows ----
    const hull = MeshBuilder.CreateCylinder('shipHull', {
      diameterTop: DECK_RADIUS * 2.1, diameterBottom: DECK_RADIUS * 2.1,
      height: 6, tessellation: 32, sideOrientation: 1
    }, scene);
    hull.position.y = 3;
    const hm = new StandardMaterial('shipHullM', scene);
    hm.diffuseColor = new Color3(0.16, 0.18, 0.23);
    hm.emissiveColor = new Color3(0.035, 0.04, 0.055);
    hm.backFaceCulling = false;
    hull.material = hm;
    this.meshes.push(hull);
    this.mats.push(hm);

    // Windows: you can see the universe you are about to fly into.
    const glass = new StandardMaterial('shipGlassM', scene);
    glass.diffuseColor = new Color3(0.03, 0.06, 0.11);
    glass.specularColor = new Color3(0.9, 0.95, 1.0);
    glass.specularPower = 200;
    glass.emissiveColor = new Color3(0.04, 0.09, 0.16);
    glass.alpha = 0.22;
    glass.backFaceCulling = false;
    this.mats.push(glass);

    for (let i = 0; i < 10; i++) {
      const a = (i / 10) * Math.PI * 2;
      const win = MeshBuilder.CreateBox('shipWin_' + i,
        { width: 3.4, height: 2.4, depth: 0.12 }, scene);
      win.position.set(
        Math.cos(a) * DECK_RADIUS * 1.04, 2.6, Math.sin(a) * DECK_RADIUS * 1.04);
      win.rotation.y = -a + Math.PI / 2;
      win.material = glass;
      win.isPickable = false;
      this.meshes.push(win);
    }

    // ---- the menu, as furniture ----
    for (const st of SHIP_STATIONS) {
      const [x, , z] = st.position;

      const plinth = MeshBuilder.CreateCylinder('plinth_' + st.id,
        { diameterTop: 1.5, diameterBottom: 1.9, height: 1.0, tessellation: 16 },
        scene);
      plinth.position.set(x, 0.5, z);
      const pm = new StandardMaterial('plinthM_' + st.id, scene);
      pm.diffuseColor = new Color3(0.22, 0.25, 0.32);
      pm.emissiveColor = new Color3(0.05, 0.06, 0.08);
      plinth.material = pm;
      this.meshes.push(plinth);
      this.mats.push(pm);

      // The lit face. This is the "button" - you walk to it, not click it.
      const panel = MeshBuilder.CreatePlane('panel_' + st.id,
        { width: 1.5, height: 0.95 }, scene);
      panel.position.set(x, 1.42, z);
      panel.billboardMode = 7;
      const panelM = new StandardMaterial('panelM_' + st.id, scene);
      panelM.emissiveColor = st.id === 'play'
        ? new Color3(0.30, 0.95, 0.55)
        : new Color3(0.26, 0.60, 0.95);
      panelM.disableLighting = true;
      panelM.alpha = 0.9;
      panelM.backFaceCulling = false;
      panel.material = panelM;
      this.meshes.push(panel);
      this.mats.push(panelM);

      const lamp = new PointLight('plampr_' + st.id,
        new Vector3(x, 1.8, z), scene);
      lamp.diffuse = st.id === 'play'
        ? new Color3(0.3, 1.0, 0.55) : new Color3(0.3, 0.6, 1.0);
      lamp.intensity = 0.34;
      lamp.range = 9;

      this.consoles.push({ station: st, plinth, panel, mat: panelM, near: false });
    }

    // A raised platform in the middle you can jump onto, because the brief
    // was that you should be able to jump in front of the Play button.
    const dais = MeshBuilder.CreateCylinder('shipDais',
      { diameter: 4.2, height: 0.7, tessellation: 32 }, scene);
    dais.position.set(0, 0.35, 1.4);
    const daisM = new StandardMaterial('shipDaisM', scene);
    daisM.diffuseColor = new Color3(0.26, 0.30, 0.38);
    daisM.emissiveColor = new Color3(0.06, 0.07, 0.09);
    dais.material = daisM;
    this.meshes.push(dais);
    this.mats.push(daisM);

    ctx.setCameraTarget(new Vector3(0, 1.8, 0), 16);
  }

  update(dt: number, ctx: WorldContext): void {
    this.t += dt;

    const eye = ctx.camera?.position ?? this.playerPos;

    // Consoles brighten as you approach, so proximity is legible without
    // a crosshair or a tooltip.
    for (const c of this.consoles) {
      const [x, , z] = c.station.position;
      const d = Math.hypot(eye.x - x, eye.z - z);
      const near = d < 3.4;
      c.near = near;

      const pulse = 0.72 + Math.sin(this.t * 2 + x + z) * 0.1;
      const boost = near ? 1.5 : 1.0;
      const base = c.station.id === 'play'
        ? new Color3(0.30, 0.95, 0.55)
        : new Color3(0.26, 0.60, 0.95);
      c.mat.emissiveColor = base.scale(pulse * boost * this.p.lights);
      c.plinth.position.y = 0.5 + (near ? Math.sin(this.t * 4) * 0.02 : 0);
    }
  }

  /** Deck and dais heights, so walking and jumping work aboard. */
  sampleGround(x: number, z: number): { height: number; normal: Vector3 } {
    const up = new Vector3(0, 1, 0);
    // The central dais is a step up you can jump onto.
    if (Math.hypot(x - 0, z - 1.4) < 2.1) return { height: 0.7, normal: up };
    for (const c of this.consoles) {
      const [px, , pz] = c.station.position;
      if (Math.hypot(x - px, z - pz) < 0.95) return { height: 1.0, normal: up };
    }
    if (Math.hypot(x, z) <= DECK_RADIUS) return { height: DECK_Y, normal: up };
    // Off the edge: no floor. The app keeps you aboard.
    return { height: DECK_Y, normal: up };
  }

  /** The station the player is standing at, if any. */
  activeStation(): ShipStation | null {
    const c = this.consoles.find((k) => k.near);
    return c ? c.station : null;
  }

  getParams(): WorldParam[] {
    return [
      { key: 'lights', label: 'Cabin Lights', min: 0.2, max: 2, step: 0.05,
        value: this.p.lights },
      { key: 'viewport', label: 'Window Clarity', min: 0, max: 1, step: 0.01,
        value: this.p.viewport }
    ];
  }

  setParam(key: string, value: number): void {
    if (key === 'lights') this.p.lights = value;
    else if (key === 'viewport') this.p.viewport = value;
  }

  getActions(): WorldAction[] {
    return SHIP_STATIONS.map((s) => ({
      key: 'ship:' + s.id, label: s.label, glyph: s.glyph
    }));
  }

  runAction(_key: string, _ctx: WorldContext): void {
    // The app owns what each station does; the ship only presents them.
  }

  getStats(): Record<string, string> {
    const a = this.activeStation();
    return {
      'Location': 'Ship - main deck',
      'Consoles': String(this.consoles.length),
      'At console': a ? a.label : 'none'
    };
  }

  dispose(): void {
    this.meshes.forEach((m) => m.dispose());
    this.mats.forEach((m) => m.dispose());
    this.meshes = [];
    this.mats = [];
    this.consoles = [];
  }
}
