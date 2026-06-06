<template>
  <view class="template-market">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <AppIcon class="search-icon" name="search" size="28" />
      <input
        class="search-input"
        v-model="searchKey"
        placeholder="搜索计划名称或标签"
        @confirm="onSearch"
      />
    </view>
    <!-- 标签筛选 -->
    <scroll-view class="tag-scroll" scroll-x>
      <view class="tag-list">
        <view
          v-for="tag in allTags"
          :key="tag"
          class="tag-item"
          :class="{ active: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
        >
          {{ tag }}
        </view>
      </view>
    </scroll-view>
    <!-- 计划列表 -->
    <scroll-view class="template-list" scroll-y>
      <view class="template-grid">
        <view
          v-for="template in filteredTemplates"
          :key="template.id"
          class="template-card"
          @click="previewTemplate(template)"
        >
          <view class="card-cover" :class="'cover-' + (template.id.slice(-1) % 3)">
            <AppIcon class="cover-icon" name="plan" size="42" />
          </view>
          <view class="card-info">
            <text class="template-name">{{ template.name }}</text>
            <view class="template-meta">
              <text class="author">{{ template.authorName }}</text>
              <view class="rating">
                <AppIcon class="stars" name="star-fill" size="20" />
                <text>{{ template.rating }}</text>
              </view>
            </view>
            <view class="tag-row">
              <text v-for="tag in template.tags.slice(0, 2)" :key="tag" class="template-tag">{{ tag }}</text>
            </view>
            <text class="usage-count">{{ template.usageCount }}人使用</text>
          </view>
        </view>
      </view>
      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredTemplates.length === 0">
        <AppIcon class="empty-icon" name="empty" size="72" />
        <text class="empty-text">暂无相关计划</text>
      </view>
    </scroll-view>
    <!-- 预览弹窗 -->
    <view class="preview-overlay" v-if="previewVisible" @click="previewVisible = false">
      <view class="preview-content" @click.stop>
        <view class="preview-header">
          <text class="preview-title">{{ previewTemplateData?.name }}</text>
          <AppIcon class="close-btn" name="close" size="28" @click="previewVisible = false" />
        </view>
        <view class="preview-body">
          <text class="preview-desc">{{ previewTemplateData?.description }}</text>
          <view class="preview-tags">
            <text v-for="tag in previewTemplateData?.tags" :key="tag" class="preview-tag">{{ tag }}</text>
          </view>
          <view class="week-preview">
            <text class="week-title">7天安排</text>
            <view class="day-preview-list">
              <view
                v-for="day in previewTemplateData?.weeklyStructure"
                :key="day.dayOfWeek"
                class="day-preview-item"
              >
                <text class="day-label">周{{ ['一','二','三','四','五','六','日'][day.dayOfWeek-1] }}</text>
                <view class="day-plan-preview">
                  <AppIcon class="day-type-icon" :name="getTypeIcon(day.type)" size="24" />
                  <text class="day-plan-title">{{ day.title }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view class="preview-footer">
          <button class="preview-btn secondary" @click="collectTemplate">
            <AppIcon name="star-fill" size="24" /><text>收藏</text>
          </button>
          <button class="preview-btn primary" @click="useTemplate">
            <text>使用此计划</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, computed } from 'vue'
const props = defineProps({
  templates: {
    type: Array,
    default: () => []
  }
})
const emit = defineEmits(['use-template', 'collect-template'])
const searchKey = ref('')
const selectedTags = ref([])
const previewVisible = ref(false)
const previewTemplateData = ref(null)
const allTags = ['减脂', '增肌', '马拉松', '力量举', '新手', '进阶', '瑜伽']
const filteredTemplates = computed(() => {
  let result = props.templates
  if (searchKey.value.trim()) {
    const key = searchKey.value.trim().toLowerCase()
    result = result.filter(t =>
      t.name.toLowerCase().includes(key) ||
      t.tags.some(tag => tag.toLowerCase().includes(key))
    )
  }
  if (selectedTags.value.length > 0) {
    result = result.filter(t =>
      selectedTags.value.some(tag => t.tags.includes(tag))
    )
  }
  return result
})
function onSearch() {
  // 搜索已在computed中处理
}
function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx > -1) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tag)
  }
}
function previewTemplate(template) {
  previewTemplateData.value = template
  previewVisible.value = true
}
function useTemplate() {
  if (previewTemplateData.value) {
    emit('use-template', previewTemplateData.value)
    previewVisible.value = false
  }
}
function collectTemplate() {
  if (previewTemplateData.value) {
    emit('collect-template', previewTemplateData.value)
    uni.showToast({ title: '已收藏', icon: 'success' })
  }
}
function getTypeIcon(type) {
  const map = { run: 'run', strength: 'strength', yoga: 'stretch', rest: 'rest', custom: 'plan' }
  return map[type] || 'plan'
}
</script>
<style lang="scss" scoped>
.template-market {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 20rpx 30rpx;
  padding: 16rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.search-icon {
  font-size: 28rpx;
}
.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #334155;
}
.tag-scroll {
  white-space: nowrap;
  margin-bottom: 16rpx;
}
.tag-list {
  display: flex;
  gap: 12rpx;
  padding: 0 30rpx;
}
.tag-item {
  padding: 10rpx 24rpx;
  background: #f1f5f9;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: #64748b;
  transition: all 0.2s ease;
  &.active {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
  }
}
.template-list {
  flex: 1;
  padding: 0 30rpx;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}
.template-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  &:active {
    transform: scale(0.98);
  }
}
.card-cover {
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &.cover-0 { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.cover-1 { background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%); }
  &.cover-2 { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
}
.cover-icon {
  font-size: 48rpx;
}
.card-info {
  padding: 20rpx;
}
.template-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1c1c1e;
  display: block;
  margin-bottom: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.template-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.author {
  font-size: 22rpx;
  color: #64748b;
}
.rating {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx;
  color: #f59e0b;
  .stars {
    color: #f59e0b;
  }
}
.tag-row {
  display: flex;
  gap: 8rpx;
  margin-bottom: 8rpx;
}
.template-tag {
  padding: 4rpx 12rpx;
  background: #f1f5f9;
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #64748b;
}
.usage-count {
  font-size: 20rpx;
  color: #94a3b8;
}
.empty-state {
  text-align: center;
  padding: 100rpx 0;
  .empty-icon {
    font-size: 64rpx;
    display: block;
    margin-bottom: 16rpx;
  }
  .empty-text {
    font-size: 28rpx;
    color: #94a3b8;
  }
}
// 预览弹窗
.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}
.preview-content {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f1f5f9;
}
.preview-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1c1c1e;
}
.close-btn {
  font-size: 32rpx;
  color: #94a3b8;
}
.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx 30rpx;
}
.preview-desc {
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 16rpx;
  display: block;
}
.preview-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.preview-tag {
  padding: 8rpx 20rpx;
  background: #f1f5f9;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #64748b;
}
.week-preview {
  .week-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #1c1c1e;
    margin-bottom: 16rpx;
    display: block;
  }
}
.day-preview-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.day-preview-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}
.day-label {
  font-size: 24rpx;
  color: #94a3b8;
  width: 48rpx;
}
.day-plan-preview {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.day-type-icon {
  font-size: 28rpx;
}
.day-plan-title {
  font-size: 26rpx;
  color: #334155;
}
.preview-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f1f5f9;
}
.preview-btn {
  flex: 1;
  height: 84rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  &:active {
    opacity: 0.9;
  }
  &.secondary {
    background: #f1f5f9;
    color: #64748b;
  }
  &.primary {
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    color: #fff;
  }
}
</style>