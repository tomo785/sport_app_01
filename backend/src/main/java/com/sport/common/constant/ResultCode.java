package com.sport.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),
    PARAM_INVALID(4002, "参数格式不正确"),

    UNAUTHORIZED(401, "未授权"),
    TOKEN_INVALID(4011, "Token无效或已过期"),
    TOKEN_EXPIRED(4012, "Token已过期"),

    FORBIDDEN(403, "无权限访问"),

    NOT_FOUND(404, "资源不存在"),

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USER_EXIST(1003, "用户已存在"),
    PERMISSION_DENIED(1004, "权限不足，非管理员账号"),
    OLD_PASSWORD_ERROR(1005, "旧密码错误"),
    PASSWORD_NOT_MATCH(1006, "两次输入的密码不一致"),

    SMS_CODE_ERROR(2001, "验证码错误"),
    SMS_CODE_EXPIRED(2002, "验证码已过期"),
    SMS_SEND_FAILED(2003, "验证码发送失败"),

    WORKOUT_NOT_EXIST(3001, "运动记录不存在"),
    WORKOUT_ALREADY_FINISHED(3002, "运动已结束"),

    GOAL_NOT_EXIST(4001, "目标不存在");

    private final Integer code;
    private final String message;
}
