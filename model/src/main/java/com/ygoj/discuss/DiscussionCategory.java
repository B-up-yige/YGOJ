package com.ygoj.discuss;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscussionCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code; // 板块代码(唯一标识)
    private String name; // 板块名称
    private String description; // 板块描述
    private Integer sortOrder; // 排序顺序
    private Boolean isActive; // 是否启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
