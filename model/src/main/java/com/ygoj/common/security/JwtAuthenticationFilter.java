package com.ygoj.common.security;

import com.ygoj.common.constant.PermissionConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                Object jwtValue = redisTemplate.opsForValue().get(token);
                if (jwtValue != null) {
                    // 刷新 Token 过期时间
                    redisTemplate.expire(token, 7, TimeUnit.DAYS);
                    
                    // 从 Redis 中获取 JWT 字符串
                    String jwtToken = jwtValue.toString();
                    
                    // 验证并解析 JWT
                    cn.hutool.jwt.JWT jwt = jwtUtils.validateToken(jwtToken);
                    
                    if (jwt != null) {
                        // 获取用户信息
                        Long userId = jwtUtils.getUserIdFromToken(jwtToken);
                        String username = jwtUtils.getUsernameFromToken(jwtToken);
                        String role = jwtUtils.getRoleFromToken(jwtToken);
                        Long permission = jwtUtils.getPermissionFromToken(jwtToken);
                        
                        if (userId != null && username != null) {
                            // 将权限值转换为 GrantedAuthority 列表
                            List<SimpleGrantedAuthority> authorities = convertPermissionToAuthorities(role, permission);
                            
                            // 创建认证对象
                            CustomUserDetails userDetails = new CustomUserDetails(
                                userId, 
                                username, 
                                role,
                                authorities
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
                            
                            log.debug("用户认证成功: userId={}, username={}, role={}, authorities={}", 
                                userId, username, role, authorities.size());
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
    
    /**
     * 将权限值转换为 GrantedAuthority 列表
     */
    private List<SimpleGrantedAuthority> convertPermissionToAuthorities(String role, Long permission) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色
        if (role != null && !role.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(role));
            
            // 如果是管理员，添加所有自定义权限
            if (PermissionConstants.ROLE_ADMIN.equals(role)) {
                // 题目相关权限
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_CREATE));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_EDIT));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_DELETE));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_VIEW));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_CREATE"));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_EDIT"));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_DELETE"));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_SUBMIT"));
                
                // 比赛相关权限
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_CREATE));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_MANAGE));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_JOIN));
                authorities.add(new SimpleGrantedAuthority("CONTEST_CREATE"));
                authorities.add(new SimpleGrantedAuthority("CONTEST_MANAGE"));
                
                // 题集相关权限
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEMSET_CREATE));
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEMSET_MANAGE));
                authorities.add(new SimpleGrantedAuthority("PROBLEMSET_CREATE"));
                authorities.add(new SimpleGrantedAuthority("PROBLEMSET_MANAGE"));
                
                // 用户管理权限
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_USER_MANAGE));
                authorities.add(new SimpleGrantedAuthority("USER_MANAGE"));
                
                // 其他权限
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                
                return authorities;
            }
        }
        
        // 根据位运算权限值添加对应的权限
        if (permission != null) {
            // 题目相关权限
            if ((permission & (1L << PermissionConstants.PERM_PROBLEM_VIEW)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_VIEW));
            }
            if ((permission & (1L << PermissionConstants.PERM_PROBLEM_SUBMIT)) != 0) {
                authorities.add(new SimpleGrantedAuthority("PROBLEM_SUBMIT"));
            }
            if ((permission & (1L << PermissionConstants.PERM_PROBLEM_CREATE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_CREATE));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_CREATE"));
            }
            if ((permission & (1L << PermissionConstants.PERM_PROBLEM_EDIT)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_EDIT));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_EDIT"));
            }
            if ((permission & (1L << PermissionConstants.PERM_PROBLEM_DELETE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEM_DELETE));
                authorities.add(new SimpleGrantedAuthority("PROBLEM_DELETE"));
            }
            
            // 比赛相关权限
            if ((permission & (1L << PermissionConstants.PERM_CONTEST_CREATE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_CREATE));
                authorities.add(new SimpleGrantedAuthority("CONTEST_CREATE"));
            }
            if ((permission & (1L << PermissionConstants.PERM_CONTEST_MANAGE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_MANAGE));
                authorities.add(new SimpleGrantedAuthority("CONTEST_MANAGE"));
            }
            if ((permission & (1L << PermissionConstants.PERM_CONTEST_JOIN)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_CONTEST_JOIN));
            }
            
            // 题集相关权限
            if ((permission & (1L << PermissionConstants.PERM_PROBLEMSET_CREATE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEMSET_CREATE));
                authorities.add(new SimpleGrantedAuthority("PROBLEMSET_CREATE"));
            }
            if ((permission & (1L << PermissionConstants.PERM_PROBLEMSET_MANAGE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_PROBLEMSET_MANAGE));
                authorities.add(new SimpleGrantedAuthority("PROBLEMSET_MANAGE"));
            }
            
            // 用户管理权限
            if ((permission & (1L << PermissionConstants.PERM_USER_MANAGE)) != 0) {
                authorities.add(new SimpleGrantedAuthority(PermissionConstants.CUSTOM_USER_MANAGE));
                authorities.add(new SimpleGrantedAuthority("USER_MANAGE"));
            }
        }
        
        return authorities;
    }
}
