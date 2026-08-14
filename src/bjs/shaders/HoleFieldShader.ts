/**
 * HoleFieldShader — a black hole you fly past, traced rather than modelled.
 *
 * This is the same physics BlackHoleWorld uses, packaged to run on a single
 * camera-facing quad so any number of holes can exist in the open universe
 * at once.
 *
 * Why a shader and not geometry: a black hole has no surface. What you see
 * is the absence of light in directions where photons fall in, ringed by
 * light that came from BEHIND the hole and was bent around it. A mesh cannot
 * express that - an opaque sphere hides the very lensing that makes a black
 * hole look like one. So the fragment shader integrates the photon path for
 * every pixel and decides whether that ray escapes, hits the disk, or is
 * captured.
 *
 * The disk is a volume, not a plane. Sampling a single plane crossing gives
 * an infinitely thin sheet that vanishes edge-on; here the march accumulates
 * emission through a slab of gas with real vertical thickness.
 */

import { Effect } from '@babylonjs/core/Materials/effect';

export const HOLE_FIELD_SHADER = 'holeField';

const VERT = `
precision highp float;
attribute vec3 position;
attribute vec2 uv;
uniform mat4 worldViewProjection;
uniform mat4 world;
varying vec2 vUV;
varying vec3 vWorld;
void main(void){
  vUV = uv;
  vWorld = (world * vec4(position, 1.0)).xyz;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

const FRAG = `
precision highp float;

varying vec2 vUV;
varying vec3 vWorld;

uniform vec3  camPos;
uniform vec3  holePos;
uniform float time;

uniform float rs;             // horizon radius, world units
uniform float quadRadius;     // half-width of the carrier quad
uniform float horizonCover;   // shadow size relative to rs

uniform float diskInner;      // world units
uniform float diskOuter;      // world units
uniform float diskThickness;  // vertical half-thickness, world units. 0 = none
uniform float diskBright;     // 0 = this hole has NO accretion disk
uniform float diskTilt;
uniform float spin;
uniform float dopplerAmt;
uniform float diskTemp;
uniform float turbulence;

// ---------------------------------------------------------------- noise
float hash13(vec3 p){
  p = fract(p * 0.1031);
  p += dot(p, p.yzx + 33.33);
  return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p){
  vec3 i = floor(p);
  vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float n000 = hash13(i + vec3(0.0, 0.0, 0.0));
  float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
  float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
  float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
  float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
  float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
  float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
  float n111 = hash13(i + vec3(1.0, 1.0, 1.0));
  return mix(
    mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
    mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y), f.z);
}

float fbm3(vec3 p){
  float a = 0.5, s = 0.0;
  for (int i = 0; i < 4; i++){
    s += a * noise3(p);
    p *= 2.03;
    a *= 0.5;
  }
  return s;
}

// ------------------------------------------------------------- disk colour
// Returns emission and writes coverage into alpha.
vec3 diskColor(float r, float ang, float height, out float alpha){
  float t = clamp((r - diskInner) / max(diskOuter - diskInner, 1e-4), 0.0, 1.0);

  // Differential rotation: inner gas laps outer gas, which is what smears
  // the turbulence into spiral filaments instead of static blobs.
  float rr = max(r, diskInner * 0.5);
  float kepler = pow(diskInner / rr, 1.5);
  float swirl = ang + time * spin * kepler * 1.6;

  vec3 q = vec3(cos(swirl) * r, height * 2.0, sin(swirl) * r) * (2.4 / max(diskOuter, 1e-3));
  float n = fbm3(q * 3.0);
  float n2 = fbm3(q * 7.0 + 11.3);

  float dens = mix(1.0, n * 1.6, clamp(turbulence, 0.0, 1.0));
  dens *= 0.35 + 1.65 * clamp(kepler, 0.0, 1.0);
  dens *= 1.0 + 0.35 * kepler * (n2 - 0.5);

  // Hot inside, cool outside, biased by this hole's temperature so an
  // ancient cold disk and a blazar do not share a palette.
  float heat = pow(clamp((1.0 - t) * max(diskTemp, 0.01), 0.0, 1.0), 2.2);
  vec3 hot  = vec3(1.0, 0.98, 0.94);
  vec3 mid  = vec3(1.0, 0.62, 0.22);
  vec3 cool = vec3(0.72, 0.16, 0.04);
  vec3 col = mix(cool, mid, heat);
  col = mix(col, hot, pow(heat, 2.6));

  // Soft ragged edges rather than a hard rim.
  float inner = smoothstep(0.0, 0.12, t);
  float outer = 1.0 - smoothstep(0.55, 1.0, t);
  outer *= 0.55 + 0.45 * n2;
  alpha = clamp(dens * inner * outer * 1.5, 0.0, 1.0);
  return col * dens * diskBright;
}

// ------------------------------------------------------------------ the sky
/**
 * Starfield sampled along a DIRECTION, not a screen position.
 *
 * This is the piece that was missing, and the reason a screen-space warp
 * could never reproduce a real black hole image. A post-process is a
 * function uv -> uv: it rearranges pixels that were already drawn. But the
 * defining features of a lensed sky are light that the flat frame contains
 * no record of - the far side of the disk bent up over the shadow, and
 * Einstein rings, which are SEVERAL images of the SAME star. One output
 * pixel can only take one input pixel, so a warp cannot duplicate a star
 * into a ring no matter how the offsets are shaped.
 *
 * Sampling by direction has no such limit. Two different fragments whose
 * geodesics escape toward the same patch of sky both see that patch, which
 * is exactly what an Einstein ring is.
 */
vec3 skyAlongRay(vec3 dir){
  vec3 d = normalize(dir);

  // Three octaves of cell noise on the direction vector. Cheap, stable
  // under rotation, and dense enough to read as a real field.
  vec3 col = vec3(0.0);
  for (int oct = 0; oct < 3; oct++){
    float scale = 48.0 * pow(2.3, float(oct));
    vec3 p = d * scale;
    vec3 cell = floor(p);
    vec3 f = p - cell;

    // Nearest-feature search over the 8 surrounding cells.
    for (int gx = 0; gx <= 1; gx++){
      for (int gy = 0; gy <= 1; gy++){
        for (int gz = 0; gz <= 1; gz++){
          vec3 g = vec3(float(gx), float(gy), float(gz));
          vec3 id = cell + g;
          float h = hash13(id);
          // Only a fraction of cells hold a star.
          if (h < 0.86) continue;
          vec3 jitter = vec3(hash13(id + 17.1), hash13(id + 39.7), hash13(id + 71.3));
          float dist = length(f - g - (jitter - 0.5) * 0.85);
          float star = exp(-dist * dist * 210.0);
          // Cool stars dominate, as in any real sky.
          float temp = hash13(id + 5.5);
          vec3 tint = temp < 0.74
            ? vec3(1.0, 0.72 + temp * 0.3, 0.55 + temp * 0.4)
            : vec3(0.72, 0.84, 1.0);
          col += tint * star * (0.35 + h * 0.65) / (1.0 + float(oct) * 1.6);
        }
      }
    }
  }

  // A faint galactic band so the lensed sky has large-scale structure to
  // distort. Without it the warp has nothing but points to act on and the
  // wrapping is much harder to read.
  float band = exp(-pow(d.y * 3.1, 2.0));
  vec3 gas = vec3(0.16, 0.15, 0.26) * band * 0.5;
  gas += vec3(0.26, 0.17, 0.12) * band * noise3(d * 7.0) * 0.55;

  return col * 1.5 + gas;
}

void main(void){
  // Ray from the eye through this fragment of the quad.
  vec3 ro = camPos - holePos;
  vec3 rd = normalize(vWorld - camPos);

  float r0 = max(length(ro), 1e-4);
  vec3 er = normalize(ro);
  vec3 nrm = cross(er, rd);
  float nl = length(nrm);

  vec3 col = vec3(0.0);
  float transmit = 1.0;
  bool captured = false;
  // Where this fragment's light ultimately comes from, and how far the
  // geodesic swung to get there.
  vec3 escapeDir = rd;
  float totalBend = 0.0;

  // Disk basis.
  vec3 dn = normalize(vec3(sin(diskTilt), cos(diskTilt), 0.0));
  vec3 ref = abs(dn.z) < 0.9 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
  vec3 dx = normalize(cross(dn, ref));
  vec3 dz = normalize(cross(dn, dx));

  if (nl < 1e-5){
    // Exactly radial: straight in, no orbital plane to work with.
    captured = true;
  } else {
    nrm /= nl;
    vec3 et = normalize(cross(nrm, er));

    // u = 1/r parameterised by orbital angle. RK2 on u'' = -u + 1.5 rs u^2,
    // which is the Schwarzschild null geodesic in the orbital plane.
    float u = 1.0 / r0;
    float dru = dot(rd, er);
    float dtu = dot(rd, et);
    float du = -u * (dru / max(dtu, 1e-4));

    float phi = 0.0;
    float dphi = 0.035;
    vec3 prevPos = ro;
    vec3 prevPrev = ro;
    escapeDir = rd;

    for (int i = 0; i < 160; i++){
      float k1 = -u + 1.5 * rs * u * u;
      float uMid  = u  + du * dphi * 0.5;
      float duMid = du + k1 * dphi * 0.5;
      float k2 = -uMid + 1.5 * rs * uMid * uMid;
      u  += duMid * dphi;
      du += k2 * dphi;
      phi += dphi;

      if (u <= 0.0) break;                       // escaped
      float r = 1.0 / u;
      if (r <= rs * horizonCover){ captured = true; break; }

      vec3 p = (er * cos(phi) + et * sin(phi)) * r;

      // The ray's CURRENT heading, in world space. Once it stops bending
      // this is the direction it flies off toward, and that is the patch of
      // sky this fragment shows. Tracking it is what lets a fragment near
      // the rim - whose geodesic has swung through a large angle - display
      // sky from behind the hole, which is the whole point of lensing.
      escapeDir = normalize(p - prevPos);
      totalBend = phi;

      if (r > quadRadius * 2.2) break;           // left the region we draw

      // ---- volumetric disk ----
      // Accumulate through a slab of real thickness rather than testing a
      // single plane crossing, which would be a sheet of zero depth.
      if (diskBright > 0.0 && diskThickness > 0.0){
        float h1 = dot(p, dn);
        float h0 = dot(prevPos, dn);
        bool crossed = h0 * h1 < 0.0;
        bool inSlab = abs(h1) <= diskThickness;
        if (crossed || inSlab){
          vec3 hit = crossed ? mix(prevPos, p, h0 / (h0 - h1)) : p;
          float hr = length(hit);
          if (hr > diskInner && hr < diskOuter){
            float hgt = 1.0 - clamp(abs(dot(hit, dn)) / diskThickness, 0.0, 1.0);
            float ang = atan(dot(hit, dz), dot(hit, dx));
            float a;
            vec3 dc = diskColor(hr, ang, dot(hit, dn), a);

            // Relativistic beaming: gas coming toward you is brighter and
            // bluer. This is why a real disk is lopsided.
            vec3 orbit = normalize(cross(dn, hit));
            float vmag = sqrt(max(rs / (2.0 * hr), 0.0));
            vec3 toCam = normalize(camPos - holePos - hit);
            float beta = dot(orbit * vmag, -toCam);
            float dop = 1.0 / max(1.0 - beta, 0.05);
            float boost = pow(clamp(dop, 0.2, 4.0), 3.0 * dopplerAmt);
            vec3 shift = beta > 0.0
              ? mix(dc, vec3(0.75, 0.88, 1.0) * (dc.r + dc.g + dc.b) * 0.5,
                    clamp(beta * 1.6 * dopplerAmt, 0.0, 0.85))
              : mix(dc, vec3(1.0, 0.35, 0.12) * (dc.r + dc.g + dc.b) * 0.45,
                    clamp(-beta * 1.4 * dopplerAmt, 0.0, 0.8));

            float w = a * mix(0.35, 1.0, hgt) * (crossed ? 1.0 : 0.55);
            col += transmit * shift * boost * w;
            transmit *= (1.0 - clamp(w, 0.0, 1.0) * 0.92);
            if (transmit < 0.02) break;
          }
        }
      }

      prevPrev = prevPos;
      prevPos = p;
      // Widen the step as the ray escapes, so distant stretches are cheap.
      dphi = 0.035 * (1.0 + smoothstep(rs * 6.0, rs * 60.0, r) * 3.5);
    }
  }

  // ---- the lensed sky ----
  // An escaping ray carries light from whatever it is pointing at once it
  // has finished bending. Adding it here is what puts a genuinely warped
  // starfield INSIDE the hole's own render, including the two things a
  // screen-space pass can never produce: multiple images of one star, and
  // the sky wrapped tightly around the shadow's rim.
  //
  // Weighted by how far the geodesic actually swung, so that far from the
  // hole - where the ray is essentially straight and the real starfield
  // behind the quad is already correct - this contributes nothing and
  // cannot double the stars. It fades in exactly where the deflection
  // becomes real, which is also where the flat background is most wrong.
  float lensedSky = 0.0;
  if (!captured){
    lensedSky = smoothstep(0.05, 0.55, totalBend) * transmit;
    col += skyAlongRay(escapeDir) * lensedSky;
  }

  // Coverage. Captured rays are pure shadow: fully opaque black, which is
  // what actually occludes the stars behind the hole. Everything else
  // contributes only the light it picked up, so the sky shows through.
  float lum = clamp(max(max(col.r, col.g), col.b), 0.0, 1.0);
  // Where the sky is being drawn lensed, the quad must be opaque enough to
  // hide the UNBENT background behind it, or both are visible at once and
  // the stars appear doubled.
  float alpha = captured ? 1.0 : max(lum, lensedSky * 0.92);

  // Fade the quad's own rim so its square edge is never visible.
  vec2 d = (vUV - 0.5) * 2.0;
  float edge = 1.0 - smoothstep(0.86, 1.0, length(d));
  alpha *= edge;

  // Tone map so a bright disk does not clip to a flat white blob.
  col = (col * (2.51 * col + 0.03)) / (col * (2.43 * col + 0.59) + 0.14);
  col = pow(clamp(col, 0.0, 1.0), vec3(1.0 / 2.2));

  gl_FragColor = vec4(col * (captured ? 0.0 : 1.0), clamp(alpha, 0.0, 1.0));
}
`;

let registered = false;

/** Puts the hole-field shader in Babylon's store exactly once. */
export function registerHoleFieldShader(): void {
  if (registered) return;
  Effect.ShadersStore[HOLE_FIELD_SHADER + 'VertexShader'] = VERT;
  Effect.ShadersStore[HOLE_FIELD_SHADER + 'FragmentShader'] = FRAG;
  registered = true;
}
