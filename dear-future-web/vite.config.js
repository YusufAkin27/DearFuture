import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/',
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
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
