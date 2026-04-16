package com.ygoj.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 工具类
 */
public class SecurityUtils {
    
    /**
     * 获取当前认证用户
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }
    
    /**
     * 检查是否已认证
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
    
    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        CustomUserDetails user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return role.equals(user.getRole());
    }
}
