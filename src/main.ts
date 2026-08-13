import { App } from './bjs/App';

const app = new App();

/**
 * The boot overlay is opaque and sits above everything, so any failure during
 * init() would otherwise present as a black screen with unclickable UI.
 * These handlers guarantee it always comes down and the error is visible.
 */
app.init()
  .then(() => app.start())
  .catch((err) => {
    console.error('Boot failure:', err);
    try {
      (app as any).shell?.showBootError(err);
    } catch {
      document.querySelector('.boot')?.remove();
    }
  });

// Last-resort safety net: if anything leaves the overlay up, drop it.
window.setTimeout(() => {
  const boot = document.querySelector('.boot') as HTMLElement | null;
  if (boot && !(app as any).booted) {
    console.warn('Boot overlay still present after 15s - forcing dismissal.');
    boot.style.pointerEvents = 'none';
    boot.remove();
  }
}, 15000);

export { app };
