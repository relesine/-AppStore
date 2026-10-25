package com.appstore.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求DTO
 */
@Data
public class PasswordDTO {

    /** 原密码：必填 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码：6-20位 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度需在6-20位之间")
    private String newPassword;
}
