package com.sport.model.enums;

import lombok.Getter;

/**
 * 成就稀有度枚举
 */
@Getter
public enum AchievementRarityEnum {
    COMMON(1, "普通", "#CCCCCC"),
    RARE(2, "稀有", "#4287f5"),
    EPIC(3, "史诗", "#a335ee"),
    LEGENDARY(4, "传说", "#ff8000"),
    MYTHIC(5, "神话", "#ff004c");

    private final Integer code;
    private final String desc;
    private final String color;

    AchievementRarityEnum(Integer code, String desc, String color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
    }

    public static AchievementRarityEnum fromCode(Integer code) {
        if (code == null) {
            return COMMON;
        }
        for (AchievementRarityEnum rarity : values()) {
            if (rarity.getCode().equals(code)) {
                return rarity;
            }
        }
        return COMMON;
    }
}
