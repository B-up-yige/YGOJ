package com.ygoj.filesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.ygoj.filesystem", "com.ygoj.common"})
public class FileSystemMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileSystemMainApplication.class, args);
    }
}
