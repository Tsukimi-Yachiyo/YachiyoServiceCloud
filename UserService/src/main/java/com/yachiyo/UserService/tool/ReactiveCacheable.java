package com.yachiyo.UserService.tool;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReactiveCacheable {
    // 缓存的前缀，如 "user"
    String cacheName();
    // 缓存的键，如 "user:isFriend:123:456"
    // 可以使用 SpEL 表达式，如 "#currentUserId:#followeeId"
    // 也可以使用方法参数，如 "currentUserId, followeeId"
    String key() default "";
    // 缓存的过期时间，单位秒，默认 600 秒
    long ttl() default 600;
}