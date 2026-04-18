package com.yachiyo.PostingService.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentResponse extends CommentRequest implements Serializable {

    private Long id;
    private Long userId;
}
