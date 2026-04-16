package com.ygoj.problem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.ygoj.problem", "com.ygoj.common.security"})
public class ProblemMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProblemMainApplication.class, args);
    }
}
