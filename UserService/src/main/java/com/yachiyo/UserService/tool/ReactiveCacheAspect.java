package com.yachiyo.UserService.tool;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 响应式缓存切面：支持 Mono/Flux 的缓存与失效
 */
@Aspect
@Component
public class ReactiveCacheAspect {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public ReactiveCacheAspect(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --- 1. 处理 @ReactiveCacheable (获取或设置缓存) ---

    @Around("@annotation(reactiveCacheable)")
    public Object aroundCache(ProceedingJoinPoint point, ReactiveCacheable reactiveCacheable) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> returnType = signature.getReturnType();

        boolean isMono = Mono.class.isAssignableFrom(returnType);
        boolean isFlux = Flux.class.isAssignableFrom(returnType);

        if (!isMono && !isFlux) {
            try {
                return point.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        String cacheKey = getRealKey(point, signature, reactiveCacheable.cacheName(), reactiveCacheable.key());

        if (isMono) {
            return handleMono(point, cacheKey, reactiveCacheable);
        } else {
            return handleFlux(point, cacheKey, reactiveCacheable);
        }
    }

    private Mono<?> handleMono(ProceedingJoinPoint point, String key, ReactiveCacheable anno) {
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(Mono.defer(() -> {
                    try {
                        return (Mono<?>) point.proceed();
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                }).flatMap(data ->
                        redisTemplate.opsForValue()
                                .set(key, data, Duration.ofSeconds(anno.ttl()))
                                .thenReturn(data)
                ));
    }

    private Flux<?> handleFlux(ProceedingJoinPoint point, String key, ReactiveCacheable anno) {
        return redisTemplate.opsForValue().get(key)
                .flatMapMany(data -> {
                    if (data instanceof List) {
                        return Flux.fromIterable((List<?>) data);
                    }
                    return Flux.just(data);
                })
                .switchIfEmpty(Flux.defer(() -> {
                    try {
                        return ((Flux<?>) point.proceed()).collectList().flatMapMany(list -> {
                            if (list.isEmpty()) return Flux.empty();
                            return redisTemplate.opsForValue()
                                    .set(key, list, Duration.ofSeconds(anno.ttl()))
                                    .thenReturn(list)
                                    .flatMapMany(Flux::fromIterable);
                        });
                    } catch (Throwable e) {
                        return Flux.error(e);
                    }
                }));
    }

    // --- 2. 处理 @ReactiveCacheEvict (删除缓存) ---

    @Around("@annotation(ReactiveCacheEvict) || @annotation(ReactiveCacheEvicts)")
    public Object aroundEvict(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();

        // 获取所有的 Evict 注解（兼容单用和多用）
        ReactiveCacheEvict[] evicts = signature.getMethod().getAnnotationsByType(ReactiveCacheEvict.class);

        Object result;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            return Mono.error(e);
        }

        if (result instanceof Mono) {
            return ((Mono<?>) result).delayUntil(data -> {
                // 批量生成删除操作
                List<Mono<Long>> deleteOps = Arrays.stream(evicts).map(evict -> {
                    String cacheKey = getRealKey(point, signature, evict.cacheName(), evict.key());
                    return redisTemplate.delete(cacheKey)
                            .doOnSuccess(v -> System.out.println("清理缓存: " + cacheKey));
                }).collect(Collectors.toList());

                // 并行执行所有删除
                return Flux.merge(deleteOps).then();
            });
        }

        return result;
    }

    // --- 3. 公共工具方法：解析 SpEL 生成 Key ---

    private String getRealKey(ProceedingJoinPoint point, MethodSignature signature, String cacheName, String keyExpression) {
        if (keyExpression.isEmpty()) {
            Object[] args = point.getArgs();
            return "cache:" + cacheName + ":" + (args.length > 0 ? args[0] : "default");
        }

        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = point.getArgs();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return "cache:" + cacheName + ":" + Objects.requireNonNull(expression.getValue(context));
    }
}