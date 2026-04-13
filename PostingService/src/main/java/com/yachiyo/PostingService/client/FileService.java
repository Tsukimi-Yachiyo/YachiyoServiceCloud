package com.yachiyo.PostingService.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "file-service", path = "/internal/file")
public interface FileService {
}
