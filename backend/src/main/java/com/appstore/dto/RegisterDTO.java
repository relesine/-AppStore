package com.appstore.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求DTO
 * 安全说明：
 * 1. userType 仅允许 1(普通用户) 或 2(作者)，服务端会强制白名单校验，防止注册管理员账号提权
 * 2. 用户名/密码/昵称/邮箱/手机号均有格式与长度校验，防止脏数据入库
 */
@Data
public class RegisterDTO {

    /** 用户名：3-20位，字母开头，允许字母/数字/下划线 */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$", message = "用户名需以字母开头，长度3-20位，只能包含字母、数字、下划线")
    private String username;

    /** 密码：6-20位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    /** 昵称：最多20位 */
    @Size(max = 20, message = "昵称长度不能超过20位")
    private String nickname;

    /** 邮箱格式校验 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号格式校验（中国大陆11位） */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 用户类型：1-普通用户 2-作者（服务端白名单校验，禁止3管理员） */
    private Integer userType;
}
