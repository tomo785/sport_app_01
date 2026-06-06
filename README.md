# 🏃 运动健身 App

当前版本：`v1.1.3`

> 基于 uni-app Vue3 + Spring Boot 的全栈运动健身应用，支持 AI 智能教练、训练计划管理、实时运动记录、任务流程图等功能。

---

## 📱 项目简介

运动健身 App 是一款面向健身爱好者的移动端应用，支持微信小程序、H5、App 多端运行。核心功能包括：

- 🤖 **AI 智能教练** — WebSocket 实时对话，支持 Kimi / DeepSeek 多模型切换，可生成个性化训练计划
- 📋 **任务流程图** — 可视化每日训练任务链，支持拖拽排序、优先级标记、完成进度追踪
- 🏃 **运动记录** — GPS 实时轨迹记录，支持跑步/骑行，暂停/继续/结束完整生命周期
- 🎯 **目标管理** — 创建运动目标并追踪完成进度
- 📊 **数据统计** — 日/周/月多维统计，趋势图表可视化
- 🌤️ **天气集成** — 实时天气与空气质量，辅助户外运动决策
- 🌙 **深色模式** — 全局深色主题适配

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────┐
│              前端 (uni-app Vue3)               │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │微信小程序 │ │   H5   │ │   App   │        │
│  └─────────┘ └─────────┘ └─────────┘        │
│         Pinia · uview-plus · ECharts          │
├─────────────────────────────────────────────┤
│              后端 (Spring Boot)                │
│    REST API · WebSocket · JWT · MyBatis-Plus  │
├─────────────────────────────────────────────┤
│         MySQL 8.0  ←→  Redis 7.x              │
└─────────────────────────────────────────────┘
```

| 层级 | 技术栈 | 版本 |
|------|--------|------|
| 前端 | uni-app (Vue3) + Pinia + uview-plus | 3.0.0-alpha |
| 后端 | Spring Boot + MyBatis-Plus + JWT | 3.2.0 |
| 数据库 | MySQL + Redis | 8.0+ / 7.x |
| AI 服务 | Kimi (Moonshot) / DeepSeek | WebSocket 流式 |
| 天气服务 | QWeather 和风天气 | — |

---

## 📁 目录结构

```
sport_app_01/
├── backend/                    # 后端服务
│   ├── src/main/java/com/sport/
│   │   ├── api/               # API 层（Controller / DTO / VO）
│   │   ├── service/           # 服务层（8 个业务模块）
│   │   ├── mapper/            # 数据访问层
│   │   ├── model/             # 实体 & 枚举（12 实体 / 17 枚举）
│   │   └── common/            # 公共配置、工具、异常
│   ├── src/main/resources/
│   │   ├── mapper/            # MyBatis XML 映射
│   │   ├── application.yml    # 配置文件
│   │   └── schema.sql         # 数据库初始化脚本
│   └── pom.xml
│
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── pages/             # 20 个页面
│   │   │   ├── index/         # 首页（今日目标 / 活动入口）
│   │   │   ├── running/       # 运动记录页
│   │   │   ├── task/          # 任务流程图 / 详情
│   │   │   ├── goal/          # 目标管理 / 社区 / 创建
│   │   │   ├── plan/          # 计划编辑器
│   │   │   ├── stats/         # 统计页
│   │   │   ├── user/          # 登录 / 注册 / 设置 / AI 设置
│   │   │   ├── workout/       # 运动详情 / 列表
│   │   │   └── design/        # 小项目设计器
│   │   ├── components/        # 9 个公共组件
│   │   ├── api/               # API 封装
│   │   ├── stores/            # Pinia 状态管理
│   │   └── utils/             # 工具函数
│   ├── package.json
│   └── design-style.md        # 设计规范文档
│
├── .trae/                     # AI 开发文档与规格
├── logs/                      # 运行日志
└── README.md                  # 本文件
```

---

## ✅ 功能模块与完成进度

### 核心功能

| 模块 | 功能点 | 状态 |
|------|--------|------|
| **用户系统** | 手机号验证码登录 | ✅ 已完成 |
| | 密码注册 / 登录 | ✅ 已完成 |
| | 管理员账号体系 | ✅ 已完成 |
| | 游客模式 | ✅ 已完成 |
| | 微信登录 | ⏳ 待开发 |
| **运动记录** | GPS 实时轨迹（跑步/骑行） | ✅ 已完成 |
| | 运动暂停 / 继续 / 结束 | ✅ 已完成 |
| | 运动详情与历史列表 | ✅ 已完成 |
| | 地图轨迹展示 | ⏳ 待完善 |
| **任务流程** | 每日任务流程图 | ✅ 已完成 |
| | 任务卡片拖拽排序 | ✅ 已完成 |
| | 优先级标记（高/中/低） | ✅ 已完成 |
| | 完成进度追踪 | ✅ 已完成 |
| | 日历式日期选择器 | ✅ 已完成 |
| **目标管理** | 创建 / 编辑 / 删除目标 | ✅ 已完成 |
| | 目标进度追踪 | ✅ 已完成 |
| | 社区计划分享 | 🔄 基础版 |
| **训练计划** | 计划创建与编辑 | ✅ 已完成 |
| | 模板市场 | ✅ 已完成 |
| | AI 智能生成计划 | ✅ 已完成 |
| | 小项目设计器 | ✅ 已完成 |
| **AI 教练** | WebSocket 实时对话 | ✅ 已完成 |
| | 多模型切换（Kimi / DeepSeek） | ✅ 已完成 |
| | 流式输出 | ✅ 已完成 |
| | AI 计划生成 | ✅ 已完成 |
| | 自定义 API Key | ✅ 已完成 |
| **数据统计** | 日 / 周 / 月统计 | ✅ 已完成 |
| | 数据看板 | ✅ 已完成 |
| | 趋势图表（ECharts） | ✅ 已完成 |
| | 训练趋势分析 | ✅ 已完成 |
| **天气服务** | 实时天气 | ✅ 已完成 |
| | 空气质量（AQI） | ✅ 已完成 |
| | 生活指数 | ✅ 已完成 |
| **系统功能** | JWT 认证 | ✅ 已完成 |
| | Redis 缓存 | ✅ 已完成 |
| | 深色模式全局适配 | ✅ 已完成 |
| | Lottie 动画 | ✅ 已完成 |
| | 文件上传（本地） | ✅ 已完成 |
| | MinIO / 阿里云 OSS | ⏳ 待开发 |
| | 穿戴设备同步 | ⏳ 待开发 |

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+
- Node.js 18+

### 1. 克隆仓库

```bash
git clone <仓库地址>
cd sport_app_01
```

### 2. 启动后端

```bash
cd backend

# 1. 创建数据库并导入初始化脚本
mysql -u root -p < src/main/resources/schema.sql

# 2. 修改本地配置（数据库、Redis、AI API Key）
# 不要把真实密钥提交到仓库；建议使用本地 application-local.yml 或环境变量
# Kimi / DeepSeek 等 AI API Key 只保存在本机或部署平台的密钥配置中

# 3. 运行
mvn spring-boot:run
```

后端服务启动后：
- API 地址：`http://localhost:8080/api/v1`
- 接口文档：`http://localhost:8080/api/v1/doc.html` (Knife4j)
- WebSocket：`ws://localhost:8080/api/v1/ws/ai`

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 微信小程序开发
npm run dev

# H5 开发
npm run dev:h5
```

---

## 📖 子项目文档

| 文档 | 说明 |
|------|------|
| [`backend/README.md`](backend/README.md) | 后端服务详细文档（API 列表、数据库表、配置说明） |
| [`frontend/README.md`](frontend/README.md) | 前端应用详细文档（页面列表、组件说明、开发规范） |
| [`frontend/design-style.md`](frontend/design-style.md) | UI 设计规范（色彩、字体、间距、组件模式） |
| [`frontend/ADMIN_SETUP.md`](frontend/ADMIN_SETUP.md) | 管理员账号初始化指南 |
| [`frontend/USER_ACCOUNTS.md`](frontend/USER_ACCOUNTS.md) | 测试账号信息 |

---

## ⚠️ 安全提醒

**生产环境部署前请务必：**

1. 修改 `application.yml` 中的默认密码（数据库、Redis、MinIO）
2. 替换 AI API Key 为自有密钥，并仅保存到本地配置、环境变量或部署平台 Secret
3. 修改 JWT Secret
4. 使用 HTTPS 传输
5. 将 `.env`、`application-local.yml`、真实部署配置加入 `.gitignore`，避免密钥泄露

### 敏感信息提交规则

本仓库不上传任何真实敏感内容，包括但不限于：

- Kimi、DeepSeek、OpenAI 等 AI 服务 API Key
- 数据库、Redis、对象存储、JWT Secret 等生产密钥
- 个人账号、手机号、定位轨迹、健康数据等真实用户隐私
- 任何包含真实 token、cookie、authorization header 的日志或截图

仓库中的配置文件只允许保留占位符，例如 `your-kimi-api-key`。如需本地调试，请在本机单独创建私有配置文件，或通过环境变量注入。

> 当前仓库中的 `application.yml` 仅保留示例占位符。真实 Kimi API Key、DeepSeek API Key 等不得提交。

---

## 📄 许可证

MIT License
