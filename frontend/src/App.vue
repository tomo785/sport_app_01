<script>
import { useUserStore } from './stores/user'

export default {
  onLaunch() {
    console.log('App Launch')

    // 检查是否首次登录（使用 setTimeout 确保页面栈已就绪）
    setTimeout(() => {
      const userStore = useUserStore()
      this.checkFirstLogin(userStore)
    }, 300)
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  },
  methods: {
    // 检查是否首次登录
    checkFirstLogin(userStore) {
      // 1. 检查本地是否有登录标记
      const hasLoggedBefore = uni.getStorageSync('hasLoggedBefore')

      if (!hasLoggedBefore && !userStore.isLoggedIn) {
        // 首次登录且未登录，跳转到登录页面
        console.log('首次登录，跳转到登录页面')
        uni.navigateTo({
          url: '/pages/user/login',
          animationType: 'none'
        })
      } else if (userStore.isLoggedIn) {
        // 已登录，获取用户信息
        userStore.fetchUserInfo()
      }
    }
  }
}
</script>

<style lang="scss">
/* 引入全局公共样式 - 使用 @import 替代 @use 以兼容微信小程序编译环境 */
@import 'static/styles/common.scss';

/* ===== 浅色模式（默认）= 白绿色调 ===== */
.light-mode, :root {
  --bg-primary: #f0faf0;
  --bg-card: #ffffff;
  --bg-secondary: #f6fdf6;
  --text-primary: #1a1a2e;
  --text-secondary: #334155;
  --text-tertiary: #94a3b8;
  --border-color: #e8f5e9;
  --shadow-color: rgba(34, 197, 94, 0.06);
  --accent-green: #22c55e;
  --accent-blue: #3b82f6;
  --accent-purple: #8b5cf6;
}

/* ===== 深色模式 = 黑蓝紫色调 ===== */
.dark-mode {
  --bg-primary: #0a0a14;
  --bg-card: #141428;
  --bg-secondary: #1c1c36;
  --text-primary: #eeeef8;
  --text-secondary: #b8b8d0;
  --text-tertiary: #6e6e8a;
  --border-color: #2a2a48;
  --shadow-color: rgba(0, 0, 0, 0.4);
  --accent-green: #4ade80;
  --accent-blue: #60a5fa;
  --accent-purple: #a78bfa;
}

page {
  background-color: var(--bg-primary);
  font-size: 28rpx;
  color: var(--text-primary);
  transition: background-color 0.3s, color 0.3s;

  /* 为 TaskFlow 等组件提供状态栏高度 CSS 变量 */
  --status-bar-height: 44rpx;
}

/* 全局重置 */
view, text, image, input, button, scroll-view {
  box-sizing: border-box;
}

button::after {
  border: none;
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
