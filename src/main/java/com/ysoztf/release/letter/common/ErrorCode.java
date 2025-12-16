package com.ysoztf.release.letter.common;

/**
 * 统一错误码定义
 */
public enum ErrorCode {

    SUCCESS(0, "成功"),

    // 通用错误
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1004, "资源不存在"),
    SYSTEM_ERROR(2000, "系统异常"),

    // 业务相关错误示例
    MAIL_SEND_FAILED(3001, "邮件发送失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}


