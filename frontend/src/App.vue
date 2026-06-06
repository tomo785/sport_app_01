<script>
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'
export default {
  onLaunch() {
    console.log('App Launch')
    useThemeStore()
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
@import 'uview-plus/index.scss';
@import 'static/styles/common.scss';
/* ===== 浅色模式（默认）= 白绿色调 ===== */
.light-mode, :root {
  --bg-primary: #f3f4f6;
  --bg-card: #fbfbfc;
  --bg-secondary: #eceff3;
  --bg-elevated: #ffffff;
  --bg-glass: rgba(255, 255, 255, 0.82);
  --bg-glass-strong: rgba(255, 255, 255, 0.94);
  --text-primary: #18181b;
  --text-secondary: #3f3f46;
  --text-tertiary: #8a8f98;
  --border-color: #dfe3e8;
  --shadow-color: rgba(24, 24, 27, 0.08);
  --shadow-strong: rgba(24, 24, 27, 0.14);
  --accent-green: #22c55e;
  --accent-green-dark: #16a34a;
  --accent-blue: #3b82f6;
  --accent-purple: #0ea5a4;
  --accent-orange: #f97316;
  --accent-glow: rgba(34, 197, 94, 0.28);
  --card-border: rgba(24, 24, 27, 0.08);
  --card-shadow: 0 14rpx 38rpx rgba(24, 24, 27, 0.08);
  --card-shadow-soft: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
  --hero-gradient: linear-gradient(135deg, #ffffff 0%, #f3f4f6 52%, #e9edf2 100%);
  --hero-solid-gradient: linear-gradient(135deg, #22c55e 0%, #0ea5a4 58%, #3b82f6 100%);
  --page-radial-a: rgba(255, 255, 255, 0.82);
  --page-radial-b: rgba(209, 213, 219, 0.42);
  --soft-green: #e9f8ef;
  --soft-blue: #eef3f9;
  --soft-orange: #f8f1ea;
  --soft-neutral: #eef0f3;
  --surface-card-gradient: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(244, 245, 247, 0.96));
  --surface-row-gradient: linear-gradient(135deg, #eef0f3 0%, #ffffff 100%);
  --metric-bg: rgba(255, 255, 255, 0.72);
  --metric-border: rgba(24, 24, 27, 0.08);
  --input-bg: #ffffff;
  --selected-soft: #e8f7ee;
}
/* ===== 深色模式 = 黑蓝紫色调 ===== */
.dark-mode {
  --bg-primary: #020617;
  --bg-card: #08111f;
  --bg-secondary: #0d1b2e;
  --bg-elevated: #0b1628;
  --bg-glass: rgba(13, 27, 46, 0.78);
  --bg-glass-strong: rgba(10, 22, 39, 0.94);
  --text-primary: #f8fbff;
  --text-secondary: #c7d2fe;
  --text-tertiary: #7dd3fc;
  --border-color: #1e3a5f;
  --shadow-color: rgba(0, 0, 0, 0.4);
  --shadow-strong: rgba(0, 0, 0, 0.68);
  --accent-green: #22d3ee;
  --accent-green-dark: #06b6d4;
  --accent-blue: #38bdf8;
  --accent-purple: #38bdf8;
  --accent-orange: #fb923c;
  --accent-glow: rgba(34, 211, 238, 0.34);
  --card-border: rgba(125, 211, 252, 0.24);
  --card-shadow: 0 18rpx 48rpx rgba(0, 0, 0, 0.42), 0 0 0 1rpx rgba(56, 189, 248, 0.08);
  --card-shadow-soft: 0 10rpx 30rpx rgba(0, 0, 0, 0.34);
  --hero-gradient: linear-gradient(135deg, rgba(8, 17, 31, 0.98) 0%, rgba(15, 23, 42, 0.94) 48%, rgba(12, 74, 110, 0.9) 100%);
  --hero-solid-gradient: linear-gradient(135deg, #031525 0%, #0f2a54 48%, #155e75 100%);
  --page-radial-a: rgba(56, 189, 248, 0.22);
  --page-radial-b: rgba(129, 140, 248, 0.18);
  --soft-green: rgba(34, 211, 238, 0.14);
  --soft-blue: rgba(56, 189, 248, 0.16);
  --soft-orange: rgba(251, 146, 60, 0.16);
  --soft-neutral: rgba(148, 163, 184, 0.12);
  --surface-card-gradient: linear-gradient(180deg, rgba(11, 22, 40, 0.98), rgba(5, 13, 26, 0.96));
  --surface-row-gradient: linear-gradient(135deg, rgba(13, 27, 46, 0.96) 0%, rgba(8, 17, 31, 0.96) 100%);
  --metric-bg: rgba(15, 23, 42, 0.72);
  --metric-border: rgba(125, 211, 252, 0.18);
  --input-bg: #071426;
  --selected-soft: rgba(34, 211, 238, 0.16);
}
page {
  background:
    radial-gradient(circle at 12% 0%, var(--page-radial-a), transparent 34%),
    radial-gradient(circle at 88% 10%, var(--page-radial-b), transparent 30%),
    var(--bg-primary);
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