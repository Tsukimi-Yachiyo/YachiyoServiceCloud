package com.yachiyo.QQBotService.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatResp {
    private String answer; // 将要回复的消息
    private Long groupId; // 将要回复的群号
    private Integer replyMsgId; // 将要回复的消息ID
    private Long atId; // 将要@的成员QQ号
    private MessageMatcher messageMatcher; // 关注消息的匹配器
}
