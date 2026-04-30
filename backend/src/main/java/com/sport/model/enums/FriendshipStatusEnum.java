package com.sport.model.enums;

import lombok.Getter;

/**
 * 好友关系状态枚举
 */
@Getter
public enum FriendshipStatusEnum {
    PENDING(0, "待确认"),
    ACCEPTED(1, "已接受"),
    REJECTED(2, "已拒绝"),
    BLOCKED(3, "已拉黑"),
    DELETED(4, "已删除");

    private final Integer code;
    private final String desc;

    FriendshipStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FriendshipStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (FriendshipStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
