package com.yachiyo.CoinService.controller;

import com.yachiyo.CoinService.dto.CoinChangeRequest;
import com.yachiyo.CoinService.result.Result;
import com.yachiyo.CoinService.service.CoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coin-change")
@RequiredArgsConstructor
@Validated
public class CoinChangeController {
    private final CoinService coinChangeService;

    /**
     * 金币交易
     * @param coinChangeRequest 金币交易请求
     * @return 金币交易结果
     */
    @PostMapping("/change")
    public Result<Boolean> changeCoin(@RequestBody @Valid CoinChangeRequest coinChangeRequest) {
        return coinChangeService.changeCoin(coinChangeRequest);
    }

    /**
     * 获取金币
     * @return 金币
     */
    @PostMapping("/get")
    public Result<Integer> getCoin() {
        return coinChangeService.getCoin();
    }
}
