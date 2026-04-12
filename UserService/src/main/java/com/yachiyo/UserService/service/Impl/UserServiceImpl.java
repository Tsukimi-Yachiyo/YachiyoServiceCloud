package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.dto.PosterDetailResponse;
import com.yachiyo.UserService.dto.UserDetailResponse;
import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.repository.UserDetailRepository;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserService;
import com.yachiyo.UserService.utils.AsyncFileUtil;
import com.yachiyo.UserService.utils.FileUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.security.config.http.MatcherType.path;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private FileUrlUtil fileUrlUtil;

    @Override
    public Mono<Result<UserDetailResponse>> getUserDetail(Long userId) {
        return userDetailRepository.findById(userId)
                .map(userDetail -> {
                    UserDetailResponse resp = new UserDetailResponse();
                    resp.setUserIntroduction(userDetail.getUserIntroduction());
                    resp.setUserName(userDetail.getUserName());
                    resp.setUserCity(userDetail.getUserCity());
                    resp.setUserGender(userDetail.getUserGender());
                    resp.setUserBirthday(userDetail.getUserBirthday());
                    return Result.success(resp);
                })
                .defaultIfEmpty(Result.error("404", "用户不存在"));
    }

    @Override
    public Mono<Result<Boolean>> updateUserDetail(Long userId, UserDetailResponse userDetailResponse) {
        return userDetailRepository.findById(userId)
                .flatMap(existUser -> {
                    // 只更新有值的字段
                    if (userDetailResponse.getUserName() != null) {
                        existUser.setUserName(userDetailResponse.getUserName());
                    }
                    if (userDetailResponse.getUserIntroduction() != null) {
                        existUser.setUserIntroduction(userDetailResponse.getUserIntroduction());
                    }
                    if (userDetailResponse.getUserCity() != null) {
                        existUser.setUserCity(userDetailResponse.getUserCity());
                    }
                    if (userDetailResponse.getUserGender() != null) {
                        existUser.setUserGender(userDetailResponse.getUserGender());
                    }
                    if (userDetailResponse.getUserBirthday() != null) {
                        existUser.setUserBirthday(userDetailResponse.getUserBirthday());
                    }

                    return userDetailRepository.save(existUser);
                })
                .map(saved -> Result.success(true))
                .defaultIfEmpty(Result.error("404", "用户不存在"));
    }

    @Override
    public Mono<Result<Boolean>> updateUserAvatar(Long userId, FilePart userAvatar) {
        String dir = userId.toString();
        String filePath = dir + "/avatar.jpg";
        return AsyncFileUtil.createDirectory(dir)
                .then(userAvatar.content()
                        .flatMap(buffer -> {
                            try {
                                byte[] bytes = new byte[buffer.readableByteCount()];
                                buffer.read(bytes);
                                return AsyncFileUtil.writeBytes(filePath, bytes);
                            } finally {
                                DataBufferUtils.release(buffer);
                            }
                        })
                        .then())
                .thenReturn(Result.success(true))
                .onErrorResume(e -> Mono.just(Result.error("500", "头像上传失败")));
    }

    @Override
    public Mono<Result<String>> getUserAvatar(Long userId) {
        String dir = userId.toString() + "/avatar.jpg";

        return fileUrlUtil.generateFileUrl(dir, 60 * 5)
                .map(Result::success);
    }

    @Override
    public Mono<Result<PosterDetailResponse>> getPosterDetail(Long userId) {
        // 拼接正确的文件路径：用户ID/avatar.jpg
        String filePath = userId + "/avatar.jpg";

        return userDetailRepository.findById(userId)
                .flatMap(userDetail -> {
                    // 先查用户，再生成头像URL
                    return fileUrlUtil.generateFileUrl(filePath, 60 * 5)
                            .map(url -> {
                                PosterDetailResponse response = new PosterDetailResponse();
                                response.setUserName(userDetail.getUserName());
                                response.setUserAvatar(url); // 可能为null
                                return Result.success(response);
                            });
                })
                // 用户不存在返回404
                .defaultIfEmpty(Result.error("404", "用户不存在：" + userId));
    }
}