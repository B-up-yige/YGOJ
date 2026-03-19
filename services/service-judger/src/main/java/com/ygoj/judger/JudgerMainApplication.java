package com.ygoj.judger;

import com.ygoj.judger.rabbitmq.InitRabbitMQ;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class JudgerMainApplication {
    public static void main(String[] args) {
        InitRabbitMQ.init();

        SpringApplication.run(JudgerMainApplication.class, args);
    }
}
