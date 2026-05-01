package com.yachiyo.QQBotService.service;

import com.mikuac.shiro.core.Bot;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.result.Result;

import java.util.List;

public interface OneBotService {
    /**
     * 获取指定群的最新一条消息
     * @param bot Bot实例
     * @param groupId 群号
     * @return 最新一条消息
     */
    Result<GroupMessageResp> getLatest(Bot bot, Long groupId);

    /**
     * 获取指定群的最新N条消息
     * @param bot Bot实例
     * @param groupId 群号
     * @param size 要获取的消息数量
     * @return 最新N条消息列表
     */
    Result<List<GroupMessageResp>> get(Bot bot, Long groupId, Integer size);

    /**
     * 根据消息ID获取消息详情
     * @param bot Bot实例
     * @param messageId 消息ID
     * @return 消息详情
     */
    Result<GroupMessageResp> get(Bot bot, Integer messageId);

    /**
     * 发送消息到指定群，自动解析CQ码
     * @param bot Bot实例
     * @param groupMessageReq 发送消息请求对象，包含群号、消息内容等信息
     * @return 成功时返回消息ID
     */
    Result<Integer> send(Bot bot, GroupMessageReq groupMessageReq);
}
