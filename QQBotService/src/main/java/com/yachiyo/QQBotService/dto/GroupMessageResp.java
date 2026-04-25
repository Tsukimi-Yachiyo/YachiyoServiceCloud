package com.yachiyo.QQBotService.dto;

import com.mikuac.shiro.dto.action.response.MsgResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageResp {
    private String plainText;
    private MsgResp.Sender sender;
    private FormattedMessage formattedMessage;
}
