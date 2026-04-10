package com.yachiyo.Gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.yachiyo.Gateway", "com.yachiyo.Utils", "com.yachiyo.Config"})
@SpringBootApplication
@EnableDiscoveryClient   // 启用服务发现（Nacos）
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}