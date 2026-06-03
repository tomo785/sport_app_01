# 回退 create.vue 创建计划流程到改造前

## 摘要
用户认为之前的抽屉组件交互更好，决定将 `create.vue` 全部回退到优化改造之前的状态。索引页 `index.vue` 的改动（今日目标活动卡片改造）保留不动。

## 当前状态分析

`create.vue` 在上一轮优化中做了以下改动：
1. **步骤条**：从数字圆点改为横线进度条
2. **Step 2 每周安排**：纵向卡片 → 横向周历 + 内嵌展开面板
3. **Step 3 活动定制**：折叠面板 → 始终展开

## 改造方案

**唯一操作**：将 `d:\Acode\frontend\src\pages\goal\create.vue` 恢复到 git 仓库中的原始版本。

```bash
git checkout -- d:\Acode\frontend\src\pages\goal\create.vue
```

这会恢复：
- 原始数字圆点步骤条
- 原始纵向日期卡片 + 底部抽屉弹窗
- 原始折叠面板活动定制

## 不影响的内容

- `d:\Acode\frontend\src\pages\index\index.vue` 的今日目标活动卡片改造保持不变
- `d:\Acode\frontend\src\components\AiPlanDialog.vue` 的修改保持不变
- 其他所有文件不变

## 验证步骤

1. 打开创建计划页面，确认步骤条恢复为数字圆点样式
2. Step 2 点击日期弹出底部抽屉
3. Step 3 点击课程展开/折叠步骤编辑
