package com.yachiyo.QQBotService.client;

import com.yachiyo.QQBotService.dto.ai.GroupChatResp;
import com.yachiyo.QQBotService.dto.ai.GroupPromptReq;
import com.yachiyo.QQBotService.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", path = "/internal/chat")
public interface AIClient {
    @PostMapping("/group")
    Result<GroupChatResp> groupChat(@RequestBody GroupPromptReq groupPromptReq);
}
