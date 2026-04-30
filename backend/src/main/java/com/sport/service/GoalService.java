package com.sport.service;

import com.sport.api.dto.GoalCreateDTO;
import com.sport.api.dto.GoalUpdateDTO;
import com.sport.api.vo.GoalVO;

import java.util.List;

/**
 * 运动目标服务接口
 */
public interface GoalService {

    /**
     * 创建目标
     *
     * @param userId 用户ID
     * @param dto    创建DTO
     * @return 目标ID
     */
    Long createGoal(Long userId, GoalCreateDTO dto);

    /**
     * 获取用户目标列表
     *
     * @param userId 用户ID
     * @param status 状态筛选
     * @return 目标列表
     */
    List<GoalVO> getGoalList(Long userId, Integer status);

    /**
     * 获取目标详情
     *
     * @param userId 用户ID
     * @param goalId 目标ID
     * @return 目标详情
     */
    GoalVO getGoalDetail(Long userId, Long goalId);

    /**
     * 更新目标
     *
     * @param userId 用户ID
     * @param goalId 目标ID
     * @param dto    更新DTO
     */
    void updateGoal(Long userId, Long goalId, GoalUpdateDTO dto);

    /**
     * 删除目标
     *
     * @param userId 用户ID
     * @param goalId 目标ID
     */
    void deleteGoal(Long userId, Long goalId);

    /**
     * 完成目标
     *
     * @param userId 用户ID
     * @param goalId 目标ID
     */
    void completeGoal(Long userId, Long goalId);
}
