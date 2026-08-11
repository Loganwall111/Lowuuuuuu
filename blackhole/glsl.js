/* ---------------------------------------------------------------------------
 * Singularity Vault — GLSL shader sources (registered into BABYLON.Effect.ShadersStore)
 * The main pass ray-marches photon geodesics around a Schwarzschild-style black
 * hole, plus two extra "dimensions" (bloodstream tunnel, kaleidoscope void) and
 * the journey warp overlay.
 * Global: window.BH_GLSL.install()
 * ------------------------------------------------------------------------- */
(function () {
  const LIB = `
float hash13(vec3 p3){ p3 = fract(p3 * 0.1031); p3 += dot(p3, p3.zyx + 31.32); return fract((p3.x + p3.y) * p3.z); }
vec3 hash33(vec3 p3){
  p3 = fract(p3 * vec3(0.1031, 0.1030, 0.0973));
  p3 += dot(p3, p3.yxz + 33.33);
  return fract((p3.xxy + p3.yxx) * p3.zyx);
}
float noise3(vec3 p){
  vec3 i = floor(p); vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float n000 = hash13(i);
  float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
  float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
  float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
  float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
  float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
  float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
  float n111 = hash13(i + vec3(1.0, 1.0, 1.0));
  return mix(mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
             mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y), f.z);
}
float fbm3(vec3 p){
  float a = 0.5; float r = 0.0;
  for (int i = 0; i < 4; i++){ r += a * noise3(p); p = p * 2.04 + 9.7; a *= 0.5; }
  return r;
}
float hash12(vec2 p){ vec3 p3 = fract(vec3(p.xyx) * 0.1031); p3 += dot(p3, p3.yzx + 33.33); return fract((p3.x + p3.y) * p3.z); }
float noise2(vec2 p){
  vec2 i = floor(p); vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), f.x),
             mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), f.x), f.y);
}
float fbm2(vec2 p){
  float a = 0.5; float r = 0.0;
  for (int i = 0; i < 4; i++){ r += a * noise2(p); p = p * 2.07 + 5.2; a *= 0.5; }
  return r;
}
mat2 rot2(float a){ float c = cos(a); float s = sin(a); return mat2(c, s, -s, c); }
`;

  const STARS = `
float starLayer(vec3 d, float scale, float density){
  vec3 q = d * scale;
  vec3 cell = floor(q);
  vec3 h = hash33(cell);
  if (h.x > 0.06 * density) return 0.0;
  vec3 off = 0.15 + 0.7 * hash33(cell + 19.19);
  float dd = length(fract(q) - off);
  float tw = 0.72 + 0.28 * sin(uTime * (1.0 + h.y * 5.0) + h.z * 60.0);
  return smoothstep(0.11, 0.0, dd) * tw;
}
vec3 stars(vec3 d){
  float s = starLayer(d, 90.0, uStarDensity) * 2.0
          + starLayer(d, 210.0, uStarDensity) * 1.25
          + starLayer(d, 420.0, uStarDensity);
  return s * mix(vec3(1.0, 0.92, 0.80), vec3(0.72, 0.85, 1.30), hash13(floor(d * 210.0)));
}
vec3 skyEnv(vec3 d){
  vec3 col = stars(d);
  vec3 bandN = normalize(vec3(0.35, 1.0, 0.25));
  float band = exp(-pow(dot(d, bandN), 2.0) * 4.0);
  vec3 nb = mix(uTintA, uTintB, fbm3(d * 2.3 * uNebScale + 5.0));
  col += band * nb * (0.30 + 0.70 * fbm3(d * 5.0 * uNebScale + 2.0)) * uBandAmt * 0.55;
  col += fbm3(d * 3.1 * uNebScale + 11.0) * uNebulaAmt * uTintB * 0.45;
  return col;
}
`;

  const TEMP_COLOR = `
vec3 tempColor(float t){
  vec3 cold = vec3(0.85, 0.28, 0.06);
  vec3 mid  = vec3(1.18, 0.62, 0.22);
  vec3 hot  = vec3(1.05, 0.95, 1.20);
  vec3 c = mix(cold, mid, clamp(t * 0.9, 0.0, 1.0));
  c = mix(c, hot, clamp((t - 0.8) * 0.8, 0.0, 1.0));
  return c;
}
`;

  const BH_FRAG = `
varying vec2 vUV;
uniform sampler2D textureSampler;

uniform vec2 uRes;
uniform float uTime;
uniform vec3 uCamPos;
uniform vec3 uCamRight;
uniform vec3 uCamUp;
uniform vec3 uCamFwd;
uniform float uFovTan;
uniform float uAspect;
uniform float uModeA;
uniform float uMix;
uniform float uWarp;

uniform float uRS;
uniform float uSpin;
uniform float uLensStr;
uniform float uDtScale;
uniform int uSteps;
uniform float uDiskInner;
uniform float uDiskOuter;
uniform float uDiskTemp;
uniform float uDiskBright;
uniform float uBeaming;
uniform float uSwirlK;
uniform vec3 uDiskN;
uniform vec3 uDiskT1;
uniform vec3 uDiskT2;
uniform float uRingBoost;
uniform float uStarDensity;
uniform float uNebulaAmt;
uniform float uBandAmt;
uniform vec3 uTintA;
uniform vec3 uTintB;
uniform float uPulse;
uniform float uFractK;
uniform vec3 uFractC;
uniform float uFractGlow;
uniform vec3 uHolePos;
uniform float uTwinOn;
uniform vec3 uTwinPos;
uniform float uRS2;
uniform vec3 uRingCol;
uniform vec3 uDiskTint;
uniform float uNebScale;

${LIB}
${STARS}
${TEMP_COLOR}

/* --------------------------- dimension 0 : black hole ------------------- */
vec3 diskShade(vec3 hp, float hr, vec3 rd){
  float rr = hr / uRS;
  float irr = uDiskInner / uRS;
  float orr = uDiskOuter / uRS;
  float ang = atan(dot(hp, uDiskT2), dot(hp, uDiskT1));
  float kepl = pow(irr / max(rr, 0.4), 1.5);
  float a2 = ang + uTime * uSwirlK * kepl * 1.6;
  vec2 pol = vec2(cos(a2), sin(a2));
  float n1 = fbm3(vec3(pol * 1.7, rr * 1.8));
  float n2 = fbm3(vec3(pol * 4.4 + 3.7, rr * 8.5));
  float dens = pow(clamp(n1 * 0.62 + n2 * 0.38, 0.0, 1.0), 1.6) * 2.1;
  float rel = max(rr / irr, 1.0);
  float temp = uDiskTemp / pow(rel, 0.8);
  dens *= smoothstep(irr, irr * 1.12, rr) * (1.0 - smoothstep(orr * 0.72, orr, rr));
  vec3 tint = tempColor(temp);
  vec3 tang = normalize(cross(uDiskN, hp));
  float side = dot(tang, -rd);
  float dop = clamp(1.0 / (1.0 - uBeaming * 0.55 * side), 0.28, 3.2);
  float beam = pow(dop, 3.0);
  vec3 dopTint = mix(vec3(1.05, 0.70, 0.48), vec3(0.70, 0.85, 1.40), clamp(side * 0.5 + 0.5, 0.0, 1.0));
  vec3 c = tint * dens * beam * dopTint * uDiskBright;
  c += tint * exp(-(rel - 1.0) * 1.6) * 0.6 * uDiskBright * beam;
  return c * uDiskTint;
}

vec3 bhScene(vec3 ro, vec3 rd){
  vec3 p = ro;
  vec3 v = rd;
  vec3 hv = cross(p, v);
  float h2 = dot(hv, hv);
  vec3 col = vec3(0.0);
  float minR = 100000.0;
  float captured = 0.0;
  float prevS = dot(p, uDiskN);
  float atten = 1.0;
  for (int i = 0; i < 420; i++){
    if (i >= uSteps) break;
    float r2 = dot(p, p);
    float r = sqrt(r2);
    if (r < minR) minR = r;
    if (r < uRS) { captured = 1.0; break; }
    float dt = uDtScale * clamp(r - uRS * 0.75, 0.10, 1.5);
    vec3 acc = -p * (1.5 * h2 * uLensStr / (r2 * r2 * r));
    if (uTwinOn > 0.5){
      vec3 pt = p - uTwinPos;
      vec3 hv2 = cross(pt, v);
      float h2b = dot(hv2, hv2);
      float r2b = dot(pt, pt);
      float rb = sqrt(r2b);
      acc += -pt * (1.5 * h2b * uLensStr / (r2b * r2b * rb + 0.0001));
      if (rb < uRS2) { captured = 1.0; break; }
    }
    v += acc * dt;
    if (uSpin > 0.001){
      vec3 fr = cross(uDiskN, p) * (uSpin * 2.0 / (r2 * r + 0.4));
      v += fr * dt * 2.5;
    }
    vec3 np = p + v * dt;
    float ns = dot(np, uDiskN);
    if (prevS * ns < 0.0){
      float tt = prevS / (prevS - ns);
      vec3 hit = mix(p, np, tt);
      vec3 hp = hit - uDiskN * dot(hit, uDiskN);
      float hr = length(hp);
      if (hr > uDiskInner && hr < uDiskOuter){
        col += diskShade(hp, hr, v) * atten;
        atten *= 0.55;
      }
    }
    p = np;
    prevS = ns;
    if ((i & 3) == 0){
      float gl2 = exp(-abs(dot(p, uDiskN)) * 2.0) * exp(-max(r - uRS * 2.0, 0.0) * 0.35);
      col += vec3(1.0, 0.72, 0.42) * gl2 * 0.048 * uDiskBright * atten;
    }
    if (r > 80.0 && dot(p, v) > 0.0) break;
  }
  if (captured < 0.5){
    col += skyEnv(normalize(v));
  }
  float ring = exp(-pow((minR / uRS - 1.5) * 3.2, 2.0));
  col += uRingCol * ring * uRingBoost * 0.30;
  return col;
}

/* --------------------------- dimension 1 : bloodstream ------------------ */
vec2 bloodCenter(float z){
  return vec2(sin(z * 0.21) * 2.2 + sin(z * 0.070) * 3.0,
              cos(z * 0.17) * 2.0 + cos(z * 0.052) * 2.4);
}
float bloodWall(vec3 p){
  vec2 c = bloodCenter(p.z);
  float R = 4.3 + 0.7 * sin(p.z * 0.33) + 0.5 * fbm3(vec3(0.0, 0.0, p.z * 0.16));
  R += 0.22 * sin(uTime * 2.3 - p.z * 0.9) * uPulse;
  float d = length(p.xy - c) - R;
  d -= 0.45 * fbm3(p * 0.55 + vec3(0.0, 0.0, uTime * 0.4));
  return d;
}
float bloodCells(vec3 p){
  float seg = 7.0;
  float cz = floor(p.z / seg);
  float d = 1000.0;
  for (int k = 0; k < 2; k++){
    float id = cz + float(k);
    vec3 h = hash33(vec3(id * 7.31, id * 3.17, id * 9.71));
    float zz = id * seg + seg * 0.5;
    vec2 c = bloodCenter(zz);
    vec3 cc = vec3(c.x + (h.x - 0.5) * 4.4, c.y + (h.y - 0.5) * 4.4, zz + (h.z - 0.5) * 3.0);
    cc.xy += vec2(sin(uTime * 0.7 + h.x * 9.0), cos(uTime * 0.8 + h.y * 9.0)) * 0.4;
    float rad = 0.85 + h.z * 0.75;
    vec3 q = (p - cc) * vec3(1.0, 1.9, 1.0);
    d = min(d, length(q) - rad);
  }
  return d;
}
float bloodSDF(vec3 p){
  return min(bloodWall(p), bloodCells(p));
}
vec3 bloodScene(vec3 ro, vec3 rd){
  float t = 0.0;
  vec3 p = ro;
  float d = 0.0;
  for (int i = 0; i < 88; i++){
    p = ro + rd * t;
    d = bloodSDF(p);
    if (d < 0.02 * (1.0 + t * 0.30) || t > 75.0) break;
    t += d * 0.85;
  }
  vec3 fogCol = vec3(0.030, 0.003, 0.006);
  if (t > 75.0) return fogCol;
  vec2 e = vec2(0.05, 0.0);
  vec3 n = normalize(vec3(
    bloodSDF(p + e.xyy) - bloodSDF(p - e.xyy),
    bloodSDF(p + e.yxy) - bloodSDF(p - e.yxy),
    bloodSDF(p + e.yyx) - bloodSDF(p - e.yyx)));
  float isCell = step(bloodCells(p), bloodWall(p));
  float streak = fbm3(vec3(p.xy * 0.5, p.z * 0.12 - uTime * 0.9));
  vec3 wallAlb = mix(vec3(0.30, 0.018, 0.028), vec3(0.55, 0.06, 0.05), streak);
  vec3 cellAlb = vec3(0.55, 0.12, 0.10);
  vec3 albedo = mix(wallAlb, cellAlb, isCell);
  vec3 lpos = vec3(bloodCenter(p.z + 9.0), p.z + 9.0);
  vec3 L = normalize(lpos - p);
  float att = 1.0 / (1.0 + 0.045 * dot(lpos - p, lpos - p));
  float dif = max(dot(n, L), 0.0) * att;
  float head = max(dot(n, -rd), 0.0) * 0.35;
  float spec = pow(max(dot(reflect(-L, n), -rd), 0.0), 26.0) * 0.55 * att;
  float rim = pow(1.0 - max(dot(n, -rd), 0.0), 3.0);
  vec3 lightCol = vec3(1.0, 0.45, 0.38) * (0.8 + 0.25 * uPulse);
  vec3 col = albedo * (0.10 + dif * lightCol + head * vec3(0.9, 0.5, 0.45));
  col += spec * vec3(0.9, 0.35, 0.30);
  col += rim * vec3(0.45, 0.08, 0.08) * 0.6;
  col += isCell * vec3(0.35, 0.05, 0.04) * 0.25;
  float fogF = 1.0 - exp(-t * 0.085);
  return mix(col, fogCol, fogF);
}

/* --------------------------- dimension 2 : kaleidoscope ----------------- */
vec3 pal(float t){
  return uTintA + uTintB * cos(6.28318 * (t + vec3(0.0, 0.33, 0.67)));
}
vec3 fractScene(vec3 ro, vec3 rd){
  vec3 acc = vec3(0.0);
  float phase = uTime * 0.35;
  for (int s = 0; s < 2; s++){
    vec3 p = ro + rd * (2.5 + float(s) * 6.5);
    vec3 z = p;
    float trap = 0.0;
    for (int i = 0; i < 13; i++){
      z = abs(z);
      float len2 = dot(z, z);
      z *= uFractK / clamp(len2, 0.15, 4.5);
      z -= uFractC;
      trap += exp(-len2 * 0.30);
    }
    vec3 pc = pal(trap * 0.11 + phase * 0.06 + float(s) * 0.33);
    acc += pc * trap * 0.028 * (1.0 - float(s) * 0.35);
  }
  float cen = pow(max(0.0, 1.0 - length(rd.xy)), 2.2);
  acc += pal(phase * 0.05 + 0.45) * cen * 0.5;
  acc += pal(dot(rd, vec3(0.5774)) * 0.25 + phase * 0.04) * 0.07;
  return acc * uFractGlow;
}

vec3 evalMode(float mode, vec3 ro, vec3 rd){
  if (mode < 0.5) return bhScene(ro - uHolePos, rd);
  if (mode < 1.5) return bloodScene(ro, rd);
  vec3 r2 = rd;
  r2.xy = rot2(r2.xy, uTime * 0.05);
  r2.yz = rot2(r2.yz, sin(uTime * 0.033) * 0.4);
  return fractScene(ro, r2);
}

vec3 warpOverlay(vec2 uv, vec3 base){
  if (uWarp < 0.001) return base;
  vec2 c = uv - 0.5;
  float r = length(c) * 2.2;
  float a = atan(c.y, c.x);
  float st1 = fbm2(vec2(a * 2.5, r * 6.0 - uTime * 5.0));
  float st2 = fbm2(vec2(a * 5.0 + 3.0, r * 11.0 - uTime * 9.0));
  float core = pow(max(0.0, 1.0 - r), 2.0);
  vec3 wc = mix(uTintB * 1.2, vec3(1.35, 1.40, 1.50), clamp(core * 1.6, 0.0, 1.0));
  wc *= (0.35 + st1 * 0.85 + st2 * 0.45);
  wc += vec3(1.20, 1.25, 1.30) * core * 1.7;
  float m = smoothstep(0.15, 0.9, uWarp * (0.55 + core * 0.95));
  return mix(base, wc, clamp(m, 0.0, 1.0));
}

void main(void){
  vec2 ndc = vUV * 2.0 - 1.0;
  vec3 rd = normalize(uCamFwd + uFovTan * (ndc.x * uAspect * uCamRight + ndc.y * uCamUp));
  vec3 ro = uCamPos;
  vec3 col = evalMode(uModeA, ro, rd);
  // composite real scene meshes (summoned planets, star, belt, comet) over the raymarch
  vec4 scn = texture2D(textureSampler, vUV);
  col = mix(col, scn.rgb, smoothstep(0.12, 0.45, scn.a));
  col = warpOverlay(vUV, col);
  gl_FragColor = vec4(col, 1.0);
}
`;

  /* Small sky dome used by the Hollow House dimension */
  const HSKY_VERTEX = `
attribute vec3 position;
uniform mat4 worldViewProjection;
varying vec3 vDir;
void main(void){
  vDir = position;
  vec4 p = worldViewProjection * vec4(position, 1.0);
  gl_Position = p.xyww;
}
`;
  const HSKY_FRAG = `
varying vec3 vDir;
uniform float uTime;
${LIB}
void main(void){
  vec3 d = normalize(vDir);
  vec3 col = mix(vec3(0.010, 0.014, 0.030), vec3(0.030, 0.048, 0.095), clamp(d.y, 0.0, 1.0));
  col = mix(vec3(0.045, 0.05, 0.07), col, smoothstep(-0.05, 0.25, d.y));
  vec3 q = d * 160.0;
  vec3 cell = floor(q);
  vec3 h = hash33(cell);
  if (h.x < 0.05){
    vec3 off = 0.15 + 0.7 * hash33(cell + 7.7);
    float dd = length(fract(q) - off);
    float tw = 0.7 + 0.3 * sin(uTime * (1.0 + h.y * 4.0) + h.z * 50.0);
    col += smoothstep(0.10, 0.0, dd) * tw * mix(vec3(1.0, 0.9, 0.8), vec3(0.8, 0.9, 1.2), h.z) * 1.4;
  }
  vec3 moonDir = normalize(vec3(-0.45, 0.55, -0.55));
  float md = dot(d, moonDir);
  col += vec3(0.95, 0.98, 1.05) * smoothstep(0.99955, 0.99985, md) * 3.0;
  col += vec3(0.4, 0.5, 0.75) * pow(max(md, 0.0), 300.0) * 0.9;
  float aur = pow(max(0.0, sin(atan(d.x, d.z) * 3.0 + fbm2(d.xz * 3.0 + uTime * 0.05) * 5.0)), 2.0);
  col += vec3(0.1, 0.5, 0.3) * aur * smoothstep(0.1, 0.5, d.y) * (1.0 - smoothstep(0.5, 1.0, d.y)) * 0.10;
  gl_FragColor = vec4(col, 1.0);
}
`;

  window.BH_GLSL = {
    install() {
      BABYLON.Effect.ShadersStore["singularityFragmentShader"] = BH_FRAG;
      BABYLON.Effect.ShadersStore["hskyVertexShader"] = HSKY_VERTEX;
      BABYLON.Effect.ShadersStore["hskyFragmentShader"] = HSKY_FRAG;
    }
  };
})();
