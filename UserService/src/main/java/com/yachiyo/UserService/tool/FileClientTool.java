package com.yachiyo.UserService.tool;

import com.yachiyo.UserService.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Component @Slf4j
public class FileClientTool {

    // 将阻塞的 Feign 调用包装为响应式 Mono，并自动解包 Result
    public <T> Mono<T> callFileClient(Supplier<Result<T>> feignCall, String errorMsg) {
        return Mono.fromCallable(feignCall::get)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    if (String.valueOf(HttpStatus.OK.value()).equals(result.getCode())) {
                        return Mono.justOrEmpty(result.getData());
                    } else {
                        log.error("{}: code={}, message={}", errorMsg, result.getCode(), result.getMessage());
                        return Mono.error(new RuntimeException(errorMsg + ": " + result.getMessage()));
                    }
                });
    }
}
