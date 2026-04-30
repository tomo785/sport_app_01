package com.sport.model.enums;

import lombok.Getter;

/**
 * 目标类型枚举
 */
@Getter
public enum GoalTypeEnum {
    DAILY_DISTANCE(1, "每日距离"),
    WEEKLY_TIMES(2, "每周次数");

    private final Integer code;
    private final String desc;

    GoalTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GoalTypeEnum fromCode(Integer code) {
        if (code == null) {
            return DAILY_DISTANCE;
        }
        for (GoalTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DAILY_DISTANCE;
    }
}
