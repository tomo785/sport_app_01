package com.sport.model.enums;

import lombok.Getter;

/**
 * 训练课程类型枚举
 */
@Getter
public enum TrainingCourseTypeEnum {
    WARM_UP(1, "热身"),
    CARDIO(2, "有氧"),
    STRENGTH(3, "力量"),
    HIIT(4, "高强度间歇"),
    STRETCHING(5, "拉伸"),
    COOL_DOWN(6, "放松"),
    COMBO(7, "组合训练");

    private final Integer code;
    private final String desc;

    TrainingCourseTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TrainingCourseTypeEnum fromCode(Integer code) {
        if (code == null) {
            return CARDIO;
        }
        for (TrainingCourseTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return CARDIO;
    }
}
