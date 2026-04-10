package com.ygoj.judger.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener {
    private static final int MAX_RETRY_COUNT = 3;
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Autowired
    Sandbox sandbox;

    @RabbitListener(queues = {"judger_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                log.info("开始处理判题任务，尝试次数: {}/{}", retryCount + 1, MAX_RETRY_COUNT);
                
                //json对象化
                SandboxExecuteRequest sandboxExecuteRequest = JSON.parseObject(message, SandboxExecuteRequest.class);
                
                //处理判题
                SandboxExecuteResponse result = sandbox.sandboxExecute(sandboxExecuteRequest);

                //处理结果
                String json = JSON.toJSONString(result);
                rabbitTemplate.convertAndSend("judgeResult_exchange", "ygoj", json);

                //确认消息
                channel.basicAck(deliveryTag, false);
                log.info("判题任务处理成功，recordId: {}", sandboxExecuteRequest.getRecordId());
                return;
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.error("判题任务处理失败，尝试次数: {}/{}, 错误信息: {}", 
                    retryCount, MAX_RETRY_COUNT, e.getMessage(), e);
                
                if (retryCount >= MAX_RETRY_COUNT) {
                    // 达到最大重试次数，拒绝消息并发送到死信队列
                    log.error("判题任务达到最大重试次数，将消息发送到死信队列");
                    try {
                        // 构造错误响应
                        SandboxExecuteResponse errorResponse = new SandboxExecuteResponse();
                        errorResponse.setStatus("SE"); // System Error
                        errorResponse.setRecordId(JSON.parseObject(message).getLong("recordId"));
                        String errorJson = JSON.toJSONString(errorResponse);
                        rabbitTemplate.convertAndSend("judgeResult_exchange", "ygoj", errorJson);
                        
                        // 拒绝消息，不重新入队
                        channel.basicNack(deliveryTag, false, false);
                    } catch (Exception ex) {
                        log.error("发送错误响应失败", ex);
                        throw new AmqpRejectAndDontRequeueException("判题失败且无法发送错误响应", ex);
                    }
                } else {
                    // 等待一段时间后重试
                    try {
                        Thread.sleep(1000L * retryCount); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AmqpRejectAndDontRequeueException("重试被中断", ie);
                    }
                }
            }
        }
    }
}
