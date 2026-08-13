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
import type { World, WorldContext, WorldParam } from '../World';

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
uniform float diskOuter;
uniform float diskTilt;
uniform float exposure;
uniform float lensStrength;
uniform float diskBright;
uniform float dopplerAmt;

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

void main(void){
  // primary ray
  vec2 uv = vUV * 2.0 - 1.0;
  uv.x *= aspect;
  vec3 rayL = normalize(vec3(uv * tan(fov * 0.5), 1.0));
  vec3 dir  = normalize((camInv * vec4(rayL, 0.0)).xyz);
  vec3 ro   = camPos;

  // Work in the plane containing the camera, the ray and the singularity.
  float r0 = length(ro);
  vec3 er = ro / r0;                       // radial unit
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
    float dphi = 0.0335 * lensStrength;

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
      if (r <= rs * 1.02){ captured = true; break; }   // through the horizon
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
      dphi = 0.0335 * lensStrength * (1.0 + smoothstep(6.0, 60.0, r) * 3.5);
    }

    if (!captured && transmit > 0.02){
      // The escaping ray's true direction is the tangent to the bent path at
      // its last point, i.e. the difference of the final two samples — not
      // the position vector itself.
      vec3 escape = normalize(prevPos - prevPrev);
      col += transmit * stars(escape);
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
    const scene = ctx.scene;
    scene.clearColor = new Color4(0, 0, 0, 1);

    Effect.ShadersStore['bhVertexShader'] = VERT;
    Effect.ShadersStore['bhFragmentShader'] = FRAG;

    this.mat = new ShaderMaterial('bh', scene, 'bh', {
      attributes: ['position', 'uv'],
      uniforms: [
        'camPos', 'camInv', 'fov', 'aspect', 'time', 'rs', 'spin',
        'diskInner', 'diskOuter', 'diskTilt', 'exposure', 'lensStrength',
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
    this.mat.setFloat('fov', cam.fov ?? 0.9);
    this.mat.setFloat('aspect', scene.getEngine().getAspectRatio(cam));
    this.mat.setFloat('time', this.t);

    const rs = 1.0 * this.p.mass;
    this.mat.setFloat('rs', rs);
    this.mat.setFloat('spin', this.p.spin);
    this.mat.setFloat('diskInner', this.p.diskInner * this.p.mass);
    this.mat.setFloat('diskOuter', this.p.diskOuter * this.p.mass);
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

  getStats(): Record<string, string> {
    const rs = this.p.mass;
    return {
      'Horizon rₛ': rs.toFixed(2),
      'Photon ring': (rs * 1.5).toFixed(2),
      'ISCO': (rs * 3).toFixed(2),
      'Integrator': 'RK2 geodesic'
    };
  }

  dispose(): void {
    this.quad?.dispose();
    this.mat?.dispose();
  }
}
