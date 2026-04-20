package com.yachiyo.QQBotService.service;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.entity.GroupMessage;
import com.yachiyo.QQBotService.result.Result;

import java.util.List;

public interface GroupMessageService {
    /**
     * 获取指定群的最新一条消息
     * @param groupId 群号
     * @return 最新一条消息
     */
    // TODO：增加从数据库中查询的降级方案
    Result<GroupMessageResp> getLatest(Long groupId);

    /**
     * 获取指定群的最新N条消息
     * @param groupId 群号
     * @param size 要获取的消息数量
     * @return 最新N条消息列表
     */
    // TODO：增加从数据库中查询的降级方案
    Result<List<GroupMessageResp>> get(Long groupId, Integer size);

    /**
     * 发送消息到指定群，自动解析CQ码
     * @param groupMessageReq 发送消息请求对象，包含群号、消息内容等信息
     * @return 成功时返回消息ID
     */
    Result<Integer> send(GroupMessageReq groupMessageReq);

    Result<Boolean> onSendMessage(GroupMessage groupMessage);

    Result<Boolean> onDeleteMessage(Integer messageId);

    GroupMessage computeGroupMessage(Bot bot, GroupMessageEvent event);
}
