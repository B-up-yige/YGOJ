package com.ygoj.judger.sandbox;

import com.ygoj.judger.CompileInfo;
import com.ygoj.judger.ExecuteDetail;
import lombok.Data;

import java.util.List;

@Data
public class SandboxExecuteResponse {
    List<ExecuteDetail> detail;
    String status;
    CompileInfo compileInfo;
}
