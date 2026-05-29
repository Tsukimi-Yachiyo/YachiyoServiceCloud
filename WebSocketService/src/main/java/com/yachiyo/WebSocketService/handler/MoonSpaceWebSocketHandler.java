package com.yachiyo.WebSocketService.handler;

import com.kaguya.metaverse.protocol.MoonMessageProtos;
import com.yachiyo.WebSocketService.manager.RoomManager;
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
import java.nio.ByteBuffer;
import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoonSpaceWebSocketHandler extends BinaryWebSocketHandler {

    private final RoomManager roomManager;

    private final RedisTemplate<String, Object> redisTemplate;

    private final QueryParamsTool queryParamsTool;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : "";

        Map<String, String> queryParams = queryParamsTool.parseQueryParams(query);

        String roomId = queryParams.get("roomId");
        String userIdStr = queryParams.get("userId");
        String token = queryParams.get("token");
        if (queryParamsTool.parseToken(userIdStr, token)) {
            session.close();
            return;
        }

        Long userId = Long.parseLong(userIdStr);

        session.getAttributes().put("roomId", roomId);
        session.getAttributes().put("userId", userId);

        roomManager.joinRoom(roomId, userId, session);
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) {
        try {
            ByteBuffer payload = message.getPayload();
            MoonMessageProtos.SpacePacket packet = MoonMessageProtos.SpacePacket.parseFrom(payload);

            String roomId = (String) session.getAttributes().get("roomId");
            Long userId = (Long) session.getAttributes().get("userId");

            processPacket(roomId, userId, packet);
        } catch (Exception e) {
            log.error("数据包处理异常", e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String roomId = (String) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");
        if (roomId != null && userId != null) {
            roomManager.leaveRoom(roomId, userId);
        }
    }

    private void processPacket(String roomId, Long userId, MoonMessageProtos.SpacePacket packet) {
        int opcode = packet.getOpcode();
        switch (opcode) {
            case 1: { // 玩家移动
                roomManager.updatePlayerPosition(roomId, userId, packet.getPayload());
                break;
            }
            case 2: { // 聊天
                // 处理聊天逻辑
                ;
            }
            case 3: { // 动画互动
                // 处理动画互动逻辑
                ;
                break;
            }
            case 4: { // 玩家互动
                // 处理玩家互动逻辑
                ;
                break;
            }
            default:
                log.warn("未知 opcode: {}", opcode);
                break;
        }
        // TODO: 可扩展 opcode = 2 (聊天), opcode = 3 (动画互动) 等
    }
}