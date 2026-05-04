package com.yachiyo.QQBotService.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class NapCatUtils {
    @Value("${bot.yachiyo.self-id}")
    @Getter
    private Long botId;

    @Autowired
    private BotContainer botContainer;

    public Bot getBot() {
        return getBot(botId);
    }

    public Bot getBot(Long botId) {
        Bot bot = botContainer.robots.get(botId);

        if (bot == null) {
            if (botContainer.robots.size() == 1) {
                bot = botContainer.robots.values().iterator().next();
                log.warn("未找到指定机器人：{}，已返回唯一实例：{}", botId, bot.getSelfId());
            } else {
                throw new RuntimeException("未找到指定机器人：" + botId);
            }
        } else {
            if (bot.getSession() == null || !bot.getSession().isOpen()) {
                throw new IllegalStateException("机器人连接已断开: " + botId);
            }
        }

        return bot;
    }

    public Collection<Long> getAllBotIds() {
        return botContainer.robots.keySet();
    }

    /**
     * 通过文件ID下载文件，NapCat会将文件下载到QQ的临时目录
     * @param fileId 文件ID
     * @return 本地文件路径，或下载失败时返回 null
     */
    public String downloadFile(String fileId) {
        try {
            Map<String, Object> body = Map.of(
                    "file_id", fileId
            );

            HttpResponse<JsonNode> response = Unirest.post("http://localhost:3000/get_file")
                    .header("Authorization", "Bearer " + getBot().getToken())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();

            var data = response.getBody().getObject().getJSONObject("data");
            return data.getString("url");
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
            return null;
        }
    }
}
