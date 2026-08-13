import { defineConfig } from 'vite';

export default defineConfig({
  base: '/',
  build: {
    target: 'esnext',
    outDir: 'dist',
    assetsDir: 'assets'
  },
  server: {
    port: 8080,
    host: '0.0.0.0',
    cors: true,
    allowedHosts: true,
    hmr: { clientPort: 443 },
    headers: { 'X-Frame-Options': 'ALLOWALL' }
  }
});
