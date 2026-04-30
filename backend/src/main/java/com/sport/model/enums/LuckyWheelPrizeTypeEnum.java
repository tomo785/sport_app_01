package com.sport.model.enums;

import lombok.Getter;

/**
 * 大转盘运动决定类型枚举
 */
@Getter
public enum LuckyWheelPrizeTypeEnum {
    RUNNING(1, "跑步"),
    WALKING(2, "健走"),
    CYCLING(3, "骑行"),
    REST(4, "休息"),
    STRENGTH(5, "力量训练"),
    YOGA(6, "瑜伽"),
    SWIMMING(7, "游泳"),
    HIKING(8, "徒步"),
    DANCING(9, "跳舞"),
    HOME_WORKOUT(10, "居家运动");

    private final Integer code;
    private final String desc;

    LuckyWheelPrizeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LuckyWheelPrizeTypeEnum fromCode(Integer code) {
        if (code == null) {
            return REST;
        }
        for (LuckyWheelPrizeTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return REST;
    }
}
