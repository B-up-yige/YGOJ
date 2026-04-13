package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Problemset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long authorId;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
