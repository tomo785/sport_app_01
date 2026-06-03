import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  // 初始化：读取 localStorage
  const saved = uni.getStorageSync('theme_mode')
  if (saved === 'dark') {
    isDark.value = true
  }

  // 应用主题到页面
  function applyTheme(dark) {
    const root = document.querySelector('html') || document.documentElement
    if (root) {
      if (dark) {
        root.classList.add('dark-mode')
        root.classList.remove('light-mode')
      } else {
        root.classList.add('light-mode')
        root.classList.remove('dark-mode')
      }
    }
    // 切换底部导航栏颜色
    try {
      uni.setTabBarStyle({
        backgroundColor: dark ? '#141428' : '#ffffff',
        color: dark ? '#6e6e8a' : '#94a3b8',
        selectedColor: dark ? '#a78bfa' : '#3b82f6',
        borderStyle: dark ? '#2a2a48' : 'white'
      })
    } catch (e) { /* tabBar API may not be available on all platforms */ }
    uni.setStorageSync('theme_mode', dark ? 'dark' : 'light')
  }

  // 切换主题
  function toggleTheme() {
    isDark.value = !isDark.value
    applyTheme(isDark.value)
  }

  // 初始化时立即应用
  applyTheme(isDark.value)

  return {
    isDark,
    toggleTheme
  }
})
