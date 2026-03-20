package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Testcase {
    @TableId(type = IdType.AUTO)
    Long id;
    Long problemId;
    String inputFileId;
    String outputFileId;
    String inputFileName;
    String outputFileName;
}
