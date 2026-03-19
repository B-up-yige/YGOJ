package com.ygoj.judger.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.service.JudgeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void addJudgeTask(SandboxExecuteRequest sandboxExecuteRequest) {
        String json = JSON.toJSONString(sandboxExecuteRequest);
        rabbitTemplate.convertAndSend("judger_exchange", "ygoj", json);
    }
}
