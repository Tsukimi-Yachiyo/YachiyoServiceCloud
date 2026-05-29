package com.yachiyo.WebSocketService.manager;

import com.google.protobuf.ByteString;
import com.kaguya.metaverse.protocol.MoonMessageProtos;
import com.yachiyo.WebSocketService.entity.PlayerPosition;
import com.yachiyo.WebSocketService.mapper.PlayerPositionMapper;
import com.yachiyo.WebSocketService.tool.PositionTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@EnableScheduling
@AllArgsConstructor
public class RoomManager {

    // 针对百人规模，单机内存足以支撑。
    // 房间映射: RoomID -> (UserID -> Session)
    private final ConcurrentHashMap<String, Map<Long, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 全局映射: UserID -> Session (用于跨房间寻找用户)
    private final ConcurrentHashMap<Long, WebSocketSession> globalSessions = new ConcurrentHashMap<>();

    // 房间状态映射 (本地缓存): RoomID -> (UserID -> PlayerPosition)
    private final ConcurrentHashMap<String, Map<Long, MoonMessageProtos.PlayerPosition>> roomStates = new ConcurrentHashMap<>();

    private final AtomicLong currentTick = new AtomicLong(0);

    // 引入 RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;

    // 引入 PlayerPositionMapper
    private final PlayerPositionMapper playerPositionMapper;

    // 引入 PositionTool
    private final PositionTool positionTool;

    // Redis Key 前缀
    private static final String REDIS_ROOM_STATE_PREFIX = "room:state:";

    /**
     * 向指定房间内的所有在线玩家广播 SpacePacket 包
     * 在单机架构下，直接遍历本地 Session 即可，效率极高。
     */
    public void broadcastToRoom(String roomId, MoonMessageProtos.SpacePacket packet) {
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;

        byte[] data = packet.toByteArray();
        BinaryMessage binaryMessage = new BinaryMessage(data);

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    // JDK 21 虚拟线程环境下，即使发生网络拥塞，也只会挂起轻量级虚拟线程
                    session.sendMessage(binaryMessage);
                } catch (IOException e) {
                    log.error("广播消息失败 (Room: {}): {}", roomId, e.getMessage());
                }
            }
        });
    }

    /**
     * 向特定用户发送定向消息
     */
    @SuppressWarnings("all")
    public void sendToUser(Long targetUserId, MoonMessageProtos.SpacePacket packet) {
        WebSocketSession session = globalSessions.get(targetUserId);
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

    // 玩家加入房间
    public void joinRoom(String roomId, Long userId, WebSocketSession session) {
        // 1. 注册映射
        roomSessions.computeIfAbsent(roomId, _ -> new ConcurrentHashMap<>()).put(userId, session);
        globalSessions.put(userId, session);
        log.info("玩家 [{}] 加入了房间 [{}]", userId, roomId);

        // 2. 构造并发送上线广播包 (OpCode = 4)
        MoonMessageProtos.PlayerJoinLeave joinNotice = MoonMessageProtos.PlayerJoinLeave.newBuilder()
                .setUserId(userId)
                .setIsJoin(true)
                .build();

        MoonMessageProtos.SpacePacket broadcastPacket = MoonMessageProtos.SpacePacket.newBuilder()
                .setOpcode(4)
                .setPayload(joinNotice.toByteString())
                .build();

        broadcastToRoom(roomId, broadcastPacket);

        // 3. 从数据库中获取玩家坐标
        float x = 0;
        float y = 0;
        PlayerPosition playerPosition = playerPositionMapper.selectById(userId);
        if (playerPosition != null) {
            x = playerPosition.getX();
            y = playerPosition.getY();
        }
        
        // 4. 构造并发送玩家位置广播包 (OpCode = 5)
        MoonMessageProtos.PlayerPosition position = MoonMessageProtos.PlayerPosition.newBuilder()
                .setUserId(userId)
                .setX(x)
                .setY(y)
                .build();
        
        MoonMessageProtos.SpacePacket positionPacket = MoonMessageProtos.SpacePacket.newBuilder()
                .setOpcode(5)
                .setPayload(position.toByteString())
                .build();

        // 5. 向区块内的玩家发送位置广播
        Map<Long, MoonMessageProtos.PlayerPosition> states = roomStates.get(roomId);
        states.put(userId, position);
        for  (Long id : positionTool.getPlayersInSameBlock(x, y, states)) {
            sendToUser(id, positionPacket);
            // 6. 更新 Redis 状态
            try {
                redisTemplate.opsForHash().put(REDIS_ROOM_STATE_PREFIX + roomId, id.toString(), position);
                redisTemplate.expire(REDIS_ROOM_STATE_PREFIX + roomId, 60, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("更新 Redis 状态失败: User [{}], Room [{}]", id, roomId, e);
            }
            // 将其他玩家的位置发送给新玩家 (OpCode = 5)
            MoonMessageProtos.SpacePacket otherPositionPacket = MoonMessageProtos.SpacePacket.newBuilder()
                    .setOpcode(5)
                    .setPayload(states.get(id).toByteString())
                    .build();
            sendToUser(userId, otherPositionPacket);
        }
    }

    // 玩家离开房间
    public void leaveRoom(String roomId, Long userId) {
        // 1. 移除本地映射
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(userId);
        }
        globalSessions.remove(userId);

        Map<Long, MoonMessageProtos.PlayerPosition> states = roomStates.get(roomId);
        float x;
        float y;
        if (states != null) {
            MoonMessageProtos.PlayerPosition position = states.get(userId);
            if (position != null) {
                x = position.getX();
                y = position.getY();
                PlayerPosition playerPosition = new PlayerPosition();
                playerPosition.setId(userId);
                playerPosition.setX(x);
                playerPosition.setY(y);
                if (!(playerPositionMapper.updateById(playerPosition) > 0)) {
                    playerPositionMapper.insert(playerPosition);
                }
            }
            states.remove(userId);
        }

        // 2. 从 Redis 状态中移除
        try {
            redisTemplate.opsForHash().delete(REDIS_ROOM_STATE_PREFIX + roomId, userId.toString());
        } catch (Exception e) {
            log.error("从 Redis 移除玩家坐标失败: User [{}], Room [{}]", userId, roomId, e);
        }

        log.info("玩家 [{}] 离开了房间 [{}]", userId, roomId);

        // 3. 构造并发送下线广播包 (OpCode = 4)
        MoonMessageProtos.PlayerJoinLeave leaveNotice = MoonMessageProtos.PlayerJoinLeave.newBuilder()
                .setUserId(userId)
                .setIsJoin(false)
                .build();

        MoonMessageProtos.SpacePacket packet = MoonMessageProtos.SpacePacket.newBuilder()
                .setOpcode(4)
                .setPayload(leaveNotice.toByteString())
                .build();

        broadcastToRoom(roomId, packet);
    }

    // 更新玩家坐标
    public void updatePlayerPosition(String roomId, Long userId, ByteString payload) {
        try {
            MoonMessageProtos.PlayerTransform transform = MoonMessageProtos.PlayerTransform.parseFrom(payload);

            // 更新玩家位置, 并广播
            MoonMessageProtos.PlayerPosition oldPosition = roomStates.get(roomId).get(userId);
            if (oldPosition != null) {
                MoonMessageProtos.PlayerPosition position = MoonMessageProtos.PlayerPosition.newBuilder()
                        .setUserId(userId)
                        .setX(oldPosition.getX() + transform.getX())
                        .setY(oldPosition.getY() + transform.getY())
                        .build();
                for (Long id : positionTool.getPlayersInSameBlock(oldPosition.getX(), oldPosition.getY(), roomStates.get(roomId))) {
                    sendToUser(id, MoonMessageProtos.SpacePacket.newBuilder()
                            .setOpcode(1)
                            .setPayload(transform.toByteString())
                            .build());
                }
                roomStates.get(roomId).put(userId, position);
            }else {
                log.warn("玩家 [{}] 未在房间 [{}] 中", userId, roomId);
                return;
            }

            // 同步写入 Redis (为未来的分布式做准备)
            try {
                redisTemplate.opsForHash().put(REDIS_ROOM_STATE_PREFIX + roomId, userId.toString(), transform.toByteArray());
                redisTemplate.expire(REDIS_ROOM_STATE_PREFIX + roomId, 60, TimeUnit.MINUTES);
            } catch (Exception e) {
                // Redis 写入失败不应影响主游戏逻辑
                log.warn("向 Redis 写入玩家坐标失败", e);
            }

        } catch (Exception e) {
            log.error("Protobuf 解析移动数据失败", e);
        }
    }

    // 50ms 帧同步广播
    @Scheduled(fixedRate = 50)
    public void broadcastRoomStates() {
        long tick = currentTick.incrementAndGet();

        roomStates.forEach((roomId, players) -> {
            if (players.isEmpty()) return;

            MoonMessageProtos.RoomSyncFrame frame = MoonMessageProtos.RoomSyncFrame.newBuilder()
                    .setServerTick(tick)
                    .addAllPlayers(players.values())
                    .build();

            MoonMessageProtos.SpacePacket packet = MoonMessageProtos.SpacePacket.newBuilder()
                    .setOpcode(100)
                    .setPayload(frame.toByteString())
                    .build();

            broadcastToRoom(roomId, packet);
        });
    }
}