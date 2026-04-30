package com.sport.model.enums;

import lombok.Getter;

/**
 * 奖励类型枚举
 */
@Getter
public enum RewardTypeEnum {
    POINTS(1, "积分"),
    EXP(2, "经验值"),
    BADGE(3, "徽章"),
    MEDAL(4, "奖牌"),
    TITLE(5, "称号"),
    AVATAR_FRAME(6, "头像框"),
    EQUIPMENT(7, "装备");

    private final Integer code;
    private final String desc;

    RewardTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RewardTypeEnum fromCode(Integer code) {
        if (code == null) {
            return POINTS;
        }
        for (RewardTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return POINTS;
    }
}
