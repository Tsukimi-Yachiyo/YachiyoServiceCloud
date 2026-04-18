package com.yachiyo.PostingService.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostEncapsulateResponse implements Serializable {

    private String title;

    private Long posterId;

    private String coverImage;
}
