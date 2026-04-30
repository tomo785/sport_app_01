package com.sport.model.enums;

import lombok.Getter;

/**
 * 排行榜周期枚举
 */
@Getter
public enum LeaderboardPeriodEnum {
    DAILY(1, "每日"),
    WEEKLY(2, "每周"),
    MONTHLY(3, "每月");

    private final Integer code;
    private final String desc;

    LeaderboardPeriodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LeaderboardPeriodEnum fromCode(Integer code) {
        if (code == null) {
            return DAILY;
        }
        for (LeaderboardPeriodEnum period : values()) {
            if (period.getCode().equals(code)) {
                return period;
            }
        }
        return DAILY;
    }
}
