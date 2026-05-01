package com.yachiyo.QQBotService.service.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.OneBotService;
import com.yachiyo.QQBotService.service.GroupMessageService;
import com.yachiyo.QQBotService.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OneBotServiceImpl implements OneBotService {
    @Autowired
    private FormatUtils formatUtils;

    @Autowired
    private GroupMessageService groupMessageService;

    @Override
    public Result<GroupMessageResp> getLatest(Bot bot, Long groupId) {
        try {
            // 优先从数据库中查询
            return groupMessageService.selectLastest(groupId);
        } catch (Exception _) {}

        try {
            var actionData = bot.getGroupMsgHistory(groupId, null, 1, false);
            var msgResp = actionData.getData().getMessages().getFirst();
            return Result.success(new GroupMessageResp(
                    msgResp.getPlainText(),
                    msgResp.getUserId(),
                    formatUtils.format(msgResp)
            ));
        } catch (Exception e) {
            return Result.error("获取消息失败: ", e.getMessage());
        }
    }

    @Override
    public Result<List<GroupMessageResp>> get(Bot bot, Long groupId, Integer size) {
        try {
            // 优先从数据库中查询
            var result = groupMessageService.selectMessage(groupId, size);
            if (!result.getData().isEmpty()) return result;
        } catch (Exception _) {}

        try {
            var actionData = bot.getGroupMsgHistory(groupId, null, size, false);
            List<GroupMessageResp> groupMessageRespList = new ArrayList<>();
            for (var msgResp : actionData.getData().getMessages()) {
                groupMessageRespList.add(new GroupMessageResp(
                        msgResp.getPlainText(),
                        msgResp.getUserId(),
                        formatUtils.format(msgResp)
                ));
            }
            return Result.success(groupMessageRespList);
        } catch (Exception e) {
            return Result.error("获取消息失败: ", e.getMessage());
        }
    }

    @Override
    public Result<GroupMessageResp> get(Bot bot, Integer messageId) {
        try {
            // 优先从数据库中查询
            return groupMessageService.selectGroupMessageResp(messageId);
        } catch (Exception _) {}

        try {
            var actionData = bot.getMsg(messageId);
            var msgResp = actionData.getData();
            return Result.success(new GroupMessageResp(
                    msgResp.getPlainText(),
                    msgResp.getUserId(),
                    formatUtils.format(msgResp)
            ));
        } catch (Exception e) {
            return Result.error("获取消息失败: ", e.getMessage());
        }
    }

    @Override
    public Result<Integer> send(Bot bot, GroupMessageReq groupMessageReq) {
        try {
            int msgId = bot.sendGroupMsg(groupMessageReq.getGroupId(), groupMessageReq.getMessage(), false).getData().getMessageId();
            MsgResp msgResp = bot.getMsg(msgId).getData();
            groupMessageService.onSendMessage(groupMessageService.computeGroupMessage(bot, msgResp, groupMessageReq.getGroupId(), msgId));
            return Result.success(msgId);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage(), null);
        }
    }
}
