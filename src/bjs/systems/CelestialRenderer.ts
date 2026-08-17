/**
 * CelestialRenderer — draws the celestial catalog.
 *
 * The catalog produces up to a few hundred bodies in view at once. Giving
 * each one its own mesh and material would be a few hundred draw calls and
 * a few hundred materials, which is exactly how a space game ends up at 20
 * fps looking at nothing.
 *
 * So bodies are drawn as THIN INSTANCES of one shared sphere, with per
 * instance colour supplied through an instanced vertex buffer. That is one
 * draw call for the entire field however many bodies are in it. The shader
 * is a small custom one rather than StandardMaterial because these are
 * emissive bodies seen across enormous distances - they need a limb
 * darkening falloff and an additive corona, and they must not be affected
 * by scene lighting or fog.
 *
 * STREAMING. Only cells near the camera are realised, and the set is
 * rebuilt when the camera has moved far enough to change which cells are
 * in range. Rebuilding every frame would defeat the point of a lattice
 * that is cheap to sample.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import type { Scene } from '@babylonjs/core/scene';
import {
  bodiesNear, DEFAULT_FIELD, type CelestialBody, type FieldOptions
} from './CelestialCatalog';

export const CELESTIAL_EFFECT = 'celestialBody';

/**
 * The body shader.
 *
 * Limb darkening plus a rim corona. Both are functions of the angle
 * between the surface normal and the eye, which is what makes a sphere
 * read as a luminous BODY rather than as a flat lit ball - the edge of a
 * star is dimmer than its centre, and the atmosphere above the edge glows.
 *
 * No time uniform anywhere: nothing here pulses.
 */
export const CELESTIAL_VERT = `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
attribute vec4 bodyColor;
uniform mat4 viewProjection;
// world is supplied exactly once by <instancesDeclaration>. Declaring it
// here as well is a WebGL2 redefinition error when instancing is disabled.
// Supplied explicitly rather than relying on an engine-injected
// vEyePosition, which is only defined for the built-in material paths.
uniform vec3 eyePos;
varying vec3 vNormal;
varying vec3 vView;
varying vec4 vColor;
#include<instancesDeclaration>
void main(void){
  #include<instancesVertex>
  vec4 wp = finalWorld * vec4(position, 1.0);
  vNormal = normalize((finalWorld * vec4(normal, 0.0)).xyz);
  vView = normalize(wp.xyz - eyePos);
  vColor = bodyColor;
  gl_Position = viewProjection * wp;
}
`;

export const CELESTIAL_FRAG = `
precision highp float;
varying vec3 vNormal;
varying vec3 vView;
varying vec4 vColor;

void main(void){
  // How edge-on this fragment is. 1 at the centre of the disc, 0 at the rim.
  float facing = clamp(dot(vNormal, -vView), 0.0, 1.0);

  // Limb darkening. The classic Eddington approximation: a real star's
  // edge is about 40% as bright as its centre, which is most of what
  // makes it look like a sphere of gas rather than a painted circle.
  float limb = 0.4 + 0.6 * pow(facing, 0.55);

  // Rim corona: light scattered just above the limb, brightest exactly
  // where the surface turns away.
  float rim = pow(1.0 - facing, 2.6);

  vec3 body = vColor.rgb * limb;
  vec3 corona = vColor.rgb * rim * 1.35;

  // Brightness rides in alpha, so one buffer carries colour and intensity.
  vec3 col = (body + corona) * vColor.a;

  // Filmic toe. Without it, faint bodies lift into a uniform grey haze
  // instead of fading properly into the dark.
  float l = max(max(col.r, col.g), col.b);
  col *= l * l / (l * l + 0.0025);

  gl_FragColor = vec4(col, 1.0);
}
`;

export function registerCelestialShader(): void {
  Effect.ShadersStore[CELESTIAL_EFFECT + 'VertexShader'] = CELESTIAL_VERT;
  Effect.ShadersStore[CELESTIAL_EFFECT + 'FragmentShader'] = CELESTIAL_FRAG;
}

export interface CelestialRenderOptions {
  /** How far bodies are drawn, world units. */
  range: number;
  /** Rebuild once the camera has moved this far. */
  rebuildAfter: number;
  /** Hard cap on instances, so a dense pocket cannot stall a frame. */
  maxBodies: number;
}

export const DEFAULT_CELESTIAL_RENDER: CelestialRenderOptions = {
  range: 42000,
  rebuildAfter: 2200,
  maxBodies: 420
};

export class CelestialRenderer {
  opts: CelestialRenderOptions;
  field: FieldOptions;
  private scene: Scene | null = null;
  private mesh: Mesh | null = null;
  private mat: ShaderMaterial | null = null;
  private lastBuild = new Vector3(1e12, 1e12, 1e12);
  private on = true;
  /** Bodies currently realised. */
  live: CelestialBody[] = [];

  constructor(
    opts: Partial<CelestialRenderOptions> = {},
    field: Partial<FieldOptions> = {}
  ) {
    this.opts = { ...DEFAULT_CELESTIAL_RENDER, ...opts };
    this.field = { ...DEFAULT_FIELD, ...field };
  }

  get count(): number { return this.live.length; }
  setEnabled(v: boolean): void {
    this.on = v;
    if (this.mesh) this.mesh.setEnabled(v);
  }

  attach(scene: Scene): void {
    if (this.mesh) return;
    this.scene = scene;
    registerCelestialShader();

    // Low segment count on purpose: these are almost always small on
    // screen, and 16x16 is indistinguishable from 64x64 at that size while
    // costing a sixteenth of the vertices.
    const m = MeshBuilder.CreateSphere('celestialBody', {
      diameter: 2, segments: 16
    }, scene);

    const mat = new ShaderMaterial(CELESTIAL_EFFECT, scene, CELESTIAL_EFFECT, {
      attributes: ['position', 'normal', 'bodyColor'],
      uniforms: ['world', 'viewProjection', 'eyePos'],
      needAlphaBlending: false
    });
    // Emissive bodies own their own brightness; scene fog and lights must
    // not touch them or a distant star fades into the fog colour.
    mat.backFaceCulling = true;
    mat.fogEnabled = false;

    m.material = mat;
    m.isPickable = false;
    m.alwaysSelectAsActiveMesh = true;
    m.renderingGroupId = 0;
    m.setEnabled(this.on);

    this.mesh = m;
    this.mat = mat;
  }

  /**
   * Rebuilds the instance set if the camera has moved far enough.
   *
   * Returns true if a rebuild actually happened, which the tests use to
   * confirm the streaming threshold does what it claims.
   */
  update(eye: Vector3): boolean {
    if (!this.mesh || !this.on) return false;
    // The eye moves every frame even when the instance set does not, so
    // this is written unconditionally, before the rebuild early-out.
    this.mat?.setVector3('eyePos', eye);
    if (Vector3.Distance(eye, this.lastBuild) < this.opts.rebuildAfter) {
      return false;
    }
    this.lastBuild.copyFrom(eye);

    let found = bodiesNear(eye.x, eye.y, eye.z, this.opts.range, this.field);
    if (found.length > this.opts.maxBodies) {
      // Keep the nearest: those are the ones with screen area.
      found = found
        .map((b) => ({
          b,
          d: (b.x - eye.x) ** 2 + (b.y - eye.y) ** 2 + (b.z - eye.z) ** 2
        }))
        .sort((p, q) => p.d - q.d)
        .slice(0, this.opts.maxBodies)
        .map((p) => p.b);
    }
    this.live = found;

    const n = found.length;
    if (n === 0) {
      this.mesh.thinInstanceCount = 0;
      return true;
    }

    const matrices = new Float32Array(n * 16);
    const colors = new Float32Array(n * 4);
    const q = Quaternion.Identity();
    const scale = new Vector3(1, 1, 1);
    const pos = new Vector3();
    const tmp = Matrix.Identity();

    for (let i = 0; i < n; i++) {
      const b = found[i];
      scale.set(b.radius, b.radius, b.radius);
      pos.set(b.x, b.y, b.z);
      Matrix.ComposeToRef(scale, q, pos, tmp);
      tmp.copyToArray(matrices, i * 16);
      colors[i * 4 + 0] = b.tint[0];
      colors[i * 4 + 1] = b.tint[1];
      colors[i * 4 + 2] = b.tint[2];
      // Alpha carries luminosity * per-body brightness, so the shader
      // needs only one extra attribute rather than two.
      colors[i * 4 + 3] = b.spec.luminosity * b.brightness;
    }

    this.mesh.thinInstanceSetBuffer('matrix', matrices, 16, true);
    this.mesh.thinInstanceSetBuffer('bodyColor', colors, 4, true);
    return true;
  }

  stats(): Record<string, string> {
    return { 'Celestials': this.on ? String(this.live.length) : 'off' };
  }

  dispose(): void {
    this.mesh?.dispose();
    this.mat?.dispose();
    this.mesh = null;
    this.mat = null;
    this.live = [];
  }
}
