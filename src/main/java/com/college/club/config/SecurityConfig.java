package com.college.club.config;
import jakarta.servlet.http.Cookie;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.Result;
// 关键修正1：导入你实际的过滤器类（包路径+类名完全匹配）
import com.college.club.config.JsonUsernamePasswordLoginFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.io.PrintWriter;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    // 认证管理器Bean（不变）
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 关键修正2：过滤器Bean方法名+返回值，匹配你的实际类名JsonUsernamePasswordLoginFilter
    @Bean
    public JsonUsernamePasswordLoginFilter jsonUsernamePasswordLoginFilter() throws Exception {
        JsonUsernamePasswordLoginFilter filter = new JsonUsernamePasswordLoginFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setFilterProcessesUrl("/api/user/login"); // 登录接口路径，和控制类一致
        filter.setUsernameParameter("username"); // 匹配JSON传参名
        filter.setPasswordParameter("password"); // 匹配JSON传参名
        filter.setAuthenticationSuccessHandler(customSuccessHandler()); // 成功处理器
        filter.setAuthenticationFailureHandler(customFailureHandler()); // 失败处理器
        return filter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())// 关闭CSRF，适配JSON请求（已清理重复）
                // 完整跨域配置：允许携带Cookie + 暴露响应头（解决Set-Cookie不显示核心）
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true); // 允许跨域携带Cookie
                    config.setExposedHeaders(Collections.singletonList("*")); // 暴露所有响应头（关键！让浏览器显示Set-Cookie）
                    config.setMaxAge(3600L); // 预检请求缓存1小时，减少请求
                    return config;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))// 已清理重复
                .addFilterBefore(jsonUsernamePasswordLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 放行登录/注册/所有Swagger/Knife4j接口
                        .requestMatchers("/api/user/**",
                                "/api/activity/join/generateQr/**",  // 放行生成二维码接口
                                "/api/activity/join/scan",
                                "/sign.html",          //新增：放行签到页面
                                "/sign.html/**",       //新增：放行带参数的签到页面（手机扫码跳转）// 放行扫码签到接口
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                              ).permitAll()
                        // 其他所有接口必须登录
                        .anyRequest().authenticated()
                )
                // 未登录访问受保护接口，统一返回自定义Result
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setContentType("application/json;charset=utf-8");
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            PrintWriter out = res.getWriter();
                            out.write(new ObjectMapper().writeValueAsString(Result.fail(401, "请先登录后再操作")));
                            out.flush();
                            out.close();
                        })
                );
        return http.build();
    }
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (req, res, auth) -> {
            res.setContentType("application/json;charset=utf-8");
            HttpSession session = ((HttpServletRequest) req).getSession(true);

            // 正确存储认证信息（核心修复）
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

//            // 替换所有 Cookie 创建代码（旧代码全部删除）
//            String cookieHeader = String.format("JSESSIONID=%s; Path=/; HttpOnly; SameSite=None", session.getId());
//            res.addHeader("Set-Cookie", cookieHeader);

            // 返回登录成功响应
            PrintWriter out = res.getWriter();
            out.write(new ObjectMapper().writeValueAsString(Result.success("登录成功")));
            out.flush();
            out.close();
        };
    }
    // 登录失败处理器（不变，修复了BusinessException类型转换问题）
    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (req, res, e) -> {
            res.setContentType("application/json;charset=utf-8");
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            PrintWriter out = res.getWriter();
            String msg;

            // 提取被Security包装的自定义业务异常
            if (e.getCause() instanceof BusinessException) {
                msg = ((BusinessException) e.getCause()).getMessage();
            } else {
                // 处理原生认证异常
                msg = "Bad credentials".equals(e.getMessage())
                        ? "用户名或密码错误"
                        : "登录失败，请检查账号密码";
            }

            out.write(new ObjectMapper().writeValueAsString(Result.failParam(msg)));
            out.flush();
            out.close();
        };
    }
}