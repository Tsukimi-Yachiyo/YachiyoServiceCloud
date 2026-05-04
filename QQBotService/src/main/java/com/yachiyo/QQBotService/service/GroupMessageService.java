package com.yachiyo.QQBotService.service;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.entity.GroupMessage;
import com.yachiyo.QQBotService.result.Result;

import java.util.List;

public interface GroupMessageService {
    Result<Boolean> onSendMessage(Bot bot, MessageEvent messageEvent, long groupId, int messageId);

    Result<Boolean> onSendMessage(Bot bot, GroupMessageEvent event);

    Result<Boolean> onDeleteMessage(Integer messageId);

    Result<GroupMessageResp> selectLastest(Long groupId);

    Result<List<GroupMessageResp>> selectMessage(Long groupId, Integer size);

    Result<GroupMessageResp> selectGroupMessageResp(Integer messageId);
}
