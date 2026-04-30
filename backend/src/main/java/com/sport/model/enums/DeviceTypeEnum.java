package com.sport.model.enums;

import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
public enum DeviceTypeEnum {
    SMARTWATCH(1, "智能手表"),
    FITNESS_BAND(2, "手环"),
    SMARTPHONE(3, "手机"),
    CHEST_STRAP(4, "心率带"),
    BIKE_COMPUTER(5, "码表"),
    TREADMILL(6, "跑步机"),
    ELLIPTICAL(7, "椭圆机"),
    ROWING_MACHINE(8, "划船机"),
    OTHER(99, "其他");

    private final Integer code;
    private final String desc;

    DeviceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceTypeEnum fromCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        for (DeviceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
