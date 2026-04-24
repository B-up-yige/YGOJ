package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemsetProblem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long problemsetId;
    private Long problemId;
    private LocalDateTime addedAt;
    
    // 非数据库字段，用于显示题目名称
    @TableField(exist = false)
    private String problemTitle;
}
