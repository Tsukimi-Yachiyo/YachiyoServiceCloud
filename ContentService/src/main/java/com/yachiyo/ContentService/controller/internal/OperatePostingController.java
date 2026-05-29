package com.yachiyo.ContentService.controller.internal;

import com.yachiyo.ContentService.dto.PostingQueryRequest;
import com.yachiyo.ContentService.dto.ReviewRequest;
import com.yachiyo.ContentService.entity.Posting;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/posting")
@RequiredArgsConstructor
@Validated
public class OperatePostingController {

    private final AdminService adminService;

    @PostMapping("/review")
    public Result<Boolean> reviewPosting(@RequestBody @Valid ReviewRequest request) {
        return adminService.reviewPosting(request);
    }

    @PostMapping("/query-postings")
    public Result<List<Posting>> queryPostings(@RequestBody @Valid PostingQueryRequest request) {
        return adminService.queryPostings(request);
    }
}
