package com.ygoj.record;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录信息DTO，包含题目名称和用户昵称
 */
@Data
public class RecordWithInfo {
    Long id;
    Long userId;
    String userName;  // 用户昵称
    Long problemId;
    String problemTitle;  // 题目标题
    Long contestId;
    String code;
    String status;
    String language;
    Long compileTime;
    Long compileMemory;
    String compileStdout;
    String compileStderr;
    LocalDateTime submitTime;
}
