/**
 * BlackHoleWorld — Schwarzschild ray-marched black hole.
 *
 * Real gravitational lensing: photon paths are integrated in the equatorial
 * plane using the geodesic ODE  d2u/dphi2 = -u + 3/2 * rs * u^2  (u = 1/r).
 * Background stars, the accretion disk and companion bodies are all sampled
 * along the bent path, so light genuinely wraps around the horizon and the
 * far side of the disk appears above and below it (the Einstein ring).
 */

import { Scene } from '@babylonjs/core/scene';
import { Vector3, Matrix } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { GLSL_NOISE } from '../Noise';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';
import { rollAnomaly, ANOMALY_COVER, STANDARD_COVER } from '../systems/BlackHoleBody';
import { safeFloat, safeAspect } from '../SafeUniforms';
import {
  LENS_PROFILES, LENS_ORDER, LENS_MODE_ID, LENS_FIELDS, cloneProfile,
  sanitizeProfile, randomAlienProfile, describeProfile,
  type LensProfile, type LensMode
} from '../systems/LensProfiles';
import {
  BLACK_HOLES, HOLE_ORDER, horizonRadius, iscoRadius, photonSphere,
  deflectionScale, describeHole, type HoleKind
} from '../systems/BlackHoleTypes';

const FRAG = `
precision highp float;
varying vec2 vUV;

uniform vec3  camPos;
uniform mat4  camInv;
uniform float fov;
uniform float aspect;
uniform float time;

uniform float rs;          // Schwarzschild radius
uniform float spin;        // disk rotation speed
uniform float diskInner;
// How far the black horizon extends relative to the disk's inner edge.
// 1.0+ masks the inner disc completely (standard). Below 1.0 pulls the
// shadow back to the centre and exposes the Moire interference pattern
// that the disk sampling produces there (the "fractured" anomaly).
uniform float horizonCover;
uniform float diskOuter;
uniform float diskTilt;
uniform float exposure;
uniform float lensStrength;
uniform float diskBright;
uniform float dopplerAmt;

// ---- lens profile: every hole bends light its own way ----
uniform float lensMode;       // see LENS_MODE_ID
uniform float lensFalloff;
uniform float ringAmt;        // 0 = no photon ring at all
uniform float ringRadius;
uniform float lensSymmetry;   // 0 = radial, 6 = six-fold, ...
uniform float lensDistortion;
uniform float lensTwist;
uniform float lensChroma;
uniform vec3  lensTint;
uniform float lensSoftness;

// ---- interior view: looking back out from inside the horizon ----
uniform float insideAmt;      // 0 = outside, 1 = fully inside
uniform vec3  exitDir;        // direction back toward where you came from

${GLSL_NOISE}

// ---- starfield sampled in a direction ----
vec3 stars(vec3 dir){
  vec3 c = vec3(0.0);
  // three octaves of sparse points
  for (int k = 0; k < 3; k++){
    float sc = 90.0 + float(k) * 140.0;
    vec3 p = dir * sc;
    vec3 ip = floor(p);
    vec3 fp = fract(p);
    for (int i = -1; i <= 1; i++)
    for (int j = -1; j <= 1; j++)
    for (int l = -1; l <= 1; l++){
      vec3 o = vec3(float(i), float(j), float(l));
      vec3 h = hash33(ip + o) * 0.5 + 0.5;
      vec3 pos = o + h;
      float d = length(fp - pos);
      float bright = pow(max(0.0, 1.0 - d * 2.4), 22.0);
      // colour by pseudo stellar class
      float t = fract(h.x * 7.3);
      vec3 tint = t < 0.7 ? mix(vec3(0.75,0.85,1.0), vec3(1.0), t)
                          : mix(vec3(1.0,0.82,0.55), vec3(1.0,0.6,0.4), t);
      c += tint * bright * (0.35 + h.z * 0.9);
    }
  }
  // faint nebula wash so the void isn't flat black
  float n = fbm(dir * 2.4 + 11.0, 5, 2.1, 0.55) * 0.5 + 0.5;
  float n2 = fbm(dir * 5.1 - 4.0, 4, 2.3, 0.5) * 0.5 + 0.5;
  vec3 neb = mix(vec3(0.03,0.05,0.12), vec3(0.16,0.05,0.18), n) * pow(n2, 2.5) * 0.9;
  return c + neb;
}

// ---- accretion disk emission at radius r, angle a ----
vec3 diskColor(float r, float a, out float alpha){
  float t = clamp((r - diskInner) / max(diskOuter - diskInner, 0.001), 0.0, 1.0);
  // turbulent spiral structure
  float sw = a * 2.0 - time * spin * 1.4 + r * 0.7;
  float n  = fbm(vec3(cos(sw) * r * 0.28, sin(sw) * r * 0.28, time * 0.12), 5, 2.2, 0.55) * 0.5 + 0.5;
  float n2 = fbm(vec3(cos(sw) * r * 0.9,  sin(sw) * r * 0.9,  time * 0.2), 4, 2.4, 0.5) * 0.5 + 0.5;
  float dens = pow(n, 1.6) * (0.55 + n2 * 0.85);
  // temperature: hot inside, cool outside
  float heat = pow(1.0 - t, 2.2);
  vec3 hot  = vec3(1.0, 0.98, 0.94);
  vec3 mid  = vec3(1.0, 0.62, 0.22);
  vec3 cool = vec3(0.72, 0.16, 0.04);
  vec3 col = mix(cool, mid, heat);
  col = mix(col, hot, pow(heat, 2.6));
  // soft inner/outer edges
  float edge = smoothstep(0.0, 0.10, t) * (1.0 - smoothstep(0.82, 1.0, t));
  alpha = clamp(dens * edge * 1.5, 0.0, 1.0);
  return col * dens * diskBright;
}


// ---- angular modulation: what makes an alien lens look alien ----
// Returns a multiplier on the deflection for a given azimuth and radius.
float lensShape(float ang, float r){
  float m = 1.0;

  // symmetric folds: hexagonal, shattered, kaleidoscope
  if (lensSymmetry > 0.5){
    float folds = lensSymmetry;
    m += cos(ang * folds) * lensDistortion;
  }

  // concentric shells
  if (lensMode > 8.5 && lensMode < 9.5){
    m += sin(r * 2.6) * lensDistortion;
  }

  // spiral drag winds the modulation around the axis
  if (abs(lensTwist) > 0.001){
    m += sin(ang + lensTwist / max(r, 0.35)) * lensDistortion * 0.6;
  }

  return max(0.02, m);
}

// Deflection per step for this profile. The physical case is 1/b, but the
// falloff exponent lets a hole bend light gently and widely, or almost not
// at all until the very edge.
float deflectionAt(float r, float ang){
  float base = 0.0335 * lensStrength;
  // falloff reshapes how quickly the bending fades with distance
  float scale = pow(clamp(rs / max(r, 1e-4), 0.0, 1.0), lensFalloff - 1.0);
  return base * scale * lensShape(ang, r);
}

// ---- defensive helpers ----
// A single NaN anywhere in a ray direction turns the whole frame black and
// reports nothing, so the two operations that can produce one are wrapped.
vec3 safeNormalize(vec3 v, vec3 fallback){
  float l2 = dot(v, v);
  if (!(l2 > 1e-20)) return fallback;   // catches 0 and NaN (NaN fails >)
  return v * inversesqrt(l2);
}
float finiteOr(float v, float fallback){
  // NaN fails every comparison, so this catches NaN and both infinities.
  return (v > -1e30 && v < 1e30) ? v : fallback;
}

void main(void){
  // primary ray
  vec2 uv = vUV * 2.0 - 1.0;
  // A canvas mid-resize yields a NaN aspect on the CPU side; guarded there
  // too, but a bad uniform must never be able to blank the screen.
  uv.x *= finiteOr(aspect, 1.7777);
  vec3 rayL = safeNormalize(vec3(uv * tan(finiteOr(fov, 0.9) * 0.5), 1.0),
                            vec3(0.0, 0.0, 1.0));
  vec3 dir  = safeNormalize((camInv * vec4(rayL, 0.0)).xyz, vec3(0.0, 0.0, 1.0));
  vec3 ro   = camPos;

  // Work in the plane containing the camera, the ray and the singularity.
  // The camera sitting exactly at the origin makes r0 zero, and ro/r0 then
  // yields NaN in all three components - the reported "camera at (0,0,0)"
  // case. Held off the singularity by a hair instead.
  float r0 = max(length(ro), 1e-4);
  vec3 er = safeNormalize(ro, vec3(0.0, 0.0, 1.0));   // radial unit
  vec3 nrm = cross(er, dir);               // orbital plane normal
  float nl = length(nrm);

  vec3 col = vec3(0.0);
  float transmit = 1.0;

  if (nl < 1e-5){
    // radial plunge — straight in
    col = stars(dir);
  } else {
    nrm /= nl;
    vec3 et = normalize(cross(nrm, er));   // tangential unit

    // disk plane normal (tilted) plus a stable in-plane basis for the
    // azimuth, built with a reference axis that can never be parallel to dn.
    vec3 dn = normalize(vec3(sin(diskTilt), cos(diskTilt), 0.0));
    vec3 ref = abs(dn.z) < 0.9 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
    vec3 dx = normalize(cross(dn, ref));
    vec3 dz = normalize(cross(dn, dx));

    // initial conditions for u(phi)
    float u  = 1.0 / r0;
    // du/dphi from the ray direction
    float dru = dot(dir, er);
    float dtu = dot(dir, et);
    float du = -u * (dru / max(dtu, 1e-4));

    float phi = 0.0;
    // step size: fine near the hole, coarse far away
    const int STEPS = 320;
    // azimuth of this ray around the hole, used by the alien lens shapes
    float rayAng = atan(dot(dir, et), dot(dir, er));
    float dphi = deflectionAt(r0, rayAng);

    vec3 prevPos = ro;
    vec3 prevPrev = ro;
    bool captured = false;

    for (int i = 0; i < STEPS; i++){
      // RK2 on  u'' = -u + 1.5 * rs * u^2
      float k1 = -u + 1.5 * rs * u * u;
      float uMid  = u  + du * dphi * 0.5;
      float duMid = du + k1 * dphi * 0.5;
      float k2 = -uMid + 1.5 * rs * uMid * uMid;
      u  += duMid * dphi;
      du += k2 * dphi;
      phi += dphi;

      if (u <= 0.0) break;              // escaped to infinity
      float r = 1.0 / u;
      // The shadow radius is derived from rs and horizonCover only, so the
      // black region and the disk maths share a single source of truth and
      // cannot drift apart as the camera moves.
      if (r <= rs * (1.02 + lensSoftness) * horizonCover){ captured = true; break; }
      if (r > 900.0) break;

      // position along the bent path
      vec3 p = (er * cos(phi) + et * sin(phi)) * r;

      // ---- disk intersection test (crossing the disk plane) ----
      float d0 = dot(prevPos, dn);
      float d1 = dot(p, dn);
      if (d0 * d1 < 0.0){
        float f = d0 / (d0 - d1);
        vec3 hit = mix(prevPos, p, f);
        float hr = length(hit);
        if (hr > diskInner && hr < diskOuter){
          float ang = atan(dot(hit, dz), dot(hit, dx));
          float a;
          vec3 dc = diskColor(hr, ang, a);

          // relativistic beaming + doppler shift
          vec3 orbit = normalize(cross(dn, hit));
          float vmag = sqrt(max(rs / (2.0 * hr), 0.0));
          vec3 vel = orbit * vmag;
          vec3 toCam = normalize(camPos - hit);
          float beta = dot(vel, -toCam);
          float dop = 1.0 / max(1.0 - beta, 0.05);
          float boost = pow(clamp(dop, 0.2, 4.0), 3.0 * dopplerAmt);
          // blueshift toward white, redshift toward red
          vec3 shift = beta > 0.0
            ? mix(dc, vec3(0.75, 0.88, 1.0) * (dc.r + dc.g + dc.b) * 0.5, clamp(beta * 1.6 * dopplerAmt, 0.0, 0.85))
            : mix(dc, vec3(1.0, 0.35, 0.12) * (dc.r + dc.g + dc.b) * 0.45, clamp(-beta * 1.4 * dopplerAmt, 0.0, 0.8));

          col += transmit * shift * boost * a;
          transmit *= (1.0 - a * 0.92);
          if (transmit < 0.02) break;
        }
      }
      prevPrev = prevPos;
      prevPos = p;

      // widen the step as we escape
      dphi = deflectionAt(r, rayAng + phi) * (1.0 + smoothstep(6.0, 60.0, r) * 3.5);
    }

    if (!captured && transmit > 0.02){
      // The escaping ray's true direction is the tangent to the bent path at
      // its last point, i.e. the difference of the final two samples — not
      // the position vector itself.
      vec3 escape = normalize(prevPos - prevPrev);

      vec3 sky;
      if (lensChroma > 0.001){
        // each wavelength bends slightly differently, so the stars smear
        // into little spectra near the hole
        float sp = lensChroma * 0.06;
        vec3 axis = normalize(cross(escape, er) + vec3(1e-5));
        vec3 rDir = normalize(escape + axis * sp);
        vec3 bDir = normalize(escape - axis * sp);
        sky = vec3(stars(rDir).r, stars(escape).g, stars(bDir).b);
      } else {
        sky = stars(escape);
      }
      col += transmit * sky * lensTint;
    }

    // ---- photon ring. Some holes simply do not have one. ----
    if (ringAmt > 0.001 && ringRadius > 0.001){
      // how close this ray passed to the ring radius
      float rr = rs * ringRadius;
      float closest = 1.0 / max(u, 1e-5);
      float band = exp(-pow((closest - rr) / max(rr * 0.16, 1e-3), 2.0));
      col += lensTint * band * ringAmt * 0.85 * lensShape(rayAng, rr);
    }

    // ---- looking back out from inside the horizon ----
    // Once inside, the outside universe collapses into a shrinking window in
    // the direction you fell from. This is what lets you look back at where
    // you were.
    if (insideAmt > 0.001){
      float toExit = dot(dir, normalize(exitDir));
      // the window narrows as you fall deeper
      float aperture = mix(0.35, 0.985, clamp(insideAmt, 0.0, 1.0));
      float w = smoothstep(aperture, aperture + 0.06, toExit);
      vec3 outside = stars(dir) * 1.4 + lensTint * 0.25;
      col = mix(col, outside, w * clamp(insideAmt, 0.0, 1.0));
      // everything else darkens toward the singularity, but never to pure black
      col = mix(col, col * 0.35 + lensTint * 0.03, clamp(insideAmt, 0.0, 1.0) * (1.0 - w));
    }
  }

  // photon ring bloom right outside the shadow
  col = col * exposure;

  // filmic tonemap
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  col = pow(clamp(col, 0.0, 1.0), vec3(1.0 / 2.2));
  gl_FragColor = vec4(col, 1.0);
}
`;

const VERT = `
precision highp float;
attribute vec3 position;
attribute vec2 uv;
varying vec2 vUV;
void main(void){
  vUV = uv;
  gl_Position = vec4(position.xy, 0.0, 1.0);
}
`;

export class BlackHoleWorld implements World {
  id = 'blackhole';
  name = 'Singularity';
  private quad!: Mesh;
  private mat!: ShaderMaterial;
  private t = 0;

  /** The currently selected variety; drives every derived quantity. */
  private kind: HoleKind = 'schwarzschild';

  /** This hole's own lens profile. Fully editable per hole. */
  lens: LensProfile = cloneProfile(LENS_PROFILES.schwarzschild);

  /** 0 outside the horizon, 1 deep inside. Drives the look-back view. */
  /**
   * Rare "fractured" horizons pull the shadow back to the dead centre and
   * expose the geometric interference pattern around it. Rolled once per
   * instance from a seed so a given hole never changes character.
   */
  isAnomaly = false;
  /** Seed for the anomaly roll. Set before build() to pin the outcome. */
  anomalySeed = 0x5f3a11;
  private inside = 0;
  private exitDirection = new Vector3(0, 0, -1);

  private p = {
    mass: 1.0,
    spin: 1.0,
    diskInner: 3.2,
    diskOuter: 16.0,
    diskTilt: 0.42,
    exposure: 1.15,
    lens: 1.0,
    diskBright: 1.25,
    doppler: 1.0
  };

  async build(ctx: WorldContext): Promise<void> {
    // Rolled from a per-instance seed rather than Math.random() so the hole
    // is the same kind every time you come back to it - a rare find that
    // reshuffled when you looked away would mean nothing.
    this.isAnomaly = rollAnomaly(this.anomalySeed);

    const scene = ctx.scene;
    scene.clearColor = new Color4(0, 0, 0, 1);

    Effect.ShadersStore['bhVertexShader'] = VERT;
    Effect.ShadersStore['bhFragmentShader'] = FRAG;

    this.mat = new ShaderMaterial('bh', scene, 'bh', {
      attributes: ['position', 'uv'],
      uniforms: [
        'camPos', 'camInv', 'fov', 'aspect', 'time', 'rs', 'spin',
        'lensMode', 'lensFalloff', 'ringAmt', 'ringRadius', 'lensSymmetry',
        'lensDistortion', 'lensTwist', 'lensChroma', 'lensTint', 'lensSoftness',
        'insideAmt', 'exitDir',
        'diskInner', 'diskOuter', 'diskTilt', 'exposure', 'lensStrength', 'horizonCover',
        'diskBright', 'dopplerAmt'
      ]
    });
    this.mat.backFaceCulling = false;
    this.mat.depthFunction = 519; // ALWAYS

    this.quad = MeshBuilder.CreatePlane('bhQuad', { size: 2 }, scene);
    this.quad.material = this.mat;
    this.quad.infiniteDistance = true;
    this.quad.isPickable = false;
    this.quad.alwaysSelectAsActiveMesh = true;
    this.quad.freezeWorldMatrix();

    ctx.setCameraTarget(Vector3.Zero(), 26);
  }

  update(dt: number, ctx: WorldContext): void {
    this.t += dt;
    const cam = ctx.camera;
    const scene = ctx.scene;

    const inv = Matrix.Invert(cam.getViewMatrix());
    this.mat.setVector3('camPos', cam.position);
    this.mat.setMatrix('camInv', inv);
    this.mat.setFloat('fov', safeFloat(cam.fov ?? 0.9, 0.9));
    // ROOT CAUSE OF THE BLACK SCREEN. A canvas mid-resize has zero height,
    // so getAspectRatio() returns 0/0 = NaN. The shader does `uv.x *=
    // aspect`, so one NaN frame makes every ray direction NaN and nothing
    // is drawn at all. Opening a panel resizes the canvas, which is why
    // the report was "open the options menu and the screen goes black".
    const eng = scene.getEngine();
    this.mat.setFloat('aspect',
      safeAspect(eng.getRenderWidth(), eng.getRenderHeight()));
    this.mat.setFloat('time', this.t);

    // Mass drives the horizon radius, and a zero or negative rs makes the
    // capture test meaningless. Floored rather than merely finite-checked.
    const rs = Math.max(1e-3, safeFloat(1.0 * this.p.mass, 1.0));
    this.mat.setFloat('rs', rs);

    // ---- per-hole lens profile ----
    const L = this.lens;
    this.mat.setFloat('lensMode', LENS_MODE_ID[L.mode] ?? 0);
    this.mat.setFloat('lensFalloff', L.falloff);
    this.mat.setFloat('ringAmt', L.ring);
    this.mat.setFloat('ringRadius', L.ringRadius);
    this.mat.setFloat('lensSymmetry', L.symmetry);
    this.mat.setFloat('lensDistortion', L.distortion);
    this.mat.setFloat('lensTwist', L.twist);
    this.mat.setFloat('lensChroma', L.chroma);
    this.mat.setColor3('lensTint', new Color3(L.tint[0], L.tint[1], L.tint[2]));
    this.mat.setFloat('lensSoftness', L.softness);
    this.mat.setFloat('insideAmt', this.inside);
    this.mat.setVector3('exitDir', this.exitDirection);
    this.mat.setFloat('spin', this.p.spin);
    this.mat.setFloat('horizonCover',
      safeFloat(this.isAnomaly ? ANOMALY_COVER : STANDARD_COVER, STANDARD_COVER));
    // The disk needs a non-zero width or `(r - inner) / (outer - inner)`
    // divides by zero inside the shader.
    const dIn = Math.max(1e-3, safeFloat(this.p.diskInner * this.p.mass, 3.2));
    this.mat.setFloat('diskInner', dIn);
    this.mat.setFloat('diskOuter',
      Math.max(dIn + 1e-3, safeFloat(this.p.diskOuter * this.p.mass, 16)));
    this.mat.setFloat('diskTilt', this.p.diskTilt);
    this.mat.setFloat('exposure', this.p.exposure);
    this.mat.setFloat('lensStrength', this.p.lens);
    this.mat.setFloat('diskBright', this.p.diskBright);
    this.mat.setFloat('dopplerAmt', this.p.doppler);
  }

  getParams(): WorldParam[] {
    return [
      { key: 'mass', label: 'Black Hole Mass', min: 0.3, max: 3.5, step: 0.05, value: this.p.mass, unit: '×M☉' },
      { key: 'lens', label: 'Lensing Precision', min: 0.5, max: 2.0, step: 0.05, value: this.p.lens },
      { key: 'spin', label: 'Disk Rotation', min: 0, max: 3, step: 0.05, value: this.p.spin },
      { key: 'diskInner', label: 'Disk Inner Radius', min: 2.2, max: 8, step: 0.1, value: this.p.diskInner },
      { key: 'diskOuter', label: 'Disk Outer Radius', min: 8, max: 34, step: 0.5, value: this.p.diskOuter },
      { key: 'diskTilt', label: 'Disk Inclination', min: 0, max: 1.5, step: 0.02, value: this.p.diskTilt, unit: 'rad' },
      { key: 'diskBright', label: 'Disk Luminosity', min: 0.2, max: 3, step: 0.05, value: this.p.diskBright },
      { key: 'doppler', label: 'Relativistic Beaming', min: 0, max: 2, step: 0.05, value: this.p.doppler },
      { key: 'exposure', label: 'Exposure', min: 0.3, max: 2.5, step: 0.05, value: this.p.exposure }
    ];
  }

  setParam(key: string, value: number): void {
    (this.p as any)[key] = value;
  }

  /**
   * Drives the interior view. When the player crosses a horizon the outside
   * universe collapses into a window in the direction they fell from, so
   * they can look back at where they were.
   */
  setInterior(depth: number, exitDirection: Vector3): void {
    this.inside = Number.isFinite(depth) ? Math.max(0, Math.min(1, depth)) : 0;
    if (exitDirection && exitDirection.lengthSquared() > 1e-9) {
      this.exitDirection.copyFrom(exitDirection.clone().normalize());
    }
  }

  /** Replaces this hole's lens profile wholesale. */
  setLens(profile: LensProfile): void {
    this.lens = sanitizeProfile(profile);
  }

  getActions(): WorldAction[] {
    return [
      ...LENS_ORDER.map((m) => ({
        key: 'lens:' + m,
        label: LENS_PROFILES[m].name,
        glyph: LENS_PROFILES[m].glyph
      })),
      { key: 'lens:random', label: 'Random Alien Lens', glyph: '🎲' },
      // Anomalies are a ~7% find in the wild; this lets you look at one on
      // purpose rather than only ever stumbling across it.
      { key: 'anomaly:toggle',
        label: this.isAnomaly ? 'Heal Horizon' : 'Fracture Horizon', glyph: '💠' },
      { key: 'anomaly:reroll', label: 'Reroll Horizon', glyph: '🔀' },
      ...HOLE_ORDER.map((k) => ({
      key: 'hole:' + k,
      label: BLACK_HOLES[k].name,
      glyph: BLACK_HOLES[k].glyph
    }))
    ];
  }

  runAction(key: string, _ctx: WorldContext): void {
    if (key === 'anomaly:toggle') { this.isAnomaly = !this.isAnomaly; return; }
    if (key === 'anomaly:reroll') {
      this.anomalySeed = (this.anomalySeed * 1664525 + 1013904223) >>> 0;
      this.isAnomaly = rollAnomaly(this.anomalySeed);
      return;
    }
    if (key.startsWith('lens:')) {
      const m = key.slice(5);
      this.lens = m === 'random'
        ? randomAlienProfile()
        : sanitizeProfile(LENS_PROFILES[m as LensMode] ?? LENS_PROFILES.schwarzschild);
      return;
    }
    if (!key.startsWith('hole:')) return;
    const k = key.slice(5) as HoleKind;
    if (!BLACK_HOLES[k]) return;
    this.applyHoleType(k);
  }

  /**
   * Switching type rewrites the physics, not just the label: horizon, ISCO,
   * photon sphere and deflection all come from the type's mass/spin/charge.
   */
  applyHoleType(k: HoleKind): void {
    const t = BLACK_HOLES[k];
    this.kind = k;
    this.p.mass = horizonRadius(t);
    this.p.spin = Math.min(t.spin, 1);
    this.p.lens = deflectionScale(t);
    this.p.diskBright = t.discBrightness;
    this.p.diskInner = Math.max(1.2, iscoRadius(t) / Math.max(horizonRadius(t), 0.001));
    this.p.diskOuter = this.p.diskInner * (4 + t.spin * 2);
    this.p.doppler = 0.35 + t.spin * 1.4;
  }

  currentKind(): HoleKind {
    return this.kind;
  }

  getStats(): Record<string, string> {
    const t = BLACK_HOLES[this.kind];
    return {
      ...describeHole(t),
      ...describeProfile(this.lens),
      'Integrator': 'RK2 geodesic',
      'Deflection': deflectionScale(t).toFixed(2) + '×',
      'Horizon': this.isAnomaly ? 'Fractured — anomaly' : 'Standard',
      'Inside horizon': this.inside > 0
        ? Math.round(this.inside * 100) + '% — look back along your entry path'
        : 'no'
    };
  }

  dispose(): void {
    this.quad?.dispose();
    this.mat?.dispose();
  }
}
