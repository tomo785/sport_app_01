package com.sport.model.enums;

import lombok.Getter;

/**
 * 动作类型枚举
 */
@Getter
public enum ExerciseTypeEnum {
    CARDIO(1, "有氧"),
    STRENGTH(2, "力量"),
    STRETCH(3, "拉伸");

    private final Integer code;
    private final String desc;

    ExerciseTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExerciseTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ExerciseTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
