package com.yachiyo.QQBotService.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FortuneResult {
    private boolean isHistory; // 是否是历史抽签结果
    private String result; // 抽签结果
}
