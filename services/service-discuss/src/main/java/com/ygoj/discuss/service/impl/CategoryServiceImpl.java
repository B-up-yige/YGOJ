package com.ygoj.discuss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ygoj.discuss.DiscussionCategory;
import com.ygoj.discuss.mapper.DiscussionCategoryMapper;
import com.ygoj.discuss.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private DiscussionCategoryMapper categoryMapper;

    @Override
    public List<DiscussionCategory> getActiveCategories() {
        try {
            log.debug("获取启用的板块列表");
            
            LambdaQueryWrapper<DiscussionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DiscussionCategory::getIsActive, true)
                   .orderByAsc(DiscussionCategory::getSortOrder);
            
            return categoryMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取启用板块列表异常", e);
            throw new RuntimeException("获取板块列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DiscussionCategory> getAllCategories() {
        try {
            log.debug("获取所有板块列表");
            
            LambdaQueryWrapper<DiscussionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(DiscussionCategory::getSortOrder);
            
            return categoryMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取所有板块列表异常", e);
            throw new RuntimeException("获取板块列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DiscussionCategory getCategoryById(Long id) {
        try {
            log.debug("获取板块详情, categoryId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("板块ID不能为空");
            }
            
            return categoryMapper.selectById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取板块详情异常, categoryId: {}", id, e);
            throw new RuntimeException("获取板块详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createCategory(DiscussionCategory category) {
        try {
            log.info("创建板块, code: {}, name: {}", category.getCode(), category.getName());
            
            if (category == null) {
                throw new IllegalArgumentException("板块信息不能为空");
            }
            if (category.getCode() == null || category.getCode().trim().isEmpty()) {
                throw new IllegalArgumentException("板块代码不能为空");
            }
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("板块名称不能为空");
            }
            
            // 检查code是否已存在
            LambdaQueryWrapper<DiscussionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DiscussionCategory::getCode, category.getCode());
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new IllegalArgumentException("板块代码已存在: " + category.getCode());
            }
            
            // 设置默认值
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            if (category.getIsActive() == null) {
                category.setIsActive(true);
            }
            
            categoryMapper.insert(category);
            log.info("创建板块成功, categoryId: {}", category.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建板块异常", e);
            throw new RuntimeException("创建板块失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateCategory(DiscussionCategory category) {
        try {
            log.info("更新板块, categoryId: {}", category.getId());
            
            if (category == null || category.getId() == null) {
                throw new IllegalArgumentException("板块信息或ID不能为空");
            }
            
            categoryMapper.updateById(category);
            log.info("更新板块成功, categoryId: {}", category.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新板块异常, categoryId: {}", category.getId(), e);
            throw new RuntimeException("更新板块失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        try {
            log.info("删除板块, categoryId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("板块ID不能为空");
            }
            
            categoryMapper.deleteById(id);
            log.info("删除板块成功, categoryId: {}", id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除板块异常, categoryId: {}", id, e);
            throw new RuntimeException("删除板块失败: " + e.getMessage(), e);
        }
    }
}
