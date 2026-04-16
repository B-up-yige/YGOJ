package com.ygoj.user.service;

import com.ygoj.common.Result;
import com.ygoj.user.Userinfo;

import java.util.Map;

public interface UserService {
    void register(Userinfo userinfo);
    Userinfo getUserinfoByUsername(String username);
    Userinfo login(String loginStr, String password);

    void logout(String token);

    Userinfo getUserinfoById(Long id);

    Long getUserIdByToken(String token);
    
    /**
     * 获取所有用户列表（分页）
     */
    Result getAllUsers(Long page, Long pageSize);
    
    /**
     * 更新用户权限
     */
    Result updateUserPermission(Long userId, String role, Long permission);
    
    /**
     * 删除用户
     */
    Result deleteUser(Long userId);
}
