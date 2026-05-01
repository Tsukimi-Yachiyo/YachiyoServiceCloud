package com.yachiyo.QQBotService.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.yachiyo.QQBotService.dto.ai.AtMatcher;
import com.yachiyo.QQBotService.dto.ai.MessageMatcher;
import com.yachiyo.QQBotService.enums.CQMatchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {
    @Autowired
    private CQCodeUtils cqCodeUtils;

    public boolean messageMatch(Bot bot, MessageMatcher messageMatcher, MessageEvent event) {
        String plainText = event.getPlainText();
        String plainTextRegex = messageMatcher.getPlainTextRegex();
        boolean isPlainTextMatch = plainTextRegex == null || plainTextRegex.isBlank() || plainText.matches(messageMatcher.getPlainTextRegex());

        AtMatcher atMatcher = messageMatcher.getAtMatcher();
        boolean isAtMatch = atMatcher == null || atMatcher.matches(bot.getSelfId(), cqCodeUtils.getAtIdList(event.getArrayMsg()));

        CQMatchRule cqMatchRule = messageMatcher.getCqMatchRule();
        boolean isCQMatch = cqMatchRule == null || cqMatchRule.matches(event.getArrayMsg(), cqCodeUtils);

        return isPlainTextMatch && isAtMatch && isCQMatch;
    }
}
