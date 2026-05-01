package com.yachiyo.QQBotService.enums;

import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.yachiyo.QQBotService.utils.CQCodeUtils;

import java.util.List;

public enum CQMatchRule {
    ANY_FILE, // 任意文件，包括图片、视频
    ONLY_FILE, // 仅限文件消息
    RECORD, // 语音消息
    IMAGE, // 图片消息
    VIDEO, // 视频消息
    ;

    public boolean matches(List<ArrayMsg> cqCodeList, CQCodeUtils cqCodeUtils) {
        return switch (this) {
            case ANY_FILE -> cqCodeList.stream().anyMatch(cqCodeUtils::isFileLike);
            case ONLY_FILE -> cqCodeList.stream().anyMatch(arrayMsg -> cqCodeUtils.typeEq(arrayMsg, "file"));
            case RECORD -> cqCodeList.stream().anyMatch(arrayMsg -> cqCodeUtils.typeEq(arrayMsg, MsgTypeEnum.record));
            case IMAGE -> cqCodeList.stream().anyMatch(arrayMsg -> cqCodeUtils.typeEq(arrayMsg, MsgTypeEnum.image));
            case VIDEO -> cqCodeList.stream().anyMatch(arrayMsg -> cqCodeUtils.typeEq(arrayMsg, MsgTypeEnum.video));
        };
    }
}
