#version 120

precision highp float;
precision highp int;

uniform int worldTime;
uniform vec3 sunPosition;
uniform vec3 upPosition;

varying vec4 color;
varying vec3 viewPos;

// 1. Day Sky: Periwinkle Lavender -> Golden Amber
vec3 getDaySky(float h) {
    vec3 cZenith  = vec3(0.549, 0.529, 0.910); // #8c87e8
    vec3 cLilac   = vec3(0.686, 0.608, 0.886); // #af9be2
    vec3 cMauve   = vec3(0.835, 0.682, 0.839); // #d5aed6
    vec3 cPeach   = vec3(0.957, 0.722, 0.604); // #f4b89a
    vec3 cApricot = vec3(0.969, 0.769, 0.451); // #f7c473
    vec3 cHorizon = vec3(0.973, 0.714, 0.282); // #f8b648
    if (h < 0.08) return mix(cHorizon, cApricot, h / 0.08);
    if (h < 0.22) return mix(cApricot, cPeach, (h - 0.08) / 0.14);
    if (h < 0.45) return mix(cPeach, cMauve, (h - 0.22) / 0.23);
    if (h < 0.72) return mix(cMauve, cLilac, (h - 0.45) / 0.27);
    return mix(cLilac, cZenith, (h - 0.72) / 0.28);
}

// 2. Noon Sky: Vivid Bright Story Mode Azure
vec3 getNoonSky(float h) {
    vec3 cZenith  = vec3(0.368, 0.549, 0.949);
    vec3 cMid     = vec3(0.529, 0.765, 0.980);
    vec3 cHorizon = vec3(0.882, 0.894, 0.941);
    if (h < 0.35) return mix(cHorizon, cMid, h / 0.35);
    return mix(cMid, cZenith, (h - 0.35) / 0.65);
}

// 3. Sunset / Twilight Sky: Royal Violet -> Vivid Magenta -> Fiery Coral -> Orange
vec3 getSunsetSky(float h) {
    vec3 cZenith  = vec3(0.220, 0.039, 0.329); // #380a54 Royal dark violet
    vec3 cMagenta = vec3(0.486, 0.082, 0.408); // #7c1568 Rich magenta
    vec3 cRose    = vec3(0.663, 0.125, 0.447); // #a92072 Vibrant rose
    vec3 cCoral   = vec3(0.941, 0.314, 0.282); // #f05048 Fiery coral
    vec3 cHorizon = vec3(0.976, 0.533, 0.157); // #f98828 Fiery sunset orange
    if (h < 0.10) return mix(cHorizon, cCoral, h / 0.10);
    if (h < 0.30) return mix(cCoral, cRose, (h - 0.10) / 0.20);
    if (h < 0.60) return mix(cRose, cMagenta, (h - 0.30) / 0.30);
    return mix(cMagenta, cZenith, (h - 0.60) / 0.40);
}

// 4. Night Sky: Deep Obsidian Midnight -> Dark Violet -> Deep Indigo Horizon
vec3 getNightSky(float h) {
    vec3 cZenith  = vec3(0.063, 0.016, 0.110); // #10041c Deep obsidian midnight
    vec3 cMid     = vec3(0.098, 0.039, 0.176); // #190a2d Dark royal purple
    vec3 cHorizon = vec3(0.157, 0.110, 0.294); // #281c4b Deep twilight indigo
    if (h < 0.40) return mix(cHorizon, cMid, h / 0.40);
    return mix(cMid, cZenith, (h - 0.40) / 0.60);
}

void main() {
    vec3 nView = normalize(viewPos);
    // Smoothly extend horizon color below the horizon to eliminate dark bands completely
    float h = clamp(nView.y, 0.0, 1.0);

    float sunY = normalize(sunPosition).y;

    vec3 dayCol    = getDaySky(h);
    vec3 noonCol   = getNoonSky(h);
    vec3 sunsetCol = getSunsetSky(h);
    vec3 nightCol  = getNightSky(h);

    float noonWeight = clamp(sunY * 1.5 - 0.5, 0.0, 1.0);
    vec3 fullDayCol = mix(dayCol, noonCol, noonWeight);

    float sunsetWeight = clamp(1.0 - abs(sunY - 0.05) / 0.25, 0.0, 1.0);
    sunsetWeight = smoothstep(0.0, 1.0, sunsetWeight);

    float nightWeight = clamp((-sunY - 0.05) / 0.25, 0.0, 1.0);
    nightWeight = smoothstep(0.0, 1.0, nightWeight);

    float dayWeight = clamp((sunY - 0.10) / 0.25, 0.0, 1.0);
    dayWeight = smoothstep(0.0, 1.0, dayWeight);

    vec3 finalCol = fullDayCol * dayWeight + sunsetCol * sunsetWeight + nightCol * nightWeight;
    float totalW = dayWeight + sunsetWeight + nightWeight;
    if (totalW > 0.001) {
        finalCol /= totalW;
    } else {
        finalCol = fullDayCol;
    }

    finalCol *= max(color.rgb, vec3(0.35));
    gl_FragColor = vec4(finalCol, 1.0);
}
