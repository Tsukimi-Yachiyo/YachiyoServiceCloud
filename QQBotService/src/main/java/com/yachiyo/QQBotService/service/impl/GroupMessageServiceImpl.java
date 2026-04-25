package com.yachiyo.QQBotService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.GroupMessageResp;
import com.yachiyo.QQBotService.entity.ChatGroup;
import com.yachiyo.QQBotService.entity.ForwardMessage;
import com.yachiyo.QQBotService.entity.GroupMessage;
import com.yachiyo.QQBotService.mapper.ChatGroupMapper;
import com.yachiyo.QQBotService.mapper.ForwardMessageMapper;
import com.yachiyo.QQBotService.mapper.MessageMapper;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.GroupMessageService;
import com.yachiyo.QQBotService.utils.BotUtils;
import com.yachiyo.QQBotService.utils.CQCodeUtils;
import com.yachiyo.QQBotService.utils.MessageUtils;
import com.yachiyo.QQBotService.utils.UnixUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupMessageServiceImpl implements GroupMessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private BotUtils botUtils;

    @Autowired
    private ForwardMessageMapper forwardMessageMapper;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private UnixUtils unixUtils;

    @Autowired
    private CQCodeUtils CQCodeUtils;

    @Autowired
    private MessageUtils messageUtils;

    @Override
    public Result<GroupMessageResp> getLatest(Long groupId) {
        try {
            var actionData = botUtils.getBot().getGroupMsgHistory(groupId, null, 1, false);
            var msgResp = actionData.getData().getMessages().getFirst();
            return Result.success(new GroupMessageResp(
                    msgResp.getPlainText(),
                    msgResp.getSender(),
                    messageUtils.format(msgResp)
            ));
        } catch (Exception e) {
            return Result.error("获取消息失败: ", e.getMessage());
        }
    }

    @Override
    public Result<List<GroupMessageResp>> get(Long groupId, Integer size) {
        try {
            var actionData = botUtils.getBot().getGroupMsgHistory(groupId, null, size, false);
            List<GroupMessageResp> groupMessageRespList = new ArrayList<>();
            for (var msgResp : actionData.getData().getMessages()) {
                groupMessageRespList.add(new GroupMessageResp(
                        msgResp.getPlainText(),
                        msgResp.getSender(),
                        messageUtils.format(msgResp)
                ));
            }
            return Result.success(groupMessageRespList);
        } catch (Exception e) {
            return Result.error("获取消息失败: ", e.getMessage());
        }
    }

    @Override
    public Result<Integer> send(GroupMessageReq groupMessageReq) {
        try {
            var actionData = botUtils.getBot().sendGroupMsg(
                    groupMessageReq.getGroupId(),
                    groupMessageReq.getMessage(),
                    true
            );
            return Result.success(actionData.getData().getMessageId());
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage(), null);
        }
    }

    @Override
    public Result<Boolean> onSendMessage(GroupMessage groupMessage) {
        try {
            if (chatGroupMapper.selectById(groupMessage.getGroupId()) == null) {
                ChatGroup chatGroup = new ChatGroup();
                chatGroup.setGroupId(groupMessage.getGroupId());
                chatGroupMapper.insert(chatGroup);
            }

            int rows = messageMapper.insert(groupMessage);
            if (rows > 0) {
                return Result.success(true);
            } else {
                return Result.error("保存消息失败", null);
            }
        } catch (Exception e) {
            return Result.error("保存消息失败 : ", e.getMessage());
        }
    }

    @Override
    public Result<Boolean> onDeleteMessage(Integer messageId) {
        try {
            QueryWrapper<GroupMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("message_id", messageId);
            GroupMessage groupMessage = messageMapper.selectOne(queryWrapper);

            if (groupMessage == null) {
                return Result.error("404", "消息不存在", null);
            }

            groupMessage.setIsRecalled(true);

            int rows = messageMapper.updateById(groupMessage);
            return Result.success(rows > 0);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage(), null);
        }
    }

    @Override
    public GroupMessage computeGroupMessage(Bot bot, GroupMessageEvent event) {
        GroupMessage groupMessage = new GroupMessage();

        groupMessage.setGroupId(event.getGroupId());
        groupMessage.setSendTime(unixUtils.ofSecond(event.getTime()));
        groupMessage.setMessageId(Long.valueOf(event.getMessageId()));
        groupMessage.setSenderId(event.getUserId());
        groupMessage.setPlainText(event.getPlainText().trim());

        groupMessage.setBySelf(event.getUserId().equals(bot.getSelfId()));
        groupMessage.setIsRecalled(false);

        var formattedMessage = messageUtils.format(event);
        groupMessage.setAtList(formattedMessage.getAtList());
        groupMessage.setFileNames(formattedMessage.getFileNames());
        groupMessage.setPromptText(formattedMessage.getPromptText());
        groupMessage.setRelevantUrls(formattedMessage.getRelevantUrls());

        saveForwardMessage(bot, event);

        return groupMessage;
    }

    /**
     * 如果消息中包含合并转发消息，则将其内容保存到数据库中，不支持嵌套的合并转发消息，因为ShiroSDK返回的DTO中无法查询到messageId
     * @param bot 用于调用API获取合并转发消息内容
     * @param event 消息事件
     */
    private void saveForwardMessage(Bot bot, GroupMessageEvent event) {
        long forwardId = CQCodeUtils.findForwardId(event.getArrayMsg());
        if (forwardId < 0) {
            return;
        }

        // 避免重复保存
        QueryWrapper<ForwardMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("forward_id", forwardId);
        if (forwardMessageMapper.exists(queryWrapper)) {
            return;
        }

        boolean hasChild = false;
        List<String> messageList = new ArrayList<>();

        var forwardMsgResp = bot.getForwardMsg(event.getMessageId());
        if (forwardMsgResp == null || forwardMsgResp.getData() == null) {
            return;
        }
        for (MsgResp msgResp : forwardMsgResp.getData().getMessages()) {
            if (CQCodeUtils.containsForward(msgResp)) {
                hasChild = true;
            }
            messageList.add(messageUtils.format(msgResp).getPromptText());
        }

        var forwardMessage = new ForwardMessage();

        forwardMessage.setForwardId(forwardId);
        forwardMessage.setMessages(messageList);
        forwardMessage.setHasChild(hasChild);

        forwardMessageMapper.insert(forwardMessage);
    }
}
