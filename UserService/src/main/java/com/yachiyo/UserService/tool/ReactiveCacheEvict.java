package com.yachiyo.UserService.tool;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ReactiveCacheEvicts.class)
public @interface ReactiveCacheEvict {
    /** 缓存前缀 */
    String cacheName();
    /** 支持 SpEL 表达式，如 "#id" */
    String key() default "";
}

