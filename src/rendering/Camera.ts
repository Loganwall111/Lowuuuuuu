import * as THREE from 'three';

export class Camera {
    private camera: THREE.PerspectiveCamera;
    private target: THREE.Vector3;
    private radius: number = 25;
    private theta: number = -Math.PI / 2;
    private phi: number = Math.PI / 2.5;

    constructor(_renderer: THREE.WebGLRenderer) {
        this.camera = new THREE.PerspectiveCamera(
            60,
            window.innerWidth / window.innerHeight,
            0.1,
            2000
        );
        this.target = new THREE.Vector3(0, 0, 0);
        this.updateCameraPosition();
    }

    public updateCameraPosition(): void {
        // Enforce smooth coordinate conversions for free tracking
        this.camera.position.x = this.target.x + this.radius * Math.sin(this.phi) * Math.cos(this.theta);
        this.camera.position.y = this.target.y + this.radius * Math.cos(this.phi);
        this.camera.position.z = this.target.z + this.radius * Math.sin(this.phi) * Math.sin(this.theta);
        this.camera.lookAt(this.target);
    }

    public resize(): void {
        this.camera.aspect = window.innerWidth / window.innerHeight;
        this.camera.updateProjectionMatrix();
    }

    public getThreeCamera(): THREE.PerspectiveCamera { return this.camera; }
    public getTarget(): THREE.Vector3 { return this.target; }
    public setTarget(newTarget: THREE.Vector3): void { this.target.copy(newTarget); this.updateCameraPosition(); }
}
