# 修复定制页面删除、主页今日目标、跑步暂停按钮 Spec

## Why
用户反馈三个问题：
1. 定制计划页面（editor.vue）的"本周已创建"列表中，单条计划缺少删除功能（只有"清空本周"）。
2. 主页（index.vue）的"今日目标"卡片一直不显示，需要排查并添加日志辅助定位。
3. 跑步页面（running.vue）运动中底部暂停按钮的文字在深色背景下看不清。

## What Changes
- 在 `editor.vue` 的 plan-row 区域增加单条计划删除入口（已有 `removePlan` 方法，但缺少触发按钮）。
- 在 `index.vue` 的 `loadTodayPlan()` 方法中增加关键路径日志输出，帮助排查今日目标不显示的问题。
- 修复 `running.vue` 中暂停/继续按钮的 label 文字颜色，使其在深色面板上可见。

## Impact
-  affected specs: 定制页面交互、主页数据展示、跑步页面UI
-  affected code:
  - `frontend/src/pages/plan/editor.vue`
  - `frontend/src/pages/index/index.vue`
  - `frontend/src/pages/running/running.vue`

## ADDED Requirements
### Requirement: 单条计划删除
The system SHALL 在 editor.vue 的"本周已创建"列表中，为每条计划提供删除按钮。

#### Scenario: 用户删除单条计划
- **WHEN** 用户在定制页面看到"本周已创建"列表
- **THEN** 每条计划右侧显示"删除"按钮
- **WHEN** 用户点击"删除"
- **THEN** 调用 `removePlan(dayOfWeek)` 从 `weeklyPlan.days` 中移除该日计划

### Requirement: 今日目标加载日志
The system SHALL 在 `loadTodayPlan()` 的关键路径输出日志，便于排查今日目标不显示问题。

#### Scenario: 加载今日计划时输出日志
- **WHEN** 主页加载今日目标数据
- **THEN** 在以下节点输出 `console.log`：
  - 进入 `loadTodayPlan()` 时
  - 读取到 `weeklyPlan_current` 时（输出内容摘要）
  - 匹配到今日计划时（输出 plan 信息）
  - 未匹配到任何计划时（输出原因）
  - 发生异常时（保持现有 error 日志）

## MODIFIED Requirements
### Requirement: 跑步暂停按钮文字可见
The system SHALL 确保跑步页面运动中底部控制面板的按钮 label 在深色背景上清晰可读。

#### Scenario: 运动中查看暂停按钮
- **WHEN** 用户进入运动中状态（phase === 'active'）
- **THEN** 底部面板按钮 label（暂停/继续/结束/下一项）文字颜色与背景有足够对比度
- **当前问题**: `.panel-btn-label` 使用 `rgba(255,255,255,0.5)`，在深色面板上可能过淡
- **修复方式**: 提升 label 文字亮度或调整颜色
