package com.ygoj.judger.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import com.ygoj.judger.sandbox.impl.SandboxImpl;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    Sandbox sandbox;

    @SneakyThrows
    @RabbitListener(queues = {"judger_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        //json对象化
        SandboxExecuteRequest sandboxExecuteRequest = JSON.parseObject(message, SandboxExecuteRequest.class);
        //处理判题
        SandboxExecuteResponse result = sandbox.sandboxExecute(sandboxExecuteRequest);

        //处理结果
        String json = JSON.toJSONString(result);
        rabbitTemplate.convertAndSend("judgeResult_exchange", "ygoj", json);

        //确认消息
        channel.basicAck(deliveryTag, false);
    }
}
