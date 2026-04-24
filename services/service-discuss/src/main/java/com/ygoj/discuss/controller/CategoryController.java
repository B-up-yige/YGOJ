package com.ygoj.discuss.controller;

import com.ygoj.common.Result;
import com.ygoj.discuss.DiscussionCategory;
import com.ygoj.discuss.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/discuss/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有启用的板块列表（公开访问）
     */
    @GetMapping("/list")
    public Result getActiveCategories() {
        try {
            log.debug("获取启用板块列表请求");
            List<DiscussionCategory> categories = categoryService.getActiveCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取启用板块列表失败", e);
            return Result.error(500, "获取板块列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有板块列表（管理员）
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin/list")
    public Result getAllCategories() {
        try {
            log.info("管理员获取所有板块列表请求");
            List<DiscussionCategory> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取所有板块列表失败", e);
            return Result.error(500, "获取板块列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建板块（管理员）
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/admin/create")
    public Result createCategory(@RequestBody DiscussionCategory category) {
        try {
            log.info("管理员创建板块请求, code: {}, name: {}", category.getCode(), category.getName());
            
            if (category == null) {
                return Result.error(400, "板块信息不能为空");
            }
            
            categoryService.createCategory(category);
            return Result.success();
        } catch (Exception e) {
            log.error("创建板块失败", e);
            return Result.error(500, "创建板块失败: " + e.getMessage());
        }
    }

    /**
     * 更新板块（管理员）
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PutMapping("/admin/update")
    public Result updateCategory(@RequestBody DiscussionCategory category) {
        try {
            log.info("管理员更新板块请求, categoryId: {}", category.getId());
            
            if (category == null || category.getId() == null) {
                return Result.error(400, "板块信息或ID不能为空");
            }
            
            categoryService.updateCategory(category);
            return Result.success();
        } catch (Exception e) {
            log.error("更新板块失败, categoryId: {}", category.getId(), e);
            return Result.error(500, "更新板块失败: " + e.getMessage());
        }
    }

    /**
     * 删除板块（管理员）
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/admin/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        try {
            log.info("管理员删除板块请求, categoryId: {}", id);
            
            if (id == null) {
                return Result.error(400, "板块ID不能为空");
            }
            
            categoryService.deleteCategory(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除板块失败, categoryId: {}", id, e);
            return Result.error(500, "删除板块失败: " + e.getMessage());
        }
    }
}
