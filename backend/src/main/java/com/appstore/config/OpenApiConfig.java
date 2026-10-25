package com.appstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

/**
 * Swagger/OpenAPI 文档配置（v1.3 企业级增强）
 * <p>
 * 访问地址：
 * <ul>
 *   <li>Swagger UI:      http://localhost:8080/api/swagger-ui/index.html</li>
 *   <li>OpenAPI JSON:    http://localhost:8080/api/v3/api-docs</li>
 * </ul>
 * 右上角 Authorize 按钮可携带 Bearer Token 调试受保护接口。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("轻量应用商店 AppStore REST API")
                        .description("覆盖用户端/作者端/管理端完整业务闭环的应用分发平台接口文档。"
                                + "统一前缀 /api；统一返回 {code, message, data}；"
                                + "认证方式：请求头 Authorization: Bearer &lt;token&gt;")
                        .version("v1.3.0")
                        .contact(new Contact().name("AppStore Team"))
                        .license(new License().name("Internal Use Only")))
                .servers(Arrays.asList(
                        new Server().url("/").description("本地/网关相对路径")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("粘贴登录接口返回的 accessToken")))
                .security(Collections.singletonList(
                        new SecurityRequirement().addList(SECURITY_SCHEME_NAME)));
    }
}
