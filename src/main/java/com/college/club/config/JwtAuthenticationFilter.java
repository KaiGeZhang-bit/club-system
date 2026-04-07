//package com.college.club.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.college.club.common.vo.Result;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// * JWT认证过滤器：拦截所有请求，验证Token并设置登录上下文（适配Spring Boot 3.x + JDK17）
// * 继承OncePerRequestFilter：确保每个请求只执行一次过滤逻辑
// */
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    // 日志（方便排查Token解析异常）
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//
//    // 注入第二步写的JWT工具类
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    // 注入Spring Security的用户详情服务（你项目中已实现，用于查询用户信息）
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    // 新增：注入ObjectMapper，用于返回JSON格式异常
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    /**
//     * 核心过滤逻辑：解析Token → 验证Token → 设置登录状态
//     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        // 1. 从请求头获取Token（前端需按格式传：Authorization: Bearer <token>）
//        String authHeader = request.getHeader("Authorization");
//        String username = null;
//        String jwtToken = null;
//
//        // 2. 解析Token（去掉"Bearer "前缀）
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            jwtToken = authHeader.substring(7); // 截取第7位之后的字符串（"Bearer "是6个字符+1个空格）
//            try {
//                // 从Token中提取用户名（调用JwtTokenUtil的方法）
//                username = jwtTokenUtil.extractUsername(jwtToken);
//            } catch (Exception e) {
//                // Token解析失败（如篡改、过期、格式错误），返回自定义JSON提示
//                logger.error("Token解析失败，请求路径：{}，异常信息：{}", request.getRequestURI(), e.getMessage());
//                // ========== 新增：Token解析失败时返回友好JSON ==========
//                response.setContentType("application/json;charset=utf-8");
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                PrintWriter out = response.getWriter();
//                out.write(objectMapper.writeValueAsString(Result.fail(401, "Token无效或已过期：" + e.getMessage())));
//                out.flush();
//                out.close();
//                return; // 终止过滤器链，不再放行
//                // ========== 新增结束 ==========
//            }
//        }
//
//        // 3. Token有效且当前未登录时，设置登录状态
//        // 条件：① 能从Token中提取用户名 ② Security上下文无登录信息
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            // 3.1 从数据库查询用户详情（调用你项目中已实现的UserDetailsService）
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//
//            // 3.2 验证Token有效性（用户名匹配 + 未过期）
//            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
//                // 3.3 构造认证信息，存入Security上下文
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                // 补充请求详情（如IP地址、SessionID，不影响核心逻辑）
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                // 关键：存入上下文后，后续接口调用getCurrentUser()就能获取用户信息
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        }
//
//        // 4. 放行请求：继续执行后续过滤器（如登录过滤器、权限校验）
//        filterChain.doFilter(request, response);
//    }
//}

package com.college.club.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.college.club.common.vo.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // ========== 新增：白名单路径直接放行 ==========
        String requestURI = request.getRequestURI();
        if (isWhitelistPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        // =========================================

        // 1. 从请求头获取Token
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                logger.error("Token解析失败，请求路径：{}，异常信息：{}", requestURI, e.getMessage());
                // 返回JSON错误，终止过滤器链
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                PrintWriter out = response.getWriter();
                out.write(objectMapper.writeValueAsString(Result.fail(401, "Token无效或已过期：" + e.getMessage())));
                out.flush();
                out.close();
                return;
            }
        }

        // 2. Token有效且未登录时，设置登录上下文
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 3. 放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 判断请求路径是否属于白名单（无需任何 Token 校验）
     * 应与 SecurityConfig 中的放行路径保持一致
     */
    private boolean isWhitelistPath(String uri) {
        return uri.equals("/doc.html") ||
                uri.startsWith("/webjars/") ||
                uri.startsWith("/v3/api-docs/") ||
                uri.startsWith("/swagger-resources/") ||
                uri.startsWith("/swagger-ui/") ||
                uri.equals("/favicon.ico");
    }
}