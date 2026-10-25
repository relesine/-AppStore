package com.appstore.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前用户上下文工具（从JWT拦截器设置的request attribute中获取用户信息）
 */
public class UserContext {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String USER_TYPE = "userType";

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute(USER_ID);
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
        }
        return null;
    }

    public static String getUsername() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object username = request.getAttribute(USERNAME);
            return username != null ? username.toString() : null;
        }
        return null;
    }

    public static Integer getUserType() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userType = request.getAttribute(USER_TYPE);
            if (userType != null) {
                return Integer.valueOf(userType.toString());
            }
        }
        return null;
    }
}
