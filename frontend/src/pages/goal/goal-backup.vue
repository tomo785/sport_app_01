<template>
  <view class="container">
    <!-- 顶部 Tab -->
    <view class="header">
      <view
        class="tab"
        :class="{ active: activeTab === 'all' }"
        @click="activeTab = 'all'"
      >全部卡片</view>
      <view
        class="tab"
        :class="{ active: activeTab === 'fixed' }"
        @click="activeTab = 'fixed'"
      >固定项目</view>
      <view
        class="tab"
        :class="{ active: activeTab === 'random' }"
        @click="activeTab = 'random'"
      >随机项目</view>
    </view>
    <!-- 全部卡片 -->
    <scroll-view class="card-list" scroll-y v-if="activeTab === 'all'">
      <view class="tip-bar">
        <text class="tip-text">管理你的小目标卡片，新增、编辑或删除</text>
      </view>
      <view class="action-bar">
        <button class="add-btn" @click="openModal()">+ 添加小卡片</button>
      </view>
      <view class="item-card" v-for="item in goalCards" :key="item.id">
        <AppIcon class="item-icon" :name="item.icon" size="34" />
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-amount">{{ item.amount }}</text>
        </view>
        <view class="item-actions">
          <text class="action-edit" @click="openModal(item)">编辑</text>
          <text class="action-delete" @click="deleteCard(item.id)">删除</text>
        </view>
      </view>
      <view class="empty" v-if="goalCards.length === 0">
        <text>还没有小目标卡片，点击上方按钮添加</text>
      </view>
    </scroll-view>
    <!-- 固定项目 -->
    <scroll-view class="card-list" scroll-y v-if="activeTab === 'fixed'">
      <view class="tip-bar">
        <text class="tip-text">勾选要放入「固定项目」的卡片，每日将按日期生成固定挑战</text>
      </view>
      <view class="check-card" v-for="item in goalCards" :key="item.id" @click="toggleFixed(item.id)">
        <text class="check-box" :class="{ checked: fixedIds.includes(item.id) }">
          {{ fixedIds.includes(item.id) ? '' : '' }}
        </text>
        <AppIcon class="item-icon" :name="item.icon" size="34" />
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-amount">{{ item.amount }}</text>
        </view>
      </view>
    </scroll-view>
    <!-- 随机项目 -->
    <scroll-view class="card-list" scroll-y v-if="activeTab === 'random'">
      <view class="tip-bar">
        <text class="tip-text">勾选要放入「随机项目」池的卡片，刷新时将从中随机抽取</text>
      </view>
      <view class="check-card" v-for="item in goalCards" :key="item.id" @click="toggleRandom(item.id)">
        <text class="check-box" :class="{ checked: randomIds.includes(item.id) }">
          {{ randomIds.includes(item.id) ? '' : '' }}
        </text>
        <AppIcon class="item-icon" :name="item.icon" size="34" />
        <view class="item-info">
          <text class="item-name">{{ item.name }}</text>
          <text class="item-amount">{{ item.amount }}</text>
        </view>
      </view>
    </scroll-view>
    <!-- 添加/编辑弹窗 -->
    <view class="modal" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ editingId ? '编辑卡片' : '添加卡片' }}</text>
          <AppIcon class="modal-close" name="close" size="28" @click="showModal = false" />
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">图标</text>
            <input class="input" v-model="form.icon" placeholder="例如: strength" maxlength="20" />
          </view>
          <view class="form-item">
            <text class="form-label">动作名称</text>
            <input class="input" v-model="form.name" placeholder="例如: 深蹲" maxlength="10" />
          </view>
          <view class="form-item">
            <text class="form-label">运动量</text>
            <input class="input" v-model="form.amount" placeholder="例如: 20次 × 2组" maxlength="20" />
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn-cancel" @click="showModal = false">取消</button>
          <button class="btn-confirm" @click="saveCard">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
const activeTab = ref('all')
const showModal = ref(false)
const editingId = ref('')
const goalCards = ref([])
const fixedIds = ref([])
const randomIds = ref([])
const form = ref({
  icon: '',
  name: '',
  amount: ''
})
const STORAGE_KEY = 'goalCardConfig'
function loadData() {
  const saved = uni.getStorageSync(STORAGE_KEY)
  if (saved) {
    try {
      const data = JSON.parse(saved)
      goalCards.value = data.goalCards || []
      fixedIds.value = data.fixedIds || []
      randomIds.value = data.randomIds || []
    } catch (e) {
      initDefault()
    }
  } else {
    initDefault()
  }
}
function saveData() {
  const data = {
    goalCards: goalCards.value,
    fixedIds: fixedIds.value,
    randomIds: randomIds.value
  }
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(data))
}
function initDefault() {
  goalCards.value = [
    { id: 'g1', icon: 'strength', name: '深蹲', amount: '20次 × 2组' },
    { id: 'g2', icon: 'strength', name: '俯卧撑', amount: '15次 × 2组' },
    { id: 'g3', icon: 'strength', name: '哑铃弯举', amount: '12次 × 3组' },
    { id: 'g4', icon: 'stretch', name: '平板支撑', amount: '45秒 × 2组' },
    { id: 'g5', icon: 'star-fill', name: '开合跳', amount: '30次 × 2组' },
    { id: 'g6', icon: 'intensity', name: '高抬腿', amount: '40次 × 2组' },
    { id: 'g7', icon: 'run', name: '原地跑', amount: '60秒 × 2组' },
    { id: 'g8', icon: 'target', name: '卷腹', amount: '20次 × 2组' },
    { id: 'g9', icon: 'level', name: '登山者', amount: '20次 × 2组' },
    { id: 'g10', icon: 'strength', name: '弓步蹲', amount: '12次 × 2组' },
    { id: 'g11', icon: 'strength', name: '臂屈伸', amount: '12次 × 2组' },
    { id: 'g12', icon: 'stretch', name: '侧平板', amount: '30秒 × 2组' },
    { id: 'g13', icon: 'intensity', name: '波比跳', amount: '10次 × 2组' },
    { id: 'g14', icon: 'strength', name: '肩推', amount: '12次 × 2组' },
    { id: 'g15', icon: 'star-fill', name: '空中自行车', amount: '20次 × 2组' }
  ]
  fixedIds.value = goalCards.value.map(i => i.id)
  randomIds.value = goalCards.value.map(i => i.id)
  saveData()
}
function openModal(item) {
  if (item) {
    editingId.value = item.id
    form.value = { icon: item.icon, name: item.name, amount: item.amount }
  } else {
    editingId.value = ''
    form.value = { icon: '', name: '', amount: '' }
  }
  showModal.value = true
}
function saveCard() {
  if (!form.value.icon.trim() || !form.value.name.trim() || !form.value.amount.trim()) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (editingId.value) {
    const item = goalCards.value.find(i => i.id === editingId.value)
    if (item) {
      item.icon = form.value.icon.trim()
      item.name = form.value.name.trim()
      item.amount = form.value.amount.trim()
    }
  } else {
    goalCards.value.push({
      id: 'g' + Date.now(),
      icon: form.value.icon.trim(),
      name: form.value.name.trim(),
      amount: form.value.amount.trim()
    })
  }
  saveData()
  showModal.value = false
}
function deleteCard(id) {
  uni.showModal({
    title: '提示',
    content: '确定删除这张卡片吗？',
    success: (res) => {
      if (res.confirm) {
        goalCards.value = goalCards.value.filter(i => i.id !== id)
        fixedIds.value = fixedIds.value.filter(i => i !== id)
        randomIds.value = randomIds.value.filter(i => i !== id)
        saveData()
      }
    }
  })
}
function toggleFixed(id) {
  if (fixedIds.value.includes(id)) {
    fixedIds.value = fixedIds.value.filter(i => i !== id)
  } else {
    fixedIds.value.push(id)
  }
  saveData()
}
function toggleRandom(id) {
  if (randomIds.value.includes(id)) {
    randomIds.value = randomIds.value.filter(i => i !== id)
  } else {
    randomIds.value.push(id)
  }
  saveData()
}
onMounted(() => {
  loadData()
})
onShow(() => {
  loadData()
})
</script>
<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20rpx 30rpx calc(20rpx + env(safe-area-inset-top));
}
.tab {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  padding: 16rpx 0;
  border-bottom: 4rpx solid transparent;
  &.active {
    color: #fff;
    font-weight: 600;
    border-bottom-color: #fff;
  }
}
.card-list {
  height: calc(100vh - 120rpx - env(safe-area-inset-top));
  padding: 0 30rpx 40rpx;
}
.tip-bar {
  background: #e0e7ff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin: 20rpx 0;
}
.tip-text {
  font-size: 24rpx;
  color: #4c51bf;
}
.action-bar {
  margin-bottom: 20rpx;
}
.add-btn {
  width: 100%;
  height: 80rpx;
  background: #667eea;
  color: #fff;
  font-size: 28rpx;
  border-radius: 16rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    opacity: 0.9;
  }
}
.item-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.item-icon {
  font-size: 44rpx;
  margin-right: 20rpx;
}
.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  .item-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1c1c1e;
  }
  .item-amount {
    font-size: 24rpx;
    color: #64748b;
  }
}
.item-actions {
  display: flex;
  gap: 20rpx;
  .action-edit {
    font-size: 26rpx;
    color: #667eea;
    padding: 8rpx 16rpx;
    background: #f0f4ff;
    border-radius: 12rpx;
  }
  .action-delete {
    font-size: 26rpx;
    color: #ef4444;
    padding: 8rpx 16rpx;
    background: #fef2f2;
    border-radius: 12rpx;
  }
}
.check-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  &:active {
    background: #f8fafc;
  }
}
.check-box {
  width: 40rpx;
  height: 40rpx;
  border-radius: 10rpx;
  border: 2rpx solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #fff;
  margin-right: 20rpx;
  flex-shrink: 0;
  &.checked {
    background: #667eea;
    border-color: #667eea;
  }
}
.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  width: 80%;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}
.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.modal-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}
.modal-body {
  padding: 30rpx;
}
.form-item {
  margin-bottom: 30rpx;
  &:last-child {
    margin-bottom: 0;
  }
}
.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 16rpx;
}
.input {
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
}
.modal-footer {
  display: flex;
  padding: 20rpx 30rpx 30rpx;
  gap: 20rpx;
  button {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}
.btn-cancel {
  background: #f5f5f5;
  color: #666;
}
.btn-confirm {
  background: #667eea;
  color: #fff;
}
</style>