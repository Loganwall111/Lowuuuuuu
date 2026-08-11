/* ---------------------------------------------------------------------------
 * Ocean Worlds — GLSL shader sources (registered into BABYLON.Effect.ShadersStore)
 * Global: window.WATER_GLSL.install()
 * ------------------------------------------------------------------------- */
(function () {
  /* Shared math + procedural sky. Shaders that include SKY_LIB must declare the
   * sky uniforms listed in SKY_UNIFORMS. */
  const LIB = `
float hash12(vec2 p){ vec3 p3 = fract(vec3(p.xyx) * 0.1031); p3 += dot(p3, p3.yzx + 33.33); return fract((p3.x + p3.y) * p3.z); }
float vnoise(vec2 p){
  vec2 i = floor(p); vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float a = hash12(i);
  float b = hash12(i + vec2(1.0, 0.0));
  float c = hash12(i + vec2(0.0, 1.0));
  float d = hash12(i + vec2(1.0, 1.0));
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}
float fbm2(vec2 p){
  float a = 0.5; float r = 0.0;
  for (int i = 0; i < 4; i++){ r += a * vnoise(p); p = p * 2.03 + 11.17; a *= 0.5; }
  return r;
}
float fbm2s(vec2 p){
  float a = 0.5; float r = 0.0;
  for (int i = 0; i < 3; i++){ r += a * vnoise(p); p = p * 2.11 + 7.3; a *= 0.5; }
  return r;
}
mat2 rot2(float a){ float c = cos(a); float s = sin(a); return mat2(c, s, -s, c); }
`;

  const SKY_UNIFORMS = `
uniform vec3 uSunDir;
uniform vec3 uSunTint;
uniform vec3 uHorizonTint;
uniform vec3 uZenithTint;
uniform float uCloudCover;
uniform float uCloudScale;
uniform float uSkyT;
uniform vec2 uWindSky;
uniform float uStarBoost;
uniform float uAurora;
uniform vec3 uPlanetDir;
uniform float uPlanetSize;
uniform vec3 uPlanetA;
uniform vec3 uPlanetB;
`;

  const SKY_LIB = `
float starGrid(vec3 d, float scale, float density){
  vec3 q = d * scale;
  vec3 cell = floor(q);
  float h = fract(sin(dot(cell, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
  if (h > density) return 0.0;
  vec3 off = 0.2 + 0.6 * vec3(
    fract(h * 13.7), fract(h * 41.3), fract(h * 97.1));
  float dd = length(fract(q) - off);
  float tw = 0.72 + 0.28 * sin(uSkyT * (1.5 + h * 5.0) + h * 80.0);
  return smoothstep(0.10, 0.0, dd) * tw;
}
vec3 starField(vec3 d){
  float s = starGrid(d, 130.0, 0.055) * 1.5 + starGrid(d, 260.0, 0.04) * 1.1 + starGrid(d, 60.0, 0.03) * 2.2;
  vec3 tint = mix(vec3(1.0, 0.92, 0.82), vec3(0.72, 0.84, 1.25), fract(sin(dot(floor(d * 130.0), vec3(7.13, 3.71, 9.17))) * 2451.7));
  return s * tint * uStarBoost;
}
vec3 skyColor(vec3 d, float withDisc){
  float sunH = uSunDir.y;
  float dayAmt = smoothstep(-0.12, 0.28, sunH);
  vec3 day = mix(uHorizonTint, uZenithTint, pow(clamp(d.y, 0.0, 1.0), 0.5));
  float az = 0.0;
  vec2 flatD = vec2(d.x, d.z);
  vec2 flatS = vec2(uSunDir.x, uSunDir.z);
  if (length(flatD) > 0.001 && length(flatS) > 0.001) az = max(dot(normalize(flatD), normalize(flatS)), 0.0);
  float sunset = pow(clamp(1.0 - abs(sunH) * 2.6, 0.0, 1.0), 2.0);
  day += vec3(1.15, 0.42, 0.12) * sunset * pow(az, 3.0) * (1.0 - clamp(d.y, 0.0, 1.0)) * 1.4;
  vec3 night = vec3(0.008, 0.013, 0.032) + vec3(0.012, 0.02, 0.05) * clamp(d.y, 0.0, 1.0);
  if (dayAmt < 0.98) {
    night += starField(d) * (1.0 - dayAmt);
    float band = exp(-pow(dot(d, normalize(vec3(0.45, 0.8, 0.35))), 2.0) * 3.5);
    night += band * vec3(0.10, 0.14, 0.24) * (1.0 - dayAmt) * (0.5 + 0.5 * fbm2s(d.xz * 4.0 + d.y * 3.0)) * uStarBoost;
  }
  vec3 col = mix(night, day, dayAmt);
  float sd = max(dot(d, uSunDir), 0.0);
  col += uSunTint * pow(sd, 20.0) * (0.10 + 0.25 * sunset + 0.12 * dayAmt);
  col += uSunTint * pow(sd, 320.0) * 1.4;
  col += uSunTint * smoothstep(0.99935, 0.99975, sd) * 16.0 * withDisc;
  if (d.y > 0.012 && uCloudCover > 0.001) {
    vec2 cuv = d.xz / (d.y + 0.16) * uCloudScale + uWindSky * uSkyT * 0.012;
    float cov = fbm2(cuv);
    float base = 1.0 - uCloudCover;
    float cl = smoothstep(base, base + 0.38, cov);
    cl *= smoothstep(0.012, 0.30, d.y);
    float lit = clamp(pow(sd, 2.0) * 0.9 + 0.35, 0.0, 1.0);
    vec3 cloudCol = mix(vec3(0.16, 0.18, 0.24), vec3(1.18, 1.14, 1.06), lit);
    cloudCol = mix(cloudCol * 0.30 + vec3(0.02, 0.03, 0.06), cloudCol, dayAmt);
    cloudCol += vec3(0.9, 0.35, 0.12) * sunset * pow(az, 2.0) * 0.6;
    col = mix(col, cloudCol, cl * 0.85);
  }
  if (uAurora > 0.001 && dayAmt < 0.55) {
    float ang = atan(d.x, d.z);
    float w = fbm2s(vec2(ang * 2.2 + uSkyT * 0.06, d.y * 3.0 - uSkyT * 0.03));
    float curt = pow(max(0.0, sin(ang * 5.0 + w * 7.0 + uSkyT * 0.25)), 2.0);
    float vert = smoothstep(0.02, 0.25, d.y) * (1.0 - smoothstep(0.35, 0.9, d.y));
    vec3 ac = mix(vec3(0.05, 0.9, 0.55), vec3(0.35, 0.2, 0.95), 0.5 + 0.5 * sin(ang + uSkyT * 0.1));
    col += ac * curt * vert * uAurora * (0.55 - dayAmt) * 1.6;
  }
  if (uPlanetSize > 0.001) {
    float pd = dot(d, uPlanetDir);
    float cosR = cos(uPlanetSize);
    if (pd > cosR - 0.06) {
      vec3 upRef = abs(uPlanetDir.y) > 0.93 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
      vec3 t1 = normalize(cross(uPlanetDir, upRef));
      vec3 t2 = cross(t1, uPlanetDir);
      float x = dot(d, t1) / sin(uPlanetSize);
      float y = dot(d, t2) / sin(uPlanetSize);
      float r2 = x * x + y * y;
      float wob = fbm2s(vec2(y * 2.5, uSkyT * 0.008)) * 2.4;
      float bands = 0.5 + 0.5 * sin(y * 8.0 + wob * 3.0);
      float bands2 = 0.5 + 0.5 * sin(y * 23.0 - wob * 4.0 + 1.7);
      vec3 pc = mix(uPlanetA, uPlanetB, bands * 0.7 + bands2 * 0.3);
      float limb = sqrt(max(0.0, 1.0 - r2));
      float lit = clamp(0.5 + 0.9 * dot(d, uSunDir), 0.04, 1.0);
      float diskMask = 1.0 - smoothstep(0.86, 1.0, r2);
      vec3 atmoGlow = uPlanetB * (smoothstep(0.7, 1.0, r2) - smoothstep(1.0, 1.22, r2)) * 0.55 * lit;
      col = mix(col, pc * (0.3 + 0.7 * lit) * (0.35 + 0.65 * limb), diskMask) + atmoGlow;
    }
  }
  return col;
}
`;

  const SKY_VERTEX = `
attribute vec3 position;
uniform mat4 worldViewProjection;
varying vec3 vSkyDir;
void main(void){
  vSkyDir = position;
  vec4 p = worldViewProjection * vec4(position, 1.0);
  gl_Position = p.xyww;
}
`;

  const SKY_FRAGMENT = `
varying vec3 vSkyDir;
${SKY_UNIFORMS}
${LIB}
${SKY_LIB}
void main(void){
  vec3 d = normalize(vSkyDir);
  vec3 col = skyColor(d, 1.0);
  gl_FragColor = vec4(col, 1.0);
}
`;

  /* ------------------------------------------------ water surface -------- */
  const WATER_VERTEX = `
attribute vec3 position;
uniform mat4 worldViewProjection;
uniform float uTime;
uniform float uWaveData[64];
uniform float uWaveData2[64];
uniform int uWaveCount;
uniform float uChop;
uniform float uAmpSum;
uniform float uWhirl[16];
varying vec3 vWPos;
varying vec3 vNormalW;
varying float vCrest;
void main(void){
  vec3 p = position;
  vec3 disp = vec3(0.0);
  vec2 grad = vec2(0.0, 0.0);
  float crest = 0.0;
  for (int i = 0; i < 16; i++){
    if (i >= uWaveCount) break;
    vec2 dir = vec2(uWaveData[i * 4], uWaveData[i * 4 + 1]);
    float k = uWaveData[i * 4 + 2];
    float amp = uWaveData[i * 4 + 3];
    float c = uWaveData2[i * 4];
    float ph0 = uWaveData2[i * 4 + 1];
    float ph = k * dot(dir, p.xz) - k * c * uTime + ph0;
    float s = sin(ph);
    float co = cos(ph);
    float q = min(uChop / (k * amp * float(uWaveCount)), 1.0 / (amp * k));
    disp.y += amp * s;
    disp.x += q * amp * dir.x * co;
    disp.z += q * amp * dir.z * co;
    grad += dir * (k * amp * co);
    crest += amp * s;
  }
  // whirlpool funnels dip the surface
  for (int i = 0; i < 4; i++){
    float ws = uWhirl[i * 4 + 2];
    if (ws > 0.001){
      vec2 dvec = p.xz - vec2(uWhirl[i * 4], uWhirl[i * 4 + 1]);
      float wr = uWhirl[i * 4 + 3];
      float dip = ws * exp(-dot(dvec, dvec) / (wr * wr));
      disp.y -= dip;
      grad -= (dvec * (-2.0 / (wr * wr))) * dip;
    }
  }
  vec3 dp = p + disp;
  vNormalW = normalize(vec3(-grad.x, 1.0, -grad.y));
  vCrest = crest / max(uAmpSum, 0.0001);
  vWPos = dp;
  gl_Position = worldViewProjection * vec4(dp, 1.0);
}
`;

  const WATER_FRAGMENT = `
varying vec3 vWPos;
varying vec3 vNormalW;
varying float vCrest;
uniform vec3 uCamPos;
uniform float uTime;
uniform vec3 uDeepColor;
uniform vec3 uShallowColor;
uniform vec3 uFoamColor;
uniform float uClarity;
uniform float uFoamAmt;
uniform float uSeaLevel;
uniform sampler2D tHeight;
uniform float uMapHalf;
uniform float uHMin;
uniform float uHScale;
uniform float uLava;
uniform float uGlow;
uniform vec3 uGlowColor;
uniform float uFogDensity;
uniform vec3 uFogColor;
uniform float uDetailK;
uniform float uReflGain;
uniform float uGlitter;
uniform vec2 uWindWater;
uniform vec2 uFlow;
uniform float uWhirl[16];
${SKY_UNIFORMS}
${LIB}
${SKY_LIB}
void main(void){
  vec3 V = normalize(uCamPos - vWPos);
  vec3 N = normalize(vNormalW);
  float e = 0.22;
  vec2 adv = uWindWater * 0.9 + uFlow;
  vec2 p1 = vWPos.xz * 0.30 + adv * uTime;
  vec2 p2 = vWPos.xz * 1.35 - (uWindWater * 1.4 + uFlow) * uTime;
  vec2 dn = vec2(vnoise(p1 + vec2(e, 0.0)) - vnoise(p1 - vec2(e, 0.0)),
                 vnoise(p1 + vec2(0.0, e)) - vnoise(p1 - vec2(0.0, e)));
  dn += 0.6 * vec2(vnoise(p2 + vec2(e, 0.0)) - vnoise(p2 - vec2(e, 0.0)),
                   vnoise(p2 + vec2(0.0, e)) - vnoise(p2 - vec2(0.0, e)));
  N = normalize(N + vec3(dn.x, 0.0, dn.y) * uDetailK);

  vec2 huv = vWPos.xz / (uMapHalf * 2.0) + 0.5;
  float tH = texture2D(tHeight, huv).r * uHScale + uHMin;
  float depth = uSeaLevel - tH;
  float alpha = 0.97;
  vec3 col = vec3(0.0);

  // foam sampling coordinates: swirl around whirlpools
  vec2 fuv = vWPos.xz;
  float whirlFoam = 0.0;
  for (int i = 0; i < 4; i++){
    float ws = uWhirl[i * 4 + 2];
    if (ws > 0.001){
      vec2 cxy = vec2(uWhirl[i * 4], uWhirl[i * 4 + 1]);
      vec2 dvec = fuv - cxy;
      float d = length(dvec);
      float wr = uWhirl[i * 4 + 3];
      if (d < wr * 3.5){
        float ang = uTime * ws * (1.6 / (d * 0.30 + 0.45)) * 0.35;
        fuv = cxy + rot2(ang) * dvec;
        whirlFoam += ws * 0.30 * exp(-d * d / (wr * wr * 1.8));
      }
    }
  }

  if (uLava > 0.5) {
    vec2 lp = fuv * 0.045 + uFlow * uTime * 0.02;
    float crustN = fbm2(lp * 3.0);
    float flow = fbm2(lp * 8.0 + crustN * 2.0 - uTime * 0.05);
    float crack = smoothstep(0.42, 0.62, flow);
    vec3 crust = vec3(0.05, 0.04, 0.05) * (0.5 + crustN);
    vec3 hot = mix(vec3(2.6, 0.55, 0.08), vec3(3.5, 1.9, 0.45), pow(flow, 2.0));
    col = mix(crust, hot, crack);
    col += vec3(2.0, 0.5, 0.05) * smoothstep(0.55, 0.95, vCrest * 0.5 + 0.5) * 0.8 * crack;
    float shoreGlow = 1.0 - smoothstep(0.0, 2.0, depth);
    col += vec3(2.2, 0.6, 0.1) * shoreGlow * 0.6;
    float F = 0.03 + 0.97 * pow(1.0 - max(dot(N, V), 0.0), 5.0);
    vec3 R = reflect(-V, N); R.y = abs(R.y);
    col += skyColor(R, 0.0) * F * 0.4;
    alpha = 1.0;
  } else {
    float NoV = max(dot(N, V), 0.0);
    float F = 0.02 + 0.98 * pow(1.0 - NoV, 5.0);
    vec3 R = reflect(-V, N);
    R.y = abs(R.y) + 0.02;
    vec3 refl = skyColor(normalize(R), 0.0);
    float dFac = exp(-max(depth, 0.0) * uClarity);
    vec3 refr = mix(uDeepColor, uShallowColor, dFac);
    float caust = pow(vnoise(vWPos.xz * 2.4 + adv * uTime * 0.9) * vnoise(vWPos.xz * 2.7 - adv * uTime * 0.8) * 1.9, 2.0);
    refr += uShallowColor * caust * dFac * 0.5 * clamp(uSunDir.y, 0.0, 1.0);
    col = mix(refr, refl, F * uReflGain);
    vec3 H = normalize(V + uSunDir);
    float sparkle = 0.6 + 0.7 * vnoise(vWPos.xz * 5.0 + uTime * 2.6);
    float spec = pow(max(dot(N, H), 0.0), 660.0) * 3.4 * sparkle;
    float sheen = pow(max(dot(N, H), 0.0), 48.0) * 0.28;
    float sunUp = smoothstep(-0.04, 0.06, uSunDir.y);
    col += uSunTint * (spec + sheen) * sunUp * uGlitter;
    float crestN = clamp(vCrest * 0.5 + 0.5, 0.0, 1.0);
    float crestFoam = smoothstep(0.55, 0.9, crestN) * fbm2(fuv * 0.55 - (uWindWater + uFlow * 0.6) * uTime * 1.8);
    float shore = 1.0 - smoothstep(0.0, 2.4, depth);
    float bandPhase = 0.5 + 0.5 * sin(depth * 5.0 - uTime * 2.2);
    float shoreFoam = shore * bandPhase * (0.4 + 0.6 * fbm2(fuv * 0.7 + uTime * 0.4));
    float foam = clamp((crestFoam * 1.1 + shoreFoam * 1.5) * uFoamAmt + whirlFoam, 0.0, 1.0);
    float foamLight = 0.30 + 0.70 * clamp(uSunDir.y, 0.0, 1.0) + uGlow * 0.35;
    col = mix(col, uFoamColor * foamLight, foam);
    col += uGlowColor * (crestFoam * 1.3 + shoreFoam * 0.7) * uGlow;
    alpha = clamp(0.45 + depth * 0.10 + F * 0.35, 0.0, 0.97);
    alpha = mix(alpha, 1.0, foam * 0.75);
  }
  float dist = length(uCamPos - vWPos);
  float fogF = 1.0 - exp(-uFogDensity * dist);
  col = mix(col, uFogColor, clamp(fogF, 0.0, 1.0));
  gl_FragColor = vec4(col, alpha);
}
`;

  window.WATER_GLSL = {
    install() {
      BABYLON.Effect.ShadersStore["skyVertexShader"] = SKY_VERTEX;
      BABYLON.Effect.ShadersStore["skyFragmentShader"] = SKY_FRAGMENT;
      BABYLON.Effect.ShadersStore["oceanVertexShader"] = WATER_VERTEX;
      BABYLON.Effect.ShadersStore["oceanFragmentShader"] = WATER_FRAGMENT;
    }
  };
})();
