package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.dto.LoginDTO;
import com.appstore.dto.PasswordDTO;
import com.appstore.dto.RegisterDTO;
import com.appstore.service.AuthService;
import com.appstore.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * 认证控制器：登录、注册、个人资料、修改密码
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /** 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    /** 用户注册（服务端白名单校验用户类型） */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    public Result<LoginVO> getCurrentUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 更新当前登录用户的个人资料（昵称、邮箱、手机、头像）
     * 此接口需要登录，且只能修改当前用户自己的信息
     */
    @PutMapping("/profile")
    public Result<LoginVO> updateProfile(@RequestBody Map<String, String> params) {
        return Result.success(authService.updateProfile(params));
    }

    /** 修改当前登录用户的密码（需校验原密码） */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        authService.changePassword(passwordDTO);
        return Result.success();
    }
}
