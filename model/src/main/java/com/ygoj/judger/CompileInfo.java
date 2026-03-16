package com.ygoj.judger;

import lombok.Data;

@Data
public class CompileInfo {
    Long time;
    Long memory;
    String stdout = null;
    String stderr = null;
    Long code;
}
