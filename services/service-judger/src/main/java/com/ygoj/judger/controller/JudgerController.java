package com.ygoj.judger.controller;

import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JudgerController {
    @Autowired
    JudgeService judgeService;

    /**
     * 判题接口(内部服务调用 - 需要登录)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/judge")
    public Result judge(@RequestBody SandboxExecuteRequest sandboxExecuteRequest) {
        try {
            log.info("收到判题请求, recordId: {}, language: {}", 
                    sandboxExecuteRequest.getRecordId(), sandboxExecuteRequest.getLanguage());
            
            // 参数校验
            if (sandboxExecuteRequest == null) {
                return Result.error(400, "判题请求不能为空");
            }
            if (sandboxExecuteRequest.getRecordId() == null) {
                return Result.error(400, "记录ID不能为空");
            }
            if (sandboxExecuteRequest.getCode() == null || sandboxExecuteRequest.getCode().trim().isEmpty()) {
                return Result.error(400, "代码不能为空");
            }
            if (sandboxExecuteRequest.getLanguage() == null || sandboxExecuteRequest.getLanguage().trim().isEmpty()) {
                return Result.error(400, "编程语言不能为空");
            }
            
            judgeService.addJudgeTask(sandboxExecuteRequest);
            log.info("判题任务已添加到消息队列, recordId: {}", sandboxExecuteRequest.getRecordId());
            return Result.success();
        } catch (Exception e) {
            log.error("判题请求处理失败", e);
            return Result.error(500, "判题请求处理失败: " + e.getMessage());
        }
    }
}

