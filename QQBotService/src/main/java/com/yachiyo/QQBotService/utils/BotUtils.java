package com.yachiyo.QQBotService.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class BotUtils {
    @Value("${bot.yachiyo.self-id}")
    @Getter
    private Long botId;

    @Autowired
    private BotContainer botContainer;

    @Autowired
    private CQCodeUtils cqCodeUtils;

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
}
