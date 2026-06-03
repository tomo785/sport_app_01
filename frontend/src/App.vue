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

page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  color: #333;

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
