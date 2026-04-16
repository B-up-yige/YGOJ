package com.ygoj.problem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(excludeName = {
    "org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration"
})
@EnableFeignClients
@EnableDiscoveryClient
public class ProblemMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProblemMainApplication.class, args);
    }
}
