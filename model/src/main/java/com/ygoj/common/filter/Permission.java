package com.ygoj.common.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解，配合权限拦截器 {@link AuthInterceptor} 使用
 * 支持多种权限验证模式：角色权限、位运算权限、自定义权限
 * 
 * @author xushangyi
 * @date 2026/02/11 12:18
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    
    /**
     * 权限类型
     */
    PermissionType type() default PermissionType.ROLE;
    
    /**
     * 权限值
     * - ROLE类型：角色名称，如 "ADMIN", "USER", "CONTEST_ADMIN"
     * - BIT类型：权限位索引，如 0, 1, 2
     * - CUSTOM类型：自定义权限标识，如 "problem:create", "contest:manage"
     */
    String value() default "";
    
    /**
     * 是否需要登录（默认需要）
     */
    boolean requireLogin() default true;
    
    /**
     * 权限验证失败时的提示信息
     */
    String message() default "权限不足";
    
    /**
     * 逻辑关系（当指定多个权限时使用）
     */
    Logical logical() default Logical.OR;
    
    /**
     * 额外权限值（用于多权限验证）
     */
    String[] extra() default {};
    
    /**
     * 权限类型枚举
     */
    enum PermissionType {
        /** 角色权限 */
        ROLE,
        /** 位运算权限 */
        BIT,
        /** 自定义权限 */
        CUSTOM
    }
    
    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /** 满足任一权限即可 */
        OR,
        /** 需要满足所有权限 */
        AND
    }
}
