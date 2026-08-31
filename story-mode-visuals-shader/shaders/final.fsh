#version 120
/* DRAWBUFFERS:0 */

#include "/lib.glsl"

/*
  FINAL: Telltale ink outlines (Sobel depth+normal), Season-1 LUT grade,
  story vignette, cinematic letterbox and film grain.
*/

varying vec2 texcoord;

uniform sampler2D colortex0;
uniform sampler2D depthtex1;
uniform sampler2D colortex2;

uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform float sunAngle;
uniform float nightVision;
uniform float blindness;
uniform vec3  cameraPosition;

uniform int STYLE; //settings preset
uniform float SATURATION; //settings saturation
uniform float VIGNETTE_STR; //settings vignette

void main() {
    vec2 uv = texcoord;
    vec3 color = texture2D(colortex0, uv).rgb;

    // ==================== TELLTALE INK OUTLINES =============================
#ifdef OUTLINE
    vec2 px = vec2(1.0 / viewWidth, 1.0 / viewHeight);
    float d = texture2D(depthtex1, uv).r;

    float depthEdge = 0.0;
    if (d < 0.9999) {
        float dl = texture2D(depthtex1, uv + vec2(-px.x, 0.0)).r;
        float dr = texture2D(depthtex1, uv + vec2( px.x, 0.0)).r;
        float du = texture2D(depthtex1, uv + vec2(0.0, -px.y)).r;
        float dd = texture2D(depthtex1, uv + vec2(0.0,  px.y)).r;
        depthEdge = abs(dl - dr) + abs(du - dd);
    }

    vec3 n  = texture2D(colortex2, uv).rgb;
    vec3 nl = texture2D(colortex2, uv + vec2(-px.x, 0.0)).rgb;
    vec3 nr = texture2D(colortex2, uv + vec2( px.x, 0.0)).rgb;
    vec3 nu = texture2D(colortex2, uv + vec2(0.0, -px.y)).rgb;
    vec3 nd = texture2D(colortex2, uv + vec2(0.0,  px.y)).rgb;
    float normalEdge = length(nl - nr) + length(nu - nd);

    float depthWeight = smoothstep(0.02, 0.35, depthEdge * 420.0);
    float normalWeight = smoothstep(0.30, 1.0, normalEdge * 3.0);
    float outline = clamp(depthWeight * 0.8 + normalWeight * 0.6, 0.0, 1.0);
    outline *= 1.0 - smoothstep(0.55, 0.9995, d);          // fade with distance
    color = mix(color, vec3(0.045, 0.045, 0.06), outline * 0.85);
#endif

    // ==================== SEASON-1 LUT GRADE ================================
    color = grade(color, SATURATION);

    // style presets: 0 Story Mode | 1 Vibrant | 2 Moody
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    if (STYLE == 1) {
        color = mix(vec3(luma), color, 1.30);
    } else if (STYLE == 2) {
        color = mix(vec3(luma), color, 0.55);
        color = mix(vec3(0.5), color, 1.16);
    }

    // ==================== VIGNETTE ==========================================
#ifdef VIGNETTE
    vec2 vc = uv - 0.5;
    float aspect = viewWidth / max(viewHeight, 1.0);
    float vig = smoothstep(0.85, 0.30, length(vc * vec2(1.0, aspect)));
    float nightW = smoothstep(0.55, 0.35, sunAngle);
    vec4 caveFog = sampledFog(cameraPosition);
    float underground = smoothstep(0.5, 0.9, caveFog.a);
    float vigStr = mix(0.30, 0.62, max(nightW, underground)) * VIGNETTE_STR;
    color *= mix(1.0, mix(1.0, 0.55, vig), vigStr);
#endif

    // ==================== CINEMATIC LETTERBOX ===============================
#ifdef LETTERBOX
    float bar = smoothstep(0.112, 0.102, uv.y) + smoothstep(0.888, 0.898, uv.y);
    color *= 1.0 - bar;
#endif

    // ==================== FILM GRAIN ========================================
#ifdef FILM_GRAIN
    float grain = hash12(uv * vec2(viewWidth, viewHeight) + frameTimeCounter * 1.7);
    float grainStr = 0.028 * (STYLE == 2 ? 1.8 : 1.0);
    color += (grain - 0.5) * grainStr;
#endif

    // night vision / blindness compat
    color = mix(color, color * 0.03, blindness);
    color = mix(color, vec3(0.55, 0.9, 0.6) * color, nightVision * 0.4);

    gl_FragData[0] = vec4(color, 1.0);
}
