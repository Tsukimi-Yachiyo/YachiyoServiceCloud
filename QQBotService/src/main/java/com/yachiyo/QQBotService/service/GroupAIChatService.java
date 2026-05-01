package com.yachiyo.QQBotService.service;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.yachiyo.QQBotService.result.Result;

public interface GroupAIChatService {
    Result<Boolean> onSendMessage(Bot bot, GroupMessageEvent event);
}
