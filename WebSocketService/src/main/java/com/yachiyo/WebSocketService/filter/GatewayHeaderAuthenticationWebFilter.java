package com.yachiyo.WebSocketService.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayHeaderAuthenticationWebFilter implements WebFilter {

    @Override @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String requestPath = exchange.getRequest().getPath().value();

        if (!requestPath.startsWith("/ws")) {
            return chain.filter(exchange);
        }

        String userId = headers.getFirst("X-User-Id");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(Objects.requireNonNull(userId), null, null);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}