package com.ygoj.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 用户信息实体
 */
@Data
public class Userinfo {
    @TableId(type = IdType.AUTO)
    Long id;
    String username;
    String nickname;
    String password;
    String email;
    
    /**
     * 用户角色: USER, ADMIN, CONTEST_ADMIN等
     */
    String role;
    
    /**
     * 位运算权限值（用于细粒度权限控制）
     * 例如: 1=查看题目, 2=提交代码, 4=创建题目, 8=管理比赛等
     */
    Long permission;
}