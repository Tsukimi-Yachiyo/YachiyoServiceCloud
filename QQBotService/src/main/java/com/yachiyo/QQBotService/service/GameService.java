package com.yachiyo.QQBotService.service;

import com.yachiyo.QQBotService.dto.game.FortuneResult;
import com.yachiyo.QQBotService.result.Result;

public interface GameService {
    Result<FortuneResult> fortune(Long userId);
}
