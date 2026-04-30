package com.sport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.api.dto.GoalCreateDTO;
import com.sport.api.dto.GoalUpdateDTO;
import com.sport.api.vo.GoalVO;
import com.sport.mapper.WorkoutGoalMapper;
import com.sport.model.entity.WorkoutGoal;
import com.sport.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运动目标服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final WorkoutGoalMapper workoutGoalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGoal(Long userId, GoalCreateDTO dto) {
        WorkoutGoal goal = new WorkoutGoal();
        goal.setUserId(userId);
        goal.setType(dto.getType());
        goal.setTargetValue(dto.getTargetValue());
        goal.setStartDate(dto.getStartDate());
        goal.setEndDate(dto.getEndDate());
        goal.setStatus(1); // 进行中
        goal.setCreateTime(LocalDateTime.now());
        goal.setUpdateTime(LocalDateTime.now());

        workoutGoalMapper.insert(goal);
        log.info("创建目标 - 用户ID: {}, 目标ID: {}", userId, goal.getId());

        return goal.getId();
    }

    @Override
    public List<GoalVO> getGoalList(Long userId, Integer status) {
        LambdaQueryWrapper<WorkoutGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutGoal::getUserId, userId);

        if (status != null) {
            wrapper.eq(WorkoutGoal::getStatus, status);
        }

        wrapper.orderByDesc(WorkoutGoal::getCreateTime);

        List<WorkoutGoal> goals = workoutGoalMapper.selectList(wrapper);
        return goals.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public GoalVO getGoalDetail(Long userId, Long goalId) {
        WorkoutGoal goal = workoutGoalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            return null;
        }
        return convertToVO(goal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoal(Long userId, Long goalId, GoalUpdateDTO dto) {
        WorkoutGoal goal = workoutGoalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            return;
        }

        if (dto.getType() != null) {
            goal.setType(dto.getType());
        }
        if (dto.getTargetValue() != null) {
            goal.setTargetValue(dto.getTargetValue());
        }
        if (dto.getStartDate() != null) {
            goal.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            goal.setEndDate(dto.getEndDate());
        }
        if (dto.getStatus() != null) {
            goal.setStatus(dto.getStatus());
        }
        goal.setUpdateTime(LocalDateTime.now());

        workoutGoalMapper.updateById(goal);
        log.info("更新目标 - 用户ID: {}, 目标ID: {}", userId, goalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoal(Long userId, Long goalId) {
        WorkoutGoal goal = workoutGoalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            return;
        }

        workoutGoalMapper.deleteById(goalId);
        log.info("删除目标 - 用户ID: {}, 目标ID: {}", userId, goalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeGoal(Long userId, Long goalId) {
        WorkoutGoal goal = workoutGoalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            return;
        }

        goal.setStatus(2); // 已完成
        goal.setUpdateTime(LocalDateTime.now());
        workoutGoalMapper.updateById(goal);
        log.info("完成目标 - 用户ID: {}, 目标ID: {}", userId, goalId);
    }

    /**
     * 转换为VO
     */
    private GoalVO convertToVO(WorkoutGoal goal) {
        GoalVO vo = new GoalVO();
        BeanUtils.copyProperties(goal, vo);

        // 设置类型名称
        vo.setTypeName(getGoalTypeName(goal.getType()));

        // 设置状态描述
        vo.setStatusDesc(getGoalStatusName(goal.getStatus()));

        return vo;
    }

    /**
     * 获取目标类型名称
     */
    private String getGoalTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "每日距离";
            case 2: return "每周次数";
            default: return "其他";
        }
    }

    /**
     * 获取目标状态名称
     */
    private String getGoalStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已取消";
            case 1: return "进行中";
            case 2: return "已完成";
            default: return "未知";
        }
    }
}
