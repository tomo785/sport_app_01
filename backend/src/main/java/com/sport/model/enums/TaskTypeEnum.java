package com.sport.model.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 */
@Getter
public enum TaskTypeEnum {
    DAILY(1, "每日任务"),
    WEEKLY(2, "每周任务"),
    CHALLENGE(3, "挑战任务"),
    EVENT(4, "限时活动"),
    ACHIEVEMENT(5, "成就任务");

    private final Integer code;
    private final String desc;

    TaskTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskTypeEnum fromCode(Integer code) {
        if (code == null) {
            return DAILY;
        }
        for (TaskTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DAILY;
    }
}
