package com.appstore.controller;

import com.appstore.common.PageResult;
import com.appstore.common.Result;
import com.appstore.common.ResultCode;
import com.appstore.entity.User;
import com.appstore.exception.BusinessException;
import com.appstore.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户控制器（管理后台）
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType) {
        return Result.success(userService.getUserList(pageNum, pageSize, keyword, userType));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id");
        Object statusObj = params.get("status");
        if (idObj == null || statusObj == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        Long id = Long.valueOf(idObj.toString());
        Integer status = Integer.valueOf(statusObj.toString());
        userService.updateStatus(id, status);
        return Result.success();
    }
}
