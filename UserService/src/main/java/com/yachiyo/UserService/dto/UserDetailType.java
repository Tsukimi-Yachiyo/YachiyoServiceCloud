package com.yachiyo.UserService.dto;

import java.util.*;

public enum UserDetailType {
    // --- 独立消息 (基础字段) ---
    NAME, INTRODUCTION, CITY, GENDER, PHONE,QQ, BIRTHDAY, AVATAR, FOLLOWER_COUNT, FOLLOWEE_COUNT,IS_FOLLOWED,IS_FOLLOWING,

    // --- 聚合类型 (复合详情) ---
    /**
     * 自定义详情类型：包含用户的所有独立消息字段
     */
    SELF(NAME, INTRODUCTION, CITY, GENDER, PHONE,QQ, BIRTHDAY, AVATAR, FOLLOWER_COUNT, FOLLOWEE_COUNT),
        
    /**
     * 发布详情类型：包含用户的基本信息和头像
     */
    POSTER(NAME, AVATAR),
        
    /**
     * 搜索用户详情类型：包含用户的基本信息、头像、关注者数量和关注数量
     */
    SEARCH(NAME, AVATAR, FOLLOWER_COUNT, FOLLOWEE_COUNT,IS_FOLLOWED,IS_FOLLOWING),

    /**
     * 关注详情类型：包含关注和被关注
     */
    FOLLOW(FOLLOWER_COUNT, FOLLOWEE_COUNT,IS_FOLLOWED,IS_FOLLOWING),

    /**
     * 公共详情类型：包含用户的介绍、城市和性别
     */
    PUBLIC(INTRODUCTION, CITY, GENDER),

    /**
     * 好友详情类型：包含用户的基本信息、头像、介绍、城市、性别、手机号和出生日期
     */
    FRIEND(NAME, AVATAR, INTRODUCTION, CITY, GENDER, PHONE, BIRTHDAY);

    private final List<UserDetailType> components;
    
    // 构造函数：接受零个或多个 DetailType 作为子组件
    UserDetailType(UserDetailType... components) {
        this.components = Arrays.asList(components);
    }

    /**
     * 获取当前类型包含的所有独立消息字段
     * 如果是独立字段本身，则返回包含自身的列表
     * @return 包含的所有独立消息字段
     */
    public List<UserDetailType> getBasicFields() {
        if (this.components.isEmpty()) {
            return Collections.singletonList(this);
        }
        return this.components;
    }
    
    /**
     * 获取当前详情类型的名称
     * @return 详情类型的名称
     */
    public String getDetailType() {
        return name();
    }
}