package com.yachiyo.QQBotService.controller;

import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.GroupMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupMessageController {
    private final GroupMessageService groupMessageService;

    @GetMapping("/latest")
    public Result<GroupMessageResp> getLastest(
            @RequestParam("groupId") Long groupId
    ) {
        return groupMessageService.getLatest(groupId);
    }

    @GetMapping("/get")
    public Result<List<GroupMessageResp>> get(
            @RequestParam("groupId") Long groupId,
            @RequestParam("size") Integer size
    ) {
        return groupMessageService.get(groupId, size);
    }

    @PostMapping("/send")
    public Result<Integer> sendMessage(
            @RequestBody GroupMessageReq groupMessageReq
    ) {
        return groupMessageService.send(groupMessageReq);
    }
}
