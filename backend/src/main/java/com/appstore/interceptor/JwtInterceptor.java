package com.appstore.interceptor;

import com.appstore.common.ResultCode;
import com.appstore.entity.User;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.UserMapper;
import com.appstore.util.JwtUtil;
import com.appstore.util.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证+权限拦截器
 * 用户类型：1=普通用户，2=作者，3=管理员
 * 安全说明：每次请求校验令牌有效性 + 用户账号状态（被禁用的用户令牌立即失效）
 * v1.2 说明：新增"可选认证"模式——公开接口（如应用详情/公告/版本列表）在携带token时
 * 解析用户上下文（作者/管理员可查看未上架内容），未携带token时按游客放行。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserMapper userMapper;

    // 需要管理员权限的路径前缀（userType=3）
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/user/list", "/user/status", "/user/delete", "/user/update",
            "/category/add", "/category/update", "/category/delete",
            "/notice/add", "/notice/update", "/notice/delete",
            "/app/audit", "/audit",
            "/statistics/dashboard"
    );

    // 需要作者或管理员权限的路径前缀（userType=2或3）
    private static final List<String> AUTHOR_PATHS = Arrays.asList(
            "/app/author", "/app/add", "/app/update", "/app/delete",
            "/version/add", "/version/update", "/version/delete",
            "/statistics/author",
            "/file/upload"
    );

    // 公开接口（可选认证）：携带token时解析用户上下文，未携带时按游客放行
    // 这些接口的可见性收敛逻辑在Service层完成（作者/管理员可见未上架内容，游客不可见）
    private static final List<String> OPTIONAL_AUTH_PATHS = Arrays.asList(
            "/app/detail",
            "/notice/list",
            "/notice/detail",
            "/version/list"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 去掉context-path前缀 (/api)
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api")) {
            requestPath = requestPath.substring(4);
        }

        boolean optionalAuth = matchesPath(requestPath, OPTIONAL_AUTH_PATHS);

        String token = request.getHeader(header);
        if (token == null || token.isEmpty()) {
            // 可选认证接口：游客放行；受保护接口：拒绝
            if (optionalAuth) {
                return true;
            }
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 去掉Bearer前缀
        if (token.startsWith(prefix.trim())) {
            token = token.substring(prefix.trim().length());
        }

        if (!jwtUtil.validateToken(token)) {
            // 可选认证接口：无效/过期token按游客放行；受保护接口：拒绝
            if (optionalAuth) {
                return true;
            }
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 将用户信息存入request attribute
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);

        // 安全加固：校验账号状态，被禁用的用户令牌立即失效
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == 0) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }
            // 以数据库中的最新用户类型为准，防止类型被篡改
            userType = user.getUserType();
            username = user.getUsername();
        }

        request.setAttribute(UserContext.USER_ID, userId);
        request.setAttribute(UserContext.USERNAME, username);
        request.setAttribute(UserContext.USER_TYPE, userType);

        // 权限校验
        // 检查是否需要管理员权限
        if (matchesPath(requestPath, ADMIN_PATHS)) {
            if (userType == null || userType != 3) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        // 检查是否需要作者权限
        if (matchesPath(requestPath, AUTHOR_PATHS)) {
            if (userType == null || (userType != 2 && userType != 3)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        return true;
    }

    /**
     * 检查请求路径是否匹配指定的路径前缀列表
     */
    private boolean matchesPath(String requestPath, List<String> pathPrefixes) {
        for (String prefix : pathPrefixes) {
            if (requestPath.equals(prefix) || requestPath.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }
}
