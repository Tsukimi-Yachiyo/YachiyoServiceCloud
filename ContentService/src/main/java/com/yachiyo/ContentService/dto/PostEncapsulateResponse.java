package com.yachiyo.ContentService.dto;

import lombok.Data;

@Data
public class PostEncapsulateResponse{

    private Long postingId;

    private String title;

    private Long posterId;

    private String coverImage;
}
