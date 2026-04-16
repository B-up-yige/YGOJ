package com.ygoj.common.shiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Shiro 用户信息封装类
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiroUser implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 位运算权限值
     */
    private Long permission;
}
