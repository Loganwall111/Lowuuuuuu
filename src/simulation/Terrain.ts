import * as THREE from 'three';

export class Terrain {
    private mesh: THREE.Mesh;

    constructor(scene: THREE.Scene) {
        const geometry = new THREE.PlaneGeometry(200, 200, 100, 100);
        const material = new THREE.MeshPhongMaterial({ 
            color: 0x3d3d3d, 
            wireframe: true,
            flatShading: true 
        });

        this.mesh = new THREE.Mesh(geometry, material);
        this.mesh.rotation.x = -Math.PI / 2;
        this.mesh.position.y = -5;
        scene.add(this.mesh);
        
        this.generateProceduralPeaks();
    }

    private generateProceduralPeaks(): void {
        const pos = this.mesh.geometry.attributes.position;
        for (let i = 0; i < pos.count; i++) {
            const x = pos.getX(i);
            const y = pos.getY(i);
            pos.setZ(i, Math.sin(x * 0.05) * Math.cos(y * 0.05) * 5);
        }
        this.mesh.geometry.computeVertexNormals();
    }
}
