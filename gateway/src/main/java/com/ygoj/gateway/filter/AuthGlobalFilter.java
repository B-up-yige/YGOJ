package com.ygoj.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        
        //获取token
        String token = request.getHeaders().getFirst("Authorization");
        String jwt = "NULL";

        log.debug("网关鉴权请求, uri: {}, token: {}", uri, token != null ? "exists" : "null");

        //在redis获取token储存的jwt信息
        //同时token续期
        if(token != null) {
            if (redisTemplate.hasKey(token)) {
                jwt = (String) redisTemplate.opsForValue().getAndExpire(token, 7, TimeUnit.DAYS);
                log.debug("Token验证成功并续期, token: {}", token);
            } else {
                log.warn("Token不存在或已过期, token: {}", token);
            }
        }

        //更新请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("JWT", jwt)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        log.debug("转发请求到下游服务, uri: {}", uri);
        Mono<Void> filter = chain.filter(mutatedExchange);

        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
