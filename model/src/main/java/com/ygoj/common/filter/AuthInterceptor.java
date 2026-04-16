package com.ygoj.common.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限拦截器，验证用户是否有访问接口的权限
 * 
 * @author xushangyi
 * @date 2026/02/11 12:18
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        try {
            return checkPermission(request, response, handler);
        } catch (Exception e) {
            log.error("权限验证异常", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限验证失败\"}");
            return false;
        }
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler) throws Exception {
        // 跳过非控制器请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查是否有权限注解（方法级别或类级别）
        Permission permission = getPermissionAnnotation(method, handlerMethod);
        if (permission == null) {
            return true; // 没有权限注解，放行
        }

        // 检查是否需要登录
        if (!permission.requireLogin()) {
            return true; // 不需要登录，放行
        }

        // 获取并验证Token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("未提供认证令牌, uri: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        // 解析JWT
        JWT jwt;
        try {
            jwt = JWTUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        // 根据权限类型进行验证
        return validatePermission(jwt, permission, response);
    }

    /**
     * 获取权限注解（优先方法级别，其次类级别）
     */
    private Permission getPermissionAnnotation(Method method, HandlerMethod handlerMethod) {
        // 先检查方法级别
        if (method.isAnnotationPresent(Permission.class)) {
            return method.getAnnotation(Permission.class);
        }
        
        // 再检查类级别
        Class<?> clazz = handlerMethod.getBeanType();
        if (clazz.isAnnotationPresent(Permission.class)) {
            return clazz.getAnnotation(Permission.class);
        }
        
        return null;
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        // 优先从 Authorization header 获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            // 支持 "Bearer <token>" 格式
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }
        
        // 兼容旧的 JWT header
        String jwtHeader = request.getHeader("JWT");
        if (jwtHeader != null && !jwtHeader.isEmpty() && !"NULL".equals(jwtHeader)) {
            return jwtHeader;
        }
        
        return null;
    }

    /**
     * 验证权限
     */
    private boolean validatePermission(JWT jwt, Permission permission, HttpServletResponse response) {
        Permission.PermissionType type = permission.type();
        String value = permission.value();
        String[] extra = permission.extra();
        Permission.Logical logical = permission.logical();
        
        switch (type) {
            case ROLE:
                return validateRolePermission(jwt, value, extra, logical, permission.message(), response);
            case BIT:
                return validateBitPermission(jwt, value, extra, logical, permission.message(), response);
            case CUSTOM:
                return validateCustomPermission(jwt, value, extra, logical, permission.message(), response);
            default:
                log.warn("未知的权限类型: {}", type);
                return false;
        }
    }

    /**
     * 验证角色权限
     */
    private boolean validateRolePermission(JWT jwt, String role, String[] extraRoles, 
                                          Permission.Logical logical, String message,
                                          HttpServletResponse response) {
        // 从JWT中获取用户角色
        Object roleObj = jwt.getPayload("role");
        if (roleObj == null) {
            log.warn("JWT中未包含角色信息");
            sendForbiddenResponse(response, message);
            return false;
        }
        
        String userRole = roleObj.toString();
        
        // 收集所有需要验证的角色
        Set<String> requiredRoles = new HashSet<>();
        if (role != null && !role.isEmpty()) {
            requiredRoles.add(role);
        }
        if (extraRoles != null) {
            requiredRoles.addAll(Arrays.asList(extraRoles));
        }
        
        if (requiredRoles.isEmpty()) {
            return true; // 没有指定角色，放行
        }
        
        boolean hasPermission;
        if (logical == Permission.Logical.OR) {
            // OR逻辑：满足任一角色即可
            hasPermission = requiredRoles.contains(userRole);
        } else {
            // AND逻辑：需要同时拥有所有角色（通常只用一个角色）
            hasPermission = requiredRoles.contains(userRole);
        }
        
        if (!hasPermission) {
            log.warn("角色权限验证失败, 用户角色: {}, 需要角色: {}", userRole, requiredRoles);
            sendForbiddenResponse(response, message);
            return false;
        }
        
        return true;
    }

    /**
     * 验证位运算权限
     */
    private boolean validateBitPermission(JWT jwt, String bitIndex, String[] extraBits,
                                         Permission.Logical logical, String message,
                                         HttpServletResponse response) {
        // 从JWT中获取用户权限值
        Object permObj = jwt.getPayload("permission");
        if (permObj == null) {
            log.warn("JWT中未包含权限信息");
            sendForbiddenResponse(response, message);
            return false;
        }
        
        long userPermission;
        try {
            userPermission = Long.parseLong(permObj.toString());
        } catch (NumberFormatException e) {
            log.error("权限值格式错误: {}", permObj);
            sendForbiddenResponse(response, message);
            return false;
        }
        
        // 收集所有需要验证的权限位
        Set<Integer> requiredBits = new HashSet<>();
        if (bitIndex != null && !bitIndex.isEmpty()) {
            try {
                requiredBits.add(Integer.parseInt(bitIndex));
            } catch (NumberFormatException e) {
                log.error("权限位索引格式错误: {}", bitIndex);
                sendForbiddenResponse(response, message);
                return false;
            }
        }
        if (extraBits != null) {
            for (String bit : extraBits) {
                try {
                    requiredBits.add(Integer.parseInt(bit));
                } catch (NumberFormatException e) {
                    log.error("权限位索引格式错误: {}", bit);
                    sendForbiddenResponse(response, message);
                    return false;
                }
            }
        }
        
        if (requiredBits.isEmpty()) {
            return true; // 没有指定位，放行
        }
        
        boolean hasPermission;
        if (logical == Permission.Logical.OR) {
            // OR逻辑：满足任一权限位即可
            hasPermission = requiredBits.stream()
                .anyMatch(bit -> (userPermission & (1L << bit)) != 0);
        } else {
            // AND逻辑：需要拥有所有权限位
            hasPermission = requiredBits.stream()
                .allMatch(bit -> (userPermission & (1L << bit)) != 0);
        }
        
        if (!hasPermission) {
            log.warn("位运算权限验证失败, 用户权限: {}, 需要权限位: {}", userPermission, requiredBits);
            sendForbiddenResponse(response, message);
            return false;
        }
        
        return true;
    }

    /**
     * 验证自定义权限
     */
    private boolean validateCustomPermission(JWT jwt, String permission, String[] extraPermissions,
                                            Permission.Logical logical, String message,
                                            HttpServletResponse response) {
        // 从JWT中获取用户权限列表
        Object permsObj = jwt.getPayload("permissions");
        if (permsObj == null) {
            log.warn("JWT中未包含自定义权限信息");
            sendForbiddenResponse(response, message);
            return false;
        }
        
        // 假设permissions是一个逗号分隔的字符串
        String userPermissions = permsObj.toString();
        Set<String> userPermSet = new HashSet<>(Arrays.asList(userPermissions.split(",")));
        
        // 收集所有需要验证的权限
        Set<String> requiredPerms = new HashSet<>();
        if (permission != null && !permission.isEmpty()) {
            requiredPerms.add(permission);
        }
        if (extraPermissions != null) {
            requiredPerms.addAll(Arrays.asList(extraPermissions));
        }
        
        if (requiredPerms.isEmpty()) {
            return true; // 没有指定权限，放行
        }
        
        boolean hasPermission;
        if (logical == Permission.Logical.OR) {
            // OR逻辑：满足任一权限即可
            hasPermission = requiredPerms.stream()
                .anyMatch(userPermSet::contains);
        } else {
            // AND逻辑：需要拥有所有权限
            hasPermission = userPermSet.containsAll(requiredPerms);
        }
        
        if (!hasPermission) {
            log.warn("自定义权限验证失败, 用户权限: {}, 需要权限: {}", userPermSet, requiredPerms);
            sendForbiddenResponse(response, message);
            return false;
        }
        
        return true;
    }

    /**
     * 发送403响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\"}");
        } catch (Exception e) {
            log.error("发送响应失败", e);
        }
    }
}
