package com.yachiyo.UserService.client;

import com.yachiyo.UserService.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service") // 直接写服务名，不走网关
public interface UserDetailClient {

    @GetMapping("/internal/user/detail/init/{id}")
    Result<Boolean> initUserDetail(@PathVariable Long id);

    @GetMapping("/internal/user/detail/login/{id}")
    Result<Boolean> login(@PathVariable Long id);
}
