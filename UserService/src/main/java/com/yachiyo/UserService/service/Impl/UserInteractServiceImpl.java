package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.entity.FollowLink;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserInteractService;
import com.yachiyo.UserService.tool.ReactiveCacheEvict;
import com.yachiyo.UserService.tool.ReactiveCacheable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class UserInteractServiceImpl implements UserInteractService {

    private final R2dbcEntityTemplate template;

    @Override
    @ReactiveCacheEvict(cacheName = "user:followees", key = "#userId")
    @ReactiveCacheEvict(cacheName = "user:followers", key = "#followeeId")
    @ReactiveCacheEvict(cacheName = "user:isFriend", key = "#userId + ',' + #followeeId")
    @ReactiveCacheEvict(cacheName = "user:friend", key = "#followeeId")
    @ReactiveCacheEvict(cacheName = "user:friend", key = "#userId")
    public Mono<Result<Boolean>> follow(Long userId, Long followeeId) {

        if (followeeId.equals(userId)) {
            return Mono.just(Result.error("400", "不能关注自己", "不能关注自己"));
        }

        // 💡 修复点 1：严格使用数据库真实的列名 "follower" 和 "followee"
        Query relationQuery = Query.query(Criteria.where("follower").is(userId).and("followee").is(followeeId));

        return template.select(FollowLink.class)
                .matching(relationQuery)
                .exists()
                .flatMap(exists -> {
                    if (exists) {
                        // 🚨 修复点 2：绝不能直接 .all()，必须加上 .matching(relationQuery) 才能只删除当前关注记录
                        return template.delete(FollowLink.class).matching(relationQuery).all().thenReturn(true);
                    } else {
                        FollowLink followLink = new FollowLink();
                        // 注意：这里调用的是实体类的 setter，取决于你 Java 实体类怎么写的
                        followLink.setFollowerId(userId);
                        followLink.setFolloweeId(followeeId);
                        return template.insert(followLink).thenReturn(true);
                    }
                })
                .map(_ -> Result.success(true))
                .onErrorResume(error -> {
                    log.error("关注/取消关注操作引发异常: ", error);
                    return Mono.just(Result.error("500", error.getMessage(), "关注操作失败"));
                });
    }

    @Override
    @ReactiveCacheable(cacheName = "user:followees", key = "#userId")
    public Flux<Result<Long>> getFolloweeList(Long userId) {
        return template.select(FollowLink.class)
                .matching(Query.query(Criteria.where("follower").is(userId)).columns("followee"))
                .all().mapNotNull(FollowLink::getFolloweeId)
                .map(Result::success)
                .onErrorResume(error -> Flux.just(Result.error("500", error.getMessage(), "获取关注列表失败")));
    }

    @Override
    @ReactiveCacheable(cacheName = "user:followers", key = "#userId")
    public Flux<Result<Long>> getFollowerList(Long userId) {
        return template.select(FollowLink.class)
                .matching(Query.query(Criteria.where("followee").is(userId)).columns("follower"))
                .all().mapNotNull(FollowLink::getFollowerId)
                .map(Result::success)
                .onErrorResume(error -> Flux.just(Result.error("500", error.getMessage(), "获取粉丝列表失败")));
    }

    @Override
    @ReactiveCacheable(cacheName = "user:isFriend", key = "#currentUserId + ',' + #followeeId")
    public Mono<Result<Boolean>> isFriend(Long currentUserId, Long followeeId) {
        Mono<Boolean> isFollowingMono = template.select(FollowLink.class)
                .matching(Query.query(Criteria.where("follower").is(currentUserId).and("followee").is(followeeId)))
                .exists()
                .defaultIfEmpty(false);

        Mono<Boolean> isFollowedMono = template.select(FollowLink.class)
                .matching(Query.query(Criteria.where("followee").is(currentUserId).and("follower").is(followeeId)))
                .exists()
                .defaultIfEmpty(false);

        return Mono.zip(isFollowingMono, isFollowedMono)
                .map(tuple -> Result.success(tuple.getT1() && tuple.getT2()))
                .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "判断好友状态失败")));
    }

    @Override
    @ReactiveCacheable(cacheName = "user:friends", key = "#currentUserId")
    public Flux<Result<Long>> friends(Long currentUserId) {
        return getFolloweeList(currentUserId)
                .mapNotNull(Result::getData)
                .flatMap(followeeId -> template.select(FollowLink.class)
                        .matching(Query.query(Criteria.where("followee").is(currentUserId).and("follower").is(followeeId)))
                        .exists()
                        .defaultIfEmpty(false)
                        .filter(Boolean::booleanValue)
                        .map(_ -> Result.success(followeeId))
                )
                .onErrorResume(error -> Flux.just(Result.error("500", error.getMessage(), "获取好友列表失败")));
    }
}