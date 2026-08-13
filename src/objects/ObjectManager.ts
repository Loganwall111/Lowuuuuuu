import * as THREE from 'three';
import { Universe } from '../simulation/Universe';
import { ObjectLibrary } from './ObjectLibrary';

export class ObjectManager {
    private objects: Map<string, THREE.Mesh> = new Map();

    constructor(private scene: THREE.Scene, private universe: Universe) {}

    public createObject(type: string, config: any): THREE.Mesh {
        const geometry = ObjectLibrary.getGeometry(type);
        const material = ObjectLibrary.getMaterial(config);
        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.copy(config.position || new THREE.Vector3());
        this.scene.add(mesh);
        this.objects.set(config.id || Math.random().toString(), mesh);
        return mesh;
    }

    public update(delta: number): void {
        // Handle object life cycles and physics sync
    }
}
