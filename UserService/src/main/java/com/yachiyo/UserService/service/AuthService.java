package com.yachiyo.UserService.service;

import com.yachiyo.UserService.dto.LoginRequest;
import com.yachiyo.UserService.dto.MailLoginRequest;
import com.yachiyo.UserService.dto.RegisterRequest;
import com.yachiyo.UserService.result.Result;
import reactor.core.publisher.Mono;

public interface AuthService {

    /**
     * 登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    Mono<Result<String>> Login(LoginRequest loginRequest);

    /**
     * 注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    Mono<Result<String>> Register(RegisterRequest registerRequest);

    /**
     * 发送验证码
     * @param email 邮箱
     * @return 发送结果
     */
    Mono<Result<Boolean>> SendCode(String email);

    /**
     * 更改密码
     * @param registerRequest 更改密码请求
     * @return 更改密码结果
     */
    Mono<Result<Boolean>> ChangePassword(RegisterRequest registerRequest);

    /**
     * 退出登录
     * @return 退出登录结果
     */
    Mono<Result<Boolean>> Logout(Long userId);

    /**
     * 邮箱登录
     * @param mailLoginRequest 登录邮箱登录请求
     * @return 登录结果
     */
    Mono<Result<String>> LoginByEmail(MailLoginRequest mailLoginRequest);

    /**
     * 刷新令牌
     * @return 刷新令牌结果
     */
    Mono<Result<String>> RefreshToken(String token);

    /**
     * 冻结账户
     * @param userId 用户ID
     * @return 是否冻结成功
     */
    Mono<Result<Boolean>> Freeze(Long userId);

    /**
     * 获取 Ws 连接令牌
     * @return 连接令牌
     */
    Mono<Result<String>> GetWsToken(Long userId);

    /**
     * 解冻账户
     * @param userId 用户ID
     * @return 是否解冻成功
     */
    Mono<Result<Boolean>> Thaw(Long userId);
}
