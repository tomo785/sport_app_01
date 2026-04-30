package com.sport.model.enums;

import lombok.Getter;

/**
 * 训练计划状态枚举
 */
@Getter
public enum TrainingPlanStatusEnum {
    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    PAUSED(3, "已暂停"),
    ABANDONED(4, "已放弃");

    private final Integer code;
    private final String desc;

    TrainingPlanStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TrainingPlanStatusEnum fromCode(Integer code) {
        if (code == null) {
            return NOT_STARTED;
        }
        for (TrainingPlanStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return NOT_STARTED;
    }
}
