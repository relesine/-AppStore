package com.appstore.config;

import com.appstore.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置（拦截器+静态资源映射）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    /** 上传文件根目录（与 application.yml 的 file.upload-path 保持一致） */
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/app/list",
                        "/app/search",
                        "/app/hot",
                        "/app/new",
                        "/category/list",
                        "/category/sub/**",
                        "/comment/list/**",
                        "/uploads/**",
                        "/error",
                        // v1.3 企业级：接口文档与运维监控端点
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**",
                        "/favicon.ico"
                );
        // 注意：/app/detail/**、/notice/list、/notice/detail/**、/version/list/**
        // 未列入排除名单，由拦截器"可选认证"模式处理（v1.2），
        // 使作者/管理员携带token时可获得用户上下文查看未上架内容，游客按匿名放行
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 统一从配置读取上传目录，避免路径散落硬编码
        String location = "file:" + uploadPath.replace('\\', '/');
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
