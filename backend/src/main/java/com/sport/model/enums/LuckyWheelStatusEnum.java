package com.sport.model.enums;

import lombok.Getter;

/**
 * 大转盘活动状态枚举
 */
@Getter
public enum LuckyWheelStatusEnum {
    NOT_STARTED(0, "未开始"),
    ACTIVE(1, "进行中"),
    ENDED(2, "已结束"),
    SUSPENDED(3, "已暂停");

    private final Integer code;
    private final String desc;

    LuckyWheelStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LuckyWheelStatusEnum fromCode(Integer code) {
        if (code == null) {
            return NOT_STARTED;
        }
        for (LuckyWheelStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return NOT_STARTED;
    }
}
