#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 normalVec;
in vec3 viewDir;
in vec3 worldPos;

out vec4 fragColor;

void main() {
    // 1. Fresnel glow effect: borders are bright blue, center remains see-through
    vec3 n = normalize(normalVec);
    vec3 v = normalize(viewDir);
    float ndotv = max(dot(n, v), 0.0);
    float fresnel = pow(1.0 - ndotv, 2.5);

    // 2. Cyan-blue energy shield (#00E5FF)
    vec3 cyanColor = vec3(0.0, 0.898, 1.0); // #00E5FF
    vec3 electricWhite = vec3(0.75, 0.95, 1.0);

    // 3. Glowing scrolling blocky voxel hex-grid / pixelated matrix texture
    float t = GameTime * 1200.0;
    vec2 scrollUv = texCoord0 * vec2(48.0, 24.0) + vec2(t * 0.15, t * 0.08);
    vec2 gridCell = floor(scrollUv);
    
    // Pixelated blocky voxel matrix cell noise
    float cellHash = fract(sin(dot(gridCell, vec2(12.9898, 78.233))) * 43758.5453);
    float gridBorder = step(0.90, fract(scrollUv.x)) + step(0.90, fract(scrollUv.y));
    float hexPattern = clamp(step(0.78, cellHash) * 0.65 + gridBorder * 0.45, 0.0, 1.0);

    // Sine-wave pulse simulating active electric shielding
    float pulse = 0.85 + 0.15 * sin(t * 3.2 + texCoord0.y * 6.28);

    // Border is brighter cyan-blue while the center remains see-through (~0.10 alpha)
    float edgeAlpha = clamp(fresnel * 0.85 + hexPattern * 0.35, 0.10, 0.95);
    vec3 finalRgb = mix(cyanColor, electricWhite, fresnel * 0.60 + hexPattern * 0.40);

    float alpha = edgeAlpha * pulse * vertexColor.a;

    // Depth testing enabled in pipeline masks out back-faces when inside
    fragColor = vec4(finalRgb * pulse, alpha) * ColorModulator;
}
