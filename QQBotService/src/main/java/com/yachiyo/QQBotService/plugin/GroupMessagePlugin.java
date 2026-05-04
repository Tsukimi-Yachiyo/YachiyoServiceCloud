package com.yachiyo.QQBotService.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.GroupMsgDeleteNoticeHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.yachiyo.QQBotService.service.GroupAIChatService;
import com.yachiyo.QQBotService.service.GroupMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Shiro
@Component
@Slf4j
// TODO: AI自行设置日程表
// TODO: 更多接口
public class GroupMessagePlugin {
    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private GroupAIChatService groupAIChatService;

    @GroupMessageHandler
    public int onNoAtGroupMessage(Bot bot, GroupMessageEvent event) {
        // 记录消息入库
        groupMessageService.onSendMessage(bot, event);
        if (!event.getPlainText().matches("^\\s*/.*")) {
            // 以斜杠或空白加斜杠开头的消息是为指令，不尝试触发AI
            var result = groupAIChatService.onSendMessage(bot, event);
            log.info("AI聊天处理结果: {}", result);
        }
        return BotPlugin.MESSAGE_IGNORE;
    }

    @GroupMsgDeleteNoticeHandler // 撤回消息
    public int onGroupMsgDeleteNotice(Bot bot, GroupMsgDeleteNoticeEvent event) {
        groupMessageService.onDeleteMessage(event.getMessageId());
        return BotPlugin.MESSAGE_IGNORE;
    }
}
