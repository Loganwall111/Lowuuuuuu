/**
 * SaveSystem — durable persistence with autosave and crash recovery.
 *
 * Everything is stored under a versioned key so a future format change can
 * migrate rather than corrupt. Every read is defensive: a corrupt or
 * truncated entry is discarded and reported instead of throwing, because a
 * bad save must never prevent the app from starting.
 */

export const SAVE_VERSION = 1;
const KEY_PREFIX = 'ups.save.v' + SAVE_VERSION + '.';
const AUTOSAVE_KEY = KEY_PREFIX + 'autosave';
const INDEX_KEY = KEY_PREFIX + 'index';
const PREFS_KEY = KEY_PREFIX + 'prefs';

export interface SaveEntry {
  id: string;
  name: string;
  world: string;
  time: number;
  version: number;
  data: unknown;
}

export interface StorageLike {
  getItem(k: string): string | null;
  setItem(k: string, v: string): void;
  removeItem(k: string): void;
}

/** In-memory fallback so the app still works with storage disabled. */
class MemoryStore implements StorageLike {
  private m = new Map<string, string>();
  getItem(k: string): string | null { return this.m.has(k) ? this.m.get(k)! : null; }
  setItem(k: string, v: string): void { this.m.set(k, v); }
  removeItem(k: string): void { this.m.delete(k); }
}

function defaultStore(): StorageLike {
  try {
    if (typeof localStorage !== 'undefined') {
      const probe = '__ups_probe__';
      localStorage.setItem(probe, '1');
      localStorage.removeItem(probe);
      return localStorage;
    }
  } catch {
    // private browsing or blocked storage - fall through
  }
  return new MemoryStore();
}

export class SaveSystem {
  private store: StorageLike;
  private autosaveTimer = 0;
  autosaveInterval = 20;
  lastError: string | null = null;

  constructor(store?: StorageLike) {
    this.store = store ?? defaultStore();
  }

  /* ------------------------------- named saves ------------------------------- */

  save(name: string, world: string, data: unknown): SaveEntry | null {
    const entry: SaveEntry = {
      id: 'sv' + Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
      name, world, time: Date.now(), version: SAVE_VERSION, data
    };
    try {
      this.store.setItem(KEY_PREFIX + entry.id, JSON.stringify(entry));
      const idx = this.index();
      idx.push({ id: entry.id, name, world, time: entry.time });
      this.store.setItem(INDEX_KEY, JSON.stringify(idx));
      this.lastError = null;
      return entry;
    } catch (e) {
      // quota exceeded is the common case; drop the oldest save and retry once
      this.lastError = String(e);
      const idx = this.index();
      if (idx.length) {
        this.remove(idx[0].id);
        try {
          this.store.setItem(KEY_PREFIX + entry.id, JSON.stringify(entry));
          const i2 = this.index();
          i2.push({ id: entry.id, name, world, time: entry.time });
          this.store.setItem(INDEX_KEY, JSON.stringify(i2));
          this.lastError = null;
          return entry;
        } catch { /* give up quietly */ }
      }
      return null;
    }
  }

  load(id: string): SaveEntry | null {
    try {
      const raw = this.store.getItem(KEY_PREFIX + id);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as SaveEntry;
      if (!parsed || typeof parsed !== 'object' || parsed.version !== SAVE_VERSION) {
        this.lastError = 'incompatible save version';
        return null;
      }
      return parsed;
    } catch (e) {
      this.lastError = 'corrupt save discarded: ' + String(e);
      this.remove(id);
      return null;
    }
  }

  remove(id: string): void {
    try {
      this.store.removeItem(KEY_PREFIX + id);
      this.store.setItem(INDEX_KEY,
        JSON.stringify(this.index().filter((e) => e.id !== id)));
    } catch { /* ignore */ }
  }

  index(): { id: string; name: string; world: string; time: number }[] {
    try {
      const raw = this.store.getItem(INDEX_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  list(): { id: string; name: string; world: string; time: number }[] {
    return [...this.index()].reverse();
  }

  /* -------------------------------- autosave -------------------------------- */

  /** Call every frame; writes at most once per `autosaveInterval` seconds. */
  tick(dt: number, capture: () => { world: string; data: unknown } | null): boolean {
    this.autosaveTimer += dt;
    if (this.autosaveTimer < this.autosaveInterval) return false;
    this.autosaveTimer = 0;
    return this.autosave(capture);
  }

  autosave(capture: () => { world: string; data: unknown } | null): boolean {
    try {
      const snap = capture();
      if (!snap) return false;
      this.store.setItem(AUTOSAVE_KEY, JSON.stringify({
        id: 'autosave', name: 'Autosave', world: snap.world,
        time: Date.now(), version: SAVE_VERSION, data: snap.data
      }));
      return true;
    } catch (e) {
      this.lastError = String(e);
      return false;
    }
  }

  /** Returns the autosave if one exists and is readable. */
  recover(): SaveEntry | null {
    try {
      const raw = this.store.getItem(AUTOSAVE_KEY);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as SaveEntry;
      if (!parsed || parsed.version !== SAVE_VERSION) return null;
      return parsed;
    } catch {
      try { this.store.removeItem(AUTOSAVE_KEY); } catch { /* ignore */ }
      return null;
    }
  }

  clearAutosave(): void {
    try { this.store.removeItem(AUTOSAVE_KEY); } catch { /* ignore */ }
  }

  /* -------------------------------- preferences ------------------------------- */

  getPrefs<T extends object>(fallback: T): T {
    try {
      const raw = this.store.getItem(PREFS_KEY);
      if (!raw) return fallback;
      const parsed = JSON.parse(raw);
      return (parsed && typeof parsed === 'object')
        ? { ...fallback, ...parsed } : fallback;
    } catch {
      return fallback;
    }
  }

  setPrefs(prefs: object): void {
    try {
      this.store.setItem(PREFS_KEY, JSON.stringify(prefs));
    } catch { /* ignore */ }
  }

  /** Export as a downloadable JSON string. */
  exportAll(): string {
    return JSON.stringify({
      version: SAVE_VERSION,
      exported: Date.now(),
      saves: this.index().map((e) => this.load(e.id)).filter(Boolean)
    }, null, 2);
  }

  /** Imports a previously exported bundle. Returns how many were restored. */
  importAll(json: string): number {
    try {
      const parsed = JSON.parse(json);
      if (!parsed?.saves || !Array.isArray(parsed.saves)) return 0;
      let n = 0;
      for (const s of parsed.saves) {
        if (s && s.data !== undefined && this.save(s.name ?? 'Imported', s.world ?? 'sandbox', s.data)) n++;
      }
      return n;
    } catch {
      return 0;
    }
  }
}
