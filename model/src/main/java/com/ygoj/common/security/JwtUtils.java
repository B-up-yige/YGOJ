package com.ygoj.common.security;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {
    
    private static final String SECRET_KEY = "YGOJ_SECRET_KEY_2026";
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7天
    
    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username, String role, Long permission) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("permission", permission);
        payload.put("createTime", System.currentTimeMillis());
        
        return JWTUtil.createToken(payload, SECRET_KEY.getBytes());
    }
    
    /**
     * 验证并解析 JWT
     */
    public JWT validateToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            
            // 验证签名
            if (!jwt.setKey(SECRET_KEY.getBytes()).verify()) {
                log.warn("JWT 签名验证失败");
                return null;
            }
            
            // 检查过期时间
            Object createTimeObj = jwt.getPayload("createTime");
            if (createTimeObj != null) {
                Long createTime = Long.parseLong(createTimeObj.toString());
                if (System.currentTimeMillis() - createTime > EXPIRATION_TIME) {
                    log.warn("JWT 已过期");
                    return null;
                }
            }
            
            return jwt;
        } catch (Exception e) {
            log.error("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从 JWT 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        JWT jwt = validateToken(token);
        if (jwt == null) {
            return null;
        }
        Object userId = jwt.getPayload("userId");
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }
    
    /**
     * 从 JWT 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        JWT jwt = validateToken(token);
        if (jwt == null) {
            return null;
        }
        Object username = jwt.getPayload("username");
        return username != null ? username.toString() : null;
    }
    
    /**
     * 从 JWT 中获取角色
     */
    public String getRoleFromToken(String token) {
        JWT jwt = validateToken(token);
        if (jwt == null) {
            return null;
        }
        Object role = jwt.getPayload("role");
        return role != null ? role.toString() : null;
    }
    
    /**
     * 从 JWT 中获取权限值
     */
    public Long getPermissionFromToken(String token) {
        JWT jwt = validateToken(token);
        if (jwt == null) {
            return null;
        }
        Object permission = jwt.getPayload("permission");
        return permission != null ? Long.parseLong(permission.toString()) : null;
    }
}
