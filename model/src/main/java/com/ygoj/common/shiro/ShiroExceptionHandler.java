package com.ygoj.common.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Shiro 全局异常处理器
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Slf4j
@RestControllerAdvice
public class ShiroExceptionHandler {

    /**
     * 处理未认证异常
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public Map<String, Object> handleUnauthenticatedException(UnauthenticatedException e) {
        log.warn("用户未认证: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put("message", "请先登录");
        response.put("data", null);
        
        return response;
    }

    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Map<String, Object> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("用户未授权: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", "权限不足，无法访问该资源");
        response.put("data", null);
        
        return response;
    }

    /**
     * 处理其他 Shiro 异常
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleShiroException(Exception e) {
        log.error("Shiro 异常: ", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "系统错误: " + e.getMessage());
        response.put("data", null);
        
        return response;
    }
}
