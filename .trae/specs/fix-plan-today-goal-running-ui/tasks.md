# Tasks

- [x] Task 1: 修复定制页面单条计划删除按钮
  - [x] SubTask 1.1: 在 `editor.vue` 的 `plan-row` 中为每条已创建计划添加删除按钮
  - [x] SubTask 1.2: 确保点击删除调用已有的 `removePlan(dayOfWeek)` 方法
  - [x] SubTask 1.3: 删除后即时更新列表展示

- [x] Task 2: 为主页今日目标加载添加日志
  - [x] SubTask 2.1: 在 `index.vue` 的 `loadTodayPlan()` 方法入口添加日志
  - [x] SubTask 2.2: 在读取 `weeklyPlan_current` 后添加日志输出内容摘要
  - [x] SubTask 2.3: 在匹配到今日计划时输出 plan 信息
  - [x] SubTask 2.4: 在未匹配到任何计划的分支输出原因日志

- [x] Task 3: 修复跑步页面暂停按钮文字不可见
  - [x] SubTask 3.1: 调整 `running.vue` 中 `.panel-btn-label` 的颜色，使其在深色背景上可见
  - [x] SubTask 3.2: 同步检查室内运动面板（`.indoor-panel`）的按钮 label 颜色是否也需要调整

# Task Dependencies
- 各 Task 之间无依赖，可并行执行
