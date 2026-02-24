import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  base: '/',
  build: { outDir: 'dist', assetsDir: 'assets' },
  server: {
    proxy: {
      '/api': { target: 'https://api.dearfuture.info', changeOrigin: true },
      '/login': { target: 'https://api.dearfuture.info', changeOrigin: true },
      '/oauth2': { target: 'https://api.dearfuture.info', changeOrigin: true },
    },
  },
});
