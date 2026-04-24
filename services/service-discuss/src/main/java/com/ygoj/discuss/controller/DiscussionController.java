package com.ygoj.discuss.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.discuss.DiscussionPost;
import com.ygoj.discuss.DiscussionComment;
import com.ygoj.discuss.service.DiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/discuss")
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;

    /**
     * 获取帖子列表(公开访问)
     */
    @GetMapping("/posts")
    public Result getPostList(@RequestParam(required = false, defaultValue = "1") Long page,
                              @RequestParam(required = false, defaultValue = "10") Long pageSize,
                              @RequestParam(required = false) String category) {
        try {
            log.debug("获取帖子列表请求, page: {}, pageSize: {}, category: {}", page, pageSize, category);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10L;
            }
            
            Page<DiscussionPost> posts = discussionService.getPostList(page, pageSize, category);
            return Result.success(posts);
        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return Result.error(500, "获取帖子列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取帖子详情(公开访问)
     */
    @GetMapping("/post/{id}")
    public Result getPostById(@PathVariable Long id) {
        try {
            log.info("获取帖子详情请求, postId: {}", id);
            
            if (id == null) {
                return Result.error(400, "帖子ID不能为空");
            }
            
            // 增加浏览量
            discussionService.incrementViewCount(id);
            
            DiscussionPost post = discussionService.getPostById(id);
            if (post == null) {
                return Result.error(404, "帖子不存在");
            }
            
            return Result.success(post);
        } catch (Exception e) {
            log.error("获取帖子详情失败, postId: {}", id, e);
            return Result.error(500, "获取帖子详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建帖子(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/create")
    public Result createPost(@RequestBody DiscussionPost post) {
        try {
            log.info("创建帖子请求, title: {}", post.getTitle());
            
            if (post == null) {
                return Result.error(400, "帖子信息不能为空");
            }
            
            discussionService.createPost(post);
            return Result.success();
        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return Result.error(500, "创建帖子失败: " + e.getMessage());
        }
    }

    /**
     * 更新帖子(需要登录且是作者或管理员)
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/post/update")
    public Result updatePost(@RequestBody DiscussionPost post) {
        try {
            log.info("更新帖子请求, postId: {}", post.getId());
            
            if (post == null || post.getId() == null) {
                return Result.error(400, "帖子信息或ID不能为空");
            }
            
            // 获取当前用户ID
            Long currentUserId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            // 检查权限：作者本人或管理员
            DiscussionPost existingPost = discussionService.getPostById(post.getId());
            if (existingPost == null) {
                return Result.error(404, "帖子不存在");
            }
            
            String currentRole = com.ygoj.common.security.SecurityUtils.getCurrentRole();
            boolean isAdmin = "ADMIN".equals(currentRole) || "SUPER_ADMIN".equals(currentRole);
            boolean isAuthor = currentUserId.equals(existingPost.getAuthorId());
            
            if (!isAdmin && !isAuthor) {
                return Result.error(403, "无权限编辑此帖子");
            }
            
            discussionService.updatePost(post);
            return Result.success();
        } catch (Exception e) {
            log.error("更新帖子失败, postId: {}", post.getId(), e);
            return Result.error(500, "更新帖子失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子(需要管理员权限)
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/post/{id}")
    public Result deletePost(@PathVariable Long id) {
        try {
            log.info("删除帖子请求, postId: {}", id);
            
            if (id == null) {
                return Result.error(400, "帖子ID不能为空");
            }
            
            // 获取当前用户ID
            Long currentUserId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            // 检查权限：作者本人或管理员
            DiscussionPost post = discussionService.getPostById(id);
            if (post == null) {
                return Result.error(404, "帖子不存在");
            }
            
            String currentRole = com.ygoj.common.security.SecurityUtils.getCurrentRole();
            boolean isAdmin = "ADMIN".equals(currentRole) || "SUPER_ADMIN".equals(currentRole);
            boolean isAuthor = currentUserId.equals(post.getAuthorId());
            
            if (!isAdmin && !isAuthor) {
                return Result.error(403, "无权限删除此帖子");
            }
            
            discussionService.deletePost(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除帖子失败, postId: {}", id, e);
            return Result.error(500, "删除帖子失败: " + e.getMessage());
        }
    }

    /**
     * 获取评论列表(公开访问)
     */
    @GetMapping("/comments/{postId}")
    public Result getComments(@PathVariable Long postId) {
        try {
            log.debug("获取评论列表请求, postId: {}", postId);
            
            if (postId == null) {
                return Result.error(400, "帖子ID不能为空");
            }
            
            List<DiscussionComment> comments = discussionService.getCommentsByPostId(postId);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败, postId: {}", postId, e);
            return Result.error(500, "获取评论列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建评论(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment/create")
    public Result createComment(@RequestBody DiscussionComment comment) {
        try {
            log.info("创建评论请求, postId: {}", comment.getPostId());
            
            if (comment == null) {
                return Result.error(400, "评论信息不能为空");
            }
            
            discussionService.createComment(comment);
            return Result.success();
        } catch (Exception e) {
            log.error("创建评论失败", e);
            return Result.error(500, "创建评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论(需要管理员权限)
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/comment/{id}")
    public Result deleteComment(@PathVariable Long id) {
        try {
            log.info("删除评论请求, commentId: {}", id);
            
            if (id == null) {
                return Result.error(400, "评论ID不能为空");
            }
            
            discussionService.deleteComment(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除评论失败, commentId: {}", id, e);
            return Result.error(500, "删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 置顶/取消置顶帖子(需要管理所有帖子的权限或管理员)
     */
    @PreAuthorize("hasAuthority('POST_MANAGE_ALL') or hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PutMapping("/post/{id}/pin")
    public Result togglePinPost(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> request) {
        try {
            log.info("切换帖子置顶状态请求, postId: {}, request: {}", id, request);
            
            if (id == null) {
                return Result.error(400, "帖子ID不能为空");
            }
            
            Boolean isPinned = request.get("isPinned");
            if (isPinned == null) {
                return Result.error(400, "置顶状态不能为空");
            }
            
            // 获取当前用户ID
            Long currentUserId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            discussionService.togglePinPost(id, isPinned);
            return Result.success(isPinned ? "置顶成功" : "取消置顶成功");
        } catch (Exception e) {
            log.error("切换帖子置顶状态失败, postId: {}", id, e);
            return Result.error(500, "操作失败: " + e.getMessage());
        }
    }
}
