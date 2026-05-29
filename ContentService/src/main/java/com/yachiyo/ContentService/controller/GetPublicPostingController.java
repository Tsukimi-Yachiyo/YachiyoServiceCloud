package com.yachiyo.ContentService.controller;

import com.yachiyo.ContentService.dto.GetPostingResponse;
import com.yachiyo.ContentService.dto.PostEncapsulateResponse;
import com.yachiyo.ContentService.dto.PostStatsResponse;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.PostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/posting")
@RequiredArgsConstructor
@Validated
public class GetPublicPostingController {


    private final PostingService postingService;

    /**
     * 获取帖子
     */
    @GetMapping("/{id}")
    public Result<GetPostingResponse> getPosting(@PathVariable("id") Long postingId) {
        return postingService.getPosting(postingId);
    }

    /**
     * 获取帖子统计信息
     */
    @GetMapping("/stats")
    public Result<PostStatsResponse> getPostingStats(@RequestParam Long postingId) {
        return postingService.getPostingStats(postingId);
    }

    /**
     * 搜索帖子
     */
    @GetMapping("/search")
    public Result<List<PostEncapsulateResponse>> searchPosting(@RequestParam String keyword, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return postingService.searchPosting(keyword, pageNum, pageSize);
    }

    /**
     * 点赞的帖子
     */
    @PostMapping("/like")
    public Result<List<Long>> likePosting() {
        return postingService.getLikePosting();
    }

    /**
     * 收藏的帖子
     */
    @PostMapping("/collection")
    public Result<List<Long>> collectionPosting() {
        return postingService.getCollectionPosting();
    }

    /**
     * 获取帖子简述
     *
     * @param postingId 帖子id
     * @return 帖子简述
     */
    @GetMapping("/encapsulate")
    public Result<PostEncapsulateResponse> getPostingEncapsulate(@RequestParam Long postingId) {
        return postingService.getPostingEncapsulate(postingId);
    }

    /**
     * 获取一个用户的帖子
     */
    @GetMapping("/user")
    public Result<List<Long>> getUserPosting(@RequestParam Long userId) {
        return postingService.getUserPosting(userId);
    }
}
