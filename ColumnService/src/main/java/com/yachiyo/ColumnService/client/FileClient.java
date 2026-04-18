package com.yachiyo.ColumnService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "file-service", path = "/internal/file")
public interface FileClient {

    @GetMapping("/getUrl")
    String getUrl(@RequestParam("url") String url,
                  @RequestParam("time") long time,
                  @RequestParam(value = "prefix", defaultValue = "save") String prefix) ;
}
