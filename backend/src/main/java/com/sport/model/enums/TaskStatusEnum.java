package com.sport.model.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatusEnum {
    PENDING(0, "待完成"),
    COMPLETED(1, "已完成"),
    CLAIMED(2, "已领取"),
    EXPIRED(3, "已过期"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String desc;

    TaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
