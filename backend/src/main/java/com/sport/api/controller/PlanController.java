package com.sport.api.controller;

import com.sport.api.dto.PlanCreateDTO;
import com.sport.api.dto.PlanSearchDTO;
import com.sport.api.dto.PlanUpdateDTO;
import com.sport.api.vo.PlanDetailVO;
import com.sport.api.vo.PlanVO;
import com.sport.common.constant.ResultCode;
import com.sport.common.util.JwtUtil;
import com.sport.common.util.Result;
import com.sport.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练计划控制器
 */
@Tag(name = "训练计划", description = "计划创建、查询、更新、删除、社区共享、智能匹配")
@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    @Operation(summary = "创建训练计划")
    @PostMapping
    public Result<Map<String, Object>> createPlan(
            @Valid @RequestBody PlanCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        Long planId = planService.createPlan(userId, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("planId", planId);
        return Result.success(result);
    }

    @Operation(summary = "获取我的计划列表")
    @GetMapping("/my")
    public Result<List<PlanVO>> getMyPlans(
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        List<PlanVO> list = planService.getMyPlans(userId, status);
        return Result.success(list);
    }

    @Operation(summary = "获取社区计划列表")
    @GetMapping("/community")
    public Result<List<PlanVO>> getCommunityPlans(
            @Parameter(description = "搜索条件") PlanSearchDTO searchDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        List<PlanVO> list = planService.getCommunityPlans(userId, searchDTO);
        return Result.success(list);
    }

    @Operation(summary = "智能匹配计划")
    @PostMapping("/match")
    public Result<List<PlanVO>> matchPlans(
            @RequestBody PlanSearchDTO searchDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        List<PlanVO> list = planService.matchPlans(userId, searchDTO);
        return Result.success(list);
    }

    @Operation(summary = "获取计划详情")
    @GetMapping("/{id}")
    public Result<PlanDetailVO> getPlanDetail(
            @Parameter(description = "计划ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        PlanDetailVO vo = planService.getPlanDetail(userId, id);
        if (vo == null) {
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "更新计划")
    @PutMapping("/{id}")
    public Result<Void> updatePlan(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @RequestBody PlanUpdateDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        planService.updatePlan(userId, id, dto);
        return Result.success();
    }

    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(
            @Parameter(description = "计划ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        planService.deletePlan(userId, id);
        return Result.success();
    }

    @Operation(summary = "上传计划到社区")
    @PostMapping("/{id}/upload")
    public Result<Void> uploadToCommunity(
            @Parameter(description = "计划ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        planService.uploadToCommunity(userId, id);
        return Result.success("计划已上传至社区");
    }

    @Operation(summary = "采用社区计划")
    @PostMapping("/{id}/adopt")
    public Result<Void> adoptPlan(
            @Parameter(description = "计划ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        planService.adoptPlan(userId, id);
        return Result.success("已采用该计划");
    }
}
