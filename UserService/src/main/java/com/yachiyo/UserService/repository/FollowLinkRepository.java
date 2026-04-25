package com.yachiyo.UserService.repository;

import com.yachiyo.UserService.entity.FollowLink;
import org.springframework.data.domain.Range;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface FollowLinkRepository extends R2dbcRepository<FollowLink, Long> {

    @Query("""
        insert into follow_link (
            follower, followee
        ) values (
            :followerId, :followeeId
        )
    """)
    Mono<Integer> insert(FollowLink followLink);

    Flux<FollowLink> findByFollowerId(Long userId);

    Flux<FollowLink> findByFolloweeId(Long userId);

    Mono<Boolean> existsByFolloweeIdAndFollowerId(Long userId, Long followeeId);

    Mono<Long> countByFolloweeId(Long userId);

    Mono<Boolean> deleteByFolloweeIdAndFollowerId(Long userId, Long followeeId);
}
