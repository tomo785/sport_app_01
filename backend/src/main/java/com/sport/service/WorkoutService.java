package com.sport.service;

import com.sport.api.vo.WorkoutDetailVO;
import com.sport.api.vo.WorkoutListVO;

import java.util.List;
import java.util.Map;

/**
 * 运动记录服务接口
 */
public interface WorkoutService {

    /**
     * 开始运动
     */
    Long startWorkout(Long userId, Integer type);

    /**
     * 暂停运动
     */
    void pauseWorkout(Long userId, Long recordId);

    /**
     * 继续运动
     */
    void resumeWorkout(Long userId, Long recordId);

    /**
     * 结束运动
     */
    WorkoutDetailVO finishWorkout(Long userId, Long recordId, Map<String, Object> metrics);

    /**
     * 获取运动详情
     */
    WorkoutDetailVO getWorkoutDetail(Long userId, Long recordId);

    /**
     * 获取运动列表
     */
    WorkoutListVO getWorkoutList(Long userId, Integer page, Integer size, Integer type, String startDate, String endDate);

    /**
     * 上传轨迹点
     */
    void uploadTrajectory(Long userId, Long recordId, List<Map<String, Object>> trajectory);

    /**
     * 获取运动轨迹
     */
    List<Map<String, Object>> getWorkoutTrajectory(Long userId, Long recordId);
}
