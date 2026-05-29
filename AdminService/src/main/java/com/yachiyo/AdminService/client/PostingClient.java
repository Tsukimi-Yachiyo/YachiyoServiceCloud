package com.yachiyo.AdminService.client;

import com.yachiyo.AdminService.dto.PostingQueryRequest;
import com.yachiyo.AdminService.dto.PostingResponse;
import com.yachiyo.AdminService.dto.ReviewRequest;
import com.yachiyo.AdminService.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "content-service", path = "/internal/posting")
public interface PostingClient {

    @PostMapping("/review")
    Result<Boolean> reviewPosting(@RequestBody ReviewRequest request);

    @PostMapping("/query-postings")
    Result<List<PostingResponse>> queryPostings(@RequestBody PostingQueryRequest request);
}
