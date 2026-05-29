package com.yachiyo.ContentService.controller;

import com.yachiyo.ContentService.dto.CommentRequest;
import com.yachiyo.ContentService.dto.CommentResponse;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/posting")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    /**
     * 添加评论
     *
     * @param commentRequest 评论请求
     * @return 添加结果
     */
    @PutMapping("/comment")
    public Result<Boolean> addComment(@RequestBody @Valid CommentRequest commentRequest) {
        return commentService.addComment(commentRequest);
    }

    /**
     * 获取评论列表
     *
     * @param postingId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/comment")
    public Result<List<CommentResponse>> getCommentList(@RequestParam Long postingId) {
        return commentService.getCommentList(postingId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comment")
    public Result<Boolean> deleteComment(@RequestParam Long commentId) {
        return commentService.deleteComment(commentId);
    }
}
