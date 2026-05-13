package com.yachiyo.QQBotService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageReq {
    private Long groupId;
    private String message; // 可以正常使用CQ码，但暂时用纯文本吧，构建CQ码比较麻烦
}
