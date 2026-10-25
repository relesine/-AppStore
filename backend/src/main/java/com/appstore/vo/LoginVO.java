package com.appstore.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private Integer userType;
    private String avatar;
    private String email;
    private String phone;
}
