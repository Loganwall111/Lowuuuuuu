import * as THREE from 'three';
import { PhysicsEntity, CelestialConfig } from './UniverseEntities';

export class Planet implements PhysicsEntity {
    public id: string;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.05, 0);
    public mesh: THREE.Mesh | null = null;
    public atmosphere: THREE.Mesh | null = null;

    constructor(public config: Partial<CelestialConfig> = {}) {
        this.id = config.id || `planet_${Math.random().toString(36).slice(2, 7)}`;
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.position.copy(this.position);
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
        }
        if (this.atmosphere) {
            this.atmosphere.position.copy(this.position);
        }
    }
}
