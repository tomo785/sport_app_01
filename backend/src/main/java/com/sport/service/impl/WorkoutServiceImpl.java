package com.sport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.api.vo.WorkoutDetailVO;
import com.sport.api.vo.WorkoutListVO;
import com.sport.mapper.WorkoutRecordMapper;
import com.sport.mapper.WorkoutTrajectoryMapper;
import com.sport.model.entity.WorkoutRecord;
import com.sport.model.entity.WorkoutTrajectory;
import com.sport.model.enums.WorkoutStatusEnum;
import com.sport.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运动记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutRecordMapper workoutRecordMapper;
    private final WorkoutTrajectoryMapper workoutTrajectoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startWorkout(Long userId, Integer type) {
        WorkoutRecord record = new WorkoutRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(WorkoutStatusEnum.IN_PROGRESS.getCode());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        workoutRecordMapper.insert(record);
        log.info("开始运动 - 用户ID: {}, 记录ID: {}, 类型: {}", userId, record.getId(), type);

        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseWorkout(Long userId, Long recordId) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return;
        }

        record.setStatus(WorkoutStatusEnum.PAUSED.getCode());
        record.setUpdateTime(LocalDateTime.now());
        workoutRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeWorkout(Long userId, Long recordId) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return;
        }

        record.setStatus(WorkoutStatusEnum.IN_PROGRESS.getCode());
        record.setUpdateTime(LocalDateTime.now());
        workoutRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkoutDetailVO finishWorkout(Long userId, Long recordId) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return null;
        }

        // 计算运动数据
        LocalDateTime endTime = LocalDateTime.now();
        record.setEndTime(endTime);
        record.setStatus(WorkoutStatusEnum.COMPLETED.getCode());

        // 计算时长（秒）
        if (record.getStartTime() != null) {
            long seconds = java.time.Duration.between(record.getStartTime(), endTime).getSeconds();
            record.setDuration((int) seconds);
        }

        // 从轨迹计算距离（简化计算，实际应该累加轨迹点距离）
        List<WorkoutTrajectory> trajectories = workoutTrajectoryMapper.selectByRecordId(recordId);
        if (trajectories != null && !trajectories.isEmpty()) {
            // 计算总距离（简化）
            record.setDistance(calculateDistance(trajectories));
            // 计算卡路里（简化：每公里约60大卡）
            record.setCalories((int) (record.getDistance() / 1000.0 * 60));
        }

        record.setUpdateTime(LocalDateTime.now());
        workoutRecordMapper.updateById(record);

        return convertToDetailVO(record, trajectories);
    }

    @Override
    public WorkoutDetailVO getWorkoutDetail(Long userId, Long recordId) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return null;
        }

        List<WorkoutTrajectory> trajectories = workoutTrajectoryMapper.selectByRecordId(recordId);
        return convertToDetailVO(record, trajectories);
    }

    @Override
    public WorkoutListVO getWorkoutList(Long userId, Integer page, Integer size, Integer type, String startDate, String endDate) {
        LambdaQueryWrapper<WorkoutRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutRecord::getUserId, userId);

        if (type != null) {
            wrapper.eq(WorkoutRecord::getType, type);
        }

        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            wrapper.ge(WorkoutRecord::getStartTime, start.atStartOfDay());
        }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            wrapper.le(WorkoutRecord::getStartTime, end.plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc(WorkoutRecord::getStartTime);

        Page<WorkoutRecord> pageParam = new Page<>(page, size);
        Page<WorkoutRecord> pageResult = workoutRecordMapper.selectPage(pageParam, wrapper);

        List<WorkoutDetailVO> list = pageResult.getRecords().stream()
                .map(r -> convertToDetailVO(r, null))
                .collect(Collectors.toList());

        WorkoutListVO vo = new WorkoutListVO();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(page);
        vo.setSize(size);
        vo.setPages(pageResult.getPages());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadTrajectory(Long userId, Long recordId, List<Map<String, Object>> trajectory) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return;
        }

        int sequence = 0;
        for (Map<String, Object> point : trajectory) {
            WorkoutTrajectory trajectoryPoint = new WorkoutTrajectory();
            trajectoryPoint.setRecordId(recordId);
            trajectoryPoint.setUserId(userId);
            trajectoryPoint.setLatitude(new BigDecimal(point.get("latitude").toString()));
            trajectoryPoint.setLongitude(new BigDecimal(point.get("longitude").toString()));
            trajectoryPoint.setAltitude(point.get("altitude") != null ? Integer.valueOf(point.get("altitude").toString()) : null);
            trajectoryPoint.setSpeed(point.get("speed") != null ? new BigDecimal(point.get("speed").toString()) : null);
            trajectoryPoint.setTimestamp(Long.valueOf(point.get("timestamp").toString()));
            trajectoryPoint.setSequence(sequence++);
            trajectoryPoint.setCreateTime(LocalDateTime.now());

            workoutTrajectoryMapper.insert(trajectoryPoint);
        }
    }

    @Override
    public List<Map<String, Object>> getWorkoutTrajectory(Long userId, Long recordId) {
        WorkoutRecord record = workoutRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return new ArrayList<>();
        }

        List<WorkoutTrajectory> trajectories = workoutTrajectoryMapper.selectByRecordId(recordId);
        return trajectories.stream().map(t -> {
            Map<String, Object> point = new HashMap<>();
            point.put("latitude", t.getLatitude());
            point.put("longitude", t.getLongitude());
            point.put("altitude", t.getAltitude());
            point.put("speed", t.getSpeed());
            point.put("timestamp", t.getTimestamp());
            point.put("sequence", t.getSequence());
            return point;
        }).collect(Collectors.toList());
    }

    /**
     * 计算距离（简化版，实际应使用更精确的算法）
     */
    private Integer calculateDistance(List<WorkoutTrajectory> trajectories) {
        if (trajectories == null || trajectories.size() < 2) {
            return 0;
        }

        double totalDistance = 0;
        for (int i = 1; i < trajectories.size(); i++) {
            WorkoutTrajectory prev = trajectories.get(i - 1);
            WorkoutTrajectory curr = trajectories.get(i);
            totalDistance += calculateDistanceBetweenPoints(
                    prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(),
                    curr.getLatitude().doubleValue(), curr.getLongitude().doubleValue()
            );
        }

        return (int) totalDistance;
    }

    /**
     * 计算两点间距离（米）
     */
    private double calculateDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 转换为详情VO
     */
    private WorkoutDetailVO convertToDetailVO(WorkoutRecord record, List<WorkoutTrajectory> trajectories) {
        WorkoutDetailVO vo = new WorkoutDetailVO();
        BeanUtils.copyProperties(record, vo);

        // 设置状态描述
        WorkoutStatusEnum statusEnum = WorkoutStatusEnum.fromCode(record.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");

        // 设置类型名称
        vo.setTypeName(getWorkoutTypeName(record.getType()));

        // 格式化时间
        if (record.getStartTime() != null) {
            vo.setStartTimeStr(record.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (record.getEndTime() != null) {
            vo.setEndTimeStr(record.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return vo;
    }

    /**
     * 获取运动类型名称
     */
    private String getWorkoutTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "跑步";
            case 2: return "健走";
            case 3: return "骑行";
            default: return "其他";
        }
    }
}
