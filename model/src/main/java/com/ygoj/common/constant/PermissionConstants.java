package com.ygoj.common.constant;

/**
 * 权限常量定义
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
public class PermissionConstants {
    
    // ==================== 角色常量 ====================
    
    /** 超级管理员（只有一个，初始admin账号） */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    
    /** 管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /** 普通用户 */
    public static final String ROLE_USER = "USER";
    
    // ==================== 位运算权限常量 ====================
    
    /** 代码提交权限 */
    public static final int PERM_PROBLEM_SUBMIT = 0;
    
    /** 参加比赛权限 */
    public static final int PERM_CONTEST_JOIN = 1;
    
    /** 创建题目 */
    public static final int PERM_PROBLEM_CREATE = 2;
    
    /** 管理自己的题目（编辑+删除） */
    public static final int PERM_PROBLEM_MANAGE_OWN = 3;
    
    /** 管理所有题目（编辑+删除） */
    public static final int PERM_PROBLEM_MANAGE_ALL = 4;
    
    /** 创建比赛 */
    public static final int PERM_CONTEST_CREATE = 5;
    
    /** 管理自己的比赛（编辑+删除） */
    public static final int PERM_CONTEST_MANAGE_OWN = 6;
    
    /** 管理所有比赛（编辑+删除） */
    public static final int PERM_CONTEST_MANAGE_ALL = 7;
    
    /** 创建题集 */
    public static final int PERM_PROBLEMSET_CREATE = 8;
    
    /** 管理自己的题集（编辑+删除+管理题目） */
    public static final int PERM_PROBLEMSET_MANAGE_OWN = 9;
    
    /** 管理所有题集（编辑+删除+管理题目） */
    public static final int PERM_PROBLEMSET_MANAGE_ALL = 10;
    
    /** 创建帖子 */
    public static final int PERM_POST_CREATE = 11;
    
    /** 管理自己的帖子（编辑+删除） */
    public static final int PERM_POST_MANAGE_OWN = 12;
    
    /** 管理所有帖子（编辑+删除+板块管理） */
    public static final int PERM_POST_MANAGE_ALL = 13;
    
    /** 发表评论 */
    public static final int PERM_COMMENT_CREATE = 14;
    
    /** 删除自己的评论 */
    public static final int PERM_COMMENT_DELETE_OWN = 15;
    
    /** 删除所有评论 */
    public static final int PERM_COMMENT_DELETE_ALL = 16;
    
    /** 用户管理（修改他人权限和角色） */
    public static final int PERM_USER_MANAGE = 17;
    
    // ==================== 自定义权限常量 ====================
    
    /** 题目:创建 */
    public static final String CUSTOM_PROBLEM_CREATE = "problem:create";
    
    /** 题目:管理自己的 */
    public static final String CUSTOM_PROBLEM_MANAGE_OWN = "problem:manage:own";
    
    /** 题目:管理所有 */
    public static final String CUSTOM_PROBLEM_MANAGE_ALL = "problem:manage:all";
    
    /** 比赛:创建 */
    public static final String CUSTOM_CONTEST_CREATE = "contest:create";
    
    /** 比赛:管理自己的 */
    public static final String CUSTOM_CONTEST_MANAGE_OWN = "contest:manage:own";
    
    /** 比赛:管理所有 */
    public static final String CUSTOM_CONTEST_MANAGE_ALL = "contest:manage:all";
    
    /** 题集:创建 */
    public static final String CUSTOM_PROBLEMSET_CREATE = "problemset:create";
    
    /** 题集:管理自己的 */
    public static final String CUSTOM_PROBLEMSET_MANAGE_OWN = "problemset:manage:own";
    
    /** 题集:管理所有 */
    public static final String CUSTOM_PROBLEMSET_MANAGE_ALL = "problemset:manage:all";
    
    /** 帖子:创建 */
    public static final String CUSTOM_POST_CREATE = "post:create";
    
    /** 帖子:管理自己的 */
    public static final String CUSTOM_POST_MANAGE_OWN = "post:manage:own";
    
    /** 帖子:管理所有 */
    public static final String CUSTOM_POST_MANAGE_ALL = "post:manage:all";
    
    /** 评论:创建 */
    public static final String CUSTOM_COMMENT_CREATE = "comment:create";
    
    /** 评论:删除自己的 */
    public static final String CUSTOM_COMMENT_DELETE_OWN = "comment:delete:own";
    
    /** 评论:删除所有 */
    public static final String CUSTOM_COMMENT_DELETE_ALL = "comment:delete:all";
    
    /** 用户:管理 */
    public static final String CUSTOM_USER_MANAGE = "user:manage";
}
