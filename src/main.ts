import { App } from './core/App';

// Entry point: App.ts owns construction and wiring of all subsystems.
const app = new App();

app.init().then(() => {
  app.start();
}).catch((err) => {
  console.error('Engine initialization failed:', err);
});

export { app };
