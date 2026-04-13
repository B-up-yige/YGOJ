package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
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
}
