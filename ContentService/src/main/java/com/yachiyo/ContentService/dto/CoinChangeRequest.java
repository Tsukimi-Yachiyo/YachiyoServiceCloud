package com.yachiyo.ContentService.dto;

import com.yachiyo.ContentService.enumeration.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
public class CoinChangeRequest {
    private Long fromUserId;

    private Long toUserId;

    private TradeType type;

    private Double amount;
}
