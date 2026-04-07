package com.college.club.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components; // 优化导入（不用写全类名）
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 适配 Knife4j 4.4.0 的 OpenAPI 3 配置（解决授权按钮不显示、Token 传递问题）
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. 定义JWT安全方案（适配Knife4j 4.4.0，显式指定所有参数）
        String securitySchemeName = "BearerAuth"; // 授权方案ID（全局唯一）
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)       // 类型：HTTP
                .scheme("bearer")                     // 认证方案：bearer
                .bearerFormat("JWT")                 // 格式：JWT（标注说明）
                .in(SecurityScheme.In.HEADER)        // 传递位置：请求头（显式指定，4.4.0必填）
                .name("Authorization");              // 请求头名称（和过滤器一致）

        // 2. 全局授权要求（绑定上面的授权方案ID）
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName); // 必须和上面的securitySchemeName一致

        // 3. 构建OpenAPI文档（适配Knife4j 4.4.0）
        return new OpenAPI()
                // 文档基础信息（非必需，但完善后更易读）
                .info(new Info()
                        .title("高校社团管理系统API")
                        .version("v1.0")
                        .description("基于Spring Boot 3 + JWT + Knife4j 4.4.0的API文档")
                        .contact(new Contact().name("开发团队").email("dev@example.com")))
                // 4. 注册安全组件（核心：Knife4j 4.4.0必须显式注册）
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, jwtScheme))
                // 5. 全局启用JWT授权（所有接口默认带Authorization请求头）
                .security(java.util.Collections.singletonList(securityRequirement));
    }
}