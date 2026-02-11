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
//        ServerHttpRequest request = exchange.getRequest();
//        ServerHttpResponse response = exchange.getResponse();
//        String uri = request.getURI().toString();
//        Long startTime = System.currentTimeMillis();
//        log.info("请求【{}】开始，时间：{}", uri, startTime);

//        Mono<Void> filter =  chain.filter(exchange)
//                .doFinally((result)->{
//                    Long endTime = System.currentTimeMillis();
//                    log.info("请求【{}】结束，时间：{}，耗时：{}ms", uri, endTime, endTime - startTime);
//                });

//        return filter;

//        redisTemplate.opsForValue().set("1",
//                "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJwZXJtaXNzaW9uIjoxfQ.1R-hKI4U1KNavoey_Ug-1yGKwelhlflDlvyYF9PnBlbVmou2u0LV7mTTKYqYSM_DtsEGDeDkgxSQaMn589C-aA");

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取token
        String token = request.getHeaders().getFirst("Authorization");
        String jwt = "NULL";

        //在redis获取token储存的jwt信息
        //同时token续期
        if(token != null) {
            if (redisTemplate.hasKey(token)) {
                jwt = (String) redisTemplate.opsForValue().getAndExpire(token, 7, TimeUnit.DAYS);
            }
        }

        //更新请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("JWT", jwt)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        Mono<Void> filter =  chain.filter(mutatedExchange);

        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
