package com.yachiyo.WebSocketService.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("chat_message")
@Data
public class ChatMessage {

    private Long sessionId;

    private Long userId;

    private String message;

    private LocalDateTime createTime;
}
