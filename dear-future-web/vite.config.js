import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/',
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (id.includes('node_modules')) {
            if (id.includes('react-dom') || id.includes('react')) return 'vendor-react';
            if (id.includes('react-router')) return 'vendor-router';
          }
        },
      },
    },
    chunkSizeWarningLimit: 500,
  },
  server: {
    proxy: {
      '/api': {
        target: 'https://api.dearfuture.info',
        changeOrigin: true,
        secure: false,
      },
      '/oauth2': {
        target: 'https://api.dearfuture.info',
        changeOrigin: true,
        secure: false,
      },
      '/login/oauth2': {
        target: 'https://api.dearfuture.info',
        changeOrigin: true,
        secure: false,
      },
      '/uploads': {
        target: 'https://api.dearfuture.info',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
