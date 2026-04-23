package com.ygoj.discuss.service;

import com.ygoj.discuss.DiscussionCategory;

import java.util.List;

public interface CategoryService {
    /**
     * 获取所有启用的板块列表
     */
    List<DiscussionCategory> getActiveCategories();
    
    /**
     * 获取所有板块列表（管理员）
     */
    List<DiscussionCategory> getAllCategories();
    
    /**
     * 根据ID获取板块
     */
    DiscussionCategory getCategoryById(Long id);
    
    /**
     * 创建板块（管理员）
     */
    void createCategory(DiscussionCategory category);
    
    /**
     * 更新板块（管理员）
     */
    void updateCategory(DiscussionCategory category);
    
    /**
     * 删除板块（管理员）
     */
    void deleteCategory(Long id);
}
