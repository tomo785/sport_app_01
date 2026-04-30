package com.sport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.api.dto.TaskReorderDTO;
import com.sport.api.vo.*;
import com.sport.mapper.UserDailyTaskMapper;
import com.sport.mapper.UserTaskExerciseMapper;
import com.sport.mapper.UserDailyStatsMapper;
import com.sport.model.entity.UserDailyStats;
import com.sport.model.entity.UserDailyTask;
import com.sport.model.entity.UserTaskExercise;
import com.sport.model.enums.DailyTaskStatusEnum;
import com.sport.model.enums.DailyTaskTypeEnum;
import com.sport.model.enums.ExerciseStatusEnum;
import com.sport.model.enums.ExerciseTypeEnum;
import com.sport.model.enums.TaskFlowTypeEnum;
import com.sport.model.enums.TaskPriorityEnum;
import com.sport.service.DailyTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskServiceImpl implements DailyTaskService {

    private final UserDailyTaskMapper dailyTaskMapper;
    private final UserTaskExerciseMapper taskExerciseMapper;
    private final UserDailyStatsMapper dailyStatsMapper;

    @Override
    public TodayTaskVO getTodayTask(Long userId) {
        LocalDate today = LocalDate.now();
        UserDailyTask task = dailyTaskMapper.selectByUserIdAndDate(userId, today);
        
        if (task == null) {
            // 如果没有今日任务，创建一个默认的
            createDefaultTodayTask(userId);
            task = dailyTaskMapper.selectByUserIdAndDate(userId, today);
        }
        
        return convertToVO(task);
    }

    @Override
    public TaskDashboardVO getTaskDashboard(Long userId) {
        // 获取今日任务
        TodayTaskVO todayTask = getTodayTask(userId);
        
        // 构建进度信息
        TaskProgressVO progress = TaskProgressVO.builder()
                .totalTasks(todayTask.getTotalExercises())
                .completedTasks(todayTask.getCompletedExercises())
                .remainingTasks(todayTask.getRemainingTasks())
                .progressPercent(todayTask.getProgress())
                .totalDuration(todayTask.getTotalDuration())
                .completedDuration(todayTask.getCompletedDuration())
                .remainingTime(todayTask.getRemainingTime())
                .totalSets(todayTask.getTotalSets())
                .completedSets(todayTask.getCompletedSets())
                .build();
        
        // 构建数据看板
        DataBoardVO dataBoard = DataBoardVO.builder()
                .heartRate(todayTask.getHeartRateAvg() != null ? todayTask.getHeartRateAvg() : 0)
                .caloriesBurned(todayTask.getCaloriesBurned())
                .completionRate(todayTask.getProgress())
                .goalProgress(calculateGoalProgress(userId))
                .targetCalories(500)
                .targetDuration(60)
                .build();
        
        // 获取趋势数据
        List<TrainingTrendVO> weeklyTrend = getWeeklyTrend(userId);
        List<TrainingTrendVO> monthlyTrend = getMonthlyTrend(userId);
        
        return TaskDashboardVO.builder()
                .todayTask(todayTask)
                .progress(progress)
                .dataBoard(dataBoard)
                .weeklyTrend(weeklyTrend)
                .monthlyTrend(monthlyTrend)
                .build();
    }

    @Override
    public List<TrainingTrendVO> getWeeklyTrend(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        
        List<TrainingTrendVO> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        
        // 查询最近7天的统计数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            UserDailyStats stats = dailyStatsMapper.selectByUserIdAndDate(userId, date);
            
            TrainingTrendVO vo = TrainingTrendVO.builder()
                    .date(date)
                    .dateLabel(date.format(formatter))
                    .duration(stats != null ? stats.getTotalDuration() / 60 : 0)
                    .calories(stats != null ? stats.getTotalCalories() : 0)
                    .exerciseCount(stats != null ? stats.getRecordCount() : 0)
                    .goalAchieved(stats != null && stats.getGoalProgress() != null && stats.getGoalProgress() >= 100)
                    .build();
            trend.add(vo);
        }
        
        return trend;
    }

    @Override
    public List<TrainingTrendVO> getMonthlyTrend(Long userId) {
        LocalDate today = LocalDate.now();
        List<TrainingTrendVO> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        
        // 查询最近30天的统计数据（每3天一个点）
        for (int i = 27; i >= 0; i -= 3) {
            LocalDate date = today.minusDays(i);
            UserDailyStats stats = dailyStatsMapper.selectByUserIdAndDate(userId, date);
            
            TrainingTrendVO vo = TrainingTrendVO.builder()
                    .date(date)
                    .dateLabel(date.format(formatter))
                    .duration(stats != null ? stats.getTotalDuration() / 60 : 0)
                    .calories(stats != null ? stats.getTotalCalories() : 0)
                    .exerciseCount(stats != null ? stats.getRecordCount() : 0)
                    .goalAchieved(stats != null && stats.getGoalProgress() != null && stats.getGoalProgress() >= 100)
                    .build();
            trend.add(vo);
        }
        
        return trend;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTodayTask(Long userId) {
        LocalDate today = LocalDate.now();
        UserDailyTask task = dailyTaskMapper.selectByUserIdAndDate(userId, today);
        
        if (task == null) {
            createTodayTask(userId);
            task = dailyTaskMapper.selectByUserIdAndDate(userId, today);
        }
        
        if (task.getStatus() == DailyTaskStatusEnum.PENDING.getCode()) {
            task.setStatus(DailyTaskStatusEnum.IN_PROGRESS.getCode());
            task.setStartTime(java.time.LocalDateTime.now());
            dailyTaskMapper.updateById(task);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeExercise(Long userId, Long taskExerciseId, Integer actualReps, Integer actualDuration) {
        UserTaskExercise exercise = taskExerciseMapper.selectById(taskExerciseId);
        if (exercise == null || !exercise.getUserId().equals(userId)) {
            return;
        }
        
        exercise.setSetsCompleted(exercise.getSetsCompleted() + 1);
        exercise.setRepsCompleted(actualReps);
        exercise.setDurationActual(actualDuration);
        
        // 如果已完成所有组数
        if (exercise.getSetsCompleted() >= exercise.getSetsPlanned()) {
            exercise.setStatus(ExerciseStatusEnum.COMPLETED.getCode());
            exercise.setCompletedTime(java.time.LocalDateTime.now());
        } else {
            exercise.setStatus(ExerciseStatusEnum.IN_PROGRESS.getCode());
        }
        
        taskExerciseMapper.updateById(exercise);
        
        // 更新任务进度
        updateTaskProgress(userId, exercise.getTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskProgress(Long userId, Long taskId) {
        UserDailyTask task = dailyTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            return;
        }
        
        // 获取所有动作
        List<UserTaskExercise> exercises = taskExerciseMapper.selectByTaskId(taskId);
        
        int completedExercises = 0;
        int completedSets = 0;
        int completedDuration = 0;
        int caloriesBurned = 0;
        
        for (UserTaskExercise exercise : exercises) {
            if (exercise.getStatus() == ExerciseStatusEnum.COMPLETED.getCode()) {
                completedExercises++;
            }
            completedSets += exercise.getSetsCompleted();
            completedDuration += exercise.getDurationActual() != null ? exercise.getDurationActual() : 0;
            caloriesBurned += exercise.getCaloriesBurned() != null ? exercise.getCaloriesBurned() : 0;
        }
        
        task.setCompletedExercises(completedExercises);
        task.setCompletedSets(completedSets);
        task.setCompletedDuration(completedDuration / 60);
        task.setCaloriesBurned(caloriesBurned);
        
        // 计算进度百分比
        if (task.getTotalExercises() > 0) {
            int progress = (completedExercises * 100) / task.getTotalExercises();
            task.setProgress(progress);
        }
        
        // 检查是否全部完成
        if (completedExercises >= task.getTotalExercises()) {
            task.setStatus(DailyTaskStatusEnum.COMPLETED.getCode());
            task.setEndTime(java.time.LocalDateTime.now());
        }
        
        dailyTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTodayTask(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 检查是否已存在
        UserDailyTask existingTask = dailyTaskMapper.selectByUserIdAndDate(userId, today);
        if (existingTask != null) {
            return;
        }
        
        createDefaultTodayTask(userId);
    }

    /**
     * 创建默认今日任务
     */
    private void createDefaultTodayTask(Long userId) {
        LocalDate today = LocalDate.now();
        
        UserDailyTask task = new UserDailyTask();
        task.setUserId(userId);
        task.setTaskDate(today);
        task.setTaskType(DailyTaskTypeEnum.SYSTEM_PLAN.getCode());
        task.setTitle("上肢力量训练");
        task.setSubtitle("强化胸肌与三头肌");
        task.setTotalExercises(3);
        task.setCompletedExercises(0);
        task.setTotalDuration(45);
        task.setCompletedDuration(0);
        task.setTotalSets(10);
        task.setCompletedSets(0);
        task.setRestTime(60);
        task.setStatus(DailyTaskStatusEnum.PENDING.getCode());
        task.setProgress(0);
        task.setCaloriesBurned(0);
        
        dailyTaskMapper.insert(task);
        
        // 创建默认动作
        createDefaultExercises(task.getId(), userId);
    }

    /**
     * 创建默认动作
     */
    private void createDefaultExercises(Long taskId, Long userId) {
        // 俯卧撑
        UserTaskExercise exercise1 = new UserTaskExercise();
        exercise1.setUserId(userId);
        exercise1.setTaskId(taskId);
        exercise1.setExerciseId(1L);
        exercise1.setExerciseName("俯卧撑");
        exercise1.setSetsPlanned(3);
        exercise1.setSetsCompleted(0);
        exercise1.setRepsPlanned(15);
        exercise1.setRestTime(60);
        exercise1.setStatus(ExerciseStatusEnum.PENDING.getCode());
        exercise1.setSortOrder(1);
        exercise1.setPriority(TaskPriorityEnum.MEDIUM.getCode());
        exercise1.setTaskFlowType(TaskFlowTypeEnum.STRENGTH.getCode());
        exercise1.setIcon(TaskFlowTypeEnum.STRENGTH.getIcon());
        taskExerciseMapper.insert(exercise1);
        
        // 哑铃卧推
        UserTaskExercise exercise2 = new UserTaskExercise();
        exercise2.setUserId(userId);
        exercise2.setTaskId(taskId);
        exercise2.setExerciseId(2L);
        exercise2.setExerciseName("哑铃卧推");
        exercise2.setSetsPlanned(4);
        exercise2.setSetsCompleted(0);
        exercise2.setRepsPlanned(12);
        exercise2.setRestTime(60);
        exercise2.setStatus(ExerciseStatusEnum.PENDING.getCode());
        exercise2.setSortOrder(2);
        exercise2.setPriority(TaskPriorityEnum.HIGH.getCode());
        exercise2.setTaskFlowType(TaskFlowTypeEnum.STRENGTH.getCode());
        exercise2.setIcon(TaskFlowTypeEnum.STRENGTH.getIcon());
        taskExerciseMapper.insert(exercise2);
        
        // 平板支撑
        UserTaskExercise exercise3 = new UserTaskExercise();
        exercise3.setUserId(userId);
        exercise3.setTaskId(taskId);
        exercise3.setExerciseId(3L);
        exercise3.setExerciseName("平板支撑");
        exercise3.setSetsPlanned(3);
        exercise3.setSetsCompleted(0);
        exercise3.setDurationPlanned(60);
        exercise3.setRestTime(60);
        exercise3.setStatus(ExerciseStatusEnum.PENDING.getCode());
        exercise3.setSortOrder(3);
        exercise3.setPriority(TaskPriorityEnum.MEDIUM.getCode());
        exercise3.setTaskFlowType(TaskFlowTypeEnum.CORE.getCode());
        exercise3.setIcon(TaskFlowTypeEnum.CORE.getIcon());
        taskExerciseMapper.insert(exercise3);
    }

    /**
     * 转换为VO
     */
    private TodayTaskVO convertToVO(UserDailyTask task) {
        TodayTaskVO vo = new TodayTaskVO();
        BeanUtils.copyProperties(task, vo);
        
        // 设置描述
        DailyTaskStatusEnum statusEnum = DailyTaskStatusEnum.fromCode(task.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
        
        DailyTaskTypeEnum typeEnum = DailyTaskTypeEnum.fromCode(task.getTaskType());
        vo.setTaskTypeDesc(typeEnum != null ? typeEnum.getDesc() : "未知");
        
        // 计算剩余任务数和时间
        int remainingTasks = task.getTotalExercises() - task.getCompletedExercises();
        vo.setRemainingTasks(Math.max(remainingTasks, 0));
        
        int remainingTime = task.getTotalDuration() - task.getCompletedDuration();
        vo.setRemainingTime(Math.max(remainingTime, 0));
        
        // 获取动作列表
        List<UserTaskExercise> exercises = taskExerciseMapper.selectByTaskId(task.getId());
        List<TaskExerciseVO> exerciseVOs = exercises.stream().map(this::convertExerciseToVO).collect(Collectors.toList());
        vo.setExercises(exerciseVOs);
        
        return vo;
    }

    /**
     * 转换动作记录为VO
     */
    private TaskExerciseVO convertExerciseToVO(UserTaskExercise exercise) {
        TaskExerciseVO vo = new TaskExerciseVO();
        BeanUtils.copyProperties(exercise, vo);
        
        ExerciseStatusEnum statusEnum = ExerciseStatusEnum.fromCode(exercise.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
        
        // 默认为力量训练
        vo.setType(2);
        vo.setTypeDesc("力量");
        
        return vo;
    }

    /**
     * 计算目标完成进度
     */
    private Integer calculateGoalProgress(Long userId) {
        LocalDate today = LocalDate.now();
        UserDailyStats stats = dailyStatsMapper.selectByUserIdAndDate(userId, today);
        
        if (stats == null || stats.getGoalProgress() == null) {
            return 0;
        }
        return stats.getGoalProgress();
    }

    // ==================== 任务流程图方法 ====================

    @Override
    public TaskFlowVO getTaskFlow(Long userId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        // 查询每日任务
        UserDailyTask task = dailyTaskMapper.selectByUserIdAndDate(userId, date);
        
        if (task == null) {
            // 如果不存在则创建默认任务
            createDefaultTodayTask(userId);
            task = dailyTaskMapper.selectByUserIdAndDate(userId, date);
        }
        
        // 构建目标信息
        TaskFlowGoalVO goal = new TaskFlowGoalVO();
        goal.setId(task.getId());
        goal.setTitle(task.getTitle());
        goal.setDescription(task.getSubtitle());
        goal.setTargetProgress(100);
        goal.setCurrentProgress(task.getProgress() != null ? task.getProgress() : 0);
        
        // 查询动作列表
        List<UserTaskExercise> exercises = taskExerciseMapper.selectByTaskId(task.getId());
        
        List<TaskFlowItemVO> taskItems = exercises.stream()
                .map(this::convertToTaskFlowItem)
                .collect(Collectors.toList());
        
        // 计算统计数据
        int totalTasks = taskItems.size();
        int completedTasks = (int) taskItems.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 2)
                .count();
        int totalProgress = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
        
        TaskFlowVO flowVO = new TaskFlowVO();
        flowVO.setGoal(goal);
        flowVO.setTasks(taskItems);
        flowVO.setTotalTasks(totalTasks);
        flowVO.setCompletedTasks(completedTasks);
        flowVO.setTotalProgress(totalProgress);
        
        return flowVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderTaskFlow(Long userId, List<TaskReorderDTO> orderData) {
        if (orderData == null || orderData.isEmpty()) {
            return;
        }
        
        for (TaskReorderDTO dto : orderData) {
            if (dto.getTaskId() == null || dto.getOrder() == null) {
                continue;
            }
            
            UserTaskExercise exercise = taskExerciseMapper.selectById(dto.getTaskId());
            if (exercise == null || !exercise.getUserId().equals(userId)) {
                continue;
            }
            
            exercise.setSortOrder(dto.getOrder());
            taskExerciseMapper.updateById(exercise);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTaskFlowItem(Long userId, Long taskExerciseId) {
        UserTaskExercise exercise = taskExerciseMapper.selectById(taskExerciseId);
        if (exercise == null || !exercise.getUserId().equals(userId)) {
            return;
        }
        
        // 将动作设为进行中
        exercise.setStatus(ExerciseStatusEnum.IN_PROGRESS.getCode());
        taskExerciseMapper.updateById(exercise);
        
        // 如果父任务也是待开始，将其设为进行中
        UserDailyTask task = dailyTaskMapper.selectById(exercise.getTaskId());
        if (task != null && task.getUserId().equals(userId) 
                && task.getStatus() == DailyTaskStatusEnum.PENDING.getCode()) {
            task.setStatus(DailyTaskStatusEnum.IN_PROGRESS.getCode());
            task.setStartTime(java.time.LocalDateTime.now());
            dailyTaskMapper.updateById(task);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskPriority(Long userId, Long taskExerciseId, String priority) {
        UserTaskExercise exercise = taskExerciseMapper.selectById(taskExerciseId);
        if (exercise == null || !exercise.getUserId().equals(userId)) {
            return;
        }
        
        TaskPriorityEnum priorityEnum = TaskPriorityEnum.fromCode(priority);
        exercise.setPriority(priorityEnum.getCode());
        taskExerciseMapper.updateById(exercise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateTaskStatus(Long userId, List<Long> taskExerciseIds, Integer status) {
        if (taskExerciseIds == null || taskExerciseIds.isEmpty() || status == null) {
            return;
        }
        
        for (Long taskExerciseId : taskExerciseIds) {
            UserTaskExercise exercise = taskExerciseMapper.selectById(taskExerciseId);
            if (exercise == null || !exercise.getUserId().equals(userId)) {
                continue;
            }
            
            exercise.setStatus(status);
            if (status == ExerciseStatusEnum.COMPLETED.getCode()) {
                exercise.setCompletedTime(java.time.LocalDateTime.now());
                // 如果完成，默认设置已完成组数等于计划组数
                if (exercise.getSetsCompleted() == null || exercise.getSetsCompleted() < exercise.getSetsPlanned()) {
                    exercise.setSetsCompleted(exercise.getSetsPlanned());
                }
            }
            taskExerciseMapper.updateById(exercise);
            
            // 更新父任务进度
            updateTaskProgress(userId, exercise.getTaskId());
        }
    }

    /**
     * 转换为任务流程图单项VO
     */
    private TaskFlowItemVO convertToTaskFlowItem(UserTaskExercise exercise) {
        TaskFlowItemVO vo = new TaskFlowItemVO();
        vo.setId(exercise.getId());
        vo.setName(exercise.getExerciseName());
        
        // 类型和图标
        String flowType = exercise.getTaskFlowType();
        if (flowType == null || flowType.isEmpty()) {
            // 根据原有ExerciseTypeEnum转换
            flowType = TaskFlowTypeEnum.STRENGTH.getCode();
        }
        vo.setType(flowType);
        vo.setIcon(exercise.getIcon() != null ? exercise.getIcon() : TaskFlowTypeEnum.getIconByCode(flowType));
        
        // 优先级
        String priority = exercise.getPriority();
        vo.setPriority(priority != null ? priority : TaskPriorityEnum.MEDIUM.getCode());
        
        // 状态
        vo.setStatus(exercise.getStatus() != null ? exercise.getStatus() : 0);
        
        // 时长（秒转分钟）
        if (exercise.getDurationPlanned() != null && exercise.getDurationPlanned() > 0) {
            vo.setDuration(exercise.getDurationPlanned() / 60);
        } else {
            // 默认每组动作估算时长
            vo.setDuration(10);
        }
        
        // 组数
        vo.setSets(exercise.getSetsPlanned() != null ? exercise.getSetsPlanned() : 1);
        
        // 卡路里（预计）
        if (exercise.getCaloriesBurned() != null && exercise.getCaloriesBurned() > 0) {
            vo.setCalories(exercise.getCaloriesBurned());
        } else {
            // 简单估算：每分钟约8千卡
            vo.setCalories(vo.getDuration() * 8);
        }
        
        // 顺序
        vo.setOrder(exercise.getSortOrder() != null ? exercise.getSortOrder() : 0);
        
        return vo;
    }
}
