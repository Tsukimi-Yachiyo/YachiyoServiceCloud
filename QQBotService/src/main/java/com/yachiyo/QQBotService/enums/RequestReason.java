package com.yachiyo.QQBotService.enums;

public enum RequestReason {
    AT, // 机器人被AT
    KEYWORD, // 满足主动调用条件
    AI_REGEX // 满足AI给出的正则条件
    ;
}
