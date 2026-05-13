package com.yachiyo.QQBotService.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.yachiyo.QQBotService.dto.GroupMessageReq;
import com.yachiyo.QQBotService.plugin.feature.FeatureCategory;
import com.yachiyo.QQBotService.plugin.feature.FeatureRegistry;
import com.yachiyo.QQBotService.service.OneBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Shiro
@Component
public class FeaturePlugin {
    public static final String CMD_FUNCTION = "^\\s*/功能\\s*$";

    @Autowired
    private OneBotService oneBotService;

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, cmd = CMD_FUNCTION)
    public int onFeatureCheck(Bot bot, AnyMessageEvent event) {
        // 按照功能分类展示功能列表
        for (var category : FeatureCategory.values()) {
            var features = FeatureRegistry.getFeatures(category);
            if (features.isEmpty()) continue;

            StringBuilder sb = new StringBuilder();
            sb.append(category.getDescription()).append("：");
            for (var feature : features) {
                sb.append(feature.getDisplayCommand()).append(" ");
            }

            oneBotService.send(bot, event.getGroupId(), sb.toString());
        }

        return BotPlugin.MESSAGE_IGNORE;
    }
}
