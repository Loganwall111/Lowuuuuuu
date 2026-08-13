import * as THREE from 'three';

export class Renderer {
    private renderer: THREE.WebGLRenderer;
    private scene: THREE.Scene;

    constructor(canvas: HTMLCanvasElement) {
        this.renderer = new THREE.WebGLRenderer({
            canvas: canvas,
            antialias: true,
            powerPreference: "high-performance",
            alpha: false,
            stencil: true,
            depth: true
        });
        
        // High-fidelity display configuration profile
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
        this.renderer.toneMappingExposure = 1.0;

        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x020408);
        this.scene.fog = new THREE.FogExp2(0x020408, 0.002);

        this.initializeDefaultLighting();
    }

    private initializeDefaultLighting(): void {
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.2);
        this.scene.add(ambientLight);

        const primarySun = new THREE.DirectionalLight(0xfff8e7, 1.5);
        primarySun.position.set(100, 50, 100);
        primarySun.castShadow = true;
        primarySun.shadow.mapSize.width = 2048;
        primarySun.shadow.mapSize.height = 2048;
        primarySun.shadow.camera.near = 0.5;
        primarySun.shadow.camera.far = 500;
        
        const d = 50;
        primarySun.shadow.camera.left = -d;
        primarySun.shadow.camera.right = d;
        primarySun.shadow.camera.top = d;
        primarySun.shadow.camera.bottom = -d;
        primarySun.shadow.bias = -0.0005;
        this.scene.add(primarySun);
    }

    public render(camera: THREE.Camera): void {
        this.renderer.render(this.scene, camera);
    }

    public resize(): void {
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    }

    public getThreeRenderer(): THREE.WebGLRenderer { return this.renderer; }
    public getScene(): THREE.Scene { return this.scene; }
}
