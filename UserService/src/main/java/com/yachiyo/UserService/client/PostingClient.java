package com.yachiyo.UserService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "content-service")
public interface PostingClient {

    /**
     * 推荐发布
     */
    @PostMapping("/internal/posting/recommend")
    void recommendPosting();
}
