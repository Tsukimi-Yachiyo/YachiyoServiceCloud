package com.yachiyo.QQBotService.plugin.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeatureCategory {
    GAME("小游戏"),
    MISC("其他")
    ;

    private final String description;
}
