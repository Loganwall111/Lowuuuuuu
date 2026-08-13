import { App } from './bjs/App';

/**
 * Boot entry.
 *
 * The hard-won lesson here: every loading and error surface used to be built
 * by JavaScript, so any failure *before* that code ran left the user staring
 * at the page background with no explanation - a completely black screen.
 *
 * index.html now paints a lit background and a boot panel with no JS at all,
 * and exposes window.__bootFail(). This module's job is to take that panel
 * down when the first frame really is on screen, and to route every failure
 * into it otherwise.
 */

declare global {
  interface Window {
    __bootFail?: (why: string, detail?: unknown) => void;
    __appBooted?: boolean;
  }
}

function fail(why: string, detail: unknown): void {
  console.error(why, detail);
  const d = detail instanceof Error ? (detail.stack || detail.message) : String(detail);
  if (typeof window.__bootFail === 'function') window.__bootFail(why, d);
}

/** Takes down the static HTML boot panel. Safe to call more than once. */
function clearStaticBoot(): void {
  window.__appBooted = true;
  const sb = document.getElementById('staticBoot');
  if (sb) {
    sb.classList.add('gone');
    setTimeout(() => sb.remove(), 500);
  }
}

let app: App;
try {
  app = new App();
} catch (err) {
  // Constructing the app should never throw, but if it does there is no
  // shell to report through, so the static panel is the only way to speak.
  fail('The application could not be created.', err);
  throw err;
}

app.init()
  .then(() => {
    app.start();
    // Only claim success once a frame has actually been requested.
    clearStaticBoot();
  })
  .catch((err) => {
    fail('Boot failure while starting the simulation.', err);
    try {
      (app as any).shell?.showBootError(err);
      clearStaticBoot();
    } catch {
      document.querySelector('.boot')?.remove();
    }
  });

// Last-resort safety net for the app's own (JS-built) overlay.
window.setTimeout(() => {
  const boot = document.querySelector('.boot') as HTMLElement | null;
  if (boot && !(app as any).booted) {
    console.warn('Boot overlay still present after 15s - forcing dismissal.');
    boot.style.pointerEvents = 'none';
    boot.remove();
  }
}, 15000);

export { app };
