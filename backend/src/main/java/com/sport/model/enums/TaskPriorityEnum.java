package com.sport.model.enums;

import lombok.Getter;

/**
 * 任务优先级枚举
 */
@Getter
public enum TaskPriorityEnum {

    HIGH("high", "高"),
    MEDIUM("medium", "中"),
    LOW("low", "低");

    private final String code;
    private final String desc;

    TaskPriorityEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskPriorityEnum fromCode(String code) {
        if (code == null) {
            return MEDIUM;
        }
        for (TaskPriorityEnum priority : values()) {
            if (priority.getCode().equalsIgnoreCase(code)) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
