package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.config.FastMethodConfig;
import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.repository.UserDetailRepository;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserDetailUpdateService;
import com.yachiyo.UserService.utils.AsyncFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailUpdateServiceImpl implements UserDetailUpdateService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private FastMethodConfig fastMethodConfig;

    /**
     * 内部同步调用 → 仍然返回 Result<Boolean>
     */
    @Override
    public Result<Boolean> initUserDetail(Long id) {
        try {
            // 阻塞取结果（内部同步调用允许）
            UserDetail userDetail = new UserDetail();
            userDetail.setUserId(id);
            AsyncFileUtil.createDirectory(id.toString()).block();
            userDetailRepository.save(userDetail).block();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("500", e.getMessage(), "初始化用户详情失败");
        }
    }

    @Override
    public Result<Boolean> login(Long id) {
        try {
            fastMethodConfig.getBirthday(id);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("500",null, e.getMessage());
        }
    }
}
