package com.ygoj.discuss;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ygoj.user.Userinfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscussionComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long authorId;
    
    @TableField(exist = false)
    private Userinfo author;
    
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
