package com.yachiyo.UserService.controller;

import com.yachiyo.UserService.dto.UserDetailDTO;
import com.yachiyo.UserService.dto.UserDetailType;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserDetailGetService;
import com.yachiyo.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
@Validated
public class SelfUserDetailController {

    private final UserService userService;

    private final UserDetailGetService userDetailGetService;

    /**
     * 更新用户头像
     *
     * @param avatar 头像
     * @return 是否更新成功
     */
    @PutMapping("/avatar")
    public Mono<Result<Boolean>> updateAvatar(@AuthenticationPrincipal String userId,
                                              @RequestPart("avatar") FilePart avatar) {
        return userService.updateUserAvatar(Long.parseLong(userId), avatar);
    }

    /**
     * 获取用户头像
     * @return 用户头像
     */
     @GetMapping("/avatar")
    public Mono<Result<String>> getUserAvatar(@AuthenticationPrincipal String userId) {
        return userService.getUserAvatar(Long.parseLong(userId));
    }

    /**
     * 更新用户详情
     *
     * @param userDetailDTO 用户详情
     * @return 是否更新成功
     */
    @PutMapping("/detail")
    public Mono<Result<Boolean>> updateUserDetail(@AuthenticationPrincipal String userId,
                                                  @RequestBody UserDetailDTO userDetailDTO) {
        return userService.updateUserDetail(Long.parseLong(userId), userDetailDTO);
    }

    /**
     * 获取用户详情
     * @return 用户详情
     */
    @GetMapping("/detail")
    public Mono<Result<UserDetailDTO>> getUserDetail(@AuthenticationPrincipal String userId) {
        return userDetailGetService.getDetail(Long.parseLong(userId), Long.parseLong(userId), UserDetailType.SELF);
    }
}
