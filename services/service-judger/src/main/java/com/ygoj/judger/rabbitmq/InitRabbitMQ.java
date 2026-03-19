package com.ygoj.judger.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class InitRabbitMQ {
    public static void init() {
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("root");
            factory.setPassword("root");
            factory.setVirtualHost("/judgeQueue");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "judger_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            String QUEUE_NAME = "judger_queue";
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "ygoj");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}