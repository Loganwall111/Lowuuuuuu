/**
 * Time Controller - Manages simulation time with full control
 */

export type TimeMode = 'playing' | 'paused' | 'slowMotion' | 'fastForward' | 'stepping';

export class TimeController {
  private mode: TimeMode = 'playing';
  private speed: number = 1.0;
  private elapsed: number = 0; // Simulation time in seconds
  private deltaTime: number = 0;
  private lastTimestamp: number = 0;
  private accumulator: number = 0;
  
  // Callbacks
  private callbacks = {
    onTick: [] as ((delta: number, speed: number) => void)[],
    onModeChange: [] as ((mode: TimeMode) => void)[],
    onSpeedChange: [] as ((speed: number) => void)[],
    onPlay: [] as (() => void)[],
    onPause: [] as (() => void)[],
  };
  
  // Configuration
  private config = {
    minSpeed: 0,
    maxSpeed: 10,
    slowMotionSpeed: 0.2,
    fastForwardSpeed: 3,
    pausedSpeed: 0,
    defaultSpeed: 1,
    fixedTimeStep: 1 / 60,
    maxFrameTime: 0.25, // Prevent spiral of death
  };

  initialize(): void {
    this.lastTimestamp = performance.now();
    this.elapsed = 0;
    this.mode = 'playing';
    this.speed = this.config.defaultSpeed;
  }

  start(): void {
    this.lastTimestamp = performance.now();
    this.mode = 'playing';
    this.speed = this.config.defaultSpeed;
  }

  stop(): void {
    this.mode = 'paused';
  }

  update(delta: number): void {
    if (this.mode === 'paused') {
      this.deltaTime = 0;
      this.accumulator = 0;
      return;
    }

    // Clamp frame time to prevent spiral of death
    const frameTime = Math.min(delta, this.config.maxFrameTime);
    
    // Apply speed multiplier
    const effectiveDelta = frameTime * this.speed;
    
    // Use fixed time step for stability
    this.accumulator += effectiveDelta;
    
    let steps = 0;
    const maxSteps = 10; // Prevent infinite loop
    
    while (this.accumulator >= this.config.fixedTimeStep && steps < maxSteps) {
      this.deltaTime = this.config.fixedTimeStep;
      this.elapsed += this.deltaTime;
      this.accumulator -= this.config.fixedTimeStep;
      steps++;
      
      // Trigger tick for each fixed step
      this.callbacks.onTick.forEach(cb => cb(this.deltaTime, this.speed));
    }
    
    if (this.accumulator > this.config.fixedTimeStep) {
      this.accumulator = 0; // Discard excess to prevent drift
    }
    
    if (steps === 0) {
      this.deltaTime = 0;
    }
  }

  // Time control methods
  play(): void {
    if (this.mode === 'playing') return;
    
    const previousMode = this.mode;
    this.mode = 'playing';
    
    if (previousMode === 'paused') {
      this.callbacks.onPlay.forEach(cb => cb());
    }
    
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
  }

  pause(): void {
    if (this.mode === 'paused') return;
    
    const previousMode = this.mode;
    this.mode = 'paused';
    
    if (previousMode !== 'paused') {
      this.callbacks.onPause.forEach(cb => cb());
    }
    
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
  }

  togglePause(): void {
    if (this.mode === 'paused') {
      this.play();
    } else {
      this.pause();
    }
  }

  slowMotion(): void {
    this.speed = this.config.slowMotionSpeed;
    this.mode = 'slowMotion';
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
    this.callbacks.onSpeedChange.forEach(cb => cb(this.speed));
  }

  fastForward(): void {
    this.speed = this.config.fastForwardSpeed;
    this.mode = 'fastForward';
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
    this.callbacks.onSpeedChange.forEach(cb => cb(this.speed));
  }

  setSpeed(speed: number): void {
    this.speed = Math.max(this.config.minSpeed, Math.min(this.config.maxSpeed, speed));
    
    if (this.speed === 0) {
      this.pause();
    } else if (this.speed < 0.5) {
      this.mode = 'slowMotion';
    } else if (this.speed > 2) {
      this.mode = 'fastForward';
    } else {
      this.mode = 'playing';
    }
    
    this.callbacks.onSpeedChange.forEach(cb => cb(this.speed));
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
  }

  setMode(mode: TimeMode): void {
    this.mode = mode;
    
    switch (mode) {
      case 'playing':
        this.speed = this.config.defaultSpeed;
        break;
      case 'paused':
        this.speed = 0;
        break;
      case 'slowMotion':
        this.speed = this.config.slowMotionSpeed;
        break;
      case 'fastForward':
        this.speed = this.config.fastForwardSpeed;
        break;
      case 'stepping':
        this.speed = 0;
        break;
    }
    
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
    this.callbacks.onSpeedChange.forEach(cb => cb(this.speed));
  }

  step(): void {
    if (this.mode !== 'stepping' && this.mode !== 'paused') return;
    
    this.elapsed += this.config.fixedTimeStep;
    this.deltaTime = this.config.fixedTimeStep;
    this.callbacks.onTick.forEach(cb => cb(this.deltaTime, this.speed));
  }

  // Time accessors
  getElapsed(): number { return this.elapsed; }
  getDeltaTime(): number { return this.deltaTime; }
  getSpeed(): number { return this.speed; }
  getMode(): TimeMode { return this.mode; }
  isPlaying(): boolean { return this.mode === 'playing'; }
  isPaused(): boolean { return this.mode === 'paused'; }

  // Time formatting
  formatTime(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    const ms = Math.floor((seconds % 1) * 100);
    
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}.${ms.toString().padStart(2, '0')}`;
  }

  getFormattedElapsed(): string {
    return this.formatTime(this.elapsed);
  }

  // Aliases used by App.ts
  getSpeedMultiplier(): number { return this.speed; }
  getElapsedTime(): number { return this.elapsed; }

  // Reset time
  reset(): void {
    this.elapsed = 0;
    this.deltaTime = 0;
    this.accumulator = 0;
    this.mode = 'playing';
    this.speed = this.config.defaultSpeed;
    this.lastTimestamp = performance.now();
    
    this.callbacks.onModeChange.forEach(cb => cb(this.mode));
    this.callbacks.onSpeedChange.forEach(cb => cb(this.speed));
  }

  // Event subscriptions
  onTick(callback: (delta: number, speed: number) => void): void {
    this.callbacks.onTick.push(callback);
  }

  onModeChange(callback: (mode: TimeMode) => void): void {
    this.callbacks.onModeChange.push(callback);
  }

  onSpeedChange(callback: (speed: number) => void): void {
    this.callbacks.onSpeedChange.push(callback);
  }

  onPlay(callback: () => void): void {
    this.callbacks.onPlay.push(callback);
  }

  onPause(callback: () => void): void {
    this.callbacks.onPause.push(callback);
  }

  // Clear callbacks
  clearCallbacks(): void {
    this.callbacks = {
      onTick: [],
      onModeChange: [],
      onSpeedChange: [],
      onPlay: [],
      onPause: [],
    };
  }
}
