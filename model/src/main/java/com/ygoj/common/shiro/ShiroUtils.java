package com.ygoj.common.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * Shiro 权限检查工具类
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
public class ShiroUtils {

    /**
     * 获取当前用户
     */
    public static ShiroUser getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            return (ShiroUser) subject.getPrincipal();
        }
        return null;
    }

    /**
     * 检查是否已认证
     */
    public static boolean isAuthenticated() {
        Subject subject = SecurityUtils.getSubject();
        return subject != null && subject.isAuthenticated();
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        Subject subject = SecurityUtils.getSubject();
        return subject != null && subject.hasRole(role);
    }

    /**
     * 检查是否有任一角色
     */
    public static boolean hasAnyRole(String... roles) {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return false;
        }
        for (String role : roles) {
            if (subject.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有角色
     */
    public static boolean hasAllRoles(String... roles) {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return false;
        }
        for (String role : roles) {
            if (!subject.hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        Subject subject = SecurityUtils.getSubject();
        return subject != null && subject.isPermitted(permission);
    }

    /**
     * 检查是否有任一权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return false;
        }
        for (String permission : permissions) {
            if (subject.isPermitted(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有权限
     */
    public static boolean hasAllPermissions(String... permissions) {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return false;
        }
        for (String permission : permissions) {
            if (!subject.isPermitted(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        ShiroUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        ShiroUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        ShiroUser user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 登出
     */
    public static void logout() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null) {
            subject.logout();
        }
    }
}
