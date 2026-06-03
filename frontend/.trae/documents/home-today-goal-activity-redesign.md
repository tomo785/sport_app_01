# 首页今日目标活动卡片美化改造计划

## 摘要
将首页"今日目标"区域内多个训练活动的卡片堆叠布局，改造为更紧凑美观的横向标签流布局：每个活动一行，左侧用彩色圆点+名称，右侧用彩色小标签横向排列展示参数。同时移除所有 emoji 图标。

## 当前状态分析

### 问题
- 当前 `index.vue` 第109-128行的 `activity-cards` 区域，每个训练活动渲染为一个独立卡片（白色背景、圆角、阴影），多个活动时会形成卡片堆叠，视觉上臃肿
- 每个活动卡片使用 emoji 图标（🏃 💪 🧘 😴 ⚡），用户要求去掉 emoji
- 卡片之间间距较大，占用过多纵向空间

### 涉及的文件
- `d:\Acode\frontend\src\pages\index\index.vue` — 首页，包含今日目标区域模板和样式

### 数据结构
`todayPlan.activities` 数组，每个元素结构：
```js
{
  icon: '🏃',      // emoji 图标字符串
  name: '间歇跑',   // 活动名称
  desc: '配速 5:30', // 描述（可选）
  tags: [          // 标签数组
    { type: 'distance', text: '5km', color: '#22c55e', bg: '#ecfdf5' },
    { type: 'time', text: '30min', color: '#3b82f6', bg: '#eff6ff' }
  ]
}
```

## 改造方案

### 1. 模板改造（index.vue 第109-128行）

将现有的卡片堆叠布局：
```html
<view class="activity-cards" v-if="todayPlan.activities && todayPlan.activities.length > 0">
  <view class="activity-card" v-for="(act, idx) in todayPlan.activities" :key="idx">
    <view class="ac-main">
      <text class="ac-icon">{{ act.icon }}</text>
      <view class="ac-info">
        <text class="ac-name">{{ act.name }}</text>
      </view>
    </view>
    <view class="ac-tags" v-if="act.tags && act.tags.length > 0">
      <view class="ac-tag" v-for="(tag, tIdx) in act.tags" :key="tIdx" :style="{ background: tag.bg, color: tag.color }">
        {{ tag.text }}
      </view>
    </view>
  </view>
</view>
```

改造为紧凑的横向行布局：
```html
<view class="activity-list" v-if="todayPlan.activities && todayPlan.activities.length > 0">
  <view class="activity-row" v-for="(act, idx) in todayPlan.activities" :key="idx">
    <view class="activity-left">
      <view class="activity-dot" :style="{ background: getActivityColor(act).color }"></view>
      <text class="activity-name">{{ act.name }}</text>
    </view>
    <view class="activity-tags" v-if="act.tags && act.tags.length > 0">
      <view class="activity-tag" v-for="(tag, tIdx) in act.tags" :key="tIdx" :style="{ background: tag.bg, color: tag.color }">
        {{ tag.text }}
      </view>
    </view>
  </view>
</view>
```

### 2. 新增辅助函数

在 `<script setup>` 中新增 `getActivityColor` 函数，根据活动类型返回对应颜色：

```js
const activityColorMap = {
  run: { color: '#22c55e' },
  strength: { color: '#3b82f6' },
  yoga: { color: '#a855f7' },
  rest: { color: '#94a3b8' },
  custom: { color: '#f97316' }
}

function getActivityColor(act) {
  // 优先从 tag 类型推断
  if (act.tags && act.tags.length > 0) {
    const firstTag = act.tags[0]
    if (firstTag.type === 'run') return activityColorMap.run
    if (firstTag.type === 'strength') return activityColorMap.strength
    if (firstTag.type === 'yoga') return activityColorMap.yoga
    if (firstTag.type === 'rest') return activityColorMap.rest
    if (firstTag.type === 'custom') return activityColorMap.custom
  }
  // 从名称关键词推断
  const name = (act.name || '').toLowerCase()
  if (name.includes('跑')) return activityColorMap.run
  if (name.includes('力量') || name.includes('训练')) return activityColorMap.strength
  if (name.includes('瑜伽')) return activityColorMap.yoga
  if (name.includes('休息')) return activityColorMap.rest
  return activityColorMap.custom
}
```

### 3. 样式改造（index.vue style 区域）

删除旧的 `.activity-cards`、`.activity-card`、`.ac-icon` 等样式，新增以下样式：

```scss
// 活动列表（紧凑行布局）
.activity-list {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.activity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 20rpx;
  background: #f8fafc;
  border-radius: 16rpx;
}

.activity-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex-shrink: 0;
}

.activity-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.activity-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.activity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  justify-content: flex-end;
}

.activity-tag {
  font-size: 22rpx;
  font-weight: 600;
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  line-height: 1;
  white-space: nowrap;
}
```

### 4. 同时清理今日训练区域的 emoji

首页第166行的今日训练卡片也使用了 emoji 图标：
```html
<text class="wc-icon">{{ getWorkoutTypeInfo(item.type).icon }}</text>
```

将 `getWorkoutTypeInfo` 返回的 emoji 替换为纯色圆点背景+文字：
- 修改 `workoutTypeMap` 中 `icon` 字段改为空字符串或移除
- 修改 `.wc-icon` 样式，从显示 emoji 改为显示一个带背景色的圆角方块或圆点

具体改造：
```js
const workoutTypeMap = {
  1: { name: '跑步', color: '#22c55e', bg: '#ecfdf5' },
  2: { name: '健走', color: '#3b82f6', bg: '#eff6ff' },
  3: { name: '骑行', color: '#f97316', bg: '#fff7ed' }
}
```

模板中：
```html
<view class="wc-icon" :style="{ background: getWorkoutTypeInfo(item.type).bg }">
  <view class="wc-dot" :style="{ background: getWorkoutTypeInfo(item.type).color }"></view>
</view>
```

样式：
```scss
.wc-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.wc-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}
```

## 假设与决策

1. **颜色映射**：使用现有标签的颜色体系（跑步=绿、力量=蓝、瑜伽=紫、休息=灰、自定义=橙），保持一致性
2. **圆点大小**：10rpx 直径，足够醒目但不突兀
3. **行间距**：16rpx，比原来卡片间距（20rpx+卡片内边距）更紧凑
4. **背景色**：行使用 `#f8fafc` 浅灰背景，区别于卡片白色背景，更轻盈
5. **标签保留**：用户说"可以添加一些小标签"，所以保留 tags 并让它们更紧凑

## 验证步骤

1. 在微信开发者工具中打开首页
2. 确保有今日目标数据（可在"定制"页面创建训练计划）
3. 观察今日目标区域：
   - 多个活动应显示为紧凑的行，而非堆叠卡片
   - 每行左侧有彩色圆点+名称，右侧有彩色标签
   - 无 emoji 出现
4. 检查今日训练区域的运动记录卡片，图标也应变为彩色圆点
5. 检查无今日目标时的空状态不受影响
