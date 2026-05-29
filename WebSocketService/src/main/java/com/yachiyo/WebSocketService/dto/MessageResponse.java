package com.yachiyo.WebSocketService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {

    private String message;

    private Long fromUserId;

    private LocalDateTime createTime;
}
