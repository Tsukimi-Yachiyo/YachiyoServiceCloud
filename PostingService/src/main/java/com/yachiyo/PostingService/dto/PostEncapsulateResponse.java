package com.yachiyo.PostingService.dto;

import lombok.Data;

@Data
public class PostEncapsulateResponse {

    private String title;

    private Long posterId;

    private String coverImage;
}
