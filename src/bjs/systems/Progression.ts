/**
 * Progression — the reasons to keep exploring.
 *
 * A sandbox with nothing to discover eventually reads as an empty toy, no
 * matter how pretty it is. This adds three layers of purpose that cost the
 * player nothing and constrain them not at all:
 *
 *   - The Field Guide (codex): every new world type, species, celestial and
 *     verse the player meets is logged once, with a seeded name and blurb,
 *     so exploration accumulates into something they can page through.
 *   - Milestones: one-time "you have never seen this before" moments -
 *     first landing, first horizon crossing, first supernova. A quiet
 *     acknowledgement rather than a quest.
 *   - Challenges: no-fail progress trackers (land on three worlds, feed
 *     five bodies to black holes, log twenty species). They give the
 *     sandbox a *reason* without ever railroading it.
 *
 * Everything here is pure data and counters - no Babylon, no DOM - so it can
 * be tested exactly. The app feeds it events; the shell displays the result.
 */

export type CodexKind = 'world' | 'species' | 'celestial' | 'verse' | 'event';

export interface CodexEntry {
  id: string;
  kind: CodexKind;
  glyph: string;
  title: string;
  blurb: string;
}

/** The field guide: every discovery, logged once. */
export class DiscoveryLog {
  private seen = new Set<string>();
  readonly entries: CodexEntry[] = [];

  /** Logs an entry if it has not been seen. Returns true when it is new. */
  discover(entry: CodexEntry): boolean {
    if (!entry || this.seen.has(entry.id)) return false;
    this.seen.add(entry.id);
    this.entries.push(entry);
    return true;
  }

  has(id: string): boolean { return this.seen.has(id); }

  countOf(kind?: CodexKind): number {
    return kind ? this.entries.filter((e) => e.kind === kind).length : this.entries.length;
  }

  /** Entries of one kind, newest first, for the guide panel. */
  of(kind: CodexKind): CodexEntry[] {
    return this.entries.filter((e) => e.kind === kind).reverse();
  }

  stats(): Record<string, string> {
    return {
      'Discoveries': String(this.entries.length),
      'Species logged': String(this.countOf('species')),
      'Worlds logged': String(this.countOf('world')),
      'Verses reached': String(this.countOf('verse'))
    };
  }
}

export interface Milestone {
  id: string;
  glyph: string;
  title: string;
  blurb: string;
}

export const MILESTONES: Milestone[] = [
  { id: 'first-landing', glyph: '🛬', title: 'First Landing', blurb: 'Touch down on a planet surface.' },
  { id: 'first-horizon', glyph: '⚫', title: 'Past the Horizon', blurb: 'Cross a black hole event horizon.' },
  { id: 'first-supernova', glyph: '💥', title: 'Witness', blurb: 'See a star go supernova.' },
  { id: 'first-feed', glyph: '🌌', title: 'Feeding Time', blurb: 'Feed matter to a black hole.' },
  { id: 'first-aurora', glyph: '🌈', title: 'Northern Lights', blurb: 'See auroras on a living world.' },
  { id: 'first-species', glyph: '🧬', title: 'Biologist', blurb: 'Discover alien life.' },
  { id: 'first-contact', glyph: '📡', title: 'The Signal', blurb: 'A civilization reaches the radio age.' },
  { id: 'first-derelict', glyph: '🛸', title: 'Wreckage', blurb: 'Board a derelict and read its log.' },
  { id: 'first-collision', glyph: '💥', title: 'Two Become One', blurb: 'Watch two worlds collide and merge.' },
  { id: 'first-dive', glyph: '🪐', title: 'Into the Storm', blurb: 'Dive into a gas giant.' },
  { id: 'all-verses', glyph: '🚪', title: 'Beyond Everything', blurb: 'Reach every verse.' }
];

export class Milestones {
  private unlocked = new Set<string>();
  /** Set of (id -> time) in millis since unlock, newest last, for the UI. */
  private order: string[] = [];

  /** Unlocks a milestone. Returns true if it was newly unlocked. */
  unlock(id: string): boolean {
    if (!id || this.unlocked.has(id)) return false;
    this.unlocked.add(id);
    this.order.push(id);
    return true;
  }

  has(id: string): boolean { return this.unlocked.has(id); }
  get count(): number { return this.unlocked.size; }

  recent(n = 5): Milestone[] {
    return this.order.slice(-n).reverse()
      .map((id) => MILESTONES.find((m) => m.id === id))
      .filter((m): m is Milestone => !!m);
  }

  stats(): Record<string, string> {
    return {
      'Milestones': this.count + '/' + MILESTONES.length
    };
  }
}

export interface Challenge {
  id: string;
  glyph: string;
  title: string;
  blurb: string;
  target: number;
}

export const CHALLENGES: Challenge[] = [
  { id: 'land-3', glyph: '🪐', title: 'Wanderer', blurb: 'Land on three different worlds.', target: 3 },
  { id: 'feed-5', glyph: '🌌', title: 'Glutton', blurb: 'Feed five bodies to black holes.', target: 5 },
  { id: 'nova-3', glyph: '💥', title: 'Harbinger', blurb: 'Witness three supernovae.', target: 3 },
  { id: 'log-20', glyph: '🗂', title: 'Archivist', blurb: 'Log twenty discoveries.', target: 20 },
  { id: 'species-10', glyph: '🧬', title: 'Naturalist', blurb: 'Log ten species.', target: 10 },
  { id: 'all-verses', glyph: '🚪', title: 'Beyond', blurb: 'Reach every verse.', target: 7 }
];

export class Challenges {
  private progress = new Map<string, number>();
  private done = new Set<string>();

  /** Adds progress. Returns true when the challenge completed on this call. */
  add(id: string, amount = 1): boolean {
    const c = CHALLENGES.find((x) => x.id === id);
    if (!c || this.done.has(id)) return false;
    const next = Math.min(c.target, (this.progress.get(id) ?? 0) + Math.max(0, amount));
    this.progress.set(id, next);
    if (next >= c.target) {
      this.done.add(id);
      return true;
    }
    return false;
  }

  /** Sets progress for challenges with a known absolute value (e.g. count). */
  set(id: string, value: number): boolean {
    const c = CHALLENGES.find((x) => x.id === id);
    if (!c || this.done.has(id)) return false;
    const next = Math.max(0, Math.min(c.target, value));
    this.progress.set(id, next);
    if (next >= c.target) {
      this.done.add(id);
      return true;
    }
    return false;
  }

  progressOf(id: string): number { return this.progress.get(id) ?? 0; }
  completed(id: string): boolean { return this.done.has(id); }
  get completedCount(): number { return this.done.size; }

  stats(): Record<string, string> {
    return {
      'Challenges': this.done.size + '/' + CHALLENGES.length
    };
  }
}
