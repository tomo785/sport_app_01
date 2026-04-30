package com.sport.model.enums;

import lombok.Getter;

/**
 * 排行榜类型枚举
 */
@Getter
public enum LeaderboardTypeEnum {
    DAILY_STEPS(1, "每日步数"),
    DAILY_CALORIES(2, "每日卡路里"),
    DAILY_DISTANCE(3, "每日距离"),
    WEEKLY_STEPS(4, "每周步数"),
    WEEKLY_CALORIES(5, "每周卡路里"),
    WEEKLY_DISTANCE(6, "每周距离"),
    MONTHLY_STEPS(7, "每月步数"),
    MONTHLY_CALORIES(8, "每月卡路里"),
    MONTHLY_DISTANCE(9, "每月距离");

    private final Integer code;
    private final String desc;

    LeaderboardTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LeaderboardTypeEnum fromCode(Integer code) {
        if (code == null) {
            return DAILY_STEPS;
        }
        for (LeaderboardTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DAILY_STEPS;
    }
}
