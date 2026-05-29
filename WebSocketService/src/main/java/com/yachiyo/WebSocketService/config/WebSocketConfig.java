package com.yachiyo.WebSocketService.config;

import com.yachiyo.WebSocketService.handler.ChatWebSocketHandler;
import com.yachiyo.WebSocketService.handler.MoonSpaceWebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 开启传统 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    private final MoonSpaceWebSocketHandler moonSpaceWebSocketHandler;

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(MoonSpaceWebSocketHandler moonSpaceWebSocketHandler, ChatWebSocketHandler chatWebSocketHandler) {
        this.moonSpaceWebSocketHandler = moonSpaceWebSocketHandler;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册二进制处理器，允许所有跨域请求
        registry.addHandler(moonSpaceWebSocketHandler, "/ws/room")
                .setAllowedOrigins("*");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}