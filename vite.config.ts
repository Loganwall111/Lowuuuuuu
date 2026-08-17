import { defineConfig } from 'vite';

/**
 * The dev server has to satisfy two very different environments.
 *
 * In a cloud sandbox the page is served through an HTTPS proxy on a public
 * hostname, so the hot-reload socket must be told to connect on port 443 -
 * otherwise the browser tries the raw dev port, which is not exposed, and
 * live reload silently stops working.
 *
 * On a normal local machine that same setting is actively harmful: the
 * browser would try wss://localhost:443 when the server is really on 8080,
 * so every edit would require a manual refresh with no error to explain why.
 *
 * Defaulting to local and opting in to the proxy behaviour keeps `npm run
 * dev` working out of the box on a laptop, which is the common case. Set
 * HMR_CLIENT_PORT=443 when running behind an HTTPS proxy.
 */
const proxyPort = process.env.HMR_CLIENT_PORT
  ? Number(process.env.HMR_CLIENT_PORT)
  : undefined;

export default defineConfig({
  // GitHub Pages serves this repository from /Low/. Local and Arena builds
  // remain root-relative unless the deployment workflow opts into that base.
  base: process.env.VITE_BASE_PATH || '/',
  build: {
    target: 'esnext',
    outDir: 'dist',
    assetsDir: 'assets'
  },
  server: {
    port: 8080,
    // 0.0.0.0 also serves localhost, so this is safe locally and is what
    // lets a container or another device on the LAN reach the server.
    host: '0.0.0.0',
    cors: true,
    allowedHosts: true,
    ...(proxyPort ? { hmr: { clientPort: proxyPort } } : {}),
    headers: { 'X-Frame-Options': 'ALLOWALL' }
  }
});
