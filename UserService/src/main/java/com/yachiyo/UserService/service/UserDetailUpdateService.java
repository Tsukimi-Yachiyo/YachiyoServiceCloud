package com.yachiyo.UserService.service;

import com.yachiyo.UserService.result.Result;

public interface UserDetailUpdateService {

    Result<Boolean> initUserDetail(Long id);

    Result<Boolean> login(Long id);
}
