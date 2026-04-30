<script>
import { useUserStore } from './stores/user'

export default {
  onLaunch() {
    console.log('App Launch')
    
    // 初始化用户状态
    const userStore = useUserStore()
    
    // 检查是否首次登录
    this.checkFirstLogin(userStore)
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
@use 'static/styles/common.scss' as *;

page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  color: #333;
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
