package com.yachiyo.QQBotService.enums;

import java.util.List;
import java.util.Set;

public enum AtMatchRule {
    /**
     * 消息at了自己时匹配成功
     */
    SELF,
    /**
     * 消息at列表与匹配列表完全相同时匹配成功
     */
    COMPETE,
    /**
     * 消息at列表是匹配列表的子集时匹配成功
     */
    SUB,
    /**
     * 消息at列表与匹配列表有交集时匹配成功
     */
    ANY,
    /**
     * 消息at列表与匹配列表没有交集时匹配成功
     */
    NONE // 消息没有at任何人或匹配列表中的成员
    ;

    public boolean matches(List<String> matchList, Long selfId, List<String> atList) {
        boolean containsAtAll = atList.stream().anyMatch("all"::equals);

        Set<String> matchSet = Set.copyOf(matchList);
        Set<String> atSet = Set.copyOf(atList);

        return switch (this) {
            case SELF -> matchList.contains(selfId.toString());
            case COMPETE -> matchSet.equals(atSet);
            case SUB -> {
                if (containsAtAll) yield true;
                else yield atSet.isEmpty() || matchSet.containsAll(atSet);
            }
            case ANY -> {
                if (containsAtAll) yield true;
                else yield atSet.stream().anyMatch(matchSet::contains);
            }
            case NONE -> {
                if (containsAtAll) yield false;
                else yield atSet.isEmpty() || atSet.stream().noneMatch(matchSet::contains);
            }
        };
    }
}
