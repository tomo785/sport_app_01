package com.sport.api.controller;

import com.sport.api.vo.WorkoutDetailVO;
import com.sport.api.vo.WorkoutListVO;
import com.sport.common.util.Result;
import com.sport.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动记录控制器
 */
@Tag(name = "运动记录", description = "运动开始、结束、轨迹上传、记录查询")
@RestController
@RequestMapping("/workout")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final com.sport.common.util.JwtUtil jwtUtil;

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

    @Operation(summary = "开始运动")
    @PostMapping("/start")
    public Result<Map<String, Object>> startWorkout(
            @Parameter(description = "运动类型 1跑步 2健走 3骑行") @RequestParam Integer type,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        Long recordId = workoutService.startWorkout(userId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", recordId);
        return Result.success(result);
    }

    @Operation(summary = "暂停运动")
    @PutMapping("/pause")
    public Result<Void> pauseWorkout(
            @Parameter(description = "运动记录ID") @RequestParam Long recordId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        workoutService.pauseWorkout(userId, recordId);
        return Result.success();
    }

    @Operation(summary = "继续运动")
    @PutMapping("/resume")
    public Result<Void> resumeWorkout(
            @Parameter(description = "运动记录ID") @RequestParam Long recordId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        workoutService.resumeWorkout(userId, recordId);
        return Result.success();
    }

    @Operation(summary = "结束运动")
    @PutMapping("/finish")
    public Result<WorkoutDetailVO> finishWorkout(
            @Parameter(description = "运动记录ID") @RequestParam Long recordId,
            @RequestBody(required = false) Map<String, Object> metrics,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        WorkoutDetailVO vo = workoutService.finishWorkout(userId, recordId, metrics);
        return Result.success(vo);
    }

    @Operation(summary = "获取运动详情")
    @GetMapping("/{id}")
    public Result<WorkoutDetailVO> getWorkoutDetail(
            @Parameter(description = "运动记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        WorkoutDetailVO vo = workoutService.getWorkoutDetail(userId, id);
        return Result.success(vo);
    }

    @Operation(summary = "获取运动列表")
    @GetMapping("/list")
    public Result<WorkoutListVO> getWorkoutList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "运动类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        WorkoutListVO vo = workoutService.getWorkoutList(userId, page, size, type, startDate, endDate);
        return Result.success(vo);
    }

    @Operation(summary = "上传轨迹点")
    @PostMapping("/trajectory")
    public Result<Void> uploadTrajectory(
            @RequestBody Map<String, Object> data,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        Long recordId = Long.valueOf(data.get("recordId").toString());
        List<Map<String, Object>> trajectory = (List<Map<String, Object>>) data.get("trajectory");

        workoutService.uploadTrajectory(userId, recordId, trajectory);
        return Result.success();
    }

    @Operation(summary = "获取运动轨迹")
    @GetMapping("/{id}/trajectory")
    public Result<List<Map<String, Object>>> getWorkoutTrajectory(
            @Parameter(description = "运动记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        List<Map<String, Object>> trajectory = workoutService.getWorkoutTrajectory(userId, id);
        return Result.success(trajectory);
    }
}
