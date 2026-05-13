package com.yachiyo.QQBotService.dto.ai;

import com.yachiyo.QQBotService.enums.AtMatchRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtMatcher {
    private List<String> matchList; // 需要匹配的@列表，可以是QQ号或昵称
    private AtMatchRule atMatchRule;

    public boolean matches(Long selfId, List<String> atList) {
        return atMatchRule.matches(matchList, selfId, atList);
    }
}
