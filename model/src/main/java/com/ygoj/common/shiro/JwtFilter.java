package com.ygoj.common.shiro;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.http.HttpStatus;

/**
 * JWT 认证过滤器
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Slf4j
public class JwtFilter extends AuthenticatingFilter {

    /**
     * 创建 Token
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = extractToken(httpRequest);
        
        if (token != null && !token.isEmpty()) {
            return new JwtToken(token);
        }
        return null;
    }

    /**
     * 判断是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // OPTIONS 请求直接放行
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            return true;
        }
        
        // 检查是否有 Token
        String token = extractToken(httpRequest);
        return token != null && !token.isEmpty();
    }

    /**
     * 当访问被拒绝时的处理
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
        return false;
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
