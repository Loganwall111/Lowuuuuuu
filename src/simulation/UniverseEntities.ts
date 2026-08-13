import * as THREE from 'three';

// Configuration typing matrices satisfying deep system requirements
export interface CelestialConfig {
    id?: string;
    mass?: number;
    radius?: number;
    density?: number;
    type?: string;
    color?: THREE.Color;
    starCount?: number;
}

// Global N-Body interface alignment for registration mechanics
export interface PhysicsEntity {
    id: string;
    position: THREE.Vector3;
    velocity: THREE.Vector3;
    rotation: THREE.Vector3; // Fully substituted Euler mapping
    angularVelocity: THREE.Vector3;
}

export class Galaxy {
    public id: string;
    public config: CelestialConfig;
    private starsData: { position: THREE.Vector3 }[] = [];

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `galaxy_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            type: cfg.type || 'spiral',
            starCount: cfg.starCount || 10000,
            color: cfg.color || new THREE.Color(0xffffff)
        };
        this.generateProceduralStars();
    }

    private generateProceduralStars(): void {
        const count = this.config.starCount || 1000;
        for (let i = 0; i < count; i++) {
            // Standard spiral density distribution algorithm 
            const theta = Math.random() * Math.PI * 2;
            const r = Math.pow(Math.random(), 2) * 500;
            const armOffset = (i % 2 === 0 ? 0 : Math.PI);
            const spiralTheta = theta + (r * 0.02) + armOffset;

            this.starsData.push({
                position: new THREE.Vector3(
                    Math.cos(spiralTheta) * r,
                    (Math.random() - 0.5) * (50 - r * 0.1),
                    Math.sin(spiralTheta) * r
                )
            });
        }
    }

    public getStar(index: number): { position: THREE.Vector3 } {
        if (index < 0 || index >= this.starsData.length) {
            return { position: new THREE.Vector3(0, 0, 0) };
        }
        return this.starsData[index];
    }
}

export class StarSystem {
    public id: string;
    public config: CelestialConfig;
    public star: Star | null = null;
    public age: number = 0;
    private planets: Planet[] = [];

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `system_${Math.random().toString(36).slice(2, 7)}`;
        this.config = cfg;
    }

    public addPlanet(planet: Planet): void {
        this.planets.push(planet);
    }

    public update(delta: number, time: number): void {
        this.age += delta;
        if (this.star) this.star.update(delta, time);
        for (const planet of this.planets) {
            planet.update(delta, time);
        }
    }
}

export class Star implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3();
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `star_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            mass: cfg.mass || 1.0,
            radius: cfg.radius || 2.0,
            color: cfg.color || new THREE.Color(0xffaa00)
        };
    }

    public update(delta: number, _time: number): void {
        // Continuous uniform spatial star core rotation tracking
        this.rotation.y += (this.angularVelocity.y || 0.01) * delta;
        if (this.mesh) {
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
        }
    }
}

export class Planet implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.05, 0);
    public mesh: THREE.Mesh | null = null;
    private moons: Moon[] = [];

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `planet_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            mass: cfg.mass || 0.01,
            radius: cfg.radius || 1.0,
            color: cfg.color || new THREE.Color(0x0088ff)
        };
    }

    public addMoon(moon: Moon): void {
        this.moons.push(moon);
    }

    public update(delta: number, time: number): void {
        // Handle planetary axial spinning equations 
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
            this.mesh.position.copy(this.position);
        }
        // Propagate system updates deep down to child moon objects
        for (const moon of this.moons) {
            moon.update(delta, time);
        }
    }
}

export class Moon implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.1, 0);
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `moon_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            mass: cfg.mass || 0.0001,
            radius: cfg.radius || 0.25,
            color: cfg.color || new THREE.Color(0xaaaaaa)
        };
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
            this.mesh.position.copy(this.position);
        }
    }
}

export class Asteroid implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(
        Math.random() * 0.05,
        Math.random() * 0.05,
        Math.random() * 0.05
    );
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `asteroid_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            mass: cfg.mass || 0.00001,
            radius: cfg.radius || Math.random() * 0.2 + 0.05,
            color: cfg.color || new THREE.Color(0x888888)
        };
    }

    public update(delta: number, _time: number): void {
        this.rotation.addScaledVector(this.angularVelocity, delta);
        if (this.mesh) {
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
            this.mesh.position.copy(this.position);
        }
    }
}

export class Comet implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.02, 0);
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `comet_${Math.random().toString(36).slice(2, 7)}`;
        this.config = cfg;
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.position.copy(this.position);
        }
    }
}

export class BlackHole implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.5, 0);
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `blackhole_${Math.random().toString(36).slice(2, 7)}`;
        this.config = {
            mass: cfg.mass || 10.0,
            radius: cfg.radius || 1.5,
            color: cfg.color || new THREE.Color(0x000000)
        };
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.position.copy(this.position);
        }
    }
}

export class Wormhole implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3(0, 0.2, 0);
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `wormhole_${Math.random().toString(36).slice(2, 7)}`;
        this.config = cfg;
    }

    public update(delta: number, _time: number): void {
        this.rotation.y += this.angularVelocity.y * delta;
        if (this.mesh) {
            this.mesh.position.copy(this.position);
        }
    }
}

export class Spacecraft implements PhysicsEntity {
    public id: string;
    public config: CelestialConfig;
    public position: THREE.Vector3 = new THREE.Vector3();
    public velocity: THREE.Vector3 = new THREE.Vector3();
    public rotation: THREE.Vector3 = new THREE.Vector3();
    public angularVelocity: THREE.Vector3 = new THREE.Vector3();
    public mesh: THREE.Mesh | null = null;

    constructor(cfg: Partial<CelestialConfig> = {}) {
        this.id = cfg.id || `spacecraft_${Math.random().toString(36).slice(2, 7)}`;
        this.config = cfg;
    }

    public update(delta: number, _time: number): void {
        if (this.mesh) {
            this.mesh.position.copy(this.position);
            this.mesh.rotation.set(this.rotation.x, this.rotation.y, this.rotation.z);
        }
    }
}
