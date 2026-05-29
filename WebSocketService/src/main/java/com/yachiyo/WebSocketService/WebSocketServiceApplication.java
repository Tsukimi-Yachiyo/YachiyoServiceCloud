package com.yachiyo.WebSocketService;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.yachiyo.WebSocketService.mapper")
@EnableFeignClients(basePackages = "com.yachiyo.WebSocketService.client")
@EnableDiscoveryClient
public class WebSocketServiceApplication {
    public static void main(String[] args) {

        SpringApplication.run(WebSocketServiceApplication.class, args);
    }
}
