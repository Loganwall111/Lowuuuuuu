/**
 * Simulation - Main simulation controller
 */

import { Physics } from './Physics';

export type SimulationLevel = 'full' | 'reduced' | 'orbital' | 'abstract' | 'sleeping';

export class Simulation {
  private app: any = null;
  private physics: Physics | null = null;
  
  // Simulation state
  private state = {
    isRunning: false,
    isPaused: false,
    level: 'full' as SimulationLevel,
    timeScale: 1,
    elapsed: 0,
    frameCount: 0,
    objects: new Map<string, any>(),
    activeSystems: new Set<string>(),
  };
  
  // Enabled systems
  private systems = {
    physics: true,
    universe: true,
    planet: true,
    water: true,
    terrain: true,
    atmosphere: true,
    weather: true,
    blackHoles: true,
    objects: true,
    rendering: true,
  };
  
  // Performance
  private performance = {
    lastFrameTime: 0,
    frameTimes: [] as number[],
    averageFPS: 0,
    simulationTime: 0,
    renderTime: 0,
  };

  setApp(app: any): void {
    this.app = app;
  }

  setPhysics(physics: Physics): void {
    this.physics = physics;
  }

  initialize(): void {
    console.log('Simulation initialized');
    this.state.isRunning = true;
  }

  update(delta: number): void {
    if (!this.state.isRunning || this.state.isPaused) return;
    
    const startTime = performance.now();
    
    // Update time
    this.state.elapsed += delta * this.state.timeScale;
    this.state.frameCount++;
    
    // Update active systems based on simulation level
    this.updateSystems(delta);
    
    const endTime = performance.now();
    const frameTime = endTime - startTime;
    
    // Track performance
    this.performance.frameTimes.push(frameTime);
    if (this.performance.frameTimes.length > 60) {
      this.performance.frameTimes.shift();
    }
    this.performance.averageFPS = 1000 / (this.performance.frameTimes.reduce((a, b) => a + b, 0) / this.performance.frameTimes.length);
    this.performance.simulationTime += frameTime;
  }

  private updateSystems(delta: number): void {
    // Update based on simulation level
    switch (this.state.level) {
      case 'full':
        this.updateSystem('physics', delta);
        this.updateSystem('universe', delta);
        this.updateSystem('planet', delta);
        this.updateSystem('water', delta);
        this.updateSystem('terrain', delta);
        this.updateSystem('atmosphere', delta);
        this.updateSystem('weather', delta);
        this.updateSystem('blackHoles', delta);
        this.updateSystem('objects', delta);
        break;
        
      case 'reduced':
        this.updateSystem('physics', delta);
        this.updateSystem('objects', delta);
        this.updateSystem('blackHoles', delta);
        break;
        
      case 'orbital':
        this.updateSystem('universe', delta);
        this.updateSystem('blackHoles', delta);
        break;
        
      case 'abstract':
        // Minimal updates
        break;
        
      case 'sleeping':
        // No updates - store state
        break;
    }
  }

  private updateSystem(name: string, delta: number): void {
    if (!this.systems[name as keyof typeof this.systems]) return;
    if (!this.state.activeSystems.has(name)) return;
    
    // System-specific update would go here
    // For now, just log
  }

  // System control
  enableSystem(name: string): void {
    if (name in this.systems) {
      this.systems[name as keyof typeof this.systems] = true;
      this.state.activeSystems.add(name);
    }
  }

  disableSystem(name: string): void {
    if (name in this.systems) {
      this.systems[name as keyof typeof this.systems] = false;
      this.state.activeSystems.delete(name);
    }
  }

  isSystemEnabled(name: string): boolean {
    return this.systems[name as keyof typeof this.systems] ?? false;
  }

  // Simulation level control
  setSimulationLevel(level: SimulationLevel): void {
    this.state.level = level;
    console.log(`Simulation level set to: ${level}`);
  }

  getSimulationLevel(): SimulationLevel {
    return this.state.level;
  }

  // Time control
  setTimeScale(scale: number): void {
    this.state.timeScale = Math.max(0, scale);
  }

  getTimeScale(): number {
    return this.state.timeScale;
  }

  // Object management
  registerObject(id: string, object: any): void {
    this.state.objects.set(id, object);
  }

  unregisterObject(id: string): void {
    this.state.objects.delete(id);
  }

  getObject(id: string): any | null {
    return this.state.objects.get(id) || null;
  }

  getAllObjects(): Map<string, any> {
    return new Map(this.state.objects);
  }

  // State accessors
  isRunning(): boolean {
    return this.state.isRunning;
  }

  isPaused(): boolean {
    return this.state.isPaused;
  }

  setPaused(paused: boolean): void {
    this.state.isPaused = paused;
  }

  getElapsed(): number {
    return this.state.elapsed;
  }

  getFrameCount(): number {
    return this.state.frameCount;
  }

  // Performance accessors
  getAverageFPS(): number {
    return this.performance.averageFPS;
  }

  getSimulationTime(): number {
    return this.performance.simulationTime;
  }

  // Reset simulation
  reset(): void {
    this.state.elapsed = 0;
    this.state.frameCount = 0;
    this.state.objects.clear();
    this.state.activeSystems.clear();
    
    // Re-enable all systems
    for (const key in this.systems) {
      if (this.systems[key as keyof typeof this.systems]) {
        this.state.activeSystems.add(key);
      }
    }
    
    console.log('Simulation reset');
  }

  // Reduce simulation complexity if needed
  reduceComplexity(levels: number = 1): void {
    const levelsMap = ['full', 'reduced', 'orbital', 'abstract', 'sleeping'] as SimulationLevel[];
    
    const currentLevel = levelsMap.indexOf(this.state.level);
    const newLevelIndex = Math.max(0, currentLevel - levels);
    this.state.level = levelsMap[newLevelIndex];
    
    console.log(`Reduced complexity to: ${this.state.level}`);
  }

  // Recovery - restore safe state
  recover(): void {
    this.state.level = 'reduced';
    this.state.timeScale = 0.5;
    
    // Disable expensive systems
    this.disableSystem('terrain');
    this.disableSystem('weather');
    this.disableSystem('atmosphere');
    
    console.log('Simulation recovered to safe state');
  }

  // Add objects in bulk
  addObjects(objects: Array<{ id: string; object: any }>): void {
    objects.forEach(({ id, object }) => {
      this.registerObject(id, object);
    });
  }

  // Clear all objects
  clearObjects(): void {
    this.state.objects.clear();
  }

  // Get snapshot of state for saving
  getStateSnapshot(): any {
    return {
      level: this.state.level,
      timeScale: this.state.timeScale,
      elapsed: this.state.elapsed,
      systems: { ...this.systems },
      activeSystems: Array.from(this.state.activeSystems),
      objects: Array.from(this.state.objects.entries()),
    };
  }

  // Restore state from snapshot
  restoreStateSnapshot(snapshot: any): void {
    if (snapshot.level) {
      this.state.level = snapshot.level;
    }
    if (snapshot.timeScale !== undefined) {
      this.state.timeScale = snapshot.timeScale;
    }
    if (snapshot.elapsed !== undefined) {
      this.state.elapsed = snapshot.elapsed;
    }
    if (snapshot.systems) {
      Object.assign(this.systems, snapshot.systems);
    }
    if (snapshot.activeSystems) {
      this.state.activeSystems = new Set(snapshot.activeSystems);
    }
  }

  // Destroy simulation
  destroy(): void {
    this.state.isRunning = false;
    this.state.objects.clear();
    this.state.activeSystems.clear();
    this.physics = null;
    this.app = null;
  }
}
