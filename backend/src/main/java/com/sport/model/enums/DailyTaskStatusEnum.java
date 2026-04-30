package com.sport.model.enums;

import lombok.Getter;

/**
 * 每日任务状态枚举
 */
@Getter
public enum DailyTaskStatusEnum {
    PENDING(0, "待开始"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    SKIPPED(3, "已跳过");

    private final Integer code;
    private final String desc;

    DailyTaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DailyTaskStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (DailyTaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
