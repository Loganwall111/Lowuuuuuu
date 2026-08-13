import { App } from './bjs/App';

const app = new App();
app.init()
  .then(() => app.start())
  .catch((err) => {
    console.error('Boot failure:', err);
    const m = document.getElementById('bootMsg');
    if (m) m.textContent = 'error: ' + (err?.message ?? err);
  });

export { app };
