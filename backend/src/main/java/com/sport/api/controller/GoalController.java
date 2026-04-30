package com.sport.api.controller;

import com.sport.api.dto.GoalCreateDTO;
import com.sport.api.dto.GoalUpdateDTO;
import com.sport.api.vo.GoalVO;
import com.sport.common.util.JwtUtil;
import com.sport.common.util.Result;
import com.sport.service.GoalService;
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
 * 运动目标控制器
 */
@Tag(name = "运动目标", description = "目标创建、查询、更新、删除")
@RestController
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final JwtUtil jwtUtil;

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    @Operation(summary = "创建目标")
    @PostMapping
    public Result<Map<String, Object>> createGoal(
            @Valid @RequestBody GoalCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        Long goalId = goalService.createGoal(userId, dto);
        Map<String, Object> result = new HashMap<>();
        result.put("goalId", goalId);
        return Result.success(result);
    }

    @Operation(summary = "获取目标列表")
    @GetMapping
    public Result<List<GoalVO>> getGoalList(
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        List<GoalVO> list = goalService.getGoalList(userId, status);
        return Result.success(list);
    }

    @Operation(summary = "获取目标详情")
    @GetMapping("/{id}")
    public Result<GoalVO> getGoalDetail(
            @Parameter(description = "目标ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        GoalVO vo = goalService.getGoalDetail(userId, id);
        if (vo == null) {
            return Result.fail(com.sport.common.constant.ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "更新目标")
    @PutMapping("/{id}")
    public Result<Void> updateGoal(
            @Parameter(description = "目标ID") @PathVariable Long id,
            @RequestBody GoalUpdateDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        goalService.updateGoal(userId, id, dto);
        return Result.success();
    }

    @Operation(summary = "删除目标")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoal(
            @Parameter(description = "目标ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        goalService.deleteGoal(userId, id);
        return Result.success();
    }

    @Operation(summary = "完成目标")
    @PutMapping("/{id}/complete")
    public Result<Void> completeGoal(
            @Parameter(description = "目标ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        goalService.completeGoal(userId, id);
        return Result.success();
    }
}
