-- 创建数据库
CREATE DATABASE IF NOT EXISTS sport_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sport_app;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(32) UNIQUE COMMENT '账号',
    phone           VARCHAR(20) UNIQUE COMMENT '手机号',
    password        VARCHAR(128) COMMENT '加密密码',
    openid          VARCHAR(128) UNIQUE COMMENT '微信OpenID',
    unionid         VARCHAR(128) UNIQUE COMMENT '微信UnionID',
    nickname        VARCHAR(64) COMMENT '昵称',
    avatar          VARCHAR(512) COMMENT '头像URL',
    gender          TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    age             INT COMMENT '年龄',
    height          INT COMMENT '身高(厘米)',
    weight          DECIMAL(5,2) COMMENT '体重(公斤)',
    role            TINYINT DEFAULT 1 COMMENT '角色 1普通用户 2管理员',
    status          TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag     TINYINT DEFAULT 0 COMMENT '删除标识 0未删除 1已删除',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 运动记录表
CREATE TABLE IF NOT EXISTS t_workout_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    type            TINYINT NOT NULL COMMENT '运动类型 1跑步 2健走 3骑行',
    start_time      DATETIME NOT NULL COMMENT '开始时间',
    end_time        DATETIME COMMENT '结束时间',
    duration        INT COMMENT '时长(秒)',
    distance        INT COMMENT '距离(米)',
    calories        INT COMMENT '卡路里(千卡)',
    avg_speed       DECIMAL(5,2) COMMENT '平均配速(米/秒)',
    max_speed       DECIMAL(5,2) COMMENT '最大配速(米/秒)',
    min_altitude    INT COMMENT '最低海拔(米)',
    max_altitude    INT COMMENT '最高海拔(米)',
    total_ascent    INT COMMENT '累计爬升(米)',
    total_descent   INT COMMENT '累计下降(米)',
    status          TINYINT DEFAULT 0 COMMENT '状态 0进行中 1已完成 2已取消',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_time (user_id, start_time),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动记录表';

-- 运动轨迹点表
CREATE TABLE IF NOT EXISTS t_workout_trajectory (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轨迹点ID',
    record_id       BIGINT NOT NULL COMMENT '运动记录ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    latitude        DECIMAL(10,7) NOT NULL COMMENT '纬度',
    longitude       DECIMAL(10,7) NOT NULL COMMENT '经度',
    altitude        INT COMMENT '海拔(米)',
    speed           DECIMAL(5,2) COMMENT '速度(米/秒)',
    timestamp       BIGINT NOT NULL COMMENT '时间戳(毫秒)',
    sequence        INT NOT NULL COMMENT '序号',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_record_id (record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动轨迹点表';

-- 运动目标表
CREATE TABLE IF NOT EXISTS t_workout_goal (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '目标ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    type            TINYINT NOT NULL COMMENT '目标类型 1每日距离 2每周次数',
    target_value    INT NOT NULL COMMENT '目标值(米/次数)',
    start_date      DATE NOT NULL COMMENT '开始日期',
    end_date        DATE COMMENT '结束日期 NULL表示长期',
    status          TINYINT DEFAULT 1 COMMENT '状态 0已取消 1进行中 2已完成',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动目标表';

-- 用户每日统计表
CREATE TABLE IF NOT EXISTS t_user_daily_stats (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    stat_date       DATE NOT NULL COMMENT '统计日期',
    total_distance  INT DEFAULT 0 COMMENT '总距离(米)',
    total_duration  INT DEFAULT 0 COMMENT '总时长(秒)',
    total_calories  INT DEFAULT 0 COMMENT '总卡路里(千卡)',
    record_count    INT DEFAULT 0 COMMENT '运动次数',
    goal_id         BIGINT COMMENT '关联目标ID',
    goal_progress   INT DEFAULT 0 COMMENT '目标完成进度(%)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_user_id (user_id),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日统计表';

-- 用户每周统计表
CREATE TABLE IF NOT EXISTS t_user_weekly_stats (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    week_start      DATE NOT NULL COMMENT '周开始日期(周一)',
    week_end        DATE NOT NULL COMMENT '周结束日期(周日)',
    total_distance  INT DEFAULT 0 COMMENT '总距离(米)',
    total_duration  INT DEFAULT 0 COMMENT '总时长(秒)',
    total_calories  INT DEFAULT 0 COMMENT '总卡路里(千卡)',
    record_count    INT DEFAULT 0 COMMENT '运动次数',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_week (user_id, week_start),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每周统计表';

-- 用户每月统计表
CREATE TABLE IF NOT EXISTS t_user_monthly_stats (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    stat_year       INT NOT NULL COMMENT '统计年份',
    stat_month      TINYINT NOT NULL COMMENT '统计月份 1-12',
    total_distance  INT DEFAULT 0 COMMENT '总距离(米)',
    total_duration  INT DEFAULT 0 COMMENT '总时长(秒)',
    total_calories  INT DEFAULT 0 COMMENT '总卡路里(千卡)',
    record_count    INT DEFAULT 0 COMMENT '运动次数',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_month (user_id, stat_year, stat_month),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每月统计表';

-- 短信验证码表
CREATE TABLE IF NOT EXISTS t_sms_code (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    code            VARCHAR(6) NOT NULL COMMENT '验证码',
    type            TINYINT NOT NULL COMMENT '类型 1注册 2登录 3找回密码',
    expire_time     DATETIME NOT NULL COMMENT '过期时间',
    used            TINYINT DEFAULT 0 COMMENT '是否已使用 0否 1是',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phone_type (phone, type),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- 训练动作表（课程下的具体动作）
CREATE TABLE IF NOT EXISTS t_training_exercise (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动作ID',
    course_id           BIGINT NOT NULL COMMENT '所属课程ID',
    name                VARCHAR(100) NOT NULL COMMENT '动作名称',
    description         VARCHAR(500) COMMENT '动作描述',
    type                TINYINT DEFAULT 1 COMMENT '动作类型 1有氧 2力量 3拉伸',
    duration            INT COMMENT '动作时长(秒)，用于计时动作',
    sets                INT DEFAULT 1 COMMENT '组数',
    reps                INT COMMENT '每组次数',
    rest_time           INT DEFAULT 60 COMMENT '组间休息(秒)',
    equipment           VARCHAR(100) COMMENT '所需器械',
    target_muscles      VARCHAR(200) COMMENT '目标肌群',
    difficulty          TINYINT DEFAULT 1 COMMENT '难度 1简单 2中等 3困难',
    demo_image          VARCHAR(512) COMMENT '示范图片',
    demo_video          VARCHAR(512) COMMENT '示范视频',
    sort_order          INT DEFAULT 0 COMMENT '排序',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         TINYINT DEFAULT 0 COMMENT '删除标识',
    INDEX idx_course_id (course_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练动作表';

-- 训练计划表（社区计划复用此表，is_official=1 表示官方/社区计划）
CREATE TABLE IF NOT EXISTS t_training_plan (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    name            VARCHAR(100) NOT NULL COMMENT '计划名称',
    description     VARCHAR(500) COMMENT '计划描述',
    cover_image     VARCHAR(512) COMMENT '封面图URL',
    level           TINYINT COMMENT '难度等级 1新手 2进阶 3高手 4专家',
    total_weeks     INT COMMENT '总周数',
    workouts_per_week INT COMMENT '每周训练天数',
    total_duration  INT COMMENT '总时长(分钟)',
    target_distance BIGINT COMMENT '目标距离(米)',
    target_calories INT COMMENT '目标卡路里(千卡)',
    coach_name      VARCHAR(64) COMMENT '教练名称',
    is_official     TINYINT DEFAULT 0 COMMENT '是否官方/社区计划 0否 1是',
    view_count      INT DEFAULT 0 COMMENT '浏览量',
    enroll_count    INT DEFAULT 0 COMMENT '采用人数',
    rating          FLOAT DEFAULT 0 COMMENT '评分',
    rating_count    INT DEFAULT 0 COMMENT '评分人数',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT DEFAULT 0 COMMENT '删除标识 0未删除 1已删除',
    INDEX idx_level (level),
    INDEX idx_official (is_official),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划表';

-- 训练课程表（计划下的具体课程）
CREATE TABLE IF NOT EXISTS t_training_course (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    plan_id             BIGINT NOT NULL COMMENT '所属计划ID',
    name                VARCHAR(100) NOT NULL COMMENT '课程名称',
    description         VARCHAR(500) COMMENT '课程描述',
    week                INT COMMENT '第几周',
    day                 INT COMMENT '第几天',
    type                TINYINT COMMENT '课程类型 1热身 2有氧 3力量 4HIIT 5拉伸 6缓和 7综合',
    level               TINYINT COMMENT '难度等级',
    duration            INT COMMENT '时长(分钟)',
    warm_up_duration    INT COMMENT '热身时长(分钟)',
    cool_down_duration  INT COMMENT '缓和时长(分钟)',
    video_url           VARCHAR(512) COMMENT '视频URL',
    audio_url           VARCHAR(512) COMMENT '音频URL',
    sort_order          INT DEFAULT 0 COMMENT '排序',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag         TINYINT DEFAULT 0 COMMENT '删除标识',
    INDEX idx_plan_id (plan_id),
    INDEX idx_week_day (week, day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练课程表';

-- 用户今日任务表（任务卡片核心表）
CREATE TABLE IF NOT EXISTS t_user_daily_task (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    task_date           DATE NOT NULL COMMENT '任务日期',
    plan_id             BIGINT COMMENT '关联训练计划ID',
    course_id           BIGINT COMMENT '关联课程ID',
    task_type           TINYINT DEFAULT 1 COMMENT '任务类型 1系统计划 2自定义 3AI推荐',
    title               VARCHAR(100) NOT NULL COMMENT '任务标题',
    subtitle            VARCHAR(200) COMMENT '副标题/描述',
    total_exercises     INT DEFAULT 0 COMMENT '总动作数',
    completed_exercises INT DEFAULT 0 COMMENT '已完成动作数',
    total_duration      INT DEFAULT 0 COMMENT '预计总时长(分钟)',
    completed_duration  INT DEFAULT 0 COMMENT '已完成时长(分钟)',
    total_sets          INT DEFAULT 0 COMMENT '总组数',
    completed_sets      INT DEFAULT 0 COMMENT '已完成组数',
    rest_time           INT DEFAULT 60 COMMENT '默认休息时间(秒)',
    status              TINYINT DEFAULT 0 COMMENT '状态 0待开始 1进行中 2已完成 3已跳过',
    progress            INT DEFAULT 0 COMMENT '完成进度 0-100',
    calories_burned     INT DEFAULT 0 COMMENT '已消耗卡路里',
    heart_rate_avg      INT COMMENT '平均心率',
    heart_rate_max      INT COMMENT '最大心率',
    start_time          DATETIME COMMENT '开始时间',
    end_time            DATETIME COMMENT '结束时间',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, task_date),
    INDEX idx_user_id (user_id),
    INDEX idx_task_date (task_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日任务表';

-- 用户任务动作记录表
CREATE TABLE IF NOT EXISTS t_user_task_exercise (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    task_id             BIGINT NOT NULL COMMENT '任务ID',
    exercise_id         BIGINT NOT NULL COMMENT '动作ID',
    exercise_name       VARCHAR(100) COMMENT '动作名称（快照）',
    sets_planned        INT DEFAULT 1 COMMENT '计划组数',
    sets_completed      INT DEFAULT 0 COMMENT '已完成组数',
    reps_planned        INT COMMENT '计划每组次数',
    reps_completed      INT COMMENT '实际完成次数',
    weight_used         DECIMAL(5,2) COMMENT '使用重量(kg)',
    duration_planned    INT COMMENT '计划时长(秒)',
    duration_actual     INT COMMENT '实际用时(秒)',
    rest_time           INT DEFAULT 60 COMMENT '休息时长(秒)',
    status              TINYINT DEFAULT 0 COMMENT '状态 0未完成 1进行中 2已完成',
    calories_burned     INT DEFAULT 0 COMMENT '消耗卡路里',
    heart_rate_avg      INT COMMENT '平均心率',
    heart_rate_max      INT COMMENT '最大心率',
    completed_time      DATETIME COMMENT '完成时间',
    sort_order          INT DEFAULT 0 COMMENT '排序',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_task (user_id, task_id),
    INDEX idx_exercise_id (exercise_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户任务动作记录表';

-- 用户训练计划关联表（记录用户采用/创建的计划进度）
CREATE TABLE IF NOT EXISTS t_user_training_plan (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    plan_id             BIGINT NOT NULL COMMENT '计划ID',
    status              TINYINT DEFAULT 1 COMMENT '状态 0未开始 1进行中 2已完成 3已暂停 4已放弃',
    start_date          DATE COMMENT '开始日期',
    end_date            DATE COMMENT '结束日期',
    current_week        INT DEFAULT 1 COMMENT '当前周',
    current_day         INT DEFAULT 1 COMMENT '当前天',
    completed_courses   INT DEFAULT 0 COMMENT '已完成课程数',
    total_courses       INT DEFAULT 0 COMMENT '总课程数',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_plan (user_id, plan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户训练计划关联表';

-- 任务进度表（任务卡片进度追踪）
CREATE TABLE IF NOT EXISTS t_task_progress (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    task_id             BIGINT NOT NULL COMMENT '任务ID',
    current_progress    INT DEFAULT 0 COMMENT '当前进度',
    target_progress     INT DEFAULT 0 COMMENT '目标进度',
    status              TINYINT DEFAULT 0 COMMENT '状态',
    complete_time       DATETIME COMMENT '完成时间',
    claim_time          DATETIME COMMENT '领取时间',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务进度表';

-- 插入默认管理员账号（账号: admin，密码: admin123）
-- 密码使用 MD5 加密: 0192023a7bbd73250516f069df18b500
INSERT INTO t_user (username, phone, password, nickname, gender, role, status, create_time, update_time)
VALUES ('admin', '13800000000', '0192023a7bbd73250516f069df18b500', 'admin', 0, 2, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE username = 'admin', password = '0192023a7bbd73250516f069df18b500', role = 2;
