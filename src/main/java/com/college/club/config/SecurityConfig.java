package com.college.club.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 关闭所有接口的权限拦截
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // 所有接口放行
                .csrf(csrf -> csrf.disable()) // 关闭CSRF（测试用，生产需开启）
                .cors(cors -> cors.disable()); // 关闭跨域拦截（可选）
        return http.build();
    }
}