#version 150

// Story Look clouds: the near deck stays blocky-vanilla but brighter, with
// a faint cool lift on the underside so decks read against the pastel sky.
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

uniform sampler2D Sampler0;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    float a = tex.a * vertexColor.a;
    if (a < 0.12) discard;
    vec3 col = tex.rgb * vertexColor.rgb * 1.10 + vec3(0.020, 0.020, 0.035);
    fragColor = vec4(col, a * 0.96);
}
