package com.sport.service;

import com.sport.api.dto.TaskReorderDTO;
import com.sport.api.vo.TaskDashboardVO;
import com.sport.api.vo.TaskFlowVO;
import com.sport.api.vo.TodayTaskVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日任务服务接口
 */
public interface DailyTaskService {

    /**
     * 获取今日任务
     */
    TodayTaskVO getTodayTask(Long userId);

    /**
     * 获取任务看板数据（上区块完整数据）
     */
    TaskDashboardVO getTaskDashboard(Long userId);

    /**
     * 获取7天训练趋势
     */
    List<com.sport.api.vo.TrainingTrendVO> getWeeklyTrend(Long userId);

    /**
     * 获取30天训练趋势
     */
    List<com.sport.api.vo.TrainingTrendVO> getMonthlyTrend(Long userId);

    /**
     * 开始今日任务
     */
    void startTodayTask(Long userId);

    /**
     * 完成动作
     */
    void completeExercise(Long userId, Long taskExerciseId, Integer actualReps, Integer actualDuration);

    /**
     * 更新任务进度
     */
    void updateTaskProgress(Long userId, Long taskId);

    /**
     * 创建今日任务（如果不存在）
     */
    void createTodayTask(Long userId);

    /**
     * 获取任务流程图数据
     */
    TaskFlowVO getTaskFlow(Long userId, LocalDate date);

    /**
     * 更新任务顺序
     */
    void reorderTaskFlow(Long userId, List<TaskReorderDTO> orderData);

    /**
     * 开始指定任务（动作）
     */
    void startTaskFlowItem(Long userId, Long taskExerciseId);

    /**
     * 更新任务优先级
     */
    void updateTaskPriority(Long userId, Long taskExerciseId, String priority);

    /**
     * 批量更新任务状态
     */
    void batchUpdateTaskStatus(Long userId, List<Long> taskExerciseIds, Integer status);
}
