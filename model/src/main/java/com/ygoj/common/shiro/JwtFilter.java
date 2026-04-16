package com.ygoj.common.shiro;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Slf4j
public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // OPTIONS 请求直接放行（用于跨域预检）
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        
        // 提取 Token
        String token = extractToken(httpRequest);
        
        if (token != null && !token.isEmpty()) {
            try {
                // 创建 JWT Token 并登录
                JwtToken jwtToken = new JwtToken(token);
                Subject subject = SecurityUtils.getSubject();
                subject.login(jwtToken);
                log.debug("用户认证成功");
            } catch (AuthenticationException e) {
                log.error("认证失败: {}", e.getMessage());
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":401,\"message\":\"认证失败: " + e.getMessage() + "\"}");
                return;
            }
        } else {
            // 没有 Token，返回 401
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return;
        }
        
        // 继续过滤链
        chain.doFilter(request, response);
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        // 优先从 Authorization header 获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }
        
        // 兼容旧的 JWT header
        String jwtHeader = request.getHeader("JWT");
        if (jwtHeader != null && !jwtHeader.isEmpty() && !"NULL".equals(jwtHeader)) {
            return jwtHeader;
        }
        
        return null;
    }
}
