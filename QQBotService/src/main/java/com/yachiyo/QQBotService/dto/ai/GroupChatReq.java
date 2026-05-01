package com.yachiyo.QQBotService.dto.ai;

import com.yachiyo.QQBotService.enums.RequestReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatReq {
    private Long groupId; // 消息来源群ID
    private Integer messageId; // 消息ID
    private RequestReason requestReason; // 请求原因
//    private CallType callType; // 调用类型
}
