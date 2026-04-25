package com.yachiyo.UserService.config;

import com.github.kwfilter.util.KeyWordFilter;
import com.yachiyo.UserService.result.Result;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Configuration @Slf4j
public class FastMethodConfig {

    @Bean
    public KeyWordFilter keyWordFilter() {
        return KeyWordFilter.getInstance();
    }

    @Bean
    public SpringFormEncoder feignFormEncoder() {
        return new SpringFormEncoder();
    }

    // 将阻塞的 Feign 调用包装为响应式 Mono，并自动解包 Result
    public <T> Mono<T> callFileClient(Supplier<Result<T>> feignCall, String errorMsg) {
        return Mono.fromCallable(feignCall::get)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    if (String.valueOf(HttpStatus.OK.value()).equals(result.getCode()) && result.getData() != null) {
                        return Mono.just(result.getData());
                    } else {
                        log.error("{}: code={}, message={}", errorMsg, result.getCode(), result.getMessage());
                        return Mono.error(new RuntimeException(errorMsg + ": " + result.getMessage()));
                    }
                });
    }
}
