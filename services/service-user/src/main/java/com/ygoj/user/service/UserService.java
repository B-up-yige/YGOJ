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
     * 拉黑/解禁用户
     */
    Result banUser(Long userId, Integer isBanned);
    
    /**
     * 更新用户信息（昵称、邮箱）
     */
    Result updateUserInfo(Long userId, String nickname, String email);
    
    /**
     * 修改密码
     */
    Result changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 管理员重置用户密码
     */
    Result resetUserPassword(Long userId, String newPassword);
}
