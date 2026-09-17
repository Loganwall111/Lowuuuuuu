#version 150

uniform float GameTime;
uniform float Layer;
uniform float PlayerDelta;
uniform vec2 WorldOrigin;
uniform float SheetRadius;
uniform vec2 FlowDirection;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

vec3 fluidPalette(float value) {
    float segment = fract(value) * 4.0;
    vec3 teal = vec3(0.015, 0.62, 0.58);
    vec3 cyan = vec3(0.02, 0.92, 0.82);
    vec3 violet = vec3(0.34, 0.10, 0.86);
    vec3 magenta = vec3(0.98, 0.025, 0.44);
    vec3 amber = vec3(1.00, 0.34, 0.055);
    if (segment < 1.0) {
        return mix(teal, cyan, smoothstep(0.0, 1.0, segment));
    }
    if (segment < 2.0) {
        return mix(cyan, violet, smoothstep(1.0, 2.0, segment));
    }
    if (segment < 3.0) {
        return mix(violet, magenta, smoothstep(2.0, 3.0, segment));
    }
    return mix(magenta, amber, smoothstep(3.0, 4.0, segment));
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    vec2 worldPosition = WorldOrigin + centered * SheetRadius;
    float time = GameTime * 1.25 + Layer * 0.71;
    vec2 flow = normalize(FlowDirection + vec2(0.0001));
    vec2 crossFlow = vec2(-flow.y, flow.x);

    float along = dot(centered, flow);
    float across = dot(centered, crossFlow);
    float waveA = sin(along * 17.0 + time * 1.8 + sin(across * 8.0) * 1.7);
    float waveB = cos(across * 21.0 - time * 1.35 + sin(along * 6.0) * 2.1);
    vec2 displaced = centered + vec2(waveB, waveA) * 0.045;
    displaced += flow * sin(time * 0.92 + Layer * 1.7 + along * 4.0) * 0.028;

    float radial = length(displaced);

    // World-anchored basin fields cut the broad sheet into irregular authored
    // pool silhouettes. The same equations are evaluated server-side by
    // SiftFluidField so current force exists only inside the visible volume.
    float basinA = 0.5 + 0.5 * sin(
            worldPosition.x * 0.018 + sin(worldPosition.y * 0.013 + Layer * 1.9) * 2.7 + Layer * 0.7
    );
    float basinB = 0.5 + 0.5 * cos(
            worldPosition.y * 0.021 - sin(worldPosition.x * 0.011 - Layer * 1.3) * 2.1 - Layer * 0.41
    );
    float poolShape = smoothstep(0.28, 0.64, basinA * 0.62 + basinB * 0.38);
    float poolMask = (1.0 - smoothstep(0.73, 1.02, radial)) * poolShape;
    float flowCoordinate = dot(displaced, flow);
    float iridescence = fract(
            0.27 + Layer * 0.17 + displaced.x * 0.25 + displaced.y * 0.18
            + waveA * 0.08 + time * 0.035 + flowCoordinate * 0.12
    );

    vec3 color = fluidPalette(iridescence);
    float lightWave = 0.5 + 0.5 * waveB;
    color *= 0.42 + 0.40 * lightWave;

    // Interlocking flow bands act like stylized caustics across the moving sheet.
    float streamA = sin(flowCoordinate * 34.0 - time * 2.4 + sin(across * 9.0 + time) * 1.5);
    float streamB = cos(across * 27.0 + time * 1.9 + along * 3.0);
    float caustic = smoothstep(0.68, 0.98, 0.5 + 0.5 * streamA * streamB);
    float currentLine = 1.0 - smoothstep(0.0, 0.12, abs(sin(flowCoordinate * 18.0 - time * 1.7 + waveA)));
    color += fluidPalette(fract(iridescence + 0.18)) * caustic * poolMask * 0.24;
    color += vec3(0.35, 0.88, 0.78) * currentLine * poolMask * 0.08;

    // Bright ripples become intersection foam when the player crosses a sheet.
    float foamWave = abs(sin(radial * 31.0 - time * 2.3 + waveA * 2.0));
    float foam = smoothstep(0.80, 0.98, foamWave) * smoothstep(0.25, 0.92, poolMask);
    float intersection = exp(-max(PlayerDelta, 0.0) * 3.8);
    color += vec3(1.0, 0.96, 0.86) * foam * intersection * 1.6;

    // Fresnel-like edge light and narrow moving glints sell the layer as liquid
    // without introducing a solid block, fluid collision, or sampled texture.
    float edgeGlow = smoothstep(0.48, 0.92, radial) * poolMask;
    float glintWave = max(0.0, sin(flowCoordinate * 23.0 - time * 3.2 + across * 4.0));
    float glint = pow(glintWave, 12.0) * poolMask;
    color += fluidPalette(fract(iridescence + 0.21)) * edgeGlow * 0.26;
    color += vec3(0.76, 1.0, 0.94) * glint * (0.20 + intersection * 0.42);

    float alpha = poolMask * (0.16 + 0.18 * lightWave + caustic * 0.10);
    alpha += foam * intersection * 0.38 + glint * 0.10;
    alpha *= vertexColor.a;

    if (alpha < 0.012) {
        discard;
    }
    fragColor = vec4(color, clamp(alpha, 0.0, 0.94));
}
