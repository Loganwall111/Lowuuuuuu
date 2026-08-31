#version 120

varying vec3 viewDir;

void main() {
    gl_Position = ftransform();
    viewDir = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
