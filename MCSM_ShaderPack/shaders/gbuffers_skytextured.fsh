#version 120

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform long worldTime;
uniform float sunAngle;
uniform vec3 sunPosition;

varying vec4 color;
varying vec2 texcoord;
varying float vLiveTime;

void main() {
    float liveTime = float(worldTime);
    if (liveTime < 0.5) {
        liveTime = vLiveTime;
        if (liveTime < 0.5) {
            liveTime = mod(sunAngle * 24000.0, 24000.0);
            if (liveTime < 0.5 && length(sunPosition) > 0.01) {
                float sY = normalize(sunPosition).y;
                float sX = normalize(sunPosition).x;
                float a = atan(sY, sX);
                liveTime = mod((0.5 - a / 6.2831853) * 24000.0, 24000.0);
            }
        }
    }

    vec4 col = texture2D(gtexture, texcoord);

    if (col.a < 0.01) {
        discard;
    }

    // Shift the sampled sun/moon/custom-skybox quads with live game time:
    // lavender night shade and warm orange sunrise/sunset glow, so the
    // custom sky maps fade through the cycle instead of staying static.
    float t = mod(liveTime, 24000.0);
    float dayAmt = smoothstep(-0.15, 0.25, sin(6.2831853 * t / 24000.0));
    float sunsetAmt = clamp(1.0 - abs(dayAmt - 0.30) / 0.30, 0.0, 1.0);
    vec3 warmTint = vec3(1.08, 0.78, 0.48);      // MCSM orange horizon glow
    vec3 lavenderNight = vec3(0.62, 0.60, 0.90); // MCSM lavender night tint
    col.rgb *= mix(vec3(1.0), lavenderNight, (1.0 - dayAmt) * 0.55);
    col.rgb = mix(col.rgb, col.rgb * warmTint, sunsetAmt * 0.80);

    col *= color;
    gl_FragColor = col;
}
