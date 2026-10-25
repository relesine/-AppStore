package com.appstore.service;

import com.appstore.dto.LoginDTO;
import com.appstore.dto.PasswordDTO;
import com.appstore.dto.RegisterDTO;
import com.appstore.vo.LoginVO;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /** 用户登录 */
    LoginVO login(LoginDTO loginDTO);

    /** 用户注册（服务端强制校验用户类型白名单） */
    void register(RegisterDTO registerDTO);

    /** 获取当前登录用户信息 */
    LoginVO getCurrentUserInfo();

    /**
     * 更新当前登录用户的个人资料（昵称、邮箱、手机、头像）
     * @param params 包含 nickname/email/phone/avatar 的 Map
     * @return 更新后的用户信息
     */
    LoginVO updateProfile(Map<String, String> params);

    /** 修改当前登录用户的密码（需校验原密码） */
    void changePassword(PasswordDTO passwordDTO);
}
