package com.ygoj.judger;

import lombok.Data;

@Data
public class ExecuteDetail {
    Long time;
    Long memory;
    Long code;
    String stderr = null;
    String status;
}
