package com.ygoj.common.constant;

/**
 * 权限常量定义
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
public class PermissionConstants {
    
    // ==================== 角色常量 ====================
    
    /** 普通用户 */
    public static final String ROLE_USER = "USER";
    
    /** 管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /** 比赛管理员 */
    public static final String ROLE_CONTEST_ADMIN = "CONTEST_ADMIN";
    
    /** 题目管理员 */
    public static final String ROLE_PROBLEM_ADMIN = "PROBLEM_ADMIN";
    
    // ==================== 位运算权限常量 ====================
    
    /** 查看题目 */
    public static final int PERM_PROBLEM_VIEW = 0;
    
    /** 提交代码 */
    public static final int PERM_PROBLEM_SUBMIT = 1;
    
    /** 创建题目 */
    public static final int PERM_PROBLEM_CREATE = 2;
    
    /** 编辑题目 */
    public static final int PERM_PROBLEM_EDIT = 3;
    
    /** 删除题目 */
    public static final int PERM_PROBLEM_DELETE = 4;
    
    /** 查看提交记录 */
    public static final int PERM_RECORD_VIEW = 5;
    
    /** 查看排行榜 */
    public static final int PERM_RANKING_VIEW = 6;
    
    /** 创建比赛 */
    public static final int PERM_CONTEST_CREATE = 7;
    
    /** 管理比赛 */
    public static final int PERM_CONTEST_MANAGE = 8;
    
    /** 参加比赛 */
    public static final int PERM_CONTEST_JOIN = 9;
    
    /** 创建题集 */
    public static final int PERM_PROBLEMSET_CREATE = 10;
    
    /** 管理题集 */
    public static final int PERM_PROBLEMSET_MANAGE = 11;
    
    /** 查看题集 */
    public static final int PERM_PROBLEMSET_VIEW = 12;
    
    /** 用户管理 */
    public static final int PERM_USER_MANAGE = 13;
    
    /** 系统配置（重测等） */
    public static final int PERM_SYSTEM_CONFIG = 14;
    
    // ==================== 自定义权限常量 ====================
    
    /** 问题:创建 */
    public static final String CUSTOM_PROBLEM_CREATE = "problem:create";
    
    /** 问题:编辑 */
    public static final String CUSTOM_PROBLEM_EDIT = "problem:edit";
    
    /** 问题:删除 */
    public static final String CUSTOM_PROBLEM_DELETE = "problem:delete";
    
    /** 问题:查看 */
    public static final String CUSTOM_PROBLEM_VIEW = "problem:view";
    
    /** 比赛:创建 */
    public static final String CUSTOM_CONTEST_CREATE = "contest:create";
    
    /** 比赛:管理 */
    public static final String CUSTOM_CONTEST_MANAGE = "contest:manage";
    
    /** 比赛:参加 */
    public static final String CUSTOM_CONTEST_JOIN = "contest:join";
    
    /** 题集:创建 */
    public static final String CUSTOM_PROBLEMSET_CREATE = "problemset:create";
    
    /** 题集:管理 */
    public static final String CUSTOM_PROBLEMSET_MANAGE = "problemset:manage";
    
    /** 用户:管理 */
    public static final String CUSTOM_USER_MANAGE = "user:manage";
}
