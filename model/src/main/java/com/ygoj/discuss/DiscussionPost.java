package com.ygoj.discuss;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ygoj.user.Userinfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscussionPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    
    @TableField(exist = false)
    private Userinfo author;
    
    private String category; // 板块分类
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isPinned;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
