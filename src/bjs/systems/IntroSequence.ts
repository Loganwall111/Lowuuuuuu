/**
 * IntroSequence — the opening, replacing the old main menu.
 *
 * The flow is:
 *
 *   title  -> click Play
 *   garage -> an infinite white room; walk to the door
 *   lesson -> people there teach you how to play, one rule at a time
 *   portal -> step through it
 *   ship   -> you arrive on a ship. The ship IS the main menu: the buttons
 *             are objects in the room and you can walk and jump in front of
 *             them.
 *
 * This module is the state machine only: no rendering, no DOM. Keeping it
 * separate means the sequence can be tested exhaustively, and the garage,
 * the ship and the tutorial can each be built as ordinary worlds.
 */

export type IntroStage = 'title' | 'garage' | 'lesson' | 'portal' | 'ship' | 'playing';

/** Ordered, because progress is a walk along this list. */
export const STAGES: IntroStage[] = [
  'title', 'garage', 'lesson', 'portal', 'ship', 'playing'
];

export interface Lesson {
  id: string;
  /** Who is talking. */
  speaker: string;
  text: string;
  /** Keys this lesson is about, highlighted while it is on screen. */
  keys: string[];
  /** The player must do this before the lesson will advance. */
  requires: 'none' | 'move' | 'look' | 'jump' | 'interact';
}

/**
 * The rules, taught by the people in the garage. Deliberately short: the
 * point is to be flying within a minute, not to read a manual.
 */
export const LESSONS: Lesson[] = [
  {
    id: 'welcome',
    speaker: 'Instructor',
    text: 'You are awake. Good. This room is not real, but the next one is.',
    keys: [],
    requires: 'none'
  },
  {
    id: 'move',
    speaker: 'Instructor',
    text: 'Walk with W A S D. Go on, try it.',
    keys: ['W', 'A', 'S', 'D'],
    requires: 'move'
  },
  {
    id: 'look',
    speaker: 'Instructor',
    text: 'Move the mouse to look around. Press C to lock the cursor.',
    keys: ['Mouse', 'C'],
    requires: 'look'
  },
  {
    id: 'jump',
    speaker: 'Technician',
    text: 'Space to jump. You will need it - not everything has stairs.',
    keys: ['Space'],
    requires: 'jump'
  },
  {
    id: 'zoom',
    speaker: 'Technician',
    text: 'Hold Shift and roll the wheel to zoom. Z puts it back.',
    keys: ['Shift', 'Wheel', 'Z'],
    requires: 'none'
  },
  {
    id: 'throttle',
    speaker: 'Pilot',
    text: 'Out there the wheel is your throttle. Roll it up and the stars streak.',
    keys: ['Wheel'],
    requires: 'none'
  },
  {
    id: 'grab',
    speaker: 'Pilot',
    text: 'G grabs whatever you are looking at. B throws it. Yes, at planets.',
    keys: ['G', 'B', 'V'],
    requires: 'none'
  },
  {
    id: 'freedom',
    speaker: 'Instructor',
    text: 'There is no objective. Build something, break it, watch what happens.',
    keys: [],
    requires: 'none'
  },
  {
    id: 'portal',
    speaker: 'Instructor',
    text: 'The portal behind me goes to your ship. Step through when you are ready.',
    keys: [],
    requires: 'interact'
  }
];

/** What the ship's console offers. These are objects in the room, not a list. */
export interface ShipStation {
  id: string;
  label: string;
  hint: string;
  /** Where it sits in the ship, in metres from the centre. */
  position: [number, number, number];
  glyph: string;
}

export const SHIP_STATIONS: ShipStation[] = [
  { id: 'play', label: 'Launch', hint: 'Drop into the universe',
    position: [0, 0, 6], glyph: '▶' },
  { id: 'universe', label: 'New Universe', hint: 'Reseed everything',
    position: [-5, 0, 3], glyph: '✦' },
  { id: 'graphics', label: 'Graphics', hint: 'Fidelity and effects',
    position: [5, 0, 3], glyph: '◈' },
  { id: 'presets', label: 'Presets', hint: 'Prebuilt scenarios',
    position: [-6.5, 0, -2], glyph: '⬡' },
  { id: 'library', label: 'Library', hint: 'Things you can spawn',
    position: [6.5, 0, -2], glyph: '❋' },
  { id: 'load', label: 'Continue', hint: 'Your last universe',
    position: [0, 0, -6], glyph: '⟲' }
];

export interface IntroState {
  stage: IntroStage;
  /** Index into LESSONS while in the lesson stage. */
  lesson: number;
  /** True once the whole intro has been finished or skipped. */
  done: boolean;
}

export class IntroSequence {
  private stage: IntroStage = 'title';
  private lessonIdx = 0;
  private finished = false;
  private taught = new Set<string>();
  private listeners: ((s: IntroState) => void)[] = [];

  get state(): IntroState {
    return { stage: this.stage, lesson: this.lessonIdx, done: this.finished };
  }

  get currentLesson(): Lesson | null {
    if (this.stage !== 'lesson') return null;
    return LESSONS[this.lessonIdx] ?? null;
  }

  /** Fraction of the intro completed, for a progress pip. */
  get progress(): number {
    const i = STAGES.indexOf(this.stage);
    const base = i / (STAGES.length - 1);
    if (this.stage === 'lesson' && LESSONS.length > 0) {
      const within = this.lessonIdx / LESSONS.length;
      return base + within / (STAGES.length - 1);
    }
    return base;
  }

  onChange(fn: (s: IntroState) => void): void { this.listeners.push(fn); }

  private emit(): void {
    const s = this.state;
    this.listeners.forEach((f) => f(s));
  }

  /** Advances to the next stage. Called by Play, by the door, by the portal. */
  advance(): IntroStage {
    const i = STAGES.indexOf(this.stage);
    if (i < 0 || i >= STAGES.length - 1) {
      this.finished = true;
      this.emit();
      return this.stage;
    }
    this.stage = STAGES[i + 1];
    if (this.stage === 'playing') this.finished = true;
    this.emit();
    return this.stage;
  }

  /**
   * Advances one lesson. Returns true while there are lessons left; when the
   * last one is acknowledged it moves on to the portal by itself, so the
   * player is never left waiting for a prompt that will not come.
   */
  nextLesson(): boolean {
    if (this.stage !== 'lesson') return false;
    const cur = LESSONS[this.lessonIdx];
    if (cur) this.taught.add(cur.id);
    if (this.lessonIdx < LESSONS.length - 1) {
      this.lessonIdx++;
      this.emit();
      return true;
    }
    this.advance();     // -> portal
    return false;
  }

  /** True if the player has been shown a given rule. */
  hasLearned(id: string): boolean { return this.taught.has(id); }

  /**
   * Reports something the player did. If the current lesson was waiting for
   * exactly that, it completes. Lets the tutorial respond to actions rather
   * than making everything a click-through.
   */
  didAction(action: Lesson['requires']): boolean {
    const cur = this.currentLesson;
    if (!cur || cur.requires === 'none' || cur.requires !== action) return false;
    return this.nextLesson() || true;
  }

  /**
   * Skips the whole intro. Anyone on their second run wants this, and it must
   * always be available - being trapped in a tutorial is unforgivable.
   */
  skip(): void {
    this.stage = 'playing';
    this.lessonIdx = LESSONS.length;
    LESSONS.forEach((l) => this.taught.add(l.id));
    this.finished = true;
    this.emit();
  }

  /** Jumps straight to the ship, i.e. the main menu, skipping the tutorial. */
  toShip(): void {
    this.stage = 'ship';
    this.lessonIdx = LESSONS.length;
    this.emit();
  }

  reset(): void {
    this.stage = 'title';
    this.lessonIdx = 0;
    this.finished = false;
    this.taught.clear();
    this.emit();
  }

  /** Which world should be loaded for the current stage. */
  worldFor(): string {
    switch (this.stage) {
      case 'title': return 'garage';   // the sim renders behind the title
      case 'garage':
      case 'lesson':
      case 'portal': return 'garage';
      case 'ship': return 'ship';
      default: return 'planetary';
    }
  }

  stats(): Record<string, string> {
    return {
      'Intro stage': this.stage,
      'Rules learned': `${this.taught.size} / ${LESSONS.length}`
    };
  }
}
