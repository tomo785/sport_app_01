package com.sport.api.controller;

import com.sport.api.dto.BatchUpdateStatusDTO;
import com.sport.api.dto.TaskReorderDTO;
import com.sport.api.vo.TaskDashboardVO;
import com.sport.api.vo.TaskFlowVO;
import com.sport.api.vo.TodayTaskVO;
import com.sport.api.vo.TrainingTrendVO;
import com.sport.common.util.Result;
import com.sport.service.DailyTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日任务控制器（任务卡片上区块）
 */
@Tag(name = "任务卡片", description = "每日训练计划、进度、数据看板")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class DailyTaskController {

    private final DailyTaskService dailyTaskService;
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

    @Operation(summary = "获取今日任务", description = "获取用户今日的训练计划任务")
    @GetMapping("/today")
    public Result<TodayTaskVO> getTodayTask(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        TodayTaskVO vo = dailyTaskService.getTodayTask(userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取任务看板", description = "获取上区块完整数据：今日任务、进度、数据看板、训练趋势")
    @GetMapping("/dashboard")
    public Result<TaskDashboardVO> getTaskDashboard(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        TaskDashboardVO vo = dailyTaskService.getTaskDashboard(userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取7天训练趋势")
    @GetMapping("/trend/weekly")
    public Result<List<TrainingTrendVO>> getWeeklyTrend(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        List<TrainingTrendVO> list = dailyTaskService.getWeeklyTrend(userId);
        return Result.success(list);
    }

    @Operation(summary = "获取30天训练趋势")
    @GetMapping("/trend/monthly")
    public Result<List<TrainingTrendVO>> getMonthlyTrend(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        List<TrainingTrendVO> list = dailyTaskService.getMonthlyTrend(userId);
        return Result.success(list);
    }

    @Operation(summary = "开始今日任务")
    @PostMapping("/start")
    public Result<Void> startTodayTask(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.startTodayTask(userId);
        return Result.success();
    }

    @Operation(summary = "完成动作")
    @PostMapping("/exercise/{taskExerciseId}/complete")
    public Result<Void> completeExercise(
            @Parameter(description = "任务动作记录ID") @PathVariable Long taskExerciseId,
            @Parameter(description = "实际完成次数") @RequestParam(required = false) Integer actualReps,
            @Parameter(description = "实际用时(秒)") @RequestParam(required = false) Integer actualDuration,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.completeExercise(userId, taskExerciseId, actualReps, actualDuration);
        return Result.success();
    }

    @Operation(summary = "更新任务进度")
    @PostMapping("/{taskId}/update-progress")
    public Result<Void> updateTaskProgress(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.updateTaskProgress(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "创建今日任务", description = "如果今日任务不存在则创建")
    @PostMapping("/create-today")
    public Result<Void> createTodayTask(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.createTodayTask(userId);
        return Result.success();
    }

    // ==================== 任务流程图接口 ====================

    @Operation(summary = "获取任务流程图数据")
    @GetMapping("/flow")
    public Result<TaskFlowVO> getTaskFlow(
            @Parameter(description = "日期(YYYY-MM-DD)，默认今天") @RequestParam(required = false) String date,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        LocalDate queryDate = null;
        if (date != null && !date.isEmpty()) {
            queryDate = LocalDate.parse(date);
        }
        
        TaskFlowVO vo = dailyTaskService.getTaskFlow(userId, queryDate);
        return Result.success(vo);
    }

    @Operation(summary = "获取指定日期的任务流程")
    @GetMapping("/flow/date")
    public Result<TaskFlowVO> getTaskFlowByDate(
            @Parameter(description = "日期(YYYY-MM-DD)") @RequestParam String date,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        LocalDate queryDate = LocalDate.parse(date);
        TaskFlowVO vo = dailyTaskService.getTaskFlow(userId, queryDate);
        return Result.success(vo);
    }

    @Operation(summary = "更新任务顺序")
    @PostMapping("/flow/reorder")
    public Result<Void> reorderTaskFlow(
            @RequestBody List<TaskReorderDTO> orderData,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.reorderTaskFlow(userId, orderData);
        return Result.success();
    }

    @Operation(summary = "开始指定任务")
    @PostMapping("/{taskId}/start")
    public Result<Void> startTask(
            @Parameter(description = "任务动作记录ID") @PathVariable Long taskId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.startTaskFlowItem(userId, taskId);
        return Result.success();
    }

    @Operation(summary = "更新任务优先级")
    @PostMapping("/{taskId}/priority")
    public Result<Void> updateTaskPriority(
            @Parameter(description = "任务动作记录ID") @PathVariable Long taskId,
            @Parameter(description = "优先级(high/medium/low)") @RequestParam String priority,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        dailyTaskService.updateTaskPriority(userId, taskId, priority);
        return Result.success();
    }

    @Operation(summary = "批量更新任务状态")
    @PostMapping("/batch/status")
    public Result<Void> batchUpdateTaskStatus(
            @RequestBody BatchUpdateStatusDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }
        
        if (dto.getTaskIds() == null || dto.getTaskIds().isEmpty() || dto.getStatus() == null) {
            return Result.fail(com.sport.common.constant.ResultCode.PARAM_ERROR);
        }
        
        dailyTaskService.batchUpdateTaskStatus(userId, dto.getTaskIds(), dto.getStatus());
        return Result.success();
    }
}
