package com.ygoj.sandbox;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SandboxExecuteResponse {
    List<String> detail;
    String status;
    String error;
    String warning;
    Map<String, Object>compileInfo;
}
