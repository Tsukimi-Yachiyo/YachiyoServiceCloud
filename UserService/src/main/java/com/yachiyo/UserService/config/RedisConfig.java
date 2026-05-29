package com.yachiyo.UserService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        // 直接使用 JacksonJsonRedisSerializer
        RedisSerializer<Object> valueSerializer = new JacksonJsonRedisSerializer<>(Object.class);

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisSerializationContext<String, Object> context =
                RedisSerializationContext.<String, Object>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .hashValue(valueSerializer)
                        .hashKey(keySerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}