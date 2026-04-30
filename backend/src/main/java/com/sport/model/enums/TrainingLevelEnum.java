package com.sport.model.enums;

import lombok.Getter;

/**
 * 训练难度等级枚举
 */
@Getter
public enum TrainingLevelEnum {
    BEGINNER(1, "初级"),
    INTERMEDIATE(2, "中级"),
    ADVANCED(3, "高级"),
    EXPERT(4, "专业");

    private final Integer code;
    private final String desc;

    TrainingLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TrainingLevelEnum fromCode(Integer code) {
        if (code == null) {
            return BEGINNER;
        }
        for (TrainingLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return BEGINNER;
    }
}
