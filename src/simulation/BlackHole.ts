import * as THREE from 'three';
import { PhysicsEntity, CelestialConfig } from './UniverseEntities';

export class BlackHole implements PhysicsEntity {
    public id: string;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.5, 0);
    public mesh: THREE.Mesh | null = null;

    constructor(public config: Partial<CelestialConfig> = {}) {
        this.id = config.id || `blackhole_${Math.random().toString(36).slice(2, 7)}`;
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.position.copy(this.position);
            // Internal logic for lensing effect handled by Renderer/Camera post-processing
        }
    }
}
