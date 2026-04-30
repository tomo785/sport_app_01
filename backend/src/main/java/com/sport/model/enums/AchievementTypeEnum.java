package com.sport.model.enums;

import lombok.Getter;

/**
 * 成就类型枚举
 */
@Getter
public enum AchievementTypeEnum {
    MILESTONE(1, "里程碑成就"),
    STREAK(2, "连续成就"),
    TOTAL(3, "累计成就"),
    SPECIAL(4, "特殊成就");

    private final Integer code;
    private final String desc;

    AchievementTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AchievementTypeEnum fromCode(Integer code) {
        if (code == null) {
            return MILESTONE;
        }
        for (AchievementTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return MILESTONE;
    }
}
