package com.yachiyo.WebSocketService.tool;

import com.kaguya.metaverse.protocol.MoonMessageProtos;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class PositionTool {

    private final int BLOCK_SIZE = 128;

    // 是否在同一个区块
    public boolean isInSameBlock(float x1, float y1, float x2, float y2) {
        return x1 + BLOCK_SIZE >= x2 && y1 + BLOCK_SIZE >= y2 && x1 - BLOCK_SIZE <= x2 && y1 - BLOCK_SIZE <= y2;
    }

    // 获取此玩家所处区块的所有玩家
    public List<Long> getPlayersInSameBlock(float x, float y, Map<Long, MoonMessageProtos.PlayerPosition> playerPositions) {
        return playerPositions.entrySet().stream()
                .filter(entry -> isInSameBlock(x, y, entry.getValue().getX(), entry.getValue().getY()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
