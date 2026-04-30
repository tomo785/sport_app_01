# 运动App后端服务

## 技术栈

- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis 7.x
- JWT
- Knife4j (API文档)

## 项目结构

```
backend/
├── src/main/java/com/sport/
│   ├── api/                    # API层
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   └── vo/                 # 视图对象
│   ├── service/                # 服务层
│   │   └── impl/               # 服务实现
│   ├── mapper/                 # 数据访问层
│   ├── model/                  # 模型层
│   │   ├── entity/             # 实体类
│   │   └── enums/              # 枚举类
│   ├── common/                 # 公共模块
│   │   ├── config/             # 配置类
│   │   ├── constant/           # 常量类
│   │   ├── exception/          # 异常处理
│   │   └── util/               # 工具类
│   └── SportAppApplication.java # 启动类
├── src/main/resources/
│   ├── mapper/                 # MyBatis XML映射文件
│   ├── application.yml         # 配置文件
│   └── schema.sql              # 数据库初始化脚本
└── pom.xml                     # Maven配置文件
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+

### 2. 数据库初始化

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库和Redis连接信息：

```yaml
spring:
  datasource:
    username: root
    password: ZQQlpw1314
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

### 5. 访问API文档

启动成功后，访问 http://localhost:8080/api/v1/doc.html 查看API文档。

## API接口

### 用户模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | POST | /api/v1/user/sms-code | 发送短信验证码 |
| 手机号登录 | POST | /api/v1/user/login | 手机号+验证码登录 |
| 获取用户信息 | GET | /api/v1/user/info | 获取当前用户信息 |

### 运动记录模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 开始运动 | POST | /api/v1/workout/start | 创建运动记录 |
| 上传轨迹点 | POST | /api/v1/workout/trajectory | 上传实时轨迹 |
| 暂停运动 | PUT | /api/v1/workout/pause | 暂停运动 |
| 继续运动 | PUT | /api/v1/workout/resume | 继续运动 |
| 结束运动 | PUT | /api/v1/workout/finish | 结束运动并计算数据 |
| 获取运动详情 | GET | /api/v1/workout/{id} | 获取单条运动记录 |
| 获取运动列表 | GET | /api/v1/workout/list | 分页查询运动记录 |
| 获取运动轨迹 | GET | /api/v1/workout/{id}/trajectory | 获取完整轨迹 |

### 目标模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建目标 | POST | /api/v1/goal | 创建运动目标 |
| 获取目标列表 | GET | /api/v1/goal | 查询用户目标 |
| 更新目标 | PUT | /api/v1/goal/{id} | 修改目标 |
| 删除目标 | DELETE | /api/v1/goal/{id} | 删除目标 |
| 获取目标进度 | GET | /api/v1/goal/{id}/progress | 目标完成进度 |

### 统计模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 日报表 | GET | /api/v1/stats/daily | 每日运动统计 |
| 周报表 | GET | /api/v1/stats/weekly | 每周运动统计 |
| 月报表 | GET | /api/v1/stats/monthly | 每月运动统计 |
| 历史记录 | GET | /api/v1/stats/history | 历史运动趋势 |

## 数据库表结构

- `t_user` - 用户表
- `t_workout_record` - 运动记录表
- `t_workout_trajectory` - 运动轨迹点表
- `t_workout_goal` - 运动目标表
- `t_user_daily_stats` - 用户每日统计表
- `t_user_weekly_stats` - 用户每周统计表
- `t_user_monthly_stats` - 用户每月统计表
- `t_sms_code` - 短信验证码表

## 开发计划

- [x] 项目结构搭建
- [x] 用户认证模块
- [x] 运动记录模块
- [x] 运动目标模块
- [x] 数据统计模块
- [ ] 地图功能
- [ ] 微信登录
- [ ] 文件上传

## 注意事项

1. 短信服务需要配置真实的短信服务商
2. 文件上传默认使用本地存储，可切换到MinIO或阿里云OSS
3. 生产环境请修改JWT密钥和Redis密码
