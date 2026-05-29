package com.yachiyo.UserService.tool;

import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.result.Result;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;

@Component
@AllArgsConstructor
public class UserEntryTool {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    // 2. 注入 响应式 RedisTemplate（替代同步 RedisTemplate）
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    // 响应式 Hash 操作对象
    private ReactiveHashOperations<String, String, Object> hashOps() {
        return reactiveRedisTemplate.opsForHash();
    }

    /**
     * 异步初始化用户详情
     */
    public Mono<Result<Boolean>> initUserDetail(Long id) {
        UserDetail userDetail = new UserDetail();
        userDetail.setUserId(id);

        return r2dbcEntityTemplate.insert(userDetail)
                .then(Mono.just(Result.success(Boolean.TRUE)))
                .onErrorResume(e -> Mono.just(Result.error(
                        "500",
                        "初始化用户详情失败",
                        "数据库保存异常：" + e.getMessage()
                )));
    }

    /**
     * 异步登录校验（生日判断）
     */
    public Mono<Result<Boolean>> login(Long id) {
        return getBirthday(id)
                .map(Result::success)
                .onErrorReturn(Result.error("500", null, "登录校验异常"));
    }

    /**
     * 异步：从Redis读取日期 + 从R2DBC查询用户生日 + 比较
     */
    private Mono<Boolean> getBirthday(Long userId) {
        // 1. 异步读取 Redis Hash
        Mono<Object> dayMono = hashOps().get("public:date", "day");
        Mono<Object> monthMono = hashOps().get("public:date", "month");

        // 2. 组合 Redis 结果 + 异步查询 DB
        return Mono.zip(dayMono, monthMono)
                .flatMap(tuple -> {
                    Object dayObj = tuple.getT1();
                    Object monthObj = tuple.getT2();

                    // 3. 异步 R2DBC 查询用户
                    return r2dbcEntityTemplate.select(UserDetail.class)
                            .matching(Query.query(Criteria.where("id").is(userId)))
                            .one()
                            .flatMap(user -> {
                                LocalDate birthday = user.getUserBirthday();
                                if (birthday == null) {
                                    return Mono.just(true);
                                }
                                int day = birthday.getDayOfMonth();
                                int month = birthday.getMonthValue();

                                boolean result = day == Integer.parseInt(dayObj.toString())
                                        && month == Integer.parseInt(monthObj.toString());
                                String redisMainKey = "user:" + userId;
                                ReactiveHashOperations<String, String, Boolean> hashOps = reactiveRedisTemplate.opsForHash();
                                return hashOps.put(redisMainKey, "birthday", result)
                                        .then(reactiveRedisTemplate.expire(redisMainKey,  Duration.ofHours(24)))
                                        .then(Mono.just(true));
                            })
                            .defaultIfEmpty(false);
                })
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }
}