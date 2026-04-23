package com.ygoj.discuss;

/**
 * 讨论区板块分类常量
 */
public class DiscussionCategory {
    
    /** 综合讨论 */
    public static final String GENERAL = "GENERAL";
    
    /** 题目求助 */
    public static final String PROBLEM_HELP = "PROBLEM_HELP";
    
    /** 算法交流 */
    public static final String ALGORITHM = "ALGORITHM";
    
    /** Bug反馈 */
    public static final String BUG_REPORT = "BUG_REPORT";
    
    /** 建议意见 */
    public static final String SUGGESTION = "SUGGESTION";
    
    /**
     * 获取板块显示名称
     */
    public static String getCategoryName(String category) {
        if (category == null) {
            return "未知板块";
        }
        switch (category) {
            case GENERAL:
                return "综合讨论";
            case PROBLEM_HELP:
                return "题目求助";
            case ALGORITHM:
                return "算法交流";
            case BUG_REPORT:
                return "Bug反馈";
            case SUGGESTION:
                return "建议意见";
            default:
                return "未知板块";
        }
    }
}
