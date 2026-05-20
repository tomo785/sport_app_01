package com.sport.api.controller;

import com.sport.api.dto.StepsReportDTO;
import com.sport.api.dto.WeRunDecryptDTO;
import com.sport.api.vo.StatsDailyVO;
import com.sport.api.vo.StatsSummaryVO;
import com.sport.api.vo.StatsTrendVO;
import com.sport.common.util.JwtUtil;
import com.sport.common.util.Result;
import com.sport.common.util.WeChatDecryptUtil;
import com.sport.model.entity.User;
import com.sport.service.StatsService;
import com.sport.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计控制器
 */
@Tag(name = "数据统计", description = "运动数据统计、趋势分析")
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;
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

    @Operation(summary = "获取今日统计")
    @GetMapping("/today")
    public Result<StatsDailyVO> getTodayStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        StatsDailyVO vo = statsService.getDailyStats(userId, LocalDate.now());
        return Result.success(vo);
    }

    @Operation(summary = "获取指定日期统计")
    @GetMapping("/daily")
    public Result<StatsDailyVO> getDailyStats(
            @Parameter(description = "日期 (yyyy-MM-dd)") @RequestParam String date,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        LocalDate statDate = LocalDate.parse(date);
        StatsDailyVO vo = statsService.getDailyStats(userId, statDate);
        return Result.success(vo);
    }

    @Operation(summary = "获取日期范围统计列表")
    @GetMapping("/daily/list")
    public Result<List<StatsDailyVO>> getDailyStatsList(
            @Parameter(description = "开始日期 (yyyy-MM-dd)") @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") @RequestParam String endDate,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<StatsDailyVO> list = statsService.getDailyStatsList(userId, start, end);
        return Result.success(list);
    }

    @Operation(summary = "获取统计汇总")
    @GetMapping("/summary")
    public Result<StatsSummaryVO> getStatsSummary(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        StatsSummaryVO vo = statsService.getStatsSummary(userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取周趋势")
    @GetMapping("/trend/weekly")
    public Result<StatsTrendVO> getWeeklyTrend(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        StatsTrendVO vo = statsService.getWeeklyTrend(userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取月趋势")
    @GetMapping("/trend/monthly")
    public Result<StatsTrendVO> getMonthlyTrend(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        StatsTrendVO vo = statsService.getMonthlyTrend(userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取年趋势")
    @GetMapping("/trend/yearly")
    public Result<StatsTrendVO> getYearlyTrend(
            @Parameter(description = "年份") @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        StatsTrendVO vo = statsService.getYearlyTrend(userId, year);
        return Result.success(vo);
    }

    @Operation(summary = "上报今日步数")
    @PostMapping("/steps/today")
    public Result<Void> reportTodaySteps(
            @Valid @RequestBody StepsReportDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        statsService.saveStepsData(
                userId,
                dto.getSteps(),
                dto.getCalories(),
                dto.getDistance(),
                dto.getDuration()
        );
        return Result.success();
    }

    @Operation(summary = "解密微信运动数据并保存步数")
    @PostMapping("/werun/decrypt")
    public Result<StatsDailyVO> decryptWeRunData(
            @Valid @RequestBody WeRunDecryptDTO dto,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail(com.sport.common.constant.ResultCode.UNAUTHORIZED);
        }

        // 获取用户 sessionKey
        User user = userService.getUserById(userId);
        if (user == null || user.getSessionKey() == null || user.getSessionKey().isEmpty()) {
            return Result.fail(400, "请先绑定微信账号");
        }

        try {
            String decrypted = WeChatDecryptUtil.decrypt(
                    dto.getEncryptedData(),
                    user.getSessionKey(),
                    dto.getIv()
            );
            int steps = WeChatDecryptUtil.extractTodaySteps(decrypted);

            // 保存步数
            statsService.saveStepsData(userId, steps, null, null, null);

            // 返回今日统计
            StatsDailyVO vo = statsService.getDailyStats(userId, LocalDate.now());
            return Result.success(vo);
        } catch (Exception e) {
            return Result.fail(500, "微信运动数据解密失败：" + e.getMessage());
        }
    }
}
