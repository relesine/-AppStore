package com.appstore.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.appstore.common.ResultCode;
import com.appstore.dto.LoginDTO;
import com.appstore.dto.PasswordDTO;
import com.appstore.dto.RegisterDTO;
import com.appstore.entity.User;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.UserMapper;
import com.appstore.service.AuthService;
import com.appstore.util.JwtUtil;
import com.appstore.util.RedisUtil;
import com.appstore.util.UserContext;
import com.appstore.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 注册允许的用户类型白名单：1-普通用户 2-作者（禁止注册管理员账号，防止越权提权） */
    private static final Integer[] ALLOWED_REGISTER_TYPES = {1, 2};

    /** 中国大陆手机号正则 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisUtil redisUtil;

    /** 登录失败锁定阈值（连续失败次数） */
    private static final int LOGIN_FAIL_LIMIT = 5;

    /** 登录失败锁定时间（分钟） */
    private static final long LOGIN_LOCK_MINUTES = 10;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 防爆破：连续失败达到阈值后锁定账号（Redis 异常时跳过限制，保证登录可用）
        String failKey = "login:fail:" + loginDTO.getUsername();
        try {
            Object locked = redisUtil.get("login:lock:" + loginDTO.getUsername());
            if (locked != null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "登录失败次数过多，账号已锁定10分钟");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("登录防爆破检查跳过（Redis异常）: {}", e.getMessage());
        }

        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        if (user == null) {
            recordLoginFail(failKey, loginDTO.getUsername());
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 校验密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFail(failKey, loginDTO.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        // 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        // 登录成功：清除失败计数
        try {
            redisUtil.delete(failKey);
        } catch (Exception e) {
            log.warn("清除登录失败计数异常: {}", e.getMessage());
        }
        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setAvatar(user.getAvatar());
        // 登录返回值补充邮箱和手机号，避免个人资料页显示"未设置"
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        log.info("用户登录成功: {} (id={}, type={})", user.getUsername(), user.getId(), user.getUserType());
        return vo;
    }

    /**
     * 记录登录失败次数，达到阈值后锁定账号
     */
    private void recordLoginFail(String failKey, String username) {
        try {
            Long count = redisUtil.incrementWithExpire(failKey, 1, LOGIN_LOCK_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            if (count != null && count >= LOGIN_FAIL_LIMIT) {
                // 达到阈值：锁定账号并清除计数（锁定期间直接拒绝）
                redisUtil.set("login:lock:" + username, "1", LOGIN_LOCK_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                redisUtil.delete(failKey);
                log.warn("账号已触发登录锁定: {}", username);
            }
        } catch (Exception e) {
            log.warn("记录登录失败计数异常: {}", e.getMessage());
        }
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        // 安全加固：用户类型白名单校验，禁止注册为管理员(3)
        Integer userType = registerDTO.getUserType() != null ? registerDTO.getUserType() : 1;
        boolean allowed = false;
        for (Integer type : ALLOWED_REGISTER_TYPES) {
            if (type.equals(userType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不允许注册该用户类型");
        }

        // 安全加固：邮箱/手机号格式校验（空值跳过）
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()
                && !EMAIL_PATTERN.matcher(registerDTO.getEmail()).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱格式不正确");
        }
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()
                && !PHONE_PATTERN.matcher(registerDTO.getPhone()).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号格式不正确");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setNickname(registerDTO.getNickname() != null && !registerDTO.getNickname().isEmpty()
                ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setUserType(userType);
        user.setStatus(1);
        userMapper.insert(user);
        log.info("新用户注册成功: {}", registerDTO.getUsername());
    }

    @Override
    public LoginVO getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        return vo;
    }

    /**
     * 更新当前登录用户的个人资料
     * 只允许修改昵称、邮箱、手机号、头像，不允许修改用户名、密码、角色等敏感字段
     */
    @Override
    public LoginVO updateProfile(Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 只更新允许修改的字段
        if (params.containsKey("nickname") && params.get("nickname") != null) {
            if (params.get("nickname").length() > 20) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "昵称长度不能超过20位");
            }
            user.setNickname(params.get("nickname"));
        }
        if (params.containsKey("email") && params.get("email") != null) {
            String email = params.get("email");
            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱格式不正确");
            }
            user.setEmail(email);
        }
        if (params.containsKey("phone") && params.get("phone") != null) {
            String phone = params.get("phone");
            if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号格式不正确");
            }
            user.setPhone(phone);
        }
        if (params.containsKey("avatar") && params.get("avatar") != null) {
            // 头像URL必须以 /uploads/ 开头，防止外链注入
            String avatar = params.get("avatar");
            if (!avatar.isEmpty() && !avatar.startsWith("/uploads/")) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "头像地址不合法");
            }
            user.setAvatar(avatar);
        }
        userMapper.updateById(user);

        // 返回更新后的用户信息
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        return vo;
    }

    /**
     * 修改当前登录用户的密码
     * 校验原密码正确后，使用BCrypt加密新密码
     */
    @Override
    public void changePassword(PasswordDTO passwordDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 校验原密码
        if (!BCrypt.checkpw(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), "原密码错误");
        }
        // 新密码与原密码相同则直接提示
        if (passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新密码不能与原密码相同");
        }
        // 更新密码
        User update = new User();
        update.setId(user.getId());
        update.setPassword(BCrypt.hashpw(passwordDTO.getNewPassword(), BCrypt.gensalt()));
        userMapper.updateById(update);
        log.info("用户修改密码成功: {} (id={})", user.getUsername(), user.getId());
    }
}
