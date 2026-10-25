package com.appstore.common;

import lombok.Getter;

/**
 * 返回状态码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXIST(1003, "用户名已存在"),
    USER_DISABLED(1004, "账号已被禁用"),
    APP_NOT_EXIST(2001, "应用不存在"),
    APP_ALREADY_EXIST(2002, "应用已存在"),
    VERSION_ERROR(2003, "版本号错误"),
    FILE_UPLOAD_ERROR(3001, "文件上传失败"),
    FILE_TYPE_ERROR(3002, "文件格式不支持"),
    FILE_SIZE_ERROR(3003, "文件大小超出限制"),
    AUDIT_ERROR(4001, "审核操作失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
