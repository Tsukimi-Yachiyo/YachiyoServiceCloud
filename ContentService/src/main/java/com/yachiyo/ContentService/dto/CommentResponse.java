package com.yachiyo.ContentService.dto;

import lombok.Data;

@Data
public class CommentResponse {

    private Long id;

    private Long userId;

    private String content;

    private Boolean isSelf;
}
