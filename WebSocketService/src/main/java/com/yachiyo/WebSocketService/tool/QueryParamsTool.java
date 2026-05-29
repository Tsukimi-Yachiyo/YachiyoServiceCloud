package com.yachiyo.WebSocketService.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QueryParamsTool {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 解析查询参数
    // roomId=123&userId=456
    public Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) map.put(entry[0], entry[1]);
        }
        return map;
    }

    // 解析token
    public boolean parseToken(String userIdStr, String token) {
        if (userIdStr == null || token == null || token.isEmpty()) {
            log.error("连接参数错误");
            return true;
        }

        // 验证token
        String tokenFromRedis = (String) redisTemplate.opsForHash()
                .get("user:" + userIdStr, "ws_token"); // 或使用 reactive 链

        if (tokenFromRedis == null || !tokenFromRedis.equals(token)) {
            log.error("token验证失败");
            return true;
        }
        return false;
    }
}
