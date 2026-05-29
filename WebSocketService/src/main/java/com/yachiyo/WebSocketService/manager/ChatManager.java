package com.yachiyo.WebSocketService.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kaguya.metaverse.protocol.ChatMessageProtos;
import com.yachiyo.WebSocketService.client.UserClient;
import com.yachiyo.WebSocketService.dto.MessageResponse;
import com.yachiyo.WebSocketService.entity.ChatMessage;
import com.yachiyo.WebSocketService.entity.ChatSession;
import com.yachiyo.WebSocketService.mapper.ChatMessageMapper;
import com.yachiyo.WebSocketService.mapper.ChatSessionMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@AllArgsConstructor
public class ChatManager {

    private final ChatSessionMapper chatSessionMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final UserClient userClient;

    private final RedisTemplate<String, Object> redisTemplate;

    private final String REDIS_SESSION_KEY = "room:chat_session:";

    private final ConcurrentHashMap<Long, WebSocketSession> chatSessions = new ConcurrentHashMap<>();

    /**
     * 向特定用户发送定向消息
     */
    @SuppressWarnings("all")
    public void sendToUser(Long targetUserId, ChatMessageProtos.Chat packet) {
        WebSocketSession session = chatSessions.get(targetUserId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new BinaryMessage(packet.toByteArray()));
            } catch (IOException e) {
                log.error("定向发送失败 (User: {}): {}", targetUserId, e.getMessage());
            }
        } else {
            log.warn("定向发送失败：用户 [{}] 不在线", targetUserId);
        }
    }

    public void startChat(Long userId, WebSocketSession session) {
        chatSessions.put(userId, session);
    }

    public void endChat(Long userId) {
        chatSessions.remove(userId);
    }

    public void sendMessage(Long userId, BinaryMessage message) throws InvalidProtocolBufferException {
        // 解析消息
        ByteBuffer payload = message.getPayload();
        ChatMessageProtos.Chat chat = ChatMessageProtos.Chat.parseFrom(payload);
        Long toUserId = chat.getToId();

        // 验证是否是好友
        if (!userClient.isFriend(userId, toUserId).getData()) {
            log.warn("用户 [{}] 不是好友 [{}]", userId, toUserId);
            return;
        }

        // 发送消息给目标用户
        try {
            sendToUser(toUserId, chat);
        } catch (Exception e) {
            log.error("发送消息失败 (User: {}): {}", toUserId, e.getMessage());
        }

        // 持久化消息到数据库
        Long sessionId = getSessionId(userId, toUserId);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setUserId(toUserId);
        chatMessage.setMessage(chat.getMessage());
        chatMessage.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(chatMessage);

        String hashKey = REDIS_SESSION_KEY + sessionId;
        String field = "history";
        redisTemplate.opsForHash().delete(hashKey, field);
    }

    public List<MessageResponse> getMessages(Long userId, Long toUserId, LocalDateTime before) {
        Long sessionId = getSessionId(userId, toUserId);
        return getChatHistory(sessionId, before);
    }

    public List<Long> getFriend(Long userId) {
        return userClient.friends(userId).getData();
    }

    private Long getSessionId(Long userId, Long toUserId) {
        Long firstId = getFirstIdAndSecondId(userId, toUserId)[0];
        Long secondId = getFirstIdAndSecondId(userId, toUserId)[1];
        Long sessionId = (Long) redisTemplate.opsForHash().get(REDIS_SESSION_KEY + firstId + ":" + secondId, "sessionId");
        if (sessionId == null) {
            ChatSession chatSession = chatSessionMapper.selectOne(new QueryWrapper<ChatSession>()
                    .eq("first_user_id", firstId)
                    .eq("second_user_id", secondId)
            );
            if (chatSession != null) {
                sessionId = chatSession.getId();
            }else  {
                ChatSession newChatSession = new ChatSession();
                newChatSession.setFirstUserId(firstId);
                newChatSession.setSecondUserId(secondId);
                chatSessionMapper.insert(newChatSession);
                sessionId = newChatSession.getId();
            }
            // 缓存会话ID
            redisTemplate.opsForHash().put(REDIS_SESSION_KEY + firstId + ":" + secondId, "sessionId", sessionId);
            redisTemplate.opsForHash().put(REDIS_SESSION_KEY + sessionId, "firstUserId", userId);
            redisTemplate.opsForHash().put(REDIS_SESSION_KEY + sessionId, "secondUserId", toUserId);
            redisTemplate.expire(REDIS_SESSION_KEY + firstId + ":" + secondId, 60 * 5, TimeUnit.SECONDS);
        }
        return sessionId;
    }

    @SuppressWarnings("unchecked")
    public List<MessageResponse> getChatHistory(Long sessionId, LocalDateTime before) {
        String hashKey = REDIS_SESSION_KEY + sessionId;
        String field = "history:" + (before != null ? before.toString() : "latest");

        // 1. 尝试从缓存获取
        Object cached = redisTemplate.opsForHash().get(hashKey, field);
        if (cached instanceof List<?>) {
            return (List<MessageResponse>) cached;
        }

        // 2. 缓存未命中，查询数据库
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);

        // 如果 before 为 null，表示第一次拉取，取最新一页
        if (before != null) {
            queryWrapper.lt("create_time", before);   // 早于 before 的消息
        }
        queryWrapper.orderByDesc("create_time", "id")
                .last("LIMIT " + 10);

        List<ChatMessage> dbMessages = chatMessageMapper.selectList(queryWrapper);
        if (dbMessages == null) {
            dbMessages = Collections.emptyList();
        }

        // 转换成 DTO
        List<MessageResponse> messages = dbMessages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 3. 写入缓存，并设置过期时间
        redisTemplate.opsForHash().put(hashKey, field, messages);
        redisTemplate.expire(hashKey, 5, TimeUnit.MINUTES);

        return messages;
    }

    private MessageResponse convertToResponse(ChatMessage entity) {
        MessageResponse resp = new MessageResponse();
        resp.setMessage(entity.getMessage());
        resp.setFromUserId(entity.getUserId());
        resp.setCreateTime(entity.getCreateTime());
        return resp;
    }

    private Long[] getFirstIdAndSecondId(Long userId, Long toUserId) {
        return new Long[]{Math.min(userId, toUserId), Math.max(userId, toUserId)};
    }
}
