package com.ygoj.discuss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.ygoj.discuss", "com.ygoj.common"})
public class DiscussMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscussMainApplication.class, args);
    }
}
