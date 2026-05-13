package com.yachiyo.QQBotService.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.plugin.feature.Feature;
import com.yachiyo.QQBotService.plugin.feature.FeatureCategory;
import com.yachiyo.QQBotService.plugin.feature.FeatureRegistry;
import com.yachiyo.QQBotService.service.GameService;
import com.yachiyo.QQBotService.service.OneBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Shiro
@Component
public class GamePlugin {
    public static final String FORTUNE_CMD = "^\\s*/抽签\\s*$";

    @Autowired
    private GameService gameService;

    @Autowired
    private OneBotService oneBotService;

    public GamePlugin() {
        FeatureRegistry.register(FeatureCategory.GAME, new Feature("/抽签"));
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, cmd = FORTUNE_CMD)
    public int fortune(Bot bot, GroupMessageEvent event) {
        var result = gameService.fortune(event.getUserId()).getData();
        String prefix = result.isHistory() ? "今天已经抽过签了哦，结果是：" : "你的抽签结果是：";
        String msg = MsgUtils.builder()
                .reply(event.getMessageId())
                .text(prefix + result.getResult())
                .text("\n（结果纯属娱乐，切勿迷信~）")
                .face(66)
                .build();
        oneBotService.send(bot, event.getGroupId(), msg);
        return BotPlugin.MESSAGE_IGNORE;
    }
}
