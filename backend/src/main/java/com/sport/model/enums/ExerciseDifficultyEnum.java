package com.sport.model.enums;

import lombok.Getter;

/**
 * 动作难度枚举
 */
@Getter
public enum ExerciseDifficultyEnum {
    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final Integer code;
    private final String desc;

    ExerciseDifficultyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExerciseDifficultyEnum fromCode(Integer code) {
        if (code == null) {
            return EASY;
        }
        for (ExerciseDifficultyEnum difficulty : values()) {
            if (difficulty.getCode().equals(code)) {
                return difficulty;
            }
        }
        return EASY;
    }
}
