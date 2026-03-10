package com.college.club.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger3 + Knife4j 配置类（适配Spring Boot3）
 * 核心：开启JSESSIONID Cookie自动携带，解决登录后401问题
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置文档基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 定义安全方案：指定携带JSESSIONID Cookie作为身份凭证
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // 类型：API密钥
                .in(SecurityScheme.In.COOKIE)     // 携带位置：Cookie
                .name("JSESSIONID");              // Cookie名称：固定为JSESSIONID（Spring Session默认名）

        // 全局开启安全方案：所有接口自动携带上述Cookie
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JSESSIONID");

        return new OpenAPI()
                // 文档标题、版本、描述
                .info(new Info()
                        .title("高校社团管理系统API文档")
                        .version("v1.0")
                        .description("基于Spring Boot3 + Security的社团管理系统，支持JSON登录、用户信息管理"))
                // 配置安全组件（Cookie）
                .components(new Components().addSecuritySchemes("JSESSIONID", securityScheme))
                // 全局应用安全方案
                .addSecurityItem(securityRequirement);
    }
}