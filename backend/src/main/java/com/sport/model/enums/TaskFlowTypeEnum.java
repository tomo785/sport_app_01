package com.sport.model.enums;

import lombok.Getter;

/**
 * 任务流程图类型枚举
 */
@Getter
public enum TaskFlowTypeEnum {

    WARMUP("warmup", "热身", "🔥"),
    STRENGTH("strength", "力量", "💪"),
    CARDIO("cardio", "有氧", "❤️"),
    CORE("core", "核心", "🎯"),
    STRETCH("stretch", "拉伸", "🧘"),
    REST("rest", "休息", "😴");

    private final String code;
    private final String desc;
    private final String icon;

    TaskFlowTypeEnum(String code, String desc, String icon) {
        this.code = code;
        this.desc = desc;
        this.icon = icon;
    }

    public static TaskFlowTypeEnum fromCode(String code) {
        if (code == null) {
            return STRENGTH;
        }
        for (TaskFlowTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return STRENGTH;
    }

    public static String getIconByCode(String code) {
        TaskFlowTypeEnum type = fromCode(code);
        return type.getIcon();
    }
}
