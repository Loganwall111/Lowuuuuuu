#version 120

varying vec4 color;
varying vec3 viewPos;

// Minecraft: Story Mode Official Daytime Sky Palette
vec3 getStoryModeDaySky(float elev) {
    vec3 cZenith   = vec3(0.549, 0.529, 0.910); // #8c87e8 periwinkle lavender zenith
    vec3 cLilac    = vec3(0.686, 0.608, 0.886); // #af9be2 soft lilac
    vec3 cMauve    = vec3(0.835, 0.682, 0.839); // #d5aed6 soft mauve lilac-pink
    vec3 cPeach    = vec3(0.957, 0.722, 0.604); // #f4b89a warm peach-pink
    vec3 cApricot  = vec3(0.969, 0.769, 0.451); // #f7c473 warm golden apricot
    vec3 cHorizon  = vec3(0.973, 0.714, 0.282); // #f8b648 rich golden amber horizon
    vec3 cVoid     = vec3(0.350, 0.220, 0.150);

    if (elev < 0.0) {
        float t = clamp(-elev / 0.20, 0.0, 1.0);
        return mix(cHorizon, cVoid, t);
    } else if (elev < 0.06) {
        return mix(cHorizon, cApricot, smoothstep(0.0, 1.0, elev / 0.06));
    } else if (elev < 0.18) {
        return mix(cApricot, cPeach, smoothstep(0.0, 1.0, (elev - 0.06) / 0.12));
    } else if (elev < 0.38) {
        return mix(cPeach, cMauve, smoothstep(0.0, 1.0, (elev - 0.18) / 0.20));
    } else if (elev < 0.65) {
        return mix(cMauve, cLilac, smoothstep(0.0, 1.0, (elev - 0.38) / 0.27));
    } else {
        return mix(cLilac, cZenith, smoothstep(0.0, 1.0, (elev - 0.65) / 0.35));
    }
}

void main() {
    float elev = normalize(viewPos).y;
    vec3 skyCol = getStoryModeDaySky(elev);
    gl_FragColor = vec4(skyCol, 1.0);
}
