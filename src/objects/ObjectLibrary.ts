import * as THREE from 'three';

export const ObjectLibrary = {
    getGeometry(type: string): THREE.BufferGeometry {
        switch(type) {
            case 'sphere': return new THREE.SphereGeometry(1, 32, 32);
            case 'box': return new THREE.BoxGeometry(1, 1, 1);
            case 'cylinder': return new THREE.CylinderGeometry(0.5, 0.5, 1, 32);
            default: return new THREE.BoxGeometry(1, 1, 1);
        }
    },
    getMaterial(config: any): THREE.Material {
        return new THREE.MeshPhysicalMaterial({
            color: config.color || 0x00ff00,
            metalness: 0.5,
            roughness: 0.2,
            transmission: config.transmission || 0,
            thickness: 0.5
        });
    }
};
