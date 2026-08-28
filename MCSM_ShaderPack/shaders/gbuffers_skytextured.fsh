#version 120

precision highp float;
precision highp int;

uniform sampler2D texture;
uniform sampler2D gtexture;
uniform int worldTime;
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

    vec4 col = texture2D(texture, texcoord);
    if (col.a == 0.0 && col.rgb == vec3(0.0)) {
        col = texture2D(gtexture, texcoord);
    }
    col *= color;

    if (col.a < 0.01) {
        discard;
    }
    gl_FragColor = col;
}
