package com.ygoj.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RtGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        Long startTime = System.currentTimeMillis();
        log.info("请求【{}】开始，时间：{}", uri, startTime);

        Mono<Void> filter =  chain.filter(exchange)
                .doFinally((result)->{
                    Long endTime = System.currentTimeMillis();
                    log.info("请求【{}】结束，时间：{}，耗时：{}ms", uri, endTime, endTime - startTime);
                });

        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
