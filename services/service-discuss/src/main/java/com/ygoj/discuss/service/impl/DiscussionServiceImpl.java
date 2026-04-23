package com.ygoj.discuss.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.discuss.DiscussionPost;
import com.ygoj.discuss.DiscussionComment;
import com.ygoj.discuss.feign.UserFeignClient;
import com.ygoj.discuss.mapper.DiscussionPostMapper;
import com.ygoj.discuss.mapper.DiscussionCommentMapper;
import com.ygoj.discuss.service.DiscussionService;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DiscussionServiceImpl implements DiscussionService {
    
    @Autowired
    private DiscussionPostMapper postMapper;
    
    @Autowired
    private DiscussionCommentMapper commentMapper;
    
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public Page<DiscussionPost> getPostList(Long page, Long pageSize, String category) {
        try {
            log.debug("获取帖子列表, page: {}, pageSize: {}, category: {}", page, pageSize, category);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10L;
            }
            
            Page<DiscussionPost> postPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<DiscussionPost> wrapper = new LambdaQueryWrapper<>();
            
            // 如果指定了category，则筛选该板块
            if (category != null && !category.trim().isEmpty()) {
                wrapper.eq(DiscussionPost::getCategory, category.trim());
            }
            
            // 先显示置顶的，再按创建时间倒序
            wrapper.orderByDesc(DiscussionPost::getIsPinned)
                   .orderByDesc(DiscussionPost::getCreatedAt);
            
            Page<DiscussionPost> result = postMapper.selectPage(postPage, wrapper);
            
            // 填充作者信息
            for (DiscussionPost post : result.getRecords()) {
                fillAuthorInfo(post);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取帖子列表异常", e);
            throw new RuntimeException("获取帖子列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DiscussionPost getPostById(Long id) {
        try {
            log.debug("获取帖子详情, postId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
            
            DiscussionPost post = postMapper.selectById(id);
            if (post != null) {
                fillAuthorInfo(post);
            }
            
            return post;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取帖子详情异常, postId: {}", id, e);
            throw new RuntimeException("获取帖子详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createPost(DiscussionPost post) {
        try {
            log.info("创建帖子, title: {}, authorId: {}", post.getTitle(), post.getAuthorId());
            
            if (post == null) {
                throw new IllegalArgumentException("帖子信息不能为空");
            }
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("帖子标题不能为空");
            }
            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("帖子内容不能为空");
            }
            if (post.getAuthorId() == null) {
                throw new IllegalArgumentException("作者ID不能为空");
            }
            
            // 初始化字段
            if (post.getViewCount() == null) {
                post.setViewCount(0);
            }
            if (post.getCommentCount() == null) {
                post.setCommentCount(0);
            }
            if (post.getIsPinned() == null) {
                post.setIsPinned(false);
            }
            if (post.getIsLocked() == null) {
                post.setIsLocked(false);
            }
            
            postMapper.insert(post);
            log.info("创建帖子成功, postId: {}", post.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建帖子异常", e);
            throw new RuntimeException("创建帖子失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updatePost(DiscussionPost post) {
        try {
            log.info("更新帖子, postId: {}", post.getId());
            
            if (post == null || post.getId() == null) {
                throw new IllegalArgumentException("帖子信息或ID不能为空");
            }
            
            postMapper.updateById(post);
            log.info("更新帖子成功, postId: {}", post.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新帖子异常, postId: {}", post.getId(), e);
            throw new RuntimeException("更新帖子失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        try {
            log.info("删除帖子, postId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
            
            // 删除帖子的所有评论
            LambdaQueryWrapper<DiscussionComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DiscussionComment::getPostId, id);
            commentMapper.delete(wrapper);
            
            // 删除帖子
            postMapper.deleteById(id);
            log.info("删除帖子成功, postId: {}", id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除帖子异常, postId: {}", id, e);
            throw new RuntimeException("删除帖子失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        try {
            log.debug("增加浏览量, postId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
            
            DiscussionPost post = postMapper.selectById(id);
            if (post != null) {
                post.setViewCount(post.getViewCount() + 1);
                postMapper.updateById(post);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("增加浏览量异常, postId: {}", id, e);
            throw new RuntimeException("增加浏览量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DiscussionComment> getCommentsByPostId(Long postId) {
        try {
            log.debug("获取评论列表, postId: {}", postId);
            
            if (postId == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
            
            LambdaQueryWrapper<DiscussionComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DiscussionComment::getPostId, postId)
                   .orderByAsc(DiscussionComment::getCreatedAt);
            
            List<DiscussionComment> comments = commentMapper.selectList(wrapper);
            
            // 填充作者信息
            for (DiscussionComment comment : comments) {
                fillCommentAuthorInfo(comment);
            }
            
            return comments;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取评论列表异常, postId: {}", postId, e);
            throw new RuntimeException("获取评论列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createComment(DiscussionComment comment) {
        try {
            log.info("创建评论, postId: {}, authorId: {}", comment.getPostId(), comment.getAuthorId());
            
            if (comment == null) {
                throw new IllegalArgumentException("评论信息不能为空");
            }
            if (comment.getPostId() == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("评论内容不能为空");
            }
            if (comment.getAuthorId() == null) {
                throw new IllegalArgumentException("作者ID不能为空");
            }
            
            commentMapper.insert(comment);
            
            // 更新帖子的评论数
            DiscussionPost post = postMapper.selectById(comment.getPostId());
            if (post != null) {
                post.setCommentCount(post.getCommentCount() + 1);
                postMapper.updateById(post);
            }
            
            log.info("创建评论成功, commentId: {}", comment.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建评论异常", e);
            throw new RuntimeException("创建评论失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        try {
            log.info("删除评论, commentId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("评论ID不能为空");
            }
            
            DiscussionComment comment = commentMapper.selectById(id);
            if (comment != null) {
                // 删除评论
                commentMapper.deleteById(id);
                
                // 更新帖子的评论数
                DiscussionPost post = postMapper.selectById(comment.getPostId());
                if (post != null && post.getCommentCount() > 0) {
                    post.setCommentCount(post.getCommentCount() - 1);
                    postMapper.updateById(post);
                }
            }
            
            log.info("删除评论成功, commentId: {}", id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除评论异常, commentId: {}", id, e);
            throw new RuntimeException("删除评论失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 填充帖子作者信息
     */
    private void fillAuthorInfo(DiscussionPost post) {
        if (post.getAuthorId() != null) {
            try {
                Result result = userFeignClient.userinfo(post.getAuthorId());
                if (result != null && result.getData() != null) {
                    Userinfo author = JSON.parseObject(JSON.toJSONString(result.getData()), Userinfo.class);
                    post.setAuthor(author);
                }
            } catch (Exception e) {
                log.warn("获取帖子作者信息失败, postId: {}, authorId: {}", post.getId(), post.getAuthorId(), e);
            }
        }
    }
    
    /**
     * 填充评论作者信息
     */
    private void fillCommentAuthorInfo(DiscussionComment comment) {
        if (comment.getAuthorId() != null) {
            try {
                Result result = userFeignClient.userinfo(comment.getAuthorId());
                if (result != null && result.getData() != null) {
                    Userinfo author = JSON.parseObject(JSON.toJSONString(result.getData()), Userinfo.class);
                    comment.setAuthor(author);
                }
            } catch (Exception e) {
                log.warn("获取评论作者信息失败, commentId: {}, authorId: {}", comment.getId(), comment.getAuthorId(), e);
            }
        }
    }
}
