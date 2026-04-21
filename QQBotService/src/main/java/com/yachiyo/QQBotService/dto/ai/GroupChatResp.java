package com.yachiyo.QQBotService.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatResp {
    private Long groupId; // 将要回复的群号
    private String answer; // 将要回复的消息
    private List<Long> atList; // 将要@的成员QQ号列表
    private Long replyMsgId; // 将要回复的消息ID
    private String nextRegex; // 下一次正则匹配的内容
    private Long regexExpireTimeSeconds; // 下一次正则匹配的过期时间，单位为秒
}
