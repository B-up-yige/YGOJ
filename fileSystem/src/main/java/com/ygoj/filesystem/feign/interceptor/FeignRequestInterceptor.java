package com.ygoj.filesystem.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * Feign 请求拦截器，传递 JWT Token 到下游服务
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 1. 从 RequestContextHolder 获取当前 HTTP 请求属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 2. 获取原始请求的所有头信息
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = request.getHeader(name);

                    // 3. 将指定头信息复制到 Feign 请求模板
                    if (isRequiredHeader(name)) {
                        template.header(name, value);
                    }
                }
            }
        }
    }

    /**
     * 判断该请求头是否需要传递到下游
     * @param headerName 请求头名称
     * @return boolean
     */
    private boolean isRequiredHeader(String headerName) {
        return "authorization".equalsIgnoreCase(headerName);
    }
}
