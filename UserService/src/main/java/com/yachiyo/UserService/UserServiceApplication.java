package com.yachiyo.UserService;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient   // 启用服务发现（Nacos）
public class UserServiceApplication {
    void main(String[] args) {

        SpringApplication.run(UserServiceApplication.class, args);
    }
}
