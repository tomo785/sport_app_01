#  运动 App 前端
> 基于 uni-app Vue3 + Pinia 的跨端运动健身应用，支持微信小程序、H5、App。
---
##  技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| uni-app | 3.0.0-alpha (Vue3) | 跨端框架 |
| Vue | 3.3.4 | 前端框架 |
| Pinia | 2.1.6 | 状态管理 |
| uview-plus | 3.1.43 | UI 组件库 |
| ECharts | 5.4.3 | 图表可视化 |
| Lottie | 3.3.1 / 1.0.12 | 动画效果 |
| vue-i18n | 9.2.2 | 国际化 |
| Sass | 1.98.0 | CSS 预处理器 |
| Vite | 5.2.8 | 构建工具 |
---
##  目录结构
```
frontend/
├── src/
│   ├── pages/                   # 20 个页面
│   │   ├── index/
│   │   │   └── index.vue        # 首页：今日目标、活动入口、步数
│   │   ├── running/
│   │   │   └── running.vue      # 运动记录：GPS 跑步/骑行
│   │   ├── task/
│   │   │   ├── task-flow.vue    # 任务流程图（日历 + 任务链）
│   │   │   ├── task-detail.vue  # 任务详情
│   │   │   └── exercise-detail.vue # 动作详情
│   │   ├── goal/
│   │   │   ├── goal.vue         # 目标管理首页
│   │   │   ├── create.vue       # 创建目标
│   │   │   ├── detail.vue       # 目标详情
│   │   │   ├── community.vue    # 社区计划分享
│   │   │   ├── goal-backup.vue  # 目标备份
│   │   │   └── activity-editor.vue # 活动编辑器
│   │   ├── plan/
│   │   │   └── editor.vue       # 训练计划编辑器
│   │   ├── stats/
│   │   │   └── stats.vue        # 数据统计（日/周/月 + 趋势）
│   │   ├── user/
│   │   │   ├── login.vue        # 登录页（手机/密码/管理员/游客）
│   │   │   ├── register.vue     # 注册页
│   │   │   ├── profile.vue      # 个人中心
│   │   │   ├── profile-edit.vue # 编辑资料
│   │   │   ├── settings.vue     # 系统设置
│   │   │   └── ai-settings.vue  # AI 模型设置
│   │   ├── workout/
│   │   │   ├── workout.vue      # 运动列表
│   │   │   └── detail.vue       # 运动详情
│   │   └── design/
│   │       └── design.vue       # 小项目设计器
│   │
│   ├── components/              # 9 个公共组件
│   │   ├── AiPlanDialog.vue     # AI 对话弹窗（WebSocket 流式）
│   │   ├── TaskCard.vue         # 任务卡片
│   │   ├── TaskFlow.vue         # 任务流程组件
│   │   ├── TrainingStatusCard.vue # 训练状态卡片
│   │   └── plan/
│   │       ├── PlanCard.vue     # 计划卡片
│   │       ├── PlanEditor.vue   # 计划编辑器核心
│   │       ├── TemplateMarket.vue # 模板市场
│   │       ├── TypeSelector.vue # 类型选择器
│   │       └── WeekDateBar.vue  # 周日期选择条
│   │
│   ├── api/                     # API 封装
│   │   ├── user.js
│   │   ├── workout.js
│   │   ├── goal.js
│   │   ├── plan.js
│   │   ├── task.js
│   │   ├── stats.js
│   │   └── weather.js
│   │
│   ├── stores/                  # Pinia 状态管理
│   │   ├── user.js              # 用户状态
│   │   ├── plan.js              # 计划状态
│   │   └── theme.js             # 主题状态（深色模式）
│   │
│   ├── utils/                   # 工具函数
│   │   ├── request.js           # HTTP 请求拦截器
│   │   └── steps.js             # 微信步数相关
│   │
│   ├── static/                  # 静态资源
│   ├── App.vue                  # 应用入口
│   ├── main.js                  # 主入口
│   ├── manifest.json            # 应用配置
│   └── pages.json               # 页面路由
│
├── scripts/                     # 初始化脚本
│   ├── init-admin.js            # 管理员初始化
│   └── init-user.js             # 测试用户初始化
│
├── Lottie/                      # Lottie 动画资源
├── .env                         # 开发环境变量
├── .env.production              # 生产环境变量
├── package.json
├── design-style.md              # UI 设计规范
├── ADMIN_SETUP.md               # 管理员设置指南
├── USER_ACCOUNTS.md             # 测试账号
├── TASK_FLOW_README.md          # 任务流程文档
└── TASK_FLOW_BACKEND.md         # 任务流程后端文档
```
---
##  快速开始
### 环境要求
- Node.js 18+
- npm / pnpm
### 安装依赖
```bash
npm install
```
### 开发模式
```bash
# 微信小程序（默认）
npm run dev
# H5
npm run dev:h5
# App
npm run dev:app
# iOS
npm run dev:ios
# Android
npm run dev:android
```
### 构建发布
```bash
# 微信小程序
npm run build
# H5
npm run build:h5
# App
npm run build:app
```
---
##  页面列表
| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/pages/index/index` | 今日目标、步数、活动入口、天气 |
| 运动记录 | `/pages/running/running` | GPS 实时跑步/骑行 |
| 任务流程 | `/pages/task/task-flow` | 每日训练任务链 + 日历 |
| 任务详情 | `/pages/task/task-detail` | 单任务详情 |
| 动作详情 | `/pages/task/exercise-detail` | 训练动作说明 |
| 目标首页 | `/pages/goal/goal` | 目标列表与管理 |
| 创建目标 | `/pages/goal/create` | 新建运动目标 |
| 目标详情 | `/pages/goal/detail` | 目标进度详情 |
| 社区 | `/pages/goal/community` | 计划分享社区 |
| 计划编辑器 | `/pages/plan/editor` | 训练计划创建/编辑 |
| 统计页 | `/pages/stats/stats` | 日/周/月统计图表 |
| 登录 | `/pages/user/login` | 多方式登录 |
| 注册 | `/pages/user/register` | 手机号注册 |
| 个人中心 | `/pages/user/profile` | 用户资料 |
| 编辑资料 | `/pages/user/profile-edit` | 修改个人信息 |
| 系统设置 | `/pages/user/settings` | 设置页 |
| AI 设置 | `/pages/user/ai-settings` | AI 模型选择与 Key 配置 |
| 运动列表 | `/pages/workout/workout` | 历史运动记录 |
| 运动详情 | `/pages/workout/detail` | 单次运动详情 |
| 设计器 | `/pages/design/design` | 小项目设计器 |
---
##  核心组件
| 组件 | 说明 |
|------|------|
| `AiPlanDialog` | AI 对话弹窗，支持 WebSocket 流式输出、多模型切换、自定义 API Key |
| `TaskFlow` | 任务流程图核心组件，含日历选择器、任务链、拖拽排序 |
| `TaskCard` | 任务卡片，支持优先级标签、状态指示、选中高亮 |
| `TrainingStatusCard` | 训练状态卡片，展示当前训练进度 |
| `PlanCard` | 计划卡片，展示训练计划概要 |
| `PlanEditor` | 计划编辑器，支持周视图、课程添加、动作配置 |
| `TemplateMarket` | 模板市场，展示系统推荐训练模板 |
| `TypeSelector` | 运动类型选择器 |
| `WeekDateBar` | 周日期选择条 |
---
##  设计规范
详见 [`design-style.md`](design-style.md)，包含：
- 色彩体系（主色 `#22c55e`、辅助色、状态色）
- 渐变规范（主色 / 深色 / 蓝紫 / 级别）
- 圆角与间距标准
- 字体层级
- 阴影规范
- 布局模式（页面 / 卡片 / 列表 / 底部栏）
- 组件模式（标签、选项卡、按钮、输入框、空状态）
- 毛玻璃效果
- 运动类型配色映射
---
##  API 封装
所有 API 请求统一封装在 `src/api/` 目录下，使用 `request.js` 拦截器处理：
- 自动注入 JWT Token (`Authorization: Bearer <token>`)
- Token 过期自动跳转登录
- 统一错误处理
示例：
```javascript
import { getUserInfo } from '@/api/user.js'
const res = await getUserInfo()
```
---
##  深色模式
全局深色模式已通过 `stores/theme.js` 管理，适配策略：
- 页面背景：`#0f172a` → `#1e293b`
- 卡片背景：`#1e293b`
- 文字反色：白/灰层级
- 所有页面和组件均已适配
---
##  开发计划
- [x] 项目搭建与基础配置
- [x] 登录/注册/游客/管理员多方式认证
- [x] 首页（今日目标、步数、天气）
- [x] 运动记录（GPS、轨迹、暂停恢复）
- [x] 目标管理（CRUD、进度）
- [x] 任务流程图（日历、拖拽、优先级）
- [x] 训练计划（编辑器、模板市场）
- [x] AI 对话（WebSocket、流式、多模型）
- [x] AI 计划生成
- [x] 数据统计（ECharts 图表）
- [x] 深色模式全局适配
- [x] Lottie 动画
- [x] 小项目设计器
- [ ] 地图轨迹展示
- [ ] 社区互动（点赞、评论）
- [ ] 微信登录
- [ ] 穿戴设备同步
---
##  相关文档
| 文档 | 说明 |
|------|------|
| [`design-style.md`](design-style.md) | UI 设计规范 |
| [`ADMIN_SETUP.md`](ADMIN_SETUP.md) | 管理员账号初始化 |
| [`USER_ACCOUNTS.md`](USER_ACCOUNTS.md) | 测试账号信息 |
| [`TASK_FLOW_README.md`](TASK_FLOW_README.md) | 任务流程图开发文档 |
| [`TASK_FLOW_BACKEND.md`](TASK_FLOW_BACKEND.md) | 任务流程后端对接文档 |