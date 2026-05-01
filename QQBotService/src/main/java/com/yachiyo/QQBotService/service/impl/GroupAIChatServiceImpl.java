package com.yachiyo.QQBotService.service.impl;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.yachiyo.QQBotService.client.AIClient;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.dto.ai.GroupChatReq;
import com.yachiyo.QQBotService.dto.ai.GroupChatResp;
import com.yachiyo.QQBotService.dto.ai.MessageMatcher;
import com.yachiyo.QQBotService.enums.RequestReason;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.GroupAIChatService;
import com.yachiyo.QQBotService.service.OneBotService;
import com.yachiyo.QQBotService.utils.CQCodeUtils;
import com.yachiyo.QQBotService.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GroupAIChatServiceImpl implements GroupAIChatService {
    @Autowired
    private AIClient aiClient;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private CQCodeUtils cqCodeUtils;

    @Autowired
    private OneBotService oneBotService;

    @Autowired
    private RedisTemplate<String, MessageMatcher> redisTemplate;

    // 第一个是groupId，第二个是messageId
    private static final String MESSAGE_MATCHER_KEY = "public:message_matcher:%s:%s";

    @Override
    public Result<Boolean> onSendMessage(Bot bot, GroupMessageEvent event) {
        // 被AT，直接调用进行回复
        if (cqCodeUtils.atEq(event.getArrayMsg(), bot.getSelfId())) {
            return handleWithReason(bot, event, RequestReason.AT);
        }

        // 没有被AT，进入消息匹配
        MessageMatcher messageMatcher = redisTemplate.opsForValue().getAndDelete(String.format(MESSAGE_MATCHER_KEY, event.getGroupId(), event.getMessageId()));
        if (messageMatcher != null && messageUtils.messageMatch(bot, messageMatcher, event)) {
            return handleWithReason(bot, event, RequestReason.MESSAGE_MATCHER);
        }

        return Result.success(false);
    }

    private Result<Boolean> handleWithReason(Bot bot, GroupMessageEvent event, RequestReason reason) {
        GroupChatResp resp = aiClient.groupChat(computeRequest(event, reason)).getData();
        if (resp == null) {
            return Result.error("500", "AI回复消息失败", "AI服务返回空响应");
        }

        try {
            GroupMessageReq groupMessageReq = new GroupMessageReq();
            groupMessageReq.setGroupId(event.getGroupId());
            String msg = MsgUtils.builder()
                    .text(resp.getAnswer())
                    .reply(resp.getReplyMsgId())
                    .at(resp.getAtId())
                    .build();
            groupMessageReq.setMessage(msg);

            oneBotService.send(bot, groupMessageReq);
        } catch (Exception e) {
            return Result.error("500", "AI回复消息失败", e.getMessage());
        } finally {
            redisTemplate.opsForValue().set(
                    String.format(MESSAGE_MATCHER_KEY, event.getGroupId(), event.getMessageId()),
                    resp.getMessageMatcher(), 30, TimeUnit.SECONDS
            );
        }

        return Result.success(true);
    }

    private GroupChatReq computeRequest(GroupMessageEvent event, RequestReason requestReason) {
        GroupChatReq request = new GroupChatReq();

        request.setMessageId(event.getMessageId());
        request.setGroupId(event.getGroupId());
        request.setRequestReason(requestReason);

        return request;
    }
}
