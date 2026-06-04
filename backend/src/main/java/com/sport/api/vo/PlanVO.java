package com.sport.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练计划VO（列表项）
 */
@Data
public class PlanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer level;

    private String levelName;

    private Integer totalWeeks;

    private Integer workoutsPerWeek;

    private Integer totalDuration;

    private String coachName;

    private Boolean isOfficial;

    private Integer viewCount;

    private Integer enrollCount;

    private Float rating;

    private Integer ratingCount;

    /** 用户关联状态 */
    private Integer userStatus;

    private String userStatusName;

    private Integer currentWeek;

    private Integer completedCourses;

    private Integer totalCourses;

    private LocalDateTime createTime;

    /** 课程列表 */
    private List<CourseVO> courses;
}
