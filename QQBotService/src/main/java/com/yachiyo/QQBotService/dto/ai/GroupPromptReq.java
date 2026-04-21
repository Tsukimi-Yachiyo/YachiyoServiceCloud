package com.yachiyo.QQBotService.dto.ai;

import com.yachiyo.QQBotService.enums.RequestReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupPromptReq {
    private String userPrompt; // 用户输入的消息内容
    private Long groupId; // 消息来源群ID
    private Long senderId; // 消息来源用户ID
    private RequestReason requestReason; // 请求原因
//    private CallType callType; // 调用类型
}
