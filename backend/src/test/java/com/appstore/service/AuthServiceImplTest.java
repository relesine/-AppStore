package com.appstore.service;

import cn.hutool.crypto.digest.BCrypt;
import com.appstore.common.ResultCode;
import com.appstore.dto.LoginDTO;
import com.appstore.dto.RegisterDTO;
import com.appstore.entity.User;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.UserMapper;
import com.appstore.service.impl.AuthServiceImpl;
import com.appstore.util.JwtUtil;
import com.appstore.util.RedisUtil;
import com.appstore.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 * 覆盖：注册白名单防提权、登录密码校验、登录防爆破锁定
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = BCrypt.hashpw("123456", BCrypt.gensalt());
    }

    // ---------- 注册防提权 ----------

    @Test
    @DisplayName("注册时禁止创建管理员账号（userType=3 拒绝）")
    void register_shouldRejectAdminType() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("hacker");
        dto.setPassword("123456");
        dto.setUserType(3);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(dto));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册普通用户（userType=1）成功")
    void register_shouldAllowUserType() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setUserType(1);

        authService.register(dto);
        verify(userMapper, times(1)).insert(any(User.class));
    }

    // ---------- 登录 ----------

    @Test
    @DisplayName("登录成功返回Token与用户信息")
    void login_shouldSucceed() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setPassword(hashedPassword);
        user.setUserType(1);
        user.setStatus(1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtil.generateToken(1L, "user1", 1)).thenReturn("token-abc");
        when(redisUtil.get(anyString())).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("user1");
        dto.setPassword("123456");

        LoginVO vo = authService.login(dto);
        assertNotNull(vo);
        assertEquals("token-abc", vo.getToken());
        assertEquals(1L, vo.getUserId());
        // 登录成功后清除失败计数
        verify(redisUtil, times(1)).delete("login:fail:user1");
    }

    @Test
    @DisplayName("密码错误抛出密码错误异常")
    void login_shouldRejectWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setPassword(hashedPassword);
        user.setUserType(1);
        user.setStatus(1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(redisUtil.get(anyString())).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("user1");
        dto.setPassword("wrong-password");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(ResultCode.PASSWORD_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("账号被锁定（连续失败5次）时拒绝登录")
    void login_shouldRejectLockedAccount() {
        when(redisUtil.get("login:lock:user1")).thenReturn("1");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("user1");
        dto.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals("登录失败次数过多，账号已锁定10分钟", ex.getMessage());
        // 锁定期间不再查询数据库
        verify(userMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户名不存在时抛出用户不存在异常")
    void login_shouldRejectUnknownUser() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(redisUtil.get(anyString())).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("ghost");
        dto.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), ex.getCode());
    }
}
