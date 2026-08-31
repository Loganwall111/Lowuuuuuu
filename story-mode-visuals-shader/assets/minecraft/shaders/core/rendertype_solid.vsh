#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(ModelViewMat, pos, FogShape);

    vec3 lightmap = minecraft_sample_lightmap(Sampler2, UV2).rgb;
    float lum = dot(lightmap, vec3(0.2126, 0.7152, 0.0722));

    // Story Mode style: warm sun, cool shade
    vec3 tint = mix(vec3(0.90, 0.94, 1.06), vec3(1.10, 1.03, 0.92), clamp(lum * 1.25, 0.0, 1.0));

    // soft directional shading from above (kept from the original pack)
    float groundShadow = max(0.5, dot(normalize(Normal), normalize(vec3(0.3, 1.0, 0.2))));

    // baked AO-like contact shading at the base of blocks
    float yFrac = fract(pos.y);
    float contact = 1.0;
    if (Normal.y < -0.5) {
        contact = 0.90;                                        // undersides
    } else if (abs(Normal.y) < 0.5) {
        contact = mix(0.93, 1.0, smoothstep(0.0, 0.22, yFrac)); // wall bases
    }

    vertexColor = vec4(Color.rgb * lightmap * tint * (groundShadow * contact), Color.a);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
