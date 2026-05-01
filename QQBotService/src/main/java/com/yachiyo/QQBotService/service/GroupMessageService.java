package com.yachiyo.QQBotService.service;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.entity.GroupMessage;
import com.yachiyo.QQBotService.result.Result;

import java.util.List;

public interface GroupMessageService {
    Result<Boolean> onSendMessage(GroupMessage groupMessage);

    Result<Boolean> onDeleteMessage(Integer messageId);

    GroupMessage computeGroupMessage(Bot bot, GroupMessageEvent event);

    GroupMessage computeGroupMessage(Bot bot, MsgResp msgResp, long groupId, int messageId);

    Result<GroupMessageResp> selectLastest(Long groupId);

    Result<List<GroupMessageResp>> selectMessage(Long groupId, Integer size);

    Result<GroupMessageResp> selectGroupMessageResp(Integer messageId);
}
