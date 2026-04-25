package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.client.FileClient;
import com.yachiyo.UserService.config.FastMethodConfig;
import com.yachiyo.UserService.dto.PublicUserDetailResponse;
import com.yachiyo.UserService.dto.SearchDetailResponse;
import com.yachiyo.UserService.entity.FollowLink;
import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.repository.FollowLinkRepository;
import com.yachiyo.UserService.repository.UserDetailRepository;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserInteractService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Service
@AllArgsConstructor @Slf4j
public class UserInteractServiceImpl implements UserInteractService {

    private final UserDetailRepository userDetailRepository;

    private final FileClient fileClient;

    private static final String AVATAR_PATH_FORMAT = "%d/avatar.jpg";

    private final FollowLinkRepository followLinkRepository;

    private final FastMethodConfig fastMethodConfig;

    @Override
    public Mono<Result<Boolean>> follow(Long userId, Long followeeId) {

        if (followeeId.equals(userId)) {
            return Mono.just(Result.error("400", "不能关注自己", "不能关注自己"));
        }

        FollowLink followLink = new FollowLink();
        followLink.setFollowerId(userId);
        followLink.setFolloweeId(followeeId);

        return followLinkRepository.existsByFolloweeIdAndFollowerId(followeeId, userId)
                .flatMap(exists -> exists ? followLinkRepository.deleteByFolloweeIdAndFollowerId(followeeId, userId) : followLinkRepository.save(followLink))
                .map(_ -> Result.success(true))
                .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "关注失败")));
    }

    @Override
    public Flux<Result<Long>> getFolloweeList(Long userId) {
        return followLinkRepository.findByFollowerId(userId)
                .map(FollowLink::getFolloweeId)
                .map(Result::success)
                .onErrorResume(error -> Flux.just(Result.error("500", error.getMessage(), "获取关注列表失败")));
    }

    @Override
    public Flux<Result<Long>> getFollowerList(Long userId) {
        return followLinkRepository.findByFolloweeId(userId)
                .map(FollowLink::getFollowerId)
                .map(Result::success)
                .onErrorResume(error -> Flux.just(Result.error("500", error.getMessage(), "获取粉丝列表失败")));
    }

    @Override
    public Mono<Result<SearchDetailResponse>> getOnesInteractionDetail(Long followeeId) {
        Mono<Long> followeeCountMono = followLinkRepository.countByFollowerId(followeeId);

        Mono<Long> followerCountMono = followLinkRepository.countByFolloweeId(followeeId);

        return Mono.zip(
                        userDetailRepository.findById(followeeId),
                        followeeCountMono,
                        followerCountMono
                )
                .flatMap(tuple -> {
                    UserDetail user = tuple.getT1();
                    Long followeeCount = tuple.getT2();  // 关注数
                    Long followerCount = tuple.getT3();  // 粉丝数

                    String filePath = String.format(AVATAR_PATH_FORMAT, followeeId);

                    return fastMethodConfig.callFileClient(
                            () -> Result.success(fileClient.getUrl(filePath, System.currentTimeMillis())),
                            "获取头像URL失败"
                    )
                            .onErrorResume(e -> Mono.just("无头像URL"))
                            .map(avatarUrl -> {
                                SearchDetailResponse resp = new SearchDetailResponse();
                                resp.setUserName(user.getUserName());
                                resp.setUserAvatar(avatarUrl);
                                resp.setFolloweeCount(followeeCount);
                                resp.setFollowerCount(followerCount);
                                return Result.success(resp);
                            })
                            .onErrorResume(e -> {
                                SearchDetailResponse resp = new SearchDetailResponse();
                                resp.setUserName(user.getUserName());
                                resp.setUserAvatar(null); // 失败给null
                                resp.setFolloweeCount(followeeCount);
                                resp.setFollowerCount(followerCount);
                                return Mono.just(Result.success(resp));
                            });
                })
                .switchIfEmpty(Mono.just(Result.error("404", "用户不存在", "用户不存在")))
                .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "获取用户互动详情失败")));
    }

    @Override
    public Mono<Result<PublicUserDetailResponse>> getOnesDetail(Long userId) {
        return userDetailRepository.findById(userId)
                .map(userDetail -> {
                    PublicUserDetailResponse publicUserDetailResponse = new PublicUserDetailResponse();
                    publicUserDetailResponse.setUserIntroduction(userDetail.getUserIntroduction());
                    publicUserDetailResponse.setUserCity(userDetail.getUserCity());
                    publicUserDetailResponse.setUserGender(userDetail.getUserGender());
                    return Result.success(publicUserDetailResponse);
                })
                .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "获取用户详情失败")));
    }

    @Override
    public Mono<Result<Boolean>> getFollowStatus(Long userId, Long followeeId) {
        return followLinkRepository.existsByFolloweeIdAndFollowerId(followeeId, userId)
                .defaultIfEmpty(false)
                .map(Result::success)
                .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "获取关注状态失败")));
    }

    @Override
    public Flux<Result<SearchDetailResponse>> searchUser(Long currentUserId, String userName, int pageNum, int pageSize) {

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        return userDetailRepository.findByUserNameContains(userName, PageRequest.of(pageNum - 1, pageSize))
                .flatMap(userDetail -> {
                    // 并发查询
                    Mono<Boolean> isFollowing = followLinkRepository.existsByFolloweeIdAndFollowerId(userDetail.getUserId(), currentUserId).defaultIfEmpty(false);
                    Mono<Boolean> isFollowed = followLinkRepository.existsByFolloweeIdAndFollowerId(currentUserId, userDetail.getUserId()).defaultIfEmpty(false);
                    Mono<Long> followerCount = followLinkRepository.countByFolloweeId(userDetail.getUserId());

                    String filePath = String.format(AVATAR_PATH_FORMAT, userDetail.getUserId());

                    // 组合三个 Mono
                    return Mono.zip(isFollowing, isFollowed, followerCount)
                            .flatMap(tuple3 -> fastMethodConfig.callFileClient(
                                    () -> Result.success(fileClient.getUrl(filePath, System.currentTimeMillis())),
                                    "获取头像URL失败"
                            )
                                    .onErrorResume(e -> Mono.just("无头像URL"))
                                    .map(avatarUrl -> {
                                        SearchDetailResponse resp = new SearchDetailResponse();
                                        resp.setUserId(userDetail.getUserId());
                                        resp.setUserName(userDetail.getUserName());
                                        resp.setUserAvatar(avatarUrl);
                                        resp.setIsFollowing(tuple3.getT1());
                                        resp.setIsFollowed(tuple3.getT2());
                                        resp.setFollowerCount(tuple3.getT3());
                                        return resp;
                                    }));
                })
                .map(Result::success) // 每条结果包装成 Result
                .onErrorResume(error ->
                        Mono.just(Result.error("500", error.getMessage(), "搜索用户失败"))
                );
    }

    @Override
    public Mono<Result<Boolean>> isFriend(Long currentUserId, Long followeeId) {
        Mono<Boolean> isFollowingMono = followLinkRepository.existsByFolloweeIdAndFollowerId(currentUserId, followeeId).defaultIfEmpty(false);
        Mono<Boolean> isFollowedMono = followLinkRepository.existsByFolloweeIdAndFollowerId(followeeId, currentUserId).defaultIfEmpty(false);

        return Mono.zip(isFollowingMono, isFollowedMono)
                .map(tuple -> {
                    boolean following = tuple.getT1();
                    boolean followed = tuple.getT2();
                    return Result.success(following && followed);
                });
    }

    @Override
    public Flux<Result<Long>> friends(Long currentUserId) {

        return getFolloweeList(currentUserId)
                .map(Result::getData)
                .flatMap(
                followeeId -> followLinkRepository.existsByFolloweeIdAndFollowerId(currentUserId, followeeId).defaultIfEmpty(false)
                        .filter(Boolean::booleanValue)
                        .thenReturn(Result.success(followeeId))
                        .onErrorResume(error -> Mono.just(Result.error("500", error.getMessage(), "获取关注状态失败")))
                );

    }
}
