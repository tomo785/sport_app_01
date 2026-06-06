<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-inner">
        <AppIcon class="search-icon" name="search" size="28" />
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索计划名称"
          placeholder-class="search-placeholder"
        />
        <AppIcon class="search-clear" v-if="searchKeyword" name="close" size="24" @click="searchKeyword = ''" />
      </view>
    </view>
    <!-- 分类标签 -->
    <scroll-view class="tag-scroll" scroll-x :show-scrollbar="false">
      <view class="tag-track">
        <view
          class="tag-item"
          :class="{ active: activeCategory === cat.key }"
          v-for="cat in categories"
          :key="cat.key"
          @click="activeCategory = cat.key"
        >
          <text class="tag-text">{{ cat.label }}</text>
        </view>
      </view>
    </scroll-view>
    <!-- 计划列表 -->
    <scroll-view
      class="plan-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="plan-list" v-if="filteredPlans.length > 0">
        <view
          class="community-card"
          v-for="plan in filteredPlans"
          :key="plan.id"
          @click="goPlanDetail(plan)"
        >
          <view class="card-main">
            <view class="card-top">
              <text class="card-name">{{ plan.name }}</text>
              <view class="card-tags" v-if="plan.tags && plan.tags.length">
                <text class="card-tag" v-for="(tag, tIdx) in plan.tags" :key="tIdx">{{ tag }}</text>
              </view>
            </view>
            <view class="card-meta">
              <text class="meta-author">{{ plan.authorName || '官方' }}</text>
              <text class="meta-divider" v-if="plan.rating">·</text>
              <view class="meta-rating" v-if="plan.rating"><AppIcon name="star-fill" size="20" /><text>{{ plan.rating }}</text></view>
              <text class="meta-divider" v-if="plan.usageCount || plan.enrollCount">·</text>
              <text class="meta-count" v-if="plan.usageCount || plan.enrollCount">
                {{ plan.usageCount || plan.enrollCount }} 人采用
              </text>
            </view>
            <text class="card-desc" v-if="plan.description">{{ plan.description }}</text>
          </view>
          <view class="card-action" @click.stop="doAdopt(plan)">
            <view class="adopt-btn" :class="{ adopted: plan.userStatus === 1 }">
              <text class="adopt-text">{{ plan.userStatus === 1 ? '已采用' : '采用' }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else-if="!loading">
        <text class="empty-text">暂无匹配计划</text>
        <text class="empty-sub">试试其他关键词或分类</text>
      </view>
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCommunityPlans, adoptPlan, getCommunityTemplates } from '@/api/plan'
const refreshing = ref(false)
const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const plans = ref([])
const categories = [
  { key: 'all', label: '全部' },
  { key: 'run', label: '跑步' },
  { key: 'strength', label: '力量' },
  { key: 'yoga', label: '瑜伽' },
  { key: 'bike', label: '骑行' },
  { key: 'fatloss', label: '减脂' },
]
const filteredPlans = computed(() => {
  let list = plans.value
  // 分类筛选
  if (activeCategory.value !== 'all') {
    const catMap = {
      run: ['跑', '马拉松', '间歇'],
      strength: ['力量', '增肌', '塑形', '深蹲', '卧推'],
      yoga: ['瑜伽', '拉伸'],
      bike: ['骑行', '单车'],
      fatloss: ['减脂', '燃脂', '减肥'],
    }
    const keywords = catMap[activeCategory.value] || []
    list = list.filter(p => {
      const text = `${p.name || ''} ${p.description || ''} ${(p.tags || []).join(' ')}`
      return keywords.some(k => text.includes(k))
    })
  }
  // 搜索筛选
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(p => {
      const text = `${p.name || ''} ${p.description || ''} ${(p.tags || []).join(' ')}`.toLowerCase()
      return text.includes(kw)
    })
  }
  return list
})
onMounted(() => {
  loadPlans()
})
onShow(() => {
  loadPlans()
})
function loadPlans() {
  loading.value = true
  getCommunityPlans()
    .then(res => {
      if (res.code === 200 && res.data) {
        plans.value = normalizePlans(res.data)
      } else {
        fallbackLocal()
      }
    })
    .catch(() => fallbackLocal())
    .finally(() => { loading.value = false })
}
function fallbackLocal() {
  const tpl = getCommunityTemplates()
  if (tpl && tpl.length) {
    plans.value = normalizePlans(tpl)
  } else {
    plans.value = []
  }
}
function normalizePlans(list) {
  return list.map(p => ({
    ...p,
    userStatus: p.userStatus || 0,
    rating: p.rating || 5,
    usageCount: p.usageCount || p.enrollCount || 0,
    tags: p.tags || (p.targetAudience ? [p.targetAudience] : []),
  }))
}
function onRefresh() {
  refreshing.value = true
  loadPlans()
  setTimeout(() => { refreshing.value = false }, 500)
}
function doAdopt(plan) {
  if (plan.userStatus === 1) return
  adoptPlan(plan.id)
    .then(() => {
      plan.userStatus = 1
      uni.showToast({ title: '已采用', icon: 'success' })
    })
    .catch(() => {
      // 离线模式下模拟成功
      plan.userStatus = 1
      uni.showToast({ title: '已采用（本地）', icon: 'success' })
    })
}
function goPlanDetail(plan) {
  if (plan && plan.id) {
    uni.navigateTo({ url: `/pages/goal/detail?id=${plan.id}` })
  }
}
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}
// 搜索栏
.search-bar {
  padding: 24rpx 28rpx;
  background: #fff;
}
.search-inner {
  display: flex;
  align-items: center;
  background: #f5f5f7;
  border-radius: 16rpx;
  padding: 0 20rpx;
  height: 72rpx;
}
.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  color: #9ca3af;
}
.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
  height: 72rpx;
  line-height: 72rpx;
}
.search-placeholder {
  color: #9ca3af;
}
.search-clear {
  font-size: 24rpx;
  color: #9ca3af;
  padding: 8rpx;
}
// 分类标签
.tag-scroll {
  background: #fff;
  padding: 0 28rpx 20rpx;
  white-space: nowrap;
}
.tag-track {
  display: flex;
  gap: 16rpx;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 28rpx;
  border-radius: 32rpx;
  background: #f5f5f7;
  transition: all 0.2s;
  &.active {
    background: #3366FF;
    .tag-text {
      color: #fff;
      font-weight: 600;
    }
  }
}
.tag-text {
  font-size: 26rpx;
  color: #4b5563;
  white-space: nowrap;
}
// 计划列表
.plan-scroll {
  flex: 1;
  padding: 0 28rpx;
}
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-top: 20rpx;
}
.community-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
  gap: 20rpx;
  &:active {
    opacity: 0.85;
  }
}
.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.card-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
}
.card-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #000000;
}
.card-tags {
  display: flex;
  gap: 8rpx;
}
.card-tag {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  background: #eff6ff;
  color: #3366ff;
  font-weight: 500;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}
.meta-author {
  font-size: 24rpx;
  color: #6b7280;
}
.meta-divider {
  font-size: 24rpx;
  color: #d1d5db;
}
.meta-rating {
  font-size: 24rpx;
  color: #f59e0b;
  font-weight: 500;
}
.meta-count {
  font-size: 24rpx;
  color: #9ca3af;
}
.card-desc {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-action {
  flex-shrink: 0;
}
.adopt-btn {
  width: 120rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #3366ff;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: #2952cc;
  }
  &.adopted {
    background: #e5e7eb;
    .adopt-text {
      color: #9ca3af;
    }
  }
}
.adopt-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}
// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}
.empty-text {
  font-size: 32rpx;
  color: #6b7280;
  font-weight: 500;
}
.empty-sub {
  font-size: 26rpx;
  color: #9ca3af;
  margin-top: 12rpx;
}
.bottom-spacer {
  height: 40rpx;
}
</style>