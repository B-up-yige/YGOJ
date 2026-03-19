package com.ygoj.sandbox;

import com.ygoj.judger.rabbitmq.InitRabbitMQ;
import org.junit.jupiter.api.Test;

public class RabbitMqTest {
    @Test
    public void test() {
        InitRabbitMQ.init();
    }
}
