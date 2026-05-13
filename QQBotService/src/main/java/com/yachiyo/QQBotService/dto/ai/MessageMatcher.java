package com.yachiyo.QQBotService.dto.ai;

import com.yachiyo.QQBotService.enums.CQMatchRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息匹配器，包含纯文本正则表达式、at匹配和CQ码匹配规则
 * 三个匹配条件必须同时满足才算匹配成功
 * 三个匹配条件都可以为空，表示不限制该条件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMatcher {
    private String plainTextRegex; // 纯文本正则表达式
    private AtMatcher atMatcher; // at匹配
    private CQMatchRule cqMatchRule; // CQ码匹配规则
}
