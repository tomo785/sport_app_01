# 创建训练计划流程页面优化计划

## 摘要
优化 `create.vue` 三步创建计划流程的 UI 和交互体验，保持原有功能不变。重点改造：步骤条视觉风格、每周安排日期卡片布局、活动定制步骤编辑交互。

## 当前状态分析

### 涉及的文件
- `d:\Acode\frontend\src\pages\goal\create.vue` — 三步创建计划流程页（唯一修改文件）

### 当前问题

#### 1. 步骤条（第4-15行）
- 当前使用数字圆点（1/2/3）+ 文字标签 + 底部连线
- 风格偏传统，数字圆点在小程序中显示不够精致
- 当前步骤和已完成步骤的视觉区分度不够

#### 2. Step 2 每周安排（第52-92行 + 弹窗第95-177行）
- 7天卡片纵向排列，每个卡片占一整行，滚动量大
- 已设置课程的卡片和空卡片视觉差异小（仅边框颜色）
- 点击日期后弹出底部弹窗添加课程，操作路径较长
- 弹窗内左右分栏（筛选+内容）在小屏幕上拥挤
- 快速添加的活动用 `content-chip` 网格排列，但每个 chip 占空间大

#### 3. Step 3 活动定制（第180-241行）
- 所有课程用折叠面板展示（`al-header` + `al-detail`），需要点击展开才能编辑步骤
- 展开后步骤列表 + 添加表单全部暴露，信息密度高
- 添加步骤表单字段多（名称、类型、时长/组数/次数/休息/距离），布局拥挤
- 空状态用 emoji 图标（`empty-icon`），已清空但占位仍在

#### 4. 整体风格
- 配色以 `#1e293b` 深色文字 + `#22c55e` 绿色为主，与首页 `#1c1c1e` + `#22c55e` 基本一致
- 但圆角、间距、阴影等细节与首页不完全统一

## 改造方案

### 改造1：步骤条美化（模板第4-15行 + 样式第781-841行）

**目标**：去掉数字圆点，改为更现代的进度指示器

**模板改造**：
```html
<!-- 顶部步骤条 -->
<view class="steps-bar">
  <view class="step-track">
    <view class="step-progress" :style="{ width: ((currentStep - 1) / (stepLabels.length - 1)) * 100 + '%' }"></view>
  </view>
  <view class="step-items">
    <view class="step-item" v-for="(label, idx) in stepLabels" :key="idx"
      :class="{ active: currentStep >= idx + 1, current: currentStep === idx + 1 }"
      @click="currentStep = idx + 1">
      <view class="step-dot-modern">
        <view class="step-dot-inner" v-if="currentStep > idx + 1"></view>
      </view>
      <text class="step-label-modern">{{ label }}</text>
    </view>
  </view>
</view>
```

**样式改造**：
```scss
.steps-bar {
  padding: 24rpx 32rpx 16rpx;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.step-track {
  position: relative;
  height: 4rpx;
  background: #f1f5f9;
  border-radius: 2rpx;
  margin-bottom: 16rpx;
}

.step-progress {
  height: 100%;
  background: linear-gradient(90deg, #22c55e 0%, #16a34a 100%);
  border-radius: 2rpx;
  transition: width 0.4s ease;
}

.step-items {
  display: flex;
  justify-content: space-between;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.step-dot-modern {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #f1f5f9;
  border: 4rpx solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.step-item.active .step-dot-modern {
  background: #22c55e;
  border-color: #dcfce7;
}

.step-item.current .step-dot-modern {
  background: #fff;
  border-color: #22c55e;
  box-shadow: 0 0 0 4rpx rgba(34, 197, 94, 0.15);
}

.step-dot-inner {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #fff;
}

.step-label-modern {
  font-size: 22rpx;
  color: #94a3b8;
  transition: all 0.3s;
}

.step-item.active .step-label-modern {
  color: #22c55e;
  font-weight: 600;
}

.step-item.current .step-label-modern {
  color: #16a34a;
  font-weight: 700;
}
```

### 改造2：Step 2 每周安排优化（模板第52-92行 + 弹窗第95-177行）

**目标**：日期卡片更紧凑，添加课程更直观

#### 2a. 日期卡片改为横向周历布局

**模板改造**（替换第70-91行的 day-grid）：
```html
<scroll-view class="day-grid-scroll" scroll-y>
  <view class="week-calendar">
    <view class="wc-row">
      <view class="wc-day" v-for="(label, idx) in dayLabels" :key="idx"
        :class="{ 'has-courses': getDayCourses(idx + 1).length > 0, 'active': editingDay === idx + 1 }"
        @click="editDay(idx + 1)">
        <text class="wc-day-label">{{ label }}</text>
        <view class="wc-day-dot" v-if="getDayCourses(idx + 1).length > 0"
          :style="{ background: getDayDotColor(idx + 1) }"></view>
        <view class="wc-day-dot-empty" v-else></view>
        <text class="wc-day-count" v-if="getDayCourses(idx + 1).length > 0">
          {{ getDayCourses(idx + 1).length }}项
        </text>
        <text class="wc-day-count rest" v-else>休息</text>
      </view>
    </view>

    <!-- 选中日期下方的课程列表 -->
    <view class="day-detail-panel" v-if="editingDay > 0">
      <view class="ddp-header">
        <text class="ddp-title">周{{ dayLabels[editingDay - 1] }} 安排</text>
        <view class="ddp-add" @click="showAddPanel = true">
          <text class="ddp-add-text">+ 添加课程</text>
        </view>
      </view>
      <view class="ddp-courses" v-if="getDayCourses(editingDay).length > 0">
        <view class="ddp-course" v-for="(course, ci) in getDayCourses(editingDay)" :key="ci">
          <view class="ddp-course-left">
            <view class="ddp-course-dot" :style="{ background: getCourseColor(course.type) }"></view>
            <text class="ddp-course-name">{{ course.name }}</text>
          </view>
          <view class="ddp-course-meta">
            <text class="ddp-meta-tag">{{ course.duration }}分钟</text>
          </view>
          <text class="ddp-course-del" @click="removeCourse(ci)">✕</text>
        </view>
      </view>
      <view class="ddp-empty" v-else>
        <text>暂无课程，点击上方「添加课程」</text>
      </view>
    </view>
  </view>
</scroll-view>
```

**新增数据**：
```js
const showAddPanel = ref(false)
```

**新增辅助函数**（放在 `getActivityColor` 附近）：
```js
function getDayDotColor(day) {
  const courses = getDayCourses(day)
  if (courses.length === 0) return '#cbd5e1'
  const typeColors = { 1: '#22c55e', 2: '#3b82f6', 3: '#a855f7', 4: '#f97316', 5: '#64748b' }
  return typeColors[courses[0].type] || '#22c55e'
}

function getCourseColor(type) {
  const typeColors = { 1: '#22c55e', 2: '#3b82f6', 3: '#a855f7', 4: '#f97316', 5: '#64748b' }
  return typeColors[type] || '#22c55e'
}
```

**修改 editDay 函数**：
```js
function editDay(day) {
  if (editingDay.value === day) {
    editingDay.value = 0
    showAddPanel.value = false
    return
  }
  editingDay.value = day
  showAddPanel.value = false
  customCourse.name = ''
  customCourse.type = 1
  customCourse.duration = 30
  addTab.value = 'quick'
  loadMyActivities()
}
```

#### 2b. 添加课程面板改为内嵌展开（替代弹窗）

**模板改造**（在 day-detail-panel 下方添加）：
```html
    <!-- 添加课程面板（内嵌展开） -->
    <view class="add-panel" v-if="showAddPanel && editingDay > 0">
      <view class="add-panel-head">
        <text class="add-panel-title">添加课程</text>
        <text class="add-panel-close" @click="showAddPanel = false">✕</text>
      </view>

      <!-- Tab 切换 -->
      <view class="add-panel-tabs">
        <view class="apt-tab" :class="{ active: addTab === 'quick' }" @click="addTab = 'quick'">快速添加</view>
        <view class="apt-tab" :class="{ active: addTab === 'my' }" @click="addTab = 'my'">我的活动</view>
        <view class="apt-tab" :class="{ active: addTab === 'custom' }" @click="addTab = 'custom'">自定义</view>
      </view>

      <!-- 快速添加 -->
      <view class="add-panel-body" v-if="addTab === 'quick'">
        <view class="quick-list">
          <view class="quick-item" v-for="act in quickActivities" :key="act.type" @click="quickAddCourse(act)">
            <view class="quick-item-left">
              <view class="quick-dot" :style="{ background: getCourseColor(act.type) }"></view>
              <text class="quick-name">{{ act.name }}</text>
            </view>
            <text class="quick-duration">{{ act.duration }}分钟</text>
          </view>
        </view>
      </view>

      <!-- 我的活动 -->
      <view class="add-panel-body" v-if="addTab === 'my'">
        <view class="quick-list" v-if="myActivities.length > 0">
          <view class="quick-item" v-for="act in myActivities" :key="act.id" @click="addMyActivity(act)">
            <view class="quick-item-left">
              <view class="quick-dot" style="background: #3b82f6;"></view>
              <text class="quick-name">{{ act.title || act.name }}</text>
            </view>
            <text class="quick-duration">{{ (act.exercises || []).length }}个步骤</text>
          </view>
        </view>
        <view class="panel-empty" v-else>
          <text>暂无自定义活动</text>
          <text class="panel-empty-hint">去「定制」页面创建吧</text>
        </view>
      </view>

      <!-- 自定义 -->
      <view class="add-panel-body" v-if="addTab === 'custom'">
        <view class="custom-form-modern">
          <input class="cfm-input" v-model="customCourse.name" placeholder="活动名称" />
          <view class="cfm-row">
            <picker class="cfm-picker" :range="activityTypes" :value="customTypeIndex" @change="onCustomTypeChange">
              <text>{{ activityTypes[customTypeIndex] }}</text>
            </picker>
            <input class="cfm-input sm" v-model.number="customCourse.duration" placeholder="时长(分)" type="number" />
          </view>
          <view class="cfm-btn" @click="addCustomCourse">添加活动</view>
        </view>
      </view>
    </view>
```

**删除原有弹窗模板**（第95-177行的 `.modal-mask` 到 `.modal-foot` 整个弹窗区块）

**修改 closeDayEditor**：
```js
function closeDayEditor() {
  editingDay.value = 0
  showAddPanel.value = false
}
```

#### 2c. 样式新增/替换

删除旧的 `.day-grid`、`.day-card`、`.modal-mask`、`.modal-card`、`.modal-body`、`.add-filter-side`、`.add-filter-content`、`.content-grid`、`.content-chip` 等样式。

新增样式：
```scss
// 周历横向布局
.week-calendar {
  padding: 0 24rpx 20rpx;
}

.wc-row {
  display: flex;
  gap: 12rpx;
  padding: 20rpx 0;
}

.wc-day {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx 0;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &:active {
    background: #f8fafc;
  }

  &.has-courses {
    border-color: #bbf7d0;
    background: #f0fdf4;
  }

  &.active {
    border-color: #22c55e;
    box-shadow: 0 2rpx 12rpx rgba(34, 197, 94, 0.12);
  }
}

.wc-day-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
}

.wc-day-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
}

.wc-day-dot-empty {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #e2e8f0;
}

.wc-day-count {
  font-size: 20rpx;
  color: #22c55e;
  font-weight: 600;

  &.rest {
    color: #cbd5e1;
    font-weight: 400;
  }
}

// 日期详情面板
.day-detail-panel {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-top: 16rpx;
}

.ddp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.ddp-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
}

.ddp-add {
  padding: 10rpx 20rpx;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border-radius: 20rpx;
}

.ddp-add-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.ddp-courses {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ddp-course {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 18rpx 20rpx;
  background: #f8fafc;
  border-radius: 14rpx;
}

.ddp-course-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.ddp-course-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.ddp-course-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.ddp-course-meta {
  flex-shrink: 0;
}

.ddp-meta-tag {
  font-size: 22rpx;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.ddp-course-del {
  font-size: 24rpx;
  color: #94a3b8;
  padding: 8rpx;

  &:active {
    color: #ef4444;
  }
}

.ddp-empty {
  text-align: center;
  padding: 40rpx 0;
  color: #94a3b8;
  font-size: 26rpx;
}

// 添加面板
.add-panel {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-top: 16rpx;
}

.add-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.add-panel-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
}

.add-panel-close {
  font-size: 28rpx;
  color: #94a3b8;
  padding: 8rpx;
}

.add-panel-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.apt-tab {
  flex: 1;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #64748b;
  background: #f8fafc;
  transition: all 0.2s;

  &.active {
    background: #f0fdf4;
    color: #16a34a;
    font-weight: 600;
    border: 1rpx solid #bbf7d0;
  }
}

.add-panel-body {
  max-height: 480rpx;
  overflow-y: auto;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 20rpx;
  background: #f8fafc;
  border-radius: 14rpx;
  transition: all 0.2s;

  &:active {
    background: #f0fdf4;
  }
}

.quick-item-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.quick-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.quick-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.quick-duration {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 500;
}

.panel-empty {
  text-align: center;
  padding: 60rpx 20rpx;
  color: #94a3b8;
  font-size: 26rpx;
}

.panel-empty-hint {
  font-size: 22rpx;
  color: #cbd5e1;
  margin-top: 8rpx;
}

// 自定义表单
.custom-form-modern {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.cfm-input {
  height: 72rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  font-size: 28rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;

  &.sm {
    flex: 1;
  }
}

.cfm-row {
  display: flex;
  gap: 12rpx;
}

.cfm-picker {
  flex: 1;
  height: 72rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #475569;
}

.cfm-btn {
  height: 72rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}
```

### 改造3：Step 3 活动定制优化（模板第180-241行）

**目标**：去掉展开收起，步骤编辑更直观

**模板改造**：
```html
    <!-- Step 3: 活动定制（全局预览） -->
    <scroll-view v-if="currentStep === 3" class="step-body" scroll-y>
      <view class="section-label">所有训练活动</view>

      <view class="course-list" v-if="allCourses.length > 0">
        <view class="course-item" v-for="(course, idx) in allCourses" :key="idx">
          <!-- 课程头部 -->
          <view class="course-header">
            <view class="course-header-left">
              <view class="course-dot" :style="{ background: getCourseColor(course.type) }"></view>
              <view class="course-info">
                <text class="course-name">第{{ course.week }}周 周{{ dayLabels[course.day - 1] }} · {{ course.name }}</text>
                <text class="course-meta">{{ course.duration || 0 }}分钟 · {{ course.exercises.length }}个步骤</text>
              </view>
            </view>
          </view>

          <!-- 步骤列表（始终展开） -->
          <view class="course-steps">
            <view class="cs-step" v-for="(ex, ei) in course.exercises" :key="ei">
              <view class="cs-step-left">
                <text class="cs-step-index">{{ ei + 1 }}</text>
                <text class="cs-step-name">{{ ex.name }}</text>
              </view>
              <view class="cs-step-meta">
                <text v-if="ex.duration">{{ ex.duration }}秒</text>
                <text v-if="ex.sets && ex.reps">{{ ex.sets }}组×{{ ex.reps }}次</text>
                <text v-if="ex.distance">{{ (ex.distance / 1000).toFixed(1) }}km</text>
              </view>
              <text class="cs-step-del" @click="removeExercise(idx, ei)">✕</text>
            </view>
          </view>

          <!-- 添加步骤（内嵌，始终可见） -->
          <view class="add-step-inline">
            <view class="asi-row">
              <input class="asi-input" v-model="newExercise.name" placeholder="步骤名称" />
              <picker class="asi-picker" :range="exerciseTypes" :value="exTypeIndex" @change="onExTypeChange">
                <text>{{ exerciseTypes[exTypeIndex] }}</text>
              </picker>
            </view>
            <view class="asi-row" v-if="newExercise.type === 1">
              <input class="asi-input sm" v-model.number="newExercise.duration" placeholder="时长(秒)" type="number" />
              <input class="asi-input sm" v-model.number="newExercise.distance" placeholder="距离(米)" type="number" />
            </view>
            <view class="asi-row" v-if="newExercise.type === 2">
              <input class="asi-input sm" v-model.number="newExercise.sets" placeholder="组数" type="number" />
              <input class="asi-input sm" v-model.number="newExercise.reps" placeholder="次数" type="number" />
              <input class="asi-input sm" v-model.number="newExercise.restTime" placeholder="休息(秒)" type="number" />
            </view>
            <view class="asi-actions">
              <view class="asi-btn add" @click="addExercise(idx)">添加步骤</view>
              <view class="asi-btn repeat" @click="addRepeatGroup(idx)">添加重复组</view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="allCourses.length === 0">
        <text class="empty-title">还没有添加训练活动</text>
        <text class="empty-sub">请返回上一步，点击日期添加训练内容</text>
      </view>
    </scroll-view>
```

**删除的数据**：`editingCourseIdx` ref 不再需要，删除相关逻辑。

**修改 addExercise**：添加成功后自动清空表单（已有逻辑，保持不变）。

**样式新增/替换**：

删除旧的 `.activity-list`、`.al-header`、`.al-emoji`、`.al-toggle`、`.al-detail`、`.ex-drag`、`.add-step-form`、`.asf-label`、`.asf-btn` 等样式。

新增样式：
```scss
// 课程列表
.course-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.course-item {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.course-header {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.course-header-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.course-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.course-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.course-meta {
  font-size: 22rpx;
  color: #94a3b8;
}

// 步骤列表
.course-steps {
  padding: 16rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.cs-step {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.cs-step-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.cs-step-index {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #f0fdf4;
  color: #16a34a;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cs-step-name {
  font-size: 26rpx;
  font-weight: 500;
  color: #334155;
}

.cs-step-meta {
  display: flex;
  gap: 8rpx;
  flex-shrink: 0;

  text {
    font-size: 20rpx;
    color: #94a3b8;
    background: #f1f5f9;
    padding: 4rpx 10rpx;
    border-radius: 6rpx;
  }
}

.cs-step-del {
  font-size: 24rpx;
  color: #94a3b8;
  padding: 8rpx;

  &:active {
    color: #ef4444;
  }
}

// 内嵌添加步骤
.add-step-inline {
  padding: 16rpx 24rpx 24rpx;
  background: #fafafa;
  border-top: 1rpx solid #f1f5f9;
}

.asi-row {
  display: flex;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.asi-input {
  flex: 1;
  height: 60rpx;
  border-radius: 10rpx;
  padding: 0 14rpx;
  background: #fff;
  font-size: 26rpx;
  color: #1e293b;
  border: 2rpx solid #e2e8f0;

  &.sm {
    flex: 1;
  }
}

.asi-picker {
  width: 140rpx;
  height: 60rpx;
  border-radius: 10rpx;
  padding: 0 14rpx;
  background: #fff;
  border: 2rpx solid #e2e8f0;
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #475569;
  flex-shrink: 0;
}

.asi-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 4rpx;
}

.asi-btn {
  flex: 1;
  height: 60rpx;
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
}

.asi-btn.add {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
}

.asi-btn.repeat {
  background: #dbeafe;
  color: #2563eb;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  text-align: center;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #334155;
  margin-top: 16rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: #94a3b8;
  margin-top: 8rpx;
}
```

### 改造4：清理残留 emoji/空方法

- 第669-671行的 `getTypeEmoji` 函数返回空字符串，可删除
- 第184行的 `<text class="al-emoji"></text>` 已空，改造后已移除
- 第237行的 `<text class="empty-icon"></text>` 已空，改造后已移除

## 假设与决策

1. **步骤条可点击跳转**：保留原有逻辑 `@click="currentStep = idx + 1"`，用户可以直接点击步骤标签跳转
2. **日期卡片点击行为变更**：从「点击日期 → 弹窗添加课程」改为「点击日期 → 展开日期详情面板 → 点击添加课程 → 展开添加面板」，操作路径多了一步但更直观
3. **再次点击日期关闭**：如果点击已选中的日期，关闭该日期的详情面板
4. **Step 3 始终展开**：不再使用折叠面板，每个课程的步骤列表和添加表单始终可见，减少点击次数
5. **配色统一**：继续使用 `#22c55e` 绿色系为主色调，与首页保持一致
6. **删除的样式确认**：旧样式如 `.modal-mask`、`.modal-card`、`.activity-list`（旧）、`.al-header` 等将不再使用，需从 style 中删除

## 验证步骤

1. 进入「定制」页面 → 点击「创建计划」
2. **步骤条**：观察是否为横线进度条 + 圆点标记，切换步骤时进度条有动画过渡
3. **Step 1 基本信息**：确认表单填写、周数选择、难度选择功能正常
4. **Step 2 每周安排**：
   - 7天应为横向排列的小卡片
   - 有课程的日期显示彩色圆点 + "N项"，空日期显示灰色圆点 + "休息"
   - 点击日期展开下方详情面板，显示该日课程列表
   - 点击「添加课程」展开添加面板（快速添加/我的活动/自定义三个Tab）
   - 添加课程后该日期卡片状态更新
   - 周切换、复制到下周/所有周功能正常
5. **Step 3 活动定制**：
   - 所有课程直接展开显示，无需点击展开
   - 每个课程下方直接显示步骤列表和内嵌添加表单
   - 添加步骤、添加重复组、删除步骤功能正常
6. **保存计划**：填写完整信息后保存，确认数据正确存储
7. **编辑计划**：从已有计划列表进入编辑，确认数据回显正确
