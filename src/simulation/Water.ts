import * as THREE from 'three';

export class Water {
    private mesh: THREE.Mesh;
    private material: THREE.ShaderMaterial;

    constructor(scene: THREE.Scene) {
        const geometry = new THREE.PlaneGeometry(100, 100, 128, 128);
        this.material = new THREE.ShaderMaterial({
            uniforms: {
                time: { value: 0 },
                waterColor: { value: new THREE.Color(0x001e0f) }
            },
            vertexShader: `
                uniform float time;
                varying vec2 vUv;
                void main() {
                    vUv = uv;
                    vec3 pos = position;
                    pos.z += sin(pos.x * 0.1 + time) * 2.0;
                    gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
                }
            `,
            fragmentShader: `
                uniform vec3 waterColor;
                varying vec2 vUv;
                void main() {
                    gl_FragColor = vec4(waterColor, 0.8);
                }
            `,
            transparent: true
        });

        this.mesh = new THREE.Mesh(geometry, this.material);
        this.mesh.rotation.x = -Math.PI / 2;
        scene.add(this.mesh);
    }

    public update(time: number): void {
        this.material.uniforms.time.value = time;
    }
}
