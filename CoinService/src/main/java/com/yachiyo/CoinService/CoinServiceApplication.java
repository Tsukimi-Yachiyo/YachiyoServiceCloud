package com.yachiyo.CoinService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.yachiyo.CoinService.client")
@EnableDiscoveryClient   // 开启服务注册发现（新版本可省略，但建议显式声明）
@MapperScan("com.yachiyo.CoinService.mapper")
public class CoinServiceApplication {
    void main(String[] args) {
        SpringApplication.run(CoinServiceApplication.class, args);
    }
}
