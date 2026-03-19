package com.ygoj.judger.controller;

import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JudgerController {
    @Autowired
    JudgeService judgeService;

    @PostMapping("/judge")
    public Result judge(@RequestBody SandboxExecuteRequest sandboxExecuteRequest) {
        judgeService.addJudgeTask(sandboxExecuteRequest);
        return Result.success();
    }
}
