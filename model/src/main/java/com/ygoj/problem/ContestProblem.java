package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ContestProblem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contestId;
    private Long problemId;
    private Integer problemOrder;
    private String problemLabel;
    
    // 非数据库字段，用于显示题目名称
    @TableField(exist = false)
    private String problemTitle;
}
