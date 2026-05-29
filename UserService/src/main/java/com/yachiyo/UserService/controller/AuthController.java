package com.yachiyo.UserService.controller;

import com.yachiyo.UserService.dto.LoginRequest;
import com.yachiyo.UserService.dto.MailLoginRequest;
import com.yachiyo.UserService.dto.RegisterRequest;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.AuthService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Mono<Result<String>> Login(@RequestBody @Valid LoginRequest loginRequest) {
        return authService.Login(loginRequest);
    }

    /**
     * 注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Mono<Result<String>> Register(@RequestBody @Valid RegisterRequest registerRequest) {
        return authService.Register(registerRequest);
    }

    /**
     * 发送验证码
     * @param email 邮箱
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Mono<Result<Boolean>> SendCode(@RequestParam @Valid String email) {
        return authService.SendCode(email);
    }

    /**
     * 更改密码
     * @param registerRequest 更改密码请求
     * @return 更改密码结果
     */
    @PostMapping("/change-password")
    public Mono<Result<Boolean>> ChangePassword(@RequestBody @Valid RegisterRequest registerRequest) {
        return authService.ChangePassword(registerRequest);
    }

    /**
     * 邮箱登录
     * @param mailLoginRequest 登录邮箱登录请求
     * @return 登录结果
     */
    @PostMapping("/login-by-email")
    public Mono<Result<String>> LoginByEmail(@RequestBody @Valid MailLoginRequest mailLoginRequest) {
        return authService.LoginByEmail(mailLoginRequest);
    }

    /**
     * 刷新令牌
     * @return 刷新令牌结果
     */
    @PostMapping("/refresh-token")
    public Mono<Result<String>> RefreshToken(@RequestParam String refreshToken) {
        return authService.RefreshToken(refreshToken);
    }

    /**
     * 退出登录
     * @return 退出登录结果
     */
    @PostMapping("/logout")
    public Mono<Result<Boolean>> Logout(@AuthenticationPrincipal @NonNull String userId) {
        return authService.Logout(Long.parseLong(userId));
    }

    /**
     * 获取 Ws 连接令牌
     * @return 连接令牌
     */
    @GetMapping("/ws-token")
    public Mono<Result<String>> GetWsToken(@AuthenticationPrincipal @NonNull String userId) {
        return authService.GetWsToken(Long.parseLong(userId));
    }

    /**
     * 冻结账户
     * @param userId 用户ID
     * @return 是否冻结成功
     */
    @PostMapping("/freeze")
    public Mono<Result<Boolean>> Freeze(@AuthenticationPrincipal @NonNull String userId) {
        return authService.Freeze(Long.parseLong(userId));
    }

    /**
     * 解冻账户
     * @param userId 用户ID
     * @return 是否解冻成功
     */
    @PostMapping("/thaw")
    public Mono<Result<Boolean>> Thaw(@RequestParam String userId) {
        return authService.Thaw(Long.parseLong(userId));
    }
}
