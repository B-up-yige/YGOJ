package com.ygoj.judger.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 添加判题任务到消息队列
     *
     * @param sandboxExecuteRequest 沙箱执行请求
     */
    @Override
    public void addJudgeTask(SandboxExecuteRequest sandboxExecuteRequest) {
        log.info("添加判题任务到消息队列, recordId: {}", sandboxExecuteRequest.getRecordId());
        String json = JSON.toJSONString(sandboxExecuteRequest);
        rabbitTemplate.convertAndSend("judger_exchange", "ygoj", json);
        log.debug("判题任务已发送到RabbitMQ, recordId: {}", sandboxExecuteRequest.getRecordId());
    }
}
