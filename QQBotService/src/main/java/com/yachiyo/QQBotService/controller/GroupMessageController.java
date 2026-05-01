package com.yachiyo.QQBotService.controller;

import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.OneBotService;
import com.yachiyo.QQBotService.utils.BotUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupMessageController {
    private final BotUtils botUtils;
    private final OneBotService oneBotService;

    @GetMapping("/latest")
    public Result<GroupMessageResp> getLastest(
            @RequestParam("groupId") Long groupId
    ) {
        return oneBotService.getLatest(botUtils.getBot(), groupId);
    }

    @GetMapping("/get")
    public Result<List<GroupMessageResp>> get(
            @RequestParam("groupId") Long groupId,
            @RequestParam("size") Integer size
    ) {
        return oneBotService.get(botUtils.getBot(), groupId, size);
    }

    @GetMapping("/get")
    public Result<GroupMessageResp> get(
            @RequestParam("messageId") Integer messageId
    ) {
        return oneBotService.get(botUtils.getBot(), messageId);
    }

    /**
     * 发送消息
     * @param groupMessageReq 消息请求体
     * @return 发送后的消息ID
     */
    @PostMapping("/send")
    public Result<Integer> sendMessage(
            @RequestBody GroupMessageReq groupMessageReq
    ) {
        return oneBotService.send(botUtils.getBot(), groupMessageReq);
    }
}
