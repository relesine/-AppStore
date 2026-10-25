package com.appstore.service;

import com.appstore.common.PageResult;
import com.appstore.entity.User;

public interface UserService {
    PageResult<User> getUserList(Integer pageNum, Integer pageSize, String keyword, Integer userType);
    User getUserById(Long id);
    void updateUser(User user);
    void deleteUser(Long id);
    void updateStatus(Long id, Integer status);
    long getUserCount();
    long getTodayNewUserCount();
}
