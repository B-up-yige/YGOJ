package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ygoj.user.Userinfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Problemset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long authorId;
    @TableField(exist = false)
    private Userinfo author;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
