package com.yachiyo.QQBotService.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import org.springframework.stereotype.Component;

@Shiro
@Component
public class GamePlugin {
    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, startWith = "/抽签")
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        bot.sendGroupMsg(event.getGroupId(), "抽签功能正在开发中！ZZZ...", false);

        return BotPlugin.MESSAGE_IGNORE;
    }
}
