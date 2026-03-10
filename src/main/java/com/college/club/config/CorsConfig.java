package com.college.club.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置核心类：解决Swagger跨域无法存储/携带JSESSIONID Cookie
 * 无此类，Swagger自动带Cookie的配置完全失效！
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 本地调试允许所有域名（生产可指定具体域名，如http://localhost:8080）
        config.addAllowedOriginPattern("*");
        // 允许所有请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 核心中的核心：允许跨域携带Cookie（无此行，一切白搭）
        config.setAllowCredentials(true);
        // 预检请求有效期（3600秒），避免频繁发送预检请求
        config.setMaxAge(3600L);

        // 所有接口生效跨域规则
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}