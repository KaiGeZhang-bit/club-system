package com.college.club.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 自定义JSON登录过滤器：解析JSON请求体中的账号密码，适配你的项目
 * 放在com.college.club.config包下
 */
public class JsonUsernamePasswordLoginFilter extends UsernamePasswordAuthenticationFilter {

    // 复用项目已有的JSON解析器，无需额外配置
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 从JSON请求体解析username和password（和前端传参名一致）
            Map<String, String> loginParams = objectMapper.readValue(request.getInputStream(), Map.class);
            // 获取账号密码（参数名严格对应前端JSON的key：username/password）
            String username = loginParams.get(getUsernameParameter());
            String password = loginParams.get(getPasswordParameter());

            // 构造认证Token，走Spring Security原生认证逻辑（密码比对/用户查询）
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            // 设置请求详情（IP、Session等，框架自动处理）
            setDetails(request, authToken);
            // 交给认证管理器处理，和原有表单登录逻辑完全一致
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("登录请求解析失败！请确保请求体为合法JSON格式", e);
        }
    }
}