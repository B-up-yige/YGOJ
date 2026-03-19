package com.ygoj.judger.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import com.ygoj.judger.sandbox.impl.SandboxImplByDocker;
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

    @SneakyThrows
    @RabbitListener(queues = {"judger_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        //json对象化
        SandboxExecuteRequest sandboxExecuteRequest = JSON.parseObject(message, SandboxExecuteRequest.class);
        //处理判题
        SandboxImplByDocker sandbox = new SandboxImplByDocker();
        SandboxExecuteResponse result = sandbox.sandboxExecute(sandboxExecuteRequest);

        //处理结果
        //TODO 传输结果
        System.out.println(result);

        //确认消息
        channel.basicAck(deliveryTag, false);
    }
}
