package com.yachiyo.QQBotService.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.yachiyo.QQBotService.plugin.feature.FeatureCategory;
import com.yachiyo.QQBotService.plugin.feature.FeatureRegistry;
import org.springframework.stereotype.Component;

@Shiro
@Component
public class FeaturePlugin {
    public static final String CMD_FUNCTION = "^\\s*/功能\\s*$";

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

            bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        }

        return BotPlugin.MESSAGE_IGNORE;
    }
}
