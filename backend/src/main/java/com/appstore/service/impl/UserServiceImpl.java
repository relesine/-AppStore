package com.appstore.service.impl;

import com.appstore.common.PageResult;
import com.appstore.common.ResultCode;
import com.appstore.entity.User;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.UserMapper;
import com.appstore.service.UserService;
import com.appstore.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /** 中国大陆手机号正则 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    @Resource
    private UserMapper userMapper;

    @Override
    public PageResult<User> getUserList(Integer pageNum, Integer pageSize, String keyword, Integer userType) {
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Page<User> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        Page<User> result = userMapper.selectPage(pageQuery, wrapper);
        // 敏感信息脱敏：清除密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        return new PageResult<>(result.getTotal(), result.getRecords(), page, size);
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        // 字段白名单：不通过此接口修改密码/用户名/角色（防止越权提权）
        user.setPassword(null);
        user.setUsername(null);
        user.setUserType(null);
        // 格式校验：昵称/邮箱/手机号（非空时校验）
        if (user.getNickname() != null) {
            if (user.getNickname().trim().isEmpty() || user.getNickname().length() > 20) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "昵称长度需在1-20位之间");
            }
            user.setNickname(user.getNickname().trim());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱格式不正确");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            if (!PHONE_PATTERN.matcher(user.getPhone()).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号格式不正确");
            }
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        // 禁止管理员删除自己，防止系统失去管理员
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能删除当前登录账号");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        userMapper.deleteById(id);
        log.info("删除用户: id={}, 操作人={}", id, UserContext.getUsername());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "状态值不合法");
        }
        // 禁止管理员禁用自己
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能禁用当前登录账号");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("更新用户状态: id={}, status={}, 操作人={}", id, status, UserContext.getUsername());
    }

    @Override
    public long getUserCount() {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
    }

    @Override
    public long getTodayNewUserCount() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, start, end)
                .eq(User::getDeleted, 0));
    }
}
