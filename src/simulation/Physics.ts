/**
 * Physics Engine - Unified physics framework
 */

import * as THREE from 'three';

export interface PhysicsObject {
  id: string;
  position: THREE.Vector3;
  velocity: THREE.Vector3;
  acceleration: THREE.Vector3;
  mass: number;
  radius: number;
  isStatic: boolean;
  isKinematic: boolean;
  rotation: THREE.Vector3;
  angularVelocity: THREE.Vector3;
  material: {
    density: number;
    restitution: number;
    friction: number;
    buoyancy: number;
    gravityFactor: number;
  };
  forces: THREE.Vector3;
  torque: THREE.Vector3;
  isSleeping: boolean;
  collisionShape: 'sphere' | 'box' | 'cylinder' | 'mesh' | 'none';
}

export interface CollisionResult {
  collided: boolean;
  objectA: PhysicsObject;
  objectB: PhysicsObject;
  contactPoint: THREE.Vector3;
  contactNormal: THREE.Vector3;
  penetrationDepth: number;
  relativeVelocity: number;
}

export interface WaterInteraction {
  object: PhysicsObject;
  waterLevel: number;
  submergedVolume: number;
  buoyancyForce: THREE.Vector3;
  dragForce: THREE.Vector3;
  isFloating: boolean;
  isSinking: boolean;
}

export class Physics {
  private objects: Map<string, PhysicsObject> = new Map();
  private gravity = new THREE.Vector3(0, -9.81, 0);
  private gravityFactor = 1;
  private waterLevel = 0;
  
  // Physics settings
  private settings = {
    enableGravity: true,
    enableCollisions: true,
    enableBuoyancy: true,
    enableDrag: true,
    enableFriction: true,
    enableSleeping: true,
    integrationMethod: 'euler', // euler, verlet, runge-kutta
    fixedTimeStep: 1 / 60,
    maxSubSteps: 10,
    restitution: 0.5,
    friction: 0.5,
    airDensity: 1.225,
    waterDensity: 1000,
    tolerance: 0.001,
  };
  
  // Spatial partitioning
  private spatialGrid: Map<string, PhysicsObject[]> = new Map();
  private gridCellSize = 10;
  
  // Collision callbacks
  private collisionCallbacks: ((result: CollisionResult) => void)[] = [];
  private waterInteractionCallbacks: ((interaction: WaterInteraction) => void)[] = [];

  // Object management
  registerObject(obj: PhysicsObject): void {
    this.objects.set(obj.id, obj);
    this.updateSpatialGrid();
  }

  unregisterObject(id: string): boolean {
    if (this.objects.delete(id)) {
      this.updateSpatialGrid();
      return true;
    }
    return false;
  }

  getObject(id: string): PhysicsObject | null {
    return this.objects.get(id) || null;
  }

  getAllObjects(): PhysicsObject[] {
    return Array.from(this.objects.values());
  }

  getAllActiveObjects(): PhysicsObject[] {
    return Array.from(this.objects.values()).filter(obj => !obj.isSleeping);
  }

  // Clear all objects
  clearObjects(): void {
    this.objects.clear();
    this.spatialGrid.clear();
  }

  // Physics settings
  setGravity(x: number, y: number, z: number): void {
    this.gravity.set(x, y, z);
  }

  setGravityVector(vec: THREE.Vector3): void {
    this.gravity.copy(vec);
  }

  getGravity(): THREE.Vector3 {
    return this.gravity.clone();
  }

  setGravityStrength(factor: number): void {
    this.gravityFactor = factor;
  }

  setWaterLevel(level: number): void {
    this.waterLevel = level;
  }

  getWaterLevel(): number {
    return this.waterLevel;
  }

  // Physics update
  update(deltaTime: number): void {
    const dt = Math.min(deltaTime, this.settings.fixedTimeStep * this.settings.maxSubSteps);
    const substeps = Math.ceil(dt / this.settings.fixedTimeStep);
    const subDt = dt / substeps;

    // Update spatial grid
    this.updateSpatialGrid();

    // Physics substeps
    for (let step = 0; step < substeps; step++) {
      this.updatePhysics(subDt);
    }

    // Apply gravity to non-static, non-sleeping objects
    if (this.settings.enableGravity) {
      this.applyGravity();
    }

    // Handle collisions
    if (this.settings.enableCollisions) {
      this.handleCollisions();
    }

    // Handle water interactions
    if (this.settings.enableBuoyancy) {
      this.handleWaterInteractions();
    }

    // Apply damping
    this.applyDamping();
  }

  private updatePhysics(dt: number): void {
    for (const obj of this.objects.values()) {
      if (obj.isStatic || obj.isKinematic || obj.isSleeping) continue;

      // Apply forces to acceleration
      obj.acceleration.copy(obj.forces).divideScalar(obj.mass);

      // Apply gravity
      if (this.settings.enableGravity) {
        obj.acceleration.add(this.gravity.clone().multiplyScalar(obj.material.gravityFactor * this.gravityFactor));
      }

      // Integrate (simple Euler for now)
      if (this.settings.integrationMethod === 'euler') {
        obj.velocity.add(obj.acceleration.clone().multiplyScalar(dt));
        obj.position.add(obj.velocity.clone().multiplyScalar(dt));
      }

      // Integrate rotation
      obj.rotation.x += obj.angularVelocity.x * dt;
      obj.rotation.y += obj.angularVelocity.y * dt;
      obj.rotation.z += obj.angularVelocity.z * dt;

      // Clear forces for next step
      obj.forces.set(0, 0, 0);
      obj.torque.set(0, 0, 0);

      // Check if object should sleep
      if (this.settings.enableSleeping) {
        const speed = obj.velocity.length();
        const angularSpeed = obj.angularVelocity.length();
        if (speed < this.settings.tolerance && angularSpeed < this.settings.tolerance) {
          obj.isSleeping = true;
        } else {
          obj.isSleeping = false;
        }
      }
    }
  }

  private applyGravity(): void {
    for (const obj of this.objects.values()) {
      if (obj.isStatic || obj.isKinematic) continue;
      
      const gravForce = this.gravity.clone()
        .multiplyScalar(obj.mass * obj.material.gravityFactor * this.gravityFactor);
      obj.forces.add(gravForce);
    }
  }

  private handleCollisions(): void {
    const objects = Array.from(this.objects.values());
    const objectsToCheck = this.settings.enableSleeping 
      ? objects.filter(obj => !obj.isSleeping) 
      : objects;

    for (let i = 0; i < objectsToCheck.length; i++) {
      for (let j = i + 1; j < objectsToCheck.length; j++) {
        const objA = objectsToCheck[i];
        const objB = objectsToCheck[j];
        
        if (objA.isStatic && objB.isStatic) continue;
        
        const result = this.checkCollision(objA, objB);
        
        if (result.collided) {
          this.resolveCollision(result);
          
          // Trigger callbacks
          this.collisionCallbacks.forEach(cb => cb(result));
        }
      }
    }
  }

  private checkCollision(objA: PhysicsObject, objB: PhysicsObject): CollisionResult {
    const result: CollisionResult = {
      collided: false,
      objectA: objA,
      objectB: objB,
      contactPoint: new THREE.Vector3(),
      contactNormal: new THREE.Vector3(),
      penetrationDepth: 0,
      relativeVelocity: 0,
    };

    // Sphere-sphere collision (most common)
    if (objA.collisionShape === 'sphere' && objB.collisionShape === 'sphere') {
      const diff = new THREE.Vector3().copy(objB.position).sub(objA.position);
      const dist = diff.length();
      const minDist = objA.radius + objB.radius;

      if (dist < minDist && dist > 0.001) {
        result.collided = true;
        result.contactPoint.copy(objA.position).add(diff.clone().multiplyScalar(objA.radius / dist));
        result.contactNormal.copy(diff).normalize();
        result.penetrationDepth = minDist - dist;
        
        // Relative velocity along normal
        const relVel = new THREE.Vector3().copy(objB.velocity).sub(objA.velocity);
        result.relativeVelocity = relVel.dot(result.contactNormal);
      }
    }

    return result;
  }

  private resolveCollision(result: CollisionResult): void {
    const { objectA, objectB, contactNormal, penetrationDepth, relativeVelocity } = result;
    
    if (objectA.isStatic && objectB.isStatic) return;
    
    // Calculate impulse
    const restitution = Math.min(objectA.material.restitution, objectB.material.restitution);
    const invMassA = objectA.isStatic ? 0 : 1 / objectA.mass;
    const invMassB = objectB.isStatic ? 0 : 1 / objectB.mass;
    const invMassTotal = invMassA + invMassB;
    
    if (invMassTotal === 0) return;
    
    // Relative velocity
    const relVel = new THREE.Vector3().copy(objectB.velocity).sub(objectA.velocity);
    const velAlongNormal = relVel.dot(contactNormal);
    
    // Don't resolve if velocities are separating
    if (velAlongNormal > 0) return;
    
    // Calculate impulse scalar
    const j = -(1 + restitution) * velAlongNormal / invMassTotal;
    
    // Apply impulse
    const impulse = contactNormal.clone().multiplyScalar(j);
    
    if (!objectA.isStatic) {
      objectA.velocity.add(impulse.clone().multiplyScalar(-invMassA / invMassA));
    }
    if (!objectB.isStatic) {
      objectB.velocity.add(impulse.clone().multiplyScalar(invMassB / invMassB));
    }
    
    // Position correction
    const correction = contactNormal.clone().multiplyScalar(penetrationDepth / invMassTotal);
    if (!objectA.isStatic) {
      objectA.position.add(correction.clone().multiplyScalar(-invMassA / invMassA));
    }
    if (!objectB.isStatic) {
      objectB.position.add(correction.clone().multiplyScalar(invMassB / invMassB));
    }
  }

  private handleWaterInteractions(): void {
    const waterY = this.waterLevel;
    
    for (const obj of this.objects.values()) {
      if (obj.isStatic || obj.isSleeping) continue;
      
      const interaction: WaterInteraction = {
        object: obj,
        waterLevel: waterY,
        submergedVolume: 0,
        buoyancyForce: new THREE.Vector3(),
        dragForce: new THREE.Vector3(),
        isFloating: false,
        isSinking: false,
      };
      
      // Check if object is in water
      const objectBottom = obj.position.y - obj.radius;
      const objectTop = obj.position.y + obj.radius;
      
      if (objectBottom < waterY && objectTop > waterY) {
        // Object is partially submerged
        const submergedHeight = waterY - objectBottom;
        const submergenceRatio = submergedHeight / (obj.radius * 2);
        interaction.submergedVolume = obj.radius * obj.radius * Math.PI * submergenceRatio * 4 / 3;
        interaction.isFloating = true;
        
        // Buoyancy force
        const buoyancyMagnitude = interaction.submergedVolume * this.settings.waterDensity * Math.abs(this.gravity.y);
        interaction.buoyancyForce.set(0, buoyancyMagnitude, 0);
        
        // Drag force
        const velocityInWater = new THREE.Vector3().copy(obj.velocity);
        velocityInWater.y = Math.min(0, velocityInWater.y); // Only drag when moving down
        const dragMagnitude = 0.5 * this.settings.waterDensity * velocityInWater.length() * velocityInWater.length() * obj.radius * obj.radius;
        if (dragMagnitude > 0) {
          const dragDir = velocityInWater.clone().normalize();
          interaction.dragForce.copy(dragDir.multiplyScalar(-dragMagnitude));
        }
        
        // Apply forces
        obj.forces.add(interaction.buoyancyForce);
        obj.forces.add(interaction.dragForce);
        
        // Check if sinking or floating
        if (buoyancyMagnitude < obj.mass * Math.abs(this.gravity.y)) {
          interaction.isSinking = true;
        }
      }
      
      this.waterInteractionCallbacks.forEach(cb => cb(interaction));
    }
  }

  private applyDamping(): void {
    const damping = 0.999;
    const angularDamping = 0.99;
    
    for (const obj of this.objects.values()) {
      if (obj.isStatic || obj.isKinematic || obj.isSleeping) continue;
      
      obj.velocity.multiplyScalar(damping);
      obj.angularVelocity.multiplyScalar(angularDamping);
    }
  }

  // Spatial grid for efficient collision detection
  private updateSpatialGrid(): void {
    this.spatialGrid.clear();
    
    for (const obj of this.objects.values()) {
      const cellKey = this.getCellKey(obj.position);
      if (!this.spatialGrid.has(cellKey)) {
        this.spatialGrid.set(cellKey, []);
      }
      this.spatialGrid.get(cellKey)!.push(obj);
    }
  }

  private getCellKey(position: THREE.Vector3): string {
    const x = Math.floor(position.x / this.gridCellSize);
    const y = Math.floor(position.y / this.gridCellSize);
    const z = Math.floor(position.z / this.gridCellSize);
    return `${x},${y},${z}`;
  }

  // Apply force to object
  applyForce(id: string, force: THREE.Vector3, point?: THREE.Vector3): void {
    const obj = this.objects.get(id);
    if (!obj || obj.isStatic) return;
    
    obj.forces.add(force);
    
    if (point) {
      const r = new THREE.Vector3().copy(point).sub(obj.position);
      obj.torque.add(new THREE.Vector3().crossVectors(r, force));
    }
  }

  applyForceAtPosition(id: string, force: THREE.Vector3, position: THREE.Vector3): void {
    this.applyForce(id, force, position);
  }

  // Apply impulse
  applyImpulse(id: string, impulse: THREE.Vector3): void {
    const obj = this.objects.get(id);
    if (!obj || obj.isStatic) return;
    
    obj.velocity.add(impulse.clone().divideScalar(obj.mass));
  }

  // Set velocity directly
  setVelocity(id: string, velocity: THREE.Vector3): void {
    const obj = this.objects.get(id);
    if (!obj || obj.isStatic || obj.isKinematic) return;
    
    obj.velocity.copy(velocity);
  }

  // Set position directly
  setPosition(id: string, position: THREE.Vector3): void {
    const obj = this.objects.get(id);
    if (!obj) return;
    
    obj.position.copy(position);
  }

  // Wake up sleeping object
  wakeUp(id: string): void {
    const obj = this.objects.get(id);
    if (obj) {
      obj.isSleeping = false;
    }
  }

  wakeAllObjects(): void {
    for (const obj of this.objects.values()) {
      obj.isSleeping = false;
    }
  }

  // Physics settings
  setEnableGravity(enabled: boolean): void {
    this.settings.enableGravity = enabled;
  }

  setEnableCollisions(enabled: boolean): void {
    this.settings.enableCollisions = enabled;
  }

  setEnableBuoyancy(enabled: boolean): void {
    this.settings.enableBuoyancy = enabled;
  }

  setEnableDrag(enabled: boolean): void {
    this.settings.enableDrag = enabled;
  }

  setEnableFriction(enabled: boolean): void {
    this.settings.enableFriction = enabled;
  }

  setEnableSleeping(enabled: boolean): void {
    this.settings.enableSleeping = enabled;
  }

  setRestitution(value: number): void {
    this.settings.restitution = Math.max(0, Math.min(1, value));
  }

  setFriction(value: number): void {
    this.settings.friction = Math.max(0, value);
  }

  // Collision callbacks
  onCollision(callback: (result: CollisionResult) => void): void {
    this.collisionCallbacks.push(callback);
  }

  offCollision(callback: (result: CollisionResult) => void): void {
    this.collisionCallbacks = this.collisionCallbacks.filter(cb => cb !== callback);
  }

  onWaterInteraction(callback: (interaction: WaterInteraction) => void): void {
    this.waterInteractionCallbacks.push(callback);
  }

  offWaterInteraction(callback: (interaction: WaterInteraction) => void): void {
    this.waterInteractionCallbacks = this.waterInteractionCallbacks.filter(cb => cb !== callback);
  }

  // Create physics object helpers
  createSphere(id: string, position: THREE.Vector3, radius: number, mass: number): PhysicsObject {
    return {
      id,
      position: position.clone(),
      velocity: new THREE.Vector3(),
      acceleration: new THREE.Vector3(),
      mass,
      radius,
      isStatic: false,
      isKinematic: false,
      rotation: new THREE.Vector3(),
      angularVelocity: new THREE.Vector3(),
      material: {
        density: mass / (4/3 * Math.PI * radius * radius * radius),
        restitution: 0.5,
        friction: 0.5,
        buoyancy: 0.5,
        gravityFactor: 1,
      },
      forces: new THREE.Vector3(),
      torque: new THREE.Vector3(),
      isSleeping: false,
      collisionShape: 'sphere',
    };
  }

  createStaticSphere(id: string, position: THREE.Vector3, radius: number): PhysicsObject {
    const obj = this.createSphere(id, position, radius, 0);
    obj.mass = 0;
    obj.isStatic = true;
    return obj;
  }

  // Get statistics
  getObjectCount(): number {
    return this.objects.size;
  }

  getActiveObjectCount(): number {
    return Array.from(this.objects.values()).filter(obj => !obj.isSleeping).length;
  }

  // Clear physics
  destroy(): void {
    this.objects.clear();
    this.spatialGrid.clear();
    this.collisionCallbacks = [];
    this.waterInteractionCallbacks = [];
  }
}
