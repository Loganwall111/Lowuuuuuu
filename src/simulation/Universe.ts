/**
 * Universe - Manages universe-scale simulation
 */

import * as THREE from 'three';
import { Galaxy, StarSystem, Star, Planet as SimPlanet, Moon, Asteroid, Comet, BlackHole as SimBH, Wormhole, Spacecraft } from './UniverseEntities';
import { Physics } from './Physics';

export interface UniverseConfig {
  backgroundColor: number;
  fogColor: number;
  fogDensity: number;
  starDensity: number;
  galaxyCount: number;
  ambientLight: number;
  enableGravity: boolean;
  enableOrbits: boolean;
}

export class Universe {
  private config: UniverseConfig = {
    backgroundColor: 0x000005,
    fogColor: 0x000010,
    fogDensity: 0.0001,
    starDensity: 10000,
    galaxyCount: 100,
    ambientLight: 0.1,
    enableGravity: true,
    enableOrbits: true,
  };
  
  private galaxies: Galaxy[] = [];
  private starSystems: StarSystem[] = [];
  private planets: SimPlanet[] = [];
  private moons: Moon[] = [];
  private asteroids: Asteroid[] = [];
  private comets: Comet[] = [];
  private blackHoles: SimBH[] = [];
  private wormholes: Wormhole[] = [];
  private spacecraft: Spacecraft[] = [];
  private stars: Star[] = [];
  
  private physics: Physics | null = null;
  private scene: THREE.Scene | null = null;
  
  // Scale management
  private currentScale: 'local' | 'planetary' | 'orbital' | 'stellar' | 'galactic' | 'universal' = 'planetary';
  private targetScale: typeof this.currentScale = 'planetary';
  
  // Camera target
  private focusTarget: any = null;
  private followTarget: any = null;
  
  // Visualization
  private starField: THREE.Points | null = null;
  private galaxyMeshes: THREE.Object3D[] = [];
  
  constructor(scene?: THREE.Scene) {
    if (scene) this.scene = scene;
  }

  initialize(): void {
    console.log('Universe initialized');
  }

  setScene(scene: THREE.Scene): void {
    this.scene = scene;
  }

  setPhysics(physics: Physics): void {
    this.physics = physics;
  }

  // Scale management
  setScale(scale: typeof this.currentScale): void {
    this.currentScale = scale;
    this.onScaleChange();
  }

  getScale(): typeof this.currentScale {
    return this.currentScale;
  }

  setTargetScale(scale: typeof this.currentScale): void {
    this.targetScale = scale;
    this.animateScaleTransition();
  }

  private onScaleChange(): void {
    console.log(`Scale changed to: ${this.currentScale}`);
    
    // Adjust visibility based on scale
    switch (this.currentScale) {
      case 'local':
        // Show only local objects
        break;
      case 'planetary':
        // Show planet and local moons
        break;
      case 'orbital':
        // Show orbital system
        break;
      case 'stellar':
        // Show star system
        break;
      case 'galactic':
        // Show galaxy
        break;
      case 'universal':
        // Show everything
        break;
    }
  }

  private animateScaleTransition(): void {
    const startScale = this.currentScale;
    const endScale = this.targetScale;
    const duration = 2000; // ms
    const startTime = performance.now();
    
    const transition = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3); // Ease out cubic
      
      // Interpolate scale effects
      // ...
      
      if (progress < 1) {
        requestAnimationFrame(transition);
      } else {
        this.currentScale = endScale;
        this.onScaleChange();
      }
    };
    
    requestAnimationFrame(transition);
  }

  // Galaxy management
  createGalaxy(config: Partial<Galaxy['config']> = {}): Galaxy {
    const galaxy = new Galaxy(config);
    this.galaxies.push(galaxy);
    
    // Create galaxy visualization
    if (this.scene) {
      this.createGalaxyVisualization(galaxy);
    }
    
    return galaxy;
  }

  createRandomGalaxy(): Galaxy {
    const types = ['spiral', 'barred-spiral', 'elliptical', 'irregular'];
    const type = types[Math.floor(Math.random() * types.length)];
    
    const config: Partial<Galaxy['config']> = {
      type,
      starCount: Math.floor(Math.random() * 100000) + 1000,
      color: new THREE.Color().setHSL(Math.random(), 0.5, 0.5),
      size: Math.random() * 50000 + 10000,
    };
    
    return this.createGalaxy(config);
  }

  private createGalaxyVisualization(galaxy: Galaxy): void {
    if (!this.scene) return;
    
    // Create particle system for galaxy visualization
    const geometry = new THREE.BufferGeometry();
    const count = Math.min(galaxy.config.starCount, 5000);
    const positions = new Float32Array(count * 3);
    
    for (let i = 0; i < count; i++) {
      const star = galaxy.getStar(i);
      if (star) {
        positions[i * 3] = star.position.x;
        positions[i * 3 + 1] = star.position.y;
        positions[i * 3 + 2] = star.position.z;
      }
    }
    
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    
    const material = new THREE.PointsMaterial({
      size: 0.5,
      color: galaxy.config.color,
      transparent: true,
      opacity: 0.8,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
    });
    
    const points = new THREE.Points(geometry, material);
    this.scene.add(points);
    this.galaxyMeshes.push(points);
  }

  getGalaxies(): Galaxy[] {
    return this.galaxies;
  }

  getGalaxyCount(): number {
    return this.galaxies.length;
  }

  // Star system management
  createStarSystem(config: Partial<StarSystem['config']> = {}): StarSystem {
    const system = new StarSystem(config);
    this.starSystems.push(system);
    this.stars.push(system.star);
    
    return system;
  }

  createRandomStarSystem(): StarSystem {
    const starTypes = ['red-dwarf', 'yellow', 'blue', 'white', 'giant'];
    const type = starTypes[Math.floor(Math.random() * starTypes.length)];
    
    const config: Partial<StarSystem['config']> = {
      starType: type,
      starMass: Math.random() * 20 + 0.5,
      starRadius: Math.random() * 10 + 1,
      temperature: Math.random() * 30000 + 2000,
    };
    
    return this.createStarSystem(config);
  }

  getStarSystems(): StarSystem[] {
    return this.starSystems;
  }

  // Planet management
  createPlanet(config: Partial<SimPlanet['config']> = {}): SimPlanet {
    const planet = new SimPlanet(config);
    this.planets.push(planet);
    
    if (this.physics) {
      this.physics.registerObject({
        id: planet.id,
        position: planet.position,
        velocity: planet.velocity,
        acceleration: new THREE.Vector3(),
        mass: planet.config.mass,
        radius: planet.config.radius,
        isStatic: false,
        isKinematic: false,
        rotation: planet.rotation,
        angularVelocity: planet.angularVelocity,
        material: {
          density: planet.config.density,
          restitution: 0.5,
          friction: 0.5,
          buoyancy: 0.3,
          gravityFactor: 1,
        },
        forces: new THREE.Vector3(),
        torque: new THREE.Vector3(),
        isSleeping: false,
        collisionShape: 'sphere',
      });
    }
    
    return planet;
  }

  createRandomPlanet(): SimPlanet {
    const planetTypes = ['earth-like', 'ocean', 'desert', 'ice', 'lava', 'rocky', 'gas-giant', 'toxic'];
    const type = planetTypes[Math.floor(Math.random() * planetTypes.length)];
    
    const configs: Record<string, Partial<SimPlanet['config']>> = {
      'earth-like': {
        mass: 5.97e24,
        radius: 6371,
        density: 5514,
        temperature: 288,
        atmosphere: 1,
        oceanCoverage: 0.7,
      },
      'ocean': {
        mass: 4e24,
        radius: 7000,
        density: 4000,
        temperature: 275,
        atmosphere: 0.8,
        oceanCoverage: 0.95,
      },
      'desert': {
        mass: 4e24,
        radius: 6000,
        density: 5000,
        temperature: 350,
        atmosphere: 0.5,
        oceanCoverage: 0.1,
      },
      'ice': {
        mass: 3e24,
        radius: 5000,
        density: 2000,
        temperature: 180,
        atmosphere: 0.3,
        oceanCoverage: 0.3,
      },
      'lava': {
        mass: 5e24,
        radius: 6500,
        density: 6000,
        temperature: 1500,
        atmosphere: 0.4,
        oceanCoverage: 0,
      },
      'rocky': {
        mass: 3e24,
        radius: 5000,
        density: 5500,
        temperature: 200,
        atmosphere: 0.2,
        oceanCoverage: 0,
      },
      'gas-giant': {
        mass: 1.9e27,
        radius: 70000,
        density: 1300,
        temperature: 150,
        atmosphere: 1,
        oceanCoverage: 0,
      },
      'toxic': {
        mass: 4.5e24,
        radius: 6000,
        density: 4500,
        temperature: 320,
        atmosphere: 0.7,
        oceanCoverage: 0.2,
      },
    };
    
    const config = configs[type] || configs['rocky'];
    config.type = type;
    
    return this.createPlanet(config);
  }

  getPlanets(): SimPlanet[] {
    return this.planets;
  }

  getPlanet(id: string): SimPlanet | null {
    return this.planets.find(p => p.id === id) || null;
  }

  getPlanetCount(): number {
    return this.planets.length;
  }

  // Moon management
  createMoon(config: Partial<Moon['config']> = {}): Moon {
    const moon = new Moon(config);
    this.moons.push(moon);
    return moon;
  }

  getMoons(): Moon[] {
    return this.moons;
  }

  // Asteroid management
  createAsteroid(config: Partial<Asteroid['config']> = {}): Asteroid {
    const asteroid = new Asteroid(config);
    this.asteroids.push(asteroid);
    
    if (this.physics) {
      this.physics.registerObject({
        id: asteroid.id,
        position: asteroid.position,
        velocity: asteroid.velocity,
        acceleration: new THREE.Vector3(),
        mass: asteroid.config.mass,
        radius: asteroid.config.radius,
        isStatic: false,
        isKinematic: false,
        rotation: asteroid.rotation,
        angularVelocity: new THREE.Vector3(Math.random(), Math.random(), Math.random()),
        material: {
          density: asteroid.config.density,
          restitution: 0.3,
          friction: 0.5,
          buoyancy: 0.1,
          gravityFactor: 1,
        },
        forces: new THREE.Vector3(),
        torque: new THREE.Vector3(),
        isSleeping: false,
        collisionShape: 'sphere',
      });
    }
    
    return asteroid;
  }

  createRandomAsteroid(): Asteroid {
    const config: Partial<Asteroid['config']> = {
      mass: Math.random() * 1e15 + 1e10,
      radius: Math.random() * 5000 + 100,
      density: Math.random() * 3000 + 1000,
    };
    
    return this.createAsteroid(config);
  }

  getAsteroids(): Asteroid[] {
    return this.asteroids;
  }

  // Comet management
  createComet(config: Partial<Comet['config']> = {}): Comet {
    const comet = new Comet(config);
    this.comets.push(comet);
    return comet;
  }

  getComets(): Comet[] {
    return this.comets;
  }

  // Black hole management
  createBlackHole(config: Partial<SimBH['config']> = {}): SimBH {
    const blackHole = new SimBH(config);
    this.blackHoles.push(blackHole);
    return blackHole;
  }

  createRandomBlackHole(): SimBH {
    const types = ['stellar', 'intermediate', 'supermassive'];
    const type = types[Math.floor(Math.random() * types.length)];
    
    const configs: Record<string, Partial<SimBH['config']>> = {
      'stellar': {
        mass: Math.random() * 50 + 3,
        radius: 10,
        spin: Math.random() * 0.99,
      },
      'intermediate': {
        mass: Math.random() * 10000 + 100,
        radius: 100,
        spin: Math.random() * 0.99,
      },
      'supermassive': {
        mass: Math.random() * 1e10 + 1e6,
        radius: 10000,
        spin: Math.random() * 0.99,
      },
    };
    
    const config = configs[type] || configs['stellar'];
    config.type = type;
    
    return this.createBlackHole(config);
  }

  getBlackHoles(): SimBH[] {
    return this.blackHoles;
  }

  getBlackHole(id: string): SimBH | null {
    return this.blackHoles.find(bh => bh.id === id) || null;
  }

  // Wormhole management
  createWormhole(config: Partial<Wormhole['config']> = {}): Wormhole {
    const wormhole = new Wormhole(config);
    this.wormholes.push(wormhole);
    return wormhole;
  }

  getWormholes(): Wormhole[] {
    return this.wormholes;
  }

  // Spacecraft management
  createSpacecraft(config: Partial<Spacecraft['config']> = {}): Spacecraft {
    const spacecraft = new Spacecraft(config);
    this.spacecraft.push(spacecraft);
    return spacecraft;
  }

  getSpacecraft(): Spacecraft[] {
    return this.spacecraft;
  }

  // Focus management (for camera)
  focusOn(target: any): void {
    this.focusTarget = target;
  }

  follow(target: any): void {
    this.followTarget = target;
  }

  unfocus(): void {
    this.focusTarget = null;
    this.followTarget = null;
  }

  // Universe visualization
  createStarField(count: number = 5000): void {
    if (!this.scene) return;
    
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(count * 3);
    
    for (let i = 0; i < count; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 100000;
      positions[i * 3 + 1] = (Math.random() - 0.5) * 100000;
      positions[i * 3 + 2] = (Math.random() - 0.5) * 100000;
    }
    
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    
    const material = new THREE.PointsMaterial({
      size: 0.5,
      color: 0xffffff,
      sizeAttenuation: true,
      transparent: true,
      opacity: 0.8,
    });
    
    this.starField = new THREE.Points(geometry, material);
    this.scene.add(this.starField);
  }

  // Universe update
  update(delta: number, externalTime?: number): void {
    const time = externalTime !== undefined ? externalTime : this.getTime();
    
    // Update star systems
    this.starSystems.forEach(system => system.update(delta, time));
    
    // Update planets
    this.planets.forEach(planet => planet.update(delta, time));
    
    // Update moons
    this.moons.forEach(moon => moon.update(delta, time));
    
    // Update black holes
    this.blackHoles.forEach(bh => bh.update(delta, time));
    
    // Update asteroids
    this.asteroids.forEach(asteroid => asteroid.update(delta, time));
    
    // Update comets
    this.comets.forEach(comet => comet.update(delta, time));
    
    // Update spacecraft
    this.spacecraft.forEach(spacecraft => spacecraft.update(delta, time));
    
    // Update scale transition
    if (this.currentScale !== this.targetScale) {
      this.animateScaleTransition();
    }
  }

  // Universe time
  getTime(): number {
    return this.starSystems.reduce((sum, sys) => sum + sys.age, 0);
  }

  // Clear universe
  clear(): void {
    this.galaxies = [];
    this.starSystems = [];
    this.planets = [];
    this.moons = [];
    this.asteroids = [];
    this.comets = [];
    this.blackHoles = [];
    this.wormholes = [];
    this.spacecraft = [];
    this.stars = [];
    
    if (this.scene) {
      this.galaxyMeshes.forEach(mesh => this.scene!.remove(mesh));
      this.galaxyMeshes = [];
      
      if (this.starField) {
        this.scene.remove(this.starField);
        this.starField = null;
      }
    }
    
    this.physics?.clearObjects();
  }

  // Get statistics
  getStatistics(): any {
    return {
      galaxies: this.galaxies.length,
      starSystems: this.starSystems.length,
      stars: this.stars.length,
      planets: this.planets.length,
      moons: this.moons.length,
      asteroids: this.asteroids.length,
      comets: this.comets.length,
      blackHoles: this.blackHoles.length,
      wormholes: this.wormholes.length,
      spacecraft: this.spacecraft.length,
    };
  }

  // Load preset universe
  loadPreset(name: string): void {
    switch (name) {
      case 'solar-system':
        this.loadSolarSystem();
        break;
      case 'star-cluster':
        this.loadStarCluster();
        break;
      case 'galaxy':
        this.loadGalaxy();
        break;
      default:
        this.loadRandomUniverse();
    }
  }

  private loadSolarSystem(): void {
    const system = this.createStarSystem({
      starType: 'yellow',
      starMass: 1.989e30,
      starRadius: 696340,
      temperature: 5778,
    });
    
    const earth = this.createPlanet({
      type: 'earth-like',
      mass: 5.97e24,
      radius: 6371,
      density: 5514,
      temperature: 288,
      atmosphere: 1,
      oceanCoverage: 0.7,
    });
    
    system.addPlanet(earth);
    
    const moon = this.createMoon({
      mass: 7.35e22,
      radius: 1737,
      orbitRadius: 384400,
      orbitPeriod: 27.3,
    });
    
    earth.addMoon(moon);
  }

  private loadStarCluster(): void {
    const count = 50;
    for (let i = 0; i < count; i++) {
      this.createRandomStarSystem();
    }
  }

  private loadGalaxy(): void {
    const galaxy = this.createRandomGalaxy();
    
    // Add some star systems
    for (let i = 0; i < 20; i++) {
      const star = galaxy.getStar(Math.floor(Math.random() * galaxy.config.starCount));
      if (star) {
        this.createStarSystem({
          position: star.position.clone(),
          starType: 'random',
        });
      }
    }
  }

  private loadRandomUniverse(): void {
    // Random galaxies
    const galaxyCount = Math.floor(Math.random() * 5) + 1;
    for (let i = 0; i < galaxyCount; i++) {
      this.createRandomGalaxy();
    }
    
    // Random star systems
    const systemCount = Math.floor(Math.random() * 30) + 5;
    for (let i = 0; i < systemCount; i++) {
      this.createRandomStarSystem();
    }
    
    // Random planets
    const planetCount = Math.floor(Math.random() * 20) + 5;
    for (let i = 0; i < planetCount; i++) {
      this.createRandomPlanet();
    }
  }

  // Destroy universe
  destroy(): void {
    this.clear();
    this.physics = null;
    this.scene = null;
  }
}
