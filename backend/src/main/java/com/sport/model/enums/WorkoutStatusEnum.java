package com.sport.model.enums;

import lombok.Getter;

/**
 * 运动状态枚举
 * 对应数据库: status TINYINT DEFAULT 0 COMMENT '状态 0进行中 1已完成 2已取消 3暂停中'
 */
@Getter
public enum WorkoutStatusEnum {

    IN_PROGRESS(0, "进行中"),
    COMPLETED(1, "已完成"),
    CANCELLED(2, "已取消"),
    PAUSED(3, "暂停中");

    private final Integer code;
    private final String desc;

    WorkoutStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkoutStatusEnum fromCode(Integer code) {
        if (code == null) {
            return IN_PROGRESS;
        }
        for (WorkoutStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return IN_PROGRESS;
    }
}
