package com.yachiyo.PostingService.dto;

import lombok.Data;

@Data
public class CommentResponse extends CommentRequest {

    private Long id;
    private Long userId;
    private String content;
}
