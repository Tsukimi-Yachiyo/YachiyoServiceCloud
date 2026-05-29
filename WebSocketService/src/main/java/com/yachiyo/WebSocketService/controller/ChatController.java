package com.yachiyo.WebSocketService.controller;

import com.yachiyo.WebSocketService.dto.MessageResponse;
import com.yachiyo.WebSocketService.dto.Result;
import com.yachiyo.WebSocketService.manager.ChatManager;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/api/v2/chat")
@AllArgsConstructor
@Validated
public class ChatController {

    private final ChatManager chatManager;

    /**
     * 获取好友列表
     */
    @GetMapping("/friend")
    public Result<List<Long>> friend() {
        Long userId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        return Result.success(chatManager.getFriend(userId));
    }

    /**
     * 获取聊天记录
     * @param friendId 好友ID
     * @param before 从该时间开始获取聊天记录
     * @return 聊天记录列表
     */
    @GetMapping("/messages")
    public Result<List<MessageResponse>> messages(@RequestParam Long friendId, @RequestParam LocalDateTime before) {
        Long userId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        return Result.success(chatManager.getMessages(userId, friendId, before));
    }
}
