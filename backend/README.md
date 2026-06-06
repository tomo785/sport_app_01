# 🖥️ 运动 App 后端服务

> Spring Boot 3.2 + MyBatis-Plus 构建的 RESTful API 服务，支持 WebSocket 实时通信、多提供商 AI 对话、JWT 认证。

---

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | Web 框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL Connector/J | 8.0+ | 数据库驱动 |
| Redis | 7.x | 缓存 / Session |
| JWT (jjwt) | 0.12.3 | 身份认证 |
| Knife4j | 4.4.0 | API 文档 |
| WebSocket | — | AI 实时对话 |
| Hutool | 5.8.24 | 工具库 |
| Fastjson2 | 2.0.43 | JSON 处理 |
| Lombok | 1.18.30 | 代码简化 |

---

## 📁 项目结构

```
backend/
├── src/main/java/com/sport/
│   ├── api/
│   │   ├── controller/          # 8 个控制器
│   │   │   ├── AIController.java           # AI 对话 & 计划生成
│   │   │   ├── DailyTaskController.java    # 每日任务
│   │   │   ├── GoalController.java         # 运动目标
│   │   │   ├── PlanController.java         # 训练计划
│   │   │   ├── StatsController.java        # 数据统计
│   │   │   ├── UserController.java         # 用户认证
│   │   │   ├── WeatherController.java      # 天气服务
│   │   │   └── WorkoutController.java      # 运动记录
│   │   ├── dto/                 # 请求 DTO（20+）
│   │   ├── vo/                  # 响应 VO（20+）
│   │   └── ws/
│   │       └── AIWebSocketHandler.java    # WebSocket AI 处理器
│   ├── service/                 # 服务接口 & 实现（8 个模块）
│   │   ├── AIService.java
│   │   ├── DailyTaskService.java
│   │   ├── GoalService.java
│   │   ├── PlanService.java
│   │   ├── StatsService.java
│   │   ├── UserService.java
│   │   ├── WeatherService.java
│   │   └── WorkoutService.java
│   ├── mapper/                  # MyBatis Mapper
│   ├── model/
│   │   ├── entity/              # 12 个实体类
│   │   │   ├── User.java
│   │   │   ├── WorkoutRecord.java
│   │   │   ├── WorkoutTrajectory.java
│   │   │   ├── WorkoutGoal.java
│   │   │   ├── UserDailyStats.java
│   │   │   ├── UserDailyTask.java
│   │   │   ├── UserTaskExercise.java
│   │   │   ├── UserTrainingPlan.java
│   │   │   ├── TrainingPlan.java
│   │   │   ├── TrainingCourse.java
│   │   │   └── TrainingExercise.java
│   │   └── enums/               # 17 个枚举类
│   │       ├── GenderEnum.java
│   │       ├── WorkoutTypeEnum.java
│   │       ├── WorkoutStatusEnum.java
│   │       ├── TaskTypeEnum.java
│   │       ├── TaskStatusEnum.java
│   │       ├── TaskPriorityEnum.java
│   │       ├── ExerciseTypeEnum.java
│   │       ├── ExerciseStatusEnum.java
│   │       ├── ExerciseDifficultyEnum.java
│   │       ├── TrainingLevelEnum.java
│   │       ├── TrainingPlanStatusEnum.java
│   │       ├── TrainingCourseTypeEnum.java
│   │       ├── DailyTaskTypeEnum.java
│   │       ├── DailyTaskStatusEnum.java
│   │       ├── GoalTypeEnum.java
│   │       ├── RewardTypeEnum.java
│   │       └── TaskFlowTypeEnum.java
│   └── common/
│       ├── config/              # 配置类
│       │   ├── AIProviderConfig.java        # 多 AI 提供商配置
│       │   ├── CorsConfig.java              # 跨域配置
│       │   ├── Knife4jConfig.java           # API 文档配置
│       │   ├── MybatisPlusConfig.java       # MyBatis-Plus 配置
│       │   ├── RestTemplateConfig.java      # HTTP 客户端
│       │   ├── TokenHandshakeInterceptor.java # WebSocket Token 校验
│       │   └── WebSocketConfig.java         # WebSocket 配置
│       ├── constant/            # 常量
│       ├── exception/           # 全局异常处理
│       └── util/                # 工具类
│           ├── JwtUtil.java
│           └── Result.java
├── src/main/resources/
│   ├── mapper/                  # XML 映射文件
│   ├── application.yml          # 主配置文件
│   └── schema.sql               # 数据库初始化脚本
└── pom.xml
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+

### 1. 数据库初始化

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 2. 配置修改

编辑 `src/main/resources/application.yml`，修改以下配置项：

```yaml
spring:
  datasource:
    username: your_db_username
    password: your_db_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

jwt:
  secret: your_random_secret_key_at_least_32_chars

ai:
  providers:
    kimi:
      api-key: your_kimi_api_key
    deepseek:
      api-key: your_deepseek_api_key

weather:
  api-key: your_qweather_api_key
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 访问 API 文档

启动成功后，访问 http://localhost:8080/api/v1/doc.html 查看接口文档。

---

## 📡 API 接口概览

### 用户模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | POST | `/api/v1/user/sms-code` | 发送短信验证码 |
| 手机号登录 | POST | `/api/v1/user/login` | 手机号 + 验证码登录 |
| 密码登录 | POST | `/api/v1/user/login/password` | 手机号 + 密码登录 |
| 用户注册 | POST | `/api/v1/user/register` | 手机号注册 |
| 获取用户信息 | GET | `/api/v1/user/info` | 获取当前用户信息 |
| 更新用户信息 | PUT | `/api/v1/user/info` | 修改用户资料 |
| 修改密码 | PUT | `/api/v1/user/password` | 修改登录密码 |
| 管理员登录 | POST | `/api/v1/admin/login` | 管理员账号登录 |

### 运动记录模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 开始运动 | POST | `/api/v1/workout/start` | 创建运动记录 |
| 上传轨迹点 | POST | `/api/v1/workout/trajectory` | 上传实时 GPS 轨迹 |
| 暂停运动 | PUT | `/api/v1/workout/{id}/pause` | 暂停运动 |
| 继续运动 | PUT | `/api/v1/workout/{id}/resume` | 继续运动 |
| 结束运动 | PUT | `/api/v1/workout/{id}/finish` | 结束运动并计算数据 |
| 获取运动详情 | GET | `/api/v1/workout/{id}` | 单条运动记录详情 |
| 获取运动列表 | GET | `/api/v1/workout/list` | 分页查询运动记录 |
| 获取运动轨迹 | GET | `/api/v1/workout/{id}/trajectory` | 获取完整 GPS 轨迹 |

### 目标模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建目标 | POST | `/api/v1/goal` | 创建运动目标 |
| 获取目标列表 | GET | `/api/v1/goal` | 查询用户目标 |
| 获取目标详情 | GET | `/api/v1/goal/{id}` | 单条目标详情 |
| 更新目标 | PUT | `/api/v1/goal/{id}` | 修改目标 |
| 删除目标 | DELETE | `/api/v1/goal/{id}` | 删除目标 |
| 获取目标进度 | GET | `/api/v1/goal/{id}/progress` | 目标完成进度 |

### 任务模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取今日任务看板 | GET | `/api/v1/task/dashboard` | 今日任务总览 |
| 获取任务流程图 | GET | `/api/v1/task/flow` | 任务链数据 |
| 更新任务顺序 | POST | `/api/v1/task/reorder` | 拖拽排序保存 |
| 更新任务优先级 | PUT | `/api/v1/task/{id}/priority` | 修改优先级 |
| 开始任务 | POST | `/api/v1/task/{id}/start` | 开始指定任务 |
| 完成任务 | POST | `/api/v1/task/{id}/complete` | 标记任务完成 |
| 批量更新状态 | POST | `/api/v1/task/batch/status` | 批量状态更新 |

### 计划模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建计划 | POST | `/api/v1/plan` | 创建训练计划 |
| 获取计划列表 | GET | `/api/v1/plan` | 查询计划列表 |
| 获取计划详情 | GET | `/api/v1/plan/{id}` | 单条计划详情 |
| 更新计划 | PUT | `/api/v1/plan/{id}` | 修改计划 |
| 删除计划 | DELETE | `/api/v1/plan/{id}` | 删除计划 |
| 获取模板市场 | GET | `/api/v1/plan/templates` | 系统推荐模板 |
| AI 生成计划 | POST | `/api/v1/plan/ai-generate` | AI 智能生成训练计划 |

### AI 模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取模型列表 | GET | `/api/v1/ai/models` | 可用 AI 模型列表（key 脱敏） |
| 对话补全 | POST | `/api/v1/ai/chat` | 单次对话（非流式） |
| WebSocket 对话 | WS | `/api/v1/ws/ai` | 实时流式对话（推荐） |

### 统计模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 数据看板 | GET | `/api/v1/stats/dashboard` | 首页数据看板 |
| 日报表 | GET | `/api/v1/stats/daily` | 每日运动统计 |
| 周报表 | GET | `/api/v1/stats/weekly` | 每周运动统计 |
| 月报表 | GET | `/api/v1/stats/monthly` | 每月运动统计 |
| 历史趋势 | GET | `/api/v1/stats/trend` | 历史运动趋势 |
| 训练趋势 | GET | `/api/v1/stats/training-trend` | 训练趋势分析 |

### 天气模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 实时天气 | GET | `/api/v1/weather/now` | 当前天气 |
| 空气质量 | GET | `/api/v1/weather/aqi` | AQI 指数 |
| 生活指数 | GET | `/api/v1/weather/indices` | 运动/穿衣等指数 |

---

## 🗄️ 数据库表结构

| 表名 | 说明 |
|------|------|
| `t_user` | 用户表 |
| `t_workout_record` | 运动记录表 |
| `t_workout_trajectory` | 运动轨迹点表 |
| `t_workout_goal` | 运动目标表 |
| `t_user_daily_stats` | 用户每日统计表 |
| `t_user_daily_task` | 用户每日任务表 |
| `t_user_task_exercise` | 任务动作明细表 |
| `t_user_training_plan` | 用户训练计划表 |
| `t_training_plan` | 系统训练计划表 |
| `t_training_course` | 训练课程表 |
| `t_training_exercise` | 训练动作表 |
| `t_sms_code` | 短信验证码表 |

---

## 🔧 配置说明

### AI 模型配置

支持多提供商配置，默认使用 DeepSeek：

真实 API Key 不要写入仓库。请使用本地私有配置、环境变量或部署平台 Secret 注入，仓库中只保留 `your_kimi_key`、`your_deepseek_key` 这类占位符。

```yaml
ai:
  default-provider: deepseek
  providers:
    kimi:
      name: Kimi K2.6
      api-key: your_kimi_key
      base-url: https://api.moonshot.cn/v1
      model: kimi-k2.6
    deepseek:
      name: DeepSeek V4 Flash
      api-key: your_deepseek_key
      base-url: https://api.deepseek.com/v1
      model: deepseek-v4-flash
```

### 文件存储配置

```yaml
file:
  storage-type: local  # local / minio / aliyun
  local:
    upload-path: ./uploads/
    access-url: http://localhost:8080/uploads/
```

---

## 📋 开发计划

- [x] 项目结构搭建
- [x] 用户认证模块（手机/密码/管理员/游客）
- [x] 运动记录模块（GPS 轨迹、暂停恢复）
- [x] 运动目标模块
- [x] 数据统计模块（日/周/月 + 趋势）
- [x] 任务流程模块（拖拽排序、优先级、日历）
- [x] 训练计划模块（CRUD + 模板市场 + AI 生成）
- [x] AI 对话模块（WebSocket 流式 + 多模型）
- [x] 天气服务集成
- [x] WebSocket Token 握手校验
- [ ] 微信登录集成
- [ ] 地图轨迹展示
- [ ] MinIO / 阿里云 OSS 文件上传
- [ ] 社区互动（点赞、评论）
- [ ] 穿戴设备数据同步

---

## ⚠️ 注意事项

1. **生产环境**请修改 JWT Secret、数据库密码、Redis 密码
2. **AI API Key**请替换为自有密钥，但不得提交到 Git 仓库
3. **短信服务**需要配置真实短信服务商（当前为模拟）
4. **文件上传**默认本地存储，生产环境建议切换 MinIO/OSS
