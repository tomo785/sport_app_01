# Sport App 设计风格指南
> 基于项目现有 20 个页面的样式代码提取归纳，作为后续开发的统一参考。
---
## 1. 色彩体系
### 1.1 主色（Primary）
| 用途 | 色值 | 示例 |
|---|---|---|
| 主色 | `#22c55e` | 按钮、选中态、进度环、标签 |
| 主色深 | `#16a34a` | 选中文字、强调文字 |
| 主色浅底 | `#f0fdf4` | 选中背景、卡片浅底 |
| 主色渐变 | `linear-gradient(135deg, #22c55e 0%, #16a34a 100%)` | 主操作按钮 |
### 1.2 辅助色
| 用途 | 色值 | 说明 |
|---|---|---|
| 蓝色 | `#3b82f6` / `#2563eb` / `#3366FF` | 力量训练、链接、辅助按钮 |
| 蓝色浅底 | `#eff6ff` / `#dbeafe` | 蓝色选中背景 |
| 紫色 | `#8b5cf6` / `#764ba2` / `#667eea` | 统计页头部、社区、渐变装饰 |
| 橙色 | `#f59e0b` / `#f97316` | HIIT、进阶级别、热门卡片 |
| 红色 | `#ef4444` / `#dc2626` | 删除、错误、专家级别 |
| 粉色 | `#ec4899` | 热门计划卡片装饰 |
| 青色 | `#0ea5a4` | 瑜伽类型 |
### 1.3 中性色（文字）
| 层级 | 色值 | 用途 |
|---|---|---|
| 一级文字 | `#1c1c1e` / `#0f172a` / `#000000` | 标题、主文字 |
| 二级文字 | `#333` / `#334155` / `#374151` | 副标题、卡片标题 |
| 三级文字 | `#555` / `#666` / `#64748b` | 辅助说明 |
| 四级文字 | `#999` / `#94a3b8` / `#9CA3AF` | 标签、描述、时间 |
| 五级文字 | `#bbb` / `#c7c7cc` / `#cbd5e1` | 极弱提示 |
### 1.4 背景色
| 用途 | 色值 |
|---|---|
| 页面背景 | `#f5f5f5` |
| 卡片背景 | `#fff` / `#ffffff` |
| 输入框/浅底 | `#f8fafc` / `#f9fafb` |
| 分割线 | `#f1f5f9` / `#eee` / `#e5e5e5` / `#E5E7EB` |
| 深色背景 | `#0f172a` / `#1e293b`（运动中页面、计划编辑器头部） |
### 1.5 状态色
| 状态 | 色值 | 用途 |
|---|---|---|
| 成功/完成 | `#22c55e` 底 + `#16a34a` 字 | 已完成标签 |
| 警告/GPS一般 | `#fbbf24` | GPS 精度指示 |
| 错误/GPS弱 | `#ef4444` | GPS 弱信号 |
| 禁用 | `opacity: 0.6` | 禁用按钮 |
---
## 2. 渐变
| 名称 | 值 | 使用场景 |
|---|---|---|
| 主色渐变 | `linear-gradient(135deg, #22c55e 0%, #16a34a 100%)` | 主按钮、空状态按钮、步数进度 |
| 深色渐变 | `linear-gradient(135deg, #0f172a 0%, #1e3a8a 52%, #312e81 100%)` | 计划编辑器头部、运动室内背景 |
| 蓝紫渐变 | `linear-gradient(135deg, #667eea 0%, #764ba2 100%)` | 统计页头部 |
| 蓝靛渐变 | `linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)` | 选择器确认按钮、侧边栏选中 |
| 蓝靛渐变2 | `linear-gradient(135deg, #2563eb 0%, #4f46e5 100%)` | 发布按钮 |
| 登录绿 | `linear-gradient(135deg, #4CAF50 0%, #45a049 100%)` | 登录按钮 |
| 级别渐变 | 绿→深绿 / 蓝→深蓝 / 橙→深橙 / 红→深红 | 计划级别封面 |
| 热门卡片 | `#F59E0B→#FBBF24` / `#3B82F6→#60A5FA` / `#EC4899→#F472B6` | 热门计划卡片背景 |
---
## 3. 圆角（border-radius）
| 级别 | 值 | 使用场景 |
|---|---|---|
| 大圆角 | `24rpx` | 卡片、弹窗顶部、热门卡片 |
| 中圆角 | `16rpx` ~ `20rpx` | 按钮、选项卡、输入框、标签 |
| 小圆角 | `10rpx` ~ `14rpx` | 登录输入框、小元素 |
| 胶囊形 | `28rpx` ~ `48rpx` | 筛选标签、开始运动按钮、模式标签 |
| 圆形 | `50%` | 头像、图标容器、步骤点、GPS 指示点 |
---
## 4. 间距
### 4.1 页面级
| 场景 | 值 |
|---|---|
| 页面水平内边距 | `28rpx` ~ `30rpx` |
| 卡片间距 | `20rpx` |
| 卡片内边距 | `28rpx` ~ `30rpx` |
| 底部安全区 | `calc(100rpx + env(safe-area-inset-bottom))` |
### 4.2 组件级
| 场景 | 值 |
|---|---|
| 元素间距 gap | `12rpx` ~ `24rpx` |
| 列表项间距 | `20rpx` |
| 表单项间距 | `28rpx` ~ `40rpx` |
| 标签内边距 | `6rpx 16rpx` ~ `20rpx 32rpx` |
---
## 5. 字体
### 5.1 字号
| 级别 | 值 | 用途 |
|---|---|---|
| 超大 | `64rpx` | 步数数字 |
| 大标题 | `40rpx` ~ `48rpx` | 页面标题、活动名称 |
| 标题 | `32rpx` ~ `36rpx` | 卡片标题、区块标题 |
| 正文 | `26rpx` ~ `30rpx` | 正文内容、按钮文字 |
| 辅助 | `22rpx` ~ `24rpx` | 标签、描述、提示 |
| 极小 | `20rpx` | 单位、极弱提示 |
### 5.2 字重
| 级别 | 值 | 用途 |
|---|---|---|
| 特粗 | `800` | 步数、大数字 |
| 粗 | `700` / `bold` | 标题、选中态 |
| 半粗 | `600` | 副标题、按钮、标签 |
| 中 | `500` | 正文强调 |
| 常规 | `400` / `normal` | 正文 |
---
## 6. 阴影（box-shadow）
| 级别 | 值 | 使用场景 |
|---|---|---|
| 轻阴影 | `0 2rpx 8rpx rgba(0, 0, 0, 0.03)` ~ `0 2rpx 12rpx rgba(0, 0, 0, 0.06)` | 卡片 |
| 中阴影 | `0 2rpx 12rpx rgba(0, 0, 0, 0.08)` ~ `0 8rpx 20rpx rgba(15, 23, 42, 0.08)` | 浮层、弹窗 |
| 重阴影 | `0 16rpx 34rpx rgba(15, 23, 42, 0.28)` | 英雄区卡片 |
| 按钮阴影 | `0 6rpx 20rpx rgba(34, 197, 94, 0.35)` | 主操作按钮（带主色光晕） |
| 上阴影 | `0 -4rpx 24rpx rgba(0, 0, 0, 0.06)` | 底部上拉卡片 |
---
## 7. 动画与过渡
| 场景 | 值 |
|---|---|
| 通用过渡 | `transition: all 0.2s ease` |
| 弹窗滑入 | `transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.3s ease` |
| 卡片展开 | `transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1)` |
| 按钮按下 | `transform: scale(0.95)` / `opacity: 0.85` |
| 选中缩放 | `transform: scale(1.1)` |
| 脉冲动画 | `@keyframes pulse-ring` — opacity 0.3→0.8 + scale 1→1.05，4s ease-in-out infinite |
---
## 8. 布局模式
### 8.1 页面结构
```
┌─────────────────────┐
│ 状态栏占位           │
├─────────────────────┤
│ 页面标题/导航栏      │
├─────────────────────┤
│                     │
│  内容区（scroll）    │
│  padding: 0 28rpx   │
│                     │
├─────────────────────┤
│ 底部安全区           │
└─────────────────────┘
```
### 8.2 卡片布局
- 白色背景 + `border-radius: 24rpx` + `padding: 28rpx` + 轻阴影
- 卡片间距 `margin-bottom: 20rpx`
- 卡片头部：标题 + 操作，`display: flex; justify-content: space-between`
### 8.3 列表布局
- 竖向列表：`display: flex; flex-direction: column; gap: 20rpx`
- 横向滚动：`white-space: nowrap; overflow-x: scroll`
- 网格：`display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx`
### 8.4 底部操作栏
- 固定底部：`position: fixed; bottom: 0; left: 0; right: 0`
- 双按钮：主按钮 `flex: 7`，次按钮 `flex: 3`
- 底部安全区：`padding-bottom: calc(24rpx + env(safe-area-inset-bottom))`
---
## 9. 组件模式
### 9.1 筛选标签
```scss
.chip {
  padding: 10rpx~12rpx 24rpx~28rpx;
  border-radius: 28rpx~32rpx;
  background: #f5f5f5;
  color: #666;
  &.active { background: #22c55e; color: #fff; font-weight: 600; }
}
```
### 9.2 选项卡
```scss
.tab {
  padding: 20rpx 0;
  border-radius: 16rpx;
  background: #f5f5f5;
  &.active { background: #f0fdf4; border: 2rpx solid #22c55e; }
}
```
### 9.3 表单输入
```scss
.form-input {
  height: 80rpx~90rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  font-size: 28rpx;
  color: #334155;
}
```
### 9.4 主操作按钮
```scss
.primary-btn {
  height: 90rpx~96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  box-shadow: 0 6rpx 20rpx rgba(34, 197, 94, 0.35);
  &:active { opacity: 0.85; transform: scale(0.98); }
}
```
### 9.5 空状态
```scss
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  .empty-icon { font-size: 80rpx; margin-bottom: 30rpx; }
  .empty-title { font-size: 32rpx; font-weight: 600; color: #333; }
  .empty-desc { font-size: 26rpx; color: #999; margin-top: 12rpx; }
  .empty-btn {
    margin-top: 40rpx;
    padding: 20rpx 60rpx;
    background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
    border-radius: 40rpx;
    color: #fff; font-size: 28rpx; font-weight: 600;
  }
}
```
### 9.6 状态标签
```scss
.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  &.completed { background: #f0fdf4; color: #16a34a; }
}
```
### 9.7 类型标签
```scss
.type-tag {
  font-size: 22rpx;
  font-weight: 600;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  // 有氧: bg=#ecfdf5, color=#22c55e
  // 力量: bg=#eff6ff, color=#3b82f6
  // 拉伸: bg=#fdf4ff, color=#a855f7
  // HIIT: bg=#fff7ed, color=#f97316
  // 综合: bg=#f8fafc, color=#64748b
}
```
---
## 10. 毛玻璃效果
| 场景 | 值 |
|---|---|
| GPS 浮层 | `background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(8px)` |
| 活动选择条 | `background: linear-gradient(to bottom, rgba(255,255,255,0.85) 0%, rgba(255,255,255,0.4) 70%, transparent 100%)` |
| 锁屏遮罩 | `background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(4px)` |
| 暂停浮层 | `background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(12px)` |
---
## 11. 运动类型配色映射
| 类型 | 图标 | 主色 | 浅底 | 渐变 |
|---|---|---|---|---|
| 跑步/有氧 | run | `#22c55e` | `#ecfdf5` / `#f0fdf4` | `#22c55e → #16a34a` |
| 力量 | strength | `#3b82f6` | `#eff6ff` | `#3b82f6 → #2563eb` |
| 瑜伽/拉伸 | stretch | `#a855f7` / `#0ea5a4` | `#fdf4ff` | — |
| HIIT | intensity | `#f97316` | `#fff7ed` | — |
| 休息 | rest | `#94a3b8` | `#f1f5f9` | — |
| 骑行 | cycling | `#22c55e` | `#d1fae5 → #a7f3d0` | — |
| 健走 | walk | `#3b82f6` | `#dbeafe → #bfdbfe` | — |
---
## 12. 设计原则
1. **绿色运动感**：主色 `#22c55e` 贯穿全局，传达健康、活力
2. **轻量卡片**：白底 + 微阴影 + 大圆角，层次分明但不厚重
3. **渐变提氛围**：深色渐变用于沉浸式场景（运动中、编辑器头部），浅色渐变用于装饰
4. **字号强对比**：标题 700/800 + 大字号 vs 描述 400 + 小字号，信息层级清晰
5. **交互轻反馈**：按下 `scale(0.95~0.98)` + `opacity` 变化，不过度动画
6. **状态即颜色**：选中=绿底绿字，完成=绿标签，警告=黄点，错误=红点
7. **避免使用 表情符号**：图标优先使用图片资源（`/static/images/`）或 SVG 图标，不使用 表情符号 作为 UI 图标。表情符号 在不同平台渲染不一致，影响视觉统一性。仅在无合适图标资源时作为临时占位使用