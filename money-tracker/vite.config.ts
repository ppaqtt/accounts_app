import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      manifest: {
        name: '小萌记账',
        short_name: '小萌记账',
        description: '可爱又好用的记账APP',
        theme_color: '#FF9ECD',
        background_color: '#fff5f5',
        display: 'standalone',
        orientation: 'portrait',
        icons: [
          {
            src: 'icons/icon-64x64.png',
            sizes: '64x64',
            type: 'image/png',
          },
          {
            src: 'icons/icon-128x128.png',
            sizes: '128x128',
            type: 'image/png',
          },
          {
            src: 'icons/icon-256x256.png',
            sizes: '256x256',
            type: 'image/png',
          },
          {
            src: 'icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any',
          },
        ],
      },
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,jpg,ico}'],
      },
    }),
  ],
})
