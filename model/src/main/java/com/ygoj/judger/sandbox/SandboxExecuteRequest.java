package com.ygoj.judger.sandbox;

import lombok.Data;

import java.util.List;

@Data
public class SandboxExecuteRequest {
    List<String> inputList;
    List<String> outputList;
    String code;
    String language;
    String compileCommand;
    String runCommand;
    String fileName;
    Long timeLimit;
    Long memoryLimit;
    String image;
    Long recordId;
}