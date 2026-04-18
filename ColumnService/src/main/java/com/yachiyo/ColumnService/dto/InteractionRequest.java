package com.yachiyo.ColumnService.dto;

import com.yachiyo.PostingService.enumeration.InteractionAction;
import lombok.Data;

@Data
public class InteractionRequest {

    Long postId;
    InteractionAction action;
}
