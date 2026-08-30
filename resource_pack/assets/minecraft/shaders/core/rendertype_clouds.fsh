#version 330

in float cloudDepth;
in vec3 cloudCoord;

uniform sampler2D Sampler0;
uniform float GameTime;

out vec4 fragColor;

// Mathematical 3D cloud noise — no loose image textures
float hash3(vec3 p) {
    p = fract(p * vec3(127.1, 311.7, 74.3));
    p += dot(p, p.yzx + 19.19);
    return fract(p.x * p.y * p.z);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash3(i);
    float b = hash3(i + vec3(1.0, 0.0, 0.0));
    float c = hash3(i + vec3(0.0, 1.0, 0.0));
    float d = hash3(i + vec3(1.0, 1.0, 0.0));
    float e = hash3(i + vec3(0.0, 0.0, 1.0));
    float f2 = hash3(i + vec3(1.0, 0.0, 1.0));
    float g = hash3(i + vec3(0.0, 1.0, 1.0));
    float h = hash3(i + vec3(1.0, 1.0, 1.0));
    float mixX = mix(a, b, f.x);
    float mixX2 = mix(c, d, f.x);
    float mixY = mix(mixX, mixX2, f.y);
    float mixX3 = mix(e, f2, f.x);
    float mixX4 = mix(g, h, f.x);
    float mixY2 = mix(mixX3, mixX4, f.y);
    return mix(mixY, mixY2, f.z);
}

void main() {
    vec3 p = cloudCoord * 0.15 + vec3(0.0, GameTime * 0.05, 0.0);
    float n = noise(p);
    float cloud = smoothstep(0.35, 0.65, n);
    // Blocky cloud mesh volume thickness (vertical extrude by 2.5x preserved from vertex stage)
    cloud *= clamp(cloudDepth * 0.8 + 0.2, 0.0, 1.0);
    // Lavender-tinted cloud layer blend
    vec3 lavender = vec3(0.72, 0.55, 1.0);
    vec3 cloudColor = mix(vec3(1.0), lavender, cloud * 0.35);
    fragColor = vec4(cloudColor, cloud);
}
