#version 150
#moj_import <minecraft:fog.glsl>

#moj_import <minecraft:mcsm_visuals.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform float GameTime;
uniform vec3 PlayerPos;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec3 normal;
out vec3 worldPos;
out float tierFactor;
out vec3 emissiveColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = UV2;
    normal = Normal;
    worldPos = Position + PlayerPos;
    
    // Tier detection for emissive shading
    float y = worldPos.y;
    if (y >= -1250.0 && y <= -1101.0) {
        // Orange-to-Pink Emissive Shading - vertex-shimmed full-bright
        tierFactor = 1.0;
        float depth = clamp((-1101.0 - y) / 149.0, 0.0, 1.0);
        float wave = sin(GameTime * 0.01 + y * 0.05) * 0.5 + 0.5;
        vec3 orange = vec3(1.0, 0.5, 0.1);
        vec3 pink = vec3(1.0, 0.4, 0.7);
        emissiveColor = mix(orange, pink, depth + wave * 0.2) * 1.5;
    } else if (y >= -2032.0 && y <= -1801.0) {
        tierFactor = 0.8;
        // Iridescent fluid tint
        emissiveColor = iridescentWater(worldPos, normalize(-worldPos), normal, GameTime * 0.01);
    } else {
        tierFactor = 0.0;
        emissiveColor = vec3(0.0);
    }
}
