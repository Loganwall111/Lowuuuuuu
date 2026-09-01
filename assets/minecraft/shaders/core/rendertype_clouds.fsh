#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 position;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    
    // Generates a dynamic vertical transparency gradient layer just like Story Mode
    float gradient = clamp((position.y - 120.0) / 16.0, 0.0, 1.0);
    
    fragColor = vec4(color.rgb, color.a * gradient);
}
