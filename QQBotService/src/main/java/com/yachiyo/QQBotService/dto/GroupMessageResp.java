package com.yachiyo.QQBotService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageResp {
    private String plainText;
    private Long senderId;
    private FormattedMessage formattedMessage;
}
