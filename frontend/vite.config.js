import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni()
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@api': resolve(__dirname, 'src/api'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@static': resolve(__dirname, 'src/static')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 使用现代 Sass API
        api: 'modern',
        // 禁用旧版 JavaScript API 警告
        silenceDeprecations: ['legacy-js-api'],
        // 全局变量
        additionalData: `
          $primary-color: #3b82f6;
          $secondary-color: #8b5cf6;
          $success-color: #10b981;
          $warning-color: #f97316;
          $danger-color: #ef4444;
        `
      }
    }
  },
  build: {
    // 构建优化
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    // uni-app 不需要自定义分包策略，移除 manualChunks 中针对 node_modules 的配置
    rollupOptions: {
      // 仅用于H5平台的分包配置，小程序/APP平台由uni-app处理
      output: process.env.UNI_PLATFORM === 'h5' ? {
        manualChunks: (id) => {
          // 将echarts单独分包
          if (id.includes('echarts')) {
            return 'echarts'
          }
          // 将uview-plus单独分包
          if (id.includes('uview-plus')) {
            return 'uview-plus'
          }
        }
      } : undefined
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  optimizeDeps: {
    include: ['echarts'],
    exclude: []
  }
})
