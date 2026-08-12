import { defineConfig } from 'vite'
export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    cors: true,
    hmr: { clientPort: 443 },
    headers: { 'X-Frame-Options': 'ALLOWALL' },
    allowedHosts: true
  },
  preview: {
    host: '0.0.0.0',
    port: 4173
  }
})
