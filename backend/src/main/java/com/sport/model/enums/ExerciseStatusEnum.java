package com.sport.model.enums;

import lombok.Getter;

/**
 * 动作状态枚举
 */
@Getter
public enum ExerciseStatusEnum {
    PENDING(0, "未完成"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成");

    private final Integer code;
    private final String desc;

    ExerciseStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExerciseStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (ExerciseStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
