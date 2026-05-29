package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.client.WalletInitClient;
import com.yachiyo.UserService.dto.LoginRequest;
import com.yachiyo.UserService.dto.MailLoginRequest;
import com.yachiyo.UserService.dto.RegisterRequest;
import com.yachiyo.UserService.entity.User;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.AuthService;
import com.yachiyo.UserService.tool.FileClientTool;
import com.yachiyo.UserService.tool.SafeTool;
import com.yachiyo.UserService.tool.UserEntryTool;
import com.yachiyo.UserService.utils.JwtUtils;
import com.yachiyo.UserService.utils.MailUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserEntryTool userEntryTool;
    private final R2dbcEntityTemplate template;
    private final JwtUtils jwtUtils;
    private final WalletInitClient walletInitClient;
    private final SafeTool safeTool;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final MailUtils mailUtils;
    private final FileClientTool fileClientTool;

    @Override
    public Mono<Result<String>> Login(LoginRequest loginRequest) {
        String encryptedPassword = safeTool.md5(loginRequest.getPassword());

        return template.selectOne(
                        Query.query(Criteria.where("name").is(loginRequest.getUsername())), User.class)
                .flatMap(user -> {
                    if (!user.getPassword().equals(encryptedPassword)) {
                        return Mono.just(Result.<String>error("400.2", "密码错误", null));
                    }
                    if (user.getIsLocked()) {
                        return Mono.just(Result.<String>error("400.3", "用户已被禁用", null));
                    }
                    user.setIsOnline(true);
                    user.setUpdateTime(LocalDateTime.now());
                    return template.update(user)
                            .flatMap(_ -> userEntrySystem(user))
                            .map(token -> Result.success(token, "登录成功", null));
                })
                .switchIfEmpty(Mono.just(Result.error("400.1", "用户名不存在", null)));
    }

    @Override
    public Mono<Result<String>> Register(RegisterRequest registerRequest) {
        return template.selectOne(Query.query(Criteria.where("name").is(registerRequest.getUsername())), User.class)
                .flatMap(_ -> Mono.just(Result.<String>error("400", "用户名已存在", null)))
                .switchIfEmpty(
                        verifyCode(registerRequest.getEmail(), registerRequest.getCode())
                                .flatMap(isValid -> {
                                    if (!isValid) return Mono.just(Result.error("400", "验证码错误", null));

                                    User user = new User();
                                    user.setName(registerRequest.getUsername());
                                    user.setPassword(safeTool.md5(registerRequest.getPassword()));
                                    user.setEmail(registerRequest.getEmail());

                                    return template.insert(user)
                                            .flatMap(savedUser ->
                                                    fileClientTool.callFileClient(
                                                            () ->walletInitClient.initWallet(savedUser.getId()),
                                                            "初始化钱包失败")
                                                            .then(userEntryTool.initUserDetail(savedUser.getId()))
                                                            .then(userEntrySystem(savedUser))
                                                            .map(token -> Result.success(token, "注册成功", null))
                                            );
                                })
                )
                .onErrorResume(e -> Mono.just(Result.error("500", "注册失败", e.getMessage())));
    }

    @Override
    public Mono<Result<Boolean>> SendCode(String email) {
        String code = safeTool.generateCode(6);
        String key = "code:" + email;

        return redisTemplate.opsForValue().get(key)
                .flatMap(_ -> Mono.just(Result.<Boolean>error("400", "验证码已发送，请稍后再试", null)))
                .switchIfEmpty(
                        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10))
                                .then(mailUtils.sendMail(email, "验证码", code))
                                .thenReturn(Result.success(true, "验证码发送成功", null))
                                .onErrorResume(e -> Mono.just(Result.error("500", "内部发送失败", e.getMessage())))
                )
                .onErrorResume(e -> Mono.just(Result.error("500", "验证码校验失败", e.getMessage())));
    }

    @Override
    public Mono<Result<Boolean>> ChangePassword(RegisterRequest registerRequest) {
        return verifyCode(registerRequest.getEmail(), registerRequest.getCode())
                .flatMap(isValid -> {
                    if (!isValid) return Mono.just(Result.error("400.1", "验证码错误", null));

                    return template.selectOne(Query.query(Criteria.where("name").is(registerRequest.getUsername())), User.class)
                            .flatMap(user -> {
                                if (!user.getEmail().equals(registerRequest.getEmail())) {
                                    return Mono.just(Result.<Boolean>error("400.3", "用户邮箱不一致", null));
                                }
                                user.setPassword(safeTool.md5(registerRequest.getPassword()));
                                return template.update(user).thenReturn(Result.success(true, "密码更改成功", null));
                            })
                            .switchIfEmpty(Mono.just(Result.error("400.2", "用户名不存在", null)));
                });
    }

    @Override
    public Mono<Result<Boolean>> Logout(Long userId) {
        return redisTemplate.delete("user:" + userId)
                .flatMap(result -> {
                    if (result > 0) {
                        return template.update(
                                Query.query(Criteria.where("id").is(userId)),
                                Update.update("is_online", false),
                                User.class).map(res -> {
                            if (res > 0) {
                                return Result.success(true, "登出成功", null);
                            } else {
                                return Result.error("400", "用户不存在", null);
                            }
                        });
                    } else {
                        return Mono.just(Result.error("400", "用户不存在", null));
                    }
                });
    }

    @Override
    public Mono<Result<String>> LoginByEmail(MailLoginRequest mailLoginRequest) {
        return verifyCode(mailLoginRequest.getEmail(), mailLoginRequest.getCode())
                .flatMap(isValid -> {
                    if (!isValid) return Mono.just(Result.error("400", "验证码错误", null));

                    return template.selectOne(Query.query(Criteria.where("email").is(mailLoginRequest.getEmail())), User.class)
                            .flatMap(user -> {
                                if (user.getIsLocked()) {
                                    return Mono.just(Result.<String>error("400.2", "用户已被锁定", null));
                                }else{
                                    user.setIsOnline(true);
                                    user.setUpdateTime(LocalDateTime.now());
                                    return template.update(user)
                                            .flatMap(_ -> userEntrySystem(user))
                                            .map(token -> Result.success(token, "登录成功", null));
                                }
                            })
                            .switchIfEmpty(Mono.just(Result.error("400.1", "邮箱不存在", null)));
                });
    }

    @Override
    public Mono<Result<String>> RefreshToken(String token) {
        if (!jwtUtils.isTokenValid(token)) {
            return Mono.just(Result.error("403", "token 不合法", null));
        }
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        String key = "user:" + userId;
        return redisTemplate.hasKey(key)
                .flatMap(exists -> {
                    if (!exists) {
                        return template.selectOne(Query.query(Criteria.where("id").is(userId)).columns("is_online"), User.class)
                            .map(User::getIsOnline)
                            .defaultIfEmpty(false)
                            .flatMap(isOnline -> {
                                if (isOnline) {
                                    User user = new User();
                                    user.setId(userId);
                                    user.setUpdateTime(LocalDateTime.now());
                                    return template.update(user)
                                            .then(userEntrySystem(user))
                                            .map(newToken -> Result.success(newToken, "刷新令牌成功", null));
                                } else {
                                    return Mono.just(Result.success(null, "刷新令牌成功", null));
                                }
                            });
                    }

                    return redisTemplate.delete(key)
                            .then(template.selectOne(Query.query(Criteria.where("id").is(userId)), User.class))
                            .flatMap(user -> userEntrySystem(user).map(newToken -> Result.success(newToken, "刷新令牌成功", null)));
                });
    }

    @Override
    public Mono<Result<Boolean>> Freeze(Long userId) {
        return template.update(
                Query.query(Criteria.where("id").is(userId)),
                Update.update("is_locked", true),
                User.class).map(res -> {
            if (res > 0) {
                return Result.success(true, "冻结成功", null);
            } else {
                return Result.error("400", "用户不存在", null);
            }
        });
    }

    @Override
    public Mono<Result<String>> GetWsToken(Long userId) {
        String token = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

        return redisTemplate.opsForHash()
                .put("user:" + userId, "ws_token", token)
                .map(_ -> Result.success(token + "." + userId, "获取成功", null));
    }

    @Override
    public Mono<Result<Boolean>> Thaw(Long userId) {
        return template.update(
                Query.query(Criteria.where("id").is(userId)),
                Update.update("is_locked", false),
                User.class).map(res -> {
            if (res > 0) {
                return Result.success(true, "解冻成功", null);
            } else {
                return Result.error("400", "用户不存在", null);
            }
        });
    }

    private Mono<String> userEntrySystem(User user){
        Long userId = user.getId();
        return userEntryTool.login(userId)
                .flatMap(result -> {
                    if (result.getData()) {
                        return safeTool.getUnique(userId)
                                .map(unique -> jwtUtils.generateToken(userId, user.getName(), unique));
                    } else {
                        return Mono.just("");
                    }
                });
    }

    private Mono<Boolean> verifyCode(String email, String code) {
        return redisTemplate.opsForValue().get("code:" + email)
                .map(storedCode -> code != null && code.equals(storedCode))
                .defaultIfEmpty(false);
    }
}
