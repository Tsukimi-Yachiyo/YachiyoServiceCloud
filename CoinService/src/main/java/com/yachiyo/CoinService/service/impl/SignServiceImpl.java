package com.yachiyo.CoinService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yachiyo.CoinService.dto.CoinChangeRequest;
import com.yachiyo.CoinService.entity.CoinLog;
import com.yachiyo.CoinService.mapper.CoinLogMapper;
import com.yachiyo.CoinService.result.Result;
import com.yachiyo.CoinService.service.CoinService;
import com.yachiyo.CoinService.service.SignService;
import com.yachiyo.CoinService.utils.CurrentUserIdProvider;
import com.yachiyo.CoinService.utils.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SignServiceImpl implements SignService {
    @Autowired
    private CoinLogMapper coinLogMapper;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CurrentUserIdProvider currentUserIdProvider;

    @Override
    public Result<Boolean> signIn() {
        // 从安全上下文获取当前用户id
        Long userId = currentUserIdProvider.getCurrentUserId();

        // 从数据库中获取用户详情
//        UserDetail userDetail = userDetailMapper.selectById(userId);
//        if (userDetail == null) {
//            return Result.error("404", "用户不存在"+userId);
//        }

        if (getSignCountForToday(userId) > 0) {
            return Result.error("400", "今日已签到");
        }
        // 签到成功
        CoinChangeRequest coinChangeRequest = new CoinChangeRequest();
        coinChangeRequest.setToUserId(userId);
        coinChangeRequest.setType(TradeType.CHECKIN);
        coinChangeRequest.setAmount(8.0);
        return coinService.changeCoin(coinChangeRequest);
    }

    @Override
    public Result<Boolean> getSignInStatus() {
        return Result.success(getSignCountForToday(currentUserIdProvider.getCurrentUserId()) > 0);
    }

    private Long getSignCountForToday(Long userId) {
        QueryWrapper<CoinLog> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .eq("business_type", TradeType.CHECKIN.name())
                .eq("create_time", LocalDate.now());
        return coinLogMapper.selectCount(qw);
    }
}
