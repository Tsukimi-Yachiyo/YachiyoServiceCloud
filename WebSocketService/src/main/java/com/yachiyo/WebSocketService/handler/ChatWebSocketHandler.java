package com.yachiyo.WebSocketService.handler;

import com.yachiyo.WebSocketService.manager.ChatManager;
import com.yachiyo.WebSocketService.tool.QueryParamsTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends BinaryWebSocketHandler {

    private final ChatManager chatManager;

    private final RedisTemplate<String, Object> redisTemplate;

    private final QueryParamsTool queryParamsTool;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : "";

        Map<String, String> queryParams = queryParamsTool.parseQueryParams(query);

        String userId = queryParams.get("userId");
        String token = queryParams.get("token");
        if (queryParamsTool.parseToken(userId, token)) {
            session.close();
            return;
        }
        Long userIdLong = Long.parseLong(userId);
        session.getAttributes().put("userId", userIdLong);
        chatManager.startChat(userIdLong, session);
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {
        Long userId = (Long) session.getAttributes().get("userId");
        chatManager.sendMessage(userId, message);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            chatManager.endChat(userId);
        }
    }
}
