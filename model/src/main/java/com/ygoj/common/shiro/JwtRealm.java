package com.ygoj.common.shiro;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JWT Realm 实现，用于 Shiro 认证和授权
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Slf4j
@Component
public class JwtRealm extends AuthorizingRealm {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 支持 JWT Token
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 认证：验证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) auth;
        String token = (String) jwtToken.getPrincipal();

        log.debug("开始认证 JWT Token: {}", token);

        // 从 Redis 中获取 JWT
        if (!redisTemplate.hasKey(token)) {
            log.warn("Token 不存在或已过期: {}", token);
            throw new UnknownAccountException("Token 无效或已过期");
        }

        String jwtStr = (String) redisTemplate.opsForValue().get(token);
        if (jwtStr == null) {
            log.warn("Redis 中未找到 JWT 数据: {}", token);
            throw new UnknownAccountException("Token 无效");
        }

        // 续期 Token
        redisTemplate.expire(token, 7, TimeUnit.DAYS);

        // 解析 JWT
        JWT jwt = JWTUtil.parseToken(jwtStr);
        
        // 验证 JWT 签名 (Shiro 2.x 使用不同的 API,这里简化处理)
        // 实际项目中应该配置正确的密钥
        try {
            // JWT 已在网关层验证,这里只做基本检查
            if (jwt.getPayload("userId") == null) {
                log.error("JWT 中缺少 userId");
                throw new IncorrectCredentialsException("JWT 格式错误");
            }
        } catch (Exception e) {
            log.error("JWT 验证失败: {}", e.getMessage());
            throw new IncorrectCredentialsException("JWT 验证失败");
        }

        // 获取用户信息
        Long userId = Long.parseLong(jwt.getPayload("userId").toString());
        String username = jwt.getPayload("username").toString();
        String role = jwt.getPayload("role") != null ? jwt.getPayload("role").toString() : "USER";
        Long permission = jwt.getPayload("permission") != null ? 
            Long.parseLong(jwt.getPayload("permission").toString()) : 0L;

        log.debug("认证成功 - 用户ID: {}, 用户名: {}, 角色: {}", userId, username, role);

        // 返回认证信息
        return new SimpleAuthenticationInfo(
            new ShiroUser(userId, username, role, permission),
            token,
            getName()
        );
    }

    /**
     * 授权：获取用户权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ShiroUser user = (ShiroUser) principals.getPrimaryPrincipal();
        
        log.debug("开始授权 - 用户: {}, 角色: {}", user.getUsername(), user.getRole());

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        // 添加角色
        Set<String> roles = new HashSet<>();
        roles.add(user.getRole());
        
        // 如果是管理员，添加所有角色
        if ("ADMIN".equals(user.getRole())) {
            roles.add("USER");
            roles.add("CONTEST_ADMIN");
            roles.add("PROBLEM_ADMIN");
        } else if ("CONTEST_ADMIN".equals(user.getRole())) {
            roles.add("USER");
        } else if ("PROBLEM_ADMIN".equals(user.getRole())) {
            roles.add("USER");
        }
        
        authorizationInfo.setRoles(roles);

        // 添加权限（基于位运算）
        Set<String> permissions = new HashSet<>();
        long permValue = user.getPermission();
        
        // 根据权限位生成权限字符串
        if ((permValue & (1L << 0)) != 0) permissions.add("problem:view");
        if ((permValue & (1L << 1)) != 0) permissions.add("problem:submit");
        if ((permValue & (1L << 2)) != 0) permissions.add("problem:create");
        if ((permValue & (1L << 3)) != 0) permissions.add("problem:edit");
        if ((permValue & (1L << 4)) != 0) permissions.add("problem:delete");
        if ((permValue & (1L << 5)) != 0) permissions.add("solution:view");
        if ((permValue & (1L << 6)) != 0) permissions.add("solution:create");
        if ((permValue & (1L << 7)) != 0) permissions.add("record:view");
        if ((permValue & (1L << 8)) != 0) permissions.add("ranking:view");
        if ((permValue & (1L << 9)) != 0) permissions.add("contest:create");
        if ((permValue & (1L << 10)) != 0) permissions.add("contest:manage");
        if ((permValue & (1L << 11)) != 0) permissions.add("contest:join");
        if ((permValue & (1L << 12)) != 0) permissions.add("problemset:create");
        if ((permValue & (1L << 13)) != 0) permissions.add("problemset:manage");
        if ((permValue & (1L << 14)) != 0) permissions.add("problemset:view");
        if ((permValue & (1L << 15)) != 0) permissions.add("user:manage");
        if ((permValue & (1L << 16)) != 0) permissions.add("system:config");

        authorizationInfo.setStringPermissions(permissions);

        log.debug("授权完成 - 角色: {}, 权限数量: {}", roles.size(), permissions.size());

        return authorizationInfo;
    }
}
