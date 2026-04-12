package com.yachiyo.CoinService.service.impl;

import com.yachiyo.CoinService.dto.CoinChangeRequest;
import com.yachiyo.CoinService.result.Result;
import com.yachiyo.CoinService.service.CoinService;
import com.yachiyo.CoinService.utils.CoinUtils;
import com.yachiyo.CoinService.utils.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CoinServiceImpl implements CoinService {
    @Autowired
    private CoinUtils coinUtils;

    @Override
    public Result<Boolean> changeCoin(CoinChangeRequest request) {
        try {
            String businessType = request.getType().name();
            Double amount = request.getAmount();
            Long userId = getCurrentUserId();
            // 双向交易需要额外处理付款方
            if (request.getType() == TradeType.TIP) {
                // 当前用户必须是付款方，并且不能给自己打赏
                if (userId.equals(request.getFromUserId()) && !userId.equals(request.getToUserId())) {
                    // 打赏金额必须为正数
                    // 扣款
                    amount = Math.max(amount, 0);
                    coinUtils.changeCoin(request.getFromUserId(), - amount, businessType);
                } else {
                    return Result.error("400", businessType, "打赏交易只能对其他用户进行操作");
                }
            }
            // 收款
            coinUtils.changeCoin(request.getToUserId(), amount, businessType);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("400", "更改用户余额失败", e.getMessage());
        }
    }

    @Override
    public Result<Integer> getCoin() {
        try {
            return Result.success(coinUtils.getCoin(getCurrentUserId()));
        } catch (Exception e) {
            return Result.error("401", "获取余额失败", e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return (Long.valueOf((String) authentication.getPrincipal()));
        } else {
            throw new IllegalStateException("未获取到登录用户信息");
        }
    }
}
