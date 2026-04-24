package com.ygoj.discuss.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.discuss.DiscussionPost;
import com.ygoj.discuss.DiscussionComment;

import java.util.List;

public interface DiscussionService {
    /**
     * 分页获取帖子列表
     */
    Page<DiscussionPost> getPostList(Long page, Long pageSize, String category);
    
    /**
     * 获取帖子详情
     */
    DiscussionPost getPostById(Long id);
    
    /**
     * 创建帖子
     */
    void createPost(DiscussionPost post);
    
    /**
     * 更新帖子
     */
    void updatePost(DiscussionPost post);
    
    /**
     * 删除帖子
     */
    void deletePost(Long id);
    
    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);
    
    /**
     * 获取评论列表
     */
    List<DiscussionComment> getCommentsByPostId(Long postId);
    
    /**
     * 创建评论
     */
    void createComment(DiscussionComment comment);
    
    /**
     * 删除评论
     */
    void deleteComment(Long id);
    
    /**
     * 置顶/取消置顶帖子
     * @param postId 帖子ID
     * @param isPinned 是否置顶
     */
    void togglePinPost(Long postId, Boolean isPinned);
}
