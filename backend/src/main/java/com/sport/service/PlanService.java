package com.sport.service;

import com.sport.api.dto.PlanCreateDTO;
import com.sport.api.dto.PlanSearchDTO;
import com.sport.api.dto.PlanUpdateDTO;
import com.sport.api.vo.PlanDetailVO;
import com.sport.api.vo.PlanVO;

import java.util.List;

/**
 * 训练计划服务接口
 */
public interface PlanService {

    /**
     * 创建计划
     */
    Long createPlan(Long userId, PlanCreateDTO dto);

    /**
     * 获取我的计划列表
     */
    List<PlanVO> getMyPlans(Long userId, Integer status);

    /**
     * 获取社区计划列表
     */
    List<PlanVO> getCommunityPlans(Long userId, PlanSearchDTO searchDTO);

    /**
     * 智能匹配计划
     */
    List<PlanVO> matchPlans(Long userId, PlanSearchDTO searchDTO);

    /**
     * 获取计划详情
     */
    PlanDetailVO getPlanDetail(Long userId, Long planId);

    /**
     * 更新计划
     */
    void updatePlan(Long userId, Long planId, PlanUpdateDTO dto);

    /**
     * 删除计划
     */
    void deletePlan(Long userId, Long planId);

    /**
     * 上传计划到社区
     */
    void uploadToCommunity(Long userId, Long planId);

    /**
     * 采用社区计划
     */
    void adoptPlan(Long userId, Long planId);
}
