/**
 * HistorySystem — undo/redo and named snapshots.
 *
 * Worlds implement capture()/restore() over a plain serialisable state
 * object, so history works identically for any world without the history
 * code knowing anything about physics.
 */

export interface Snapshot<T = unknown> {
  id: string;
  label: string;
  time: number;
  state: T;
}

export interface Snapshotable<T = unknown> {
  captureState(): T;
  restoreState(state: T): void;
}

export class HistorySystem<T = unknown> {
  private undoStack: Snapshot<T>[] = [];
  private redoStack: Snapshot<T>[] = [];
  private saved: Snapshot<T>[] = [];
  private limit: number;
  private target: Snapshotable<T> | null = null;

  constructor(limit = 40) {
    this.limit = limit;
  }

  attach(target: Snapshotable<T> | null): void {
    this.target = target;
    this.undoStack = [];
    this.redoStack = [];
  }

  /** Records the current state before a destructive operation. */
  push(label: string): void {
    if (!this.target) return;
    try {
      this.undoStack.push({
        id: 'u' + Date.now() + Math.random().toString(36).slice(2, 6),
        label, time: Date.now(),
        state: this.target.captureState()
      });
      if (this.undoStack.length > this.limit) this.undoStack.shift();
      this.redoStack = [];   // a new action invalidates the redo branch
    } catch (e) {
      console.warn('history capture failed:', e);
    }
  }

  undo(): string | null {
    if (!this.target || !this.undoStack.length) return null;
    const entry = this.undoStack.pop()!;
    try {
      this.redoStack.push({
        id: 'r' + Date.now(), label: entry.label, time: Date.now(),
        state: this.target.captureState()
      });
      this.target.restoreState(entry.state);
      return entry.label;
    } catch (e) {
      console.warn('undo failed:', e);
      return null;
    }
  }

  redo(): string | null {
    if (!this.target || !this.redoStack.length) return null;
    const entry = this.redoStack.pop()!;
    try {
      this.undoStack.push({
        id: 'u' + Date.now(), label: entry.label, time: Date.now(),
        state: this.target.captureState()
      });
      this.target.restoreState(entry.state);
      return entry.label;
    } catch (e) {
      console.warn('redo failed:', e);
      return null;
    }
  }

  /** Named snapshot the user can return to at any time. */
  save(label: string): Snapshot<T> | null {
    if (!this.target) return null;
    const snap: Snapshot<T> = {
      id: 's' + Date.now() + Math.random().toString(36).slice(2, 6),
      label, time: Date.now(),
      state: this.target.captureState()
    };
    this.saved.push(snap);
    if (this.saved.length > 24) this.saved.shift();
    return snap;
  }

  load(id: string): boolean {
    const snap = this.saved.find((s) => s.id === id);
    if (!snap || !this.target) return false;
    this.push('before loading "' + snap.label + '"');
    this.target.restoreState(snap.state);
    return true;
  }

  remove(id: string): void {
    this.saved = this.saved.filter((s) => s.id !== id);
  }

  list(): Snapshot<T>[] {
    return [...this.saved].reverse();
  }

  canUndo(): boolean { return this.undoStack.length > 0; }
  canRedo(): boolean { return this.redoStack.length > 0; }
  undoLabel(): string | null { return this.undoStack.at(-1)?.label ?? null; }
  redoLabel(): string | null { return this.redoStack.at(-1)?.label ?? null; }

  clear(): void {
    this.undoStack = [];
    this.redoStack = [];
  }
}
