package com.sport.model.enums;

import lombok.Getter;

/**
 * 每日任务类型枚举
 */
@Getter
public enum DailyTaskTypeEnum {
    SYSTEM_PLAN(1, "系统计划"),
    CUSTOM(2, "自定义"),
    AI_RECOMMEND(3, "AI推荐");

    private final Integer code;
    private final String desc;

    DailyTaskTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DailyTaskTypeEnum fromCode(Integer code) {
        if (code == null) {
            return SYSTEM_PLAN;
        }
        for (DailyTaskTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SYSTEM_PLAN;
    }
}
