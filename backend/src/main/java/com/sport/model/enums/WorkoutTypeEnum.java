package com.sport.model.enums;

import lombok.Getter;

/**
 * 运动类型枚举
 */
@Getter
public enum WorkoutTypeEnum {
    RUNNING(1, "跑步"),
    WALKING(2, "健走"),
    CYCLING(3, "骑行");

    private final Integer code;
    private final String desc;

    WorkoutTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkoutTypeEnum fromCode(Integer code) {
        if (code == null) {
            return RUNNING;
        }
        for (WorkoutTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return RUNNING;
    }
}
