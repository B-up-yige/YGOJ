package com.ygoj.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头获取 Token
            String token = extractToken(request);
            
            if (token != null && !token.isEmpty()) {
                // 检查 Token 是否在 Redis 中存在（验证有效性）
                if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
                    // 刷新 Token 过期时间
                    redisTemplate.expire(token, 7, TimeUnit.DAYS);
                    
                    // 验证并解析 JWT
                    cn.hutool.jwt.JWT jwt = jwtUtils.validateToken(token);
                    
                    if (jwt != null) {
                        // 获取用户信息
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        String username = jwtUtils.getUsernameFromToken(token);
                        String role = jwtUtils.getRoleFromToken(token);
                        
                        if (userId != null && username != null) {
                            // 创建认证对象
                            CustomUserDetails userDetails = new CustomUserDetails(
                                userId, 
                                username, 
                                role,
                                Collections.emptyList()
                            );
                            
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                                );
                            
                            authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            
                            // 设置到 SecurityContext
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            log.debug("用户认证成功: userId={}, username={}", userId, username);
                        }
                    } else {
                        log.warn("JWT 验证失败");
                    }
                } else {
                    log.warn("Token 在 Redis 中不存在或已过期");
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证处理异常", e);
        }
        
        filterChain.doFilter(request, response);
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
