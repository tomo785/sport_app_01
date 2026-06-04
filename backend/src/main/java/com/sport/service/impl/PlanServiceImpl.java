package com.sport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.api.dto.CourseDTO;
import com.sport.api.dto.ExerciseDTO;
import com.sport.api.dto.PlanCreateDTO;
import com.sport.api.dto.PlanSearchDTO;
import com.sport.api.dto.PlanUpdateDTO;
import com.sport.api.vo.CourseVO;
import com.sport.api.vo.ExerciseVO;
import com.sport.api.vo.PlanDetailVO;
import com.sport.api.vo.PlanVO;
import com.sport.mapper.*;
import com.sport.model.entity.*;
import com.sport.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 训练计划服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final TrainingPlanMapper trainingPlanMapper;
    private final TrainingCourseMapper trainingCourseMapper;
    private final TrainingExerciseMapper trainingExerciseMapper;
    private final UserTrainingPlanMapper userTrainingPlanMapper;

    private static final int TYPE_CARDIO = 1;
    private static final int TYPE_STRENGTH = 2;
    private static final int TYPE_STRETCHING = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(Long userId, PlanCreateDTO dto) {
        TrainingPlan plan = new TrainingPlan();
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setCoverImage(dto.getCoverImage());
        plan.setLevel(dto.getLevel());
        plan.setTotalWeeks(dto.getTotalWeeks());
        plan.setIsOfficial(false);
        plan.setViewCount(0);
        plan.setEnrollCount(0);
        plan.setRating(0f);
        plan.setRatingCount(0);
        plan.setSortOrder(0);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());

        trainingPlanMapper.insert(plan);

        // 计算运动天数
        int workoutsPerWeek = 0;
        Set<Integer> workoutDays = new HashSet<>();

        // 创建课程
        if (dto.getCourses() != null && !dto.getCourses().isEmpty()) {
            int totalDuration = 0;
            int sortOrder = 0;
            for (CourseDTO courseDTO : dto.getCourses()) {
                TrainingCourse course = new TrainingCourse();
                course.setPlanId(plan.getId());
                course.setName(courseDTO.getName());
                course.setDescription(courseDTO.getDescription());
                course.setWeek(courseDTO.getWeek());
                course.setDay(courseDTO.getDay());
                course.setType(courseDTO.getType());
                course.setLevel(courseDTO.getLevel());
                course.setDuration(courseDTO.getDuration());
                course.setWarmUpDuration(courseDTO.getWarmUpDuration());
                course.setCoolDownDuration(courseDTO.getCoolDownDuration());
                course.setSortOrder(courseDTO.getSortOrder() != null ? courseDTO.getSortOrder() : sortOrder++);
                course.setCreateTime(LocalDateTime.now());
                course.setUpdateTime(LocalDateTime.now());
                trainingCourseMapper.insert(course);

                totalDuration += (courseDTO.getDuration() != null ? courseDTO.getDuration() : 0);
                workoutDays.add(courseDTO.getDay());

                // 创建动作/步骤
                if (courseDTO.getExercises() != null && !courseDTO.getExercises().isEmpty()) {
                    int exSortOrder = 0;
                    for (ExerciseDTO exDTO : courseDTO.getExercises()) {
                        TrainingExercise exercise = new TrainingExercise();
                        exercise.setCourseId(course.getId());
                        exercise.setName(exDTO.getName());
                        exercise.setDescription(exDTO.getDescription());
                        exercise.setType(exDTO.getType());
                        exercise.setDuration(exDTO.getDuration());
                        exercise.setSets(exDTO.getSets());
                        exercise.setReps(exDTO.getReps());
                        exercise.setRestTime(exDTO.getRestTime());
                        exercise.setEquipment(exDTO.getEquipment());
                        exercise.setTargetMuscles(exDTO.getTargetMuscles());
                        exercise.setDifficulty(exDTO.getDifficulty());
                        exercise.setSortOrder(exDTO.getSortOrder() != null ? exDTO.getSortOrder() : exSortOrder++);
                        exercise.setCreateTime(LocalDateTime.now());
                        exercise.setUpdateTime(LocalDateTime.now());
                        trainingExerciseMapper.insert(exercise);
                    }
                }
            }

            plan.setWorkoutsPerWeek(workoutDays.size());
            plan.setTotalDuration(totalDuration > 0 ? totalDuration : null);
            trainingPlanMapper.updateById(plan);
        }

        // 自动关联用户
        UserTrainingPlan utp = new UserTrainingPlan();
        utp.setUserId(userId);
        utp.setPlanId(plan.getId());
        utp.setStatus(1);
        utp.setStartDate(LocalDate.now());
        utp.setEndDate(LocalDate.now().plusWeeks(dto.getTotalWeeks()));
        utp.setCurrentWeek(1);
        utp.setCurrentDay(1);
        utp.setCompletedCourses(0);
        utp.setTotalCourses(dto.getCourses() != null ? dto.getCourses().size() * dto.getTotalWeeks() : 0);
        utp.setCreateTime(LocalDateTime.now());
        utp.setUpdateTime(LocalDateTime.now());
        userTrainingPlanMapper.insert(utp);

        log.info("创建训练计划 - 用户ID: {}, 计划ID: {}, 名称: {}", userId, plan.getId(), plan.getName());
        return plan.getId();
    }

    @Override
    public List<PlanVO> getMyPlans(Long userId, Integer status) {
        LambdaQueryWrapper<UserTrainingPlan> utpWrapper = new LambdaQueryWrapper<>();
        utpWrapper.eq(UserTrainingPlan::getUserId, userId);
        if (status != null) {
            utpWrapper.eq(UserTrainingPlan::getStatus, status);
        }
        utpWrapper.orderByDesc(UserTrainingPlan::getCreateTime);

        List<UserTrainingPlan> utps = userTrainingPlanMapper.selectList(utpWrapper);
        if (utps.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> planIds = utps.stream().map(UserTrainingPlan::getPlanId).collect(Collectors.toList());
        List<TrainingPlan> plans = trainingPlanMapper.selectBatchIds(planIds);
        Map<Long, TrainingPlan> planMap = plans.stream()
                .collect(Collectors.toMap(TrainingPlan::getId, p -> p));

        // 批量查询所有课程
        LambdaQueryWrapper<TrainingCourse> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.in(TrainingCourse::getPlanId, planIds)
                .orderByAsc(TrainingCourse::getWeek)
                .orderByAsc(TrainingCourse::getDay)
                .orderByAsc(TrainingCourse::getSortOrder);
        List<TrainingCourse> allCourses = trainingCourseMapper.selectList(courseWrapper);
        Map<Long, List<TrainingCourse>> courseMap = allCourses.stream()
                .collect(Collectors.groupingBy(TrainingCourse::getPlanId));

        return utps.stream().map(utp -> {
            TrainingPlan plan = planMap.get(utp.getPlanId());
            if (plan == null) return null;
            PlanVO vo = convertToPlanVO(plan, utp);
            List<TrainingCourse> courses = courseMap.getOrDefault(plan.getId(), Collections.emptyList());
            List<CourseVO> courseVOs = courses.stream().map(course -> {
                CourseVO cvo = new CourseVO();
                BeanUtils.copyProperties(course, cvo);
                cvo.setTypeName(getCourseTypeName(course.getType()));
                return cvo;
            }).collect(Collectors.toList());
            vo.setCourses(courseVOs);
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<PlanVO> getCommunityPlans(Long userId, PlanSearchDTO searchDTO) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingPlan::getIsOfficial, true);

        if (searchDTO != null) {
            if (StringUtils.hasText(searchDTO.getKeyword())) {
                wrapper.like(TrainingPlan::getName, searchDTO.getKeyword());
            }
            if (searchDTO.getLevel() != null) {
                wrapper.eq(TrainingPlan::getLevel, searchDTO.getLevel());
            }
            if (searchDTO.getTotalWeeks() != null) {
                wrapper.eq(TrainingPlan::getTotalWeeks, searchDTO.getTotalWeeks());
            }
            // 排序
            if ("rating".equals(searchDTO.getSortBy())) {
                wrapper.orderByDesc(TrainingPlan::getRating);
            } else if ("enrollCount".equals(searchDTO.getSortBy())) {
                wrapper.orderByDesc(TrainingPlan::getEnrollCount);
            } else {
                wrapper.orderByDesc(TrainingPlan::getViewCount);
            }
        } else {
            wrapper.orderByDesc(TrainingPlan::getViewCount);
        }

        List<TrainingPlan> plans = trainingPlanMapper.selectList(wrapper);

        // 获取用户已采用的计划
        LambdaQueryWrapper<UserTrainingPlan> utpWrapper = new LambdaQueryWrapper<>();
        utpWrapper.eq(UserTrainingPlan::getUserId, userId);
        Set<Long> adoptedPlanIds = userTrainingPlanMapper.selectList(utpWrapper).stream()
                .map(UserTrainingPlan::getPlanId)
                .collect(Collectors.toSet());

        return plans.stream().map(plan -> {
            PlanVO vo = convertToPlanVO(plan, null);
            vo.setUserStatus(adoptedPlanIds.contains(plan.getId()) ? 1 : 0);
            vo.setUserStatusName(adoptedPlanIds.contains(plan.getId()) ? "已采用" : null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PlanVO> matchPlans(Long userId, PlanSearchDTO searchDTO) {
        // 智能匹配：根据用户问卷回答匹配最合适的计划
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingPlan::getIsOfficial, true);

        if (searchDTO == null) {
            wrapper.orderByDesc(TrainingPlan::getRating);
            return trainingPlanMapper.selectList(wrapper).stream()
                    .map(p -> convertToPlanVO(p, null))
                    .collect(Collectors.toList());
        }

        // 精确匹配等级
        if (searchDTO.getLevel() != null) {
            wrapper.eq(TrainingPlan::getLevel, searchDTO.getLevel());
        }

        // 周数匹配范围 ±2
        if (searchDTO.getTotalWeeks() != null) {
            wrapper.between(TrainingPlan::getTotalWeeks,
                    Math.max(1, searchDTO.getTotalWeeks() - 2),
                    searchDTO.getTotalWeeks() + 2);
        }

        // 训练天数匹配范围
        if (searchDTO.getDaysPerWeek() != null) {
            wrapper.between(TrainingPlan::getWorkoutsPerWeek,
                    Math.max(1, searchDTO.getDaysPerWeek() - 1),
                    searchDTO.getDaysPerWeek() + 1);
        }

        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.like(TrainingPlan::getName, searchDTO.getKeyword());
        }

        // 目标映射到关键词
        if (StringUtils.hasText(searchDTO.getGoal())) {
            String goal = searchDTO.getGoal();
            if ("减脂".equals(goal)) {
                wrapper.like(TrainingPlan::getDescription, "减脂");
            } else if ("增肌".equals(goal)) {
                wrapper.like(TrainingPlan::getDescription, "增肌");
            } else if ("耐力".equals(goal)) {
                wrapper.like(TrainingPlan::getDescription, "耐力");
            } else if ("备赛".equals(goal)) {
                wrapper.like(TrainingPlan::getDescription, "备赛");
            }
        }

        // 按评分和热度综合排序
        wrapper.orderByDesc(TrainingPlan::getRating);
        wrapper.orderByDesc(TrainingPlan::getEnrollCount);

        List<TrainingPlan> plans = trainingPlanMapper.selectList(wrapper);

        LambdaQueryWrapper<UserTrainingPlan> utpWrapper = new LambdaQueryWrapper<>();
        utpWrapper.eq(UserTrainingPlan::getUserId, userId);
        Set<Long> adoptedPlanIds = userTrainingPlanMapper.selectList(utpWrapper).stream()
                .map(UserTrainingPlan::getPlanId)
                .collect(Collectors.toSet());

        return plans.stream().map(plan -> {
            PlanVO vo = convertToPlanVO(plan, null);
            vo.setUserStatus(adoptedPlanIds.contains(plan.getId()) ? 1 : 0);
            vo.setUserStatusName(adoptedPlanIds.contains(plan.getId()) ? "已采用" : null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PlanDetailVO getPlanDetail(Long userId, Long planId) {
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            return null;
        }

        // 增加浏览量
        plan.setViewCount((plan.getViewCount() != null ? plan.getViewCount() : 0) + 1);
        trainingPlanMapper.updateById(plan);

        PlanDetailVO vo = new PlanDetailVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setLevelName(getLevelName(plan.getLevel()));

        // 获取用户关联状态
        LambdaQueryWrapper<UserTrainingPlan> utpWrapper = new LambdaQueryWrapper<>();
        utpWrapper.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId)
                .orderByDesc(UserTrainingPlan::getCreateTime)
                .last("LIMIT 1");
        UserTrainingPlan utp = userTrainingPlanMapper.selectList(utpWrapper).stream().findFirst().orElse(null);
        if (utp != null) {
            vo.setUserStatus(utp.getStatus());
            vo.setUserStatusName(getUserPlanStatusName(utp.getStatus()));
            vo.setCurrentWeek(utp.getCurrentWeek());
        }

        // 获取课程及动作
        LambdaQueryWrapper<TrainingCourse> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(TrainingCourse::getPlanId, planId)
                .orderByAsc(TrainingCourse::getWeek)
                .orderByAsc(TrainingCourse::getDay)
                .orderByAsc(TrainingCourse::getSortOrder);
        List<TrainingCourse> courses = trainingCourseMapper.selectList(courseWrapper);

        List<Long> courseIds = courses.stream().map(TrainingCourse::getId).collect(Collectors.toList());
        Map<Long, List<TrainingExercise>> exerciseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<TrainingExercise> exWrapper = new LambdaQueryWrapper<>();
            exWrapper.in(TrainingExercise::getCourseId, courseIds)
                    .orderByAsc(TrainingExercise::getSortOrder);
            List<TrainingExercise> allExercises = trainingExerciseMapper.selectList(exWrapper);
            exerciseMap = allExercises.stream()
                    .collect(Collectors.groupingBy(TrainingExercise::getCourseId));
        }

        final Map<Long, List<TrainingExercise>> tempexerciseMap = exerciseMap;

        List<CourseVO> courseVOs = courses.stream().map(course -> {
            CourseVO cvo = new CourseVO();
            BeanUtils.copyProperties(course, cvo);
            cvo.setTypeName(getCourseTypeName(course.getType()));


            List<TrainingExercise> exercises = tempexerciseMap.getOrDefault(course.getId(), Collections.emptyList());
            List<ExerciseVO> exVOs = exercises.stream().map(ex -> {
                ExerciseVO evo = new ExerciseVO();
                BeanUtils.copyProperties(ex, evo);
                evo.setTypeName(getExerciseTypeName(ex.getType()));
                return evo;
            }).collect(Collectors.toList());
            cvo.setExercises(exVOs);

            return cvo;
        }).collect(Collectors.toList());

        vo.setCourses(courseVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(Long userId, Long planId, PlanUpdateDTO dto) {
        // 验证是该用户的计划
        LambdaQueryWrapper<UserTrainingPlan> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId);
        if (!userTrainingPlanMapper.exists(checkWrapper)) {
            return;
        }

        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            return;
        }

        if (StringUtils.hasText(dto.getName())) {
            plan.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            plan.setDescription(dto.getDescription());
        }
        if (dto.getCoverImage() != null) {
            plan.setCoverImage(dto.getCoverImage());
        }
        if (dto.getLevel() != null) {
            plan.setLevel(dto.getLevel());
        }
        if (dto.getTotalWeeks() != null) {
            plan.setTotalWeeks(dto.getTotalWeeks());
        }
        plan.setUpdateTime(LocalDateTime.now());
        trainingPlanMapper.updateById(plan);

        // 如果传入了新课程列表，则全量替换
        if (dto.getCourses() != null && !dto.getCourses().isEmpty()) {
            // 删除旧课程
            LambdaQueryWrapper<TrainingCourse> courseWrapper = new LambdaQueryWrapper<>();
            courseWrapper.eq(TrainingCourse::getPlanId, planId);
            List<TrainingCourse> oldCourses = trainingCourseMapper.selectList(courseWrapper);
            List<Long> oldCourseIds = oldCourses.stream().map(TrainingCourse::getId).collect(Collectors.toList());

            if (!oldCourseIds.isEmpty()) {
                LambdaQueryWrapper<TrainingExercise> exWrapper = new LambdaQueryWrapper<>();
                exWrapper.in(TrainingExercise::getCourseId, oldCourseIds);
                trainingExerciseMapper.delete(exWrapper);
            }
            LambdaQueryWrapper<TrainingCourse> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(TrainingCourse::getPlanId, planId);
            trainingCourseMapper.delete(delWrapper);

            // 插入新课程
            Set<Integer> workoutDays = new HashSet<>();
            int sortOrder = 0;
            for (CourseDTO courseDTO : dto.getCourses()) {
                TrainingCourse course = new TrainingCourse();
                course.setPlanId(planId);
                course.setName(courseDTO.getName());
                course.setDescription(courseDTO.getDescription());
                course.setWeek(courseDTO.getWeek());
                course.setDay(courseDTO.getDay());
                course.setType(courseDTO.getType());
                course.setLevel(courseDTO.getLevel());
                course.setDuration(courseDTO.getDuration());
                course.setWarmUpDuration(courseDTO.getWarmUpDuration());
                course.setCoolDownDuration(courseDTO.getCoolDownDuration());
                course.setSortOrder(courseDTO.getSortOrder() != null ? courseDTO.getSortOrder() : sortOrder++);
                course.setCreateTime(LocalDateTime.now());
                course.setUpdateTime(LocalDateTime.now());
                trainingCourseMapper.insert(course);

                workoutDays.add(courseDTO.getDay());

                if (courseDTO.getExercises() != null && !courseDTO.getExercises().isEmpty()) {
                    int exSortOrder = 0;
                    for (ExerciseDTO exDTO : courseDTO.getExercises()) {
                        TrainingExercise exercise = new TrainingExercise();
                        exercise.setCourseId(course.getId());
                        exercise.setName(exDTO.getName());
                        exercise.setDescription(exDTO.getDescription());
                        exercise.setType(exDTO.getType());
                        exercise.setDuration(exDTO.getDuration());
                        exercise.setSets(exDTO.getSets());
                        exercise.setReps(exDTO.getReps());
                        exercise.setRestTime(exDTO.getRestTime());
                        exercise.setEquipment(exDTO.getEquipment());
                        exercise.setTargetMuscles(exDTO.getTargetMuscles());
                        exercise.setDifficulty(exDTO.getDifficulty());
                        exercise.setSortOrder(exDTO.getSortOrder() != null ? exDTO.getSortOrder() : exSortOrder++);
                        exercise.setCreateTime(LocalDateTime.now());
                        exercise.setUpdateTime(LocalDateTime.now());
                        trainingExerciseMapper.insert(exercise);
                    }
                }
            }

            plan.setWorkoutsPerWeek(workoutDays.size());
            trainingPlanMapper.updateById(plan);
        }

        log.info("更新训练计划 - 用户ID: {}, 计划ID: {}", userId, planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long userId, Long planId) {
        LambdaQueryWrapper<UserTrainingPlan> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId);
        if (!userTrainingPlanMapper.exists(checkWrapper)) {
            return;
        }

        // 删除用户关联
        LambdaQueryWrapper<UserTrainingPlan> delUtp = new LambdaQueryWrapper<>();
        delUtp.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId);
        userTrainingPlanMapper.delete(delUtp);

        // 删除课程和动作
        LambdaQueryWrapper<TrainingCourse> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(TrainingCourse::getPlanId, planId);
        List<TrainingCourse> courses = trainingCourseMapper.selectList(courseWrapper);
        List<Long> courseIds = courses.stream().map(TrainingCourse::getId).collect(Collectors.toList());
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<TrainingExercise> exWrapper = new LambdaQueryWrapper<>();
            exWrapper.in(TrainingExercise::getCourseId, courseIds);
            trainingExerciseMapper.delete(exWrapper);
        }
        LambdaQueryWrapper<TrainingCourse> delCourse = new LambdaQueryWrapper<>();
        delCourse.eq(TrainingCourse::getPlanId, planId);
        trainingCourseMapper.delete(delCourse);

        // 删除计划本身
        trainingPlanMapper.deleteById(planId);

        log.info("删除训练计划 - 用户ID: {}, 计划ID: {}", userId, planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadToCommunity(Long userId, Long planId) {
        // 1. 检查用户是否拥有该计划
        LambdaQueryWrapper<UserTrainingPlan> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId);
        if (!userTrainingPlanMapper.exists(checkWrapper)) {
            throw new RuntimeException("该计划不存在或您无权上传，请先创建并保存计划");
        }

        // 2. 检查计划是否存在
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }

        // 3. 更新为社区计划
        plan.setIsOfficial(true);
        plan.setUpdateTime(LocalDateTime.now());
        trainingPlanMapper.updateById(plan);
        log.info("上传计划到社区 - 用户ID: {}, 计划ID: {}", userId, planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adoptPlan(Long userId, Long planId) {
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            return;
        }

        // 检查是否已采用
        LambdaQueryWrapper<UserTrainingPlan> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserTrainingPlan::getUserId, userId)
                .eq(UserTrainingPlan::getPlanId, planId);
        if (userTrainingPlanMapper.exists(checkWrapper)) {
            return;
        }

        plan.setEnrollCount((plan.getEnrollCount() != null ? plan.getEnrollCount() : 0) + 1);
        trainingPlanMapper.updateById(plan);

        UserTrainingPlan utp = new UserTrainingPlan();
        utp.setUserId(userId);
        utp.setPlanId(planId);
        utp.setStatus(1);
        utp.setStartDate(LocalDate.now());
        utp.setEndDate(LocalDate.now().plusWeeks(plan.getTotalWeeks() != null ? plan.getTotalWeeks() : 4));
        utp.setCurrentWeek(1);
        utp.setCurrentDay(1);
        utp.setCompletedCourses(0);

        // 计算总课程数
        LambdaQueryWrapper<TrainingCourse> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TrainingCourse::getPlanId, planId);
        long courseCount = trainingCourseMapper.selectCount(countWrapper);
        utp.setTotalCourses((int) courseCount * (plan.getTotalWeeks() != null ? plan.getTotalWeeks() : 1));

        utp.setCreateTime(LocalDateTime.now());
        utp.setUpdateTime(LocalDateTime.now());
        userTrainingPlanMapper.insert(utp);

        log.info("采用社区计划 - 用户ID: {}, 计划ID: {}", userId, planId);
    }

    // ==================== 转换方法 ====================

    private PlanVO convertToPlanVO(TrainingPlan plan, UserTrainingPlan utp) {
        PlanVO vo = new PlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setLevelName(getLevelName(plan.getLevel()));

        if (utp != null) {
            vo.setUserStatus(utp.getStatus());
            vo.setUserStatusName(getUserPlanStatusName(utp.getStatus()));
            vo.setCurrentWeek(utp.getCurrentWeek());
            vo.setCompletedCourses(utp.getCompletedCourses());
            vo.setTotalCourses(utp.getTotalCourses());
        }

        return vo;
    }

    private String getLevelName(Integer level) {
        if (level == null) return "未知";
        switch (level) {
            case 1: return "新手";
            case 2: return "进阶";
            case 3: return "高手";
            case 4: return "专家";
            default: return "未知";
        }
    }

    private String getUserPlanStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未开始";
            case 1: return "进行中";
            case 2: return "已完成";
            case 3: return "已暂停";
            case 4: return "已放弃";
            default: return "未知";
        }
    }

    private String getCourseTypeName(Integer type) {
        if (type == null) return "其他";
        switch (type) {
            case 1: return "热身";
            case 2: return "有氧";
            case 3: return "力量";
            case 4: return "HIIT";
            case 5: return "拉伸";
            case 6: return "缓和";
            case 7: return "综合";
            default: return "其他";
        }
    }

    private String getExerciseTypeName(Integer type) {
        if (type == null) return "其他";
        switch (type) {
            case 1: return "有氧";
            case 2: return "力量";
            case 3: return "拉伸";
            default: return "其他";
        }
    }
}
