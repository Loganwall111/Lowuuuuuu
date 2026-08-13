/**
 * Snapshot System - Manages simulation state snapshots for recovery and replay
 */

export interface SnapshotMetadata {
  id: string;
  timestamp: number;
  name: string;
  description?: string;
  type: 'auto' | 'manual' | 'bookmark' | 'recovery';
  state: SnapshotState;
}

export interface SnapshotState {
  // Time
  elapsed: number;
  speed: number;
  
  // Universe
  universe?: any;
  
  // Planet
  planet?: any;
  
  // Water
  water?: any;
  
  // Terrain
  terrain?: any;
  
  // Objects
  objects?: Array<any>;
  
  // Simulation state
  simulation?: any;
  
  // Black holes
  blackHoles?: Array<any>;
  
  // Custom data
  custom?: Record<string, any>;
}

export class SnapshotSystem {
  private snapshots: Map<string, SnapshotMetadata> = new Map();
  private snapshotOrder: string[] = [];
  private maxSnapshots = 100;
  
  // Auto-save configuration
  private autoSaveInterval = 30000; // 30 seconds
  private autoSaveTimer: ReturnType<typeof setTimeout> | null = null;
  private autoSaveEnabled = true;
  
  // Recovery state
  private lastGoodState: SnapshotMetadata | null = null;
  private recoveryMode = false;
  
  // Callbacks
  private callbacks = {
    onSnapshotCreate: [] as ((metadata: SnapshotMetadata) => void)[],
    onSnapshotRestore: [] as ((metadata: SnapshotMetadata) => void)[],
    onSnapshotDelete: [] as ((id: string) => void)[],
    onRecovery: [] as ((state: SnapshotState) => void)[],
    onError: [] as ((error: string) => void)[],
  };

  initialize(): void {
    console.log('Snapshot system initialized');
  }

  // Create a manual snapshot
  createSnapshot(name: string, description?: string, customData?: Record<string, any>): string {
    const id = this.generateSnapshotId();
    
    const metadata: SnapshotMetadata = {
      id,
      timestamp: Date.now(),
      name,
      description,
      type: 'manual',
      state: {
        elapsed: 0,
        speed: 1,
        custom: customData,
      }
    };
    
    this.snapshots.set(id, metadata);
    this.snapshotOrder.push(id);
    
    // Trim old snapshots if over limit
    this.trimSnapshots();
    
    this.callbacks.onSnapshotCreate.forEach(cb => cb(metadata));
    
    console.log(`Snapshot created: ${name} (${id})`);
    return id;
  }

  // Create an auto snapshot
  createAutoSnapshot(): string {
    if (!this.autoSaveEnabled) return '';
    
    const id = this.generateSnapshotId();
    const metadata: SnapshotMetadata = {
      id,
      timestamp: Date.now(),
      name: `Auto Save ${new Date().toLocaleTimeString()}`,
      type: 'auto',
      state: {
        elapsed: 0,
        speed: 1,
      }
    };
    
    this.snapshots.set(id, metadata);
    this.snapshotOrder.push(id);
    
    // Keep only last 10 auto snapshots
    this.trimAutoSnapshots();
    
    this.callbacks.onSnapshotCreate.forEach(cb => cb(metadata));
    
    // Update last good state
    this.lastGoodState = metadata;
    
    return id;
  }

  // Create a bookmark
  createBookmark(name: string, description?: string): string {
    const id = this.generateSnapshotId();
    
    const metadata: SnapshotMetadata = {
      id,
      timestamp: Date.now(),
      name,
      description,
      type: 'bookmark',
      state: {
        elapsed: 0,
        speed: 1,
      }
    };
    
    this.snapshots.set(id, metadata);
    this.snapshotOrder.push(id);
    
    this.callbacks.onSnapshotCreate.forEach(cb => cb(metadata));
    
    console.log(`Bookmark created: ${name}`);
    return id;
  }

  // Create a recovery snapshot
  createRecoverySnapshot(state: SnapshotState): string {
    const id = this.generateSnapshotId();
    
    const metadata: SnapshotMetadata = {
      id,
      timestamp: Date.now(),
      name: 'Recovery Point',
      type: 'recovery',
      state: { ...state }
    };
    
    this.snapshots.set(id, metadata);
    this.snapshotOrder.push(id);
    
    this.lastGoodState = metadata;
    
    return id;
  }

  // Restore a snapshot
  restoreSnapshot(id: string): boolean {
    const metadata = this.snapshots.get(id);
    if (!metadata) {
      this.callbacks.onError.forEach(cb => cb(`Snapshot not found: ${id}`));
      return false;
    }
    
    this.callbacks.onSnapshotRestore.forEach(cb => cb(metadata));
    this.callbacks.onRecovery.forEach(cb => cb(metadata.state));
    
    console.log(`Snapshot restored: ${metadata.name}`);
    return true;
  }

  // Restore the last good state
  restoreLastGoodState(): boolean {
    if (!this.lastGoodState) {
      this.callbacks.onError.forEach(cb => cb('No last good state available'));
      return false;
    }
    
    return this.restoreSnapshot(this.lastGoodState.id);
  }

  // Delete a snapshot
  deleteSnapshot(id: string): boolean {
    if (!this.snapshots.has(id)) return false;
    
    this.snapshots.delete(id);
    this.snapshotOrder = this.snapshotOrder.filter(sid => sid !== id);
    
    this.callbacks.onSnapshotDelete.forEach(cb => cb(id));
    
    // Update last good state if needed
    if (this.lastGoodState?.id === id) {
      this.lastGoodState = null;
    }
    
    console.log(`Snapshot deleted: ${id}`);
    return true;
  }

  // Get snapshot by ID
  getSnapshot(id: string): SnapshotMetadata | null {
    return this.snapshots.get(id) || null;
  }

  // Get all snapshots
  getAllSnapshots(): SnapshotMetadata[] {
    return this.snapshotOrder
      .map(id => this.snapshots.get(id))
      .filter((m): m is SnapshotMetadata => m !== null)
      .sort((a, b) => b.timestamp - a.timestamp);
  }

  // Get snapshots by type
  getSnapshotsByType(type: SnapshotMetadata['type']): SnapshotMetadata[] {
    return this.getAllSnapshots().filter(s => s.type === type);
  }

  // Get manual snapshots
  getManualSnapshots(): SnapshotMetadata[] {
    return this.getSnapshotsByType('manual');
  }

  // Get auto snapshots
  getAutoSnapshots(): SnapshotMetadata[] {
    return this.getSnapshotsByType('auto');
  }

  // Get bookmarks
  getBookmarks(): SnapshotMetadata[] {
    return this.getSnapshotsByType('bookmark');
  }

  // Get count of snapshots
  getSnapshotCount(): number {
    return this.snapshots.size;
  }

  // Check if snapshot exists
  hasSnapshot(id: string): boolean {
    return this.snapshots.has(id);
  }

  // Clear all snapshots
  clearAll(): void {
    this.snapshots.clear();
    this.snapshotOrder = [];
    this.lastGoodState = null;
    
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
      this.autoSaveTimer = null;
    }
    
    console.log('All snapshots cleared');
  }

  // Clear only auto snapshots
  clearAutoSnapshots(): void {
    const autoIds = this.getAutoSnapshots().map(s => s.id);
    autoIds.forEach(id => this.deleteSnapshot(id));
  }

  // Configure auto-save
  setAutoSaveInterval(intervalMs: number): void {
    this.autoSaveInterval = Math.max(1000, intervalMs);
    
    if (this.autoSaveEnabled) {
      this.startAutoSave();
    }
  }

  startAutoSave(): void {
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
    }
    
    if (!this.autoSaveEnabled) return;
    
    this.autoSaveTimer = setInterval(() => {
      this.createAutoSnapshot();
    }, this.autoSaveInterval);
    
    console.log(`Auto-save started (every ${this.autoSaveInterval}ms)`);
  }

  stopAutoSave(): void {
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
      this.autoSaveTimer = null;
    }
    
    console.log('Auto-save stopped');
  }

  setAutoSaveEnabled(enabled: boolean): void {
    this.autoSaveEnabled = enabled;
    
    if (enabled) {
      this.startAutoSave();
    } else {
      this.stopAutoSave();
    }
  }

  // Recovery mode
  enterRecoveryMode(): void {
    this.recoveryMode = true;
    console.log('Recovery mode entered');
  }

  exitRecoveryMode(): void {
    this.recoveryMode = false;
    console.log('Recovery mode exited');
  }

  isInRecoveryMode(): boolean {
    return this.recoveryMode;
  }

  // Get last good state
  getLastGoodState(): SnapshotMetadata | null {
    return this.lastGoodState;
  }

  // Update snapshot state (for saving current state)
  updateSnapshotState(id: string, state: Partial<SnapshotState>): boolean {
    const metadata = this.snapshots.get(id);
    if (!metadata) return false;
    
    metadata.state = { ...metadata.state, ...state };
    return true;
  }

  // Generate unique ID
  private generateSnapshotId(): string {
    return `snap-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  // Trim old snapshots
  private trimSnapshots(): void {
    while (this.snapshots.size > this.maxSnapshots) {
      const oldestId = this.snapshotOrder.shift();
      if (oldestId) {
        this.snapshots.delete(oldestId);
      }
    }
  }

  // Trim auto snapshots to keep only last 10
  private trimAutoSnapshots(): void {
    const autoSnapshots = this.getAutoSnapshots();
    if (autoSnapshots.length <= 10) return;
    
    const toDelete = autoSnapshots.slice(10);
    toDelete.forEach(s => this.deleteSnapshot(s.id));
  }

  // Event subscriptions
  onSnapshotCreate(callback: (metadata: SnapshotMetadata) => void): void {
    this.callbacks.onSnapshotCreate.push(callback);
  }

  onSnapshotRestore(callback: (metadata: SnapshotMetadata) => void): void {
    this.callbacks.onSnapshotRestore.push(callback);
  }

  onSnapshotDelete(callback: (id: string) => void): void {
    this.callbacks.onSnapshotDelete.push(callback);
  }

  onRecovery(callback: (state: SnapshotState) => void): void {
    this.callbacks.onRecovery.push(callback);
  }

  onError(callback: (error: string) => void): void {
    this.callbacks.onError.push(callback);
  }

  // Clear callbacks
  clearCallbacks(): void {
    this.callbacks = {
      onSnapshotCreate: [],
      onSnapshotRestore: [],
      onSnapshotDelete: [],
      onRecovery: [],
      onError: [],
    };
  }

  // Destroy
  destroy(): void {
    this.clearAll();
    this.clearCallbacks();
  }
}
