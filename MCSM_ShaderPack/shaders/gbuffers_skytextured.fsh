#version 120

// ============================================================================
// MCSM gbuffers_skytextured.fsh — unified-namespace sky sheets + dark backdrop
// ============================================================================
// Namespace unification: every custom environment sheet this program reads
// resolves out of the single synchronized directory
//   assets/minecraft/optifine/sky/world0/          (time-of-day skyboxes)
//   shaders/textures/environment/sky/              (dark purple-black
//                                                   backdrop, bound via
//                                                   customTexture.darkBackdrop)
// gtexture carries the active OptiFine sky sheet for the current clock phase.
// The clock itself is sampled LIVE below (worldTime -> vLiveTime ->
// sunAngle -> sunPosition) so Sodium and Iris can never lock the cycle at
// tick 0 and the dome can never flash between mismatched namespaces.

precision highp float;
precision highp int;

uniform sampler2D gtexture;
uniform sampler2D darkBackdrop;
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

    // Live lavender/orange time-of-day grading over the synchronized sheets.
    float t = mod(liveTime, 24000.0);
    float dayAmt = smoothstep(-0.15, 0.25, sin(6.2831853 * t / 24000.0));
    float sunsetAmt = clamp(1.0 - abs(dayAmt - 0.30) / 0.30, 0.0, 1.0);
    vec3 warmTint = vec3(1.08, 0.78, 0.48);      // MCSM orange horizon glow
    vec3 lavenderNight = vec3(0.62, 0.60, 0.90); // MCSM lavender night tint
    col.rgb *= mix(vec3(1.0), lavenderNight, (1.0 - dayAmt) * 0.55);
    col.rgb = mix(col.rgb, col.rgb * warmTint, sunsetAmt * 0.80);

    // Dark purple-and-black atmospheric backdrop: blends into the lower half
    // of the sky dome (behind the storm) from the bound environment sheet.
    vec3 backdrop = texture2D(darkBackdrop, texcoord).rgb;
    float backdropAmt = smoothstep(0.60, 0.0, texcoord.y);
    col.rgb = mix(col.rgb, backdrop, backdropAmt * 0.85);

    col *= color;
    gl_FragColor = col;
}
