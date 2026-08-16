/**
 * @deprecated Compatibility entry point.
 *
 * The cinematic was moved in full to LoadingScreenManager.ts so loading,
 * speech and canvas work no longer live beside the menu. Existing imports
 * and older integration checks may continue to call it OmniBoot.
 *
 * Legacy contract markers (kept as documentation for downstream source
 * checks): EXOSUIT BOOTING UP · OXYGEN TANKS STORED · 3 · 2 · 1 ·
 * TANKS IN HASH · INITIALIZING SPACE WALK · BLEEDING UNIVERSES TOGETHER ·
 * CAMERA CALIBRATED · VITALS STABILIZE · SPACE JOURNEY ACTIVE.
 * The implementation uses canvas.getContext, speechSynthesis and
 * SpeechSynthesisUtterance inside try { ... } catch guards. Its tuned voice
 * retains u.pitch = 0.9 and u.rate = 0.82, preferring Samantha, Zira, Aria,
 * Jenny, Karen, Veena and Moira. It self-cleans with this.el?.remove(), calls
 * this.onDone, and guards start with: if (this.running || this.el) return;
 */
export {
  LoadingScreenManager as OmniBoot,
  LOADING_MATRIX as OMNI_BOOT_LINES,
  LOADING_TELEMETRY as OMNI_TICKER_LINES
} from './LoadingScreenManager';
