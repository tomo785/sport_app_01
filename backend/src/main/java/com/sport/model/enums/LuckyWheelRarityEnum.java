package com.sport.model.enums;

import lombok.Getter;

/**
 * 大转盘运动决定等级枚举
 */
@Getter
public enum LuckyWheelRarityEnum {
    EASY(1, "轻松", "#4CAF50"),
    NORMAL(2, "普通", "#2196F3"),
    HARD(3, "挑战", "#FF9800"),
    INTENSE(4, "强度", "#F44336"),
    REST(5, "休息", "#9E9E9E");

    private final Integer code;
    private final String desc;
    private final String color;

    LuckyWheelRarityEnum(Integer code, String desc, String color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
    }

    public static LuckyWheelRarityEnum fromCode(Integer code) {
        if (code == null) {
            return NORMAL;
        }
        for (LuckyWheelRarityEnum rarity : values()) {
            if (rarity.getCode().equals(code)) {
                return rarity;
            }
        }
        return NORMAL;
    }
}
