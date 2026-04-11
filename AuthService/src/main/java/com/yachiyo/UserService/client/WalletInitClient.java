package com.yachiyo.UserService.client;

import com.yachiyo.UserService.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "wallet-service")
public interface WalletInitClient {

    @GetMapping("/internal/wallet/init/{id}")
    Result<Boolean> initWallet(@PathVariable Long id);
}
