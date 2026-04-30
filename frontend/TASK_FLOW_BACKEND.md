# 任务流程图后端实体修改文档

## 概述
为支持新的任务流程图界面功能，需要对后端数据库实体进行修改，主要涉及任务表的结构扩展。

## 数据库表结构变更

### 1. 任务表 (task) 扩展

#### 新增字段

```sql
-- 优先级字段
ALTER TABLE task ADD COLUMN priority VARCHAR(10) DEFAULT 'medium' COMMENT '优先级: high-高, medium-中, low-低';

-- 任务顺序字段
ALTER TABLE task ADD COLUMN task_order INT DEFAULT 1 COMMENT '任务在流程中的顺序';

-- 任务类型字段
ALTER TABLE task ADD COLUMN task_type VARCHAR(20) DEFAULT 'strength' COMMENT '任务类型: warmup-热身, strength-力量, cardio-有氧, core-核心, stretch-拉伸, rest-休息';

-- 预计时长字段(分钟)
ALTER TABLE task ADD COLUMN duration INT DEFAULT 0 COMMENT '预计完成时长(分钟)';

-- 消耗卡路里字段
ALTER TABLE task ADD COLUMN calories INT DEFAULT 0 COMMENT '预计消耗卡路里';

-- 组数字段
ALTER TABLE task ADD COLUMN sets INT DEFAULT 1 COMMENT '训练组数';

-- 任务图标字段
ALTER TABLE task ADD COLUMN icon VARCHAR(50) DEFAULT NULL COMMENT '任务图标';

-- 创建索引
CREATE INDEX idx_task_order ON task(task_date, task_order);
CREATE INDEX idx_task_priority ON task(priority);
CREATE INDEX idx_task_type ON task(task_type);
```

### 2. 任务目标表 (task_goal) 新建

```sql
CREATE TABLE task_goal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '目标标题',
    description TEXT COMMENT '目标描述',
    target_progress INT DEFAULT 100 COMMENT '目标进度值',
    current_progress INT DEFAULT 0 COMMENT '当前进度值',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已关闭, 1-进行中, 2-已完成',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务目标表';
```

### 3. 任务与目标关联表 (task_goal_relation) 新建

```sql
CREATE TABLE task_goal_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goal_id BIGINT NOT NULL COMMENT '目标ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_goal_task (goal_id, task_id),
    INDEX idx_goal_id (goal_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务与目标关联表';
```

## Java 实体类修改

### 1. Task.java (任务实体)

```java
@Entity
@Table(name = "task")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "task_date")
    private LocalDate taskDate;
    
    // ==================== 新增字段 ====================
    
    /** 优先级: high-高, medium-中, low-低 */
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    /** 任务在流程中的顺序 */
    @Column(name = "task_order")
    private Integer order = 1;
    
    /** 任务类型 */
    @Column(name = "task_type")
    @Enumerated(EnumType.STRING)
    private TaskType taskType = TaskType.STRENGTH;
    
    /** 预计完成时长(分钟) */
    @Column(name = "duration")
    private Integer duration = 0;
    
    /** 预计消耗卡路里 */
    @Column(name = "calories")
    private Integer calories = 0;
    
    /** 训练组数 */
    @Column(name = "sets")
    private Integer sets = 1;
    
    /** 任务图标 */
    @Column(name = "icon")
    private String icon;
    
    // ==================== 原有字段 ====================
    
    @Column(name = "status")
    private Integer status = 0; // 0-未开始, 1-进行中, 2-已完成
    
    @Column(name = "progress")
    private Integer progress = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 枚举定义
    public enum Priority {
        HIGH("高"), MEDIUM("中"), LOW("低");
        
        private final String label;
        
        Priority(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum TaskType {
        WARMUP("热身", "🔥"),
        STRENGTH("力量", "💪"),
        CARDIO("有氧", "❤️"),
        CORE("核心", "🎯"),
        STRETCH("拉伸", "🧘"),
        REST("休息", "😴");
        
        private final String label;
        private final String icon;
        
        TaskType(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getIcon() {
            return icon;
        }
    }
    
    // Getters and Setters...
}
```

### 2. TaskGoal.java (目标实体) - 新建

```java
@Entity
@Table(name = "task_goal")
public class TaskGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "target_progress")
    private Integer targetProgress = 100;
    
    @Column(name = "current_progress")
    private Integer currentProgress = 0;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "status")
    private Integer status = 1; // 0-已关闭, 1-进行中, 2-已完成
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters...
}
```

## API 接口定义

### 新增接口

#### 1. 获取任务流程图数据
```
GET /api/task/flow

请求参数:
  - date: string (YYYY-MM-DD) 日期，默认为今天

响应数据:
{
  "code": 200,
  "message": "success",
  "data": {
    "goal": {
      "id": 1,
      "title": "增肌塑形计划",
      "description": "12周增肌训练，提升力量与体型",
      "targetProgress": 100,
      "currentProgress": 40
    },
    "tasks": [
      {
        "id": 1,
        "name": "热身运动",
        "type": "warmup",
        "priority": "high",
        "status": 2,
        "duration": 10,
        "sets": 1,
        "calories": 50,
        "order": 1
      }
      // ... 更多任务
    ],
    "totalTasks": 5,
    "completedTasks": 2,
    "totalProgress": 40
  }
}
```

#### 2. 更新任务顺序
```
POST /api/task/flow/reorder

请求体:
[
  {"taskId": 1, "order": 2},
  {"taskId": 2, "order": 1},
  {"taskId": 3, "order": 3}
]

响应数据:
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 3. 更新任务优先级
```
POST /api/task/{taskId}/priority

请求参数:
  - priority: string (high/medium/low)

响应数据:
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 4. 开始指定任务
```
POST /api/task/{taskId}/start

响应数据:
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1,
    "status": 1,
    "startTime": "2026-04-11T09:00:00"
  }
}
```

#### 5. 获取指定日期任务流程
```
GET /api/task/flow/date

请求参数:
  - date: string (YYYY-MM-DD) 日期

响应数据: 同 /api/task/flow
```

#### 6. 批量更新任务状态
```
POST /api/task/batch/status

请求体:
{
  "taskIds": [1, 2, 3],
  "status": 2
}

响应数据:
{
  "code": 200,
  "message": "success",
  "data": {
    "updatedCount": 3
  }
}
```

## 前端 API 封装 (已更新)

已在 `src/api/task.js` 中添加以下方法：

```javascript
// 获取任务流程图数据
export function getTaskFlow(date) {
  return get('/task/flow', { date })
}

// 获取指定日期的任务流程
export function getTaskFlowByDate(date) {
  return get('/task/flow/date', { date })
}

// 更新任务顺序
export function updateTaskOrder(orderData) {
  return post('/task/flow/reorder', orderData)
}

// 开始指定任务
export function startTask(taskId) {
  return post(`/task/${taskId}/start`)
}

// 更新任务优先级
export function updateTaskPriority(taskId, priority) {
  return post(`/task/${taskId}/priority`, { priority })
}

// 获取任务详情
export function getTaskDetail(taskId) {
  return get(`/task/${taskId}/detail`)
}

// 批量更新任务状态
export function batchUpdateTaskStatus(taskIds, status) {
  return post('/task/batch/status', { taskIds, status })
}
```

## 数据迁移脚本

```sql
-- 为现有任务设置默认值
UPDATE task SET 
    priority = 'medium',
    task_order = id,
    task_type = 'strength',
    duration = 15,
    calories = 100,
    sets = 3
WHERE priority IS NULL;

-- 创建默认目标
INSERT INTO task_goal (user_id, title, description, target_progress, current_progress, start_date, status)
SELECT DISTINCT user_id, '健身目标', '坚持训练，保持健康', 100, 0, CURDATE(), 1
FROM task
WHERE NOT EXISTS (
    SELECT 1 FROM task_goal WHERE task_goal.user_id = task.user_id
);
```

## 注意事项

1. **兼容性**: 新字段都有默认值，不影响现有数据
2. **索引优化**: 已为常用查询字段创建索引
3. **数据一致性**: 更新任务顺序时需要考虑并发问题
4. **前端适配**: 前端组件已做好空值处理，向后兼容
