package com.yachiyo.PostingService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yachiyo.PostingService.mapper")
public class PostingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostingServiceApplication.class, args);
    }
}
